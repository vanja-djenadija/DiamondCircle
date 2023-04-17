package org.unibl.etf.pj2.diamondcircle.model.figures;

import org.unibl.etf.pj2.diamondcircle.GameService;
import org.unibl.etf.pj2.diamondcircle.model.elements.Diamond;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.unibl.etf.pj2.diamondcircle.Main.gameService;

// singleton
public class GhostFigure extends Thread {

    private final ArrayList<Integer> path = new ArrayList<>();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static GhostFigure instance = null;

    private GhostFigure() {
        generatePath();
    }

    public static GhostFigure getInstance() {
        if (instance == null)
            instance = new GhostFigure();
        return instance;
    }

    private void generatePath() {
        path.clear();
        ArrayList<Integer> tempPath = new ArrayList<>();
        int numberOfDiamonds = random.nextInt(2, gameService.getMatrixDim() + 1);
        int i = 0;
        while (i < numberOfDiamonds) {
            int randomIndex = random.nextInt(gameService.getPathSize());
            int pathElement = gameService.getPathElement(randomIndex);
            if (!tempPath.contains(pathElement)) {
                tempPath.add(pathElement);
                i++;
            }
        }
        for (int j = 0; j < gameService.getPathSize(); j++) {
            int pathElement = gameService.getPathElement(j);
            if (tempPath.contains(pathElement))
                path.add(pathElement);
        }
    }

    @Override
    public void run() {
        int matrixDim = gameService.getMatrixDim();
        while (!gameService.isGameOver()) {
            // adding diamonds on fields
            for (Integer diamondIndex : path) {
                if (gameService.isGameOver()) break;
                synchronized (gameService.PAUSE_LOCK) {
                    if (gameService.isPause()) {
                        try {
                            gameService.PAUSE_LOCK.wait();
                        } catch (InterruptedException e) {
                            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                        }
                    }
                }
                diamondIndex--;
                int x = diamondIndex / matrixDim;
                int y = diamondIndex % matrixDim;
                if (gameService.matrix[x][y] == null) {
                    Diamond diamond = new Diamond();
                    gameService.matrix[x][y] = diamond; // update matrix of elements
                    gameService.getAddDiamond().accept(diamond, diamondIndex); // update GUI
                }
                // sleep for 5 seconds
                try {
                    //noinspection BusyWait
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                }
            }
            generatePath();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}