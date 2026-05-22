package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FenSerializationTest {
	@Test
	void normalLegalMovesStillWork() {
		Game game = new Game();

		assertTrue(game.move(1, 4, 3, 4));
		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", game.toFEN());
	}

	@Test
	void startingPositionSerializesToFen() {
		Game game = new Game();

		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", game.toFEN());
	}

	@Test
	void loadFenRoundTripsThroughSerializer() {
		String fen = "r3k2r/pppq1ppp/2npbn2/3Np3/2B1P3/2N2Q2/PPP2PPP/R3K2R b KQkq - 7 12";
		Game game = TestSupport.gameFromFen(fen);

		assertEquals(fen, game.toFEN());
	}

	@Test
	void loadFenClearsMoveHistory() {
		Game game = new Game();
		assertTrue(game.move(1, 4, 3, 4));

		game.loadFEN("4k3/8/8/8/8/8/8/4K3 w - - 0 1");

		assertTrue(game.getMoveHistory().isEmpty());
	}
}
