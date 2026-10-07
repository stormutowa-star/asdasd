"""Tiny numpy z-buffer rasterizer to preview the generated mesh with its texture."""
import sys
import numpy as np
from PIL import Image


def load_parts(path):
    d = np.load(path)
    names = sorted({k.split('_')[0] for k in d.files})
    return [(n, d[n + '_verts'], d[n + '_tris'], d[n + '_uvs'], d[n + '_normals']) for n in names]


def rot(yaw, pitch, roll=0.0):
    cy, sy = np.cos(yaw), np.sin(yaw)
    cp, sp = np.cos(pitch), np.sin(pitch)
    cr, sr = np.cos(roll), np.sin(roll)
    Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rx = np.array([[1, 0, 0], [0, cp, -sp], [0, sp, cp]])
    Rz = np.array([[cr, -sr, 0], [sr, cr, 0], [0, 0, 1]])
    return Rx @ Ry @ Rz


def render(parts, tex, R, size=(1400, 500), light=(0.3, 0.5, 0.8), bg=(40, 40, 46)):
    Wd, Hd = size
    pts = np.vstack([(p[1] @ R.T) for p in parts])
    lo, hi = pts[:, :2].min(0), pts[:, :2].max(0)
    scale = 0.92 * min(Wd / (hi[0] - lo[0]), Hd / (hi[1] - lo[1]))
    off = np.array([Wd, Hd]) / 2 - scale * (lo + hi) / 2
    img = np.zeros((Hd, Wd, 3)); img[:] = bg
    zb = np.full((Hd, Wd), -np.inf)
    L = np.array(light, float); L /= np.linalg.norm(L)
    th, tw = tex.shape[:2]
    for name, V, T, UV, N in parts:
        P = V @ R.T
        Nn = N @ R.T
        sx = P[:, 0] * scale + off[0]
        sy = Hd - (P[:, 1] * scale + off[1])
        for t in T:
            x0, x1, x2 = sx[t]; y0, y1, y2 = sy[t]
            area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
            if area >= 0:  # back-face (screen y flipped => CCW becomes negative area)
                continue
            xmin, xmax = int(max(min(x0, x1, x2), 0)), int(min(max(x0, x1, x2) + 1, Wd))
            ymin, ymax = int(max(min(y0, y1, y2), 0)), int(min(max(y0, y1, y2) + 1, Hd))
            if xmin >= xmax or ymin >= ymax:
                continue
            gx, gy = np.meshgrid(np.arange(xmin, xmax) + 0.5, np.arange(ymin, ymax) + 0.5)
            w0 = ((x1 - gx) * (y2 - gy) - (x2 - gx) * (y1 - gy)) / area
            w1 = ((x2 - gx) * (y0 - gy) - (x0 - gx) * (y2 - gy)) / area
            w2 = 1 - w0 - w1
            inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
            if not inside.any():
                continue
            z = w0 * P[t[0], 2] + w1 * P[t[1], 2] + w2 * P[t[2], 2]
            sub = zb[ymin:ymax, xmin:xmax]
            upd = inside & (z > sub)
            if not upd.any():
                continue
            sub[upd] = z[upd]
            u = w0 * UV[t[0], 0] + w1 * UV[t[1], 0] + w2 * UV[t[2], 0]
            v = w0 * UV[t[0], 1] + w1 * UV[t[1], 1] + w2 * UV[t[2], 1]
            n = (w0[..., None] * Nn[t[0]] + w1[..., None] * Nn[t[1]] + w2[..., None] * Nn[t[2]])
            n /= np.linalg.norm(n, axis=-1, keepdims=True) + 1e-9
            diff = np.clip(n @ L, 0, 1)
            hvec = L + np.array([0, 0, 1.0]); hvec /= np.linalg.norm(hvec)
            spec = np.clip(n @ hvec, 0, 1) ** 40
            c = tex[np.clip((v * th).astype(int), 0, th - 1), np.clip((u * tw).astype(int), 0, tw - 1)]
            col = c * (0.35 + 0.75 * diff[..., None]) + 120 * spec[..., None]
            img[ymin:ymax, xmin:xmax][upd] = np.clip(col[upd], 0, 255)
    return Image.fromarray(img.astype(np.uint8))


if __name__ == '__main__':
    parts = load_parts(sys.argv[1])
    tex = np.asarray(Image.open(sys.argv[2]).convert('RGB')).astype(float)
    out = sys.argv[3]
    views = [
        ('front', rot(0, 0, -np.pi / 2)),
        ('back', rot(np.pi, 0, -np.pi / 2)),
        ('edge', rot(np.pi / 2, 0, -np.pi / 2)),
        ('persp', rot(0.75, 0.0, -np.pi / 2)),
        ('persp2', rot(0.0, 0.0, -np.pi / 2.3) @ rot(0, -0.9, 0)),
    ]
    ims = [render(parts, tex, R) for _, R in views]
    W = max(i.width for i in ims)
    sheet = Image.new('RGB', (W, sum(i.height for i in ims)))
    y = 0
    for im in ims:
        sheet.paste(im, (0, y)); y += im.height
    sheet.save(out)
