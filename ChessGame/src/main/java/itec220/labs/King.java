package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * To handle the logic of the king chess piece
 * @author Donovan Horne
 *
 */
public class King extends Piece {
	private static final int[] RANK_OFFSETS = { 0, 0, 1, -1, 1, 1, -1, -1 };
	private static final int[] FILE_OFFSETS = { 1, -1, 0, 0, 1, -1, 1, -1 };
	private boolean hasMoved = false;

	/**
	 * Constructor for a new King
	 * @param color color of the king
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 */
	King(Color color, int rank, int file) {
		super(PieceType.KING, color, rank, file);
	}
	
	/**
	 * Constructor for creating a deep copy of the king
	 * @param king the king you are creating a copy from
	 */
	King(King king){
		super(PieceType.KING, king.getColor(), king.getRank(), king.getFile());
		this.setHasMoved(king.getHasMoved());
	}

	/**
	 * Used to see if the king is in check by checking offsets to see if a piece of the other color is there, and is
	 * 		the correct instance piece
	 * @param copy Copy of the board used to check pieces on the board
	 * @return Returns a boolean of whether the King is in check
	 */
	public boolean isInCheck(Board copy) {
		Color attackingColor = this.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE;
		return copy.isSquareAttacked(this.getRank(), this.getFile(), attackingColor);
	}

	/**
	 * Used to get the valid moves for a king using the offsets declared
	 * @param copy a copy of the current game's board state
	 * @param kingCheck true if you don't care if the king is in check, false if you want to check
	 * @return Return an ArrayList of valid moves
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board board, boolean kingCheck) {
		Piece[][] pieces = board.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();
		if (curRank >= board.BOARD_SIZE || curRank < 0 || curFile >= board.BOARD_SIZE || curFile < 0) {
			System.out.println("Something went wrong with the king at " + this);
		} else {
			for (int i = 0; i < RANK_OFFSETS.length; i++) {
				int newRank = curRank + (1 * RANK_OFFSETS[i]);
				int newFile = curFile + (1 * FILE_OFFSETS[i]);
				if (newRank >= board.BOARD_SIZE || newRank < 0 || newFile >= board.BOARD_SIZE || newFile < 0) {
					continue;
				}
				if (pieces[newRank][newFile] == null || pieces[newRank][newFile].getColor() != this.getColor()) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					} else if (this.isValidMove(newRank, newFile, board)) {
						moves.add(new SimpleEntry<>(newRank, newFile));
					}
				}
			}
			addCastleMove(board, pieces, moves, kingCheck, curRank, curFile, 1, 3, 2);
			addCastleMove(board, pieces, moves, kingCheck, curRank, curFile, -1, 4, -2);
		}
		return moves;
	}

	private void addCastleMove(Board board, Piece[][] pieces, ArrayList<SimpleEntry<Integer, Integer>> moves,
			boolean kingCheck, int curRank, int curFile, int direction, int rookOffset, int kingOffset) {
		int rookFile = curFile + (direction * rookOffset);
		int destinationFile = curFile + kingOffset;
		if (this.hasMoved || rookFile < 0 || rookFile >= board.BOARD_SIZE || destinationFile < 0
				|| destinationFile >= board.BOARD_SIZE) {
			return;
		}
		for (int file = curFile + direction; file != rookFile; file += direction) {
			if (pieces[curRank][file] != null) {
				return;
			}
		}
		if (!(pieces[curRank][rookFile] instanceof Rook)) {
			return;
		}
		Rook rook = (Rook) pieces[curRank][rookFile];
		if (rook.getColor() != this.getColor() || rook.getHasMoved()) {
			return;
		}
		if (kingCheck) {
			moves.add(new SimpleEntry<>(curRank, destinationFile));
			return;
		}
		Color opposingColor = this.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE;
		if (board.isSquareAttacked(curRank, curFile, opposingColor)
				|| board.isSquareAttacked(curRank, curFile + direction, opposingColor)
				|| board.isSquareAttacked(curRank, destinationFile, opposingColor)) {
			return;
		}
		Board simulatedBoard = board.simulateMove(curRank, curFile, curRank, destinationFile);
		if (simulatedBoard != null && !simulatedBoard.isKingInCheck(this.getColor())) {
			moves.add(new SimpleEntry<>(curRank, destinationFile));
		}
	}

	
	/**
	 * Convert the current piece to a string
	 * @return return a string of the piece
	 */
	@Override
	public String toString() {
		return "K" + columnLetters[this.getFile()] + (this.getRank() + 1);
	}

	/**
	 * return whether the king has moved, used for castling
	 * @return return a boolean of whether the king has moved
	 */
	public boolean getHasMoved() {
		return hasMoved;
	}

	/**
	 * set whether it has moved, used for castling
	 * @param hasMoved the value to set this.hasMoved with
	 */
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
}
