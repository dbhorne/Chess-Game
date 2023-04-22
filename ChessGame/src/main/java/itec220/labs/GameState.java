package itec220.labs;

/**
 * Purpose:	Create an enum that will store the different game states, checkmate, draw, etc.
 * Date:	4/20/2023
 * @author Donovan Horne
 */
public enum GameState {
	IN_PROGRESS, WHITEINCHECK, BLACKINCHECK, STALEMATE, WHITEWINS, BLACKWINS, DRAW;

	/**
	 * Construct each game state
	 */
	GameState() {

	}
}
