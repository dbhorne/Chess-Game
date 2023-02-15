package itec220.labs;

import javafx.scene.control.Button;

public class ChessButton extends Button {
	ChessButton(int rank, int file){
		super();
		this.rank = rank;
		this.file = file;
	}
	public final int rank;
	public final int file;
	protected final char[] colomnLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
	
	@Override
	public String toString() {
		return colomnLetters[this.file] + "" + (this.rank + 1);
	}
}
