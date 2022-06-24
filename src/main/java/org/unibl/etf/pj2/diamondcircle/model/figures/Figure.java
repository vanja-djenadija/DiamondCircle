package org.unibl.etf.pj2.diamondcircle.model.figures;

import javafx.util.Duration;
import org.unibl.etf.pj2.diamondcircle.GameService;
import org.unibl.etf.pj2.diamondcircle.Main;
import org.unibl.etf.pj2.diamondcircle.model.elements.Color;
import org.unibl.etf.pj2.diamondcircle.model.elements.Diamond;
import org.unibl.etf.pj2.diamondcircle.model.elements.Element;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.unibl.etf.pj2.diamondcircle.Main.gameService;

public abstract class Figure extends Thread implements Element {

    private static int counter = 0;
    private String name;
    private Color color;
    private int numberOfDiamonds = 0;
    private int numberOfSteps = 0;
    private int numberOfCrossedFields = 0;
    private long movementTime = 0;
    private boolean isFinished = false;
    private boolean isStarted = false;
    private boolean isFallen = false;
    private int startPosition = 0;
    private int endPosition = 0;

    private final ArrayList<Integer> crossedFields = new ArrayList<>();
    public final ReentrantLock LOCK = new ReentrantLock();

    public Figure() {
        super();
    }

    public Figure(Color color) {
        this.color = color;
        name = "Figura " + (++counter);
    }

    public Figure(Color color, int numberOfSteps) {
        this.color = color;
        this.numberOfSteps = numberOfSteps;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setNumberOfSteps(int numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    @Override
    public String toString() {
        return ";" + color +
                ";steps=" + numberOfSteps + " crossed=" + numberOfCrossedFields + " diamond=" + numberOfDiamonds;
    }

    @Override
    public void run() {
        isStarted = true;
        synchronized (LOCK) {
            while (!gameService.isLastField(numberOfCrossedFields)) {
                try {
                    LOCK.wait(); // wait for move
                } catch (InterruptedException e) {
                    Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                }

                long start = System.currentTimeMillis();
                if (isFallen) {
                    isFinished = true;
                } else {
                    int matrixDim = gameService.getMatrixDim();
                    startPosition = gameService.getPathElement(numberOfCrossedFields);

                    // make figure continue where it left in previous move
                    int currentIndex, indexBeforeMove;
                    if (numberOfCrossedFields > 0 && numberOfSteps + numberOfDiamonds > 0) {
                        indexBeforeMove = gameService.getPathElement(numberOfCrossedFields - 1) - 1;
                        int x = indexBeforeMove / matrixDim;
                        int y = indexBeforeMove % matrixDim;
                        if (gameService.matrix[x][y] instanceof Diamond) {
                            pickDiamond(indexBeforeMove, x, y);
                        }
                        gameService.getRemoveFigure().accept(indexBeforeMove);
                        gameService.matrix[x][y] = null;
                    }
                    int x, y, temp = numberOfCrossedFields;
                    int numberOfStepsTemp = numberOfSteps;
                    for (int i = 0; numberOfCrossedFields < gameService.getPathSize() && i < numberOfStepsTemp + numberOfDiamonds; i++, numberOfCrossedFields++) {
                        // pause movement of figure
                        synchronized (gameService.PAUSE_LOCK) {
                            try {
                                if (gameService.isPause())
                                    //noinspection WaitWhileHoldingTwoLocks
                                    gameService.PAUSE_LOCK.wait();
                            } catch (InterruptedException e) {
                                Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                            }
                        }
                        currentIndex = gameService.getPathElement(numberOfCrossedFields) - 1;
                        x = currentIndex / matrixDim;
                        y = currentIndex % matrixDim;
                        if (temp + numberOfStepsTemp + numberOfDiamonds - 1 < gameService.getPathSize() - 1) {
                            endPosition = gameService.getPathElement(temp + numberOfStepsTemp + numberOfDiamonds - 1);
                        }
                        Element element = gameService.matrix[x][y];
                        if (element instanceof Diamond)
                            pickDiamond(currentIndex, x, y);

                        if (element instanceof Figure && (i + 1 == numberOfStepsTemp + numberOfDiamonds))
                            numberOfStepsTemp++;

                        if (!(element instanceof Figure)) {
                            gameService.matrix[x][y] = this; // update matrix
                            gameService.getAddFigure().accept(this, currentIndex); // update gui - add figure
                        }
                        try {
                            //noinspection BusyWait
                            Thread.sleep(gameService.SLEEP_TIME); // move every 1 second
                        } catch (InterruptedException e) {
                            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                        }
                        // remove figure ONLY if it is not end of a move, OR end of movement for figure
                        if ((i + 1 < numberOfStepsTemp + numberOfDiamonds || gameService.isLastField(numberOfCrossedFields + 1)) && !(element instanceof Figure)) {
                            gameService.getRemoveFigure().accept(currentIndex); // update gui - remove figure
                            gameService.matrix[x][y] = null;
                        }
                        crossedFields.add(currentIndex + 1);
                    }
                }
                long end = System.currentTimeMillis();
                movementTime += (end - start);
                Duration duration = new Duration(movementTime);
                // save to file info about each movement
                new FigureMovement(this.name, this.getLabel(), String.format("%02d:%02d:%02d", (int) duration.toHours(), (int) duration.toMinutes(), (int) duration.toSeconds()), this.color, this.crossedFields);
                LOCK.notify(); // notify for end of move
            }
        }
        isFinished = true;
    }

    private void pickDiamond(int index, int x, int y) {
        gameService.getRemoveDiamond().accept(index);
        numberOfDiamonds++;
        gameService.matrix[x][y] = null;
    }

    public String getResult() {
        return String.format("\t%s (%s, %s) - pređeni put %s - stigla do cilja: %s\n", name, getType(), color, crossedFields,
                (Main.gameService.isLastField(numberOfCrossedFields) ? "Da" : "Ne"));
    }

    public String getLabel() {
        return getClass().getSimpleName().substring(0, 1);
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    public String getFigureName() {
        return name;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public int getNumberOfFields() {
        return numberOfSteps + numberOfDiamonds;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void fallInsideHole(int index) {
        gameService.getRemoveFigure().accept(index);
        isFallen = true;
    }
}
