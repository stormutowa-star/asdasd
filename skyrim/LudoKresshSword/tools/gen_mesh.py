"""Build the Ludo Kressh Sword mesh from the reference silhouette.

Each part (blade, guard, grip, pommel) is a 2D mask taken from the reference
image; it is triangulated and "inflated" into a closed 3D shell:
  * blade  -> diamond cross-section (height proportional to distance to edge)
  * others -> round cross-section (sqrt of a Poisson solution)
Output: work/parts.npz with per-part verts/tris/uvs/normals in Skyrim units.
"""
import sys
import numpy as np
from PIL import Image
from scipy import ndimage as ndi
from scipy import sparse
from scipy.sparse.linalg import spsolve
from skimage import measure, morphology
import triangle as tr

REF = sys.argv[1]
OUT = sys.argv[2]

img = np.asarray(Image.open(REF).convert('RGB')).astype(float)
H, W, _ = img.shape
YC = 137.0          # blade centre line (image rows)
S = 0.095           # Skyrim units per pixel
X_POMMEL = 72.0     # image column where pommel ends / grip starts
X_GUARD = 280.0     # image column where grip ends / guard starts
GRIP_COMPRESS = 0.593
GUARD_FACE_Y = 4.5  # guard face sits 4.5 units above the hand (origin)
X_GUARD_FACE = 285.0

# ---------------------------------------------------------------- masks
bg = np.array([241, 241, 241.])
diff = np.abs(img - bg).max(axis=2)
mask = ndi.binary_fill_holes(ndi.binary_closing(diff > 9, iterations=2))
lab, n = ndi.label(mask)
mask = lab == (np.argmax(ndi.sum(mask, lab, range(1, n + 1))) + 1)
mask = ndi.binary_opening(mask, iterations=1)

R, G, B = img[..., 0], img[..., 1], img[..., 2]
yy, xx = np.mgrid[0:H, 0:W]
dy = np.abs(yy - YC)
# the reference has a pale smudge next to the lower arrowhead: drop washed-out
# grey pixels around the guard arms
mn, mx = img.min(axis=2), img.max(axis=2)
smudge = (mn > 175) & (mx - mn < 30) & (xx >= 280) & (xx <= 345) & (dy > 35)
mask = mask & ~ndi.binary_dilation(smudge, iterations=1)
lab, n = ndi.label(mask)
mask = lab == (np.argmax(ndi.sum(mask, lab, range(1, n + 1))) + 1)
green = (G - R > 18) & (G - B > 2) & mask


def clean(m, close=1):
    m = ndi.binary_closing(m, iterations=close) if close else m
    m = ndi.binary_fill_holes(m)
    lab, n = ndi.label(m)
    if n > 1:
        sizes = ndi.sum(m, lab, range(1, n + 1))
        keep = [i + 1 for i, s in enumerate(sizes) if s > 40]
        m = np.isin(lab, keep)
    return m & mask


Image.fromarray((mask * 255).astype(np.uint8)).save(OUT.replace('.npz', '_mask.png'))
Image.fromarray(np.stack([green, mask & ~green & (xx < 300) & (dy <= 19) & (xx > 60), mask & (xx >= 300) & ~green], 2).astype(np.uint8) * 255).save(OUT.replace('.npz', '_regions.png'))

blade_m = clean(mask & (xx >= 300) & ((dy <= 30) | (xx >= 336)), 0)
guard_m = clean(((green & (xx >= 278) & (xx <= 500)) |
                 (mask & (xx >= 280) & (xx <= 336) & (dy > 30))), 2)
grip_m = mask & (xx >= 60) & (xx <= 300) & (dy <= 19)
pommel_m = clean(mask & (xx <= 74), 1)

# ---------------------------------------------------------------- height fields


def poisson_height(m):
    """Solve lap(h) = -2 inside m, h = 0 outside; return sqrt(h)."""
    idx = -np.ones(m.shape, int)
    pts = np.argwhere(m)
    idx[m] = np.arange(len(pts))
    rows, cols, vals = [], [], []
    for k, (y, x) in enumerate(pts):
        rows.append(k); cols.append(k); vals.append(4.0)
        for ny, nx in ((y - 1, x), (y + 1, x), (y, x - 1), (y, x + 1)):
            if 0 <= ny < H and 0 <= nx < W and m[ny, nx]:
                rows.append(k); cols.append(idx[ny, nx]); vals.append(-1.0)
    A = sparse.csr_matrix((vals, (rows, cols)), shape=(len(pts), len(pts)))
    h = spsolve(A, np.full(len(pts), 2.0))
    out = np.zeros(m.shape)
    out[m] = np.sqrt(np.maximum(h, 0))
    return out


blade_d = ndi.distance_transform_edt(blade_m)
blade_h = 0.15 * blade_d
blade_h_smooth = ndi.gaussian_filter(blade_h, 3.0)

heights = {
    'blade': blade_h,
    'guard': 0.72 * poisson_height(guard_m) + np.where(guard_m, blade_h_smooth, 0),
    'grip': poisson_height(grip_m),
    'pommel': 0.85 * poisson_height(pommel_m),
}
masks = {'blade': blade_m, 'guard': guard_m, 'grip': grip_m, 'pommel': pommel_m}
max_area = {'blade': 30.0, 'guard': 5.0, 'grip': 8.0, 'pommel': 4.0}
smooth = {'blade': False, 'guard': True, 'grip': True, 'pommel': True}

# ---------------------------------------------------------------- triangulation


def outlines(m):
    p = np.pad(ndi.gaussian_filter(m.astype(float), 0.7), 1)
    cs = measure.find_contours(p, 0.5)
    polys = []
    for c in cs:
        c = c - 1.0  # undo pad -> (row, col)
        c = measure.approximate_polygon(c, tolerance=0.45)
        if len(c) < 4:
            continue
        c = c[:-1]  # closed contour repeats first point
        area = 0.5 * np.sum(c[:-1, 1] * c[1:, 0] - c[1:, 1] * c[:-1, 0])
        polys.append((c[:, ::-1].copy(), area))  # -> (x, y)
    return polys


def bilinear(f, x, y):
    return ndi.map_coordinates(f, [y, x], order=1, mode='constant')


def build_part(name):
    m, hf = masks[name], heights[name]
    polys = outlines(m)
    verts, segs = [], []
    holes = []
    for poly, area in polys:
        base = len(verts)
        verts.extend(poly.tolist())
        k = len(poly)
        segs.extend([[base + i, base + (i + 1) % k] for i in range(k)])
    nb = len(verts)
    extra = []
    if name == 'blade':
        sk = morphology.skeletonize(m & (blade_d > 3))
        pts = np.argwhere(sk)[:, ::-1].astype(float)
        extra = pts[::3].tolist()
    data = {'vertices': np.array(verts + extra), 'segments': np.array(segs)}
    t = tr.triangulate(data, 'pq28a%gQ' % max_area[name])
    V2 = t['vertices']
    T = t['triangles']
    # boundary vertices: those lying on segments
    on_bnd = np.zeros(len(V2), bool)
    on_bnd[np.unique(t['segments'])] = True
    z = bilinear(hf, V2[:, 0], V2[:, 1])
    z[on_bnd] = 0.0
    if name == 'blade':
        z = np.maximum(z, 0.0)
    # drop triangles outside the mask (holes / concavities triangulated by mistake)
    cen = V2[T].mean(axis=1)
    inside = bilinear(m.astype(float), cen[:, 0], cen[:, 1]) > 0.5
    T = T[inside]
    if smooth[name]:
        # zero-thickness slivers (all corners on the outline) at spike tips
        T = T[~on_bnd[T].all(axis=1)]
    # in-plane outward normal of the outline at boundary vertices
    out2 = np.zeros((len(V2), 2))
    for a, b in t['segments']:
        e = V2[b] - V2[a]
        nrm = np.array([e[1], -e[0]]) / (np.linalg.norm(e) + 1e-12)
        mid = (V2[a] + V2[b]) / 2
        if bilinear(m.astype(float), np.array([mid[0] + nrm[0]]), np.array([mid[1] + nrm[1]]))[0] > 0.5:
            nrm = -nrm
        out2[a] += nrm
        out2[b] += nrm
    return V2, T, z, on_bnd, out2


def to_world(V2, z):
    x, y = V2[:, 0], V2[:, 1]
    Y = np.where(x >= X_GUARD,
                 GUARD_FACE_Y + (x - X_GUARD_FACE) * S,
                 np.where(x >= X_POMMEL,
                          GUARD_FACE_Y + (X_GUARD - X_GUARD_FACE) * S - (X_GUARD - x) * S * GRIP_COMPRESS,
                          GUARD_FACE_Y + (X_GUARD - X_GUARD_FACE) * S - (X_GUARD - X_POMMEL) * S * GRIP_COMPRESS
                          - (X_POMMEL - x) * S))
    X = -(y - YC) * S
    Z = z * S
    return np.stack([X, Y, Z], axis=1)


def vertex_normals(P, T):
    fn = np.cross(P[T[:, 1]] - P[T[:, 0]], P[T[:, 2]] - P[T[:, 0]])
    n = np.zeros_like(P)
    for i in range(3):
        np.add.at(n, T[:, i], fn)
    ln = np.linalg.norm(n, axis=1, keepdims=True)
    ln[ln == 0] = 1
    return n / ln


parts = {}
for name in ['blade', 'guard', 'grip', 'pommel']:
    V2, T, z, on_bnd, out2 = build_part(name)
    n = len(V2)
    top = to_world(V2, z)
    bot = to_world(V2, -z)
    uv = np.stack([V2[:, 0] / W, V2[:, 1] / H], axis=1)
    # (x, y) -> (X=-y, Y=x) keeps orientation, so Triangle's CCW output is
    # front-facing for the +Z side; the -Z side gets reversed winding.
    T_top = T
    T_bot = T[:, ::-1]
    if smooth[name]:
        # weld boundary: bottom side reuses top boundary vertices
        remap = np.arange(n) + n
        remap[on_bnd] = np.nonzero(on_bnd)[0]
        P = np.vstack([top, bot])
        UV = np.vstack([uv, uv])
        Tb = remap[T_bot]
        Tall = np.vstack([T_top, Tb])
        used = np.unique(Tall)
        newidx = -np.ones(len(P), int)
        newidx[used] = np.arange(len(used))
        N = vertex_normals(P, Tall)
        # round parts are vertical at the outline: use the outline's outward normal
        bn = np.stack([-out2[:, 1], out2[:, 0], np.zeros(n)], axis=1)  # image (dx,dy) -> world (X,Y)
        bn /= np.linalg.norm(bn, axis=1, keepdims=True) + 1e-12
        N[:n][on_bnd] = bn[on_bnd]
        P, UV, Tall, N = P[used], UV[used], newidx[Tall], N[used]
    else:
        P = np.vstack([top, bot])
        UV = np.vstack([uv, uv])
        Tall = np.vstack([T_top, T_bot + n])
        N = vertex_normals(P, Tall)
    # sanity: geometric normals agree with vertex normals
    fn = np.cross(P[Tall[:, 1]] - P[Tall[:, 0]], P[Tall[:, 2]] - P[Tall[:, 0]])
    cz = P[Tall].mean(axis=1)[:, 2]
    sel = np.abs(cz) > 1e-4
    agree = np.mean(np.sign(fn[sel, 2]) == np.sign(cz[sel]))
    print(f'{name}: {len(P)} verts, {len(Tall)} tris, normal agreement {agree:.3f}, '
          f'bbox {P.min(0).round(2)} {P.max(0).round(2)}')
    parts[name] = dict(verts=P.astype(np.float32), tris=Tall.astype(np.int32),
                       uvs=UV.astype(np.float32), normals=N.astype(np.float32))

np.savez(OUT, **{f'{k}_{f}': v for k, d in parts.items() for f, v in d.items()})
