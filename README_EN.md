<div align="center">
  <p>
    <a href="README.md"><img alt="简体中文" src="https://img.shields.io/badge/语言-简体中文-6e7781?style=flat-square"></a>
    <a href="README_EN.md"><img alt="English" src="https://img.shields.io/badge/Language-English-2f81f7?style=flat-square"></a>
  </p>

  <h1>KlotskiPuzzle</h1>

  <p><strong>A Java 22+ Swing learning project for multi-cell board modeling, A* solving, and non-blocking animated playback.</strong></p>

  <p>
    <a href="https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml/badge.svg"></a>
    <a href="https://openjdk.org/projects/jdk/22/"><img alt="Java 22+" src="https://img.shields.io/badge/Java-22%2B-orange.svg"></a>
    <a href="LICENSE"><img alt="MIT code license" src="https://img.shields.io/badge/Code%20License-MIT-blue.svg"></a>
  </p>

  <img src="docs/assets/klotski-preview.svg" width="760" alt="Neutral KlotskiPuzzle board illustration">
</div>

KlotskiPuzzle is a Java 22+ Swing implementation of Huarong Dao (Klotski) that evolved from a Java GUI course project. Its primary audience is students and junior Java developers who know the language basics and want to build their first project combining a desktop UI with a search algorithm.

The repository is not positioned as a production-ready commercial game. It demonstrates how to organize multi-cell movement rules, a playable UI, A* search, background work, animated playback, local saves, and automated tests into a project that can be run, verified, and extended.

## Why This Project Exists

Many Java course examples provide either a Swing interface or an isolated algorithm. The learner still has to solve several integration problems:

- A piece may occupy a 1×1, 1×2, 2×1, or 2×2 rectangle, making movement rules more involved than ordinary maze traversal;
- Separate player and solver rules quickly create cases where the UI accepts a move that the solver rejects;
- Running A* on the Swing event dispatch thread freezes the interface, while replaying the result introduces another lifecycle problem;
- Screenshots and source archives alone do not prove that a project builds, passes tests, or remains maintainable.

KlotskiPuzzle addresses these problems in one repository with explicit technical boundaries. It is intended for study, experimentation, and refactoring rather than submission as course work.

## Quick Start

### Requirements

- JDK 22 or later;
- Maven 3.9 or later;
- A desktop environment capable of running Swing;
- Git LFS to check out the animated background assets.

```bash
git clone https://github.com/44-99/KlotskiPuzzle.git
cd KlotskiPuzzle
git lfs pull
mvn clean verify
mvn exec:java
```

After building, the JAR can also be launched directly:

```bash
java -jar target/klotski-puzzle-1.0.0-SNAPSHOT.jar
```

Images and audio are packaged as classpath resources, so the JAR does not depend on the process working directory.

## What You Can Learn

| Question | Approach in this project | Start here |
|---|---|---|
| How should multi-cell pieces move? | A matrix represents the board, while a pure Java rules layer identifies pieces and enforces collisions, bounds, and the solved state | [`BoardRules.java`](src/main/java/model/BoardRules.java) |
| How can player and solver logic share rules? | Player actions and solver expansion both call `BoardRules.applyMove` | [`GameController.java`](src/main/java/controller/GameController.java), [`HuaRongDaoSolver.java`](src/main/java/model/HuaRongDaoSolver.java) |
| How can A* remain bounded and observable? | `PriorityQueue`, a best-known-step map, parent reconstruction, cancellation checks, and a discovered-state limit | [`HuaRongDaoSolver.java`](src/main/java/model/HuaRongDaoSolver.java) |
| How can Swing stay responsive? | `SwingWorker` performs search in the background and `Swing Timer` replays moves on the EDT | [`ControlPanel.java`](src/main/java/view/game/ControlPanel.java) |
| How does course code become a verifiable project? | Maven builds it, JUnit 5 checks rules and paths, and GitHub Actions runs continuous integration with Java 22 | [`pom.xml`](pom.xml), [`src/test/java`](src/test/java) |
| How are local-data boundaries handled? | Player data lives under the user directory, passwords use PBKDF2, and saves and rankings use temporary-file replacement | [`data`](src/main/java/data) |

The AI execution flow is:

```text
Board snapshot -> SwingWorker -> A* search -> Result -> Swing Timer -> BoardRules -> UI playback
```

## Playable Features

- A 5×4 Huarong Dao board with three built-in layouts;
- Keyboard, WASD, mouse, and on-screen directional controls;
- Background A* search with progress, cancellation, and animated playback;
- Undo, restart, and a 180-second timed challenge;
- Local players, saves, step rankings, and time rankings;
- Background music plus move, victory, and defeat sound effects.

## Controls and Local Data

1. Create a local player or continue as a guest;
2. Choose a layout and select a piece;
3. Move with the arrow keys, WASD, or the on-screen buttons;
4. Use the undo action to revert a move and the AI action to start or stop playback;
5. Local players can save progress; guest progress is not persisted.

Player profiles, saves, and leaderboard data are stored in `${user.home}/.klotski-puzzle/`. This is a single-machine profile system, not an online account service. Base64 is used only as save-data encoding and is not encryption.

## Project Structure

```text
KlotskiPuzzle/
├── src/main/java/
│   ├── controller/   # Player actions, animation, saves, and completion flow
│   ├── data/         # Local data paths and player credentials
│   ├── model/        # Board model, layouts, movement rules, and A* solver
│   ├── util/         # Classpath resources and background-music lifecycle
│   └── view/         # Swing windows and components
├── src/test/java/    # JUnit 5 tests
├── resources/        # Runtime and demonstration media
├── docs/             # Documentation and project visuals
└── pom.xml           # Java 22 Maven build
```

The source uses coarse `model`, `controller`, and `view` packages, but it is not strict MVC: the controller still owns parts of audio, saves, leaderboards, and dialogs. This makes responsibility separation a useful follow-up refactoring exercise.

## Verification

```bash
mvn clean verify
```

Automated tests cover board integrity, legal and illegal movement for all four piece types, built-in layout solving and path replay, cancellation and state limits, local password hashing, leaderboard recovery, and packaged resources. A resource test also prevents the animated login GIF from silently regressing to a static image. See the CI badge at the top for the latest build result.

## Project Boundaries

- The project requires Java 22+ and targets desktop environments that support Swing;
- A* keeps at most 250,000 discovered states by default and reports the limit explicitly. Peak-memory and optimal-move benchmarks for standard layouts have not been established yet;
- The interface uses a fixed window and absolute positioning, so small-screen and high-DPI support is limited;
- Profiles, rankings, and saves are local only; there is no server-side authentication or cross-device synchronization;
- The current music, video/GIF, and character images have no verifiable redistribution permission. Replace them with original, CC0, or explicitly redistributable media before public demonstrations or binary releases. See [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing. Useful areas include solver benchmarks, GUI integration tests, responsibility separation, responsive layouts, and replacement media with documented redistribution rights.

## License

The source code is licensed under the [MIT License](LICENSE). Media under `resources/` is not automatically covered by MIT; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
