package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * To create a class that communicated with the board, which will handle piece logic,
 * 			and allow for this class to handle game state logic, such as check, checkmate, etc.
 * @author Donovan Horne
 *
 */
public class Game {
	private Board board;
	private GameState currState;
	private Color currMove;
	private LinkedList<String> boardStates = new LinkedList<>();
	private int halfMoveClock = 0;
	private int fullMoveNumber = 1;

	/**
	 * Constructor for the game, create a new board, and update the current move and game state
	 */
	Game() {
		board = new Board();
		currState = GameState.IN_PROGRESS;
		currMove = Color.WHITE;
	}

	/**
	 * Promote a pawn on the board
	 * @param rank row of the pawn
	 * @param file column of the pawn
	 * @param type the type of piece the pawn is promoting to
	 */
	public void promote(int rank, int file, PieceType type) {
		board.promote(rank, file, type);
	}

	/**
	 * Get the color of the current move
	 * @return Returns a Color enum
	 */
	public Color getCurrMove() {
		return currMove;
	}

	/**
	 * Get the current game state, i.e IN_PROGRESS
	 * @return Returns a GameState enum
	 */
	public GameState getCurrState() {
		return currState;
	}

	/**
	 * Get the valid moves for a specific piece
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 * @return Returns a ArrayList of valid moves for the given piece
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file) {
		return board.getValidMoves(rank, file, currMove);
	}

	/**
	 * Return a boolean of whether the game is over or not
	 * @return Returns a boolean, true if the game is over, false if it will continue
	 */
	public boolean gameOver() {
		if (currState == GameState.BLACKWINS || currState == GameState.WHITEWINS || currState == GameState.DRAW
				|| currState == GameState.STALEMATE) {
			return true;
		}
		return false;
	}

	/**
	 * Call a move on the board
	 * @param startX the starting row of the piece
	 * @param startY the starting column of the piece
	 * @param endX the destination row of the piece
	 * @param endY the destination column of the piece
	 * @return Return a boolean based on whether the move was made or not
	 */
	public boolean move(int startX, int startY, int endX, int endY) {
		int takenBefore = board.getNumOfTakenPieces();
		if (board.move(startX, startY, endX, endY, currMove)) {
			boolean isCapture = board.getNumOfTakenPieces() > takenBefore;
			boolean isPawnMove = board.getPiece(endX, endY) instanceof Pawn;
			if (isPawnMove || isCapture) {
				halfMoveClock = 0;
			} else {
				halfMoveClock++;
			}
			if (currMove == Color.BLACK) {
				fullMoveNumber++;
			}
			currState = updateGameState();
			updateMoveTracker(board.getBoardString());
			currMove = currMove == Color.WHITE ? Color.BLACK : Color.WHITE;
			return true;
		} else {
			return false;
		}
	}

	/** game speed issues happen here
	 *  Update the current game state, speed issues happen because we calculate all the moves on the
	 *  the board
	 *  @return Return the GameState enum
	 */
	public GameState updateGameState() {
		GameState tempState = GameState.IN_PROGRESS;
		if (currMove == Color.WHITE) {
			if (board.isKingInCheck(Color.BLACK)) {
				tempState = GameState.BLACKINCHECK;

				if (board.getBlackMoves().size() == 0) {
					tempState = GameState.WHITEWINS;
				}
			} else if (board.getBlackMoves().size() == 0) {
				tempState = GameState.STALEMATE;
			} else if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
				tempState = GameState.DRAW;
			}
		} else {
			if (board.isKingInCheck(Color.WHITE)) {
				tempState = GameState.WHITEINCHECK;
				if (board.getWhiteMoves().size() == 0) {
					tempState = GameState.BLACKWINS;
				}
			} else if (board.getWhiteMoves().size() == 0) {
				tempState = GameState.STALEMATE;
			} else if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
				tempState = GameState.DRAW;
			}
		}
		return tempState;
	}
	
	
	/** 
	 * W.I.P Used to track 3 move repeting, should work
	 * @param newBoardState a string of the current board state
	 */
	public void updateMoveTracker(String newBoardState) {
		if(Collections.frequency(boardStates, newBoardState) == 2) {
			currState = GameState.DRAW;
		} else {
			boardStates.add(newBoardState);
		}
	}

	/**
	 * Get a deep copy of the games current board
	 * @return Returns a copy of the current board
	 */
	public Board getCopyOfCurrBoard() {
		return board.copy();
	}

	/**
	 * Get the number of taken pieces from the board
	 * @return Returns an int of the number of taken pieces
	 */
	public int getNumTakenPieces() {
		return board.getNumOfTakenPieces();
	}

	/**
	 * return the current game state
	 * @return return the current game state enum
	 */
	public GameState getGameState() {
		return this.currState;
	}

	/**
	 * Serialize the current game state to a FEN string
	 * @return FEN string representing the current position
	 */
	public String toFEN() {
		StringBuilder sb = new StringBuilder();

		// 1. Piece placement
		sb.append(board.toFENPiecePlacement());
		sb.append(' ');

		// 2. Active color
		sb.append(currMove == Color.WHITE ? 'w' : 'b');
		sb.append(' ');

		// 3. Castling availability
		StringBuilder castling = new StringBuilder();
		Piece[][] pieces = board.getPieces();
		if (canCastle(pieces, 0, 4, 0, 7)) castling.append('K');
		if (canCastle(pieces, 0, 4, 0, 0)) castling.append('Q');
		if (canCastle(pieces, 7, 4, 7, 7)) castling.append('k');
		if (canCastle(pieces, 7, 4, 7, 0)) castling.append('q');
		sb.append(castling.length() == 0 ? "-" : castling.toString());
		sb.append(' ');

		// 4. En passant target square
		java.util.AbstractMap.SimpleEntry<Integer, Integer> ep = board.getEnPassant();
		if (ep == null) {
			sb.append('-');
		} else {
			int pawnRow = ep.getKey();
			int pawnCol = ep.getValue();
			Piece epPawn = board.getPiece(pawnRow, pawnCol);
			int targetRow = (epPawn != null && epPawn.getColor() == Color.WHITE) ? pawnRow - 1 : pawnRow + 1;
			sb.append((char) ('a' + pawnCol));
			sb.append((char) ('1' + targetRow));
		}
		sb.append(' ');

		// 5 & 6. Halfmove clock and fullmove number
		sb.append(halfMoveClock);
		sb.append(' ');
		sb.append(fullMoveNumber);

		return sb.toString();
	}

	private boolean canCastle(Piece[][] pieces, int kingRow, int kingCol, int rookRow, int rookCol) {
		Piece king = pieces[kingRow][kingCol];
		Piece rook = pieces[rookRow][rookCol];
		if (!(king instanceof King) || !(rook instanceof Rook)) return false;
		return !((King) king).getHasMoved() && !((Rook) rook).getHasMoved();
	}

	/**
	 * Return the halfmove clock (resets on pawn move or capture; used for 50-move rule)
	 * @return halfmove clock as an int
	 */
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	/**
	 * Return the fullmove number (starts at 1, increments after every black move)
	 * @return fullmove number as an int
	 */
	public int getFullMoveNumber() {
		return fullMoveNumber;
	}
}
