package itec220.labs;

import javafx.scene.control.Button;

/**
 * Purpose:	To create a button that is used for promotions
 * Date: 	4/20/2023
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
	
	public final PieceType type;
}