"""Derive an SSJ5 (AF) hair from DMZ's SSJ4 hair: same spiky silhouette, much longer mane down the back."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import dmzhair as h

BACK_LEN, SIDE_LEN, TOP_LEN = 1.9, 1.35, 1.2

def scale_face(tag, key, mult, droop=0.0, straighten=1.0):
    if key not in tag: return
    for s in tag[key][1]['items']:
        ls = s.get('ls', (5, 1.0))[1]
        s['ls'] = (5, round(min(ls * mult, 4.0), 3))
        if droop and 'rx' in s:
            s['rx'] = (5, round(min(s['rx'][1] + droop, 172.0), 3))
        if straighten != 1.0:
            for c in ('cx', 'cz'):
                if c in s: s[c] = (5, round(s[c][1] * straighten, 3))

prefix, tag = h.decode(open(sys.argv[1]).read().strip())
scale_face(tag, 'B', BACK_LEN, droop=12.0, straighten=0.6)   # mane: longer, hangs lower, straighter so it doesn't curl up
scale_face(tag, 'L', SIDE_LEN)
scale_face(tag, 'R', SIDE_LEN)
scale_face(tag, 'T', TOP_LEN)
tag['n'] = (8, 'SSJ5 AF')
code = h.encode(prefix, tag)
assert h.plain(h.decode(code)[1]) == h.plain(tag)
print(code)
