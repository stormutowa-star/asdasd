extends Node
## Autoload "CardImages": carga perezosa de imágenes de cartas.
## Busca en res://pics, user://pics y si no las encuentra las descarga (como EDOPro) y las guarda en caché.

signal texture_ready(code: int)

const IMAGE_URL := "https://images.ygoprodeck.com/images/cards/%d.jpg"
const MAX_PARALLEL := 3

var download_enabled := true
var _textures := {}
var _failed := {}
var _queue: Array[int] = []
var _active := 0


func _ready() -> void:
	DirAccess.make_dir_recursive_absolute("user://pics")


## Devuelve la textura si está disponible; si no, la pide y emite texture_ready(code) al llegar.
func get_texture(code: int) -> Texture2D:
	if code <= 0:
		return null
	if _textures.has(code):
		return _textures[code]
	for path in ["res://pics/%d.jpg" % code, "res://pics/%d.png" % code, "user://pics/%d.jpg" % code]:
		if FileAccess.file_exists(path):
			var tex := _load_texture(path)
			if tex:
				_textures[code] = tex
				return tex
	if download_enabled and not _failed.has(code) and not _queue.has(code):
		_queue.append(code)
		_pump()
	return null


func _load_texture(path: String) -> Texture2D:
	var img := Image.new()
	if img.load(path) != OK:
		return null
	return ImageTexture.create_from_image(img)


func _pump() -> void:
	while _active < MAX_PARALLEL and not _queue.is_empty():
		var code: int = _queue.pop_front()
		_active += 1
		var http := HTTPRequest.new()
		http.timeout = 20
		add_child(http)
		http.request_completed.connect(_on_done.bind(code, http))
		if http.request(IMAGE_URL % code) != OK:
			_on_done(HTTPRequest.RESULT_CANT_CONNECT, 0, PackedStringArray(), PackedByteArray(), code, http)


func _on_done(result: int, status: int, _headers: PackedStringArray, body: PackedByteArray, code: int, http: HTTPRequest) -> void:
	_active -= 1
	http.queue_free()
	if result == HTTPRequest.RESULT_SUCCESS and status == 200 and body.size() > 0:
		var img := Image.new()
		if img.load_jpg_from_buffer(body) == OK or img.load_png_from_buffer(body) == OK:
			img.save_jpg("user://pics/%d.jpg" % code, 0.9)
			_textures[code] = ImageTexture.create_from_image(img)
			texture_ready.emit(code)
		else:
			_failed[code] = true
	else:
		_failed[code] = true
		# Si no hay red no insistimos con el resto de la cola
		if result != HTTPRequest.RESULT_SUCCESS:
			download_enabled = false
			_queue.clear()
	_pump()
