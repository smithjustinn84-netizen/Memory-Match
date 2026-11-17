#!/usr/bin/env python3
"""Synthesise Memory Match's "Card Room" sound effects and re-level the music loop.

Everything is generated from code with fixed seeds, so the output is reproducible and
licence-free. Requires numpy, plus afconvert (macOS) or ffmpeg for AAC encoding; the
--music step also needs ffmpeg.

    python3 tools/audio/generate_sfx.py --out /tmp/sfx      # audition without touching the app
    python3 tools/audio/generate_sfx.py                     # write into composeResources/files
    python3 tools/audio/generate_sfx.py --only flip,match
    python3 tools/audio/generate_sfx.py --music             # also trim/level music.m4a
"""

from __future__ import annotations

import argparse
import math
import re
import shutil
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

import numpy as np

SR = 44100
ROOT = Path(__file__).resolve().parents[2]
FILES = ROOT / "shared/core/src/commonMain/composeResources/files"
CEILING_DB = -1.0
MUSIC_TARGET_LUFS = -22.0
LOOP_CROSSFADE_S = 0.03


# --------------------------------------------------------------------------- primitives


def silence(dur: float) -> np.ndarray:
    return np.zeros(int(SR * dur))


def t_axis(dur: float) -> np.ndarray:
    return np.arange(int(SR * dur)) / SR


def env(t: np.ndarray, attack: float, tau: float) -> np.ndarray:
    """Linear attack then exponential decay with time constant tau."""
    attack = max(attack, 1.0 / SR)
    return np.where(t < attack, t / attack, np.exp(-(t - attack) / tau))


def note(name: str) -> float:
    """'C5' -> Hz (A4 = 440)."""
    m = re.fullmatch(r"([A-G])([#b]?)(-?\d)", name)
    if not m:
        raise ValueError(name)
    semis = {"C": -9, "D": -7, "E": -5, "F": -4, "G": -2, "A": 0, "B": 2}[m[1]]
    semis += {"#": 1, "b": -1, "": 0}[m[2]]
    semis += (int(m[3]) - 4) * 12
    return 440.0 * 2 ** (semis / 12)


def biquad(x: np.ndarray, kind: str, freq: float, q: float = 0.707) -> np.ndarray:
    """RBJ cookbook biquad (causal, so transients get no pre-echo)."""
    w0 = 2 * math.pi * freq / SR
    alpha = math.sin(w0) / (2 * q)
    cw = math.cos(w0)
    if kind == "lp":
        b = [(1 - cw) / 2, 1 - cw, (1 - cw) / 2]
    elif kind == "hp":
        b = [(1 + cw) / 2, -(1 + cw), (1 + cw) / 2]
    elif kind == "bp":
        b = [alpha, 0.0, -alpha]
    else:
        raise ValueError(kind)
    a0, a1, a2 = 1 + alpha, -2 * cw, 1 - alpha
    b0, b1, b2 = (v / a0 for v in b)
    a1 /= a0
    a2 /= a0
    out = [0.0] * len(x)
    x1 = x2 = y1 = y2 = 0.0
    for i, xi in enumerate(x.tolist()):
        yi = b0 * xi + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1, y2, y1 = x1, xi, y1, yi
        out[i] = yi
    return np.asarray(out)


def place(dst: np.ndarray, src: np.ndarray, at: float, gain: float = 1.0) -> np.ndarray:
    start = int(SR * at)
    end = start + len(src)
    if end > len(dst):
        dst = np.concatenate([dst, np.zeros(end - len(dst))])
    dst[start:end] += src * gain
    return dst


def room(x: np.ndarray, wet: float, rt60: float = 0.55, seed: int = 7) -> np.ndarray:
    """Small felt-lined room: convolve with a decaying, darkened noise tail."""
    if wet <= 0:
        return x
    rng = np.random.default_rng(seed)
    t = t_axis(rt60 * 1.2)
    ir = rng.standard_normal(len(t)) * np.exp(-6.9 * t / rt60)
    ir = biquad(ir, "lp", 3200)
    ir[: int(0.008 * SR)] = 0  # pre-delay
    ir /= np.sqrt((ir**2).sum())
    n = len(x) + len(ir) - 1
    nfft = 1 << (n - 1).bit_length()
    tail = np.fft.irfft(np.fft.rfft(x, nfft) * np.fft.rfft(ir, nfft), nfft)[:n]
    dry = np.concatenate([x, np.zeros(len(ir) - 1)])
    return dry + wet * tail


# --------------------------------------------------------------------------- instruments


def card_snap(rng: np.random.Generator, bright: float = 1.0, dur: float = 0.07) -> np.ndarray:
    """Card stock hitting felt: a bright papery tick over a soft low thump."""
    t = t_axis(dur)
    tick = rng.standard_normal(len(t)) * env(t, 0.0004, 0.006)
    tick = biquad(biquad(tick, "hp", 1400 * bright), "lp", 6500 * bright)
    body = biquad(rng.standard_normal(len(t)) * env(t, 0.0005, 0.010), "bp", 2300 * bright, 2.5)
    sweep = 150 - 60 * np.clip(t / 0.03, 0, 1)
    thump = np.sin(2 * np.pi * np.cumsum(sweep) / SR) * env(t, 0.001, 0.014)
    return tick * 0.9 + body * 0.6 + thump * 0.45


def swish(rng: np.random.Generator, dur: float = 0.035) -> np.ndarray:
    """A card sliding off the deck."""
    t = t_axis(dur)
    s = rng.standard_normal(len(t)) * env(t, dur * 0.6, dur * 0.25)
    return biquad(biquad(s, "hp", 900), "lp", 4200)


def chip_clack(rng: np.random.Generator, pitch: float = 1.0) -> np.ndarray:
    """Clay poker chips knocking together: a few short resonant modes."""
    t = t_axis(0.05)
    out = np.zeros(len(t))
    for f, tau, amp in ((3100, 0.006, 1.0), (4650, 0.004, 0.7), (6900, 0.003, 0.4), (1850, 0.008, 0.35)):
        f *= pitch * rng.uniform(0.97, 1.03)
        out += amp * np.sin(2 * np.pi * f * t + rng.uniform(0, 2 * np.pi)) * env(t, 0.0003, tau)
    click = biquad(rng.standard_normal(len(t)) * env(t, 0.0002, 0.0015), "hp", 2500)
    return out * 0.5 + click * 0.4


def bell(freq: float, tau: float, dur: float | None = None, bright: float = 1.0) -> np.ndarray:
    """Small struck brass bell: inharmonic partials, higher ones dying faster."""
    dur = dur or tau * 5
    t = t_axis(dur)
    out = np.zeros(len(t))
    for ratio, amp in ((1.0, 1.0), (2.0, 0.42 * bright), (2.76, 0.30 * bright), (4.07, 0.16 * bright), (5.43, 0.09 * bright)):
        f = freq * ratio
        if f > SR * 0.45:
            continue
        out += amp * np.sin(2 * np.pi * f * t) * env(t, 0.0015, tau / ratio**0.7)
    return out


def mallet(freq: float, tau: float, dur: float | None = None) -> np.ndarray:
    """Warm, mostly harmonic chime (glockenspiel-ish but softer)."""
    dur = dur or tau * 5
    t = t_axis(dur)
    out = np.zeros(len(t))
    for ratio, amp in ((1.0, 1.0), (2.0, 0.25), (3.0, 0.08), (4.2, 0.10)):
        out += amp * np.sin(2 * np.pi * freq * ratio * t) * env(t, 0.002, tau / ratio**0.8)
    return out


def horn(freq: float, dur: float, attack: float = 0.03, release: float = 0.12, bright: float = 1.0) -> np.ndarray:
    """Mellow brass: additive harmonics that bloom after the attack, light vibrato."""
    total = dur + release * 4
    t = t_axis(total)
    vib = 1 + 0.003 * np.sin(2 * np.pi * 5.5 * t) * np.clip((t - 0.15) / 0.2, 0, 1)
    phase = 2 * np.pi * freq * np.cumsum(vib) / SR
    out = np.zeros(len(t))
    for n in range(1, 11):
        if freq * n > 9000:
            break
        bloom = np.clip(t / (attack * (1 + 0.25 * n)), 0, 1)
        out += (n ** -(1.7 / bright)) * bloom * np.sin(n * phase)
    shape = np.where(t < dur, 1.0, np.exp(-(t - dur) / release))
    shape *= np.clip(t / attack, 0, 1)
    detuned = np.sin(phase * 1.0023) * 0.15 * shape
    return biquad(out * shape + detuned, "lp", 4500 * bright)


def felt_thud(rng: np.random.Generator, freq: float) -> np.ndarray:
    t = t_axis(0.16)
    sweep = freq * (1 - 0.25 * np.clip(t / 0.06, 0, 1))
    tone = np.sin(2 * np.pi * np.cumsum(sweep) / SR)
    tone += 0.3 * np.sin(2 * 2 * np.pi * np.cumsum(sweep) / SR)
    tone *= env(t, 0.003, 0.035)
    puff = biquad(rng.standard_normal(len(t)) * env(t, 0.002, 0.012), "lp", 900)
    return biquad(tone + puff * 0.5, "lp", 1400)


# --------------------------------------------------------------------------- the sounds


def sfx_flip(rng):
    x = silence(0.1)
    x = place(x, swish(rng, 0.03), 0.0, 0.3)
    x = place(x, card_snap(rng), 0.02)
    return x, 0.04


def sfx_deal(rng):
    x = silence(0.5)
    at = 0.0
    for i in range(7):
        x = place(x, swish(rng, 0.025), at, 0.2)
        x = place(x, card_snap(rng, bright=rng.uniform(0.9, 1.1)), at + 0.015, rng.uniform(0.6, 0.85) + 0.05 * (i == 6))
        at += rng.uniform(0.048, 0.062)
    return x, 0.08


def sfx_click(rng):
    t = t_axis(0.045)
    x = np.sin(2 * np.pi * 1750 * t) * env(t, 0.0003, 0.004)
    x += 0.5 * np.sin(2 * np.pi * 3300 * t) * env(t, 0.0003, 0.002)
    x += 0.4 * np.sin(2 * np.pi * 620 * t) * env(t, 0.0005, 0.006)
    x += 0.3 * biquad(rng.standard_normal(len(t)) * env(t, 0.0002, 0.001), "hp", 2000)
    return x, 0.0


def sfx_plink(rng):
    x = bell(note("G6"), 0.12, 0.45)
    x = place(x, chip_clack(rng, 1.15), 0.0, 0.15)
    return x, 0.18


def sfx_match(rng):
    x = silence(0.65)
    x = place(x, chip_clack(rng), 0.0, 0.5)
    x = place(x, chip_clack(rng, 1.06), 0.012, 0.3)
    x = place(x, mallet(note("G5"), 0.16, 0.5), 0.0, 0.8)
    x = place(x, mallet(note("C6"), 0.20, 0.55), 0.075)
    x = place(x, bell(note("C7"), 0.08, 0.3, 0.6), 0.075, 0.12)
    return x, 0.18


def sfx_mismatch(rng):
    x = silence(0.32)
    x = place(x, felt_thud(rng, 233), 0.0)
    x = place(x, felt_thud(rng, 185), 0.11, 0.8)
    return x, 0.08


def sfx_nuts(rng):
    x = silence(1.7)
    at = 0.0
    gap = 0.075
    for i in range(14):
        x = place(x, chip_clack(rng, rng.uniform(0.92, 1.08)), at, 0.55 * (0.94**i))
        at += gap * rng.uniform(0.8, 1.2)
        gap *= 0.9
    for i, n in enumerate(("C5", "E5", "G5", "C6", "E6")):
        last = i == 4
        x = place(x, mallet(note(n), 0.5 if last else 0.18, 1.2 if last else 0.4), 0.05 + 0.09 * i, 0.75)
    x = place(x, horn(note("C5"), 0.55, 0.08, 0.25), 0.41, 0.25)
    x = place(x, horn(note("G5"), 0.55, 0.08, 0.25), 0.41, 0.18)
    x = place(x, bell(note("E7"), 0.25, 0.9, 0.5), 0.45, 0.12)
    return x, 0.22


def sfx_win(rng):
    x = silence(2.1)
    for i, n in enumerate(("G4", "C5", "E5")):
        x = place(x, horn(note(n), 0.09, 0.015, 0.05, 1.15), 0.12 * i, 0.8)
    for n, g in (("C5", 0.55), ("E5", 0.5), ("G5", 0.5), ("C6", 0.6)):
        x = place(x, horn(note(n), 0.75, 0.03, 0.22, 1.1), 0.36, g)
    for i, n in enumerate(("C7", "E7", "G7")):
        x = place(x, bell(note(n), 0.35, 1.3, 0.6), 0.36 + 0.07 * i, 0.12)
    return x, 0.25


def sfx_lose(rng):
    x = silence(1.7)
    for i, (n, d) in enumerate((("G4", 0.28), ("Eb4", 0.28), ("C4", 0.55))):
        x = place(x, horn(note(n), d, 0.05, 0.18 if i < 2 else 0.25, 0.7), 0.32 * i, 0.8)
    x = place(x, mallet(note("C5"), 0.4, 1.0), 0.64, 0.15)
    return biquad(x, "lp", 2600), 0.25


def sfx_highscore(rng):
    x = silence(1.5)
    run = ("C6", "D6", "E6", "G6", "A6", "C7", "E7")
    for i, n in enumerate(run):
        x = place(x, bell(note(n), 0.3, 0.9, 0.7), 0.06 * i, 0.55)
    x = place(x, bell(note("C7"), 0.6, 1.1, 0.5), 0.06 * len(run), 0.6)
    for n in ("C5", "E5", "G5"):
        x = place(x, horn(note(n), 0.6, 0.12, 0.3, 0.8), 0.05, 0.12)
    return x, 0.28


# name -> (builder, short-term RMS target dBFS, seed)
SOUNDS = {
    "flip": (sfx_flip, -20.0, 11),
    "deal": (sfx_deal, -20.0, 12),
    "click": (sfx_click, -21.0, 13),
    "plink": (sfx_plink, -19.0, 14),
    "match": (sfx_match, -16.0, 15),
    "mismatch": (sfx_mismatch, -17.0, 16),
    "nuts": (sfx_nuts, -14.0, 17),
    "win": (sfx_win, -14.0, 18),
    "lose": (sfx_lose, -15.0, 19),
    "highscore": (sfx_highscore, -14.0, 20),
}


# --------------------------------------------------------------------------- mastering


def short_term_rms_db(x: np.ndarray, window: float = 0.05) -> float:
    """Loudest 50 ms RMS: tracks how loud a short effect feels better than whole-file RMS."""
    w = min(len(x), int(SR * window))
    power = np.convolve(x**2, np.ones(w) / w, mode="valid")
    return 10 * math.log10(max(power.max(), 1e-20))


def peak_db(x: np.ndarray) -> float:
    return 20 * math.log10(max(np.abs(x).max(), 1e-12))


def limit(x: np.ndarray, ceiling_db: float = CEILING_DB, lookahead: int = 66, release: float = 0.05) -> np.ndarray:
    """Lookahead peak limiter: gain never exceeds what the next `lookahead` samples allow."""
    ceil = 10 ** (ceiling_db / 20)
    need = np.minimum(1.0, ceil / np.maximum(np.abs(x), 1e-12))
    padded = np.concatenate([need, np.ones(lookahead)])
    windowed = np.lib.stride_tricks.sliding_window_view(padded, lookahead + 1).min(axis=1)
    coeff = math.exp(-1.0 / (release * SR))
    smooth = windowed.copy()
    level = 1.0
    for i, g in enumerate(windowed.tolist()):
        level = g if g < level else g + coeff * (level - g)
        smooth[i] = level
    kernel = np.ones(lookahead) / lookahead
    smooth = np.minimum(smooth, np.convolve(np.concatenate([np.ones(lookahead - 1), smooth]), kernel, "valid"))
    return np.clip(x * smooth, -ceil, ceil)


def finish(x: np.ndarray, target_db: float) -> np.ndarray:
    # Trim once the 10 ms envelope falls 50 dB under its peak, so reverb tails stay short.
    w = int(0.01 * SR)
    level = np.sqrt(np.convolve(x**2, np.ones(w) / w, mode="same"))
    above = np.nonzero(level > level.max() * 10 ** (-50 / 20))[0]
    x = x[: above[-1] + 1].copy()
    fade_in, fade_out = int(0.001 * SR), int(0.005 * SR)
    x[:fade_in] *= np.linspace(0, 1, fade_in)
    x[-fade_out:] *= np.linspace(1, 0, fade_out)
    x = x * 10 ** ((target_db - short_term_rms_db(x)) / 20)
    return limit(x)


# --------------------------------------------------------------------------- io


def write_wav(path: Path, x: np.ndarray) -> None:
    data = np.atleast_2d(x.T).T if x.ndim == 1 else x
    pcm = (np.clip(data, -1, 1) * 32767).astype("<i2")
    with wave.open(str(path), "wb") as w:
        w.setnchannels(pcm.shape[1] if pcm.ndim == 2 else 1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())


def read_wav(path: Path) -> np.ndarray:
    with wave.open(str(path)) as w:
        ch = w.getnchannels()
        data = np.frombuffer(w.readframes(w.getnframes()), dtype="<i2")
    return data.reshape(-1, ch).astype(np.float64) / 32768


def encode_m4a(wav: Path, out: Path, bitrate: int) -> None:
    if shutil.which("afconvert"):
        cmd = ["afconvert", "-f", "m4af", "-d", "aac", "-b", str(bitrate), str(wav), str(out)]
    elif shutil.which("ffmpeg"):
        cmd = ["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav), "-c:a", "aac", "-b:a", str(bitrate), str(out)]
    else:
        sys.exit("Need afconvert or ffmpeg to encode AAC")
    subprocess.run(cmd, check=True)


def integrated_lufs(wav: Path) -> float:
    res = subprocess.run(
        ["ffmpeg", "-hide_banner", "-nostats", "-i", str(wav), "-af", "ebur128", "-f", "null", "-"],
        capture_output=True, text=True, check=True,
    )
    matches = re.findall(r"I:\s+(-?[\d.]+) LUFS", res.stderr)
    return float(matches[-1])


# --------------------------------------------------------------------------- music


def process_music(out_dir: Path, work: Path) -> None:
    if not shutil.which("ffmpeg"):
        sys.exit("--music needs ffmpeg")
    src = FILES / "music.m4a"
    raw = work / "music_raw.wav"
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(src), str(raw)], check=True)
    x = read_wav(raw)
    loud = np.nonzero(np.abs(x).max(axis=1) > 1e-4)[0]
    head_gap, tail_gap = loud[0], len(x) - 1 - loud[-1]
    x = x[loud[0] : loud[-1] + 1]
    print(f"music: trimmed {head_gap / SR * 1000:.0f} ms of leading and {tail_gap / SR * 1000:.0f} ms of trailing silence")
    if head_gap or tail_gap:
        n = int(LOOP_CROSSFADE_S * SR)
        ramp = np.linspace(0, np.pi / 2, n)[:, None]
        head = x[:n] * np.sin(ramp) + x[-n:] * np.cos(ramp)
        x = np.concatenate([head, x[n:-n]])
        print(f"music: crossfaded the loop seam over {LOOP_CROSSFADE_S * 1000:.0f} ms")
    level = work / "music_level.wav"
    write_wav(level, x)
    gain_db = MUSIC_TARGET_LUFS - integrated_lufs(level)
    x = x * 10 ** (gain_db / 20)
    x = np.stack([limit(x[:, c]) for c in range(x.shape[1])], axis=1)
    final = work / "music.wav"
    write_wav(final, x)
    print(f"music: gain {gain_db:+.1f} dB -> {integrated_lufs(final):.1f} LUFS, peak {peak_db(x):.1f} dBFS, {len(x) / SR:.2f} s")
    encode_m4a(final, out_dir / "music.m4a", 192000)
    shutil.copy(final, out_dir / "music.wav") if out_dir != FILES else None


# --------------------------------------------------------------------------- main


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--out", type=Path, default=FILES, help="output folder (default: the app's composeResources/files)")
    parser.add_argument("--only", help="comma-separated subset, e.g. flip,match")
    parser.add_argument("--music", action="store_true", help="also trim the loop seam and re-level music.m4a")
    args = parser.parse_args()

    names = args.only.split(",") if args.only else list(SOUNDS)
    unknown = set(names) - set(SOUNDS)
    if unknown:
        sys.exit(f"Unknown sounds: {', '.join(sorted(unknown))}")
    args.out.mkdir(parents=True, exist_ok=True)
    keep_wavs = args.out.resolve() != FILES.resolve()

    rendered = {}
    with tempfile.TemporaryDirectory() as tmp:
        work = Path(tmp)
        print(f"{'sound':<10} {'length':>7} {'rms':>7} {'target':>7} {'peak':>6}")
        for name in names:
            build, target, seed = SOUNDS[name]
            dry, wet = build(np.random.default_rng(seed))
            x = finish(room(dry, wet, rt60=0.3 if wet < 0.1 else 0.55, seed=seed), target)
            rendered[name] = x
            wav = (args.out if keep_wavs else work) / f"{name}.wav"
            write_wav(wav, x)
            encode_m4a(wav, args.out / f"{name}.m4a", 96000)
            print(f"{name:<10} {len(x) / SR:>6.2f}s {short_term_rms_db(x):>7.1f} {target:>7.1f} {peak_db(x):>6.1f}")
        if args.music:
            process_music(args.out, work)

    if keep_wavs:
        gap = silence(0.7)
        parts = []
        for name in names:
            parts += [rendered[name], gap]
        if "match" in rendered:
            # Preview the combo pitch ladder the game applies (major pentatonic steps).
            for semis in (0, 2, 4, 7):
                ratio = 2 ** (semis / 12)
                src = rendered["match"]
                idx = np.arange(0, len(src) - 1, ratio)
                parts += [np.interp(idx, np.arange(len(src)), src), silence(0.25)]
        write_wav(args.out / "audition.wav", np.concatenate(parts))
        print(f"audition: {args.out / 'audition.wav'} (order: {', '.join(names)}, then the match combo ladder)")


if __name__ == "__main__":
    main()
