package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Depth-limited alpha-beta chess bot with tactical leaf search and static heuristics.
 */
public class ChessBot {
	private static final int DEFAULT_DEPTH = 2;
	private static final int CHECKMATE_SCORE = 1_000_000;
	private static final int STALEMATE_SCORE = 0;
	private static final int STALEMATE_WHILE_AHEAD_SCORE = -200_000;
	private static final int STALEMATE_WHILE_BEHIND_SCORE = 200_000;
	private static final int WINNING_MATERIAL_MARGIN = 300;
	private static final int ENDGAME_MAX_DEPTH = 7;
	private static final int CHECK_BONUS = 35;
	private static final int MAX_QUIESCENCE_DEPTH = 4;
	private static final int TT_EXACT = 0;
	private static final int TT_LOWER_BOUND = 1;
	private static final int TT_UPPER_BOUND = 2;

	private static final int[][] PAWN_TABLE = {
		{ 0, 0, 0, 0, 0, 0, 0, 0 },
		{ 5, 10, 10, -15, -15, 10, 10, 5 },
		{ 3, 4, 8, 18, 18, 8, 4, 3 },
		{ 2, 3, 6, 16, 16, 6, 3, 2 },
		{ 2, 3, 5, 14, 14, 5, 3, 2 },
		{ 4, 6, 8, 10, 10, 8, 6, 4 },
		{ 12, 14, 14, 16, 16, 14, 14, 12 },
		{ 0, 0, 0, 0, 0, 0, 0, 0 }
	};
	private static final int[][] KNIGHT_TABLE = {
		{ -50, -35, -25, -20, -20, -25, -35, -50 },
		{ -30, -10, 5, 8, 8, 5, -10, -30 },
		{ -20, 8, 18, 24, 24, 18, 8, -20 },
		{ -18, 10, 24, 32, 32, 24, 10, -18 },
		{ -18, 10, 24, 32, 32, 24, 10, -18 },
		{ -20, 8, 18, 24, 24, 18, 8, -20 },
		{ -30, -10, 5, 8, 8, 5, -10, -30 },
		{ -50, -35, -25, -20, -20, -25, -35, -50 }
	};
	private static final int[][] BISHOP_TABLE = {
		{ -20, -10, -10, -10, -10, -10, -10, -20 },
		{ -10, 8, 6, 6, 6, 6, 8, -10 },
		{ -10, 8, 12, 14, 14, 12, 8, -10 },
		{ -10, 6, 14, 18, 18, 14, 6, -10 },
		{ -10, 6, 14, 18, 18, 14, 6, -10 },
		{ -10, 8, 12, 14, 14, 12, 8, -10 },
		{ -10, 8, 6, 6, 6, 6, 8, -10 },
		{ -20, -10, -10, -10, -10, -10, -10, -20 }
	};
	private static final int[][] ROOK_TABLE = {
		{ 0, 0, 4, 8, 8, 4, 0, 0 },
		{ -4, 0, 0, 2, 2, 0, 0, -4 },
		{ -4, 0, 0, 2, 2, 0, 0, -4 },
		{ -4, 0, 0, 2, 2, 0, 0, -4 },
		{ -4, 0, 0, 2, 2, 0, 0, -4 },
		{ -2, 0, 0, 2, 2, 0, 0, -2 },
		{ 8, 12, 12, 14, 14, 12, 12, 8 },
		{ 0, 0, 4, 8, 8, 4, 0, 0 }
	};
	private static final int[][] QUEEN_TABLE = {
		{ -20, -10, -10, -5, -5, -10, -10, -20 },
		{ -10, 0, 4, 4, 4, 4, 0, -10 },
		{ -10, 4, 8, 8, 8, 8, 4, -10 },
		{ -5, 4, 8, 12, 12, 8, 4, -5 },
		{ -5, 4, 8, 12, 12, 8, 4, -5 },
		{ -10, 4, 8, 8, 8, 8, 4, -10 },
		{ -10, 0, 4, 4, 4, 4, 0, -10 },
		{ -20, -10, -10, -5, -5, -10, -10, -20 }
	};
	private static final int[][] KING_MIDDLEGAME_TABLE = {
		{ 28, 35, 14, 0, 0, 14, 35, 28 },
		{ 18, 20, 0, -8, -8, 0, 20, 18 },
		{ -12, -20, -24, -30, -30, -24, -20, -12 },
		{ -30, -35, -40, -45, -45, -40, -35, -30 },
		{ -35, -40, -45, -50, -50, -45, -40, -35 },
		{ -40, -45, -50, -55, -55, -50, -45, -40 },
		{ -45, -50, -55, -60, -60, -55, -50, -45 },
		{ -50, -55, -60, -65, -65, -60, -55, -50 }
	};
	private static final int[][] KING_ENDGAME_TABLE = {
		{ -45, -30, -20, -15, -15, -20, -30, -45 },
		{ -25, -10, 0, 5, 5, 0, -10, -25 },
		{ -15, 0, 15, 18, 18, 15, 0, -15 },
		{ -10, 5, 18, 25, 25, 18, 5, -10 },
		{ -10, 5, 18, 25, 25, 18, 5, -10 },
		{ -15, 0, 15, 18, 18, 15, 0, -15 },
		{ -25, -10, 0, 5, 5, 0, -10, -25 },
		{ -45, -30, -20, -15, -15, -20, -30, -45 }
	};

	private final Color color;
	private final int depth;
	private final Random random;
	private final int threadCount;
	private final Map<String, TranspositionEntry> transpositionTable = new HashMap<>();

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

	Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot, List<String> priorPositions) {
		if (legalMoves == null || legalMoves.isEmpty() || boardSnapshot == null) {
			return null;
		}
		transpositionTable.clear();
		ArrayList<Move> orderedMoves = new ArrayList<>(legalMoves);
		orderMoves(boardSnapshot, orderedMoves, color, null);
		ArrayList<Move> immediateMates = immediateMatingMoves(orderedMoves, boardSnapshot);
		if (!immediateMates.isEmpty()) {
			return immediateMates.get(random.nextInt(immediateMates.size()));
		}
		int effectiveDepth = effectiveDepth(boardSnapshot, legalMoves.size());
		ArrayList<ScoredMove> scoredMoves = scoreSequentially(orderedMoves, boardSnapshot, effectiveDepth, priorPositions);
		int bestScore = Integer.MIN_VALUE;
		for (ScoredMove scoredMove : scoredMoves) {
			if (scoredMove.score > bestScore) {
				bestScore = scoredMove.score;
			}
		}
		int tolerance = Math.abs(bestScore) < CHECKMATE_SCORE / 2 ? 12 : 0;
		ArrayList<Move> candidates = new ArrayList<>();
		for (ScoredMove scoredMove : scoredMoves) {
			if (scoredMove.score >= bestScore - tolerance) {
				candidates.add(scoredMove.move);
			}
		}
		return candidates.get(random.nextInt(candidates.size()));
	}

	private ArrayList<Move> immediateMatingMoves(ArrayList<Move> legalMoves, Board boardSnapshot) {
		ArrayList<Move> mates = new ArrayList<>();
		Color enemy = opponent(color);
		for (Move move : legalMoves) {
			Board afterMove = applyMove(boardSnapshot, move);
			if (afterMove != null && afterMove.isKingInCheck(enemy)
					&& generateLegalMoves(afterMove, enemy).isEmpty()) {
				mates.add(move);
			}
		}
		return mates;
	}

	private ArrayList<ScoredMove> scoreSequentially(ArrayList<Move> legalMoves, Board boardSnapshot,
			int effectiveDepth, List<String> priorPositions) {
		ArrayList<ScoredMove> scoredMoves = new ArrayList<>();
		for (Move move : legalMoves) {
			scoredMoves.add(scoreRootMove(move, boardSnapshot, effectiveDepth, priorPositions));
		}
		return scoredMoves;
	}

	private ScoredMove scoreRootMove(Move move, Board boardSnapshot, int effectiveDepth,
			List<String> priorPositions) {
		Board afterMove = applyMove(boardSnapshot, move);
		int score = afterMove == null ? Integer.MIN_VALUE
				: minimax(afterMove, effectiveDepth - 1, opponent(color), Integer.MIN_VALUE + 1,
						Integer.MAX_VALUE - 1, 1);
		if (score != Integer.MIN_VALUE && Math.abs(score) < CHECKMATE_SCORE) {
			score += moveOrderingScore(boardSnapshot, move, color) / 100;
			score -= immediateRecapturePenalty(boardSnapshot, afterMove, move);
			String posKey = transpositionKey(afterMove, opponent(color));
			int repeats = Collections.frequency(priorPositions, posKey);
			if (repeats >= 2) {
				score -= 500_000;
			} else if (repeats >= 1) {
				score -= 20_000;
			}
		}
		return new ScoredMove(move, score);
	}

	private int effectiveDepth(Board board, int legalMoveCount) {
		int pieceCount = totalPieceCount(board);
		int dynamicDepth = depth;
		if (pieceCount <= 3) {
			dynamicDepth = 7;
		} else if (pieceCount <= 5) {
			dynamicDepth = 6;
		} else if (pieceCount <= 8) {
			dynamicDepth = 5;
		} else if (pieceCount <= 12) {
			dynamicDepth = 4;
		}
		if (legalMoveCount > 35) {
			dynamicDepth = Math.min(dynamicDepth, 5);
		}
		return Math.max(depth, Math.min(ENDGAME_MAX_DEPTH, dynamicDepth));
	}

	int effectiveDepthForTesting(Board board, int legalMoveCount) {
		return effectiveDepth(board, legalMoveCount);
	}

	private int minimax(Board board, int remainingDepth, Color sideToMove, int alpha, int beta, int plyFromRoot) {
		ArrayList<Move> moves = generateLegalMoves(board, sideToMove);
		if (moves.isEmpty()) {
			return terminalScore(board, sideToMove, plyFromRoot);
		}
		if (remainingDepth == 0) {
			return quiescence(board, sideToMove, alpha, beta, MAX_QUIESCENCE_DEPTH, plyFromRoot);
		}

		int originalAlpha = alpha;
		int originalBeta = beta;
		String key = transpositionKey(board, sideToMove) + " " + plyFromRoot;
		TranspositionEntry entry = transpositionTable.get(key);
		Move tableMove = null;
		if (entry != null) {
			tableMove = entry.bestMove;
			if (entry.depth >= remainingDepth) {
				if (entry.flag == TT_EXACT) {
					return entry.score;
				}
				if (entry.flag == TT_LOWER_BOUND) {
					alpha = Math.max(alpha, entry.score);
				} else if (entry.flag == TT_UPPER_BOUND) {
					beta = Math.min(beta, entry.score);
				}
				if (alpha >= beta) {
					return entry.score;
				}
			}
		}

		orderMoves(board, moves, sideToMove, tableMove);
		Move bestMove = null;
		int bestScore;
		if (sideToMove == color) {
			bestScore = Integer.MIN_VALUE + 1;
			for (Move move : moves) {
				Board afterMove = applyMove(board, move);
				if (afterMove == null) {
					continue;
				}
				int score = minimax(afterMove, remainingDepth - 1, opponent(sideToMove), alpha, beta,
						plyFromRoot + 1);
				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
				alpha = Math.max(alpha, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
		} else {
			bestScore = Integer.MAX_VALUE - 1;
			for (Move move : moves) {
				Board afterMove = applyMove(board, move);
				if (afterMove == null) {
					continue;
				}
				int score = minimax(afterMove, remainingDepth - 1, opponent(sideToMove), alpha, beta,
						plyFromRoot + 1);
				if (score < bestScore) {
					bestScore = score;
					bestMove = move;
				}
				beta = Math.min(beta, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
		}
		storeTransposition(key, remainingDepth, bestScore, originalAlpha, originalBeta, bestMove);
		return bestScore;
	}

	private int quiescence(Board board, Color sideToMove, int alpha, int beta, int remainingCapturesDepth,
			int plyFromRoot) {
		ArrayList<Move> allMoves = generateLegalMoves(board, sideToMove);
		if (allMoves.isEmpty()) {
			return terminalScore(board, sideToMove, plyFromRoot);
		}
		int standPat = evaluate(board, sideToMove);
		if (remainingCapturesDepth == 0) {
			return standPat;
		}
		if (sideToMove == color) {
			if (standPat >= beta) {
				return beta;
			}
			alpha = Math.max(alpha, standPat);
		} else {
			if (standPat <= alpha) {
				return alpha;
			}
			beta = Math.min(beta, standPat);
		}

		ArrayList<Move> moves = tacticalMoves(allMoves);
		orderMoves(board, moves, sideToMove, null);
		if (sideToMove == color) {
			int bestScore = standPat;
			for (Move move : moves) {
				Board afterMove = applyMove(board, move);
				if (afterMove == null) {
					continue;
				}
				bestScore = Math.max(bestScore,
						quiescence(afterMove, opponent(sideToMove), alpha, beta, remainingCapturesDepth - 1,
								plyFromRoot + 1));
				alpha = Math.max(alpha, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
			return bestScore;
		}
		int bestScore = standPat;
		for (Move move : moves) {
			Board afterMove = applyMove(board, move);
			if (afterMove == null) {
				continue;
			}
			bestScore = Math.min(bestScore,
					quiescence(afterMove, opponent(sideToMove), alpha, beta, remainingCapturesDepth - 1,
							plyFromRoot + 1));
			beta = Math.min(beta, bestScore);
			if (beta <= alpha) {
				break;
			}
		}
		return bestScore;
	}

	private ArrayList<Move> tacticalMoves(ArrayList<Move> moves) {
		ArrayList<Move> tacticalMoves = new ArrayList<>();
		for (Move move : moves) {
			if (move.capturedType != null || move.promotionType != null) {
				tacticalMoves.add(move);
			}
		}
		return tacticalMoves;
	}

	private void storeTransposition(String key, int remainingDepth, int score, int originalAlpha, int originalBeta,
			Move bestMove) {
		int flag = TT_EXACT;
		if (score <= originalAlpha) {
			flag = TT_UPPER_BOUND;
		} else if (score >= originalBeta) {
			flag = TT_LOWER_BOUND;
		}
		transpositionTable.put(key, new TranspositionEntry(remainingDepth, score, flag, bestMove));
	}

	private int terminalScore(Board board, Color sideToMove, int plyFromRoot) {
		if (board.isKingInCheck(sideToMove)) {
			return sideToMove == color ? -CHECKMATE_SCORE + plyFromRoot : CHECKMATE_SCORE - plyFromRoot;
		}
		int balance = materialBalance(board);
		if (balance > WINNING_MATERIAL_MARGIN) {
			return STALEMATE_WHILE_AHEAD_SCORE;
		}
		if (balance < -WINNING_MATERIAL_MARGIN) {
			return STALEMATE_WHILE_BEHIND_SCORE;
		}
		return STALEMATE_SCORE;
	}

	private int evaluate(Board board, Color sideToMove) {
		int score = 0;
		Piece[][] pieces = board.getPieces();
		int[] whitePawnsByFile = new int[8];
		int[] blackPawnsByFile = new int[8];
		int whiteBishops = 0;
		int blackBishops = 0;
		int phaseMaterial = 0;

		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece == null) {
					continue;
				}
				int side = piece.getColor() == color ? 1 : -1;
				score += side * (pieceValue(piece.getType()) + pieceSquareValue(piece, rank, file, pieces));
				score += side * centerControlValue(piece, rank, file);
				score += side * developmentValue(piece);
				score += side * promotionProgressValue(piece);
				if (piece.getType() == PieceType.BISHOP) {
					if (piece.getColor() == Color.WHITE) {
						whiteBishops++;
					} else {
						blackBishops++;
					}
				}
				if (piece.getType() == PieceType.PAWN) {
					if (piece.getColor() == Color.WHITE) {
						whitePawnsByFile[file]++;
					} else {
						blackPawnsByFile[file]++;
					}
				} else if (piece.getType() != PieceType.KING) {
					phaseMaterial += pieceValue(piece.getType());
				}
			}
		}

		score += pawnStructureScore(whitePawnsByFile, blackPawnsByFile);
		score += passedPawnScore(pieces);
		score += bishopPairScore(whiteBishops, blackBishops);
		score += castlingAndKingSafetyScore(board, pieces, phaseMaterial);
		score += mobilityScore(board);
		score += endgameMatingScore(board, pieces);

		Color opponent = opponent(color);
		if (board.isKingInCheck(opponent)) {
			score += CHECK_BONUS;
		}
		if (board.isKingInCheck(color)) {
			score -= CHECK_BONUS;
		}
		ArrayList<Move> moves = generateLegalMoves(board, sideToMove);
		if (moves.isEmpty()) {
			score += terminalScore(board, sideToMove, 0);
		}
		return score;
	}

	int evaluateForTesting(Board board, Color sideToMove) {
		return evaluate(board, sideToMove);
	}

	private int pieceSquareValue(Piece piece, int rank, int file, Piece[][] pieces) {
		int tableRank = piece.getColor() == Color.WHITE ? rank : 7 - rank;
		switch (piece.getType()) {
		case PAWN:
			return PAWN_TABLE[tableRank][file];
		case KNIGHT:
			return KNIGHT_TABLE[tableRank][file];
		case BISHOP:
			return BISHOP_TABLE[tableRank][file];
		case ROOK:
			return ROOK_TABLE[tableRank][file];
		case QUEEN:
			return QUEEN_TABLE[tableRank][file];
		case KING:
			int phaseMaterial = totalNonPawnMaterial(pieces);
			int middleGame = KING_MIDDLEGAME_TABLE[tableRank][file];
			int endGame = KING_ENDGAME_TABLE[tableRank][file];
			int phase = Math.min(100, phaseMaterial * 100 / 6400);
			return (middleGame * phase + endGame * (100 - phase)) / 100;
		default:
			return 0;
		}
	}

	private int centerControlValue(Piece piece, int rank, int file) {
		int rankDistance = Math.abs(rank - 3) + Math.abs(rank - 4);
		int fileDistance = Math.abs(file - 3) + Math.abs(file - 4);
		int centerScore = 12 - (rankDistance + fileDistance);
		if (piece.getType() == PieceType.PAWN) {
			return centerScore;
		}
		if (piece.getType() == PieceType.KNIGHT || piece.getType() == PieceType.BISHOP) {
			return centerScore * 2;
		}
		return Math.max(0, centerScore / 2);
	}

	private int developmentValue(Piece piece) {
		if (piece.getType() == PieceType.KNIGHT || piece.getType() == PieceType.BISHOP) {
			int homeRank = piece.getColor() == Color.WHITE ? 0 : 7;
			return piece.getRank() == homeRank ? -8 : 8;
		}
		if (piece.getType() == PieceType.QUEEN) {
			int homeRank = piece.getColor() == Color.WHITE ? 0 : 7;
			if (piece.getRank() != homeRank || piece.getFile() != 3) {
				return -16;
			}
		}
		if (piece.getType() == PieceType.PAWN) {
			int homeRank = piece.getColor() == Color.WHITE ? 1 : 6;
			int file = piece.getFile();
			if ((file == 3 || file == 4) && piece.getRank() != homeRank) {
				return 45;
			}
		}
		return 0;
	}

	private int promotionProgressValue(Piece piece) {
		if (piece.getType() != PieceType.PAWN) {
			return 0;
		}
		int progress = piece.getColor() == Color.WHITE ? piece.getRank() : 7 - piece.getRank();
		return progress * progress * 2;
	}

	private int pawnStructureScore(int[] whitePawnsByFile, int[] blackPawnsByFile) {
		return pawnStructureFor(Color.WHITE, whitePawnsByFile) - pawnStructureFor(Color.BLACK, blackPawnsByFile);
	}

	private int pawnStructureFor(Color side, int[] pawnsByFile) {
		int score = 0;
		for (int file = 0; file < pawnsByFile.length; file++) {
			if (pawnsByFile[file] > 1) {
				score -= (pawnsByFile[file] - 1) * 22;
			}
			if (pawnsByFile[file] > 0) {
				boolean leftPawn = file > 0 && pawnsByFile[file - 1] > 0;
				boolean rightPawn = file < 7 && pawnsByFile[file + 1] > 0;
				if (!leftPawn && !rightPawn) {
					score -= 14 * pawnsByFile[file];
				}
			}
		}
		return side == color ? score : -score;
	}

	private int passedPawnScore(Piece[][] pieces) {
		int score = 0;
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (!(piece instanceof Pawn)) {
					continue;
				}
				if (isPassedPawn(pieces, piece)) {
					int progress = piece.getColor() == Color.WHITE ? rank : 7 - rank;
					int bonus = 18 + progress * progress * 3;
					score += piece.getColor() == color ? bonus : -bonus;
				}
			}
		}
		return score;
	}

	private boolean isPassedPawn(Piece[][] pieces, Piece pawn) {
		int direction = pawn.getColor() == Color.WHITE ? 1 : -1;
		Color enemy = opponent(pawn.getColor());
		for (int file = Math.max(0, pawn.getFile() - 1); file <= Math.min(7, pawn.getFile() + 1); file++) {
			for (int rank = pawn.getRank() + direction; rank >= 0 && rank < 8; rank += direction) {
				Piece piece = pieces[rank][file];
				if (piece instanceof Pawn && piece.getColor() == enemy) {
					return false;
				}
			}
		}
		return true;
	}

	private int bishopPairScore(int whiteBishops, int blackBishops) {
		int score = 0;
		if (whiteBishops >= 2) {
			score += color == Color.WHITE ? 35 : -35;
		}
		if (blackBishops >= 2) {
			score += color == Color.BLACK ? 35 : -35;
		}
		return score;
	}

	private int castlingAndKingSafetyScore(Board board, Piece[][] pieces, int phaseMaterial) {
		int phase = Math.min(100, phaseMaterial * 100 / 6400);
		int score = kingSafetyFor(board, pieces, Color.WHITE, phase) - kingSafetyFor(board, pieces, Color.BLACK, phase);
		return color == Color.WHITE ? score : -score;
	}

	private int kingSafetyFor(Board board, Piece[][] pieces, Color side, int phase) {
		King king = board.getKing(side);
		if (king == null) {
			return -CHECKMATE_SCORE / 2;
		}
		int score = 0;
		int homeRank = side == Color.WHITE ? 0 : 7;
		if (king.getRank() == homeRank && (king.getFile() == 6 || king.getFile() == 2)) {
			score += 45;
		} else if (king.getRank() != homeRank || king.getFile() != 4) {
			score -= 10;
		}
		int shieldRank = side == Color.WHITE ? king.getRank() + 1 : king.getRank() - 1;
		if (shieldRank >= 0 && shieldRank < 8) {
			for (int file = Math.max(0, king.getFile() - 1); file <= Math.min(7, king.getFile() + 1); file++) {
				Piece shield = pieces[shieldRank][file];
				if (shield instanceof Pawn && shield.getColor() == side) {
					score += 10;
				} else {
					score -= 8;
				}
			}
		}
		return score * phase / 100;
	}

	private int mobilityScore(Board board) {
		int ownMoves = generateLegalMoves(board, color).size();
		int enemyMoves = generateLegalMoves(board, opponent(color)).size();
		return (ownMoves - enemyMoves) * 3;
	}

	private int endgameMatingScore(Board board, Piece[][] pieces) {
		int ownMaterial = nonKingMaterial(pieces, color);
		int enemyMaterial = nonKingMaterial(pieces, opponent(color));
		int balance = ownMaterial - enemyMaterial;
		if (Math.abs(balance) <= WINNING_MATERIAL_MARGIN || Math.max(ownMaterial, enemyMaterial) < 500
				|| Math.min(ownMaterial, enemyMaterial) > 100) {
			return 0;
		}

		Color winningSide = balance > 0 ? color : opponent(color);
		Color losingSide = opponent(winningSide);
		King winningKing = board.getKing(winningSide);
		King losingKing = board.getKing(losingSide);
		if (winningKing == null || losingKing == null) {
			return 0;
		}

		int losingKingRank = losingKing.getRank();
		int losingKingFile = losingKing.getFile();
		int edgeDistance = Math.min(Math.min(losingKingRank, 7 - losingKingRank),
				Math.min(losingKingFile, 7 - losingKingFile));
		int nearestCornerDistance = Math.min(
				Math.min(losingKingRank + losingKingFile, losingKingRank + (7 - losingKingFile)),
				Math.min((7 - losingKingRank) + losingKingFile, (7 - losingKingRank) + (7 - losingKingFile)));
		int kingDistance = Math.abs(winningKing.getRank() - losingKingRank)
				+ Math.abs(winningKing.getFile() - losingKingFile);
		int score = (3 - edgeDistance) * 45 + (6 - nearestCornerDistance) * 10 + (14 - kingDistance) * 12;

		ArrayList<Move> losingMoves = generateLegalMoves(board, losingSide);
		if (winningSide == color && losingMoves.size() <= 2 && !board.isKingInCheck(losingSide)) {
			score -= (3 - losingMoves.size()) * 70;
		}
		return winningSide == color ? score : -score;
	}

	private int nonKingMaterial(Piece[][] pieces, Color side) {
		int material = 0;
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece != null && piece.getColor() == side && piece.getType() != PieceType.KING) {
					material += pieceValue(piece.getType());
				}
			}
		}
		return material;
	}

	private int materialBalance(Board board) {
		Piece[][] pieces = board.getPieces();
		return nonKingMaterial(pieces, color) - nonKingMaterial(pieces, opponent(color));
	}

	private int totalPieceCount(Board board) {
		int count = 0;
		Piece[][] pieces = board.getPieces();
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				if (pieces[rank][file] != null) {
					count++;
				}
			}
		}
		return count;
	}

	private int totalNonPawnMaterial(Piece[][] pieces) {
		int material = 0;
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece != null && piece.getType() != PieceType.PAWN && piece.getType() != PieceType.KING) {
					material += pieceValue(piece.getType());
				}
			}
		}
		return material;
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

	private void orderMoves(final Board board, ArrayList<Move> moves, final Color sideToMove, final Move tableMove) {
		ArrayList<OrderedMove> orderedMoves = new ArrayList<>();
		for (Move move : moves) {
			int score = moveOrderingScore(board, move, sideToMove);
			if (sameMove(move, tableMove)) {
				score += 2_000_000;
			}
			orderedMoves.add(new OrderedMove(move, score));
		}
		Collections.sort(orderedMoves, new Comparator<OrderedMove>() {
			public int compare(OrderedMove first, OrderedMove second) {
				return Integer.compare(second.score, first.score);
			}
		});
		moves.clear();
		for (OrderedMove orderedMove : orderedMoves) {
			moves.add(orderedMove.move);
		}
	}

	private int moveOrderingScore(Board board, Move move, Color sideToMove) {
		int score = 0;
		Board afterMove = applyMove(board, move);
		if (afterMove != null) {
			ArrayList<Move> replies = generateLegalMoves(afterMove, opponent(sideToMove));
			if (replies.isEmpty() && afterMove.isKingInCheck(opponent(sideToMove))) {
				score += 1_000_000;
			} else if (afterMove.isKingInCheck(opponent(sideToMove))) {
				score += 25_000;
			} else if (sideToMove == color && replies.isEmpty() && materialBalance(board) > WINNING_MATERIAL_MARGIN) {
				score -= 500_000;
			}
		}
		if (move.capturedType != null) {
			Piece attacker = board.getPiece(move.startRank, move.startFile);
			int attackerValue = attacker == null ? 0 : pieceValue(attacker.getType());
			score += 100_000 + pieceValue(move.capturedType) * 10 - attackerValue;
		}
		if (move.promotionType != null) {
			score += 80_000 + pieceValue(move.promotionType);
		}
		score += centerMoveScore(move);
		Piece movingPiece = board.getPiece(move.startRank, move.startFile);
		if (movingPiece != null
				&& (movingPiece.getType() == PieceType.KNIGHT || movingPiece.getType() == PieceType.BISHOP)
				&& move.startRank == (movingPiece.getColor() == Color.WHITE ? 0 : 7)) {
			score += 250;
		}
		if (movingPiece != null && movingPiece.getType() == PieceType.QUEEN
				&& (move.startRank == 0 || move.startRank == 7)) {
			score -= 180;
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

	private int centerMoveScore(Move move) {
		return 40 - (Math.abs(move.endRank - 3) + Math.abs(move.endRank - 4)
				+ Math.abs(move.endFile - 3) + Math.abs(move.endFile - 4)) * 5;
	}

	private boolean sameMove(Move first, Move second) {
		return first != null && second != null
				&& first.startRank == second.startRank
				&& first.startFile == second.startFile
				&& first.endRank == second.endRank
				&& first.endFile == second.endFile
				&& first.promotionType == second.promotionType;
	}

	private String transpositionKey(Board board, Color sideToMove) {
		return board.toFENPiecePlacement() + " " + (sideToMove == Color.WHITE ? "w" : "b") + " "
				+ castlingAvailability(board) + " " + enPassantTarget(board);
	}

	private String castlingAvailability(Board board) {
		StringBuilder castling = new StringBuilder();
		Piece[][] pieces = board.getPieces();
		if (canCastle(pieces, 0, 4, 0, 7)) {
			castling.append('K');
		}
		if (canCastle(pieces, 0, 4, 0, 0)) {
			castling.append('Q');
		}
		if (canCastle(pieces, 7, 4, 7, 7)) {
			castling.append('k');
		}
		if (canCastle(pieces, 7, 4, 7, 0)) {
			castling.append('q');
		}
		return castling.length() == 0 ? "-" : castling.toString();
	}

	private boolean canCastle(Piece[][] pieces, int kingRow, int kingCol, int rookRow, int rookCol) {
		Piece king = pieces[kingRow][kingCol];
		Piece rook = pieces[rookRow][rookCol];
		if (!(king instanceof King) || !(rook instanceof Rook)) {
			return false;
		}
		return !((King) king).getHasMoved() && !((Rook) rook).getHasMoved();
	}

	private String enPassantTarget(Board board) {
		SimpleEntry<Integer, Integer> enPassant = board.getEnPassant();
		if (enPassant == null) {
			return "-";
		}
		int pawnRow = enPassant.getKey();
		int pawnCol = enPassant.getValue();
		Piece enPassantPawn = board.getPiece(pawnRow, pawnCol);
		int targetRow = enPassantPawn != null && enPassantPawn.getColor() == Color.WHITE ? pawnRow - 1 : pawnRow + 1;
		return "" + (char) ('a' + pawnCol) + (char) ('1' + targetRow);
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

	private static final class ScoredMove {
		private final Move move;
		private final int score;

		private ScoredMove(Move move, int score) {
			this.move = move;
			this.score = score;
		}
	}

	private static final class OrderedMove {
		private final Move move;
		private final int score;

		private OrderedMove(Move move, int score) {
			this.move = move;
			this.score = score;
		}
	}

	private static final class TranspositionEntry {
		private final int depth;
		private final int score;
		private final int flag;
		private final Move bestMove;

		private TranspositionEntry(int depth, int score, int flag, Move bestMove) {
			this.depth = depth;
			this.score = score;
			this.flag = flag;
			this.bestMove = bestMove;
		}
	}
}
