package org.unibl.etf.pj2.diamondcircle.model.figures;

import org.unibl.etf.pj2.diamondcircle.model.elements.Color;

public class LevitatingFigure extends Figure implements ILevitatable {

    public LevitatingFigure() {
        super();
    }

    public LevitatingFigure(Color color) {
        super(color);
    }

    public LevitatingFigure(Color color, int numberOfSteps) {
        super(color, numberOfSteps);
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + super.toString();
    }

    @Override
    public String getLabel() {
        return getClass().getSimpleName().substring(0, 1);
    }

    public String getType() {
        return getClass().getSimpleName();
    }
}
