package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Knight extends Piece {

	Knight(Color color, int rank, int file) {
		super(PieceType.KNIGHT, color, rank, file);
	}

	Knight(Pawn pawn){
		super(PieceType.KNIGHT, pawn.getColor(), pawn.getRank(), pawn.getFile());
	}

	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();

		int[] rowOffsets = { 2, 1, -1, -2, -2, -1, 1, 2 };
		int[] colOffsets = { 1, 2, 2, 1, -1, -2, -2, -1 };

		for (int i = 0; i < rowOffsets.length; i++) {
			int row = curRank + rowOffsets[i];
			int col = curFile + colOffsets[i];
			if (row >= 0 && row <= 7 && col >= 0 && col <= 7) {
				if (pieces[row][col] == null || pieces[row][col].getColor() != this.getColor()) {
					if(kingCheck) {
						moves.add(new SimpleEntry<>(row, col));
					} else if(this.isValidMove(row, col, copy)) {
						moves.add(new SimpleEntry<>(row, col));
					}
				}
			}
		}

		return moves;
	}

	@Override
	public String toString() {
		return "N" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
