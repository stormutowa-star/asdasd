extends Node
## Autoload "CardImages": imágenes de cartas guardadas en la carpeta pics/ (como EDOPro).
##  - preload_decks(): al iniciar descarga las que falten de todos los mazos de Decks/.
##  - ensure(code): al pasar el cursor por una carta comprueba si existe y, si no, la descarga con prioridad.
##  - get_texture(code) / get_art_texture(code): carta completa / ilustración recortada y pixelada.

signal texture_ready(code: int)
signal preload_progress(done: int, total: int, failed: int)
signal preload_finished(done: int, failed: int)

const DEFAULT_URL := "https://images.ygoprodeck.com/images/cards/%d.jpg"
const MAX_PARALLEL := 4
const RETRY_COOLDOWN_MS := 15000
const ART_PIXELS := 36 # resolución de la ilustración pixelada (estilo GBA)

var download_enabled := true
## Se puede cambiar con la variable de entorno YGO_PICS_URL (p.ej. un servidor propio o para pruebas)
var image_url := DEFAULT_URL


func _ready() -> void:
	var env := OS.get_environment("YGO_PICS_URL")
	if env != "":
		image_url = env
var _textures := {} # code -> Texture2D (carta completa)
var _arts := {} # code -> Texture2D (ilustración pixelada)
var _failed := {} # code -> ticks del último fallo
var _queue: Array[int] = []
var _in_flight := {}
var _active := 0

var _preload_total := 0
var _preload_done := 0
var _preload_failed := 0
var _preload_pending := {}


func has_image(code: int) -> bool:
	return code > 0 and (FileAccess.file_exists(Paths.pic_path(code)) or FileAccess.file_exists("res://pics/%d.jpg" % code))


## Comprueba si la imagen está en pics/ y si no la pone la primera de la cola.
func ensure(code: int, priority := true) -> void:
	if code <= 0 or not download_enabled or has_image(code) or _in_flight.has(code):
		return
	if _failed.has(code) and Time.get_ticks_msec() - int(_failed[code]) < RETRY_COOLDOWN_MS:
		return
	_failed.erase(code)
	_queue.erase(code)
	if priority:
		_queue.push_front(code)
	else:
		_queue.append(code)
	_pump()


## Textura de la carta completa (o null mientras no esté descargada).
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
	# Zona de la ilustración en una carta estándar (421x614)
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


# ---------------------------------------------------------------- precarga al iniciar

## Descarga en pics/ las imágenes de todas las cartas de los mazos de Decks/ que falten.
func preload_decks() -> int:
	var codes := {}
	for path in Paths.deck_files():
		var deck: Dictionary = CardDB.load_deck(path)
		for section in ["main", "extra", "side"]:
			for c in deck[section]:
				codes[int(c)] = true
	_preload_pending.clear()
	for c in codes:
		if not has_image(c):
			_preload_pending[c] = true
	_preload_total = _preload_pending.size()
	_preload_done = 0
	_preload_failed = 0
	if _preload_total == 0 or not download_enabled:
		preload_finished.emit.call_deferred(0, 0)
		return 0
	for c in _preload_pending:
		ensure(c, false)
	return _preload_total


func _preload_step(code: int, ok: bool) -> void:
	if not _preload_pending.has(code):
		return
	_preload_pending.erase(code)
	_preload_done += 1
	if not ok:
		_preload_failed += 1
	preload_progress.emit(_preload_done, _preload_total, _preload_failed)
	if _preload_pending.is_empty():
		preload_finished.emit(_preload_done, _preload_failed)


# ---------------------------------------------------------------- descargas

func _pump() -> void:
	while _active < MAX_PARALLEL and not _queue.is_empty():
		var code: int = _queue.pop_front()
		if has_image(code):
			_preload_step(code, true)
			continue
		_start_request(code, code)


## `url_code` puede ser el alias (arte original) si el código exacto no existe en el servidor.
func _start_request(code: int, url_code: int) -> void:
	_active += 1
	_in_flight[code] = true
	var http := HTTPRequest.new()
	http.timeout = 25
	add_child(http)
	http.request_completed.connect(_on_done.bind(code, url_code, http))
	if http.request(image_url % url_code) != OK:
		_on_done(HTTPRequest.RESULT_CANT_CONNECT, 0, PackedStringArray(), PackedByteArray(), code, url_code, http)


func _on_done(result: int, status: int, _headers: PackedStringArray, body: PackedByteArray, code: int, url_code: int, http: HTTPRequest) -> void:
	_active -= 1
	_in_flight.erase(code)
	http.queue_free()
	var ok := false
	if result == HTTPRequest.RESULT_SUCCESS and status == 200 and body.size() > 0:
		var img := Image.new()
		if img.load_jpg_from_buffer(body) == OK or img.load_png_from_buffer(body) == OK:
			var f := FileAccess.open(Paths.pic_path(code), FileAccess.WRITE)
			if f:
				f.store_buffer(body)
				f.close()
			_textures.erase(code)
			_arts.erase(code)
			ok = true
	elif status == 404 and url_code == code:
		var alias: int = CardDB.get_card(code).get("alias", 0)
		if alias > 0:
			_start_request(code, alias)
			return
	if ok:
		texture_ready.emit(code)
	else:
		_failed[code] = Time.get_ticks_msec()
	_preload_step(code, ok)
	_pump()
