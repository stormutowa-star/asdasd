extends Node
## Prueba de la caché de imágenes: precarga de los mazos en pics/ y descarga al pasar el cursor.
## Requiere un servidor de imágenes: YGO_PICS_URL=http://127.0.0.1:8765/%d.jpg
## Uso: godot --headless --path game res://tests/pics_test.tscn


func _ready() -> void:
	var codes := {}
	for path in CardDB.deck_paths():
		var d := CardDB.load_deck(path)
		for c in d.main + d.extra + d.side:
			codes[c] = true
	for c in codes:
		DirAccess.remove_absolute(Paths.pic_path(c))
	print("Mazos en ", Paths.decks_dir, ": ", CardDB.deck_paths().size(), " · cartas distintas: ", codes.size())
	var missing := CardImages.preload_decks()
	print("Faltan al iniciar: ", missing)
	var res: Array = await CardImages.preload_finished
	var have := 0
	for c in codes:
		if FileAccess.file_exists(Paths.pic_path(c)):
			have += 1
	print("Precarga: descargadas=%d fallidas=%d · en pics/: %d/%d" % [res[0], res[1], have, codes.size()])
	var ok := have == codes.size()
	# Hover: se borra una imagen y se simula pasar el cursor por la carta
	var code: int = codes.keys()[0]
	DirAccess.remove_absolute(Paths.pic_path(code))
	var view := CardView.new()
	view.setup(code, {}, true)
	add_child(view)
	var board_like := func(v: CardView): CardImages.ensure(v.code, true)
	view.hovered.connect(board_like)
	view.hovered.emit(view)
	var got: int = await CardImages.texture_ready
	var hover_ok := got == code and FileAccess.file_exists(Paths.pic_path(code))
	print("Hover: imagen %d descargada=%s · arte pixelado=%s" % [code, hover_ok, CardImages.get_art_texture(code) != null])
	for c in codes:
		DirAccess.remove_absolute(Paths.pic_path(c))
	print("RESULTADO: ", "OK" if ok and hover_ok else "FALLO")
	get_tree().quit(0 if ok and hover_ok else 1)
