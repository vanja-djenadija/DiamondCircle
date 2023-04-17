package org.unibl.etf.pj2.diamondcircle.model.cards;

import javafx.scene.image.Image;

import java.io.File;

public abstract class Card {

    private final String imagePath;

    public Card(String imagePath) {
        this.imagePath = imagePath;
    }

    public Image getCardImage() {
        return new Image(new File(imagePath).toURI().toString(), 200, 300, false, false);
    }
}
