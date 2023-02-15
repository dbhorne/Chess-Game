package itec220.labs;

import javafx.scene.layout.StackPane;

public class ChessStackPane extends StackPane {
	ChessStackPane(int rank, int file){
		super();
		this.rank = rank;
		this.file = file;
	}
	public final int rank;
	public final int file;
}
