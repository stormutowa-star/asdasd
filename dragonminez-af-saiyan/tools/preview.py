"""Rough 3D preview of a DMZ hair code, mirroring HairRenderer.renderStrandInterpolated (static pose, time=0)."""
import sys, math, numpy as np
import os; sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import dmzhair as h
import matplotlib; matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection

FACES = {'F': (0, 1, 4), 'B': (1, 4, 4), 'L': (2, 4, 4), 'R': (3, 4, 4), 'T': (4, 4, 4)}
BASE_ROT = {'F': (-90, 0, 0), 'B': (90, 0, 0), 'L': (0, 0, 90), 'R': (0, 0, -90), 'T': (0, 0, 0)}

def base_pos(face, idx):
    _, rows, cols = FACES[face]
    row, col = idx // cols, idx % cols
    P = [-3, -1, 1, 3]; Y = [0, -1.5, -3, -4.5]
    gx, gz, ry = P[col % 4], P[row % 4], Y[row % 4]
    return {'F': (gx, 7.25, -4), 'B': (gx, 7.25 + ry, 4), 'L': (-3.95, 7.25 + ry, gx),
            'R': (3.95, 7.25 + ry, -gx), 'T': (gx, 7.85, gz)}[face]

def rx(d):
    a = math.radians(d); c, s = math.cos(a), math.sin(a)
    return np.array([[1,0,0,0],[0,c,-s,0],[0,s,c,0],[0,0,0,1]])
def ry(d):
    a = math.radians(d); c, s = math.cos(a), math.sin(a)
    return np.array([[c,0,s,0],[0,1,0,0],[-s,0,c,0],[0,0,0,1]])
def rz(d):
    a = math.radians(d); c, s = math.cos(a), math.sin(a)
    return np.array([[c,-s,0,0],[s,c,0,0],[0,0,1,0],[0,0,0,1]])
def tr(x, y, z):
    m = np.eye(4); m[:3, 3] = (x, y, z); return m
def sc(x, y, z):
    return np.diag([x, y, z, 1.0])
def rot(x, y, z):
    m = np.eye(4)
    if x: m = m @ rx(x)
    if y: m = m @ ry(y)
    if z: m = m @ rz(z)
    return m

def strand_polys(face, s):
    idx = s.get('i', 0) - FACES[face][0] * 100
    p = base_pos(face, idx)
    M = tr(*p) @ rot(s.get('rx', 0.0), s.get('ry', 0.0), s.get('rz', 0.0))
    M = M @ sc(s.get('sx', 1.0), s.get('sy', 1.0), s.get('sz', 1.0))
    w0, h0, d0 = s.get('cw', 2.0), s.get('ch', 2.0), s.get('cd', 2.0)
    ls = s.get('ls', 1.0); sf = 1.0; prev = 0; polys = []
    for i in range(s.get('l', 0)):
        cw, ch, cd = w0 * sf, h0 * sf * ls, d0 * sf
        if i > 0:
            M = M @ tr(0, prev, 0) @ rot(s.get('cx', 0.0), s.get('cy', 0.0), s.get('cz', 0.0))
        hw, hd = cw / 2, cd / 2
        corners = np.array([[x, y, z, 1] for x in (-hw, hw) for y in (0, ch) for z in (-hd, hd)])
        W = (M @ corners.T).T[:, :3]
        q = [[0,1,3,2],[4,5,7,6],[0,1,5,4],[2,3,7,6],[0,2,6,4],[1,3,7,5]]
        polys += [[W[k] for k in f] for f in q]
        prev = ch; sf *= 0.85
    return polys

def render(code, out, color, title):
    _, tag = h.decode(code); P = h.plain(tag)
    polys = []
    for f in 'FBLRT':
        for s in P.get(f, []): polys += strand_polys(f, s)
    head = []
    for x0,x1,y0,y1,z0,z1 in [(-4,4,0,8,-4,4)]:
        C = np.array([[x,y,z] for x in (x0,x1) for y in (y0,y1) for z in (z0,z1)])
        for f in [[0,1,3,2],[4,5,7,6],[0,1,5,4],[2,3,7,6],[0,2,6,4],[1,3,7,5]]: head.append([C[k] for k in f])
    fig = plt.figure(figsize=(12, 4.2)); fig.suptitle(title)
    for n, (el, az, name) in enumerate([(15, -60, '3/4 frente'), (5, 0, 'lado'), (15, 120, '3/4 espalda')]):
        ax = fig.add_subplot(1, 3, n + 1, projection='3d')
        # map model (x,y,z) -> plot (x, z, y) so y is up; front face is at -z
        mp = lambda poly: [(v[0], v[2], v[1]) for v in poly]
        ax.add_collection3d(Poly3DCollection([mp(p) for p in head], facecolor='#f1c9a5', edgecolor='#a07050', lw=0.3, alpha=1))
        ax.add_collection3d(Poly3DCollection([mp(p) for p in polys], facecolor=color, edgecolor='#333', lw=0.15, alpha=0.95))
        ax.set_xlim(-14, 14); ax.set_ylim(-14, 14); ax.set_zlim(-20, 16)
        ax.view_init(elev=el, azim=az); ax.set_box_aspect((1, 1, 1.07)); ax.set_axis_off(); ax.set_title(name)
    plt.tight_layout(); plt.savefig(out, dpi=90)

if __name__ == '__main__':
    render(open(sys.argv[1]).read().strip(), sys.argv[2], sys.argv[3], sys.argv[4])
