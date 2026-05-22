package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class KingRulesTest {
	@Test
	void kingsMayNotMoveAdjacentToEachOther() {
		Game game = TestSupport.gameFromFen("8/8/8/8/8/8/6k1/4K3 w - - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves = game.getValidMoves(0, 4);

		assertFalse(TestSupport.containsMove(moves, 0, 5));
		assertFalse(TestSupport.containsMove(moves, 1, 5));
		assertTrue(TestSupport.containsMove(moves, 1, 4));
	}

	@Test
	void kingMayNotCaptureIntoAnAttackedSquare() {
		Game game = TestSupport.gameFromFen("4r2k/8/8/8/8/8/4q3/4K3 w - - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves = game.getValidMoves(0, 4);

		assertFalse(TestSupport.containsMove(moves, 1, 4));
	}

	@Test
	void movingOntoTheOpposingKingSquareIsNotLegal() {
		Game game = TestSupport.gameFromFen("4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1");

		assertFalse(TestSupport.containsMove(game.getValidMoves(1, 4), 7, 4));
	}
}
