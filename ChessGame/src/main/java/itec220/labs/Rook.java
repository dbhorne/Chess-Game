/* Author: 	Donovan Horne
 * Purpose:	To handle the logic of a rook piece in chess
 * Date:	4/20/2023
 */

package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Rook extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0 };
	private boolean hasMoved;

	/* Constructor of a new rook
	 * @param color the color of the rook
	 * @param rank the row of the new rook
	 * @param file the column of the new rook
	 */
	Rook(Color color, int rank, int file) {
		super(PieceType.ROOK, color, rank, file);
	}
	
	/* Used when promoting a pawn to a rook
	 * @param pawn the pawn that is being promoted
	 */
	Rook(Pawn pawn){
		super(PieceType.ROOK, pawn.getColor(), pawn.getRank(), pawn.getFile());
	}
	
	/* Used when creating a deep copy of a rook
	 * @param rook the rook you are creating a copy from
	 */
	Rook(Rook rook){
		super(PieceType.ROOK, rook.getColor(), rook.getRank(), rook.getFile());
		this.setHasMoved(rook.getHasMoved());
	}

	/* Used to get a list of the rooks valid moves, using the row/column offsets until reaching a piece
	 * @param copy a copy of the current board state
	 * @param kingCheck set to true if you want to allow the move to be valid without checking
	 * 			to see if it will put the king in check
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();

		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile > copy.BOARD_SIZE || curRank < 0) {
			System.out.println("Something went wrong with the rook at " + this);
		} else {
			for (int check = 0; check < 4; check++) {
				int rankOffset = RANK_OFFSETS[check];
				int fileOffset = FILE_OFFSETS[check];

				for (int i = 1; i < copy.BOARD_SIZE; i++) {
					int newRank = curRank + (i * rankOffset);
					int newFile = curFile + (i * fileOffset);

					if (newRank < 0 || newRank >= copy.BOARD_SIZE || newFile < 0 || newFile >= copy.BOARD_SIZE) {
						break;
					}
					if (pieces[newRank][newFile] == null) {
						if(kingCheck) {
							moves.add(new SimpleEntry<>(newRank, newFile));
						} else if(this.isValidMove(newRank, newFile, copy)) {
							moves.add(new SimpleEntry<>(newRank, newFile));
						}
					} else {
						if (pieces[newRank][newFile].getColor() != this.getColor()) {
							if(kingCheck) {
								moves.add(new SimpleEntry<>(newRank, newFile));								
							} else if(this.isValidMove(newRank, newFile, copy)) {
								moves.add(new SimpleEntry<>(newRank, newFile));
							}
						}
						break;
					}
				}
			}
		}

		return moves;
	}

	// Return a string of the rook using it's row and column
	@Override
	public String toString() {
		return "R" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}

	// Return whether the rook has moved, used for castling
	public boolean getHasMoved() {
		return hasMoved;
	}

	// Set whether the rook has moved, used for castling
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
}
