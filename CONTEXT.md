# Klotski Algorithm Lab

KlotskiPuzzle exists to make Klotski search behavior playable, observable, comparable, and reproducible.

## Language

**Algorithm Lab**:
The primary product: an interactive environment for playing Klotski while observing and comparing reproducible solver behavior.
_Avoid_: Mini-game, coursework demo, Codex showcase

**Play Mode**:
The product mode for a human puzzle session, including manual movement, optional timing, persistence, and explicitly qualified results.
_Avoid_: Game screen, manual mode

**Play Session**:
One human attempt performed under a fixed Puzzle Definition, movement rule, and Challenge Rules version.
_Avoid_: Game, run

**Lab Mode**:
The product mode for configuring, inspecting, replaying, and comparing search experiments without player rankings or challenge timing.
_Avoid_: AI mode, solver screen

**Search Walkthrough**:
The flagship learning experience in which a user advances through a solver's decisions and inspects why each state is considered.
_Avoid_: AI playback, solution animation

**Search Overview**:
The aggregate view of an experiment's exact progress, including frontier growth, discovered states, expanded states, and current priority.
_Avoid_: Search animation

**State Inspector**:
The focused explanation of one expanded state, its candidate moves, their scores, and why each candidate was accepted or rejected.
_Avoid_: Board preview, move history

**Solution Replay**:
The stepwise presentation of the final solution path after a search completes.
_Avoid_: Search walkthrough, AI reasoning

**Piece Class**:
The rule-bearing kind of a piece, which determines its geometry and participates in search-state identity. Pieces of the same class are interchangeable to a solver.
_Avoid_: Character, hero, piece name

**Display Identity**:
A stable presentation identity assigned to a piece for one puzzle session; it preserves names and appearance but never changes search-state identity.
_Avoid_: Piece class, solver identity

**Cell Step**:
A movement rule in which translating one piece by one board cell has a cost of one.
_Avoid_: Step, move

**Piece Move**:
A movement rule in which translating one piece through any positive number of clear cells in one direction has a total cost of one.
_Avoid_: Step, slide

**Puzzle Definition**:
An immutable, validated, and shareable description of a puzzle's initial board and rule choices, identified by its content.
_Avoid_: Level file, map, difficulty

**Puzzle Preset**:
A bundled Puzzle Definition with a stable identity and a curated purpose or recognized layout name.
_Avoid_: Difficulty, level

**Challenge Rules**:
The Play Mode policy for timing, assistance, and result qualification, independent of the selected Puzzle Definition.
_Avoid_: Difficulty, puzzle rules

**Qualified Result**:
A completed Play Session that began at the initial state, remained manual and unassisted, used no undo or loaded progress, and kept one rules version throughout. It is comparable only with results for the same puzzle, movement rule, and Challenge Rules version.
_Avoid_: Score, win

**Assisted Completion**:
A valid completion of a Play Session after using assistance, undo, or loaded progress; it is never included with Qualified Results.
_Avoid_: Ranked result, failure

**Legacy Result**:
A pre-v2 leaderboard entry that lacks the puzzle, movement rule, assistance, and rules-version evidence required for qualification.
_Avoid_: Qualified result, migrated score

**Player Profile**:
An optional local display identity that groups a person's saves and Qualified Results without claiming authentication or tamper resistance.
_Avoid_: Account, user, login

**Algorithm Comparison**:
A secondary experiment that evaluates multiple solvers on the same puzzle under one move definition and one metrics contract.
_Avoid_: Uncontrolled benchmark, timing race

**Search Experiment**:
A reproducible execution of one search strategy against one puzzle under declared parameters, limits, and metric definitions.
_Avoid_: Solver run, benchmark run

**Experiment Record**:
A versioned, shareable artifact containing a Search Experiment's reproducible configuration, outcome, deterministic metrics, and qualified environment observations. A complete search trace is an optional attachment rather than required record content.
_Avoid_: Save file, log, screenshot

**HTML Experiment Report**:
A self-contained, read-only presentation of an Experiment Record that can be opened without Java and never reimplements solver behavior.
_Avoid_: Web app, web game, solver frontend

**Deterministic Metric**:
An experiment result that must remain identical for the same puzzle, strategy, parameters, and version, such as path length or expanded-state count.
_Avoid_: Performance number

**Environment Metric**:
An experiment result that is meaningful only with its runtime environment, such as elapsed time or peak memory.
_Avoid_: Deterministic result
