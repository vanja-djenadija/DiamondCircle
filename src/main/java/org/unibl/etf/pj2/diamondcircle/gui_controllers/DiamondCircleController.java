package org.unibl.etf.pj2.diamondcircle.gui_controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.unibl.etf.pj2.diamondcircle.GameService;
import org.unibl.etf.pj2.diamondcircle.Main;
import org.unibl.etf.pj2.diamondcircle.Util;
import org.unibl.etf.pj2.diamondcircle.model.cards.Card;
import org.unibl.etf.pj2.diamondcircle.model.elements.Diamond;
import org.unibl.etf.pj2.diamondcircle.model.elements.Hole;
import org.unibl.etf.pj2.diamondcircle.model.figures.Figure;
import org.unibl.etf.pj2.diamondcircle.model.figures.FigureMovement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.*;
import static org.unibl.etf.pj2.diamondcircle.Main.gameService;


public class DiamondCircleController {

    private static final String RESULTS_FXML = "/fxml/Results.fxml";
    private static final String FIGURE_MOVEMENT_FXML = "/fxml/FigureMovement.fxml";

    @FXML
    public Label numberOfRoundsPlayed;

    @FXML
    public BorderPane borderPane;

    @FXML
    private HBox centerHBox;

    @FXML
    private ImageView cardImageView;

    @FXML
    private TextArea cardTextArea;

    @FXML
    private HBox playersHBox;

    @FXML
    private ListView<String> figuresListView;

    @FXML
    private Label timeLabel;

    @FXML
    private Label numbersPlayedLabel;

    @FXML
    private Button startButton;

    private StackPane[][] fieldMatrix;
    private final int matrixDimension;
    private static String filePath;
    private int startGameButtonClicked = 0;


    public DiamondCircleController() {
        matrixDimension = Main.gameService.getMatrixDim(); // getting matrix dim from GameService
        Consumer<Card> cardConsumer = (Card card) -> Platform.runLater(() -> cardImageView.setImage(card.getCardImage()));
        BiConsumer<Diamond, Integer> addDiamondConsumer = (diamond, index) -> Platform.runLater(() -> ((Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0)).setGraphic(new ImageView(diamond.getDiamondImage())));
        Consumer<Integer> removeDiamondConsumer = (index) -> Platform.runLater(() -> ((Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0)).setGraphic(null));
        BiConsumer<Hole, Integer> addHole = (hole, index) -> Platform.runLater(() -> {
            Label label = (Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0);
            label.setStyle(label.getStyle() + "; -fx-border-width: 3px; -fx-border-color: " + hole.getColor());
        });
        Consumer<Integer> removeHole = (index) -> Platform.runLater(() -> {
            Label label = (Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0);
            label.setStyle(label.getStyle() + "; -fx-border-color: TRANSPARENT");
        });
        BiConsumer<Figure, Integer> addFigureConsumer = (figure, index) -> Platform.runLater(() -> {
            Label labelUp = (Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0);
            labelUp.setText(figure.getLabel());
            labelUp.setStyle("-fx-background-color: " + figure.getColor());
        });
        Consumer<Integer> removeFigureConsumer = (index) -> Platform.runLater(() -> {
            Label labelUp = (Label) fieldMatrix[index / matrixDimension][index % matrixDimension].getChildren().get(0);
            labelUp.setText(Integer.toString(index + 1));
            labelUp.setStyle("-fx-background-color: TRANSPARENT");
        });
        Runnable gameOverRunnable = () -> Platform.runLater(() -> {
            startButton.setDisable(true);
            numbersPlayedLabel.setText(Util.getNumbersGamePlayed());
        });
        gameService.setShowCard(cardConsumer);
        gameService.setAddDiamond(addDiamondConsumer);
        gameService.setRemoveDiamond(removeDiamondConsumer);
        gameService.setAddFigure(addFigureConsumer);
        gameService.setRemoveFigure(removeFigureConsumer);
        gameService.setGameOverRunnable(gameOverRunnable);
        gameService.setAddHole(addHole);
        gameService.setRemoveHole(removeHole);
    }

    @FXML
    public void initialize() {
        fieldMatrix = new StackPane[matrixDimension][matrixDimension];
        GridPane gridPane = Util.createGridPane(matrixDimension, fieldMatrix);
        centerHBox.getChildren().add(1, gridPane);
        cardImageView.setStyle("-fx-background-color: WHITE");
        ArrayList<Label> playerLabels = generateLabels();
        playersHBox.getChildren().addAll(playerLabels);
        figuresListView.getItems().addAll(Main.gameService.getFigureNames());
        numbersPlayedLabel.setText(Util.getNumbersGamePlayed());
    }

    @FXML
    private void buttonAction() {
        if (startGameButtonClicked % 2 == 0)
            startGame();
        else
            pauseGame();

        startGameButtonClicked++;
    }

    private void startGame() {
        if (startGameButtonClicked == 0) {
            Thread gameDuration = measureGameDuration();
            gameDuration.start();
            new Thread(() -> gameService.gameStart()).start(); // GameService business logic (non-blocking gui)
            Thread movementMessage = movementMessage();
            movementMessage.start();
        }
        startButton.setText("Zaustavi");
        gameService.setPause(false);
    }

    private void pauseGame() {
        startButton.setText("Pokreni");
        gameService.setPause(true);
    }

    private Thread movementMessage() {
        return new Thread(() -> {
            while (!gameService.isGameOver()) {
                if (!gameService.isPause()) {
                    Platform.runLater(() -> cardTextArea.setText(gameService.getMovementMessage()));
                    try {
                        //noinspection BusyWait
                        sleep(gameService.SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                    }
                }
            }
        });
    }

    private Thread measureGameDuration() {
        return new Thread(() -> {
            int h = 0, m = 0, s = 0;
            while (!gameService.isGameOver()) {
                if (!gameService.isPause()) {
                    String time = String.format("%d h %d m %d s", h, m, s);
                    gameService.setElapsedTime(time);
                    Platform.runLater(() -> timeLabel.setText(time));
                    try {
                        //noinspection BusyWait
                        sleep(gameService.SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                    }
                    s++;
                    if (s >= 60) {
                        m++;
                        s %= 60;
                    }
                    if (m >= 60) {
                        h++;
                        m %= 60;
                    }
                }
            }
            System.out.printf("Game OVER Total time: %d h %d m %d s%n", h, m, s);
            gameService.setElapsedTime(String.format("%d h %d m %d s", h, m, s));
        });
    }

    private ArrayList<Label> generateLabels() {
        HashMap<String, String> players = Main.gameService.getPlayers();
        ArrayList<Label> playerLabels = new ArrayList<>();
        for (Map.Entry<String, String> entry : players.entrySet()) {
            Label l = new Label(entry.getKey());
            l.setTextFill(Color.valueOf(entry.getValue()));
            l.setStyle("-fx-font-weight: bold");
            playerLabels.add(l);
        }
        return playerLabels;
    }

    public void showResults() {
        try {
            Util.newInputWindow("Results", RESULTS_FXML).show();
        } catch (IOException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }

    public void showFigureMovement() {
        Object selectedItem = figuresListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        filePath = selectedItem.toString();
        File[] files = new File(FigureMovement.MOVEMENTS_PATH).listFiles();
        assert files != null;
        if (Arrays.stream(files).anyMatch((file) -> file.getName().endsWith(filePath + ".ser"))) {
            try {
                Util.newInputWindow("Figure Movement", FIGURE_MOVEMENT_FXML).show();
            } catch (IOException e) {
                Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
            }
        }

    }

    public static String getFilePath() {
        return filePath;
    }
}