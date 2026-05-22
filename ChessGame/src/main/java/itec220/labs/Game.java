package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * To create a class that communicated with the board, which will handle piece logic,
 * 			and allow for this class to handle game state logic, such as check, checkmate, etc.
 * @author Donovan Horne
 *
 */
public class Game {
	private Board board;
	private GameState currState;
	private Color currMove;
	private ArrayList<Move> moveHistory = new ArrayList<>();
	private LinkedList<String> boardStates = new LinkedList<>();
	private int halfMoveClock = 0;
	private int fullMoveNumber = 1;
	private boolean promotionPending = false;
	private int promotionRank = -1;
	private int promotionFile = -1;
	private Move pendingPromotionMove = null;

	/**
	 * Constructor for the game, create a new board, and update the current move and game state
	 */
	Game() {
		board = new Board();
		currState = GameState.IN_PROGRESS;
		currMove = Color.WHITE;
		boardStates.add(getPositionIdentity());
	}

	/**
	 * Promote a pawn on the board
	 * @param rank row of the pawn
	 * @param file column of the pawn
	 * @param type the type of piece the pawn is promoting to
	 */
	public void promote(int rank, int file, PieceType type) {
		if (type == null || type == PieceType.PAWN || type == PieceType.KING) {
			throw new IllegalArgumentException("Pawns may only promote to queen, rook, bishop, or knight");
		}
		board.promote(rank, file, type);
		if (promotionPending && rank == promotionRank && file == promotionFile) {
			moveHistory.add(new Move(pendingPromotionMove.startRank, pendingPromotionMove.startFile,
					pendingPromotionMove.endRank, pendingPromotionMove.endFile, type,
					pendingPromotionMove.capturedType, pendingPromotionMove.enPassantCapture,
					pendingPromotionMove.castle));
			promotionPending = false;
			promotionRank = -1;
			promotionFile = -1;
			pendingPromotionMove = null;
			currState = updateGameState();
			updateMoveTracker();
		}
	}

	/**
	 * Get the color of the current move
	 * @return Returns a Color enum
	 */
	public Color getCurrMove() {
		return currMove;
	}

	/**
	 * Get the current game state, i.e IN_PROGRESS
	 * @return Returns a GameState enum
	 */
	public GameState getCurrState() {
		return currState;
	}

	/**
	 * Return whether a GUI-style pawn promotion is waiting for a selected piece type.
	 * @return true if a promotion choice is pending
	 */
	public boolean isPromotionPending() {
		return promotionPending;
	}

	/**
	 * Get the valid moves for a specific piece
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 * @return Returns a ArrayList of valid moves for the given piece
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file) {
		return board.getValidMoves(rank, file, currMove);
	}

	/**
	 * Get all legal moves for the current side to move.
	 * @return Returns every legal move for the current side
	 */
	public ArrayList<Move> getLegalMoves() {
		ArrayList<Move> legalMoves = new ArrayList<>();
		if (promotionPending) {
			return legalMoves;
		}
		Piece[][] pieces = board.getPieces();
		for (int rank = 0; rank < pieces.length; rank++) {
			for (int file = 0; file < pieces[rank].length; file++) {
				Piece piece = pieces[rank][file];
				if (piece == null || piece.getColor() != currMove) {
					continue;
				}
				for (SimpleEntry<Integer, Integer> destination : piece.getValidMoves(board, false)) {
					addMoveOptions(legalMoves, piece, destination.getKey(), destination.getValue());
				}
			}
		}
		return legalMoves;
	}

	/**
	 * Get a copy of the moves applied to this game.
	 * @return defensive copy of move history
	 */
	public ArrayList<Move> getMoveHistory() {
		return new ArrayList<>(moveHistory);
	}

	/**
	 * Ask a bot for a legal move without applying it to the game.
	 * @param bot bot that will select the move
	 * @return selected move, or null if no move is available
	 */
	public Move getBotMove(ChessBot bot) {
		if (bot == null || bot.getColor() != currMove) {
			return null;
		}
		return bot.chooseMove(getLegalMoves(), board.copy());
	}

	/**
	 * Return a boolean of whether the game is over or not
	 * @return Returns a boolean, true if the game is over, false if it will continue
	 */
	public boolean gameOver() {
		if (currState == GameState.BLACKWINS || currState == GameState.WHITEWINS || currState == GameState.DRAW
				|| currState == GameState.STALEMATE) {
			return true;
		}
		return false;
	}

	/**
	 * Call a move on the board
	 * @param startX the starting row of the piece
	 * @param startY the starting column of the piece
	 * @param endX the destination row of the piece
	 * @param endY the destination column of the piece
	 * @return Return a boolean based on whether the move was made or not
	 */
	public boolean move(int startX, int startY, int endX, int endY) {
		return move(startX, startY, endX, endY, null);
	}

	/**
	 * Move using a shared Move model.
	 * @param move move to apply
	 * @return true if the move was applied
	 */
	public boolean move(Move move) {
		if (move == null || !matchesLegalMove(move)) {
			return false;
		}
		return applyMove(move);
	}

	private boolean move(int startX, int startY, int endX, int endY, PieceType promotionType) {
		return applyMove(new Move(startX, startY, endX, endY, promotionType));
	}

	private boolean applyMove(Move move) {
		if (promotionPending) {
			return false;
		}
		int startX = move.startRank;
		int startY = move.startFile;
		int endX = move.endRank;
		int endY = move.endFile;
		PieceType promotionType = move.promotionType;
		int takenBefore = board.getNumOfTakenPieces();
		Piece movingPiece = board.getPiece(startX, startY);
		Move moveToRecord = copyWithBoardMoveMetadata(move, movingPiece, endX, endY);
		if (board.move(startX, startY, endX, endY, currMove)) {
			boolean isCapture = board.getNumOfTakenPieces() > takenBefore;
			boolean isPawnMove = movingPiece instanceof Pawn;
			if (isPawnMove || isCapture) {
				halfMoveClock = 0;
			} else {
				halfMoveClock++;
			}
			if (currMove == Color.BLACK) {
				fullMoveNumber++;
			}
			if (isPromotionMove(movingPiece, endX)) {
				if (promotionType == null) {
					promotionPending = true;
					promotionRank = endX;
					promotionFile = endY;
					pendingPromotionMove = moveToRecord;
					currMove = currMove == Color.WHITE ? Color.BLACK : Color.WHITE;
					return true;
				}
				board.promote(endX, endY, promotionType);
			}
			moveHistory.add(moveToRecord);
			currMove = currMove == Color.WHITE ? Color.BLACK : Color.WHITE;
			currState = updateGameState();
			updateMoveTracker();
			return true;
		} else {
			return false;
		}
	}

	private void addMoveOptions(ArrayList<Move> legalMoves, Piece piece, int endRank, int endFile) {
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
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, PieceType.QUEEN,
					capturedType, enPassantCapture, castle));
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, PieceType.ROOK,
					capturedType, enPassantCapture, castle));
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, PieceType.BISHOP,
					capturedType, enPassantCapture, castle));
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, PieceType.KNIGHT,
					capturedType, enPassantCapture, castle));
		} else {
			legalMoves.add(new Move(piece.getRank(), piece.getFile(), endRank, endFile, null,
					capturedType, enPassantCapture, castle));
		}
	}

	private Move copyWithBoardMoveMetadata(Move move, Piece movingPiece, int endRank, int endFile) {
		if (move.capturedType != null || move.enPassantCapture || move.castle) {
			return move;
		}
		Piece capturedPiece = board.getPiece(endRank, endFile);
		PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();
		boolean enPassantCapture = movingPiece instanceof Pawn && move.startFile != endFile && capturedPiece == null;
		if (enPassantCapture) {
			SimpleEntry<Integer, Integer> enPassant = board.getEnPassant();
			if (enPassant != null && board.getPiece(enPassant.getKey(), enPassant.getValue()) != null) {
				capturedType = board.getPiece(enPassant.getKey(), enPassant.getValue()).getType();
			}
		}
		boolean castle = movingPiece instanceof King && Math.abs(endFile - move.startFile) == 2;
		return new Move(move.startRank, move.startFile, move.endRank, move.endFile,
				move.promotionType, capturedType, enPassantCapture, castle);
	}

	private boolean matchesLegalMove(Move requestedMove) {
		for (Move legalMove : getLegalMoves()) {
			if (legalMove.startRank == requestedMove.startRank
					&& legalMove.startFile == requestedMove.startFile
					&& legalMove.endRank == requestedMove.endRank
					&& legalMove.endFile == requestedMove.endFile
					&& legalMove.promotionType == requestedMove.promotionType) {
				return true;
			}
		}
		return false;
	}

	private boolean isPromotionMove(Piece piece, int endRank) {
		return piece instanceof Pawn
				&& ((piece.getColor() == Color.WHITE && endRank == 7)
				|| (piece.getColor() == Color.BLACK && endRank == 0));
	}

	/** game speed issues happen here
	 *  Update the current game state, speed issues happen because we calculate all the moves on the
	 *  the board
	 *  @return Return the GameState enum
	 */
	public GameState updateGameState() {
		Color sideToMove = currMove;
		boolean inCheck = board.isKingInCheck(sideToMove);
		ArrayList<SimpleEntry<Integer, Integer>> legalMoves =
				sideToMove == Color.WHITE ? board.getWhiteMoves() : board.getBlackMoves();
		if (legalMoves.isEmpty()) {
			if (inCheck) {
				return sideToMove == Color.WHITE ? GameState.BLACKWINS : GameState.WHITEWINS;
			}
			return GameState.STALEMATE;
		}
		if (inCheck) {
			return sideToMove == Color.WHITE ? GameState.WHITEINCHECK : GameState.BLACKINCHECK;
		}
		if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
			return GameState.DRAW;
		}
		return GameState.IN_PROGRESS;
	}
	
	
	/** 
	 * Used to track threefold repetition based on full position identity.
	 */
	public void updateMoveTracker() {
		String newBoardState = getPositionIdentity();
		boardStates.add(newBoardState);
		if(Collections.frequency(boardStates, newBoardState) >= 3) {
			currState = GameState.DRAW;
		}
	}

	/**
	 * Get a deep copy of the games current board
	 * @return Returns a copy of the current board
	 */
	public Board getCopyOfCurrBoard() {
		return board.copy();
	}

	/**
	 * Get the number of taken pieces from the board
	 * @return Returns an int of the number of taken pieces
	 */
	public int getNumTakenPieces() {
		return board.getNumOfTakenPieces();
	}

	/**
	 * return the current game state
	 * @return return the current game state enum
	 */
	public GameState getGameState() {
		return this.currState;
	}

	/**
	 * Serialize the current game state to a FEN string
	 * @return FEN string representing the current position
	 */
	public String toFEN() {
		StringBuilder sb = new StringBuilder();

		// 1. Piece placement
		sb.append(board.toFENPiecePlacement());
		sb.append(' ');

		// 2. Active color
		sb.append(currMove == Color.WHITE ? 'w' : 'b');
		sb.append(' ');

		// 3. Castling availability
		sb.append(getCastlingAvailability());
		sb.append(' ');

		// 4. En passant target square
		sb.append(getEnPassantTarget());
		sb.append(' ');

		// 5 & 6. Halfmove clock and fullmove number
		sb.append(halfMoveClock);
		sb.append(' ');
		sb.append(fullMoveNumber);

		return sb.toString();
	}

	private boolean canCastle(Piece[][] pieces, int kingRow, int kingCol, int rookRow, int rookCol) {
		Piece king = pieces[kingRow][kingCol];
		Piece rook = pieces[rookRow][rookCol];
		if (!(king instanceof King) || !(rook instanceof Rook)) return false;
		return !((King) king).getHasMoved() && !((Rook) rook).getHasMoved();
	}

	String getPositionIdentity() {
		return board.toFENPiecePlacement() + " " + (currMove == Color.WHITE ? "w" : "b") + " "
				+ getCastlingAvailability() + " " + getEnPassantTarget();
	}

	/**
	 * Load a position from a FEN string, replacing the current game state entirely.
	 * @param fen the FEN string to parse
	 * @throws IllegalArgumentException if the FEN is malformed
	 */
	public void loadFEN(String fen) {
		String[] fields = fen.trim().split(" ");
		if (fields.length != 6) {
			throw new IllegalArgumentException("FEN must have exactly 6 space-separated fields");
		}

		// --- 1. Piece placement ---
		String[] ranks = fields[0].split("/");
		if (ranks.length != 8) {
			throw new IllegalArgumentException("FEN piece placement must have exactly 8 ranks");
		}
		Pawn.resetPawnID();
		Piece[][] pieces = new Piece[8][8];
		for (int fenRank = 0; fenRank < 8; fenRank++) {
			int row = 7 - fenRank; // FEN rank 0 (rank 8) → board row 7
			String rankStr = ranks[fenRank];
			int col = 0;
			for (char c : rankStr.toCharArray()) {
				if (col > 8) throw new IllegalArgumentException("Rank " + (8 - fenRank) + " exceeds 8 files");
				if (Character.isDigit(c)) {
					col += c - '0';
				} else {
					pieces[row][col] = charToPiece(c, row, col);
					col++;
				}
			}
			if (col != 8) throw new IllegalArgumentException("Rank " + (8 - fenRank) + " does not sum to 8 files");
		}

		// Set madeFirstMove on pawns not on their starting row
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (pieces[row][col] instanceof Pawn) {
					Pawn p = (Pawn) pieces[row][col];
					boolean onStart = (p.getColor() == Color.WHITE && row == 1)
							|| (p.getColor() == Color.BLACK && row == 6);
					if (!onStart) p.makeFirstMove();
				}
			}
		}

		// --- 2. Active color ---
		if (!fields[1].equals("w") && !fields[1].equals("b")) {
			throw new IllegalArgumentException("Active color must be 'w' or 'b'");
		}
		currMove = fields[1].equals("w") ? Color.WHITE : Color.BLACK;

		// --- 3. Castling rights ---
		// Default all kings/rooks to hasMoved=true, then unlock per castling right
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (pieces[row][col] instanceof King) ((King) pieces[row][col]).setHasMoved(true);
				if (pieces[row][col] instanceof Rook) ((Rook) pieces[row][col]).setHasMoved(true);
			}
		}
		String castling = fields[2];
		if (!castling.equals("-")) {
			for (char c : castling.toCharArray()) {
				switch (c) {
					case 'K': unlockCastle(pieces, 0, 4, 0, 7); break;
					case 'Q': unlockCastle(pieces, 0, 4, 0, 0); break;
					case 'k': unlockCastle(pieces, 7, 4, 7, 7); break;
					case 'q': unlockCastle(pieces, 7, 4, 7, 0); break;
					default: throw new IllegalArgumentException("Invalid castling character: " + c);
				}
			}
		}

		// --- Build board (do this before en passant so getPiece works) ---
		board = new Board(pieces);

		// --- 4. En passant ---
		board.setEnPassant(null);
		if (!fields[3].equals("-")) {
			if (fields[3].length() != 2) throw new IllegalArgumentException("Invalid en passant square: " + fields[3]);
			int epCol = fields[3].charAt(0) - 'a';
			int epTargetRow = fields[3].charAt(1) - '1';
			if (epCol < 0 || epCol > 7 || epTargetRow < 0 || epTargetRow > 7) {
				throw new IllegalArgumentException("En passant square out of range: " + fields[3]);
			}
			// Target square is where the capturing pawn lands; the vulnerable pawn is one rank beyond
			int pawnRow = (epTargetRow < 4) ? epTargetRow + 1 : epTargetRow - 1;
			Piece epPiece = board.getPiece(pawnRow, epCol);
			if (!(epPiece instanceof Pawn)) {
				throw new IllegalArgumentException("No pawn found at en passant pawn square");
			}
			((Pawn) epPiece).canEnPassant(true);
			board.setEnPassant(new java.util.AbstractMap.SimpleEntry<>(pawnRow, epCol));
		}

		// --- 5 & 6. Halfmove clock and fullmove number ---
		try {
			halfMoveClock = Integer.parseInt(fields[4]);
			fullMoveNumber = Integer.parseInt(fields[5]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Halfmove/fullmove fields must be integers");
		}

		// --- Reset state tracking ---
		boardStates.clear();
		boardStates.add(getPositionIdentity());
		moveHistory.clear();
		promotionPending = false;
		promotionRank = -1;
		promotionFile = -1;
		pendingPromotionMove = null;
		currState = updateGameState();
	}

	private Piece charToPiece(char c, int row, int col) {
		Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
		switch (Character.toLowerCase(c)) {
			case 'k': return new King(color, row, col);
			case 'q': return new Queen(color, row, col);
			case 'r': return new Rook(color, row, col);
			case 'b': return new Bishop(color, row, col);
			case 'n': return new Knight(color, row, col);
			case 'p': return new Pawn(color, row, col);
			default: throw new IllegalArgumentException("Unknown FEN piece character: " + c);
		}
	}

	private void unlockCastle(Piece[][] pieces, int kingRow, int kingCol, int rookRow, int rookCol) {
		if (pieces[kingRow][kingCol] instanceof King) ((King) pieces[kingRow][kingCol]).setHasMoved(false);
		if (pieces[rookRow][rookCol] instanceof Rook) ((Rook) pieces[rookRow][rookCol]).setHasMoved(false);
	}

	/**
	 * Return the halfmove clock (resets on pawn move or capture; used for 50-move rule)
	 * @return halfmove clock as an int
	 */
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	/**
	 * Return the fullmove number (starts at 1, increments after every black move)
	 * @return fullmove number as an int
	 */
	public int getFullMoveNumber() {
		return fullMoveNumber;
	}

	private String getCastlingAvailability() {
		StringBuilder castling = new StringBuilder();
		Piece[][] pieces = board.getPieces();
		if (canCastle(pieces, 0, 4, 0, 7)) castling.append('K');
		if (canCastle(pieces, 0, 4, 0, 0)) castling.append('Q');
		if (canCastle(pieces, 7, 4, 7, 7)) castling.append('k');
		if (canCastle(pieces, 7, 4, 7, 0)) castling.append('q');
		return castling.length() == 0 ? "-" : castling.toString();
	}

	private String getEnPassantTarget() {
		java.util.AbstractMap.SimpleEntry<Integer, Integer> ep = board.getEnPassant();
		if (ep == null) {
			return "-";
		}
		int pawnRow = ep.getKey();
		int pawnCol = ep.getValue();
		Piece epPawn = board.getPiece(pawnRow, pawnCol);
		int targetRow = (epPawn != null && epPawn.getColor() == Color.WHITE) ? pawnRow - 1 : pawnRow + 1;
		return "" + (char) ('a' + pawnCol) + (char) ('1' + targetRow);
	}
}
