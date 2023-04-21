/* Author:	Donovan Horne
 * Purpose:	To handle the logic of the king chess piece
 * Date:	4/20/2023
 */

package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class King extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };
	private boolean hasMoved = false;

	/* Constructor for a new King
	 * @param color color of the king
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 */
	King(Color color, int rank, int file) {
		super(PieceType.KING, color, rank, file);
	}
	
	/* Constructor for creating a deep copy of the king
	 * @param king the king you are creating a copy from
	 */
	King(King king){
		super(PieceType.KING, king.getColor(), king.getRank(), king.getFile());
		this.setHasMoved(king.getHasMoved());
	}

	/* Used to see if the king is in check by checking a list to see if their position is in it
	 * @param possibleMoves the list of possible moves of the other color
	 */
	public boolean isInCheck(ArrayList<SimpleEntry<Integer, Integer>> possibleMoves) {
		return possibleMoves.contains(new SimpleEntry<>(this.getRank(), this.getFile()));
	}

	/* Used to get the valid moves for a king using the offsets declared
	 * @param copy a copy of the current game's board state
	 * @param kingCheck true if you don't care if the king is in check, false if you want to check
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

	
	// Convert the current piece to a string
	@Override
	public String toString() {
		return "K" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}

	// return whether the king has moved, used for castling
	public boolean getHasMoved() {
		return hasMoved;
	}

	// set whether it has moved, used for castling
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
}
