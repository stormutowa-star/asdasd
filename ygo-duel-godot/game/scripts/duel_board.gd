class_name DuelBoard
extends Control
## Tablero de duelo estilo EDOPro: campo, manos, panel de información, registro y diálogos.

## Configuración que deja el menú: {deck0, deck1, first ("random"/"me"/"cpu"), lp, seed}
static var config := {}

const CARD_RATIO := CardView.CARD_RATIO
const COL_HIGHLIGHT_ACTION := Color(0.3, 0.85, 1.0)
const COL_HIGHLIGHT_SELECT := Color(1.0, 0.85, 0.2)
const COL_ZONE := Color(1, 1, 1, 0.18)
const COL_ZONE_FREE := Color(0.3, 1.0, 0.4)

var ctrl: DuelController
var me := 0 # índice del jugador local en el core

var current_req := {}
var _actions := {} # "c:l:s" -> [[texto, respuesta], ...]
var _select_keys := {} # cartas candidatas en una selección (para resaltarlas en el campo)
var _place_free: Array = []
var _place_chosen: Array = []
var _menu_responses: Array = []
var _dialog_busy := false

# Geometría
var _cw := 90.0
var _ch := 120.0
var _card_w := 70.0
var _x0 := 0.0
var _y0 := 0.0

# Nodos
var field_layer: Control
var zone_layer: ZoneLayer
var cards_layer: Control
var fx_layer: Control
var info_view: CardView
var info_name: Label
var info_type: Label
var info_stats: Label
var info_desc: RichTextLabel
var name_labels: Array[Label] = []
var lp_labels: Array[Label] = []
var lp_bars: Array[ProgressBar] = []
var turn_label: Label
var phase_label: Label
var prompt_label: Label
var chain_label: Label
var btn_bp: Button
var btn_m2: Button
var btn_ep: Button
var log_box: RichTextLabel
var dialog: PromptDialog
var action_menu: PopupMenu
var auto_place_cb: CheckBox
var auto_chain_cb: CheckBox
var fast_cb: CheckBox
var _attack_line: Line2D
var _end_panel: PanelContainer


class ZoneLayer:
	extends Control
	var board: DuelBoard

	func _draw() -> void:
		board._draw_zones(self)

	func _gui_input(event: InputEvent) -> void:
		if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
			board._on_zone_clicked(event.position)


func _ready() -> void:
	var th := Theme.new()
	th.default_font_size = 15
	theme = th
	_build_ui()
	resized.connect(_relayout)
	_start_duel()


# ================================================================ construcción de la interfaz

func _build_ui() -> void:
	var bg := ColorRect.new()
	bg.color = Color("0d1220")
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	bg.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(bg)

	var root := HBoxContainer.new()
	root.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_theme_constant_override("separation", 0)
	add_child(root)

	# --- Panel izquierdo: información de la carta
	var left := _panel(Color("111827"))
	left.custom_minimum_size.x = 300
	root.add_child(left)
	var lv := VBoxContainer.new()
	lv.add_theme_constant_override("separation", 6)
	left.add_child(lv)
	var cc := CenterContainer.new()
	lv.add_child(cc)
	info_view = CardView.new()
	info_view.set_card_size(250)
	info_view.mouse_filter = Control.MOUSE_FILTER_IGNORE
	info_view.setup(0, {}, false)
	cc.add_child(info_view)
	info_name = Label.new()
	info_name.add_theme_font_size_override("font_size", 19)
	info_name.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	lv.add_child(info_name)
	info_type = Label.new()
	info_type.modulate = Color(0.75, 0.85, 1.0)
	info_type.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	lv.add_child(info_type)
	info_stats = Label.new()
	info_stats.modulate = Color(1, 0.9, 0.6)
	lv.add_child(info_stats)
	info_desc = RichTextLabel.new()
	info_desc.size_flags_vertical = Control.SIZE_EXPAND_FILL
	info_desc.add_theme_font_size_override("normal_font_size", 14)
	lv.add_child(info_desc)

	# --- Centro: campo
	field_layer = Control.new()
	field_layer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	field_layer.size_flags_vertical = Control.SIZE_EXPAND_FILL
	field_layer.clip_contents = true
	root.add_child(field_layer)
	zone_layer = ZoneLayer.new()
	zone_layer.board = self
	zone_layer.set_anchors_preset(Control.PRESET_FULL_RECT)
	field_layer.add_child(zone_layer)
	cards_layer = Control.new()
	cards_layer.set_anchors_preset(Control.PRESET_FULL_RECT)
	cards_layer.mouse_filter = Control.MOUSE_FILTER_IGNORE
	field_layer.add_child(cards_layer)
	fx_layer = Control.new()
	fx_layer.set_anchors_preset(Control.PRESET_FULL_RECT)
	fx_layer.mouse_filter = Control.MOUSE_FILTER_IGNORE
	field_layer.add_child(fx_layer)
	_attack_line = Line2D.new()
	_attack_line.width = 6
	_attack_line.default_color = Color(1, 0.25, 0.2, 0.85)
	_attack_line.visible = false
	fx_layer.add_child(_attack_line)
	field_layer.resized.connect(_relayout)

	dialog = PromptDialog.new()
	dialog.card_hovered.connect(_show_info)
	field_layer.add_child(dialog)

	action_menu = PopupMenu.new()
	action_menu.id_pressed.connect(_on_action_chosen)
	add_child(action_menu)

	# --- Panel derecho: LP, fase, botones y registro
	var right := _panel(Color("111827"))
	right.custom_minimum_size.x = 340
	root.add_child(right)
	var rv := VBoxContainer.new()
	rv.add_theme_constant_override("separation", 6)
	right.add_child(rv)
	for i in 2:
		var box := VBoxContainer.new()
		var nl := Label.new()
		nl.add_theme_font_size_override("font_size", 17)
		box.add_child(nl)
		var ll := Label.new()
		ll.add_theme_font_size_override("font_size", 30)
		ll.text = "8000"
		box.add_child(ll)
		var bar := ProgressBar.new()
		bar.max_value = 8000
		bar.value = 8000
		bar.show_percentage = false
		bar.custom_minimum_size.y = 10
		box.add_child(bar)
		name_labels.append(nl)
		lp_labels.append(ll)
		lp_bars.append(bar)
		rv.add_child(box)
		if i == 0:
			turn_label = Label.new()
			rv.add_child(turn_label)
			phase_label = Label.new()
			phase_label.add_theme_font_size_override("font_size", 18)
			phase_label.modulate = Color(1, 0.85, 0.4)
			rv.add_child(phase_label)
	var hb := HBoxContainer.new()
	rv.add_child(hb)
	btn_bp = _button(hb, "Batalla", func(): _respond(OCGResponse.command(6)))
	btn_m2 = _button(hb, "Main 2", func(): _respond(OCGResponse.command(2)))
	btn_ep = _button(hb, "Fin de turno", _on_end_turn)
	prompt_label = Label.new()
	prompt_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	prompt_label.modulate = Color(0.6, 1, 0.7)
	rv.add_child(prompt_label)
	chain_label = Label.new()
	chain_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	chain_label.modulate = Color(0.5, 0.9, 1)
	rv.add_child(chain_label)
	log_box = RichTextLabel.new()
	log_box.bbcode_enabled = true
	log_box.scroll_following = true
	log_box.size_flags_vertical = Control.SIZE_EXPAND_FILL
	log_box.add_theme_font_size_override("normal_font_size", 13)
	log_box.add_theme_font_size_override("bold_font_size", 13)
	rv.add_child(log_box)
	auto_place_cb = CheckBox.new()
	auto_place_cb.text = "Colocar en zona automáticamente"
	auto_place_cb.button_pressed = true
	rv.add_child(auto_place_cb)
	auto_chain_cb = CheckBox.new()
	auto_chain_cb.text = "No preguntar por cadenas"
	rv.add_child(auto_chain_cb)
	fast_cb = CheckBox.new()
	fast_cb.text = "Animaciones rápidas"
	fast_cb.toggled.connect(func(on): if ctrl: ctrl.event_delay = 0.08 if on else 0.35)
	rv.add_child(fast_cb)
	var hb2 := HBoxContainer.new()
	rv.add_child(hb2)
	_button(hb2, "Rendirse", _on_surrender)
	_button(hb2, "Menú", func(): _go_menu())


func _panel(color: Color) -> PanelContainer:
	var p := PanelContainer.new()
	var sb := StyleBoxFlat.new()
	sb.bg_color = color
	sb.set_content_margin_all(10)
	p.add_theme_stylebox_override("panel", sb)
	return p


func _button(parent: Control, text: String, cb: Callable) -> Button:
	var b := Button.new()
	b.text = text
	b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	b.custom_minimum_size.y = 36
	b.pressed.connect(cb)
	parent.add_child(b)
	return b


# ================================================================ duelo

func _start_duel() -> void:
	var cfg := config
	var decks := CardDB.deck_paths()
	if decks.is_empty():
		_log("[color=red]No hay mazos en res://decks[/color]")
		return
	var my_deck := CardDB.load_deck(cfg.get("deck0", decks[0]))
	var cpu_deck := CardDB.load_deck(cfg.get("deck1", decks[min(1, decks.size() - 1)]))
	var first: String = cfg.get("first", "random")
	var seed: int = cfg.get("seed", int(Time.get_unix_time_from_system() * 1000.0) ^ randi())
	if first == "random":
		me = seed & 1
	else:
		me = 0 if first == "me" else 1
	ctrl = DuelController.new()
	add_child(ctrl)
	ctrl.names[me] = cfg.get("name", "Tú")
	ctrl.names[1 - me] = "CPU"
	var d0 := my_deck if me == 0 else cpu_deck
	var d1 := cpu_deck if me == 0 else my_deck
	var ai := [me != 0, me != 1]
	ctrl.event_delay = 0.35
	ctrl.input_requested.connect(_on_input_requested)
	ctrl.field_updated.connect(_rebuild)
	ctrl.message_received.connect(_on_message)
	ctrl.log_line.connect(_log)
	ctrl.duel_finished.connect(_on_duel_finished)
	var lp: int = cfg.get("lp", 8000)
	if not ctrl.setup(d0, d1, {"seed": seed, "ai": ai, "lp": lp}):
		_log("[color=red]No se pudo crear el duelo. ¿Está compilada la extensión ocgcore_gd?[/color]")
		return
	for p in 2:
		lp_bars[_ui_index(p)].max_value = lp
	_log("[color=yellow]¡Comienza el duelo! Empieza %s.[/color]" % ctrl.names[0])
	ctrl.start()


## 0 = rival (arriba), 1 = jugador local (abajo) en el panel derecho
func _ui_index(p: int) -> int:
	return 1 if p == me else 0


func _on_message(msg: Dictionary) -> void:
	match msg.type:
		OCG.MSG_ATTACK:
			_show_attack(msg)
		OCG.MSG_BATTLE, OCG.MSG_DAMAGE_STEP_END, OCG.MSG_NEW_PHASE, OCG.MSG_ATTACK_DISABLED:
			_attack_line.visible = false
		OCG.MSG_DAMAGE, OCG.MSG_PAY_LPCOST:
			_flash_lp(msg.player, Color(1, 0.3, 0.3))
		OCG.MSG_RECOVER:
			_flash_lp(msg.player, Color(0.3, 1, 0.4))
		OCG.MSG_CHAINING, OCG.MSG_SUMMONING, OCG.MSG_SPSUMMONING, OCG.MSG_FLIPSUMMONING:
			if msg.code:
				var d := CardDB.get_card(msg.code)
				_show_info_data(msg.code, d, true)
	_update_status()


func _on_duel_finished(winner: int, reason: int) -> void:
	current_req = {}
	_update_buttons()
	_rebuild()
	var text := "¡HAS GANADO!" if winner == me else ("Has perdido..." if winner >= 0 else "Empate")
	_end_panel = PanelContainer.new()
	var sb := StyleBoxFlat.new()
	sb.bg_color = Color(0.05, 0.05, 0.1, 0.95)
	sb.border_color = Color(1, 0.8, 0.2)
	sb.set_border_width_all(3)
	sb.set_corner_radius_all(10)
	sb.set_content_margin_all(24)
	_end_panel.add_theme_stylebox_override("panel", sb)
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 12)
	_end_panel.add_child(v)
	var l := Label.new()
	l.text = text
	l.add_theme_font_size_override("font_size", 40)
	l.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(l)
	var r := Label.new()
	r.text = CardDB.victory_strings.get(reason, OCG.WIN_REASONS.get(reason, ""))
	r.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(r)
	var h := HBoxContainer.new()
	h.add_theme_constant_override("separation", 12)
	v.add_child(h)
	_button(h, "Revancha", func(): get_tree().reload_current_scene())
	_button(h, "Menú principal", func(): _go_menu())
	field_layer.add_child(_end_panel)
	await get_tree().process_frame
	_end_panel.position = (field_layer.size - _end_panel.size) / 2.0


func _go_menu() -> void:
	if ctrl:
		ctrl.stop()
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")


func _on_surrender() -> void:
	if ctrl == null or ctrl.finished:
		return
	ctrl.stop()
	_log("[color=orange]Te has rendido.[/color]")
	_on_duel_finished(1 - me, 0x4)


func _log(text: String) -> void:
	log_box.append_text(text + "\n")


# ================================================================ peticiones de entrada

func _respond(response: PackedByteArray) -> void:
	if current_req.is_empty():
		return
	current_req = {}
	_actions.clear()
	_select_keys.clear()
	_place_free.clear()
	_place_chosen.clear()
	prompt_label.text = ""
	_update_buttons()
	zone_layer.queue_redraw()
	ctrl.respond(response)


func _hint_text(default_text := "Selecciona") -> String:
	var t := CardDB.sys(ctrl.last_hint.get(me, 0))
	return t if t != "" else default_text


func _desc(desc: int, code := 0) -> String:
	var t := CardDB.desc_text(desc)
	if code:
		t = t.replace("%ls", CardDB.card_name(code))
	return t


func _on_input_requested(msg: Dictionary) -> void:
	current_req = msg
	_actions.clear()
	_select_keys.clear()
	match msg.type:
		OCG.MSG_SELECT_IDLECMD:
			_collect_idle_actions(msg)
			prompt_label.text = "Tu turno: haz clic en una carta para ver sus acciones."
		OCG.MSG_SELECT_BATTLECMD:
			_collect_battle_actions(msg)
			prompt_label.text = "Fase de Batalla: elige un monstruo para atacar."
		OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD:
			_start_place_selection(msg)
	_update_buttons()
	_rebuild()
	if msg.type in [OCG.MSG_SELECT_IDLECMD, OCG.MSG_SELECT_BATTLECMD, OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD]:
		return
	await _ask_dialog(msg)


func _ask_dialog(msg: Dictionary) -> void:
	_dialog_busy = true
	var resp = null
	match msg.type:
		OCG.MSG_SELECT_CHAIN:
			if msg.chains.is_empty() and not msg.forced:
				resp = OCGResponse.cancel()
			elif auto_chain_cb.button_pressed and not msg.forced:
				resp = OCGResponse.cancel()
			else:
				var cards: Array = msg.chains.map(func(c): return _entry(c, _desc(c.desc, c.code) if c.desc else "Activar"))
				var title := "¿Activar un efecto en cadena?" if not msg.forced else "Elige el efecto a activar"
				if not ctrl.chain.is_empty():
					title += " (respuesta a %s)" % CardDB.card_name(ctrl.chain.back().code)
				var idx: int = await dialog.ask_pick(title, cards, [], "", "" if msg.forced else "No activar")
				resp = OCGResponse.int32(msg.chains[idx].index) if idx >= 0 else OCGResponse.cancel()
		OCG.MSG_SELECT_EFFECTYN:
			var t := _desc(msg.desc, msg.code) if msg.desc else CardDB.sys(95).replace("%ls", CardDB.card_name(msg.code))
			var yes: bool = await dialog.ask_yes_no(t, msg.code)
			resp = OCGResponse.int32(1 if yes else 0)
		OCG.MSG_SELECT_YESNO:
			var yes2: bool = await dialog.ask_yes_no(_desc(msg.desc))
			resp = OCGResponse.int32(1 if yes2 else 0)
		OCG.MSG_SELECT_OPTION:
			var opts: Array = msg.options.map(func(o): return _desc(o))
			resp = OCGResponse.int32(await dialog.ask_options(_hint_text("Elige una opción"), opts))
		OCG.MSG_SELECT_CARD:
			_mark_candidates(msg.cards)
			var cards2: Array = msg.cards.map(func(c): return _entry(c))
			var sel = await dialog.ask_cards(_hint_text(), cards2, msg.min, msg.max, msg.cancelable)
			resp = OCGResponse.cancel() if sel == null else OCGResponse.cards(sel.map(func(i): return msg.cards[i].index))
		OCG.MSG_SELECT_TRIBUTE:
			_mark_candidates(msg.cards)
			var cards3: Array = msg.cards.map(func(c): return _entry(c))
			var validator := func(sel: Array) -> bool:
				var total := 0
				for i in sel: total += int(msg.cards[i].release_param)
				return total >= msg.min and sel.size() <= msg.max and not sel.is_empty()
			var sel3 = await dialog.ask_cards(_hint_text("Selecciona los monstruos a sacrificar"), cards3, 1, msg.max, msg.cancelable, validator)
			resp = OCGResponse.cancel() if sel3 == null else OCGResponse.cards(sel3.map(func(i): return msg.cards[i].index))
		OCG.MSG_SELECT_UNSELECT_CARD:
			var all: Array = msg.select_cards + msg.unselect_cards
			_mark_candidates(all)
			var pre := range(msg.select_cards.size(), all.size())
			var idx4: int = await dialog.ask_pick(_hint_text(), all.map(func(c): return _entry(c)), pre,
					"Terminar" if msg.finishable else "", "Cancelar" if msg.cancelable and not msg.finishable else "",
					"Mín. %d, máx. %d" % [msg.min, msg.max])
			resp = OCGResponse.select_unselect(idx4) if idx4 >= 0 else OCGResponse.cancel()
		OCG.MSG_SELECT_SUM:
			var all5: Array = msg.must_cards + msg.cards
			_mark_candidates(all5)
			var must_n: int = msg.must_cards.size()
			var validator5 := func(sel: Array) -> bool:
				var total := 0
				for c in msg.must_cards: total += int(c.param) & 0xffff
				for i in sel:
					if i < must_n: return false
					total += int(msg.cards[i - must_n].param) & 0xffff
				return total == msg.acc if msg.select_mode == 0 else total >= msg.acc
			var sel5 = await dialog.ask_cards("%s (suma %d)" % [_hint_text(), msg.acc], all5.map(func(c): return _entry(c, "%d" % (int(c.param) & 0xffff))),
					msg.min, max(msg.max, all5.size()), false, validator5)
			resp = OCGResponse.cards((sel5 if sel5 else []).map(func(i): return msg.cards[i - must_n].index))
		OCG.MSG_SELECT_POSITION:
			resp = OCGResponse.int32(await dialog.ask_position(msg.code, msg.positions))
		OCG.MSG_SORT_CARD, OCG.MSG_SORT_CHAIN:
			resp = OCGResponse.sort([])
		OCG.MSG_SELECT_COUNTER:
			var cards7: Array = msg.cards.map(func(c): var e := _entry(c); e.counters = c.counters; return e)
			resp = OCGResponse.counters(await dialog.ask_counters(_hint_text("Retira contadores"), cards7, msg.count))
		OCG.MSG_ANNOUNCE_RACE:
			resp = OCGResponse.uint64(await dialog.ask_flags("Declara un Tipo de monstruo", OCG.RACES, msg.available, msg.count))
		OCG.MSG_ANNOUNCE_ATTRIB:
			resp = OCGResponse.int32(await dialog.ask_flags("Declara un Atributo", OCG.ATTRIBUTES, msg.available, msg.count))
		OCG.MSG_ANNOUNCE_NUMBER:
			resp = OCGResponse.int32(await dialog.ask_options(_hint_text("Declara un número"), msg.options.map(func(o): return str(o))))
		OCG.MSG_ANNOUNCE_CARD:
			resp = OCGResponse.int32(await dialog.ask_card_name(_hint_text("Declara el nombre de una carta")))
		OCG.MSG_ROCK_PAPER_SCISSORS:
			resp = OCGResponse.int32(1 + await dialog.ask_options("Piedra, papel o tijera", ["Piedra", "Papel", "Tijera"]))
		_:
			resp = OCGResponse.int32(0)
	_dialog_busy = false
	_respond(resp)


## Datos para mostrar una carta de un mensaje en un diálogo (ocultando las boca abajo del rival).
func _entry(c: Dictionary, label := "") -> Dictionary:
	var loc: int = c.get("location", 0)
	var pos: int = c.get("position", 0)
	var visible := true
	if c.get("controller", me) != me and loc & (OCG.LOCATION_ONFIELD | OCG.LOCATION_HAND | OCG.LOCATION_DECK | OCG.LOCATION_EXTRA) \
			and not (loc & OCG.LOCATION_ONFIELD and pos & OCG.POS_FACEUP):
		visible = false
	if loc == OCG.LOCATION_REMOVED and pos & OCG.POS_FACEDOWN and c.get("controller", me) != me:
		visible = false
	var q = ctrl.get_card_at(c.get("controller", 0), loc, c.get("sequence", 0)) if loc & OCG.LOCATION_ONFIELD else null
	var data: Dictionary = q if q is Dictionary and q.get("code", 0) == c.code else {}
	if label == "":
		var side := "" if c.get("controller", me) == me else " (rival)"
		label = OCG.location_name(loc) + side
	return {"code": c.code, "face_up": visible, "data": data, "label": label}


func _mark_candidates(cards: Array) -> void:
	_select_keys.clear()
	for c in cards:
		_select_keys[_key(c.controller, c.location, c.sequence)] = true
	_rebuild()


func _key(c: int, l: int, s: int) -> String:
	return "%d:%d:%d" % [c, l, s]


func _collect_idle_actions(msg: Dictionary) -> void:
	var add := func(e: Dictionary, text: String, resp: PackedByteArray):
		var k := _key(e.controller, e.location, e.sequence)
		if not _actions.has(k): _actions[k] = []
		_actions[k].append([text, resp])
	for e in msg.summonable: add.call(e, "Invocar de Modo Normal", OCGResponse.command(0, e.index))
	for e in msg.spsummonable: add.call(e, "Invocar de Modo Especial", OCGResponse.command(1, e.index))
	for e in msg.repositionable:
		var q = ctrl.get_card_at(e.controller, e.location, e.sequence)
		var txt := "Invocar por Volteo" if q and int(q.get("position", 0)) & OCG.POS_FACEDOWN else "Cambiar posición"
		add.call(e, txt, OCGResponse.command(2, e.index))
	for e in msg.msetable: add.call(e, "Colocar boca abajo", OCGResponse.command(3, e.index))
	for e in msg.ssetable: add.call(e, "Colocar", OCGResponse.command(4, e.index))
	for e in msg.activatable:
		var d := _desc(e.desc, e.code) if e.desc else ""
		add.call(e, "Activar" + (": " + d if d != "" else ""), OCGResponse.command(5, e.index))


func _collect_battle_actions(msg: Dictionary) -> void:
	for e in msg.activatable:
		var k := _key(e.controller, e.location, e.sequence)
		if not _actions.has(k): _actions[k] = []
		var d := _desc(e.desc, e.code) if e.desc else ""
		_actions[k].append(["Activar" + (": " + d if d != "" else ""), OCGResponse.command(0, e.index)])
	var opp_has_monsters: bool = ctrl.field[1 - me].mzone.any(func(c): return c != null)
	for e in msg.attackable:
		var k := _key(e.controller, e.location, e.sequence)
		if not _actions.has(k): _actions[k] = []
		var txt := "Ataque directo" if e.direct and not opp_has_monsters else "Atacar"
		_actions[k].append([txt, OCGResponse.command(1, e.index)])


func _update_buttons() -> void:
	var t: int = current_req.get("type", 0)
	btn_bp.disabled = not (t == OCG.MSG_SELECT_IDLECMD and current_req.to_bp)
	btn_m2.disabled = not (t == OCG.MSG_SELECT_BATTLECMD and current_req.to_m2)
	btn_ep.disabled = not ((t == OCG.MSG_SELECT_IDLECMD or t == OCG.MSG_SELECT_BATTLECMD) and current_req.to_ep)


func _on_end_turn() -> void:
	var t: int = current_req.get("type", 0)
	if t == OCG.MSG_SELECT_IDLECMD:
		_respond(OCGResponse.command(7))
	elif t == OCG.MSG_SELECT_BATTLECMD:
		_respond(OCGResponse.command(3))


func _on_card_clicked(view: CardView) -> void:
	_show_info(view)
	var k := _key(view.controller, view.location, view.sequence)
	if current_req.is_empty() or _dialog_busy:
		return
	if current_req.type in [OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD]:
		_on_zone_clicked(view.position + view.size / 2.0)
		return
	if not _actions.has(k):
		return
	_open_action_menu(_actions[k])


func _open_action_menu(actions: Array) -> void:
	action_menu.clear()
	_menu_responses.clear()
	for i in actions.size():
		action_menu.add_item(actions[i][0], i)
		_menu_responses.append(actions[i][1])
	action_menu.reset_size()
	action_menu.popup(Rect2i(Vector2i(get_viewport().get_mouse_position()), Vector2i.ZERO))


func _on_action_chosen(id: int) -> void:
	if id >= 0 and id < _menu_responses.size():
		_respond(_menu_responses[id])


# ---------------------------------------------------------------- selección de zona

func _start_place_selection(msg: Dictionary) -> void:
	_place_free = OCGResponse.free_zones(msg.player, msg.flag)
	_place_chosen.clear()
	if msg.type == OCG.MSG_SELECT_PLACE and auto_place_cb.button_pressed:
		var order := [2, 1, 3, 0, 4, 5, 6, 7]
		var mine := _place_free.filter(func(z): return z.player == me)
		var pool := mine if not mine.is_empty() else _place_free
		pool.sort_custom(func(a, b): return order.find(a.sequence) < order.find(b.sequence))
		_respond.call_deferred(OCGResponse.places(pool.slice(0, msg.count)))
		return
	var what := "zona(s) a inutilizar" if msg.type == OCG.MSG_SELECT_DISFIELD else "zona"
	prompt_label.text = "Elige %d %s (resaltadas en verde)." % [msg.count, what]
	zone_layer.queue_redraw()


func _on_zone_clicked(pos: Vector2) -> void:
	if current_req.is_empty():
		return
	if current_req.type in [OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD]:
		for z in _place_free:
			if _zone_rect(z.player, z.location, z.sequence).grow(6).has_point(pos):
				if z in _place_chosen:
					return
				_place_chosen.append(z)
				zone_layer.queue_redraw()
				if _place_chosen.size() >= current_req.count:
					_respond(OCGResponse.places(_place_chosen))
				return
		return
	# Clic en un montón: abrir el visor
	for p in 2:
		for loc in [OCG.LOCATION_GRAVE, OCG.LOCATION_REMOVED, OCG.LOCATION_EXTRA]:
			if _zone_rect(p, loc, 0).has_point(pos):
				_open_pile(p, loc)
				return


func _open_pile(p: int, loc: int) -> void:
	if _dialog_busy:
		return
	var side: Dictionary = ctrl.field[p]
	var list: Array = side.grave if loc == OCG.LOCATION_GRAVE else (side.removed if loc == OCG.LOCATION_REMOVED else side.extra)
	if list.is_empty():
		return
	var cards := []
	for i in list.size():
		var c: Dictionary = list[i]
		var vis := _is_visible(p, loc, c)
		var k := _key(p, loc, i)
		cards.append({"code": c.get("code", 0), "face_up": vis, "data": c if vis else {},
				"label": "▶ acciones" if _actions.has(k) else ""})
	_dialog_busy = true
	var title := "%s de %s (%d)" % [OCG.location_name(loc), ctrl.names[p], list.size()]
	var idx: int = await dialog.ask_pick(title, cards, [], "", "Cerrar")
	_dialog_busy = false
	if idx >= 0:
		var k := _key(p, loc, idx)
		if _actions.has(k) and not current_req.is_empty():
			_open_action_menu(_actions[k])


# ================================================================ dibujo del campo

func _relayout() -> void:
	var area := field_layer.size
	if area.x <= 0 or area.y <= 0:
		return
	_ch = floor(area.y / 7.25)
	_card_w = floor(min(_ch * 0.9 / CARD_RATIO, area.x / 10.5))
	_cw = max(_card_w + 12.0, floor(min(area.x / 9.4, _ch * 0.95)))
	_x0 = floor((area.x - 9.0 * _cw) / 2.0)
	_y0 = floor((area.y - 7.0 * _ch) / 2.0)
	if dialog.visible:
		dialog._recenter()
	_rebuild()


func _cell_rect(col: int, row: int) -> Rect2:
	var cx := _x0 + (col + 1.5) * _cw
	var cy := _y0 + (row + 0.5) * _ch
	var sz := Vector2(_card_w, _card_w * CARD_RATIO)
	return Rect2(Vector2(cx, cy) - sz / 2.0, sz)


## Rectángulo de una zona desde el punto de vista local (el rival aparece girado 180º).
func _zone_rect(p: int, loc: int, seq: int) -> Rect2:
	var local := p == me
	var col := 0
	var row := 0
	match loc:
		OCG.LOCATION_MZONE:
			if seq >= 5:
				col = (2 if seq == 5 else 4) if local else (4 if seq == 5 else 2)
				return _cell_rect(col, 3)
			col = seq + 1; row = 4
		OCG.LOCATION_SZONE:
			if seq == 5:
				col = 0; row = 4
			elif seq >= 6:
				col = 0 if seq == 6 else 6; row = 5
			else:
				col = seq + 1; row = 5
		OCG.LOCATION_GRAVE: col = 6; row = 4
		OCG.LOCATION_DECK: col = 6; row = 5
		OCG.LOCATION_EXTRA: col = 0; row = 5
		OCG.LOCATION_REMOVED: col = 7; row = 4
	if not local:
		col = 6 - col
		row = 6 - row
	return _cell_rect(col, row)


func _draw_zones(layer: Control) -> void:
	var font := get_theme_default_font()
	for p in 2:
		var zones := []
		for s in 5: zones.append([OCG.LOCATION_MZONE, s, ""])
		for s in 5: zones.append([OCG.LOCATION_SZONE, s, ""])
		zones.append([OCG.LOCATION_SZONE, 5, "Campo"])
		zones.append([OCG.LOCATION_GRAVE, 0, "Cementerio"])
		zones.append([OCG.LOCATION_DECK, 0, "Deck"])
		zones.append([OCG.LOCATION_EXTRA, 0, "Extra"])
		zones.append([OCG.LOCATION_REMOVED, 0, "Desterr."])
		if p == me:
			zones.append([OCG.LOCATION_MZONE, 5, "Zona Extra"])
			zones.append([OCG.LOCATION_MZONE, 6, "Zona Extra"])
		for z in zones:
			var r := _zone_rect(p, z[0], z[1])
			var col := COL_ZONE if p == me else Color(1, 0.6, 0.6, 0.16)
			layer.draw_rect(r, Color(col, 0.05))
			layer.draw_rect(r, col, false, 1.5)
			if z[2] != "":
				layer.draw_string(font, r.position + Vector2(0, r.size.y / 2.0), z[2], HORIZONTAL_ALIGNMENT_CENTER, r.size.x, 11, Color(1, 1, 1, 0.35))
	for z in _place_free:
		var r2 := _zone_rect(z.player, z.location, z.sequence)
		var chosen: bool = z in _place_chosen
		layer.draw_rect(r2.grow(3), COL_ZONE_FREE if not chosen else COL_HIGHLIGHT_SELECT, false, 3.0)
		layer.draw_rect(r2, Color(COL_ZONE_FREE, 0.15))


func _is_visible(p: int, loc: int, c: Dictionary) -> bool:
	var pos: int = c.get("position", 0)
	if loc & (OCG.LOCATION_GRAVE):
		return true
	if loc == OCG.LOCATION_REMOVED:
		return pos & OCG.POS_FACEUP or p == me
	if p == me:
		return true
	if loc & OCG.LOCATION_ONFIELD:
		return pos & OCG.POS_FACEUP
	if loc == OCG.LOCATION_EXTRA:
		return pos & OCG.POS_FACEUP
	return c.get("is_public", false)


func _rebuild() -> void:
	if ctrl == null or cards_layer == null:
		return
	for c in cards_layer.get_children():
		c.queue_free()
	zone_layer.queue_redraw()
	for p in 2:
		var side: Dictionary = ctrl.field[p]
		for i in side.mzone.size():
			if side.mzone[i] != null:
				_place_card(side.mzone[i], p, OCG.LOCATION_MZONE, i, _zone_rect(p, OCG.LOCATION_MZONE, i))
		for i in side.szone.size():
			if side.szone[i] != null and i <= 5:
				_place_card(side.szone[i], p, OCG.LOCATION_SZONE, i, _zone_rect(p, OCG.LOCATION_SZONE, i))
		_place_pile(p, OCG.LOCATION_DECK, side.deck, {})
		_place_pile(p, OCG.LOCATION_GRAVE, side.grave.size(), side.grave.back() if not side.grave.is_empty() else {})
		_place_pile(p, OCG.LOCATION_REMOVED, side.removed.size(), side.removed.back() if not side.removed.is_empty() else {})
		_place_pile(p, OCG.LOCATION_EXTRA, side.extra.size(), {})
		_place_hand(p, side.hand)
	_update_status()


func _place_card(c: Dictionary, p: int, loc: int, seq: int, rect: Rect2) -> CardView:
	var cv := CardView.new()
	cv.controller = p
	cv.location = loc
	cv.sequence = seq
	var vis := _is_visible(p, loc, c)
	cv.setup(c.get("code", 0), c, vis)
	cv.set_card_size(rect.size.x)
	cv.position = rect.position
	var pos: int = c.get("position", 0)
	if loc == OCG.LOCATION_MZONE:
		cv.defense_pos = (pos & OCG.POS_DEFENSE) != 0
		cv.show_stats = vis and (pos & OCG.POS_FACEUP) != 0
		if p == me and pos & OCG.POS_FACEDOWN:
			cv.modulate = Color(1, 1, 1, 0.75) # propia boca abajo: visible pero translúcida
	elif loc == OCG.LOCATION_SZONE and p == me and pos & OCG.POS_FACEDOWN:
		cv.modulate = Color(1, 1, 1, 0.75)
	var k := _key(p, loc, seq)
	if _actions.has(k):
		cv.highlight = COL_HIGHLIGHT_ACTION
	elif _select_keys.has(k):
		cv.highlight = COL_HIGHLIGHT_SELECT
	cv.clicked.connect(_on_card_clicked)
	cv.hovered.connect(_show_info)
	cards_layer.add_child(cv)
	return cv


func _place_pile(p: int, loc: int, count: int, top: Dictionary) -> void:
	if count <= 0:
		return
	var rect := _zone_rect(p, loc, 0)
	var vis := not top.is_empty() and _is_visible(p, loc, top)
	var cv := _place_card(top if vis else {}, p, loc, max(count - 1, 0), rect)
	cv.setup(top.get("code", 0) if vis else 0, top if vis else {}, vis)
	cv.mouse_filter = Control.MOUSE_FILTER_PASS
	cv.badge = ""
	var hl := false
	for i in count:
		if _actions.has(_key(p, loc, i)):
			hl = true
	cv.highlight = COL_HIGHLIGHT_ACTION if hl else Color.TRANSPARENT
	cv.clicked.disconnect(_on_card_clicked)
	cv.clicked.connect(func(_v): if loc != OCG.LOCATION_DECK: _open_pile(p, loc))
	var lbl := Label.new()
	lbl.text = str(count)
	lbl.add_theme_font_size_override("font_size", 13)
	var sb := StyleBoxFlat.new()
	sb.bg_color = Color(0, 0, 0, 0.8)
	sb.set_corner_radius_all(4)
	sb.content_margin_left = 4
	sb.content_margin_right = 4
	lbl.add_theme_stylebox_override("normal", sb)
	lbl.position = rect.position + Vector2(rect.size.x - 16, -8)
	lbl.mouse_filter = Control.MOUSE_FILTER_IGNORE
	cards_layer.add_child(lbl)


func _place_hand(p: int, hand: Array) -> void:
	var n := hand.size()
	if n == 0:
		return
	var row := 6 if p == me else 0
	var area_w := field_layer.size.x
	var step: float = min(_card_w + 8.0, (area_w * 0.92 - _card_w) / max(n - 1, 1))
	var total := step * (n - 1) + _card_w
	var x := (area_w - total) / 2.0
	var y := _y0 + (row + 0.5) * _ch - _card_w * CARD_RATIO / 2.0
	for i in n:
		var c: Dictionary = hand[i]
		var cv := _place_card(c, p, OCG.LOCATION_HAND, i, Rect2(Vector2(x + i * step, y), Vector2(_card_w, _card_w * CARD_RATIO)))
		if p == me:
			cv.mouse_entered.connect(func(): cv.position.y = y - 14)
			cv.mouse_exited.connect(func(): cv.position.y = y)


func _update_status() -> void:
	if ctrl == null:
		return
	for p in 2:
		var ui := _ui_index(p)
		name_labels[ui].text = ctrl.names[p] + ("  ◀ turno" if ctrl.turn_player == p and ctrl.turn > 0 else "")
		lp_labels[ui].text = "LP %d" % ctrl.field[p].lp
		lp_bars[ui].value = ctrl.field[p].lp
	turn_label.text = "Turno %d" % ctrl.turn
	phase_label.text = OCG.PHASE_NAMES.get(ctrl.phase, "")
	var links := []
	for i in ctrl.chain.size():
		links.append("%d. %s" % [i + 1, CardDB.card_name(ctrl.chain[i].code)])
	chain_label.text = ("Cadena: " + "  ".join(links)) if not links.is_empty() else ""


func _flash_lp(p: int, color: Color) -> void:
	var l := lp_labels[_ui_index(p)]
	l.modulate = color
	var tw := create_tween()
	tw.tween_property(l, "modulate", Color.WHITE, 0.6)


func _show_attack(msg: Dictionary) -> void:
	var a: Dictionary = msg.attacker
	var from := _zone_rect(a.controller, a.location, a.sequence).get_center()
	var to: Vector2
	if msg.direct:
		var opp: int = 1 - int(a.controller)
		to = Vector2(field_layer.size.x / 2.0, _y0 + (0.5 if opp != me else 6.5) * _ch)
	else:
		var t: Dictionary = msg.target
		to = _zone_rect(t.controller, t.location, t.sequence).get_center()
	_attack_line.points = PackedVector2Array([from, to])
	_attack_line.visible = true


# ================================================================ panel de información

func _show_info(view: CardView) -> void:
	if view == null:
		return
	if not view.face_up:
		info_view.setup(0, {}, false)
		info_name.text = "Carta boca abajo"
		info_type.text = ""
		info_stats.text = ""
		info_desc.text = ""
		return
	_show_info_data(view.code, view.data, true)


func _show_info_data(code: int, data: Dictionary, vis: bool) -> void:
	var base := CardDB.get_card(code)
	if base.is_empty():
		return
	var d := base.duplicate()
	for k in data:
		d[k] = data[k]
	info_view.setup(code, d, vis)
	info_name.text = base.get("name", "")
	var type: int = base.get("type", 0)
	info_type.text = OCG.type_line(type)
	if type & OCG.TYPE_MONSTER:
		var lvl := "Rango" if type & OCG.TYPE_XYZ else ("Link" if type & OCG.TYPE_LINK else "Nivel")
		info_type.text = "[%s] %s / %s" % [OCG.race_name(base.get("race", 0)), OCG.attribute_name(base.get("attribute", 0)), info_type.text]
		var level: int = d.get("rank", 0) if type & OCG.TYPE_XYZ and d.get("rank", 0) else d.get("level", base.get("level", 0))
		info_stats.text = "%s %d   ATK %s / DEF %s" % [lvl, level, _stat(d.get("attack", 0)), "-" if type & OCG.TYPE_LINK else _stat(d.get("defense", 0))]
	else:
		info_stats.text = ""
	info_desc.text = base.get("desc", "")


func _stat(v) -> String:
	return "?" if int(v) < 0 else str(v)
