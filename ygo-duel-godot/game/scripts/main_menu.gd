extends Control
## Menú principal: elegir mazos, quién empieza y LP iniciales.

var deck_paths: PackedStringArray
var my_deck: OptionButton
var cpu_deck: OptionButton
var first: OptionButton
var lp: SpinBox
var images: CheckBox


func _ready() -> void:
	var th := Theme.new()
	th.default_font_size = 18
	theme = th
	var bg := ColorRect.new()
	bg.color = Color("0d1220")
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(bg)
	var center := CenterContainer.new()
	center.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(center)
	var panel := PanelContainer.new()
	var sb := StyleBoxFlat.new()
	sb.bg_color = Color("151d30")
	sb.border_color = Color("d9a441")
	sb.set_border_width_all(2)
	sb.set_corner_radius_all(10)
	sb.set_content_margin_all(28)
	panel.add_theme_stylebox_override("panel", sb)
	center.add_child(panel)
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 14)
	panel.add_child(v)
	var title := Label.new()
	title.text = "YU-GI-OH! DUEL — Godot + ocgcore"
	title.add_theme_font_size_override("font_size", 34)
	title.add_theme_color_override("font_color", Color("f0c060"))
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(title)
	var sub := Label.new()
	var ver := YGODuel.get_core_version()
	sub.text = "Prototipo estilo EDOPro · ocgcore v%d.%d · %d cartas cargadas" % [ver[0], ver[1], CardDB.db.get_card_count()]
	sub.modulate = Color(0.7, 0.75, 0.85)
	sub.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(sub)
	var grid := GridContainer.new()
	grid.columns = 2
	grid.add_theme_constant_override("h_separation", 16)
	grid.add_theme_constant_override("v_separation", 10)
	v.add_child(grid)
	deck_paths = CardDB.deck_paths()
	my_deck = _option(grid, "Tu mazo")
	cpu_deck = _option(grid, "Mazo de la CPU")
	for p in deck_paths:
		my_deck.add_item(p.get_file().get_basename())
		cpu_deck.add_item(p.get_file().get_basename())
	if deck_paths.size() > 1:
		cpu_deck.select(1)
	first = _option(grid, "Empieza")
	for t in ["Aleatorio", "Yo", "La CPU"]:
		first.add_item(t)
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
	images.text = "Descargar imágenes de las cartas (ygoprodeck)"
	images.button_pressed = true
	v.add_child(images)
	var start := Button.new()
	start.text = "¡A DUELO!"
	start.custom_minimum_size = Vector2(0, 54)
	start.add_theme_font_size_override("font_size", 24)
	start.pressed.connect(_on_start)
	v.add_child(start)
	var help := Label.new()
	help.text = "Clic en tus cartas para ver sus acciones · Pasa el ratón por encima para leerlas\nMazos en res://decks o user://decks (formato .ydk de EDOPro)"
	help.modulate = Color(0.6, 0.65, 0.75)
	help.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	help.add_theme_font_size_override("font_size", 14)
	v.add_child(help)
	if deck_paths.is_empty():
		start.disabled = true
		sub.text = "No se encontraron mazos (.ydk)"


func _option(grid: GridContainer, label: String) -> OptionButton:
	var l := Label.new()
	l.text = label
	grid.add_child(l)
	var o := OptionButton.new()
	o.custom_minimum_size.x = 280
	grid.add_child(o)
	return o


func _on_start() -> void:
	CardImages.download_enabled = images.button_pressed
	DuelBoard.config = {
		"deck0": deck_paths[my_deck.selected],
		"deck1": deck_paths[cpu_deck.selected],
		"first": ["random", "me", "cpu"][first.selected],
		"lp": int(lp.value),
	}
	get_tree().change_scene_to_file("res://scenes/duel.tscn")
