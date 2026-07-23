# Support explicit movement rules

KlotskiPuzzle distinguishes Cell Step, where each one-cell translation costs one, from Piece Move, where a same-direction translation across any positive number of clear cells costs one. Cell Step remains the compatibility default, while experiments may select either rule; solver results may be compared only when they use the same movement rule because the choice changes the graph, optimal path, and heuristic requirements.
