# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Java chess game with a JavaFX GUI, FEN import/export, player-vs-player, player-vs-bot, and bot-vs-bot modes. The Maven project lives in `ChessGame/` and uses Java 11, JavaFX 17, and JUnit 5. Entry point is `itec220.labs.ChessGUI`.

## Build & Run Commands

All commands run from `ChessGame/`:

```bash
mvn clean javafx:run        # Build and launch the GUI
mvn clean compile           # Compile only
mvn test                    # Run JUnit engine regression tests
mvn javadoc:javadoc         # Generate JavaDocs into ChessGame/target/site/apidocs/
```

Run Maven commands from `ChessGame/`, not from the repository root. Generated Maven output belongs under `ChessGame/target/`. Do not commit generated JavaDocs; `ChessGame/doc/` and `ChessGame/target/` are ignored.

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
| GUI | `ChessGUI`, `ChessButton`, `ChessStackPane`, `PromoteButton` | JavaFX scene graph, menu flow, board rendering, user input, sounds, FEN display/copy |
| Controller | `GameController`, `GameViewListener` | Coordinates players, human moves, async bot turns, restart/FEN load, and view notifications |
| Players/bot | `ChessPlayer`, `HumanPlayer`, `BotPlayer`, `ChessBot` | Human/bot side abstraction and bot move selection |
| Game logic | `Game`, `Move` | Turn management, legal move model, move history, promotion state, FEN import/export, repetition tracking |
| Board logic | `Board` | Piece placement, move execution, check detection, en passant, castling, simulation |
| Pieces | `Piece` (abstract) + 6 subclasses | Per-piece move generation |
| Enums | `Color`, `PieceType`, `GameState` | Shared constants |

### Key design points

- **`GameController` is the GUI/engine boundary** - route GUI moves through `submitHumanMove()`, promotion through `promote()`, restarts through `restart()`, and FEN replacement through `loadFen()`. It also prevents human input while bots are thinking.
- **Bot turns are asynchronous in the GUI** - `GameController.pollMove()` uses a short `PauseTransition` and a JavaFX `Task<Move>`. Tests can use `pollMoveSynchronouslyForTesting()` to avoid background timing.
- **`Move` is the shared move model** - it stores start/end coordinates plus optional promotion, captured piece type, en passant, and castling metadata. Prefer `Game.move(Move)` for bot/test flows where promotion choice is explicit.
- **`Game.getLegalMoves()` returns full legal `Move` objects** for the current side. Promotion moves are expanded to queen, rook, bishop, and knight options.
- **`Piece.getValidMoves(Board copy, boolean kingCheck)`** - every piece implements this and returns destination coordinates. `kingCheck=true` skips the self-check filter, which is used internally to avoid recursive check evaluation.
- **`Board.isLegalMove()` / `simulateMove()`** - simulate moves on a board copy to verify the moving side does not leave its king in check.
- **`Board.copy()`** - deep-copies the board and en passant state; used extensively before speculative move evaluation.
- **Move coordinates** - `(rank, file)` map to `(row, col)` in a `Piece[8][8]` array. White starts at rows 0-1, Black at rows 6-7.
- **En passant** - tracked via `Board.enPassant` (`SimpleEntry<Integer,Integer>`), cleared after each move, and serialized through FEN target-square handling.
- **Castling** - `King` and `Rook` both track `hasMoved`; castling right = `+2` file, left = `-2` file for the king.
- **Promotion** - human moves without an explicit promotion type set `Game.isPromotionPending()` and wait for the GUI's `PromoteButton` choice. Bot/test moves should include `Move.promotionType` so the move can complete immediately. `Board.promote()` replaces a `Pawn` in-place.
- **FEN support** - `Game.toFEN()` serializes the full position and counters; `Game.loadFEN()` replaces the game state and rehydrates castling/en passant rights.
- **Position identity** - `Game.getPositionIdentity()` intentionally includes piece placement, side to move, castling rights, and en passant availability for repetition tracking.
- **3-fold repetition** - `Game.updateMoveTracker()` stores position identity strings in a `LinkedList` and sets `DRAW` on the third repeat.
- **Attack detection** - `Board.isSquareAttacked()` uses direct piece attack rules. Prefer it for king safety and castling path checks instead of deriving attacks from legal move generation.
- **Chess bot** - `ChessBot` is depth-limited alpha-beta search with quiescence, move ordering, a per-search transposition table, and static evaluation heuristics. Keep bot changes deterministic in tests by using package-private constructors with injected `Random`/thread count where practical.
- **GUI board rendering** - `ChessGUI` builds stable square nodes once and updates image/highlight nodes in place. Avoid rebuilding the whole grid for every move unless the UI structure itself changes.

### Resources (loaded via classpath)

Piece PNGs and sound files (`ChessMove.mp3`, `ChessCapture.mp3`) live in `ChessGame/src/main/resources/itec220/labs/`. CSS is at the same path as `styles.css`.

## Tests

Tests live under `ChessGame/src/test/java/itec220/labs/` and use JUnit Jupiter. The suite is split by behavior:

- `MoveGenerationTest`, `KingRulesTest`, `CastlingTest`, `PromotionTest` cover core chess rules.
- `FenSerializationTest` covers FEN round-tripping, counters, castling rights, and en passant targets.
- `RepetitionAndStateTest` covers draw/state transitions and repetition identity.
- `ChessBotTest` covers bot move selection and evaluation behavior.
- `GameControllerTest` covers controller coordination, human/bot turns, promotion, restart, and FEN load.
- `TestSupport` contains shared test setup helpers.

When changing rule logic, add or update focused regression tests in the matching test class. When changing controller/player behavior, prefer controller tests over JavaFX scene tests unless rendering itself is the thing being changed.
