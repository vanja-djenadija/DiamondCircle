package org.unibl.etf.pj2.diamondcircle.model.cards;

public class BasicCard extends Card {

    private static final String CARD_IMAGE_PATH_PREFIX = "src/main/resources/img/card";
    private final int numberOfFields;

    public BasicCard(int numberOfFields) {
        super(String.format(CARD_IMAGE_PATH_PREFIX + "%d.png", numberOfFields));
        this.numberOfFields = numberOfFields;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }
}
