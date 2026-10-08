#ifndef YGO_CARD_DATABASE_H
#define YGO_CARD_DATABASE_H

#include <cstdint>
#include <unordered_map>
#include <vector>

#include <godot_cpp/classes/ref_counted.hpp>
#include <godot_cpp/variant/dictionary.hpp>
#include <godot_cpp/variant/packed_int32_array.hpp>
#include <godot_cpp/variant/packed_string_array.hpp>
#include <godot_cpp/variant/string.hpp>

namespace godot {

// Base de datos de cartas en memoria, cargada desde ficheros .cdb (SQLite) de EDOPro/ProjectIgnis.
class YGOCardDatabase : public RefCounted {
	GDCLASS(YGOCardDatabase, RefCounted)

public:
	struct Entry {
		uint32_t code = 0;
		uint32_t alias = 0;
		std::vector<uint16_t> setcodes; // terminado en 0, listo para OCG_CardData
		uint32_t type = 0;
		uint32_t level = 0;
		uint32_t attribute = 0;
		uint64_t race = 0;
		int32_t attack = 0;
		int32_t defense = 0;
		uint32_t lscale = 0;
		uint32_t rscale = 0;
		uint32_t link_marker = 0;
		String name;
		String desc;
		PackedStringArray strings;
	};

	YGOCardDatabase() = default;

	int load_cdb(const String &path);
	bool has_card(int code) const;
	Dictionary get_card(int code) const;
	PackedInt32Array get_codes() const;
	int get_card_count() const;
	void clear();

	const Entry *find(uint32_t code) const;

protected:
	static void _bind_methods();

private:
	std::unordered_map<uint32_t, Entry> cards;
};

} // namespace godot

#endif
