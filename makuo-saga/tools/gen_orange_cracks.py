"""Draws the Super Saiyan Orange crack overlay: glowing cracks from the hands up to the shoulders.

The texture uses the vanilla player-skin layout (64x64), which is what DragonMineZ's player model uses.
It is white/grey on purpose: DragonMineZ tints it with the form's extraFormColor, and the addon pulses that
color between dark lava and bright orange.

Usage: python3 gen_orange_cracks.py <output png>
"""
import random, sys
from PIL import Image

img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
px = img.load()
CORE, EDGE = (255, 255, 255, 255), (170, 170, 170, 255)


def put(x, y, c):
    if 0 <= x < 64 and 0 <= y < 64:
        if px[x, y] != CORE:
            px[x, y] = c


def vein(x0, y0, w, h, rnd, length):
    """One meandering crack from the bottom (hand) of a w x h face upwards, `length` pixels long."""
    x, y = x0 + rnd.randrange(w), y0 + h - 1
    for _ in range(length):
        if y < y0:
            break
        put(x, y, CORE)
        if rnd.random() < 0.15:
            put(x + rnd.choice((-1, 1)), y, EDGE)
        y -= 1
        if rnd.random() < 0.35:
            x = min(x0 + w - 1, max(x0, x + rnd.choice((-1, 1))))


def arm(u, v, seed):
    """Box UV of a 4x12x4 arm at (u, v): east, north, west, south faces + the shoulder (up) face."""
    rnd = random.Random(seed)
    for i, fx in enumerate((u, u + 4, u + 8, u + 12)):
        vein(fx, v + 4, 4, 12, rnd, 12)          # main crack: hand to shoulder
        if i % 2 == 0:
            vein(fx, v + 4, 4, 12, rnd, 5)       # short branch around the hand
    for x, y in ((u + 5, v + 1), (u + 6, v + 2)):
        put(x, y, EDGE)


# right arm + its sleeve layer, left arm + its sleeve layer
arm(40, 16, 11)
arm(40, 32, 11)
arm(32, 48, 23)
arm(48, 48, 23)

# Shoulders: short cracks on the top corners of the torso, front (u 20..28) and back (u 32..40), v 20..
for (x, y) in [(20, 20), (21, 21), (20, 22), (27, 20), (26, 21), (27, 22),
               (32, 20), (33, 21), (32, 22), (39, 20), (38, 21), (39, 22)]:
    put(x, y, CORE if y == 20 else EDGE)
# Torso up face, next to each shoulder
for (x, y) in [(20, 17), (21, 18), (27, 17), (26, 18)]:
    put(x, y, EDGE)

img.save(sys.argv[1])
print('cracks written')
