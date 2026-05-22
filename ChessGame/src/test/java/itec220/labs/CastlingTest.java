package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class CastlingTest {
	@Test
	void castlingDoesNotRecurseOrCrash() {
		Game game = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves =
				assertDoesNotThrow(() -> game.getValidMoves(0, 4));

		assertTrue(TestSupport.containsMove(moves, 0, 6));
	}

	@Test
	void castlingIsDisallowedWhileInCheck() {
		Game game = TestSupport.gameFromFen("4r1k1/8/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(TestSupport.containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsDisallowedThroughCheck() {
		Game game = TestSupport.gameFromFen("5rk1/8/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(TestSupport.containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsDisallowedIntoCheck() {
		Game game = TestSupport.gameFromFen("6r1/k7/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(TestSupport.containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsAllowedWhenAllConditionsAreSatisfied() {
		Game game = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");

		assertTrue(TestSupport.containsMove(game.getValidMoves(0, 4), 0, 6));
		assertTrue(game.move(0, 4, 0, 6));
		assertInstanceOf(King.class, game.getCopyOfCurrBoard().getPiece(0, 6));
		assertInstanceOf(Rook.class, game.getCopyOfCurrBoard().getPiece(0, 5));
	}
}
