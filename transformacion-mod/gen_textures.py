# Generador de texturas pixel-art (estilo Minecraft) para el mod Transformacion
import sys, random, math
from PIL import Image, ImageDraw

OUT = sys.argv[1]

def hx(s):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255)

# ---------- paleta ----------
BLACK0 = hx('#07040a'); BLACK1 = hx('#120a14'); BLACK2 = hx('#221321'); BLACK3 = hx('#38213a')
WINE0 = hx('#0d0610'); WINE1 = hx('#1d0b20'); WINE2 = hx('#34102f'); WINE3 = hx('#52183f'); WINE4 = hx('#7a2150')
MEM = [hx('#14060f'), hx('#260a1c'), hx('#3e1030'), hx('#5c1a45'), hx('#7a2658')]
VEIN0 = hx('#9a0f34'); VEIN1 = hx('#e01c48'); VEIN2 = hx('#ff6a82')
RUBY0 = hx('#7a0a22'); RUBY1 = hx('#d4143c'); RUBY2 = hx('#ff4a66'); RUBY3 = hx('#ffc4d0')
HAIR0 = hx('#1a0f0d'); HAIR1 = hx('#2e1b15'); HAIR2 = hx('#47291e'); HAIR3 = hx('#6a4230'); HAIR4 = hx('#8f6448')
BURG = hx('#5a1a24'); BURG2 = hx('#852530')
SKIN0 = hx('#f0d0be'); SKIN1 = hx('#e0b4a2'); SKIN2 = hx('#c08c80'); SKIN3 = hx('#9a6a68')
LIPS = hx('#b04858')
IRIS = hx('#7a2a3a'); IRIS2 = hx('#c06070'); WHITE = hx('#f2e6f6')
GREY0 = hx('#4a4060'); GREY1 = hx('#6a6088')
RED0 = hx('#8f0f26'); RED1 = hx('#dc2040'); RED2 = hx('#ff506a')
CLEAR = (0, 0, 0, 0)

class Sheet:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.d = Image.new('RGBA', (w, h), CLEAR)   # difusa
        self.g = Image.new('RGBA', (w, h), CLEAR)   # brillo (emisiva)
    def p(self, x, y, c, glow=None):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.d.putpixel((x, y), c)
            if glow is not None:
                self.g.putpixel((x, y), glow)
            else:
                self.g.putpixel((x, y), CLEAR)
    def get(self, x, y):
        return self.d.getpixel((x, y))
    def paste_region(self, sub, x, y):
        self.d.paste(sub.d, (x, y)); self.g.paste(sub.g, (x, y))
    def save(self, name, glow=True):
        self.d.save(f'{OUT}/{name}.png')
        if glow:
            self.g.save(f'{OUT}/{name}_glow.png')

def line(img, x0, y0, x1, y1, fn):
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx = 1 if x0 < x1 else -1; sy = 1 if y0 < y1 else -1
    err = dx + dy
    while True:
        fn(x0, y0)
        if x0 == x1 and y0 == y1: break
        e2 = 2 * err
        if e2 >= dy: err += dy; x0 += sx
        if e2 <= dx: err += dx; y0 += sy

BAYER = [[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]
def dither(x, y, t):  # t en 0..1 -> devuelve True para el tono superior
    return (BAYER[y % 4][x % 4] + 0.5) / 16.0 < t

def ramp_pick(ramp, t, x, y):
    t = max(0.0, min(0.9999, t)) * (len(ramp) - 1)
    i = int(t); f = t - i
    return ramp[i + 1] if dither(x, y, f) else ramp[i]

def poly_mask(w, h, pts):
    m = Image.new('L', (w, h), 0)
    ImageDraw.Draw(m).polygon(pts, fill=255)
    return m

def outline_mask(mask):
    w, h = mask.size
    px = mask.load()
    edge = set()
    for y in range(h):
        for x in range(w):
            if px[x, y]:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if nx < 0 or ny < 0 or nx >= w or ny >= h or not px[nx, ny]:
                        edge.add((x, y)); break
    return edge

def gem(s, x, y, glow=True):
    # rubi de 2x2 con brillo
    G = RUBY2 if glow else None
    s.p(x, y, RUBY2, RUBY2); s.p(x + 1, y, RUBY1, RUBY1)
    s.p(x, y + 1, RUBY1, RUBY1); s.p(x + 1, y + 1, RUBY0, RUBY0)
    s.p(x, y, RUBY3, RUBY3)

# ======================================================================
# ALAS  (arte con la raiz a la izquierda y la punta a la derecha, vista desde atras del ala derecha)
# ======================================================================
def make_wing(W, H, poly, root, vein_targets, frame_pts, eye=None, gem_pos=None, seed=1):
    rnd = random.Random(seed)
    s = Sheet(W, H)
    mask = poly_mask(W, H, poly)
    edge = outline_mask(mask)
    px = mask.load()
    rx, ry = root
    maxd = max(math.hypot(x - rx, y - ry) for y in range(H) for x in range(W) if px[x, y]) or 1
    # membrana con degradado: oscuro en la raiz y el borde, claro en el centro
    for y in range(H):
        for x in range(W):
            if px[x, y]:
                d = math.hypot(x - rx, y - ry) / maxd
                # luz: centro medio del ala
                t = 1.0 - abs(d - 0.55) * 1.7
                t += 0.12 * math.sin(x * 0.7) * math.cos(y * 0.5)
                c = ramp_pick(MEM, t, x, y)
                if dither(x + 1, y, max(0.0, d - 0.55) * 1.2) and c in (MEM[1], MEM[2]):
                    c = hx('#2c1240') if c == MEM[2] else hx('#1c0a2c')
                s.p(x, y, c)
    # parche claro (mancha del ala)
    if eye:
        ex, ey, er = eye
        for y in range(H):
            for x in range(W):
                if px[x, y]:
                    dd = math.hypot((x - ex) / 1.2, (y - ey))
                    if dd < er:
                        s.p(x, y, ramp_pick(MEM, 0.55 + 0.45 * (1 - dd / er), x, y))
                    elif dd < er + 1.1:
                        s.p(x, y, MEM[0])
    # venas
    veins = set()
    def addv(x, y):
        if 0 <= x < W and 0 <= y < H and px[x, y]:
            veins.add((x, y))
    for vi, (tx, ty) in enumerate(vein_targets):
        rx, ry = root[0] + (vi % 3), root[1] - (vi % 3) * 2 + (1 if vi % 2 else 0)
        # curva suave (bezier cuadratica) para que no sean rectas
        mx = (rx + tx) / 2 + rnd.uniform(-3, 3); my = (ry + ty) / 2 + rnd.uniform(-3, 3) - 2
        prev = (rx, ry)
        for i in range(1, 25):
            t = i / 24.0
            bx = (1 - t) ** 2 * rx + 2 * (1 - t) * t * mx + t * t * tx
            by = (1 - t) ** 2 * ry + 2 * (1 - t) * t * my + t * t * ty
            cur = (int(round(bx)), int(round(by)))
            line(None, prev[0], prev[1], cur[0], cur[1], addv)
            prev = cur
            # ramita
            if i == 13 and vi % 2 == 0:
                ang = math.atan2(ty - ry, tx - rx) + rnd.choice([-0.8, 0.8])
                bx2 = int(cur[0] + math.cos(ang) * 5); by2 = int(cur[1] + math.sin(ang) * 5)
                line(None, cur[0], cur[1], bx2, by2, addv)
    for (x, y) in veins:
        s.p(x, y, VEIN1 if (x + 2 * y) % 5 == 0 else VEIN0, VEIN1 if (x + 2 * y) % 5 == 0 else None)
    # hueso del borde (marco oscuro) + contorno
    for (x, y) in edge:
        s.p(x, y, BLACK1)
    for i in range(len(frame_pts) - 1):
        (a, b), (c, d) = frame_pts[i], frame_pts[i + 1]
        def fr(x, y):
            for ox, oy in ((0, 0), (0, 1), (1, 0)):
                if 0 <= x + ox < W and 0 <= y + oy < H and px[x + ox, y + oy]:
                    s.p(x + ox, y + oy, BLACK0)
        line(None, a, b, c, d, fr)
        def hi(x, y):
            if 0 <= x < W and 0 <= y - 1 < H and px[x, y - 1] and s.get(x, y - 1) != BLACK1:
                pass
    # brillo del hueso
    for i in range(len(frame_pts) - 1):
        (a, b), (c, d) = frame_pts[i], frame_pts[i + 1]
        def hi2(x, y):
            xx, yy = x + 1, y + 2
            if 0 <= xx < W and 0 <= yy < H and px[xx, yy] and (xx, yy) not in veins and s.get(xx, yy)[:3] not in [BLACK0[:3], BLACK1[:3]]:
                s.p(xx, yy, BLACK3)
        line(None, a, b, c, d, hi2)
    if gem_pos:
        gx, gy = gem_pos
        gem(s, gx, gy)
    # puntas del borde con brillo magenta tenue
    for (x, y) in edge:
        if rnd.random() < 0.18:
            s.p(x, y, BLACK2)
    return s

F = 0.9
def sc(pts):
    return [(int(round(x * F)), int(round(y * F))) for x, y in pts]

def build_wings():
    # ---- ala superior (32x36 -> escalada) ; bisagra cerca del borde inferior-interior
    upoly = sc([(0, 27), (0, 21), (2, 13), (5, 7), (9, 3), (14, 0), (18, 0), (16, 3), (21, 3), (26, 5), (30, 9), (31, 15), (29, 18), (31, 22),
             (28, 24), (29, 28), (25, 29), (25, 33), (21, 31), (19, 35), (15, 31), (11, 33), (9, 28), (5, 29), (3, 26)])
    UW, UH = 29, 32
    utargets = sc([(9, 6), (16, 5), (23, 6), (29, 12), (27, 20), (25, 27), (20, 31), (14, 29), (8, 26)])
    uframe = sc([(1, 25), (3, 15), (7, 7), (13, 2), (17, 1)])
    up = make_wing(UW, UH, upoly, sc([(2, 25)])[0], utargets, uframe, eye=(int(19 * F), int(15 * F), 4), gem_pos=sc([(6, 18)])[0], seed=3)
    # ---- ala inferior (24x28 -> escalada)
    lpoly = sc([(0, 2), (0, 6), (2, 12), (5, 17), (8, 22), (11, 27), (13, 24), (14, 27), (17, 25), (16, 21), (20, 18), (18, 14), (22, 11), (21, 7),
             (23, 4), (19, 3), (20, 0), (14, 1), (8, 0), (3, 0)])
    LW, LH = 22, 25
    ltargets = sc([(6, 11), (11, 20), (13, 25), (17, 20), (20, 13), (20, 6), (12, 4)])
    lframe = sc([(1, 3), (8, 2), (14, 3), (20, 4)])
    lp = make_wing(LW, LH, lpoly, sc([(1, 3)])[0], ltargets, lframe, eye=(int(13 * F), int(12 * F), 3), gem_pos=None, seed=7)
    print('UP', up.w, up.h, 'root', sc([(2, 25)])[0], 'LOW', lp.w, lp.h, 'root', sc([(1, 3)])[0])

    def flip(sh):
        r = Sheet(sh.w, sh.h)
        r.d = sh.d.transpose(Image.FLIP_LEFT_RIGHT); r.g = sh.g.transpose(Image.FLIP_LEFT_RIGHT); return r

    # hoja 128x128: cada caja w x h x 1 -> cara frontal en (u+1,v+1), trasera en (u+w+2, v+1)
    S = Sheet(128, 128)
    def put_wing(u, v, art, mirrored):
        # cara trasera (se ve desde atras) y delantera (espejo)
        back = flip(art) if mirrored else art
        front = art if mirrored else flip(art)
        S.paste_region(front, u + 1, v + 1)
        S.paste_region(back, u + art.w + 2, v + 1)
    # ala derecha: artwork directo en la cara trasera
    put_wing(0, 0, up, False)
    put_wing(0, 36, up, True)
    put_wing(0, 72, lp, False)
    put_wing(48, 72, lp, True)
    S.save('wings')

# ======================================================================
# CABEZA / PELO
# ======================================================================
def hair_pixel(rnd, x, y, h, light=0.0):
    # mechones verticales suaves: cada columna tiene un tono base y brillos en tramos
    cx = (x * 2654435761) & 0xffff
    base = [0.28, 0.40, 0.33, 0.46, 0.36, 0.30][(x * 5 + x // 3) % 6]
    band = 0.16 if ((y + (cx >> 5)) % 7) in (0, 1) else 0.0
    t = base + band + light + 0.14 * (1 - y / max(1, h))
    if (x * 3 + 1) % 11 == 0 and y % 6 != 5:
        return ramp_pick([BURG, BURG2], 0.2 + (1 - y / max(1, h)) * 0.7, x, y)
    return ramp_pick([HAIR0, HAIR1, HAIR2, HAIR3, HAIR4], t, x, y)

def build_head():
    S = Sheet(64, 64)
    rnd = random.Random(11)
    # ---- cara  (caja 8x8x8 en (0,0)); frontal en (8,8)
    def face_front():
        f = Sheet(8, 8)
        for y in range(8):
            for x in range(8):
                f.p(x, y, SKIN0 if y < 6 else SKIN1)
        for x in range(8):
            f.p(x, 0, HAIR1); f.p(x, 1, HAIR1)
        # marca de la frente
        f.p(3, 2, RED1, RED1); f.p(4, 2, RED1, RED1)
        f.p(3, 3, RED0, RED0); f.p(4, 3, RED0, RED0)
        f.p(2, 2, RED0, RED0); f.p(5, 2, RED0, RED0)
        # cejas afiladas
        for x in (1, 2): f.p(x, 3, HAIR1)
        for x in (5, 6): f.p(x, 3, HAIR1)
        f.p(0, 3, HAIR2); f.p(7, 3, HAIR2)
        # ojos
        for x in (1, 2, 5, 6): f.p(x, 4, HAIR0)
        f.p(1, 5, WHITE); f.p(2, 5, IRIS, IRIS)
        f.p(5, 5, IRIS, IRIS); f.p(6, 5, WHITE)
        f.p(2, 4, HAIR0)
        # sombra suave bajo ojos / mejillas
        for x in (1, 2, 5, 6): f.p(x, 6, SKIN1)
        f.p(0, 6, SKIN2); f.p(7, 6, SKIN2)
        # nariz y boca
        f.p(3, 6, SKIN2); f.p(4, 6, SKIN1)
        f.p(3, 7, LIPS); f.p(4, 7, LIPS)
        f.p(2, 7, SKIN2); f.p(5, 7, SKIN2)
        f.p(0, 7, SKIN2); f.p(7, 7, SKIN2)
        return f
    S.paste_region(face_front(), 8, 8)
    side = Sheet(8, 8)
    for y in range(8):
        for x in range(8):
            side.p(x, y, HAIR1 if y < 4 else SKIN1)
    for reg in ((0, 8), (16, 8), (24, 8)):
        S.paste_region(side, *reg)
    top = Sheet(8, 8)
    for y in range(8):
        for x in range(8): top.p(x, y, HAIR1)
    S.paste_region(top, 8, 0); S.paste_region(top, 16, 0)
    # ---- casco de pelo: caja 8x8x8 en (32,0): top (40,0) front (40,8) right (32,8) left (48,8) back (56,8)
    hr = random.Random(5)
    tp = Sheet(8, 8)
    for y in range(8):
        for x in range(8):
            tp.p(x, y, hair_pixel(hr, x, y, 8, light=0.1 + (0.15 if (x in (3, 4) and y < 6) else 0)))
    S.paste_region(tp, 40, 0)
    for u, v in ((32, 8), (48, 8), (56, 8)):
        sd = Sheet(8, 8)
        for y in range(8):
            for x in range(8):
                sd.p(x, y, hair_pixel(hr, x + u, y, 8))
        if u != 56:
            # el pelo no llega a la cara: deja la zona de la cara/oreja libre abajo
            for y in range(5, 8):
                for x in range(8): sd.p(x, y, CLEAR)
            if u == 32:  # lado derecho (cara a la derecha)
                for y in range(5, 8):
                    for x in range(0, 3): pass
        S.paste_region(sd, u, v)
    # flequillo (frente)
    fr = Sheet(8, 8)
    for y in range(2):
        for x in range(8): fr.p(x, y, hair_pixel(hr, x, y, 4, 0.1))
    for x in (0, 7):
        for y in range(2, 5): fr.p(x, y, hair_pixel(hr, x, y, 6))
    fr.p(1, 2, hair_pixel(hr, 1, 2, 4)); fr.p(6, 2, hair_pixel(hr, 6, 2, 4))
    fr.p(3, 2, CLEAR); fr.p(4, 2, CLEAR)
    S.paste_region(fr, 40, 8)
    # ---- pelo trasero largo: caja 10x12x2 en (0,16): front (2,18) back (14,18)
    def long_hair(w, h, seed):
        L = Sheet(w, h)
        rr = random.Random(seed)
        for y in range(h):
            for x in range(w):
                L.p(x, y, hair_pixel(rr, x, y, h, 0.05))
        return L
    lh = long_hair(10, 12, 21)
    # mechones puntiagudos en el borde inferior
    for x in range(10):
        cut = [3, 1, 4, 0, 3, 5, 1, 4, 0, 2][x]
        for y in range(12 - cut, 12): lh.p(x, y, CLEAR)
    S.paste_region(lh, 2, 18); S.paste_region(lh, 14, 18)
    sidepix = Sheet(2, 12)
    for y in range(12):
        for x in range(2): sidepix.p(x, y, HAIR1)
    S.paste_region(sidepix, 0, 18); S.paste_region(sidepix, 12, 18)
    tb = Sheet(10, 2)
    for y in range(2):
        for x in range(10): tb.p(x, y, HAIR1)
    S.paste_region(tb, 2, 16); S.paste_region(tb, 12, 16)
    # ---- pelo trasero 2 (9x12x2) en (0,32): front (2,34) back (13,34)
    lh2 = long_hair(9, 12, 33)
    for x in range(9):
        cut = [6, 2, 7, 0, 5, 8, 2, 6, 3][x]
        for y in range(12 - cut, 12): lh2.p(x, y, CLEAR)
    S.paste_region(lh2, 2, 34); S.paste_region(lh2, 13, 34)
    sp2 = Sheet(2, 12)
    for y in range(12):
        for x in range(2): sp2.p(x, y, HAIR1)
    S.paste_region(sp2, 0, 34); S.paste_region(sp2, 11, 34)
    tb2 = Sheet(9, 2)
    for y in range(2):
        for x in range(9): tb2.p(x, y, HAIR1)
    S.paste_region(tb2, 2, 32); S.paste_region(tb2, 11, 32)
    # ---- mechon lateral (1x9x2 en (24,16)): front (26,18)... usamos region 6x11
    for u, v in ((24, 16),):
        lock = Sheet(6, 11)
        rr = random.Random(41)
        for y in range(11):
            for x in range(6):
                lock.p(x, y, hair_pixel(rr, x, y, 11, 0.05))
        S.paste_region(lock, u, v)
    # ---- pinchos de pelo (caja 2x6x2 en (48,16)): region 8x8
    tuft = Sheet(8, 8)
    rr = random.Random(55)
    for y in range(8):
        for x in range(8):
            tuft.p(x, y, hair_pixel(rr, x, y, 8, 0.12))
    S.paste_region(tuft, 48, 16)
    # ---- adorno de joya pequena (1x2x1 -> 4x3) en (56,16)
    g = Sheet(4, 3)
    for x in range(4):
        for y in range(3): g.p(x, y, RUBY1, RUBY1)
    S.paste_region(g, 56, 16)
    S.save('head')

# ======================================================================
# CUERPO: corpiño + faldas
# ======================================================================
def cloth(rnd, x, y, h, base=0.35):
    t = base + 0.4 * (y / max(1, h)) * 0.0 + rnd.uniform(-0.08, 0.08)
    return ramp_pick([WINE0, WINE1, WINE2, WINE3, WINE4], t, x, y)

def build_body():
    S = Sheet(128, 128)
    rr = random.Random(77)
    # ---- corpiño: caja 8x12x4 en (0,0): top(4,0) bottom(12,0) right(0,4) front(4,4) left(12,4) back(16,4)
    def bodice_front():
        f = Sheet(8, 12)
        for y in range(12):
            for x in range(8):
                t = 0.30 + 0.38 * (y / 11.0) + (0.1 if x in (3, 4) else 0)
                f.p(x, y, ramp_pick([WINE0, WINE1, WINE2, WINE3, WINE4], t + rr.uniform(-.05, .05), x, y))
        # costillas negras curvas (como el diseño)
        for y, span in ((2, 3), (4, 3), (6, 2), (8, 2)):
            for k in range(span):
                f.p(3 - k, y + (1 if k == span - 1 else 0), BLACK0)
                f.p(4 + k, y + (1 if k == span - 1 else 0), BLACK0)
        # columna vertebral negra
        for y in range(0, 12):
            f.p(3, y, BLACK1); f.p(4, y, BLACK1)
        # borde de cuello negro
        for x in range(8): f.p(x, 0, BLACK0)
        for y in range(12): 
            f.p(0, y, BLACK1); f.p(7, y, BLACK1)
        # joyas de la columna
        for y, big in ((1, False), (4, True), (7, True), (10, False)):
            f.p(3, y, RUBY2, RUBY2); f.p(4, y, RUBY1, RUBY1)
            if big:
                f.p(3, y + 1, RUBY1, RUBY1); f.p(4, y + 1, RUBY0, RUBY0)
                f.p(3, y, RUBY3, RUBY3)
        return f
    def bodice_back():
        f = Sheet(8, 12)
        for y in range(12):
            for x in range(8):
                t = 0.25 + 0.35 * (y / 11.0) + (0.07 if (x + y) % 4 == 0 else 0)
                f.p(x, y, ramp_pick([WINE0, WINE1, WINE2, WINE3, WINE4], t, x, y))
        for x in range(8): f.p(x, 0, BLACK0)
        for y in range(12): f.p(0, y, BLACK1); f.p(7, y, BLACK1)
        for y in range(1, 12, 3):
            for x in range(1, 7): f.p(x, y, BLACK1)   # cordones del corset
        for y in range(0, 12): f.p(3, y, BLACK0); f.p(4, y, BLACK0)
        return f
    def bodice_side():
        f = Sheet(4, 12)
        for y in range(12):
            for x in range(4):
                t = 0.3 + 0.35 * (y / 11.0)
                f.p(x, y, ramp_pick([WINE0, WINE1, WINE2, WINE3, WINE4], t, x, y))
        for x in range(4): f.p(x, 0, BLACK0)
        for y in range(12): f.p(0, y, BLACK1); f.p(3, y, BLACK1)
        return f
    S.paste_region(bodice_front(), 4, 4); S.paste_region(bodice_back(), 16, 4)
    S.paste_region(bodice_side(), 0, 4); S.paste_region(bodice_side(), 12, 4)
    tp = Sheet(8, 4)
    for y in range(4):
        for x in range(8): tp.p(x, y, BLACK0)
    S.paste_region(tp, 4, 0); S.paste_region(tp, 12, 0)
    # ---- cuello/hombros: caja 12x2x6 en (0,20): top(6,20) front(6,26) ... region 24x8 (mas pequeño que antes)
    def shoulder_part():
        r = Sheet(24, 8)
        for y in range(8):
            for x in range(24):
                r.p(x, y, ramp_pick([BLACK0, BLACK1, BLACK2, BLACK3], 0.28 + (0.35 if y < 6 and (x % 5 == 0) else 0) + (0.15 if y == 6 else 0), x, y))
        for x in (8, 15):
            pass
        return r
    S.paste_region(shoulder_part(), 0, 20)
    # ---- falda delantera: caja 10x12x1 en (0,30): front(1,31) back(12,31)  (region 22x13)
    def skirt_tex(w, h, front):
        k = Sheet(w, h)
        rnd = random.Random(w * 31 + (3 if front else 5))
        # contorno de los picos del dobladillo
        for y in range(h):
            for x in range(w):
                t = 0.30 + 0.5 * (y / (h - 1.0)) ** 1.2
                # degradado: mas claro hacia el medio de la falda, mas oscuro arriba
                t = 0.2 + 0.5 * math.sin(min(1.0, y / (h * 0.9)) * math.pi * 0.62)
                t += 0.05 * math.sin(x * 1.3)
                k.p(x, y, ramp_pick([WINE0, WINE1, WINE2, WINE3, WINE4], t, x, y))
        # pliegues verticales
        for x in range(2, w - 1, 3):
            for y in range(2, h - 1):
                if dither(x, y, 0.55): k.p(x, y, WINE1)
        # cinturon negro arriba
        for x in range(w):
            k.p(x, 0, BLACK0); k.p(x, 1, BLACK1)
        # dobladillo negro con picos
        picos = 2 if w <= 7 else 3
        pw = max(2, w // (picos * 2 + 1) * 1)
        cuts = []
        for x in range(w):
            ph = (x * 2 * picos // w) % 2
            # picos triangulares: la profundidad depende de la distancia al centro del pico
            seg = w / (picos * 2.0)
            c = ((x % (2 * seg)) / seg)
            tri = c if c <= 1 else 2 - c
            cuts.append(int(round((1.0 - tri) * 3)))
        for x in range(w):
            c = cuts[x]
            for y in range(h - 3, h):
                k.p(x, y, BLACK1)
            for y in range(h - c, h):
                k.p(x, y, CLEAR)
            k.p(x, h - 3 - 0, BLACK0 if True else BLACK1)
        for x in range(w):
            for y in range(h - 5, h - 3):
                if k.get(x, y)[3] and dither(x, y, 0.35): k.p(x, y, BLACK1)
        # costuras
        for y in range(2, h - 3): 
            k.p(w // 2 if w % 2 else w // 2 - 1, y, BLACK1)
        if front:
            gem(k, w // 2 - 1, h - 6)
        return k
    f = skirt_tex(10, 14, True)
    S.paste_region(f, 1, 31); S.paste_region(f, 12, 31)
    # falda trasera, mas larga: 12x14x1 en (0,46): front (1,47) back(14,47)
    b = skirt_tex(12, 15, False)
    S.paste_region(b, 1, 49); S.paste_region(b, 14, 49)
    # falda lateral: caja 1x12x7 en (0,64): right (0,7)->(0,71) 7x12 ; left (8,71)
    sd = skirt_tex(7, 14, False)
    S.paste_region(sd, 0, 73); S.paste_region(sd, 8, 73)
    S.save('body')

# ======================================================================
# GUANTELETES
# ======================================================================
def build_gauntlet():
    S = Sheet(64, 32)
    rr = random.Random(99)
    def black(x, y, hi=0.0):
        t = 0.25 + hi + rr.uniform(-.05, .08)
        return ramp_pick([BLACK0, BLACK1, BLACK2, BLACK3, GREY0], t, x, y)
    # caja 4x7x4 en (0,0): top(4,0) bottom(8,0) right(0,4) front(4,4) left(8,4) back(12,4)
    def face(w, h, stripe=True, big=False):
        f = Sheet(w, h)
        for y in range(h):
            for x in range(w):
                f.p(x, y, black(x, y, 0.2 if x == 1 else 0))
        if stripe:
            for x in range(w):
                f.p(x, 0, BLACK0)
                f.p(x, 2, RED0); f.p(x, 4, RED0)
                f.p(x, 6, BLACK0)
                f.p(x, 1, BLACK2); f.p(x, 3, BLACK1); f.p(x, 5, BLACK1)
            # venas rojas brillantes puntuales (como el original)
            for (x, y) in ((1, 2), (3, 4), (0, 4), (2, 2)):
                if x < w:
                    f.p(x, y, RED1, RED1)
        return f
    for (u, v, w, h) in ((0, 4, 4, 7), (4, 4, 4, 7), (8, 4, 4, 7), (12, 4, 4, 7)):
        S.paste_region(face(w, h), u, v)
    tp = Sheet(4, 4)
    for y in range(4):
        for x in range(4): tp.p(x, y, BLACK0)
    S.paste_region(tp, 4, 0); S.paste_region(tp, 8, 0)
    # pinchos: caja 2x4x2 en (16,0): region 8x6 ; punta 1x3x1 en (16,8): region 4x4
    sp = Sheet(8, 6)
    for y in range(6):
        for x in range(8): sp.p(x, y, black(x, y, 0.25 if y < 2 else 0))
    for x in range(8): sp.p(x, 5, BLACK0)
    S.paste_region(sp, 16, 0)
    tip = Sheet(4, 4)
    for y in range(4):
        for x in range(4): tip.p(x, y, black(x, y, 0.3 if y < 2 else 0.05))
    S.paste_region(tip, 16, 8)
    # nudillos: caja 3x2x1 en (24,0)
    kn = Sheet(8, 3)
    for y in range(3):
        for x in range(8): kn.p(x, y, black(x, y, 0.25))
    for x in range(8): kn.p(x, 2, RED1, RED1)
    S.paste_region(kn, 24, 0)
    S.save('gauntlet')

# ======================================================================
# ICONO DEL ITEM 16x16
# ======================================================================
def build_item():
    S = Sheet(16, 16)
    art = [
        "................",
        "..kk........kk..",
        ".kvvk......kvvk.",
        "kvmvvk....kvvmvk",
        "kvmmvvk..kvvmmvk",
        "kvvmmvvkkvvmmvvk",
        ".kvvmvvkrrkvvmvk",
        ".kkvvmkrRRrkmvkk",
        "..kvvvkrRPrkvvk.",
        "..kkvmkrRRrkmkk.",
        "...kvvkkrrkkvk..",
        "...kkvvkkkkvvk..",
        "....kkmvkkvmkk..",
        ".....kkvkkvkk...",
        "......kkkkkk....",
        "................",
    ]
    pal = {'k': BLACK0, 'v': MEM[2], 'm': VEIN1, 'r': RUBY0, 'R': RUBY1, 'P': RUBY3}
    for y, row in enumerate(art):
        for x, ch in enumerate(row):
            if ch in pal: S.p(x, y, pal[ch])
    # sombreado
    for (x, y) in ((2, 3), (3, 4), (4, 5), (13, 3), (12, 4), (11, 5)):
        S.p(x, y, MEM[3])
    for (x, y) in ((7, 7),):
        S.p(x, y, RUBY2)
    S.d.save(f'{OUT}/item_transformacion.png')

build_wings(); build_head(); build_body(); build_gauntlet(); build_item()
print('ok')
