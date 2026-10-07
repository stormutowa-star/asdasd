"""Paleta global de 256 colores (indice 0 = transparente en MUGEN).
1..72   colores del personaje (k-means de la referencia)
100..   rampas de efectos (rayo, fuego, chispas, polvo, estela, oro...)"""
import os
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
BUILD = os.path.join(HERE, 'build')


def ramp(stops, n):
    stops = np.array(stops, float)
    t = np.linspace(0, len(stops) - 1, n)
    out = []
    for v in t:
        i = min(int(v), len(stops) - 2)
        f = v - i
        out.append(stops[i] * (1 - f) + stops[i + 1] * f)
    return np.round(out).astype(int)


RAMPS = {
    # nombre: (inicio, n, paradas)
    'bolt':  (100, 16, [(10, 20, 70), (30, 70, 200), (60, 160, 255), (170, 230, 255), (255, 255, 255)]),
    'fire':  (116, 16, [(60, 0, 0), (170, 20, 10), (240, 80, 20), (255, 180, 40), (255, 250, 200)]),
    'spark': (132, 16, [(120, 40, 0), (230, 120, 10), (255, 210, 60), (255, 250, 200), (255, 255, 255)]),
    'dust':  (148, 16, [(60, 50, 45), (120, 105, 90), (180, 170, 155), (230, 225, 215)]),
    'trail': (164, 16, [(20, 40, 110), (60, 110, 220), (150, 200, 255), (235, 245, 255)]),
    'gold':  (180, 16, [(90, 50, 0), (190, 120, 20), (250, 200, 60), (255, 245, 180)]),
    'blood': (196, 16, [(40, 0, 10), (130, 0, 20), (220, 40, 40), (255, 140, 120)]),
    'green': (212, 16, [(0, 50, 20), (20, 140, 60), (90, 230, 120), (220, 255, 220)]),
    'gray':  (228, 28, [(0, 0, 0), (60, 64, 80), (140, 146, 170), (255, 255, 255)]),
}


def build_palette():
    pal = np.zeros((256, 3), int)
    pal[0] = (255, 0, 255)
    C = np.load(os.path.join(BUILD, 'pal_char.npy')).astype(int)
    pal[1:1 + len(C)] = C
    for name, (start, n, stops) in RAMPS.items():
        pal[start:start + n] = ramp(stops, n)
    return pal


def ramp_index(name, t):
    """t en [0,1] -> indice de paleta de la rampa."""
    start, n, _ = RAMPS[name]
    return start + int(round(max(0, min(1, t)) * (n - 1)))


PAL = None


def get():
    global PAL
    if PAL is None:
        PAL = build_palette()
    return PAL


def nearest(rgb, lo=1, hi=72):
    p = get()[lo:hi + 1]
    d = ((p - np.array(rgb)) ** 2).sum(1)
    return lo + int(d.argmin())


def shade_map(factor, lo=1, hi=72):
    """mapa indice->indice que oscurece/aclara los colores del personaje."""
    p = get()
    m = np.arange(256)
    for i in range(lo, hi + 1):
        c = np.clip(p[i] * factor + (0 if factor <= 1 else (factor - 1) * 30), 0, 255)
        m[i] = nearest(c, lo, hi)
    return m


# ------------------------------------------------------------- variantes
import colorsys


def _classify(c):
    r, g, b = [v / 255 for v in c]
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    hd = h * 360
    if s > 0.5 and 185 <= hd <= 240 and l > 0.12:
        return 'skin'
    if s > 0.3 and 15 <= hd <= 65 and l > 0.12:
        return 'gold'
    return 'armor'


def _tf(c, hue=None, sat=None, lmul=1.0, ladd=0.0, smul=1.0):
    r, g, b = [v / 255 for v in c]
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    if hue is not None:
        h = hue / 360
    if sat is not None:
        s = sat
    s = min(1, s * smul)
    l = min(1, max(0, l * lmul + ladd))
    return [int(round(v * 255)) for v in colorsys.hls_to_rgb(h, l, s)]


VARIANTS = [
    ('Original', {}),
    ('Fuerza de los Dioses', dict(skin=dict(hue=356), armor=dict(hue=0, smul=0.3, lmul=0.85), gold=dict())),
    ('Sombra', dict(skin=dict(hue=275, lmul=0.75), armor=dict(lmul=0.55, smul=0.3), gold=dict(sat=0.05, lmul=1.1))),
    ('Paladin Dorado', dict(skin=dict(), armor=dict(hue=44, sat=0.45, lmul=1.05), gold=dict(sat=0.1, lmul=1.2))),
    ('Vigil Verde', dict(skin=dict(hue=128, lmul=0.9), armor=dict(hue=28, sat=0.3), gold=dict(hue=18))),
    ('Hielo', dict(skin=dict(hue=188, smul=0.6, ladd=0.12), armor=dict(lmul=1.15, ladd=0.08, smul=0.4),
                   gold=dict(hue=200, sat=0.5, lmul=1.1))),
]


def variant(i):
    base = get().copy()
    name, rules = VARIANTS[i]
    if not rules:
        return base
    for k in range(1, 73):
        cls = _classify(base[k])
        lum = base[k] @ [0.3, 0.59, 0.11]
        if cls == 'armor' and lum < 22:
            continue   # contornos
        base[k] = _tf(base[k], **rules.get(cls, {}))
    return base
