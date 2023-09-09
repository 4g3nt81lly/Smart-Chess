package chess.ui.gui.views.game;

import chess.ui.gui.ChessGUI;
import chess.ui.gui.views.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static chess.ui.Presets.*;

public class ChevronButton extends JButton {

    // REQUIRES: orientation is one of SwingConstants.LEFT or SwingConstants.RIGHT
    // EFFECTS: returns the chevron image icon of the given orientation
    private static ImageIcon getChevronIcon(int orientation) {
        String suffix = (orientation == SwingConstants.LEFT) ? "left" : "right";
        // https://stackoverflow.com/a/2856518
        ImageIcon imageIcon = new ImageIcon(
                ChessGUI.getResource("/images/chevron." + suffix + ".png")
        );
        int scaleFactor = 8;
        return ViewUtils.scaleImageIcon(imageIcon,
                imageIcon.getIconWidth() / scaleFactor,
                imageIcon.getIconHeight() / scaleFactor);
    }

    // REQUIRES: orientation is one of SwingConstants.LEFT or SwingConstants.RIGHT
    // EFFECTS: constructs a chevron button of the given orientation
    public ChevronButton(int orientation) {
        super(getChevronIcon(orientation));
        this.setFocusable(false);
        this.setBorderPainted(false);
        this.setBackground(lightGray);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if (isEnabled()) {
                    setBackground(gray);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (isEnabled()) {
                    setBackground(lightGray);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (isEnabled()) {
                    setBackground(gray);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                setBackground(lightGray);
            }
        });
    }

    // EFFECTS: paints the component
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cornerRadius = 20;

        graphics.setColor(this.getBackground());
        if (!this.isEnabled()) {
            graphics.setColor(disabledChevronButtonColor);
        }
        graphics.fillRoundRect(0, 0, this.getWidth(), this.getHeight(),
                cornerRadius, cornerRadius);

        super.paintComponent(g);
    }
}
