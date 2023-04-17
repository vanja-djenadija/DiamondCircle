package org.unibl.etf.pj2.diamondcircle.model.elements;

import javafx.scene.image.Image;

import java.io.File;

public class Diamond implements Element {

    private static final String DIAMOND_PATH = "src/main/resources/img/diamond-element.png";
    private final String imagePath;

    public Diamond() {
        this.imagePath = DIAMOND_PATH;
    }

    public Image getDiamondImage() {
        return new Image(new File(imagePath).toURI().toString(), 15, 15, false, false);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
