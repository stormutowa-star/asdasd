"""
Mini-motor para definir modelos de entidad estilo Minecraft (cubos + UV de caja) y
- pintar la textura por posicion 3D,
- previsualizarlos con un rasterizador de software (misma matematica que ModelPart/Cube de vanilla),
- exportar el codigo Java (LayerDefinition).

Convenciones (las de vanilla):
  * espacio del modelo: x = izquierda de la entidad, y hacia ABAJO, z hacia ATRAS (el frente es -z)
  * en este archivo los offsets/centros se escriben en coordenadas "naturales":
        (x lateral, y ARRIBA, z ADELANTE)  ->  modelo = (x, -y, -z)
  * rotaciones en convencion del modelo (Rz*Ry*Rx), igual que ModelPart.
"""
import math
import numpy as np


def nat2model(p):
    return (p[0], -p[1], -p[2])


class Cube:
    def __init__(self, part, w, h, d, center, mat, opts):
        self.part = part
        self.w, self.h, self.d = int(w), int(h), int(d)
        cx, cy, cz = center
        # esquina minima en espacio del modelo, relativa al pivote de la parte
        self.x0 = cx - w / 2.0
        self.y0 = -(cy + h / 2.0)
        self.z0 = -(cz + d / 2.0)
        self.mat = mat
        self.opts = opts
        self.u = 0
        self.v = 0
        self.polys = None  # se rellena en build_polys()

    @property
    def region_size(self):
        return 2 * (self.d + self.w), self.d + self.h

    def build_polys(self):
        """Replica exacta de ModelPart.Cube (sin mirror). Devuelve poligonos con vertices (locales a la parte) y UV en texels."""
        x, y, z = self.x0, self.y0, self.z0
        w, h, d = self.w, self.h, self.d
        f, f1, f2 = x + w, y + h, z + d
        V7 = (x, y, z); V0 = (f, y, z); V1 = (f, f1, z); V2 = (x, f1, z)
        V3 = (x, y, f2); V4 = (f, y, f2); V5 = (f, f1, f2); V6 = (x, f1, f2)
        u, v = self.u, self.v
        f4 = u; f5 = u + d; f6 = u + d + w; f7 = u + d + w + w; f8 = u + d + w + d; f9 = u + d + w + d + w
        f10 = v; f11 = v + d; f12 = v + d + h

        def poly(name, verts, u1, v1, u2, v2, normal):
            uvs = [(u2, v1), (u1, v1), (u1, v2), (u2, v2)]
            return dict(name=name, verts=[np.array(a, float) for a in verts], uvs=uvs,
                        normal=np.array(normal, float), rect=(u1, v1, u2, v2))

        self.polys = [
            poly('DOWN', [V4, V3, V7, V0], f5, f10, f6, f11, (0, -1, 0)),   # y min (visualmente ARRIBA)
            poly('UP', [V1, V2, V6, V5], f6, f11, f7, f10, (0, 1, 0)),      # y max (visualmente abajo)
            poly('WEST', [V7, V3, V6, V2], f4, f11, f5, f12, (-1, 0, 0)),
            poly('NORTH', [V0, V7, V2, V1], f5, f11, f6, f12, (0, 0, -1)),  # frente
            poly('EAST', [V4, V0, V1, V5], f6, f11, f8, f12, (1, 0, 0)),
            poly('SOUTH', [V3, V4, V5, V6], f8, f11, f9, f12, (0, 0, 1)),
        ]


class Part:
    def __init__(self, name, parent, off, rot=(0, 0, 0)):
        self.name = name
        self.parent = parent
        self.off = nat2model(off)
        self.rot = rot  # (x, y, z) rad
        self.scale = 1.0
        self.cubes = []
        self.children = []
        if parent:
            parent.children.append(self)

    def cube(self, w, h, d, center, mat='scale', **opts):
        c = Cube(self, w, h, d, center, mat, opts)
        self.cubes.append(c)
        return c

    def path(self):
        names = []
        p = self
        while p is not None:
            names.append(p.name)
            p = p.parent
        return list(reversed(names))


class Model:
    def __init__(self, tex_w=256, tex_h=256):
        self.tex_w, self.tex_h = tex_w, tex_h
        self.root = Part('root', None, (0, 24 - 24, 0))  # se coloca en y=24 (suelo) al exportar
        self.root.off = (0, 24, 0)
        self.parts = {'root': self.root}

    def part(self, name, parent, off, rot=(0, 0, 0)):
        assert name not in self.parts, name
        p = Part(name, parent, off, rot)
        self.parts[name] = p
        return p

    def all_cubes(self):
        out = []
        for p in self.parts.values():
            out.extend(p.cubes)
        return out

    # ---------- empaquetado UV ----------
    def pack(self):
        cubes = sorted(self.all_cubes(), key=lambda c: (-c.region_size[1], -c.region_size[0]))
        x = y = 0
        shelf_h = 0
        for c in cubes:
            rw, rh = c.region_size
            if x + rw > self.tex_w:
                x = 0
                y += shelf_h
                shelf_h = 0
            c.u, c.v = x, y
            x += rw
            shelf_h = max(shelf_h, rh)
            if y + rh > self.tex_h:
                raise RuntimeError('textura demasiado pequena: %d x %d' % (self.tex_w, self.tex_h))
        for c in cubes:
            c.build_polys()
        return y + shelf_h

    # ---------- pintar ----------
    def paint(self, painter):
        """painter(cube, face_name, local_model_pos(np3), normal) -> (r,g,b,a) ; local relativo a la parte"""
        img = np.zeros((self.tex_h, self.tex_w, 4), np.uint8)
        for c in self.all_cubes():
            for pl in c.polys:
                u1, v1, u2, v2 = pl['rect']
                P = pl['verts']  # P[0]=(u2,v1) P[1]=(u1,v1) P[2]=(u1,v2) P[3]=(u2,v2)
                ua, ub = sorted((u1, u2)); va, vb = sorted((v1, v2))
                for tv in range(int(va), int(vb)):
                    for tu in range(int(ua), int(ub)):
                        uc, vc = tu + 0.5, tv + 0.5
                        s = (uc - u1) / (u2 - u1)
                        t = (vc - v1) / (v2 - v1)
                        pos = P[1] + s * (P[0] - P[1]) + t * (P[2] - P[1])
                        # borde de cara (0 = justo en el borde)
                        edge = min(tu - ua, ub - 1 - tu, tv - va, vb - 1 - tv)
                        img[tv, tu] = painter(c, pl['name'], pos, pl['normal'], edge)
        return img

    # ---------- transformaciones ----------
    @staticmethod
    def rot_matrix(rx, ry, rz):
        cx, sx = math.cos(rx), math.sin(rx)
        cy, sy = math.cos(ry), math.sin(ry)
        cz, sz = math.cos(rz), math.sin(rz)
        Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
        Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
        Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
        return Rz @ Ry @ Rx

    def world_transform(self, part, pose=None):
        """Matriz (R,t) del espacio local de la parte al espacio del modelo (px). pose: dict nombre->(dx,dy,dz,rx,ry,rz) deltas de animacion."""
        chain = []
        p = part
        while p is not None:
            chain.append(p)
            p = p.parent
        R = np.eye(3)
        t = np.zeros(3)
        for p in reversed(chain):
            off = np.array(p.off, float)
            rot = np.array(p.rot, float)
            if pose and p.name in pose:
                d = pose[p.name]
                off = off + np.array(d[:3], float)
                rot = rot + np.array(d[3:], float)
            t = t + R @ off
            R = R @ self.rot_matrix(*rot) * p.scale
        return R, t

    def polygons_world(self, pose=None):
        """Poligonos en espacio 'mundo' del render: (x,y,z)->(-x,-y,z), unidades de bloque. El frente queda en -Z."""
        out = []
        cache = {}
        for c in self.all_cubes():
            if c.part.name not in cache:
                cache[c.part.name] = self.world_transform(c.part, pose)
            R, t = cache[c.part.name]
            for pl in c.polys:
                vs = []
                for v in pl['verts']:
                    m = R @ v + t
                    vs.append(np.array([-m[0], -m[1], m[2]]) / 16.0)
                n = R @ pl['normal']
                n = np.array([-n[0], -n[1], n[2]])
                out.append(dict(verts=vs, uvs=pl['uvs'], rect=pl['rect'], normal=n, cube=c))
        return out

    # ---------- exportar a Java ----------
    def java_layer(self, class_name, package):
        lines = []
        a = lines.append
        a('package %s;' % package)
        a('')
        a('import java.util.HashMap;')
        a('import java.util.Map;')
        a('')
        a('import net.minecraft.client.model.geom.ModelPart;')
        a('import net.minecraft.client.model.geom.PartPose;')
        a('import net.minecraft.client.model.geom.builders.CubeListBuilder;')
        a('import net.minecraft.client.model.geom.builders.LayerDefinition;')
        a('import net.minecraft.client.model.geom.builders.MeshDefinition;')
        a('import net.minecraft.client.model.geom.builders.PartDefinition;')
        a('')
        a('/** GENERADO por tools/build_assets.py - no editar a mano. */')
        a('public final class %s {' % class_name)
        a('    private %s() {}' % class_name)
        a('')
        a('    public static final int TEX_W = %d;' % self.tex_w)
        a('    public static final int TEX_H = %d;' % self.tex_h)
        a('')
        a('    public static LayerDefinition createBodyLayer() {')
        a('        MeshDefinition mesh = new MeshDefinition();')
        a('        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));')

        def emit(part, parent_var, indent):
            for ch in part.children:
                var = 'p_' + ch.name
                b = 'CubeListBuilder.create()'
                for c in ch.cubes:
                    b += '\n%s        .texOffs(%d, %d).addBox(%sF, %sF, %sF, %dF, %dF, %dF)' % (
                        indent, c.u, c.v, fmt(c.x0), fmt(c.y0), fmt(c.z0), c.w, c.h, c.d)
                if any(abs(r) > 1e-9 for r in ch.rot):
                    pose = 'PartPose.offsetAndRotation(%sF, %sF, %sF, %sF, %sF, %sF)' % (
                        fmt(ch.off[0]), fmt(ch.off[1]), fmt(ch.off[2]), fmt(ch.rot[0]), fmt(ch.rot[1]), fmt(ch.rot[2]))
                else:
                    pose = 'PartPose.offset(%sF, %sF, %sF)' % (fmt(ch.off[0]), fmt(ch.off[1]), fmt(ch.off[2]))
                a('%sPartDefinition %s = %s.addOrReplaceChild("%s", %s,\n%s        %s);' % (indent, var, parent_var, ch.name, b, indent, pose))
                emit(ch, var, indent)

        emit(self.root, 'root', '        ')
        a('        return LayerDefinition.create(mesh, TEX_W, TEX_H);')
        a('    }')
        a('')
        a('    /** Mapa nombre -> parte, para animar. */')
        a('    public static Map<String, ModelPart> collect(ModelPart modelRoot) {')
        a('        Map<String, ModelPart> m = new HashMap<>();')
        a('        ModelPart root = modelRoot.getChild("root");')
        a('        m.put("root", root);')
        for name, p in self.parts.items():
            if name == 'root':
                continue
            path = p.path()[1:]
            expr = 'root' + ''.join('.getChild("%s")' % n for n in path)
            a('        m.put("%s", %s);' % (name, expr))
            if abs(p.scale - 1.0) > 1e-6:
                a('        m.get("%s").xScale = %sF;' % (name, fmt(p.scale)))
                a('        m.get("%s").yScale = %sF;' % (name, fmt(p.scale)))
                a('        m.get("%s").zScale = %sF;' % (name, fmt(p.scale)))
        a('        return m;')
        a('    }')
        a('}')
        return '\n'.join(lines) + '\n'


def fmt(x):
    s = ('%.4f' % x).rstrip('0').rstrip('.')
    if s in ('-0', ''):
        s = '0'
    return s


# =========================== rasterizador de previsualizacion ===========================

def render(polys, tex, size=(900, 900), yaw=0.0, pitch=0.15, scale=None, center=None, bg=(24, 28, 40),
           light=(0.35, 0.8, -0.5), tex_w=None, tex_h=None, ssaa=2):
    """Render ortografico con z-buffer. yaw=0 -> vista frontal (camara en el lado -Z)."""
    W, H = size[0] * ssaa, size[1] * ssaa
    th, tw = tex.shape[0], tex.shape[1]
    cy_, sy_ = math.cos(yaw), math.sin(yaw)
    cp, sp = math.cos(pitch), math.sin(pitch)
    # direccion de la camara -> origen
    cam_dir = np.array([sy_ * cp, sp, -cy_ * cp])  # posicion relativa
    f = -cam_dir
    up_w = np.array([0, 1.0, 0])
    right = np.cross(f, up_w)
    right /= np.linalg.norm(right)
    up = np.cross(right, f)

    allv = np.array([v for p in polys for v in p['verts']])
    if center is None:
        center = (allv.min(0) + allv.max(0)) / 2
    proj = np.stack([(allv - center) @ right, (allv - center) @ up], 1)
    if scale is None:
        ext = max(np.ptp(proj[:, 0]), np.ptp(proj[:, 1]))
        scale = 0.92 * min(W, H) / ext
    img = np.zeros((H, W, 3), np.float32)
    img[:] = np.array(bg, np.float32)
    zbuf = np.full((H, W), 1e9, np.float32)
    Ld = np.array(light); Ld /= np.linalg.norm(Ld)

    def tri(p0, p1, p2, t0, t1, t2, shade):
        # p: (sx, sy, depth)  t: (u, v) en texels
        minx = int(max(0, math.floor(min(p0[0], p1[0], p2[0]))))
        maxx = int(min(W - 1, math.ceil(max(p0[0], p1[0], p2[0]))))
        miny = int(max(0, math.floor(min(p0[1], p1[1], p2[1]))))
        maxy = int(min(H - 1, math.ceil(max(p0[1], p1[1], p2[1]))))
        if minx > maxx or miny > maxy:
            return
        den = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1])
        if abs(den) < 1e-9:
            return
        xs = np.arange(minx, maxx + 1) + 0.5
        ys = np.arange(miny, maxy + 1) + 0.5
        X, Y = np.meshgrid(xs, ys)
        l0 = ((p1[1] - p2[1]) * (X - p2[0]) + (p2[0] - p1[0]) * (Y - p2[1])) / den
        l1 = ((p2[1] - p0[1]) * (X - p2[0]) + (p0[0] - p2[0]) * (Y - p2[1])) / den
        l2 = 1 - l0 - l1
        m = (l0 >= -1e-6) & (l1 >= -1e-6) & (l2 >= -1e-6)
        if not m.any():
            return
        Z = l0 * p0[2] + l1 * p1[2] + l2 * p2[2]
        U = l0 * t0[0] + l1 * t1[0] + l2 * t2[0]
        V = l0 * t0[1] + l1 * t1[1] + l2 * t2[1]
        ui = np.clip(np.floor(U).astype(int), 0, tw - 1)
        vi = np.clip(np.floor(V).astype(int), 0, th - 1)
        col = tex[vi, ui]
        a = col[..., 3] > 127
        sub = zbuf[miny:maxy + 1, minx:maxx + 1]
        ok = m & a & (Z < sub)
        if not ok.any():
            return
        sub[ok] = Z[ok]
        reg = img[miny:maxy + 1, minx:maxx + 1]
        reg[ok] = col[..., :3][ok].astype(np.float32) * shade

    for p in polys:
        vs = p['verts']
        n = p['normal']
        nn = np.linalg.norm(n)
        if nn > 0:
            n = n / nn
        shade = 0.55 + 0.45 * max(0.0, float(n @ Ld)) + 0.1 * abs(float(n @ f)) * 0.0
        shade = min(1.15, shade + 0.15)
        pts = []
        for v in vs:
            d = v - center
            pts.append((W / 2 + (d @ right) * scale, H / 2 - (d @ up) * scale, float(d @ f)))
        t = p['uvs']
        tri(pts[0], pts[1], pts[2], t[0], t[1], t[2], shade)
        tri(pts[0], pts[2], pts[3], t[0], t[2], t[3], shade)
    out = np.clip(img, 0, 255).astype(np.uint8)
    if ssaa > 1:
        out = out.reshape(size[1], ssaa, size[0], ssaa, 3).mean((1, 3)).astype(np.uint8)
    return out, center, scale
