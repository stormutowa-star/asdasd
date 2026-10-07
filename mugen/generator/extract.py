"""Paso 1: separa al personaje del fondo de la imagen de referencia,
reconstruye la rejilla de pixel-art nativa (200x200), la espeja para que
mire a la derecha (convencion MUGEN) y la cuantiza a una paleta."""
import os
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

HERE = os.path.dirname(os.path.abspath(__file__))
BUILD = os.path.join(HERE, 'build')
N = 200          # la ilustracion es pixel-art de 200x200 escalado a 1024
K = 72           # colores del personaje


def main():
    os.makedirs(BUILD, exist_ok=True)
    a = np.asarray(Image.open(os.path.join(HERE, 'reference.jpg')).convert('RGB')).astype(int)
    mn, mx = a.min(2), a.max(2)
    bglike = (mn > 115) & ((mx - mn) < 50)
    lab, _ = ndi.label(bglike)
    border = set(np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1], lab[700, :]]))) - {0}
    bg = np.isin(lab, list(border))
    np.save(os.path.join(BUILD, 'bg_mask.npy'), bg[:, ::-1])   # espejado, 1024x1024

    P = 1024 / N
    nat = np.zeros((N, N, 3))
    alpha = np.zeros((N, N))
    af = a.astype(float)
    for j in range(N):
        for i in range(N):
            cx = (i + 0.5) * P - 0.5
            cy = (j + 0.5) * P - 0.5
            x0, y0 = int(round(cx - 1)), int(round(cy - 1))
            nat[j, i] = np.median(af[y0:y0 + 3, x0:x0 + 3].reshape(-1, 3), 0)
            alpha[j, i] = 1 - bg[y0:y0 + 3, x0:x0 + 3].mean()
    nat = nat[:, ::-1]
    mask = (alpha > 0.5)[:, ::-1]
    mask[:62, :50] = False      # retrato/HUD
    mask[176:, :] = False       # placa con el nombre
    lab, n = ndi.label(mask)
    sizes = ndi.sum(mask, lab, range(1, n + 1))
    mask &= np.isin(lab, [i + 1 for i, s in enumerate(sizes) if s > 200])

    px = nat[mask]
    rng = np.random.default_rng(0)
    C = px[rng.choice(len(px), K, replace=False)]
    for _ in range(40):
        lbl = ((px[:, None, :] - C[None]) ** 2).sum(2).argmin(1)
        for k in range(K):
            if (lbl == k).any():
                C[k] = px[lbl == k].mean(0)
    C = np.clip(np.round(C), 0, 255)
    C = C[np.argsort(C @ [0.3, 0.59, 0.11])]
    idx = ((nat[:, :, None, :] - C[None, None]) ** 2).sum(3).argmin(2) + 1
    idx[~mask] = 0
    np.save(os.path.join(BUILD, 'pal_char.npy'), C)
    np.save(os.path.join(BUILD, 'nat_idx.npy'), idx)
    print('extract ok', mask.sum(), 'px')


if __name__ == '__main__':
    main()
