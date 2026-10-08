"""Generates the Makuo saga entity textures by recoloring DragonMineZ's own saga textures (GPL-3.0).

Usage: python3 gen_textures.py <dragonminez assets dir> <output textures/entity/sagas dir>
"""
import colorsys, os, sys
from PIL import Image

SRC, OUT = sys.argv[1], sys.argv[2]
os.makedirs(OUT, exist_ok=True)


def hexrgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def load(name):
    return Image.open(os.path.join(SRC, 'textures/entity/sagas', name + '.png')).convert('RGBA')


# ---------- pixel classification ----------
def classify_piccolo(r, g, b):
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    h *= 360
    if s < 0.15:
        return 'eye_white' if v > 0.6 else 'pupil'
    if 80 <= h <= 160:
        return 'skin'
    if 200 <= h <= 225 and v > 0.4:
        return 'sash'
    if 240 <= h <= 290:
        return 'gi'
    if (h < 20 or h > 340) and v > 0.7:
        return 'muscle'
    if (h < 30 or h > 340) and v <= 0.7:
        return 'shoes'
    return 'other'


def classify_slug(r, g, b):
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    h *= 360
    if s < 0.15:
        return 'white' if v > 0.6 else 'pupil'
    if 240 <= h <= 290:
        return 'armor'
    if 30 <= h < 60 and v > 0.6:
        return 'gold'
    if (h < 30 or h > 340) and v > 0.55 and s > 0.6:
        return 'orange' if h > 5 else 'red'
    if 80 <= h <= 160:
        return 'skin'
    if 185 <= h <= 225:
        return 'gem'
    return 'dark'


def recolor(img, classify, palette):
    """Recolor each class to its palette color, keeping the original light/shade of every pixel."""
    px = img.load()
    w, hgt = img.size
    groups = {}
    for y in range(hgt):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            groups.setdefault(classify(r, g, b), []).append((x, y, max(r, g, b) / 255))
    out = img.copy()
    op = out.load()
    for cls, pts in groups.items():
        if cls not in palette:
            continue
        target = hexrgb(palette[cls])
        mean_v = sum(p[2] for p in pts) / len(pts) or 1
        for x, y, v in pts:
            f = max(0.45, min(1.6, v / mean_v))
            op[x, y] = tuple(min(255, int(c * f)) for c in target) + (255,)
    return out


# ---------- painting helpers for the piccolo model's hidden parts ----------
def fill(img, x0, y0, x1, y1, color, shade=0.0):
    p = img.load()
    rgb = hexrgb(color)
    for y in range(y0, y1):
        for x in range(x0, x1):
            t = (y - y0) / max(1, y1 - y0 - 1)
            f = 1.0 - shade * t
            p[x, y] = tuple(min(255, int(c * f)) for c in rgb) + (255,)


def cape(img, color, tattered=False):
    # cape / cape2 / cape3 are 10x6 planes: front at u 0..10, back at u 10..20, v 32..50
    fill(img, 0, 32, 20, 50, color, shade=0.35)
    p = img.load()
    for x in range(0, 20):  # darker fold lines
        if x % 4 == 1:
            for y in range(32, 50):
                r, g, b, a = p[x, y]
                p[x, y] = (int(r * 0.82), int(g * 0.82), int(b * 0.82), a)
    if tattered:
        for x in range(0, 20):
            cut = (x * 7) % 4
            for y in range(50 - cut, 50):
                p[x, y] = (0, 0, 0, 0)


def shoulder_pads(img, color, trim):
    # 6x4x5 box with uv (42,37): region u 42..64, v 37..46
    fill(img, 42, 37, 64, 46, color, shade=0.3)
    p = img.load()
    t = hexrgb(trim)
    for x in range(42, 64):
        p[x, 45] = t + (255,)
    for x in range(47, 53):
        p[x, 37] = t + (255,)


def eyes(img, color):
    """Piccolo's pupils are the dark pixels on the face (head front, u 8..16, v 8..16)."""
    p = img.load()
    for y in range(8, 16):
        for x in range(8, 16):
            r, g, b, a = p[x, y]
            if a and max(r, g, b) < 60:
                p[x, y] = hexrgb(color) + (255,)


def glow_eyes(img):
    p = img.load()
    for y in range(8, 16):
        for x in range(8, 16):
            r, g, b, a = p[x, y]
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if a and (s < 0.15 or max(r, g, b) < 60):
                p[x, y] = (250, 235, 255, 255)


def crystal_fists(img, base='#9b4dff', light='#e0c2ff'):
    """Forearm boxes (right: uv-faces 40..56 x 26..32, left: 32..48 x 58..64) become amethyst."""
    p = img.load()
    for (x0, y0, x1, y1) in [(40, 26, 56, 32), (32, 58, 48, 64)]:
        for y in range(y0, y1):
            for x in range(x0, x1):
                c = light if (x + y) % 3 == 0 else base
                p[x, y] = hexrgb(c) + (255,)


def piccolo(name, pal, *, cape_color=None, tattered=False, pads=None, pupils=None, glow=False, fists=False):
    img = recolor(load('saga_piccolo'), classify_piccolo, pal)
    if cape_color:
        cape(img, cape_color, tattered)
    if pads:
        shoulder_pads(img, *pads)
    if pupils:
        eyes(img, pupils)
    if glow:
        glow_eyes(img)
    if fists:
        crystal_fists(img)
    img.save(os.path.join(OUT, name + '.png'))
    return img


def slug(name, pal):
    img = recolor(load('saga_slug'), classify_slug, pal)
    img.save(os.path.join(OUT, name + '.png'))
    return img


# Makuo, el Exiliado: dark green skin, black/violet gi, white tattered cape, violet eyes
piccolo('saga_makuo', {'skin': '#2f7d45', 'muscle': '#b0457e', 'gi': '#1b1426', 'sash': '#8a3cf0',
                       'shoes': '#2a1636'},
        cape_color='#ecebf2', tattered=True, pads=('#e4e2ee', '#8a3cf0'), pupils='#a64dff')

# Drakhul, el Coloso (base and giant share the look)
for n in ('saga_drakhul', 'saga_drakhul_giant'):
    piccolo(n, {'skin': '#4f7f2b', 'muscle': '#c2564e', 'gi': '#4a0f1a', 'sash': '#111111', 'shoes': '#1c1c1c'},
            pads=('#2b2b33', '#8a3cf0'), pupils='#ff3b3b')

# Garuk, el Puño de Cristal: brown-green brute, grey gi, amethyst fists and pads
piccolo('saga_garuk', {'skin': '#5c7a2e', 'muscle': '#c96a4c', 'gi': '#3b3b44', 'sash': '#9b4dff', 'shoes': '#222228'},
        pads=('#9b4dff', '#e0c2ff'), pupils='#9b4dff', fists=True)

# Seiryn, la Voz del Vacío: pale green sorcerer in a long violet robe
piccolo('saga_seiryn', {'skin': '#6fb36a', 'muscle': '#d98ab8', 'gi': '#5a1f9a', 'sash': '#d4a017', 'shoes': '#2c0f4d'},
        cape_color='#4a157f', pads=('#5a1f9a', '#d4a017'), pupils='#ffd24d')

# Vokkar, la Sombra: teal-green assassin, navy-black gi, red sash
piccolo('saga_vokkar', {'skin': '#2d8a6e', 'muscle': '#c24f6a', 'gi': '#0f1626', 'sash': '#c81e3a', 'shoes': '#0a0d14'},
        pupils='#c81e3a')

# Saien, el último Guardián (friendly quest NPC): old, pale, white robe, dark blue cape
piccolo('saga_saien', {'skin': '#8fae86', 'muscle': '#d9a0a8', 'gi': '#e6e3d8', 'sash': '#6b4a2b', 'shoes': '#4a3424'},
        cape_color='#1f2f5c', pads=('#e6e3d8', '#6b4a2b'))

# Zhar warriors and elites: Slug's armored soldier look, recolored to black armor with violet veins
slug('saga_zhar', {'armor': '#141119', 'gold': '#3a2452', 'orange': '#1d1824', 'red': '#7a2fd0', 'white': '#2b2833',
                   'gem': '#b06bff', 'skin': '#3f8f4f', 'dark': '#120f16'})
slug('saga_zhar_elite', {'armor': '#1d1626', 'gold': '#c79bff', 'orange': '#6a2fc0', 'red': '#9b4dff', 'white': '#e8d8ff',
                         'gem': '#f2e6ff', 'skin': '#2f7d45', 'dark': '#1a1024'})

# Makuo — Alas del Vacío: 128x128 (wing UVs live in the bottom half)
final = recolor(load('saga_piccolo'), classify_piccolo,
                {'skin': '#8b3fd6', 'muscle': '#e3a6ff', 'gi': '#101014', 'sash': '#d4a017', 'shoes': '#1a1022'})
shoulder_pads(final, '#15121c', '#d4a017')
glow_eyes(final)
big = final.resize((128, 128), Image.NEAREST)
p = big.load()
# Wing blades: each blade face strip is painted as a vertical gradient (deep violet root -> lilac -> white edge).
for x in range(0, 128):
    for y in range(64, 128):
        t = (y - 64) / 63
        core = (x % 8) in (3, 4)
        base = [(70, 10, 140), (150, 60, 235), (225, 190, 255)]
        if t < 0.5:
            a, b2, k = base[0], base[1], t / 0.5
        else:
            a, b2, k = base[1], base[2], (t - 0.5) / 0.5
        c = [int(a[i] + (b2[i] - a[i]) * k) for i in range(3)]
        if core:
            c = [min(255, int(v * 1.25 + 20)) for v in c]
        p[x, y] = tuple(c) + (255,)
big.save(os.path.join(OUT, 'saga_makuo_wings.png'))
print('textures written to', OUT)
