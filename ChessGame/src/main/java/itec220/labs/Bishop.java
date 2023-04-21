/* Author:  Donovan Horne
 * Purpose: To handle the logic for a bishop piece in chess
 * Date:	4/20/2023
 */

package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Bishop extends Piece {
	private static final int[] RANK_OFFSETS = { 1, -1, 1, -1 };
	private static final int[] FILE_OFFSETS = { 1, 1, -1, -1 };

	/* Constructor for a bishop with the position and color
	 * @param color for the color of the piece
	 * @param rank The rank of the piece being created
	 * @param file The file of the piece being created
	 */
	Bishop(Color color, int rank, int file) {
		super(PieceType.BISHOP, color, rank, file);
	}
	
	/* Constructor for a bishop being promoted from a pawn
	 * @param pawn The pawn that is being promoted to a bishop
	 */
	Bishop(Pawn pawn){
		super(PieceType.BISHOP, pawn.getColor(), pawn.getRank(), pawn.getFile());
	}

	/* Method to return a list of valid moves for a piece
	 * @param copy a copy of the current state of the board
	 * @param kingCheck used if you are checking to see if the king is in check, ignoring future moves
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();

		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile > copy.BOARD_SIZE || curRank < 0) {
			System.out.println("Something went wrong with the bishop at " + this);
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
							if(this.isValidMove(newRank, newFile, copy)) {
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

	// toString method for the bishop piece
	@Override
	public String toString() {
		return "B" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
