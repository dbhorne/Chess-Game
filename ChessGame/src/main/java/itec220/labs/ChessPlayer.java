package itec220.labs;

import java.util.ArrayList;
import java.util.List;

/**
 * A controller for one side of a chess game.
 */
interface ChessPlayer {
	Color getColor();

	boolean isHuman();

	Move chooseMove(ArrayList<Move> legalMoves, Board boardSnapshot, List<String> priorPositions);
}
