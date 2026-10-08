extends Node
## Autoload "GBA": aspecto pixel-art inspirado en los Yu-Gi-Oh! de Game Boy Advance
## (serie World Championship Tournament). Todas las texturas se generan por código (son originales);
## cualquiera puede sustituirse dejando un PNG con el mismo nombre en la carpeta skin/ (ver Paths).

const PX := 3 # tamaño en pantalla de un "píxel GBA"

# Paleta
const C_OUTLINE := Color("060914")
const C_NAVY_DARK := Color("070b1e")
const C_NAVY := Color("0f1940")
const C_NAVY_LIGHT := Color("1b2a66")
const C_GRID := Color("24387e")
const C_WIN := Color("2446b0")
const C_WIN_TOP := Color("3a64d8")
const C_WIN_DARK := Color("12256e")
const C_WIN_EDGE := Color("8fb0ff")
const C_WHITE := Color("f4f4ec")
const C_GOLD := Color("f0c048")
const C_GOLD_DARK := Color("a06a18")
const C_RED := Color("e0402c")
const C_GREEN := Color("50d070")
const C_TEXT_DIM := Color("9fb0d8")
const C_YELLOW := Color("ffe040")

var font_ui: FontFile
var font_big: FontFile
var theme: Theme
var _cache := {}


func _enter_tree() -> void:
	font_ui = _load_font("res://assets/fonts/VT323-Regular.ttf")
	font_big = _load_font("res://assets/fonts/Silkscreen-Regular.ttf")
	theme = _build_theme()


func _load_font(path: String) -> FontFile:
	# Se carga como recurso importado (así funciona también en el juego exportado)
	var f: FontFile = (load(path) as FontFile).duplicate()
	f.antialiasing = TextServer.FONT_ANTIALIASING_NONE
	f.hinting = TextServer.HINTING_NONE
	f.subpixel_positioning = TextServer.SUBPIXEL_POSITIONING_DISABLED
	f.generate_mipmaps = false
	return f


# ================================================================ texturas

## Devuelve una textura del skin: skin/<name>.png si existe, si no la generada.
func tex(name: String) -> Texture2D:
	if _cache.has(name):
		return _cache[name]
	var t: Texture2D = null
	var custom := Paths.skin_dir.path_join(name + ".png")
	if FileAccess.file_exists(custom):
		var img := Image.new()
		if img.load(custom) == OK:
			t = ImageTexture.create_from_image(img)
	if t == null:
		var img2: Image = call("_gen_" + name)
		t = ImageTexture.create_from_image(img2)
	_cache[name] = t
	return t


func _img(w: int, h: int, fill := Color.TRANSPARENT) -> Image:
	var img := Image.create(w, h, false, Image.FORMAT_RGBA8)
	img.fill(fill)
	return img


func _rect(img: Image, x: int, y: int, w: int, h: int, c: Color) -> void:
	img.fill_rect(Rect2i(x, y, w, h), c)


func _frame(img: Image, x: int, y: int, w: int, h: int, c: Color) -> void:
	_rect(img, x, y, w, 1, c)
	_rect(img, x, y + h - 1, w, 1, c)
	_rect(img, x, y, 1, h, c)
	_rect(img, x + w - 1, y, 1, h, c)


## Dibuja un mapa de caracteres (cada carácter es un color de `pal`, '.' transparente).
func _sprite(img: Image, x: int, y: int, rows: Array, pal: Dictionary) -> void:
	for j in rows.size():
		var row: String = rows[j]
		for i in row.length():
			var ch := row[i]
			if pal.has(ch):
				img.set_pixel(x + i, y + j, pal[ch])


func _upscale(img: Image, k := PX) -> Image:
	img.resize(img.get_width() * k, img.get_height() * k, Image.INTERPOLATE_NEAREST)
	return img


## Ventana azul con borde blanco redondeado, como los menús de GBA.
func _gen_window() -> Image:
	var img := _img(12, 12)
	_rect(img, 1, 0, 10, 12, C_OUTLINE)
	_rect(img, 0, 1, 12, 10, C_OUTLINE)
	_rect(img, 2, 1, 8, 10, C_WHITE)
	_rect(img, 1, 2, 10, 8, C_WHITE)
	_rect(img, 2, 2, 8, 8, C_WIN_EDGE)
	_rect(img, 3, 3, 6, 6, C_WIN)
	_rect(img, 3, 3, 6, 1, C_WIN_TOP)
	return _upscale(img)


## Ventana oscura con borde dorado (textos, registro).
func _gen_window_dark() -> Image:
	var img := _img(12, 12)
	_rect(img, 1, 0, 10, 12, C_OUTLINE)
	_rect(img, 0, 1, 12, 10, C_OUTLINE)
	_rect(img, 2, 1, 8, 10, C_GOLD)
	_rect(img, 1, 2, 10, 8, C_GOLD)
	_rect(img, 2, 2, 8, 8, C_GOLD_DARK)
	_rect(img, 3, 3, 6, 6, Color("0a1030"))
	return _upscale(img)


func _gen_button() -> Image:
	return _button_img(Color("2f4cb8"), C_WIN_EDGE, Color("16287a"), C_WHITE)


func _gen_button_hover() -> Image:
	return _button_img(Color("3d60d8"), C_YELLOW, Color("1c3290"), C_YELLOW)


func _gen_button_pressed() -> Image:
	return _button_img(Color("1c3290"), Color("16287a"), C_WIN_EDGE, C_YELLOW)


func _gen_button_disabled() -> Image:
	return _button_img(Color("2a3150"), Color("3c4466"), Color("1a1f36"), Color("4a5272"))


func _button_img(fill: Color, light: Color, dark: Color, border: Color) -> Image:
	var img := _img(10, 10)
	_rect(img, 1, 0, 8, 10, border)
	_rect(img, 0, 1, 10, 8, border)
	_rect(img, 1, 1, 8, 8, fill)
	_rect(img, 1, 1, 8, 1, light)
	_rect(img, 1, 1, 1, 8, light)
	_rect(img, 1, 8, 8, 1, dark)
	_rect(img, 8, 1, 1, 8, dark)
	return _upscale(img)


## Losetas del tapete de duelo (se repite).
func _gen_mat_tile() -> Image:
	var img := _img(16, 16, C_NAVY)
	for y in 16:
		for x in 16:
			if (x + y) % 16 == 0 or (x - y + 16) % 16 == 0:
				img.set_pixel(x, y, C_NAVY_LIGHT)
			elif (x * 7 + y * 3) % 11 == 0:
				img.set_pixel(x, y, Color("0c1536"))
	img.set_pixel(8, 8, C_GRID)
	return _upscale(img)


func _zone_img(inner: Color, icon: Array, icon_pal: Dictionary) -> Image:
	var img := _img(24, 34)
	_rect(img, 0, 0, 24, 34, C_OUTLINE)
	_rect(img, 1, 1, 22, 32, inner.darkened(0.55))
	_rect(img, 1, 1, 22, 1, inner.lightened(0.15))
	_rect(img, 1, 1, 1, 32, inner.lightened(0.15))
	_rect(img, 1, 32, 22, 1, inner.darkened(0.75))
	_rect(img, 22, 1, 1, 32, inner.darkened(0.75))
	_rect(img, 3, 3, 18, 28, Color(0.02, 0.03, 0.09, 0.85))
	_frame(img, 3, 3, 18, 28, inner.darkened(0.2))
	if not icon.is_empty():
		_sprite(img, 12 - icon[0].length() / 2, 17 - icon.size() / 2, icon, icon_pal)
	return _upscale(img)


const ICON_SWORD := [
	"......W", ".....WG", "....WG.", "Y..WG..", ".YWG...", ".BY....", "B.Y....",
]
const ICON_SCROLL := [
	".WWWWW.", "W.....W", ".WGGGW.", ".W...W.", ".WGGGW.", "W.....W", ".WWWWW.",
]
const ICON_SKULL := [
	".WWWWW.", "WWWWWWW", "W.WWW.W", "WWWWWWW", ".WW.WW.", ".WWWWW.", ".W.W.W.",
]
const ICON_DECK := [
	"..WWWW.", ".WGGGW.", "WWWWGW.", "WGGWGW.", "WGGWW..", "WGGW...", "WWWW...",
]
const ICON_STAR := [
	"...W...", "...W...", "WWWWWWW", ".WWWWW.", "..WWW..", ".WW.WW.", "W.....W",
]
const ICON_VOID := [
	"..WWW..", ".W...W.", "W..G..W", "W.GGG.W", "W..G..W", ".W...W.", "..WWW..",
]
const ICON_MOUNTAIN := [
	".......", "...W...", "..WGW..", ".WGGGW.", ".WGGGGW", "WGGGGGW", "WWWWWWW",
]


func _icon_pal(c: Color) -> Dictionary:
	return {"W": Color(c, 0.55), "G": Color(c, 0.3), "Y": Color(C_GOLD, 0.5), "B": Color(c, 0.4)}


func _gen_zone_monster() -> Image:
	return _zone_img(Color("c0702c"), ICON_SWORD, _icon_pal(Color("ffb070")))


func _gen_zone_spell() -> Image:
	return _zone_img(Color("2a9c88"), ICON_SCROLL, _icon_pal(Color("80f0d8")))


func _gen_zone_field() -> Image:
	return _zone_img(Color("3f8a3a"), ICON_MOUNTAIN, _icon_pal(Color("a0f090")))


func _gen_zone_grave() -> Image:
	return _zone_img(Color("6a6a7a"), ICON_SKULL, _icon_pal(Color("d0d0e0")))


func _gen_zone_deck() -> Image:
	return _zone_img(Color("8a5a2a"), ICON_DECK, _icon_pal(Color("f0c080")))


func _gen_zone_extra() -> Image:
	return _zone_img(Color("7a3fa0"), ICON_STAR, _icon_pal(Color("e0a0ff")))


func _gen_zone_banish() -> Image:
	return _zone_img(Color("2c5ab0"), ICON_VOID, _icon_pal(Color("a0c8ff")))


func _gen_zone_emz() -> Image:
	return _zone_img(Color("b04a8a"), ICON_STAR, _icon_pal(Color("ffa0e0")))


## Dorso de carta (diseño propio).
func _gen_card_back() -> Image:
	var img := _img(24, 34)
	var brown := Color("6a3818")
	_rect(img, 0, 0, 24, 34, Color("1a0c04"))
	_rect(img, 1, 1, 22, 32, Color("8c4c20"))
	_rect(img, 2, 2, 20, 30, brown)
	for y in range(2, 32):
		for x in range(2, 22):
			if (x + y) % 4 == 0:
				img.set_pixel(x, y, brown.darkened(0.15))
	var cx := 11.5
	var cy := 16.5
	for y in 34:
		for x in 24:
			var d := pow((x - cx) / 8.0, 2) + pow((y - cy) / 11.0, 2)
			if d < 1.0:
				img.set_pixel(x, y, Color("2a1206") if d < 0.72 else C_GOLD)
	# Remolino central
	var swirl := [
		"..YYY..", ".Y...Y.", "Y..Y..Y", "Y.Y.Y.Y", "Y..YY.Y", ".Y....Y", "..YYYY.",
	]
	_sprite(img, 9, 13, swirl, {"Y": Color("e09a30")})
	_frame(img, 1, 1, 22, 32, Color("c07a30"))
	return img


## Iconos pequeños para la interfaz
func _gen_icon_atk() -> Image:
	var img := _img(9, 9)
	_sprite(img, 0, 0, [
		"........W", ".......WG", "......WG.", ".....WG..", "Y...WG...", ".Y.WG....", "..YG.....", ".BBY.....", "BB..Y....",
	], {"W": C_WHITE, "G": Color("b0b8c8"), "Y": C_GOLD, "B": Color("8a5a2a")})
	return _upscale(img, 2)


func _gen_icon_def() -> Image:
	var img := _img(9, 9)
	_sprite(img, 0, 0, [
		".WWWWWWW.", "WBBBBBBBW", "WBBGGGBBW", "WBBGBGBBW", "WBBGGGBBW", ".WBBBBBW.", ".WBBBBBW.", "..WBBBW..", "...WWW...",
	], {"W": C_WHITE, "B": Color("3a64d8"), "G": C_GOLD})
	return _upscale(img, 2)


func _gen_icon_star() -> Image:
	var img := _img(7, 7)
	_sprite(img, 0, 0, ICON_STAR, {"W": Color("ffd030")})
	img.set_pixel(3, 3, Color("ff8a20"))
	return img


func _gen_icon_lp() -> Image:
	var img := _img(9, 8)
	_sprite(img, 0, 0, [
		".RR...RR.", "RWRR.RRRR", "RWRRRRRRR", "RRRRRRRRR", ".RRRRRRR.", "..RRRRR..", "...RRR...", "....R....",
	], {"R": C_RED, "W": C_WHITE})
	return _upscale(img, 2)


# ================================================================ tema de la interfaz

func _stylebox(name: String, margin: int, content: int) -> StyleBoxTexture:
	var sb := StyleBoxTexture.new()
	sb.texture = tex(name)
	sb.set_texture_margin_all(margin)
	sb.set_content_margin_all(content)
	sb.axis_stretch_horizontal = StyleBoxTexture.AXIS_STRETCH_MODE_STRETCH
	sb.axis_stretch_vertical = StyleBoxTexture.AXIS_STRETCH_MODE_STRETCH
	return sb


func window_box(content := 14) -> StyleBoxTexture:
	return _stylebox("window", 4 * PX, content)


func window_dark_box(content := 12) -> StyleBoxTexture:
	return _stylebox("window_dark", 4 * PX, content)


func _build_theme() -> Theme:
	var t := Theme.new()
	t.default_font = font_ui
	t.default_font_size = 22
	t.set_color("font_color", "Label", C_WHITE)
	t.set_color("font_shadow_color", "Label", Color(0, 0, 0, 0.6))
	t.set_constant("shadow_offset_x", "Label", 1)
	t.set_constant("shadow_offset_y", "Label", 1)
	t.set_stylebox("panel", "PanelContainer", window_box())
	t.set_stylebox("panel", "Panel", window_box())
	var states := {"normal": "button", "hover": "button_hover", "pressed": "button_pressed",
			"disabled": "button_disabled", "focus": "button_hover"}
	for type in ["Button", "OptionButton", "CheckBox"]:
		for st in states:
			if type == "CheckBox":
				t.set_stylebox(st, type, StyleBoxEmpty.new())
				continue
			var sb := _stylebox(states[st], 3 * PX, 0)
			sb.content_margin_left = 12
			sb.content_margin_right = 12
			sb.content_margin_top = 6
			sb.content_margin_bottom = 6
			t.set_stylebox(st, type, sb)
		t.set_color("font_color", type, C_WHITE)
		t.set_color("font_hover_color", type, C_YELLOW)
		t.set_color("font_pressed_color", type, C_YELLOW)
		t.set_color("font_focus_color", type, C_YELLOW)
		t.set_color("font_disabled_color", type, Color("5a6280"))
	t.set_stylebox("panel", "PopupMenu", window_box(8))
	t.set_color("font_color", "PopupMenu", C_WHITE)
	t.set_color("font_hover_color", "PopupMenu", C_YELLOW)
	var hover := StyleBoxFlat.new()
	hover.bg_color = Color(1, 0.9, 0.3, 0.25)
	hover.anti_aliasing = false
	t.set_stylebox("hover", "PopupMenu", hover)
	t.set_font_size("font_size", "PopupMenu", 22)
	t.set_constant("v_separation", "PopupMenu", 10)
	for type in ["LineEdit", "SpinBox", "ItemList"]:
		t.set_stylebox("normal" if type != "ItemList" else "panel", type, window_dark_box(8))
		t.set_stylebox("focus", type, window_dark_box(8))
	t.set_color("font_color", "LineEdit", C_WHITE)
	t.set_color("font_color", "ItemList", C_WHITE)
	t.set_color("font_selected_color", "ItemList", C_YELLOW)
	t.set_stylebox("normal", "RichTextLabel", StyleBoxEmpty.new())
	t.set_color("default_color", "RichTextLabel", C_WHITE)
	var bar_bg := StyleBoxFlat.new()
	bar_bg.bg_color = C_OUTLINE
	bar_bg.anti_aliasing = false
	bar_bg.set_border_width_all(2)
	bar_bg.border_color = C_WHITE
	var bar_fill := StyleBoxFlat.new()
	bar_fill.bg_color = C_GREEN
	bar_fill.anti_aliasing = false
	t.set_stylebox("background", "ProgressBar", bar_bg)
	t.set_stylebox("fill", "ProgressBar", bar_fill)
	t.set_color("font_color", "ProgressBar", C_WHITE)
	var scroll_bg := StyleBoxFlat.new()
	scroll_bg.bg_color = Color(0, 0, 0, 0.3)
	var grab := StyleBoxFlat.new()
	grab.bg_color = C_WIN_EDGE
	grab.anti_aliasing = false
	for sbtype in ["VScrollBar", "HScrollBar"]:
		t.set_stylebox("scroll", sbtype, scroll_bg)
		t.set_stylebox("grabber", sbtype, grab)
		t.set_stylebox("grabber_highlight", sbtype, grab)
		t.set_stylebox("grabber_pressed", sbtype, grab)
	t.set_stylebox("panel", "TooltipPanel", window_dark_box(6))
	return t


## Etiqueta con la fuente "grande" pixelada (números, rótulos).
func big_label(text: String, size := 24, color := C_WHITE) -> Label:
	var l := Label.new()
	l.text = text
	l.add_theme_font_override("font", font_big)
	l.add_theme_font_size_override("font_size", size)
	l.add_theme_color_override("font_color", color)
	l.add_theme_color_override("font_outline_color", C_OUTLINE)
	l.add_theme_constant_override("outline_size", 6)
	return l


## Colores del marco de carta según el tipo.
static func frame_color(type: int) -> Color:
	if type & OCG.TYPE_SPELL: return Color("1d9e74")
	if type & OCG.TYPE_TRAP: return Color("bc4a84")
	if type & OCG.TYPE_TOKEN: return Color("9a9aa0")
	if type & OCG.TYPE_LINK: return Color("2b5fa8")
	if type & OCG.TYPE_XYZ: return Color("303036")
	if type & OCG.TYPE_SYNCHRO: return Color("d8d8d8")
	if type & OCG.TYPE_FUSION: return Color("8e4ab5")
	if type & OCG.TYPE_RITUAL: return Color("5d8ccf")
	if type & OCG.TYPE_EFFECT: return Color("d0702c")
	return Color("d0a850")


const ATTR_COLORS := {0x01: Color("8a5a2a"), 0x02: Color("2a7ad8"), 0x04: Color("e0402c"),
		0x08: Color("40b050"), 0x10: Color("f0d040"), 0x20: Color("7a2ab0"), 0x40: Color("e0b030")}
const ATTR_LETTER := {0x01: "T", 0x02: "A", 0x04: "F", 0x08: "V", 0x10: "L", 0x20: "O", 0x40: "D"}
