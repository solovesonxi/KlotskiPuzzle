# Changelog

All notable changes to KlotskiPuzzle are documented here.

## Unreleased

## 2.0.0-beta.1 - 2026-07-24

- Remove password registration/login and the credential repository; replace them with a direct Play Mode / Algorithm Lab start screen without deleting legacy user files.
- Add deterministic Search Expansion events with candidate scores and explicit accept/reject decisions.
- Add Search Overview, State Inspector, and validated Solution Replay with previous, play/pause, next, and slider navigation.
- Add versioned JSON Experiment Record export containing puzzle identity, configuration, outcome, solution, metrics, and runtime context.
- Replace the operating-system split divider with a narrow themed divider, continuous drag feedback, sensible minimum widths, and themed combo/spinner controls.
- Split Lab presentation into experiment controls, overview, inspector, replay, and shared board-rendering modules.
- Begin the v2 algorithm-lab foundation with validated content-addressed Puzzle Definitions, explicit Cell Step and Piece Move rules, and stable Puzzle Presets.
- Add one deterministic experiment runner for BFS, Greedy Best-First, A*, and Weighted A* with cancellation, limits, paths, and shared metrics.
- Add an independent Lab Mode preview with background execution, a board preview, live counters, bilingual controls, and 1280x720 render coverage.
- Unify the start, difficulty, game, piece, leaderboard, button, text, and audio presentation under a modern Eastern strategy-board art direction.
- Replace the four on-screen direction buttons with press-and-slide gestures while retaining arrow-key and WASD input.
- Render localized piece names in Java over original lacquered-wood assets and add selection feedback.
- Regenerate four original pentatonic background tracks and add selection, invalid-move, and undo effects.
- Fix English resource loading on Chinese systems and preserve active AI search or playback when switching languages.
- Keep leaderboard content opaque so an empty board cannot leak through the dialog background.
- Add accessible names and tooltips to the icon-only music controls.
- Add a command-line report for fresh solver status, path, search, and timing metrics.
- Make English the default GitHub README, retain a Chinese switch, and generate locale-specific demo GIFs.
- Add matching English and Chinese contribution guides.
- Add complete English and Simplified Chinese runtime messages with `--lang` selection and bundle parity tests.
- Keep countdown state in the game model instead of parsing localized label text.
- Prepare tagged releases with a portable Windows application and SHA-256 checksums.
- Add a public scope-focused roadmap and document language-specific launch commands.
- Add an in-game language button that immediately refreshes start, Lab, and gameplay controls.
- Replace the default leaderboard presentation with a dedicated parchment-themed dialog.
- Group the original previous, play/pause, and next icons into a labeled global music control.

## 1.0.0 - 2026-07-23

- Require Java 22 or later and add reproducible Maven/JUnit verification.
- Share movement rules between player input and the A* solver.
- Run solving in the background with progress, cancellation, and EDT playback.
- Store local player data outside the source tree and hash passwords with PBKDF2.
- Extract save persistence, sound effects, AI coordination, and leaderboard UI from large controllers.
- Replace all media of uncertain provenance with original programmatically generated images and audio.
- Add bilingual documentation, contribution guidance, issue forms, CI, and tag-based releases.
