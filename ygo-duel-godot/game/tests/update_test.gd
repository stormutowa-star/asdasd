extends Node
## Prueba: actualizar la base de cartas (DataUpdater), importar un mazo moderno en formato ydke://
## y jugar duelos IA contra IA con él. Servidor de pruebas:
##   YGO_SCRIPTS_URL=http://127.0.0.1:8766/scripts.zip YGO_CDB_URL=http://127.0.0.1:8766/cdb.zip
##   godot --headless --path game res://tests/update_test.tscn -- <fichero_ydke> [duelos]


func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	var before := CardDB.db.get_card_count()
	var t0 := Time.get_ticks_msec()
	DataUpdater.update_async()
	var res: Array = await DataUpdater.update_finished
	print("Actualización: ok=%s · %s · %d → %d cartas · %d ms" % [res[0], res[1], before, CardDB.db.get_card_count(), Time.get_ticks_msec() - t0])
	var text := FileAccess.get_file_as_string(args[0])
	var deck := CardDB.normalize_deck(CardDB.parse_deck_text(text))
	var unknown := CardDB.unknown_cards(deck)
	print("Mazo ydke: main=%d extra=%d desconocidas=%d" % [deck.main.size(), deck.extra.size(), unknown.size()])
	var kaiba := CardDB.load_deck(CardDB.deck_paths()[0])
	var games := int(args[1]) if args.size() > 1 else 3
	var failures := 0
	var core_errors := 0
	for g in games:
		var c := DuelController.new()
		add_child(c)
		c.log_line.connect(func(t):
			if t.contains("[core]"):
				core_errors += 1
				if core_errors <= 5: print("   ", t))
		c.setup(deck, deck if g % 2 == 0 else kaiba, {"seed": 900 + g, "ai": [true, true]})
		var t1 := Time.get_ticks_msec()
		c.start()
		while not c.finished and c.turn <= 60 and Time.get_ticks_msec() - t1 < 90000:
			await get_tree().process_frame
		print("Duelo %d: %s, %d turnos, ganador %d, LP %d/%d" % [g, "terminado" if c.finished else "SIN TERMINAR", c.turn, c.winner, c.field[0].lp, c.field[1].lp])
		if not c.finished:
			failures += 1
		c.stop()
		c.queue_free()
	var ok: bool = res[0] and unknown.is_empty() and failures == 0
	print("Errores del core: %d" % core_errors)
	print("RESULTADO: ", "OK" if ok else "FALLO")
	get_tree().quit(0 if ok else 1)
