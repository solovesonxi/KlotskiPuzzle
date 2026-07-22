# Changelog

All notable changes to KlotskiPuzzle are documented here.

## 1.0.0 - 2026-07-23

- Require Java 22 or later and add reproducible Maven/JUnit verification.
- Share movement rules between player input and the A* solver.
- Run solving in the background with progress, cancellation, and EDT playback.
- Store local player data outside the source tree and hash passwords with PBKDF2.
- Extract save persistence, sound effects, AI coordination, and leaderboard UI from large controllers.
- Replace all media of uncertain provenance with original programmatically generated images and audio.
- Add bilingual documentation, contribution guidance, issue forms, CI, and tag-based releases.
