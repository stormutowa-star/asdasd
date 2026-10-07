"""Post-process the PyNifly output so the Havok blocks match vanilla SE weapons:
  * bhkListShape gets one (empty) HavokFilter per sub shape, as nif.xml and
    Bethesda's files have (nifly writes Num Filters = 0);
  * bhkBoxShape stores the convex radius in the unused 4th dimension float.
"""
import struct
import sys
from nifcheck import read

src, dst = sys.argv[1], sys.argv[2]
b = bytearray(open(src, 'rb').read())
h, blocks, roots = read(src)
first = blocks[0]['offset']
out_blocks = []
for d in blocks:
    raw = bytearray(b[d['offset']:d['offset'] + d['size']])
    if d['type'] == 'bhkListShape' and len(d['filters']) != len(d['subshapes']):
        n = len(d['subshapes'])
        # Num Filters is the last uint of the block when it holds no filters
        assert struct.unpack_from('<I', raw, len(raw) - 4)[0] == 0
        raw = raw[:-4] + struct.pack('<I', n) + b'\0\0\0\0' * n
    if d['type'] == 'bhkBoxShape':
        # material(4) radius(4) unused(8) dims(12) unused_f(4)
        struct.pack_into('<f', raw, 28, d['radius'])
    out_blocks.append(bytes(raw))
header = bytearray(b[:first])
for i, ob in enumerate(out_blocks):
    struct.pack_into('<I', header, h['sizes_offset'] + 4 * i, len(ob))
footer = b[h['blocks_end']:]
open(dst, 'wb').write(bytes(header) + b''.join(out_blocks) + bytes(footer))
h2, blocks2, _ = read(dst)
for d in blocks2:
    if d['type'] in ('bhkListShape', 'bhkBoxShape'):
        print(d['type'], d.get('filters'), d.get('unused_f'))
print('fixed ->', dst)
