class_name CardView
extends Control
## Carta dibujada al estilo GBA.
##  - Modo "pixel" (campo y mano): marco del color del tipo, ilustración pixelada, estrellas y ATK/DEF.
##  - Modo "full" (paneles y diálogos): imagen completa de pics/ si existe; si no, el modo pixel.

signal clicked(view: CardView)
signal hovered(view: CardView)
signal unhovered(view: CardView)

const CARD_RATIO := 1.4545 # alto / ancho
const GRID_W := 22.0 # anchura en "píxeles GBA" del sprite de carta

var code := 0
var data := {}
var controller := 0
var location := 0
var sequence := 0
var face_up := true
var full_art := false # usar la imagen completa de la carta
var show_stats := false
var defense_pos := false
var highlight := Color.TRANSPARENT
var selected := false
var dimmed := false
var badge := ""

var _art: Texture2D
var _full: Texture2D


func _init() -> void:
	mouse_filter = Control.MOUSE_FILTER_STOP
	focus_mode = Control.FOCUS_NONE
	texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	mouse_entered.connect(func(): hovered.emit(self))
	mouse_exited.connect(func(): unhovered.emit(self))


func setup(p_code: int, p_data: Dictionary, p_face_up: bool) -> CardView:
	code = p_code
	data = CardDB.get_card(p_code).duplicate()
	for k in p_data:
		data[k] = p_data[k]
	face_up = p_face_up and p_code != 0
	_art = null
	_full = null
	if face_up:
		_load_textures()
		if (_art == null or (full_art and _full == null)) and not CardImages.texture_ready.is_connected(_on_texture_ready):
			CardImages.texture_ready.connect(_on_texture_ready)
	queue_redraw()
	return self


func _load_textures() -> void:
	if full_art:
		_full = CardImages.get_texture(code)
		texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR if _full else CanvasItem.TEXTURE_FILTER_NEAREST
	_art = CardImages.get_art_texture(code)


func set_card_size(w: float) -> void:
	custom_minimum_size = Vector2(w, round(w * CARD_RATIO))
	size = custom_minimum_size
	pivot_offset = size / 2.0


func _on_texture_ready(c: int) -> void:
	if c == code and face_up:
		_load_textures()
		queue_redraw()


func _gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		clicked.emit(self)
		accept_event()


func _draw() -> void:
	var center := size / 2.0
	if defense_pos:
		draw_set_transform_matrix(Transform2D(PI / 2.0, Vector2(0.86, 0.86), 0.0, center) * Transform2D(0.0, -center))
	var rect := Rect2(Vector2.ZERO, size)
	if not face_up:
		draw_texture_rect(GBA.tex("card_back"), rect, false)
	elif full_art and _full:
		draw_texture_rect(_full, rect, false)
	else:
		_draw_pixel_card(rect)
	if dimmed:
		draw_rect(rect, Color(0, 0, 0, 0.5))
	if highlight.a > 0.0:
		var u: float = max(2.0, size.x / GRID_W)
		draw_rect(rect.grow(u * 0.5), highlight, false, u)
	if selected:
		draw_rect(rect, Color(1, 0.88, 0.25, 0.28))
		draw_rect(rect.grow(3), GBA.C_YELLOW, false, 3.0)
	draw_set_transform_matrix(Transform2D.IDENTITY)
	if badge != "":
		draw_rect(Rect2(size.x - 18, -2, 20, 18), GBA.C_RED)
		draw_rect(Rect2(size.x - 18, -2, 20, 18), GBA.C_WHITE, false, 2.0)
		draw_string(GBA.font_big, Vector2(size.x - 14, 12), badge, HORIZONTAL_ALIGNMENT_LEFT, -1, 12, GBA.C_WHITE)


func _stat(v) -> String:
	return "?" if int(v) < 0 else str(v)


## Sprite de carta de 22x32 "píxeles GBA" escalado al tamaño del control.
func _draw_pixel_card(rect: Rect2) -> void:
	if rect.size.x < GRID_W:
		return
	var u := rect.size.x / GRID_W
	var type: int = data.get("type", 0)
	var frame := GBA.frame_color(type)
	var px := func(x: float, y: float, w: float, h: float, c: Color):
		draw_rect(Rect2(rect.position + Vector2(x * u, y * u), Vector2(w * u, h * u)), c)
	px.call(0, 0, GRID_W, 32, GBA.C_OUTLINE)
	px.call(1, 1, GRID_W - 2, 30, frame)
	px.call(1, 1, GRID_W - 2, 1, frame.lightened(0.35))
	px.call(1, 1, 1, 30, frame.lightened(0.2))
	px.call(1, 30, GRID_W - 2, 1, frame.darkened(0.4))
	px.call(GRID_W - 2, 1, 1, 30, frame.darkened(0.4))
	# Barra del nombre
	px.call(2, 2, GRID_W - 4, 2, frame.darkened(0.25))
	# Atributo (orbe)
	var attr: int = data.get("attribute", 0)
	if type & OCG.TYPE_MONSTER and attr:
		px.call(GRID_W - 5, 2, 2, 2, GBA.ATTR_COLORS.get(attr, Color.GRAY))
	# Ilustración
	var art_rect := Rect2(rect.position + Vector2(2 * u, 5 * u), Vector2(18 * u, 15 * u))
	px.call(2, 5, 18, 15, GBA.C_OUTLINE)
	if _art:
		var src := Rect2(0, _art.get_height() * 0.08, _art.get_width(), _art.get_height() * 0.84)
		draw_texture_rect_region(_art, art_rect.grow(-u * 0.5), src)
	else:
		px.call(3, 6, 16, 13, frame.darkened(0.5))
		var label := "?" if type & OCG.TYPE_MONSTER else ("M" if type & OCG.TYPE_SPELL else "T")
		draw_string(GBA.font_big, art_rect.position + Vector2(0, art_rect.size.y * 0.62), label,
				HORIZONTAL_ALIGNMENT_CENTER, art_rect.size.x, int(u * 6), Color(1, 1, 1, 0.5))
	# Estrellas de nivel
	if type & OCG.TYPE_MONSTER and not type & OCG.TYPE_LINK:
		var lv: int = min(int(data.get("rank", 0) if type & OCG.TYPE_XYZ else data.get("level", 0)), 12)
		var star_col := Color("303030") if type & OCG.TYPE_XYZ else GBA.C_YELLOW
		for i in lv:
			var sx := GRID_W - 3.5 - i * 1.5
			if sx < 2: break
			px.call(sx, 20.5, 1, 1, star_col)
	# Caja inferior
	px.call(2, 22, GRID_W - 4, 8, Color(0.04, 0.05, 0.12))
	var fs := int(max(8.0, u * 3.2))
	var y1 := rect.position.y + 25.6 * u
	var y2 := rect.position.y + 29.2 * u
	var x := rect.position.x + 3 * u
	var w := (GRID_W - 6) * u
	if type & OCG.TYPE_MONSTER:
		var atk_col := GBA.C_WHITE
		var atk: int = data.get("attack", 0)
		var base: int = data.get("base_attack", atk)
		if atk > base: atk_col = GBA.C_GREEN
		elif atk < base: atk_col = Color("ff8070")
		draw_string(GBA.font_big, Vector2(x, y1), "A" + _stat(atk), HORIZONTAL_ALIGNMENT_RIGHT, w, fs, atk_col)
		if type & OCG.TYPE_LINK:
			draw_string(GBA.font_big, Vector2(x, y2), "L-%d" % int(data.get("link", 0)), HORIZONTAL_ALIGNMENT_RIGHT, w, fs, Color("80b0ff"))
		else:
			draw_string(GBA.font_big, Vector2(x, y2), "D" + _stat(data.get("defense", 0)), HORIZONTAL_ALIGNMENT_RIGHT, w, fs, GBA.C_TEXT_DIM)
	else:
		var label2 := "MAGIA" if type & OCG.TYPE_SPELL else "TRAMPA"
		draw_string(GBA.font_big, Vector2(x - u, (y1 + y2) / 2.0), label2, HORIZONTAL_ALIGNMENT_CENTER, w + 2 * u, fs, frame.lightened(0.4))
