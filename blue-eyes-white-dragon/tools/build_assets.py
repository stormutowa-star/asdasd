"""
Genera: textura del dragon, textura emisiva (ojos), modelo Java y previsualizaciones.
Uso:  python3 tools/build_assets.py [--preview-only]
"""
import os, sys, math
import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mcmodel
import dragon_def

ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'src/main/resources/assets/blueeyes/textures/entity')
JAVA = os.path.join(ROOT, 'src/main/java/com/ricardo/blueeyes')
PREV = os.path.join(HERE, 'preview')

def main():
    M = dragon_def.build()
    used = M.pack()
    print('textura usada hasta v =', used, 'de', M.tex_h, '| cubos:', len(M.all_cubes()))
    tex = M.paint(dragon_def.paint)
    glow = M.paint(dragon_def.paint_glow)
    os.makedirs(PREV, exist_ok=True)
    if '--preview-only' not in sys.argv:
        os.makedirs(RES, exist_ok=True)
        Image.fromarray(tex, 'RGBA').save(os.path.join(RES, 'blue_eyes_white_dragon.png'))
        Image.fromarray(glow, 'RGBA').save(os.path.join(RES, 'blue_eyes_white_dragon_eyes.png'))
        with open(os.path.join(JAVA, 'client', 'BlueEyesLayer.java'), 'w') as f:
            f.write(M.java_layer('BlueEyesLayer', 'com.ricardo.blueeyes.client'))
    Image.fromarray(tex, 'RGBA').resize((M.tex_w*3, M.tex_h*3), Image.NEAREST).save(os.path.join(PREV, 'texture.png'))
    polys = M.polygons_world()
    views = {'front': (0.0, 0.12), 'threeq': (-0.7, 0.18), 'side': (-math.pi/2, 0.05), 'back': (math.pi, 0.12)}
    # misma escala y centro para todas
    _, center, scale = mcmodel.render(polys, tex, size=(700, 700), yaw=0, pitch=0.1)
    ext = polys and np.array([v for p in polys for v in p['verts']])
    print('extent (bloques) x:%.2f..%.2f y:%.2f..%.2f z:%.2f..%.2f' % (ext[:,0].min(), ext[:,0].max(), -ext[:,1].max()+0, ext[:,1].min()*-1, ext[:,2].min(), ext[:,2].max()))
    # anclajes (para Java): boca y ojos en espacio de entidad (bloques, pies en y=0, +z delante)
    def anchor(part, nat):
        R, t = M.world_transform(M.parts[part])
        m = R @ np.array([nat[0], -nat[1], -nat[2]], float) + t
        return (-m[0] / 16.0, (24 - m[1]) / 16.0 * 1.0, -m[2] / 16.0)
    print('boca(punta)   x=%.2f y=%.2f z=%.2f' % anchor('head', (0, 3.0, 38.0)))
    print('ojo/cabeza    x=%.2f y=%.2f z=%.2f' % anchor('head', (0, 6.0, 6.0)))
    for name, (yaw, pitch) in views.items():
        img, _, _ = mcmodel.render(polys, tex, size=(700, 700), yaw=yaw, pitch=pitch, scale=scale*0.95, center=center)
        Image.fromarray(img).save(os.path.join(PREV, name + '.png'))
    # primeros planos
    def cam_at(part, nat):
        R, t = M.world_transform(M.parts[part])
        m = R @ np.array([nat[0], -nat[1], -nat[2]], float) + t
        return np.array([-m[0], -m[1], m[2]]) / 16.0
    hc = cam_at('head', (0, 6, 14))
    for name, yaw, pitch in (('head_front', 0.3, 0.1), ('head_side', -math.pi/2, 0.0), ('head_3q', -0.8, 0.25)):
        img, _, _ = mcmodel.render(polys, tex, size=(700, 700), yaw=yaw, pitch=pitch, scale=scale*3.2, center=hc)
        Image.fromarray(img).save(os.path.join(PREV, name + '.png'))
    print('ok')

if __name__ == '__main__':
    main()
