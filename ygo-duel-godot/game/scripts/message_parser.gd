class_name OCGMessageParser
## Convierte los mensajes binarios de ocgcore (little-endian) en Dictionary.
## Formato tomado de playerop.cpp / operations.cpp / processor.cpp del core de EDOPro.

class Reader:
	var buf: PackedByteArray
	var pos := 0

	func _init(b: PackedByteArray) -> void:
		buf = b

	func u8() -> int:
		var v := buf.decode_u8(pos); pos += 1; return v

	func u16() -> int:
		var v := buf.decode_u16(pos); pos += 2; return v

	func u32() -> int:
		var v := buf.decode_u32(pos); pos += 4; return v

	func s32() -> int:
		var v := buf.decode_s32(pos); pos += 4; return v

	func u64() -> int:
		var v := buf.decode_u64(pos); pos += 8; return v

	func remaining() -> int:
		return buf.size() - pos

	## loc_info: u8 controller, u8 location, u32 sequence, u32 position
	func loc_info() -> Dictionary:
		return {"controller": u8(), "location": u8(), "sequence": u32(), "position": u32()}


static func parse(raw: PackedByteArray) -> Dictionary:
	var r := Reader.new(raw)
	var msg := {"type": r.u8(), "raw": raw}
	match msg.type:
		OCG.MSG_HINT:
			msg.hint_type = r.u8(); msg.player = r.u8(); msg.data = r.u64()
		OCG.MSG_WIN:
			msg.player = r.u8(); msg.reason = r.u8()
		OCG.MSG_SELECT_BATTLECMD:
			msg.player = r.u8()
			msg.activatable = _read_activatable(r, false)
			msg.attackable = []
			for i in r.u32():
				msg.attackable.append({"code": r.u32(), "controller": r.u8(), "location": r.u8(),
						"sequence": r.u8(), "direct": r.u8() != 0, "index": i})
			msg.to_m2 = r.u8() != 0
			msg.to_ep = r.u8() != 0
		OCG.MSG_SELECT_IDLECMD:
			msg.player = r.u8()
			msg.summonable = _read_simple_cards(r, true)
			msg.spsummonable = _read_simple_cards(r, true)
			msg.repositionable = _read_simple_cards(r, false)
			msg.msetable = _read_simple_cards(r, true)
			msg.ssetable = _read_simple_cards(r, true)
			msg.activatable = _read_activatable(r, false)
			msg.to_bp = r.u8() != 0
			msg.to_ep = r.u8() != 0
			msg.can_shuffle = r.u8() != 0
		OCG.MSG_SELECT_EFFECTYN:
			msg.player = r.u8(); msg.code = r.u32(); msg.loc = r.loc_info(); msg.desc = r.u64()
		OCG.MSG_SELECT_YESNO:
			msg.player = r.u8(); msg.desc = r.u64()
		OCG.MSG_SELECT_OPTION:
			msg.player = r.u8()
			msg.options = []
			for i in r.u8():
				msg.options.append(r.u64())
		OCG.MSG_SELECT_CARD:
			msg.player = r.u8(); msg.cancelable = r.u8() != 0
			msg.min = r.u32(); msg.max = r.u32()
			msg.cards = _read_cards_loc(r, false)
		OCG.MSG_SELECT_TRIBUTE:
			msg.player = r.u8(); msg.cancelable = r.u8() != 0
			msg.min = r.u32(); msg.max = r.u32()
			msg.cards = []
			for i in r.u32():
				msg.cards.append({"code": r.u32(), "controller": r.u8(), "location": r.u8(),
						"sequence": r.u32(), "release_param": r.u8(), "index": i})
		OCG.MSG_SELECT_UNSELECT_CARD:
			msg.player = r.u8(); msg.finishable = r.u8() != 0; msg.cancelable = r.u8() != 0
			msg.min = r.u32(); msg.max = r.u32()
			msg.select_cards = _read_cards_loc(r, false)
			msg.unselect_cards = _read_cards_loc(r, false)
		OCG.MSG_SELECT_CHAIN:
			msg.player = r.u8(); msg.spe_count = r.u8(); msg.forced = r.u8() != 0
			msg.hint_timing = r.u32(); msg.other_timing = r.u32()
			msg.chains = _read_activatable(r, true)
		OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD:
			msg.player = r.u8(); msg.count = r.u8(); msg.flag = r.u32()
		OCG.MSG_SELECT_POSITION:
			msg.player = r.u8(); msg.code = r.u32(); msg.positions = r.u8()
		OCG.MSG_SORT_CHAIN, OCG.MSG_SORT_CARD:
			msg.player = r.u8()
			msg.cards = []
			for i in r.u32():
				msg.cards.append({"code": r.u32(), "controller": r.u8(), "location": r.u32(), "sequence": r.u32()})
		OCG.MSG_SELECT_COUNTER:
			msg.player = r.u8(); msg.counter_type = r.u16(); msg.count = r.u16()
			msg.cards = []
			for i in r.u32():
				msg.cards.append({"code": r.u32(), "controller": r.u8(), "location": r.u8(),
						"sequence": r.u8(), "counters": r.u16()})
		OCG.MSG_SELECT_SUM:
			msg.player = r.u8(); msg.select_mode = r.u8(); msg.acc = r.u32()
			msg.min = r.u32(); msg.max = r.u32()
			msg.must_cards = _read_cards_loc(r, true)
			msg.cards = _read_cards_loc(r, true)
		OCG.MSG_CONFIRM_DECKTOP, OCG.MSG_CONFIRM_CARDS:
			msg.player = r.u8()
			msg.cards = []
			for i in r.u32():
				msg.cards.append({"code": r.u32(), "controller": r.u8(), "location": r.u8(), "sequence": r.u32()})
		OCG.MSG_SHUFFLE_DECK:
			msg.player = r.u8()
		OCG.MSG_SHUFFLE_HAND, OCG.MSG_SHUFFLE_EXTRA:
			msg.player = r.u8()
			msg.codes = []
			for i in r.u32():
				msg.codes.append(r.u32())
		OCG.MSG_NEW_TURN:
			msg.player = r.u8()
		OCG.MSG_NEW_PHASE:
			msg.phase = r.u16()
		OCG.MSG_MOVE:
			msg.code = r.u32(); msg.from = r.loc_info(); msg.to = r.loc_info(); msg.reason = r.u32()
		OCG.MSG_POS_CHANGE:
			msg.code = r.u32()
			msg.loc = {"controller": r.u8(), "location": r.u8(), "sequence": r.u8()}
			msg.prev_position = r.u8(); msg.position = r.u8()
		OCG.MSG_SET, OCG.MSG_SUMMONING, OCG.MSG_SPSUMMONING, OCG.MSG_FLIPSUMMONING:
			msg.code = r.u32(); msg.loc = r.loc_info()
		OCG.MSG_SWAP:
			msg.code1 = r.u32(); msg.loc1 = r.loc_info(); msg.code2 = r.u32(); msg.loc2 = r.loc_info()
		OCG.MSG_CHAINING:
			msg.code = r.u32(); msg.loc = r.loc_info()
			msg.triggering_controller = r.u8(); msg.triggering_location = r.u8()
			msg.triggering_sequence = r.u32(); msg.desc = r.u64(); msg.chain_count = r.u32()
		OCG.MSG_CHAINED, OCG.MSG_CHAIN_SOLVING, OCG.MSG_CHAIN_SOLVED, OCG.MSG_CHAIN_NEGATED, OCG.MSG_CHAIN_DISABLED:
			msg.chain_count = r.u8()
		OCG.MSG_DRAW:
			msg.player = r.u8()
			msg.cards = []
			for i in r.u32():
				msg.cards.append({"code": r.u32(), "position": r.u32()})
		OCG.MSG_DAMAGE, OCG.MSG_RECOVER, OCG.MSG_LPUPDATE, OCG.MSG_PAY_LPCOST:
			msg.player = r.u8(); msg.amount = r.u32()
		OCG.MSG_EQUIP, OCG.MSG_CARD_TARGET, OCG.MSG_CANCEL_TARGET:
			msg.loc1 = r.loc_info(); msg.loc2 = r.loc_info()
		OCG.MSG_ATTACK:
			msg.attacker = r.loc_info(); msg.target = r.loc_info()
			msg.direct = int(msg.target.location) == 0
		OCG.MSG_BATTLE:
			msg.attacker = r.loc_info(); msg.atk_attack = r.s32(); msg.atk_defense = r.s32(); msg.atk_destroyed = r.u8()
			msg.target = r.loc_info(); msg.tgt_attack = r.s32(); msg.tgt_defense = r.s32(); msg.tgt_destroyed = r.u8()
		OCG.MSG_TOSS_COIN, OCG.MSG_TOSS_DICE:
			msg.player = r.u8()
			msg.results = []
			for i in r.u8():
				msg.results.append(r.u8())
		OCG.MSG_ROCK_PAPER_SCISSORS:
			msg.player = r.u8()
		OCG.MSG_HAND_RES:
			msg.result = r.u8()
		OCG.MSG_ANNOUNCE_RACE:
			msg.player = r.u8(); msg.count = r.u8(); msg.available = r.u64()
		OCG.MSG_ANNOUNCE_ATTRIB:
			msg.player = r.u8(); msg.count = r.u8(); msg.available = r.u32()
		OCG.MSG_ANNOUNCE_CARD, OCG.MSG_ANNOUNCE_NUMBER:
			msg.player = r.u8()
			msg.options = []
			for i in r.u8():
				msg.options.append(r.u64())
		OCG.MSG_CARD_HINT:
			msg.loc = r.loc_info(); msg.hint_type = r.u8(); msg.data = r.u64()
		OCG.MSG_MISSED_EFFECT:
			msg.loc = r.loc_info(); msg.code = r.u32()
		OCG.MSG_BECOME_TARGET, OCG.MSG_CARD_SELECTED:
			msg.cards = []
			for i in r.u32():
				msg.cards.append(r.loc_info())
	return msg


## Mensajes que esperan una respuesta del jugador indicado en msg.player
static func needs_response(type: int) -> bool:
	return type in [OCG.MSG_SELECT_BATTLECMD, OCG.MSG_SELECT_IDLECMD, OCG.MSG_SELECT_EFFECTYN,
			OCG.MSG_SELECT_YESNO, OCG.MSG_SELECT_OPTION, OCG.MSG_SELECT_CARD, OCG.MSG_SELECT_CHAIN,
			OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_POSITION, OCG.MSG_SELECT_TRIBUTE, OCG.MSG_SORT_CHAIN,
			OCG.MSG_SELECT_COUNTER, OCG.MSG_SELECT_SUM, OCG.MSG_SELECT_DISFIELD, OCG.MSG_SORT_CARD,
			OCG.MSG_SELECT_UNSELECT_CARD, OCG.MSG_ROCK_PAPER_SCISSORS, OCG.MSG_ANNOUNCE_RACE,
			OCG.MSG_ANNOUNCE_ATTRIB, OCG.MSG_ANNOUNCE_CARD, OCG.MSG_ANNOUNCE_NUMBER]


static func _read_simple_cards(r: Reader, seq32: bool) -> Array:
	var out := []
	for i in r.u32():
		out.append({"code": r.u32(), "controller": r.u8(), "location": r.u8(),
				"sequence": r.u32() if seq32 else r.u8(), "index": i})
	return out


## Efectos activables. En SELECT_CHAIN la ubicación es un loc_info completo.
static func _read_activatable(r: Reader, full_loc: bool) -> Array:
	var out := []
	for i in r.u32():
		var e := {"code": r.u32(), "index": i}
		if full_loc:
			var loc := r.loc_info()
			e.merge(loc)
		else:
			e.controller = r.u8(); e.location = r.u8(); e.sequence = r.u32()
		e.desc = r.u64()
		e.client_mode = r.u8()
		out.append(e)
	return out


static func _read_cards_loc(r: Reader, with_param: bool) -> Array:
	var out := []
	for i in r.u32():
		var c := {"code": r.u32(), "index": i}
		c.merge(r.loc_info())
		if with_param:
			c.param = r.u32()
		out.append(c)
	return out
