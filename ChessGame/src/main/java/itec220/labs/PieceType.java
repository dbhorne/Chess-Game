package itec220.labs;

public enum PieceType {
	KNIGHT("Knight"), KING("King"), QUEEN("Queen"), PAWN("Pawn"), BISHOP("Bishop"), ROOK("Rook");
	
	public final String name;
	
	PieceType(String name){
		this.name = name;
	}
}
