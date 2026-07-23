# KlotskiPuzzle roadmap

This roadmap keeps the project focused on its primary audience: Java students, junior developers, and learners studying Swing, state modeling, and A* search. It is not a promise of dates; it explains what belongs in the project and what does not.

## Now

- Complete and test English and Simplified Chinese runtime messages.
- Keep Java 22 builds, packaged resources, solver paths, and persistence behavior reproducible in CI.
- Produce both an executable JAR and a portable Windows application for tagged releases.

## Next

- Replace fixed 1532×864 positioning with layouts that remain usable on smaller screens and high-DPI displays.
- Add pause, single-step, and speed controls to AI playback so learners can inspect each move.
- Record a real bilingual gameplay walkthrough after the responsive layout is visually verified.

## Later

- Add a validated level editor with import/export for reproducible boards.
- Compare BFS, Dijkstra, and A* under the same move definition and benchmark contract.
- Add focused GUI lifecycle tests where they provide stable value.

## Not planned

- Online accounts, cloud leaderboards, multiplayer, or a server backend.
- Rewriting the project in a web framework merely to broaden the technology list.
- Features that bypass `BoardRules` or weaken the small, readable teaching architecture.

Ideas are welcome in [GitHub Discussions](https://github.com/44-99/KlotskiPuzzle/discussions). Scoped bugs and implementation-ready requests belong in [GitHub Issues](https://github.com/44-99/KlotskiPuzzle/issues).
