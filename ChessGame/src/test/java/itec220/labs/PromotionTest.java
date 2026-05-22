package itec220.labs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class PromotionTest {
	@Test
	void promotionPositionReturnsAllPromotionPieceTypes() {
		Game game = TestSupport.gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		ArrayList<Move> moves = game.getLegalMoves();

		assertTrue(TestSupport.containsPromotion(moves, PieceType.QUEEN));
		assertTrue(TestSupport.containsPromotion(moves, PieceType.ROOK));
		assertTrue(TestSupport.containsPromotion(moves, PieceType.BISHOP));
		assertTrue(TestSupport.containsPromotion(moves, PieceType.KNIGHT));
	}

	@Test
	void moveModelPromotionUpdatesFenToPromotedPiece() {
		Game game = TestSupport.gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.QUEEN)));

		assertEquals("6Q1/8/8/8/8/8/8/4k2K b - - 0 1", game.toFEN());
	}

	@Test
	void promotionRecalculatesCheckState() {
		Game game = TestSupport.gameFromFen("4k3/6P1/8/8/8/8/8/4K3 w - - 0 1");

		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.QUEEN)));

		assertEquals(GameState.BLACKINCHECK, game.getCurrState());
	}

	@Test
	void moveHistoryRecordsNormalAndExplicitPromotionMoves() {
		Game game = TestSupport.gameFromFen("8/6P1/8/8/8/8/4P3/4k2K w - - 0 1");

		assertTrue(game.move(1, 4, 2, 4));
		assertTrue(game.move(0, 4, 0, 3));
		assertTrue(game.move(Move.promotion(6, 6, 7, 6, PieceType.ROOK)));

		assertEquals(3, game.getMoveHistory().size());
		assertEquals(Move.normal(1, 4, 2, 4), game.getMoveHistory().get(0));
		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.ROOK), game.getMoveHistory().get(2));
	}

	@Test
	void guiStylePromotionRecordsOnlyFinalPromotedPosition() throws Exception {
		Game game = TestSupport.gameFromFen("8/6P1/8/8/8/8/8/4k2K w - - 0 1");

		assertTrue(game.move(6, 6, 7, 6));
		assertTrue(game.getMoveHistory().isEmpty());
		assertEquals(1, TestSupport.boardStates(game).size());

		game.promote(7, 6, PieceType.QUEEN);

		assertEquals(1, game.getMoveHistory().size());
		assertEquals(Move.promotion(6, 6, 7, 6, PieceType.QUEEN), game.getMoveHistory().get(0));
		assertEquals(2, TestSupport.boardStates(game).size());
		assertTrue(TestSupport.boardStates(game).getLast().startsWith("6Q1/8/8/8/8/8/8/4k2K b"));
	}
}
