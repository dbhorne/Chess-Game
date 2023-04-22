
package itec220.labs;

/**
 * Used to bypass checking if every piece is an instance of another piece, just store
 * 			the piece type for each piece
 * @author Donovan Horne
 *
 */
public enum PieceType {
	/** Represents piece of type Knight */
	KNIGHT("Knight"), 
	/** Represents piece of type King */
	KING("King"), 
	/** Represents piece of type Queen */
	QUEEN("Queen"), 
	/** Represents piece of type Pawn */
	PAWN("Pawn"), 
	/** Represents piece of type Bishop */
	BISHOP("Bishop"), 
	/** Represents piece of type Rook */
	ROOK("Rook");
	
	/** String representation of each piece, i.e King for KING enum */
	public final String name;
	
	/**
	 * Creates an enum for each type of piece on the chess board
	 * @param name The string version of the Pieces name
	 */
	PieceType(String name){
		this.name = name;
	}
}
