class_name BoardFx
extends RefCounted
## Animaciones del duelo al estilo de los Yu-Gi-Oh! de GBA: rótulos de turno y fase, robo de cartas,
## invocaciones con destello, activaciones ampliadas en el centro, embestida de ataque, escena de batalla
## "VS", destrucción en fragmentos, LP que bajan contando y temblor de pantalla.

const ANIMATED := [OCG.MSG_NEW_TURN, OCG.MSG_NEW_PHASE, OCG.MSG_DRAW, OCG.MSG_SUMMONING,
		OCG.MSG_SPSUMMONING, OCG.MSG_FLIPSUMMONING, OCG.MSG_SET, OCG.MSG_POS_CHANGE, OCG.MSG_CHAINING,
		OCG.MSG_CHAIN_NEGATED, OCG.MSG_CHAIN_DISABLED, OCG.MSG_ATTACK, OCG.MSG_BATTLE, OCG.MSG_MOVE,
		OCG.MSG_DAMAGE, OCG.MSG_RECOVER, OCG.MSG_PAY_LPCOST, OCG.MSG_TOSS_COIN, OCG.MSG_TOSS_DICE,
		OCG.MSG_EQUIP]

const REASON_DESTROY := 0x1
const REASON_RELEASE := 0x2

var board: DuelBoard
var speed := 1.0
var enabled := true


func _init(p_board: DuelBoard) -> void:
	board = p_board


## 0 = normal, 1 = rápida, 2 = sin animaciones
func set_speed_mode(mode: int) -> void:
	enabled = mode != 2
	speed = 2.2 if mode == 1 else 1.0
	if board.ctrl:
		board.ctrl.event_delay = 0.0 if enabled else 0.05


func animates(type: int) -> bool:
	return enabled and type in ANIMATED


# ================================================================ utilidades

func _d(t: float) -> float:
	return t / speed


func _wait(t: float) -> void:
	await board.get_tree().create_timer(_d(t)).timeout


func _tw() -> Tween:
	return board.create_tween()


func _layer() -> Control:
	return board.fx_layer


func _view(loc: Dictionary) -> CardView:
	var v = board.views.get(board.key(int(loc.get("controller", 0)), int(loc.get("location", 0)), int(loc.get("sequence", 0))))
	return v if v != null and is_instance_valid(v) else null


func _refresh() -> void:
	board.ctrl.refresh_field()


func _center(v: Control) -> Vector2:
	return v.position + v.size / 2.0


func _clone(v: CardView, parent: Control) -> CardView:
	var g := CardView.new()
	g.full_art = v.full_art
	g.set_card_size(v.size.x)
	g.setup(v.code, v.data, v.face_up)
	g.defense_pos = v.defense_pos
	g.position = v.position
	g.mouse_filter = Control.MOUSE_FILTER_IGNORE
	parent.add_child(g)
	return g


func _rect_node(r: Rect2, c: Color) -> ColorRect:
	var n := ColorRect.new()
	n.position = r.position
	n.size = r.size
	n.color = c
	n.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_layer().add_child(n)
	return n


# ================================================================ entrada

func animate(msg: Dictionary) -> void:
	if not enabled:
		return
	match msg.type:
		OCG.MSG_NEW_TURN:
			board._update_status()
			if board.ctrl.turn == 1:
				Sfx.play("special")
				await _banner("¡A DUELO!", GBA.C_GOLD, 0.7, 56, 110)
			Sfx.play("turn")
			var mine: bool = msg.player == board.me
			await _banner("TU TURNO" if mine else "TURNO RIVAL", GBA.C_YELLOW if mine else Color("ff8070"), 0.7, 44)
		OCG.MSG_NEW_PHASE:
			board._update_status()
			Sfx.play("phase")
			await _banner(String(OCG.PHASE_NAMES.get(msg.phase, "")).to_upper(), Color("a0d0ff"), 0.25, 26, 54)
		OCG.MSG_DRAW:
			_refresh()
			await _draw_cards(msg.player, msg.cards.size())
		OCG.MSG_SUMMONING:
			_refresh()
			await _summon(msg.loc, false)
		OCG.MSG_SPSUMMONING:
			_refresh()
			await _summon(msg.loc, true)
		OCG.MSG_FLIPSUMMONING:
			_refresh()
			await _flip(msg.loc)
		OCG.MSG_POS_CHANGE:
			_refresh()
			if (msg.prev_position & OCG.POS_FACEDOWN) and (msg.position & OCG.POS_FACEUP):
				await _flip(msg.loc)
			else:
				await _wobble(msg.loc)
		OCG.MSG_SET:
			_refresh()
			await _set_card(msg.loc)
		OCG.MSG_CHAINING:
			_refresh()
			await _activate(msg)
		OCG.MSG_CHAIN_NEGATED, OCG.MSG_CHAIN_DISABLED:
			await _stamp("¡NEGADO!")
		OCG.MSG_ATTACK:
			await _attack(msg)
		OCG.MSG_BATTLE:
			await _battle(msg)
		OCG.MSG_MOVE:
			await _move(msg)
		OCG.MSG_DAMAGE:
			await _lp(msg.player, -int(msg.amount), true)
		OCG.MSG_PAY_LPCOST:
			await _lp(msg.player, -int(msg.amount), false)
		OCG.MSG_RECOVER:
			await _lp(msg.player, int(msg.amount), false)
		OCG.MSG_TOSS_COIN:
			Sfx.play("flip")
			await _banner("MONEDA: " + ", ".join(msg.results.map(func(r): return "CARA" if r else "CRUZ")), GBA.C_GOLD, 0.6, 30)
		OCG.MSG_TOSS_DICE:
			Sfx.play("flip")
			await _banner("DADO: " + ", ".join(msg.results.map(func(r): return str(r))), GBA.C_GOLD, 0.6, 30)
		OCG.MSG_EQUIP:
			_refresh()
			var v := _view(msg.loc2)
			if v:
				Sfx.play("flip")
				_sparkles(_center(v), 10, Color("80ffb0"))
				await _wait(0.25)


# ================================================================ rótulos

func _banner(text: String, color: Color, hold: float, font_size := 40, height := 84) -> void:
	var layer := _layer()
	var w := layer.size.x
	var band := Control.new()
	band.mouse_filter = Control.MOUSE_FILTER_IGNORE
	band.size = Vector2(w, height)
	band.position = Vector2(w, round(layer.size.y / 2.0 - height / 2.0))
	layer.add_child(band)
	var bg := ColorRect.new()
	bg.color = Color(0.02, 0.03, 0.12, 0.9)
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	band.add_child(bg)
	for y in [0, height - 4]:
		var line := ColorRect.new()
		line.color = GBA.C_GOLD
		line.position = Vector2(0, y)
		line.size = Vector2(w, 4)
		band.add_child(line)
	var l := GBA.big_label(text, font_size, color)
	l.set_anchors_preset(Control.PRESET_FULL_RECT)
	l.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	l.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	band.add_child(l)
	var t := _tw()
	t.tween_property(band, "position:x", 0.0, _d(0.16)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	t.tween_interval(_d(hold))
	t.tween_property(band, "position:x", -w, _d(0.14)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	await t.finished
	band.queue_free()


func _stamp(text: String) -> void:
	Sfx.play("negate")
	var layer := _layer()
	var l := GBA.big_label(text, 54, GBA.C_RED)
	layer.add_child(l)
	await board.get_tree().process_frame
	l.pivot_offset = l.size / 2.0
	l.position = (layer.size - l.size) / 2.0
	l.rotation = -0.18
	l.scale = Vector2(3, 3)
	var t := _tw()
	t.tween_property(l, "scale", Vector2.ONE, _d(0.18)).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	t.tween_interval(_d(0.5))
	t.tween_property(l, "modulate:a", 0.0, _d(0.2))
	await t.finished
	l.queue_free()


func end_banner(won: bool, draw: bool) -> void:
	Sfx.play("win" if won else "lose")
	if not enabled:
		return
	var text := "EMPATE" if draw else ("¡VICTORIA!" if won else "DERROTA")
	await _banner(text, GBA.C_YELLOW if won else Color("ff8070"), 1.2, 56, 110)


# ================================================================ cartas

func _draw_cards(p: int, count: int) -> void:
	var hand: Array = board.ctrl.field[p].hand
	var deck_rect := board.zone_rect(p, OCG.LOCATION_DECK, 0)
	var n := hand.size()
	var t := _tw().set_parallel(true)
	var any := false
	for k in range(max(0, n - count), n):
		var v := _view({"controller": p, "location": OCG.LOCATION_HAND, "sequence": k})
		if v == null:
			continue
		any = true
		var target := v.position
		var delay := _d(0.09) * (k - (n - count))
		v.position = deck_rect.position
		v.modulate.a = 0.0
		t.tween_property(v, "position", target, _d(0.24)).set_delay(delay).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
		t.tween_property(v, "modulate:a", 1.0, _d(0.1)).set_delay(delay)
		t.tween_callback(Sfx.play.bind("draw")).set_delay(delay)
	if any:
		await t.finished
	else:
		t.kill()


func _summon(loc: Dictionary, special: bool) -> void:
	var v := _view(loc)
	if v == null:
		return
	Sfx.play("special" if special else "summon")
	var c := _center(v)
	if special:
		var pillar := _rect_node(Rect2(Vector2(v.position.x + v.size.x * 0.15, 0), Vector2(v.size.x * 0.7, _layer().size.y)), Color(1, 1, 0.75, 0.0))
		var tp := _tw()
		tp.tween_property(pillar, "color:a", 0.55, _d(0.12))
		tp.tween_property(pillar, "color:a", 0.0, _d(0.35))
		tp.tween_callback(pillar.queue_free)
		_sparkles(c, 18, Color("fff0a0"))
	v.scale = Vector2(1.8, 1.8)
	v.modulate = Color(3, 3, 3, 0.0)
	var t := _tw().set_parallel(true)
	t.tween_property(v, "scale", Vector2.ONE, _d(0.3)).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	t.tween_property(v, "modulate", Color.WHITE, _d(0.3))
	_ring(c, Color("fff6c0") if not special else Color("ffe060"))
	await t.finished
	_restore_alpha(v)


func _restore_alpha(v: CardView) -> void:
	if is_instance_valid(v) and v.controller == board.me and v.location & OCG.LOCATION_ONFIELD and not v.face_up:
		v.modulate = Color(1, 1, 1, 0.8)


func _flip(loc: Dictionary) -> void:
	var v := _view(loc)
	if v == null:
		return
	Sfx.play("flip")
	v.scale = Vector2(0.0, 1.1)
	var t := _tw()
	t.tween_property(v, "scale", Vector2(1, 1), _d(0.2)).set_trans(Tween.TRANS_SINE)
	_ring(_center(v), Color("c0e8ff"))
	await t.finished


func _wobble(loc: Dictionary) -> void:
	var v := _view(loc)
	if v == null:
		return
	Sfx.play("set")
	var t := _tw()
	t.tween_property(v, "rotation", 0.25, _d(0.07))
	t.tween_property(v, "rotation", -0.15, _d(0.07))
	t.tween_property(v, "rotation", 0.0, _d(0.07))
	await t.finished


func _set_card(loc: Dictionary) -> void:
	var v := _view(loc)
	if v == null:
		return
	Sfx.play("set")
	var target := v.position
	var off := 50.0 if int(loc.controller) == board.me else -50.0
	v.position = target + Vector2(0, off)
	var a := v.modulate.a
	v.modulate.a = 0.0
	var t := _tw().set_parallel(true)
	t.tween_property(v, "position", target, _d(0.2)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	t.tween_property(v, "modulate:a", a, _d(0.15))
	await t.finished


## La carta activada se amplía en el centro con su número de cadena.
func _activate(msg: Dictionary) -> void:
	var src := _view(msg.loc)
	Sfx.play("activate")
	var layer := _layer()
	var from := _center(src) if src else layer.size / 2.0
	if src:
		var tf := _tw()
		tf.tween_property(src, "modulate", Color(3, 3, 2, 1), _d(0.08))
		tf.tween_property(src, "modulate", Color.WHITE, _d(0.12))
	var holder := Control.new()
	holder.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(holder)
	var w := 200.0
	var h := roundf(w * CardView.CARD_RATIO)
	holder.size = Vector2(w, h)
	holder.position = ((layer.size - holder.size) / 2.0).round()
	holder.pivot_offset = holder.size / 2.0
	var glow := ColorRect.new()
	glow.color = Color(1, 0.9, 0.3, 0.6)
	glow.position = Vector2(-8, -8)
	glow.size = holder.size + Vector2(16, 16)
	holder.add_child(glow)
	var card := CardView.new()
	card.full_art = true
	card.set_card_size(w)
	card.setup(msg.code, {}, true)
	card.mouse_filter = Control.MOUSE_FILTER_IGNORE
	holder.add_child(card)
	var lbl := GBA.big_label("CADENA %d" % int(msg.chain_count), 26, GBA.C_YELLOW)
	lbl.position = Vector2(-40, -48)
	lbl.size = Vector2(w + 80, 40)
	lbl.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	holder.add_child(lbl)
	var name := Label.new()
	name.text = CardDB.card_name(msg.code)
	name.add_theme_font_size_override("font_size", 28)
	name.add_theme_color_override("font_outline_color", GBA.C_OUTLINE)
	name.add_theme_constant_override("outline_size", 6)
	name.position = Vector2(-80, h + 10)
	name.size = Vector2(w + 160, 30)
	name.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	holder.add_child(name)
	holder.scale = Vector2(0.2, 0.2)
	holder.modulate.a = 0.0
	var t := _tw()
	t.set_parallel(true)
	t.tween_property(holder, "scale", Vector2.ONE, _d(0.22)).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	t.tween_property(holder, "modulate:a", 1.0, _d(0.12))
	t.chain().tween_property(glow, "color:a", 0.15, _d(0.2))
	t.chain().tween_property(glow, "color:a", 0.6, _d(0.2))
	t.chain().tween_interval(_d(0.3))
	t.chain().tween_property(holder, "scale", Vector2(0.2, 0.2), _d(0.18)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	t.parallel().tween_property(holder, "position", from - holder.size / 2.0, _d(0.18))
	t.parallel().tween_property(holder, "modulate:a", 0.0, _d(0.18))
	await t.finished
	holder.queue_free()


# ================================================================ batalla

func _attack(msg: Dictionary) -> void:
	var v := _view(msg.attacker)
	if v == null:
		return
	Sfx.play("attack")
	var start := v.position
	var to: Vector2
	if msg.direct:
		to = board.lp_label_center(1 - int(msg.attacker.controller))
	else:
		var tv := _view(msg.target)
		to = _center(tv) if tv else board.lp_label_center(1 - int(msg.attacker.controller))
	var dir := (to - _center(v))
	v.z_index = 20
	var t := _tw()
	t.tween_property(v, "position", start - dir.normalized() * 10.0, _d(0.08))
	t.tween_property(v, "position", start + dir * 0.55, _d(0.14)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	t.tween_callback(_shake.bind(5.0, 0.15))
	t.tween_property(v, "position", start, _d(0.18)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	await t.finished
	if is_instance_valid(v):
		v.z_index = 0


## Escena "VS" con las dos cartas, sus valores y el resultado del combate.
func _battle(msg: Dictionary) -> void:
	var layer := _layer()
	var a: Dictionary = msg.attacker
	var d: Dictionary = msg.target
	var atk_card = board.ctrl.get_card_at(a.controller, a.location, a.sequence)
	if atk_card == null:
		return
	var has_target: bool = int(d.location) != 0
	var def_card = board.ctrl.get_card_at(d.controller, d.location, d.sequence) if has_target else null
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", GBA.window_box(18))
	panel.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(panel)
	var hb := HBoxContainer.new()
	hb.add_theme_constant_override("separation", 26)
	hb.alignment = BoxContainer.ALIGNMENT_CENTER
	panel.add_child(hb)
	var cw := 132.0
	var left := _battle_side(atk_card, a, cw, "ATK", int(msg.atk_attack))
	hb.add_child(left[0])
	var vs := GBA.big_label("VS", 40, GBA.C_RED)
	vs.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	hb.add_child(vs)
	var right: Array
	if has_target and def_card != null:
		var defending: bool = int(def_card.get("position", 0)) & OCG.POS_DEFENSE != 0
		right = _battle_side(def_card, d, cw, "DEF" if defending else "ATK", int(msg.tgt_defense if defending else msg.tgt_attack))
	else:
		right = _battle_direct(1 - int(a.controller), cw)
	hb.add_child(right[0])
	await board.get_tree().process_frame
	panel.position = ((layer.size - panel.size) / 2.0).round()
	panel.pivot_offset = panel.size / 2.0
	panel.scale = Vector2(0.85, 0.85)
	panel.modulate.a = 0.0
	var t := _tw().set_parallel(true)
	t.tween_property(panel, "scale", Vector2.ONE, _d(0.14))
	t.tween_property(panel, "modulate:a", 1.0, _d(0.12))
	await t.finished
	await _wait(0.3)
	# Embestida del atacante
	var acv: CardView = left[1]
	var sx := acv.position.x
	var t2 := _tw()
	t2.tween_property(acv, "position:x", sx + 70.0, _d(0.1)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	t2.tween_property(acv, "position:x", sx, _d(0.14))
	await _wait(0.1)
	Sfx.play("hit")
	var flash := ColorRect.new()
	flash.color = Color(1, 1, 1, 0.85)
	flash.set_anchors_preset(Control.PRESET_FULL_RECT)
	flash.mouse_filter = Control.MOUSE_FILTER_IGNORE
	panel.add_child(flash)
	_tw().tween_property(flash, "color:a", 0.0, _d(0.25))
	_shake(6.0, 0.2)
	await t2.finished
	if msg.atk_destroyed:
		_shatter_view(acv)
	if has_target and msg.tgt_destroyed and right.size() > 1:
		_shatter_view(right[1])
	if msg.atk_destroyed or msg.tgt_destroyed:
		Sfx.play("destroy")
	await _wait(0.55)
	var t3 := _tw()
	t3.tween_property(panel, "modulate:a", 0.0, _d(0.15))
	await t3.finished
	panel.queue_free()


func _battle_side(card: Dictionary, loc: Dictionary, w: float, stat_name: String, value: int) -> Array:
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 6)
	var holder := Control.new()
	holder.custom_minimum_size = Vector2(w, round(w * CardView.CARD_RATIO))
	v.add_child(holder)
	var cv := CardView.new()
	cv.set_card_size(w)
	# En el cálculo de daño ambas cartas ya están boca arriba
	cv.setup(int(card.get("code", 0)), card, true)
	cv.mouse_filter = Control.MOUSE_FILTER_IGNORE
	holder.add_child(cv)
	var lbl := GBA.big_label("%s %d" % [stat_name, value], 22, GBA.C_YELLOW if stat_name == "ATK" else Color("a0c8ff"))
	lbl.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(lbl)
	return [v, cv]


func _battle_direct(p: int, w: float) -> Array:
	var v := VBoxContainer.new()
	v.alignment = BoxContainer.ALIGNMENT_CENTER
	v.custom_minimum_size = Vector2(w, round(w * CardView.CARD_RATIO) + 40)
	var l := GBA.big_label("ATAQUE\nDIRECTO", 22, GBA.C_RED)
	l.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(l)
	var lp := GBA.big_label("LP %d" % int(board.lp_shown[p]), 22, GBA.C_WHITE)
	lp.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(lp)
	return [v]


# ================================================================ movimientos y destrucción

func _move(msg: Dictionary) -> void:
	var from: Dictionary = msg.from
	var to: Dictionary = msg.to
	var fl := int(from.location)
	var tl := int(to.location)
	if fl & OCG.LOCATION_OVERLAY or fl == tl:
		return
	var from_field := (fl & OCG.LOCATION_ONFIELD) != 0
	var from_hand := fl == OCG.LOCATION_HAND
	if not from_field and not (from_hand and tl & (OCG.LOCATION_GRAVE | OCG.LOCATION_REMOVED)):
		return
	if tl & OCG.LOCATION_ONFIELD:
		return # las invocaciones/colocaciones tienen su propia animación
	var v := _view(from)
	if v == null:
		return
	var reason := int(msg.reason)
	if from_field and reason & REASON_DESTROY:
		var t := _tw()
		for i in 2:
			t.tween_property(v, "modulate", Color(4, 4, 4, 1), _d(0.06))
			t.tween_property(v, "modulate", Color.WHITE, _d(0.06))
		await t.finished
		Sfx.play("destroy")
		_shatter_view(v)
		_shake(4.0, 0.15)
		await _wait(0.45)
		return
	var g := _clone(v, _layer())
	v.visible = false
	var t2 := _tw().set_parallel(true)
	if tl == OCG.LOCATION_REMOVED:
		Sfx.play("special", 0.7)
		g.pivot_offset = g.size / 2.0
		t2.tween_property(g, "scale", Vector2(1.4, 1.4), _d(0.35))
		t2.tween_property(g, "modulate", Color(0.4, 0.6, 3.0, 0.0), _d(0.35))
	elif from_field and reason & REASON_RELEASE:
		Sfx.play("set", 1.3)
		t2.tween_property(g, "position:y", g.position.y - 40.0, _d(0.3))
		t2.tween_property(g, "modulate", Color(3, 3, 3, 0.0), _d(0.3))
	else:
		var dest := board.zone_rect(int(to.controller), tl, 0) if tl != OCG.LOCATION_HAND else board.hand_rect(int(to.controller), 0)
		t2.tween_property(g, "position", dest.position, _d(0.28)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN_OUT)
		t2.tween_property(g, "modulate:a", 0.3, _d(0.28))
	await t2.finished
	g.queue_free()


## Rompe la carta en fragmentos que salen despedidos.
func _shatter_view(v: CardView) -> void:
	if v == null or not is_instance_valid(v):
		return
	var layer := _layer()
	var origin := v.get_global_rect().position - layer.get_global_rect().position
	var size := v.size
	var cols := 4
	var rows := 5
	var pw := size.x / cols
	var ph := size.y / rows
	for j in rows:
		for i in cols:
			var piece := Control.new()
			piece.clip_contents = true
			piece.mouse_filter = Control.MOUSE_FILTER_IGNORE
			piece.position = origin + Vector2(i * pw, j * ph)
			piece.size = Vector2(ceil(pw), ceil(ph))
			piece.pivot_offset = piece.size / 2.0
			var c := CardView.new()
			c.set_card_size(size.x)
			c.setup(v.code, v.data, v.face_up)
			c.defense_pos = v.defense_pos
			c.position = -Vector2(i * pw, j * ph)
			c.mouse_filter = Control.MOUSE_FILTER_IGNORE
			piece.add_child(c)
			layer.add_child(piece)
			var dir := Vector2(i - (cols - 1) / 2.0, j - (rows - 1) / 2.0).normalized()
			dir = dir.rotated(randf_range(-0.4, 0.4))
			var dist := randf_range(50.0, 110.0)
			var t := _tw().set_parallel(true)
			t.tween_property(piece, "position", piece.position + dir * dist + Vector2(0, 40), _d(0.5)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
			t.tween_property(piece, "rotation", randf_range(-2.5, 2.5), _d(0.5))
			t.tween_property(piece, "modulate", Color(2, 1.4, 0.6, 0.0), _d(0.5))
			t.chain().tween_callback(piece.queue_free)
	v.visible = false
	_sparkles(origin + size / 2.0, 14, Color("ffb040"))


# ================================================================ LP

func _lp(p: int, delta: int, is_damage: bool) -> void:
	if delta == 0:
		return
	board.lp_animating[p] = true
	var from: int = board.lp_shown[p]
	var to: int = max(0, from + delta)
	Sfx.play("damage" if delta < 0 else "recover")
	if is_damage:
		_shake(8.0, 0.3)
		var red := _rect_node(Rect2(Vector2.ZERO, _layer().size), Color(1, 0, 0, 0.28))
		var tr := _tw()
		tr.tween_property(red, "color:a", 0.0, _d(0.35))
		tr.tween_callback(red.queue_free)
	# Número flotante
	var txt := ("-%d" if delta < 0 else "+%d") % abs(delta)
	var lbl := GBA.big_label(txt, 40, GBA.C_RED if delta < 0 else GBA.C_GREEN)
	_layer().add_child(lbl)
	await board.get_tree().process_frame
	lbl.position = (board.lp_label_center(p) - lbl.size / 2.0).round()
	var tf := _tw().set_parallel(true)
	tf.tween_property(lbl, "position:y", lbl.position.y - 50.0, _d(0.9))
	tf.tween_property(lbl, "modulate:a", 0.0, _d(0.4)).set_delay(_d(0.5))
	tf.chain().tween_callback(lbl.queue_free)
	# Contador que rueda
	var ui: int = board._ui_index(p)
	_last_tick = 0
	var t := _tw()
	t.tween_method(_lp_step.bind(p), float(from), float(to), _d(0.7))
	board.lp_labels[ui].modulate = Color(1, 0.5, 0.5) if delta < 0 else Color(0.6, 1, 0.6)
	await t.finished
	board.lp_labels[ui].modulate = Color.WHITE
	board.lp_shown[p] = to
	board.set_lp_label(p, to)
	board.lp_animating[p] = false


var _last_tick := 0


func _lp_step(val: float, p: int) -> void:
	board.set_lp_label(p, int(val))
	var now := Time.get_ticks_msec()
	if now - _last_tick > 55:
		_last_tick = now
		Sfx.play("tick")


# ================================================================ efectos

func _shake(strength: float, dur: float) -> void:
	var nodes: Array[Control] = [board.zone_layer, board.cards_layer]
	var t := _tw()
	var steps := 6
	for i in steps + 1:
		var off := Vector2.ZERO if i == steps else Vector2(randf_range(-strength, strength), randf_range(-strength, strength)).round()
		t.tween_property(nodes[0], "position", off, _d(dur / steps))
		t.parallel().tween_property(nodes[1], "position", off, _d(dur / steps))


func _ring(center: Vector2, color: Color) -> void:
	var r := RingFx.new()
	r.color = color
	r.position = center
	_layer().add_child(r)
	var t := _tw().set_parallel(true)
	t.tween_property(r, "radius", 90.0, _d(0.4)).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	t.tween_property(r, "modulate:a", 0.0, _d(0.4))
	t.chain().tween_callback(r.queue_free)


func _sparkles(center: Vector2, n: int, color: Color) -> void:
	for i in n:
		var s := ColorRect.new()
		var sz := float([3, 6, 6, 9].pick_random())
		s.size = Vector2(sz, sz)
		s.color = color if randf() < 0.7 else GBA.C_WHITE
		s.position = center - s.size / 2.0
		s.mouse_filter = Control.MOUSE_FILTER_IGNORE
		_layer().add_child(s)
		var dir := Vector2.from_angle(randf() * TAU) * randf_range(30.0, 100.0) + Vector2(0, -40)
		var t := _tw().set_parallel(true)
		t.tween_property(s, "position", s.position + dir, _d(randf_range(0.35, 0.6))).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
		t.tween_property(s, "modulate:a", 0.0, _d(0.6))
		t.chain().tween_callback(s.queue_free)


class RingFx:
	extends Control
	var radius := 10.0:
		set(v):
			radius = v
			queue_redraw()
	var color := Color.WHITE

	func _init() -> void:
		mouse_filter = Control.MOUSE_FILTER_IGNORE

	func _draw() -> void:
		# Rombo pixelado que se expande
		var r := radius
		var pts := PackedVector2Array([Vector2(0, -r), Vector2(r, 0), Vector2(0, r), Vector2(-r, 0), Vector2(0, -r)])
		draw_polyline(pts, color, 4.0)
		draw_rect(Rect2(-r * 0.7, -r * 0.7, r * 1.4, r * 1.4), Color(color, 0.5), false, 2.0)
