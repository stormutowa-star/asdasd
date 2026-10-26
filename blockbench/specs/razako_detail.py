#!/usr/bin/env python3
"""Detalles pintados de Razako sobre las texturas que deja paint.py (se ejecuta DESPUES de paint.py):
bordes de placa con luz, remaches, grietas verdes (con brillo), capa rasgada, costuras del cuero, filo y arañazos de la hoja.
Uso: python3 -I razako_detail.py <razako.bbmodel>"""
import base64, io, json, os, random, sys
from PIL import Image

path = sys.argv[1]
model = json.load(open(path))
root = os.path.dirname(os.path.dirname(os.path.abspath(path)))
outdir = os.path.join(root, "src/main/resources/assets/keinloree/textures/entity")
name = model.get("kl_name", "razako")
base = Image.open(os.path.join(outdir, name + ".png")).convert("RGBA")
glow = Image.open(os.path.join(outdir, name + "_glow.png")).convert("RGBA")
bp, gp = base.load(), glow.load()
TW, TH = base.size
rng = random.Random(77)
GREEN = (76, 255, 122, 255)
GREEN_D = (30, 140, 66, 255)


def faces(el):
    for fname, f in el["faces"].items():
        u0, v0, u1, v1 = f["uv"]
        x0, x1 = sorted((int(round(u0)), int(round(u1))))
        y0, y1 = sorted((int(round(v0)), int(round(v1))))
        if x1 - x0 > 0 and y1 - y0 > 0:
            yield fname, x0, y0, x1, y1


def put(x, y, col, g=False):
    if 0 <= x < TW and 0 <= y < TH and bp[x, y][3] > 0:
        bp[x, y] = col
        if g:
            gp[x, y] = col


def mul(c, k):
    return (max(0, min(255, int(c[0] * k))), max(0, min(255, int(c[1] * k))), max(0, min(255, int(c[2] * k))), c[3])


def crack(x0, y0, x1, y1, thick=False):
    """Grieta verde zigzagueante de arriba a abajo (o de lado a lado) dentro de una cara; emite luz."""
    w, h = x1 - x0, y1 - y0
    vertical = h >= w
    n = h if vertical else w
    pos = rng.randint(2, max(2, (w if vertical else h) - 3))
    for i in range(1, n - 1):
        pos += rng.choice((-1, 0, 0, 1))
        pos = max(1, min((w if vertical else h) - 2, pos))
        x, y = (x0 + pos, y0 + i) if vertical else (x0 + i, y0 + pos)
        put(x, y, GREEN, True)
        if thick:
            put(x + 1, y, GREEN_D, True)
        if rng.random() < 0.12:  # ramita
            for k in range(1, 3):
                put(x + (k if vertical else 0), y + (0 if vertical else k), GREEN_D, True)


for el in model["elements"]:
    mat = el.get("kl_mat", "")
    glow_el = el.get("kl_glow", False)
    for fname, x0, y0, x1, y1 in faces(el):
        w, h = x1 - x0, y1 - y0
        if glow_el:
            # nucleo/grietas: centro mas claro y bordes mas oscuros
            for y in range(y0, y1):
                for x in range(x0, x1):
                    if x in (x0, x1 - 1) or y in (y0, y1 - 1):
                        put(x, y, mul(bp[x, y], 0.8), True)
                    elif mat == "glow_red" and w > 3:
                        put(x, y, (255, 90 + rng.randint(0, 40), 70, 255), True)
            continue
        if mat in ("armor_black", "armor_cracked", "armor_crimson", "armor_edge") and w >= 4 and h >= 4:
            base_col = bp[x0 + w // 2, y0 + h // 2]
            # biselado: luz arriba/izquierda, sombra abajo/derecha
            for x in range(x0 + 1, x1 - 1):
                put(x, y0 + 1, mul(base_col, 1.55))
                put(x, y1 - 2, mul(base_col, 0.6))
            for y in range(y0 + 1, y1 - 1):
                put(x0 + 1, y, mul(base_col, 1.35))
                put(x1 - 2, y, mul(base_col, 0.65))
            # remaches en las esquinas de las caras grandes
            if w >= 7 and h >= 7 and fname in ("north", "south", "up"):
                for cx, cy in ((x0 + 3, y0 + 3), (x1 - 4, y0 + 3), (x0 + 3, y1 - 4), (x1 - 4, y1 - 4)):
                    put(cx, cy, mul(base_col, 2.0))
                    put(cx + 1, cy, mul(base_col, 1.5))
                    put(cx, cy + 1, mul(base_col, 0.45))
                    put(cx + 1, cy + 1, mul(base_col, 0.4))
            # arañazos
            if mat != "armor_crimson" and w >= 6 and h >= 6:
                for _ in range(max(1, w * h // 90)):
                    sx, sy = rng.randint(x0 + 2, x1 - 3), rng.randint(y0 + 2, y1 - 3)
                    for k in range(rng.randint(2, 4)):
                        put(sx + k, sy + k // 2, mul(base_col, 1.8))
            if mat == "armor_crimson":  # filo dorado-oxidado en el borde de placa
                for x in range(x0, x1):
                    put(x, y0, mul(base_col, 0.45))
            if mat == "armor_cracked" and w >= 6 and h >= 6 and fname in ("north", "south", "up", "east", "west"):
                crack(x0, y0, x1, y1, thick=w > 10)
        elif mat == "leather" and w >= 4 and h >= 4:
            col = bp[x0 + w // 2, y0 + h // 2]
            for x in range(x0 + 1, x1 - 1, 3):  # costura
                put(x, y0 + 1, mul(col, 1.7))
                put(x, y1 - 2, mul(col, 1.7))
            if h >= 8:
                for y in range(y0 + 2, y1 - 2, 3):
                    put(x0 + w // 2, y, mul(col, 0.6))
        elif mat == "cloth" and w >= 3:
            col = bp[x0 + w // 2, y0 + h // 2]
            for y in range(y0, y1):
                if (y - y0) % 4 == 0:
                    for x in range(x0, x1):
                        put(x, y, mul(col, 1.25))
        elif mat == "cape":
            col = bp[x0 + w // 2, y0 + h // 2]
            for x in range(x0, x1):  # pliegues verticales
                if (x - x0) % 4 == 1:
                    for y in range(y0, y1):
                        put(x, y, mul(bp[x, y], 0.75))
            for x in range(x0, x1):  # borde inferior rasgado (se recorta con alfa)
                depth = rng.choice((0, 1, 1, 2, 3, 5)) if h > 8 else rng.choice((0, 0, 1))
                for d in range(depth):
                    yy = y1 - 1 - d
                    if 0 <= x < TW and 0 <= yy < TH and fname not in ("up", "down"):
                        bp[x, yy] = (0, 0, 0, 0)
                        gp[x, yy] = (0, 0, 0, 0)
            if fname not in ("up", "down"):  # fino borde mas claro
                for x in range(x0, x1):
                    put(x, y0, mul(col, 1.5))
        elif mat == "blade" and w >= 2 and h >= 4:
            col = bp[x0 + w // 2, y0 + h // 2]
            for y in range(y0, y1):  # gradiente: mas claro hacia la punta
                k = 1.0 + 0.5 * (1 - (y - y0) / max(1, h - 1)) if fname in ("north", "south", "east", "west") else 1.0
                for x in range(x0, x1):
                    put(x, y, mul(bp[x, y], k))
            if w >= 3:
                put(x0 + w // 2, y0 + h // 3, mul(col, 0.4))
            for _ in range(max(1, w * h // 70)):
                sx, sy = rng.randint(x0, x1 - 1), rng.randint(y0, y1 - 1)
                for k in range(rng.randint(2, 4)):
                    put(sx, sy + k, mul(col, 2.0))
        elif mat == "blade_edge" and w >= 2:
            for y in range(y0, y1):
                for x in range(x0, x1):
                    put(x, y, mul(bp[x, y], 1.0 + 0.25 * ((x + y) % 2)))
        elif mat == "iron" and w >= 2 and h >= 2:
            col = bp[x0 + w // 2, y0 + h // 2]
            for y in range(y0, y1):
                for x in range(x0, x1):
                    if (x + y) % 3 == 0:
                        put(x, y, mul(col, 0.7))
                    elif (x + y) % 5 == 0:
                        put(x, y, mul(col, 1.4))

# embebe de nuevo las texturas en el .bbmodel
def b64(img):
    buf = io.BytesIO()
    img.save(buf, "PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()

base.save(os.path.join(outdir, name + ".png"))
glow.save(os.path.join(outdir, name + "_glow.png"))
model["textures"][0]["source"] = b64(base)
model["textures"][1]["source"] = b64(glow)
json.dump(model, open(path, "w"))
print("razako_detail: texturas detalladas en", outdir)
