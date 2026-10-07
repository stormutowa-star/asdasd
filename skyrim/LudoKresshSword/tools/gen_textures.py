"""Build the sword textures from the reference image.

diffuse  (DXT1, 2048x512): reference colours, edge-padded so the UV seams never
                          sample the white background.
normal   (DXT5, 2048x512): fine surface detail from the painting's luminance,
                          alpha = specular mask (blade bright, grip dull).
env mask (DXT1, 1024x256): cubemap reflection strength per material.
"""
import sys
import numpy as np
from PIL import Image, ImageFilter
from scipy import ndimage as ndi
from dds import write_dds

REF, MASK, REGIONS, OUTDIR = sys.argv[1:5]
TW, TH = 2048, 512

img = np.asarray(Image.open(REF).convert('RGB')).astype(float)
mask = np.asarray(Image.open(MASK)) > 0
reg = np.asarray(Image.open(REGIONS).convert('RGB')) > 0
green, grip, blade = reg[..., 0], reg[..., 1], reg[..., 2]


def pad(arr, m):
    """Fill pixels outside m with the nearest pixel inside m."""
    core = ndi.binary_erosion(m, iterations=1)
    _, (iy, ix) = ndi.distance_transform_edt(~core, return_indices=True)
    return arr[iy, ix]


def up(arr, size=(TW, TH)):
    a = np.clip(arr, 0, 255).astype(np.uint8)
    return Image.fromarray(a).resize(size, Image.LANCZOS)


# ---------------------------------------------------------------- diffuse
dif = img.copy()
# tame the baked white rim highlights on the blade a little (the engine adds
# its own specular and cubemap reflections on top)
lum = dif.mean(axis=2, keepdims=True)
hot = (blade[..., None]) & (lum > 200)
dif = np.where(hot, 200 + (dif - 200) * 0.5, dif)
dif = pad(dif, mask)
dif_img = up(dif).filter(ImageFilter.UnsharpMask(radius=2, percent=60, threshold=2))
dif_img.save(f'{OUTDIR}/_diffuse_preview.png')
write_dds(f'{OUTDIR}/LudoKresshSword.dds', dif_img.convert('RGBA'), 'DXT1')

# ---------------------------------------------------------------- normal + specular
big = np.asarray(up(pad(img, mask)).convert('L')).astype(float) / 255.0
detail = big - ndi.gaussian_filter(big, 6.0)          # high-pass: engraving / wrap detail
inside = np.asarray(up(ndi.binary_erosion(mask, iterations=2) * 255.0).convert('L')) / 255.0
detail *= ndi.gaussian_filter(inside, 2.0)            # flat outside the silhouette (padding)
h = ndi.gaussian_filter(detail, 0.8) * 6.0
dx = ndi.sobel(h, axis=1) / 8.0
dy = ndi.sobel(h, axis=0) / 8.0
n = np.stack([-dx, -dy, np.ones_like(h)], axis=2)    # DirectX-style (+V = down)
n /= np.linalg.norm(n, axis=2, keepdims=True)
spec_small = np.where(blade, 210.0, np.where(green, 150.0, np.where(grip, 45.0, 120.0)))
spec_small = pad(spec_small[..., None].repeat(3, 2), mask)[..., 0]
spec = np.asarray(up(spec_small).convert('L')).astype(float)
nrm = np.dstack([(n + 1.0) * 127.5, spec])
nrm_img = Image.fromarray(np.clip(nrm, 0, 255).round().astype(np.uint8), 'RGBA')
nrm_img.save(f'{OUTDIR}/_normal_preview.png')
write_dds(f'{OUTDIR}/LudoKresshSword_n.dds', nrm_img, 'DXT5', renormalize=True)

# ---------------------------------------------------------------- environment mask
env_small = np.where(blade, 190.0, np.where(green, 110.0, np.where(grip, 18.0, 90.0)))
env_small = pad(env_small[..., None].repeat(3, 2), mask)
env_img = up(env_small, (TW // 2, TH // 2))
write_dds(f'{OUTDIR}/LudoKresshSword_m.dds', env_img.convert('RGBA'), 'DXT1')
print('textures written')
