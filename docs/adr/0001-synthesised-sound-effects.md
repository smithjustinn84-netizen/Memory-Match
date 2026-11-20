# ADR-0001: Generate sound effects from code

**Status:** Accepted (implemented in `b74d2415`, 2026-11-16)
**Date:** 2026-11-19
**Deciders:** Justin Smith (maintainer)

## Context

Memory Match plays ten short effects (flip, deal, click, plink, match, mismatch, nuts, win, lose,
highscore) and one looping music track. They ship as `.m4a` files in
`shared/core/src/commonMain/composeResources/files/` and are played by a separate `AudioService`
on each platform: SoundPool on Android, AVAudioPlayer pools on iOS, JavaFX on desktop.

Problems with the files before this change:

- **Provenance.** Where the earlier files came from, and under what licence, was never recorded in
  the repo. That gets in the way of shipping to the stores.
- **Uneven levels.** The effects were never mixed against each other, so frequent sounds like
  flips could be as loud as the win stinger. The music loop had a 150 ms gap at its seam.
- **Fit.** The sounds did not match the "Card Room" design: felt, brass, card stock and chips.
- **Size.** The ten effects took about 555 KB and the music about 995 KB.

Two requests also called for runtime control: the match sound should climb in pitch with the
combo, and music should fade in and out instead of cutting.

## Decision

Generate every effect **offline** with `tools/audio/generate_sfx.py`, a numpy script with a fixed
seed per sound, and **commit the encoded `.m4a` output** as ordinary resources. The app does no
synthesis at runtime.

The script also masters each sound:

- It normalises each effect by short-term RMS (its loudest 50 ms window) to a target for its group:

  | group | sounds | target |
  |---|---|---|
  | frequent | flip, deal, click, plink | -21 to -19 dB |
  | feedback | match, mismatch | -17 to -16 dB |
  | stingers | nuts, win, lose, highscore | -15 to -14 dB |

- A look-ahead limiter holds peaks at -1 dBFS.
- With `--music`, it trims the 150 ms gap from the music loop seam, applies a 30 ms crossfade, and
  levels the track to -22 LUFS integrated.

These runtime changes shipped in the same commit:

- **`AudioService.playEffect(effect, rate)`.** `rate` scales speed and pitch together. Each
  platform clamps it to 0.5–2.
- **`AudioPitch`.** On a combo, the match sound steps up 0, 2, 4 and 7 semitones, then holds at
  about 1.5×. Flips and deals get about ±3% random jitter.
- **`MusicFader`.** One tested fader in common code: 600 ms fade in, 400 ms fade out. It handles a
  start that arrives during a fade-out. Each platform supplies only a small `MusicTrack`.
- **Platform backends.** iOS uses the ambient audio session, which respects the silent switch and
  mixes with other audio. Desktop uses polyphonic `AudioClip` and names cached files by content
  hash, so an update never plays a stale copy.

## Options considered

### Option A: Offline synthesis, output committed (chosen)

| Dimension | Assessment |
|---|---|
| Complexity | Medium. A ~480-line Python script, and nothing new in the app. |
| Cost | None. No licences or attribution. |
| Runtime cost | Same as before: decoding a small AAC file. |
| Team familiarity | DSP is niche, but the script is short, commented and built from simple primitives. |

**Pros:**
- Reproducible and licence-free.
- Levels are set by rule, not by ear.
- Changing a sound is a reviewable code change.
- The effects are about a quarter of their old total size (about 555 KB down to about 135 KB).

**Cons:**
- Needs numpy, plus `afconvert` (macOS) or `ffmpeg`, to regenerate.
- The committed binaries can drift from the script.
- Synthesised sound has a lower ceiling than recorded foley.

### Option B: CC0 or commercial sample packs

| Dimension | Assessment |
|---|---|
| Complexity | Low to choose; medium to edit and level by hand. |
| Cost | Free (CC0) to paid. Each file's licence has to be tracked. |
| Runtime cost | Same as A. |
| Team familiarity | High. |

**Pros:**
- Recorded sounds can be more realistic.
- No DSP code to maintain.

**Cons:**
- Attribution and licence records for every file.
- Levels and character vary from pack to pack, and fixing them is manual work in an editor.
- Changing a sound means finding a new sample.

### Option C: Synthesis on the device at runtime

| Dimension | Assessment |
|---|---|
| Complexity | High. Three platform audio paths would need to render buffers. |
| Cost | None in licences. |
| Runtime cost | CPU on the audio thread, plus a risk of latency on low-end Android. |
| Team familiarity | Low. |

**Pros:**
- No audio assets at all.
- Unlimited variation per play.

**Cons:**
- A large amount of platform-specific DSP and buffer code.
- Harder to test.
- Audio-thread performance risk.
- The variation we wanted (pitch by combo, jitter) is already available from `rate` on recorded
  files.

### Option D: Keep the existing files and only re-level them

| Dimension | Assessment |
|---|---|
| Complexity | Low. |
| Cost | None. |
| Runtime cost | Same as A. |
| Team familiarity | High. |

**Pros:**
- The least work.

**Cons:**
- Leaves the provenance question open.
- Does not fit the Card Room design.
- Re-levelling would be a one-off manual job with no record of how it was done.

## Trade-off analysis

A and D are the only options that keep the runtime as simple as it was, and D does not solve
provenance or fit. Choosing A over B trades recorded realism for licence-free, reproducible output
and levels set by rule. For short UI cues in a card game, that is a good trade.

C was rejected because the runtime variation we needed comes cheaply from playback rate, which all
three platforms support. Synthesising on the device would add risk without adding anything a
player would hear.

## Consequences

**Easier**
- Tweak, audition (`--out /tmp/sfx` writes WAVs and an `audition.wav` reel) and regenerate any
  sound with one command.
- Add a new effect: write a builder, give it a target and a seed, and add an enum entry.
- Keep the mix consistent. The script prints each sound's RMS against its target, and its peak.
- Fades and pitch behave the same on every platform, and both are covered by `MusicFaderTest` and
  `AudioPitchTest`.

**Harder**
- Regenerating requires numpy plus `afconvert` or `ffmpeg`. `--music` requires `ffmpeg`.
- Nothing checks that the committed `.m4a` files match what the script would produce. A
  hand-edited file or an uncommitted script change can drift unnoticed.
- `afconvert` and `ffmpeg` encode differently, so regenerating on another machine changes every
  binary even when the sounds did not change.

**To revisit**
- ~~On Android, the `MediaPlayer` fallback ignored `rate`.~~ Fixed: it now applies `rate`
  through `PlaybackParams`.
- ~~On iOS, an effect requested before its pool had loaded was skipped silently.~~ Fixed: it
  waits up to 250 ms for that effect's preload, then plays it or logs why not.
- The loudness targets were set offline. Check them on real phone speakers and headphones.

## Action items

1. [x] Generate and commit the effects and the re-levelled music (`b74d2415`).
2. [x] Add `AudioPitch` and `MusicFader` with tests.
3. [x] Pass `rate` through the Android `MediaPlayer` fallback (`PlaybackParams`; minSdk is 26).
4. [x] iOS waits up to 250 ms for an effect that has not loaded yet, then logs if it drops it.
5. [ ] Listening pass on Android and iOS hardware, then adjust the targets in `SOUNDS`.
6. [ ] Optional: record the script version or a hash of each sound alongside the committed files,
       so drift is visible in review.
