package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * To handle the game logic for a pawn
 * @author Donovan Horne
 *
 */
public class Pawn extends Piece {
	private int thisPawnID;
	private static int pawnID = 0;
	private boolean madeFirstMove = false;
	private boolean canBeEnPassanted = false;

	/**
	 * Constructor for creating a new pawn also incrementing the pawnID
	 * @param color the color of the new pawn
	 * @param rank the row of the pawn
	 * @param file the column of the pawn
	 */
	Pawn(Color color, int rank, int file) {
		super(PieceType.PAWN, color, rank, file);
		thisPawnID = pawnID++;
	}

	/**
	 * Constructor used when creating a deep copy of the pawn
	 * @param pawn the pawn you are creating a copy of
	 */
	Pawn(Pawn pawn) {
		super(PieceType.PAWN, pawn.getColor(), pawn.getRank(), pawn.getFile());
		this.thisPawnID = pawn.getThisPawnID();
		this.madeFirstMove = pawn.getMadeFirstMove();
		this.canBeEnPassanted = pawn.getEnPassant();
	}

	/**
	 * Called when the pawn makes it's first move
	 */
	public void makeFirstMove() {
		this.madeFirstMove = true;
	}

	/**
	 * Returns whether the pawn has made a move yet or not
	 * @return Return a boolean of whether the pawn has moved
	 */
	public boolean getMadeFirstMove() {
		return this.madeFirstMove;
	}

	/**
	 * Set the canBeEnPassanted flag
	 * @param flag the flag you want to set this.canBeEnPassanted to
	 */
	public void canEnPassant(Boolean flag) {
		this.canBeEnPassanted = flag;
	}

	/**
	 * Return whether the pawn can be en passanted
	 * @return Return boolean value of this.canBeEnPassanted
	 */
	public boolean getEnPassant() {
		return this.canBeEnPassanted;
	}

	/**
	 * Resets the static variable pawnID
	 */
	public static void resetPawnID() {
		pawnID = 0;
	}

	
	/**
	 * Returns a list of valid moves for the pawn
	 * @param copy a copy of the current games board
	 * @param kingCheck set to true if you do not wish to see if the move will put the king in check
	 * @return Return an ArrayList of valid moves for the piece
	 */
	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();
		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile >= copy.BOARD_SIZE || curFile < 0) {
			System.out.println("Something went wrong in the pawn class, take a second to check on " + this);
		} else {
			if (this.getColor() == Color.WHITE) {
				int fwd = curRank + 1;
				if (fwd < 8 && pieces[fwd][curFile] == null) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(fwd, curFile));
					} else if (this.isValidMove(fwd, curFile, copy)) {
						moves.add(new SimpleEntry<>(fwd, curFile));
					}
				}
				for (int df : new int[]{-1, 1}) {
					int diagFile = curFile + df;
					if (diagFile < 0 || diagFile >= 8) continue;
					if (fwd < 8 && pieces[fwd][diagFile] != null && pieces[fwd][diagFile].getColor() == Color.BLACK) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(fwd, diagFile));
						} else if (this.isValidMove(fwd, diagFile, copy)) {
							moves.add(new SimpleEntry<>(fwd, diagFile));
						}
					}
					if (pieces[curRank][diagFile] != null && pieces[curRank][diagFile].getColor() == Color.BLACK
							&& pieces[curRank][diagFile] instanceof Pawn) {
						Pawn temp = (Pawn) pieces[curRank][diagFile];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(fwd, diagFile));
							} else if (this.isValidMove(fwd, diagFile, copy)) {
								moves.add(new SimpleEntry<>(fwd, diagFile));
							}
						}
					}
				}
				if (!madeFirstMove && fwd < 8 && curRank + 2 < 8
						&& pieces[fwd][curFile] == null && pieces[curRank + 2][curFile] == null) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(curRank + 2, curFile));
					} else if (this.isValidMove(curRank + 2, curFile, copy)) {
						moves.add(new SimpleEntry<>(curRank + 2, curFile));
					}
				}

			} else if (this.getColor() == Color.BLACK) {
				int fwd = curRank - 1;
				if (fwd >= 0 && pieces[fwd][curFile] == null) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(fwd, curFile));
					} else if (this.isValidMove(fwd, curFile, copy)) {
						moves.add(new SimpleEntry<>(fwd, curFile));
					}
				}
				for (int df : new int[]{-1, 1}) {
					int diagFile = curFile + df;
					if (diagFile < 0 || diagFile >= 8) continue;
					if (fwd >= 0 && pieces[fwd][diagFile] != null && pieces[fwd][diagFile].getColor() == Color.WHITE) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(fwd, diagFile));
						} else if (this.isValidMove(fwd, diagFile, copy)) {
							moves.add(new SimpleEntry<>(fwd, diagFile));
						}
					}
					if (pieces[curRank][diagFile] != null && pieces[curRank][diagFile].getColor() == Color.WHITE
							&& pieces[curRank][diagFile] instanceof Pawn) {
						Pawn temp = (Pawn) pieces[curRank][diagFile];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(fwd, diagFile));
							} else if (this.isValidMove(fwd, diagFile, copy)) {
								moves.add(new SimpleEntry<>(fwd, diagFile));
							}
						}
					}
				}
				if (!madeFirstMove && fwd >= 0 && curRank - 2 >= 0
						&& pieces[fwd][curFile] == null && pieces[curRank - 2][curFile] == null) {
					if (kingCheck) {
						moves.add(new SimpleEntry<>(curRank - 2, curFile));
					} else if (this.isValidMove(curRank - 2, curFile, copy)) {
						moves.add(new SimpleEntry<>(curRank - 2, curFile));
					}
				}
			}
		}
		return moves;
	}

	
	/**
	 * Return the current pawns ID
	 * @return return the pawnsID as an int
	 */
	public int getThisPawnID() {
		return thisPawnID;
	}

	/**
	 * Convert the pawn to a string using the rnak and file
	 * @return Returns a string of the piece
	 */
	@Override
	public String toString() {
		return "" + columnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
