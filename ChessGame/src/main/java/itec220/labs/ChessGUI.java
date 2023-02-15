package itec220.labs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ChessGUI extends Application {
	
	private GridPane grid = new GridPane();
	private Game game = new Game();
	private Region left = new Region();
	private Region right = new Region();
	private Label currentColor = new Label();
	private Label lastMove = new Label();	
	private ChessButton[][] buttons = new ChessButton[8][8]; 
	private ChessStackPane[][] spaces = new ChessStackPane[8][8];
	private SimpleEntry<Integer, Integer> moveFrom = null;
	private ArrayList<SimpleEntry<Integer, Integer>> moveList = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpGUI();
		updateBoard();
    	
        BorderPane root = new BorderPane();
        root.setTop(currentColor);
        root.setCenter(grid);
        root.setRight(right);
        root.setBottom(lastMove);
        root.setLeft(left);
        Scene scene = new Scene(root, 350, 420);
        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	public void setUpGUI() {
		currentColor.setText(String.format("%s's move", game.getCurrMove().name));
		
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
              ChessButton tb = (ChessButton) e.getSource();
              click(tb);
            }
        };
        
        for(int i = 0; i < 8; i++) {
        	for(int j = 0; j < 8; j ++) {
        		spaces[i][j] = new ChessStackPane(7-i, j);
        		buttons[i][j] = new ChessButton(7-i, j);
        		buttons[i][j].setOnAction(event);
        		spaces[i][j].getChildren().add(buttons[i][j]);
        		grid.add(spaces[i][j], j, i);
        	}
        }
	}
	
	public void click(ChessButton button) {
		if(!moveList.isEmpty() && moveFrom != null) {
			if(moveList.contains(new SimpleEntry<>(button.rank, button.file))){
				if(game.move(moveFrom.getKey(), moveFrom.getValue(), button.rank, button.file)) {
					clearValidMoves();
					updateBoard();
					if(gameIsOver()) {
						
					} else {
						moveFrom = null;
						moveList.clear();
						currentColor.setText(String.format("%s's move", game.getCurrMove().name));
						lastMove.setText("Last Move: " + button.toString());
					}
				} else {
					clearValidMoves();
					moveFrom = null;
					moveList.clear();
					lastMove.setText("Invalid Move, please try again. ");
				}
			} else {
				clearValidMoves();
				moveFrom = new SimpleEntry<>(button.rank, button.file);
				moveList = game.getValidMoves(button.rank, button.file);
				showValidMoves();
			}
		} else {
			clearValidMoves();
			moveFrom = new SimpleEntry<>(button.rank, button.file);
			moveList = game.getValidMoves(button.rank, button.file);
			showValidMoves();
		}
	}
	
	public boolean gameIsOver() {
		switch(game.getCurrState()) {
		case BLACKWINS:
		case WHITEWINS:
		case DRAW:
		case STALEMATE:
			setEndText();
			return true;
		case WHITEINCHECK:
		case BLACKINCHECK:
		case IN_PROGRESS:
		default:
			return false;
		}
	}
	
	public void setEndText() {
		switch(game.getCurrState()) {
		case WHITEWINS:
			lastMove.setText("White Won!");
			break;
		case BLACKWINS:
			lastMove.setText("Black Won!");
			break;
		case STALEMATE:
			lastMove.setText("Dang, looks like a stalemate.");
			break;
		case DRAW: 
			lastMove.setText("A game played perfectly always ends in a draw.");
			break;
		default:
			break;
		}
	}
	
	public void updateBoard() {
		Board brd = game.getCopyOfCurrBoard();
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Piece piece = brd.getPiece(spaces[i][j].rank, spaces[i][j].file);
				if(piece != null) {
					spaces[i][j].getChildren().add(new ImageView(getImage(piece)));
				}
			}
		}
	}
	
	public Image getImage(Piece piece) {
		PieceType type = piece.getType();
		itec220.labs.Color color = piece.getColor();
		Image image = null;
		switch(type) {
		case QUEEN:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhiteQueen.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackQueen.png");
			}
			break;
		case KING:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhiteKing.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackKing.png");
			}
			break;
		case PAWN:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhitePawn.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackPawn.png");
			}
			break;
		case KNIGHT:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhiteKnight.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackKnight.png");
			}
			break;
		case ROOK:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhiteRook.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackRook.png");
			}
			break;
		case BISHOP:
			if(color == itec220.labs.Color.WHITE) {
				image = new Image("File:itec220.labs.Images/WhiteBishop.png");
			} else {
				image = new Image("File:itec220.labs.Images/BlackBishop.png");
			}
			break;	
		}
		return image;
		
	}
	
	public void showValidMoves() {
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(moveList.contains(new SimpleEntry<>(spaces[i][j].rank, spaces[i][j].file))) {
					Circle circ = new Circle(5);
					circ.setFill(Color.BLACK);
					circ.setOpacity(0.25);
					spaces[i][j].getChildren().add(circ);
				}
			}
		}
	}
	
	public void clearValidMoves() {
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(moveList.contains(new SimpleEntry<>(spaces[i][j].rank, spaces[i][j].file))) {
					System.out.print("here2");
					spaces[i][j].getChildren().remove(spaces[i][j].getChildren().size()-1);
				}
			}
		}
	}

	 public static void main(String[] args) {
	        launch(args);
	    }
}
