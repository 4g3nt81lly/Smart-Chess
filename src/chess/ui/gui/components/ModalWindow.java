package chess.ui.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModalWindow extends JDialog {

    private final MouseAdapter clickedElsewhereAdaptor;

    public ModalWindow(Window owner) {
        super(owner);
        this.setModal(true);
        this.setResizable(false);
        this.clickedElsewhereAdaptor = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ModalWindow.this.clickedElsewhere();
            }
        };
        this.setFocusResignable(true);
    }

    public final void setFocusResignable(boolean resignable) {
        if (resignable) {
            this.addMouseListener(this.clickedElsewhereAdaptor);
        } else {
            this.removeMouseListener(this.clickedElsewhereAdaptor);
        }
    }

    protected void clickedElsewhere() {
        this.requestFocus();
    }

    public final void centerRelativeToOwner(Window owner) {
        int x = owner.getX() + owner.getWidth() / 2 - this.getWidth() / 2;
        int y = owner.getY() + (owner.getHeight() - this.getHeight()) / 2;
        this.setLocation(x, y);
    }

    public void display() {
        this.setVisible(true);
    }

    public void dismiss() {
        this.setVisible(false);
    }

    public void close() {
        this.dismiss();
        this.setFocusResignable(false);
        this.dispose();
    }

}
