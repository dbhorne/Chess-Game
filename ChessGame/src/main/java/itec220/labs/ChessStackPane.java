package itec220.labs;

import javafx.scene.layout.StackPane;

/**
 * To create a stack pane that also stores the rank and file
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
	
	/** The rank/row of the stack pane */
	public final int rank;
	/** The file/column of the stack pane */
	public final int file;
}
