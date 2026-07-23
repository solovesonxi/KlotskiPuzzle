# Separate Play Mode from Lab Mode

KlotskiPuzzle provides Play Mode for human puzzle sessions and Lab Mode for reproducible search experiments. They share puzzle definitions, movement rules, and board rendering, but keep independent lifecycles so solver assistance, experiment controls, rankings, and challenge timing cannot silently affect one another.
