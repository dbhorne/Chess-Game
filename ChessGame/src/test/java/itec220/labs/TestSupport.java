package itec220.labs;

import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;

final class TestSupport {
	private TestSupport() {
	}

	static Game gameFromFen(String fen) {
		Game game = new Game();
		game.loadFEN(fen);
		return game;
	}

	static boolean containsMove(ArrayList<SimpleEntry<Integer, Integer>> moves, int rank, int file) {
		return moves.contains(new SimpleEntry<>(rank, file));
	}

	static boolean containsPromotion(ArrayList<Move> moves, PieceType type) {
		for (Move move : moves) {
			if (move.startRank == 6 && move.startFile == 6 && move.endRank == 7 && move.endFile == 6
					&& move.promotionType == type) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	static LinkedList<String> boardStates(Game game) throws Exception {
		Field field = Game.class.getDeclaredField("boardStates");
		field.setAccessible(true);
		return (LinkedList<String>) field.get(game);
	}

	static final class FirstMoveBot implements ChessPlayer {
		private final Color color;

		FirstMoveBot(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}

		public boolean isHuman() {
			return false;
		}

		public Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot) {
			return legalMoves.isEmpty() ? null : legalMoves.get(0);
		}
	}
}
