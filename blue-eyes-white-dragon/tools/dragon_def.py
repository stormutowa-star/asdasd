"""
Definicion del Dragon Blanco de Ojos Azules (modelo + pintura de textura).
Coordenadas "naturales" (x lateral, y arriba, z adelante); ver mcmodel.py.
"""
import math
import numpy as np
from mcmodel import Model

# ------------------------------------------------------------------ paleta (sacada de la carta)
HI = (232, 251, 254)   # casi blanco
LT = (176, 236, 249)   # cian claro
MD = (100, 208, 234)    # cian
DK = (50, 150, 192)    # turquesa
DD = (22, 84, 120)     # azul profundo
BK = (7, 28, 50)       # casi negro azulado
MEM_LT = (120, 190, 222)
MEM_MD = (52, 120, 172)
MEM_DK = (16, 46, 88)
MAROON = (48, 10, 24)
TONGUE = (104, 26, 44)
EYE = (36, 76, 255)
EYE_HI = (190, 215, 255)


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def mul(c, k):
    return tuple(max(0, min(255, v * k)) for v in c)


def noise(x, y, z, salt=0):
    h = (int(math.floor(x * 2 + 1000)) * 73856093) ^ (int(math.floor(y * 2 + 1000)) * 19349663) ^ \
        (int(math.floor(z * 2 + 1000)) * 83492791) ^ (salt * 2654435761)
    h &= 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xFFFFFFFF
    h ^= h >> 15
    return (h & 0xFFFF) / 65535.0


# ------------------------------------------------------------------ forma del ala (plano X=envergadura, Z=cuerda hacia atras)
WRIST = (36.0, 1.0)
TIPS = [(58.0, 9.0), (54.0, 29.0), (42.0, 43.0), (26.0, 46.0)]
BODY_ATTACH = (3.0, 33.0)


def _wing_polygon():
    pts = [(0.0, 0.0), WRIST, TIPS[0]]
    chain = TIPS + [BODY_ATTACH]
    for a, b in zip(chain[:-1], chain[1:]):
        for k in range(1, 8):
            t = k / 8.0
            px = a[0] + (b[0] - a[0]) * t
            pz = a[1] + (b[1] - a[1]) * t
            # el borde se hunde hacia la muneca (festoneado de murcielago)
            dx, dz = WRIST[0] - px, WRIST[1] - pz
            L = math.hypot(dx, dz)
            depth = 6.5 * math.sin(math.pi * t)
            pts.append((px + dx / L * depth, pz + dz / L * depth))
        pts.append(b)
    return pts


WING_POLY = _wing_polygon()


def _inside(poly, x, z):
    inside = False
    n = len(poly)
    j = n - 1
    for i in range(n):
        xi, zi = poly[i]
        xj, zj = poly[j]
        if (zi > z) != (zj > z) and x < (xj - xi) * (z - zi) / (zj - zi + 1e-12) + xi:
            inside = not inside
        j = i
    return inside


def _dist_edge(poly, x, z):
    best = 1e9
    n = len(poly)
    for i in range(n):
        ax, az = poly[i]
        bx, bz = poly[(i + 1) % n]
        best = min(best, _seg_dist(x, z, ax, az, bx, bz))
    return best


def _seg_dist(px, pz, ax, az, bx, bz):
    dx, dz = bx - ax, bz - az
    L2 = dx * dx + dz * dz
    t = 0 if L2 == 0 else max(0, min(1, ((px - ax) * dx + (pz - az) * dz) / L2))
    return math.hypot(px - (ax + t * dx), pz - (az + t * dz))


def wing_pixel(X, Z):
    """Color RGBA de la membrana en el plano del ala. None = transparente."""
    if not _inside(WING_POLY, X, Z):
        return None
    d_edge = _dist_edge(WING_POLY, X, Z)
    # huesos (dedos) desde la muneca
    d_bone = min(_seg_dist(X, Z, WRIST[0], WRIST[1], t[0], t[1]) for t in TIPS)
    # tambien un hueso fino a lo largo del borde delantero
    d_lead = abs(Z - 0.8) if X <= WRIST[0] else 99
    if d_bone < 1.0 or d_lead < 1.6:
        return LT_BONE
    # membrana: oscura cerca de los dedos y del borde de ataque, clara hacia el borde de fuga
    near = min(d_bone, d_lead + 3)
    c = lerp(MEM_DK, MEM_LT, near / 11.0)
    c = lerp(c, MEM_MD, 0.25)
    if d_edge < 1.0:
        c = lerp(c, BK, 0.55)
    # veteado muy suave
    c = mul(c, 0.94 + 0.12 * noise(X, Z, 0, 7))
    return tuple(int(v) for v in c)


LT_BONE = (206, 240, 250)


# ------------------------------------------------------------------ pintura
def eye_pixel(cube, face, rel):
    """rel = posicion natural relativa al centro del cubo. Devuelve (rgb, glow) o None."""
    eye = cube.opts.get('eye')
    if not eye or face not in ('WEST', 'EAST'):
        return None
    ez, ey = eye                       # centro del ojo (z adelante, y arriba)
    dz = rel[2] - ez
    dy = rel[1] - ey
    # forma de ojo rasgado: mas largo que alto, inclinado hacia abajo-adelante
    dz2 = dz + dy * 0.8
    if abs(dz2) <= 2.55 and abs(dy) <= 1.55:
        if abs(dz2) <= 1.55 and abs(dy) <= 1.05:
            return (EYE_HI if (dz > 0.3 and dy > 0.1) else EYE), True
        return (20, 44, 190), True
    if abs(dz2) <= 3.1 and 1.55 < dy <= 2.6:      # ceja
        return DD, False
    return None


def eye_plate_pixel(cube, face, rel):
    """Ojo almendrado azul en la cara exterior de una placa fina. Devuelve (rgb, glow) o None."""
    side = cube.opts.get('side', 1)
    if face != ('EAST' if side > 0 else 'WEST'):
        return None
    u = rel[2] / (cube.d / 2.0)      # -1 atras .. 1 delante
    v = rel[1] / (cube.h / 2.0)      # -1 abajo .. 1 arriba
    # almendra inclinada: punta delantera mas baja
    vv = v + u * 0.35
    shape = (u * u) + (vv * 1.9) ** 2
    if shape <= 0.55:
        if u > 0.05 and vv > 0.0:
            return EYE_HI, True
        return EYE, True
    if shape <= 0.95:
        return (16, 34, 150), True
    if shape <= 1.35 or vv > 0.55:
        return BK, False
    return None


def smooth_pixel(cube, face, rel, normal, edge, n):
    """Piel lisa sombreada como el dibujo de la carta (sin ladrillos): claro arriba, turquesa a los lados, azul oscuro abajo."""
    o = cube.opts
    up = -normal[1]
    t = (rel[1] + cube.h / 2.0) / max(1.0, cube.h)       # 0 abajo .. 1 arriba
    if up > 0.5:
        c = lerp(LT, HI, 0.75)
    elif up < -0.5:
        c = lerp(DD, DK, 0.35)
        if o.get('roof'):
            c = mul(MAROON, 0.85)
        if o.get('mouth'):
            pass
    else:
        # lados: brillo en la parte alta, sombra fuerte abajo (estilo anime de la carta)
        if t > 0.72:
            c = lerp(LT, HI, (t - 0.72) / 0.28 * 0.8)
        elif t > 0.35:
            c = lerp(MD, LT, (t - 0.35) / 0.37)
        else:
            c = lerp(DD, MD, t / 0.35)
        if o.get('lip') and t < 0.16:
            c = BK
    if o.get('mouth') and up > 0.5:
        c = MAROON
        if abs(rel[0]) < 1.2:
            c = TONGUE
    c = mul(c, 0.97 + 0.05 * n)
    if o.get('ink') and edge == 0 and up < 0.5:
        c = lerp(c, BK, 0.45)
    return tuple(int(v) for v in c) + (255,)


def paint(cube, face, pos, normal, edge):
    m = cube.mat
    o = cube.opts
    # centro del cubo (modelo) y posicion relativa natural
    cxm = cube.x0 + cube.w / 2.0
    cym = cube.y0 + cube.h / 2.0
    czm = cube.z0 + cube.d / 2.0
    rel = (pos[0] - cxm, -(pos[1] - cym), -(pos[2] - czm))     # natural: (x, y arriba, z adelante)
    up = -normal[1]            # >0 si la cara mira hacia ARRIBA (visual)
    front = -normal[2]         # >0 si la cara mira hacia ADELANTE
    n = noise(pos[0], pos[1], pos[2], hash(face) & 255)

    if m == 'membrane':
        s = o['side']
        if face in ('UP', 'DOWN'):
            X = o['x_off'] + (pos[0] * s)
            Z = pos[2] + o.get('z_off', 0.0)
            # el cubo esta construido con el borde delantero en z=z0
            Z = pos[2] - cube.z0
            c = wing_pixel(X, Z)
            if c is None:
                return (0, 0, 0, 0)
            return c + (255,)
        # canto fino de 1px: casi no se ve; transparente para no dibujar un marco
        return (0, 0, 0, 0)

    if m == 'eye':
        ep = eye_plate_pixel(cube, face, rel)
        if ep is not None:
            return tuple(int(v) for v in ep[0]) + (255,)
        m = 'smooth'

    if m in ('smooth', 'smooth_jaw'):
        return smooth_pixel(cube, face, rel, normal, edge, n)

    if m == 'tooth':
        t = (rel[1] + cube.h / 2.0) / max(1.0, cube.h)
        c = lerp((205, 232, 240), (255, 255, 255), 1 - t if o.get('down', True) else t)
        return tuple(int(v) for v in c) + (255,)

    # ---- color base segun material
    if m == 'claw':
        axis, sgn = o['tip']
        L = {'x': cube.w, 'y': cube.h, 'z': cube.d}[axis]
        k = {'x': rel[0], 'y': rel[1], 'z': rel[2]}[axis] * sgn
        t = (k + L / 2.0) / max(1.0, L)
        c = lerp(DK, HI, t * 1.1)
        if edge == 0:
            c = lerp(c, BK, 0.55)
        return tuple(int(v) for v in c) + (255,)

    if m == 'horn':
        axis, sgn = o['tip']
        L = {'x': cube.w, 'y': cube.h, 'z': cube.d}[axis]
        k = {'x': rel[0], 'y': rel[1], 'z': rel[2]}[axis] * sgn
        t = (k + L / 2.0) / max(1.0, L)
        c = lerp(lerp(LT, DK, 0.35), HI, t)
        if edge == 0:
            c = lerp(c, BK if o.get('ink') else DD, 0.5 if o.get('ink') else 0.4)
        return tuple(int(v) for v in c) + (255,)

    if m == 'bone':
        c = lerp(LT_BONE, LT, 0.4 + 0.3 * n)
        if edge == 0:
            c = lerp(c, DK, 0.5)
        return tuple(int(v) for v in c) + (255,)

    if m == 'mouth_top':     # mandibula inferior: cara de arriba = interior de la boca
        if up > 0.5:
            c = MAROON
            if abs(rel[0]) < 1.6:
                c = TONGUE
            return tuple(int(v) for v in c) + (255,)
    if m == 'mouth_roof' or (m == 'skull' and o.get('roof')):
        if up < -0.5:
            return tuple(int(v) for v in mul(MAROON, 0.8 + 0.2 * n)) + (255,)
    if m == 'throat':
        return tuple(int(v) for v in mul(MAROON, 0.45)) + (255,)

    # ---- piel de escamas (base)
    a, b = {'x': (pos[2], pos[1]), 'y': (pos[0], pos[2]), 'z': (pos[0], pos[1])}[
        'x' if abs(normal[0]) > 0.5 else ('y' if abs(normal[1]) > 0.5 else 'z')]
    row = math.floor(b / 3.0)
    col = math.floor((a + (row % 2) * 2.0) / 4.0)
    fa = (a + (row % 2) * 2.0) / 4.0 - col
    fb = b / 3.0 - row
    base = MD
    if up > 0.5:
        base = LT
    elif up < -0.5:
        base = lerp(DK, MD, 0.35)
    else:
        # gradiente vertical en las caras laterales
        t = (rel[1] + cube.h / 2.0) / max(1.0, cube.h)
        base = lerp(MD, LT, t * 0.9)
    c = base
    # escamas: lineas oscuras y brillo arriba-izquierda
    if fb < 0.2 or fa < 0.12:
        c = lerp(c, DK, 0.12)
    elif fb > 0.62 and fa > 0.2:
        c = lerp(c, HI, 0.08)
    c = mul(c, 0.94 + 0.1 * n)

    if m == 'belly' and front > 0.5:
        # placas del vientre: bandas claras con linea oscura (como en la carta)
        band = (rel[1] + cube.h / 2.0)
        k = band % 4.0
        edge_fall = min(abs(rel[0] + cube.w / 2.0), abs(rel[0] - cube.w / 2.0))
        c = lerp(HI, LT, 0.15 + 0.25 * (k / 4.0))
        if k < 1.0:
            c = lerp(DK, MD, 0.5)
        if edge_fall < 2.0:
            c = lerp(c, MD, 0.6)
        c = mul(c, 0.96 + 0.08 * n)
    if m == 'belly' and up < -0.5:
        c = lerp(c, HI, 0.5)

    if m == 'skull':
        e = eye_pixel(cube, face, rel)
        if e:
            return tuple(int(v) for v in e[0]) + (255,)
        # ceja oscura sobre el ojo y fosas nasales
        if abs(normal[0]) > 0.5 and 'eye' in o:
            ez, ey = o['eye']
            if abs(rel[2] - ez) < 4 and 2.0 < rel[1] - ey < 3.1:
                c = lerp(c, DD, 0.7)
        nost = o.get('nostril')
        if nost and up > 0.5:
            nz, nx = nost
            if abs(abs(rel[0]) - nx) < 0.9 and abs(rel[2] - nz) < 0.9:
                c = BK
        if front > 0.5 and o.get('snout_tip'):
            c = lerp(c, HI, 0.25)

    if m in ('dark',):
        c = lerp(DK, DD, 0.5 + 0.4 * n)

    if o.get('ink') and edge == 0:
        c = lerp(c, BK, 0.55)
    elif o.get('outline', True) and edge == 0 and min(cube.w, cube.h, cube.d) >= 3:
        c = lerp(c, DD, 0.30)
    return tuple(int(v) for v in c) + (255,)


def paint_glow(cube, face, pos, normal, edge):
    """Textura emisiva: solo los ojos."""
    if cube.mat not in ('skull', 'eye'):
        return (0, 0, 0, 0)
    cxm = cube.x0 + cube.w / 2.0
    cym = cube.y0 + cube.h / 2.0
    czm = cube.z0 + cube.d / 2.0
    rel = (pos[0] - cxm, -(pos[1] - cym), -(pos[2] - czm))
    e = eye_plate_pixel(cube, face, rel) if cube.mat == 'eye' else eye_pixel(cube, face, rel)
    if e and e[1]:
        return tuple(int(v) for v in e[0]) + (255,)
    return (0, 0, 0, 0)


# ------------------------------------------------------------------ el modelo
HEAD_SCALE = 1.3


def build():
    M = Model(512, 512)
    root = M.root

    # ---------------- pelvis y torso
    pelvis = M.part('pelvis', root, (0, 34, 3))
    pelvis.cube(24, 16, 22, (0, 0, 0), 'belly')
    pelvis.cube(20, 20, 18, (0, 0, 0), 'belly')

    chest = M.part('chest', pelvis, (0, 5, -5), (-0.30, 0, 0))
    chest.cube(30, 24, 24, (0, 12, -1), 'belly')
    chest.cube(24, 28, 26, (0, 12, -1), 'belly')
    # placas dorsales del torso
    for i, (zz, hh) in enumerate([(-9, 6), (-4, 7)]):
        pass

    # ---------------- cuello en S (4 segmentos gruesos) - cabeza arriba a la derecha de la carta
    neck_defs = [  # (w, h, d, rotX): 7 tramos cortos -> curva en S suave
        (20, 9, 18, -0.62),
        (19, 9, 17, 0.10),
        (18, 9, 17, 0.30),
        (18, 9, 16, 0.38),
        (17, 9, 16, 0.40),
        (16, 9, 15, 0.38),
        (15, 9, 15, 0.30),
    ]
    prev = chest
    py = 24.0
    pz = -2.0
    for i, (w, h, d, rx) in enumerate(neck_defs):
        nk = M.part('neck%d' % (i + 1), prev, (0, py, pz), (rx, 0, 0))
        nk.cube(w, h, d, (0, h / 2.0 - 1, 0), 'belly')
        nk.cube(w - 4, h, d + 2, (0, h / 2.0 - 1, 0), 'belly')      # bisel
        if i % 2 == 0:
            nk.cube(3, 4, 5, (0, h, d / 2.0 - 2.0), 'horn', tip=('y', 1), outline=False)
        prev = nk
        py = h - 2.5
        pz = 0.0

    # ---------------- cabeza curva, como la carta: se construye por "rebanadas" inclinadas
    # que siguen el perfil (frente que baja en curva hacia un hocico largo), con aristas biseladas.
    head = M.part('head', prev, (0, 9, 1.0), (-0.65, 0, 0))
    head.scale = HEAD_SCALE

    def smoothstep(e0, e1, x):
        x = max(0.0, min(1.0, (x - e0) / (e1 - e0)))
        return x * x * (3 - 2 * x)

    def loft(parent, prefix, z0, z1, n, prof, mat, **opts):
        step = (z1 - z0) / n
        pts = []
        for i in range(n + 1):
            pts.append(prof(i / n))
        for i in range(n):
            t = (i + 0.5) / n
            yb, yt, w = prof(t)
            # pendiente de la linea media para inclinar la rebanada
            ya = (pts[i][0] + pts[i][1]) / 2.0
            yb2 = (pts[i + 1][0] + pts[i + 1][1]) / 2.0
            ang = -math.atan2(yb2 - ya, step)
            h = max(2, int(round(yt - yb)))
            w = max(2, int(round(w)))
            d = int(math.ceil(step)) + 1
            sl = parent and M.part('%s%d' % (prefix, i), parent, (0, (yb + yt) / 2.0, z0 + step * (i + 0.5)), (ang, 0, 0))
            core_h = max(1, h - 3)
            sl.cube(w, core_h, d, (0, 0, 0), mat, **opts)
            if w > 4:
                sl.cube(w - 4, h, d, (0, 0, 0), mat, **opts)
        return pts

    def head_prof(t):
        yb = 1.5 + 1.0 * t
        yt = 16.0 - 5.5 * t ** 1.3 - 2.5 * smoothstep(0.80, 1.0, t)
        w = 16.0 - 7.0 * t ** 1.2
        return yb, yt, w

    loft(head, 'skull', -4.0, 37.0, 14, head_prof, 'smooth', roof=True, lip=True)
    # punta redondeada del hocico
    head.cube(6, 3, 3, (0, 4.2, 37.8), 'smooth')
    # fosas nasales
    for sx in (-1, 1):
        head.cube(1, 1, 2, (sx * 1.6, 6.6, 35.0), 'dark')
    # ojos almendrados (placas finas sobre la piel) y cejas que los cubren
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        ep = M.part('eye' + s, head, (sx * 6.6, 9.6, 8.5), (0.10, sx * 0.20, -sx * 0.10))
        ep.cube(1, 4, 7, (sx * 0.4, 0, 0), 'eye', side=sx)
        br = M.part('brow' + s, head, (sx * 5.6, 12.4, 9.0), (0.38, -sx * 0.22, sx * 0.30))
        br.cube(4, 3, 13, (0, 0, -2.0), 'horn', tip=('z', 1), ink=True)
        # mejilla curva tras el ojo
        ck = M.part('cheekPlate' + s, head, (sx * 7.4, 6.0, 2.0), (0.0, sx * 0.25, 0))
        ck.cube(2, 7, 9, (0, 0, 0), 'smooth', ink=True)
    # dientes: hileras largas y finas como en la carta + colmillos
    for i in range(9):
        zz = 10.0 + i * 3.0
        yb, yt, w = head_prof((zz + 4.0) / 41.0)
        ln = 4 if i % 2 == 0 else 3
        for sx in (-1, 1):
            tp = M.part('toothU%d%s' % (i, 'R' if sx < 0 else 'L'), head, (sx * (w / 2.0 - 1.3), yb + 0.5, zz), (-0.25, 0, 0))
            tp.cube(1, ln, 1, (0, -ln / 2.0, 0), 'tooth')
    for sx in (-1, 1):
        tp = M.part('fangU' + ('R' if sx < 0 else 'L'), head, (sx * 2.2, 2.4, 35.0), (-0.2, 0, 0))
        tp.cube(1, 5, 1, (0, -2.5, 0), 'tooth')
    head.cube(10, 4, 8, (0, 0.5, 6.0), 'throat')

    # corona de espinas curvas (3 tramos cada una) que irradian hacia atras
    crown = [  # (x, y, z, rotX, rotY, largo1, curva)
        (4.0, 15.0, 1.5, 0.80, 0.18, 12, 0.22),
        (6.5, 12.0, 0.0, 0.35, 0.50, 11, 0.18),
        (7.5, 7.5, 0.5, 0.00, 0.75, 9, 0.15),
    ]
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        for i, (x, y, z, rx, ry, L, cv) in enumerate(crown):
            prev_p = M.part('spike%d%s' % (i, s), head, (sx * x, y, z), (rx, sx * ry, 0))
            prev_p.cube(4, 4, L, (0, 0, -L / 2.0), 'horn', tip=('z', -1), ink=True)
            L2 = int(L * 0.75)
            p2 = M.part('spikeB%d%s' % (i, s), prev_p, (0, 0, -L + 0.5), (cv, sx * 0.05, 0))
            p2.cube(3, 3, L2, (0, 0, -L2 / 2.0), 'horn', tip=('z', -1), ink=True)
            L3 = int(L * 0.55)
            p3 = M.part('spikeC%d%s' % (i, s), p2, (0, 0, -L2 + 0.5), (cv, sx * 0.05, 0))
            p3.cube(2, 2, L3, (0, 0, -L3 / 2.0), 'horn', tip=('z', -1), ink=True)
    c0 = M.part('crest0', head, (0, 15.5, 4.0), (1.15, 0, 0))
    c0.cube(3, 3, 11, (0, 0, -5.5), 'horn', tip=('z', -1), ink=True)
    c1 = M.part('crest1', c0, (0, 0, -10.5), (0.25, 0, 0))
    c1.cube(2, 2, 8, (0, 0, -4.0), 'horn', tip=('z', -1), ink=True)

    # mandibula inferior curva (abierta en reposo, como la carta)
    jaw = M.part('jaw', head, (0, 2.0, 0.0), (0.72, 0, 0))

    def jaw_prof(t):
        yt = 0.0
        yb = -6.0 + 3.5 * t ** 0.9
        w = 13.0 - 6.5 * t
        return yb, yt, w

    loft(jaw, 'jawSeg', 0.0, 35.0, 10, jaw_prof, 'smooth', mouth=True)
    for i in range(9):
        zz = 8.5 + i * 2.9
        yb, yt, w = jaw_prof(zz / 35.0)
        ln = 4 if i % 2 == 1 else 3
        for sx in (-1, 1):
            tp = M.part('toothD%d%s' % (i, 'R' if sx < 0 else 'L'), jaw, (sx * (w / 2.0 - 1.3), -0.5, zz), (0.25, 0, 0))
            tp.cube(1, ln, 1, (0, ln / 2.0, 0), 'tooth', down=False)
    for sx in (-1, 1):
        tp = M.part('fangD' + ('R' if sx < 0 else 'L'), jaw, (sx * 2.0, -0.5, 33.0), (0.2, 0, 0))
        tp.cube(1, 4, 1, (0, 2.0, 0), 'tooth', down=False)
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        jp = M.part('jawSpike' + s, jaw, (sx * 5.5, -2.5, 3.0), (-0.25, sx * 0.55, 0))
        jp.cube(2, 2, 9, (0, 0, -4.5), 'horn', tip=('z', -1), ink=True)

    # ---------------- cola (7 segmentos, se curva hacia arriba)
    tail_defs = [  # (w,h,d, rotX)  rotX>0 -> sube la punta
        (18, 14, 14, -0.05),
        (16, 12, 13, 0.20),
        (14, 11, 13, 0.32),
        (12, 10, 12, 0.42),
        (10, 8, 12, 0.48),
        (8, 6, 11, 0.50),
        (5, 4, 10, 0.45),
    ]
    prev = pelvis
    tz = -10.0
    for i, (w, h, d, rx) in enumerate(tail_defs):
        tp = M.part('tail%d' % (i + 1), prev, (0, 0 if i else 0.5, tz), (rx, -0.14 if i else -0.2, 0))
        tp.cube(w, h, d, (0, 0, -d / 2.0 + 0.5), 'belly' if i < 5 else 'scale')
        if i < 6:
            tp.cube(2, 4 if i < 4 else 3, 5, (0, h / 2.0 + 1.0, -d / 2.0), 'horn', tip=('y', 1), outline=False)
        prev = tp
        tz = -d + 1.0
    # punta de lanza
    tt = M.part('tailtip', prev, (0, 0, -9.0), (0.25, 0, 0))
    tt.cube(3, 3, 9, (0, 0, -3), 'horn', tip=('z', -1), outline=False)

    # ---------------- piernas
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        thigh = M.part('thigh' + s, pelvis, (sx * 13.0, -3, 0), (-0.55, 0, 0))
        thigh.cube(14, 16, 15, (0, -6.5, 0), 'scale')
        shin = M.part('shin' + s, thigh, (0, -13, 0.5), (1.05, 0, 0))
        shin.cube(11, 15, 11, (0, -6.5, 0), 'scale')
        foot = M.part('foot' + s, shin, (0, -13.5, 0), (-0.5, 0, 0))
        foot.cube(13, 5, 13, (0, -1.5, 3.0), 'scale')
        for i, tx in enumerate((-4.0, 0, 4.0)):
            fp = M.part('toe%d%s' % (i, s), foot, (tx, -2.0, 7.8), (0.45, tx * 0.05, 0))
            fp.cube(3, 3, 8, (0, 0, 4), 'claw', tip=('z', 1))
        hp = M.part('heel' + s, foot, (0, -1.5, -2.5), (0.5, 0, 0))
        hp.cube(2, 2, 5, (0, 0, -2.5), 'claw', tip=('z', -1))

    # ---------------- brazos con garras (alzados hacia delante como en la carta)
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        up = M.part('arm' + s, chest, (sx * 17.0, 19, -6.0), (-0.95, sx * 0.25, sx * 0.20))
        up.cube(11, 17, 11, (0, -6.5, 0), 'scale')
        lo = M.part('forearm' + s, up, (0, -13.5, 0), (-0.75, 0, 0))
        lo.cube(10, 15, 10, (0, -6.0, 0), 'scale')
        hand = M.part('hand' + s, lo, (0, -13.0, 0), (1.05, 0, 0))
        hand.cube(13, 6, 12, (0, -1.5, 0), 'scale')
        for i, tx in enumerate((-4.4, 0.0, 4.4)):
            cl = M.part('claw%d%s' % (i, s), hand, (tx, -4.5, 0.0), (0.30, tx * 0.04, 0))
            cl.cube(4, 6, 4, (0, -2.5, 0), 'scale')
            cl2 = M.part('claw2%d%s' % (i, s), cl, (0, -5.5, 0), (0.45, 0, 0))
            cl2.cube(3, 12, 3, (0, -5.5, 0), 'claw', tip=('y', -1))
        th = M.part('thumb' + s, hand, (sx * 6.5, -1.5, 2.5), (0.5, 0, sx * 0.5))
        th.cube(3, 10, 3, (0, -4.5, 0), 'claw', tip=('y', -1))

    # ---------------- alas
    for sx in (-1, 1):
        s = 'R' if sx < 0 else 'L'
        raise_ = 1.25
        sweep = 0.30
        wr = M.part('wing' + s, chest, (sx * 12.0, 24, 9.0), (-0.50, -sx * sweep, -sx * raise_))
        # huesos: humero + antebrazo
        wr.cube(18, 6, 6, (sx * 9.0, 0, 0), 'bone', outline=False)
        wr.cube(19, 5, 5, (sx * 27.0, 0, 0), 'bone', outline=False)
        # membrana interior
        wr.cube(36, 1, 48, (sx * 18.0, 0.0, -24.0), 'membrane', side=sx, x_off=0.0)
        wo = M.part('wingOut' + s, wr, (sx * 36.0, 0, 0), (0.0, -sx * 0.10, -sx * 0.18))
        wo.cube(22, 1, 48, (sx * 11.0, 0.0, -24.0), 'membrane', side=sx, x_off=36.0)
        wo.cube(22, 4, 4, (sx * 11.0, 0, -1.0), 'bone', outline=False)
        # puntas de los dedos (garra del ala)
        wo.cube(3, 5, 3, (sx * 1.0, 3.5, -0.5), 'claw', tip=('y', 1))

    return M


def mouth_anchor_info(M):
    """Posicion (en bloques, espacio de entidad con y=0 en los pies, +z delante) de la punta de la boca con la pose de reposo."""
    return None
