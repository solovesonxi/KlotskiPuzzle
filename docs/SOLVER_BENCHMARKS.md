# Solver regression baselines

These baselines make solver changes measurable. They are regression checks for the current implementation, not a cross-project performance ranking and not a claim that every move-counting convention has the same optimum.

## Definition

- One move means translating one piece by one cell.
- Search is bounded at 250,000 discovered positions.
- The A* priority is `g + h`, where `h` is the Manhattan distance from the 2×2 target piece to the exit position.
- The test replays every returned move through `BoardRules.applyMove` before accepting the result.

## Java 22 baseline

Measured on 2026-07-23 with Temurin 22.0.2 on Windows. Wall-clock time is shown only as local context; CI asserts move counts and state ceilings because elapsed time varies by machine.

| Preset | Moves | Expanded | Discovered | Local time |
|---|---:|---:|---:|---:|
| Beginner | 23 | 12,730 | 15,522 | 106 ms |
| Intermediate | 53 | 54,907 | 57,362 | 358 ms |
| Expert | 116 | 25,539 | 25,636 | 97 ms |

The expert preset taking more moves but exploring fewer positions is not a contradiction: layout structure and heuristic guidance affect frontier size independently of solution length.

Run the automated baseline with:

```bash
mvn -Dtest=HuaRongDaoSolverTest test
```

When changing the heuristic, neighbor order, or state representation, report the same move definition, preset matrices, JDK, expanded/discovered counts, and hardware. Update the ceilings only when the new behavior is understood and documented.
