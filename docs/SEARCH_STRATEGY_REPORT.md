# Reproducible search strategy report

This report compares the four Algorithm Lab strategies under one declared experiment contract. It is intended to make project changes reproducible, not to rank Klotski solvers implemented with different rules or hardware.

## Version boundary

The report command was added on 2026-07-27 in commit [`80ac8c2`](https://github.com/44-99/KlotskiPuzzle/commit/80ac8c2c7b11967f5fcc6d0a42c95c7c4af3bcd0), after the `v2.0.0-beta.1` release. The published beta Windows package and executable JAR demonstrate Play Mode and Algorithm Lab, but they do not contain `cli.SearchStrategyReport`. Run the command from current `main` or from that commit.

## Experiment contract

| Parameter | Value |
|---|---|
| Puzzle preset | `tutorial` |
| Movement rule | `CELL_STEP` — one piece translated by one cell costs one move |
| Strategies | BFS, Greedy Best-First, A*, Weighted A* |
| Maximum discovered states | 250,000 |
| Weighted A* heuristic weight | 1.5 |
| Runtime used for the checked-in records | Eclipse Temurin 22.0.2, Windows 11 x64 |
| Result validation | Every returned path is replayed through the shared puzzle rules |

Identical puzzle content, movement rule, parameters, state limit, code revision, and deterministic tie-breaking produce the same path and search counts. Elapsed time is an environment metric and is not asserted as deterministic.

## Result

![Expanded states for four search strategies under the tutorial Cell Step contract](assets/tutorial-cell-step-expanded-states.svg)

| Strategy | Status | Moves | Expanded | Discovered | Maximum frontier |
|---|---|---:|---:|---:|---:|
| BFS | Solved | 23 | 19,837 | 22,233 | 2,422 |
| Greedy Best-First | Solved | 28 | 1,292 | 1,621 | 345 |
| A* | Solved | 23 | 12,445 | 14,848 | 2,453 |
| Weighted A* (`w = 1.5`) | Solved | 23 | 10,716 | 13,002 | 2,295 |

For this layout, Greedy Best-First expands the fewest states but returns a 28-move path. BFS, A*, and Weighted A* each return 23 moves in this run, while A* and Weighted A* expand fewer states than BFS. This single experiment does not prove that Weighted A* is always optimal or that one strategy is universally faster.

The checked-in TSV includes the local elapsed time from the generation run (`204`, `10`, `100`, and `71` ms). Those values are useful only as local context and should be regenerated before making a performance claim about another machine.

## Reproduce it

Requirements: Java 22 or later and Maven 3.9 or later.

PowerShell:

```powershell
mvn -q exec:java `
  '-Dexec.mainClass=cli.SearchStrategyReport' `
  '-Dexec.args=tutorial cell-step --tsv=docs/data/search-strategy-report/tutorial-cell-step.tsv --json-dir=docs/data/search-strategy-report/records'
```

POSIX shells:

```bash
mvn -q exec:java \
  -Dexec.mainClass=cli.SearchStrategyReport \
  -Dexec.args="tutorial cell-step --tsv=docs/data/search-strategy-report/tutorial-cell-step.tsv --json-dir=docs/data/search-strategy-report/records"
```

The command prints TSV to standard output, writes the same table to the requested path, and exports one versioned Experiment Record per strategy.

## Evidence files

- [Tab-separated comparison data](data/search-strategy-report/tutorial-cell-step.tsv)
- [BFS Experiment Record](data/search-strategy-report/records/tutorial-cell-step-bfs.json)
- [Greedy Best-First Experiment Record](data/search-strategy-report/records/tutorial-cell-step-greedy-best-first.json)
- [A* Experiment Record](data/search-strategy-report/records/tutorial-cell-step-a-star.json)
- [Weighted A* Experiment Record](data/search-strategy-report/records/tutorial-cell-step-weighted-a-star.json)
- [`SearchStrategyReport` source](../src/main/java/cli/SearchStrategyReport.java)
- [Deterministic regression test](../src/test/java/cli/SearchStrategyReportTest.java)

The JSON records include the content-addressed puzzle identity, complete solution, deterministic metrics, Java version, operating system, and architecture. The TSV is convenient for a spreadsheet or chart; the JSON records are the reviewable experiment artifacts.
