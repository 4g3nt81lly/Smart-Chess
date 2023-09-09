package chess.ui.gui.views.game;

import chess.model.player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static chess.model.TextSymbols.blackKing;
import static chess.model.TextSymbols.whiteKing;
import static chess.ui.Presets.*;

public class PlayerBox extends JPanel {

    private final Player player;

    private boolean toMove = false;

    private final JLabel playerNameLabel;

    // EFFECTS: constructs a player box view for the given player
    public PlayerBox(Player player) {
        super();
        this.player = player;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setAlignmentY(CENTER_ALIGNMENT);

        this.setPreferredSize(new Dimension(gamePanelMaxWidth, 40));
        this.setMaximumSize(new Dimension(gamePanelMaxWidth, 40));
        this.setAlignmentX(LEFT_ALIGNMENT);

        char textSymbol = player.isWhite() ? whiteKing : blackKing;
        JLabel playerIconLabel = new JLabel(String.valueOf(textSymbol));
        playerIconLabel.setBorder(new EmptyBorder(0, 0, 7, 0));
        playerIconLabel.setFont(playerIconLabel.getFont()
                .deriveFont(35f));
        this.add(Box.createHorizontalStrut(15));
        this.add(playerIconLabel);

        JLabel playerNameLabel = new JLabel(player.getName());
        playerNameLabel.setToolTipText(player.getName());
        playerNameLabel.setPreferredSize(new Dimension(gamePanelMaxWidth - 80, 30));
        playerNameLabel.setMaximumSize(new Dimension(gamePanelMaxWidth - 80, 30));
        playerNameLabel.setFont(playerNameLabel.getFont()
                .deriveFont(Font.BOLD));
        this.add(Box.createHorizontalStrut(10));
        this.add(playerNameLabel);
        this.playerNameLabel = playerNameLabel;
    }

    public void setToMove(boolean toMove) {
        this.toMove = toMove;
    }

    public void render() {
        this.playerNameLabel.setText(this.player.getName());
    }

    // EFFECTS: paints the component
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = this.getWidth();
        int height = this.getHeight();
        int cornerRadius = 20;

        if (this.toMove) {
            graphics.setColor(lightGray);
            graphics.fillRoundRect(0, 0, width, height,
                    cornerRadius, cornerRadius);

            int chevronSideLength = 10;
            int adjustOffset = 1;
            int chevronX = gamePanelMaxWidth - 25;
            int[] xs = new int[] { chevronX, chevronX, chevronX + chevronSideLength };
            int[] ys = new int[] {
                    height / 2 - chevronSideLength / 2 - adjustOffset,
                    height / 2 + chevronSideLength / 2 - adjustOffset,
                    height / 2 - adjustOffset
            };
            graphics.setColor(black);
            graphics.fillPolygon(xs, ys, 3);
        }
    }
}
