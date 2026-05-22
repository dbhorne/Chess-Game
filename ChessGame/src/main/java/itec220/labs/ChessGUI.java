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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
public class ChessGUI extends Application implements GameViewListener {

	private static final int BOARD_SIZE = 8;
	private static final int PIECE_SIZE = 62;
	private static final int MOVE_INDICATOR_RADIUS = 10;
	private static final int CAPTURE_INDICATOR_RADIUS = 28;
	private static final int SCENE_WIDTH = 1120;
	private static final int SCENE_HEIGHT = 780;
	private static final String MOVE_SOUND = "/itec220/labs/ChessMove.mp3";
	private static final String CAPTURE_SOUND = "/itec220/labs/ChessCapture.mp3";
	private static final String CHECK_SOUND = "/itec220/labs/move-check.mp3";

	private final Media chessMove = loadMedia(MOVE_SOUND);
	private final Media chessTake = loadMedia(CAPTURE_SOUND);
	private final Media chessCheck = loadMedia(CHECK_SOUND);
	private final GridPane grid = new GridPane();
	private Game game = new Game();
	private GameController controller;
	private final Region leftRegion = new Region();
	private final VBox leftButtons = new VBox(12);
	private final Button restart = new Button("Restart");
	private final Button exitNoSave = new Button("Exit without Saving");
	private final Button saveAndExit = new Button("Save and Exit");
	private final Button playerVsPlayerButton = new Button("Player vs. Player");
	private final Button playerVsBotButton = new Button("Player vs. Bot");
	private final Button botVsBotButton = new Button("Bot vs. Bot");
	private final Button playWhiteButton = new Button("Play White");
	private final Button playBlackButton = new Button("Play Black");
	private final Button startGameButton = new Button("Start Game");
	private final TextField fenField = new TextField();
	private final Button loadFenButton = new Button("Load FEN");
	private final Label fenError = new Label();
	private final Region right = new Region();
	private final VBox bottom = new VBox(8);
	private final HBox moveBar = new HBox(12);
	private final HBox fenBar = new HBox(8);
	private final TextField currentFenField = new TextField();
	private final Button copyFenButton = new Button("Copy FEN");
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
	private GameMode gameMode = GameMode.PLAYER_VS_PLAYER;
	private Color humanColor = Color.WHITE;
	private ArrayList<SimpleEntry<Integer, Integer>> moveList = new ArrayList<>();
	private final PromoteButton[] promoteButtons = { new PromoteButton("Knight", PieceType.KNIGHT),
			new PromoteButton("Queen", PieceType.QUEEN), new PromoteButton("Rook", PieceType.ROOK),
			new PromoteButton("Bishop", PieceType.BISHOP) };
	private final EnumMap<PieceType, Image> whiteImages = new EnumMap<>(PieceType.class);
	private final EnumMap<PieceType, Image> blackImages = new EnumMap<>(PieceType.class);

	private enum GameMode {
		PLAYER_VS_PLAYER,
		PLAYER_VS_BOT,
		BOT_VS_BOT
	}

	
	/**
	 * Set up the starting stage of the GUI
	 * @param primaryStage default parameter for JavaFX
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpGUI();
		controller = createController(game);
		updateBoard();

		BorderPane root = new BorderPane();
		BorderPane menu = new BorderPane();

		fenField.setPromptText("e.g. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		fenField.setPrefWidth(620);
		fenError.getStyleClass().add("error-label");
		VBox menuCenter = new VBox(12, playerVsPlayerButton, playerVsBotButton, botVsBotButton,
				new HBox(10, playWhiteButton, playBlackButton), startGameButton, new Separator(),
				new Label("Start from FEN position"), fenField, loadFenButton, fenError);
		menuCenter.getStyleClass().add("menu-panel");
		menuCenter.setAlignment(Pos.CENTER);
		menuCenter.setPadding(new Insets(20));
		menu.setCenter(menuCenter);

		root.setTop(currentColor);
		root.setCenter(grid);
		root.setRight(right);
		moveBar.getChildren().add(lastMove);
		bottom.getChildren().add(moveBar);
		bottom.getChildren().add(fenBar);
		root.setBottom(bottom);
		root.setLeft(left);
		Scene sceneGame = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
		Scene sceneMain = new Scene(menu, SCENE_WIDTH, SCENE_HEIGHT);

		EventHandler<ActionEvent> mainMenuEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (controller != null) {
					controller.stop();
				}
				primaryStage.setScene(sceneMain);
			}
		};

		EventHandler<ActionEvent> mainMenuAndSave = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (controller != null) {
					controller.stop();
				}
				primaryStage.setScene(sceneMain);
			}
		};

		EventHandler<ActionEvent> playerVsPlayerEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				gameMode = GameMode.PLAYER_VS_PLAYER;
				updateMenuModeButtons();
			}
		};

		EventHandler<ActionEvent> playerVsBotEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				gameMode = GameMode.PLAYER_VS_BOT;
				updateMenuModeButtons();
			}
		};

		EventHandler<ActionEvent> botVsBotEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				gameMode = GameMode.BOT_VS_BOT;
				updateMenuModeButtons();
			}
		};

		EventHandler<ActionEvent> playWhiteEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				humanColor = Color.WHITE;
				updateMenuModeButtons();
			}
		};

		EventHandler<ActionEvent> playBlackEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				humanColor = Color.BLACK;
				updateMenuModeButtons();
			}
		};

		EventHandler<ActionEvent> startGameEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				game = new Game();
				controller = createController(game);
				startGameScene(primaryStage, sceneGame);
			}
		};

		EventHandler<ActionEvent> loadFenEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				String fen = fenField.getText().trim();
				try {
					if (fen.isEmpty()) throw new IllegalArgumentException("FEN string is empty");
					game = new Game();
					controller = createController(game);
					controller.loadFen(fen);
					game = controller.getGame();
					fenError.setText("");
					startGameScene(primaryStage, sceneGame);
				} catch (IllegalArgumentException ex) {
					fenError.setText("Invalid FEN: " + ex.getMessage());
				}
			}
		};

		exitNoSave.setOnAction(mainMenuEvent);
		saveAndExit.setOnAction(mainMenuAndSave);
		playerVsPlayerButton.setOnAction(playerVsPlayerEvent);
		playerVsBotButton.setOnAction(playerVsBotEvent);
		botVsBotButton.setOnAction(botVsBotEvent);
		playWhiteButton.setOnAction(playWhiteEvent);
		playBlackButton.setOnAction(playBlackEvent);
		startGameButton.setOnAction(startGameEvent);
		loadFenButton.setOnAction(loadFenEvent);
		updateMenuModeButtons();

		sceneGame.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		sceneMain.getStylesheets().add(ChessGUI.class.getResource("styles.css").toExternalForm());
		primaryStage.setTitle("Chess");
		primaryStage.setResizable(false);
		primaryStage.setScene(sceneMain);
		primaryStage.show();
	}

	private void startGameScene(Stage primaryStage, Scene sceneGame) {
		resetGameView();
		primaryStage.setScene(sceneGame);
		controller.pollMove();
	}

	private GameController createController(Game game) {
		switch (gameMode) {
		case PLAYER_VS_BOT:
			return humanColor == Color.WHITE
					? new GameController(game, new HumanPlayer(Color.WHITE), new BotPlayer(Color.BLACK), this)
					: new GameController(game, new BotPlayer(Color.WHITE), new HumanPlayer(Color.BLACK), this);
		case BOT_VS_BOT:
			return new GameController(game, new BotPlayer(Color.WHITE), new BotPlayer(Color.BLACK), this);
		case PLAYER_VS_PLAYER:
		default:
			return new GameController(game, new HumanPlayer(Color.WHITE), new HumanPlayer(Color.BLACK), this);
		}
	}

	private void resetGameView() {
		lastMove.setText("");
		clearSelection();
		clearPromotionButtons();
		numTakenPieces = game.getNumTakenPieces();
		updateStatusLabel();
		updateFenDisplay();
		disableButtons();
		enableButtons();
		updateBoard();
	}

	private void updateMenuModeButtons() {
		playerVsPlayerButton.setText(gameMode == GameMode.PLAYER_VS_PLAYER
				? "Player vs. Player *" : "Player vs. Player");
		playerVsBotButton.setText(gameMode == GameMode.PLAYER_VS_BOT ? "Player vs. Bot *" : "Player vs. Bot");
		botVsBotButton.setText(gameMode == GameMode.BOT_VS_BOT ? "Bot vs. Bot *" : "Bot vs. Bot");
		playWhiteButton.setDisable(gameMode != GameMode.PLAYER_VS_BOT);
		playBlackButton.setDisable(gameMode != GameMode.PLAYER_VS_BOT);
		playWhiteButton.setText(humanColor == Color.WHITE ? "Play White *" : "Play White");
		playBlackButton.setText(humanColor == Color.BLACK ? "Play Black *" : "Play Black");
	}

	private boolean isBotTurn() {
		return controller != null && !controller.isHumanTurn();
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
		leftRegion.getStyleClass().add("side-panel");
		right.getStyleClass().add("side-panel");
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
		bottom.setMinHeight(96);
		bottom.setPadding(new Insets(10, 16, 14, 16));
		moveBar.setAlignment(Pos.CENTER_LEFT);
		moveBar.setMinHeight(38);
		fenBar.setAlignment(Pos.CENTER_LEFT);
		currentFenField.setEditable(false);
		currentFenField.setFocusTraversable(false);
		currentFenField.setPrefWidth(760);
		HBox.setHgrow(currentFenField, Priority.ALWAYS);
		copyFenButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				ClipboardContent content = new ClipboardContent();
				content.putString(currentFenField.getText());
				Clipboard.getSystemClipboard().setContent(content);
			}
		});
		fenBar.getChildren().add(currentFenField);
		fenBar.getChildren().add(copyFenButton);
		EventHandler<ActionEvent> restartEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				controller.restart();
				game = controller.getGame();
				resetGameView();
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
		if (isBotTurn()) {
			return;
		}
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
		if (!controller.submitHumanMove(startRank, startFile, rank, file)) {
			clearSelection();
			updateBoard();
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
				controller.promote(rank, file, tb.type);
				updateSquare(game.getCopyOfCurrBoard(), rank, file);
				clearPromotionButtons();
				enableButtons();
				updateStatusLabel();
				updateFenDisplay();
				ArrayList<Move> history = game.getMoveHistory();
				if (!history.isEmpty()) {
					lastMove.setText("Last Move: " + history.get(history.size() - 1));
				}
			}
		};
		for (Button b : promoteButtons) {
			b.setOnAction(event);
			if (!moveBar.getChildren().contains(b)) {
				moveBar.getChildren().add(b);
			}
		}
	}

	private void updateLastMove(int rank, int file) {
		if (promotionPending) {
			return;
		}
		ArrayList<Move> history = game.getMoveHistory();
		if (!history.isEmpty()) {
			lastMove.setText("Last Move: " + history.get(history.size() - 1));
		}
	}

	public void onGameStateChanged(Move move, Board beforeMove) {
		game = controller.getGame();
		if (move == null || beforeMove == null) {
			updateBoard();
			updateStatusLabel();
			updateFenDisplay();
			if (!gameIsOver()) {
				enableButtons();
			}
			return;
		}

		Piece movingPiece = beforeMove.getPiece(move.startRank, move.startFile);
		boolean enPassantCapture = move.enPassantCapture
				|| (movingPiece instanceof Pawn
				&& move.startFile != move.endFile
				&& beforeMove.getPiece(move.endRank, move.endFile) == null);
		boolean isCapture = game.getNumTakenPieces() != numTakenPieces;
		numTakenPieces = game.getNumTakenPieces();
		GameState state = game.getCurrState();
		boolean inCheck = state == GameState.WHITEINCHECK || state == GameState.BLACKINCHECK;
		playSound(inCheck ? chessCheck : isCapture ? chessTake : chessMove);
		clearSelection();
		updateMoveSquares(move.startRank, move.startFile, move.endRank, move.endFile, movingPiece, enPassantCapture);
		handlePromotionIfNeeded(move.endRank, move.endFile);
		if (promotionPending) {
			updateFenDisplay();
			return;
		}
		updateStatusLabel();
		updateLastMove(move.endRank, move.endFile);
		updateFenDisplay();
		if (!gameIsOver()) {
			enableButtons();
		}
	}

	public void onBotThinkingChanged(boolean thinking) {
		if (thinking) {
			clearSelection();
			disableButtons();
		} else {
			enableButtons();
		}
	}

	public void onInvalidMove(String message) {
		lastMove.setText(message);
		enableButtons();
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
		if (promotionPending || isBotTurn()) {
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
		updateFenDisplay();
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
			moveBar.getChildren().remove(b);
		}
		promotionPending = false;
	}

	private void updateFenDisplay() {
		if (game != null) {
			currentFenField.setText(game.toFEN());
		} else {
			currentFenField.setText("");
		}
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
