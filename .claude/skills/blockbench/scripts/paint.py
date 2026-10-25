#!/usr/bin/env python3
"""Uso: paint.py <modelo.bbmodel> [paleta.json] [dir_texturas]
Pinta la textura base y la _glow segun el 'kl_mat'/'kl_glow' de cada cubo, y las incrusta en el .bbmodel.
Paleta: {"mat": "#RRGGBB" | {"color": "#RRGGBB", "noise": 6, "edge": true}}; si no se da archivo se usa
la paleta (kl_palette) guardada por build_model.js."""
import base64, io, json, os, random, sys
from PIL import Image

# Sombreado por cara: arriba +12 %, lados -8/-16 %, abajo -25 %
SHADE = {"up": 1.12, "north": 1.0, "south": 0.92, "east": 0.92, "west": 0.84, "down": 0.75}


def hex_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def clamp(v):
    return max(0, min(255, int(round(v))))


def main():
    path = sys.argv[1]
    model = json.load(open(path))
    palette = model.get("kl_palette", {})
    if len(sys.argv) > 2 and sys.argv[2] != "-":
        palette = json.load(open(sys.argv[2]))
    name = model.get("kl_name") or os.path.splitext(os.path.basename(path))[0]
    root = os.path.dirname(os.path.dirname(os.path.abspath(path)))
    outdir = sys.argv[3] if len(sys.argv) > 3 else os.path.join(root, "src/main/resources/assets/keinloree/textures/entity")
    os.makedirs(outdir, exist_ok=True)
    tw, th = model["resolution"]["width"], model["resolution"]["height"]
    base = Image.new("RGBA", (tw, th), (0, 0, 0, 0))
    glow = Image.new("RGBA", (tw, th), (0, 0, 0, 0))
    bp, gp = base.load(), glow.load()
    rng = random.Random(1234)
    for el in model["elements"]:
        mat = palette.get(el.get("kl_mat", "default"), "#808080")
        if isinstance(mat, str):
            mat = {"color": mat}
        col = hex_rgb(mat["color"])
        noise = mat.get("noise", 6)
        edge = mat.get("edge", True)
        is_glow = el.get("kl_glow", False)
        for fname, face in el["faces"].items():
            u0, v0, u1, v1 = face["uv"]
            x0, x1 = sorted((int(round(u0)), int(round(u1))))
            y0, y1 = sorted((int(round(v0)), int(round(v1))))
            w, h = x1 - x0, y1 - y0
            if w <= 0 or h <= 0:
                continue
            k = SHADE.get(fname, 1.0)
            for y in range(y0, y1):
                for x in range(x0, x1):
                    if not (0 <= x < tw and 0 <= y < th):
                        continue
                    f = k
                    if edge and w >= 3 and h >= 3 and (x in (x0, x1 - 1) or y in (y0, y1 - 1)):
                        f *= 0.8
                    n = rng.uniform(-noise, noise)
                    px = (clamp(col[0] * f + n), clamp(col[1] * f + n), clamp(col[2] * f + n), 255)
                    bp[x, y] = px
                    if is_glow:
                        gp[x, y] = px
    base.save(os.path.join(outdir, name + ".png"))
    glow.save(os.path.join(outdir, name + "_glow.png"))

    def embed(img, fname, tid, mode):
        buf = io.BytesIO()
        img.save(buf, "PNG")
        return {"path": "", "name": fname, "folder": "", "namespace": "", "id": str(tid), "width": tw, "height": th,
                "uv_width": tw, "uv_height": th, "particle": False, "use_as_default": tid == 0,
                "layers_enabled": False, "sync_to_project": "", "render_mode": mode, "render_sides": "auto",
                "frame_time": 1, "frame_order_type": "loop", "frame_order": "", "frame_interpolate": False,
                "visible": True, "internal": True, "saved": True, "uuid": "00000000-0000-4000-8000-00000000000%d" % tid,
                "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()}

    model["textures"] = [embed(base, name + ".png", 0, "default"), embed(glow, name + "_glow.png", 1, "emissive")]
    for el in model["elements"]:
        for face in el["faces"].values():
            face["texture"] = 0
    json.dump(model, open(path, "w"))
    print("paint: %s -> %s/%s.png (+_glow)" % (path, outdir, name))


main()
