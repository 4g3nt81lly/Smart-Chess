package chess.ui.gui.views;

import chess.ui.Interface;
import chess.ui.gui.ChessGUI;
import chess.ui.gui.components.HPanel;
import chess.ui.gui.components.VPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

import static chess.model.TextSymbols.blackPawn;

public final class MainMenuWindow extends JFrame {

    // EFFECTS: constructs a main window with the given GUI interface
    public MainMenuWindow() {
        super(Interface.appName);

        this.setResizable(false);

        JMenuBar menuBar = ChessGUI.createMenuBar();
        this.setJMenuBar(menuBar);

        VPanel mainPanel = new VPanel();
        mainPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        HPanel titlePanel = new HPanel();

        JLabel iconLabel = new JLabel(String.valueOf(blackPawn));
        iconLabel.setFont(iconLabel.getFont()
                .deriveFont(24f));
        titlePanel.add(iconLabel);
        titlePanel.addSpacer(15);

        JLabel titleLabel = new JLabel(Interface.appName);
        titleLabel.setFont(titleLabel.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(18f)
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.05)));
        titleLabel.setBorder(new EmptyBorder(10, 0, 7, 0));
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel);
        mainPanel.addSpacer(10);

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFocusable(false);
        newGameButton.setFont(newGameButton.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.05)));
        newGameButton.addActionListener(e -> ChessGUI.getInstance().newGame());
        mainPanel.add(newGameButton);
        mainPanel.addSpacer(5);

        JButton loadGameButton = new JButton("Load Game");
        loadGameButton.setFocusable(false);
        loadGameButton.setFont(loadGameButton.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.05)));
        loadGameButton.addActionListener(e -> ChessGUI.getInstance().loadGame());
        mainPanel.add(loadGameButton);

        this.add(mainPanel);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pack();
        this.setLocationRelativeTo(null);
    }

}
