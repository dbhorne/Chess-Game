package itec220.labs;

import javafx.scene.control.Button;

public class PromoteButton extends Button {
	PromoteButton(String name, PieceType type){
		super(name);
		this.type = type;
	}
	
	public final PieceType type;
}
