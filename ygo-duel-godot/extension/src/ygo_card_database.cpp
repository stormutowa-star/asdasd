#include "ygo_card_database.h"

#include <cstring>

#include <godot_cpp/classes/file_access.hpp>
#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/variant/utility_functions.hpp>

#include "ocgapi_constants.h"
#include "sqlite3.h"

using namespace godot;

void YGOCardDatabase::_bind_methods() {
	ClassDB::bind_method(D_METHOD("load_cdb", "path"), &YGOCardDatabase::load_cdb);
	ClassDB::bind_method(D_METHOD("has_card", "code"), &YGOCardDatabase::has_card);
	ClassDB::bind_method(D_METHOD("get_card", "code"), &YGOCardDatabase::get_card);
	ClassDB::bind_method(D_METHOD("get_codes"), &YGOCardDatabase::get_codes);
	ClassDB::bind_method(D_METHOD("get_card_count"), &YGOCardDatabase::get_card_count);
	ClassDB::bind_method(D_METHOD("clear"), &YGOCardDatabase::clear);
}

static String column_string(sqlite3_stmt *stmt, int col) {
	const unsigned char *txt = sqlite3_column_text(stmt, col);
	if (txt == nullptr)
		return String();
	return String::utf8(reinterpret_cast<const char *>(txt));
}

// Carga (y fusiona) un fichero .cdb. Devuelve el número de cartas leídas o -1 si hay error.
// Se lee con FileAccess para que funcione también con rutas res:// dentro de un .pck exportado.
int YGOCardDatabase::load_cdb(const String &path) {
	PackedByteArray bytes = FileAccess::get_file_as_bytes(path);
	if (bytes.size() == 0) {
		UtilityFunctions::push_error("YGOCardDatabase: no se pudo leer ", path);
		return -1;
	}
	sqlite3 *db = nullptr;
	if (sqlite3_open(":memory:", &db) != SQLITE_OK) {
		sqlite3_close(db);
		return -1;
	}
	const sqlite3_int64 size = bytes.size();
	auto *buffer = static_cast<unsigned char *>(sqlite3_malloc64(size));
	std::memcpy(buffer, bytes.ptr(), size);
	int rc = sqlite3_deserialize(db, "main", buffer, size, size,
			SQLITE_DESERIALIZE_FREEONCLOSE | SQLITE_DESERIALIZE_READONLY);
	if (rc != SQLITE_OK) {
		UtilityFunctions::push_error("YGOCardDatabase: ", path, " no es una base de datos SQLite válida");
		sqlite3_close(db);
		return -1;
	}
	const char *sql =
			"SELECT d.id, d.alias, d.setcode, d.type, d.atk, d.def, d.level, d.race, d.attribute, "
			"t.name, t.desc, t.str1, t.str2, t.str3, t.str4, t.str5, t.str6, t.str7, t.str8, "
			"t.str9, t.str10, t.str11, t.str12, t.str13, t.str14, t.str15, t.str16 "
			"FROM datas d LEFT JOIN texts t ON d.id = t.id";
	sqlite3_stmt *stmt = nullptr;
	if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
		UtilityFunctions::push_error("YGOCardDatabase: consulta inválida en ", path, ": ", String::utf8(sqlite3_errmsg(db)));
		sqlite3_close(db);
		return -1;
	}
	int count = 0;
	while (sqlite3_step(stmt) == SQLITE_ROW) {
		Entry e;
		e.code = static_cast<uint32_t>(sqlite3_column_int64(stmt, 0));
		e.alias = static_cast<uint32_t>(sqlite3_column_int64(stmt, 1));
		uint64_t setcode = static_cast<uint64_t>(sqlite3_column_int64(stmt, 2));
		for (int i = 0; i < 4; ++i) {
			uint16_t sc = static_cast<uint16_t>((setcode >> (i * 16)) & 0xffff);
			if (sc)
				e.setcodes.push_back(sc);
		}
		e.setcodes.push_back(0);
		e.type = static_cast<uint32_t>(sqlite3_column_int64(stmt, 3));
		e.attack = static_cast<int32_t>(sqlite3_column_int64(stmt, 4));
		e.defense = static_cast<int32_t>(sqlite3_column_int64(stmt, 5));
		if (e.type & TYPE_LINK) {
			e.link_marker = static_cast<uint32_t>(e.defense);
			e.defense = 0;
		}
		uint32_t level = static_cast<uint32_t>(sqlite3_column_int64(stmt, 6));
		e.level = level & 0xff;
		e.lscale = (level >> 24) & 0xff;
		e.rscale = (level >> 16) & 0xff;
		e.race = static_cast<uint64_t>(sqlite3_column_int64(stmt, 7));
		e.attribute = static_cast<uint32_t>(sqlite3_column_int64(stmt, 8));
		e.name = column_string(stmt, 9);
		e.desc = column_string(stmt, 10);
		for (int i = 0; i < 16; ++i)
			e.strings.push_back(column_string(stmt, 11 + i));
		const uint32_t code = e.code;
		cards[code] = std::move(e);
		++count;
	}
	sqlite3_finalize(stmt);
	sqlite3_close(db);
	return count;
}

const YGOCardDatabase::Entry *YGOCardDatabase::find(uint32_t code) const {
	auto it = cards.find(code);
	return it == cards.end() ? nullptr : &it->second;
}

bool YGOCardDatabase::has_card(int code) const {
	return find(static_cast<uint32_t>(code)) != nullptr;
}

Dictionary YGOCardDatabase::get_card(int code) const {
	Dictionary d;
	const Entry *e = find(static_cast<uint32_t>(code));
	if (e == nullptr)
		return d;
	d["code"] = e->code;
	d["alias"] = e->alias;
	PackedInt32Array setcodes;
	for (uint16_t sc : e->setcodes)
		if (sc)
			setcodes.push_back(sc);
	d["setcodes"] = setcodes;
	d["type"] = e->type;
	d["level"] = e->level;
	d["attribute"] = e->attribute;
	d["race"] = static_cast<int64_t>(e->race);
	d["attack"] = e->attack;
	d["defense"] = e->defense;
	d["lscale"] = e->lscale;
	d["rscale"] = e->rscale;
	d["link_marker"] = e->link_marker;
	d["name"] = e->name;
	d["desc"] = e->desc;
	d["strings"] = e->strings;
	return d;
}

PackedInt32Array YGOCardDatabase::get_codes() const {
	PackedInt32Array out;
	out.resize(static_cast<int64_t>(cards.size()));
	int64_t i = 0;
	for (const auto &kv : cards)
		out.set(i++, static_cast<int32_t>(kv.first));
	return out;
}

int YGOCardDatabase::get_card_count() const {
	return static_cast<int>(cards.size());
}

void YGOCardDatabase::clear() {
	cards.clear();
}
