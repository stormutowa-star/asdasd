"""Rough preview of a GeckoLib (bedrock) geo model with face colors sampled from its texture.
Mirrors GeckoLib's baking: x is mirrored, cube rotations are applied as (-x, -y, z) in Z-Y-X order.

Usage: python3 preview_model.py <geo.json> <texture.png> <out.png> [title]
"""
import json, math, sys
import numpy as np
from PIL import Image
import matplotlib; matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection

geo = json.load(open(sys.argv[1]))['minecraft:geometry'][0]
tex = Image.open(sys.argv[2]).convert('RGBA')
TW, TH = geo['description']['texture_width'], geo['description']['texture_height']
sx, sy = tex.size[0] / TW, tex.size[1] / TH


def rot(rx, ry, rz):
    def R(axis, d):
        a = math.radians(d); c, s = math.cos(a), math.sin(a)
        if axis == 'x': return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])
        if axis == 'y': return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])
        return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])
    return R('z', rz) @ R('y', ry) @ R('x', rx)


def face_color(uvinfo, face, size):
    w, h, d = size
    if isinstance(uvinfo, list):
        u, v = uvinfo
        boxes = {'up': (u + d, v, w, d), 'down': (u + d + w, v, w, d), 'east': (u, v + d, d, h),
                 'north': (u + d, v + d, w, h), 'west': (u + d + w, v + d, d, h), 'south': (u + d + w + d, v + d, w, h)}
        x0, y0, fw, fh = boxes[face]
    else:
        if face not in uvinfo: return None
        (x0, y0), (fw, fh) = uvinfo[face]['uv'], uvinfo[face]['uv_size']
    xa, xb = sorted([x0 * sx, (x0 + fw) * sx]); ya, yb = sorted([y0 * sy, (y0 + fh) * sy])
    xa, ya = int(xa), int(ya); xb, yb = max(xa + 1, int(math.ceil(xb))), max(ya + 1, int(math.ceil(yb)))
    reg = np.array(tex.crop((xa, ya, min(xb, tex.size[0]), min(yb, tex.size[1]))))
    if reg.size == 0: return None
    reg = reg.reshape(-1, 4); reg = reg[reg[:, 3] > 0]
    if len(reg) == 0: return None
    return tuple(reg[:, :3].mean(axis=0) / 255)


polys, cols = [], []
for bone in geo['bones']:
    for c in bone.get('cubes', []):
        ox, oy, oz = c['origin']; w, h, d = c['size']; inf = c.get('inflate', 0)
        x0, x1 = -(ox + w) - inf, -ox + inf  # GeckoLib mirrors x
        y0, y1, z0, z1 = oy - inf, oy + h + inf, oz - inf, oz + d + inf
        P = np.array([[x, y, z] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)], float)
        if 'rotation' in c:
            rx_, ry_, rz_ = c['rotation']; px, py, pz = c['pivot']
            piv = np.array([-px, py, pz])
            P = (rot(-rx_, -ry_, rz_) @ (P - piv).T).T + piv
        faces = {'west': [0, 1, 3, 2], 'east': [4, 5, 7, 6], 'down': [0, 2, 6, 4], 'up': [1, 3, 7, 5],
                 'north': [0, 1, 5, 4], 'south': [2, 3, 7, 6]}
        for f, idx in faces.items():
            col = face_color(c['uv'], f, (w, h, d))
            if col is None: continue
            polys.append([P[i] for i in idx]); cols.append(col)

fig = plt.figure(figsize=(13, 5)); fig.suptitle(sys.argv[4] if len(sys.argv) > 4 else '')
for n, (el, az, name) in enumerate([(8, -90, 'frente'), (8, -40, '3/4'), (8, 0, 'lado'), (12, 90, 'espalda')]):
    ax = fig.add_subplot(1, 4, n + 1, projection='3d')
    ax.add_collection3d(Poly3DCollection([[(p[0], p[2], p[1]) for p in poly] for poly in polys],
                                         facecolors=cols, edgecolor=(0, 0, 0, 0.25), lw=0.2, alpha=1.0))
    ax.set_xlim(-30, 30); ax.set_ylim(-30, 30); ax.set_zlim(0, 58)
    ax.view_init(elev=el, azim=az); ax.set_box_aspect((1, 1, 0.97)); ax.set_axis_off(); ax.set_title(name)
plt.tight_layout(); plt.savefig(sys.argv[3], dpi=80)
