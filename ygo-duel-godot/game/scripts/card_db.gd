extends Node
## Autoload "CardDB": base de datos de cartas (.cdb), cadenas del sistema y lectura de mazos .ydk.

const DATA_DIR := "res://data"
var EXPANSION_DIRS := ["res://data/expansions", "user://expansions"]

var db: YGOCardDatabase
var system_strings := {}
var victory_strings := {}
var counter_strings := {}
var setnames := {}
var _cache := {}


func _ready() -> void:
	var own := Paths.base_dir.path_join("expansions")
	if not own in EXPANSION_DIRS:
		EXPANSION_DIRS.append(own)
	db = YGOCardDatabase.new()
	reload()
	_load_strings(DATA_DIR + "/strings.conf")
	_load_strings(DATA_DIR + "/strings_es.conf") # sobrescribe con el español cuando existe


## (Re)carga las bases de datos: la incluida y las de expansions/ (p.ej. tras actualizar).
func reload() -> void:
	db.clear()
	_cache.clear()
	var n := db.load_cdb(DATA_DIR + "/cards.cdb")
	print("CardDB: %d cartas en cards.cdb" % n)
	for dir in EXPANSION_DIRS:
		var files := _list_files(dir, ".cdb")
		files.sort_custom(func(a, b): return _cdb_order(a) < _cdb_order(b))
		for f in files:
			print("CardDB: expansión %s (%d cartas)" % [f, db.load_cdb(f)])
	print("CardDB: %d cartas en total" % db.get_card_count())


## cards.cdb primero, luego release-*, luego prerelease-* (los últimos sobrescriben)
func _cdb_order(path: String) -> String:
	var f := path.get_file()
	return ("0" if f == "cards.cdb" else ("1" if f.begins_with("release") else "2")) + f


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


## Lee un mazo: .ydk de EDOPro/YGOPRODeck/Master Duel, o un enlace ydke:// guardado en el fichero.
## → {"main": [...], "extra": [...], "side": [...]}
func load_deck(path: String) -> Dictionary:
	var f := FileAccess.open(path, FileAccess.READ)
	if f == null:
		push_error("No se pudo abrir el mazo " + path)
		return {"main": [], "extra": [], "side": []}
	return parse_deck_text(f.get_as_text())


## Admite el formato YDK (#main / #extra / !side, con comentarios "#..." y códigos con ceros a la izquierda)
## y el formato ydke://main!extra!side! (base64 de enteros de 32 bits little-endian).
func parse_deck_text(text: String) -> Dictionary:
	var deck := {"main": [], "extra": [], "side": []}
	var t := text.strip_edges()
	var ydke := t.find("ydke://")
	if ydke >= 0:
		var parts := t.substr(ydke + 7).split("!")
		var keys := ["main", "extra", "side"]
		for i in min(3, parts.size()):
			var raw := Marshalls.base64_to_raw(parts[i].strip_edges())
			for j in range(0, raw.size() - 3, 4):
				deck[keys[i]].append(raw.decode_u32(j))
		return deck
	var section := "main"
	var num := RegEx.create_from_string("^(\\d+)")
	for raw_line in t.split("\n"):
		var line := raw_line.strip_edges()
		var low := line.to_lower()
		if low.begins_with("#main"): section = "main"
		elif low.begins_with("#extra"): section = "extra"
		elif low.begins_with("!side") or low.begins_with("#side"): section = "side"
		elif line.begins_with("#") or line.begins_with("!"): continue
		else:
			var m := num.search(line)
			if m:
				deck[section].append(m.get_string(1).to_int())
	return deck


## Coloca en el Extra Deck los monstruos de Fusión/Sincronía/Xyz/Link que vengan en el Main (y al revés).
func normalize_deck(deck: Dictionary) -> Dictionary:
	var out := {"main": [], "extra": deck.get("extra", []).duplicate(), "side": deck.get("side", []).duplicate()}
	for c in deck.get("main", []):
		var type: int = get_card(c).get("type", 0)
		if type & OCG.TYPE_MONSTER and OCG.is_extra_deck_type(type):
			out.extra.append(c)
		else:
			out.main.append(c)
	return out


## Códigos del mazo que no están en la base de datos (cartas demasiado nuevas → actualizar base).
func unknown_cards(deck: Dictionary) -> Array:
	var out := []
	for section in ["main", "extra", "side"]:
		for c in deck.get(section, []):
			if not db.has_card(c) and not c in out:
				out.append(c)
	return out


## Guarda un mazo en Decks/<nombre>.ydk y devuelve la ruta.
func save_deck(deck: Dictionary, name: String) -> String:
	var safe := name.validate_filename().strip_edges()
	if safe == "":
		safe = "Mazo"
	var path := Paths.decks_dir.path_join(safe + ".ydk")
	var i := 2
	while FileAccess.file_exists(path):
		path = Paths.decks_dir.path_join("%s (%d).ydk" % [safe, i])
		i += 1
	var f := FileAccess.open(path, FileAccess.WRITE)
	if f == null:
		return ""
	f.store_line("#created by YGO Duel")
	f.store_line("#main")
	for c in deck.main: f.store_line(str(c))
	f.store_line("#extra")
	for c in deck.extra: f.store_line(str(c))
	f.store_line("!side")
	for c in deck.side: f.store_line(str(c))
	f.close()
	return path


## Mazos .ydk de la carpeta Decks/ (ver Paths).
func deck_paths() -> PackedStringArray:
	return Paths.deck_files()


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
