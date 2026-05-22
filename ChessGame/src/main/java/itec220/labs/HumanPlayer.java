package itec220.labs;

import java.util.ArrayList;

/**
 * Human-controlled side. The GUI supplies moves through board input.
 */
final class HumanPlayer implements ChessPlayer {
	private final Color color;

	HumanPlayer(Color color) {
		if (color == null) {
			throw new IllegalArgumentException("Player color is required");
		}
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public boolean isHuman() {
		return true;
	}

	public Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot) {
		return null;
	}
}
