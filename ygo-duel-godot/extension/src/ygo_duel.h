#ifndef YGO_DUEL_H
#define YGO_DUEL_H

#include <string>
#include <unordered_map>
#include <vector>

#include <godot_cpp/classes/ref_counted.hpp>
#include <godot_cpp/variant/array.hpp>
#include <godot_cpp/variant/dictionary.hpp>
#include <godot_cpp/variant/packed_byte_array.hpp>
#include <godot_cpp/variant/string.hpp>

#include "ocgapi.h"
#include "ygo_card_database.h"

namespace godot {

// Envoltorio fino sobre la API C de ocgcore (EDOPro). Toda la lógica del duelo
// (parseo de mensajes, respuestas, interfaz e IA) vive en GDScript.
class YGODuel : public RefCounted {
	GDCLASS(YGODuel, RefCounted)

public:
	YGODuel() = default;
	~YGODuel();

	void set_database(const Ref<YGOCardDatabase> &p_db);
	Ref<YGOCardDatabase> get_database() const;
	int add_script_directory(const String &path);

	int create_duel(int64_t seed, int64_t flags, int starting_lp, int starting_hand, int draw_per_turn);
	bool load_script(const String &name);
	void new_card(int team, int duelist, int code, int controller, int location, int sequence, int position);
	void start_duel();
	int process();
	Array get_messages();
	void set_response(const PackedByteArray &response);
	int query_count(int player, int location);
	Variant query_card(int player, int location, int sequence, int flags);
	Array query_location(int player, int location, int flags);
	PackedByteArray query_field_raw();
	bool is_active() const;
	void end_duel();
	static Array get_core_version();

	// Callbacks de ocgcore
	void on_read_card(uint32_t code, OCG_CardData *data);
	int on_read_script(OCG_Duel duel, const char *name);
	void on_log(const char *text, int type);

protected:
	static void _bind_methods();

private:
	void index_directory(const String &path, int depth);
	static Dictionary parse_card_query(const uint8_t *&ptr, const uint8_t *end, bool &empty);

	OCG_Duel duel = nullptr;
	Ref<YGOCardDatabase> db;
	std::unordered_map<std::string, String> script_index;
	std::vector<uint16_t> empty_setcodes{ 0 };
};

} // namespace godot

#endif
