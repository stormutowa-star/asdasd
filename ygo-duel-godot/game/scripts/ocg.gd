class_name OCG
## Constantes de ocgcore (ver thirdparty/ygopro-core/ocgapi_constants.h) y textos en español.

# Ubicaciones
const LOCATION_DECK := 0x01
const LOCATION_HAND := 0x02
const LOCATION_MZONE := 0x04
const LOCATION_SZONE := 0x08
const LOCATION_GRAVE := 0x10
const LOCATION_REMOVED := 0x20
const LOCATION_EXTRA := 0x40
const LOCATION_OVERLAY := 0x80
const LOCATION_ONFIELD := 0x0C

# Posiciones
const POS_FACEUP_ATTACK := 0x1
const POS_FACEDOWN_ATTACK := 0x2
const POS_FACEUP_DEFENSE := 0x4
const POS_FACEDOWN_DEFENSE := 0x8
const POS_FACEUP := 0x5
const POS_FACEDOWN := 0xA
const POS_ATTACK := 0x3
const POS_DEFENSE := 0xC

# Tipos de carta
const TYPE_MONSTER := 0x1
const TYPE_SPELL := 0x2
const TYPE_TRAP := 0x4
const TYPE_NORMAL := 0x10
const TYPE_EFFECT := 0x20
const TYPE_FUSION := 0x40
const TYPE_RITUAL := 0x80
const TYPE_TRAPMONSTER := 0x100
const TYPE_SPIRIT := 0x200
const TYPE_UNION := 0x400
const TYPE_GEMINI := 0x800
const TYPE_TUNER := 0x1000
const TYPE_SYNCHRO := 0x2000
const TYPE_TOKEN := 0x4000
const TYPE_QUICKPLAY := 0x10000
const TYPE_CONTINUOUS := 0x20000
const TYPE_EQUIP := 0x40000
const TYPE_FIELD := 0x80000
const TYPE_COUNTER := 0x100000
const TYPE_FLIP := 0x200000
const TYPE_TOON := 0x400000
const TYPE_XYZ := 0x800000
const TYPE_PENDULUM := 0x1000000
const TYPE_SPSUMMON := 0x2000000
const TYPE_LINK := 0x4000000
const TYPES_EXTRA_DECK := TYPE_FUSION | TYPE_SYNCHRO | TYPE_XYZ | TYPE_LINK

# Consultas (OCG_DuelQuery*)
const QUERY_CODE := 0x1
const QUERY_POSITION := 0x2
const QUERY_ALIAS := 0x4
const QUERY_TYPE := 0x8
const QUERY_LEVEL := 0x10
const QUERY_RANK := 0x20
const QUERY_ATTRIBUTE := 0x40
const QUERY_RACE := 0x80
const QUERY_ATTACK := 0x100
const QUERY_DEFENSE := 0x200
const QUERY_BASE_ATTACK := 0x400
const QUERY_BASE_DEFENSE := 0x800
const QUERY_REASON := 0x1000
const QUERY_REASON_CARD := 0x2000
const QUERY_EQUIP_CARD := 0x4000
const QUERY_TARGET_CARD := 0x8000
const QUERY_OVERLAY_CARD := 0x10000
const QUERY_COUNTERS := 0x20000
const QUERY_OWNER := 0x40000
const QUERY_STATUS := 0x80000
const QUERY_IS_PUBLIC := 0x100000
const QUERY_LSCALE := 0x200000
const QUERY_RSCALE := 0x400000
const QUERY_LINK := 0x800000

const QUERY_FIELD_FLAGS := QUERY_CODE | QUERY_POSITION | QUERY_ALIAS | QUERY_TYPE | QUERY_LEVEL \
		| QUERY_RANK | QUERY_ATTRIBUTE | QUERY_RACE | QUERY_ATTACK | QUERY_DEFENSE \
		| QUERY_BASE_ATTACK | QUERY_BASE_DEFENSE | QUERY_EQUIP_CARD | QUERY_OVERLAY_CARD \
		| QUERY_COUNTERS | QUERY_OWNER | QUERY_STATUS | QUERY_LINK | QUERY_LSCALE | QUERY_RSCALE

# Mensajes
const MSG_RETRY := 1
const MSG_HINT := 2
const MSG_WAITING := 3
const MSG_START := 4
const MSG_WIN := 5
const MSG_UPDATE_DATA := 6
const MSG_UPDATE_CARD := 7
const MSG_SELECT_BATTLECMD := 10
const MSG_SELECT_IDLECMD := 11
const MSG_SELECT_EFFECTYN := 12
const MSG_SELECT_YESNO := 13
const MSG_SELECT_OPTION := 14
const MSG_SELECT_CARD := 15
const MSG_SELECT_CHAIN := 16
const MSG_SELECT_PLACE := 18
const MSG_SELECT_POSITION := 19
const MSG_SELECT_TRIBUTE := 20
const MSG_SORT_CHAIN := 21
const MSG_SELECT_COUNTER := 22
const MSG_SELECT_SUM := 23
const MSG_SELECT_DISFIELD := 24
const MSG_SORT_CARD := 25
const MSG_SELECT_UNSELECT_CARD := 26
const MSG_CONFIRM_DECKTOP := 30
const MSG_CONFIRM_CARDS := 31
const MSG_SHUFFLE_DECK := 32
const MSG_SHUFFLE_HAND := 33
const MSG_REFRESH_DECK := 34
const MSG_SWAP_GRAVE_DECK := 35
const MSG_SHUFFLE_SET_CARD := 36
const MSG_REVERSE_DECK := 37
const MSG_DECK_TOP := 38
const MSG_SHUFFLE_EXTRA := 39
const MSG_NEW_TURN := 40
const MSG_NEW_PHASE := 41
const MSG_CONFIRM_EXTRATOP := 42
const MSG_MOVE := 50
const MSG_POS_CHANGE := 53
const MSG_SET := 54
const MSG_SWAP := 55
const MSG_FIELD_DISABLED := 56
const MSG_SUMMONING := 60
const MSG_SUMMONED := 61
const MSG_SPSUMMONING := 62
const MSG_SPSUMMONED := 63
const MSG_FLIPSUMMONING := 64
const MSG_FLIPSUMMONED := 65
const MSG_CHAINING := 70
const MSG_CHAINED := 71
const MSG_CHAIN_SOLVING := 72
const MSG_CHAIN_SOLVED := 73
const MSG_CHAIN_END := 74
const MSG_CHAIN_NEGATED := 75
const MSG_CHAIN_DISABLED := 76
const MSG_CARD_SELECTED := 80
const MSG_RANDOM_SELECTED := 81
const MSG_BECOME_TARGET := 83
const MSG_DRAW := 90
const MSG_DAMAGE := 91
const MSG_RECOVER := 92
const MSG_EQUIP := 93
const MSG_LPUPDATE := 94
const MSG_UNEQUIP := 95
const MSG_CARD_TARGET := 96
const MSG_CANCEL_TARGET := 97
const MSG_PAY_LPCOST := 100
const MSG_ADD_COUNTER := 101
const MSG_REMOVE_COUNTER := 102
const MSG_ATTACK := 110
const MSG_BATTLE := 111
const MSG_ATTACK_DISABLED := 112
const MSG_DAMAGE_STEP_START := 113
const MSG_DAMAGE_STEP_END := 114
const MSG_MISSED_EFFECT := 120
const MSG_BE_CHAIN_TARGET := 121
const MSG_CREATE_RELATION := 122
const MSG_RELEASE_RELATION := 123
const MSG_TOSS_COIN := 130
const MSG_TOSS_DICE := 131
const MSG_ROCK_PAPER_SCISSORS := 132
const MSG_HAND_RES := 133
const MSG_ANNOUNCE_RACE := 140
const MSG_ANNOUNCE_ATTRIB := 141
const MSG_ANNOUNCE_CARD := 142
const MSG_ANNOUNCE_NUMBER := 143
const MSG_CARD_HINT := 160
const MSG_TAG_SWAP := 161
const MSG_RELOAD_FIELD := 162
const MSG_AI_NAME := 163
const MSG_SHOW_HINT := 164
const MSG_PLAYER_HINT := 165
const MSG_MATCH_KILL := 170
const MSG_CUSTOM_MSG := 180
const MSG_REMOVE_CARDS := 190

# Pistas
const HINT_EVENT := 1
const HINT_MESSAGE := 2
const HINT_SELECTMSG := 3
const HINT_OPSELECTED := 4
const HINT_EFFECT := 5
const HINT_RACE := 6
const HINT_ATTRIB := 7
const HINT_CODE := 8
const HINT_NUMBER := 9
const HINT_CARD := 10
const HINT_ZONE := 11

# Fases
const PHASE_DRAW := 0x01
const PHASE_STANDBY := 0x02
const PHASE_MAIN1 := 0x04
const PHASE_BATTLE_START := 0x08
const PHASE_BATTLE_STEP := 0x10
const PHASE_DAMAGE := 0x20
const PHASE_DAMAGE_CAL := 0x40
const PHASE_BATTLE := 0x80
const PHASE_MAIN2 := 0x100
const PHASE_END := 0x200

# Opciones de duelo (Master Rule 5, la de EDOPro por defecto)
const DUEL_PZONE := 0x800
const DUEL_EMZONE := 0x2000
const DUEL_FSX_MMZONE := 0x4000
const DUEL_TRAP_MONSTERS_NOT_USE_ZONE := 0x8000
const DUEL_TRIGGER_ONLY_IN_LOCATION := 0x20000
const DUEL_MODE_MR5 := DUEL_PZONE | DUEL_EMZONE | DUEL_FSX_MMZONE | DUEL_TRAP_MONSTERS_NOT_USE_ZONE | DUEL_TRIGGER_ONLY_IN_LOCATION

# Estados de OCG_DuelProcess
const STATUS_END := 0
const STATUS_AWAITING := 1
const STATUS_CONTINUE := 2

const ATTRIBUTES := {0x01: "TIERRA", 0x02: "AGUA", 0x04: "FUEGO", 0x08: "VIENTO", 0x10: "LUZ", 0x20: "OSCURIDAD", 0x40: "DIVINIDAD"}
const RACES := {
	0x1: "Guerrero", 0x2: "Lanzador de Conjuros", 0x4: "Hada", 0x8: "Demonio", 0x10: "Zombi",
	0x20: "Máquina", 0x40: "Aqua", 0x80: "Piro", 0x100: "Roca", 0x200: "Bestia Alada",
	0x400: "Planta", 0x800: "Insecto", 0x1000: "Trueno", 0x2000: "Dragón", 0x4000: "Bestia",
	0x8000: "Guerrero-Bestia", 0x10000: "Dinosaurio", 0x20000: "Pez", 0x40000: "Serpiente Marina",
	0x80000: "Reptil", 0x100000: "Psíquico", 0x200000: "Bestia Divina", 0x400000: "Dios Creador",
	0x800000: "Wyrm", 0x1000000: "Ciberso", 0x2000000: "Ilusión",
}
const PHASE_NAMES := {
	PHASE_DRAW: "Robo", PHASE_STANDBY: "Espera", PHASE_MAIN1: "Principal 1",
	PHASE_BATTLE_START: "Batalla", PHASE_BATTLE_STEP: "Batalla", PHASE_DAMAGE: "Batalla",
	PHASE_DAMAGE_CAL: "Batalla", PHASE_BATTLE: "Batalla", PHASE_MAIN2: "Principal 2", PHASE_END: "Final",
}
const LOCATION_NAMES := {
	LOCATION_DECK: "Deck", LOCATION_HAND: "Mano", LOCATION_MZONE: "Zona de Monstruos",
	LOCATION_SZONE: "Zona de Magias/Trampas", LOCATION_GRAVE: "Cementerio",
	LOCATION_REMOVED: "Desterradas", LOCATION_EXTRA: "Extra Deck", LOCATION_OVERLAY: "Material Xyz",
}
const WIN_REASONS := {
	0x0: "LP a 0", 0x1: "Sin cartas para robar", 0x2: "Tiempo", 0x3: "Conexión",
	0x4: "Rendición", 0x10: "Exodia", 0x11: "Puerta del Destino", 0x12: "Último Turno",
}


static func attribute_name(attr: int) -> String:
	return ATTRIBUTES.get(attr, "?")


static func race_name(race: int) -> String:
	for k in RACES:
		if race & k:
			return RACES[k]
	return "?"


static func type_line(type: int) -> String:
	if type & TYPE_SPELL:
		var sub := ""
		if type & TYPE_QUICKPLAY: sub = "de Juego Rápido "
		elif type & TYPE_CONTINUOUS: sub = "Continua "
		elif type & TYPE_EQUIP: sub = "de Equipo "
		elif type & TYPE_FIELD: sub = "de Campo "
		elif type & TYPE_RITUAL: sub = "de Ritual "
		return "Carta Mágica " + sub.strip_edges()
	if type & TYPE_TRAP:
		var sub := ""
		if type & TYPE_CONTINUOUS: sub = "Continua"
		elif type & TYPE_COUNTER: sub = "de Contraefecto"
		return ("Carta de Trampa " + sub).strip_edges()
	var parts: PackedStringArray = []
	for pair in [[TYPE_FUSION, "Fusión"], [TYPE_RITUAL, "Ritual"], [TYPE_SYNCHRO, "Sincronía"],
			[TYPE_XYZ, "Xyz"], [TYPE_LINK, "Link"], [TYPE_PENDULUM, "Péndulo"], [TYPE_TUNER, "Cantante"],
			[TYPE_FLIP, "Volteo"], [TYPE_TOON, "Toon"], [TYPE_SPIRIT, "Espíritu"], [TYPE_UNION, "Unión"],
			[TYPE_GEMINI, "Géminis"], [TYPE_TOKEN, "Ficha"]]:
		if type & pair[0]:
			parts.append(pair[1])
	if type & TYPE_EFFECT:
		parts.append("Efecto")
	elif type & TYPE_NORMAL:
		parts.append("Normal")
	return "Monstruo " + "/".join(parts)


static func location_name(loc: int) -> String:
	return LOCATION_NAMES.get(loc & ~LOCATION_OVERLAY, "?")


static func is_extra_deck_type(type: int) -> bool:
	return (type & TYPES_EXTRA_DECK) != 0
