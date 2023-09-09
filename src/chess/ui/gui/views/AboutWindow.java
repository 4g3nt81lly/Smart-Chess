package chess.ui.gui.views;

import chess.ui.gui.ChessGUI;
import chess.ui.gui.components.HPanel;
import chess.ui.gui.components.ImageWell;
import chess.ui.gui.components.ModalWindow;
import chess.ui.gui.components.VPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Year;

public class AboutWindow extends ModalWindow {

    public AboutWindow() {
        super(null);
        HPanel mainPanel = new HPanel();
        mainPanel.setBorder(new EmptyBorder(30, 30, 40, 30));

        ImageWell imageWell = new ImageWell(
                ViewUtils.scaleImageIcon(ChessGUI.appIcon, 64, 64)
        );
        imageWell.setPreferredSize(new Dimension(64, 64));

        mainPanel.add(imageWell);
        mainPanel.addSpacer(15);

        VPanel descriptionPanel = new VPanel();
        descriptionPanel.setHorizontalAlignment(LEFT_ALIGNMENT);

        JLabel appNameLabel = new JLabel(ChessGUI.appName);
        appNameLabel.setFont(appNameLabel.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(16f));
        descriptionPanel.add(appNameLabel);
        descriptionPanel.addSpacer(7);

        JLabel copyrightLabel = new JLabel("Billy Li © " + Year.now());
        copyrightLabel.setFont(copyrightLabel.getFont()
                .deriveFont(12f));
        descriptionPanel.add(copyrightLabel);
        descriptionPanel.addSpacer(7);

        JTextArea appDescriptionLabel = new JTextArea(
                "This is a smart single/double-user Chess game for Chess lovers or learners. "
                        + "Its features include saving and loading games and providing an "
                        + "easily-extendable interface that supports custom implementations of "
                        + "smart chess-playing agents to play against. The last feature may be of "
                        + "great interest to A.I. learners or practitioners.\n"
                        + "This was originally a course project (also the author's first Java project) "
                        + "out of pure passion for exploration."
        );
        appDescriptionLabel.setColumns(30);
        appDescriptionLabel.setRows(10);
        appDescriptionLabel.setLineWrap(true);
        appDescriptionLabel.setWrapStyleWord(true);
        appDescriptionLabel.setEditable(false);
        appDescriptionLabel.setFocusable(false);
        appDescriptionLabel.setOpaque(false);
        appDescriptionLabel.setFont(appDescriptionLabel.getFont()
                .deriveFont(13f));
        descriptionPanel.add(appDescriptionLabel);

        mainPanel.add(descriptionPanel);
        this.add(mainPanel);

        this.pack();
        this.setLocationRelativeTo(null);
    }

}
