# Asset provenance and licensing

KlotskiPuzzle does not bundle third-party game artwork, recordings, music samples, video footage, or screenshots.

## Original project assets

The tracked runtime media under `resources/original/` and the raster previews under `docs/assets/` were generated specifically for this repository on 2026-07-23 by [`tools/generate_original_assets.py`](tools/generate_original_assets.py).

- Backgrounds, chess pieces, icons, previews, and GIF frames are drawn from geometric primitives, gradients, and deterministic noise.
- Music and sound effects are synthesized from mathematical waveforms; they contain no recorded or sampled audio.
- The generation script may use a locally installed font to rasterize labels. No font file is copied into or distributed with the repository.

These generated assets are distributed under the same [MIT License](LICENSE) as the source code. They may be used, modified, and redistributed under that license.

## Removed legacy media

Earlier revisions contained media whose redistribution permission could not be verified. Those files are not part of the current tree or release artifact and were removed from the repository's published Git history before the `v1.0.0` release.

If a future contribution adds external media, the pull request must record its source, author, license, and redistribution terms here before the asset is merged.
