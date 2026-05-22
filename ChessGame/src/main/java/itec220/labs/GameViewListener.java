package itec220.labs;

/**
 * Notifications from the game controller to a view.
 */
interface GameViewListener {
	void onGameStateChanged(Move move, Board beforeMove);

	void onBotThinkingChanged(boolean thinking);

	void onInvalidMove(String message);
}
