"""Paso 5: efectos visuales procedurales (indices de paleta, pensados para
mezcla aditiva en MUGEN). Cada efecto es una lista de cuadros
(imagen, eje_x, eje_y, duracion)."""
import math
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import palette as PL
import parts as PT
import rig

HERE = os.path.dirname(os.path.abspath(__file__))
rng = np.random.default_rng(7)


class Field:
    """campo de intensidad 0..1 centrado en (cx, cy)."""
    def __init__(self, w, h, cx=None, cy=None):
        self.w, self.h = w, h
        self.cx = w // 2 if cx is None else cx
        self.cy = h // 2 if cy is None else cy
        self.f = np.zeros((h, w))
        self.yy, self.xx = np.mgrid[0:h, 0:w]
        self.xx = self.xx + 0.5 - self.cx
        self.yy = self.yy + 0.5 - self.cy

    def line(self, p0, p1, width, inten=1.0, glow=0.0):
        x0, y0 = p0
        x1, y1 = p1
        dx, dy = x1 - x0, y1 - y0
        L2 = dx * dx + dy * dy + 1e-9
        t = np.clip(((self.xx - x0) * dx + (self.yy - y0) * dy) / L2, 0, 1)
        d = np.hypot(self.xx - (x0 + t * dx), self.yy - (y0 + t * dy))
        v = np.clip(1 - (d - width / 2) / 1.0, 0, 1) * inten
        if glow:
            v = np.maximum(v, inten * 0.45 * np.exp(-np.maximum(d - width / 2, 0) / glow))
        self.f = np.maximum(self.f, v)

    def polyline(self, pts, width, inten=1.0, glow=0.0):
        for a, b in zip(pts[:-1], pts[1:]):
            self.line(a, b, width, inten, glow)

    def disc(self, c, r, inten=1.0, soft=1.0):
        d = np.hypot(self.xx - c[0], self.yy - c[1])
        self.f = np.maximum(self.f, np.clip((r - d) / soft, 0, 1) * inten)

    def glowdisc(self, c, r, inten=1.0):
        d = np.hypot(self.xx - c[0], self.yy - c[1])
        self.f = np.maximum(self.f, inten * np.clip(1 - d / r, 0, 1) ** 1.5)

    def ring(self, c, r, width, inten=1.0, sx=1.0, sy=1.0):
        d = np.hypot((self.xx - c[0]) / sx, (self.yy - c[1]) / sy)
        v = np.clip(1 - np.abs(d - r) / (width / 2), 0, 1) * inten
        self.f = np.maximum(self.f, v)

    def to_idx(self, ramp, thresh=0.07, gamma=1.0):
        v = np.clip(self.f, 0, 1) ** gamma
        start, n, _ = PL.RAMPS[ramp]
        idx = start + np.round(v * (n - 1)).astype(int)
        idx[v < thresh] = 0
        return idx

    def frame(self, ramp, t, thresh=0.07, gamma=1.0):
        return crop(self.to_idx(ramp, thresh, gamma), self.cx, self.cy, t)


def crop(img, cx, cy, t):
    ys, xs = np.nonzero(img)
    if len(ys) == 0:
        img = np.zeros((2, 2), int)
        img[0, 0] = PL.ramp_index('gray', 0.0)
        return (img, 1, 1, t)
    y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    return (img[y0:y1, x0:x1], cx - x0, cy - y0, t)


def bolt(p0, p1, rough=0.35, depth=5):
    pts = [np.array(p0, float), np.array(p1, float)]
    for _ in range(depth):
        new = [pts[0]]
        for a, b in zip(pts[:-1], pts[1:]):
            m = (a + b) / 2
            d = b - a
            n = np.array([-d[1], d[0]])
            m = m + n * rng.normal(0, rough * 0.5)
            new += [m, b]
        pts = new
        rough *= 0.6
    return [tuple(p) for p in pts]


FX = {}   # anim -> dict(frames, loop, trans)


def fx(no, frames, loop=None, trans='add', name=''):
    FX[no] = dict(frames=frames, loop=loop, trans=trans, name=name)


def build():
    # ---------------- 6000 chispa de corte
    frs = []
    for k, (L, w) in enumerate([(14, 3), (26, 4), (34, 3), (36, 2), (30, 1.2)]):
        f = Field(80, 80)
        a = math.radians(-35)
        d = np.array([math.cos(a), math.sin(a)])
        f.line(-d * L, d * L, w, 1.0, glow=3)
        f.line(np.array([-d[1], d[0]]) * L * 0.35, -np.array([-d[1], d[0]]) * L * 0.35, max(1, w - 2), 0.8, glow=2)
        if k >= 2:
            for _ in range(5):
                ang = rng.uniform(0, 2 * math.pi)
                r = rng.uniform(10, 26) * (k / 3)
                f.disc((math.cos(ang) * r, math.sin(ang) * r), 1.2, 0.9)
        frs.append(f.frame('trail', 2))
    fx(6000, frs, name='chispa corte')

    # ---------------- 6010 chispa fuerte (estallido)
    def burst(ramp, scale=1.0, n=6):
        out = []
        for k in range(n):
            f = Field(120, 120)
            t = (k + 1) / n
            R = (12 + 40 * t) * scale
            inten = 1.0 - 0.6 * t
            for i in range(10):
                a = i * math.pi / 5 + 0.2
                ln = R * (1.0 if i % 2 == 0 else 0.6)
                f.line((0, 0), (math.cos(a) * ln, math.sin(a) * ln), max(1, 5 * (1 - t)), inten, glow=3)
            f.ring((0, 0), R * 0.8, 3 * (1 - t) + 1, inten * 0.8)
            if k < 2:
                f.glowdisc((0, 0), 18 * scale, 1.0)
            out.append(f.frame(ramp, 2))
        return out
    fx(6010, burst('spark'), name='chispa fuerte')
    fx(6040, burst('fire', 1.15), name='chispa roja')

    # ---------------- 6020 chispa de guardia
    frs = []
    for k in range(5):
        f = Field(80, 100)
        t = (k + 1) / 5
        f.ring((-10, 0), 18 + 14 * t, 4 * (1 - t) + 1.5, 1 - 0.5 * t, sx=0.45, sy=1.0)
        if k < 2:
            f.glowdisc((0, 0), 16, 1)
        frs.append(f.frame('trail', 2))
    fx(6020, frs, name='chispa guardia')

    # ---------------- 6030 chispa electrica
    frs = []
    for k in range(6):
        f = Field(110, 110)
        t = (k + 1) / 6
        for i in range(7):
            a = rng.uniform(0, 2 * math.pi)
            R = rng.uniform(20, 46) * (0.6 + 0.6 * t)
            f.polyline(bolt((0, 0), (math.cos(a) * R, math.sin(a) * R), 0.4, 4), 1.4, 1 - 0.5 * t, glow=2.5)
        if k < 3:
            f.glowdisc((0, 0), 22, 1.0)
        frs.append(f.frame('bolt', 2))
    fx(6030, frs, name='chispa electrica')

    # ---------------- 6100 Storm Hammer (guantelete de rayo)
    fist = PT.get_parts()['fist']
    pal = PL.get()
    frs = []
    for k in range(4):
        f = Field(90, 70)
        f.glowdisc((0, 0), 30, 0.75)
        for i in range(3):
            a = rng.uniform(0, 2 * math.pi)
            f.polyline(bolt((math.cos(a) * 8, math.sin(a) * 8), (math.cos(a) * 30, math.sin(a) * 26), 0.5, 4),
                       1.2, 0.95, glow=2)
        # estela
        for j in range(4):
            f.line((-14 - j * 8, rng.uniform(-6, 6)), (-34 - j * 8, rng.uniform(-6, 6)), 1.5, 0.6 - j * 0.12, glow=2)
        idx = f.to_idx('bolt')
        # guantelete encima (puño agrandado y teñido)
        fimg = fist.img
        S = 2.6
        h, w = fimg.shape
        for yy in range(int(h * S)):
            for xx in range(int(w * S)):
                v = fimg[int(yy / S), int(xx / S)]
                if v:
                    lum = (pal[v] @ [0.3, 0.59, 0.11]) / 255
                    X = int(f.cx - w * S / 2 + xx)
                    Y = int(f.cy - h * S / 2 + yy)
                    if lum < 0.08:
                        idx[Y, X] = PL.ramp_index('bolt', 0.2)
                    else:
                        idx[Y, X] = PL.ramp_index('bolt', 0.45 + 0.55 * min(1, lum * 1.6))
        frs.append(crop(idx, f.cx, f.cy, 3))
    fx(6100, frs, loop=0, name='storm hammer')

    # ---------------- 6110 explosion del Storm Hammer (area)
    frs = []
    for k in range(9):
        f = Field(200, 180, 100, 100)
        t = (k + 1) / 9
        R = 20 + 70 * t
        if k < 3:
            f.glowdisc((0, 0), 40 + 20 * k, 1.0)
        f.ring((0, 0), R, 6 * (1 - t) + 2, 1 - 0.6 * t, sx=1.0, sy=0.75)
        for i in range(9):
            a = i * 2 * math.pi / 9 + rng.uniform(-0.2, 0.2)
            f.polyline(bolt((math.cos(a) * R * 0.3, math.sin(a) * R * 0.22),
                            (math.cos(a) * R * 1.05, math.sin(a) * R * 0.8), 0.45, 4), 1.6, 1 - 0.55 * t, glow=2.5)
        frs.append(f.frame('bolt', 3))
    fx(6110, frs, name='explosion de rayo')

    # ---------------- 6120 rayo del cielo
    frs = []
    for k in range(5):
        f = Field(90, 300, 45, 290)
        pts = bolt((rng.uniform(-10, 10), -290), (0, 0), 0.25, 6)
        f.polyline(pts, 4 if k < 3 else 2, 1.0, glow=5)
        for _ in range(3):
            i = rng.integers(10, len(pts) - 10)
            p = pts[i]
            f.polyline(bolt(p, (p[0] + rng.uniform(-40, 40), p[1] + rng.uniform(10, 50)), 0.4, 4), 1.3, 0.8, glow=2)
        f.glowdisc((0, 0), 30, 0.9)
        frs.append(f.frame('bolt', 2))
    fx(6120, frs, name='rayo del cielo')

    # ---------------- 6130 aturdimiento electrico (sobre el rival)
    frs = []
    for k in range(4):
        f = Field(70, 90, 35, 80)
        for i in range(4):
            x = rng.uniform(-25, 25)
            y = rng.uniform(-70, -10)
            f.polyline(bolt((x, y), (x + rng.uniform(-14, 14), y + rng.uniform(-16, 16)), 0.5, 3), 1.2, 0.95, glow=2)
        frs.append(f.frame('bolt', 3))
    fx(6130, frs, loop=0, name='aturdido')

    # ---------------- 6140 carga en la mano
    frs = []
    for k in range(3):
        f = Field(40, 40)
        f.glowdisc((0, 0), 12 + k * 2, 0.9)
        for i in range(3):
            a = rng.uniform(0, 2 * math.pi)
            f.polyline(bolt((0, 0), (math.cos(a) * 16, math.sin(a) * 16), 0.5, 3), 1.0, 0.9, glow=1.5)
        frs.append(f.frame('bolt', 2))
    fx(6140, frs, loop=0, name='carga mano')

    # ---------------- 6200 onda de Great Cleave
    def crescent(f, R, th, inten, sy=1.0):
        d = np.hypot(f.xx + R * 0.55, f.yy / sy)
        ang = np.arctan2(f.yy / sy, f.xx + R * 0.55)
        band = np.clip(1 - np.abs(d - R) / th, 0, 1)
        taper = np.clip(1 - np.abs(ang) / 1.25, 0, 1) ** 0.7
        f.f = np.maximum(f.f, band * taper * inten)
    frs = []
    for k in range(4):
        f = Field(100, 130, 60, 110)
        f.yy = f.yy + 52
        crescent(f, 46, 10, 1.0)
        crescent(f, 40, 5, 0.55 + 0.1 * (k % 2))
        frs.append(crop(f.to_idx('trail', 0.1), f.cx, f.cy, 2))
    fx(6200, frs, loop=0, name='onda cleave')
    frs = []
    for k in range(4):
        f = Field(100, 130, 60, 110)
        f.yy = f.yy + 52
        crescent(f, 46 + 6 * k, 10 - 2 * k, 1.0 - 0.22 * k)
        frs.append(crop(f.to_idx('trail', 0.1), f.cx, f.cy, 2))
    fx(6210, frs, name='onda cleave fin')
    # version roja (God's Strength)
    fx(6220, [(np.where(a > 0, a - PL.RAMPS['trail'][0] + PL.RAMPS['fire'][0], 0), x, y, t)
              for a, x, y, t in FX[6200]['frames']], loop=0, name='onda roja')

    # ---------------- 6300 anillo del Warcry
    frs = []
    for k in range(8):
        f = Field(260, 160, 130, 110)
        t = (k + 1) / 8
        f.ring((0, -40), 20 + 100 * t, 6 * (1 - t) + 2, 1 - 0.7 * t, sx=1.0, sy=0.55)
        f.ring((0, -40), 10 + 70 * t, 3 * (1 - t) + 1, 0.8 - 0.6 * t, sx=1.0, sy=0.55)
        frs.append(f.frame('gold', 2))
    fx(6300, frs, name='anillo warcry')
    # escudo del Warcry (anillo translucido con destellos que giran)
    frs = []
    for k in range(4):
        f = Field(110, 140, 55, 136)
        c = (0, -64)
        d = np.hypot((f.xx - c[0]) / 46, (f.yy - c[1]) / 64)
        ang = np.arctan2(f.yy - c[1], f.xx - c[0])
        edge = np.clip(1 - np.abs(d - 1) / 0.06, 0, 1)
        glint = 0.35 + 0.35 * np.clip(np.cos(3 * ang + k * math.pi / 2), 0, 1) ** 4
        f.f = edge * glint
        frs.append(f.frame('trail', 4, thresh=0.12))
    fx(6310, frs, loop=0, name='escudo warcry')

    # ---------------- 6400 aura de God's Strength (llamas rojas)
    frs = []
    for k in range(6):
        f = Field(130, 170, 65, 160)
        for i in range(26):
            x = rng.uniform(-44, 44)
            base = -abs(x) * 0.25 - rng.uniform(0, 20)
            h = rng.uniform(40, 120) * (1 - abs(x) / 70)
            wob = rng.uniform(-8, 8)
            pts = [(x + wob * math.sin(j * 0.9 + k), base - h * j / 6) for j in range(7)]
            for j, (a, b) in enumerate(zip(pts[:-1], pts[1:])):
                f.line(a, b, 7 * (1 - j / 7) + 1, 0.55 * (1 - j / 8), glow=3)
        frs.append(f.frame('fire', 3, thresh=0.08))
    fx(6400, frs, loop=0, name='aura dios')
    frs = []
    for k in range(7):
        f = Field(260, 260, 130, 170)
        t = (k + 1) / 7
        if k < 3:
            f.glowdisc((0, -50), 60 + 20 * k, 1.0)
        f.ring((0, -50), 20 + 100 * t, 8 * (1 - t) + 2, 1 - 0.6 * t)
        for i in range(12):
            a = i * math.pi / 6
            f.line((math.cos(a) * 30 * t, -50 + math.sin(a) * 30 * t),
                   (math.cos(a) * 120 * t, -50 + math.sin(a) * 120 * t), 3 * (1 - t) + 1, 0.9 - 0.6 * t)
        frs.append(f.frame('fire', 3))
    fx(6410, frs, name='estallido dios')

    # ---------------- 6420/6421 ojos de God's Strength: brillo + destello
    for no, side in ((6420, -1), (6421, 1)):
        frs = []
        for k, (L, inten) in enumerate([(3, .7), (6, .95), (9, 1.0), (7, .85), (4, .6), (2, .4)]):
            f = Field(40, 40)
            for ex in (-3.5, 3.5):
                f.glowdisc((ex, 0), 4.5, 0.75)
            cx = 3.5 * side
            a = math.radians(15 * k)
            for i in range(4):
                ang = a + i * math.pi / 2
                f.line((cx, 0), (cx + math.cos(ang) * L, math.sin(ang) * L), 1.2, inten, glow=1.2)
            f.glowdisc((cx, 0), 3.5, inten)
            frs.append(f.frame('gold', 2))
        fx(no, frs, name='ojos dios')

    # ---------------- 6500 polvo / 6510 grieta y onda en el suelo
    frs = []
    for k in range(7):
        f = Field(120, 60, 60, 56)
        t = (k + 1) / 7
        for i in range(7):
            x = (i - 3) * 9 * (0.6 + t)
            f.glowdisc((x, -8 - 12 * t - abs(i - 3) * 2), 14 + 10 * t, 0.9 - 0.7 * t)
        frs.append(f.frame('dust', 3, thresh=0.12))
    fx(6500, frs, trans='none', name='polvo')
    frs = []
    for k in range(8):
        f = Field(240, 90, 120, 80)
        t = (k + 1) / 8
        f.ring((0, 0), 20 + 100 * t, 5 * (1 - t) + 2, 1 - 0.7 * t, sx=1.0, sy=0.18)
        for i in range(8):
            x = rng.uniform(-30, 30) * (1 + 2 * t)
            y = -rng.uniform(5, 50) * math.sin(math.pi * min(1, t * 1.2))
            f.disc((x, y), 2.2, 0.9 - 0.5 * t)
        if k < 3:
            f.glowdisc((0, -6), 40, 0.8)
        frs.append(f.frame('spark', 3))
    fx(6510, frs, name='onda suelo')

    # ---------------- 6600 aura de carga de poder
    frs = []
    for k in range(4):
        f = Field(130, 160, 65, 150)
        for i in range(14):
            x = rng.uniform(-40, 40)
            h = rng.uniform(40, 110)
            f.line((x, -rng.uniform(0, 10)), (x + rng.uniform(-6, 6), -h), 3, 0.5, glow=3)
        for i in range(3):
            x = rng.uniform(-35, 35)
            y = rng.uniform(-110, -20)
            f.polyline(bolt((x, y), (x + rng.uniform(-20, 20), y + rng.uniform(-25, 25)), 0.5, 3), 1.2, 1.0, glow=2)
        frs.append(f.frame('bolt', 3))
    fx(6600, frs, loop=0, name='aura carga')


def cutin(title, sub):
    """panel de 'koma' estilo JUS con retrato y nombre del ataque."""
    W, H = 320, 96
    pal = PL.get()
    img = Image.new('RGB', (W, H), (250, 250, 250))
    d = ImageDraw.Draw(img)
    r = np.random.default_rng(3)
    for i in range(70):          # lineas de velocidad
        y = r.uniform(0, H)
        x0 = r.uniform(0, W * 0.5)
        d.line([(x0, y), (W, y + r.uniform(-4, 4))], fill=(180, 190, 215), width=int(r.integers(1, 3)))
    ref = cut_ref((230, 200, 230))
    face = ref.crop((250, 196, 560, 406))
    face = face.resize((180, 120), Image.LANCZOS)
    img.paste(face, (6, -14))
    d.polygon([(150, 0), (W, 0), (W, H), (128, H)], fill=(18, 20, 40))
    def fit(text, size, maxw):
        path = '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
        while size > 7:
            try:
                fnt = ImageFont.truetype(path, size)
            except OSError:
                return ImageFont.load_default()
            if d.textlength(text, font=fnt) <= maxw:
                return fnt
            size -= 1
        return fnt
    font = fit(title, 18, 156)
    font2 = fit(sub, 12, 150)
    d.text((154, 28), title, fill=(255, 210, 70), font=font)
    d.text((158, 56), sub, fill=(200, 215, 255), font=font2)
    d.rectangle([0, 0, W - 1, H - 1], outline=(0, 0, 0), width=4)
    d.line([(148, 0), (126, H)], fill=(0, 0, 0), width=4)
    arr = np.asarray(img).astype(int)
    lo, hi = 1, 72
    p = pal[lo:hi + 1]
    extra = list(range(180, 196)) + list(range(228, 256))
    cand = np.array(list(range(lo, hi + 1)) + extra)
    pc = pal[cand]
    flat = arr.reshape(-1, 3)
    dd = ((flat[:, None, :] - pc[None]) ** 2).sum(2)
    idx = cand[dd.argmin(1)].reshape(H, W)
    return (idx, 0, 0, 1)


def cut_ref(bgcol):
    """referencia espejada con el fondo sustituido (degradado azul oscuro o color)."""
    ref = Image.open(os.path.join(HERE, 'reference.jpg')).convert('RGB').transpose(Image.FLIP_LEFT_RIGHT)
    bg = np.load(os.path.join(HERE, 'build', 'bg_mask.npy'))
    a = np.asarray(ref).astype(float)
    if bgcol is None:
        g = np.linspace(0, 1, 1024)[:, None, None]
        back = (1 - g) * np.array([40, 60, 120]) + g * np.array([8, 10, 30])
        back = np.broadcast_to(back, a.shape)
    else:
        back = np.broadcast_to(np.array(bgcol, float), a.shape)
    a = np.where(bg[..., None], back, a)
    return Image.fromarray(a.astype(np.uint8))


def portraits():
    pal = PL.get()
    ref = cut_ref(None)
    # en la imagen espejada la cabeza esta en x ~ 1024-800..1024-380
    big = ref.crop((1024 - 790, 190, 1024 - 410, 633)).resize((120, 140), Image.LANCZOS)
    small = ref.crop((1024 - 700, 200, 1024 - 520, 380)).resize((25, 25), Image.LANCZOS)
    out = []
    for im in (small, big):
        arr = np.asarray(im).astype(int)
        p = pal[1:73]
        dd = ((arr.reshape(-1, 3)[:, None, :] - p[None]) ** 2).sum(2)
        idx = (dd.argmin(1) + 1).reshape(arr.shape[:2])
        idx[0, :] = idx[-1, :] = 1
        idx[:, 0] = idx[:, -1] = 1
        out.append(idx)
    return out


if __name__ == '__main__':
    build()
    pal = PL.get().copy()
    pal[0] = (20, 20, 30)
    rows = []
    for no, d in FX.items():
        ims = [Image.fromarray(pal[a].astype(np.uint8)) for a, x, y, t in d['frames']]
        h = max(i.height for i in ims)
        w = sum(i.width + 4 for i in ims) + 50
        row = Image.new('RGB', (w, h + 4), (40, 40, 50))
        ImageDraw.Draw(row).text((2, 2), str(no), fill=(255, 255, 0))
        x = 50
        for i in ims:
            row.paste(i, (x, 2))
            x += i.width + 4
        rows.append(row)
    W = max(r.width for r in rows)
    Hh = sum(r.height for r in rows)
    sh = Image.new('RGB', (W, Hh), (40, 40, 50))
    y = 0
    for r in rows:
        sh.paste(r, (0, y))
        y += r.height
    sh.save(os.path.join(HERE, 'build', 'fx_sheet.png'))
    c = cutin("GOD'S STRENGTH", 'Fuerza de los Dioses')
    Image.fromarray(PL.get()[c[0]].astype(np.uint8)).save(os.path.join(HERE, 'build', 'cutin.png'))
    s, b = portraits()
    Image.fromarray(PL.get()[b].astype(np.uint8)).resize((240, 280), Image.NEAREST).save(os.path.join(HERE, 'build', 'portrait.png'))
    print(sh.size)
