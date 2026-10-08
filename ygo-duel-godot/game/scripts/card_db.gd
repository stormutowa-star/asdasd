extends Node
## Autoload "CardDB": base de datos de cartas (.cdb), cadenas del sistema y lectura de mazos .ydk.

const DATA_DIR := "res://data"
const EXPANSION_DIRS := ["res://data/expansions", "user://expansions"]

var db: YGOCardDatabase
var system_strings := {}
var victory_strings := {}
var counter_strings := {}
var setnames := {}
var _cache := {}


func _ready() -> void:
	db = YGOCardDatabase.new()
	var n := db.load_cdb(DATA_DIR + "/cards.cdb")
	print("CardDB: %d cartas en cards.cdb" % n)
	for dir in EXPANSION_DIRS:
		for f in _list_files(dir, ".cdb"):
			print("CardDB: expansión %s (%d cartas)" % [f, db.load_cdb(f)])
	_load_strings(DATA_DIR + "/strings.conf")
	_load_strings(DATA_DIR + "/strings_es.conf") # sobrescribe con el español cuando existe


## Directorios de scripts Lua para YGODuel.add_script_directory (los últimos tienen prioridad).
func script_directories() -> Array:
	var dirs := [DATA_DIR + "/script"]
	for dir in EXPANSION_DIRS:
		if DirAccess.dir_exists_absolute(dir + "/script"):
			dirs.append(dir + "/script")
	return dirs


func get_card(code: int) -> Dictionary:
	if not _cache.has(code):
		_cache[code] = db.get_card(code)
	return _cache[code]


func card_name(code: int) -> String:
	var c := get_card(code)
	return c.get("name", "Carta #%d" % code) if not c.is_empty() else ("Carta #%d" % code if code else "Carta oculta")


func sys(id: int) -> String:
	return system_strings.get(id, "")


## Descripción de un efecto (uint64): (código << 20) | índice de str, o cadena del sistema si código == 0.
func desc_text(desc: int) -> String:
	if desc == 0:
		return ""
	var code := desc >> 20
	var idx := desc & 0xfffff
	if code == 0:
		var s := sys(idx)
		return s if s != "" else "#%d" % idx
	var c := get_card(code)
	if c.is_empty():
		return "Efecto de #%d" % code
	var strs: PackedStringArray = c.get("strings", PackedStringArray())
	if idx < strs.size() and strs[idx] != "":
		return strs[idx]
	return "Efecto de %s" % c.get("name", "")


## Lee un .ydk de EDOPro → {"main": [...], "extra": [...], "side": [...]}
func load_deck(path: String) -> Dictionary:
	var deck := {"main": [], "extra": [], "side": []}
	var f := FileAccess.open(path, FileAccess.READ)
	if f == null:
		push_error("No se pudo abrir el mazo " + path)
		return deck
	var section := "main"
	while not f.eof_reached():
		var line := f.get_line().strip_edges()
		if line == "#main": section = "main"
		elif line == "#extra": section = "extra"
		elif line == "!side": section = "side"
		elif line.is_valid_int(): deck[section].append(line.to_int())
	return deck


func deck_paths() -> PackedStringArray:
	var out := PackedStringArray()
	for f in _list_files("res://decks", ".ydk"):
		out.append(f)
	for f in _list_files("user://decks", ".ydk"):
		out.append(f)
	return out


func _load_strings(path: String) -> void:
	var f := FileAccess.open(path, FileAccess.READ)
	if f == null:
		return
	while not f.eof_reached():
		var line := f.get_line()
		if not line.begins_with("!"):
			continue
		var parts := line.split(" ", false, 2)
		if parts.size() < 3:
			continue
		var key: String = parts[1]
		var id := key.hex_to_int() if key.begins_with("0x") else key.to_int()
		match parts[0]:
			"!system": system_strings[id] = parts[2]
			"!victory": victory_strings[id] = parts[2]
			"!counter": counter_strings[id] = parts[2]
			"!setname": setnames[id] = parts[2]


func _list_files(dir: String, ext: String) -> Array:
	var out := []
	if not DirAccess.dir_exists_absolute(dir):
		return out
	for f in DirAccess.get_files_at(dir):
		if f.ends_with(ext):
			out.append(dir.path_join(f))
	return out
