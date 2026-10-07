import sys, os, numpy as np
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcmodel, dragon_def
from PIL import Image
out=[]
for v in sys.argv[1:]:
    a=[float(x) for x in v.split(',')]
    dragon_def.WING_RAISE, dragon_def.WING_SWEEP, dragon_def.WING_PITCH = a[0], a[1], a[2]
    dragon_def.WING_OUT = (a[3], a[4], a[5])
    M=dragon_def.build(); M.pack(); tex=M.paint(dragon_def.paint); polys=M.polygons_world()
    img,_,_=mcmodel.render(polys,tex,size=(350,350),yaw=-0.75,pitch=0.05,bg=(60,40,60),ssaa=1)
    out.append(Image.fromarray(img))
W=Image.new('RGB',(350*len(out),350))
for i,im in enumerate(out): W.paste(im,(i*350,0))
W.save('preview/wingtest.png')
