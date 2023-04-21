/* Author:	Donovan Horne
 * Purpose:	Create an enum that will store the different game states, checkmate, draw, etc.
 * Date:	4/20/2023
 */

package itec220.labs;

public enum GameState {
	IN_PROGRESS, WHITEINCHECK, BLACKINCHECK, STALEMATE, WHITEWINS, BLACKWINS, DRAW;

	GameState() {

	}
}
