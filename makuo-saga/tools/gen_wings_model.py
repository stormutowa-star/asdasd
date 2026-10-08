"""Builds saga_makuo_wings.geo.json: DragonMineZ's saga_piccolo model (GPL-3.0) at 128x128 UVs,
plus two Heilig-Flügel-style wings made of fanned blades of violet light.

Usage: python3 gen_wings_model.py <dragonminez assets dir> <output geo dir>
"""
import copy, json, os, sys

SRC, OUT = sys.argv[1], sys.argv[2]
geo = json.load(open(os.path.join(SRC, 'geo/entity/sagas/saga_piccolo.geo.json')))
model = copy.deepcopy(geo)
g = model['minecraft:geometry'][0]
g['description']['identifier'] = 'geometry.saga_makuo_wings'
g['description']['texture_width'] = 128
g['description']['texture_height'] = 128


def scale_uv(uv):
    if isinstance(uv, list):
        return [v * 2 for v in uv]
    return {face: {'uv': [v * 2 for v in d['uv']], 'uv_size': [v * 2 for v in d['uv_size']]} for face, d in uv.items()}


for bone in g['bones']:
    for cube in bone.get('cubes', []):
        cube['uv'] = scale_uv(cube['uv'])
    if bone['name'] in ('cape', 'cape2', 'cape3'):
        bone['cubes'] = []  # no cape in the final form: the wings replace it

# Wing UVs: every blade face samples the vertical gradient painted at v 64..128 (root dark, tip bright)
def blade_uv(u):
    return {
        'north': {'uv': [u, 64], 'uv_size': [8, 64]},
        'south': {'uv': [u, 64], 'uv_size': [8, 64]},
        'east': {'uv': [u + 3, 64], 'uv_size': [2, 64]},
        'west': {'uv': [u + 3, 64], 'uv_size': [2, 64]},
        'up': {'uv': [u + 3, 127], 'uv_size': [2, 1]},
        'down': {'uv': [u + 3, 64], 'uv_size': [2, 1]},
    }

# (angle from vertical, length, width, depth offset) — long outer blades, then short bright inner ones
BLADES = [(8, 20, 2.4, 0.0), (20, 26, 2.4, 0.0), (33, 30, 2.4, 0.0), (46, 31, 2.4, 0.0),
          (59, 28, 2.2, 0.0), (71, 23, 2.0, 0.0), (82, 16, 1.8, 0.0),
          (16, 12, 1.6, 0.45), (34, 15, 1.6, 0.45), (52, 15, 1.6, 0.45), (70, 11, 1.6, 0.45)]
PIVOT_Y, PIVOT_Z, PIVOT_X = 22.0, 2.6, 1.6


def wing(side):
    # side = -1 is the model's right (same side as the right ear, which tilts outward with a negative Z rotation)
    px = side * PIVOT_X
    cubes = []
    for i, (angle, length, width, dz) in enumerate(BLADES):
        z = PIVOT_Z + dz
        cubes.append({
            'origin': [round(px - width / 2, 3), PIVOT_Y, round(z, 3)],
            'size': [width, length, 0.3],
            'pivot': [px, PIVOT_Y, round(z + 0.15, 3)],
            'rotation': [0, 0, side * angle],
            'uv': blade_uv((i % 16) * 8),
        })
    return {'name': 'wing_right' if side < 0 else 'wing_left', 'parent': 'body',
            'pivot': [px, PIVOT_Y, PIVOT_Z], 'cubes': cubes}


g['bones'].append(wing(-1))
g['bones'].append(wing(1))
os.makedirs(OUT, exist_ok=True)
with open(os.path.join(OUT, 'saga_makuo_wings.geo.json'), 'w') as f:
    json.dump(model, f, indent=1)
print('model written')
