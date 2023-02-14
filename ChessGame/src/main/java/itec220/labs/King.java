package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class King extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };

	King(Color color, int rank, int file) {
		super(PieceType.KING, color, rank, file);
	}

	public boolean isInCheck(ArrayList<SimpleEntry<Integer, Integer>> possibleMoves) {
		return possibleMoves.contains(new SimpleEntry<>(this.getRank(), this.getFile()));
	}

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
				int newRank = curRank + (i * RANK_OFFSETS[i]);
				int newFile = curFile + (i * FILE_OFFSETS[i]);
				if (newRank >= copy.BOARD_SIZE || newRank < 0 || newFile >= copy.BOARD_SIZE || newFile < 0) {
					break;
				}
				if (pieces[newRank][newFile] == null || pieces[newRank][newFile].getColor() != this.getColor()) {
					if(kingCheck) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					} else if(this.isValidMove(newRank, newFile, copy)) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					}
				}
			}
		}

		return moves;
	}

	@Override
	public String toString() {
		return "K" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
