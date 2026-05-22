package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;


/**
 * Board class to handle almost all logic for the game other than game state
 * @author Donovan Horne
 */
public class Board {
	private static final int[][] ROOK_DIRECTIONS = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
	private static final int[][] BISHOP_DIRECTIONS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
	private static final int[][] KING_DIRECTIONS = {
		{ 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
	};
	private static final int[][] KNIGHT_OFFSETS = {
		{ 2, 1 }, { 1, 2 }, { -1, 2 }, { -2, 1 }, { -2, -1 }, { -1, -2 }, { 1, -2 }, { 2, -1 }
	};
	private Piece[][] pieces = new Piece[8][8];
	/** The board size */
	public final int BOARD_SIZE = pieces.length; 
	private ArrayList<SimpleEntry<Integer, Integer>> whiteMoves = new ArrayList<>();
	private ArrayList<SimpleEntry<Integer, Integer>> blackMoves = new ArrayList<>();
	private int numOfWhitePieces = 0;
	private int numOfBlackPieces = 0;
	private SimpleEntry<Integer, Integer> enPassant = null;
	private ArrayList<Piece> takenPieces = new ArrayList<>();
	
	/**
	 * Constructor for the board, calls a reset board method
	 */
	Board() {
		resetBoard();
	}

	/**
	 * Get all possible moves for the white pieces and return them
	 * @return Return a ArrayList of all possible moves for white
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getWhiteMoves() {
		calcPieceMoves(false, Color.WHITE);
		return copyMoves(whiteMoves);
	}

	/**
	 * Get all possible moves for the black pieces and return them
	 * @return Return a ArrayList of all possible moves for black
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getBlackMoves() {
		calcPieceMoves(false, Color.BLACK);
		return copyMoves(blackMoves);
	}

	/**
	 * calculate the current number of pieces on the board for each color
	 */
	public void calcNumOfPieces() {
		numOfBlackPieces = 0;
		numOfWhitePieces = 0;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (pieces[i][j] != null) {
					if (pieces[i][j].getColor() == Color.BLACK) {
						numOfBlackPieces += 1;
					} else {
						numOfWhitePieces += 1;
					}
				}
			}
		}
	}

	
	/**
	 * Constructor for the board with a pieces array passed in to create a specific board state
	 * @param pieces Piece array with the board state you wish to have
	 */
	Board(Piece[][] pieces) {
		this.pieces = pieces;
	}

	/**
	 * resets the board to a default state
	 */
	public void resetBoard() {
		Pawn.resetPawnID();
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Piece piece = null;
				if (row == 1) {
					piece = new Pawn(Color.WHITE, row, col);
				} else if (row == 6) {
					piece = new Pawn(Color.BLACK, row, col);
				} else if (row == 0) {
					switch (col) {
					case 0:
					case 7:
						piece = new Rook(Color.WHITE, row, col);
						break;
					case 1:
					case 6:
						piece = new Knight(Color.WHITE, row, col);
						break;
					case 2:
					case 5:
						piece = new Bishop(Color.WHITE, row, col);
						break;
					case 3:
						piece = new Queen(Color.WHITE, row, col);
						break;
					case 4:
						piece = new King(Color.WHITE, row, col);
						break;
					}
				} else if (row == 7) {
					switch (col) {
					case 0:
					case 7:
						piece = new Rook(Color.BLACK, row, col);
						break;
					case 1:
					case 6:
						piece = new Knight(Color.BLACK, row, col);
						break;
					case 2:
					case 5:
						piece = new Bishop(Color.BLACK, row, col);
						break;
					case 3:
						piece = new Queen(Color.BLACK, row, col);
						break;
					case 4:
						piece = new King(Color.BLACK, row, col);
						break;
					}
				}
				pieces[row][col] = piece;
			}
		}
		calcNumOfPieces();
	}

	
	/**
	 * Creates a deep copy of the board so that the new copy doesn't reference the same pieces
	 * @return Returns a deep copy of the current Board
	 */
	public Board copy() {
		Board copy = new Board(new Piece[8][8]);
		Piece[][] tempPieces = new Piece[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Piece piece = pieces[i][j];
				if (piece == null) {
					tempPieces[i][j] = null;
				} else {
					PieceType type = piece.getType();
					Color color = piece.getColor();
					switch (type) {
					case ROOK:
						tempPieces[i][j] = new Rook((Rook) piece);
						break;
					case QUEEN:
						tempPieces[i][j] = new Queen(color, i, j);
						break;
					case KING:
						tempPieces[i][j] = new King((King) piece);
						break;
					case BISHOP:
						tempPieces[i][j] = new Bishop(color, i, j);
						break;
					case KNIGHT:
						tempPieces[i][j] = new Knight(color, i, j);
						break;
					case PAWN:
						tempPieces[i][j] = new Pawn((Pawn) piece);
						break;
					}

				}
			}
		}
		copy.setPieces(tempPieces);
		copy.enPassant = (this.enPassant != null) ? new SimpleEntry<>(this.enPassant.getKey(), this.enPassant.getValue()) : null;
		return copy;
	}

	/**
	 * Used to move a piece if the move is valid, as well as check for special cases such
	 * 		as en passant, castling, and promotions
	 * @param startX the starting row of the piece being moved
	 * @param startY the starting column of the piece being moved
	 * @param endX the destination row of the piece being moved
	 * @param endY the destination column of the piece being moved
	 * @param currColor a check to ensure the color of the piece is the correct with the one they want to move
	 * @return Returns a boolean, true if the move was made, false if it was an invalid move
	 */
	public boolean move(int startX, int startY, int endX, int endY, Color currColor) {
		if (pieces[startX][startY] != null && pieces[startX][startY].getColor() == currColor
				&& pieces[startX][startY].getValidMoves(this, false).contains(new SimpleEntry<>(endX, endY))) {
			applyMoveUnchecked(startX, startY, endX, endY, true);
			return true;
		}
		return false;
	}
	
	/**
	 * Return the number of pieces that have been taken
	 * @return Return an int value of the number of taken pieces
	 */
	public int getNumOfTakenPieces() {
		return takenPieces.size();
	}
	
	/**
	 * Used to promote a pawn to a new type of piece
	 * @param rank the row in which the pawn is in
	 * @param file the column the pawn is in 
	 * @param type the type of piece the pawn wants to promote to
	 */
	public void promote(int rank, int file, PieceType type) {
		Pawn pawn = (Pawn) pieces[rank][file];
		if (pawn.getColor() == Color.WHITE && rank == 7) {
			switch (type) {
			case QUEEN:
				pieces[rank][file] = new Queen(pawn);
				break;
			case KNIGHT:
				pieces[rank][file] = new Knight(pawn);
				break;
			case ROOK:
				pieces[rank][file] = new Rook(pawn);
				break;
			case BISHOP:
				pieces[rank][file] = new Bishop(pawn);
				break;
			default:
				break;
			}
		} else if (pawn.getColor() == Color.BLACK && rank == 0) {
			switch (type) {
			case QUEEN:
				pieces[rank][file] = new Queen(pawn);
				break;
			case KNIGHT:
				pieces[rank][file] = new Knight(pawn);
				break;
			case ROOK:
				pieces[rank][file] = new Rook(pawn);
				break;
			case BISHOP:
				pieces[rank][file] = new Bishop(pawn);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Update enPassant variable after each move, if a piece is available for being enPassant
	 * 		remove it and set it to null, and set that it cannot be taken via en passant
	 */

	public void updateEnPassant() {
		if (enPassant != null) {
			if (pieces[enPassant.getKey()][enPassant.getValue()] != null) {
				((Pawn) pieces[enPassant.getKey()][enPassant.getValue()]).canEnPassant(false);
			}
			enPassant = null;
		}
	}

	/**
	 * Return the King of the color specified
	 * @param color Color of the king you wish to return
	 * @return Returns a piece of type King
	 */
	public King getKing(Color color) {
		int kingRow = -1;
		int kingCol = -1;
		for (int row = 0; row < pieces.length; row++) {
			for (int col = 0; col < pieces[0].length; col++) {
				if (pieces[row][col] != null && pieces[row][col].getType() == PieceType.KING
						&& pieces[row][col].getColor() == color) {
					kingRow = row;
					kingCol = col;
					break;
				}
			}
		}
		if (kingRow == -1 || kingCol == -1) {
			return null;
		}
		return (King) pieces[kingRow][kingCol];
	}

	/**
	 * Used to call the method for each king called isInCheck
	 * @param colorOfKing color of the king you wish to determine
	 * @return Returns a boolean based on if the king is in check
	 */
	public boolean isKingInCheck(Color colorOfKing) {
		King king = getKing(colorOfKing);
		if (king != null) {
			return king.isInCheck(this);
		}
		return true;
	}

	
	/**
	 * Used to set the pieces on the board
	 * @param pieces the Piece array you wish to set the board to
	 */
	public void setPieces(Piece[][] pieces) {
		this.pieces = pieces;
	}

	/**
	 * Return the pieces array of the board state
	 * @return Returns the current Piece array
	 */
	public Piece[][] getPieces() {
		return pieces;
	}

	/**
	 * Return a piece at a specific position on the board
	 * @param rank the row in which the piece is
	 * @param file the column in which the piece is
	 * @return Returns a piece based on rank and file
	 */
	public Piece getPiece(int rank, int file) {
		return pieces[rank][file];
	}

	/**
	 * Used to get a list of valid moves for a specific piece
	 * @param rank row of the piece
	 * @param file column of the piece
	 * @param currMove A check of the color of the piece you want
	 * @return Returns an ArrayList of valid moves for given piece
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file, Color currMove) {
		if (pieces[rank][file] != null) {
			if (pieces[rank][file].getColor() == currMove) {
				return pieces[rank][file].getValidMoves(this, false);
			}
		}
		return new ArrayList<SimpleEntry<Integer, Integer>>();
	}

	/**
	 * Used to update all possible moves on the board for a specific color
	 * @param kingFlag if you want to check if the king is going to be in check or to ignore
	 * @param color the color of pieces you wish to calculate all the moves for
	 */
	public void calcPieceMoves(boolean kingFlag, Color color) {
		ArrayList<SimpleEntry<Integer, Integer>> moves = collectMoves(color, kingFlag);
		if(color == Color.WHITE) {
			whiteMoves = moves;
		} else {
			blackMoves = moves;
		}
	}

	/** 
	 * Convert the current board state into a string
	 * @return String of the current board
	 */
	@Override
	public String toString() {
		String temp = "";
		for (int i = pieces.length - 1; i >= 0; i--) {
			for (int j = 0; j < pieces[i].length; j++) {
				if (pieces[i][j] != null) {
					switch (pieces[i][j].getType()) {
					case KING:
						temp += "K ";
						break;
					case ROOK:
						temp += "R ";
						break;
					case QUEEN:
						temp += "Q ";
						break;
					case KNIGHT:
						temp += "N ";
						break;
					case PAWN:
						temp += "P ";
						break;
					case BISHOP:
						temp += "B ";
						break;
					default:
						temp += "X ";
						break;
					}
				} else {
					temp += "X ";
				}
			}
			temp += "\n";
		}
		return temp;
	}

	/**
	 * Return the en passant pawn's position (the pawn that just moved two squares), or null
	 * @return the en passant entry, or null if none
	 */
	public SimpleEntry<Integer, Integer> getEnPassant() {
		return enPassant;
	}

	/**
	 * Set the en passant pawn position directly (used when loading a FEN string)
	 * @param ep the position of the pawn vulnerable to en passant, or null
	 */
	public void setEnPassant(SimpleEntry<Integer, Integer> ep) {
		this.enPassant = ep;
	}

	/**
	 * Generate the piece-placement field of a FEN string (rank 8 down to rank 1)
	 * @return FEN piece-placement string
	 */
	public String toFENPiecePlacement() {
		StringBuilder sb = new StringBuilder();
		for (int row = BOARD_SIZE - 1; row >= 0; row--) {
			int empty = 0;
			for (int col = 0; col < BOARD_SIZE; col++) {
				if (pieces[row][col] == null) {
					empty++;
				} else {
					if (empty > 0) {
						sb.append(empty);
						empty = 0;
					}
					sb.append(pieces[row][col].toFENChar());
				}
			}
			if (empty > 0) sb.append(empty);
			if (row > 0) sb.append('/');
		}
		return sb.toString();
	}

	/**
	 * To get a String of the board, used for keeping track of each board state
	 * @return Returns a single string of the board
	 */
	public String getBoardString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				Piece piece = pieces[i][j];
				if (piece == null) {
					sb.append("_");
				} else {
					sb.append(piece.toString());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * To get the number of pieces on the board for a specific color
	 * @param color Color of which you want know their number of pieces
	 * @return Returns and int of the number of pieces on the board
	 */
	public int getNumOfPieces(Color color) {
		calcNumOfPieces();
		return color == Color.WHITE ? numOfWhitePieces : numOfBlackPieces;
	}

	public boolean isLegalMove(Piece piece, int newRank, int newFile) {
		Piece destinationPiece = pieces[newRank][newFile];
		if (destinationPiece instanceof King && destinationPiece.getColor() != piece.getColor()) {
			return false;
		}
		Board simulatedBoard = simulateMove(piece.getRank(), piece.getFile(), newRank, newFile);
		return simulatedBoard != null && !simulatedBoard.isKingInCheck(piece.getColor());
	}

	public Board simulateMove(int startX, int startY, int endX, int endY) {
		if (!isWithinBounds(startX, startY) || !isWithinBounds(endX, endY) || pieces[startX][startY] == null) {
			return null;
		}
		Board copy = copy();
		copy.applyMoveUnchecked(startX, startY, endX, endY, false);
		return copy;
	}

	public boolean isSquareAttacked(int rank, int file, Color attackingColor) {
		for (int[] direction : ROOK_DIRECTIONS) {
			if (isSlidingAttack(rank, file, attackingColor, direction[0], direction[1], PieceType.ROOK, PieceType.QUEEN)) {
				return true;
			}
		}
		for (int[] direction : BISHOP_DIRECTIONS) {
			if (isSlidingAttack(rank, file, attackingColor, direction[0], direction[1], PieceType.BISHOP, PieceType.QUEEN)) {
				return true;
			}
		}
		for (int[] offset : KNIGHT_OFFSETS) {
			int attackRank = rank + offset[0];
			int attackFile = file + offset[1];
			if (isWithinBounds(attackRank, attackFile)) {
				Piece piece = pieces[attackRank][attackFile];
				if (piece instanceof Knight && piece.getColor() == attackingColor) {
					return true;
				}
			}
		}
		int pawnRank = attackingColor == Color.WHITE ? rank - 1 : rank + 1;
		for (int pawnFileOffset : new int[] { -1, 1 }) {
			int pawnFile = file + pawnFileOffset;
			if (isWithinBounds(pawnRank, pawnFile)) {
				Piece piece = pieces[pawnRank][pawnFile];
				if (piece instanceof Pawn && piece.getColor() == attackingColor) {
					return true;
				}
			}
		}
		for (int[] direction : KING_DIRECTIONS) {
			int attackRank = rank + direction[0];
			int attackFile = file + direction[1];
			if (isWithinBounds(attackRank, attackFile)) {
				Piece piece = pieces[attackRank][attackFile];
				if (piece instanceof King && piece.getColor() == attackingColor) {
					return true;
				}
			}
		}
		return false;
	}

	private ArrayList<SimpleEntry<Integer, Integer>> copyMoves(ArrayList<SimpleEntry<Integer, Integer>> moves) {
		ArrayList<SimpleEntry<Integer, Integer>> copy = new ArrayList<>();
		for (SimpleEntry<Integer, Integer> pair : moves) {
			copy.add(new SimpleEntry<>(pair.getKey(), pair.getValue()));
		}
		return copy;
	}

	private ArrayList<SimpleEntry<Integer, Integer>> collectMoves(Color color, boolean kingFlag) {
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				Piece piece = pieces[i][j];
				if (piece != null && piece.getColor() == color) {
					moves.addAll(piece.getValidMoves(this, kingFlag));
				}
			}
		}
		return moves;
	}

	private void applyMoveUnchecked(int startX, int startY, int endX, int endY, boolean trackTakenPieces) {
		Piece piece = pieces[startX][startY];
		Piece capturedPiece = pieces[endX][endY];
		Piece enPassantCapture = null;
		if (piece instanceof Pawn && enPassant != null && startY != endY && capturedPiece == null) {
			enPassantCapture = pieces[enPassant.getKey()][enPassant.getValue()];
			pieces[enPassant.getKey()][enPassant.getValue()] = null;
		}
		if (trackTakenPieces) {
			if (capturedPiece != null) {
				takenPieces.add(capturedPiece);
			}
			if (enPassantCapture != null) {
				takenPieces.add(enPassantCapture);
			}
		}

		updateEnPassant();
		pieces[endX][endY] = piece;
		pieces[startX][startY] = null;
		piece.setRank(endX);
		piece.setFile(endY);

		if (piece instanceof King) {
			King king = (King) piece;
			king.setHasMoved(true);
			if (endY - startY == 2) {
				moveRookForCastle(startX, startY + 3, startY + 1);
			} else if (startY - endY == 2) {
				moveRookForCastle(startX, startY - 4, startY - 1);
			}
		} else if (piece instanceof Rook) {
			((Rook) piece).setHasMoved(true);
		}

		if (piece instanceof Pawn) {
			Pawn pawn = (Pawn) piece;
			if (!pawn.getMadeFirstMove()) {
				pawn.makeFirstMove();
			}
			if (Math.abs(endX - startX) == 2) {
				pawn.canEnPassant(true);
				enPassant = new SimpleEntry<>(endX, endY);
			}
		}
	}

	private void moveRookForCastle(int rank, int rookStartFile, int rookEndFile) {
		Rook rook = (Rook) pieces[rank][rookStartFile];
		pieces[rank][rookEndFile] = rook;
		pieces[rank][rookStartFile] = null;
		rook.setRank(rank);
		rook.setFile(rookEndFile);
		rook.setHasMoved(true);
	}

	private boolean isSlidingAttack(int rank, int file, Color attackingColor, int rankStep, int fileStep,
			PieceType firstType, PieceType secondType) {
		for (int distance = 1; distance < BOARD_SIZE; distance++) {
			int attackRank = rank + (distance * rankStep);
			int attackFile = file + (distance * fileStep);
			if (!isWithinBounds(attackRank, attackFile)) {
				return false;
			}
			Piece piece = pieces[attackRank][attackFile];
			if (piece == null) {
				continue;
			}
			return piece.getColor() == attackingColor
					&& (piece.getType() == firstType || piece.getType() == secondType);
		}
		return false;
	}

	private boolean isWithinBounds(int rank, int file) {
		return rank >= 0 && rank < BOARD_SIZE && file >= 0 && file < BOARD_SIZE;
	}
}
