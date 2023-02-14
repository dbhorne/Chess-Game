package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class Pawn extends Piece {
	private int thisPawnID;
	private static int pawnID = 0;
	private boolean madeFirstMove = false;
	private boolean canBeEnPassanted = false;

	Pawn(Color color, int rank, int file) {
		super(PieceType.PAWN, color, rank, file);
		thisPawnID = pawnID++;
	}

	Pawn(Pawn pawn) {
		super(PieceType.PAWN, pawn.getColor(), pawn.getRank(), pawn.getFile());
		this.thisPawnID = pawn.getThisPawnID();
		this.canBeEnPassanted = pawn.getEnPassant();
	}

	public void makeFirstMove() {
		this.madeFirstMove = true;
	}

	public boolean getMadeFirstMove() {
		return this.madeFirstMove;
	}

	public void canEnPassant(Boolean flag) {
		this.canBeEnPassanted = flag;
	}

	public boolean getEnPassant() {
		return this.canBeEnPassanted;
	}

	public static void resetPawnID() {
		pawnID = 0;
	}

	@Override
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(Board copy, boolean kingCheck) {
		Piece[][] pieces = copy.getPieces();
		ArrayList<SimpleEntry<Integer, Integer>> moves = new ArrayList<>();
		int curRank = this.getRank();
		int curFile = this.getFile();
		if (curRank >= copy.BOARD_SIZE || curRank < 0 || curFile >= copy.BOARD_SIZE || curRank < 0) {
			System.out.println("Something went wrong in the pawn class, take a second to check on " + this);
		} else {
			if (this.getColor() == Color.WHITE) {
				if (pieces[curRank + 1][curFile] == null) {						// can white pawn move forward one space
					if (kingCheck) {
						moves.add(new SimpleEntry<>(curRank + 1, curFile));
					} else if (this.isValidMove(curRank + 1, curFile, copy)) {
						moves.add(new SimpleEntry<>(curRank + 1, curFile));
					}
				}
				if (curFile != 0 && curFile != 7) {								// check and see if the pawn is on the edge of the board
					if (pieces[curRank + 1][curFile + 1] != null				// check to see if the pawn to the upper right is not null and not white
							&& pieces[curRank + 1][curFile + 1].getColor() == Color.BLACK) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
						} else if (this.isValidMove(curRank + 1, curFile + 1, copy)) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
						}
					}
					if (pieces[curRank + 1][curFile - 1] != null				// check the upper left move
							&& pieces[curRank + 1][curFile - 1].getColor() == Color.BLACK) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
						} else if (this.isValidMove(curRank + 1, curFile - 1, copy)) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
						}
					}
					if (pieces[curRank][curFile + 1] != null && pieces[curRank][curFile + 1].getColor() == Color.BLACK
							&& pieces[curRank][curFile + 1] instanceof Pawn) {	// check enPassant to the right
						Pawn temp = (Pawn) pieces[curRank][curFile + 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
							} else if (this.isValidMove(curRank + 1, curFile + 1, copy)) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
							}
						}
					}
					if (pieces[curRank][curFile - 1] != null && pieces[curRank][curFile - 1].getColor() == Color.BLACK
							&& pieces[curRank][curFile - 1] instanceof Pawn) {	// check enPassant to the left
						Pawn temp = (Pawn) pieces[curRank][curFile - 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
							} else if (this.isValidMove(curRank + 1, curFile - 1, copy)) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
							}
						}
					}
				} else if (curFile == 0) {										// check to see if the pawn is on the left side of the board
					if (pieces[curRank + 1][curFile + 1] != null				// can it take a pawn up and to the right
							&& pieces[curRank + 1][curFile + 1].getColor() == Color.BLACK) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
						} else if (this.isValidMove(curRank + 1, curFile + 1, copy)) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
						}
					}
					if (pieces[curRank][curFile + 1] != null && pieces[curRank][curFile + 1].getColor() == Color.BLACK
							&& pieces[curRank][curFile + 1] instanceof Pawn) {	// check enPassant to the right
						Pawn temp = (Pawn) pieces[curRank][curFile + 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
							} else if (this.isValidMove(curRank + 1, curFile + 1, copy)) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile + 1));
							}
						}
					}
				} else if (curFile == 7) {										// if the pawn is on the right side of the board
					if (pieces[curRank + 1][curFile - 1] != null				// check the up and to the left space
							&& pieces[curRank + 1][curFile - 1].getColor() == Color.BLACK) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
						} else if (this.isValidMove(curRank + 1, curFile - 1, copy)) {
							moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
						}
					}
					if (pieces[curRank][curFile - 1] != null && pieces[curRank][curFile - 1].getColor() == Color.BLACK
							&& pieces[curRank][curFile - 1] instanceof Pawn) {	// check enPassant to the left
						Pawn temp = (Pawn) pieces[curRank][curFile - 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
							} else if (this.isValidMove(curRank + 1, curFile - 1, copy)) {
								moves.add(new SimpleEntry<>(curRank + 1, curFile - 1));
							}
						}
					}
				}
				if (!madeFirstMove) {
					if (pieces[curRank + 1][curFile] == null && pieces[curRank + 2][curFile] == null) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank + 2, curFile));
						} else if (this.isValidMove(curRank + 2, curFile, copy)) {
							moves.add(new SimpleEntry<>(curRank + 2, curFile));
						}
					}
				}

			} else if (this.getColor() == Color.BLACK) {
				if (pieces[curRank - 1][curFile] == null) {						// check to see if it can move forward one space
					if (kingCheck) {
						moves.add(new SimpleEntry<>(curRank - 1, curFile));
					} else if (this.isValidMove(curRank - 1, curFile, copy)) {
						moves.add(new SimpleEntry<>(curRank - 1, curFile));
					}
				}
				if (curFile != 0 && curFile != 7) {								// if the pawn isn't on the edge of the board
					if (pieces[curRank - 1][curFile + 1] != null				// check the space down and to the right
							&& pieces[curRank - 1][curFile + 1].getColor() == Color.WHITE) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
						} else if (this.isValidMove(curRank - 1, curFile + 1, copy)) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
						}
					}
					if (pieces[curRank - 1][curFile - 1] != null				// check the space down and to the left
							&& pieces[curRank - 1][curFile - 1].getColor() == Color.WHITE) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
						} else if (this.isValidMove(curRank - 1, curFile - 1, copy)) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
						}
					}
					if (pieces[curRank][curFile + 1] != null && pieces[curRank][curFile + 1].getColor() == Color.WHITE
							&& pieces[curRank][curFile + 1] instanceof Pawn) {	// check enPassant to the right
						Pawn temp = (Pawn) pieces[curRank][curFile + 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
							} else if (this.isValidMove(curRank - 1, curFile + 1, copy)) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
							}
						}
					}
					if (pieces[curRank][curFile - 1] != null && pieces[curRank][curFile - 1].getColor() == Color.WHITE
							&& pieces[curRank][curFile - 1] instanceof Pawn) {	// check enPassant to the left
						Pawn temp = (Pawn) pieces[curRank][curFile - 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
							} else if (this.isValidMove(curRank - 1, curFile - 1, copy)) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
							}
						}
					}
				} else if (curFile == 0) {										// if the pawn is on the left side of the board
					if (pieces[curRank - 1][curFile + 1] != null				// check the space down and to the right
							&& pieces[curRank - 1][curFile + 1].getColor() == Color.WHITE) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
						} else if (this.isValidMove(curRank - 1, curFile + 1, copy)) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
						}
					}
					if (pieces[curRank][curFile + 1] != null && pieces[curRank][curFile + 1].getColor() == Color.WHITE
							&& pieces[curRank][curFile + 1] instanceof Pawn) {	// check enPassant to the right
						Pawn temp = (Pawn) pieces[curRank][curFile + 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
							} else if (this.isValidMove(curRank - 1, curFile + 1, copy)) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile + 1));
							}
						}
					}
				} else if (curFile == 7) {										// if the pawn is on the right side of the board
					if (pieces[curRank - 1][curFile - 1] != null				// check the space down and to the left
							&& pieces[curRank - 1][curFile - 1].getColor() == Color.WHITE) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
						} else if (this.isValidMove(curRank - 1, curFile - 1, copy)) {
							moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
						}
					}
					if (pieces[curRank][curFile - 1] != null && pieces[curRank][curFile - 1].getColor() == Color.WHITE
							&& pieces[curRank][curFile - 1] instanceof Pawn) {	// check enPassant to the left
						Pawn temp = (Pawn) pieces[curRank][curFile - 1];
						if (temp.getEnPassant()) {
							if (kingCheck) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
							} else if (this.isValidMove(curRank - 1, curFile - 1, copy)) {
								moves.add(new SimpleEntry<>(curRank - 1, curFile - 1));
							}
						}
					}
				}
				if (!madeFirstMove) {
					if (pieces[curRank - 1][curFile] == null && pieces[curRank - 2][curFile] == null) {
						if (kingCheck) {
							moves.add(new SimpleEntry<>(curRank - 2, curFile));
						} else if (this.isValidMove(curRank - 2, curFile, copy)) {
							moves.add(new SimpleEntry<>(curRank - 2, curFile));
						}
					}
				}
			}
		}
		return moves;
	}

	public int getThisPawnID() {
		return thisPawnID;
	}

	@Override
	public String toString() {
		return "" + colomnLetters[this.getFile()] + (this.getRank() + 1);
	}
}
