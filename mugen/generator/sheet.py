"""vista previa: renderiza animaciones de poses.ANIMS en hojas de contacto."""
import sys, json
import numpy as np
from PIL import Image, ImageDraw
import rig, poses, palette as PL

pal = PL.get().copy()
pal[0] = (110, 130, 150)

def render_anim(no):
    a = poses.ANIMS[no]
    out = []
    prev_pts = None
    for fr in a['frames']:
        c, o, pts = rig.render(fr.pose, trail_from=prev_pts if fr.trail else None, style=fr.style)
        out.append(c)
        prev_pts = pts
    return out

def make(nos, path, zoom=2, crop=(125, 125, 330, 285)):
    rows = []
    for no in nos:
        fr = render_anim(no)
        ims = [Image.fromarray(pal[c].astype(np.uint8)).crop(crop) for c in fr]
        w, h = ims[0].size
        row = Image.new('RGB', (w * len(ims) + 70, h), (40, 40, 40))
        d = ImageDraw.Draw(row)
        d.text((4, 4), str(no), fill=(255, 255, 0))
        for i, im in enumerate(ims):
            row.paste(im, (70 + i * w, 0))
            d.line([(70 + i * w, 0), (70 + i * w, h)], fill=(0, 0, 0))
        rows.append(row)
    W = max(r.width for r in rows); H = sum(r.height for r in rows)
    sh = Image.new('RGB', (W, H), (40, 40, 40)); y = 0
    for r in rows:
        sh.paste(r, (0, y)); y += r.height
    sh = sh.resize((sh.width * zoom, sh.height * zoom), Image.NEAREST)
    sh.save(path)

if __name__ == '__main__':
    nos = [int(x) for x in sys.argv[2:]]
    make(nos, sys.argv[1])
