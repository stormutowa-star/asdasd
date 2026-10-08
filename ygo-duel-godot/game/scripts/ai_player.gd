extends RefCounted
## IA sencilla basada en heurísticas (no pretende ser WindBot): invoca el monstruo más fuerte,
## activa magias útiles, coloca trampas y ataca cuando la batalla es favorable.

var player := 1
var controller # DuelController
var rng := RandomNumberGenerator.new()

var _turn_seen := -1
var _activations := {}
var _planned_target := {}
var _announce_try := 0

# Cartas que sólo conviene activar en ciertas condiciones
const BOARD_WIPES := [53129443] # Dark Hole: destruye también lo propio
const BACKROW_WIPES := [19613556] # Heavy Storm


func decide(msg: Dictionary) -> PackedByteArray:
	if controller.turn != _turn_seen:
		_turn_seen = controller.turn
		_activations.clear()
	match msg.type:
		OCG.MSG_SELECT_IDLECMD: return _idle(msg)
		OCG.MSG_SELECT_BATTLECMD: return _battle(msg)
		OCG.MSG_SELECT_CHAIN: return _chain(msg)
		OCG.MSG_SELECT_EFFECTYN, OCG.MSG_SELECT_YESNO: return OCGResponse.int32(1)
		OCG.MSG_SELECT_OPTION: return OCGResponse.int32(0)
		OCG.MSG_SELECT_CARD: return _select_card(msg)
		OCG.MSG_SELECT_TRIBUTE: return _tribute(msg)
		OCG.MSG_SELECT_UNSELECT_CARD: return _unselect(msg)
		OCG.MSG_SELECT_POSITION: return _position(msg)
		OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD: return _place(msg, 0)
		OCG.MSG_SELECT_SUM: return _sum(msg)
		OCG.MSG_SELECT_COUNTER: return _counter(msg)
		OCG.MSG_SORT_CARD, OCG.MSG_SORT_CHAIN: return OCGResponse.sort([])
		OCG.MSG_ANNOUNCE_RACE: return OCGResponse.uint64(_pick_bits(msg.available, msg.count))
		OCG.MSG_ANNOUNCE_ATTRIB: return OCGResponse.int32(_pick_bits(msg.available, msg.count))
		OCG.MSG_ANNOUNCE_NUMBER: return OCGResponse.int32(0)
		OCG.MSG_ANNOUNCE_CARD: return _announce_card()
		OCG.MSG_ROCK_PAPER_SCISSORS: return OCGResponse.int32(rng.randi_range(1, 3))
	return OCGResponse.int32(0)


## Respuesta alternativa cuando el core rechaza la anterior (MSG_RETRY).
func fallback(msg: Dictionary, attempt: int) -> PackedByteArray:
	match msg.type:
		OCG.MSG_SELECT_IDLECMD:
			if msg.to_ep: return OCGResponse.command(7)
			if msg.to_bp: return OCGResponse.command(6)
		OCG.MSG_SELECT_BATTLECMD:
			if msg.to_m2: return OCGResponse.command(2)
			if msg.to_ep: return OCGResponse.command(3)
		OCG.MSG_SELECT_CHAIN:
			if not msg.forced: return OCGResponse.cancel()
			return OCGResponse.int32(min(attempt, msg.chains.size() - 1))
		OCG.MSG_SELECT_CARD:
			if msg.cancelable and attempt > 1: return OCGResponse.cancel()
			var idx := []
			for i in max(msg.min, 1):
				idx.append((i + attempt) % msg.cards.size())
			return OCGResponse.cards(idx)
		OCG.MSG_SELECT_PLACE, OCG.MSG_SELECT_DISFIELD:
			return _place(msg, attempt)
		OCG.MSG_SELECT_UNSELECT_CARD:
			var total: int = msg.select_cards.size() + msg.unselect_cards.size()
			if (msg.finishable or msg.cancelable) and attempt > 1: return OCGResponse.cancel()
			return OCGResponse.select_unselect(attempt % max(total, 1))
		OCG.MSG_ANNOUNCE_CARD:
			return _announce_card()
		OCG.MSG_ANNOUNCE_NUMBER:
			return OCGResponse.int32(attempt % max(msg.options.size(), 1))
		OCG.MSG_SELECT_OPTION:
			return OCGResponse.int32(attempt % max(msg.options.size(), 1))
	return decide(msg)


# ---------------------------------------------------------------- utilidades

func _opp() -> int:
	return 1 - player


func _side(p: int) -> Dictionary:
	return controller.field[p]


func _monsters(p: int) -> Array:
	return _side(p).mzone.filter(func(c): return c != null)


func _info(entry: Dictionary) -> Dictionary:
	var q = controller.get_card_at(entry.get("controller", 0), entry.get("location", 0), entry.get("sequence", 0))
	if q is Dictionary and not q.is_empty() and (q.get("code", 0) == entry.get("code", 0) or entry.get("code", 0) == 0):
		return q
	var d := CardDB.get_card(entry.get("code", 0))
	return {"code": d.get("code", 0), "type": d.get("type", 0), "attack": d.get("attack", 0),
			"defense": d.get("defense", 0), "level": d.get("level", 0), "position": entry.get("position", 0)}


func _value(entry: Dictionary) -> int:
	var c := _info(entry)
	var type: int = c.get("type", 0)
	if type & OCG.TYPE_MONSTER:
		if entry.get("location", 0) == OCG.LOCATION_MZONE and int(c.get("position", 0)) & OCG.POS_FACEDOWN and entry.get("controller", 0) != player:
			return 1500
		return max(int(c.get("attack", 0)), int(c.get("defense", 0)) / 2)
	return 1000


func _key(e: Dictionary) -> String:
	return "%d:%d:%d:%d" % [e.code, e.location, e.sequence, e.get("desc", 0)]


func _pick_bits(available: int, count: int) -> int:
	var out := 0
	var n := 0
	for i in 64:
		if n >= count: break
		if (available >> i) & 1:
			out |= 1 << i
			n += 1
	return out


# ---------------------------------------------------------------- fase principal

func _idle(msg: Dictionary) -> PackedByteArray:
	var my_mons := _monsters(player)
	var opp_mons := _monsters(_opp())
	# 1) Activar magias / efectos (no trampas: esas se guardan para responder)
	for e in msg.activatable:
		var c := CardDB.get_card(e.code)
		var type: int = c.get("type", 0)
		if type & OCG.TYPE_TRAP:
			continue
		if _activations.get(_key(e), 0) >= 1:
			continue
		if e.code in BOARD_WIPES and opp_mons.size() <= my_mons.size():
			continue
		if e.code in BACKROW_WIPES and _count_backrow(_opp()) <= _count_backrow(player):
			continue
		_activations[_key(e)] = _activations.get(_key(e), 0) + 1
		return OCGResponse.command(5, e.index)
	# 2) Invocaciones especiales (procedimientos tipo Valkyrion / Fusión desde el campo)
	if not msg.spsummonable.is_empty():
		var best = _best_by_attack(msg.spsummonable)
		if _activations.get("sp" + _key(best), 0) < 1:
			_activations["sp" + _key(best)] = 1
			return OCGResponse.command(1, best.index)
	# 3) Invocación Normal del monstruo más fuerte (o colocarlo si es defensivo)
	if not msg.summonable.is_empty() or not msg.msetable.is_empty():
		var best = _best_by_attack(msg.summonable) if not msg.summonable.is_empty() else null
		var best_set = _best_by_defense(msg.msetable) if not msg.msetable.is_empty() else null
		if best != null:
			var c := CardDB.get_card(best.code)
			var weak := int(c.get("attack", 0)) < 1200 and int(c.get("defense", 0)) > int(c.get("attack", 0))
			if weak and best_set != null and _strongest_atk(opp_mons) > int(c.get("attack", 0)):
				return OCGResponse.command(3, best_set.index)
			return OCGResponse.command(0, best.index)
		if best_set != null:
			return OCGResponse.command(3, best_set.index)
	# 4) Colocar trampas y magias rápidas
	for e in msg.ssetable:
		var type: int = CardDB.get_card(e.code).get("type", 0)
		if type & OCG.TYPE_TRAP or type & OCG.TYPE_QUICKPLAY:
			return OCGResponse.command(4, e.index)
	# 5) Voltear monstruos boca abajo fuertes en la Main 1
	for e in msg.repositionable:
		var q := _info(e)
		if int(q.get("position", 0)) & OCG.POS_FACEDOWN and int(q.get("attack", 0)) >= 1500 and _activations.get("pos" + _key(e), 0) < 1:
			_activations["pos" + _key(e)] = 1
			return OCGResponse.command(2, e.index)
	# 6) Pasar a batalla si hay con qué atacar
	if msg.to_bp and _monsters(player).any(func(m): return int(m.get("position", 0)) == OCG.POS_FACEUP_ATTACK):
		return OCGResponse.command(6)
	if msg.to_ep:
		return OCGResponse.command(7)
	return OCGResponse.command(6)


func _count_backrow(p: int) -> int:
	var n := 0
	for i in min(5, _side(p).szone.size()):
		if _side(p).szone[i] != null:
			n += 1
	return n


func _best_by_attack(list: Array):
	var best = null
	var best_atk := -1
	for e in list:
		var atk: int = CardDB.get_card(e.code).get("attack", 0)
		if atk > best_atk:
			best_atk = atk
			best = e
	return best


func _best_by_defense(list: Array):
	var best = null
	var best_def := -1
	for e in list:
		var d: int = CardDB.get_card(e.code).get("defense", 0)
		if d > best_def:
			best_def = d
			best = e
	return best


func _strongest_atk(mons: Array) -> int:
	var m := 0
	for c in mons:
		if int(c.get("position", 0)) & OCG.POS_FACEUP:
			m = max(m, int(c.get("attack", 0)))
	return m


# ---------------------------------------------------------------- batalla

func _battle(msg: Dictionary) -> PackedByteArray:
	var opp_mons := []
	var mz: Array = _side(_opp()).mzone
	for i in mz.size():
		if mz[i] != null:
			opp_mons.append({"seq": i, "card": mz[i]})
	var best_choice := {}
	var best_gain := -1
	for a in msg.attackable:
		var q = controller.get_card_at(a.controller, a.location, a.sequence)
		if q == null: continue
		var atk: int = q.get("attack", 0)
		if opp_mons.is_empty() or a.direct:
			if atk > best_gain:
				best_gain = atk
				best_choice = {"attacker": a, "target": -1}
			continue
		for t in opp_mons:
			var c: Dictionary = t.card
			var pos: int = c.get("position", 0)
			var gain := -1
			if pos & OCG.POS_FACEDOWN:
				gain = 300 if atk >= 1600 else -1
			elif pos & OCG.POS_DEFENSE:
				gain = 500 if atk > int(c.get("defense", 0)) else -1
			else:
				var diff: int = atk - int(c.get("attack", 0))
				gain = diff + 1000 if diff > 0 else -1
			if gain > best_gain:
				best_gain = gain
				best_choice = {"attacker": a, "target": t.seq}
	for e in msg.activatable:
		var type: int = CardDB.get_card(e.code).get("type", 0)
		if type & OCG.TYPE_MONSTER and _activations.get(_key(e), 0) < 1:
			_activations[_key(e)] = 1
			return OCGResponse.command(0, e.index)
	if best_gain >= 0 and not best_choice.is_empty():
		_planned_target = best_choice
		return OCGResponse.command(1, best_choice.attacker.index)
	if msg.to_m2:
		return OCGResponse.command(2)
	return OCGResponse.command(3)


# ---------------------------------------------------------------- cadenas

func _chain(msg: Dictionary) -> PackedByteArray:
	if msg.chains.is_empty():
		return OCGResponse.cancel()
	if msg.forced:
		return OCGResponse.int32(0)
	var chain: Array = controller.chain
	var top_is_opp: bool = not chain.is_empty() and chain.back().controller != player
	var attacked: bool = not controller.current_attack.is_empty() and controller.current_attack.attacker.controller != player
	var used := {}
	for link in chain:
		used[link.code] = true
	for e in msg.chains:
		if used.has(e.code) and e.location != OCG.LOCATION_MZONE:
			continue
		var type: int = CardDB.get_card(e.code).get("type", 0)
		var is_monster_effect: bool = type & OCG.TYPE_MONSTER and e.location != OCG.LOCATION_SZONE
		if is_monster_effect or top_is_opp or attacked:
			return OCGResponse.int32(e.index)
	return OCGResponse.cancel()


# ---------------------------------------------------------------- selección de cartas

## 1 = prefiere cartas del rival, -1 = propias y de poco valor (costes), 0 = las mejores propias
func _intent(hint: int) -> int:
	if hint in [500, 501, 504, 511, 512, 513, 519, 531, 532, 533, 579]:
		return -1
	if hint in [506, 508, 509, 518, 527, 529, 534, 573, 577]:
		return 0
	if hint in [502, 503, 505, 507, 514, 515, 516, 517, 520, 522, 523, 524, 525, 530, 575, 576]:
		return 1
	return 2 # ambiguo (p.ej. 551 "objetivos del efecto")


func _score(c: Dictionary, intent: int) -> float:
	var v := float(_value(c))
	var mine: bool = c.get("controller", player) == player
	match intent:
		-1: return (10000.0 if mine else 0.0) - v
		0: return (10000.0 if mine else 0.0) + v
		1: return (0.0 if mine else 10000.0) + v
	# Ambiguo: desde el Cementerio, el mejor; en el campo, el del rival
	if c.get("location", 0) == OCG.LOCATION_GRAVE:
		return v
	return (0.0 if mine else 10000.0) + v


func _select_card(msg: Dictionary) -> PackedByteArray:
	var hint: int = controller.last_hint.get(player, 0)
	var cards: Array = msg.cards
	if hint == 549 and not _planned_target.is_empty():
		var seq: int = _planned_target.target
		for c in cards:
			if c.controller == _opp() and c.location == OCG.LOCATION_MZONE and c.sequence == seq:
				return OCGResponse.cards([c.index])
	var intent := _intent(hint)
	var sorted := cards.duplicate()
	sorted.sort_custom(func(a, b): return _score(a, intent) > _score(b, intent))
	var n: int = clamp(max(msg.min, 1), 1, msg.max)
	var idx := []
	for i in min(n, sorted.size()):
		idx.append(sorted[i].index)
	return OCGResponse.cards(idx)


func _tribute(msg: Dictionary) -> PackedByteArray:
	var sorted: Array = msg.cards.duplicate()
	sorted.sort_custom(func(a, b): return _value(a) < _value(b))
	var idx := []
	var total := 0
	for c in sorted:
		if total >= msg.min: break
		idx.append(c.index)
		total += int(c.release_param)
	return OCGResponse.cards(idx)


func _unselect(msg: Dictionary) -> PackedByteArray:
	if msg.finishable or msg.select_cards.is_empty():
		return OCGResponse.cancel()
	var intent := _intent(controller.last_hint.get(player, 0))
	var best = msg.select_cards[0]
	for c in msg.select_cards:
		if _score(c, intent) > _score(best, intent):
			best = c
	return OCGResponse.select_unselect(best.index)


func _position(msg: Dictionary) -> PackedByteArray:
	var c := CardDB.get_card(msg.code)
	var atk: int = c.get("attack", 0)
	var def: int = c.get("defense", 0)
	var p: int = msg.positions
	if p & OCG.POS_FACEUP_ATTACK and (atk >= def or atk >= 1600):
		return OCGResponse.int32(OCG.POS_FACEUP_ATTACK)
	for pos in [OCG.POS_FACEUP_DEFENSE, OCG.POS_FACEDOWN_DEFENSE, OCG.POS_FACEUP_ATTACK, OCG.POS_FACEDOWN_ATTACK]:
		if p & pos:
			return OCGResponse.int32(pos)
	return OCGResponse.int32(OCG.POS_FACEUP_ATTACK)


func _place(msg: Dictionary, attempt: int) -> PackedByteArray:
	var zones := OCGResponse.free_zones(msg.player, msg.flag)
	var prefer_mine: bool = msg.type == OCG.MSG_SELECT_PLACE
	var order := [2, 1, 3, 0, 4, 5, 6, 7]
	zones.sort_custom(func(a, b):
		var am: bool = (a.player == player) == prefer_mine
		var bm: bool = (b.player == player) == prefer_mine
		if am != bm: return am
		return order.find(a.sequence) < order.find(b.sequence))
	var out := []
	for i in msg.count:
		if zones.is_empty(): break
		out.append(zones[(i + attempt) % zones.size()])
	return OCGResponse.places(out)


func _sum(msg: Dictionary) -> PackedByteArray:
	var acc: int = msg.acc
	var must_sum := 0
	for c in msg.must_cards:
		must_sum += int(c.param) & 0xffff
	var cards: Array = msg.cards
	var n: int = min(cards.size(), 16)
	var best_mask := -1
	var best_count := 999
	for mask in range(1, 1 << n):
		var total := must_sum
		var count := 0
		for i in n:
			if (mask >> i) & 1:
				total += int(cards[i].param) & 0xffff
				count += 1
		var ok: bool = (total == acc) if msg.select_mode == 0 else (total >= acc)
		if ok and count >= msg.min and (msg.max == 0 or count <= msg.max) and count < best_count:
			best_count = count
			best_mask = mask
	var idx := []
	if best_mask < 0:
		for i in n:
			idx.append(cards[i].index)
	else:
		for i in n:
			if (best_mask >> i) & 1:
				idx.append(cards[i].index)
	return OCGResponse.cards(idx)


func _counter(msg: Dictionary) -> PackedByteArray:
	var remaining: int = msg.count
	var out := []
	for c in msg.cards:
		var take: int = min(int(c.counters), remaining)
		out.append(take)
		remaining -= take
	return OCGResponse.counters(out)


func _announce_card() -> PackedByteArray:
	var pool: Array = controller.decks[_opp()].get("main", [])
	if pool.is_empty():
		return OCGResponse.int32(0)
	_announce_try += 1
	return OCGResponse.int32(pool[_announce_try % pool.size()])
