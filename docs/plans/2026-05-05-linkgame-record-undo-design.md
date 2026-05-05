# Link Game Record + Undo Design (2026-05-05)

## Goals
- Record the first three successful clears per game to a file.
- Provide a simple one-step undo button in the control panel.

## Scope
- Record data includes: two icon types, two positions, and link path.
- Undo restores the last successful clear only (single step).

## Data Flow
- BoardPanel handles successful clears and emits records.
- Records are appended to resource/records/Records.txt.
- Undo uses a single in-memory snapshot for the last clear.

## UI
- Add an "undo" button to the bottom control panel.
- Button triggers BoardPanel.undoLastClear().

## Error Handling
- If record file write fails, the game continues normally.
- Undo is disabled during animations or after a failed match.

## Testing Notes
- Start a new game and verify records contain exactly three lines.
- Clear once and verify undo restores icons and score/combos.
- Attempt undo after a failed match to confirm it is ignored.
