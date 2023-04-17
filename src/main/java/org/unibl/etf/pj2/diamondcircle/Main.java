package org.unibl.etf.pj2.diamondcircle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final String MAIN_FXML = "/fxml/DiamondCircle.fxml";
    public static GameService gameService;

    public static void main(String[] args) throws InterruptedException {
        try {
            gameService = new GameService();
        } catch (Exception e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
        Application.launch(Main.class); // start of gui thread
    }


    @Override
    public void start(Stage stage) throws IOException {
        stage = Util.newInputWindow("Diamond circle", MAIN_FXML);
        //closes all stages when the main stage is closed
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}
