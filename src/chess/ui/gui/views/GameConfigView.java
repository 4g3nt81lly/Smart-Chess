package chess.ui.gui.views;

import chess.model.ChessGame;
import chess.model.Color;
import chess.model.player.Player;
import chess.ui.PlayerLoader;
import chess.ui.gui.components.HPanel;
import chess.ui.gui.components.ModalWindow;
import chess.ui.gui.components.VPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class GameConfigView extends ModalWindow {

    public static int numInstances = 0;

    private final ChessGame game;

    private boolean userConfirmed = false;

    private JTextField gameNameField;

    private JCheckBox readOnlyCheckbox;

    private JTextField whitePlayerNameField;

    private JComboBox<String> whitePlayerTypeCombo;

    private JTextField blackPlayerNameField;

    private JComboBox<String> blackPlayerTypeCombo;

    // EFFECTS: constructs the game config view with the game and a custom button name
    public GameConfigView(Frame owner, ChessGame game, String buttonName) {
        super(owner);

        this.game = game;

        // main content panel
        VPanel contentPanel = new VPanel();
        contentPanel.setBorder(new EmptyBorder(15, 35, 15, 35));

        // game info section
        HPanel gameInfoSection = new HPanel();
        // game name
        this.initializeGameName(gameInfoSection);
        // read only
        this.initializeReadOnlyOption(gameInfoSection);

        contentPanel.add(gameInfoSection);
        contentPanel.addSpacer(10);
        contentPanel.add(new JSeparator());
        contentPanel.addSpacer(10);

        this.initializePlayersInfo(contentPanel);
        contentPanel.addSpacer(10);

        // CANCEL and CONFIRM
        this.initializeActionStrip(contentPanel, buttonName);

        this.setContentPane(contentPanel);

        this.setupEscapable();

        this.pack();
        this.centerRelativeToOwner(owner);
    }

    public boolean present() {
        numInstances++;
        this.display();
        return this.userConfirmed;
    }

    private void initializeGameName(HPanel contentPanel) {
        JLabel gameNameLabel = new JLabel("Game Name:");
        contentPanel.add(gameNameLabel);
        contentPanel.addSpacer(5);

        JTextField gameNameField = new JTextField(this.game.getName());
        contentPanel.add(gameNameField);
        this.gameNameField = gameNameField;
    }

    private void initializeReadOnlyOption(HPanel contentPanel) {
        JCheckBox readOnlyCheckbox = new JCheckBox("Read Only");
        readOnlyCheckbox.setFocusable(false);
        readOnlyCheckbox.setSelected(this.game.isReadOnly());
        contentPanel.add(readOnlyCheckbox);
        this.readOnlyCheckbox = readOnlyCheckbox;
    }

    private void initializePlayersInfo(VPanel contentPanel) {
        HPanel playersInfoPanel = new HPanel();

        for (Color color : Color.values()) {
            VPanel playerInfoPanel = new VPanel();
            playerInfoPanel.setHorizontalAlignment(LEFT_ALIGNMENT);

            Player player = this.game.getPlayer(color);
            String playerLabelString = color + " Player";
            if (this.game.getPlayerToMove() == player) {
                playerLabelString += " ('s turn)";
            }
            JLabel playerLabel = new JLabel(playerLabelString);
            playerLabel.setFont(playerLabel.getFont()
                    .deriveFont(Font.BOLD));
            playerInfoPanel.add(playerLabel);
            playerInfoPanel.addSpacer(7);

            // VPanel |   VPanel
            //  Name: | [________]
            //  Type: | [________]
            HPanel playerConfigPanel = new HPanel();
            playerConfigPanel.setHorizontalAlignment(LEFT_ALIGNMENT);
            playerConfigPanel.setVerticalAlignment(TOP_ALIGNMENT);

            VPanel labels = new VPanel();
            labels.setHorizontalAlignment(RIGHT_ALIGNMENT);
            labels.addSpacer(5);
            labels.add(new JLabel("Name:"));
            labels.addSpacer(13);
            labels.add(new JLabel("Type:"));

            VPanel entries = new VPanel();

            JTextField playerNameField = new JTextField(player.getName());
            entries.add(playerNameField);
            entries.addSpacer(5);

            JComboBox<String> playerTypeCombo = new JComboBox<>(this.getPlayerTypes());
            playerTypeCombo.setPreferredSize(
                    new Dimension(150, playerTypeCombo.getPreferredSize().height)
            );
            playerTypeCombo.setSelectedIndex(this.getCurrentPlayerType(player));
            playerTypeCombo.setFocusable(false);
            entries.add(playerTypeCombo);

            playerConfigPanel.add(labels);
            playerConfigPanel.addSpacer(5);
            playerConfigPanel.add(entries);
            playerInfoPanel.add(playerConfigPanel);

            playersInfoPanel.add(playerInfoPanel);

            if (player.isWhite()) {
                this.whitePlayerNameField = playerNameField;
                this.whitePlayerTypeCombo = playerTypeCombo;

                playersInfoPanel.addSpacer(10);
            } else {
                this.blackPlayerNameField = playerNameField;
                this.blackPlayerTypeCombo = playerTypeCombo;
            }
        }
        contentPanel.add(playersInfoPanel);
    }

    private void initializeActionStrip(VPanel contentPanel, String buttonName) {
        HPanel actionStrip = new HPanel();
        actionStrip.setHorizontalAlignment(LEFT_ALIGNMENT);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(e -> this.cancel());
        actionStrip.add(cancelButton);

        actionStrip.addSpacer();

        JButton confirmButton = new JButton(buttonName);
        confirmButton.setFocusable(false);
        confirmButton.addActionListener(e -> this.confirm());
        actionStrip.add(confirmButton);

        contentPanel.add(actionStrip);
    }

    private void setupEscapable() {
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    GameConfigView.this.cancel();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && e.isMetaDown()) {
                    GameConfigView.this.confirm();
                }
            }
        };
        this.addKeyListener(keyListener);
        this.gameNameField.addKeyListener(keyListener);
        this.whitePlayerNameField.addKeyListener(keyListener);
        this.blackPlayerNameField.addKeyListener(keyListener);
    }

    private String[] getPlayerTypes() {
        return PlayerLoader.getLoadedPlayerNames().toArray(new String[0]);
    }

    private int getCurrentPlayerType(Player player) {
        int currentPlayerType = 0;
        if (player != null) {
            currentPlayerType = PlayerLoader.getLoadedPlayers().indexOf(player.getClass());
        }
        return currentPlayerType;
    }

    @Override
    public void close() {
        super.close();
        if (numInstances > 0)
            numInstances--;
    }

    private void confirm() {
        this.userConfirmed = true;
        this.configureGame();
        this.close();
    }

    private void cancel() {
        this.close();
    }

    private void configureGame() {
        // set game name
        String gameName = this.gameNameField.getText();
        this.game.setName(gameName);
        this.game.setReadOnly(this.readOnlyCheckbox.isSelected());

        for (Color color : Color.values()) {
            String newPlayerName = (color.isWhite()
                    ? this.whitePlayerNameField.getText()
                    : this.blackPlayerNameField.getText());
            int newPlayerType = color.isWhite()
                    ? this.whitePlayerTypeCombo.getSelectedIndex()
                    : this.blackPlayerTypeCombo.getSelectedIndex();
            Player player = this.game.getPlayer(color);
            if (player != null) {
                // player already exists for current color
                player.setName(newPlayerName);
                int currentPlayerType = this.getCurrentPlayerType(player);
                if (newPlayerType != currentPlayerType) {
                    // user specified an alternate player type
                    int score = player.getScore();
                    Player newPlayer = PlayerLoader.generatePlayer(newPlayerName, color, score, newPlayerType);
                    this.game.setPlayer(newPlayer);
                }
            } else {
                Player newPlayer = PlayerLoader.generatePlayer(newPlayerName, color, 0, newPlayerType);
                this.game.setPlayer(newPlayer);
            }
        }
    }

}