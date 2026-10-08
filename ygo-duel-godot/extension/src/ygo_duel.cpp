#include "ygo_duel.h"

#include <algorithm>
#include <cstring>

#include <godot_cpp/classes/dir_access.hpp>
#include <godot_cpp/classes/file_access.hpp>
#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/variant/packed_string_array.hpp>
#include <godot_cpp/variant/utility_functions.hpp>

#include "ocgapi_constants.h"

using namespace godot;

namespace {

void cb_card_reader(void *payload, uint32_t code, OCG_CardData *data) {
	static_cast<YGODuel *>(payload)->on_read_card(code, data);
}

int cb_script_reader(void *payload, OCG_Duel duel, const char *name) {
	return static_cast<YGODuel *>(payload)->on_read_script(duel, name);
}

void cb_log(void *payload, const char *text, int type) {
	static_cast<YGODuel *>(payload)->on_log(text, type);
}

uint64_t splitmix64(uint64_t &state) {
	uint64_t z = (state += 0x9e3779b97f4a7c15ULL);
	z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9ULL;
	z = (z ^ (z >> 27)) * 0x94d049bb133111ebULL;
	return z ^ (z >> 31);
}

template <typename T>
T read_le(const uint8_t *&ptr) {
	T v;
	std::memcpy(&v, ptr, sizeof(T));
	ptr += sizeof(T);
	return v;
}

Dictionary read_loc_info(const uint8_t *&ptr) {
	Dictionary d;
	d["controller"] = read_le<uint8_t>(ptr);
	d["location"] = read_le<uint8_t>(ptr);
	d["sequence"] = read_le<uint32_t>(ptr);
	d["position"] = read_le<uint32_t>(ptr);
	return d;
}

} // namespace

YGODuel::~YGODuel() {
	end_duel();
}

void YGODuel::_bind_methods() {
	ClassDB::bind_method(D_METHOD("set_database", "db"), &YGODuel::set_database);
	ClassDB::bind_method(D_METHOD("get_database"), &YGODuel::get_database);
	ClassDB::bind_method(D_METHOD("add_script_directory", "path"), &YGODuel::add_script_directory);
	ClassDB::bind_method(D_METHOD("create_duel", "seed", "flags", "starting_lp", "starting_hand", "draw_per_turn"), &YGODuel::create_duel);
	ClassDB::bind_method(D_METHOD("load_script", "name"), &YGODuel::load_script);
	ClassDB::bind_method(D_METHOD("new_card", "team", "duelist", "code", "controller", "location", "sequence", "position"), &YGODuel::new_card);
	ClassDB::bind_method(D_METHOD("start_duel"), &YGODuel::start_duel);
	ClassDB::bind_method(D_METHOD("process"), &YGODuel::process);
	ClassDB::bind_method(D_METHOD("get_messages"), &YGODuel::get_messages);
	ClassDB::bind_method(D_METHOD("set_response", "response"), &YGODuel::set_response);
	ClassDB::bind_method(D_METHOD("query_count", "player", "location"), &YGODuel::query_count);
	ClassDB::bind_method(D_METHOD("query_card", "player", "location", "sequence", "flags"), &YGODuel::query_card);
	ClassDB::bind_method(D_METHOD("query_location", "player", "location", "flags"), &YGODuel::query_location);
	ClassDB::bind_method(D_METHOD("query_field_raw"), &YGODuel::query_field_raw);
	ClassDB::bind_method(D_METHOD("is_active"), &YGODuel::is_active);
	ClassDB::bind_method(D_METHOD("end_duel"), &YGODuel::end_duel);
	ClassDB::bind_static_method("YGODuel", D_METHOD("get_core_version"), &YGODuel::get_core_version);

	ADD_SIGNAL(MethodInfo("core_log", PropertyInfo(Variant::STRING, "message"), PropertyInfo(Variant::INT, "type")));

	BIND_CONSTANT(OCG_DUEL_STATUS_END);
	BIND_CONSTANT(OCG_DUEL_STATUS_AWAITING);
	BIND_CONSTANT(OCG_DUEL_STATUS_CONTINUE);
}

void YGODuel::set_database(const Ref<YGOCardDatabase> &p_db) {
	db = p_db;
}

Ref<YGOCardDatabase> YGODuel::get_database() const {
	return db;
}

// Indexa recursivamente los .lua de un directorio (nombre de fichero -> ruta).
// Dentro de una llamada gana el primero encontrado (raíz, luego "official", luego el resto);
// un directorio añadido después sobrescribe a los anteriores (útil para expansiones).
int YGODuel::add_script_directory(const String &path) {
	const size_t before = script_index.size();
	std::unordered_map<std::string, String> saved;
	saved.swap(script_index);
	index_directory(path, 0);
	const int found = static_cast<int>(script_index.size());
	for (auto &kv : saved)
		script_index.emplace(kv.first, kv.second); // lo nuevo tiene prioridad
	(void)before;
	return found;
}

void YGODuel::index_directory(const String &path, int depth) {
	if (depth > 4)
		return;
	String base = path.ends_with("/") ? path : path + String("/");
	PackedStringArray files = DirAccess::get_files_at(base);
	for (int64_t i = 0; i < files.size(); ++i) {
		String f = files[i];
		// En exportaciones los ficheros pueden aparecer con sufijo .remap/.import; sólo nos interesan los .lua
		if (!f.ends_with(".lua"))
			continue;
		std::string key = f.utf8().get_data();
		script_index.emplace(key, base + f);
	}
	PackedStringArray dirs = DirAccess::get_directories_at(base);
	std::vector<String> ordered;
	for (int64_t i = 0; i < dirs.size(); ++i)
		ordered.push_back(dirs[i]);
	std::stable_sort(ordered.begin(), ordered.end(), [](const String &a, const String &b) {
		return (a == "official") > (b == "official");
	});
	for (const String &d : ordered) {
		if (d.begins_with("."))
			continue;
		index_directory(base + d, depth + 1);
	}
}

int YGODuel::create_duel(int64_t seed, int64_t flags, int starting_lp, int starting_hand, int draw_per_turn) {
	end_duel();
	OCG_DuelOptions opts{};
	uint64_t state = static_cast<uint64_t>(seed);
	for (auto &s : opts.seed)
		s = splitmix64(state);
	opts.flags = static_cast<uint64_t>(flags);
	opts.team1 = { static_cast<uint32_t>(starting_lp), static_cast<uint32_t>(starting_hand), static_cast<uint32_t>(draw_per_turn) };
	opts.team2 = opts.team1;
	opts.cardReader = &cb_card_reader;
	opts.payload1 = this;
	opts.scriptReader = &cb_script_reader;
	opts.payload2 = this;
	opts.logHandler = &cb_log;
	opts.payload3 = this;
	opts.cardReaderDone = nullptr;
	opts.payload4 = nullptr;
	opts.enableUnsafeLibraries = 0;
	int res = OCG_CreateDuel(&duel, &opts);
	if (res != OCG_DUEL_CREATION_SUCCESS) {
		duel = nullptr;
		return res;
	}
	// Igual que EDOPro: los scripts base se cargan a mano, el resto lo pide el core.
	if (!load_script("constant.lua") || !load_script("utility.lua")) {
		UtilityFunctions::push_error("YGODuel: no se encontraron constant.lua / utility.lua en los directorios de scripts");
	}
	return res;
}

bool YGODuel::load_script(const String &name) {
	if (duel == nullptr)
		return false;
	return on_read_script(duel, name.utf8().get_data()) != 0;
}

void YGODuel::new_card(int team, int duelist, int code, int controller, int location, int sequence, int position) {
	if (duel == nullptr)
		return;
	OCG_NewCardInfo info{};
	info.team = static_cast<uint8_t>(team);
	info.duelist = static_cast<uint8_t>(duelist);
	info.code = static_cast<uint32_t>(code);
	info.con = static_cast<uint8_t>(controller);
	info.loc = static_cast<uint32_t>(location);
	info.seq = static_cast<uint32_t>(sequence);
	info.pos = static_cast<uint32_t>(position);
	OCG_DuelNewCard(duel, &info);
}

void YGODuel::start_duel() {
	if (duel != nullptr)
		OCG_StartDuel(duel);
}

int YGODuel::process() {
	if (duel == nullptr)
		return OCG_DUEL_STATUS_END;
	return OCG_DuelProcess(duel);
}

// Separa el buffer del core ([u32 tamaño][mensaje]...) en un Array de PackedByteArray.
Array YGODuel::get_messages() {
	Array out;
	if (duel == nullptr)
		return out;
	uint32_t length = 0;
	auto *buf = static_cast<const uint8_t *>(OCG_DuelGetMessage(duel, &length));
	const uint8_t *ptr = buf;
	const uint8_t *end = buf + length;
	while (ptr + sizeof(uint32_t) <= end) {
		uint32_t size = read_le<uint32_t>(ptr);
		if (ptr + size > end)
			break;
		PackedByteArray msg;
		msg.resize(size);
		if (size > 0)
			std::memcpy(msg.ptrw(), ptr, size);
		out.push_back(msg);
		ptr += size;
	}
	return out;
}

void YGODuel::set_response(const PackedByteArray &response) {
	if (duel != nullptr)
		OCG_DuelSetResponse(duel, response.ptr(), static_cast<uint32_t>(response.size()));
}

int YGODuel::query_count(int player, int location) {
	if (duel == nullptr)
		return 0;
	return static_cast<int>(OCG_DuelQueryCount(duel, static_cast<uint8_t>(player), static_cast<uint32_t>(location)));
}

// Formato de cada carta: secuencia de [u16 tamaño][u32 flag][datos] hasta QUERY_END. Hueco vacío = u16 0.
Dictionary YGODuel::parse_card_query(const uint8_t *&ptr, const uint8_t *end, bool &empty) {
	Dictionary d;
	empty = false;
	if (ptr + 2 > end) {
		empty = true;
		return d;
	}
	uint16_t first = read_le<uint16_t>(ptr);
	if (first == 0) {
		empty = true;
		return d;
	}
	uint16_t size = first;
	for (;;) {
		if (ptr + size > end || size < 4)
			break;
		const uint8_t *data = ptr;
		const uint8_t *next = ptr + size;
		uint32_t flag = read_le<uint32_t>(data);
		if (flag == QUERY_END) {
			ptr = next;
			break;
		}
		switch (flag) {
			case QUERY_CODE: d["code"] = read_le<uint32_t>(data); break;
			case QUERY_POSITION: d["position"] = read_le<uint32_t>(data); break;
			case QUERY_ALIAS: d["alias"] = read_le<uint32_t>(data); break;
			case QUERY_TYPE: d["type"] = read_le<uint32_t>(data); break;
			case QUERY_LEVEL: d["level"] = read_le<uint32_t>(data); break;
			case QUERY_RANK: d["rank"] = read_le<uint32_t>(data); break;
			case QUERY_ATTRIBUTE: d["attribute"] = read_le<uint32_t>(data); break;
			case QUERY_RACE: d["race"] = static_cast<int64_t>(read_le<uint64_t>(data)); break;
			case QUERY_ATTACK: d["attack"] = read_le<int32_t>(data); break;
			case QUERY_DEFENSE: d["defense"] = read_le<int32_t>(data); break;
			case QUERY_BASE_ATTACK: d["base_attack"] = read_le<int32_t>(data); break;
			case QUERY_BASE_DEFENSE: d["base_defense"] = read_le<int32_t>(data); break;
			case QUERY_REASON: d["reason"] = read_le<uint32_t>(data); break;
			case QUERY_COVER: d["cover"] = read_le<uint32_t>(data); break;
			case QUERY_OWNER: d["owner"] = read_le<uint8_t>(data); break;
			case QUERY_STATUS: d["status"] = read_le<uint32_t>(data); break;
			case QUERY_IS_PUBLIC: d["is_public"] = read_le<uint8_t>(data) != 0; break;
			case QUERY_LSCALE: d["lscale"] = read_le<uint32_t>(data); break;
			case QUERY_RSCALE: d["rscale"] = read_le<uint32_t>(data); break;
			case QUERY_IS_HIDDEN: d["is_hidden"] = read_le<uint8_t>(data) != 0; break;
			case QUERY_LINK:
				d["link"] = read_le<uint32_t>(data);
				d["link_marker"] = read_le<uint32_t>(data);
				break;
			case QUERY_REASON_CARD:
			case QUERY_EQUIP_CARD: {
				if (size >= 4 + 10) {
					Dictionary loc = read_loc_info(data);
					if (int(loc["location"]) != 0)
						d[flag == QUERY_EQUIP_CARD ? "equip_target" : "reason_card"] = loc;
				}
				break;
			}
			case QUERY_TARGET_CARD: {
				uint32_t n = read_le<uint32_t>(data);
				Array targets;
				for (uint32_t i = 0; i < n && data + 10 <= next; ++i)
					targets.push_back(read_loc_info(data));
				d["targets"] = targets;
				break;
			}
			case QUERY_OVERLAY_CARD: {
				uint32_t n = read_le<uint32_t>(data);
				Array overlay;
				for (uint32_t i = 0; i < n && data + 4 <= next; ++i)
					overlay.push_back(read_le<uint32_t>(data));
				d["overlay"] = overlay;
				break;
			}
			case QUERY_COUNTERS: {
				uint32_t n = read_le<uint32_t>(data);
				Dictionary counters;
				for (uint32_t i = 0; i < n && data + 4 <= next; ++i) {
					uint32_t v = read_le<uint32_t>(data);
					counters[v & 0xffff] = v >> 16;
				}
				d["counters"] = counters;
				break;
			}
			default: break;
		}
		ptr = next;
		if (ptr + 2 > end)
			break;
		size = read_le<uint16_t>(ptr);
	}
	return d;
}

Variant YGODuel::query_card(int player, int location, int sequence, int flags) {
	if (duel == nullptr)
		return Variant();
	OCG_QueryInfo info{};
	info.flags = static_cast<uint32_t>(flags);
	info.con = static_cast<uint8_t>(player);
	info.loc = static_cast<uint32_t>(location);
	info.seq = static_cast<uint32_t>(sequence);
	uint32_t length = 0;
	auto *buf = static_cast<const uint8_t *>(OCG_DuelQuery(duel, &length, &info));
	if (buf == nullptr || length == 0)
		return Variant();
	const uint8_t *ptr = buf;
	bool empty = false;
	Dictionary d = parse_card_query(ptr, buf + length, empty);
	if (empty)
		return Variant();
	return d;
}

// Devuelve un Array con un Dictionary por carta (o null para zonas vacías en MZONE/SZONE).
Array YGODuel::query_location(int player, int location, int flags) {
	Array out;
	if (duel == nullptr)
		return out;
	OCG_QueryInfo info{};
	info.flags = static_cast<uint32_t>(flags);
	info.con = static_cast<uint8_t>(player);
	info.loc = static_cast<uint32_t>(location);
	uint32_t length = 0;
	auto *buf = static_cast<const uint8_t *>(OCG_DuelQueryLocation(duel, &length, &info));
	if (buf == nullptr || length < 4)
		return out;
	const uint8_t *ptr = buf;
	const uint8_t *end = buf + length;
	uint32_t total = read_le<uint32_t>(ptr);
	if (buf + 4 + total < end)
		end = buf + 4 + total;
	while (ptr + 2 <= end) {
		bool empty = false;
		Dictionary d = parse_card_query(ptr, end, empty);
		if (empty)
			out.push_back(Variant());
		else
			out.push_back(d);
	}
	return out;
}

PackedByteArray YGODuel::query_field_raw() {
	PackedByteArray out;
	if (duel == nullptr)
		return out;
	uint32_t length = 0;
	auto *buf = static_cast<const uint8_t *>(OCG_DuelQueryField(duel, &length));
	out.resize(length);
	if (length > 0)
		std::memcpy(out.ptrw(), buf, length);
	return out;
}

bool YGODuel::is_active() const {
	return duel != nullptr;
}

void YGODuel::end_duel() {
	if (duel != nullptr) {
		OCG_DestroyDuel(duel);
		duel = nullptr;
	}
}

Array YGODuel::get_core_version() {
	int major = 0, minor = 0;
	OCG_GetVersion(&major, &minor);
	Array a;
	a.push_back(major);
	a.push_back(minor);
	return a;
}

void YGODuel::on_read_card(uint32_t code, OCG_CardData *data) {
	std::memset(data, 0, sizeof(OCG_CardData));
	data->code = code;
	data->setcodes = empty_setcodes.data();
	if (db.is_null())
		return;
	const YGOCardDatabase::Entry *e = db->find(code);
	if (e == nullptr)
		return;
	data->alias = e->alias;
	// El core copia los setcodes durante la llamada, así que basta con apuntar a los de la base de datos.
	data->setcodes = const_cast<uint16_t *>(e->setcodes.data());
	data->type = e->type;
	data->level = e->level;
	data->attribute = e->attribute;
	data->race = e->race;
	data->attack = e->attack;
	data->defense = e->defense;
	data->lscale = e->lscale;
	data->rscale = e->rscale;
	data->link_marker = e->link_marker;
}

int YGODuel::on_read_script(OCG_Duel p_duel, const char *name) {
	std::string key(name);
	const auto slash = key.find_last_of("/\\");
	if (slash != std::string::npos)
		key = key.substr(slash + 1);
	auto it = script_index.find(key);
	if (it == script_index.end())
		return 0; // p.ej. monstruos normales: no tienen script
	PackedByteArray bytes = FileAccess::get_file_as_bytes(it->second);
	if (bytes.size() == 0)
		return 0;
	return OCG_LoadScript(p_duel, reinterpret_cast<const char *>(bytes.ptr()), static_cast<uint32_t>(bytes.size()), name);
}

void YGODuel::on_log(const char *text, int type) {
	emit_signal("core_log", String::utf8(text), type);
}
