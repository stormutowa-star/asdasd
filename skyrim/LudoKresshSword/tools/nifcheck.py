"""Independent Skyrim SE NIF reader written straight from niftools nif.xml.

Parses the header and the block types used by weapon meshes and checks that
each block consumes exactly the size recorded in the header.
Unknown block types are skipped using the header block sizes.
"""
import struct
import sys
import numpy as np


class R:
    def __init__(self, b, o=0):
        self.b, self.o = b, o

    def take(self, fmt):
        v = struct.unpack_from('<' + fmt, self.b, self.o)
        self.o += struct.calcsize('<' + fmt)
        return v if len(v) > 1 else v[0]

    def raw(self, n):
        v = self.b[self.o:self.o + n]
        self.o += n
        return v

    def sized(self):
        n = self.take('I')
        return self.raw(n).decode('cp1252')

    def export(self):
        n = self.take('B')
        return self.raw(n).rstrip(b'\0').decode('cp1252')


def parse_header(r):
    line_end = r.b.index(b'\n')
    hs = r.raw(line_end + 1).decode()
    h = {'header_string': hs.strip()}
    h['version'] = r.take('I')
    h['endian'] = r.take('B')
    h['user_version'] = r.take('I')
    nblocks = r.take('I')
    h['bs_version'] = r.take('I')
    h['author'] = r.export()
    if h['bs_version'] < 131:
        h['process_script'] = r.export()
    h['export_script'] = r.export()
    if h['bs_version'] >= 103:
        h['max_filepath'] = r.export()
    ntypes = r.take('H')
    h['types'] = [r.sized() for _ in range(ntypes)]
    h['type_index'] = [r.take('H') for _ in range(nblocks)]
    h['sizes_offset'] = r.o
    h['sizes'] = [r.take('I') for _ in range(nblocks)]
    nstr = r.take('I')
    h['max_string_len'] = r.take('I')
    h['strings'] = [r.sized() for _ in range(nstr)]
    ngroups = r.take('I')
    h['groups'] = [r.take('I') for _ in range(ngroups)]
    h['nblocks'] = nblocks
    return h


def objnet(r, d, shader=False):
    if shader:
        d['shader_type'] = r.take('I')
    d['name'] = r.take('i')
    n = r.take('I')
    d['extra'] = [r.take('i') for _ in range(n)]
    d['controller'] = r.take('i')


def avobject(r, d):
    objnet(r, d)
    d['flags'] = r.take('I')
    d['translation'] = r.take('3f')
    d['rotation'] = r.take('9f')
    d['scale'] = r.take('f')
    d['collision'] = r.take('i')


def ninode(r, d):
    avobject(r, d)
    n = r.take('I')
    d['children'] = [r.take('i') for _ in range(n)]
    n = r.take('I')
    d['effects'] = [r.take('i') for _ in range(n)]


def havok_filter(r):
    return r.take('BBH')


def half(x):
    return np.frombuffer(struct.pack('<H', x), dtype=np.float16)[0].item()


def parse_block(t, r, d, h):
    if t in ('NiNode', 'BSFadeNode'):
        ninode(r, d)
    elif t == 'BSXFlags':
        d['name'] = r.take('i'); d['integer'] = r.take('I')
    elif t == 'NiStringExtraData':
        d['name'] = r.take('i'); d['string'] = r.take('i')
    elif t == 'bhkBoxShape':
        d['material'] = r.take('I'); d['radius'] = r.take('f'); r.raw(8)
        d['dims'] = r.take('3f'); d['unused_f'] = r.take('f')
    elif t == 'bhkConvexTransformShape':
        d['shape'] = r.take('i'); d['material'] = r.take('I'); d['radius'] = r.take('f'); r.raw(8)
        d['transform'] = r.take('16f')
    elif t == 'bhkListShape':
        n = r.take('I'); d['subshapes'] = [r.take('i') for _ in range(n)]
        d['material'] = r.take('I')
        d['child_shape_prop'] = r.take('3I'); d['child_filter_prop'] = r.take('3I')
        n = r.take('I'); d['filters'] = [havok_filter(r) for _ in range(n)]
    elif t == 'bhkRigidBody':
        d['shape'] = r.take('i'); d['filter'] = havok_filter(r)
        r.raw(4); d['broadphase'] = r.take('B'); r.raw(3); d['wo_prop'] = r.take('3I')
        d['entity'] = r.take('BBH')
        r.raw(4); d['filter2'] = havok_filter(r); r.raw(4); d['unk_int1'] = r.take('I')
        d['response'] = r.take('BBH')
        d['translation'] = r.take('4f'); d['rotation'] = r.take('4f')
        d['linvel'] = r.take('4f'); d['angvel'] = r.take('4f')
        d['inertia'] = r.take('12f'); d['center'] = r.take('4f')
        (d['mass'], d['lin_damp'], d['ang_damp'], d['time_factor'], d['gravity'], d['friction'],
         d['rolling'], d['restitution'], d['max_lin'], d['max_ang'], d['penetration']) = r.take('11f')
        (d['motion'], d['deactivator'], d['solver_deact'], d['quality'], d['auto_remove'],
         d['resp_mod'], d['num_shape_keys'], d['force_ppu']) = r.take('8B')
        r.raw(12)
        n = r.take('I'); d['constraints'] = [r.take('i') for _ in range(n)]
        d['body_flags'] = r.take('H') if h['bs_version'] >= 76 else r.take('I')
    elif t == 'bhkCollisionObject':
        d['target'] = r.take('i'); d['flags'] = r.take('H'); d['body'] = r.take('i')
    elif t == 'BSTriShape':
        avobject(r, d)
        d['bound'] = r.take('4f')
        d['skin'] = r.take('i'); d['shader'] = r.take('i'); d['alpha'] = r.take('i')
        vd = r.take('Q'); d['vertex_desc'] = vd
        d['num_tris'] = r.take('H'); d['num_verts'] = r.take('H')
        d['data_size'] = r.take('I')
        attrs = vd >> 44
        d['attrs'] = attrs
        expect = (vd & 0xF) * d['num_verts'] * 4 + d['num_tris'] * 6
        assert d['data_size'] == expect, ('data size', d['data_size'], expect)
        verts = []
        if d['data_size'] > 0:
            for _ in range(d['num_verts']):
                start = r.o
                v = {}
                if attrs & 0x1:
                    v['pos'] = r.take('3f')
                    if (attrs & 0x11) == 0x11:
                        v['btx'] = r.take('f')
                    else:
                        r.take('I')
                if attrs & 0x2:
                    hu, hv = r.take('2H'); v['uv'] = (half(hu), half(hv))
                if attrs & 0x8:
                    v['n'] = r.take('3B'); v['bty'] = r.take('B')
                if (attrs & 0x18) == 0x18:
                    v['t'] = r.take('3B'); v['btz'] = r.take('B')
                if attrs & 0x20:
                    v['color'] = r.take('4B')
                if attrs & 0x40:
                    r.take('4H'); r.take('4B')
                if attrs & 0x100:
                    r.take('f')
                assert r.o - start == (vd & 0xF) * 4, ('vertex stride', r.o - start, (vd & 0xF) * 4)
                verts.append(v)
            d['tris'] = [r.take('3H') for _ in range(d['num_tris'])]
        d['verts'] = verts
        d['particle_size'] = r.take('I')
        assert d['particle_size'] == 0
    elif t == 'BSLightingShaderProperty':
        objnet(r, d, shader=True)
        d['sf1'], d['sf2'] = r.take('2I')
        d['uv_offset'] = r.take('2f'); d['uv_scale'] = r.take('2f')
        d['texture_set'] = r.take('i')
        d['emissive'] = r.take('3f'); d['emissive_mult'] = r.take('f')
        d['clamp'] = r.take('I'); d['alpha'] = r.take('f'); d['refraction'] = r.take('f')
        d['gloss'] = r.take('f'); d['spec_color'] = r.take('3f'); d['spec_str'] = r.take('f')
        d['light1'], d['light2'] = r.take('2f')
        st = d['shader_type']
        if st == 1:
            d['env_scale'] = r.take('f')
        elif st == 5:
            d['skin_tint'] = r.take('3f')
        elif st == 6:
            d['hair_tint'] = r.take('3f')
        elif st == 7:
            r.take('2f')
        elif st == 11:
            r.take('6f')
        elif st == 14:
            r.take('4f')
        elif st == 16:
            r.take('7f')
    elif t == 'BSShaderTextureSet':
        n = r.take('I'); d['textures'] = [r.sized() for _ in range(n)]
    else:
        return False
    return True


def read(path):
    b = open(path, 'rb').read()
    r = R(b)
    h = parse_header(r)
    blocks = []
    for i in range(h['nblocks']):
        t = h['types'][h['type_index'][i]]
        size = h['sizes'][i]
        start = r.o
        d = {'type': t, 'index': i, 'offset': start, 'size': size}
        known = parse_block(t, r, d, h)
        if not known:
            r.o = start + size
            d['skipped'] = True
        assert r.o - start == size, f'block {i} {t}: consumed {r.o - start} != header size {size}'
        blocks.append(d)
    nroots = r.take('I')
    roots = [r.take('i') for _ in range(nroots)]
    assert r.o == len(b), ('trailing bytes', len(b) - r.o)
    h['blocks_end'] = blocks[-1]['offset'] + blocks[-1]['size'] if blocks else r.o
    return h, blocks, roots


if __name__ == '__main__':
    h, blocks, roots = read(sys.argv[1])
    print(h['header_string'], hex(h['version']), 'user', h['user_version'], 'bs', h['bs_version'])
    print('strings', h['strings'], 'roots', roots)
    for d in blocks:
        brief = {k: v for k, v in d.items() if k not in ('verts', 'tris')}
        print(brief)
