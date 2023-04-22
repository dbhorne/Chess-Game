package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;


/**
 * Board class to handle almost all logic for the game other than game state
 * @author Donovan Horne
 */
public class Board {
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
		ArrayList<SimpleEntry<Integer, Integer>> temp = new ArrayList<>();
		for (SimpleEntry<Integer, Integer> pair : whiteMoves) {
			temp.add(new SimpleEntry<>(pair.getKey(), pair.getValue()));
		}
		return temp;
	}

	/**
	 * Get all possible moves for the black pieces and return them
	 * @return Return a ArrayList of all possible moves for black
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getBlackMoves() {
		calcPieceMoves(false, Color.BLACK);
		ArrayList<SimpleEntry<Integer, Integer>> temp = new ArrayList<>();
		for (SimpleEntry<Integer, Integer> pair : blackMoves) {
			temp.add(new SimpleEntry<>(pair.getKey(), pair.getValue()));
		}
		return temp;
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
		Board copy = new Board();
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
				&& pieces[startX][startY].getValidMoves(this.copy(), false).contains(new SimpleEntry<>(endX, endY))) {
			Piece piece = pieces[startX][startY];
			if (piece instanceof Pawn) {
				if (piece.getColor() == Color.WHITE && Math.abs(startY - endY) == 1 && endX - startX == 1
						&& pieces[endX][endY] == null) {
					takenPieces.add(pieces[enPassant.getKey()][enPassant.getValue()]);
					pieces[enPassant.getKey()][enPassant.getValue()] = null;
				} else if (piece.getColor() == Color.BLACK && Math.abs(startY - endY) == 1 && startX - endX == 1
						&& pieces[endX][endY] == null) {
					takenPieces.add(pieces[enPassant.getKey()][enPassant.getValue()]);
					pieces[enPassant.getKey()][enPassant.getValue()] = null;
				}
			} else if (piece instanceof King) {
				if (endY - startY == 2) {
					((King) pieces[startX][startY]).setHasMoved(true);
					Rook castle = ((Rook) pieces[startX][startY + 3]);
					castle.setHasMoved(true);
					pieces[startX][startY + 1] = castle;
					pieces[startX][startY + 3] = null;
					castle.setRank(startX);
					castle.setFile(startY + 1);
				} else if (startY - endY == 2) {
					((King) pieces[startX][startY]).setHasMoved(true);
					Rook castle = ((Rook) pieces[startX][startY - 4]);
					castle.setHasMoved(true);
					pieces[startX][startY - 1] = castle;
					pieces[startX][startY - 4] = null;
					castle.setRank(startX);
					castle.setFile(startY - 1);
				} else {
					((King) pieces[startX][startY]).setHasMoved(true);
				}
			} else if (piece instanceof Rook) {
				((Rook) piece).setHasMoved(true);
			}
			if(pieces[endX][endY] != null) {
				takenPieces.add(pieces[endX][endY]);
			}
			updateEnPassant();
			pieces[endX][endY] = piece;
			piece.setRank(endX);
			piece.setFile(endY);
			pieces[startX][startY] = null;
			if (piece instanceof Pawn) {
				Pawn pawn = (Pawn) piece;
				if (pawn.getMadeFirstMove() == false) {
					pawn.makeFirstMove();
					if (pawn.getColor() == Color.WHITE && endX - startX == 2) {
						pawn.canEnPassant(true);
						enPassant = new SimpleEntry<>(endX, endY);
					} else if (piece.getColor() == Color.BLACK && startX - endX == 2) {
						pawn.canEnPassant(true);
						enPassant = new SimpleEntry<>(endX, endY);
					}
				}
			}
			blackMoves.clear();
			whiteMoves.clear();
			calcPieceMoves(false, Color.WHITE);
			calcPieceMoves(false, Color.BLACK);
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
	 * a method to determine if the king can move, whether it be check or there are simply no possible moves
	 * 		for the king
	 * @param colorOfKing the color of the king that you wish to determine
	 * @return Returns a boolean about whether the king has any possible moves
	 */
	public boolean canKingMove(Color colorOfKing) {
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (pieces[i][j] != null && pieces[i][j] instanceof King && pieces[i][j].getColor() == colorOfKing) {
					return pieces[i][j].getValidMoves(this.copy(), true).isEmpty();
				}
			}
		}
		return false;
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
			if (colorOfKing == Color.WHITE) {
				return king.isInCheck(blackMoves);
			} else {
				return king.isInCheck(whiteMoves);
			}
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
				return pieces[rank][file].getValidMoves(this.copy(), false);
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
		if(color == Color.WHITE) {
			whiteMoves.clear();
		} else {
			blackMoves.clear();
		}
		ArrayList<SimpleEntry<Integer, Integer>> moves;
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				if (pieces[i][j] != null) {
					if (pieces[i][j].getColor() == Color.WHITE && pieces[i][j].getColor() == color) {
						moves = pieces[i][j].getValidMoves(this.copy(), kingFlag);
						for (SimpleEntry<Integer, Integer> move : moves) {
							whiteMoves.add(move);
						}
					} else if (pieces[i][j].getColor() == color) {
						moves = pieces[i][j].getValidMoves(this.copy(), kingFlag);
						for (SimpleEntry<Integer, Integer> move : moves) {
							blackMoves.add(move);
						}
					}
				}
			}
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
}
