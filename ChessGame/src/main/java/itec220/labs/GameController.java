package itec220.labs;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * Coordinates players and move polling between the engine and the GUI.
 */
final class GameController {
	private static final int BOT_TURN_DELAY_MS = 350;

	private Game game;
	private final ChessPlayer whitePlayer;
	private final ChessPlayer blackPlayer;
	private final GameViewListener listener;
	private Task<Move> activeBotTask;
	private PauseTransition activeBotPause;
	private boolean botThinking;

	GameController(Game game, ChessPlayer whitePlayer, ChessPlayer blackPlayer, GameViewListener listener) {
		if (game == null) {
			throw new IllegalArgumentException("Game is required");
		}
		if (whitePlayer == null || whitePlayer.getColor() != Color.WHITE) {
			throw new IllegalArgumentException("White player is required");
		}
		if (blackPlayer == null || blackPlayer.getColor() != Color.BLACK) {
			throw new IllegalArgumentException("Black player is required");
		}
		this.game = game;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.listener = listener;
	}

	Game getGame() {
		return game;
	}

	Board getBoardSnapshot() {
		return game.getCopyOfCurrBoard();
	}

	Color getCurrentMove() {
		return game.getCurrMove();
	}

	GameState getCurrentState() {
		return game.getCurrState();
	}

	boolean isBotThinking() {
		return botThinking;
	}

	boolean isHumanTurn() {
		return !game.gameOver() && !game.isPromotionPending() && getCurrentPlayer().isHuman() && !botThinking;
	}

	boolean isPromotionPending() {
		return game.isPromotionPending();
	}

	void pollMove() {
		if (shouldWaitForHumanOrStop()) {
			return;
		}
		if (activeBotTask != null || activeBotPause != null) {
			return;
		}
		setBotThinking(true);
		activeBotPause = new PauseTransition(Duration.millis(BOT_TURN_DELAY_MS));
		activeBotPause.setOnFinished(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				activeBotPause = null;
				startBotTask();
			}
		});
		activeBotPause.play();
	}

	boolean submitHumanMove(int startRank, int startFile, int endRank, int endFile) {
		if (!isHumanTurn()) {
			notifyInvalidMove("It is not a human turn.");
			return false;
		}
		Board beforeMove = game.getCopyOfCurrBoard();
		Move move = new Move(startRank, startFile, endRank, endFile);
		if (!game.move(startRank, startFile, endRank, endFile)) {
			notifyInvalidMove("Invalid move, please try again.");
			return false;
		}
		notifyStateChanged(move, beforeMove);
		if (!game.isPromotionPending()) {
			pollMove();
		}
		return true;
	}

	boolean promote(int rank, int file, PieceType type) {
		if (!game.isPromotionPending()) {
			notifyInvalidMove("No promotion is pending.");
			return false;
		}
		Board beforeMove = game.getCopyOfCurrBoard();
		game.promote(rank, file, type);
		notifyStateChanged(null, beforeMove);
		pollMove();
		return true;
	}

	void restart() {
		cancelBotWork();
		game = new Game();
		notifyStateChanged(null, null);
		pollMove();
	}

	void stop() {
		cancelBotWork();
	}

	void loadFen(String fen) {
		cancelBotWork();
		Game loadedGame = new Game();
		loadedGame.loadFEN(fen);
		game = loadedGame;
		notifyStateChanged(null, null);
		pollMove();
	}

	boolean pollMoveSynchronouslyForTesting() {
		if (shouldWaitForHumanOrStop()) {
			return false;
		}
		Board beforeMove = game.getCopyOfCurrBoard();
		Move move = chooseCurrentBotMove();
		if (move == null) {
			notifyInvalidMove("Bot could not move.");
			return false;
		}
		if (!game.move(move)) {
			notifyInvalidMove("Bot could not move.");
			return false;
		}
		notifyStateChanged(move, beforeMove);
		return true;
	}

	private void startBotTask() {
		if (shouldWaitForHumanOrStop()) {
			setBotThinking(false);
			return;
		}
		final Game scheduledGame = game;
		final ChessPlayer scheduledPlayer = getCurrentPlayer();
		final ArrayList<Move> legalMoves = scheduledGame.getLegalMoves();
		final Board boardSnapshot = scheduledGame.getCopyOfCurrBoard();
		final List<String> positionHistory = scheduledGame.getPositionHistory();
		activeBotTask = new Task<Move>() {
			protected Move call() {
				return scheduledPlayer.chooseMove(legalMoves, boardSnapshot, positionHistory);
			}
		};
		activeBotTask.setOnSucceeded(event -> {
			Move move = activeBotTask.getValue();
			activeBotTask = null;
			applyBotMove(scheduledGame, move);
		});
		activeBotTask.setOnFailed(event -> {
			activeBotTask = null;
			setBotThinking(false);
			notifyInvalidMove("Bot could not move.");
		});
		Thread botThread = new Thread(activeBotTask, "chess-bot-search");
		botThread.setDaemon(true);
		botThread.start();
	}

	private Move chooseCurrentBotMove() {
		ArrayList<Move> legalMoves = game.getLegalMoves();
		Board boardSnapshot = game.getCopyOfCurrBoard();
		return getCurrentPlayer().chooseMove(legalMoves, boardSnapshot, game.getPositionHistory());
	}

	private void applyBotMove(Game scheduledGame, Move move) {
		if (scheduledGame != game) {
			setBotThinking(false);
			return;
		}
		if (move == null) {
			setBotThinking(false);
			notifyInvalidMove("Bot could not move.");
			return;
		}
		Board beforeMove = game.getCopyOfCurrBoard();
		if (!game.move(move)) {
			setBotThinking(false);
			notifyInvalidMove("Bot could not move.");
			return;
		}
		notifyStateChanged(move, beforeMove);
		setBotThinking(false);
		pollMove();
	}

	private boolean shouldWaitForHumanOrStop() {
		return game.gameOver() || game.isPromotionPending() || getCurrentPlayer().isHuman();
	}

	private ChessPlayer getCurrentPlayer() {
		return game.getCurrMove() == Color.WHITE ? whitePlayer : blackPlayer;
	}

	private void cancelBotWork() {
		if (activeBotPause != null) {
			activeBotPause.stop();
			activeBotPause = null;
		}
		if (activeBotTask != null) {
			activeBotTask.cancel();
			activeBotTask = null;
		}
		setBotThinking(false);
	}

	private void setBotThinking(boolean thinking) {
		botThinking = thinking;
		if (listener != null) {
			listener.onBotThinkingChanged(thinking);
		}
	}

	private void notifyStateChanged(Move move, Board beforeMove) {
		if (listener != null) {
			listener.onGameStateChanged(move, beforeMove);
		}
	}

	private void notifyInvalidMove(String message) {
		if (listener != null) {
			listener.onInvalidMove(message);
		}
	}
}
