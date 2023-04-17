package org.unibl.etf.pj2.diamondcircle.exceptions;

public class IllegalNumberOfPlayersException extends Exception {

    private static final String ILLEGAL_NUMBER_PLAYERS_MESSAGE = "Broj igrača nije validan.";

    public IllegalNumberOfPlayersException() {
        this(ILLEGAL_NUMBER_PLAYERS_MESSAGE);
    }

    public IllegalNumberOfPlayersException(String message) {
        super(message);
    }
}
