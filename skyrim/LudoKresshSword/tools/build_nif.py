"""Write the Skyrim SE weapon NIF from work/parts.npz using PyNifly (nifly).

Layout follows the vanilla SE weapons (checked against steeldagger.nif):
  BSFadeNode root
    BSXFlags 194 (Havok | Dynamic | Articulated)
    NiStringExtraData "Prn" = "WeaponSword"  (sheathed on the sword hip node)
    bhkCollisionObject -> bhkRigidBody (layer WEAPON) -> bhkListShape of boxes
    BSTriShape + BSLightingShaderProperty (environment-map metal)
"""
import sys
import numpy as np

sys.path.insert(0, sys.argv[1])  # PyNifly io_scene_nifly folder
from pyn.pynifly import (NifFile, BSXFlags, NiStringExtraData, BSLightingShaderProperty,
                         nifly, check_return)
from pyn.nifdefs import (PynBufferTypes, bhkListShapeProps, bhkConvexTransformShapeProps,
                         bhkBoxShapeProps, NiShapeBuf)
from pyn.nifconstants import SkyrimHavokMaterial, SkyrimCollisionLayer, bhkCOFlags
from ctypes import byref

PARTS, REFNIF, OUT = sys.argv[2:5]
TEXDIR = 'textures\\weapons\\LudoKressh\\'
HAVOK = 69.99124  # Skyrim units per Havok unit

d = np.load(PARTS)
names = ['blade', 'guard', 'grip', 'pommel']
verts, tris, uvs, norms = [], [], [], []
base = 0
for n in names:
    verts.append(d[n + '_verts']); uvs.append(d[n + '_uvs']); norms.append(d[n + '_normals'])
    tris.append(d[n + '_tris'] + base)
    base += len(d[n + '_verts'])
V = np.vstack(verts).astype(float)
T = np.vstack(tris)
UV = np.vstack(uvs).astype(float)
N = np.vstack(norms).astype(float)
assert len(V) < 65535

ref = NifFile(REFNIF)
ref_shape = ref.shape_dict['SteelDagger:0']
ref_body = ref.rootNode.collision_object.body

nif = NifFile()
nif.initialize('SKYRIMSE', OUT, 'BSFadeNode', 'LudoKresshSword')
root = nif.rootNode
BSXFlags.New(nif, 'BSX', 194, parent=root)
NiStringExtraData.New(nif, 'Prn', 'WeaponSword', parent=root)

sbuf = NiShapeBuf()
sbuf.bufType = PynBufferTypes.BSTriShapeBufType
sbuf.flags = 524302               # same NiAVObject flags as vanilla weapon shapes
shape = nif.createShapeFromData(
    'LudoKresshSword:0',
    [tuple(v) for v in V], [tuple(int(i) for i in t) for t in T],
    [tuple(u) for u in UV], [tuple(nv) for nv in N],
    props=sbuf, parent=root)

# shader: start from the vanilla steel dagger material, then tune
p = BSLightingShaderProperty.getbuf()
check_return(nifly.getBlock, ref._handle,
             ref_shape.shader.id, byref(p))
p.Shader_Type = 1                 # Environment map
p.Shader_Flags_1 = 0x82400381     # Specular | EnvMap | Recv/Cast shadows | Own_Emit | Remappable | ZTest
p.Shader_Flags_2 = 0x00008001     # ZWrite | EnvMap_Light_Fade (no vertex colours)
p.Env_Map_Scale = 1.0
p.Glossiness = 90.0
p.Spec_Str = 1.4
p.Spec_Color[0] = p.Spec_Color[1] = p.Spec_Color[2] = 1.0
p.Emissive_Color[0] = p.Emissive_Color[1] = p.Emissive_Color[2] = 0.0
p.Emissive_Mult = 1.0
p.Alpha = 1.0
p.UV_Scale_U = p.UV_Scale_V = 1.0
p.UV_Offset_U = p.UV_Offset_V = 0.0
shape.shader._properties = p
shape.save_shader_attributes()
shape.set_texture('Diffuse', TEXDIR + 'LudoKresshSword.dds')
shape.set_texture('Normal', TEXDIR + 'LudoKresshSword_n.dds')
shape.set_texture('EnvMap', 'textures\\cubemaps\\ShinyDull_e.dds')
shape.set_texture('EnvMask', TEXDIR + 'LudoKresshSword_m.dds')

# ------------------------------------------------------------ collision
def part_box(name, pad=0.0):
    P = d[name + '_verts']
    lo, hi = P.min(0) - pad, P.max(0) + pad
    return (lo + hi) / 2, (hi - lo) / 2

boxes = []
c, h = part_box('blade'); h[2] = max(h[2], 0.35); boxes.append((c, h))
G = d['guard_verts']; G = G[G[:, 1] < 11.0]   # crossguard arms only (langet lies on the blade)
lo, hi = G.min(0), G.max(0); boxes.append(((lo + hi) / 2, (hi - lo) / 2))
c1, h1 = part_box('grip'); c2, h2 = part_box('pommel')
lo = np.minimum(c1 - h1, c2 - h2); hi = np.maximum(c1 + h1, c2 + h2)
boxes.append(((lo + hi) / 2, (hi - lo) / 2))

coll = root.add_collision(None, bhkCOFlags.ACTIVE + bhkCOFlags.SYNC_ON_UPDATE)
bp = ref_body.properties.copy()
bp.collisionFilter_layer = SkyrimCollisionLayer.WEAPON
bp.collisionFilterCopy_layer = SkyrimCollisionLayer.WEAPON
bp.mass = 10.0
# inertia of a slender rod along Y (Havok units)
L = (V[:, 1].max() - V[:, 1].min()) / HAVOK
wdt = (V[:, 0].max() - V[:, 0].min()) / HAVOK
Iperp = bp.mass * L * L / 12.0
Iax = bp.mass * (wdt * wdt) / 12.0
for i in range(12):
    bp.inertiaMatrix[i] = 0.0
bp.inertiaMatrix[0] = Iperp
bp.inertiaMatrix[5] = Iax
bp.inertiaMatrix[10] = Iperp
com = np.average(np.array([b[0] for b in boxes]), axis=0,
                 weights=[np.prod(b[1]) for b in boxes]) / HAVOK
bp.center[0], bp.center[1], bp.center[2], bp.center[3] = com[0], com[1], com[2], 0.0
for i in range(4):
    bp.translation[i] = 0.0
bp.rotation[0] = bp.rotation[1] = bp.rotation[2] = 0.0
bp.rotation[3] = 1.0
bp.penetrationDepth = 0.05
body = coll.add_body(bp)

mat = SkyrimHavokMaterial.MATERIAL_BLADE_1HAND
lp = bhkListShapeProps()
lp.bhkMaterial = mat
lp.childShape_flags = lp.childFilter_flags = 0x80000000   # as in vanilla files
lst = body.add_shape(lp)
for c, h in boxes:
    radius = 0.005
    ctp = bhkConvexTransformShapeProps()
    ctp.bhkMaterial = mat
    ctp.bhkRadius = radius
    # vanilla files keep w = 0 in the translation column
    xf = [[1, 0, 0, c[0] / HAVOK], [0, 1, 0, c[1] / HAVOK], [0, 0, 1, c[2] / HAVOK], [0, 0, 0, 0]]
    for r in range(4):
        for col in range(4):
            ctp.transform[col][r] = xf[r][col]
    cts = lst.add_shape(ctp)
    bx = bhkBoxShapeProps()
    bx.bhkMaterial = mat
    bx.bhkRadius = radius
    dims = np.maximum(h / HAVOK - radius, 0.002)
    bx.bhkDimensions[0], bx.bhkDimensions[1], bx.bhkDimensions[2] = dims
    cts.add_shape(bx)

nif.save()
print('saved', OUT, len(V), 'verts', len(T), 'tris')
print('boxes', [(np.round(c, 2).tolist(), np.round(h, 2).tolist()) for c, h in boxes])
