/* Author:	Donovan Horne
 * Purpose:	An enum to create buttons for the GUI, which in addition to being a button stores the rank and file
 * Date:	4/20/2023
 */

package itec220.labs;

import javafx.scene.control.Button;

public class ChessButton extends Button {
	
	/* Create a button by calling super() and then setting rank and file
	 * @param rank Row of the button being created
	 * @param file Column of the button being created
	 */
	ChessButton(int rank, int file){
		super();
		this.rank = rank;
		this.file = file;
	}
	
	public final int rank;
	public final int file;
	protected final char[] colomnLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
	
	
	// Convert the button to a string using columnLetters
	@Override
	public String toString() {
		return colomnLetters[this.file] + "" + (this.rank + 1);
	}
}
