#!/usr/bin/env python3
"""Genera las texturas de El Retorcido (128x128) con Pillow.

Las regiones UV se leen directamente de RetorcidoModel.java (bloque //@UV-BEGIN .. //@UV-END),
asi las texOffs del modelo y la textura nunca se desincronizan. El prefijo del nombre de la
region elige el material: S = piel enferma, B = hueso, D = hueco oscuro, E = ojo, M = interior de
boca, R = sangre, T = diente manchado (T_UP/T_FANG cuelgan del hocico, T_LO/T_FANL suben de la
mandibula), L = lengua, F = carne desgarrada. Piel de mandibula, barbilla y cuello lleva sangre.

Uso:  python tools/make_textures.py        (necesita: pip install pillow)
Salida: src/main/resources/assets/retorcido/textures/entity/retorcido.png y retorcido_eyes.png
"""
import math
import random
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/ricardo/retorcido/client/RetorcidoModel.java"
OUT = ROOT / "src/main/resources/assets/retorcido/textures/entity"
SIZE = 128


# ---------------------------------------------------------------- regiones UV
def read_regions():
    text = JAVA.read_text(encoding="utf-8")
    block = text.split("//@UV-BEGIN", 1)[1].split("//@UV-END", 1)[0]
    pat = re.compile(r"UV_(\w+)\s*=\s*\{\s*(\d+),\s*(\d+),\s*(\d+),\s*(\d+),\s*(\d+)\s*\}")
    regions = []
    for m in pat.finditer(block):
        name = m.group(1)
        u, v, w, h, d = (int(m.group(i)) for i in range(2, 7))
        regions.append(dict(name=name, u=u, v=v, w=w, h=h, d=d,
                            W=2 * (w + d), H=h + d))
    return regions


def check_regions(regions):
    ok = True
    for i, r in enumerate(regions):
        if r["u"] + r["W"] > SIZE or r["v"] + r["H"] > SIZE:
            print(f"ERROR: {r['name']} se sale de {SIZE}x{SIZE}")
            ok = False
        for q in regions[:i]:
            if (r["u"] < q["u"] + q["W"] and q["u"] < r["u"] + r["W"]
                    and r["v"] < q["v"] + q["H"] and q["v"] < r["v"] + r["H"]):
                print(f"ERROR: {r['name']} se solapa con {q['name']}")
                ok = False
    return ok


# ---------------------------------------------------------------- ruido
def _hash(ix, iy, seed):
    n = (ix * 374761393 + iy * 668265263 + seed * 1442695040888963407) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def vnoise(x, y, seed):
    ix, iy = math.floor(x), math.floor(y)
    fx, fy = x - ix, y - iy
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a = _hash(ix, iy, seed)
    b = _hash(ix + 1, iy, seed)
    c = _hash(ix, iy + 1, seed)
    d = _hash(ix + 1, iy + 1, seed)
    return (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy


def fbm(x, y, seed, octaves=3):
    total, amp, freq, norm = 0.0, 1.0, 1.0, 0.0
    for o in range(octaves):
        total += vnoise(x * freq, y * freq, seed + o * 17) * amp
        norm += amp
        amp *= 0.5
        freq *= 2.0
    return total / norm


def mix(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c)


# ---------------------------------------------------------------- materiales
SKIN_BASE = (66, 55, 72)       # gris violaceo oscuro
SKIN_LIGHT = (92, 80, 98)
SKIN_GREEN = (68, 78, 70)      # manchas verdosas de podredumbre
SKIN_BRUISE = (82, 48, 86)     # morados
VEIN_RED = (120, 36, 60)
VEIN_BLUE = (52, 62, 112)
BONE = (208, 200, 178)
BONE_DARK = (150, 136, 108)
BONE_STAIN = (128, 100, 74)


BLOOD_A = (107, 10, 10)        # 0x6b0a0a
BLOOD_B = (139, 0, 0)          # 0x8b0000
BLOOD_CLOT = (26, 2, 4)        # coagulos casi negros
BLOODY_SKIN = ("S_JAW", "S_CHIN", "S_NECK")


def blood_px(rng):
    k = rng.random()
    if k < 0.12:
        return BLOOD_CLOT
    return mix(BLOOD_A, BLOOD_B, rng.random())


def paint_blood_overlay(img, r, rng, amount, top_heavy=True):
    """Manchas y regueros de sangre: nacen arriba de cada cara y bajan con largos distintos."""
    px = img.load()
    d, w, h = r["d"], r["w"], r["h"]
    # caras laterales/frontal/trasera: fila v+d .. v+d+h ; columnas u .. u+W
    y0, y1 = r["v"] + d, r["v"] + d + h
    for x in range(r["u"], r["u"] + r["W"]):
        if rng.random() < amount:
            ln = rng.randrange(max(1, h // 3), h + 1)
            for k in range(ln):
                y = y0 + k
                if y < y1:
                    px[x, y] = blood_px(rng) + (255,)
            if ln < h and rng.random() < 0.7:  # gota gorda al final del reguero
                px[x, y0 + ln - 1] = BLOOD_CLOT + (255,)
    # cara superior e inferior (v .. v+d): salpicaduras
    for y in range(r["v"], r["v"] + d):
        for x in range(r["u"] + d, r["u"] + d + 2 * w):
            if rng.random() < amount * 0.8:
                px[x, y] = blood_px(rng) + (255,)
    # manchas sueltas
    for _ in range(int(r["W"] * r["H"] * amount * 0.12)):
        sx = rng.randrange(r["u"], r["u"] + r["W"])
        sy = rng.randrange(r["v"], r["v"] + r["H"])
        px[sx, sy] = blood_px(rng) + (255,)


def paint_mouth(img, r, rng):
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            n = fbm(x * 0.6, y * 0.6, 91)
            c = mix((14, 0, 2), (74, 6, 10), n * 1.2 - 0.1)
            if rng.random() < 0.08:  # brillos humedos
                c = (120, 24, 28)
            px[x, y] = c + (255,)


def paint_blood(img, r, rng):
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            c = blood_px(rng)
            t = (y - r["v"]) / max(1, r["H"] - 1)
            if r["name"] == "R_BLOOD":
                c = shade(c, 1.0 - 0.45 * t)  # la punta de la gota mas oscura
            if rng.random() < 0.06:
                c = (170, 20, 20)  # brillo humedo
            px[x, y] = c + (255,)


def paint_tooth(img, r, rng):
    px = img.load()
    lower = r["name"] in ("T_LO", "T_FANL")  # raiz abajo (v alto) en los inferiores
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            t = (y - r["v"]) / max(1, r["H"] - 1)
            root = t if lower else 1.0 - t   # 1 en la raiz, 0 en la punta
            c = mix((214, 200, 160), (150, 128, 84), rng.random() * 0.6)
            stain = root * (1.25 if lower else 0.9) + (rng.random() - 0.5) * 0.5
            if stain > 0.55:
                c = blood_px(rng) if stain > 0.7 else mix(c, BLOOD_B, 0.6)
            elif rng.random() < 0.15:
                c = mix(c, BONE_STAIN, 0.7)
            px[x, y] = c + (255,)


def paint_tongue(img, r, rng):
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            n = fbm(x * 0.7, y * 0.7, 55)
            c = mix((150, 52, 78), (92, 20, 44), n)
            if (x + 2 * y) % 5 == 0:
                c = shade(c, 0.7)  # surcos
            if rng.random() < 0.10:
                c = blood_px(rng)
            px[x, y] = c + (255,)


def paint_flesh(img, r, rng):
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            n = fbm(x * 0.8, y * 0.8, 33)
            c = mix((140, 24, 30), (70, 8, 14), n * 1.3)
            if rng.random() < 0.12:
                c = (178, 120, 110)  # tendon / grasa
            elif rng.random() < 0.10:
                c = BLOOD_CLOT
            px[x, y] = c + (255,)


def paint_skin(img, r, rng):
    seed = sum(ord(ch) for ch in r["name"]) * 7 + 3
    # variantes por pieza para que no se vea repetido
    sickly = 0.55 if r["name"] in ("S_BELLY", "S_NECK", "S_JAW", "S_LUMP") else 0.25
    dark = 0.8 if r["name"] in ("S_HAND", "S_FOOT", "S_ARM_F", "S_SHIN") else 1.0
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            nx, ny = (x + seed) * 0.42, (y + seed * 0.5) * 0.42
            base = mix(SKIN_BASE, SKIN_LIGHT, fbm(nx, ny, seed) * 1.1 - 0.15)
            blot = fbm(nx * 0.55 + 9, ny * 0.55 + 4, seed + 5)
            if blot > 0.58 - sickly * 0.25:
                base = mix(base, SKIN_GREEN, (blot - 0.45) * 3.0)
            elif blot < 0.30:
                base = mix(base, SKIN_BRUISE, (0.30 - blot) * 4.0)
            grain = (rng.random() - 0.5) * 16
            c = shade(base, dark)
            c = tuple(max(0, min(255, int(v + grain))) for v in c)
            # manchas oscuras / poros
            if rng.random() < 0.035:
                c = shade(c, 0.6)
            px[x, y] = c + (255,)
    # venas: caminatas aleatorias finas, rojas y azuladas
    for _ in range(max(2, (r["W"] * r["H"]) // 40)):
        vx = rng.randrange(r["u"], r["u"] + r["W"])
        vy = rng.randrange(r["v"], r["v"] + r["H"])
        col = VEIN_RED if rng.random() < 0.6 else VEIN_BLUE
        dx, dy = rng.choice([(1, 0), (0, 1), (1, 1), (-1, 1)])
        for _step in range(rng.randrange(6, 16)):
            if not (r["u"] <= vx < r["u"] + r["W"] and r["v"] <= vy < r["v"] + r["H"]):
                break
            px[vx, vy] = col + (255,)
            ax, ay = vx + dy, vy + dx  # sombra a un lado de la vena
            if r["u"] <= ax < r["u"] + r["W"] and r["v"] <= ay < r["v"] + r["H"]:
                old = px[ax, ay]
                px[ax, ay] = shade(old[:3], 0.72) + (255,)
            if rng.random() < 0.35:
                dx, dy = rng.choice([(1, 0), (0, 1), (1, 1), (-1, 1), (1, -1)])
            vx += dx
            vy += dy
    # borde de cada region ligeramente oscurecido (suciedad en las costuras de las caras)
    for x in range(r["u"], r["u"] + r["W"]):
        for y in (r["v"] + r["H"] - 1,):
            px[x, y] = shade(px[x, y][:3], 0.8) + (255,)


def paint_bone(img, r, rng):
    seed = sum(ord(ch) for ch in r["name"]) * 11 + 1
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            n = fbm((x + seed) * 0.5, (y + seed) * 0.5, seed)
            c = mix(BONE, BONE_DARK, (n - 0.35) * 1.6)
            stain = fbm((x + 40) * 0.3, (y + 11) * 0.3, seed + 9)
            if stain > 0.6:
                c = mix(c, BONE_STAIN, (stain - 0.6) * 2.5)
            # los extremos de los huesos finos amarillean/oscurecen (v crece hacia la punta)
            t = (y - r["v"]) / max(1, r["H"] - 1)
            c = shade(c, 1.0 - 0.18 * t)
            grain = (rng.random() - 0.5) * 14
            c = tuple(max(0, min(255, int(v + grain))) for v in c)
            px[x, y] = c + (255,)
    # grietas
    for _ in range(max(1, (r["W"] * r["H"]) // 60)):
        vx = rng.randrange(r["u"], r["u"] + r["W"])
        vy = rng.randrange(r["v"], r["v"] + r["H"])
        for _s in range(rng.randrange(3, 8)):
            if not (r["u"] <= vx < r["u"] + r["W"] and r["v"] <= vy < r["v"] + r["H"]):
                break
            px[vx, vy] = (92, 80, 64, 255)
            vx += rng.choice((-1, 0, 1))
            vy += 1


def paint_dark(img, r, rng):
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            k = rng.random() * 8
            px[x, y] = (int(10 + k), int(5 + k * 0.5), int(9 + k), 255)


def paint_eye_base(img, r, rng):
    """Base del ojo: rojo; en la cara frontal una pupila vertical negra (ranura) en la columna central."""
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            px[x, y] = (110 + rng.randrange(20), 20, 16, 255)
    fx, fy, fw, fh = front_face(r)
    for y in range(fy, fy + fh):
        px[fx + fw // 2, y] = (0, 0, 0, 255)


def front_face(r):
    """Cara frontal (-Z) del cubo: (u + d, v + d, w, h)."""
    return r["u"] + r["d"], r["v"] + r["d"], r["w"], r["h"]


def paint_eyes_layer(img, regions):
    """Capa emisiva: transparente salvo ojos (iris rojo-blanco, pupila transparente = negra de la base)
    y ojeras debiles alrededor de las cuencas."""
    px = img.load()
    for r in regions:
        if r["name"].startswith("E_"):
            for y in range(r["v"], r["v"] + r["H"]):
                for x in range(r["u"], r["u"] + r["W"]):
                    px[x, y] = (255, 36, 24, 255)
            fx, fy, fw, fh = front_face(r)
            pc = fx + fw // 2
            for y in range(fy, fy + fh):
                for x in range(fx, fx + fw):
                    if x == pc:
                        px[x, y] = (0, 0, 0, 0)  # ranura de la pupila: no brilla, se ve la base negra
                    elif abs(x - pc) == 1 and fw >= 3:
                        px[x, y] = (255, 84, 60, 255)   # iris blanco-rojizo junto a la pupila
                    else:
                        px[x, y] = (255, 70, 50, 255)
            px[fx, fy] = (255, 240, 225, 255)  # brillo blanco
        elif r["name"].startswith("D_"):
            fx, fy, fw, fh = front_face(r)
            for y in range(fy, fy + fh):
                for x in range(fx, fx + fw):
                    edge = x in (fx, fx + fw - 1) or y in (fy, fy + fh - 1)
                    if edge:
                        px[x, y] = (120, 8, 10, 255)


# ---------------------------------------------------------------- main
def main():
    regions = read_regions()
    if not regions:
        print("ERROR: no se encontraron regiones UV en", JAVA)
        return 1
    if not check_regions(regions):
        return 1
    rng = random.Random(1337)

    tex = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for r in regions:
        kind = r["name"][0]
        if kind == "S":
            paint_skin(tex, r, rng)
            if r["name"] in BLOODY_SKIN:
                paint_blood_overlay(tex, r, rng, 0.55 if r["name"] != "S_NECK" else 0.30)
        elif kind == "B":
            paint_bone(tex, r, rng)
        elif kind == "D":
            paint_dark(tex, r, rng)
        elif kind == "E":
            paint_eye_base(tex, r, rng)
        elif kind == "M":
            paint_mouth(tex, r, rng)
        elif kind == "R":
            paint_blood(tex, r, rng)
        elif kind == "T":
            paint_tooth(tex, r, rng)
        elif kind == "L":
            paint_tongue(tex, r, rng)
        elif kind == "F":
            paint_flesh(tex, r, rng)
        else:
            print("AVISO: material desconocido para", r["name"])
    eyes = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    paint_eyes_layer(eyes, regions)

    OUT.mkdir(parents=True, exist_ok=True)
    tex.save(OUT / "retorcido.png")
    eyes.save(OUT / "retorcido_eyes.png")
    print(f"OK: {len(regions)} regiones UV, todas dentro de {SIZE}x{SIZE} y sin solaparse")
    print("  ->", OUT / "retorcido.png")
    print("  ->", OUT / "retorcido_eyes.png")
    return 0


if __name__ == "__main__":
    sys.exit(main())
