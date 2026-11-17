# Audio assets

The sound effects in `shared/core/src/commonMain/composeResources/files/*.m4a` are synthesised by
`generate_sfx.py` (numpy, fixed seeds), so they are reproducible and licence-free. The script
also fixes the loop seam of `music.m4a` and sets its level.

```bash
python3 tools/audio/generate_sfx.py --out /tmp/sfx   # audition: WAVs, m4a and audition.wav
python3 tools/audio/generate_sfx.py                  # overwrite the app's effects
python3 tools/audio/generate_sfx.py --only match     # one sound
python3 tools/audio/generate_sfx.py --music          # also trim and level music.m4a
```

Requirements: numpy; `afconvert` (macOS) or `ffmpeg` for AAC; `ffmpeg` for `--music`.

## Mix

Each effect is normalised by its loudest 50 ms (short-term RMS), and a limiter holds peaks at
-1 dBFS. The script prints each effect's length, RMS against its target, and peak.

| group | sounds | target |
|---|---|---|
| frequent | flip, deal, click, plink | -21 to -19 dB |
| feedback | match, mismatch | -17 to -16 dB |
| stingers | nuts, win, lose, highscore | -15 to -14 dB |
| music | music | -22 LUFS integrated |

At runtime the match sound climbs in pitch with the combo, and flips and deals vary slightly in
pitch (`sharedUI/.../services/AudioPitch.kt`). Music fades in and out through `MusicFader`.
