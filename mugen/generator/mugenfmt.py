"""Escritores (y lector de verificacion) de formatos de MUGEN:
SFF v1 (sprites PCX de 8 bits), SND v1 y paletas ACT."""
import struct
import numpy as np


# ------------------------------------------------------------------ PCX
def pcx_encode(img, pal):
    """img: array (h,w) de indices 0..255; pal: (256,3)."""
    h, w = img.shape
    bpl = w + (w & 1)
    hdr = bytearray(128)
    hdr[0] = 10
    hdr[1] = 5
    hdr[2] = 1
    hdr[3] = 8
    struct.pack_into('<4H', hdr, 4, 0, 0, w - 1, h - 1)
    struct.pack_into('<2H', hdr, 12, 72, 72)
    hdr[65] = 1
    struct.pack_into('<H', hdr, 66, bpl)
    struct.pack_into('<H', hdr, 68, 1)
    out = bytearray(hdr)
    for y in range(h):
        row = list(img[y].astype(int))
        if bpl > w:
            row.append(0)
        i = 0
        n = len(row)
        while i < n:
            v = row[i]
            j = i + 1
            while j < n and row[j] == v and j - i < 63:
                j += 1
            cnt = j - i
            if cnt > 1 or v >= 0xC0:
                out.append(0xC0 | cnt)
                out.append(v)
            else:
                out.append(v)
            i = j
    out.append(0x0C)
    out += bytes(np.asarray(pal, np.uint8).reshape(-1).tolist())
    return bytes(out)


def pcx_decode(data):
    w = struct.unpack_from('<H', data, 8)[0] + 1
    h = struct.unpack_from('<H', data, 10)[0] + 1
    bpl = struct.unpack_from('<H', data, 66)[0]
    pix = bytearray()
    i = 128
    need = bpl * h
    while len(pix) < need:
        b = data[i]
        i += 1
        if b >= 0xC0:
            pix += bytes([data[i]]) * (b & 0x3F)
            i += 1
        else:
            pix.append(b)
    arr = np.frombuffer(bytes(pix[:need]), np.uint8).reshape(h, bpl)[:, :w]
    return arr


# ------------------------------------------------------------------ SFF v1
def write_sff(path, sprites, pal, comment=b'Horned Blade - generado por codigo'):
    """sprites: lista de dict(group, no, img, ax, ay, link=None)."""
    groups = len(set(s['group'] for s in sprites))
    hdr = bytearray(512)
    hdr[0:12] = b'ElecbyteSpr\x00'
    hdr[12:16] = bytes([0, 1, 0, 1])
    struct.pack_into('<IIII', hdr, 16, groups, len(sprites), 512, 32)
    hdr[32] = 1     # paleta compartida
    hdr[36:36 + len(comment)] = comment
    body = bytearray()
    offset = 512
    index_of = {}
    for i, s in enumerate(sprites):
        if s.get('link') is not None:
            data = b''
            link = index_of[s['link']]
        else:
            data = pcx_encode(s['img'], pal)
            link = 0
        nxt = offset + 32 + len(data) if i < len(sprites) - 1 else 0
        sub = bytearray(32)
        struct.pack_into('<IIhhhhhB', sub, 0, nxt, len(data), int(s['ax']), int(s['ay']),
                         int(s['group']), int(s['no']), link, 0 if i == 0 else 1)
        body += sub + data
        index_of[(s['group'], s['no'])] = i
        offset += 32 + len(data)
    with open(path, 'wb') as f:
        f.write(bytes(hdr) + bytes(body))


def read_sff(path):
    d = open(path, 'rb').read()
    assert d[:12] == b'ElecbyteSpr\x00', 'firma SFF'
    ng, ni, first, subsz = struct.unpack_from('<IIII', d, 16)
    out = []
    off = first
    for i in range(ni):
        nxt, ln, ax, ay, g, n, link, same = struct.unpack_from('<IIhhhhhB', d, off)
        data = d[off + 32: off + 32 + ln]
        if ln:
            img = pcx_decode(data)
            out.append(dict(group=g, no=n, ax=ax, ay=ay, w=img.shape[1], h=img.shape[0], same=same))
        else:
            ref = out[link]
            out.append(dict(group=g, no=n, ax=ax, ay=ay, w=ref['w'], h=ref['h'], same=same, link=link))
        if nxt == 0:
            break
        off = nxt
    return out


# ------------------------------------------------------------------ SND v1
def write_snd(path, sounds):
    """sounds: dict {(grupo, numero): wav_bytes}"""
    items = sorted(sounds.items())
    hdr = bytearray(512)
    hdr[0:12] = b'ElecbyteSnd\x00'
    hdr[12:16] = bytes([0, 1, 0, 1])
    struct.pack_into('<II', hdr, 16, len(items), 512)
    body = bytearray()
    off = 512
    for i, ((g, n), wav) in enumerate(items):
        nxt = off + 16 + len(wav) if i < len(items) - 1 else 0
        body += struct.pack('<IIii', nxt, len(wav), g, n) + wav
        off += 16 + len(wav)
    with open(path, 'wb') as f:
        f.write(bytes(hdr) + bytes(body))


def read_snd(path):
    d = open(path, 'rb').read()
    assert d[:12] == b'ElecbyteSnd\x00'
    n, first = struct.unpack_from('<II', d, 16)
    out = []
    off = first
    for i in range(n):
        nxt, ln, g, s = struct.unpack_from('<IIii', d, off)
        assert d[off + 16:off + 20] == b'RIFF'
        out.append((g, s, ln))
        if nxt == 0:
            break
        off = nxt
    return out


# ------------------------------------------------------------------ ACT
def write_act(path, pal):
    """MUGEN lee los .act con los colores en orden inverso (255 primero)."""
    pal = np.asarray(pal, np.uint8)
    with open(path, 'wb') as f:
        f.write(bytes(pal[::-1].reshape(-1).tolist()))
