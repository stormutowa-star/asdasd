import sys
import numpy as np
from nifcheck import read

def frames(path, name_idx=None):
    h, blocks, _ = read(path)
    out = []
    for sh in [b for b in blocks if b['type'] == 'BSTriShape']:
        if not sh['verts'] or 't' not in sh['verts'][0]:
            continue
        V = np.array([v['pos'] for v in sh['verts']]); UV = np.array([v['uv'] for v in sh['verts']])
        dec = lambda a: np.array(a) / 255.0 * 2 - 1
        Tg = dec([v['t'] for v in sh['verts']])
        Bt = np.stack([np.array([v['btx'] for v in sh['verts']]), dec([v['bty'] for v in sh['verts']]), dec([v['btz'] for v in sh['verts']])], 1)
        T = np.array(sh['tris'])
        e1 = V[T[:, 1]] - V[T[:, 0]]; e2 = V[T[:, 2]] - V[T[:, 0]]
        du1 = UV[T[:, 1]] - UV[T[:, 0]]; du2 = UV[T[:, 2]] - UV[T[:, 0]]
        det = du1[:, 0] * du2[:, 1] - du2[:, 0] * du1[:, 1]
        ok = np.abs(det) > 1e-9
        dPdu = (e1 * du2[:, 1:2] - e2 * du1[:, 1:2]) / np.where(ok, det, 1)[:, None]
        dPdv = (e2 * du1[:, 0:1] - e1 * du2[:, 0:1]) / np.where(ok, det, 1)[:, None]
        nz = lambda a: a / (np.linalg.norm(a, axis=1, keepdims=True) + 1e-12)
        dPdu, dPdv = nz(dPdu[ok]), nz(dPdv[ok])
        tv = nz(Tg[T[ok, 0]]); bv = nz(Bt[T[ok, 0]])
        out.append((sh['name'], np.mean(np.einsum('ij,ij->i', tv, dPdu)), np.mean(np.einsum('ij,ij->i', tv, dPdv)),
                    np.mean(np.einsum('ij,ij->i', bv, dPdu)), np.mean(np.einsum('ij,ij->i', bv, dPdv))))
    return out

for p in sys.argv[1:]:
    for r in frames(p):
        print(p.split('/')[-1], 'shape', r[0], 'tangent.dPdu %.2f tangent.dPdv %.2f bitangent.dPdu %.2f bitangent.dPdv %.2f' % r[1:])
