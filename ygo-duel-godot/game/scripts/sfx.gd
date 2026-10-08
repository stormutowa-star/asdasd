extends Node
## Autoload "Sfx": efectos de sonido chiptune sintetizados por código (onda cuadrada, triangular y ruido),
## al estilo de las consolas portátiles. Se pueden sustituir con skin/sfx_<nombre>.wav.

const RATE := 22050

var enabled := true
var volume_db := -8.0
var _streams := {}
var _players: Array[AudioStreamPlayer] = []
var _next := 0


func _ready() -> void:
	for i in 8:
		var p := AudioStreamPlayer.new()
		p.volume_db = volume_db
		add_child(p)
		_players.append(p)
	_build()


func play(name: String, pitch := 1.0) -> void:
	if not enabled or not _streams.has(name):
		return
	var p := _players[_next]
	_next = (_next + 1) % _players.size()
	p.stream = _streams[name]
	p.pitch_scale = pitch
	p.volume_db = volume_db
	p.play()


func _build() -> void:
	var defs := {
		"blip": func(): return _tone([[1320, 0.035]], "square", 0.25),
		"select": func(): return _tone([[880, 0.04], [1320, 0.05]], "square", 0.25),
		"cancel": func(): return _tone([[660, 0.05], [440, 0.07]], "square", 0.25),
		"draw": func(): return _noise(0.09, 3000.0, 0.25, -0.6),
		"summon": func(): return _tone([[523, 0.06], [659, 0.06], [784, 0.06], [1047, 0.14]], "square", 0.3),
		"special": func(): return _sweep(300, 1600, 0.45, "square", 0.28, 12.0),
		"set": func(): return _tone([[220, 0.05], [165, 0.08]], "square", 0.3),
		"flip": func(): return _sweep(500, 1100, 0.12, "square", 0.25, 0.0),
		"activate": func(): return _tone([[784, 0.08], [1047, 0.08], [1568, 0.22]], "triangle", 0.45),
		"attack": func(): return _mix(_noise(0.22, 1800.0, 0.35, -1.2), _sweep(900, 200, 0.22, "square", 0.2, 0.0)),
		"hit": func(): return _noise(0.18, 1200.0, 0.5, -0.4),
		"destroy": func(): return _mix(_noise(0.55, 700.0, 0.55, -0.15), _sweep(400, 60, 0.5, "square", 0.18, 0.0)),
		"damage": func(): return _sweep(700, 120, 0.35, "square", 0.3, 20.0),
		"recover": func(): return _sweep(400, 1400, 0.35, "triangle", 0.4, 10.0),
		"tick": func(): return _tone([[1600, 0.018]], "square", 0.12),
		"turn": func(): return _tone([[392, 0.09], [523, 0.09], [659, 0.09], [784, 0.25]], "square", 0.28),
		"phase": func(): return _tone([[988, 0.05], [1319, 0.08]], "triangle", 0.4),
		"chain": func(): return _tone([[1047, 0.05], [1397, 0.05], [1047, 0.05], [1397, 0.1]], "square", 0.22),
		"negate": func(): return _tone([[311, 0.1], [233, 0.2]], "square", 0.3),
		"win": func(): return _tone([[523, 0.12], [659, 0.12], [784, 0.12], [1047, 0.12], [784, 0.12], [1047, 0.4]], "square", 0.3),
		"lose": func(): return _tone([[392, 0.18], [370, 0.18], [349, 0.18], [262, 0.5]], "triangle", 0.45),
	}
	for name in defs:
		var custom := Paths.skin_dir.path_join("sfx_%s.wav" % name)
		if FileAccess.file_exists(custom):
			var s := AudioStreamWAV.load_from_file(custom)
			if s:
				_streams[name] = s
				continue
		_streams[name] = _to_stream(defs[name].call())


func _wave(kind: String, phase: float) -> float:
	var f := fposmod(phase, 1.0)
	match kind:
		"square": return 1.0 if f < 0.5 else -1.0
		"pulse": return 1.0 if f < 0.25 else -1.0
		"triangle": return 4.0 * abs(f - 0.5) - 1.0
	return sin(f * TAU)


## Secuencia de notas [[frecuencia, duración], ...]
func _tone(notes: Array, kind: String, amp: float) -> PackedFloat32Array:
	var out := PackedFloat32Array()
	var phase := 0.0
	for n in notes:
		var count := int(RATE * float(n[1]))
		for i in count:
			phase += float(n[0]) / RATE
			var env: float = min(1.0, (count - i) / (RATE * 0.02))
			out.append(_wave(kind, phase) * amp * env)
	return out


## Barrido de frecuencia con vibrato opcional
func _sweep(f0: float, f1: float, dur: float, kind: String, amp: float, vibrato: float) -> PackedFloat32Array:
	var out := PackedFloat32Array()
	var count := int(RATE * dur)
	var phase := 0.0
	for i in count:
		var t := float(i) / count
		var f: float = lerp(f0, f1, t) * (1.0 + 0.03 * sin(TAU * vibrato * i / RATE))
		phase += f / RATE
		out.append(_wave(kind, phase) * amp * (1.0 - t * 0.7))
	return out


## Ruido "de 8 bits" (sample & hold) con caída de volumen
func _noise(dur: float, rate_hz: float, amp: float, rate_slope: float) -> PackedFloat32Array:
	var out := PackedFloat32Array()
	var count := int(RATE * dur)
	var rng := RandomNumberGenerator.new()
	rng.seed = int(rate_hz * 1000 + dur * 100)
	var hold := 0.0
	var acc := 0.0
	for i in count:
		var t := float(i) / count
		acc += max(50.0, rate_hz * (1.0 + rate_slope * t)) / RATE
		if acc >= 1.0:
			acc = fposmod(acc, 1.0)
			hold = rng.randf_range(-1.0, 1.0)
		out.append(hold * amp * pow(1.0 - t, 1.5))
	return out


func _mix(a: PackedFloat32Array, b: PackedFloat32Array) -> PackedFloat32Array:
	var n: int = max(a.size(), b.size())
	var out := PackedFloat32Array()
	out.resize(n)
	for i in n:
		var v := (a[i] if i < a.size() else 0.0) + (b[i] if i < b.size() else 0.0)
		out[i] = clamp(v, -1.0, 1.0)
	return out


func _to_stream(samples: PackedFloat32Array) -> AudioStreamWAV:
	var data := PackedByteArray()
	data.resize(samples.size() * 2)
	for i in samples.size():
		data.encode_s16(i * 2, int(clamp(samples[i], -1.0, 1.0) * 32000.0))
	var s := AudioStreamWAV.new()
	s.format = AudioStreamWAV.FORMAT_16_BITS
	s.mix_rate = RATE
	s.stereo = false
	s.data = data
	return s
