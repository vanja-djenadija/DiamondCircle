module org.unibl.etf.pj2.diamondcircle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.apache.commons.io;


    opens org.unibl.etf.pj2.diamondcircle to javafx.fxml;
    exports org.unibl.etf.pj2.diamondcircle;
    exports org.unibl.etf.pj2.diamondcircle.gui_controllers;
    exports org.unibl.etf.pj2.diamondcircle.model.elements;
    exports org.unibl.etf.pj2.diamondcircle.model.cards;
    exports org.unibl.etf.pj2.diamondcircle.model.figures;
    exports org.unibl.etf.pj2.diamondcircle.exceptions;
    opens org.unibl.etf.pj2.diamondcircle.gui_controllers to javafx.fxml;
}