package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Depth-limited alpha-beta chess bot.
 */
public class ChessBot {
	private static final int DEFAULT_DEPTH = 4;
	private static final int CHECKMATE_SCORE = 1_000_000;
	private static final int STALEMATE_SCORE = 0;
	private static final int CHECK_BONUS = 25;

	private final Color color;
	private final int depth;
	private final Random random;
	private final int threadCount;

	public ChessBot(Color color) {
		this(color, DEFAULT_DEPTH);
	}

	public ChessBot(Color color, int depth) {
		this(color, depth, new Random(), Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
	}

	ChessBot(Color color, int depth, Random random, int threadCount) {
		if (color == null) {
			throw new IllegalArgumentException("Bot color is required");
		}
		if (depth < 1) {
			throw new IllegalArgumentException("Bot depth must be at least 1");
		}
		if (random == null) {
			throw new IllegalArgumentException("Random is required");
		}
		this.color = color;
		this.depth = depth;
		this.random = random;
		this.threadCount = Math.max(1, threadCount);
	}

	public Color getColor() {
		return color;
	}

	Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot) {
		if (legalMoves == null || legalMoves.isEmpty() || boardSnapshot == null) {
			return null;
		}
		ArrayList<ScoredMove> scoredMoves = threadCount == 1
				? scoreSequentially(legalMoves, boardSnapshot)
				: scoreInParallel(legalMoves, boardSnapshot);
		int bestScore = Integer.MIN_VALUE;
		ArrayList<Move> bestMoves = new ArrayList<>();
		for (ScoredMove scoredMove : scoredMoves) {
			if (scoredMove.score > bestScore) {
				bestScore = scoredMove.score;
				bestMoves.clear();
				bestMoves.add(scoredMove.move);
			} else if (scoredMove.score == bestScore) {
				bestMoves.add(scoredMove.move);
			}
		}
		return bestMoves.get(random.nextInt(bestMoves.size()));
	}

	private ArrayList<ScoredMove> scoreSequentially(ArrayList<Move> legalMoves, Board boardSnapshot) {
		ArrayList<ScoredMove> scoredMoves = new ArrayList<>();
		for (Move move : legalMoves) {
			scoredMoves.add(scoreRootMove(move, boardSnapshot));
		}
		return scoredMoves;
	}

	private ArrayList<ScoredMove> scoreInParallel(ArrayList<Move> legalMoves, final Board boardSnapshot) {
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadCount, legalMoves.size()));
		ArrayList<Future<ScoredMove>> futures = new ArrayList<>();
		for (final Move move : legalMoves) {
			futures.add(executor.submit(new Callable<ScoredMove>() {
				public ScoredMove call() {
					return scoreRootMove(move, boardSnapshot);
				}
			}));
		}
		ArrayList<ScoredMove> scoredMoves = new ArrayList<>();
		try {
			for (Future<ScoredMove> future : futures) {
				scoredMoves.add(future.get());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return scoreSequentially(legalMoves, boardSnapshot);
		} catch (ExecutionException e) {
			return scoreSequentially(legalMoves, boardSnapshot);
		} finally {
			executor.shutdownNow();
		}
		return scoredMoves;
	}

	private ScoredMove scoreRootMove(Move move, Board boardSnapshot) {
		Board afterMove = applyMove(boardSnapshot, move);
		int score = afterMove == null ? Integer.MIN_VALUE
				: minimax(afterMove, depth - 1, opponent(color), Integer.MIN_VALUE, Integer.MAX_VALUE);
		if (score != Integer.MIN_VALUE && Math.abs(score) < CHECKMATE_SCORE) {
			score += immediateMoveScore(move);
			score -= immediateRecapturePenalty(boardSnapshot, afterMove, move);
		}
		return new ScoredMove(move, score);
	}

	private int minimax(Board board, int remainingDepth, Color sideToMove, int alpha, int beta) {
		ArrayList<Move> moves = generateLegalMoves(board, sideToMove);
		if (moves.isEmpty()) {
			return terminalScore(board, sideToMove);
		}
		if (remainingDepth == 0) {
			return evaluate(board, sideToMove);
		}
		if (sideToMove == color) {
			int bestScore = Integer.MIN_VALUE;
			for (Move move : moves) {
				Board afterMove = applyMove(board, move);
				if (afterMove == null) {
					continue;
				}
				bestScore = Math.max(bestScore,
						minimax(afterMove, remainingDepth - 1, opponent(sideToMove), alpha, beta));
				alpha = Math.max(alpha, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
			return bestScore;
		}
		int bestScore = Integer.MAX_VALUE;
		for (Move move : moves) {
			Board afterMove = applyMove(board, move);
			if (afterMove == null) {
				continue;
			}
			bestScore = Math.min(bestScore,
					minimax(afterMove, remainingDepth - 1, opponent(sideToMove), alpha, beta));
			beta = Math.min(beta, bestScore);
			if (beta <= alpha) {
				break;
			}
		}
		return bestScore;
	}

	private int terminalScore(Board board, Color sideToMove) {
		if (board.isKingInCheck(sideToMove)) {
			return sideToMove == color ? -CHECKMATE_SCORE : CHECKMATE_SCORE;
		}
		return STALEMATE_SCORE;
	}

	private int evaluate(Board board, Color sideToMove) {
		int score = 0;
		Piece[][] pieces = board.getPieces();
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece == null) {
					continue;
				}
				int value = pieceValue(piece.getType());
				score += piece.getColor() == color ? value : -value;
			}
		}
		Color opponent = opponent(color);
		if (board.isKingInCheck(opponent)) {
			score += CHECK_BONUS;
		}
		if (board.isKingInCheck(color)) {
			score -= CHECK_BONUS;
		}
		ArrayList<Move> moves = generateLegalMoves(board, sideToMove);
		if (moves.isEmpty()) {
			score += terminalScore(board, sideToMove);
		}
		return score;
	}

	private ArrayList<Move> generateLegalMoves(Board board, Color sideToMove) {
		ArrayList<Move> moves = new ArrayList<>();
		Piece[][] pieces = board.getPieces();
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece == null || piece.getColor() != sideToMove) {
					continue;
				}
				for (SimpleEntry<Integer, Integer> destination : piece.getValidMoves(board, false)) {
					addMoveOptions(moves, board, piece, destination.getKey(), destination.getValue());
				}
			}
		}
		return moves;
	}

	private void addMoveOptions(ArrayList<Move> legalMoves, Board board, Piece piece, int endRank, int endFile) {
		Piece capturedPiece = board.getPiece(endRank, endFile);
		boolean enPassantCapture = piece instanceof Pawn && piece.getFile() != endFile && capturedPiece == null;
		PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();
		if (enPassantCapture) {
			SimpleEntry<Integer, Integer> enPassant = board.getEnPassant();
			if (enPassant != null && board.getPiece(enPassant.getKey(), enPassant.getValue()) != null) {
				capturedType = board.getPiece(enPassant.getKey(), enPassant.getValue()).getType();
			}
		}
		boolean castle = piece instanceof King && Math.abs(endFile - piece.getFile()) == 2;
		if (isPromotionMove(piece, endRank)) {
			for (PieceType promotionType : promotionTypes()) {
				legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, promotionType,
						capturedType, enPassantCapture, castle));
			}
		} else {
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, null,
					capturedType, enPassantCapture, castle));
		}
	}

	private Board applyMove(Board board, Move move) {
		Board afterMove = board.simulateMove(move.startRank, move.startFile, move.endRank, move.endFile);
		if (afterMove != null && move.promotionType != null) {
			afterMove.promote(move.endRank, move.endFile, move.promotionType);
		}
		return afterMove;
	}

	private boolean isPromotionMove(Piece piece, int endRank) {
		return piece instanceof Pawn
				&& ((piece.getColor() == Color.WHITE && endRank == 7)
				|| (piece.getColor() == Color.BLACK && endRank == 0));
	}

	private List<PieceType> promotionTypes() {
		ArrayList<PieceType> types = new ArrayList<>();
		types.add(PieceType.QUEEN);
		types.add(PieceType.ROOK);
		types.add(PieceType.BISHOP);
		types.add(PieceType.KNIGHT);
		return types;
	}

	private Color opponent(Color side) {
		return side == Color.WHITE ? Color.BLACK : Color.WHITE;
	}

	private int pieceValue(PieceType type) {
		switch (type) {
		case QUEEN:
			return 900;
		case ROOK:
			return 500;
		case BISHOP:
		case KNIGHT:
			return 300;
		case PAWN:
			return 100;
		case KING:
		default:
			return 0;
		}
	}

	private int immediateMoveScore(Move move) {
		int score = 0;
		if (move.capturedType != null) {
			score += pieceValue(move.capturedType);
		}
		if (move.promotionType != null) {
			score += pieceValue(move.promotionType);
			if (move.promotionType == PieceType.QUEEN) {
				score += 1;
			}
		}
		return score;
	}

	private int immediateRecapturePenalty(Board beforeMove, Board afterMove, Move move) {
		if (afterMove == null) {
			return 0;
		}
		Piece movedPiece = beforeMove.getPiece(move.startRank, move.startFile);
		if (movedPiece == null) {
			return 0;
		}
		PieceType movedType = move.promotionType == null ? movedPiece.getType() : move.promotionType;
		for (Move opponentMove : generateLegalMoves(afterMove, opponent(color))) {
			if (opponentMove.endRank == move.endRank && opponentMove.endFile == move.endFile
					&& opponentMove.capturedType == movedType) {
				return pieceValue(movedType);
			}
		}
		return 0;
	}

	private static final class ScoredMove {
		private final Move move;
		private final int score;

		private ScoredMove(Move move, int score) {
			this.move = move;
			this.score = score;
		}
	}
}
