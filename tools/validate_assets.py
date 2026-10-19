import json
import sys

BASE = "src/main/resources/assets/greatsword"
geo = json.load(open(f"{BASE}/geo/greatsword.geo.json", encoding="utf-8"))
anim = json.load(open(f"{BASE}/animations/greatsword.animation.json", encoding="utf-8"))

print("geo format:", geo.get("format_version"))
g = geo["minecraft:geometry"][0]
print("identifier:", g.get("description", {}).get("identifier"))
bones = g["bones"]
print("bones:", [b["name"] for b in bones])

cube_count = 0
uv_ok = True
for b in bones:
    for c in b.get("cubes", []):
        cube_count += 1
        if "origin" not in c or "size" not in c or "uv" not in c:
            print("BAD cube", b["name"], c)
            uv_ok = False
        for face in c["uv"].values():
            if "uv" not in face or "uv_size" not in face:
                print("BAD face", b["name"], c)
                uv_ok = False
            else:
                u, v = face["uv"]
                w, h = face["uv_size"]
                if not (0 <= u <= 128 and 0 <= v <= 128 and 0 <= w <= 128 and 0 <= h <= 128):
                    print("UV out of range", b["name"], face)
                    uv_ok = False
        if "rotation" in c and "pivot" not in c:
            print("rotation without pivot", b["name"], c)
            uv_ok = False

print("cubes:", cube_count, "uv_ok:", uv_ok)
model_bones = {b["name"] for b in bones}
print("animations:")
for name, a in anim.get("animations", {}).items():
    used = set(a.get("bones", {}).keys())
    missing = used - model_bones
    print("  ", name, "length", a.get("animation_length"), "loop", a.get("loop"),
          "missing_bones", sorted(missing))
print("anim format:", anim.get("format_version"))

required = [
    "animation.greatsword.idle",
    "animation.greatsword.idle_sheathed",
    "animation.greatsword.draw",
    "animation.greatsword.sheathe",
    "animation.greatsword.swing1",
    "animation.greatsword.swing2",
    "animation.greatsword.swing3",
    "animation.greatsword.charge",
    "animation.greatsword.slam",
    "animation.greatsword.tackle",
]
missing = [r for r in required if r not in anim.get("animations", {})]
print("missing required animations:", missing)
sys.exit(1 if missing or not uv_ok else 0)
