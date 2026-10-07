"""Export the final Skyrim NIF mesh as a Forge OBJ item model (same triangles, UVs, normals).

Placement mimics a vanilla handheld sword sprite: grip near pixel (3.6, 3.6), blade along the
(1,1) diagonal of the 16x16 item square, thickness along Z centred on z = 8 px.
"""
import sys, json
import numpy as np
from nifcheck import read

nif, out_obj, out_display = sys.argv[1:4]
h, blocks, _ = read(nif)
sh = [b for b in blocks if b['type'] == 'BSTriShape'][0]
P = np.array([v['pos'] for v in sh['verts']], float)
UV = np.array([v['uv'] for v in sh['verts']], float)
N = np.array([v['n'] for v in sh['verts']], float) / 255 * 2 - 1
N /= np.linalg.norm(N, axis=1, keepdims=True)
T = np.array(sh['tris'], int)

K = 1.0 / 72.0                                  # Skyrim units -> blocks (sword ~1.35 blocks long)
A = np.array([1, 1, 0]) / np.sqrt(2)            # Skyrim +Y (blade)  -> up-right diagonal
C = np.array([1, -1, 0]) / np.sqrt(2)           # Skyrim +X (guard)  -> down-right diagonal
D = np.array([0, 0, 1.0])                       # Skyrim +Z (flat)   -> south
G = np.array([3.6 / 16, 3.6 / 16, 0.5])         # hand position
B = np.stack([C, A, D], axis=1)                 # columns: images of Skyrim x, y, z
assert abs(np.linalg.det(B) - 1) < 1e-9         # proper rotation: winding stays front-facing
V = G + (P * K) @ B.T
NN = N @ B.T

with open(out_obj, 'w') as f:
    f.write('# Ludo Kressh Sword - same mesh as the Skyrim SE mod (LudoKresshSword.nif)\n')
    f.write('mtllib ludo_kressh_sword.mtl\no ludo_kressh_sword\n')
    for v in V: f.write('v %.6f %.6f %.6f\n' % tuple(v))
    for u in UV: f.write('vt %.6f %.6f\n' % (u[0], 1.0 - u[1]))   # standard OBJ (v up); model sets flip_v
    for n in NN: f.write('vn %.5f %.5f %.5f\n' % tuple(n))
    f.write('usemtl sword\ns off\n')
    for t in T + 1:
        f.write('f %d/%d/%d %d/%d/%d %d/%d/%d\n' % (t[0], t[0], t[0], t[1], t[1], t[1], t[2], t[2], t[2]))

px = V * 16
lo, hi = px.min(0), px.max(0)
c = (lo + hi) / 2
ext = max(hi[0] - lo[0], hi[1] - lo[1])
s = round(15.6 / ext, 3)
gui_t = [round(-s * (c[0] - 8), 3), round(-s * (c[1] - 8), 3), 0]
display = {
    'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'gui': {'rotation': [0, 0, 0], 'translation': gui_t, 'scale': [s, s, s]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [-gui_t[0], gui_t[1], 0], 'scale': [s, s, s]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 13, 7], 'scale': [1, 1, 1]},
}
json.dump(display, open(out_display, 'w'), indent=2)
print('verts', len(V), 'tris', len(T), 'bbox px', lo.round(2), hi.round(2), 'gui scale', s, 'gui translation', gui_t)
