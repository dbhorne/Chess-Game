package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * To handle the logic of the king chess piece
 * @author Donovan Horne
 *
 */
public class King extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };
	private static final int[] RANK_CHECK_OFFSETS = {0, 0, 1, -1, 1, 1, -1, -1, 2, 1, -1, -2, -2, -1, 1, 2};
	private static final int[] FILE_CHECK_OFFSETS = {1, -1, 0, 0, 1, -1, 1, -1, 1, 2, 2, 1, -1, -2, -2, -1 };
	private boolean hasMoved = false;

	/**
	 * Constructor for a new King
	 * @param color color of the king
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 */
	King(Color color, int rank, int file) {
		super(PieceType.KING, color, rank, file);
	}
	
	/**
	 * Constructor for creating a deep copy of the king
	 * @param king the king you are creating a copy from
	 */
	King(King king){
		super(PieceType.KING, king.getColor(), king.getRank(), king.getFile());
		this.setHasMoved(king.getHasMoved());
	}

	/**
	 * Used to see if the king is in check by checking offsets to see if a piece of the other color is there, and is
	 * 		the correct instance piece
	 * @param copy Copy of the board used to check pieces on the board
	 * @return Returns a boolean of whether the King is in check
	 */
	public boolean isInCheck(Board copy) {
		Piece[][] pieces = copy.getPieces();
		
		int curRank = this.getRank();
		int curFile = this.getFile();
		for (int check = 0; check < RANK_CHECK_OFFSETS.length; check++) {
			int rankOffset = RANK_CHECK_OFFSETS[check];
			int fileOffset = FILE_CHECK_OFFSETS[check];

			for (int i = 1; i < copy.BOARD_SIZE; i++) {
				int newRank = curRank + (i * rankOffset);
				int newFile = curFile + (i * fileOffset);
				if(newRank > 7 || newFile > 7 || newRank < 0 || newFile < 0)
					break;
				
				if(pieces[newRank][newFile] != null) {
					if(check < 4) {
						if(pieces[newRank][newFile].getColor() != this.getColor() && (pieces[newRank][newFile] instanceof Rook || pieces[newRank][newFile] instanceof Queen)) {
							return true;							
						}
					} else if(check < 8) {
						if(pieces[newRank][newFile].getColor() != this.getColor()) {
							if(i == 1 && (pieces[newRank][newFile] instanceof Queen || pieces[newRank][newFile] instanceof Bishop || pieces[newRank][newFile] instanceof Pawn)) {
								return true;
							} else if(pieces[newRank][newFile] instanceof Queen || pieces[newRank][newFile] instanceof Bishop) {
								return true;
							}
						} 
					} else {
						if(pieces[newRank][newFile] != null && pieces[newRank][newFile].getColor() != this.getColor() && pieces[newRank][newFile] instanceof Knight) {
							return true;
						}
						break;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Used to get the valid moves for a king using the offsets declared
	 * @param copy a copy of the current game's board state
	 * @param kingCheck true if you don't care if the king is in check, false if you want to check
	 * @return Return an ArrayList of valid moves
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();
		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile > copy.BOARD_SIZE || curRank < 0) {
			System.out.println("Something went wrong with the king at " + this);
		} else {
			for (int i = 0; i < RANK_OFFSETS.length; i++) {
				int newRank = curRank + (1 * RANK_OFFSETS[i]);
				int newFile = curFile + (1 * FILE_OFFSETS[i]);
				if (newRank >= copy.BOARD_SIZE || newRank < 0 || newFile >= copy.BOARD_SIZE || newFile < 0) {
					continue;
				}
				if (pieces[newRank][newFile] == null || pieces[newRank][newFile].getColor() != this.getColor()) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					} else if (this.isValidMove(newRank, newFile, copy)) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					}
				}
			}
			if(curFile + 3 < 8) {
				if (!this.hasMoved && pieces[curRank][curFile + 1] == null && pieces[curRank][curFile + 2] == null
						&& pieces[curRank][curFile + 3] != null && pieces[curRank][curFile + 3] instanceof Rook) { // check
																													// castle
																													// to
																													// the
																													// right
					Rook castle = (Rook) pieces[curRank][curFile + 3];
					if (!castle.getHasMoved()) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank, curFile + 2));
						} else {
							Board copyOfCopy = copy.copy();
							copyOfCopy.move(curFile, curFile, curRank, curFile+2, getColor());
							copyOfCopy.move(curFile, curFile+3, curRank, curFile+1, getColor());
							if(!copyOfCopy.isKingInCheck(getColor())) {
								moves.add(new SimpleEntry<>(curRank, curFile + 2));
							}
						}
	
					}
				}
			}
			if(curFile - 4 >= 0) {
				if (!this.hasMoved && pieces[curRank][curFile - 1] == null && pieces[curRank][curFile - 2] == null
						&& pieces[curRank][curFile - 3] == null && pieces[curRank][curFile - 4] != null
						&& pieces[curRank][curFile - 4] instanceof Rook) {
					Rook castle = (Rook) pieces[curRank][curFile - 4]; // check castle to the left
					if(!castle.getHasMoved()) {
						if(kingCheck) {
							moves.add(new SimpleEntry<>(curRank, curFile - 2));
						} else {
							Board copyOfCopy = copy.copy();
							copyOfCopy.move(curFile, curFile, curRank, curFile-2, getColor());
							copyOfCopy.move(curFile, curFile-4, curRank, curFile-1, getColor());
							if(!copyOfCopy.isKingInCheck(getColor())) {
								moves.add(new SimpleEntry<>(curRank, curFile - 2));
							}
						}
					}
	
				}
			}
		}
		return moves;
	}

	
	/**
	 * Convert the current piece to a string
	 * @return return a string of the piece
	 */
	@Override
	public String toString() {
		return "K" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}

	/**
	 * return whether the king has moved, used for castling
	 * @return return a boolean of whether the king has moved
	 */
	public boolean getHasMoved() {
		return hasMoved;
	}

	/**
	 * set whether it has moved, used for castling
	 * @param hasMoved the value to set this.hasMoved with
	 */
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
}
