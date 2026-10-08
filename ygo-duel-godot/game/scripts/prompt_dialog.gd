class_name PromptDialog
extends PanelContainer
## Ventana de selección reutilizable. Cada ask_* devuelve el resultado con `await`.

signal finished(result)
signal card_hovered(view: CardView)

var _title: Label
var _subtitle: Label
var _content: VBoxContainer
var _buttons: HBoxContainer
var _confirm: Button
var _views: Array[CardView] = []
var _selected: Array[int] = []
var _validator: Callable
var _min := 1
var _max := 1


func _init() -> void:
	visible = false
	mouse_filter = Control.MOUSE_FILTER_STOP
	add_theme_stylebox_override("panel", GBA.window_box(16))
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 8)
	add_child(v)
	_title = Label.new()
	_title.add_theme_font_size_override("font_size", 26)
	_title.add_theme_color_override("font_color", GBA.C_YELLOW)
	_title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	_title.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	_title.custom_minimum_size.x = 420
	v.add_child(_title)
	_subtitle = Label.new()
	_subtitle.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	_subtitle.modulate = Color(0.8, 0.8, 0.8)
	v.add_child(_subtitle)
	_content = VBoxContainer.new()
	_content.add_theme_constant_override("separation", 6)
	v.add_child(_content)
	_buttons = HBoxContainer.new()
	_buttons.alignment = BoxContainer.ALIGNMENT_CENTER
	_buttons.add_theme_constant_override("separation", 12)
	v.add_child(_buttons)


func _reset(title: String, subtitle: String = "") -> void:
	_title.text = title
	_subtitle.text = subtitle
	_subtitle.visible = subtitle != ""
	for box in [_content, _buttons]:
		for c in box.get_children():
			box.remove_child(c)
			c.queue_free()
	_views.clear()
	_selected.clear()
	_confirm = null
	visible = true
	_recenter.call_deferred()


func _recenter() -> void:
	reset_size()
	var parent_size: Vector2 = get_parent().size if get_parent() is Control else get_viewport_rect().size
	position = ((parent_size - size) / 2.0).round()


func close() -> void:
	visible = false


func _done(result) -> void:
	visible = false
	finished.emit(result)


## Si se pasa `callback`, el botón lo llama en lugar de cerrar con `result`.
func _add_button(text: String, result, primary := false, callback := Callable()) -> Button:
	var b := Button.new()
	b.text = text
	b.custom_minimum_size = Vector2(110, 36)
	if primary:
		b.add_theme_color_override("font_color", Color(1, 0.85, 0.3))
	if callback.is_valid():
		b.pressed.connect(callback)
	else:
		b.pressed.connect(func(): _done(result))
	_buttons.add_child(b)
	return b


## Botones de texto. Devuelve el índice elegido (o -1 si se pulsa el de cancelar).
func ask_options(title: String, options: Array, cancel_text := "") -> int:
	_reset(title)
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 6)
	_content.add_child(box)
	for i in options.size():
		var b := Button.new()
		b.text = str(options[i])
		b.custom_minimum_size = Vector2(380, 38)
		b.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		var idx := i
		b.pressed.connect(func(): _done(idx))
		box.add_child(b)
	if cancel_text != "":
		_add_button(cancel_text, -1)
	return await finished


func ask_yes_no(title: String, card_code := 0) -> bool:
	_reset(title)
	if card_code:
		var h := HBoxContainer.new()
		h.alignment = BoxContainer.ALIGNMENT_CENTER
		_content.add_child(h)
		h.add_child(_make_view({"code": card_code}, 110))
	_add_button("Sí", true, true)
	_add_button("No", false)
	return await finished


## Selección múltiple de cartas. `cards`: Array de {code, data?, face_up?, label?}.
## Devuelve Array de índices o null si se cancela.
func ask_cards(title: String, cards: Array, p_min: int, p_max: int, cancelable: bool, validator := Callable(), subtitle := ""):
	_reset(title, subtitle)
	_min = p_min
	_max = p_max
	_validator = validator
	_build_card_grid(cards, _toggle)
	_confirm = _add_button("Confirmar", null, true, func(): _done(_selected.duplicate()))
	# Con una sola carta a elegir basta un clic: el botón sobra
	_confirm.visible = not (p_max == 1 and p_min <= 1 and not validator.is_valid())
	if cancelable:
		_add_button("Cancelar", null)
	_update_confirm()
	return await finished


## Elegir una sola carta de la lista (las `preselected` se muestran marcadas).
## Devuelve el índice o -1 si se pulsa terminar/cancelar.
func ask_pick(title: String, cards: Array, preselected: Array, finish_text := "", cancel_text := "", subtitle := "") -> int:
	_reset(title, subtitle)
	_build_card_grid(cards, func(i): _done(i))
	for i in preselected:
		if i < _views.size():
			_views[i].selected = true
			_views[i].queue_redraw()
	if finish_text != "":
		_add_button(finish_text, -1, true)
	if cancel_text != "":
		_add_button(cancel_text, -1)
	return await finished


func ask_position(code: int, positions: int) -> int:
	_reset("Elige la posición de batalla")
	var h := HBoxContainer.new()
	h.alignment = BoxContainer.ALIGNMENT_CENTER
	h.add_theme_constant_override("separation", 18)
	_content.add_child(h)
	for pair in [[OCG.POS_FACEUP_ATTACK, "Ataque"], [OCG.POS_FACEDOWN_ATTACK, "Ataque boca abajo"],
			[OCG.POS_FACEUP_DEFENSE, "Defensa"], [OCG.POS_FACEDOWN_DEFENSE, "Defensa boca abajo"]]:
		if not positions & pair[0]:
			continue
		var v := VBoxContainer.new()
		v.alignment = BoxContainer.ALIGNMENT_END
		var cv := CardView.new()
		cv.full_art = true
		cv.set_card_size(90)
		cv.setup(code, {}, (pair[0] & OCG.POS_FACEUP) != 0)
		cv.defense_pos = (pair[0] & OCG.POS_DEFENSE) != 0
		var p: int = pair[0]
		cv.clicked.connect(func(_v): _done(p))
		v.add_child(cv)
		var b := Button.new()
		b.text = pair[1]
		b.pressed.connect(func(): _done(p))
		v.add_child(b)
		h.add_child(v)
	return await finished


## Reparte `total` contadores entre varias cartas.
func ask_counters(title: String, cards: Array, total: int) -> Array:
	_reset(title, "Total a retirar: %d" % total)
	var spins: Array[SpinBox] = []
	var grid := GridContainer.new()
	grid.columns = 2
	_content.add_child(grid)
	for c in cards:
		grid.add_child(_make_view(c, 70))
		var s := SpinBox.new()
		s.min_value = 0
		s.max_value = c.counters
		grid.add_child(s)
		spins.append(s)
	_add_button("Confirmar", null, true, func():
		var sum := 0
		var out := []
		for s in spins:
			out.append(int(s.value)); sum += int(s.value)
		if sum == total:
			_done(out))
	return await finished


## Marcar exactamente `count` opciones de un mapa {bit: nombre}. Devuelve la máscara.
func ask_flags(title: String, names: Dictionary, available: int, count: int) -> int:
	_reset(title, "Elige %d" % count)
	var grid := GridContainer.new()
	grid.columns = 3
	_content.add_child(grid)
	var checks := {}
	for bit in names:
		if available & bit:
			var cb := CheckBox.new()
			cb.text = names[bit]
			grid.add_child(cb)
			checks[bit] = cb
	_add_button("Confirmar", null, true, func():
		var mask := 0
		var n := 0
		for bit in checks:
			if checks[bit].button_pressed:
				mask |= bit; n += 1
		if n == count:
			_done(mask))
	return await finished


## Declarar un nombre de carta: buscador sobre la base de datos.
func ask_card_name(title: String) -> int:
	_reset(title)
	var edit := LineEdit.new()
	edit.placeholder_text = "Escribe parte del nombre..."
	_content.add_child(edit)
	var list := ItemList.new()
	list.custom_minimum_size = Vector2(420, 260)
	_content.add_child(list)
	var codes := CardDB.db.get_codes()
	var refresh := func(text: String):
		list.clear()
		var t := text.to_lower()
		for code in codes:
			var c := CardDB.get_card(code)
			if int(c.get("alias", 0)) != 0 or int(c.get("type", 0)) & OCG.TYPE_TOKEN:
				continue
			if t == "" or String(c.get("name", "")).to_lower().contains(t):
				list.add_item(c.get("name", str(code)))
				list.set_item_metadata(list.item_count - 1, code)
				if list.item_count >= 200:
					break
	edit.text_changed.connect(refresh)
	refresh.call("")
	list.item_activated.connect(func(i): _done(list.get_item_metadata(i)))
	_add_button("Declarar", null, true, func():
		var sel := list.get_selected_items()
		if not sel.is_empty():
			_done(list.get_item_metadata(sel[0])))
	edit.grab_focus.call_deferred()
	return await finished


# ---------------------------------------------------------------- interno

func _make_view(c: Dictionary, w: float) -> CardView:
	var cv := CardView.new()
	cv.full_art = true
	cv.set_card_size(w)
	cv.setup(c.get("code", 0), c.get("data", {}), c.get("face_up", true))
	cv.hovered.connect(func(v): card_hovered.emit(v))
	cv.clicked.connect(func(_v): Sfx.play("select"))
	return cv


func _build_card_grid(cards: Array, on_click: Callable) -> void:
	var scroll := ScrollContainer.new()
	scroll.custom_minimum_size = Vector2(min(6, max(cards.size(), 1)) * 104 + 12, 200 if cards.size() <= 6 else 380)
	scroll.horizontal_scroll_mode = ScrollContainer.SCROLL_MODE_DISABLED
	_content.add_child(scroll)
	var flow := HFlowContainer.new()
	flow.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	flow.add_theme_constant_override("h_separation", 8)
	flow.add_theme_constant_override("v_separation", 8)
	scroll.add_child(flow)
	for i in cards.size():
		var c: Dictionary = cards[i]
		var box := VBoxContainer.new()
		box.add_theme_constant_override("separation", 2)
		var cv := _make_view(c, 96)
		var idx := i
		cv.clicked.connect(func(_v): on_click.call(idx))
		box.add_child(cv)
		var lbl := Label.new()
		lbl.text = c.get("label", OCG.location_name(c.get("location", 0)) if c.has("location") else "")
		lbl.add_theme_font_size_override("font_size", 16)
		lbl.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		lbl.custom_minimum_size.x = 96
		lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		box.add_child(lbl)
		flow.add_child(box)
		_views.append(cv)


func _toggle(i: int) -> void:
	if _max == 1 and _min <= 1 and not _validator.is_valid():
		_done([i])
		return
	if i in _selected:
		_selected.erase(i)
	elif _selected.size() < _max:
		_selected.append(i)
	for j in _views.size():
		_views[j].selected = j in _selected
		_views[j].badge = str(_selected.find(j) + 1) if j in _selected else ""
		_views[j].queue_redraw()
	_update_confirm()


func _update_confirm() -> void:
	if _confirm == null:
		return
	var ok := _selected.size() >= _min and _selected.size() <= _max
	if _validator.is_valid():
		ok = _validator.call(_selected)
	_confirm.disabled = not ok
	_subtitle.visible = true
	_subtitle.text = "Seleccionadas: %d  (mín. %d, máx. %d)" % [_selected.size(), _min, _max]
