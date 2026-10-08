extends Node
## Prueba sin interfaz: juega varios duelos IA contra IA y comprueba que terminan.
## godot --headless --path game res://tests/ai_vs_ai.tscn -- [duelos] [semilla]

const MAX_TURNS := 80

var stats := {"msgs": {}, "retries": 0}


func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	var games := int(args[0]) if args.size() > 0 else 5
	var seed0 := int(args[1]) if args.size() > 1 else 1234
	print("ocgcore versión: ", YGODuel.get_core_version())
	var decks := CardDB.deck_paths()
	var failures := 0
	for g in games:
		var d0 := CardDB.load_deck(decks[g % decks.size()])
		var d1 := CardDB.load_deck(decks[(g + 1) % decks.size()])
		var c := DuelController.new()
		add_child(c)
		c.names = ["IA-A", "IA-B"]
		var ok := c.setup(d0, d1, {"seed": seed0 + g, "ai": [true, true]})
		if not ok:
			failures += 1
			continue
		c.message_received.connect(func(m): stats.msgs[m.type] = stats.msgs.get(m.type, 0) + 1)
		c.log_line.connect(func(t): if t.contains("[core]") or t.contains("no válida"): print("   ", t))
		var t0 := Time.get_ticks_msec()
		c.start()
		while not c.finished and c.turn <= MAX_TURNS and Time.get_ticks_msec() - t0 < 60000:
			await get_tree().process_frame
		var ok_end := c.finished
		print("Duelo %d (semilla %d): %s tras %d turnos, ganador=%d, LP %d/%d, %d ms" % [g, seed0 + g,
				"terminado" if ok_end else "SIN TERMINAR", c.turn, c.winner, c.field[0].lp, c.field[1].lp,
				Time.get_ticks_msec() - t0])
		if not ok_end:
			failures += 1
		c.stop()
		c.queue_free()
	var keys: Array = stats.msgs.keys()
	keys.sort()
	print("Mensajes vistos: ", keys)
	print("RESULTADO: %s" % ("OK" if failures == 0 else "%d FALLOS" % failures))
	get_tree().quit(1 if failures else 0)
