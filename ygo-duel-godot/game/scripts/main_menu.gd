extends Control
## Menú principal: mazos de la carpeta Decks/, quién empieza, LP, velocidad de animación e imágenes.

var deck_paths: PackedStringArray
var my_deck: OptionButton
var cpu_deck: OptionButton
var first: OptionButton
var speed: OptionButton
var lp: SpinBox
var images: CheckBox
var start: Button
var info: Label


func _ready() -> void:
	theme = GBA.theme
	texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	var bg := TextureRect.new()
	bg.texture = GBA.tex("mat_tile")
	bg.stretch_mode = TextureRect.STRETCH_TILE
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(bg)
	var center := CenterContainer.new()
	center.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(center)
	var outer := VBoxContainer.new()
	outer.add_theme_constant_override("separation", 14)
	center.add_child(outer)
	var title := GBA.big_label("YU-GI-OH! DUEL", 56, GBA.C_GOLD)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	outer.add_child(title)
	var ver := YGODuel.get_core_version()
	var sub := GBA.big_label("OCGCORE v%d.%d  ·  %d CARTAS" % [ver[0], ver[1], CardDB.db.get_card_count()], 16, GBA.C_TEXT_DIM)
	sub.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	outer.add_child(sub)
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", GBA.window_box(22))
	outer.add_child(panel)
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 12)
	panel.add_child(v)
	var grid := GridContainer.new()
	grid.columns = 2
	grid.add_theme_constant_override("h_separation", 16)
	grid.add_theme_constant_override("v_separation", 10)
	v.add_child(grid)
	my_deck = _option(grid, "Tu mazo")
	cpu_deck = _option(grid, "Mazo de la CPU")
	first = _option(grid, "Empieza")
	for t in ["Aleatorio", "Yo", "La CPU"]:
		first.add_item(t)
	speed = _option(grid, "Animaciones")
	for t in ["Normales", "Rápidas", "Desactivadas"]:
		speed.add_item(t)
	var l := Label.new()
	l.text = "Life Points"
	grid.add_child(l)
	lp = SpinBox.new()
	lp.min_value = 100
	lp.max_value = 99999
	lp.step = 100
	lp.value = 8000
	grid.add_child(lp)
	images = CheckBox.new()
	images.text = "Descargar imágenes de las cartas que falten (pics/)"
	images.button_pressed = CardImages.download_enabled
	images.toggled.connect(func(on): CardImages.download_enabled = on)
	v.add_child(images)
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 8)
	v.add_child(row)
	var open_b := Button.new()
	open_b.text = "Abrir carpeta Decks"
	open_b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	open_b.pressed.connect(func(): OS.shell_open(Paths.decks_dir))
	row.add_child(open_b)
	var reload_b := Button.new()
	reload_b.text = "Recargar mazos"
	reload_b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	reload_b.pressed.connect(_reload_decks)
	row.add_child(reload_b)
	start = Button.new()
	start.text = "¡A DUELO!"
	start.custom_minimum_size = Vector2(0, 58)
	start.add_theme_font_override("font", GBA.font_big)
	start.add_theme_font_size_override("font_size", 28)
	start.pressed.connect(_on_start)
	v.add_child(start)
	info = Label.new()
	info.add_theme_font_size_override("font_size", 20)
	info.add_theme_color_override("font_color", GBA.C_TEXT_DIM)
	info.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	info.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	info.custom_minimum_size.x = 560
	v.add_child(info)
	_reload_decks()


func _reload_decks() -> void:
	deck_paths = CardDB.deck_paths()
	var prev_my := my_deck.selected
	var prev_cpu := cpu_deck.selected
	my_deck.clear()
	cpu_deck.clear()
	for p in deck_paths:
		my_deck.add_item(p.get_file().get_basename())
		cpu_deck.add_item(p.get_file().get_basename())
	if deck_paths.size() > 0:
		my_deck.select(clamp(prev_my, 0, deck_paths.size() - 1))
		cpu_deck.select(clamp(prev_cpu if prev_cpu >= 0 else 1, 0, deck_paths.size() - 1))
	start.disabled = deck_paths.is_empty()
	var cached := 0
	var total := {}
	for p in deck_paths:
		var d := CardDB.load_deck(p)
		for c in d.main + d.extra + d.side:
			total[c] = true
	for c in total:
		if CardImages.has_image(c):
			cached += 1
	info.text = "%d mazos en %s\nImágenes en caché (pics/): %d de %d\nPasa el ratón por una carta para leerla · clic en tus cartas para ver sus acciones" % [
			deck_paths.size(), Paths.decks_dir, cached, total.size()]
	if deck_paths.is_empty():
		info.text = "No hay mazos .ydk en " + Paths.decks_dir


func _option(grid: GridContainer, label: String) -> OptionButton:
	var l := Label.new()
	l.text = label
	grid.add_child(l)
	var o := OptionButton.new()
	o.custom_minimum_size.x = 320
	grid.add_child(o)
	return o


func _on_start() -> void:
	Sfx.play("turn")
	DuelBoard.config = {
		"deck0": deck_paths[my_deck.selected],
		"deck1": deck_paths[cpu_deck.selected],
		"first": ["random", "me", "cpu"][first.selected],
		"lp": int(lp.value),
		"speed": speed.selected,
	}
	get_tree().change_scene_to_file("res://scenes/duel.tscn")
