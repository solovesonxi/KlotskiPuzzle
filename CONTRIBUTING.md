<div align="center">
  <a href="CONTRIBUTING.md">English</a> ·
  <a href="CONTRIBUTING_ZH.md">简体中文</a>
</div>

# Contributing

Thanks for improving KlotskiPuzzle. This repository is designed for students and junior developers learning Java desktop development and search algorithms, so contributions should keep the code runnable, the rules verifiable, and the technical boundaries explainable.

Good first contributions include adding board tests, improving the A* heuristic, contributing reproducible layouts, and making the Swing UI work better across screen sizes. You may study and adapt this project for coursework, but should not submit the repository unchanged as your own assignment.

## Development requirements

- JDK 22 or later
- Maven 3.9 or later
- Optional: Python 3.11+ and Pillow, only when regenerating original assets

## Local verification

```bash
mvn verify
```

Before opening a pull request, make sure that:

- Player actions and AI playback both update the board through `model.BoardRules`;
- New rules include JUnit tests;
- `target/`, `out/`, and local player data are not committed;
- Original media is preferably generated with `tools/generate_original_assets.py`; any external media must be documented in `THIRD_PARTY_NOTICES.md` with its author, source URL, license, and attribution requirements;
- README commands and technical claims still match the implementation.

Pull requests should describe the problem, implementation, verification commands, and results. Solver performance changes should include before-and-after data from the same layouts, JDK, and hardware.
