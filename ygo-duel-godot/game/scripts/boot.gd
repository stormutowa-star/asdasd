extends Control
## Pantalla de arranque: descarga a pics/ las imágenes de todas las cartas de los mazos de Decks/.

var bar: ProgressBar
var status: Label
var detail: Label
var btn: Button
var back: TextureRect
var _t := 0.0
var _done := false


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
	var v := VBoxContainer.new()
	v.add_theme_constant_override("separation", 18)
	center.add_child(v)
	var title := GBA.big_label("YU-GI-OH! DUEL", 52, GBA.C_GOLD)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	v.add_child(title)
	var cc := CenterContainer.new()
	v.add_child(cc)
	back = TextureRect.new()
	back.texture = GBA.tex("card_back")
	back.custom_minimum_size = Vector2(96, 136)
	back.stretch_mode = TextureRect.STRETCH_SCALE
	back.pivot_offset = Vector2(48, 68)
	cc.add_child(back)
	var panel := PanelContainer.new()
	panel.custom_minimum_size.x = 620
	v.add_child(panel)
	var pv := VBoxContainer.new()
	pv.add_theme_constant_override("separation", 10)
	panel.add_child(pv)
	status = Label.new()
	status.text = "Comprobando imágenes de las cartas..."
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	status.add_theme_font_size_override("font_size", 24)
	pv.add_child(status)
	bar = ProgressBar.new()
	bar.custom_minimum_size.y = 22
	bar.show_percentage = false
	pv.add_child(bar)
	detail = Label.new()
	detail.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	detail.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	detail.add_theme_color_override("font_color", GBA.C_TEXT_DIM)
	detail.add_theme_font_size_override("font_size", 18)
	detail.text = "Mazos: %s\nImágenes: %s" % [Paths.decks_dir, Paths.pics_dir]
	pv.add_child(detail)
	btn = Button.new()
	btn.text = "Saltar"
	btn.pressed.connect(_continue)
	pv.add_child(btn)
	CardImages.preload_progress.connect(_on_progress)
	CardImages.preload_finished.connect(_on_finished)
	var missing := CardImages.preload_decks()
	if missing > 0:
		status.text = "Descargando %d imágenes de cartas..." % missing
		bar.max_value = missing
		bar.value = 0


func _process(delta: float) -> void:
	_t += delta
	back.scale.x = cos(_t * 3.0)


func _on_progress(done: int, total: int, failed: int) -> void:
	bar.max_value = total
	bar.value = done
	status.text = "Descargando imágenes: %d / %d" % [done, total]
	if failed > 0:
		status.text += "  (%d fallidas)" % failed


func _on_finished(done: int, failed: int) -> void:
	_done = true
	if failed > 0:
		status.text = "No se pudieron descargar %d imágenes (¿sin conexión?). Se reintentará al pasar el cursor." % failed
		btn.text = "Continuar"
		return
	status.text = "Imágenes listas (%d descargadas)" % done if done > 0 else "Todas las imágenes ya estaban en pics/"
	bar.max_value = 1
	bar.value = 1
	await get_tree().create_timer(0.6).timeout
	_continue()


var _leaving := false


func _continue() -> void:
	if _leaving or not is_inside_tree():
		return
	_leaving = true
	Sfx.play("select")
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")
