package itec220.labs;

import java.util.Objects;

/**
 * Immutable description of a chess move.
 * The {@code moverColor} and {@code annotation} fields are history metadata and
 * are intentionally excluded from {@code equals} and {@code hashCode}.
 */
public final class Move {
	private static final char[] FILES = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

	public final int startRank;
	public final int startFile;
	public final int endRank;
	public final int endFile;
	public final PieceType promotionType;
	public final PieceType capturedType;
	public final boolean enPassantCapture;
	public final boolean castle;
	/** Color of the side that made this move; null when not recorded in history. */
	public final Color moverColor;
	/** Check/mate suffix: "+", "#", or "". Never null. */
	public final String annotation;

	public Move(int startRank, int startFile, int endRank, int endFile) {
		this(startRank, startFile, endRank, endFile, null, null, false, false);
	}

	public Move(int startRank, int startFile, int endRank, int endFile, PieceType promotionType) {
		this(startRank, startFile, endRank, endFile, promotionType, null, false, false);
	}

	public Move(int startRank, int startFile, int endRank, int endFile, PieceType promotionType,
			PieceType capturedType, boolean enPassantCapture, boolean castle) {
		this(startRank, startFile, endRank, endFile, promotionType, capturedType, enPassantCapture, castle, null, "");
	}

	public Move(int startRank, int startFile, int endRank, int endFile, PieceType promotionType,
			PieceType capturedType, boolean enPassantCapture, boolean castle, Color moverColor, String annotation) {
		this.startRank = startRank;
		this.startFile = startFile;
		this.endRank = endRank;
		this.endFile = endFile;
		this.promotionType = promotionType;
		this.capturedType = capturedType;
		this.enPassantCapture = enPassantCapture;
		this.castle = castle;
		this.moverColor = moverColor;
		this.annotation = annotation != null ? annotation : "";
	}

	/** Return a copy of this move with history metadata (mover color and check/mate annotation) attached. */
	public Move withHistoryMetadata(Color moverColor, String annotation) {
		return new Move(startRank, startFile, endRank, endFile, promotionType,
				capturedType, enPassantCapture, castle, moverColor, annotation);
	}

	public static Move normal(int startRank, int startFile, int endRank, int endFile) {
		return new Move(startRank, startFile, endRank, endFile);
	}

	public static Move promotion(int startRank, int startFile, int endRank, int endFile, PieceType promotionType) {
		return new Move(startRank, startFile, endRank, endFile, promotionType);
	}

	@Override
	public String toString() {
		String text = square(startRank, startFile) + " -> " + square(endRank, endFile);
		if (promotionType != null) {
			text += " = " + promotionType.name();
		}
		return text + annotation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Move)) {
			return false;
		}
		Move other = (Move) obj;
		return startRank == other.startRank
				&& startFile == other.startFile
				&& endRank == other.endRank
				&& endFile == other.endFile
				&& promotionType == other.promotionType
				&& capturedType == other.capturedType
				&& enPassantCapture == other.enPassantCapture
				&& castle == other.castle;
	}

	@Override
	public int hashCode() {
		return Objects.hash(startRank, startFile, endRank, endFile, promotionType,
				capturedType, enPassantCapture, castle);
	}

	private String square(int rank, int file) {
		return "" + FILES[file] + (rank + 1);
	}
}
