package org.unibl.etf.pj2.diamondcircle;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.unibl.etf.pj2.diamondcircle.GameService.RESULTS_PATH;

public class Util {

    private static final String APP_ICON_PATH = "src/main/resources/img/diamond.png";

    public static Stage newInputWindow(String name, String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(Util.class.getResource(path));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 700, Color.TRANSPARENT);
        Stage newStage = new Stage();
        newStage.setTitle(name);
        Image applicationIcon = new Image(new File(APP_ICON_PATH).toURI().toString());
        newStage.getIcons().add(applicationIcon);
        newStage.setScene(scene);
        return newStage;
    }

    public static GridPane createGridPane(int matrixDimension, StackPane[][] fieldMatrix) {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(false);
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        for (int i = 0; i < matrixDimension; i++) {
            for (int j = 0; j < matrixDimension; j++) {
                int number = i * matrixDimension + j + 1;
                StackPane field = new StackPane();
                field.setStyle("-fx-background-color: WHITE");
                field.setAlignment(Pos.CENTER);
                field.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.2))));
                Label labelDown = new Label(Integer.toString(number));
                labelDown.setAlignment(Pos.CENTER);
                labelDown.setPrefHeight(50);
                labelDown.setPrefWidth(50);
                field.getChildren().addAll(labelDown);
                gridPane.add(field, j, i);
                fieldMatrix[i][j] = field;
            }
        }
        return gridPane;
    }

    public static String getNumbersGamePlayed() {
        File[] files = new File(RESULTS_PATH).listFiles();
        assert files != null;
        return String.valueOf(files.length);
    }

    /**
     *  Utility method used for ensuring threads go to wait after being created
     */
    public static void sleepNow() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }
}
