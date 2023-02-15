package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Board {
	private Piece[][] pieces = new Piece[8][8];
	public final int BOARD_SIZE = pieces.length;
	private ArrayList<SimpleEntry<Integer, Integer>> whiteMoves = new ArrayList<>();
	private ArrayList<SimpleEntry<Integer, Integer>> blackMoves = new ArrayList<>();
	private int numOfWhitePieces = 0;
	private int numOfBlackPieces = 0;
	private SimpleEntry<Integer, Integer> enPassant = null;

	Board() {
		resetBoard();
	}

	public ArrayList<SimpleEntry<Integer, Integer>> getWhiteMoves() {
		calcPieceMoves(false, Color.WHITE);
		ArrayList<SimpleEntry<Integer, Integer>> temp = new ArrayList<>();
		for (SimpleEntry<Integer, Integer> pair : whiteMoves) {
			temp.add(new SimpleEntry<>(pair.getKey(), pair.getValue()));
		}
		return temp;
	}

	public ArrayList<SimpleEntry<Integer, Integer>> getBlackMoves() {
		calcPieceMoves(false, Color.BLACK);
		ArrayList<SimpleEntry<Integer, Integer>> temp = new ArrayList<>();
		for (SimpleEntry<Integer, Integer> pair : blackMoves) {
			temp.add(new SimpleEntry<>(pair.getKey(), pair.getValue()));
		}
		return temp;
	}

	public void calcNumOfPieces() {
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (pieces[i][j] != null) {
					if (pieces[i][j].getColor() == Color.BLACK) {
						numOfBlackPieces = getNumOfPieces(Color.BLACK) + 1;
					} else {
						numOfWhitePieces = getNumOfPieces(Color.WHITE) + 1;
					}
				}
			}
		}
	}

	Board(Piece[][] pieces) {
		this.pieces = pieces;
	}

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

	public boolean move(int startX, int startY, int endX, int endY, Color currColor) {
		if (pieces[startX][startY] != null && pieces[startX][startY].getColor() == currColor
				&& pieces[startX][startY].getValidMoves(this.copy(), false).contains(new SimpleEntry<>(endX, endY))) {
			Piece piece = pieces[startX][startY];
			if (piece instanceof Pawn) {
				if (piece.getColor() == Color.WHITE && Math.abs(startY - endY) == 1 && endX - startX == 1
						&& pieces[endX][endY] == null) {
					pieces[enPassant.getKey()][enPassant.getValue()] = null;
				} else if (piece.getColor() == Color.BLACK && Math.abs(startY - endY) == 1 && startX - endX == 1
						&& pieces[endX][endY] == null) {
					pieces[enPassant.getKey()][enPassant.getValue()] = null;
				}
			} else if(piece instanceof King) {
				if(endY-startY == 2) {
					((King)pieces[startX][startY]).setHasMoved(true);
					Rook castle = ((Rook)pieces[startX][startY+3]);
					castle.setHasMoved(true);
					pieces[startX][startY+1] = castle;
					pieces[startX][startY+3] = null;
					castle.setRank(startX);
					castle.setFile(startY+1);					
				} else if(startY-endY == 2) {
					((King)pieces[startX][startY]).setHasMoved(true);
					Rook castle = ((Rook)pieces[startX][startY-4]);
					castle.setHasMoved(true);
					pieces[startX][startY-1] = castle;
					pieces[startX][startY-4] = null;
					castle.setRank(startX);
					castle.setFile(startY-1);	
					
				} else {
					((King)pieces[startX][startY]).setHasMoved(true);
				}
			} else if(piece instanceof Rook) {
				((Rook)piece).setHasMoved(true);
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
				} else if(pawn.getMadeFirstMove() == true) {
					checkPromotion(endX, endY);
				}
			}
			blackMoves.clear();
			whiteMoves.clear();
			return true;
		}
		return false;
	}
	
	public void checkPromotion(int rank, int file) {
		Pawn pawn = (Pawn) pieces[rank][file];
		if(pawn.getColor() == Color.WHITE && rank == 7) {
			pieces[rank][file] = new Queen(pawn);
		} else if(pawn.getColor() == Color.BLACK && rank == 0) {
			pieces[rank][file] = new Queen(pawn);
		}
	}

	public void updateEnPassant() {
		if (enPassant != null) {
			if (pieces[enPassant.getKey()][enPassant.getValue()] != null) {
				((Pawn) pieces[enPassant.getKey()][enPassant.getValue()]).canEnPassant(false);
			}
			enPassant = null;
		}
	}

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

	public boolean isKingInCheck(Color colorOfKing) {
		if (getKing(colorOfKing) != null) {
			if (colorOfKing == Color.WHITE) {
				calcPieceMoves(true, Color.BLACK);
				return getKing(colorOfKing).isInCheck(blackMoves);
			} else {
				calcPieceMoves(true, Color.WHITE);
				return getKing(colorOfKing).isInCheck(whiteMoves);
			}
		}
		return true;
	}

	public void setPieces(Piece[][] pieces) {
		this.pieces = pieces;
	}

	public Piece[][] getPieces() {
		return pieces;
	}

	public Piece getPiece(int rank, int file) {
		return pieces[rank][file];
	}

	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file, Color currMove) {
		if (pieces[rank][file] != null) {
			if (pieces[rank][file].getColor() == currMove) {
				return pieces[rank][file].getValidMoves(this.copy(), false);
			}
		}
		return new ArrayList<SimpleEntry<Integer, Integer>>();
	}

	public void calcPieceMoves(boolean kingFlag, Color color) {
		whiteMoves.clear();
		blackMoves.clear();
		ArrayList<SimpleEntry<Integer, Integer>> moves;
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				if(pieces[i][j] != null) {
					if (pieces[i][j].getColor() == Color.WHITE && pieces[i][j].getColor() == color) {
						moves = pieces[i][j].getValidMoves(this.copy(), kingFlag);
						for (SimpleEntry<Integer, Integer> move : moves) {
							whiteMoves.add(move);
						}
					} else if (pieces[i][j].getColor() == color){
						moves = pieces[i][j].getValidMoves(this.copy(), kingFlag);
						for (SimpleEntry<Integer, Integer> move : moves) {
							blackMoves.add(move);
						}
					}
				}
			}
		}
	}
	
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

	public int getNumOfPieces(Color color) {
		return color == Color.WHITE ? numOfWhitePieces : numOfBlackPieces;
	}
}
