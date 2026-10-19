#!/usr/bin/env python3
"""
Generates the GeckoLib model, its animation file, the pixel-art texture and the mod logo.

The greatsword is modelled in the same coordinate convention as a Bedrock/GeckoLib item:
origin at the bottom of the grip, +Y towards the tip, blade broad faces on the Z axis.
All dimensions are in model pixels (1/16 block).
"""
from __future__ import annotations

import json
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "greatsword"

TEX_SIZE = 128

# Texture regions: (x, y, width, height)
BLADE_SIDE = (0, 0, 32, 64)
BLADE_EDGE = (32, 0, 8, 32)
GOLD = (40, 0, 16, 16)
GOLD_DARK = (56, 0, 16, 16)
GOLD_ORNAMENT = (40, 16, 16, 16)
GOLD_LIGHT = (56, 16, 16, 16)
LEATHER = (72, 0, 16, 16)
LEATHER_DARK = (72, 16, 16, 16)
IRON_DARK = (88, 0, 16, 16)
STEEL_LIGHT = (104, 0, 16, 16)
RIVET = (120, 0, 8, 8)
DARK = (88, 16, 16, 16)

BLADE_BASE_Y = 13.0
BLADE_TIP_Y = 54.0


# ---------------------------------------------------------------------------
# Cubes / bones
# ---------------------------------------------------------------------------

def face(region, uv_size=None):
    x, y, w, h = region
    if uv_size is None:
        uv_size = (w, h)
    return {"uv": [x, y], "uv_size": [uv_size[0], uv_size[1]]}


def all_faces(region, side_region=None, top_region=None):
    side = side_region or region
    top = top_region or region
    return {
        "north": face(region),
        "south": face(region),
        "east": face(side),
        "west": face(side),
        "up": face(top),
        "down": face(top),
    }


def blade_faces(y0, y1, width_region=BLADE_SIDE):
    """Maps each blade segment to its own vertical slice of the blade texture."""
    frac0 = (y0 - BLADE_BASE_Y) / (BLADE_TIP_Y - BLADE_BASE_Y)
    frac1 = (y1 - BLADE_BASE_Y) / (BLADE_TIP_Y - BLADE_BASE_Y)
    x, y, w, h = width_region
    v0 = y + frac0 * h
    v1 = y + frac1 * h
    return {
        "north": {"uv": [x, v0], "uv_size": [w, v1 - v0]},
        "south": {"uv": [x, v0], "uv_size": [w, v1 - v0]},
        "east": face(BLADE_EDGE),
        "west": face(BLADE_EDGE),
        "up": face(IRON_DARK),
        "down": face(IRON_DARK),
    }


def cube(origin, size, faces, rotation=None, pivot=None):
    result = {
        "origin": [round(v, 3) for v in origin],
        "size": [round(v, 3) for v in size],
        "uv": faces,
    }
    if rotation is not None:
        result["rotation"] = rotation
    if pivot is not None:
        result["pivot"] = pivot
    return result


def build_model():
    handle_cubes = []
    guard_cubes = []
    blade_cubes = []

    # ---- Pommel and grip -------------------------------------------------
    handle_cubes.append(cube((-2.0, -2.6, -2.0), (4.0, 2.2, 4.0),
                             all_faces(GOLD_DARK, GOLD, GOLD_ORNAMENT)))
    handle_cubes.append(cube((-1.1, -2.1, -2.15), (2.2, 1.2, 4.3),
                             all_faces(IRON_DARK)))
    handle_cubes.append(cube((-1.4, 0.0, -1.4), (2.8, 10.0, 2.8),
                             all_faces(LEATHER, LEATHER_DARK, LEATHER_DARK)))

    for ring_y in (1.1, 4.0, 6.9, 9.2):
        handle_cubes.append(cube((-1.75, ring_y, -1.75), (3.5, 0.62, 3.5),
                                 all_faces(GOLD, GOLD_LIGHT, GOLD_LIGHT)))

    # leather wrap ridges (slightly larger than the core so they read in silhouette)
    for wrap_y in (2.3, 5.2, 8.1):
        handle_cubes.append(cube((-1.5, wrap_y, -1.5), (3.0, 0.35, 3.0),
                                 all_faces(LEATHER_DARK, LEATHER, LEATHER_DARK)))

    # collar between grip and guard
    handle_cubes.append(cube((-2.1, 9.7, -2.1), (4.2, 1.7, 4.2),
                             all_faces(GOLD_DARK, GOLD, GOLD_ORNAMENT)))

    # ---- Guard -----------------------------------------------------------
    guard_cubes.append(cube((-3.2, 11.3, -2.1), (6.4, 3.1, 4.2),
                            all_faces(GOLD, GOLD_DARK, GOLD_ORNAMENT)))
    guard_cubes.append(cube((-7.2, 11.8, -1.5), (14.4, 2.2, 3.0),
                            all_faces(GOLD_DARK, GOLD, GOLD_LIGHT)))
    guard_cubes.append(cube((-8.0, 12.2, -1.7), (1.7, 3.0, 3.4),
                            all_faces(GOLD, GOLD_DARK, GOLD_ORNAMENT)))
    guard_cubes.append(cube((6.3, 12.2, -1.7), (1.7, 3.0, 3.4),
                            all_faces(GOLD, GOLD_DARK, GOLD_ORNAMENT)))

    # fangs sweeping up from the guard
    guard_cubes.append(cube((-5.6, 13.9, -0.9), (1.25, 2.8, 1.8),
                            all_faces(GOLD, GOLD_DARK, GOLD_ORNAMENT)))
    guard_cubes.append(cube((4.35, 13.9, -0.9), (1.25, 2.8, 1.8),
                            all_faces(GOLD, GOLD_DARK, GOLD_ORNAMENT)))

    # iron plate that seats the blade
    guard_cubes.append(cube((-6.0, 12.9, -1.25), (12.0, 1.6, 2.5),
                            all_faces(IRON_DARK, DARK, IRON_DARK)))

    # ---- Blade -----------------------------------------------------------
    # Broad forged blade in five main steps, finished with a rotated diamond tip.
    segments = [
        (13.0, 24.0, 14.0, 2.30),
        (24.0, 33.0, 13.0, 2.20),
        (33.0, 41.0, 11.0, 2.10),
        (41.0, 47.0, 8.5, 2.00),
        (47.0, 51.0, 5.5, 1.90),
    ]

    for index, (y0, y1, width, depth) in enumerate(segments):
        blade_cubes.append(cube((-width / 2, y0, -depth / 2), (width, y1 - y0, depth),
                                blade_faces(y0, y1)))

        # Forged bevels along both cutting edges. Keep them slim so the broad blade face stays readable.
        if width > 2.0:
            bevel = 0.34
            blade_cubes.append(cube((-width / 2, y0, -depth / 2 - 0.03),
                                    (bevel, y1 - y0, depth + 0.06),
                                    all_faces(STEEL_LIGHT, BLADE_EDGE, IRON_DARK)))
            blade_cubes.append(cube((width / 2 - bevel, y0, -depth / 2 - 0.03),
                                    (bevel, y1 - y0, depth + 0.06),
                                    all_faces(STEEL_LIGHT, BLADE_EDGE, IRON_DARK)))

        # Narrow raised spine. It shrinks with the blade so it never floats off the surface.
        if y0 < 41.0:
            spine_y1 = min(y1, 41.0)
            blade_cubes.append(cube((-0.9, y0, -depth / 2 - 0.16),
                                    (1.8, spine_y1 - y0, depth + 0.32),
                                    all_faces(IRON_DARK, DARK, IRON_DARK)))

        # small brass rivets on both faces near the base
        if index < 3:
            for side in (-1, 1):
                z = side * (depth / 2 + 0.14)
                blade_cubes.append(cube(
                    (-3.9 if side < 0 else 3.0, y0 + 2.3,
                    z - 0.14),
                    (0.9, 0.9, 0.28), all_faces(RIVET)))

    # Rotated diamond tip: a clean point without a staircase silhouette.
    tip_side = 3.89
    blade_cubes.append(cube((-tip_side / 2, 51.0 - tip_side / 2, -0.9),
                            (tip_side, tip_side, 1.8),
                            blade_faces(49.0, 54.0),
                            rotation=[0, 0, 45], pivot=[0, 51.0, 0]))

    # Short brass reinforcement near the guard, plus a small emblem plate on both faces.
    blade_cubes.append(cube((-0.7, 14.3, -1.48), (1.4, 5.0, 2.96),
                            all_faces(GOLD_ORNAMENT, GOLD_DARK, GOLD)))
    for side in (-1, 1):
        z = side * 1.06 - (0.14 if side < 0 else 0.0)
        blade_cubes.append(cube((-1.2, 16.0, z), (2.4, 2.4, 0.28),
                                all_faces(GOLD_ORNAMENT, GOLD_DARK, GOLD_LIGHT)))

    # A few dark battle notches on the left edge near the guard.
    for notch_y in (16.2, 19.4, 22.6):
        blade_cubes.append(cube((-6.15, notch_y, -1.25), (0.9, 1.15, 2.5),
                                all_faces(IRON_DARK, DARK, DARK)))

    bones = [
        {
            "name": "root",
            "pivot": [0, 0, 0],
        },
        {
            "name": "handle",
            "parent": "root",
            "pivot": [0, 0, 0],
            "cubes": handle_cubes,
        },
        {
            "name": "guard",
            "parent": "root",
            "pivot": [0, 11.3, 0],
            "cubes": guard_cubes,
        },
        {
            "name": "blade",
            "parent": "root",
            "pivot": [0, 13.0, 0],
            "cubes": blade_cubes,
        },
    ]

    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.greatsword",
                    "texture_width": TEX_SIZE,
                    "texture_height": TEX_SIZE,
                    "visible_bounds_width": 4.0,
                    "visible_bounds_height": 4.5,
                    "visible_bounds_offset": [0, 1.8, 0],
                },
                "bones": bones,
            }
        ],
    }


# ---------------------------------------------------------------------------
# Animation file
# ---------------------------------------------------------------------------

def keyframe(time_value, rotation=None, position=None, scale=None):
    frame = {}
    if rotation is not None:
        frame["rotation"] = rotation
    if position is not None:
        frame["position"] = position
    if scale is not None:
        frame["scale"] = scale
    return (time_value, frame)


def animation(length, loop, frames, hold_last=False):
    bones = {}
    for bone_name, keys in frames.items():
        bone = {"rotation": {}}
        for time_value, frame in keys:
            if "rotation" in frame:
                bone["rotation"][f"{time_value:.2f}"] = frame["rotation"]
            if "position" in frame:
                bone.setdefault("position", {})[f"{time_value:.2f}"] = frame["position"]
            if "scale" in frame:
                bone.setdefault("scale", {})[f"{time_value:.2f}"] = frame["scale"]
        bones[bone_name] = bone

    anim = {
        "loop": "hold_on_last_frame" if hold_last else loop,
        "animation_length": length,
        "bones": bones,
    }
    return anim


def build_animations():
    animations = {}

    animations["animation.greatsword.idle"] = animation(
        2.0, True, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(1.0, [1.2, 0.0, 1.6]),
                keyframe(2.0, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.idle_sheathed"] = animation(
        3.2, True, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(1.6, [0.8, 1.2, 1.0]),
                keyframe(3.2, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.draw"] = animation(
        0.45, False, {
            "root": [
                keyframe(0.0, [62.0, 0.0, -18.0]),
                keyframe(0.14, [38.0, -8.0, 8.0]),
                keyframe(0.30, [12.0, 4.0, 2.0]),
                keyframe(0.45, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.sheathe"] = animation(
        0.40, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.18, [26.0, 0.0, -8.0]),
                keyframe(0.40, [58.0, 0.0, -14.0]),
            ],
        })

    animations["animation.greatsword.swing1"] = animation(
        0.62, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.10, [18.0, -12.0, -48.0]),
                keyframe(0.26, [-6.0, 8.0, 58.0]),
                keyframe(0.42, [-3.0, 4.0, 30.0]),
                keyframe(0.62, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.swing2"] = animation(
        0.62, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.10, [18.0, 12.0, 48.0]),
                keyframe(0.26, [-6.0, -8.0, -58.0]),
                keyframe(0.42, [-3.0, -4.0, -30.0]),
                keyframe(0.62, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.swing3"] = animation(
        0.78, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.16, [46.0, 0.0, -10.0]),
                keyframe(0.32, [-108.0, 0.0, 4.0]),
                keyframe(0.48, [-88.0, 0.0, 2.0]),
                keyframe(0.78, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.charge"] = animation(
        0.7, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.24, [18.0, 0.0, 0.0]),
                keyframe(0.36, [17.0, 1.5, 1.4]),
                keyframe(0.50, [17.5, -1.5, -1.4]),
                keyframe(0.70, [18.0, 0.0, 0.0]),
            ],
        }, hold_last=False)

    animations["animation.greatsword.slam"] = animation(
        0.72, False, {
            "root": [
                keyframe(0.0, [18.0, 0.0, 0.0]),
                keyframe(0.10, [-112.0, 0.0, 0.0]),
                keyframe(0.26, [-94.0, 0.0, 0.0]),
                keyframe(0.50, [-72.0, 0.0, 0.0]),
                keyframe(0.72, [0.0, 0.0, 0.0]),
            ],
        })

    animations["animation.greatsword.tackle"] = animation(
        0.50, False, {
            "root": [
                keyframe(0.0, [0.0, 0.0, 0.0]),
                keyframe(0.10, [-78.0, 0.0, 0.0]),
                keyframe(0.28, [-82.0, 0.0, 0.0]),
                keyframe(0.50, [0.0, 0.0, 0.0]),
            ],
        })

    return {"format_version": "1.8.0", "animations": animations}


# ---------------------------------------------------------------------------
# Texture
# ---------------------------------------------------------------------------

def lerp(a, b, t):
    return int(a + (b - a) * t)


def lerp_rgb(c1, c2, t):
    return tuple(lerp(c1[i], c2[i], t) for i in range(3))


def draw_gradient_rect(draw, region, top, bottom):
    x, y, w, h = region
    for row in range(h):
        color = lerp_rgb(top, bottom, row / max(1, h - 1))
        draw.line((x, y + row, x + w - 1, y + row), fill=color)


def add_noise(img, region, rng, strength=7):
    x, y, w, h = region
    pixels = img.load()
    for py in range(y, y + h):
        for px in range(x, x + w):
            r, g, b = pixels[px, py]
            n = rng.randint(-strength, strength)
            pixels[px, py] = (max(0, min(255, r + n)),
                              max(0, min(255, g + n)),
                              max(0, min(255, b + n)))


def build_texture():
    img = Image.new("RGB", (TEX_SIZE, TEX_SIZE), (22, 24, 22))
    draw = ImageDraw.Draw(img)
    rng = random.Random(20261019)

    # Base forged steel: dark iron with a soft vertical gradient.
    draw_gradient_rect(draw, BLADE_SIDE, (92, 97, 88), (64, 68, 61))
    add_noise(img, BLADE_SIDE, rng, 3)

    # Cutting bevels: one bright pixel on each edge, then a darker forged bevel.
    for ex in (0, 31):
        draw.line((ex, 0, ex, 63), fill=(150, 156, 142))
    for ex in (1, 2, 29, 30):
        draw.line((ex, 0, ex, 63), fill=(108, 114, 103))
    for ex in (3, 4, 5, 26, 27, 28):
        draw.line((ex, 0, ex, 63), fill=(72, 77, 70))

    # Subtle central fuller, never black.
    for ex in range(13, 19):
        draw.line((ex, 0, ex, 63), fill=(70, 75, 67))
    draw.line((13, 0, 13, 63), fill=(54, 59, 52))
    draw.line((18, 0, 18, 63), fill=(54, 59, 52))
    draw.line((15, 0, 15, 63), fill=(82, 87, 78))

    # Faint temper bands.
    for band_y in range(6, 64, 13):
        draw.line((6, band_y, 25, band_y), fill=(80, 85, 76))
        draw.line((6, band_y + 1, 25, band_y + 1), fill=(62, 67, 60))

    # Forge scratches and worn spots. Long vertical strokes read better than pixel noise.
    for _ in range(34):
        sx = rng.randint(6, 25)
        sy = rng.randint(0, 62)
        draw.line((sx, sy, sx, min(63, sy + rng.randint(2, 9))),
                  fill=(112, 117, 105) if rng.random() < 0.55 else (52, 56, 50))
    for _ in range(20):
        sx = rng.randint(6, 25)
        sy = rng.randint(0, 62)
        draw.line((sx, sy, min(26, sx + rng.randint(1, 4)), sy), fill=(84, 89, 80))
    for _ in range(14):
        sx = rng.randint(6, 25)
        sy = rng.randint(0, 62)
        draw.point((sx, sy), fill=(128, 133, 118))

    # Battle notches near the base.
    for ny in (17, 28, 40, 51):
        draw.polygon([(0, ny), (5, ny + 2), (0, ny + 4)], fill=(34, 37, 33))
        draw.line((0, ny, 4, ny + 1), fill=(144, 149, 136))

    # Brass flecks at the base of the blade.
    for _ in range(18):
        bx = rng.randint(8, 23)
        by = rng.randint(4, 17)
        draw.point((bx, by), fill=(176, 143, 74))

    # Blade edge strips.
    draw_gradient_rect(draw, BLADE_EDGE, (132, 138, 126), (86, 92, 83))
    add_noise(img, BLADE_EDGE, rng, 3)
    for ex in range(0, 8, 3):
        draw.line((32 + ex, 0, 32 + ex, 31), fill=(150, 156, 143))

    # Gold / brass.
    draw_gradient_rect(draw, GOLD, (176, 143, 74), (122, 93, 40))
    for gx in range(40, 56, 3):
        draw.line((gx, 0, gx, 15), fill=(205, 174, 103))
    draw.line((40, 2, 55, 2), fill=(96, 72, 30))
    draw.line((40, 13, 55, 13), fill=(96, 72, 30))
    add_noise(img, GOLD, rng, 5)

    draw_gradient_rect(draw, GOLD_DARK, (131, 103, 48), (86, 64, 28))
    for gx in range(56, 72, 4):
        draw.line((gx, 0, gx, 15), fill=(160, 128, 66))
    add_noise(img, GOLD_DARK, rng, 4)

    draw_gradient_rect(draw, GOLD_ORNAMENT, (192, 158, 86), (126, 96, 43))
    draw.polygon([(48, 0), (56, 8), (48, 16), (40, 8)], outline=(92, 69, 30))
    draw.line((44, 8, 52, 8), fill=(222, 190, 116))
    add_noise(img, GOLD_ORNAMENT, rng, 4)

    draw_gradient_rect(draw, GOLD_LIGHT, (220, 190, 116), (160, 128, 65))
    draw.line((56, 3, 71, 3), fill=(240, 214, 150))
    add_noise(img, GOLD_LIGHT, rng, 3)

    # Leather grip.
    draw_gradient_rect(draw, LEATHER, (64, 46, 31), (38, 27, 18))
    for offset in range(-16, 32, 6):
        draw.line((72 + offset, 15, 72 + offset + 15, 0), fill=(83, 60, 39))
        draw.line((72 + offset + 1, 15, 72 + offset + 16, 0), fill=(45, 32, 21))
    add_noise(img, LEATHER, rng, 4)

    draw_gradient_rect(draw, LEATHER_DARK, (50, 35, 24), (30, 21, 14))
    for offset in range(-16, 32, 5):
        draw.line((72 + offset, 31, 72 + offset + 15, 16), fill=(69, 49, 31))
    add_noise(img, LEATHER_DARK, rng, 3)

    # Dark iron and light steel.
    draw_gradient_rect(draw, IRON_DARK, (58, 63, 56), (38, 42, 37))
    add_noise(img, IRON_DARK, rng, 4)
    draw_gradient_rect(draw, STEEL_LIGHT, (138, 144, 131), (92, 98, 88))
    add_noise(img, STEEL_LIGHT, rng, 3)
    for sx in range(104, 120, 2):
        draw.line((sx, 0, sx, 15), fill=(118, 124, 112))

    draw_gradient_rect(draw, DARK, (38, 42, 37), (21, 24, 21))
    add_noise(img, DARK, rng, 5)

    # Rivet tile: brass stud with a dark rim.
    draw.rectangle((120, 0, 127, 7), fill=(74, 56, 26))
    draw.ellipse((121, 1, 126, 6), fill=(198, 164, 92))
    draw.point((123, 2), fill=(242, 217, 151))
    draw.point((125, 5), fill=(122, 92, 41))

    return img.convert("RGBA")


def build_logo(texture):
    logo = Image.new("RGBA", (128, 128), (18, 20, 18, 255))
    draw = ImageDraw.Draw(logo)

    # radial vignette
    for i in range(60, 0, -1):
        alpha = int(90 * (1 - i / 60))
        draw.ellipse((64 - i * 1.5, 64 - i * 1.5, 64 + i * 1.5, 64 + i * 1.5),
                     outline=(40 + alpha, 38 + alpha, 32 + alpha, 255))

    # blade silhouette
    blade = [(28, 112), (46, 92), (78, 28), (96, 18), (86, 44), (54, 100)]
    draw.polygon(blade, fill=(94, 100, 90))
    draw.polygon([(34, 106), (50, 88), (80, 30), (91, 23), (80, 44), (52, 96)],
                 fill=(58, 63, 57))
    draw.line([(38, 101), (82, 27)], fill=(166, 172, 158), width=2)

    # handle / guard
    draw.line([(22, 118), (42, 100)], fill=(42, 30, 20), width=6)
    draw.line([(25, 116), (40, 101)], fill=(126, 98, 48), width=2)
    draw.polygon([(16, 122), (24, 110), (32, 112), (24, 124)], fill=(148, 116, 55))
    draw.polygon([(20, 116), (28, 108), (33, 111), (26, 119)], fill=(208, 176, 102))

    logo.paste(Image.new("RGBA", (1, 1)), (0, 0))
    return logo


def main():
    ASSETS.joinpath("geo").mkdir(parents=True, exist_ok=True)
    ASSETS.joinpath("animations").mkdir(parents=True, exist_ok=True)
    ASSETS.joinpath("textures/item").mkdir(parents=True, exist_ok=True)

    model = build_model()
    ASSETS.joinpath("geo/greatsword.geo.json").write_text(
        json.dumps(model, indent=2), encoding="utf-8")

    animations = build_animations()
    ASSETS.joinpath("animations/greatsword.animation.json").write_text(
        json.dumps(animations, indent=2), encoding="utf-8")

    texture = build_texture()
    texture.save(ASSETS / "textures/item/greatsword.png")

    logo = build_logo(texture)
    logo.save(RES / "logo.png")

    print("Generated:")
    print(" ", ASSETS / "geo/greatsword.geo.json")
    print(" ", ASSETS / "animations/greatsword.animation.json")
    print(" ", ASSETS / "textures/item/greatsword.png")
    print(" ", RES / "logo.png")


if __name__ == "__main__":
    main()
