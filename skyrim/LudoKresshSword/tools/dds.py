"""Minimal DDS writer (DXT1 / DXT5 with full mip chain) using etcpak for BC compression."""
import struct
import numpy as np
from PIL import Image
import etcpak

DDSD_CAPS, DDSD_HEIGHT, DDSD_WIDTH, DDSD_PIXELFORMAT = 0x1, 0x2, 0x4, 0x1000
DDSD_MIPMAPCOUNT, DDSD_LINEARSIZE = 0x20000, 0x80000
DDPF_FOURCC = 0x4
DDSCAPS_COMPLEX, DDSCAPS_TEXTURE, DDSCAPS_MIPMAP = 0x8, 0x1000, 0x400000


def _mips(img):
    levels = [img]
    w, h = img.size
    while w > 4 and h > 4:
        w, h = max(w // 2, 4), max(h // 2, 4)
        levels.append(img.resize((w, h), Image.LANCZOS))
    return levels


def _compress(img, fmt):
    # etcpak takes RGBA bytes (verified with a Pillow round-trip)
    rgba = np.ascontiguousarray(np.asarray(img.convert('RGBA')))
    h, w = rgba.shape[:2]
    if fmt == 'DXT1':
        return etcpak.compress_bc1(rgba.tobytes(), w, h)
    return etcpak.compress_bc3(rgba.tobytes(), w, h)


def write_dds(path, img, fmt='DXT1', mips=True, renormalize=False):
    levels = _mips(img) if mips else [img]
    if renormalize:  # normal maps: re-normalise each mip level
        fixed = []
        for lv in levels:
            a = np.asarray(lv.convert('RGBA')).astype(float)
            n = a[..., :3] / 127.5 - 1.0
            n /= np.linalg.norm(n, axis=2, keepdims=True) + 1e-8
            a[..., :3] = np.clip((n + 1.0) * 127.5, 0, 255)
            fixed.append(Image.fromarray(a.round().astype(np.uint8), 'RGBA'))
        levels = fixed
    blobs = [_compress(lv, fmt) for lv in levels]
    w, h = img.size
    flags = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_LINEARSIZE
    caps = DDSCAPS_TEXTURE
    if len(levels) > 1:
        flags |= DDSD_MIPMAPCOUNT
        caps |= DDSCAPS_COMPLEX | DDSCAPS_MIPMAP
    pf = struct.pack('<II4sIIIII', 32, DDPF_FOURCC, fmt.encode(), 0, 0, 0, 0, 0)
    header = struct.pack('<4sIIIIIII', b'DDS ', 124, flags, h, w, len(blobs[0]), 0, len(levels))
    header += b'\0' * 44 + pf + struct.pack('<IIIII', caps, 0, 0, 0, 0)
    assert len(header) == 128
    with open(path, 'wb') as f:
        f.write(header)
        for b in blobs:
            f.write(b)
