package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public abstract class Piece {
	private int rank, file;
	private PieceType type;
	private Color color;
	protected final char[] colomnLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

	Piece(PieceType type, Color color, int rank, int file) {
		this.type = type;
		this.color = color;
		this.rank = rank;
		this.file = file;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public void setFile(int file) {
		this.file = file;
	}

	public boolean isValidMove(int newRank, int newFile, Board board) {
		Board copy = board.copy();
		Piece[][] pieces = copy.getPieces();
		pieces[this.rank][this.file] = null;
		pieces[newRank][newFile] = this;
		int origRank = this.rank;
		int origFile = this.file;
		
		this.setRank(newRank);
		this.setFile(newFile);
		copy.calcPieceMoves(true, color == Color.WHITE ? Color.BLACK : Color.WHITE);
		
		this.setRank(origRank);
		this.setFile(origFile);
		return !copy.isKingInCheck(color == Color.WHITE ? Color.BLACK : Color.WHITE);
	}

	public abstract ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck);

	public int getRank() {
		return rank;
	}

	public int getFile() {
		return file;
	}

	public PieceType getType() {
		return type;
	}

	public Color getColor() {
		return color;
	}
}
