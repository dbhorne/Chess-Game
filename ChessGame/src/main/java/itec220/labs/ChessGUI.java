package itec220.labs;

import java.util.AbstractMap.SimpleEntry;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChessGUI extends Application {

	private MediaPlayer chessMove = new MediaPlayer(
			new Media(getClass().getResource("/itec220/labs/ChessMove.mp3").toString()));
	private MediaPlayer chessTake = new MediaPlayer(
			new Media(getClass().getResource("/itec220/labs/ChessCapture.mp3").toString()));
	private GridPane grid = new GridPane();
	private Game game = new Game();
	private Region leftRegion = new Region();
	private VBox leftButtons = new VBox();
	private Button restart = new Button("Restart");
	private Button exitNoSave = new Button("Exit without Saving");
	private Button saveAndExit = new Button("Save and Exit");
	private Button playGame = new Button("Player vs. Player");
	private Region right = new Region();
	private HBox bottom = new HBox();
	private StackPane left = new StackPane();
	private Label currentColor = new Label();
	private Label lastMove = new Label();
	private ChessButton[][] buttons = new ChessButton[8][8];
	private ChessStackPane[][] spaces = new ChessStackPane[8][8];
	private Region[][] spacesBackground = new Region[8][8];
	private SimpleEntry<Integer, Integer> moveFrom = null;
	private int numTakenPieces = 0;
	private ArrayList<SimpleEntry<Integer, Integer>> moveList = new ArrayList<>();
	private PromoteButton[] promoteButtons = { new PromoteButton("Knight", PieceType.KNIGHT),
			new PromoteButton("Queen", PieceType.QUEEN), new PromoteButton("Rook", PieceType.ROOK),
			new PromoteButton("Bishop", PieceType.BISHOP) };

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpGUI();
		updateBoard();

		BorderPane root = new BorderPane();
		BorderPane menu = new BorderPane();
		menu.setCenter(playGame);
		root.setTop(currentColor);
		root.setCenter(grid);
		root.setRight(right);
		bottom.getChildren().add(lastMove);
		root.setBottom(bottom);
		root.setLeft(left);
		Scene sceneGame = new Scene(root, 1050, 700);
		Scene sceneMain = new Scene(menu, 1050, 700);

		EventHandler<ActionEvent> mainMenuEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(sceneMain);
				game = null;
			}
		};

		EventHandler<ActionEvent> mainMenuAndSave = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(sceneMain);
			}
		};

		EventHandler<ActionEvent> playGameEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(sceneGame);
				if (game == null) {
					game = new Game();
					lastMove.setText("");
				}
				currentColor.setText(String.format("%s's move", game.getCurrMove().name));
				if (!gameIsOver()) {
					disableButtons();
					enableButtons();
					numTakenPieces = game.getNumTakenPieces();
					updateBoard();
				}

			}
		};

		exitNoSave.setOnAction(mainMenuEvent);
		saveAndExit.setOnAction(mainMenuAndSave);
		playGame.setOnAction(playGameEvent);

		sceneGame.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		sceneMain.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		primaryStage.setTitle("Chess");
		primaryStage.setScene(sceneMain);
		primaryStage.show();
	}

	public void setUpGUI() {
		currentColor.setText(String.format("%s's move", game.getCurrMove().name));

		left.getChildren().add(leftRegion);
		restart.getStyleClass().add("restart");
		leftButtons.getChildren().add(restart);
		leftButtons.getChildren().add(exitNoSave);
		leftButtons.getChildren().add(saveAndExit);
		exitNoSave.getStyleClass().add("restart");
		saveAndExit.getStyleClass().add("restart");
		left.getChildren().add(leftButtons);
		EventHandler<ActionEvent> restartEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				game = new Game();
				currentColor.setText(String.format("%s's move", game.getCurrMove().name));
				disableButtons();
				enableButtons();
				lastMove.setText("");
				numTakenPieces = 0;
				updateBoard();
			}
		};
		restart.setOnAction(restartEvent);

		setUpBoard();
	}

	public void setUpBoard() {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				ChessButton tb = (ChessButton) e.getSource();
				click(tb.rank, tb.file);
			}
		};
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				spaces[i][j] = new ChessStackPane(7 - i, j);
				spacesBackground[i][j] = new Region();
				buttons[i][j] = new ChessButton(7 - i, j);
				buttons[i][j].setOnAction(event);
				if (i % 2 == 0) {
					if (j % 2 == 0) {
						spacesBackground[i][j].getStyleClass().add("chess-background-white");
					} else {
						spacesBackground[i][j].getStyleClass().add("chess-background-black");
					}
				} else {
					if (j % 2 == 0) {
						spacesBackground[i][j].getStyleClass().add("chess-background-black");
					} else {
						spacesBackground[i][j].getStyleClass().add("chess-background-white");
					}
				}
				buttons[i][j].getStyleClass().add("chess-button-transparent");
				spaces[i][j].getChildren().add(spacesBackground[i][j]);
				spaces[i][j].getChildren().add(buttons[i][j]);
				grid.add(spaces[i][j], j, i);
			}
		}
	}

	public void click(int rank, int file) {
		if (!moveList.isEmpty() && moveFrom != null && bottom.getChildren().size() == 1) {
			if (moveList.contains(new SimpleEntry<>(rank, file))) {
				if (game.move(moveFrom.getKey(), moveFrom.getValue(), rank, file)) {
					if (game.getNumTakenPieces() != numTakenPieces) {
						numTakenPieces = game.getNumTakenPieces();
						chessTake.play();
						chessTake.seek(Duration.ZERO);
					} else {
						chessMove.play();
						chessMove.seek(Duration.ZERO);
					}
					updateBoard();
					if (!gameIsOver()) {
						moveFrom = null;
						moveList.clear();
						currentColor.setText(String.format("%s's move", game.getCurrMove().name));

						if (game.getCopyOfCurrBoard().getPiece(rank, file) instanceof Pawn) {
							EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
								public void handle(ActionEvent e) {
									PromoteButton tb = (PromoteButton) e.getSource();
									game.promote(rank, file, tb.type);
									updateBoard();
									while (bottom.getChildren().size() > 1) {
										bottom.getChildren().remove(bottom.getChildren().size() - 1);
									}
									lastMove.setText(
											"Last Move: " + game.getCopyOfCurrBoard().getPiece(rank, file).toString());
								}
							};
							Pawn p = (Pawn) game.getCopyOfCurrBoard().getPiece(rank, file);
							if (p.getColor() == itec220.labs.Color.WHITE && rank == 7) {
								lastMove.setText("Looks like white can promote the pawn at: "
										+ buttons[7 - rank][file].toString());
								for (Button b : promoteButtons) {
									b.setOnAction(event);
									bottom.getChildren().add(b);
								}
							} else if (rank == 0) {
								lastMove.setText("Looks like black can promote the pawn at: "
										+ buttons[7 - rank][file].toString());
								for (Button b : promoteButtons) {
									b.setOnAction(event);
									bottom.getChildren().add(b);
								}
							} else {
								lastMove.setText(
										"Last Move: " + game.getCopyOfCurrBoard().getPiece(rank, file).toString());
							}
						} else {
							lastMove.setText("Last Move: " + game.getCopyOfCurrBoard().getPiece(rank, file).toString());
						}
					}
				} else {
					updateBoard();
					moveFrom = null;
					moveList.clear();
					lastMove.setText("Invalid Move, please try again. ");
				}
			} else {
				updateBoard();
				if (bottom.getChildren().size() == 1) {
					moveFrom = new SimpleEntry<>(rank, file);
					moveList = game.getValidMoves(rank, file);
					showValidMoves();
				}
			}
		} else {
			updateBoard();
			if (bottom.getChildren().size() == 1) {
				moveFrom = new SimpleEntry<>(rank, file);
				moveList = game.getValidMoves(rank, file);
				showValidMoves();
			}
		}
	}

	public boolean gameIsOver() {
		switch (game.getCurrState()) {
		case BLACKWINS:
		case WHITEWINS:
		case DRAW:
		case STALEMATE:
			disableButtons();
			setEndText();
			return true;
		case WHITEINCHECK:
		case BLACKINCHECK:
		case IN_PROGRESS:
		default:
			return false;
		}
	}

	public void disableButtons() {
		for (ChessButton[] bArray : buttons) {
			for (ChessButton b : bArray) {
				b.setOnAction(null);
			}
		}
	}

	public void enableButtons() {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				ChessButton tb = (ChessButton) e.getSource();
				click(tb.rank, tb.file);
			}
		};
		for (ChessButton[] bArray : buttons) {
			for (ChessButton b : bArray) {
				b.setOnAction(event);
			}
		}
	}

	public void setEndText() {
		switch (game.getCurrState()) {
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
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Piece piece = brd.getPiece(spaces[i][j].rank, spaces[i][j].file);
				while (spaces[i][j].getChildren().size() >= 2) {
					spaces[i][j].getChildren().remove(spaces[i][j].getChildren().size() - 1);
				}
				if (piece != null) {
					spaces[i][j].getChildren().add(new ImageView(getImage(piece)));
				}
				spaces[i][j].getChildren().add(buttons[i][j]);
			}
		}
	}

	public Image getImage(Piece piece) {
		PieceType type = piece.getType();
		itec220.labs.Color color = piece.getColor();
		Image image = null;
		switch (type) {
		case QUEEN:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhiteQueen.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackQueen.png"));
			}
			break;
		case KING:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhiteKing.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackKing.png"));
			}
			break;
		case PAWN:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhitePawn.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackPawn.png"));
			}
			break;
		case KNIGHT:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhiteKnight.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackKnight.png"));
			}
			break;
		case ROOK:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhiteRook.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackRook.png"));
			}
			break;
		case BISHOP:
			if (color == itec220.labs.Color.WHITE) {
				image = new Image(ChessGUI.class.getResourceAsStream("WhiteBishop.png"));
			} else {
				image = new Image(ChessGUI.class.getResourceAsStream("BlackBishop.png"));
			}
			break;
		}
		return image;

	}

	public void showValidMoves() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (moveList.contains(new SimpleEntry<>(spaces[i][j].rank, spaces[i][j].file))) {
					spaces[i][j].getChildren().remove(spaces[i][j].getChildren().size() - 1);
					Circle circ = new Circle(25);
					circ.setStroke(Color.BLACK);
					circ.setFill(Color.TRANSPARENT);
					circ.setStrokeWidth(5);
					circ.setOpacity(0.6);
					spaces[i][j].getChildren().add(circ);
					spaces[i][j].getChildren().add(buttons[i][j]);
				}
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
