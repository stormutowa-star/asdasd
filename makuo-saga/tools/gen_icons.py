"""Draws the Void Shard item (16x16) and the Vharos space pod icon (11x11).

Usage: python3 gen_icons.py <src/main/resources dir>
"""
import math, os, sys
from PIL import Image

RES = sys.argv[1]

# Void Shard: a slanted violet crystal with a bright core and a dark outline.
shard = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
p = shard.load()
OUTLINE, DARK, MID, LIGHT, CORE = (30, 8, 50, 255), (74, 21, 127, 255), (138, 60, 240, 255), (200, 150, 255, 255), (245, 230, 255, 255)
for y in range(16):
    for x in range(16):
        # diamond-ish crystal along the diagonal from (4,13) to (12,2)
        t = (x - 4) * 0.6 + (13 - y) * 0.8
        across = abs((x - 4) * 0.8 - (13 - y) * 0.6)
        width = 3.2 - abs(t - 6.5) * 0.42
        if 0 <= t <= 13 and across <= width:
            edge = across > width - 0.9
            if edge:
                p[x, y] = OUTLINE
            elif across < 0.7 and 3 < t < 10:
                p[x, y] = CORE
            elif (x + y) % 2 == 0:
                p[x, y] = LIGHT if t > 6.5 else MID
            else:
                p[x, y] = MID if t > 6.5 else DARK
shard.save(os.path.join(RES, 'assets/makuosaga/textures/item/void_shard.png'))

# Vharos: a small violet planet with a darker band.
icon = Image.new('RGBA', (11, 11), (0, 0, 0, 0))
q = icon.load()
for y in range(11):
    for x in range(11):
        d = math.hypot(x - 5, y - 5)
        if d <= 5.2:
            shade = 1.0 - max(0.0, (x + y - 6) * 0.06)
            base = (150, 70, 230) if abs(y - 6 + (x - 5) * 0.3) > 1.0 else (90, 30, 150)
            if d > 4.4:
                base = (40, 10, 70)
            q[x, y] = tuple(int(c * shade) for c in base) + (255,)
q[3, 3] = (225, 190, 255, 255)
q[4, 3] = (200, 150, 255, 255)
icon.save(os.path.join(RES, 'assets/makuosaga/textures/gui/vharos_icon.png'))
print('icons written')
