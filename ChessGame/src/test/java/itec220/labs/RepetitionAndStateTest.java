package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RepetitionAndStateTest {
	@Test
	void threefoldRepetitionTracksFullPositionIdentity() {
		Game game = TestSupport.gameFromFen("4k1n1/8/8/8/8/8/8/4K1N1 w - - 0 1");

		game.move(0, 6, 2, 5);
		game.move(7, 6, 5, 5);
		game.move(2, 5, 0, 6);
		game.move(5, 5, 7, 6);
		game.move(0, 6, 2, 5);
		game.move(7, 6, 5, 5);
		game.move(2, 5, 0, 6);
		game.move(5, 5, 7, 6);

		assertEquals(GameState.DRAW, game.getCurrState());
	}

	@Test
	void repetitionIdentityDistinguishesSideToMove() {
		Game whiteToMove = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
		Game blackToMove = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/4K3 b - - 0 1");

		assertNotEquals(whiteToMove.getPositionIdentity(), blackToMove.getPositionIdentity());
	}

	@Test
	void repetitionIdentityDistinguishesCastlingRights() {
		Game withRights = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
		Game withoutRights = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/R3K2R w - - 0 1");

		assertNotEquals(withRights.getPositionIdentity(), withoutRights.getPositionIdentity());
	}

	@Test
	void repetitionIdentityDistinguishesEnPassantAvailability() {
		Game withEnPassant = TestSupport.gameFromFen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
		Game withoutEnPassant = TestSupport.gameFromFen("4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1");

		assertNotEquals(withEnPassant.getPositionIdentity(), withoutEnPassant.getPositionIdentity());
	}

	@Test
	void updateGameStateReportsCheckCheckmateAndStalemate() {
		assertEquals(GameState.WHITEINCHECK,
				TestSupport.gameFromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1").getCurrState());
		assertEquals(GameState.WHITEWINS,
				TestSupport.gameFromFen("7k/6Q1/5K2/8/8/8/8/8 b - - 0 1").getCurrState());
		assertEquals(GameState.STALEMATE,
				TestSupport.gameFromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").getCurrState());
	}
}
