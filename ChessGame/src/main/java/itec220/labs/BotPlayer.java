package itec220.labs;

import java.util.ArrayList;
import java.util.List;

/**
 * Bot-controlled side.
 */
final class BotPlayer implements ChessPlayer {
	private final ChessBot bot;

	BotPlayer(Color color) {
		this.bot = new ChessBot(color);
	}

	public Color getColor() {
		return bot.getColor();
	}

	public boolean isHuman() {
		return false;
	}

	public Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot, List<String> priorPositions) {
		return bot.chooseMove(legalMoves, boardSnapshot, priorPositions);
	}
}
