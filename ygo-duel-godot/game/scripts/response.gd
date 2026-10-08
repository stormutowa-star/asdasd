class_name OCGResponse
## Construye los buffers de respuesta que espera ocgcore (ver playerop.cpp).


static func int32(v: int) -> PackedByteArray:
	var b := PackedByteArray()
	b.resize(4)
	b.encode_s32(0, v)
	return b


## SELECT_IDLECMD / SELECT_BATTLECMD: (índice << 16) | tipo
## Idle: 0 invocar, 1 inv. especial, 2 cambiar posición, 3 colocar monstruo, 4 colocar M/T,
##       5 activar, 6 ir a Batalla, 7 ir a Final, 8 barajar mano
## Batalla: 0 activar, 1 atacar, 2 ir a Main 2, 3 ir a Final
static func command(type: int, index: int = 0) -> PackedByteArray:
	return int32((index << 16) | type)


## SELECT_CARD / SELECT_TRIBUTE / SELECT_SUM: [i32 0][u32 n][u32 índice...]
static func cards(indices: Array) -> PackedByteArray:
	var b := PackedByteArray()
	b.resize(8 + 4 * indices.size())
	b.encode_s32(0, 0)
	b.encode_u32(4, indices.size())
	for i in indices.size():
		b.encode_u32(8 + 4 * i, int(indices[i]))
	return b


static func cancel() -> PackedByteArray:
	return int32(-1)


## SELECT_UNSELECT_CARD: [i32 1][i32 índice] (índice sobre select_cards + unselect_cards)
static func select_unselect(index: int) -> PackedByteArray:
	var b := PackedByteArray()
	b.resize(8)
	b.encode_s32(0, 1)
	b.encode_s32(4, index)
	return b


## SELECT_PLACE / SELECT_DISFIELD: 3 bytes por zona (jugador, ubicación, secuencia)
static func places(zones: Array) -> PackedByteArray:
	var b := PackedByteArray()
	for z in zones:
		b.append(int(z.player))
		b.append(int(z.location))
		b.append(int(z.sequence))
	return b


## SELECT_COUNTER: un u16 por carta
static func counters(amounts: Array) -> PackedByteArray:
	var b := PackedByteArray()
	b.resize(2 * amounts.size())
	for i in amounts.size():
		b.encode_u16(2 * i, int(amounts[i]))
	return b


## SORT_CARD / SORT_CHAIN: un i8 por carta, o -1 para dejar el orden por defecto
static func sort(order: Array) -> PackedByteArray:
	var b := PackedByteArray()
	if order.is_empty():
		b.append(0xFF)
		return b
	for v in order:
		b.append(int(v) & 0xFF)
	return b


static func uint64(v: int) -> PackedByteArray:
	var b := PackedByteArray()
	b.resize(8)
	b.encode_u64(0, v)
	return b


## Devuelve las zonas libres (bits a 0) de un flag de SELECT_PLACE, relativas a `player`.
static func free_zones(player: int, flag: int) -> Array:
	var out := []
	for side in 2:
		var p := player if side == 0 else 1 - player
		var shift := 16 * side
		for seq in 7:
			if not (flag >> (shift + seq)) & 1:
				out.append({"player": p, "location": OCG.LOCATION_MZONE, "sequence": seq})
		for seq in 8:
			if not (flag >> (shift + 8 + seq)) & 1:
				out.append({"player": p, "location": OCG.LOCATION_SZONE, "sequence": seq})
	return out
