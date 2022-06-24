package org.unibl.etf.pj2.diamondcircle.model;

import org.unibl.etf.pj2.diamondcircle.GameService;
import org.unibl.etf.pj2.diamondcircle.model.elements.Color;
import org.unibl.etf.pj2.diamondcircle.model.figures.BasicFigure;
import org.unibl.etf.pj2.diamondcircle.model.figures.Figure;
import org.unibl.etf.pj2.diamondcircle.model.figures.LevitatingFigure;
import org.unibl.etf.pj2.diamondcircle.model.figures.SuperFastFigure;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.unibl.etf.pj2.diamondcircle.Util.sleepNow;


public class Player extends Thread {
    private static final int NUMBER_OF_FIGURES = 4;
    private static int counter = 0;
    public final ReentrantLock LOCK = new ReentrantLock();

    private Figure currentFigure;
    private final String name;
    private int numberOfFields = 0;
    private int currentFigureNumber = 0;

    private boolean isFinished = false;
    private boolean isStarted = false;

    private final ArrayList<Figure> figures = new ArrayList<>(NUMBER_OF_FIGURES);

    public Player(Color color) {
        this("Igrač " + (++counter), color);
    }

    public Player(String name, Color color) {
        this.name = name;
        generateFigures(color);
    }

    private void generateFigures(Color color) {
        Random rand = new Random();
        for (int i = 0; i < NUMBER_OF_FIGURES; i++) {
            int type = rand.nextInt(3);
            Figure figure = null;
            switch (type) {
                case 0:
                    figure = new BasicFigure(color);
                    break;
                case 1:
                    figure = new LevitatingFigure(color);
                    break;
                case 2:
                    figure = new SuperFastFigure(color);
                    break;
            }
            figures.add(figure);
        }
    }

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' +
                ", figures=" + figures +
                '}';
    }

    public String getPlayerName() {
        return name;
    }

    public String getColor() {
        return figures.get(0).getColor().toString();
    }

    public ArrayList<String> getFigureNames() {
        return figures.stream().map(Figure::getFigureName).collect(Collectors.toCollection(ArrayList::new));
    }

    public String getResult() {
        StringBuilder sb = new StringBuilder(name + "\n");
        for (Figure f : figures) {
            sb.append(f.getResult());
        }
        return sb.toString();
    }

    @Override
    public void run() {
        isStarted = true;
        synchronized (this.LOCK) {
            while (currentFigureNumber != NUMBER_OF_FIGURES) {
                try {
                    this.LOCK.wait(); // wait for move
                } catch (InterruptedException e) {
                    Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                }

                currentFigure = figures.get(currentFigureNumber);
                currentFigure.setNumberOfSteps(numberOfFields);

                if (!currentFigure.isStarted()) {
                    currentFigure.start();
                    sleepNow();
                }

                synchronized (currentFigure.LOCK) {
                    currentFigure.LOCK.notify(); // figure starts its move
                    try {
                        //noinspection WaitWhileHoldingTwoLocks
                        currentFigure.LOCK.wait(); // player waits for figure to end move
                        if (currentFigure.isFinished()) {
                            currentFigureNumber++;
                        }
                    } catch (InterruptedException e) {
                        Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                    }
                }
                LOCK.notify(); // notify game-service player ended its move
            }
        }
        isFinished = true;
    }

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public Figure getCurrentFigure() {
        return currentFigure;
    }
}
