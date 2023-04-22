package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * Create a parent class for all the pieces that will store general information about 
 * 			the piece that has been created
 * @author Donovan Horne
 *
 */
public abstract class Piece {
	private int rank, file;
	private PieceType type;
	private Color color;
	protected final char[] colomnLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

	/**
	 * Constructor for every piece
	 * @param type the type of the piece
	 * @param color the color of the piece
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 */
	Piece(PieceType type, Color color, int rank, int file) {
		this.type = type;
		this.color = color;
		this.rank = rank;
		this.file = file;
	}

	/**
	 * Set the rank of the current piece, used for moves
	 * @param rank the new row of the piece
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Set the file of the current piece, used for moves
	 * @param file the new column of the piece 
	 */
	public void setFile(int file) {
		this.file = file;
	}

	/**
	 * Used to see if the move that is being made is going to put the king in check
	 * @param newRank the new row of the piece
	 * @param newFile the new column of the piece
	 * @param board a current board state, we create a copy to not make any changes
	 * @return Return a boolean of whether the move is valid
	 */
	public boolean isValidMove(int newRank, int newFile, Board board) {
		Board copy = board.copy();
		Piece[][] pieces = copy.getPieces();
		pieces[this.rank][this.file] = null;
		pieces[newRank][newFile] = this;
		int origRank = this.rank;
		int origFile = this.file;

		this.setRank(newRank);
		this.setFile(newFile);
		copy.calcPieceMoves(true, this.color == Color.WHITE ? Color.BLACK : Color.WHITE);
		boolean temp = !copy.isKingInCheck(this.color);
		if (board.getKing(this.color == Color.WHITE ? Color.BLACK : Color.WHITE) != null
				&& board.getKing(this.color == Color.WHITE ? Color.BLACK : Color.WHITE).getRank() == newRank
				&& board.getKing(this.color == Color.WHITE ? Color.BLACK : Color.WHITE).getFile() == newFile) {
			temp = true;
		}

		this.setRank(origRank);
		this.setFile(origFile);
		return temp;
	}

	/**
	 * A method for every piece to determine their valid moves
	 * @param copy a copy of the current board state
	 * @param kingCheck set to true if you wish to allow the move to be valid without checking to
	 * 			see if it will put the king in check
	 * @return Return a list of valid moves for the piece
	 */
	public abstract ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck);

	
	/**
	 * Return the current rank/row of the piece
	 * @return Return the rank/row as an int
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Return the current file/column of the piece
	 * @return Return the file/column as an int
	 */
	public int getFile() {
		return file;
	}

	/**
	 * Return the type of the piece
	 * @return Return a PieceType enum of the piece
	 */
	public PieceType getType() {
		return type;
	}

	/**
	 * Return the color of the piece
	 * @return Return a Color enum from the piece
	 */
	public Color getColor() {
		return color;
	}
}
