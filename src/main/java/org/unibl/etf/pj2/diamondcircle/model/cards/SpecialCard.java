package org.unibl.etf.pj2.diamondcircle.model.cards;

import org.unibl.etf.pj2.diamondcircle.GameService;
import org.unibl.etf.pj2.diamondcircle.model.elements.Hole;
import org.unibl.etf.pj2.diamondcircle.model.figures.Figure;
import org.unibl.etf.pj2.diamondcircle.model.figures.ILevitatable;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.unibl.etf.pj2.diamondcircle.Main.gameService;

public class SpecialCard extends Card {

    private static final String SPECIAL_CARD_IMAGE_PATH = "src/main/resources/img/special-card.png";
    private final int numberOfHoles;
    private final ArrayList<Hole> holes = new ArrayList<>();

    public SpecialCard(int numberOfHoles) {
        super(SPECIAL_CARD_IMAGE_PATH);
        this.numberOfHoles = numberOfHoles;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " Broj rupa: " + numberOfHoles;
    }

    public void makeHoles() {
        ArrayList<Integer> tempPath = new ArrayList<>(); // holes path
        ArrayList<Integer> nonLevitatingIndexes = new ArrayList<>();
        int i = 0;
        while (i < numberOfHoles) {
            int randomIndex = new Random().nextInt(gameService.getPathSize());
            int pathElement = gameService.getPathElement(randomIndex);
            if (!tempPath.contains(pathElement)) {
                tempPath.add(pathElement);
                i++;
            }
        }
        int matrixDim = gameService.getMatrixDim();
        for (Integer index : tempPath) {
            Hole hole = new Hole();
            holes.add(hole);
            int currentIndex = index - 1;
            int x = currentIndex / matrixDim;
            int y = currentIndex % matrixDim;
            nonLevitatingIndexes.add(currentIndex);
            gameService.getAddHole().accept(hole, currentIndex); // update gui - add holes
            if ((gameService.matrix[x][y] instanceof Figure) && !(gameService.matrix[x][y] instanceof ILevitatable)) {
                ((Figure) gameService.matrix[x][y]).fallInsideHole(currentIndex);
                gameService.matrix[x][y] = null;
            }
        }
        // after sleep_time remove all holes
        try {
            Thread.sleep(gameService.SLEEP_TIME);
        } catch (InterruptedException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
        // update gui - remove holes
        for (Integer index : nonLevitatingIndexes) {
            gameService.getRemoveHole().accept(index);
        }
    }
}

