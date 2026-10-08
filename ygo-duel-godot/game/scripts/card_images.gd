extends Node
## Autoload "CardImages": imágenes de cartas en la carpeta pics/ (como EDOPro).
## Las descargas se hacen en un hilo propio (HTTPClient bloqueante), sin frenar el juego:
##  - al iniciar se encolan, en silencio, todas las cartas de los mazos de Decks/ que no estén en pics/;
##  - ensure(code): al pasar el cursor por una carta se pone la primera de la cola si falta.

signal texture_ready(code: int)
signal preload_finished(done: int, failed: int)

const DEFAULT_URL := "https://images.ygoprodeck.com/images/cards/%d.jpg"
const RETRY_COOLDOWN_MS := 15000
const ART_PIXELS := 36 # resolución de la ilustración pixelada (estilo GBA)

var download_enabled := true
## Se puede cambiar con la variable de entorno YGO_PICS_URL (p.ej. un servidor propio o para pruebas)
var image_url := DEFAULT_URL

var _textures := {}
var _arts := {}
var _failed := {} # code -> ticks del último fallo (hilo principal)

# Compartido con el hilo de descargas (protegido por _mutex)
var _mutex := Mutex.new()
var _sem := Semaphore.new()
var _queue: Array[int] = []
var _queued := {}
var _exit := false
var _thread: Thread
var _alias := {} # code -> alias (copiado de CardDB para no tocarlo desde el hilo)

var _preload_pending := {}
var _preload_done := 0
var _preload_failed := 0


func _ready() -> void:
	var env := OS.get_environment("YGO_PICS_URL")
	if env != "":
		image_url = env
	_thread = Thread.new()
	_thread.start(_worker)
	preload_decks.call_deferred()


func _exit_tree() -> void:
	_mutex.lock()
	_exit = true
	_mutex.unlock()
	_sem.post()
	if _thread and _thread.is_started():
		_thread.wait_to_finish()


func has_image(code: int) -> bool:
	return code > 0 and (FileAccess.file_exists(Paths.pic_path(code)) or FileAccess.file_exists("res://pics/%d.jpg" % code))


## Comprueba si la imagen está en pics/ y si no la encola (al principio si `priority`).
func ensure(code: int, priority := true) -> void:
	if code <= 0 or not download_enabled or has_image(code):
		return
	if _failed.has(code) and Time.get_ticks_msec() - int(_failed[code]) < RETRY_COOLDOWN_MS:
		return
	_failed.erase(code)
	var alias := int(CardDB.get_card(code).get("alias", 0))
	_mutex.lock()
	_alias[code] = alias
	var already: bool = _queued.has(code)
	if already:
		if priority:
			_queue.erase(code)
			_queue.push_front(code)
	else:
		_queued[code] = true
		if priority:
			_queue.push_front(code)
		else:
			_queue.append(code)
	_mutex.unlock()
	if not already:
		_sem.post()


func get_texture(code: int) -> Texture2D:
	if code <= 0:
		return null
	if _textures.has(code):
		return _textures[code]
	var img := _load_image(code)
	if img == null:
		ensure(code, false)
		return null
	var tex := ImageTexture.create_from_image(img)
	_textures[code] = tex
	return tex


## Ilustración recortada y reducida a pocos píxeles para el campo (estilo GBA).
func get_art_texture(code: int) -> Texture2D:
	if code <= 0:
		return null
	if _arts.has(code):
		return _arts[code]
	var img := _load_image(code)
	if img == null:
		ensure(code, false)
		return null
	var w := img.get_width()
	var h := img.get_height()
	var rect := Rect2i(int(w * 0.12), int(h * 0.185), int(w * 0.76), int(w * 0.76))
	if rect.end.y > h:
		rect.size.y = h - rect.position.y
	var art := img.get_region(rect)
	art.resize(ART_PIXELS, ART_PIXELS, Image.INTERPOLATE_LANCZOS)
	var tex := ImageTexture.create_from_image(art)
	_arts[code] = tex
	return tex


func _load_image(code: int) -> Image:
	for path in [Paths.pic_path(code), "res://pics/%d.jpg" % code, "res://pics/%d.png" % code]:
		if FileAccess.file_exists(path):
			var img := Image.new()
			var bytes := FileAccess.get_file_as_bytes(path)
			if img.load_jpg_from_buffer(bytes) == OK or img.load_png_from_buffer(bytes) == OK:
				return img
	return null


## Encola en segundo plano las imágenes de todas las cartas de los mazos de Decks/ que falten.
func preload_decks() -> int:
	var codes := {}
	for path in Paths.deck_files():
		var deck: Dictionary = CardDB.load_deck(path)
		for section in ["main", "extra", "side"]:
			for c in deck[section]:
				codes[int(c)] = true
	_preload_pending.clear()
	_preload_done = 0
	_preload_failed = 0
	for c in codes:
		if not has_image(c):
			_preload_pending[c] = true
	if _preload_pending.is_empty() or not download_enabled:
		preload_finished.emit.call_deferred(0, 0)
		return 0
	for c in _preload_pending:
		ensure(c, false)
	return _preload_pending.size()


# ================================================================ hilo de descargas

func _worker() -> void:
	var client := HTTPClient.new()
	var host := [""] # host:puerto al que está conectado el cliente
	while true:
		_sem.wait()
		_mutex.lock()
		if _exit:
			_mutex.unlock()
			break
		if _queue.is_empty():
			_mutex.unlock()
			continue
		var code: int = _queue.pop_front()
		var alias: int = _alias.get(code, 0)
		var url := image_url
		_mutex.unlock()
		var ok := false
		if FileAccess.file_exists(Paths.pic_path(code)):
			ok = true
		else:
			var res := _fetch(client, host, url % code)
			if res[0] == 404 and alias > 0:
				res = _fetch(client, host, url % alias)
			if res[0] == 200:
				var body: PackedByteArray = res[1]
				var img := Image.new()
				if body.size() > 0 and (img.load_jpg_from_buffer(body) == OK or img.load_png_from_buffer(body) == OK):
					var tmp := Paths.pic_path(code) + ".part"
					var f := FileAccess.open(tmp, FileAccess.WRITE)
					if f:
						f.store_buffer(body)
						f.close()
						DirAccess.rename_absolute(tmp, Paths.pic_path(code))
						ok = true
		_mutex.lock()
		_queued.erase(code)
		_mutex.unlock()
		_on_downloaded.call_deferred(code, ok)
	client.close()


## GET síncrono (sólo desde el hilo). Devuelve [código HTTP, cuerpo]; 0 si falla la conexión.
## Si una conexión reutilizada estaba cerrada por el servidor, reintenta con una nueva.
func _fetch(client: HTTPClient, host: Array, url: String, redirects := 3) -> Array:
	var reused: bool = host[0] != ""
	var res := _fetch_once(client, host, url, redirects)
	if res[0] == 0 and reused and not _exit:
		client.close()
		host[0] = ""
		res = _fetch_once(client, host, url, redirects)
	return res


func _fetch_once(client: HTTPClient, host: Array, url: String, redirects: int) -> Array:
	var m := RegEx.create_from_string("^(https?)://([^/:]+)(?::(\\d+))?(/.*)?$").search(url)
	if m == null:
		return [0, PackedByteArray()]
	var tls := m.get_string(1) == "https"
	var hostname := m.get_string(2)
	var port := int(m.get_string(3)) if m.get_string(3) != "" else (443 if tls else 80)
	var path := m.get_string(4) if m.get_string(4) != "" else "/"
	var key := "%s:%d" % [hostname, port]
	if host[0] != key or client.get_status() != HTTPClient.STATUS_CONNECTED:
		client.close()
		host[0] = ""
		if client.connect_to_host(hostname, port, TLSOptions.client() if tls else null) != OK:
			return [0, PackedByteArray()]
		var t0 := Time.get_ticks_msec()
		while client.get_status() in [HTTPClient.STATUS_CONNECTING, HTTPClient.STATUS_RESOLVING]:
			client.poll()
			OS.delay_msec(5)
			if Time.get_ticks_msec() - t0 > 15000 or _exit:
				client.close()
				return [0, PackedByteArray()]
		if client.get_status() != HTTPClient.STATUS_CONNECTED:
			client.close()
			return [0, PackedByteArray()]
		host[0] = key
	if client.request(HTTPClient.METHOD_GET, path, ["User-Agent: YGODuel/1.0", "Accept: image/*"]) != OK:
		client.close()
		host[0] = ""
		return [0, PackedByteArray()]
	var t1 := Time.get_ticks_msec()
	while client.get_status() == HTTPClient.STATUS_REQUESTING:
		client.poll()
		OS.delay_msec(2)
		if Time.get_ticks_msec() - t1 > 20000 or _exit:
			client.close()
			host[0] = ""
			return [0, PackedByteArray()]
	if not client.has_response():
		client.close()
		host[0] = ""
		return [0, PackedByteArray()]
	var status := client.get_response_code()
	var headers := client.get_response_headers_as_dictionary()
	var body := PackedByteArray()
	while client.get_status() == HTTPClient.STATUS_BODY:
		client.poll()
		var chunk := client.read_response_body_chunk()
		if chunk.size() == 0:
			OS.delay_msec(2)
		else:
			body.append_array(chunk)
		if Time.get_ticks_msec() - t1 > 30000 or _exit:
			client.close()
			host[0] = ""
			return [0, PackedByteArray()]
	var close := false
	for k in headers:
		if String(k).to_lower() == "connection" and String(headers[k]).to_lower().contains("close"):
			close = true
	if close or client.get_status() != HTTPClient.STATUS_CONNECTED:
		client.close()
		host[0] = ""
	if status in [301, 302, 303, 307, 308] and redirects > 0:
		for k in headers:
			if String(k).to_lower() == "location":
				return _fetch(client, host, headers[k], redirects - 1)
	return [status, body]


## Vuelve al hilo principal cuando termina una descarga.
func _on_downloaded(code: int, ok: bool) -> void:
	if ok:
		_textures.erase(code)
		_arts.erase(code)
		texture_ready.emit(code)
	else:
		_failed[code] = Time.get_ticks_msec()
	if _preload_pending.has(code):
		_preload_pending.erase(code)
		_preload_done += 1
		if not ok:
			_preload_failed += 1
		if _preload_pending.is_empty():
			preload_finished.emit(_preload_done, _preload_failed)
