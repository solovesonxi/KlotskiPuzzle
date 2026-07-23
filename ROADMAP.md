# KlotskiPuzzle roadmap

KlotskiPuzzle is evolving from a playable Java Swing Huarong Dao reference into a Java 22 explainable algorithm lab. The detailed stable-v2 contract is recorded in [docs/V2_PLAN.md](docs/V2_PLAN.md); the canonical domain language lives in [CONTEXT.md](CONTEXT.md).

## Current baseline

- Play three validated 5x4 layouts with press-and-slide, arrow-key, or WASD input.
- Run bounded A* search off the Swing event-dispatch thread and replay its solution.
- Switch English and Simplified Chinese at runtime.
- Build and verify with Java 22+, Maven, JUnit, and GitHub Actions.
- Package original programmatically generated runtime media and a portable Windows application.

## V2 foundation

- Introduce versioned Puzzle Definitions, explicit Cell Step and Piece Move rules, and stable Puzzle Presets.
- Separate Play Mode sessions from Lab Mode Search Experiments while sharing rule and rendering modules.
- Define deterministic search events, Experiment Records, and qualified environment observations.
- Replace password accounts with optional local Player Profiles and user-controlled legacy migration.

## V2 lab experience

- Add Search Overview, State Inspector, and Solution Replay.
- Compare BFS, Greedy Best-First Search, A*, and Weighted A* under one declared experiment contract.
- Import and export validated 5x4 custom puzzles.
- Export optional compressed traces and self-contained read-only HTML Experiment Reports.
- Keep the interface usable at 1280x720 and common Windows display scaling.

## After stable v2

- Build a visual puzzle editor on the validated Puzzle Definition module.
- Add deeper heuristic experiments and curated teaching walkthroughs.
- Expand stable GUI lifecycle coverage where it provides reproducible value.

## Not planned

- Arbitrary sliding-block engines in stable v2.
- A duplicate JavaScript solver or complete web game.
- Online accounts, cloud leaderboards, multiplayer, or a server backend.
- Features that bypass shared puzzle rules or trade reproducibility for unexplained benchmark numbers.

Ideas are welcome in [GitHub Discussions](https://github.com/44-99/KlotskiPuzzle/discussions). Scoped bugs and implementation-ready requests belong in [GitHub Issues](https://github.com/44-99/KlotskiPuzzle/issues).
