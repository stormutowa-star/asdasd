#!/usr/bin/env python3
"""
Genera la textura pixel-art de la gran espada (greatsword) para el item de Minecraft.
Dibuja una hoja doble filo con gavilanes, empuñadura larga a dos manos y pomo,
siguiendo la silueta de la foto de referencia.
Salida: PNG RGBA sin dependencias externas (solo zlib/struct de la libreria estandar).
"""
import zlib, struct, os

# Paleta
TRANSPARENT = (0, 0, 0, 0)
# Acero de la hoja (filo claro -> centro mas oscuro = gavilanes/fuller)
EDGE_LIGHT = (214, 222, 232, 255)
STEEL_LIGHT = (178, 188, 200, 255)
STEEL_MID = (140, 151, 165, 255)
STEEL_DARK = (96, 106, 120, 255)
STEEL_SHADOW = (66, 74, 86, 255)
# Gavilanes / guarda (mas oscuro, hierro)
GUARD_LIGHT = (120, 122, 126, 255)
GUARD_MID = (88, 90, 95, 255)
GUARD_DARK = (58, 60, 65, 255)
# Inlay dorado del centro (como en la foto)
GOLD_HI = (236, 206, 132, 255)
GOLD_MID = (198, 166, 96, 255)
GOLD_DARK = (150, 122, 66, 255)
# Empuñadura ( wrap de cuerda / cuero claro como la foto )
GRIP_LIGHT = (206, 198, 178, 255)
GRIP_MID = (170, 162, 142, 255)
GRIP_DARK = (128, 121, 104, 255)
# Pomo (metal oscuro + oro)
POMO_HI = (150, 152, 158, 255)
POMO_DARK = (70, 72, 78, 255)

N = 16  # tamano 16x16 (textura clasica de item de Minecraft)

def new_grid():
    return [[TRANSPARENT for _ in range(N)] for _ in range(N)]

def px(grid, x, y, c):
    if 0 <= x < N and 0 <= y < N:
        grid[y][x] = c

# ---------------------------------------------------------------------------
# Diseno: espada orientada en diagonal clasica de Minecraft (punta arriba-dcha,
# empuñadura abajo-izquierda). La hoja ocupa la diagonal principal.
# ---------------------------------------------------------------------------
g = new_grid()

# --- HOJA (doble filo) ---
# Recorremos la diagonal desde (2,13) inferior-izq hasta (13,2) superior-dcha.
# La hoja es una banda de ~3 px de ancho perpendicular a la diagonal.
import math

# puntos de la linea central de la hoja
blade_start = (3.0, 12.0)   # base de la hoja (junto al guardamanos)
blade_tip = (13.5, 1.5)     # punta

def lerp(a, b, t):
    return (a[0] + (b[0]-a[0])*t, a[1] + (b[1]-a[1])*t)

# Dibujar la hoja rellenando por celdas cercanas a la diagonal
steps = 60
for i in range(steps + 1):
    t = i / steps
    cx, cy = lerp(blade_start, blade_tip, t)
    # ancho de la hoja: ancho en base, se estrecha hacia la punta
    half = 1.4 * (1.0 - t) + 0.2
    # vector perpendicular (la diagonal baja-dcha -> arriba-izq, perpendicular es (1,1) normalizado)
    perp = (0.7071, 0.7071)
    for s in [x*0.25 for x in range(-8, 9)]:
        ox = cx + perp[0]*s
        oy = cy + perp[1]*s
        ax, ay = int(round(ox)), int(round(oy))
        if not (0 <= ax < N and 0 <= ay < N):
            continue
        ad = abs(s)
        if ad <= half - 0.9:
            col = STEEL_MID            # centro de la hoja (fuller)
        elif ad <= half:
            col = STEEL_LIGHT
        else:
            col = EDGE_LIGHT
        px(g, ax, ay, col)

# Sombra suave en un lado del fuller para dar volumen
for i in range(steps + 1):
    t = i / steps
    cx, cy = lerp(blade_start, blade_tip, t)
    half = 1.4 * (1.0 - t) + 0.2
    perp = (0.7071, 0.7071)
    s = half - 0.6
    ox = cx + perp[0]*s
    oy = cy + perp[1]*s
    px(g, int(round(ox)), int(round(oy)), STEEL_DARK)

# Punta marcada
px(g, 13, 1, EDGE_LIGHT)
px(g, 14, 1, EDGE_LIGHT)
px(g, 13, 2, STEEL_LIGHT)
px(g, 14, 2, STEEL_LIGHT)
px(g, 12, 2, STEEL_LIGHT)

# --- INLAY DORADO CENTRAL (como la foto) ---
# Franja dorada a lo largo del centro de la hoja, cerca de la base-media
for i in range(0, 26):
    t = i / 40.0
    if t > 0.6:
        break
    cx, cy = lerp(blade_start, blade_tip, t)
    ax, ay = int(round(cx)), int(round(cy))
    px(g, ax, ay, GOLD_MID)
    px(g, ax + 1, ay, GOLD_HI)
    px(g, ax, ay + 1, GOLD_DARK)

# --- GUARDAMANOS (crossguard) ---
# Barra perpendicular a la hoja en la base
base = blade_start
# perpendicular apuntando arriba-izquierda
gp = (-0.7071, -0.7071)
for s in [x*0.5 for x in range(-7, 8)]:
    ax = int(round(base[0] + gp[0]*s + 0.7071*0))
    ay = int(round(base[1] + gp[1]*s))
    # ancho del guardamanos a lo largo de la hoja
    for w in (-1, 0, 1):
        bx = ax + int(round(0.7071*w))
        by = ay + int(round(0.7071*w))
        col = GUARD_MID if w == 0 else (GUARD_LIGHT if w < 0 else GUARD_DARK)
        px(g, bx, by, col)

# extremos del guardamanos ligeramente engrosados
px(g, 1, 12, GUARD_LIGHT)
px(g, 2, 11, GUARD_LIGHT)
px(g, 6, 8, GUARD_DARK)
px(g, 7, 7, GUARD_DARK)

# --- EMPUÑADURA (larga, dos manos) ---
grip_start = (base[0] - 0.4, base[1] + 0.4)
grip_end = (1.0, 14.0)
gsteps = 20
for i in range(gsteps + 1):
    t = i / gsteps
    cx, cy = lerp(grip_start, grip_end, t)
    # wrap: alterna tonos cinta a cinta
    band = (i // 2) % 2
    for w in (-1, 0, 1):
        bx = int(round(cx + 0.7071*w))
        by = int(round(cy + 0.7071*w))
        if band == 0:
            col = GRIP_LIGHT if w == 0 else GRIP_MID
        else:
            col = GRIP_MID if w == 0 else GRIP_DARK
        px(g, bx, by, col)

# --- POMO ---
px(g, 0, 14, POMO_HI)
px(g, 1, 15, POMO_HI)
px(g, 0, 15, POMO_DARK)
px(g, 1, 14, POMO_DARK)
px(g, 2, 15, POMO_DARK)

# ---------------------------------------------------------------------------
# Escritura del PNG
# ---------------------------------------------------------------------------
def write_png(path, grid):
    raw = bytearray()
    for row in grid:
        raw.append(0)  # filtro None por fila
        for (r, gr, b, a) in row:
            raw += bytes((r, gr, b, a))
    def chunk(typ, data):
        c = typ + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xffffffff)
    sig = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", N, N, 8, 6, 0, 0, 0)  # RGBA, 8 bits
    idat = zlib.compress(bytes(raw), 9)
    png = sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)

out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                       "src", "main", "resources", "assets", "greatsword", "textures", "item")
os.makedirs(out_dir, exist_ok=True)
out_path = os.path.join(out_dir, "greatsword.png")
write_png(out_path, g)
print("OK wrote", out_path, os.path.getsize(out_path), "bytes")
