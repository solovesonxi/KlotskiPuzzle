# Art direction

KlotskiPuzzle uses a **modern Eastern strategy board** style. The interface should feel calm,
tactile, and readable rather than imitate a historical game UI literally.

## Visual system

- Deep ink-blue and warm charcoal backgrounds keep the puzzle board dominant.
- Restrained lacquer red and muted gold identify interactive controls and selection state.
- Parchment is reserved for modal information such as difficulty and leaderboard views.
- Microsoft YaHei UI is the primary interface typeface; a calligraphic display face is used only
  for short titles. Avoid mixing multiple decorative fonts in controls.
- Pieces use one lacquered-wood construction, a shared light direction, and role-specific colors.
  Names are rendered by Java so Chinese and English stay synchronized.
- Decorative texture must remain low contrast. It may support depth, but never compete with piece
  labels, available spaces, or the exit.

## Interaction system

- Drag is the primary move gesture. Arrow keys and WASD remain keyboard alternatives.
- Persistent controls are limited to session actions, solver, leaderboard, audio, and language.
- Selection uses a gold outline and glow. Illegal movement must keep the board state unchanged and
  return a short, quiet audio cue.

## Audio system

- Background music uses original, deterministic synthesis inspired by plucked strings, breathy
  lead tones, pentatonic melody, and restrained ambient harmony.
- Music sits below interaction sounds and must not mask them. Runtime playback is reduced by 14 dB.
- Movement, selection, invalid movement, undo, victory, and defeat each have a distinct short cue.
- Every shipped sound is reproducible with `tools/generate_original_assets.py`; downloaded music or
  samples are not accepted.
