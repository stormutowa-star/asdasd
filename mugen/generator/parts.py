"""Paso 2: recorta las partes del cuerpo de la referencia (coordenadas de la
rejilla nativa espejada) y dibuja por codigo las piezas ocultas (brazos,
puños, empuñadura). Cada parte es una imagen de indices de paleta con un
pivote (articulacion) y un angulo de reposo."""
import math
import os
import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage as ndi
import palette as PL

HERE = os.path.dirname(os.path.abspath(__file__))
BUILD = os.path.join(HERE, 'build')

OUT = 1  # color de contorno (negro)


class Part:
    def __init__(self, name, img, pivot, rest=0.0, length=0.0):
        ys, xs = np.nonzero(img)
        y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
        self.name = name
        self.img = img[y0:y1, x0:x1].astype(np.uint8)
        self.pivot = (pivot[0] - x0, pivot[1] - y0)
        self.rest = rest        # angulo (grados) del hueso en la imagen
        self.length = length    # largo del hueso (px nativos)


def poly(pts, shape=(200, 200)):
    im = Image.new('L', (shape[1], shape[0]), 0)
    ImageDraw.Draw(im).polygon(pts, fill=1, outline=1)
    return np.array(im, bool)


def outline(img, color=OUT, only_light=True):
    """pone contorno en los pixeles del borde de la silueta."""
    m = img > 0
    er = ndi.binary_erosion(m, structure=np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0]]))
    edge = m & ~er
    if only_light:
        lum = PL.get()[img] @ [0.3, 0.59, 0.11]
        edge &= lum > 30
    out = img.copy()
    out[edge] = color
    return out


def inpaint(img, hole, valid):
    """rellena 'hole' propagando el color mas comun de los vecinos validos."""
    img = img.copy()
    known = valid & ~hole
    todo = hole.copy()
    H, W = img.shape
    for _ in range(80):
        if not todo.any():
            break
        grow = ndi.binary_dilation(known) & todo
        ys, xs = np.nonzero(grow)
        for y, x in zip(ys, xs):
            vals = []
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    yy, xx = y + dy, x + dx
                    if 0 <= yy < H and 0 <= xx < W and known[yy, xx]:
                        vals.append(img[yy, xx])
            if vals:
                img[y, x] = max(set(vals), key=vals.count)
        known |= grow
        todo &= ~grow
    return img


def ang(a, b):
    return math.degrees(math.atan2(b[1] - a[1], b[0] - a[0]))


def dist(a, b):
    return math.hypot(b[0] - a[0], b[1] - a[1])


# --------------------------------------------------------------------------
# geometria de la referencia (pixeles nativos, personaje mirando a la derecha)
HEAD_POLY = [(53, 44), (70, 41), (78, 36), (93, 36), (100, 41), (117, 45), (117, 57),
             (94, 57), (93, 62), (89, 65), (81, 65), (78, 61), (77, 57), (53, 57)]
TORSO_POLY = [(64, 60), (77, 54), (96, 54), (110, 59), (114, 66), (113, 88),
              (110, 100), (111, 110), (112, 129), (90, 131), (60, 130), (60, 110),
              (63, 98), (62, 76)]
PAULDRON_POLY = [(47, 66), (55, 58), (66, 56), (79, 57), (85, 63), (86, 80), (83, 88),
                 (60, 89), (50, 86), (47, 78)]
ABS_REGION = [(62, 86), (113, 86), (110, 100), (110, 109), (62, 109)]
POMMEL_HOLE = [(80, 104), (96, 104), (96, 122), (80, 122)]
THIGH_POLY = [(99, 106), (106, 104), (113, 112), (119, 123), (120, 131), (114, 137),
              (103, 137), (97, 131)]
SHIN_POLY = [(100, 131), (110, 127), (120, 128), (125, 133), (125, 141), (119, 147),
             (117, 153), (117, 159), (110, 161), (101, 160), (99, 150), (99, 139)]
FOOT_POLY = [(96, 155), (118, 155), (123, 159), (128, 163), (128, 170), (96, 170)]
BLADE_POLY = [(110, 80), (124, 67), (140, 52), (149, 40), (149, 21), (158, 13), (176, 12),
              (187, 17), (193, 26), (194, 39), (189, 45), (176, 47), (166, 53), (150, 69),
              (137, 84), (129, 92), (122, 97), (113, 93)]

PELVIS = (88, 112)
NECK = (86, 62)
SH_NEAR = (66, 78)
SH_FAR = (106, 71)
HIP_NEAR = (76, 113)
HIP_FAR = (100, 112)
HIP_T, KNEE_T, ANKLE_T = (104, 110), (112, 134), (107, 157)
GROUND_T = 169

GUARD = (121, 91)            # base de la hoja
TIP = (175, 22)
SWORD_ANG = ang(GUARD, TIP)  # ~ -52 grados
HILT_LEN = 36
GRIP_FAR = 7                 # distancia desde la guarda (mano lejana)
GRIP_NEAR = 25               # mano cercana
UPPER_LEN = 20
FORE_LEN = 26                # hasta el centro del puño


def col(rgb):
    return PL.nearest(rgb)


def build():
    nat = np.load(os.path.join(BUILD, 'nat_idx.npy')).astype(int)
    pal = PL.get()
    H, W = nat.shape
    char = nat > 0
    parts = {}

    # ---------------- cabeza
    m = poly(HEAD_POLY) & char
    img = np.where(m, nat, 0)
    parts['head'] = Part('head', outline(img), NECK)

    # ---------------- hombrera
    m = poly(PAULDRON_POLY) & char
    img = np.where(m, nat, 0)
    parts['pauldron'] = Part('pauldron', outline(img), SH_NEAR)

    # ---------------- torso (pecho + abdomen sintetizado + faldon)
    tm = poly(TORSO_POLY) & char
    torso = np.where(tm, nat, 0)
    # el abdomen esta tapado por brazos/manos: se sintetiza copiando el
    # patron de musculos del pecho hacia abajo
    absr = poly(ABS_REGION)
    chest_src = poly([(84, 64), (112, 64), (112, 88), (84, 88)]) & char
    for y in range(86, 110):
        for x in range(62, 114):
            if not absr[y, x]:
                continue
            sy = 76 + (y - 86) % 12
            sx = x
            if sx < 86:
                sx = x + 22
            if chest_src[sy, sx]:
                torso[y, x] = nat[sy, sx]
            else:
                torso[y, x] = col((10, 80, 190))
    # cinturon
    belt_dark, belt_mid, gold = col((40, 44, 80)), col((90, 100, 140)), col((230, 175, 60))
    for x in range(62, 112):
        torso[104, x] = OUT
        torso[105, x] = belt_mid
        torso[106, x] = belt_dark
        torso[107, x] = belt_dark
        torso[108, x] = OUT
    for y in range(103, 110):
        for x in range(84, 92):
            torso[y, x] = OUT if (y in (103, 109) or x in (84, 91)) else gold
    tm = tm | absr
    # pomo de la espada y mano cercana sobre el faldon -> rellenar
    hole = poly(POMMEL_HOLE) & tm
    hole &= ~poly([(60, 103), (115, 103), (115, 110), (60, 110)])
    skirt_valid = tm & poly([(58, 108), (114, 108), (114, 132), (58, 132)])
    torso = inpaint(torso, hole, skirt_valid)
    # bajo la cabeza / hombrera -> azul del pecho
    under = (poly(HEAD_POLY) | poly(PAULDRON_POLY)) & tm
    torso = inpaint(torso, under, tm & ~under & poly([(60, 54), (114, 54), (114, 92), (60, 92)]))
    torso[~tm] = 0
    parts['torso'] = Part('torso', outline(torso), PELVIS)

    # ---------------- pierna (plantilla: pierna delantera)
    m = poly(THIGH_POLY) & char
    parts['thigh'] = Part('thigh', outline(np.where(m, nat, 0)), HIP_T, ang(HIP_T, KNEE_T), dist(HIP_T, KNEE_T))
    m = poly(SHIN_POLY) & char
    parts['shin'] = Part('shin', outline(np.where(m, nat, 0)), KNEE_T, ang(KNEE_T, ANKLE_T), dist(KNEE_T, ANKLE_T))
    m = poly(FOOT_POLY) & char
    parts['foot'] = Part('foot', outline(np.where(m, nat, 0)), ANKLE_T, 0.0)

    # ---------------- espada: hoja recortada + empuñadura dibujada
    ax = (math.cos(math.radians(SWORD_ANG)), math.sin(math.radians(SWORD_ANG)))
    yy, xx = np.mgrid[0:H, 0:W]
    front = ((xx - GUARD[0]) * ax[0] + (yy - GUARD[1]) * ax[1]) > -1.5
    m = poly(BLADE_POLY) & char & front
    blade = np.where(m, nat, 0)
    # lienzo mas grande para la empuñadura
    big = np.zeros((H + 80, W + 80), int)
    off = 40
    big[off:off + H, off:off + W] = blade
    g = (GUARD[0] + off, GUARD[1] + off)
    hilt_c = [col((25, 30, 60)), col((60, 66, 100)), col((100, 110, 150))]
    nx, ny = -ax[1], ax[0]   # normal
    for t10 in range(0, HILT_LEN * 10):
        t = t10 / 10.0
        for s10 in range(-30, 31):
            s = s10 / 10.0
            px = g[0] - ax[0] * t + nx * s
            py = g[1] - ax[1] * t + ny * s
            X, Y = int(round(px)), int(round(py))
            if abs(s) > 2.2:
                c = OUT
            elif s < -0.8:
                c = hilt_c[2]
            elif s < 0.8:
                c = hilt_c[1]
            else:
                c = hilt_c[0]
            if c != OUT and int(t) % 4 == 0:
                c = hilt_c[0]
            if c == OUT and big[Y, X] not in (0, OUT):
                continue
            big[Y, X] = c
    # pomo dorado
    pc = (g[0] - ax[0] * (HILT_LEN + 1), g[1] - ax[1] * (HILT_LEN + 1))
    golds = [col((120, 70, 20)), col((210, 150, 50)), col((245, 215, 110))]
    for Y in range(int(pc[1]) - 5, int(pc[1]) + 6):
        for X in range(int(pc[0]) - 5, int(pc[0]) + 6):
            d = math.hypot(X - pc[0], Y - pc[1])
            if d <= 3.9:
                lt = (X - pc[0]) * -0.6 + (Y - pc[1]) * -0.8
                big[Y, X] = OUT if d > 3.0 else (golds[2] if lt > 1.2 else golds[1] if lt > -0.8 else golds[0])
    # guarda: barra transversal
    for t10 in range(-12, 18):
        t = t10 / 10.0
        for s10 in range(-75, 76):
            s = s10 / 10.0
            X = int(round(g[0] - ax[0] * t + nx * s))
            Y = int(round(g[1] - ax[1] * t + ny * s))
            edge = abs(s) > 6.6 or t < -0.6 or t > 1.0
            if big[Y, X] == 0 or not edge:
                big[Y, X] = OUT if edge else (golds[2] if t < 0.2 else golds[1])
    gn = (g[0] - ax[0] * GRIP_NEAR, g[1] - ax[1] * GRIP_NEAR)
    sword = Part('sword', outline(big), gn, SWORD_ANG, 0)
    sword.grip_far = GRIP_NEAR - GRIP_FAR     # distancia entre manos sobre el eje
    sword.blade_len = dist(GUARD, TIP) + GRIP_NEAR
    parts['sword'] = sword

    # ---------------- brazos y puños (procedurales)
    parts['upper'] = limb(UPPER_LEN, 6.4, 5.2, 'blue')
    parts['fore'] = limb(FORE_LEN - 4, 5.6, 4.6, 'armor')
    parts['fist'] = fist()
    return parts


def limb(L, r0, r1, kind):
    S = 4  # se dibuja a 4x y se reduce por moda para que quede pixel-art limpio
    Wd = int((L + r0 + r1 + 4) * S)
    Hd = int((max(r0, r1) * 2 + 6) * S)
    cy = Hd / 2
    cx0 = (r0 + 2) * S
    img = np.zeros((Hd, Wd), int)
    if kind == 'blue':
        ramp = [col((5, 30, 120)), col((6, 53, 160)), col((5, 88, 209)), col((19, 118, 222)), col((12, 160, 245))]
    else:
        ramp = [col((45, 49, 92)), col((74, 85, 127)), col((106, 120, 161)), col((150, 164, 204)), col((198, 210, 234))]
    for y in range(Hd):
        for x in range(Wd):
            t = (x - cx0) / (L * S)
            tc = min(1, max(0, t))
            r = (r0 + (r1 - r0) * tc) * S
            if kind == 'blue':
                r += math.sin(math.pi * tc) * 1.2 * S   # biceps
            px = cx0 + tc * L * S
            dy = y - cy
            d = math.hypot(x - px, dy)
            if d > r:
                continue
            if d > r - 1.0 * S:
                img[y, x] = OUT
                continue
            v = -dy / r   # luz desde arriba
            k = 0 if v < -0.55 else 1 if v < -0.1 else 2 if v < 0.35 else 3 if v < 0.7 else 4
            if kind == 'armor':
                # bandas grabadas del brazal
                if 0.15 < t < 0.85 and (int(x / S) % 6 == 0) and abs(dy) < r * 0.75:
                    k = max(0, k - 2)
                if 0.3 < t < 0.7 and abs(dy + r * 0.15) < 0.6 * S:
                    k = 0
            img[y, x] = ramp[k]
    small = mode_down(img, S)
    pivot = (cx0 / S, cy / S)
    p = Part(kind, small, pivot, 0.0, L)
    return p


def fist():
    S = 4
    R = 5.2
    D = int((R * 2 + 4) * S)
    img = np.zeros((D, D), int)
    c = D / 2
    ramp = [col((30, 31, 57)), col((53, 57, 89)), col((83, 92, 130)), col((127, 142, 187)), col((180, 194, 232))]
    for y in range(D):
        for x in range(D):
            dx, dy = (x - c) / S, (y - c) / S
            e = math.hypot(dx / 1.0, dy / 1.15)
            if e > R:
                continue
            if e > R - 1.0:
                img[y, x] = OUT
                continue
            v = (-dx * 0.4 - dy * 0.9) / R
            k = 0 if v < -0.5 else 1 if v < -0.1 else 2 if v < 0.3 else 3 if v < 0.6 else 4
            if abs(dy) < 2.6 and abs(dx - 1.0) < 0.3:
                k = 0  # nudillos
            if abs(dy - 1.6) < 0.3 and dx > -2:
                k = 0
            img[y, x] = ramp[k]
    small = mode_down(img, S)
    return Part('fist', small, (c / S, c / S), 0.0, 0)


def mode_down(img, S):
    H, W = img.shape[0] // S, img.shape[1] // S
    out = np.zeros((H, W), int)
    for y in range(H):
        for x in range(W):
            blk = img[y * S:(y + 1) * S, x * S:(x + 1) * S].ravel()
            op = blk[blk > 0]
            if len(op) * 2 < len(blk):
                continue
            vals, cnt = np.unique(op, return_counts=True)
            w = cnt * np.where(vals == OUT, 1.3, 1.0)
            out[y, x] = vals[w.argmax()]
    return out


_CACHE = None


def get_parts():
    global _CACHE
    if _CACHE is None:
        _CACHE = build()
    return _CACHE


if __name__ == '__main__':
    P = build()
    pal = PL.get()
    # hoja de contacto de las partes
    tiles = []
    for k, p in P.items():
        im = Image.fromarray(pal[p.img].astype(np.uint8))
        im = im.resize((p.img.shape[1] * 4, p.img.shape[0] * 4), Image.NEAREST)
        d = ImageDraw.Draw(im)
        x, y = p.pivot[0] * 4, p.pivot[1] * 4
        d.ellipse([x - 3, y - 3, x + 3, y + 3], outline=(0, 255, 0))
        tiles.append(im)
    Wt = sum(t.width for t in tiles) + 10 * len(tiles)
    Ht = max(t.height for t in tiles)
    sheet = Image.new('RGB', (Wt, Ht), (255, 0, 255))
    x = 0
    for t in tiles:
        sheet.paste(t, (x, 0))
        x += t.width + 10
    sheet.save(os.path.join(BUILD, 'parts_sheet.png'))
    print({k: (v.img.shape, v.pivot, round(v.rest, 1), round(v.length, 1)) for k, v in P.items()})
