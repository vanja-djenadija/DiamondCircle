package org.unibl.etf.pj2.diamondcircle.model.elements;

public class Hole implements Element {

    public String getColor() {
        return "BLACK";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
