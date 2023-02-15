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

	Game() {
		board = new Board();
		currState = GameState.IN_PROGRESS;
		currMove = Color.WHITE;
	}
	
	public void promote(int rank, int file, PieceType type) {
		board.promote(rank, file, type);
	}

	public Color getCurrMove() {
		return currMove;
	}

	public GameState getCurrState() {
		return currState;
	}

	public ArrayList<SimpleEntry<Integer, Integer>> getValidMoves(int rank, int file) {
		return board.getValidMoves(rank, file, currMove);
	}

	public boolean gameOver() {
		if (currState == GameState.BLACKWINS || currState == GameState.WHITEWINS || currState == GameState.DRAW
				|| currState == GameState.STALEMATE) {
			return true;
		}
		return false;
	}

	public boolean move(int startX, int startY, int endX, int endY) {
		if (board.move(startX, startY, endX, endY, currMove)) {
			currState = updateGameState();
			if (currMove == Color.BLACK) {
				updateMoveTracker(board.getBoardString());
			}
			currMove = currMove == Color.WHITE ? Color.BLACK : Color.WHITE;
			return true;
		} else {
			return false;
		}
	}

	
	// game speed issues happen here
	public GameState updateGameState() {
		GameState tempState = GameState.IN_PROGRESS;
		if (currMove == Color.WHITE && this.currState != GameState.WHITEINCHECK) {
			if (board.isKingInCheck(Color.BLACK)) { // this is causing game speed errors
				tempState = GameState.BLACKINCHECK;		
			
				if (board.getBlackMoves().size() == 0) {
					tempState = GameState.WHITEWINS;
				}
			} else if (!board.isKingInCheck(Color.BLACK) && board.getBlackMoves().size() == 0) {
				System.out.println("here");
				tempState = GameState.STALEMATE;
			} else if (board.getNumOfPieces(Color.BLACK) == 1 && board.getNumOfPieces(Color.WHITE) == 1) {
				tempState = GameState.DRAW;
			}
		} else if(this.currState != GameState.BLACKINCHECK) {
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

	public void updateMoveTracker(String newBoardState) {
		if (boardStates.size() < 4) {
			boardStates.add(newBoardState);
		} else {
			if (Collections.frequency(boardStates, newBoardState) == 2) {
				currState = GameState.DRAW;
			} else {
				boardStates.remove(0);
				boardStates.add(newBoardState);
			}
		}
	}

	public Board getCopyOfCurrBoard() {
		return board.copy();
	}
	
	public int getNumTakenPieces() {
		return board.getNumOfTakenPieces();
	}

	public GameState getGameState() {
		return this.currState;
	}
}
