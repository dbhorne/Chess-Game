package itec220.labs;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * This class will hand the UI logic behind the chess game and communicate
 * with the Game class to determine how to set up the board
 */
public class ChessUI extends Application {

    @Override
    public void start(Stage stage) {
    	stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}