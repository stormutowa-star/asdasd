extends Node
## Autoload "Paths": carpetas de datos del jugador, como en EDOPro.
##  - En el editor: la carpeta del proyecto (game/).
##  - Exportado: la carpeta del ejecutable (si no se puede escribir en ella, user://).
## Dentro: Decks/ (mazos .ydk), pics/ (imágenes de cartas) y skin/ (texturas propias opcionales).

var base_dir := ""
var decks_dir := ""
var pics_dir := ""
var skin_dir := ""


func _enter_tree() -> void:
	base_dir = _pick_base_dir()
	decks_dir = base_dir.path_join("Decks")
	pics_dir = base_dir.path_join("pics")
	skin_dir = base_dir.path_join("skin")
	DirAccess.make_dir_recursive_absolute(decks_dir)
	DirAccess.make_dir_recursive_absolute(pics_dir)
	# Evita que el editor de Godot intente importar los .jpg descargados
	if OS.has_feature("editor") and not FileAccess.file_exists(pics_dir.path_join(".gdignore")):
		var f := FileAccess.open(pics_dir.path_join(".gdignore"), FileAccess.WRITE)
		if f:
			f.store_string("")
	_install_default_decks()
	print("Datos del jugador en: ", base_dir)


func _pick_base_dir() -> String:
	if OS.has_feature("editor"):
		return ProjectSettings.globalize_path("res://").trim_suffix("/")
	var exe_dir := OS.get_executable_path().get_base_dir()
	if _is_writable(exe_dir):
		return exe_dir
	return ProjectSettings.globalize_path("user://").trim_suffix("/")


func _is_writable(dir: String) -> bool:
	var probe := dir.path_join(".write_test")
	var f := FileAccess.open(probe, FileAccess.WRITE)
	if f == null:
		return false
	f.close()
	DirAccess.remove_absolute(probe)
	return true


## Copia los mazos incluidos en el juego a Decks/ si la carpeta no tiene ninguno.
func _install_default_decks() -> void:
	if decks_dir == ProjectSettings.globalize_path("res://Decks"):
		return
	for f in DirAccess.get_files_at(decks_dir):
		if f.ends_with(".ydk"):
			return
	for f in DirAccess.get_files_at("res://Decks"):
		if f.ends_with(".ydk"):
			var data := FileAccess.get_file_as_bytes("res://Decks/" + f)
			var out := FileAccess.open(decks_dir.path_join(f), FileAccess.WRITE)
			if out:
				out.store_buffer(data)


func pic_path(code: int) -> String:
	return pics_dir.path_join("%d.jpg" % code)


func deck_files() -> PackedStringArray:
	var out := PackedStringArray()
	for f in DirAccess.get_files_at(decks_dir):
		if f.to_lower().ends_with(".ydk"):
			out.append(decks_dir.path_join(f))
	out.sort()
	return out
