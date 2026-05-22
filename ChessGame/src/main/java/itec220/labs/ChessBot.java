package itec220.labs;

import java.util.ArrayList;

/**
 * Deterministic one-ply chess bot.
 */
public class ChessBot {
	private final Color color;

	public ChessBot(Color color) {
		if (color == null) {
			throw new IllegalArgumentException("Bot color is required");
		}
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot) {
		if (legalMoves == null || legalMoves.isEmpty() || boardSnapshot == null) {
			return null;
		}
		Move bestMove = null;
		int bestScore = Integer.MIN_VALUE;
		for (Move move : legalMoves) {
			int score = score(move, boardSnapshot);
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
		}
		return bestMove;
	}

	private int score(Move move, Board boardSnapshot) {
		int score = 0;
		if (move.capturedType != null) {
			score += 1000 + pieceValue(move.capturedType);
		}
		if (move.promotionType != null) {
			score += 100 + pieceValue(move.promotionType);
			if (move.promotionType == PieceType.QUEEN) {
				score += 1;
			}
		}
		if (givesCheck(move, boardSnapshot)) {
			score += 10;
		}
		return score;
	}

	private boolean givesCheck(Move move, Board boardSnapshot) {
		Board afterMove = boardSnapshot.simulateMove(move.startRank, move.startFile, move.endRank, move.endFile);
		if (afterMove == null) {
			return false;
		}
		if (move.promotionType != null) {
			afterMove.promote(move.endRank, move.endFile, move.promotionType);
		}
		Color opponent = color == Color.WHITE ? Color.BLACK : Color.WHITE;
		return afterMove.isKingInCheck(opponent);
	}

	private int pieceValue(PieceType type) {
		switch (type) {
		case QUEEN:
			return 9;
		case ROOK:
			return 5;
		case BISHOP:
		case KNIGHT:
			return 3;
		case PAWN:
			return 1;
		case KING:
		default:
			return 0;
		}
	}
}
