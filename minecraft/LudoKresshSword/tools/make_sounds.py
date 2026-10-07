"""Synthesise the sword sounds (original audio in the style of Skyrim's blade sounds).

swing: metallic whoosh of the blade; hit: blade impact (slice + thud + ring);
poison: bubbling hiss of the Sith poison. Mono 44.1 kHz Ogg Vorbis, as Minecraft needs.
"""
import sys
import numpy as np
import soundfile as sf
from scipy import signal

SR = 44100
OUT = sys.argv[1]


def t(d):
    return np.arange(int(SR * d)) / SR


def band(x, lo, hi, order=4):
    sos = signal.butter(order, [lo, hi], btype='band', fs=SR, output='sos')
    return signal.sosfilt(sos, x)


def lowpass(x, f, order=4):
    return signal.sosfilt(signal.butter(order, f, btype='low', fs=SR, output='sos'), x)


def highpass(x, f, order=4):
    return signal.sosfilt(signal.butter(order, f, btype='high', fs=SR, output='sos'), x)


def save(name, x):
    fade = np.linspace(1, 0, int(0.01 * SR))
    x = x.copy()
    x[-len(fade):] *= fade
    x = x / (np.max(np.abs(x)) + 1e-9) * 0.89
    sf.write(f'{OUT}/{name}.ogg', x.astype(np.float32), SR, format='OGG', subtype='VORBIS')


def swing(seed, dur=0.42, peak=0.15, lo_shift=1.0):
    rng = np.random.default_rng(seed)
    tt = t(dur)
    n = rng.standard_normal(len(tt))
    low = band(n, 250 * lo_shift, 900 * lo_shift)
    mid = band(n, 900 * lo_shift, 2600 * lo_shift)
    high = band(n, 2600, 7000)
    # the air "sweeps" through the bands as the blade passes
    sweep = np.clip(tt / (dur * 0.6), 0, 1)
    body = low * (1 - sweep) + mid * np.sin(np.pi * sweep) + high * sweep ** 2 * 0.6
    env = np.where(tt < peak, (tt / peak) ** 2, np.exp(-(tt - peak) / 0.07))
    ring = sum(a * np.sin(2 * np.pi * f * tt + rng.uniform(0, 6)) for f, a in
               [(3150 * lo_shift, 0.05), (4720, 0.035), (6380, 0.02)])
    return body * env + ring * env * 0.8


def hit(seed, metal=(1870, 2960, 4230, 5610)):
    rng = np.random.default_rng(seed)
    tt = t(0.55)
    n = rng.standard_normal(len(tt))
    slice_ = highpass(n, 2500) * np.exp(-tt / 0.035) * 0.9
    click = highpass(n, 6000) * np.exp(-tt / 0.004) * 1.2
    thud_f = 95 * np.exp(-tt / 0.15) + 55
    thud = np.sin(2 * np.pi * np.cumsum(thud_f) / SR) * np.exp(-tt / 0.09) * 0.9
    slap = lowpass(n, 700) * np.exp(-tt / 0.06) * 1.4
    ring = sum(np.sin(2 * np.pi * f * (1 + rng.uniform(-0.02, 0.02)) * tt) * np.exp(-tt / d) * a
               for f, d, a in zip(metal, (0.32, 0.26, 0.2, 0.14), (0.28, 0.22, 0.16, 0.1)))
    return slice_ + click + thud + slap + ring


def poison(seed, dur=0.8):
    rng = np.random.default_rng(seed)
    tt = t(dur)
    n = rng.standard_normal(len(tt))
    hiss_env = np.minimum(tt / 0.03, 1) * np.exp(-tt / 0.32)
    hiss = band(n, 3500, 9000) * hiss_env * 0.35
    bubbles = np.zeros_like(tt)
    for _ in range(14):
        start = rng.uniform(0.0, dur * 0.7)
        length = rng.uniform(0.03, 0.07)
        f0 = rng.uniform(260, 520)
        m = (tt >= start) & (tt < start + length)
        lt = tt[m] - start
        f = f0 * (1 + 1.8 * lt / length)
        bubbles[m] += np.sin(2 * np.pi * np.cumsum(f) / SR) * np.sin(np.pi * lt / length) * rng.uniform(0.3, 0.6)
    shimmer = sum(np.sin(2 * np.pi * f * tt) * a for f, a in [(1320, 0.06), (1980, 0.04), (2640, 0.03)])
    shimmer *= (0.6 + 0.4 * np.sin(2 * np.pi * 11 * tt)) * np.exp(-tt / 0.35)
    return hiss + bubbles * np.exp(-tt / 0.45) + shimmer


save('swing1', swing(1))
save('swing2', swing(2, dur=0.38, peak=0.13, lo_shift=1.12))
save('swing3', swing(3, dur=0.46, peak=0.17, lo_shift=0.9))
save('hit1', hit(11))
save('hit2', hit(12, metal=(2010, 3120, 4480, 5900)))
save('hit3', hit(13, metal=(1720, 2790, 3980, 5320)))
save('poison1', poison(21))
save('poison2', poison(22, dur=0.7))
print('ok')
