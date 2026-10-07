import sys, numpy as np
from PIL import Image
import rig, palette as PL
pal = PL.get()

def img_of(canvas, crop=True, zoom=3, bg=(255, 0, 255)):
    p = pal.copy(); p[0] = bg
    im = Image.fromarray(p[canvas].astype(np.uint8))
    if crop:
        ys, xs = np.nonzero(canvas)
        im = im.crop((xs.min() - 4, ys.min() - 4, xs.max() + 5, ys.max() + 5))
    return im.resize((im.width * zoom, im.height * zoom), Image.NEAREST)

def sheet(canvases, zoom=2, cols=8, bg=(120, 140, 160)):
    ims = []
    for c in canvases:
        p = pal.copy(); p[0] = bg
        im = Image.fromarray(p[c].astype(np.uint8)).crop((60, 60, 380, 300))
        ims.append(im.resize((im.width * zoom, im.height * zoom), Image.NEAREST))
    w, h = ims[0].size
    rows = (len(ims) + cols - 1) // cols
    out = Image.new('RGB', (w * min(cols, len(ims)), h * rows), (0, 0, 0))
    for i, im in enumerate(ims):
        out.paste(im, ((i % cols) * w, (i // cols) * h))
    return out
