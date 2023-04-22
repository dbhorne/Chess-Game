package itec220.labs;

import javafx.scene.control.Button;

/**
 * To create a button that is used for promotions
 * @author Donovan Horne
 *
 */
public class PromoteButton extends Button {
	/**
	 * Create a button that will allow pawns to promote, will store the type of piece the pawn will
	 * 		promote to.
	 * @param name String to put on the button
	 * @param type Type of piece the button will promote to 
	 */
	PromoteButton(String name, PieceType type){
		super(name);
		this.type = type;
	}
	
	/** The type of piece the button will promote the pawn to, stored in each button
	 * 	 	for each specific type
	*/
	public final PieceType type;
}