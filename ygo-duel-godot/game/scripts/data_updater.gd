extends Node
## Autoload "DataUpdater": descarga en un hilo la base de cartas actual de ProjectIgnis (la de EDOPro)
## y la instala en expansions/ para poder usar mazos con cartas recientes:
##   - BabelCDB: cards.cdb + release-*.cdb + prerelease-*.cdb
##   - CardScripts: scripts base (utility.lua...) + official/ + pre-release/

signal update_finished(ok: bool, message: String)

const SCRIPTS_URL := "https://codeload.github.com/ProjectIgnis/CardScripts/tar.gz/refs/heads/master"
const CDB_URL := "https://codeload.github.com/ProjectIgnis/BabelCDB/tar.gz/refs/heads/master"

var busy := false
var _thread: Thread
var _cancel := false


func expansions_dir() -> String:
	return Paths.base_dir.path_join("expansions")


## Fecha de la última actualización instalada ("" si nunca).
func installed_version() -> String:
	var f := FileAccess.open(expansions_dir().path_join("version.txt"), FileAccess.READ)
	return f.get_as_text().strip_edges() if f else ""


func update_async() -> void:
	if busy:
		return
	busy = true
	_cancel = false
	var urls := {
		"scripts": OS.get_environment("YGO_SCRIPTS_URL") if OS.get_environment("YGO_SCRIPTS_URL") != "" else SCRIPTS_URL,
		"cdb": OS.get_environment("YGO_CDB_URL") if OS.get_environment("YGO_CDB_URL") != "" else CDB_URL,
	}
	_thread = Thread.new()
	_thread.start(_work.bind(urls, expansions_dir()))


func _exit_tree() -> void:
	_cancel = true
	if _thread and _thread.is_started():
		_thread.wait_to_finish()


# ================================================================ hilo

func _work(urls: Dictionary, dest: String) -> void:
	var tmp := dest + ".download"
	_rm_rf(tmp)
	DirAccess.make_dir_recursive_absolute(tmp)
	var msg := ""
	var ok := false
	var scripts_zip := tmp.path_join("scripts.tar.gz")
	var cdb_zip := tmp.path_join("cdb.tar.gz")
	if not _download(urls.scripts, scripts_zip):
		msg = "No se pudieron descargar los scripts de cartas"
	elif not _download(urls.cdb, cdb_zip):
		msg = "No se pudo descargar la base de datos de cartas"
	else:
		var staging := tmp.path_join("expansions")
		var n_cdb := _extract_cdbs(cdb_zip, staging)
		var n_lua := _extract_scripts(scripts_zip, staging.path_join("script"))
		if n_cdb == 0 or n_lua < 100:
			msg = "Los archivos descargados no son válidos"
		else:
			var f := FileAccess.open(staging.path_join("version.txt"), FileAccess.WRITE)
			f.store_string(Time.get_datetime_string_from_system(false, true))
			f.close()
			# El editor de Godot no debe importar los miles de scripts
			FileAccess.open(staging.path_join(".gdignore"), FileAccess.WRITE).close()
			_rm_rf(dest)
			if DirAccess.rename_absolute(staging, dest) == OK:
				ok = true
				msg = "Base de cartas actualizada: %d bases de datos, %d scripts" % [n_cdb, n_lua]
			else:
				msg = "No se pudo instalar en " + dest
	_rm_rf(tmp)
	_finish.call_deferred(ok, msg)


func _finish(ok: bool, msg: String) -> void:
	_thread.wait_to_finish()
	busy = false
	if ok:
		CardDB.reload()
	print("DataUpdater: ", msg)
	update_finished.emit(ok, msg)


func _extract_cdbs(archive: String, out: String) -> int:
	DirAccess.make_dir_recursive_absolute(out)
	var n := [0]
	_untar(archive, func(name: String, data: PackedByteArray):
		var parts := name.split("/")
		if parts.size() != 2 or not parts[1].ends_with(".cdb"):
			return
		var file: String = parts[1]
		if file == "cards.cdb" or file.begins_with("release-") \
				or (file.begins_with("prerelease-") and not file.contains("rush")):
			_write(out.path_join(file), data)
			n[0] += 1)
	return n[0]


func _extract_scripts(archive: String, out: String) -> int:
	var n := [0]
	var made := {}
	_untar(archive, func(name: String, data: PackedByteArray):
		if not name.ends_with(".lua"):
			return
		var rel := name.substr(name.find("/") + 1) # quita "CardScripts-master/"
		if rel.contains("/") and not rel.begins_with("official/") and not rel.begins_with("pre-release/"):
			return
		var path := out.path_join(rel)
		var dir := path.get_base_dir()
		if not made.has(dir):
			DirAccess.make_dir_recursive_absolute(dir)
			made[dir] = true
		_write(path, data)
		n[0] += 1)
	return n[0]


func _write(path: String, data: PackedByteArray) -> void:
	var f := FileAccess.open(path, FileAccess.WRITE)
	if f:
		f.store_buffer(data)
		f.close()


## Recorre un .tar.gz llamando a `cb(nombre, datos)` por cada fichero (lectura lineal).
func _untar(archive: String, cb: Callable) -> void:
	var gz := FileAccess.get_file_as_bytes(archive)
	if gz.is_empty():
		return
	var tar := gz.decompress_dynamic(-1, FileAccess.COMPRESSION_GZIP)
	gz = PackedByteArray()
	var pos := 0
	var long_name := ""
	while pos + 512 <= tar.size() and not _cancel:
		if tar[pos] == 0:
			break
		var name := _cstr(tar, pos, 100)
		var prefix := _cstr(tar, pos + 345, 155)
		if prefix != "":
			name = prefix + "/" + name
		var size := _octal(_cstr(tar, pos + 124, 12))
		var type := char(tar[pos + 156]) if tar[pos + 156] != 0 else "0"
		var data_start := pos + 512
		pos = data_start + int(ceil(size / 512.0)) * 512
		if type == "x":
			# Cabecera PAX: puede traer la ruta larga ("path=...")
			for rec in tar.slice(data_start, data_start + size).get_string_from_utf8().split("\n"):
				var eq := rec.find(" path=")
				if eq >= 0:
					long_name = rec.substr(eq + 6)
			continue
		if type == "L":
			long_name = tar.slice(data_start, data_start + size).get_string_from_utf8()
			continue
		if long_name != "":
			name = long_name
			long_name = ""
		if type == "0":
			cb.call(name, tar.slice(data_start, data_start + size))


func _cstr(b: PackedByteArray, start: int, length: int) -> String:
	var end := start
	while end < start + length and b[end] != 0:
		end += 1
	return b.slice(start, end).get_string_from_utf8()


func _octal(s: String) -> int:
	var v := 0
	for ch in s.strip_edges():
		if ch < "0" or ch > "7":
			break
		v = v * 8 + (ch.unicode_at(0) - 48)
	return v


## Descarga una URL a un fichero (sigue redirecciones).
func _download(url: String, path: String, redirects := 4) -> bool:
	var m := RegEx.create_from_string("^(https?)://([^/:]+)(?::(\\d+))?(/.*)?$").search(url)
	if m == null:
		return false
	var tls := m.get_string(1) == "https"
	var port := int(m.get_string(3)) if m.get_string(3) != "" else (443 if tls else 80)
	var client := HTTPClient.new()
	if client.connect_to_host(m.get_string(2), port, TLSOptions.client() if tls else null) != OK:
		return false
	var t0 := Time.get_ticks_msec()
	while client.get_status() in [HTTPClient.STATUS_CONNECTING, HTTPClient.STATUS_RESOLVING]:
		client.poll()
		OS.delay_msec(10)
		if _cancel or Time.get_ticks_msec() - t0 > 20000:
			return false
	if client.get_status() != HTTPClient.STATUS_CONNECTED:
		return false
	var p := m.get_string(4) if m.get_string(4) != "" else "/"
	if client.request(HTTPClient.METHOD_GET, p, ["User-Agent: YGODuel/1.0"]) != OK:
		return false
	while client.get_status() == HTTPClient.STATUS_REQUESTING:
		client.poll()
		OS.delay_msec(10)
		if _cancel:
			return false
	if not client.has_response():
		return false
	var code := client.get_response_code()
	var headers := client.get_response_headers_as_dictionary()
	if code in [301, 302, 303, 307, 308] and redirects > 0:
		for k in headers:
			if String(k).to_lower() == "location":
				client.close()
				return _download(headers[k], path, redirects - 1)
	if code != 200:
		return false
	var f := FileAccess.open(path, FileAccess.WRITE)
	if f == null:
		return false
	var last := Time.get_ticks_msec()
	while client.get_status() == HTTPClient.STATUS_BODY:
		client.poll()
		var chunk := client.read_response_body_chunk()
		if chunk.size() == 0:
			OS.delay_msec(5)
			if Time.get_ticks_msec() - last > 60000:
				break
		else:
			last = Time.get_ticks_msec()
			f.store_buffer(chunk)
		if _cancel:
			break
	f.close()
	client.close()
	var check := FileAccess.open(path, FileAccess.READ)
	return not _cancel and check != null and check.get_length() > 0


static func _rm_rf(path: String) -> void:
	if not DirAccess.dir_exists_absolute(path):
		return
	for f in DirAccess.get_files_at(path):
		DirAccess.remove_absolute(path.path_join(f))
	for d in DirAccess.get_directories_at(path):
		_rm_rf(path.path_join(d))
	DirAccess.remove_absolute(path)
