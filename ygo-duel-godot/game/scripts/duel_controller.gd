class_name DuelController
extends Node
## Ejecuta un duelo con ocgcore: bucle process() → mensajes → respuestas (humano o IA).
## La interfaz escucha las señales y responde con respond().

signal message_received(msg: Dictionary)
signal input_requested(msg: Dictionary)
signal field_updated
signal duel_finished(winner: int, reason: int)
signal log_line(text: String)

signal _response_ready(response: PackedByteArray)

const OCGAIScript := preload("res://scripts/ai_player.gd")

var duel: YGODuel
var decks := [{}, {}]
var names := ["Jugador", "CPU"]
var ai_players := {} # jugador -> OCGAI
var human_players: Array[int] = [0]
var event_delay := 0.0 # pausa tras eventos visibles (sin animador)
## Objeto con `animates(type) -> bool` y `animate(msg)` (corrutina): la interfaz anima cada
## evento y el duelo no continúa hasta que termina la animación.
var animator: Object = null
var max_ai_retries := 8

# Estado consultado al core tras cada lote de mensajes
var field := [{}, {}]
var turn := 0
var turn_player := 0
var phase := 0
var chain: Array = [] # [{code, controller, desc}]
var current_attack := {} # {attacker, target} mientras dura una batalla
var running := false
var finished := false
var winner := -1
var last_hint := {} # jugador -> último HINT_SELECTMSG
var pending_request := {}

var _retries := 0
var _response_buffer = null # la interfaz puede responder dentro de la propia señal


func _init() -> void:
	field = [_empty_side(), _empty_side()]


## options: {seed, lp, hand, draw, flags, ai: [bool, bool]}
func setup(deck0: Dictionary, deck1: Dictionary, options: Dictionary = {}) -> bool:
	decks = [deck0, deck1]
	duel = YGODuel.new()
	duel.set_database(CardDB.db)
	duel.core_log.connect(_on_core_log)
	for dir in CardDB.script_directories():
		duel.add_script_directory(dir)
	var seed: int = options.get("seed", randi() << 31 | randi())
	var res := duel.create_duel(seed, options.get("flags", OCG.DUEL_MODE_MR5),
			options.get("lp", 8000), options.get("hand", 5), options.get("draw", 1))
	if res != 0:
		push_error("No se pudo crear el duelo (código %d)" % res)
		return false
	var rng := RandomNumberGenerator.new()
	rng.seed = seed
	for team in 2:
		var main: Array = decks[team].get("main", []).duplicate()
		# Barajado de Fisher-Yates reproducible con la semilla del duelo
		for i in range(main.size() - 1, 0, -1):
			var j := rng.randi_range(0, i)
			var t = main[i]; main[i] = main[j]; main[j] = t
		for code in main:
			duel.new_card(team, 0, code, team, OCG.LOCATION_DECK, 0, OCG.POS_FACEDOWN_DEFENSE)
		for code in decks[team].get("extra", []):
			duel.new_card(team, 0, code, team, OCG.LOCATION_EXTRA, 0, OCG.POS_FACEDOWN_DEFENSE)
	var ai_flags: Array = options.get("ai", [false, true])
	human_players.clear()
	for p in 2:
		if ai_flags[p]:
			var ai = OCGAIScript.new()
			ai.player = p
			ai.controller = self
			ai.rng.seed = seed + p
			ai_players[p] = ai
		else:
			human_players.append(p)
	return true


func start() -> void:
	duel.start_duel()
	running = true
	finished = false
	refresh_field()
	_run.call_deferred()


func stop() -> void:
	running = false
	_response_ready.emit(PackedByteArray())


## Respuesta del jugador humano al último input_requested.
func respond(response: PackedByteArray) -> void:
	_response_buffer = response
	_response_ready.emit(response)


func _wait_response() -> PackedByteArray:
	if _response_buffer == null:
		await _response_ready
	var r: PackedByteArray = _response_buffer if _response_buffer != null else PackedByteArray()
	_response_buffer = null
	return r


func is_human(player: int) -> bool:
	return player in human_players


func _run() -> void:
	while running:
		var status := duel.process()
		var messages := duel.get_messages()
		for raw in messages:
			if not running:
				return
			var msg := OCGMessageParser.parse(raw)
			_track(msg)
			message_received.emit(msg)
			_log_message(msg)
			if msg.type == OCG.MSG_WIN:
				_finish(msg.player, msg.reason)
				return
			if msg.type == OCG.MSG_RETRY:
				await _handle_retry()
				continue
			if OCGMessageParser.needs_response(msg.type):
				_retries = 0
				pending_request = msg
				await _request_response(msg)
			elif animator != null and animator.animates(msg.type):
				await animator.animate(msg)
			elif event_delay > 0.0 and _is_visible_event(msg.type):
				refresh_field()
				await get_tree().create_timer(event_delay).timeout
		if status == OCG.STATUS_END:
			_finish(-1, 0)
			return
		refresh_field()
		if messages.is_empty() and status == OCG.STATUS_AWAITING:
			push_error("El core espera una respuesta pero no envió mensajes; se detiene el duelo")
			_finish(-1, 0)
			return
		# Cede un frame para no congelar la interfaz
		if is_inside_tree():
			await get_tree().process_frame


func _request_response(msg: Dictionary) -> void:
	refresh_field()
	var player: int = msg.get("player", 0)
	if ai_players.has(player):
		duel.set_response(ai_players[player].decide(msg))
	else:
		_response_buffer = null
		input_requested.emit(msg)
		var response: PackedByteArray = await _wait_response()
		if running:
			duel.set_response(response)


func _handle_retry() -> void:
	_retries += 1
	var msg := pending_request
	if msg.is_empty():
		return
	var player: int = msg.get("player", 0)
	if ai_players.has(player):
		if _retries > max_ai_retries:
			push_warning("La IA no encuentra una respuesta válida para el mensaje %d" % msg.type)
		duel.set_response(ai_players[player].fallback(msg, _retries))
	else:
		log_line.emit("[color=orange]Selección no válida, inténtalo de nuevo.[/color]")
		msg["retry"] = true
		_response_buffer = null
		input_requested.emit(msg)
		var response: PackedByteArray = await _wait_response()
		if running:
			duel.set_response(response)


func _finish(p_winner: int, reason: int) -> void:
	running = false
	finished = true
	winner = p_winner
	refresh_field()
	duel_finished.emit(p_winner, reason)


func _track(msg: Dictionary) -> void:
	match msg.type:
		OCG.MSG_NEW_TURN:
			turn += 1
			turn_player = msg.player
		OCG.MSG_NEW_PHASE:
			phase = msg.phase
			current_attack = {}
		OCG.MSG_HINT:
			if msg.hint_type == OCG.HINT_SELECTMSG:
				last_hint[msg.player] = msg.data
		OCG.MSG_CHAINING:
			chain.append({"code": msg.code, "controller": msg.loc.controller, "desc": msg.desc})
		OCG.MSG_CHAIN_END:
			chain.clear()
		OCG.MSG_ATTACK:
			current_attack = {"attacker": msg.attacker, "target": msg.target}
		OCG.MSG_DAMAGE_STEP_END, OCG.MSG_ATTACK_DISABLED:
			current_attack = {}


func _is_visible_event(type: int) -> bool:
	return type in [OCG.MSG_SUMMONING, OCG.MSG_SPSUMMONING, OCG.MSG_FLIPSUMMONING, OCG.MSG_SET,
			OCG.MSG_CHAINING, OCG.MSG_CHAIN_SOLVED, OCG.MSG_ATTACK, OCG.MSG_BATTLE, OCG.MSG_DAMAGE,
			OCG.MSG_RECOVER, OCG.MSG_PAY_LPCOST, OCG.MSG_NEW_TURN, OCG.MSG_NEW_PHASE, OCG.MSG_DRAW,
			OCG.MSG_POS_CHANGE, OCG.MSG_EQUIP]


# ---------------------------------------------------------------- estado del campo

func _empty_side() -> Dictionary:
	return {"lp": 8000, "deck": 0, "hand": [], "mzone": [], "szone": [], "grave": [], "removed": [], "extra": [], "extra_p": 0}


func refresh_field() -> void:
	if duel == null or not duel.is_active():
		return
	var flags := OCG.QUERY_FIELD_FLAGS
	for p in 2:
		var side: Dictionary = field[p]
		side.hand = _compact(duel.query_location(p, OCG.LOCATION_HAND, flags))
		side.mzone = duel.query_location(p, OCG.LOCATION_MZONE, flags)
		side.szone = duel.query_location(p, OCG.LOCATION_SZONE, flags)
		side.grave = _compact(duel.query_location(p, OCG.LOCATION_GRAVE, flags))
		side.removed = _compact(duel.query_location(p, OCG.LOCATION_REMOVED, flags))
		side.extra = _compact(duel.query_location(p, OCG.LOCATION_EXTRA, flags))
		side.deck = duel.query_count(p, OCG.LOCATION_DECK)
	_parse_field_raw(duel.query_field_raw())
	field_updated.emit()


func _compact(arr: Array) -> Array:
	return arr.filter(func(c): return c != null)


## OCG_DuelQueryField: u32 opciones; por jugador: u32 LP, zonas (u8 ocupada [+u8 pos +u32 materiales]),
## u32 deck, mano, cementerio, desterradas, extra, extra boca arriba.
func _parse_field_raw(buf: PackedByteArray) -> void:
	if buf.size() < 4:
		return
	var pos := 4
	for p in 2:
		var side: Dictionary = field[p]
		side.lp = max(0, buf.decode_s32(pos)); pos += 4
		for zone in [side.mzone.size(), side.szone.size()]:
			for i in zone:
				var used := buf.decode_u8(pos); pos += 1
				if used:
					pos += 5
		side.deck = buf.decode_u32(pos); pos += 4 * 5
		side.extra_p = buf.decode_u32(pos); pos += 4


func get_card_at(controller: int, location: int, sequence: int):
	var side: Dictionary = field[controller]
	var list: Array
	match location & ~OCG.LOCATION_OVERLAY:
		OCG.LOCATION_HAND: list = side.hand
		OCG.LOCATION_MZONE: list = side.mzone
		OCG.LOCATION_SZONE: list = side.szone
		OCG.LOCATION_GRAVE: list = side.grave
		OCG.LOCATION_REMOVED: list = side.removed
		OCG.LOCATION_EXTRA: list = side.extra
		_: return null
	if sequence >= 0 and sequence < list.size():
		return list[sequence]
	return null


# ---------------------------------------------------------------- registro de la partida

func _pname(p: int) -> String:
	return names[p] if p >= 0 and p < 2 else "?"


func _cname(code: int) -> String:
	return "[b]%s[/b]" % CardDB.card_name(code)


func _log_message(msg: Dictionary) -> void:
	var t := ""
	match msg.type:
		OCG.MSG_NEW_TURN:
			t = "[color=yellow]── Turno %d: %s ──[/color]" % [turn, _pname(msg.player)]
		OCG.MSG_NEW_PHASE:
			t = "[color=gray]Fase: %s[/color]" % OCG.PHASE_NAMES.get(msg.phase, str(msg.phase))
		OCG.MSG_DRAW:
			t = "%s roba %d carta(s)" % [_pname(msg.player), msg.cards.size()]
		OCG.MSG_SUMMONING:
			t = "%s Invoca de Modo Normal %s" % [_pname(msg.loc.controller), _cname(msg.code)]
		OCG.MSG_SPSUMMONING:
			t = "%s Invoca de Modo Especial %s" % [_pname(msg.loc.controller), _cname(msg.code) if msg.code else "un monstruo"]
		OCG.MSG_FLIPSUMMONING:
			t = "%s Invoca por Volteo %s" % [_pname(msg.loc.controller), _cname(msg.code)]
		OCG.MSG_SET:
			t = "%s coloca una carta" % _pname(msg.loc.controller)
		OCG.MSG_CHAINING:
			t = "[color=cyan]Eslabón %d: %s activa %s[/color]" % [msg.chain_count, _pname(msg.loc.controller), _cname(msg.code)]
		OCG.MSG_CHAIN_NEGATED, OCG.MSG_CHAIN_DISABLED:
			t = "[color=orange]El eslabón %d fue negado[/color]" % msg.chain_count
		OCG.MSG_ATTACK:
			var atk = get_card_at(msg.attacker.controller, msg.attacker.location, msg.attacker.sequence)
			var aname := CardDB.card_name(atk.code) if atk else "Un monstruo"
			if msg.direct:
				t = "[color=red]%s ataca directamente[/color]" % aname
			else:
				var tgt = get_card_at(msg.target.controller, msg.target.location, msg.target.sequence)
				var tname := CardDB.card_name(tgt.code) if tgt and (tgt.position & OCG.POS_FACEUP) else "un monstruo boca abajo"
				t = "[color=red]%s ataca a %s[/color]" % [aname, tname]
		OCG.MSG_DAMAGE:
			t = "[color=red]%s recibe %d de daño[/color]" % [_pname(msg.player), msg.amount]
		OCG.MSG_RECOVER:
			t = "[color=lime]%s gana %d LP[/color]" % [_pname(msg.player), msg.amount]
		OCG.MSG_PAY_LPCOST:
			t = "%s paga %d LP" % [_pname(msg.player), msg.amount]
		OCG.MSG_MOVE:
			var to: Dictionary = msg.to
			var from: Dictionary = msg.from
			if to.location == OCG.LOCATION_GRAVE and from.location & (OCG.LOCATION_ONFIELD | OCG.LOCATION_HAND):
				t = "%s va al Cementerio" % _cname(msg.code)
			elif to.location == OCG.LOCATION_REMOVED:
				t = "%s es desterrada" % _cname(msg.code)
			elif to.location == OCG.LOCATION_HAND and from.location != OCG.LOCATION_DECK and from.location != 0:
				t = "%s vuelve a la mano" % _cname(msg.code)
			elif to.location == OCG.LOCATION_HAND and from.location == OCG.LOCATION_DECK and msg.reason & 0x40:
				t = "%s añade una carta del Deck a la mano" % _pname(to.controller)
		OCG.MSG_POS_CHANGE:
			t = "%s cambia de posición" % _cname(msg.code)
		OCG.MSG_TOSS_COIN:
			t = "Moneda: " + ", ".join(msg.results.map(func(r): return "Cara" if r else "Cruz"))
		OCG.MSG_TOSS_DICE:
			t = "Dado: " + ", ".join(msg.results.map(func(r): return str(r)))
		OCG.MSG_WIN:
			var reason: String = CardDB.victory_strings.get(msg.reason, OCG.WIN_REASONS.get(msg.reason, ""))
			t = "[color=yellow][b]%s gana el duelo[/b] %s[/color]" % [_pname(msg.player), ("(" + reason + ")") if reason != "" else ""]
	if t != "":
		log_line.emit(t)


func _on_core_log(text: String, type: int) -> void:
	if type == 0:
		push_warning("ocgcore: " + text)
	log_line.emit("[color=#888888][core] %s[/color]" % text)
