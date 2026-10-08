extends Node
## Prueba de la interfaz: abre el tablero real y juega el lado humano con la IA,
## guardando capturas (también a mitad de las animaciones). Uso (necesita pantalla, p.ej. xvfb-run):
##   godot --path game res://tests/ui_smoke.tscn -- <carpeta_capturas> [semilla] [fakepics]
## Con "fakepics" genera imágenes de prueba en pics/ (y las borra al terminar) para ver el pixelado sin red.

const AIScript := preload("res://scripts/ai_player.gd")
const SHOT_EVENTS := {
	OCG.MSG_NEW_TURN: 0.25, OCG.MSG_NEW_PHASE: 0.12, OCG.MSG_SUMMONING: 0.12, OCG.MSG_SPSUMMONING: 0.15,
	OCG.MSG_CHAINING: 0.3, OCG.MSG_ATTACK: 0.12, OCG.MSG_BATTLE: 0.45, OCG.MSG_DAMAGE: 0.25, OCG.MSG_DRAW: 0.1,
}

var out_dir := "user://screens"
var board: DuelBoard
var bot
var shots := 0
var seen := {}
var fake_codes: Array[int] = []


func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	if args.size() > 0:
		out_dir = args[0]
	DirAccess.make_dir_recursive_absolute(out_dir)
	if "fakepics" in args:
		_make_fake_pics()
	var decks := CardDB.deck_paths()
	DuelBoard.config = {"deck0": decks[0], "deck1": decks[1], "first": "me", "speed": 0,
			"seed": int(args[1]) if args.size() > 1 else 42}
	CardImages.download_enabled = false
	board = load("res://scenes/duel.tscn").instantiate()
	add_child(board)
	await get_tree().process_frame
	bot = AIScript.new()
	bot.player = board.me
	bot.controller = board.ctrl
	board.ctrl.input_requested.connect(_on_request)
	board.ctrl.duel_finished.connect(_on_finished)
	board.ctrl.message_received.connect(_on_message)
	await get_tree().create_timer(600).timeout
	print("TIMEOUT")
	_cleanup()
	get_tree().quit(1)


func _on_message(msg: Dictionary) -> void:
	if not SHOT_EVENTS.has(msg.type) or seen.get("m%d" % msg.type, 0) >= (2 if msg.type == OCG.MSG_BATTLE else 1):
		return
	seen["m%d" % msg.type] = seen.get("m%d" % msg.type, 0) + 1
	await get_tree().create_timer(SHOT_EVENTS[msg.type]).timeout
	_shot("anim_%d" % msg.type)


func _on_request(msg: Dictionary) -> void:
	await get_tree().process_frame
	await get_tree().process_frame
	var key := "type%d" % msg.type
	if not seen.has(key):
		seen[key] = true
		_shot("req_%d_turn%d" % [msg.type, board.ctrl.turn])
	if msg.type == OCG.MSG_SELECT_IDLECMD and board.ctrl.turn == 3 and not seen.has("hover"):
		seen["hover"] = true
		# Simula pasar el ratón por la primera carta de la mano
		var v = board.views.get(board.key(board.me, OCG.LOCATION_HAND, 0))
		if v:
			board._on_card_hovered(v)
			await get_tree().process_frame
			_shot("hover_hand")
	# Carta propia colocada: oculta hasta pasar el cursor
	if not seen.has("set_hover"):
		for k in board.views:
			var cv = board.views[k]
			if cv is CardView and cv.peek_only:
				seen["set_hover"] = true
				_shot("set_hidden")
				cv.mouse_entered.emit()
				board._on_card_hovered(cv)
				await get_tree().process_frame
				_shot("set_hover")
				if is_instance_valid(cv): cv.mouse_exited.emit()
				break
	var resp: PackedByteArray = bot.decide(msg)
	if not board._fsel.is_empty():
		if not seen.has("fsel"):
			seen["fsel"] = true
			_shot("field_select_%d" % msg.type)
		board.current_req = {}
		board._fsel_done.emit(null)
		board.ctrl.respond(resp)
		return
	if board.dialog.visible:
		board.dialog.visible = false
		board._dialog_busy = false
	board._respond(resp)


func _on_finished(winner: int, _reason: int) -> void:
	await get_tree().create_timer(0.8).timeout
	_shot("end_banner")
	await get_tree().create_timer(2.5).timeout
	_shot("end")
	print("UI: duelo terminado en %d turnos, ganador %d (local=%d), capturas: %d" % [board.ctrl.turn, winner, board.me, shots])
	_cleanup()
	get_tree().quit(0)


func _shot(name: String) -> void:
	var img := get_viewport().get_texture().get_image()
	img.save_png(out_dir.path_join("%02d_%s.png" % [shots, name]))
	shots += 1


## Imágenes falsas de 421x614 con marco y "ilustración" de colores, sólo para la prueba.
func _make_fake_pics() -> void:
	var codes := {}
	for path in CardDB.deck_paths():
		var d := CardDB.load_deck(path)
		for c in d.main + d.extra:
			codes[c] = true
	var rng := RandomNumberGenerator.new()
	for c in codes:
		if CardImages.has_image(c):
			continue
		rng.seed = c
		var img := Image.create(421, 614, false, Image.FORMAT_RGB8)
		var type: int = CardDB.get_card(c).get("type", 0)
		img.fill(GBA.frame_color(type))
		var a := Color.from_hsv(rng.randf(), 0.7, 0.9)
		var b := Color.from_hsv(rng.randf(), 0.8, 0.4)
		for y in range(112, 432):
			for x in range(50, 371):
				var t := float(x - 50 + y - 112) / 640.0
				var col := a.lerp(b, t)
				var cx := x - 210
				var cy := y - 272
				if cx * cx + cy * cy < 90 * 90 + int(rng.randf() * 3):
					col = col.inverted().lerp(Color.WHITE, 0.2)
				img.set_pixel(x, y, col)
		img.save_jpg(Paths.pic_path(c), 0.9)
		fake_codes.append(c)


func _cleanup() -> void:
	for c in fake_codes:
		DirAccess.remove_absolute(Paths.pic_path(c))
