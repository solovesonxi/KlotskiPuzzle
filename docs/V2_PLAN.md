# KlotskiPuzzle v2 plan

## Product identity

KlotskiPuzzle v2 is a Java 22 explainable Huarong Dao algorithm lab. Its primary value is making search behavior observable, comparable, and reproducible; Play Mode proves the shared puzzle rules through a polished human experience. AI-assisted engineering is part of the project story, not the product identity.

## Implementation status

The first development slice now provides validated content-addressed Puzzle Definitions, Cell Step and Piece Move successor generation, stable Puzzle Presets, and one deterministic experiment runner for BFS, Greedy Best-First, A*, and Weighted A*. An independent bilingual Lab Mode preview runs experiments off the Swing event-dispatch thread and shows a board preview, progress counters, final metrics, and cancellation.

This is not stable v2: the full Search Overview, State Inspector, Solution Replay, import/export, Experiment Records, HTML reports, Play Mode session qualification, profile migration, and complete responsive-layout work remain open.

## Flagship experience

A user selects a validated 5x4 Puzzle Definition, configures a Search Experiment, and learns through three layers:

1. Search Overview for exact aggregate progress and frontier growth;
2. State Inspector for one expanded state, its candidates, scores, and acceptance decisions;
3. Solution Replay for the final path after search completes.

Algorithm Comparison follows the walkthrough and never replaces it with unexplained timing tables.

## Stable v2.0 definition of done

- Separate top-level Play Mode and Lab Mode with shared puzzle rules and board rendering.
- Support 1280x720 and common Windows display scaling.
- Produce deterministic search events with explicit tie-breaking.
- Compare BFS, Greedy Best-First Search, A*, and Weighted A*.
- Support both Cell Step and Piece Move movement rules, with Cell Step as the compatibility default.
- Provide Search Overview, State Inspector, and Solution Replay.
- Import and export validated, versioned 5x4 Puzzle Definitions with stable content identities.
- Replace the current difficulty concept with stable Puzzle Presets: tutorial, intermediate, and Heng Dao Li Ma.
- Export versioned Experiment Records with optional compressed complete traces.
- Generate self-contained, read-only HTML Experiment Reports without reimplementing the solver in JavaScript.
- Replace password accounts with optional local Player Profiles.
- Record AI, hint, undo, and loaded-progress completions as Assisted Completions rather than Qualified Results.
- Scope Qualified Results by puzzle identity, movement rule, and Challenge Rules version.
- Offer user-controlled legacy profile and save migration; never import Legacy Results as Qualified Results.
- Keep tests, bilingual documentation, the Windows portable package, and real demonstrations aligned with the implementation.

## Explicitly outside stable v2.0

- A visual puzzle editor; validated file import and export comes first.
- Arbitrary board dimensions, piece geometries, targets, or exits.
- A complete web game or a second JavaScript solver.
- Cloud accounts, online leaderboards, multiplayer, or a server backend.
- A plugin system.
- Additional skins or music as roadmap drivers.

## Experiment contract

- Identical puzzle, movement rule, strategy, parameters, limits, and version produce identical search order, path, and deterministic metrics.
- Elapsed time and memory are Environment Metrics and include their JDK, operating system, and hardware context.
- Algorithms are compared only under the same Puzzle Definition, movement rule, and metrics contract.
- Piece Class participates in search-state identity. Display Identity remains stable within a session but never expands the solver state space.

## Migration boundary

The current development machine's test profiles, saves, and legacy leaderboard may be permanently removed during implementation after resolving the exact target directory. Released v2 builds never delete another user's legacy data silently; they offer an explicit one-time import, skip, or delete choice.
