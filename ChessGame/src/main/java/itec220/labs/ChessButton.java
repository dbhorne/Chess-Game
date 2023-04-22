package itec220.labs;

import javafx.scene.control.Button;

/**
 * An enum to create buttons for the GUI, which in addition to being a button stores the rank and file
 * @author Donovan Horne
 */
public class ChessButton extends Button {
	
	/**
	 * Create a button by calling super() and then setting rank and file
	 * @param rank Row of the button being created
	 * @param file Column of the button being created
	 */
	ChessButton(int rank, int file){
		super();
		this.rank = rank;
		this.file = file;
	}
	/** rank/row of each button */
	public final int rank;
	/** file/column of each button*/
	public final int file;
	/** array of characters to go with each file/column, commonly used in chess, 
	 * 		i.e F7 is a square on the board*/
	protected final char[] colomnLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
	
	
	/**
	 * Convert the button to a string using columnLetters
	 * @return returns a string of the buttons
	 */
	@Override
	public String toString() {
		return colomnLetters[this.file] + "" + (this.rank + 1);
	}
}
