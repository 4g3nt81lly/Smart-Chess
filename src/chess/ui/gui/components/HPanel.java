package chess.ui.gui.components;

import chess.util.List;

import javax.swing.*;

public class HPanel extends JPanel {

    private final List<JComponent> components = new List<>();

    public HPanel() {
        super();
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(boxLayout);
        this.setHorizontalAlignment(CENTER_ALIGNMENT);
        this.setVerticalAlignment(CENTER_ALIGNMENT);
    }

    public void add(JComponent component) {
        super.add(component);
        this.components.add(component);
        component.setAlignmentX(this.getAlignmentX());
        component.setAlignmentY(this.getAlignmentY());
    }

    public void addSpacer() {
        super.add(Box.createHorizontalGlue());
    }

    public void addSpacer(int width) {
        super.add(Box.createHorizontalStrut(width));
    }

    public void setVerticalAlignment(float verticalAlignment) {
        this.setAlignmentY(verticalAlignment);
        this.components.forEach(component -> component.setAlignmentY(verticalAlignment));
    }

    public void setHorizontalAlignment(float horizontalAlignment) {
        this.setAlignmentX(horizontalAlignment);
        this.components.forEach(component -> component.setAlignmentX(horizontalAlignment));
    }
}
