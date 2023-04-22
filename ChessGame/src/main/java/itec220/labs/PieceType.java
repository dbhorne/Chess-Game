
package itec220.labs;

/**
 * Purpose:	Used to bypass checking if every piece is an instance of another piece, just store
 * 			the piece type for each piece
 * Date:	4/20/2023
 * @author Donovan Horne
 *
 */
public enum PieceType {
	KNIGHT("Knight"), KING("King"), QUEEN("Queen"), PAWN("Pawn"), BISHOP("Bishop"), ROOK("Rook");
	
	public final String name;
	
	/**
	 * Creates an enum for each type of piece on the chess board
	 * @param name The string version of the Pieces name
	 */
	PieceType(String name){
		this.name = name;
	}
}
