class_name DuelBoard
extends Control
## Tablero de duelo con estética pixel-art inspirada en los Yu-Gi-Oh! de GBA (serie WCT):
## tapete azul, zonas biseladas, cartas pixeladas, ventanas azules, cursor parpadeante y animaciones.

## Configuración que deja el menú: {deck0, deck1, first ("random"/"me"/"cpu"), lp, seed, speed}
static var config := {}

const CARD_RATIO := CardView.CARD_RATIO
const COL_ACTION := Color("40e0ff")
const COL_SELECT := Color("ffe040")
const COL_ZONE_FREE := Color("50ff70")
const PHASES := [["DP", OCG.PHASE_DRAW], ["SP", OCG.PHASE_STANDBY], ["M1", OCG.PHASE_MAIN1],
		["BP", OCG.PHASE_BATTLE_START | OCG.PHASE_BATTLE_STEP | OCG.PHASE_DAMAGE | OCG.PHASE_DAMAGE_CAL | OCG.PHASE_BATTLE],
		["M2", OCG.PHASE_MAIN2], ["EP", OCG.PHASE_END]]

var ctrl: DuelController
var fx: BoardFx
var me := 0

var current_req := {}
var _actions := {}
var _select_keys := {}
var _place_free: Array = []
var _place_chosen: Array = []
var _menu_responses: Array = []
var _dialog_busy := false
var views := {} # "c:l:s" -> CardView (campo y manos)
var lp_shown := [8000, 8000]
var lp_animating := [false, false]

# Geometría
var cw := 90.0
var ch := 120.0
var card_w := 66.0
var x0 := 0.0
var y0 := 0.0

# Nodos
var field_layer: Control
var zone_layer: ZoneLayer
var cards_layer: Control
var fx_layer: Control
var cursor: CursorFx
var info_view: CardView
var info_name: Label
var info_type: Label
var info_level: HBoxContainer
var info_atk: Label
var info_def: Label
var info_stats_box: HBoxContainer
var info_desc: RichTextLabel
var name_labels: Array[Label] = []
var lp_labels: Array[Label] = []
var lp_bars: Array[ProgressBar] = []
var player_boxes: Array[PanelContainer] = []
var turn_label: Label
var phase_badges: Array[Label] = []
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
var speed_opt: OptionButton
var sound_cb: CheckBox
var _end_panel: PanelContainer

# Selección de cartas directamente en el campo (objetivos, ataques, sacrificios...)
signal _fsel_done(result)
const COL_PICKED := Color("60ff80")
var _fsel := {}
var sel_row: HBoxContainer
var sel_confirm: Button
var sel_cancel: Button


class ZoneLayer:
	extends Control
	var board: DuelBoard

	func _draw() -> void:
		board._draw_zones(self)

	func _gui_input(event: InputEvent) -> void:
		if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
			board._on_zone_clicked(event.position)


## Cursor de esquinas parpadeante, como el de los juegos de GBA.
class CursorFx:
	extends Control
	var target := Rect2()
	var active := false
	var _t := 0.0

	func _init() -> void:
		mouse_filter = Control.MOUSE_FILTER_IGNORE

	func point(r: Rect2) -> void:
		target = r
		active = true
		queue_redraw()

	func hide_cursor() -> void:
		active = false
		queue_redraw()

	func _process(delta: float) -> void:
		if active:
			_t += delta
			queue_redraw()

	func _draw() -> void:
		if not active:
			return
		var off := 3.0 + 3.0 * (0.5 + 0.5 * sin(_t * 9.0))
		var r := target.grow(off)
		var l := 12.0
		var w := 3.0
		var col := GBA.C_YELLOW if fmod(_t, 0.5) < 0.38 else Color("ff9020")
		for c in [[r.position, Vector2(1, 1)], [Vector2(r.end.x, r.position.y), Vector2(-1, 1)],
				[Vector2(r.position.x, r.end.y), Vector2(1, -1)], [r.end, Vector2(-1, -1)]]:
			var p: Vector2 = c[0]
			var d: Vector2 = c[1]
			# Sombra + esquina
			draw_rect(Rect2(p + Vector2(min(0.0, d.x * l), min(0.0, d.y * w)) + Vector2(1, 1), Vector2(l, w)), GBA.C_OUTLINE)
			draw_rect(Rect2(p + Vector2(min(0.0, d.x * w), min(0.0, d.y * l)) + Vector2(1, 1), Vector2(w, l)), GBA.C_OUTLINE)
			draw_rect(Rect2(p + Vector2(min(0.0, d.x * l), min(0.0, d.y * w)), Vector2(l, w)), col)
			draw_rect(Rect2(p + Vector2(min(0.0, d.x * w), min(0.0, d.y * l)), Vector2(w, l)), col)


func _ready() -> void:
	theme = GBA.theme
	texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	_build_ui()
	info_stats_box.visible = false
	_update_buttons()
	resized.connect(_relayout)
	_start_duel()


# ================================================================ construcción de la interfaz

func _build_ui() -> void:
	var bg := ColorRect.new()
	bg.color = GBA.C_NAVY_DARK
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	bg.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(bg)

	var root := HBoxContainer.new()
	root.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_theme_constant_override("separation", 0)
	add_child(root)

	# --- Panel izquierdo: carta señalada
	var left := VBoxContainer.new()
	left.custom_minimum_size.x = 318
	left.add_theme_constant_override("separation", 4)
	root.add_child(_margin(left, 6))
	var pic_box := PanelContainer.new()
	pic_box.add_theme_stylebox_override("panel", GBA.window_box(10))
	left.add_child(pic_box)
	var cc := CenterContainer.new()
	pic_box.add_child(cc)
	info_view = CardView.new()
	info_view.full_art = true
	info_view.set_card_size(264)
	info_view.mouse_filter = Control.MOUSE_FILTER_IGNORE
	info_view.setup(0, {}, false)
	cc.add_child(info_view)
	var txt_box := PanelContainer.new()
	txt_box.add_theme_stylebox_override("panel", GBA.window_dark_box(12))
	txt_box.size_flags_vertical = Control.SIZE_EXPAND_FILL
	left.add_child(txt_box)
	var tv := VBoxContainer.new()
	tv.add_theme_constant_override("separation", 3)
	txt_box.add_child(tv)
	info_name = Label.new()
	info_name.add_theme_font_size_override("font_size", 25)
	info_name.add_theme_color_override("font_color", GBA.C_YELLOW)
	info_name.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	tv.add_child(info_name)
	info_type = Label.new()
	info_type.add_theme_color_override("font_color", Color("a0c8ff"))
	info_type.add_theme_font_size_override("font_size", 18)
	info_type.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	tv.add_child(info_type)
	info_level = HBoxContainer.new()
	info_level.add_theme_constant_override("separation", 1)
	tv.add_child(info_level)
	info_stats_box = HBoxContainer.new()
	info_stats_box.add_theme_constant_override("separation", 6)
	tv.add_child(info_stats_box)
	info_stats_box.add_child(_icon("icon_atk"))
	info_atk = GBA.big_label("", 16)
	info_stats_box.add_child(info_atk)
	info_stats_box.add_child(_icon("icon_def"))
	info_def = GBA.big_label("", 16)
	info_stats_box.add_child(info_def)
	info_desc = RichTextLabel.new()
	info_desc.size_flags_vertical = Control.SIZE_EXPAND_FILL
	info_desc.add_theme_font_size_override("normal_font_size", 19)
	tv.add_child(info_desc)

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
	cursor = CursorFx.new()
	cursor.set_anchors_preset(Control.PRESET_FULL_RECT)
	field_layer.add_child(cursor)
	fx_layer = Control.new()
	fx_layer.set_anchors_preset(Control.PRESET_FULL_RECT)
	fx_layer.mouse_filter = Control.MOUSE_FILTER_IGNORE
	field_layer.add_child(fx_layer)
	field_layer.resized.connect(_relayout)

	dialog = PromptDialog.new()
	dialog.card_hovered.connect(_on_card_hovered)
	field_layer.add_child(dialog)

	action_menu = PopupMenu.new()
	action_menu.id_pressed.connect(_on_action_chosen)
	add_child(action_menu)

	# --- Panel derecho
	var rv := VBoxContainer.new()
	rv.custom_minimum_size.x = 330
	rv.add_theme_constant_override("separation", 5)
	root.add_child(_margin(rv, 6))
	for i in 2:
		var box := PanelContainer.new()
		box.add_theme_stylebox_override("panel", GBA.window_box(10))
		var v := VBoxContainer.new()
		v.add_theme_constant_override("separation", 2)
		box.add_child(v)
		var nl := Label.new()
		nl.add_theme_font_size_override("font_size", 22)
		v.add_child(nl)
		var h := HBoxContainer.new()
		h.add_theme_constant_override("separation", 8)
		v.add_child(h)
		var lp_icon := GBA.big_label("LP", 20, GBA.C_RED)
		h.add_child(lp_icon)
		var ll := GBA.big_label("8000", 34, GBA.C_WHITE)
		h.add_child(ll)
		var bar := ProgressBar.new()
		bar.max_value = 8000
		bar.value = 8000
		bar.show_percentage = false
		bar.custom_minimum_size.y = 12
		v.add_child(bar)
		name_labels.append(nl)
		lp_labels.append(ll)
		lp_bars.append(bar)
		player_boxes.append(box)
		rv.add_child(box)
		if i == 0:
			var mid := PanelContainer.new()
			mid.add_theme_stylebox_override("panel", GBA.window_dark_box(8))
			rv.add_child(mid)
			var mv := VBoxContainer.new()
			mid.add_child(mv)
			turn_label = GBA.big_label("TURNO 0", 18, GBA.C_GOLD)
			turn_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
			mv.add_child(turn_label)
			var strip := HBoxContainer.new()
			strip.alignment = BoxContainer.ALIGNMENT_CENTER
			strip.add_theme_constant_override("separation", 3)
			mv.add_child(strip)
			for ph in PHASES:
				var b := GBA.big_label(ph[0], 16, Color("50608a"))
				b.custom_minimum_size = Vector2(44, 28)
				b.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
				b.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
				var sb := StyleBoxFlat.new()
				sb.bg_color = Color("0a1030")
				sb.border_color = Color("2a3a70")
				sb.set_border_width_all(2)
				sb.anti_aliasing = false
				b.add_theme_stylebox_override("normal", sb)
				strip.add_child(b)
				phase_badges.append(b)
	var hb := HBoxContainer.new()
	hb.add_theme_constant_override("separation", 4)
	rv.add_child(hb)
	btn_bp = _button(hb, "Batalla", func(): _respond(OCGResponse.command(6)))
	btn_m2 = _button(hb, "Main 2", func(): _respond(OCGResponse.command(2)))
	btn_ep = _button(hb, "Fin turno", _on_end_turn)
	prompt_label = Label.new()
	prompt_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	prompt_label.add_theme_color_override("font_color", Color("80ffa0"))
	rv.add_child(prompt_label)
	sel_row = HBoxContainer.new()
	sel_row.add_theme_constant_override("separation", 4)
	sel_row.visible = false
	rv.add_child(sel_row)
	sel_confirm = _button(sel_row, "Confirmar", func(): _fsel_finish(true))
	sel_cancel = _button(sel_row, "Cancelar", func(): _fsel_finish(false))
	chain_label = Label.new()
	chain_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	chain_label.add_theme_color_override("font_color", Color("80e0ff"))
	rv.add_child(chain_label)
	var log_panel := PanelContainer.new()
	log_panel.add_theme_stylebox_override("panel", GBA.window_dark_box(10))
	log_panel.size_flags_vertical = Control.SIZE_EXPAND_FILL
	rv.add_child(log_panel)
	log_box = RichTextLabel.new()
	log_box.bbcode_enabled = true
	log_box.scroll_following = true
	log_box.add_theme_font_size_override("normal_font_size", 18)
	log_box.add_theme_font_size_override("bold_font_size", 18)
	log_panel.add_child(log_box)
	auto_place_cb = CheckBox.new()
	auto_place_cb.text = "Colocar en zona automáticamente"
	auto_place_cb.button_pressed = true
	rv.add_child(auto_place_cb)
	auto_chain_cb = CheckBox.new()
	auto_chain_cb.text = "No preguntar por cadenas"
	rv.add_child(auto_chain_cb)
	var opt_row := HBoxContainer.new()
	rv.add_child(opt_row)
	sound_cb = CheckBox.new()
	sound_cb.text = "Sonido"
	sound_cb.button_pressed = Sfx.enabled
	sound_cb.toggled.connect(func(on): Sfx.enabled = on)
	opt_row.add_child(sound_cb)
	speed_opt = OptionButton.new()
	for t in ["Animación normal", "Animación rápida", "Sin animación"]:
		speed_opt.add_item(t)
	speed_opt.select(int(config.get("speed", 0)))
	speed_opt.item_selected.connect(func(i): if fx: fx.set_speed_mode(i))
	speed_opt.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	opt_row.add_child(speed_opt)
	var hb2 := HBoxContainer.new()
	hb2.add_theme_constant_override("separation", 4)
	rv.add_child(hb2)
	_button(hb2, "Rendirse", _on_surrender)
	_button(hb2, "Menú", func(): _go_menu())


func _margin(c: Control, m: int) -> MarginContainer:
	var mc := MarginContainer.new()
	for side in ["left", "right", "top", "bottom"]:
		mc.add_theme_constant_override("margin_" + side, m)
	mc.add_child(c)
	return mc


func _icon(name: String) -> TextureRect:
	var t := TextureRect.new()
	t.texture = GBA.tex(name)
	t.stretch_mode = TextureRect.STRETCH_KEEP_CENTERED
	t.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	return t


func _button(parent: Control, text: String, cb: Callable) -> Button:
	var b := Button.new()
	b.text = text
	b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	b.custom_minimum_size.y = 38
	b.pressed.connect(func(): Sfx.play("select"))
	b.pressed.connect(cb)
	parent.add_child(b)
	return b


# ================================================================ duelo

func _start_duel() -> void:
	var cfg := config
	var decks := CardDB.deck_paths()
	if decks.is_empty():
		_log("[color=red]No hay mazos en la carpeta Decks[/color]")
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
	fx = BoardFx.new(self)
	fx.set_speed_mode(int(cfg.get("speed", 0)))
	ctrl.animator = fx
	ctrl.input_requested.connect(_on_input_requested)
	ctrl.field_updated.connect(_rebuild)
	ctrl.message_received.connect(_on_message)
	ctrl.log_line.connect(_log)
	ctrl.duel_finished.connect(_on_duel_finished)
	var lp: int = cfg.get("lp", 8000)
	lp_shown = [lp, lp]
	if not ctrl.setup(d0, d1, {"seed": seed, "ai": [me != 0, me != 1], "lp": lp}):
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
		OCG.MSG_CHAINING, OCG.MSG_SUMMONING, OCG.MSG_SPSUMMONING, OCG.MSG_FLIPSUMMONING:
			if msg.code:
				_show_info_data(msg.code, {}, true)
	_update_status()


func _on_duel_finished(winner: int, reason: int) -> void:
	current_req = {}
	_update_buttons()
	_rebuild()
	cursor.hide_cursor()
	var won := winner == me
	await fx.end_banner(won, winner < 0)
	_end_panel = PanelContainer.new()
	_end_panel.add_theme_stylebox_override("panel", GBA.window_box(22))
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 12)
	_end_panel.add_child(v)
	var l := GBA.big_label("¡VICTORIA!" if won else ("DERROTA" if winner >= 0 else "EMPATE"), 40, GBA.C_YELLOW if won else Color("ff8070"))
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
	_end_panel.position = ((field_layer.size - _end_panel.size) / 2.0).round()


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
	_refresh_highlights()
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
	_refresh_highlights()
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
				Sfx.play("chain")
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
			var sel
			if _on_board(msg.cards):
				sel = await _field_select(_hint_text(), msg.cards, msg.min, msg.max, msg.cancelable)
			else:
				_mark_candidates(msg.cards)
				var cards2: Array = msg.cards.map(func(c): return _entry(c))
				sel = await dialog.ask_cards(_hint_text(), cards2, msg.min, msg.max, msg.cancelable)
			resp = OCGResponse.cancel() if sel == null else OCGResponse.cards(sel.map(func(i): return msg.cards[i].index))
		OCG.MSG_SELECT_TRIBUTE:
			_mark_candidates(msg.cards)
			var cards3: Array = msg.cards.map(func(c): return _entry(c))
			var validator := func(sel: Array) -> bool:
				var total := 0
				for i in sel: total += int(msg.cards[i].release_param)
				return total >= msg.min and sel.size() <= msg.max and not sel.is_empty()
			var sel3
			if _on_board(msg.cards):
				sel3 = await _field_select(_hint_text("Selecciona los monstruos a sacrificar"), msg.cards, 1, msg.max, msg.cancelable, validator)
			else:
				sel3 = await dialog.ask_cards(_hint_text("Selecciona los monstruos a sacrificar"), cards3, 1, msg.max, msg.cancelable, validator)
			resp = OCGResponse.cancel() if sel3 == null else OCGResponse.cards(sel3.map(func(i): return msg.cards[i].index))
		OCG.MSG_SELECT_UNSELECT_CARD:
			var all: Array = msg.select_cards + msg.unselect_cards
			_mark_candidates(all)
			var pre := range(msg.select_cards.size(), all.size())
			var idx4: int
			if _on_board(all):
				idx4 = await _field_select(_hint_text() + "  (mín. %d, máx. %d)" % [msg.min, msg.max], all, 1, 1,
						msg.cancelable or msg.finishable, Callable(), true, pre, "Terminar" if msg.finishable else "Cancelar")
			else:
				idx4 = await dialog.ask_pick(_hint_text(), all.map(func(c): return _entry(c)), pre,
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
			var sel5
			if msg.must_cards.is_empty() and _on_board(msg.cards):
				sel5 = await _field_select("%s (suma %d)" % [_hint_text(), msg.acc], msg.cards, msg.min, max(msg.max, msg.cards.size()), false, validator5)
			else:
				sel5 = await dialog.ask_cards("%s (suma %d)" % [_hint_text(), msg.acc], all5.map(func(c): return _entry(c, "%d" % (int(c.param) & 0xffff))),
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
		_select_keys[key(c.controller, c.location, c.sequence)] = true
	_refresh_highlights()


func key(c: int, l: int, s: int) -> String:
	return "%d:%d:%d" % [c, l & ~OCG.LOCATION_OVERLAY, s]


func _collect_idle_actions(msg: Dictionary) -> void:
	var add := func(e: Dictionary, text: String, resp: PackedByteArray):
		var k := key(e.controller, e.location, e.sequence)
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
		var k := key(e.controller, e.location, e.sequence)
		if not _actions.has(k): _actions[k] = []
		var d := _desc(e.desc, e.code) if e.desc else ""
		_actions[k].append(["Activar" + (": " + d if d != "" else ""), OCGResponse.command(0, e.index)])
	var opp_has_monsters: bool = ctrl.field[1 - me].mzone.any(func(c): return c != null)
	for e in msg.attackable:
		var k := key(e.controller, e.location, e.sequence)
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
	_on_card_hovered(view)
	var k := key(view.controller, view.location, view.sequence)
	if not _fsel.is_empty():
		if _fsel.keys.has(k):
			_fsel_click(_fsel.keys[k])
		return
	if current_req.is_empty() or _dialog_busy:
		return
	if current_req.type in [OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD]:
		_on_zone_clicked(view.position + view.size / 2.0)
		return
	if not _actions.has(k):
		return
	Sfx.play("blip")
	_open_action_menu(_actions[k])


## Al pasar el cursor: muestra la carta, mueve el cursor y descarga su imagen si aún no está en pics/.
func _on_card_hovered(view: CardView) -> void:
	if view == null:
		return
	if view.get_parent() == cards_layer:
		cursor.point(Rect2(view.position, view.size))
	if view.face_up:
		CardImages.ensure(view.code, true)
	_show_info(view)
	if view.peek_only:
		info_name.text += "  (colocada)"


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


# ---------------------------------------------------------------- selección en el campo

## True si todas las cartas están en el campo o en la mano (se pueden señalar en el tablero).
func _on_board(cards: Array) -> bool:
	if cards.is_empty():
		return false
	for c in cards:
		var loc := int(c.get("location", 0))
		if loc & OCG.LOCATION_OVERLAY or not loc & (OCG.LOCATION_ONFIELD | OCG.LOCATION_HAND):
			return false
		if not views.has(key(int(c.controller), loc, int(c.sequence))):
			return false
	return true


## Selección haciendo clic en las cartas del tablero. Devuelve Array de índices (o null si se cancela);
## en modo `pick` devuelve el índice de la carta pulsada o -1 (terminar/cancelar).
func _field_select(title: String, cards: Array, p_min: int, p_max: int, cancelable: bool,
		validator := Callable(), pick := false, preselected := [], cancel_text := "Cancelar"):
	var keys := {}
	for i in cards.size():
		keys[key(int(cards[i].controller), int(cards[i].location), int(cards[i].sequence))] = i
	_fsel = {"keys": keys, "picked": preselected.duplicate(), "min": p_min, "max": p_max,
			"validator": validator, "pick": pick, "title": title}
	sel_cancel.text = cancel_text
	sel_cancel.visible = cancelable
	sel_confirm.visible = not pick and not (p_max == 1 and p_min <= 1 and not validator.is_valid())
	sel_row.visible = sel_cancel.visible or sel_confirm.visible
	Sfx.play("chain")
	_fsel_update()
	var result = await _fsel_done
	_fsel = {}
	sel_row.visible = false
	prompt_label.text = ""
	_refresh_highlights()
	return result


func _fsel_click(i: int) -> void:
	Sfx.play("select")
	if _fsel.pick:
		_fsel_done.emit(i)
		return
	var picked: Array = _fsel.picked
	if i in picked:
		picked.erase(i)
	elif picked.size() < _fsel.max:
		picked.append(i)
	if _fsel.max == 1 and _fsel.min <= 1 and not _fsel.validator.is_valid():
		_fsel_done.emit([i])
		return
	_fsel_update()


func _fsel_update() -> void:
	var picked: Array = _fsel.picked
	var ok: bool = picked.size() >= _fsel.min and picked.size() <= _fsel.max
	if _fsel.validator.is_valid():
		ok = _fsel.validator.call(picked)
	sel_confirm.disabled = not ok
	var extra := "" if _fsel.pick else "  [%d/%d]" % [picked.size(), _fsel.max]
	prompt_label.text = "%s%s\nHaz clic en las cartas resaltadas del campo." % [_fsel.title, extra]
	_refresh_highlights()


func _fsel_finish(confirm: bool) -> void:
	if _fsel.is_empty():
		return
	if _fsel.pick:
		_fsel_done.emit(-1)
	elif confirm:
		_fsel_done.emit(_fsel.picked.duplicate())
	else:
		_fsel_done.emit(null)


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
	if not current_req.is_empty() and current_req.type in [OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD]:
		for z in _place_free:
			if zone_rect(z.player, z.location, z.sequence).grow(6).has_point(pos):
				if z in _place_chosen:
					return
				Sfx.play("select")
				_place_chosen.append(z)
				zone_layer.queue_redraw()
				if _place_chosen.size() >= current_req.count:
					_respond(OCGResponse.places(_place_chosen))
				return
		return
	for p in 2:
		for loc in [OCG.LOCATION_GRAVE, OCG.LOCATION_REMOVED, OCG.LOCATION_EXTRA]:
			if zone_rect(p, loc, 0).has_point(pos):
				_open_pile(p, loc)
				return


func _open_pile(p: int, loc: int) -> void:
	if _dialog_busy:
		return
	var side: Dictionary = ctrl.field[p]
	var list: Array = side.grave if loc == OCG.LOCATION_GRAVE else (side.removed if loc == OCG.LOCATION_REMOVED else side.extra)
	if list.is_empty():
		return
	Sfx.play("blip")
	var cards := []
	for i in list.size():
		var c: Dictionary = list[i]
		var vis := is_visible_card(p, loc, c)
		var k := key(p, loc, i)
		cards.append({"code": c.get("code", 0), "face_up": vis, "data": c if vis else {},
				"label": "▶ acciones" if _actions.has(k) else ""})
	_dialog_busy = true
	var title := "%s de %s (%d)" % [OCG.location_name(loc), ctrl.names[p], list.size()]
	var idx: int = await dialog.ask_pick(title, cards, [], "", "Cerrar")
	_dialog_busy = false
	if idx >= 0:
		var k := key(p, loc, idx)
		if _actions.has(k) and not current_req.is_empty():
			_open_action_menu(_actions[k])


# ================================================================ dibujo del campo

func _relayout() -> void:
	var area := field_layer.size
	if area.x <= 0 or area.y <= 0:
		return
	ch = floor(area.y / 7.2)
	var w: float = min(ch * 0.88 / CARD_RATIO, area.x / 10.0)
	# Tamaño múltiplo del sprite (22 "píxeles") para que el pixel-art quede nítido
	card_w = max(CardView.GRID_W * 2, floor(w / CardView.GRID_W) * CardView.GRID_W)
	cw = max(card_w + 12.0, floor(min(area.x / 9.3, ch * 0.92)))
	x0 = floor((area.x - 9.0 * cw) / 2.0)
	y0 = floor((area.y - 7.0 * ch) / 2.0)
	if dialog.visible:
		dialog._recenter()
	_rebuild()


func _cell_rect(col: int, row: int) -> Rect2:
	var cx := x0 + (col + 1.5) * cw
	var cy := y0 + (row + 0.5) * ch
	var sz := Vector2(card_w, round(card_w * CARD_RATIO))
	return Rect2((Vector2(cx, cy) - sz / 2.0).round(), sz)


## Rectángulo de una zona desde el punto de vista local (el rival aparece girado 180º).
func zone_rect(p: int, loc: int, seq: int) -> Rect2:
	var local := p == me
	var col := 0
	var row := 0
	match loc & ~OCG.LOCATION_OVERLAY:
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
		OCG.LOCATION_HAND:
			return hand_rect(p, seq)
	if not local:
		col = 6 - col
		row = 6 - row
	return _cell_rect(col, row)


func hand_rect(p: int, i: int) -> Rect2:
	var n: int = max(1, ctrl.field[p].hand.size()) if ctrl else 1
	var row := 6 if p == me else 0
	var area_w := field_layer.size.x
	var step: float = min(card_w + 8.0, (area_w * 0.9 - card_w) / max(n - 1, 1))
	var total := step * (n - 1) + card_w
	var x := (area_w - total) / 2.0
	var y := y0 + (row + 0.5) * ch - card_w * CARD_RATIO / 2.0
	return Rect2(Vector2(x + i * step, y).round(), Vector2(card_w, round(card_w * CARD_RATIO)))


func _draw_zones(layer: Control) -> void:
	var area := layer.size
	# Tapete
	var tile := GBA.tex("mat_tile")
	layer.draw_texture_rect(tile, Rect2(Vector2.ZERO, area), true)
	# Franja central (Zonas Extra) y separación de los dos lados
	var band := Rect2(0, y0 + 3 * ch, area.x, ch)
	layer.draw_rect(band, Color(0.02, 0.03, 0.1, 0.55))
	for yy in [band.position.y, band.end.y]:
		layer.draw_rect(Rect2(0, yy - 3, area.x, 3), GBA.C_GOLD_DARK)
		layer.draw_rect(Rect2(0, yy - 3, area.x, 1), GBA.C_GOLD)
	# Rectángulos de cada lado
	for p in 2:
		var top_row := 4 if p == me else 1
		var r := Rect2(x0 + 0.5 * cw - 6, y0 + top_row * ch - 4, 7 * cw + 12, 2 * ch + 8)
		layer.draw_rect(r, Color(0.0, 0.0, 0.0, 0.25))
		layer.draw_rect(r, Color(GBA.C_WIN_EDGE, 0.35) if p == me else Color(1, 0.5, 0.45, 0.3), false, 3)
	for p in 2:
		var zones := []
		for s in 5: zones.append([OCG.LOCATION_MZONE, s, "zone_monster"])
		for s in 5: zones.append([OCG.LOCATION_SZONE, s, "zone_spell"])
		zones.append([OCG.LOCATION_SZONE, 5, "zone_field"])
		zones.append([OCG.LOCATION_GRAVE, 0, "zone_grave"])
		zones.append([OCG.LOCATION_DECK, 0, "zone_deck"])
		zones.append([OCG.LOCATION_EXTRA, 0, "zone_extra"])
		zones.append([OCG.LOCATION_REMOVED, 0, "zone_banish"])
		if p == me:
			zones.append([OCG.LOCATION_MZONE, 5, "zone_emz"])
			zones.append([OCG.LOCATION_MZONE, 6, "zone_emz"])
		for z in zones:
			var zr := zone_rect(p, z[0], z[1])
			layer.draw_texture_rect(GBA.tex(z[2]), zr, false, Color(1, 1, 1, 1) if p == me else Color(1, 0.85, 0.85, 1))
	for z in _place_free:
		var r2 := zone_rect(z.player, z.location, z.sequence)
		var chosen: bool = z in _place_chosen
		var col := COL_ZONE_FREE if not chosen else COL_SELECT
		var blink := 0.25 + 0.15 * sin(Time.get_ticks_msec() / 120.0)
		layer.draw_rect(r2, Color(col, blink))
		layer.draw_rect(r2.grow(3), col, false, 3.0)
	if not _place_free.is_empty():
		layer.queue_redraw.call_deferred()


func is_visible_card(p: int, loc: int, c: Dictionary) -> bool:
	var pos: int = c.get("position", 0)
	if loc & OCG.LOCATION_GRAVE:
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
		cards_layer.remove_child(c)
		c.queue_free()
	views.clear()
	cursor.hide_cursor()
	zone_layer.queue_redraw()
	for p in 2:
		var side: Dictionary = ctrl.field[p]
		for i in side.mzone.size():
			if side.mzone[i] != null:
				_place_card(side.mzone[i], p, OCG.LOCATION_MZONE, i, zone_rect(p, OCG.LOCATION_MZONE, i))
		for i in side.szone.size():
			if side.szone[i] != null and i <= 5:
				_place_card(side.szone[i], p, OCG.LOCATION_SZONE, i, zone_rect(p, OCG.LOCATION_SZONE, i))
		_place_pile(p, OCG.LOCATION_DECK, side.deck, {})
		_place_pile(p, OCG.LOCATION_GRAVE, side.grave.size(), side.grave.back() if not side.grave.is_empty() else {})
		_place_pile(p, OCG.LOCATION_REMOVED, side.removed.size(), side.removed.back() if not side.removed.is_empty() else {})
		_place_pile(p, OCG.LOCATION_EXTRA, side.extra.size(), {})
		for i in side.hand.size():
			var cv := _place_card(side.hand[i], p, OCG.LOCATION_HAND, i, hand_rect(p, i))
			if p == me:
				var y := cv.position.y
				cv.mouse_entered.connect(func(): cv.position.y = y - 14)
				cv.mouse_exited.connect(func(): cv.position.y = y)
	_refresh_highlights()
	_update_status()


func _place_card(c: Dictionary, p: int, loc: int, seq: int, rect: Rect2) -> CardView:
	var cv := CardView.new()
	cv.controller = p
	cv.location = loc
	cv.sequence = seq
	var vis := is_visible_card(p, loc, c)
	cv.set_card_size(rect.size.x)
	cv.setup(c.get("code", 0), c, vis)
	cv.position = rect.position
	var pos: int = c.get("position", 0)
	if loc == OCG.LOCATION_MZONE:
		cv.defense_pos = (pos & OCG.POS_DEFENSE) != 0
	# Las cartas colocadas propias sólo se revelan al pasar el cursor
	if loc & OCG.LOCATION_ONFIELD and p == me and pos & OCG.POS_FACEDOWN:
		cv.peek_only = true
	cv.clicked.connect(_on_card_clicked)
	cv.hovered.connect(_on_card_hovered)
	cv.unhovered.connect(func(_v): cursor.hide_cursor())
	cards_layer.add_child(cv)
	views[key(p, loc, seq)] = cv
	return cv


func _place_pile(p: int, loc: int, count: int, top: Dictionary) -> void:
	if count <= 0:
		return
	var rect := zone_rect(p, loc, 0)
	var vis := not top.is_empty() and is_visible_card(p, loc, top)
	# Grosor del montón
	var depth: int = min(4, int(ceil(count / 10.0)))
	for i in range(depth, 0, -1):
		var back := ColorRect.new()
		back.color = Color("2a1206")
		back.position = rect.position + Vector2(i * 2, -i * 2)
		back.size = rect.size
		back.mouse_filter = Control.MOUSE_FILTER_IGNORE
		cards_layer.add_child(back)
	var cv := CardView.new()
	cv.controller = p
	cv.location = loc
	cv.sequence = count - 1
	cv.set_card_size(rect.size.x)
	cv.setup(top.get("code", 0) if vis else 0, top if vis else {}, vis)
	cv.position = rect.position
	cv.mouse_filter = Control.MOUSE_FILTER_PASS
	var hl := false
	for i in count:
		if _actions.has(key(p, loc, i)):
			hl = true
	cv.highlight = COL_ACTION if hl else Color.TRANSPARENT
	cv.hovered.connect(_on_card_hovered)
	cv.unhovered.connect(func(_v): cursor.hide_cursor())
	cv.clicked.connect(func(_v): if loc != OCG.LOCATION_DECK: _open_pile(p, loc))
	cards_layer.add_child(cv)
	views["pile:%d:%d" % [p, loc]] = cv
	var lbl := GBA.big_label(str(count), 14)
	var sb := StyleBoxFlat.new()
	sb.bg_color = GBA.C_OUTLINE
	sb.border_color = GBA.C_WHITE
	sb.set_border_width_all(2)
	sb.anti_aliasing = false
	sb.content_margin_left = 4
	sb.content_margin_right = 4
	lbl.add_theme_stylebox_override("normal", sb)
	lbl.position = rect.position + Vector2(rect.size.x - 18, rect.size.y - 14)
	lbl.mouse_filter = Control.MOUSE_FILTER_IGNORE
	cards_layer.add_child(lbl)


## Resalta las cartas con acciones (azul) y las candidatas de una selección (amarillo).
func _refresh_highlights() -> void:
	for k in views:
		var cv: CardView = views[k]
		if not is_instance_valid(cv) or k.begins_with("pile"):
			continue
		var col := Color.TRANSPARENT
		if not _fsel.is_empty() and _fsel.keys.has(k):
			col = COL_PICKED if _fsel.keys[k] in _fsel.picked else COL_SELECT
		elif _actions.has(k):
			col = COL_ACTION
		elif _select_keys.has(k):
			col = COL_SELECT
		if cv.highlight != col:
			cv.highlight = col
			cv.queue_redraw()


func _update_status() -> void:
	if ctrl == null:
		return
	for p in 2:
		var ui := _ui_index(p)
		var active: bool = ctrl.turn_player == p and ctrl.turn > 0
		name_labels[ui].text = ("» " if active else "") + ctrl.names[p]
		name_labels[ui].add_theme_color_override("font_color", GBA.C_YELLOW if active else GBA.C_WHITE)
		if not lp_animating[p]:
			lp_shown[p] = ctrl.field[p].lp
		set_lp_label(p, lp_shown[p])
	turn_label.text = "TURNO %d" % ctrl.turn
	for i in PHASES.size():
		var on: bool = ctrl.phase & PHASES[i][1] != 0
		var b := phase_badges[i]
		b.add_theme_color_override("font_color", GBA.C_OUTLINE if on else Color("50608a"))
		b.add_theme_color_override("font_outline_color", GBA.C_YELLOW if on else GBA.C_OUTLINE)
		var sb: StyleBoxFlat = b.get_theme_stylebox("normal")
		sb.bg_color = GBA.C_YELLOW if on else Color("0a1030")
		sb.border_color = GBA.C_WHITE if on else Color("2a3a70")
	var links := []
	for i in ctrl.chain.size():
		links.append("%d. %s" % [i + 1, CardDB.card_name(ctrl.chain[i].code)])
	chain_label.text = ("Cadena: " + "  ".join(links)) if not links.is_empty() else ""


func set_lp_label(p: int, value: int) -> void:
	var ui := _ui_index(p)
	lp_labels[ui].text = str(max(0, value))
	lp_bars[ui].value = max(0, value)
	var frac: float = float(value) / max(1.0, lp_bars[ui].max_value)
	var fill: StyleBoxFlat = lp_bars[ui].get_theme_stylebox("fill").duplicate()
	fill.bg_color = GBA.C_GREEN if frac > 0.5 else (GBA.C_YELLOW if frac > 0.25 else GBA.C_RED)
	lp_bars[ui].add_theme_stylebox_override("fill", fill)


func lp_label_center(p: int) -> Vector2:
	## Posición (en coordenadas del campo) donde mostrar números de daño de un jugador.
	var row := 5.6 if p == me else 1.4
	return Vector2(field_layer.size.x / 2.0, y0 + row * ch)


# ================================================================ panel de información

func _show_info(view: CardView) -> void:
	if view == null:
		return
	if not view.face_up:
		info_view.setup(0, {}, false)
		info_name.text = "Carta boca abajo"
		info_type.text = ""
		_set_level(0, false)
		info_stats_box.visible = false
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
		info_type.text = "[%s / %s]  %s" % [OCG.race_name(base.get("race", 0)), OCG.attribute_name(base.get("attribute", 0)), info_type.text]
		var level: int = d.get("rank", 0) if type & OCG.TYPE_XYZ and d.get("rank", 0) else d.get("level", base.get("level", 0))
		_set_level(level if not type & OCG.TYPE_LINK else 0, type & OCG.TYPE_XYZ != 0)
		info_stats_box.visible = true
		info_atk.text = _stat(d.get("attack", 0))
		info_def.text = ("LINK-%d" % int(d.get("link", 0))) if type & OCG.TYPE_LINK else _stat(d.get("defense", 0))
	else:
		_set_level(0, false)
		info_stats_box.visible = false
	info_desc.text = base.get("desc", "")


func _set_level(n: int, rank: bool) -> void:
	for c in info_level.get_children():
		c.queue_free()
	for i in min(n, 12):
		var t := TextureRect.new()
		t.texture = GBA.tex("icon_star")
		t.custom_minimum_size = Vector2(14, 14)
		t.stretch_mode = TextureRect.STRETCH_SCALE
		t.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
		if rank:
			t.modulate = Color(0.4, 0.4, 0.45)
		info_level.add_child(t)


func _stat(v) -> String:
	return "?" if int(v) < 0 else str(v)
