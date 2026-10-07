"""Genera GIFs de vista previa de algunas animaciones (para el readme)."""
import os, sys
import numpy as np
from PIL import Image
import rig, poses, palette as PL, fx

OUTDIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'preview')


def frames_of(no, palidx=0, bg=(28, 34, 52), zoom=2, crop=(110, 110, 330, 290)):
    pal = PL.variant(palidx).copy()
    pal[0] = bg
    a = poses.ANIMS[no]
    out = []
    prev = None
    for fr in a['frames']:
        c, o, pts = rig.render(fr.pose, trail_from=prev if fr.trail else None, style=fr.style)
        prev = pts
        im = Image.fromarray(pal[c].astype(np.uint8)).crop(crop)
        im = im.resize((im.width * zoom, im.height * zoom), Image.NEAREST)
        t = fr.t if fr.t > 0 else 30
        out.append((im, t))
    return out


def save_gif(seq, path):
    ims = [s[0] for s in seq]
    durs = [max(20, int(s[1] * 1000 / 60)) for s in seq]
    ims[0].save(path, save_all=True, append_images=ims[1:], duration=durs, loop=0, disposal=2)


if __name__ == '__main__':
    os.makedirs(OUTDIR, exist_ok=True)
    combo = frames_of(0) + frames_of(200) + frames_of(210) + frames_of(220) + frames_of(240) + frames_of(0)
    save_gif(combo, os.path.join(OUTDIR, 'combo.gif'))
    sp = frames_of(1200) + frames_of(1250)[:4] + frames_of(1300) + frames_of(3000, palidx=1)
    save_gif(sp, os.path.join(OUTDIR, 'especiales.gif'))
    walk = frames_of(20) * 2 + frames_of(100) * 2
    save_gif(walk, os.path.join(OUTDIR, 'movimiento.gif'))
    # paletas
    pal_ims = []
    for i in range(len(PL.VARIANTS)):
        im = frames_of(0, palidx=i, zoom=2)[0][0]
        pal_ims.append(im)
    W = sum(i.width for i in pal_ims); H = pal_ims[0].height
    sheet = Image.new('RGB', (W, H))
    x = 0
    for im in pal_ims:
        sheet.paste(im, (x, 0)); x += im.width
    sheet.save(os.path.join(OUTDIR, 'paletas.png'))
    print('ok')
