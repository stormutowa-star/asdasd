"""Render del modelo con la camara de la carta, junto a la carta, para comparar."""
import os, sys, math
import numpy as np
from PIL import Image
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
import mcmodel, dragon_def
yaw = float(sys.argv[1]) if len(sys.argv) > 1 else 0.7
pitch = float(sys.argv[2]) if len(sys.argv) > 2 else -0.05
M = dragon_def.build(); M.pack()
tex = M.paint(dragon_def.paint)
polys = M.polygons_world()
img, c, s = mcmodel.render(polys, tex, size=(430, 430), yaw=yaw, pitch=pitch, bg=(60, 40, 60))
ref = Image.open(sys.argv[3]).convert('RGB')
card = ref.crop((10, 10, 390, 425)).resize((430, 470)) if ref.size[0] < 500 else ref.resize((470, 470)).crop((20, 0, 450, 470))
W = Image.new('RGB', (860, 470), (0, 0, 0))
W.paste(card, (0, 0)); W.paste(Image.fromarray(img), (430, 20))
W.save(os.path.join(HERE, 'preview', 'compare.png'))
