package org.unibl.etf.pj2.diamondcircle.gui_controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.unibl.etf.pj2.diamondcircle.Main;
import org.unibl.etf.pj2.diamondcircle.Util;
import org.unibl.etf.pj2.diamondcircle.model.elements.Color;
import org.unibl.etf.pj2.diamondcircle.model.figures.FigureMovement;

import java.util.ArrayList;

public class FigureMovementController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Label movementTimeLabel;

    private StackPane[][] fieldMatrix;

    @FXML
    public void initialize() {
        int matrixDimension = Main.gameService.getMatrixDim();
        fieldMatrix = new StackPane[matrixDimension][matrixDimension];
        GridPane gridPane = Util.createGridPane(matrixDimension, fieldMatrix);
        gridPane.setAlignment(Pos.CENTER);
        FigureMovement figureMovement = FigureMovement.deserialize(DiamondCircleController.getFilePath());
        showFigureMovement(figureMovement);
        borderPane.setCenter(gridPane);
    }

    private void showFigureMovement(FigureMovement figureMovement) {
        Color color = figureMovement.getColor();
        String label = figureMovement.getLabel();
        ArrayList<Integer> crossedFields = figureMovement.getCrossedFields();
        String movementTime = figureMovement.getMovementTime();
        movementTimeLabel.setText(movementTime);
        int dim = fieldMatrix.length;
        for (Integer fieldIndex : crossedFields) {
            fieldIndex--;
            StackPane field = this.fieldMatrix[fieldIndex / dim][fieldIndex % dim];
            field.setStyle("-fx-background-color: " + color.toString());
            Label l = (Label) field.getChildren().get(0);
            l.setText(label);
        }
    }
}
