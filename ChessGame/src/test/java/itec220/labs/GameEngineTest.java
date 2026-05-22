package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class GameEngineTest {

	@Test
	void kingsMayNotMoveAdjacentToEachOther() {
		Game game = gameFromFen("8/8/8/8/8/8/6k1/4K3 w - - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves = game.getValidMoves(0, 4);

		assertFalse(containsMove(moves, 0, 5));
		assertFalse(containsMove(moves, 1, 5));
		assertTrue(containsMove(moves, 1, 4));
	}

	@Test
	void kingMayNotCaptureIntoAnAttackedSquare() {
		Game game = gameFromFen("4r2k/8/8/8/8/8/4q3/4K3 w - - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves = game.getValidMoves(0, 4);

		assertFalse(containsMove(moves, 1, 4));
	}

	@Test
	void castlingDoesNotRecurseOrCrash() {
		Game game = gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");

		ArrayList<SimpleEntry<Integer, Integer>> moves =
				assertDoesNotThrow(() -> game.getValidMoves(0, 4));

		assertTrue(containsMove(moves, 0, 6));
	}

	@Test
	void castlingIsDisallowedWhileInCheck() {
		Game game = gameFromFen("4r1k1/8/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsDisallowedThroughCheck() {
		Game game = gameFromFen("5rk1/8/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsDisallowedIntoCheck() {
		Game game = gameFromFen("6r1/k7/8/8/8/8/8/4K2R w K - 0 1");

		assertFalse(containsMove(game.getValidMoves(0, 4), 0, 6));
	}

	@Test
	void castlingIsAllowedWhenAllConditionsAreSatisfied() {
		Game game = gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");

		assertTrue(containsMove(game.getValidMoves(0, 4), 0, 6));
		assertTrue(game.move(0, 4, 0, 6));
		assertInstanceOf(King.class, game.getCopyOfCurrBoard().getPiece(0, 6));
		assertInstanceOf(Rook.class, game.getCopyOfCurrBoard().getPiece(0, 5));
	}

	@Test
	void movingOntoTheOpposingKingSquareIsNotLegal() {
		Game game = gameFromFen("4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1");

		assertFalse(containsMove(game.getValidMoves(1, 4), 7, 4));
	}

	@Test
	void threefoldRepetitionTracksFullPositionIdentity() {
		Game game = gameFromFen("4k1n1/8/8/8/8/8/8/4K1N1 w - - 0 1");

		assertTrue(game.move(0, 6, 2, 5));
		assertTrue(game.move(7, 6, 5, 5));
		assertTrue(game.move(2, 5, 0, 6));
		assertTrue(game.move(5, 5, 7, 6));
		assertTrue(game.move(0, 6, 2, 5));
		assertTrue(game.move(7, 6, 5, 5));
		assertTrue(game.move(2, 5, 0, 6));
		assertTrue(game.move(5, 5, 7, 6));

		assertEquals(GameState.DRAW, game.getCurrState());
	}

	@Test
	void repetitionIdentityDistinguishesSideToMove() {
		Game whiteToMove = gameFromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
		Game blackToMove = gameFromFen("4k3/8/8/8/8/8/8/4K3 b - - 0 1");

		assertNotEquals(whiteToMove.getPositionIdentity(), blackToMove.getPositionIdentity());
	}

	@Test
	void repetitionIdentityDistinguishesCastlingRights() {
		Game withRights = gameFromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
		Game withoutRights = gameFromFen("4k3/8/8/8/8/8/8/R3K2R w - - 0 1");

		assertNotEquals(withRights.getPositionIdentity(), withoutRights.getPositionIdentity());
	}

	@Test
	void repetitionIdentityDistinguishesEnPassantAvailability() {
		Game withEnPassant = gameFromFen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
		Game withoutEnPassant = gameFromFen("4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1");

		assertNotEquals(withEnPassant.getPositionIdentity(), withoutEnPassant.getPositionIdentity());
	}

	@Test
	void updateGameStateReportsCheckCheckmateAndStalemate() {
		assertEquals(GameState.WHITEINCHECK,
				gameFromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1").getCurrState());
		assertEquals(GameState.WHITEWINS,
				gameFromFen("7k/6Q1/5K2/8/8/8/8/8 b - - 0 1").getCurrState());
		assertEquals(GameState.STALEMATE,
				gameFromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").getCurrState());
	}

	@Test
	void normalLegalMovesStillWork() {
		Game game = new Game();

		assertTrue(game.move(1, 4, 3, 4));
		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", game.toFEN());
	}

	private Game gameFromFen(String fen) {
		Game game = new Game();
		game.loadFEN(fen);
		return game;
	}

	private boolean containsMove(ArrayList<SimpleEntry<Integer, Integer>> moves, int rank, int file) {
		return moves.contains(new SimpleEntry<>(rank, file));
	}
}
