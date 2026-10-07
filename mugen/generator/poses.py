"""Paso 4: definicion de todas las animaciones del personaje como
secuencias de poses del esqueleto. De aqui salen los sprites y el .AIR
(con cajas de colision calculadas automaticamente)."""
import copy
import math

G = -57          # altura de la pelvis de pie (px nativos)
CR = G + 18      # agachado

IDLE = dict(root=(0, G), torso=0, head=0,
            legs=dict(near=dict(ankle=(-30, -12), knee=1), far=dict(ankle=(19, -12), knee=1)),
            sword=dict(grip=(-4, -4), ang=-128))


def P(base=None, **kw):
    """copia una pose y aplica cambios (mezcla profunda en legs/arms/sword)."""
    d = copy.deepcopy(base if base is not None else IDLE)
    for k, v in kw.items():
        if k in ('legs', 'arms', 'sword') and isinstance(v, dict):
            sub = d.setdefault(k, {})
            for kk, vv in v.items():
                if isinstance(vv, dict) and isinstance(sub.get(kk), dict):
                    sub[kk].update(vv)
                else:
                    sub[kk] = vv
        elif k == 'dr':                     # desplazar raiz
            r = d['root']
            d['root'] = (r[0] + v[0], r[1] + v[1])
        else:
            d[k] = v
    return d


def legs(n, f, kn=1, kf=1, fn=0, ff=0, world=False):
    return dict(near=dict(ankle=n, knee=kn, foot=fn, world=world),
                far=dict(ankle=f, knee=kf, foot=ff, world=world))


def sw(grip, ang):
    return dict(grip=grip, ang=ang)


class F:
    """un cuadro de animacion."""
    def __init__(self, pose, t, hit=False, trail=False, style='blue', flags='', off=(0, 0),
                 c2=True, c1=None, tag=None):
        self.pose = pose
        self.t = t
        self.hit = hit          # genera Clsn1 a partir de la espada/estela
        self.trail = trail      # dibuja estela desde el cuadro anterior
        self.style = style
        self.flags = flags
        self.off = off
        self.c2 = c2            # genera Clsn2 (cuerpo)
        self.c1 = c1            # Clsn1 manuales [(x1,y1,x2,y2)] en px finales
        self.tag = tag


ANIMS = {}     # numero -> dict(frames, loop, name)


def A(no, name, frames, loop=None):
    ANIMS[no] = dict(frames=frames, loop=loop, name=name)


# ---------------------------------------------------------------- reposo
I0 = IDLE
I1 = P(dr=(0, 1), torso=1, sword=sw((-4, -3), -129))
I2 = P(dr=(0, 2), torso=2, head=-1, sword=sw((-4, -2), -130))
A(0, 'De pie', [F(I0, 8), F(I1, 7), F(I2, 10), F(I1, 7)], loop=0)

TURN = P(torso=-4, head=0, sword=sw((-2, -6), -110))
A(5, 'Giro de pie', [F(TURN, 3), F(I0, 3)])

CMID = P(dr=(1, 9), torso=4, legs=legs((-31, -12), (20, -12)), sword=sw((-6, -3), -134))
CFULL = P(root=(2, CR), torso=8, head=0, legs=legs((-33, -12), (22, -12)), sword=sw((-8, -1), -140))
CFULL2 = P(CFULL, dr=(0, 1), torso=9, sword=sw((-8, 0), -141))
A(6, 'Giro agachado', [F(P(CFULL, torso=0, sword=sw((-4, -2), -120)), 3), F(CFULL, 3)])
A(10, 'De pie a agachado', [F(CMID, 3)])
A(11, 'Agachado', [F(CFULL, 10), F(CFULL2, 10)], loop=0)
A(12, 'Agachado a de pie', [F(CMID, 3)])


# ---------------------------------------------------------------- caminar
def walk_pose(phi, back=False, S=26, H=10, cn=-8, cf=6, lean=4, run=False):
    out = {}
    for side, c, ph in (('near', cn, phi), ('far', cf, (phi + 0.5) % 1)):
        if ph < 0.5:
            u = ph / 0.5
            x = c + S / 2 - S * u
            y = -12
        else:
            u = (ph - 0.5) / 0.5
            x = c - S / 2 + S * (0.5 - 0.5 * math.cos(math.pi * u))
            y = -12 - H * math.sin(math.pi * u)
        out[side] = dict(ankle=(x, y), knee=1,
                         foot=(-15 * math.sin(math.pi * u) if ph >= 0.5 else 0))
    bob = 1.5 * abs(math.cos(2 * math.pi * phi))
    pose = P(dr=(0, bob), torso=lean + 1.5 * math.sin(4 * math.pi * phi), legs=out,
             sword=sw((-4, -4 + bob), -128 - 3 * math.sin(2 * math.pi * phi)))
    return pose


A(20, 'Caminar adelante', [F(walk_pose(k / 8), 5) for k in range(8)], loop=0)
A(21, 'Caminar atras', [F(walk_pose(1 - k / 8, lean=-2), 5) for k in range(8)], loop=0)

# ---------------------------------------------------------------- salto
JPREP = P(CMID, dr=(0, 3), torso=6)
JUP1 = P(torso=4, legs=legs((-22, -16), (16, -20)), sword=sw((-4, -8), -122))
JUP2 = P(torso=6, legs=legs((-16, -26), (14, -30)), sword=sw((-4, -10), -118))
JFWD = P(JUP2, torso=12, legs=legs((-12, -28), (16, -32)))
JBACK = P(JUP2, torso=-6, legs=legs((-20, -24), (10, -28)))
JDN = P(torso=4, legs=legs((-24, -12), (18, -9)), sword=sw((-4, -6), -132))
A(40, 'Inicio de salto', [F(JPREP, 2), F(JPREP, 2)])
A(41, 'Salto neutral (subida)', [F(JUP1, 4), F(JUP2, -1)])
A(42, 'Salto adelante (subida)', [F(JUP1, 4), F(JFWD, -1)])
A(43, 'Salto atras (subida)', [F(JUP1, 4), F(JBACK, -1)])
A(44, 'Salto neutral (bajada)', [F(JUP1, 4), F(JDN, -1)])
A(45, 'Salto adelante (bajada)', [F(JUP1, 4), F(JDN, -1)])
A(46, 'Salto atras (bajada)', [F(JUP1, 4), F(JDN, -1)])
A(47, 'Aterrizaje', [F(CMID, 3), F(I1, 3)])

# ---------------------------------------------------------------- correr / esquivar
RUN = [walk_pose(k / 6, S=44, H=16, cn=-4, cf=4, lean=18, run=True) for k in range(6)]
RUN = [P(p, sword=sw((14, -24), -158), arms=dict(far=dict(elbow=(1, 0.3)))) for p in RUN]
A(100, 'Correr', [F(p, 4) for p in RUN], loop=0)
HOP1 = P(CMID, torso=-6)
HOP2 = P(JBACK, torso=-14, head=-6)
A(105, 'Salto atras rapido', [F(HOP1, 2), F(HOP2, -1)])
A(106, 'Aterrizaje salto atras', [F(CMID, 3), F(I1, 3)])
# carrera aerea (dash aereo, estilo JUS)
ADASH = P(JFWD, torso=24, head=-6, sword=sw((14, -24), -160), arms=dict(far=dict(elbow=(1, 0.3))))
A(110, 'Dash aereo', [F(P(ADASH, torso=18), 3), F(ADASH, -1)])

# ---------------------------------------------------------------- guardia
GU = P(torso=-4, head=3, sword=sw((18, -10), -112), arms=dict(far=dict(elbow=(0.6, 1))))
GU0 = P(torso=-2, sword=sw((12, -8), -80))
GUC = P(CFULL, torso=0, head=0, sword=sw((18, -6), -110), arms=dict(far=dict(elbow=(0.6, 1))))
GUC0 = P(CFULL, sword=sw((12, -4), -76))
GUA = P(JUP2, torso=-2, sword=sw((18, -12), -112), arms=dict(far=dict(elbow=(0.6, 1))))
GUA0 = P(JUP2, sword=sw((12, -10), -82))
A(120, 'Inicio guardia (pie)', [F(GU0, 3), F(GU, -1)])
A(121, 'Inicio guardia (agachado)', [F(GUC0, 3), F(GUC, -1)])
A(122, 'Inicio guardia (aire)', [F(GUA0, 3), F(GUA, -1)])
A(130, 'Guardia (pie)', [F(GU, -1)])
A(131, 'Guardia (agachado)', [F(GUC, -1)])
A(132, 'Guardia (aire)', [F(GUA, -1)])
A(140, 'Fin guardia (pie)', [F(GU0, 3)])
A(141, 'Fin guardia (agachado)', [F(GUC0, 3)])
A(142, 'Fin guardia (aire)', [F(GUA0, 3)])
GUH = P(GU, dr=(-2, 1), torso=-9, head=-2)
GUCH = P(GUC, dr=(-2, 1), torso=-6)
GUAH = P(GUA, torso=-9)
A(150, 'Golpe en guardia (pie)', [F(GUH, 2), F(GU, -1)])
A(151, 'Golpe en guardia (agachado)', [F(GUCH, 2), F(GUC, -1)])
A(152, 'Golpe en guardia (aire)', [F(GUAH, 2), F(GUA, -1)])

# ---------------------------------------------------------------- recibir golpes
DROOP = dict(ang=-30)


def hh(k):
    return P(dr=(-1 - 2 * k, 1 + k), torso=-8 - 6 * k, head=-6 - 4 * k,
             sword=sw((-2 + 2 * k, -4 + k), -124 + 8 * k))


def hl(k):
    return P(dr=(1 + k, 2 + 3 * k), torso=10 + 8 * k, head=10 + 3 * k,
             legs=legs((-31, -12), (21, -12)), sword=sw((-8 - k, 0 + 2 * k), -140 - 10 * k))


def hc(k):
    return P(CFULL, dr=(-1 - k, 0), torso=-4 - 7 * k, head=-6 - 4 * k, sword=sw((-4, -2), -134 - 10 * k))


HH = [hh(0), hh(1), hh(2), hh(3), hh(2.4)]
HL = [hl(0), hl(1), hl(2), hl(1.6)]
HC = [hc(0), hc(1), hc(2)]
A(5000, 'Golpe alto (debil)', [F(HH[0], 4), F(HH[1], 4)])
A(5001, 'Golpe alto (medio)', [F(HH[1], 5), F(HH[2], 5)])
A(5002, 'Golpe alto (fuerte)', [F(HH[2], 5), F(HH[3], 5)])
A(5005, 'Recuperacion alto (debil)', [F(HH[2], 3), F(HH[1], 3), F(HH[0], -1)])
A(5006, 'Recuperacion alto (medio)', [F(HH[3], 3), F(HH[2], 4), F(HH[1], 4), F(HH[0], -1)])
A(5007, 'Recuperacion alto (fuerte)', [F(HH[4], 5), F(HH[2], 4), F(HH[1], 4), F(HH[0], -1)])
A(5010, 'Golpe bajo (debil)', [F(HL[0], 5), F(HL[1], 5)])
A(5011, 'Golpe bajo (medio)', [F(HL[1], 5), F(HL[2], 5), F(HL[3], 5)])
A(5012, 'Golpe bajo (fuerte)', [F(HL[2], 1), F(HL[3], 5), F(HL[2], 5), F(HL[3], 5)])
A(5015, 'Recuperacion bajo (debil)', [F(HL[1], 1), F(HL[0], -1)])
A(5016, 'Recuperacion bajo (medio)', [F(HL[2], 1), F(HL[1], 4), F(HL[0], -1)])
A(5017, 'Recuperacion bajo (fuerte)', [F(HL[2], 1), F(HL[1], -1)])
A(5020, 'Golpe agachado (debil)', [F(HC[0], 5), F(HC[1], 5)])
A(5021, 'Golpe agachado (medio)', [F(HC[1], 5), F(HC[2], 5)])
A(5022, 'Golpe agachado (fuerte)', [F(HC[2], 5), F(HC[2], 5)])
A(5025, 'Recuperacion agachado (debil)', [F(HC[1], 1), F(HC[0], -1)])
A(5026, 'Recuperacion agachado (medio)', [F(HC[2], 3), F(HC[1], 5), F(HC[0], -1)])
A(5027, 'Recuperacion agachado (fuerte)', [F(HC[2], 4), F(HC[1], 4), F(HC[0], -1)])

# en el aire / caidas (el cuerpo gira alrededor de la pelvis)
AIRBACK = P(torso=-26, head=-14, legs=legs((-6, -22), (12, -18)), sword=sw((-2, -2), 40),
            arms=dict(far=dict(elbow=(0.2, 1))))
TUCK = P(torso=20, head=10, legs=legs((-4, -34), (10, -36)), sword=sw((10, -14), -70))


def fallp(spin, up=0):
    return P(AIRBACK, spin=spin, dr=(0, up))


A(5030, 'Golpe hacia atras (aire)', [F(AIRBACK, 5)])
A(5035, 'Transicion de golpe', [F(AIRBACK, 4), F(fallp(-35), 7)])
A(5040, 'Recuperacion aerea', [F(P(TUCK, spin=-60), 3), F(P(TUCK, spin=-150), 3), F(P(TUCK, spin=-240), 3),
                                F(P(TUCK, spin=-320), 3), F(JUP1, 4), F(JUP2, 4)], loop=4)
A(5050, 'Caida (subiendo)', [F(fallp(-50), 5), F(fallp(-62), 5)])
A(5051, 'Caida tipo arriba (subiendo)', [F(fallp(-15), 3), F(fallp(-25), 3)])
A(5060, 'Caida (bajando)', [F(fallp(-70), 5), F(fallp(-78), 5)])
A(5061, 'Caida tipo arriba (bajando)', [F(fallp(-35), 5), F(fallp(-60), 5), F(fallp(-90), 5), F(fallp(-120), 5),
                                         F(fallp(-150), 5), F(fallp(-170), -1)])
TRIP0 = P(torso=8, legs=legs((-10, -26), (30, -30)), sword=sw((4, -4), -40))
A(5070, 'Derribado (barrida)', [F(TRIP0, 5), F(fallp(-40), 5), F(fallp(-60), -1)])

# tumbado: espalda en el suelo, cabeza hacia atras
LIE = P(root=(6, -30), torso=0, head=8, spin=-90,
        legs=legs((52, -9), (56, -13), kn=1, kf=1, fn=0, ff=0, world=True),
        sword=sw((14, 10), 95), arms=dict(far=dict(elbow=(1, 0.2))))
LIE_UP = P(LIE, dr=(0, -6), spin=-80)
LIE_B = P(LIE, dr=(0, -2), spin=-86)
LIE_DEAD = P(LIE, head=18)
A(5080, 'Golpe tumbado', [F(LIE_B, 4), F(LIE, 4), F(LIE, -1)])
A(5090, 'Golpe tumbado (al aire)', [F(fallp(-60), 7)])
A(5100, 'Golpe contra el suelo', [F(LIE_B, 3)])
A(5101, 'Golpe contra el suelo (tipo arriba)', [F(LIE_B, 4), F(LIE, 2)])
A(5160, 'Rebote', [F(LIE_UP, -1)])
A(5170, 'Golpe contra el suelo (rebote)', [F(LIE, 3)])
A(5110, 'Tumbado', [F(LIE, 1)])
SIT = P(root=(-4, -26), torso=-40, head=10, legs=legs((30, -10), (40, -12), kn=1, kf=1),
        sword=sw((14, 6), 20), arms=dict(far=dict(elbow=(1, 0.2))))
KNEEL = P(root=(0, -34), torso=10, head=0, legs=legs((-26, -10), (20, -12), kn=-1, kf=1, fn=40),
          sword=sw((14, 0), -70))
A(5120, 'Levantarse', [F(LIE, 5), F(SIT, 5), F(KNEEL, 5), F(CFULL, 4), F(CMID, 3)])
A(5140, 'Muerto tumbado (rondas)', [F(LIE_DEAD, 1)])
A(5150, 'Muerto tumbado (final)', [F(LIE_DEAD, 1)])
A(5200, 'Recuperacion cerca del suelo', [F(P(TUCK, spin=-120), 3), F(P(TUCK, spin=-240), 4),
                                          F(P(TUCK, spin=-330), 4), F(CMID, -1)])
A(5210, 'Recuperacion en el aire', [F(P(TUCK, spin=-60), 3), F(P(TUCK, spin=-150), 3), F(P(TUCK, spin=-240), 3),
                                     F(P(TUCK, spin=-320), 3), F(JUP1, 4), F(JUP2, 4)], loop=4)
DZ = [P(dr=(0, 3), torso=10 + 4 * s, head=18 + 6 * s, sword=sw((-10, -2), 166), legs=legs((-28, -12), (18, -12)),
        arms=dict(far=dict(elbow=(1, 0.5))), spin=2 * s) for s in (-1, 0, 1, 0)]
A(5300, 'Mareado', [F(DZ[0], 8), F(DZ[1], 8), F(DZ[2], 8), F(DZ[3], 8)], loop=0)


# ================================================================ ATAQUES
WIDE = legs((-32, -12), (24, -12))
STEP = legs((-24, -12), (30, -12))


def S(base=None, grip=(4, -4), ang=-52, **kw):
    return P(base, sword=sw(grip, ang), **kw)


# --- a: combo de 3 golpes (estilo JUS) ---------------------------------
A1 = [S(torso=-4, grip=(-2, -26), ang=-105),
      S(torso=6, grip=(14, -18), ang=-25),
      S(torso=9, grip=(18, -10), ang=15),
      S(torso=5, grip=(12, -8), ang=-10)]
A(200, 'Corte rapido (a)', [F(A1[0], 3), F(A1[1], 2, hit=True, trail=True), F(A1[2], 3, hit=True, trail=True),
                             F(A1[3], 7), F(I1, 4)])
A2 = [S(torso=8, grip=(16, -6), ang=30),
      S(torso=4, grip=(16, -18), ang=-40),
      S(torso=-4, grip=(8, -34), ang=-100),
      S(torso=-2, grip=(6, -26), ang=-80)]
A(210, 'Tajo ascendente (a,a)', [F(A2[0], 2), F(A2[1], 2, hit=True, trail=True), F(A2[2], 3, hit=True, trail=True),
                                  F(A2[3], 8), F(I1, 4)])
A3 = [S(torso=-10, head=2, grip=(-8, -40), ang=-140, legs=STEP),
      S(torso=2, grip=(12, -40), ang=-60, legs=STEP),
      S(torso=14, grip=(24, -18), ang=10, dr=(4, 4), legs=STEP),
      S(torso=18, grip=(24, -6), ang=40, dr=(6, 6), legs=legs((-24, -12), (32, -12))),
      S(torso=8, grip=(14, -6), ang=0, dr=(3, 2), legs=STEP)]
A(220, 'Gran tajo (a,a,a)', [F(A3[0], 5), F(A3[1], 2, hit=True, trail=True), F(A3[2], 3, hit=True, trail=True),
                              F(A3[3], 12), F(A3[4], 6), F(I1, 4)])

# --- b: Great Cleave (barrido amplio) ----------------------------------
B = [S(torso=-12, head=4, grip=(-14, -20), ang=175, dr=(0, 4), legs=WIDE),
     S(torso=4, grip=(8, -30), ang=-80, dr=(2, 2), legs=WIDE),
     S(torso=12, grip=(26, -22), ang=-4, dr=(4, 3), legs=STEP),
     S(torso=12, grip=(24, -18), ang=12, dr=(4, 3), legs=STEP),
     S(torso=6, grip=(14, -10), ang=-20, dr=(2, 1), legs=STEP)]
A(230, 'Great Cleave (b)', [F(B[0], 6), F(B[1], 2, hit=True, trail=True), F(B[2], 3, hit=True, trail=True),
                             F(B[3], 12), F(B[4], 6)])

# --- c: golpe vertical demoledor ---------------------------------------
C = [S(torso=-12, head=2, grip=(-4, -56), ang=-110, legs=WIDE),
     S(torso=2, grip=(10, -50), ang=-60, legs=WIDE),
     S(torso=20, grip=(24, -28), ang=12, dr=(4, 4), legs=STEP),
     S(torso=26, head=6, grip=(26, -14), ang=31, dr=(6, 10), legs=legs((-30, -12), (32, -12))),
     S(torso=10, grip=(16, -10), ang=8, dr=(3, 4), legs=STEP)]
A(240, 'Golpe demoledor (c)', [F(C[0], 8), F(C[1], 2, hit=True, trail=True), F(C[2], 2, hit=True, trail=True),
                                F(C[3], 3, hit=True), F(C[3], 14), F(C[4], 8)])

# --- F+c: embestida con la hombrera ------------------------------------
SHB = [S(CMID, torso=-8, grip=(-8, -8), ang=160),
       S(torso=24, head=-4, grip=(-10, -6), ang=165, dr=(4, 4), legs=legs((-36, -12), (20, -12))),
       S(torso=28, head=-4, grip=(-10, -6), ang=165, dr=(8, 5), legs=legs((-30, -12), (30, -12))),
       S(torso=10, grip=(0, -6), ang=-150, dr=(2, 2), legs=STEP)]
A(250, 'Embestida (F+c)', [F(SHB[0], 6), F(SHB[1], 3, hit=True, c1=[(-10, -110, 46, -30)]),
                            F(SHB[2], 6, hit=True, c1=[(-6, -106, 52, -30)]), F(SHB[2], 10), F(SHB[3], 8)])

# --- agachado -----------------------------------------------------------
CA = [S(CFULL, torso=6, grip=(6, -4), ang=-20),
      S(CFULL, torso=14, grip=(20, 0), ang=10),
      S(CFULL, torso=12, grip=(16, -2), ang=5)]
A(300, 'Corte bajo (abajo+a)', [F(CA[0], 2), F(CA[1], 3, hit=True, trail=True), F(CA[2], 6), F(CFULL, 3)])
CB = [S(CFULL, torso=16, grip=(14, 4), ang=18),
      S(CFULL, root=(2, -48), torso=6, grip=(16, -18), ang=-45),
      S(root=(0, -60), torso=-8, head=-6, grip=(10, -40), ang=-95, legs=legs((-24, -12), (16, -12))),
      S(root=(0, -59), torso=-6, head=-4, grip=(8, -38), ang=-100, legs=legs((-24, -12), (16, -12)))]
A(310, 'Tajo lunar (abajo+b)', [F(CB[0], 5), F(CB[1], 2, hit=True, trail=True), F(CB[2], 3, hit=True, trail=True),
                                 F(CB[3], 12), F(CMID, 5)])
CC = [S(CFULL, torso=-6, grip=(-12, 0), ang=170),
      S(CFULL, root=(4, -34), torso=28, grip=(18, 16), ang=8),
      S(CFULL, root=(4, -34), torso=28, grip=(20, 18), ang=-2),
      S(CFULL, root=(3, -36), torso=20, grip=(16, 12), ang=0)]
A(320, 'Barrida (abajo+c)', [F(CC[0], 5), F(CC[1], 2, hit=True), F(CC[2], 4, hit=True, trail=True),
                              F(CC[3], 14), F(CFULL, 4)])

# --- aire ---------------------------------------------------------------
JA = [S(JUP2, grip=(2, -24), ang=-100), S(JUP2, grip=(16, -10), ang=-10, torso=8),
      S(JUP2, grip=(16, -2), ang=30, torso=10), S(JUP2, grip=(12, -6), ang=10, torso=6)]
A(600, 'Corte aereo (aire a)', [F(JA[0], 2), F(JA[1], 3, hit=True, trail=True), F(JA[2], 4, hit=True, trail=True),
                                 F(JA[3], -1)])
JB = [S(JUP2, grip=(-10, -16), ang=170, torso=-8), S(JUP2, grip=(12, -24), ang=-60, torso=4),
      S(JUP2, grip=(22, -14), ang=5, torso=12), S(JUP2, grip=(18, -10), ang=15, torso=10)]
A(610, 'Hendidura aerea (aire b)', [F(JB[0], 4), F(JB[1], 2, hit=True, trail=True), F(JB[2], 4, hit=True, trail=True),
                                     F(JB[3], -1)])
JC = [S(JUP2, grip=(-4, -52), ang=-120, torso=-14), S(JUP2, grip=(14, -40), ang=-50, torso=4),
      S(JUP2, grip=(22, -8), ang=50, torso=20), S(JUP2, grip=(20, -2), ang=75, torso=20)]
A(620, 'Martillo aereo (aire c)', [F(JC[0], 6), F(JC[1], 2, hit=True, trail=True), F(JC[2], 4, hit=True, trail=True),
                                    F(JC[3], -1)])

# --- agarre -------------------------------------------------------------
ONE = dict(grip=(6, -6), ang=-62)
TH = [P(sword=sw(*ONE.values()), torso=-4, arms=dict(near=dict(hand=(0, -30)))),
      P(sword=sw(*ONE.values()), torso=10, arms=dict(near=dict(hand=(30, -40))), legs=STEP),
      P(sword=sw(*ONE.values()), torso=8, arms=dict(near=dict(hand=(26, -38))), legs=STEP)]
A(800, 'Agarre (F+c cerca)', [F(TH[0], 3), F(TH[1], 4, hit=True, c1=[(14, -82, 50, -40)]), F(TH[2], 10)])
TS = [P(sword=sw((4, -6), -66), torso=8, arms=dict(near=dict(hand=(26, -44))), legs=STEP),
      P(sword=sw((4, -6), -70), torso=-4, arms=dict(near=dict(hand=(22, -66))), legs=STEP),
      P(sword=sw((4, -6), -66), torso=20, head=16, arms=dict(near=dict(hand=(28, -62))), legs=STEP),
      P(sword=sw((4, -6), -60), torso=14, arms=dict(near=dict(hand=(40, -52))), legs=legs((-26, -12), (34, -12))),
      P(sword=sw((4, -6), -56), torso=6, legs=STEP)]
A(810, 'Lanzamiento', [F(TS[0], 6), F(TS[1], 10), F(TS[2], 8), F(TS[1], 6), F(TS[3], 10), F(TS[4], 10)])

# ================================================================ ESPECIALES
# Storm Hammer: lanza el guantelete magico
SH = [P(torso=-10, head=-4, sword=sw((8, -2), 20), arms=dict(near=dict(hand=(-26, -44)))),
      P(torso=4, sword=sw((8, -2), 20), arms=dict(near=dict(hand=(22, -50))), legs=STEP),
      P(torso=12, sword=sw((8, -2), 22), arms=dict(near=dict(hand=(36, -42))), legs=STEP),
      P(torso=10, sword=sw((8, -2), 20), arms=dict(near=dict(hand=(34, -40))), legs=STEP)]
A(1000, 'Storm Hammer (x)', [F(SH[0], 10), F(SH[1], 2), F(SH[2], 4), F(SH[3], 14), F(I1, 6)])
SHA = [P(JUP2, torso=-10, sword=sw((8, -4), 10), arms=dict(near=dict(hand=(-24, -44)))),
       P(JUP2, torso=10, sword=sw((8, -4), 12), arms=dict(near=dict(hand=(30, -30)))),
       P(JUP2, torso=14, sword=sw((8, -4), 14), arms=dict(near=dict(hand=(34, -24))))]
A(1050, 'Storm Hammer aereo (aire x)', [F(SHA[0], 8), F(SHA[1], 2), F(SHA[2], 14), F(JUP2, -1)])
RUSH0 = P(CMID, torso=-10, sword=sw((-6, -4), 160), arms=dict(near=dict(hand=(-24, -40))))
RUSH = [P(root=(0, -62), torso=42 + d, head=-12, legs=legs((-44, -34 + d), (-32, -22)),
          sword=sw((-12, -6), 172), arms=dict(near=dict(hand=(44, -30)), far=dict(elbow=(-1, 0.5))))
        for d in (0, 3)]
RUSH_END = P(JUP2, torso=-12, sword=sw((4, -10), -70))
A(1100, 'Carga del Martillo (F+x)', [F(RUSH0, 8), F(RUSH[0], 2, hit=True, c1=[(-6, -96, 60, -30)]),
                                     F(RUSH[1], 2, hit=True, c1=[(-6, -96, 60, -30)])], loop=1)
A(1101, 'Carga del Martillo (fin)', [F(RUSH_END, 6), F(JDN, -1)])

# Great Cleave especial: onda de hendidura
GC = [S(torso=-14, head=2, grip=(-16, -30), ang=-160, dr=(0, 2), legs=WIDE),
      S(torso=6, grip=(14, -36), ang=-70, legs=STEP),
      S(torso=22, grip=(28, -14), ang=15, dr=(6, 6), legs=legs((-26, -12), (34, -12))),
      S(torso=24, grip=(28, -8), ang=28, dr=(6, 8), legs=legs((-26, -12), (34, -12))),
      S(torso=8, grip=(14, -8), ang=0, dr=(2, 2), legs=STEP)]
A(1200, 'Gran Hendidura (y)', [F(GC[0], 10), F(GC[1], 2, hit=True, trail=True), F(GC[2], 3, hit=True, trail=True),
                               F(GC[3], 16), F(GC[4], 8)])
RC = [S(CFULL, torso=14, grip=(16, 6), ang=18),
      S(root=(2, -60), torso=0, grip=(16, -24), ang=-50, legs=legs((-24, -12), (16, -12))),
      S(JUP2, torso=-10, grip=(10, -46), ang=-95),
      S(JUP2, torso=-14, grip=(4, -50), ang=-110),
      S(JDN, torso=-4, grip=(6, -30), ang=-80)]
A(1250, 'Tajo Ascendente del Caballero (F+y)', [F(RC[0], 4), F(RC[1], 2, hit=True, trail=True),
                                                F(RC[2], 3, hit=True, trail=True), F(RC[3], 6, hit=True),
                                                F(RC[4], -1)])

# Warcry
WC = [P(CMID, torso=8, head=4, sword=sw((8, -2), -40)),
      P(torso=-2, head=0, legs=WIDE, sword=sw((20, -58), -80), arms=dict(near=dict(hand=(12, -36)))),
      P(torso=-3, head=-1, legs=WIDE, sword=sw((20, -59), -81), arms=dict(near=dict(hand=(13, -37))), dr=(0, -1))]
A(1300, 'Warcry (z)', [F(WC[0], 8), F(WC[1], 4), F(WC[2], 3), F(WC[1], 3), F(WC[2], 3), F(WC[1], 3),
                       F(WC[2], 3), F(WC[1], 10), F(I1, 6)])

# ================================================================ SUPERS
PLANT = dict(grip=(26, -16), ang=88)
GS = [P(CMID, torso=6, head=4, sword=sw(**PLANT), clip_ground=True,
        arms=dict(near=dict(hand=(14, -26)), far=dict(hand=(30, -24)))),
      P(torso=4, head=0, legs=WIDE, sword=sw(**PLANT), clip_ground=True,
        arms=dict(near=dict(hand=(8, -44)), far=dict(hand=(30, -46)))),
      P(torso=3, head=-1, legs=WIDE, sword=sw(**PLANT), clip_ground=True, dr=(0, -1),
        arms=dict(near=dict(hand=(9, -46)), far=dict(hand=(31, -48))))]
A(3000, "God's Strength", [F(GS[0], 12), F(GS[1], 4), F(GS[2], 3), F(GS[1], 3), F(GS[2], 3), F(GS[1], 3),
                           F(GS[2], 3), F(GS[1], 3), F(GS[2], 3), F(GS[1], 20), F(CMID, 6)])
# arremetida (supers): pose de carrera con espada atras
DASH = [P(p, torso=24, sword=sw((-12, -8), 168), arms=dict(far=dict(elbow=(-1, 0.5)))) for p in RUN[:2]]
A(3100, 'Hendidura de Tormenta (inicio)', [F(GC[0], 6), F(DASH[0], 3), F(DASH[1], 3)], loop=1)
SKY = [S(torso=-4, head=-2, grip=(6, -62), ang=-90, legs=WIDE),
       S(JUP2, torso=-5, head=-2, grip=(4, -62), ang=-92),
       S(JUP2, torso=10, grip=(18, -40), ang=-40),
       S(torso=24, grip=(26, -14), ang=31, dr=(6, 10), legs=legs((-30, -12), (32, -12)))]
A(3110, 'Tajo del cielo (alzar)', [F(SKY[0], 6), F(SKY[1], -1)])
A(3120, 'Tajo del cielo (caer)', [F(SKY[2], 2, hit=True, trail=True, style='red'), F(JC[3], -1)])
A(3130, 'Tajo del cielo (impacto)', [F(SKY[3], 3, hit=True), F(SKY[3], 20), F(C[4], 8)])
A(3150, 'Tajo rojo 1', [F(A3[0], 2), F(A3[1], 2, hit=True, trail=True, style='red'),
                        F(A3[2], 3, hit=True, trail=True, style='red'), F(A3[3], 4)])
A(3160, 'Tajo rojo 2', [F(B[0], 2), F(B[1], 2, hit=True, trail=True, style='red'),
                        F(B[2], 3, hit=True, trail=True, style='red'), F(B[3], 4)])
A(3170, 'Tajo rojo 3', [F(CB[0], 2), F(CB[1], 2, hit=True, trail=True, style='red'),
                        F(CB[2], 3, hit=True, trail=True, style='red'), F(CB[3], 6)])

# ================================================================ OTROS
CHG = [P(CMID, torso=2, head=-1, sword=sw((24, -20), 84), clip_ground=True,
         arms=dict(near=dict(hand=(-14, -24)), far=dict(hand=(14, -30), elbow=(1, 0.8)))),
       P(CMID, torso=1, head=-2, dr=(0, 1), sword=sw((24, -20), 84), clip_ground=True,
         arms=dict(near=dict(hand=(-15, -24)), far=dict(hand=(15, -30), elbow=(1, 0.8))))]
A(700, 'Cargar poder', [F(CHG[0], 4), F(CHG[1], 4)], loop=0)
A(195, 'Burla', [F(P(sword=sw((22, -36), -4), torso=4, arms=dict(near=dict(hand=(-6, -6)))), 8),
                 F(P(sword=sw((26, -38), -6), torso=6, head=-6, arms=dict(near=dict(hand=(-6, -6)))), 40),
                 F(P(sword=sw((22, -36), -4), torso=4, arms=dict(near=dict(hand=(-6, -6)))), 8), F(I1, 6)])
KNEELP = P(KNEEL, torso=16, head=18, sword=sw((22, -22), 86), clip_ground=True,
           arms=dict(near=dict(hand=(20, -26)), far=dict(hand=(24, -30), elbow=(1, 0.6))))
A(170, 'Derrota por tiempo', [F(CMID, 6), F(KNEELP, -1)])
A(175, 'Empate', [F(P(torso=0, head=10, sword=sw((8, -2), -30)), -1)])
A(190, 'Intro (espada clavada)', [F(KNEELP, 40), F(P(KNEELP, head=0, torso=8), 20), F(P(CMID, sword=sw((22, -20), 84), clip_ground=True,
                                  arms=dict(near=dict(hand=(20, -24)), far=dict(hand=(24, -28)))), 10),
                                  F(P(CMID, sword=sw((14, -28), -20)), 6), F(I0, 8), F(I1, 20)])
A(191, 'Intro (grito)', [F(I0, 10)] + [f for f in ANIMS[1300]['frames'][:8]] + [F(I1, 10)])
A(181, 'Victoria (espada en alto)', [F(I1, 6), F(SKY[0], 8),
                                     F(S(torso=-3, head=-2, grip=(8, -66), ang=-88, legs=WIDE), -1)])
A(182, 'Victoria (espada al hombro)', [F(I1, 6), F(S(torso=-2, head=-6, grip=(16, -36), ang=-152,
                                                    arms=dict(far=dict(hand=(12, -8)))), -1)])
A(183, 'Victoria (grito)', [F(GS[0], 10), F(GS[1], 6), F(GS[2], 4), F(GS[1], 4), F(GS[2], 4), F(GS[1], -1)])
