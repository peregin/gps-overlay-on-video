package peregin.tov.gui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MigPanel extends JPanel {

    public MigPanel(String layoutConstraints, String colConstraints, String rowConstraints) {
        super(new MigLayout(layoutConstraints, colConstraints, rowConstraints));
    }
}
