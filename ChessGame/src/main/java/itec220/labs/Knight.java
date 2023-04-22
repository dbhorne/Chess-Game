

package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * Purpose:	To handle the logic of the knight piece in chess
 * Date: 	4/20/2023
 * @author Donovan Horne
 */
public class Knight extends Piece {

	/**
	 * Constructor for a new knight
	 * @param color the color of the new piece
	 * @param rank row of the piece
	 * @param file the column of the piece
	 */
	Knight(Color color, int rank, int file) {
		super(PieceType.KNIGHT, color, rank, file);
	}

	/**
	 * Constructor used if you are promoting from a pawn
	 * @param pawn pawn being used to promote from
	 */
	Knight(Pawn pawn){
		super(PieceType.KNIGHT, pawn.getColor(), pawn.getRank(), pawn.getFile());
	}

	/**
	 * Used to return a list of the valid moves for this piece
	 * @param copy a copy of the current board
	 * @param kingCheck set to true if you don't want to check for the king being in check
	 * @return Return an ArrayList of valid moved
	 */
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

	/**
	 * Return a string of the knight and position
	 * @return Return a string of the piece
	 */
	@Override
	public String toString() {
		return "N" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
