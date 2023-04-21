/* Author:	Donovan Horne
 * Purpose:	To create a class that communicated with the board, which will handle piece logic,
 * 			and allow for this class to handle game state logic, such as check, checkmate, etc.
 * Date:	4/20/2023	
 */

package itec220.labs;

/* Goals:
 * 1. finish with the logic of all the pieces
 * 		1. DONE!
 * 		2. DONE! (Partially, have to wait till I set up the UI to get an input for what piece you want to promote to)
 * 		3. DONE!
 *	2. After I finish with the logic, begin setting up the layout of the UI, the chess board, and the other parts of the 
 *		UI. The UI will have a start page that asks if you want to play human v human, or against a bot, this will
 *		come into play after I incorporate the bot into the game, this panel can also be used to display the winner 
 *		after a game is over
 *	3. After finishing the UI, then start incorporating the Game class into the UI with the buttons
 *	4. after I have a working model of the game, stress test it, and if it works on all the tests, begin creating
 *		a very simple bot. 
 */

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Game {
	private Board board;
	private GameState currState;
	private Color currMove;
	private LinkedList<String> boardStates = new LinkedList<>();

	// Constructor for the game, create a new board, and update the current move and game state
	Game() {
		board = new Board();
		currState = GameState.IN_PROGRESS;
		currMove = Color.WHITE;
	}

	/* Promote a pawn on the board
	 * @param rank row of the pawn
	 * @param file column of the pawn
	 * @param type the type of piece the pawn is promoting to
	 */
	public void promote(int rank, int file, PieceType type) {
		board.promote(rank, file, type);
	}

	// Get the color of the current move
	public Color getCurrMove() {
		return currMove;
	}

	// Get the current game state, i.e IN_PROGRESS
	public GameState getCurrState() {
		return currState;
	}

	/* Get the valid moves for a specific piece
	 * @param rank the row of the piece
	 * @param file the column of the piece
	 */
	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file) {
		return board.getValidMoves(rank, file, currMove);
	}

	// Return a boolean of whether the game is over or not
	public boolean gameOver() {
		if (currState == GameState.BLACKWINS || currState == GameState.WHITEWINS || currState == GameState.DRAW
				|| currState == GameState.STALEMATE) {
			return true;
		}
		return false;
	}

	/* Call a move on the board, if it is true, update game states, if it was an invalid move
	 * 		return false
	 * @param startX the starting row of the piece
	 * @param startY the starting column of the piece
	 * @param endX the destination row of the piece
	 * @param endY the destination row of the piece
	 */
	public boolean move(int startX, int startY, int endX, int endY) {
		if (board.move(startX, startY, endX, endY, currMove)) {
			currState = updateGameState();
			updateMoveTracker(board.getBoardString());
			currMove = currMove == Color.WHITE ? Color.BLACK : Color.WHITE;
			return true;
		} else {
			return false;
		}
	}

	/* game speed issues happen here
	 *  Update the current game state, speed issues happen because we calculate all the moves on the
	 *  the board
	 */
	
	public GameState updateGameState() {
		GameState tempState = GameState.IN_PROGRESS;
		if (currMove == Color.WHITE && this.currState != GameState.WHITEINCHECK) {
			if (board.isKingInCheck(Color.BLACK)) {
				tempState = GameState.BLACKINCHECK;

				if (board.getBlackMoves().size() == 0) {
					tempState = GameState.WHITEWINS;
				}
			} else if (!board.isKingInCheck(Color.BLACK) && board.getBlackMoves().size() == 0) {
				tempState = GameState.STALEMATE;
			} else if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
				tempState = GameState.DRAW;
			}
		} else if (this.currState != GameState.BLACKINCHECK) {
			if (board.isKingInCheck(Color.WHITE)) {
				tempState = GameState.WHITEINCHECK;
				if (board.getWhiteMoves().size() == 0) {
					tempState = GameState.BLACKWINS;
				}

			} else if (!board.isKingInCheck(Color.WHITE) && board.getWhiteMoves().size() == 0) {
				tempState = GameState.STALEMATE;
			} else if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
				tempState = GameState.DRAW;
			}
		}
		return tempState;
	}
	
	
	/* W.I.P Used to track 3 move repeting, should work
	 * @param newBoardState a string of the current board state
	 */
	public void updateMoveTracker(String newBoardState) {
		if(Collections.frequency(boardStates, newBoardState) == 2) {
			currState = GameState.DRAW;
		} else {
			boardStates.add(newBoardState);
		}
	}

	// Get a deep copy of the games current board
	public Board getCopyOfCurrBoard() {
		return board.copy();
	}

	// Get the number of taken pieces from the board
	public int getNumTakenPieces() {
		return board.getNumOfTakenPieces();
	}

	// return the current game state
	public GameState getGameState() {
		return this.currState;
	}
}
