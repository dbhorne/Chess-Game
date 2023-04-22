package itec220.labs;

/**
 * Create an enum that will store the different game states, checkmate, draw, etc.
 * @author Donovan Horne
 */
public enum GameState {
	/** The game is still in progress */
	IN_PROGRESS, 
	/** The white player is in check */
	WHITEINCHECK, 
	/** The black player is in check */
	BLACKINCHECK, 
	/** The game is over, ended in stalemate */
	STALEMATE, 
	/** The game is over, white wins */
	WHITEWINS, 
	/** The game is over, black wins */
	BLACKWINS, 
	/** The game is over, it was a draw */
	DRAW;

	/**
	 * Construct each game state
	 */
	GameState() {

	}
}
