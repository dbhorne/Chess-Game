package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;
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

	@Test
	void startingPositionSerializesToFen() {
		Game game = new Game();

		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", game.toFEN());
	}

	@Test
	void loadFenRoundTripsThroughSerializer() {
		String fen = "r3k2r/pppq1ppp/2npbn2/3Np3/2B1P3/2N2Q2/PPP2PPP/R3K2R b KQkq - 7 12";
		Game game = gameFromFen(fen);

		assertEquals(fen, game.toFEN());
	}

	@Test
	void startingPositionReturnsAllLegalMovesForCurrentSide() {
		Game game = new Game();

		ArrayList<Move> moves = game.getLegalMoves();

		assertEquals(20, moves.size());
		for (Move move : moves) {
			assertTrue(containsMove(game.getValidMoves(move.startRank, move.startFile), move.endRank, move.endFile),
					"Move did not map to a valid destination: " + move);
		}
	}

	@Test
	void promotionPositionReturnsAllPromotionPieceTypes() {
		Game game = gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		ArrayList<Move> moves = game.getLegalMoves();

		assertTrue(containsPromotion(moves, PieceType.QUEEN));
		assertTrue(containsPromotion(moves, PieceType.ROOK));
		assertTrue(containsPromotion(moves, PieceType.BISHOP));
		assertTrue(containsPromotion(moves, PieceType.KNIGHT));
	}

	@Test
	void moveModelPromotionUpdatesFenToPromotedPiece() {
		Game game = gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.QUEEN)));

		assertEquals("6Q1/8/8/8/8/8/8/4k2K b - - 0 1", game.toFEN());
	}

	@Test
	void promotionRecalculatesCheckState() {
		Game game = gameFromFen("4k3/6P1/8/8/8/8/8/4K3 w - - 0 1");

		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.QUEEN)));

		assertEquals(GameState.BLACKINCHECK, game.getCurrState());
	}

	@Test
	void getBotMoveReturnsLegalMoveWithoutMutatingGame() {
		Game game = new Game();
		String fenBefore = game.toFEN();

		Move move = game.getBotMove(new ChessBot(Color.WHITE));

		assertNotNull(move);
		assertTrue(game.getLegalMoves().contains(move));
		assertEquals(fenBefore, game.toFEN());
		assertTrue(game.getMoveHistory().isEmpty());
	}

	@Test
	void botChoosesHighestValueCapture() {
		Game game = gameFromFen("q3k3/8/8/8/8/8/8/R3K3 w Q - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE));

		assertEquals(new Move(0, 0, 7, 0, null, PieceType.QUEEN, false, false), move);
	}

	@Test
	void botChoosesQueenPromotion() {
		Game game = gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE));

		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.QUEEN), move);
	}

	@Test
	void moveHistoryRecordsNormalAndExplicitPromotionMoves() {
		Game game = gameFromFen("8/6P1/8/8/8/8/4P3/4k2K w - - 0 1");

		assertTrue(game.move(1, 4, 2, 4));
		assertTrue(game.move(0, 4, 0, 3));
		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.ROOK)));

		assertEquals(3, game.getMoveHistory().size());
		assertEquals(Move.normal(1, 4, 2, 4), game.getMoveHistory().get(0));
		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.ROOK), game.getMoveHistory().get(2));
	}

	@Test
	void guiStylePromotionRecordsOnlyFinalPromotedPosition() throws Exception {
		Game game = gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		assertTrue(game.move(6, 6, 7, 6));
		assertTrue(game.getMoveHistory().isEmpty());
		assertEquals(1, boardStates(game).size());

		game.promote(7, 6, PieceType.QUEEN);

		assertEquals(1, game.getMoveHistory().size());
		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.QUEEN), game.getMoveHistory().get(0));
		assertEquals(2, boardStates(game).size());
		assertTrue(boardStates(game).getLast().startsWith("6Q1/8/8/8/8/8/8/4k2K b"));
	}

	@Test
	void loadFenClearsMoveHistory() {
		Game game = new Game();
		assertTrue(game.move(1, 4, 3, 4));

		game.loadFEN("4k3/8/8/8/8/8/8/4K3 w - - 0 1");

		assertTrue(game.getMoveHistory().isEmpty());
	}

	private Game gameFromFen(String fen) {
		Game game = new Game();
		game.loadFEN(fen);
		return game;
	}

	private boolean containsMove(ArrayList<SimpleEntry<Integer, Integer>> moves, int rank, int file) {
		return moves.contains(new SimpleEntry<>(rank, file));
	}

	private boolean containsPromotion(ArrayList<Move> moves, PieceType type) {
		for (Move move : moves) {
			if (move.startRank == 6 && move.startFile == 6 && move.endRank == 7 && move.endFile == 6
					&& move.promotionType == type) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private LinkedList<String> boardStates(Game game) throws Exception {
		Field field = Game.class.getDeclaredField("boardStates");
		field.setAccessible(true);
		return (LinkedList<String>) field.get(game);
	}
}
