package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameControllerTest {
	@Test
	void humanVsHumanControllerDoesNotAutoMoveOnPoll() {
		Game game = new Game();
		GameController controller = new GameController(game,
				new HumanPlayer(Color.WHITE), new HumanPlayer(Color.BLACK), null);

		controller.pollMove();

		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", game.toFEN());
		assertTrue(controller.isHumanTurn());
	}

	@Test
	void humanVsBotControllerWaitsWhenHumanIsToMove() {
		Game game = new Game();
		GameController controller = new GameController(game,
				new HumanPlayer(Color.WHITE), new TestSupport.FirstMoveBot(Color.BLACK), null);

		controller.pollMove();

		assertEquals(Color.WHITE, game.getCurrMove());
		assertTrue(controller.isHumanTurn());
	}

	@Test
	void botVsBotControllerCanAdvanceOneFullMovePair() {
		Game game = new Game();
		GameController controller = new GameController(game,
				new TestSupport.FirstMoveBot(Color.WHITE), new TestSupport.FirstMoveBot(Color.BLACK), null);

		assertTrue(controller.pollMoveSynchronouslyForTesting());
		assertTrue(controller.pollMoveSynchronouslyForTesting());

		assertEquals(Color.WHITE, game.getCurrMove());
		assertEquals(2, game.getMoveHistory().size());
		assertEquals(2, game.getFullMoveNumber());
	}

	@Test
	void controllerLoadedFenWithBotToMoveCanPollBotMove() {
		Game game = TestSupport.gameFromFen("4k3/8/8/8/8/8/4p3/4K3 b - - 0 1");
		GameController controller = new GameController(game,
				new HumanPlayer(Color.WHITE), new TestSupport.FirstMoveBot(Color.BLACK), null);

		assertTrue(controller.pollMoveSynchronouslyForTesting());

		assertEquals(Color.WHITE, game.getCurrMove());
		assertEquals(1, game.getMoveHistory().size());
	}
}
