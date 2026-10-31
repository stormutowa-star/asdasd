#!/usr/bin/env python3
"""Genera las texturas de El Retorcido (128x128) con Pillow.

Las regiones UV se leen directamente de RetorcidoModel.java (bloque //@UV-BEGIN .. //@UV-END),
asi las texOffs del modelo y la textura nunca se desincronizan. El prefijo del nombre de la
region elige el material: S = piel enferma, B = hueso, D = hueco oscuro, E = ojo.

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
    px = img.load()
    for y in range(r["v"], r["v"] + r["H"]):
        for x in range(r["u"], r["u"] + r["W"]):
            px[x, y] = (110 + rng.randrange(20), 20, 16, 255)


def front_face(r):
    """Cara frontal (-Z) del cubo: (u + d, v + d, w, h)."""
    return r["u"] + r["d"], r["v"] + r["d"], r["w"], r["h"]


def paint_eyes_layer(img, regions):
    """Capa emisiva: transparente salvo ojos (rojo/blanco) y ojeras debiles alrededor de las cuencas."""
    px = img.load()
    for r in regions:
        if r["name"].startswith("E_"):
            # todas las caras del ojo brillan en rojo; el frente lleva un nucleo blanco
            for y in range(r["v"], r["v"] + r["H"]):
                for x in range(r["u"], r["u"] + r["W"]):
                    px[x, y] = (255, 36, 24, 255)
            fx, fy, fw, fh = front_face(r)
            for y in range(fy, fy + fh):
                for x in range(fx, fx + fw):
                    px[x, y] = (255, 70, 50, 255)
            px[fx, fy] = (255, 235, 210, 255)  # brillo blanco
            if fw * fh > 1:
                px[fx + fw - 1, fy + fh - 1] = (255, 150, 120, 255)
        elif r["name"].startswith("D_"):
            # ojeras: aro rojo apagado en la cara frontal de la cuenca
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
        elif kind == "B":
            paint_bone(tex, r, rng)
        elif kind == "D":
            paint_dark(tex, r, rng)
        elif kind == "E":
            paint_eye_base(tex, r, rng)
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
