"""Paso 6: sonidos sintetizados (sin muestras con copyright) + voces en
español generadas con espeak-ng y procesadas para sonar como un caballero
con yelmo. Devuelve {(grupo, numero): bytes_wav}."""
import io
import os
import subprocess
import tempfile
import wave
import numpy as np

SR = 22050
rng = np.random.default_rng(11)


def t_(d):
    return np.arange(int(SR * d)) / SR


def env(n, a=0.005, d=0.2, curve=3.0):
    t = np.arange(n) / SR
    e = np.minimum(1, t / max(a, 1e-4)) * np.exp(-t / max(d, 1e-4) * curve / 3)
    return e


def lowpass(x, cut):
    a = np.exp(-2 * np.pi * cut / SR)
    y = np.zeros_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc = (1 - a) * v + a * acc
        y[i] = acc
    return y


def lp_fast(x, cut):
    # filtro paso-bajo por FFT (rapido)
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X *= 1 / (1 + (f / cut) ** 4)
    return np.fft.irfft(X, len(x))


def bp(x, lo, hi):
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X *= np.exp(-((np.log((f + 1) / np.sqrt(lo * hi))) / np.log(hi / lo) * 2) ** 2)
    return np.fft.irfft(X, len(x))


def norm(x, peak=0.9):
    m = np.max(np.abs(x)) + 1e-9
    return x / m * peak


def noise(d):
    return rng.normal(0, 1, int(SR * d))


def whoosh(d=0.25, lo=400, hi=3000, peak=0.7):
    n = int(SR * d)
    x = noise(d)
    t = np.arange(n) / n
    # barrido de frecuencia simulado con dos bandas mezcladas
    a = bp(x, lo, lo * 3) * (1 - t)
    b = bp(x, hi / 3, hi) * t
    e = np.sin(np.pi * t) ** 1.5
    return norm((a + b) * e, peak)


def impact(d=0.3, body=90, crunch=0.6, peak=0.9):
    n = int(SR * d)
    t = np.arange(n) / SR
    thump = np.sin(2 * np.pi * body * t * (1 - 0.5 * t / d)) * np.exp(-t * 18)
    cr = bp(noise(d), 800, 5000) * np.exp(-t * 30) * crunch
    return norm(thump + cr, peak)


def slash_hit(d=0.35, heavy=0.5):
    n = int(SR * d)
    t = np.arange(n) / SR
    metal = sum(np.sin(2 * np.pi * f * t) * np.exp(-t * k) for f, k in ((1850, 14), (2630, 18), (3900, 25)))
    x = impact(d, 70 + 40 * (1 - heavy), 0.8) + 0.25 * metal + whoosh(d, 1500, 6000, 0.4) * np.exp(-t * 10)
    return norm(x, 0.9)


def clang(d=0.5):
    t = t_(d)
    x = sum(np.sin(2 * np.pi * f * t + rng.uniform(0, 6)) * np.exp(-t * k)
            for f, k in ((520, 6), (1210, 8), (1730, 9), (2650, 12), (3420, 14)))
    x += bp(noise(d), 2000, 7000) * np.exp(-t * 60)
    return norm(x, 0.8)


def thunder(d=1.2):
    t = t_(d)
    crack = bp(noise(d), 1500, 8000) * np.exp(-t * 25)
    rumble = lp_fast(noise(d), 180) * np.exp(-t * 2.2) * 4
    zap = np.sign(np.sin(2 * np.pi * 60 * t)) * bp(noise(d), 300, 3000) * np.exp(-t * 12) * 0.4
    return norm(crack * 1.2 + rumble + zap, 0.95)


def zap(d=0.4):
    t = t_(d)
    buzz = np.sign(np.sin(2 * np.pi * (110 + 40 * np.sin(2 * np.pi * 9 * t)) * t))
    x = bp(buzz * noise(d) * 0.6 + buzz * 0.4, 400, 6000) * np.exp(-t * 6)
    return norm(x, 0.75)


def charge(d=0.8):
    t = t_(d)
    f = 120 + 900 * (t / d) ** 2
    ph = 2 * np.pi * np.cumsum(f) / SR
    x = np.sin(ph) * 0.5 + 0.3 * np.sign(np.sin(ph * 2.01)) + 0.4 * bp(noise(d), 1000, 6000)
    return norm(x * np.minimum(1, t * 6) * np.exp(-np.maximum(0, t - d * 0.8) * 20), 0.6)


def boom(d=1.0):
    t = t_(d)
    x = np.sin(2 * np.pi * 55 * t * (1 - 0.3 * t)) * np.exp(-t * 5) * 1.2
    x += lp_fast(noise(d), 300) * np.exp(-t * 4) * 3
    x += bp(noise(d), 1000, 6000) * np.exp(-t * 20) * 0.6
    return norm(np.tanh(x * 1.5), 0.95)


def roar(d=1.4, base=85, fire=False):
    t = t_(d)
    vib = 1 + 0.04 * np.sin(2 * np.pi * 6 * t) + 0.02 * rng.normal(0, 1, len(t)).cumsum() / 300
    f = base * vib * (1 + 0.25 * np.sin(np.pi * t / d))
    ph = 2 * np.pi * np.cumsum(f) / SR
    saw = sum(np.sin(k * ph) / k for k in range(1, 30))
    grit = noise(d) * 0.5
    src = saw + grit * np.abs(np.sin(ph / 2))
    # formantes tipo "aaah"
    x = bp(src, 500, 900) * 1.2 + bp(src, 1000, 1500) * 0.8 + bp(src, 2300, 3000) * 0.4 + lp_fast(src, 300) * 0.6
    e = np.minimum(1, t / 0.08) * np.minimum(1, (d - t) / 0.35)
    x = np.tanh(x * e * 2.5)
    if fire:
        x += lp_fast(noise(d), 900) * np.minimum(1, t / 0.3) * np.exp(-t * 1.2) * 0.8
    return norm(x, 0.9)


def chime(d=0.6):
    t = t_(d)
    x = sum(np.sin(2 * np.pi * f * t) * np.exp(-t * 5) for f in (880, 1320, 1760))
    return norm(x, 0.5)


def to_wav(x):
    x = np.clip(x, -1, 1)
    pcm = (x * 32000).astype('<i2')
    bio = io.BytesIO()
    with wave.open(bio, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())
    return bio.getvalue()


def voice(text, pitch=18, speed=150, shift=0.86, grit=0.25):
    """voz con espeak-ng, mas grave y con eco metalico de yelmo."""
    with tempfile.TemporaryDirectory() as td:
        p = os.path.join(td, 'v.wav')
        subprocess.run(['espeak-ng', '-v', 'es', '-p', str(pitch), '-s', str(speed), '-a', '180',
                        '-w', p, text], check=True, capture_output=True)
        with wave.open(p, 'rb') as w:
            sr = w.getframerate()
            x = np.frombuffer(w.readframes(w.getnframes()), '<i2').astype(float) / 32768
    # remuestrear a SR y bajar el tono
    n_out = int(len(x) * SR / sr / shift)
    xi = np.interp(np.linspace(0, len(x) - 1, n_out), np.arange(len(x)), x)
    # recorte de silencios
    idx = np.nonzero(np.abs(xi) > 0.02)[0]
    if len(idx):
        xi = xi[max(0, idx[0] - 200): idx[-1] + 800]
    # coloracion: saturacion + resonancia metalica (filtro peine)
    y = np.tanh(xi * (1.8 + grit * 4))
    out = y.copy()
    for dly, g in ((int(SR * 0.0071), 0.35), (int(SR * 0.0113), 0.25), (int(SR * 0.061), 0.18)):
        out[dly:] += y[:-dly] * g
    out = lp_fast(out, 5200)
    out = np.concatenate([out, np.zeros(int(SR * 0.15))])
    return norm(out, 0.9)


VOICES = {
    (10, 0): '¡Martillo de tormenta!',
    (10, 1): '¡Gran hendidura!',
    (10, 2): '¡Grito de guerra!',
    (10, 3): '¡Fuerza de los dioses!',
    (10, 4): '¡Juicio del caballero errante!',
    (10, 5): 'El caballero errante no sirve a nadie.',
    (10, 6): 'Ninguna ley me detiene.',
    (10, 7): '¡Agh!',
    (10, 8): '¡Nooo!',
    (10, 9): '¿Eso es todo?',
    (10, 10): '¡Ja!',
    (10, 11): '¡Hiá!',
    (10, 12): '¡Cae!',
    (10, 13): '¡Tormenta!',
    (10, 14): '¡Hendidura de tormenta!',
    (10, 15): '¡Uf!',
}


def build():
    S = {}
    S[(0, 0)] = to_wav(whoosh(0.18, 600, 4000, 0.6))
    S[(0, 1)] = to_wav(whoosh(0.26, 400, 3000, 0.7))
    S[(0, 2)] = to_wav(whoosh(0.38, 200, 2200, 0.85))
    S[(1, 0)] = to_wav(slash_hit(0.25, 0.2))
    S[(1, 1)] = to_wav(slash_hit(0.35, 0.5))
    S[(1, 2)] = to_wav(slash_hit(0.5, 1.0))
    S[(1, 3)] = to_wav(impact(0.35, 70, 0.4))
    S[(1, 5)] = to_wav(clang(0.45))
    S[(2, 0)] = to_wav(thunder(1.3))
    S[(2, 1)] = to_wav(zap(0.45))
    S[(2, 2)] = to_wav(norm(whoosh(0.4, 300, 4000, 0.6) + zap(0.4) * 0.6, 0.8))
    S[(2, 3)] = to_wav(charge(0.7))
    S[(3, 0)] = to_wav(boom(1.1))
    S[(3, 1)] = to_wav(norm(whoosh(0.6, 200, 3500, 0.8) + 0.3 * zap(0.6), 0.85))
    S[(4, 0)] = to_wav(roar(1.3, 95))
    S[(4, 1)] = to_wav(roar(1.8, 72, fire=True))
    S[(4, 2)] = to_wav(norm(lp_fast(noise(1.0), 220) * 3 + 0.3 * zap(1.0), 0.6))
    S[(4, 3)] = to_wav(chime(0.6))
    S[(5, 0)] = to_wav(norm(whoosh(0.15, 300, 1500, 0.4), 0.4))
    S[(5, 1)] = to_wav(impact(0.25, 60, 0.2, 0.7))
    S[(5, 2)] = to_wav(impact(0.15, 80, 0.3, 0.45))
    S[(6, 0)] = to_wav(norm(charge(0.5) + chime(0.5), 0.8))
    for k, txt in VOICES.items():
        try:
            S[k] = to_wav(voice(txt))
        except Exception as e:  # sin espeak: voz sustituida por un grunido
            print('voz no generada', k, e)
            S[k] = to_wav(roar(0.4, 110))
    return S


if __name__ == '__main__':
    S = build()
    os.makedirs('build/wav', exist_ok=True)
    for (g, n), w in S.items():
        open(f'build/wav/{g}_{n}.wav', 'wb').write(w)
    print(len(S), 'sonidos', sum(len(v) for v in S.values()) // 1024, 'KB')
