# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Java chess game with a JavaFX GUI. The Maven project lives in `ChessGame/` and uses Java 11 + JavaFX 17. Entry point is `itec220.labs.ChessGUI`.

## Build & Run Commands

All commands run from `ChessGame/`:

```bash
mvn clean javafx:run        # Build and launch the GUI
mvn clean compile           # Compile only
mvn javadoc:javadoc         # Generate JavaDocs into ChessGame/target/site/apidocs/
```

The pre-generated docs in `ChessGame/doc/` are checked-in snapshots — regenerate with `mvn javadoc:javadoc` rather than editing them manually.

## Architecture

### Layer separation

| Layer | Class(es) | Responsibility |
|-------|-----------|----------------|
| GUI | `ChessGUI`, `ChessButton`, `ChessStackPane`, `PromoteButton` | JavaFX scene graph, user input, rendering |
| Game logic | `Game` | Turn management, game-state transitions, 3-fold repetition draw |
| Board logic | `Board` | Piece placement, move execution, check detection, en passant, castling |
| Pieces | `Piece` (abstract) + 6 subclasses | Per-piece move generation |
| Enums | `Color`, `PieceType`, `GameState` | Shared constants |

### Key design points

- **`Piece.getValidMoves(Board copy, boolean kingCheck)`** — every piece implements this. `kingCheck=true` skips the self-check filter (used internally to avoid infinite recursion when evaluating check).
- **`Piece.isValidMove()`** — simulates the move on a board copy to verify it doesn't leave the king in check.
- **`Board.copy()`** — deep-copies the board; used extensively before any speculative move evaluation.
- **Move coordinates** — `(rank, file)` map to `(row, col)` in a `Piece[8][8]` array. White starts at rows 0–1, Black at rows 6–7.
- **En passant** — tracked via `Board.enPassant` (`SimpleEntry<Integer,Integer>`), cleared after each move.
- **Castling** — `King` and `Rook` both track `hasMoved`; castling right = `+2` file, left = `-2` file for the king.
- **Promotion** — `Board.promote()` replaces a `Pawn` in-place; the GUI adds `PromoteButton`s to the bottom bar when a pawn reaches the back rank.
- **3-fold repetition** — `Game.updateMoveTracker()` stores board-state strings in a `LinkedList` and sets `DRAW` on the third repeat.

### Resources (loaded via classpath)
Piece PNGs and sound files (`ChessMove.mp3`, `ChessCapture.mp3`) live in `ChessGame/src/main/resources/itec220/labs/`. CSS is at the same path as `styles.css`.