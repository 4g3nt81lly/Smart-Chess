package chess.ui.gui.views.game;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Color;
import chess.model.moves.Movement;
import chess.model.player.Player;
import chess.ui.gui.components.VList;
import chess.ui.gui.controllers.GameController;
import chess.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static chess.ui.Presets.*;

public final class GamePane extends JPanel {

    private final GameWindow gameWindow;

    private JLabel gameNameField;

    private JTextArea gameStateText;

    private JLabel roundInfoLabel;

    private PlayerBox whitePlayerBox;

    private PlayerBox blackPlayerBox;

    private VList<Movement> historyList;

    private ChevronButton backwardButton;

    private ChevronButton forwardButton;

    GamePane(GameWindow gameWindow) {
        super();
        this.gameWindow = gameWindow;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(new EmptyBorder(25, 25, 25, 10));

        this.addGameNameField();
        this.addSeparator(10);

        this.addGameStateLabel();
        this.addSeparator(10);

        this.addRoundInfo();
        this.addSeparator(12);

        this.addPlayerBoxes();
        this.addSeparator(12);

        this.addHistoryList();
        this.addSeparator(20);
        this.addChevronButtons();
    }

    void render() {
        ChessGame game = this.getController().getGame();

        // update basic infos
        this.gameNameField.setText(game.getName());
        this.gameStateText.setText(game.getStateDescription());
        this.roundInfoLabel.setText(this.getRoundInfo());
        // update player to move (boxes)
        Player playerToMove = game.getPlayerToMove();
        PlayerBox currentPlayersBox = this.getPlayerBox(playerToMove.getColor());
        currentPlayersBox.render();
        currentPlayersBox.setToMove(true);
        PlayerBox opponentsBox = this.getPlayerBox(playerToMove.getOppositeColor());
        opponentsBox.render();
        opponentsBox.setToMove(false);
        // update history list model
        List<Movement> newHistory = game.getPreviousMoves();
        newHistory.addAll(game.getFutureMoves().reversed());
        this.historyList.updateContents(newHistory);

        this.selectMostRecentMove();

        // refresh all views
        this.revalidate();
        this.repaint();
    }

    void navigateToSelectedMovement() {
        // get current selection
        Movement selectedMovement = this.historyList.getSelectedItem();
        if (selectedMovement != null) {
            this.getController().navigateTo(selectedMovement);
        }
    }

    void disableNavigation() {
        this.backwardButton.setEnabled(false);
        this.forwardButton.setEnabled(false);
    }

    void setNavigationEnabled(boolean backward, boolean forward) {
        this.backwardButton.setEnabled(backward);
        this.forwardButton.setEnabled(forward);
    }

    private void addSeparator(int height) {
        this.add(Box.createVerticalStrut(height));
    }

    private void addGameNameField() {
        JLabel gameNameField = new JLabel();
        // set size to enable text truncation is applicable
        gameNameField.setPreferredSize(new Dimension(gamePanelMaxWidth, 30));
        gameNameField.setMaximumSize(new Dimension(gamePanelMaxWidth, 30));
        gameNameField.setFocusable(false);

        gameNameField.setFont(gameNameField.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(headingFontSize));
        this.add(gameNameField);
        this.gameNameField = gameNameField;
    }

    private void addGameStateLabel() {
        JTextArea gameStateText = new JTextArea();
        gameStateText.setLineWrap(true);
        gameStateText.setWrapStyleWord(true);
        gameStateText.setEditable(false);
        gameStateText.setFocusable(false);
        gameStateText.setOpaque(false);
        gameStateText.setAlignmentX(LEFT_ALIGNMENT);
        // set size to enable text truncation is applicable
        gameStateText.setPreferredSize(new Dimension(gamePanelMaxWidth, 40));
        gameStateText.setMaximumSize(new Dimension(gamePanelMaxWidth, 40));

        gameStateText.setFont(gameStateText.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(subheadingFontSize));
        this.add(gameStateText);
        this.gameStateText = gameStateText;
    }

    private void addRoundInfo() {
        JLabel roundInfoLabel = new JLabel();
        roundInfoLabel.setPreferredSize(new Dimension(gamePanelMaxWidth, 40));
        roundInfoLabel.setMaximumSize(new Dimension(gamePanelMaxWidth, 40));
        this.add(roundInfoLabel);
        this.roundInfoLabel = roundInfoLabel;
    }

    private void addPlayerBoxes() {
        ChessGame game = this.getController().getGame();
        PlayerBox whitePlayerBox = new PlayerBox(game.getPlayer(Color.White));
        PlayerBox blackPlayerBox = new PlayerBox(game.getPlayer(Color.Black));
        if (game.getPlayerToMove().isWhite()) {
            whitePlayerBox.setToMove(true);
        } else {
            blackPlayerBox.setToMove(true);
        }
        this.add(whitePlayerBox);
        this.add(blackPlayerBox);
        this.whitePlayerBox = whitePlayerBox;
        this.blackPlayerBox = blackPlayerBox;
    }

    private PlayerBox getPlayerBox(Color color) {
        return color.isWhite() ? this.whitePlayerBox : this.blackPlayerBox;
    }

    private void addHistoryList() {
        Chessboard chessboard = this.getController().getGame().getChessboard();
        VList<Movement> historyList = new VList<>();
        historyList.setAlignmentX(LEFT_ALIGNMENT);
        historyList.setPreferredSize(
                new Dimension(gamePanelMaxWidth, gameHistoryListHeight)
        );
        historyList.setFont(historyList.getFont()
                .deriveFont(16f));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListViewListener(vList -> this.navigateToSelectedMovement());
        historyList.setItemRenderStrategy((value, index, isSelected, cellHasFocus) -> {
            return (index + 1) + ". " + ((Movement) value).toString(chessboard);
        });
        this.add(historyList);
        this.historyList = historyList;
    }

    private void addChevronButtons() {
        JPanel actionPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(1, 2);
        gridLayout.setHgap(30);
        actionPanel.setLayout(gridLayout);

        ChevronButton undoButton = new ChevronButton(SwingConstants.LEFT);
        undoButton.setToolTipText("Undo move.");
        undoButton.addActionListener(e -> {
            undoButton.setEnabled(false);
            GamePane.this.getController().backward();
        });
        actionPanel.add(undoButton);
        this.backwardButton = undoButton;

        ChevronButton redoButton = new ChevronButton(SwingConstants.RIGHT);
        redoButton.setToolTipText("Redo move.");
        redoButton.addActionListener(e -> {
            redoButton.setEnabled(false);
            GamePane.this.getController().forward();
        });
        actionPanel.add(redoButton);
        this.forwardButton = redoButton;

        actionPanel.setAlignmentX(LEFT_ALIGNMENT);
        actionPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        actionPanel.setPreferredSize(new Dimension(gamePanelMaxWidth, 40));
        actionPanel.setMaximumSize(new Dimension(gamePanelMaxWidth, 40));
        this.add(actionPanel);
    }

    private void selectMostRecentMove() {
        this.getController().getGame().getMostRecentMove()
                .ifPresent(mostRecentMove -> this.historyList.select(mostRecentMove));
    }

    public GameController getController() {
        return this.gameWindow.getController();
    }

    private String getRoundInfo() {
        ChessGame game = this.getController().getGame();
        String roundInfo = "Game ended at round " + game.getRoundNumber() + ".";
        if (!game.hasConcluded()) {
            roundInfo = "Round " + game.getRoundNumber() + " - "
                    + game.getPlayerToMove().getColor() + " to move";
        }
        return roundInfo;
    }

}
