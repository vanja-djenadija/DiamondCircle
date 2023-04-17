package org.unibl.etf.pj2.diamondcircle;

import org.apache.commons.io.FileUtils;
import org.unibl.etf.pj2.diamondcircle.exceptions.IllegalMatrixDimensionsException;
import org.unibl.etf.pj2.diamondcircle.exceptions.IllegalNumberOfPlayersException;
import org.unibl.etf.pj2.diamondcircle.model.Player;
import org.unibl.etf.pj2.diamondcircle.model.cards.BasicCard;
import org.unibl.etf.pj2.diamondcircle.model.cards.Card;
import org.unibl.etf.pj2.diamondcircle.model.cards.SpecialCard;
import org.unibl.etf.pj2.diamondcircle.model.elements.Color;
import org.unibl.etf.pj2.diamondcircle.model.elements.Diamond;
import org.unibl.etf.pj2.diamondcircle.model.elements.Element;
import org.unibl.etf.pj2.diamondcircle.model.elements.Hole;
import org.unibl.etf.pj2.diamondcircle.model.figures.Figure;
import org.unibl.etf.pj2.diamondcircle.model.figures.FigureMovement;
import org.unibl.etf.pj2.diamondcircle.model.figures.GhostFigure;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.unibl.etf.pj2.diamondcircle.Util.sleepNow;

public class GameService {

    private static final String MOVEMENT_MASSAGE_FORMAT = "Na potezu je %s, %s prelazi %d polja, pomjera se sa pozicije %d na poziciju %d.";
    private static final String CONFIG_PATH = "src/main/resources/config.properties";
    public static final String RESULTS_PATH = "src/main/resources/results/";
    private static final String GAME_LOGGER_PATH = "src/main/resources/logs/GameService.log";
    private static final int MIN_DIM = 7;
    private static final int MAX_DIM = 10;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    private static final int NUMBER_OF_BASIC_CARDS = 10;
    private static final int NUMBER_OF_SPECIAL_CARDS = 12;
    public final ReentrantLock PAUSE_LOCK = new ReentrantLock();
    public final long SLEEP_TIME = 100;

    static {
        try {
            Handler fileHandler = new FileHandler(GAME_LOGGER_PATH, true);
            Logger.getLogger(GameService.class.getName()).setUseParentHandlers(false);
            Logger.getLogger(GameService.class.getName()).addHandler(fileHandler);
        } catch (IOException e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }

    private int matrixDim;
    private int numberOfPlayers;
    private String elapsedTime;
    private Player currentPlayer;
    private Card currentCard;

    private volatile boolean pause = false;
    private volatile boolean gameOver = false;

    public Element[][] matrix;
    private final ArrayList<Integer> path = new ArrayList<>();
    private final LinkedList<Player> players = new LinkedList<>();
    private final LinkedList<Card> cards = new LinkedList<>();

    private BiConsumer<Diamond, Integer> addDiamond;
    private Consumer<Integer> removeDiamond;
    private BiConsumer<Figure, Integer> addFigure;
    private Consumer<Integer> removeFigure;
    private BiConsumer<Hole, Integer> addHole;
    private Consumer<Integer> removeHole;
    private Consumer<Card> showCard;
    private Runnable gameOverRunnable;


    public GameService() throws IllegalNumberOfPlayersException, IllegalMatrixDimensionsException, IOException {
        super();
        validateConfiguration();
        configurePath();
        configurePlayers();
        configureCards();
        emptyMovementsFolder();
    }

    private void emptyMovementsFolder() throws IOException {
        FileUtils.cleanDirectory(new File(FigureMovement.MOVEMENTS_PATH));
    }

    public void gameStart() {
        GhostFigure ghostFigure = GhostFigure.getInstance();
        ghostFigure.start();

        LinkedList<Player> tempPlayers = new LinkedList<>(players);
        int playersFinished = 0;
        while (!gameOver) {
            currentCard = cards.removeFirst();
            showCard.accept(currentCard);
            int numberOfFields;
            if (currentCard instanceof BasicCard) {
                numberOfFields = ((BasicCard) currentCard).getNumberOfFields();
                currentPlayer = tempPlayers.removeFirst();
                currentPlayer.setNumberOfFields(numberOfFields);

                if (!currentPlayer.isStarted()) {
                    currentPlayer.start();
                    sleepNow();
                }

                synchronized (currentPlayer.LOCK) {
                    currentPlayer.LOCK.notify(); // notify player to start playing
                    try {
                        currentPlayer.LOCK.wait(); // waits for player to finish move
                        if (!currentPlayer.isFinished())
                            tempPlayers.addLast(currentPlayer);
                        else {
                            playersFinished++;
                            if (playersFinished == numberOfPlayers)
                                gameOver = true;
                        }
                    } catch (InterruptedException e) {
                        Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
                    }
                }
            } else if (currentCard instanceof SpecialCard) {
                ((SpecialCard) currentCard).makeHoles();
            }
            cards.addLast(currentCard);
        }
        saveResults();
        gameOverRunnable.run(); // update gui after game over
    }

    public BiConsumer<Diamond, Integer> getAddDiamond() {
        return addDiamond;
    }

    public void setAddDiamond(BiConsumer<Diamond, Integer> addDiamond) {
        this.addDiamond = addDiamond;
    }

    public Consumer<Integer> getRemoveDiamond() {
        return removeDiamond;
    }

    public void setRemoveDiamond(Consumer<Integer> removeDiamond) {
        this.removeDiamond = removeDiamond;
    }

    public BiConsumer<Figure, Integer> getAddFigure() {
        return addFigure;
    }

    public void setAddFigure(BiConsumer<Figure, Integer> addFigure) {
        this.addFigure = addFigure;
    }

    public Consumer<Integer> getRemoveFigure() {
        return removeFigure;
    }

    public void setRemoveFigure(Consumer<Integer> removeFigure) {
        this.removeFigure = removeFigure;
    }

    public void setShowCard(Consumer<Card> showCard) {
        this.showCard = showCard;
    }

    public void saveResults() {
        String fileName = RESULTS_PATH + String.format("IGRA_%d.txt", System.currentTimeMillis());
        try (PrintWriter pw = new PrintWriter(fileName)) {
            for (Player p : players) {
                pw.println(p.getResult());
            }
            pw.println("Ukupno vrijeme trajanja igre: " + getElapsedTime());
        } catch (FileNotFoundException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }

    private void configureCards() {
        try {
            Properties properties = loadProperties();
            Random rand = new Random();
            int n = Integer.parseInt(properties.getProperty("n"));
            for (int i = 0; i < NUMBER_OF_BASIC_CARDS; i++) {
                cards.add(new BasicCard(1));
                cards.add(new BasicCard(2));
                cards.add(new BasicCard(3));
                cards.add(new BasicCard(4));
            }
            for (int i = 0; i < NUMBER_OF_SPECIAL_CARDS; i++) {
                cards.add(new SpecialCard(rand.nextInt(n) + 1));
            }
            Collections.shuffle(cards);
        } catch (NumberFormatException e) { // for invalid n
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }

    private void configurePlayers() {
        List<Color> colors = Arrays.asList(Color.values());
        Collections.shuffle(colors);
        colors.stream().limit(numberOfPlayers).forEach(color -> players.add(new Player(color)));
        Collections.shuffle(players);
    }

    private void configurePath() {
        Properties properties = loadProperties();
        String pathStr = null;
        switch (matrixDim) {
            case 7:
                pathStr = properties.getProperty("diamondPath7");
                break;
            case 8:
                pathStr = properties.getProperty("diamondPath8");
                break;
            case 9:
                pathStr = properties.getProperty("diamondPath9");
                break;
            case 10:
                pathStr = properties.getProperty("diamondPath10");
                break;
        }
        assert pathStr != null;
        String[] numStr = pathStr.split(",");
        Arrays.stream(numStr).forEach(s -> path.add(Integer.parseInt(s)));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        FileInputStream fip;
        try {
            fip = new FileInputStream(CONFIG_PATH);
            properties.load(fip);
        } catch (IOException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
        return properties;
    }

    private void validateConfiguration() throws IllegalNumberOfPlayersException, IllegalMatrixDimensionsException {
        Properties properties = loadProperties();
        int noPlayers = Integer.parseInt(properties.getProperty("numberOfPlayers"));
        int dim = Integer.parseInt(properties.getProperty("matrixDimension"));

        // Validation: number of players
        if (noPlayers >= MIN_PLAYERS && noPlayers <= MAX_PLAYERS)
            numberOfPlayers = noPlayers;
        else
            throw new IllegalNumberOfPlayersException();

        // Validation: matrix dimensions
        if (dim >= MIN_DIM && dim <= MAX_DIM)
            matrixDim = dim;
        else
            throw new IllegalMatrixDimensionsException();

        matrix = new Element[matrixDim][matrixDim];
    }

    public int getMatrixDim() {
        return matrixDim;
    }

    public HashMap<String, String> getPlayers() {
        return players.stream().collect(Collectors.toMap(Player::getPlayerName, Player::getColor, (e1, e2) -> e2, HashMap::new));
    }

    public ArrayList<String> getFigureNames() {
        return players.stream().map(Player::getFigureNames).flatMap(ArrayList::stream).sorted(Comparator.comparingInt(String::length)
                .thenComparing(String::toString)).collect(Collectors.toCollection(ArrayList::new));
    }

    public void setElapsedTime(String time) {
        elapsedTime = time;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        synchronized (PAUSE_LOCK) {
            if (!pause)
                PAUSE_LOCK.notifyAll();
        }
        this.pause = pause;
    }

    public boolean isLastField(int index) {
        return index >= path.size();
    }

    public int getPathSize() {
        return path.size();
    }

    public int getPathElement(int index) {
        return path.get(index);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOverRunnable(Runnable gameOverRunnable) {
        this.gameOverRunnable = gameOverRunnable;
    }

    public void setAddHole(BiConsumer<Hole, Integer> addHole) {
        this.addHole = addHole;
    }

    public BiConsumer<Hole, Integer> getAddHole() {
        return addHole;
    }

    public String getMovementMessage() {
        if (currentCard instanceof SpecialCard) return currentCard.toString();
        if (currentPlayer == null) return "";
        String playerName = currentPlayer.getPlayerName();
        Figure figure = currentPlayer.getCurrentFigure();
        if (figure == null) return "";
        String figureName = figure.getFigureName();
        int numberOfFields = figure.getNumberOfFields();
        int startPosition = figure.getStartPosition();
        int endPosition = figure.getEndPosition();
        if (startPosition == 0 || endPosition == 0) return "";
        return String.format(MOVEMENT_MASSAGE_FORMAT, playerName, figureName, numberOfFields, startPosition, endPosition);
    }

    public void setRemoveHole(Consumer<Integer> removeHole) {
        this.removeHole = removeHole;
    }

    public Consumer<Integer> getRemoveHole() {
        return removeHole;
    }
}