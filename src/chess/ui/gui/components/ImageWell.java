package chess.ui.gui.components;

import javax.swing.*;
import java.awt.*;

public class ImageWell extends JPanel {

    private Image image;

    public ImageWell(Image image) {
        super();
        this.image = image;
    }

    public ImageWell(ImageIcon imageIcon) {
        this(imageIcon.getImage());
    }

    public final Image getImage() {
        return this.image;
    }

    public final void setImage(Image image) {
        this.image = image;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(this.image, 0, 0, null);
    }

}
