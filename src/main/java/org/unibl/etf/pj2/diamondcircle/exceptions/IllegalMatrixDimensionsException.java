package org.unibl.etf.pj2.diamondcircle.exceptions;

public class IllegalMatrixDimensionsException extends Exception {
    private static final String ILLEGAL_MATRIX_DIMENSION_MESSAGE = "Dimenzija matrice nije validna.";

    public IllegalMatrixDimensionsException() {
        this(ILLEGAL_MATRIX_DIMENSION_MESSAGE);
    }

    public IllegalMatrixDimensionsException(String message) {
        super(message);
    }
}
