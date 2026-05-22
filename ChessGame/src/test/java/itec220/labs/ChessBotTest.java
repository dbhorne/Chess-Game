package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ChessBotTest {
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
	void botDepthConstructorRejectsInvalidDepth() {
		assertThrows(IllegalArgumentException.class, () -> new ChessBot(Color.WHITE, 0));
	}

	@Test
	void defaultDepthChoosesStartingMoveQuickly() {
		Game game = new Game();

		Move move = assertTimeoutPreemptively(Duration.ofSeconds(2),
				() -> game.getBotMove(new ChessBot(Color.WHITE)));

		assertNotNull(move);
		assertTrue(game.getLegalMoves().contains(move));
	}

	@Test
	void evaluationPrefersMinorPieceDevelopmentOverEarlyQueenMove() {
		ChessBot bot = new ChessBot(Color.WHITE, 1, new Random(1), 1);
		Game developed = TestSupport.gameFromFen("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 1");
		Game queenEarly = TestSupport.gameFromFen("rnbqkbnr/pppppppp/8/8/8/5Q2/PPPPPPPP/RNB1KBNR w KQkq - 0 1");

		int developedScore = bot.evaluateForTesting(developed.getCopyOfCurrBoard(), Color.WHITE);
		int queenEarlyScore = bot.evaluateForTesting(queenEarly.getCopyOfCurrBoard(), Color.WHITE);

		assertTrue(developedScore > queenEarlyScore);
	}

	@Test
	void evaluationRewardsCentralizedMinorPiece() {
		ChessBot bot = new ChessBot(Color.WHITE, 1, new Random(1), 1);
		Game centeredKnight = TestSupport.gameFromFen("4k3/8/8/3N4/8/8/8/4K3 w - - 0 1");
		Game rimKnight = TestSupport.gameFromFen("4k3/8/8/8/8/8/7N/4K3 w - - 0 1");

		int centeredScore = bot.evaluateForTesting(centeredKnight.getCopyOfCurrBoard(), Color.WHITE);
		int rimScore = bot.evaluateForTesting(rimKnight.getCopyOfCurrBoard(), Color.WHITE);

		assertTrue(centeredScore > rimScore);
	}

	@Test
	void evaluationPenalizesDoubledAndIsolatedPawns() {
		ChessBot bot = new ChessBot(Color.WHITE, 1, new Random(1), 1);
		Game connectedPawns = TestSupport.gameFromFen("4k3/8/8/8/8/8/PPP5/4K3 w - - 0 1");
		Game weakPawns = TestSupport.gameFromFen("4k3/8/8/8/8/P7/P1P5/4K3 w - - 0 1");

		int connectedScore = bot.evaluateForTesting(connectedPawns.getCopyOfCurrBoard(), Color.WHITE);
		int weakScore = bot.evaluateForTesting(weakPawns.getCopyOfCurrBoard(), Color.WHITE);

		assertTrue(connectedScore > weakScore);
	}

	@Test
	void botLooksAheadToAvoidImmediateRecapture() {
		Game game = TestSupport.gameFromFen("r3k3/1b6/8/8/8/8/8/QR2K3 w - - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE, 2, new Random(1), 1));

		assertNotEquals(new Move(0, 0, 7, 0, null, PieceType.ROOK, false, false), move);
		assertTrue(game.getLegalMoves().contains(move));
	}

	@Test
	void botFindsMateInOne() {
		Game game = TestSupport.gameFromFen("7k/8/5KQ1/8/8/8/8/8 w - - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE, 2, new Random(1), 1));

		assertNotNull(move);
		assertTrue(game.move(move));
		assertEquals(GameState.WHITEWINS, game.getCurrState());
	}

	@Test
	void transpositionTableSearchReturnsLegalMoveWithoutMutatingBoardSnapshot() {
		Game game = TestSupport.gameFromFen("r3k2r/pppq1ppp/2npbn2/3Np3/2B1P3/2N2Q2/PPP2PPP/R3K2R b KQkq - 7 12");
		String fenBefore = game.toFEN();

		Move move = game.getBotMove(new ChessBot(Color.BLACK, 3, new Random(2), 1));

		assertNotNull(move);
		assertTrue(game.getLegalMoves().contains(move));
		assertEquals(fenBefore, game.toFEN());
		assertTrue(game.getMoveHistory().isEmpty());
	}

	@Test
	void seededRandomTieBreakingReturnsLegalEqualMove() {
		Game game = TestSupport.gameFromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
		ArrayList<Move> legalMoves = game.getLegalMoves();

		Move move = game.getBotMove(new ChessBot(Color.WHITE, 1, new Random(4), 1));

		assertTrue(legalMoves.contains(move));
	}

	@Test
	void botChoosesHighestValueCapture() {
		Game game = TestSupport.gameFromFen("q3k3/8/8/8/8/8/8/R3K3 w Q - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE));

		assertEquals(new Move(0, 0, 7, 0, null, PieceType.QUEEN, false, false), move);
	}

	@Test
	void botChoosesQueenPromotion() {
		Game game = TestSupport.gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		Move move = game.getBotMove(new ChessBot(Color.WHITE));

		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.QUEEN), move);
	}
}
