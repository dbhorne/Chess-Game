package itec220.labs;

import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * To create and handle the GUI of the chess game, and communicate between the game and
 * 			the GUI
 * @author Donovan Horne
 *
 */
public class ChessGUI extends Application {

	private static final int BOARD_SIZE = 8;
	private static final int PIECE_SIZE = 62;
	private static final int MOVE_INDICATOR_RADIUS = 10;
	private static final int CAPTURE_INDICATOR_RADIUS = 28;
	private static final String MOVE_SOUND = "/itec220/labs/ChessMove.mp3";
	private static final String CAPTURE_SOUND = "/itec220/labs/ChessCapture.mp3";

	private final Media chessMove = loadMedia(MOVE_SOUND);
	private final Media chessTake = loadMedia(CAPTURE_SOUND);
	private final GridPane grid = new GridPane();
	private Game game = new Game();
	private final Region leftRegion = new Region();
	private final VBox leftButtons = new VBox(12);
	private final Button restart = new Button("Restart");
	private final Button exitNoSave = new Button("Exit without Saving");
	private final Button saveAndExit = new Button("Save and Exit");
	private final Button playGame = new Button("Player vs. Player");
	private final TextField fenField = new TextField();
	private final Button loadFenButton = new Button("Load FEN");
	private final Label fenError = new Label();
	private final Region right = new Region();
	private final HBox bottom = new HBox(12);
	private final StackPane left = new StackPane();
	private final Label currentColor = new Label();
	private final Label lastMove = new Label();
	private final ChessButton[][] buttons = new ChessButton[BOARD_SIZE][BOARD_SIZE];
	private final ChessStackPane[][] spaces = new ChessStackPane[BOARD_SIZE][BOARD_SIZE];
	private final Region[][] spacesBackground = new Region[BOARD_SIZE][BOARD_SIZE];
	private final Region[][] selectedHighlights = new Region[BOARD_SIZE][BOARD_SIZE];
	private final Circle[][] moveIndicators = new Circle[BOARD_SIZE][BOARD_SIZE];
	private final ImageView[][] pieceViews = new ImageView[BOARD_SIZE][BOARD_SIZE];
	private SimpleEntry<Integer, Integer> moveFrom = null;
	private int numTakenPieces = 0;
	private boolean promotionPending = false;
	private ArrayList<SimpleEntry<Integer, Integer>> moveList = new ArrayList<>();
	private final PromoteButton[] promoteButtons = { new PromoteButton("Knight", PieceType.KNIGHT),
			new PromoteButton("Queen", PieceType.QUEEN), new PromoteButton("Rook", PieceType.ROOK),
			new PromoteButton("Bishop", PieceType.BISHOP) };
	private final EnumMap<PieceType, Image> whiteImages = new EnumMap<>(PieceType.class);
	private final EnumMap<PieceType, Image> blackImages = new EnumMap<>(PieceType.class);

	
	/**
	 * Set up the starting stage of the GUI
	 * @param primaryStage default parameter for JavaFX
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpGUI();
		updateBoard();

		BorderPane root = new BorderPane();
		BorderPane menu = new BorderPane();

		fenField.setPromptText("e.g. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		fenField.setPrefWidth(620);
		fenError.getStyleClass().add("error-label");
		VBox menuCenter = new VBox(12, playGame, new Separator(),
				new Label("Start from FEN position"), fenField, loadFenButton, fenError);
		menuCenter.getStyleClass().add("menu-panel");
		menuCenter.setAlignment(Pos.CENTER);
		menuCenter.setPadding(new Insets(20));
		menu.setCenter(menuCenter);

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
					clearPromotionButtons();
				}
				updateStatusLabel();
				if (!gameIsOver()) {
					disableButtons();
					enableButtons();
					numTakenPieces = game.getNumTakenPieces();
					updateBoard();
				}

			}
		};

		EventHandler<ActionEvent> loadFenEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				String fen = fenField.getText().trim();
				try {
					if (fen.isEmpty()) throw new IllegalArgumentException("FEN string is empty");
					game = new Game();
					game.loadFEN(fen);
					fenError.setText("");
					lastMove.setText("");
					clearSelection();
					clearPromotionButtons();
					numTakenPieces = game.getNumTakenPieces();
					updateStatusLabel();
					disableButtons();
					enableButtons();
					updateBoard();
					primaryStage.setScene(sceneGame);
				} catch (IllegalArgumentException ex) {
					fenError.setText("Invalid FEN: " + ex.getMessage());
				}
			}
		};

		exitNoSave.setOnAction(mainMenuEvent);
		saveAndExit.setOnAction(mainMenuAndSave);
		playGame.setOnAction(playGameEvent);
		loadFenButton.setOnAction(loadFenEvent);

		sceneGame.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		sceneMain.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		primaryStage.setTitle("Chess");
		primaryStage.setScene(sceneMain);
		primaryStage.show();
	}

	/**
	 * Set up the GUI with buttons, and stylesheets
	 */
	public void setUpGUI() {
		whiteImages.put(PieceType.QUEEN,  new Image(ChessGUI.class.getResourceAsStream("WhiteQueen.png")));
		whiteImages.put(PieceType.KING,   new Image(ChessGUI.class.getResourceAsStream("WhiteKing.png")));
		whiteImages.put(PieceType.PAWN,   new Image(ChessGUI.class.getResourceAsStream("WhitePawn.png")));
		whiteImages.put(PieceType.KNIGHT, new Image(ChessGUI.class.getResourceAsStream("WhiteKnight.png")));
		whiteImages.put(PieceType.ROOK,   new Image(ChessGUI.class.getResourceAsStream("WhiteRook.png")));
		whiteImages.put(PieceType.BISHOP, new Image(ChessGUI.class.getResourceAsStream("WhiteBishop.png")));
		blackImages.put(PieceType.QUEEN,  new Image(ChessGUI.class.getResourceAsStream("BlackQueen.png")));
		blackImages.put(PieceType.KING,   new Image(ChessGUI.class.getResourceAsStream("BlackKing.png")));
		blackImages.put(PieceType.PAWN,   new Image(ChessGUI.class.getResourceAsStream("BlackPawn.png")));
		blackImages.put(PieceType.KNIGHT, new Image(ChessGUI.class.getResourceAsStream("BlackKnight.png")));
		blackImages.put(PieceType.ROOK,   new Image(ChessGUI.class.getResourceAsStream("BlackRook.png")));
		blackImages.put(PieceType.BISHOP, new Image(ChessGUI.class.getResourceAsStream("BlackBishop.png")));
		updateStatusLabel();

		left.getChildren().add(leftRegion);
		leftButtons.setAlignment(Pos.TOP_CENTER);
		leftButtons.setPadding(new Insets(18));
		restart.getStyleClass().add("restart");
		exitNoSave.getStyleClass().add("restart");
		saveAndExit.getStyleClass().add("restart");
		leftButtons.getChildren().add(restart);
		leftButtons.getChildren().add(exitNoSave);
		leftButtons.getChildren().add(saveAndExit);
		left.getChildren().add(leftButtons);
		bottom.setAlignment(Pos.CENTER_LEFT);
		bottom.setPadding(new Insets(10, 16, 14, 16));
		EventHandler<ActionEvent> restartEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				game = new Game();
				clearSelection();
				clearPromotionButtons();
				lastMove.setText("");
				numTakenPieces = 0;
				updateStatusLabel();
				disableButtons();
				enableButtons();
				updateBoard();
			}
		};
		restart.setOnAction(restartEvent);

		setUpBoard();
	}
	
	/**
	 * Set up the board using stable square nodes.
	 */
	public void setUpBoard() {
		grid.getStyleClass().add("chess-board");
		grid.setAlignment(Pos.CENTER);
		
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				ChessButton tb = (ChessButton) e.getSource();
				click(tb.rank, tb.file);
			}
		};
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				spaces[i][j] = new ChessStackPane(BOARD_SIZE - 1 - i, j);
				spaces[i][j].getStyleClass().add("chess-square");
				spacesBackground[i][j] = new Region();
				spacesBackground[i][j].getStyleClass().add((i + j) % 2 == 0
						? "chess-background-white" : "chess-background-black");

				selectedHighlights[i][j] = new Region();
				selectedHighlights[i][j].getStyleClass().add("selected-square");
				selectedHighlights[i][j].setVisible(false);
				selectedHighlights[i][j].setMouseTransparent(true);

				moveIndicators[i][j] = new Circle(MOVE_INDICATOR_RADIUS);
				moveIndicators[i][j].getStyleClass().add("move-indicator");
				moveIndicators[i][j].setVisible(false);
				moveIndicators[i][j].setMouseTransparent(true);

				pieceViews[i][j] = new ImageView();
				pieceViews[i][j].setFitWidth(PIECE_SIZE);
				pieceViews[i][j].setFitHeight(PIECE_SIZE);
				pieceViews[i][j].setPreserveRatio(true);
				pieceViews[i][j].setSmooth(true);
				pieceViews[i][j].setMouseTransparent(true);

				buttons[i][j] = new ChessButton(BOARD_SIZE - 1 - i, j);
				buttons[i][j].setOnAction(event);
				buttons[i][j].getStyleClass().add("chess-button-transparent");

				spaces[i][j].getChildren().add(spacesBackground[i][j]);
				spaces[i][j].getChildren().add(selectedHighlights[i][j]);
				spaces[i][j].getChildren().add(moveIndicators[i][j]);
				spaces[i][j].getChildren().add(pieceViews[i][j]);
				spaces[i][j].getChildren().add(buttons[i][j]);
				grid.add(spaces[i][j], j, i);
			}
		}
	}

	/**
	 * Event handler for the buttons, used to move pieces on the board
	 * @param rank Row of the button that was pressed
	 * @param file Column of the button that was pressed
	 */
	public void click(int rank, int file) {
		if (promotionPending) {
			lastMove.setText("Choose a promotion piece before moving again.");
			return;
		}
		if (moveFrom != null && moveList.contains(new SimpleEntry<>(rank, file))) {
			moveSelectedPiece(rank, file);
			return;
		}
		selectSquare(rank, file);
	}

	private void selectSquare(int rank, int file) {
		clearHighlights();
		moveFrom = new SimpleEntry<>(rank, file);
		moveList = game.getValidMoves(rank, file);
		if (moveList.isEmpty()) {
			moveFrom = null;
			lastMove.setText("No legal moves from " + buttons[BOARD_SIZE - 1 - rank][file].toString() + ".");
			return;
		}
		showValidMoves();
	}

	private void moveSelectedPiece(int rank, int file) {
		int startRank = moveFrom.getKey();
		int startFile = moveFrom.getValue();
		Board beforeMove = game.getCopyOfCurrBoard();
		Piece movingPiece = beforeMove.getPiece(startRank, startFile);
		boolean enPassantCapture = movingPiece instanceof Pawn
				&& startFile != file
				&& beforeMove.getPiece(rank, file) == null;

		if (game.move(startRank, startFile, rank, file)) {
			boolean isCapture = game.getNumTakenPieces() != numTakenPieces;
			if (isCapture) {
				numTakenPieces = game.getNumTakenPieces();
				playSound(chessTake);
			} else {
				playSound(chessMove);
			}
			clearSelection();
			updateMoveSquares(startRank, startFile, rank, file, movingPiece, enPassantCapture);
			if (!gameIsOver()) {
				updateStatusLabel();
				handlePromotionIfNeeded(rank, file);
				updateLastMove(rank, file);
			}
		} else {
			clearSelection();
			updateBoard();
			lastMove.setText("Invalid move, please try again.");
		}
	}

	private void updateMoveSquares(int startRank, int startFile, int endRank, int endFile,
			Piece movingPiece, boolean enPassantCapture) {
		Board currentBoard = game.getCopyOfCurrBoard();
		updateSquare(currentBoard, startRank, startFile);
		updateSquare(currentBoard, endRank, endFile);
		if (movingPiece instanceof King && Math.abs(endFile - startFile) == 2) {
			if (endFile > startFile) {
				updateSquare(currentBoard, startRank, startFile + 3);
				updateSquare(currentBoard, startRank, startFile + 1);
			} else {
				updateSquare(currentBoard, startRank, startFile - 4);
				updateSquare(currentBoard, startRank, startFile - 1);
			}
		}
		if (enPassantCapture) {
			updateSquare(currentBoard, startRank, endFile);
		}
	}

	private void handlePromotionIfNeeded(final int rank, final int file) {
		Board currentBoard = game.getCopyOfCurrBoard();
		Piece movedPiece = currentBoard.getPiece(rank, file);
		if (!(movedPiece instanceof Pawn)) {
			return;
		}
		Pawn pawn = (Pawn) movedPiece;
		boolean canPromote = (pawn.getColor() == itec220.labs.Color.WHITE && rank == BOARD_SIZE - 1)
				|| (pawn.getColor() == itec220.labs.Color.BLACK && rank == 0);
		if (!canPromote) {
			return;
		}
		promotionPending = true;
		disableButtons();
		lastMove.setText("Promote pawn at " + buttons[BOARD_SIZE - 1 - rank][file].toString() + ":");
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				PromoteButton tb = (PromoteButton) e.getSource();
				game.promote(rank, file, tb.type);
				updateSquare(game.getCopyOfCurrBoard(), rank, file);
				clearPromotionButtons();
				enableButtons();
				updateStatusLabel();
				Piece promotedPiece = game.getCopyOfCurrBoard().getPiece(rank, file);
				lastMove.setText("Last Move: " + promotedPiece.toString());
			}
		};
		for (Button b : promoteButtons) {
			b.setOnAction(event);
			if (!bottom.getChildren().contains(b)) {
				bottom.getChildren().add(b);
			}
		}
	}

	private void updateLastMove(int rank, int file) {
		if (promotionPending) {
			return;
		}
		Piece movedPiece = game.getCopyOfCurrBoard().getPiece(rank, file);
		if (movedPiece != null) {
			lastMove.setText("Last Move: " + movedPiece.toString());
		}
	}

	/**
	 * Check the current game state to see if the game is over, if so disable all the buttons
	 * @return Returns a boolean, true if the game is over, false if it is still continuing
	 */
	public boolean gameIsOver() {
		switch (game.getCurrState()) {
		case BLACKWINS:
		case WHITEWINS:
		case DRAW:
		case STALEMATE:
			disableButtons();
			clearPromotionButtons();
			setEndText();
			return true;
		case WHITEINCHECK:
		case BLACKINCHECK:
		case IN_PROGRESS:
		default:
			return false;
		}
	}

	/**
	 * Loop through the buttons and disable their event handler
	 */
	public void disableButtons() {
		for (ChessButton[] bArray : buttons) {
			for (ChessButton b : bArray) {
				b.setOnAction(null);
			}
		}
	}

	/**
	 * Loop through the buttons and enable their event handler
	 */
	public void enableButtons() {
		if (promotionPending) {
			return;
		}
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

	/**
	 * Set the end text based on the current game state
	 */
	public void setEndText() {
		switch (game.getCurrState()) {
		case WHITEWINS:
			currentColor.setText("Game over");
			lastMove.setText("White won!");
			break;
		case BLACKWINS:
			currentColor.setText("Game over");
			lastMove.setText("Black won!");
			break;
		case STALEMATE:
			currentColor.setText("Game over");
			lastMove.setText("Stalemate.");
			break;
		case DRAW:
			currentColor.setText("Game over");
			lastMove.setText("Draw.");
			break;
		default:
			break;
		}
	}

	/**
	 * Update the GUI board without rebuilding square children.
	 */
	public void updateBoard() {
		Board brd = game.getCopyOfCurrBoard();
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				updateSquare(brd, spaces[i][j].rank, spaces[i][j].file);
			}
		}
	}

	private void updateSquare(Board brd, int rank, int file) {
		int row = BOARD_SIZE - 1 - rank;
		Piece piece = brd.getPiece(rank, file);
		ImageView view = pieceViews[row][file];
		if (piece == null) {
			view.setImage(null);
			view.setVisible(false);
			return;
		}
		view.setImage(getImage(piece));
		view.setVisible(true);
	}

	/**
	 * Get the correct image for the pieces to be on the GUI
	 * @param piece The piece that you are getting the image for
	 * @return Returns an image created based on the piece
	 */
	
	public Image getImage(Piece piece) {
		EnumMap<PieceType, Image> cache = piece.getColor() == itec220.labs.Color.WHITE ? whiteImages : blackImages;
		return cache.get(piece.getType());
	}

	/**
	 * Show valid moves on the board, used by the click() method
	 */
	public void showValidMoves() {
		if (moveFrom != null) {
			selectedHighlights[BOARD_SIZE - 1 - moveFrom.getKey()][moveFrom.getValue()].setVisible(true);
		}
		Board board = game.getCopyOfCurrBoard();
		for (SimpleEntry<Integer, Integer> move : moveList) {
			int rank = move.getKey();
			int file = move.getValue();
			Circle indicator = moveIndicators[BOARD_SIZE - 1 - rank][file];
			Piece target = board.getPiece(rank, file);
			indicator.getStyleClass().removeAll("move-indicator", "capture-indicator");
			if (target == null) {
				indicator.setRadius(MOVE_INDICATOR_RADIUS);
				indicator.getStyleClass().add("move-indicator");
			} else {
				indicator.setRadius(CAPTURE_INDICATOR_RADIUS);
				indicator.getStyleClass().add("capture-indicator");
			}
			indicator.setVisible(true);
		}
	}

	private void clearSelection() {
		moveFrom = null;
		moveList.clear();
		clearHighlights();
	}

	private void clearHighlights() {
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				selectedHighlights[i][j].setVisible(false);
				moveIndicators[i][j].setVisible(false);
			}
		}
	}

	private void clearPromotionButtons() {
		for (Button b : promoteButtons) {
			b.setOnAction(null);
			bottom.getChildren().remove(b);
		}
		promotionPending = false;
	}

	private void updateStatusLabel() {
		switch (game.getCurrState()) {
		case WHITEINCHECK:
			currentColor.setText("White is in check");
			break;
		case BLACKINCHECK:
			currentColor.setText("Black is in check");
			break;
		default:
			currentColor.setText(String.format("%s's move", game.getCurrMove().name));
			break;
		}
	}

	private Media loadMedia(String resourcePath) {
		URL resource = getClass().getResource(resourcePath);
		return resource == null ? null : new Media(resource.toString());
	}

	private void playSound(Media media) {
		if (media == null) {
			return;
		}
		MediaPlayer player = new MediaPlayer(media);
		player.setOnEndOfMedia(new Runnable() {
			public void run() {
				player.dispose();
			}
		});
		player.play();
	}

	
	/**
	 * Launch the GUI game
	 * @param args default java argument
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
