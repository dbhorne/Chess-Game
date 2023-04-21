/* Author:	Donovan Horne
 * Purpose:	Used to bypass checking if every piece is an instance of another piece, just store
 * 			the piece type for each piece
 * Date:	4/20/2023
 */
package itec220.labs;

public enum PieceType {
	KNIGHT("Knight"), KING("King"), QUEEN("Queen"), PAWN("Pawn"), BISHOP("Bishop"), ROOK("Rook");
	
	public final String name;
	
	PieceType(String name){
		this.name = name;
	}
}
