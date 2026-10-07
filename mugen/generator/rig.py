"""Paso 3: esqueleto 2D. Recibe una pose (angulos/posiciones), coloca cada
parte con FK/IK y la rasteriza a la escala final con supermuestreo +
voto por moda, para que el resultado siga siendo pixel-art de paleta."""
import math
import numpy as np
import parts as PT
import palette as PL

P = PT.get_parts()
SCALE = 0.75          # px finales por px nativo (personaje ~100 px de alto)
SS = 4                # supermuestreo
CW, CH = 420, 340     # lienzo de trabajo (px finales)
AX, AY = 210, 270     # eje (pies en el suelo) dentro del lienzo
OUT = PT.OUT

DARK = PL.shade_map(0.70)
DARK2 = PL.shade_map(0.82)
_W = np.ones(256)
_lum = PL.get() @ [0.3, 0.59, 0.11]
_W[_lum < 25] = 1.4   # proteger contornos al reducir

THIGH_L = P['thigh'].length
SHIN_L = P['shin'].length
UPPER_L = PT.UPPER_LEN
FORE_L = PT.FORE_LEN
LIMB_DRAW = P['fore'].length       # largo dibujado del antebrazo
GRIP_SPAN = P['sword'].grip_far
PELVIS_TO_GROUND = PT.GROUND_T - PT.PELVIS[1]


def rot(a):
    r = math.radians(a)
    c, s = math.cos(r), math.sin(r)
    return np.array([[c, -s], [s, c]])


def vdir(a):
    r = math.radians(a)
    return np.array([math.cos(r), math.sin(r)])


def angle_of(v):
    return math.degrees(math.atan2(v[1], v[0]))


def ik2(base, target, l1, l2, pref):
    """IK de dos huesos. Devuelve (ang1, ang2, estiramiento)."""
    base = np.asarray(base, float)
    target = np.asarray(target, float)
    d = target - base
    dl = float(np.hypot(*d))
    stretch = 1.0
    if dl >= (l1 + l2) * 0.999:
        stretch = min(1.25, dl / ((l1 + l2) * 0.999))
        l1 *= stretch
        l2 *= stretch
    dl = max(abs(l1 - l2) + 0.01, min(dl, (l1 + l2) * 0.999))
    phi = math.atan2(d[1], d[0])
    ca = (l1 * l1 + dl * dl - l2 * l2) / (2 * l1 * dl)
    a = math.acos(max(-1, min(1, ca)))
    best = None
    for sgn in (1, -1):
        a1 = phi + sgn * a
        elbow = base + l1 * np.array([math.cos(a1), math.sin(a1)])
        score = float(np.dot(elbow - base, pref))
        if best is None or score > best[0]:
            best = (score, a1, elbow)
    _, a1, elbow = best
    a2 = math.atan2(target[1] - elbow[1], target[0] - elbow[0])
    return math.degrees(a1), math.degrees(a2), stretch


class Placement:
    def __init__(self, part, pivot_world, angle, smap=None, sx=1.0, tag=''):
        self.part = part
        self.pw = np.asarray(pivot_world, float)
        self.A = rot(angle) @ np.diag([sx, 1.0])
        self.smap = smap
        self.tag = tag


def solve(pose):
    """pose -> lista ordenada de Placement (fondo -> frente) + puntos utiles."""
    g = dict(pose)
    root = np.array(g.get('root', (0, -PELVIS_TO_GROUND)), float)
    ta = g.get('torso', 0.0)
    R = rot(ta)

    def tp(p):
        return root + R @ (np.array(p, float) - np.array(PT.PELVIS))

    pts = {'root': root}
    out = {}

    # ---- piernas
    legs = g.get('legs', {})
    for side, hip_ref in (('near', PT.HIP_NEAR), ('far', PT.HIP_FAR)):
        L = legs.get(side, {})
        hip = tp(hip_ref)
        if 'ankle' in L:
            ank = np.array(L['ankle'], float)
            if L.get('world') and g.get('spin'):
                c0 = root + np.array(g.get('spin_c', (0, 0)), float)
                ank = c0 + rot(-g['spin']) @ (ank - c0)
            kb = L.get('knee', 1)
            a1, a2, st = ik2(hip, ank, THIGH_L, SHIN_L, np.array([kb * 1.0, 0.15]))
        else:
            a1, a2 = L.get('thigh', 90), L.get('shin', 95)
        knee = hip + THIGH_L * vdir(a1)
        ankle = knee + SHIN_L * vdir(a2)
        fa = L.get('foot', 0.0)
        smap = DARK if side == 'far' else None
        out[side + '_thigh'] = Placement(P['thigh'], hip, a1 - P['thigh'].rest, smap, tag='leg')
        out[side + '_shin'] = Placement(P['shin'], knee, a2 - P['shin'].rest, smap, tag='leg')
        fp = P['foot']
        out[side + '_foot'] = Placement(fp, ankle, fa, smap, tag='leg')
        pts[side + '_ankle'] = ankle
        pts[side + '_knee'] = knee

    # ---- torso, cabeza, hombrera
    out['torso'] = Placement(P['torso'], root, ta)
    neck = tp(PT.NECK)
    out['head'] = Placement(P['head'], neck, ta + g.get('head', 0.0))
    pts['neck'] = neck
    pts['head_top'] = neck + rot(ta + g.get('head', 0.0)) @ np.array([0, -24.0])

    # ---- espada
    sw = g.get('sword', {})
    sang = sw.get('ang', PT.SWORD_ANG)
    grip = root + np.array(sw.get('grip', (6, -6)), float)
    far_grip = grip + GRIP_SPAN * vdir(sang)
    out['sword'] = Placement(P['sword'], grip, sang - P['sword'].rest, tag='sword')
    pts['grip'] = grip
    pts['far_grip'] = far_grip
    pts['tip'] = grip + (P['sword'].blade_len) * vdir(sang)
    pts['guard'] = grip + PT.GRIP_NEAR * vdir(sang)
    pts['sword_ang'] = sang

    # ---- brazos
    arms = g.get('arms', {})
    for side, sh_ref in (('near', PT.SH_NEAR), ('far', PT.SH_FAR)):
        A = arms.get(side, {})
        sh = tp(sh_ref)
        if 'hand' in A:
            hand = root + np.array(A['hand'], float)
        else:
            hand = grip if side == 'near' else far_grip
        pref = np.array(A.get('elbow', (-0.4, 1.0) if side == 'near' else (0.5, 1.0)), float)
        a1, a2, st = ik2(sh, hand, UPPER_L, FORE_L, pref)
        elbow = sh + UPPER_L * st * vdir(a1)
        smap = DARK2 if side == 'far' else None
        out[side + '_upper'] = Placement(P['upper'], sh, a1, smap, sx=st)
        out[side + '_fore'] = Placement(P['fore'], elbow, a2, smap, sx=st)
        fist_ang = A.get('fist', a2)
        out[side + '_fist'] = Placement(P['fist'], hand, fist_ang, smap)
        pts[side + '_hand'] = hand
        pts[side + '_shoulder'] = sh
        pts[side + '_stretch'] = st
        pts[side + '_upper_ang'] = a1
    pa = g.get('pauldron', 0.0) + 0.25 * max(-60, min(60, pts['near_upper_ang'] - 100))
    out['pauldron'] = Placement(P['pauldron'], tp(PT.SH_NEAR), ta + pa)

    order = ['far_upper', 'far_fore', 'SWORD_BACK', 'far_thigh', 'far_shin', 'far_foot',
             'near_thigh', 'near_shin', 'near_foot', 'torso', 'head', 'SWORD_MID',
             'far_fist', 'near_upper', 'near_fore', 'near_fist', 'pauldron', 'SWORD_FRONT']
    layer = g.get('sword_layer', 'mid')
    if g.get('far_fist_back'):
        order.remove('far_fist')
        order.insert(order.index('torso'), 'far_fist')
    seq = []
    for k in order:
        if k.startswith('SWORD_'):
            if k[6:].lower() == layer:
                seq.append(out['sword'])
        else:
            seq.append(out[k])
    if g.get('no_sword'):
        seq = [s for s in seq if s.tag != 'sword']
    spin = g.get('spin', 0.0)
    if spin:
        c = root + np.array(g.get('spin_c', (0, 0)), float)
        Rs = rot(spin)
        for pl in seq:
            pl.pw = c + Rs @ (pl.pw - c)
            pl.A = Rs @ pl.A
        for k, v in list(pts.items()):
            if isinstance(v, np.ndarray) and v.shape == (2,):
                pts[k] = c + Rs @ (v - c)
        pts['sword_ang'] = pts['sword_ang'] + spin
    return seq, pts


def raster(pl, canvas, owner=None, oid=0):
    part = pl.part
    img = part.img
    h, w = img.shape
    piv = np.array(part.pivot, float)
    Ainv = np.linalg.inv(pl.A)
    corners = np.array([[0, 0], [w, 0], [0, h], [w, h]], float) - piv
    wc = (pl.A @ corners.T).T + pl.pw
    fx = AX + wc[:, 0] * SCALE
    fy = AY + wc[:, 1] * SCALE
    x0, x1 = int(math.floor(fx.min())) - 1, int(math.ceil(fx.max())) + 1
    y0, y1 = int(math.floor(fy.min())) - 1, int(math.ceil(fy.max())) + 1
    x0, y0 = max(0, x0), max(0, y0)
    x1, y1 = min(CW, x1), min(CH, y1)
    if x1 <= x0 or y1 <= y0:
        return
    bw, bh = x1 - x0, y1 - y0
    sub = (np.arange(SS) + 0.5) / SS
    us = (np.arange(x0, x1)[:, None] + sub[None, :]).ravel()          # bw*SS
    vs = (np.arange(y0, y1)[:, None] + sub[None, :]).ravel()          # bh*SS
    U, V = np.meshgrid(us, vs)                                         # (bh*SS, bw*SS)
    wx = (U - AX) / SCALE - pl.pw[0]
    wy = (V - AY) / SCALE - pl.pw[1]
    lx = Ainv[0, 0] * wx + Ainv[0, 1] * wy + piv[0]
    ly = Ainv[1, 0] * wx + Ainv[1, 1] * wy + piv[1]
    ix = np.floor(lx).astype(int)
    iy = np.floor(ly).astype(int)
    ok = (ix >= 0) & (ix < w) & (iy >= 0) & (iy < h)
    samp = np.zeros(ix.shape, int)
    samp[ok] = img[iy[ok], ix[ok]]
    if pl.smap is not None:
        samp = pl.smap[samp]
        samp[~ok] = 0
    # agrupar en bloques SSxSS
    blk = samp.reshape(bh, SS, bw, SS).transpose(0, 2, 1, 3).reshape(bh * bw, SS * SS)
    pid = np.repeat(np.arange(bh * bw), SS * SS)
    cnt = np.bincount(pid * 256 + blk.ravel(), minlength=bh * bw * 256).reshape(bh * bw, 256)
    opaque = SS * SS - cnt[:, 0]
    mask = opaque * 2 >= SS * SS
    choice = (cnt[:, 1:] * _W[1:]).argmax(1) + 1
    mask = mask.reshape(bh, bw)
    choice = choice.reshape(bh, bw)
    region = canvas[y0:y1, x0:x1]
    region[mask] = choice[mask]
    if owner is not None:
        owner[y0:y1, x0:x1][mask] = oid


OWN = {'sword': 2, 'leg': 3}


def render(pose, outline=True, trail_from=None, style=None):
    seq, pts = solve(pose)
    canvas = np.zeros((CH, CW), int)
    owner = np.zeros((CH, CW), int)
    if trail_from is not None:
        draw_trail(canvas, owner, trail_from, pts, style or pose.get('trail_style', 'blue'))
    for i, pl in enumerate(seq):
        raster(pl, canvas, owner, OWN.get(pl.tag, 1))
    if pose.get('clip_ground'):
        below = np.zeros_like(canvas, bool)
        below[AY + 1:, :] = True
        cut = below & (owner == 2)
        canvas[cut] = 0
        owner[cut] = 0
    if outline:
        m = canvas > 0
        pad = np.pad(m, 1)
        edge = m & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
        edge &= (_lum[canvas] > 30) & (owner != 4)
        canvas[edge] = OUT
    return canvas, owner, pts


def to_final(p):
    return (AX + p[0] * SCALE, AY + p[1] * SCALE)


TRAIL_RAMPS = {'blue': 'trail', 'red': 'fire', 'gold': 'gold', 'bolt': 'bolt'}


def draw_trail(canvas, owner, p0, p1, style='blue', steps=12, r0=0.66):
    """estela (smear) barrida por la punta de la hoja entre dos poses."""
    from PIL import Image, ImageDraw
    rampn = TRAIL_RAMPS.get(style, 'trail')
    g0, g1 = np.array(p0['grip']), np.array(p1['grip'])
    a0, a1 = p0['sword_ang'], p1['sword_ang']
    da = (a1 - a0 + 180) % 360 - 180
    if abs(da) < 8 and np.hypot(*(g1 - g0)) < 6:
        return
    L = P['sword'].blade_len
    im = Image.new('L', (CW, CH), 0)
    agei = Image.new('L', (CW, CH), 0)
    d = ImageDraw.Draw(im)
    da_ = ImageDraw.Draw(agei)
    bands = [(r0, 0.84, 0.6), (0.84, 0.95, 0.8), (0.95, 1.03, 1.0)]
    prev = None
    for i in range(steps + 1):
        t = i / steps
        g = g0 + (g1 - g0) * t
        a = a0 + da * t
        dv = vdir(a)
        segs = [(g + dv * L * b0, g + dv * L * b1) for b0, b1, _ in bands]
        if prev is not None:
            for (pa, pb), (ca, cb), (_, _, lv) in zip(prev, segs, bands):
                quad = [to_final(pa), to_final(pb), to_final(cb), to_final(ca)]
                d.polygon(quad, fill=PL.ramp_index(rampn, lv * (0.8 + 0.2 * t)))
                da_.polygon(quad, fill=int(1 + 200 * t))
        prev = segs
    arr = np.array(im, int)
    age = np.array(agei, int)
    m = arr > 0
    yy, xx = np.mgrid[0:CH, 0:CW]
    old = (age > 0) & (age < 80)
    m &= ~(old & ((xx + yy) % 2 == 1))           # tramado en la parte vieja
    m &= ~((age > 0) & (age < 30))               # la cola desaparece
    canvas[m] = arr[m]
    owner[m] = 4
