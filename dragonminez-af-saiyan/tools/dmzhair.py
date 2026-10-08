"""Minimal codec for DragonMineZ hair codes (DMZ1:/DMZF1:): base62 bigint -> raw deflate -> uncompressed NBT."""
import struct, zlib, io, json, sys

B62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

def b62_decode(s):
    v = 0
    for c in s:
        v = v * 62 + B62.index(c)
    # mimic BigInteger.toByteArray() then strip leading 0
    n = (v.bit_length() + 8) // 8  # signed two's complement length
    b = v.to_bytes(n, 'big')
    if len(b) > 1 and b[0] == 0:
        b = b[1:]
    return b

def b62_encode(b):
    v = int.from_bytes(b, 'big')
    if v == 0: return B62[0]
    out = []
    while v:
        v, r = divmod(v, 62)
        out.append(B62[r])
    return ''.join(reversed(out))

# --- NBT (Java edition, big endian). Values are tagged tuples: (type, value)
def _rstr(f):
    n = struct.unpack('>H', f.read(2))[0]
    return f.read(n).decode('utf-8')

def _rpay(f, t):
    if t == 1: return struct.unpack('>b', f.read(1))[0]
    if t == 2: return struct.unpack('>h', f.read(2))[0]
    if t == 3: return struct.unpack('>i', f.read(4))[0]
    if t == 4: return struct.unpack('>q', f.read(8))[0]
    if t == 5: return struct.unpack('>f', f.read(4))[0]
    if t == 6: return struct.unpack('>d', f.read(8))[0]
    if t == 8: return _rstr(f)
    if t == 9:
        et = f.read(1)[0]; n = struct.unpack('>i', f.read(4))[0]
        return {'__list__': et, 'items': [_rpay(f, et) for _ in range(n)]}
    if t == 10:
        d = {}
        while True:
            ct = f.read(1)[0]
            if ct == 0: return d
            name = _rstr(f)
            d[name] = (ct, _rpay(f, ct))
    raise ValueError('unsupported tag %d' % t)

def read_nbt(data):
    f = io.BytesIO(data)
    t = f.read(1)[0]; assert t == 10
    _rstr(f)
    return _rpay(f, 10)

def _wstr(f, s):
    b = s.encode('utf-8'); f.write(struct.pack('>H', len(b))); f.write(b)

def _wpay(f, t, v):
    if t == 1: f.write(struct.pack('>b', v))
    elif t == 2: f.write(struct.pack('>h', v))
    elif t == 3: f.write(struct.pack('>i', v))
    elif t == 4: f.write(struct.pack('>q', v))
    elif t == 5: f.write(struct.pack('>f', v))
    elif t == 6: f.write(struct.pack('>d', v))
    elif t == 8: _wstr(f, v)
    elif t == 9:
        et = v['__list__'] if v['items'] else 0
        f.write(bytes([et])); f.write(struct.pack('>i', len(v['items'])))
        for it in v['items']: _wpay(f, et, it)
    elif t == 10:
        for k, (ct, cv) in v.items():
            f.write(bytes([ct])); _wstr(f, k); _wpay(f, ct, cv)
        f.write(b'\x00')
    else: raise ValueError(t)

def write_nbt(d):
    f = io.BytesIO(); f.write(b'\x0a'); _wstr(f, ''); _wpay(f, 10, d); return f.getvalue()

def decode(code):
    prefix, body = code.split(':', 1)
    raw = zlib.decompress(b62_decode(body), -15)
    return prefix, read_nbt(raw)

def encode(prefix, tag):
    c = zlib.compressobj(9, zlib.DEFLATED, -15)
    comp = c.compress(write_nbt(tag)) + c.flush()
    return prefix + ':' + b62_encode(comp)

def plain(v):
    if isinstance(v, tuple): return plain(v[1])
    if isinstance(v, dict) and '__list__' in v: return [plain(x) for x in v['items']]
    if isinstance(v, dict): return {k: plain(x) for k, x in v.items()}
    if isinstance(v, float): return round(v, 3)
    return v

if __name__ == '__main__':
    p, t = decode(sys.argv[1] if len(sys.argv) > 1 else sys.stdin.read().strip())
    print(p); print(json.dumps(plain(t), indent=1))
