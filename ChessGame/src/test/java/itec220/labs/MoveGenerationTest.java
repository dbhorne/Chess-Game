package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class MoveGenerationTest {
	@Test
	void startingPositionReturnsAllLegalMovesForCurrentSide() {
		Game game = new Game();

		ArrayList<Move> moves = game.getLegalMoves();

		assertEquals(20, moves.size());
		for (Move move : moves) {
			assertTrue(TestSupport.containsMove(game.getValidMoves(move.startRank, move.startFile),
					move.endRank, move.endFile), "Move did not map to a valid destination: " + move);
		}
	}
}
