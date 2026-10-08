class_name CardView
extends Control
## Representación de una carta. Usa la imagen si está disponible y si no dibuja una carta genérica
## con el color de su tipo, nombre, nivel y ATK/DEF.

signal clicked(view: CardView)
signal hovered(view: CardView)

const CARD_RATIO := 1.4545 # alto / ancho (59 x 86 mm)

var code := 0
var data := {} # resultado de la consulta al core (o de CardDB)
var controller := 0
var location := 0
var sequence := 0
var face_up := true # si el jugador local puede ver la carta
var show_stats := false # ATK/DEF actuales bajo la carta (monstruos en el campo)
var defense_pos := false
var highlight := Color.TRANSPARENT
var selected := false
var dimmed := false
var badge := "" # texto pequeño en la esquina (p.ej. orden de selección)

var _tex: Texture2D


func _init() -> void:
	mouse_filter = Control.MOUSE_FILTER_STOP
	focus_mode = Control.FOCUS_NONE


func setup(p_code: int, p_data: Dictionary, p_face_up: bool) -> CardView:
	code = p_code
	# Datos base de la BD (nombre, nivel...) sobrescritos con los valores actuales del duelo
	data = CardDB.get_card(p_code).duplicate()
	for k in p_data:
		data[k] = p_data[k]
	face_up = p_face_up and p_code != 0
	if face_up:
		_tex = CardImages.get_texture(code)
		if _tex == null and not CardImages.texture_ready.is_connected(_on_texture_ready):
			CardImages.texture_ready.connect(_on_texture_ready)
	queue_redraw()
	return self


func set_card_size(w: float) -> void:
	custom_minimum_size = Vector2(w, w * CARD_RATIO)
	size = custom_minimum_size
	pivot_offset = size / 2.0


func _on_texture_ready(c: int) -> void:
	if c == code:
		_tex = CardImages.get_texture(code)
		queue_redraw()


func _gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		clicked.emit(self)
		accept_event()


func _notification(what: int) -> void:
	if what == NOTIFICATION_MOUSE_ENTER:
		hovered.emit(self)


func _draw() -> void:
	var w := size.x
	var h := size.y
	var center := size / 2.0
	if defense_pos:
		# Girada 90º alrededor del centro y algo reducida para que quepa en la zona
		draw_set_transform_matrix(Transform2D(PI / 2.0, Vector2(0.85, 0.85), 0.0, center) * Transform2D(0.0, -center))
	var rect := Rect2(Vector2.ZERO, size)
	if face_up:
		if _tex:
			draw_texture_rect(_tex, rect, false)
		else:
			_draw_generic(rect)
	else:
		_draw_back(rect)
	if dimmed:
		draw_rect(rect, Color(0, 0, 0, 0.45))
	if highlight.a > 0.0:
		draw_rect(rect.grow(2), highlight, false, 3.0)
	if selected:
		draw_rect(rect, Color(1, 0.85, 0.2, 0.30))
		draw_rect(rect.grow(3), Color(1, 0.85, 0.2), false, 4.0)
	draw_set_transform_matrix(Transform2D.IDENTITY)
	var font := get_theme_default_font()
	if show_stats and face_up and int(data.get("type", 0)) & OCG.TYPE_MONSTER:
		var txt := "%s/%s" % [_stat(data.get("attack", 0)), _stat(data.get("defense", 0))]
		if int(data.get("type", 0)) & OCG.TYPE_LINK:
			txt = "%s/L%d" % [_stat(data.get("attack", 0)), data.get("link", 0)]
		var fs := 12
		var tw := font.get_string_size(txt, HORIZONTAL_ALIGNMENT_LEFT, -1, fs).x
		var y := h - 2
		var bg := Rect2(Vector2((w - tw) / 2.0 - 3, y - fs - 1), Vector2(tw + 6, fs + 4))
		draw_rect(bg, Color(0, 0, 0, 0.75))
		var col := Color.WHITE
		var atk: int = data.get("attack", 0)
		var base: int = data.get("base_attack", atk)
		if atk > base: col = Color(0.5, 1, 0.5)
		elif atk < base: col = Color(1, 0.5, 0.5)
		draw_string(font, Vector2((w - tw) / 2.0, y), txt, HORIZONTAL_ALIGNMENT_LEFT, -1, fs, col)
	if badge != "":
		draw_circle(Vector2(w - 9, 9), 9, Color(0.9, 0.2, 0.2))
		draw_string(font, Vector2(w - 13, 13), badge, HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color.WHITE)


func _stat(v) -> String:
	return "?" if int(v) < 0 else str(v)


static func type_color(type: int) -> Color:
	if type & OCG.TYPE_SPELL: return Color("1d9e74")
	if type & OCG.TYPE_TRAP: return Color("bc5a84")
	if type & OCG.TYPE_TOKEN: return Color("a0a0a0")
	if type & OCG.TYPE_LINK: return Color("2b5fa8")
	if type & OCG.TYPE_XYZ: return Color("2a2a2a")
	if type & OCG.TYPE_SYNCHRO: return Color("dcdcdc")
	if type & OCG.TYPE_FUSION: return Color("8e5ab5")
	if type & OCG.TYPE_RITUAL: return Color("5d8ccf")
	if type & OCG.TYPE_EFFECT: return Color("c7703a")
	return Color("c9a45c")


func _draw_generic(rect: Rect2) -> void:
	var type: int = data.get("type", 0)
	var base := type_color(type)
	draw_rect(rect, base)
	draw_rect(rect, Color(0, 0, 0, 0.6), false, 1.5)
	var w := rect.size.x
	var h := rect.size.y
	var font := get_theme_default_font()
	var dark_text := (type & OCG.TYPE_SYNCHRO) != 0 or (type & (OCG.TYPE_NORMAL | OCG.TYPE_EFFECT) and not type & (OCG.TYPE_XYZ | OCG.TYPE_LINK) and not type & (OCG.TYPE_SPELL | OCG.TYPE_TRAP))
	var text_col := Color.BLACK if dark_text else Color.WHITE
	# Marco del nombre
	var fs: int = max(8, int(w / 11.0))
	draw_rect(Rect2(3, 3, w - 6, fs + 6), Color(1, 1, 1, 0.25))
	var name: String = data.get("name", "???")
	draw_string(font, Vector2(5, 3 + fs + 1), name, HORIZONTAL_ALIGNMENT_LEFT, w - 10, fs, text_col)
	# "Arte"
	var art := Rect2(w * 0.12, h * 0.2, w * 0.76, w * 0.76)
	draw_rect(art, base.darkened(0.35))
	draw_rect(art, Color(0, 0, 0, 0.5), false, 1.0)
	if type & OCG.TYPE_MONSTER:
		var lv: int = data.get("level", 0)
		var stars := "★".repeat(min(lv, 12)) if not type & OCG.TYPE_LINK else "LINK-%d" % data.get("link", lv)
		draw_string(font, Vector2(5, h * 0.2 - 2), stars, HORIZONTAL_ALIGNMENT_RIGHT, w - 10, max(7, fs - 2), Color(1, 0.8, 0.1))
		var attr := OCG.attribute_name(data.get("attribute", 0))
		draw_string(font, art.position + Vector2(3, art.size.y / 2.0), attr, HORIZONTAL_ALIGNMENT_CENTER, art.size.x - 6, max(7, fs - 2), Color(1, 1, 1, 0.8))
		var stats := "ATK %s  DEF %s" % [_stat(data.get("attack", 0)), _stat(data.get("defense", 0))]
		if type & OCG.TYPE_LINK:
			stats = "ATK %s" % _stat(data.get("attack", 0))
		draw_string(font, Vector2(4, h - 5), stats, HORIZONTAL_ALIGNMENT_CENTER, w - 8, max(7, int(w / 15.0)), text_col)
	else:
		var label := "MAGIA" if type & OCG.TYPE_SPELL else "TRAMPA"
		draw_string(font, art.position + Vector2(0, art.size.y / 2.0 + 4), label, HORIZONTAL_ALIGNMENT_CENTER, art.size.x, max(8, fs), Color(1, 1, 1, 0.85))


func _draw_back(rect: Rect2) -> void:
	draw_rect(rect, Color("5a3a1e"))
	draw_rect(rect.grow(-3), Color("8a5a2b"), false, 2.0)
	var c := rect.get_center()
	var rx := rect.size.x * 0.32
	var ry := rect.size.y * 0.3
	var pts := PackedVector2Array()
	for i in 33:
		var a := TAU * i / 32.0
		pts.append(c + Vector2(cos(a) * rx, sin(a) * ry))
	draw_colored_polygon(pts, Color("2b1a0e"))
	draw_polyline(pts, Color("d9a441"), 2.0)
	draw_circle(c, rx * 0.35, Color("d9a441"))
	draw_rect(rect, Color(0, 0, 0, 0.7), false, 1.0)
