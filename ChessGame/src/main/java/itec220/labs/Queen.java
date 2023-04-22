package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * A class to handle the logic of a queen piece in chess
 * @author Donovan Horne
 *
 */
public class Queen extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };

	
	/**
	 * A constructor for a new queen
	 * @param color the color of the queen
	 * @param rank the row of the queen
	 * @param file the column of the queen
	 */
	Queen(Color color, int rank, int file) {
		super(PieceType.QUEEN, color, rank, file);
	}
	
	/**
	 * Used when promoting a pawn to a queen
	 * @param pawn the pawn that is being promoted
	 */
	Queen(Pawn pawn){
		super(PieceType.QUEEN, pawn.getColor(), pawn.getRank(), pawn.getFile());
	}

	/**
	 * return a list of valid moves for the queen using the row/column offsets
	 * @param copy a copy of the current board state
	 * @param kingCheck set to true if you want to allow the move to be valid without checking
	 * 			to see if it will put the queen in check
	 * @return Return a list of valid moves for the piece
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();

		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile > copy.BOARD_SIZE || curRank < 0) {
			System.out.println("Something went wrong with the queen at " + this);
		} else {
			for (int check = 0; check < RANK_OFFSETS.length; check++) {
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

	
	/**
	 * Convert the queen to a string using the rank and file
	 * @return Return a string of the piece
	 */
	@Override
	public String toString() {
		return "Q" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
