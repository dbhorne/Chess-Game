package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Queen extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };

	Queen(Color color, int rank, int file) {
		super(PieceType.QUEEN, color, rank, file);
	}

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

	@Override
	public String toString() {
		return "Q" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
