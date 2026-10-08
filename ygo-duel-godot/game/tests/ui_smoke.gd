extends Node
## Prueba de la interfaz: abre el tablero real y juega el lado humano con la IA,
## guardando capturas. Uso (necesita pantalla, p.ej. xvfb-run):
##   godot --path game res://tests/ui_smoke.tscn -- <carpeta_capturas> [semilla]

const AIScript := preload("res://scripts/ai_player.gd")

var out_dir := "user://screens"
var board: DuelBoard
var bot
var shots := 0
var seen := {}


func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	if args.size() > 0:
		out_dir = args[0]
	DirAccess.make_dir_recursive_absolute(out_dir)
	var decks := CardDB.deck_paths()
	DuelBoard.config = {"deck0": decks[0], "deck1": decks[1], "first": "me",
			"seed": int(args[1]) if args.size() > 1 else 42}
	CardImages.download_enabled = false
	board = load("res://scenes/duel.tscn").instantiate()
	add_child(board)
	await get_tree().process_frame
	bot = AIScript.new()
	bot.player = board.me
	bot.controller = board.ctrl
	board.ctrl.event_delay = 0.02
	board.ctrl.input_requested.connect(_on_request)
	board.ctrl.duel_finished.connect(_on_finished)
	await get_tree().create_timer(240).timeout
	print("TIMEOUT")
	get_tree().quit(1)


func _on_request(msg: Dictionary) -> void:
	await get_tree().process_frame
	await get_tree().process_frame
	var key := "type%d" % msg.type
	if not seen.has(key) or (msg.type == OCG.MSG_SELECT_IDLECMD and board.ctrl.turn in [1, 3, 5] and not seen.has("t%d" % board.ctrl.turn)):
		seen[key] = true
		seen["t%d" % board.ctrl.turn] = true
		_shot("req_%d_turn%d" % [msg.type, board.ctrl.turn])
	var resp: PackedByteArray = bot.decide(msg)
	if board.dialog.visible:
		board.dialog.visible = false
		board._dialog_busy = false
	board._respond(resp)


func _on_finished(winner: int, _reason: int) -> void:
	await get_tree().create_timer(0.3).timeout
	_shot("end")
	print("UI: duelo terminado en %d turnos, ganador %d (local=%d), capturas: %d" % [board.ctrl.turn, winner, board.me, shots])
	get_tree().quit(0)


func _shot(name: String) -> void:
	var img := get_viewport().get_texture().get_image()
	img.save_png(out_dir.path_join("%02d_%s.png" % [shots, name]))
	shots += 1
