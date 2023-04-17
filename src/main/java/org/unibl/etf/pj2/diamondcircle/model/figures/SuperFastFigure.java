package org.unibl.etf.pj2.diamondcircle.model.figures;

import org.unibl.etf.pj2.diamondcircle.model.elements.Color;

public class SuperFastFigure extends Figure {

    public SuperFastFigure() {
        super();
    }

    public SuperFastFigure(Color color) {
        super(color);
    }

    public SuperFastFigure(Color color, int numberOfSteps) {
        super(color, numberOfSteps);
    }

    @Override
    public void setNumberOfSteps(int numberOfSteps) {
        super.setNumberOfSteps(numberOfSteps * 2);
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + super.toString();
    }

    @Override
    public String getLabel() {
        return getClass().getSimpleName().substring(0, 1);
    }
}
