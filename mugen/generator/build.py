"""Construye los archivos binarios del personaje en ../HornedBlade:
HornedBlade.sff, HornedBlade.air, HornedBlade.snd y las paletas .act.
Uso: python3 build.py"""
import json
import math
import os
import numpy as np
from PIL import Image

import palette as PL
import rig
import poses
import fx
import sounds
import mugenfmt as MF

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.normpath(os.path.join(HERE, '..', 'HornedBlade'))
NAME = 'HornedBlade'


def to_box(x0, y0, x1, y1):
    return (int(x0) - rig.AX, int(y0) - rig.AY, int(x1) - rig.AX, int(y1) - rig.AY)


def bbox(mask, shrink=0):
    ys, xs = np.nonzero(mask)
    if len(ys) == 0:
        return None
    x0, x1, y0, y1 = xs.min() + shrink, xs.max() + 1 - shrink, ys.min() + shrink, ys.max() + 1 - shrink
    if x1 <= x0 or y1 <= y0:
        return None
    return to_box(x0, y0, x1, y1)


def split_boxes(mask, n=3):
    ys, xs = np.nonzero(mask)
    if len(ys) < 4:
        return []
    P = np.stack([xs, ys], 1).astype(float)
    c = P.mean(0)
    u, s, vt = np.linalg.svd(P - c, full_matrices=False)
    proj = (P - c) @ vt[0]
    qs = np.quantile(proj, np.linspace(0, 1, n + 1))
    out = []
    for i in range(n):
        sel = (proj >= qs[i]) & (proj <= qs[i + 1])
        if sel.sum() < 2:
            continue
        x0, x1 = xs[sel].min(), xs[sel].max() + 1
        y0, y1 = ys[sel].min(), ys[sel].max() + 1
        out.append(to_box(x0, y0, x1, y1))
    return out


def render_character():
    sprites = []          # dict(group,no,img,ax,ay)
    cache = {}            # clave de pose -> (group,no)
    air = {}              # anim -> lista de frames dict
    summary = {}
    for no in sorted(poses.ANIMS):
        a = poses.ANIMS[no]
        frames = []
        prev_pts = None
        prev_key = None
        nidx = 0
        for k, fr in enumerate(a['frames']):
            key = json.dumps(fr.pose, sort_keys=True, default=str)
            tkey = key + ('|T' + prev_key + fr.style if (fr.trail and prev_key) else '')
            canvas, owner, pts = rig.render(fr.pose, trail_from=prev_pts if fr.trail else None, style=fr.style)
            if tkey in cache:
                g, n = cache[tkey]
            else:
                ys, xs = np.nonzero(canvas)
                y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
                img = canvas[y0:y1, x0:x1]
                g, n = no, nidx
                nidx += 1
                sprites.append(dict(group=g, no=n, img=img, ax=rig.AX - x0, ay=rig.AY - y0))
                cache[tkey] = (g, n)
            c2 = []
            if fr.c2:
                up = bbox(owner == 1, 2)
                lg = bbox(owner == 3, 2)
                c2 = [b for b in (up, lg) if b]
            c1 = []
            if fr.c1:
                s = rig.SCALE
                c1 = [(int(x0 * s), int(y0 * s), int(x1 * s), int(y1 * s)) for x0, y0, x1, y1 in fr.c1]
            elif fr.hit:
                c1 = split_boxes((owner == 2) | (owner == 4), 3)
            frames.append(dict(g=g, n=n, t=fr.t, c1=c1, c2=c2, flags=fr.flags))
            prev_pts = pts
            prev_key = key
        air[no] = dict(frames=frames, loop=a['loop'], name=a['name'])
        summary[no] = dict(name=a['name'], n=len(frames),
                           hit=[i + 1 for i, f in enumerate(frames) if f['c1']],
                           total=sum(f['t'] for f in frames if f['t'] > 0))
    return sprites, air, summary


FX_BOXES = {
    6100: dict(c1=[(-18, -16, 18, 16)], c2=[(-18, -16, 18, 16)]),
    6200: dict(c1=[(-22, -98, 24, -6)], c2=[(-22, -98, 24, -6)]),
    6220: dict(c1=[(-22, -98, 24, -6)], c2=[(-22, -98, 24, -6)]),
}


def render_fx(sprites, air):
    fx.build()
    for no, d in fx.FX.items():
        frames = []
        for k, (img, ax, ay, t) in enumerate(d['frames']):
            sprites.append(dict(group=no, no=k, img=img, ax=ax, ay=ay))
            bx = FX_BOXES.get(no, {})
            frames.append(dict(g=no, n=k, t=t, c1=bx.get('c1', []), c2=bx.get('c2', []),
                               trans='A' if d['trans'] == 'add' else ''))
        air[no] = dict(frames=frames, loop=d['loop'], name='FX: ' + d['name'])
    # cut-ins estilo JUS (koma)
    names = [("GOD'S STRENGTH", 'Fuerza de los Dioses'), ('STORM CLEAVE', 'Hendidura de Tormenta'),
             ('ROGUE KNIGHT', 'Juicio del Caballero Errante')]
    for i, (t, s) in enumerate(names):
        img, ax, ay, _ = fx.cutin(t, s)
        sprites.append(dict(group=6700, no=i, img=img, ax=ax, ay=ay))
        air[6700 + i] = dict(frames=[dict(g=6700, n=i, t=-1, c1=[], c2=[])], loop=None,
                             name='Cut-in: ' + t)
    small, big = fx.portraits()
    sprites.insert(0, dict(group=9000, no=1, img=big, ax=0, ay=0))
    sprites.insert(0, dict(group=9000, no=0, img=small, ax=0, ay=0))


def write_air(path, air):
    L = ['; Horned Blade (estilo JUS) - animaciones',
         '; Generado por generator/build.py a partir del esqueleto 2D.',
         '; Clsn1 = cajas de ataque, Clsn2 = cajas de cuerpo (coordenadas relativas al eje).', '']
    for no in sorted(air):
        a = air[no]
        L.append('; ' + a['name'])
        L.append('[Begin Action %d]' % no)
        for i, f in enumerate(a['frames']):
            if a['loop'] is not None and i == a['loop'] and i > 0:
                L.append('Loopstart')
            if f['c2']:
                L.append('Clsn2: %d' % len(f['c2']))
                for j, b in enumerate(f['c2']):
                    L.append(' Clsn2[%d] = %d,%d,%d,%d' % ((j,) + tuple(b)))
            if f['c1']:
                L.append('Clsn1: %d' % len(f['c1']))
                for j, b in enumerate(f['c1']):
                    L.append(' Clsn1[%d] = %d,%d,%d,%d' % ((j,) + tuple(b)))
            line = '%d,%d, 0,0, %d' % (f['g'], f['n'], f['t'])
            if f.get('trans'):
                line += ', ,' + f['trans']
            L.append(line)
        L.append('')
    with open(path, 'w', newline='\r\n') as fh:
        fh.write('\n'.join(L))


def main():
    os.makedirs(OUT, exist_ok=True)
    sprites, air, summary = render_character()
    print('sprites personaje:', len(sprites))
    render_fx(sprites, air)
    print('sprites totales:', len(sprites))
    pal = PL.get()
    MF.write_sff(os.path.join(OUT, NAME + '.sff'), sprites, pal)
    write_air(os.path.join(OUT, NAME + '.air'), air)
    S = sounds.build()
    MF.write_snd(os.path.join(OUT, NAME + '.snd'), S)
    for i in range(len(PL.VARIANTS)):
        MF.write_act(os.path.join(OUT, '%s%d.act' % (NAME, i + 1)), PL.variant(i))
    with open(os.path.join(HERE, 'build', 'anim_summary.json'), 'w') as f:
        json.dump(summary, f, indent=1, ensure_ascii=False)
    # hoja de sprites para el readme
    print('ok ->', OUT)


if __name__ == '__main__':
    main()
