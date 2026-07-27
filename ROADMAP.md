# KlotskiPuzzle roadmap

KlotskiPuzzle is evolving from a playable Java Swing Huarong Dao reference into a Java 22 explainable algorithm lab. The detailed stable-v2 contract is recorded in [docs/V2_PLAN.md](docs/V2_PLAN.md); the canonical domain language lives in [CONTEXT.md](CONTEXT.md).

## Current baseline

- Play three validated 5x4 layouts with press-and-slide, arrow-key, or WASD input.
- Run bounded A* search off the Swing event-dispatch thread and replay its solution.
- Switch English and Simplified Chinese at runtime.
- Build and verify with Java 22+, Maven, JUnit, and GitHub Actions.
- Package original programmatically generated runtime media and a portable Windows application.
- Run independent Lab Mode experiments with four deterministic strategies and two movement rules.
- Inspect deterministic Search Expansion milestones and accepted/rejected candidates through Search Overview and State Inspector.
- Replay validated solutions with step controls and export versioned JSON Experiment Records.
- Generate a reproducible CLI comparison for BFS, Greedy Best-First, A*, and Weighted A* with checked-in TSV and JSON evidence.
- Enter Play Mode or Lab Mode from a password-free start screen.

## V2 foundation

- Add versioned Puzzle Definition import/export on top of the implemented validation, content identity, movement rules, and stable presets.
- Separate Play Mode sessions from Lab Mode Search Experiments while sharing rule and rendering modules.
- Add optional local Player Profiles and user-controlled legacy migration now that password accounts have been removed.
- Add full compressed trace capture while keeping routine Experiment Records small.

## V2 lab experience

- Turn the implemented declared-contract CLI comparison into an interactive side-by-side Lab view.
- Import and export validated 5x4 custom puzzles.
- Add side-by-side Algorithm Comparison and self-contained read-only HTML Experiment Reports.
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
