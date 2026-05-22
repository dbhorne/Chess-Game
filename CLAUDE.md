# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Java chess game with a JavaFX GUI. The Maven project lives in `ChessGame/` and uses Java 11 + JavaFX 17. Entry point is `itec220.labs.ChessGUI`.

## Build & Run Commands

All commands run from `ChessGame/`:

```bash
mvn clean javafx:run        # Build and launch the GUI
mvn clean compile           # Compile only
mvn test                    # Run JUnit engine regression tests
mvn javadoc:javadoc         # Generate JavaDocs into ChessGame/target/site/apidocs/
```

The pre-generated docs in `ChessGame/doc/` are checked-in snapshots - regenerate with `mvn javadoc:javadoc` rather than editing them manually.

## Change & Commit Expectations

- Treat notable behavior, test, documentation, dependency, build, and configuration updates as commit-worthy. Each notable change should have a corresponding git commit before the work is considered complete.
- Use conventional, scoped commit subjects with one of these prefixes:
  - `fix: ...` for bug fixes and rule-correctness changes.
  - `feature: ...` for user-visible capabilities or new supported workflows.
  - `chore: ...` for documentation, tests, maintenance, dependencies, build, and cleanup work.
- Keep commits focused on one coherent change. Do not mix unrelated GUI, game-engine, resource, and documentation edits in the same commit.
- Before committing, check `git status --short`, stage only files that belong to the change, and do not stage or revert unrelated user changes.
- Run `mvn test` from `ChessGame/` before committing code that affects game logic, move generation, FEN handling, or tests. For GUI-only work, run at least `mvn clean compile` unless the change clearly cannot affect compilation.
- Include test updates with engine-rule fixes whenever practical, especially for check detection, castling, en passant, promotion, FEN import/export, and repetition behavior.

## Architecture

### Layer separation

| Layer | Class(es) | Responsibility |
|-------|-----------|----------------|
| GUI | `ChessGUI`, `ChessButton`, `ChessStackPane`, `PromoteButton` | JavaFX scene graph, user input, rendering |
| Game logic | `Game` | Turn management, game-state transitions, FEN import/export, repetition tracking |
| Board logic | `Board` | Piece placement, move execution, check detection, en passant, castling |
| Pieces | `Piece` (abstract) + 6 subclasses | Per-piece move generation |
| Enums | `Color`, `PieceType`, `GameState` | Shared constants |

### Key design points

- **`Piece.getValidMoves(Board copy, boolean kingCheck)`** - every piece implements this. `kingCheck=true` skips the self-check filter, which is used internally to avoid recursive check evaluation.
- **`Piece.isValidMove()`** - simulates the move on a board copy to verify it does not leave the king in check.
- **`Board.copy()`** - deep-copies the board and en passant state; used extensively before speculative move evaluation.
- **Move coordinates** - `(rank, file)` map to `(row, col)` in a `Piece[8][8]` array. White starts at rows 0-1, Black at rows 6-7.
- **En passant** - tracked via `Board.enPassant` (`SimpleEntry<Integer,Integer>`), cleared after each move, and serialized through FEN target-square handling.
- **Castling** - `King` and `Rook` both track `hasMoved`; castling right = `+2` file, left = `-2` file for the king.
- **Promotion** - `Board.promote()` replaces a `Pawn` in-place; the GUI adds `PromoteButton`s to the bottom bar when a pawn reaches the back rank.
- **FEN support** - `Game.toFEN()` serializes the full position and counters; `Game.loadFEN()` replaces the game state and rehydrates castling/en passant rights.
- **Position identity** - `Game.getPositionIdentity()` intentionally includes piece placement, side to move, castling rights, and en passant availability for repetition tracking.
- **3-fold repetition** - `Game.updateMoveTracker()` stores position identity strings in a `LinkedList` and sets `DRAW` on the third repeat.
- **Attack detection** - `Board.isSquareAttacked()` uses direct piece attack rules. Prefer it for king safety and castling path checks instead of deriving attacks from legal move generation.

### Resources (loaded via classpath)

Piece PNGs and sound files (`ChessMove.mp3`, `ChessCapture.mp3`) live in `ChessGame/src/main/resources/itec220/labs/`. CSS is at the same path as `styles.css`.
