package itec220.labs;

import javafx.scene.layout.StackPane;

/**
 * Purpose:	To create a stack pane that also stores the rank and file
 * Date: 	4/20/2023
 * @author Donovan Horne
 */
public class ChessStackPane extends StackPane {
	/**
	 * Constructor for the custom stackpane
	 * @param rank The row of the stack pane
	 * @param file The column of the stack pane
	 */
	ChessStackPane(int rank, int file){
		super();
		this.rank = rank;
		this.file = file;
	}
	public final int rank;
	public final int file;
}
