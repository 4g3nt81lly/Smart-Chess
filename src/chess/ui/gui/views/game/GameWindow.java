package chess.ui.gui.views.game;

import chess.model.ChessGame;
import chess.model.player.Agent;
import chess.ui.gui.ChessGUI;
import chess.ui.gui.controllers.GameController;
import chess.ui.gui.views.game.board.ChessboardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private GameController controller;

    private final GamePane gamePane;

    private final ChessboardView chessboardView;

    private JMenuItem saveMenuItem;

    private JMenuItem revertMenuItem;

    private JMenuItem backwardMenuItem;

    private JMenuItem forwardMenuItem;

    private JMenuItem pauseMenuItem;

    private JMenuItem resumeMenuItem;

    private JMenuItem resetMenuItem;

    // EFFECTS: constructs a game window with the given game controller
    public GameWindow(GameController controller) {
        super();
        this.controller = controller;
        this.setResizable(false);
        this.configureWindowBehavior();

        // set up status bar app menus
        JMenuBar menuBar = ChessGUI.createMenuBar();
        this.setupFileAppMenu(menuBar);
        this.setupGameAppMenu(menuBar);
        this.setJMenuBar(menuBar);

        // set up window layout
        this.setLayout(new BorderLayout());

        this.gamePane = new GamePane(this);
        this.add(this.gamePane, BorderLayout.WEST);
        this.chessboardView = new ChessboardView(this);
        this.add(this.chessboardView, BorderLayout.CENTER);

        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void render() {
        this.gamePane.render();
        this.chessboardView.render();
    }

    public void updateInterface() {
        this.updateWindowTitle();
        this.updateNavigationMenuItem();
        this.updateUserActions();
    }

    private void updateWindowTitle() {
        ChessGame game = this.controller.getGame();
        String windowTitle = game.getName();
        if (game.isPaused()) {
            windowTitle = "[Paused] " + windowTitle;
        }
        if (game.isReadOnly()) {
            windowTitle = "[Read Only] " + windowTitle;
        }
        if (game.hasConcluded()) {
            windowTitle += " (Game Over)";
        }
        this.setTitle(windowTitle);
    }

    private void updateNavigationMenuItem() {
        ChessGame game = this.getController().getGame();
        boolean isInspectionOnly = game.isInspectionOnly();

        this.backwardMenuItem.setText(isInspectionOnly ? "Backward" : "Undo");
        this.forwardMenuItem.setText(isInspectionOnly ? "Forward" : "Redo");

        this.pauseMenuItem.setEnabled(
                !isInspectionOnly && !game.hasConcluded()
        );
        this.resumeMenuItem.setEnabled(game.isPaused());
    }

    public void updateUserActions() {
        ChessGame game = this.controller.getGame();
        this.saveMenuItem.setEnabled(this.controller.canSave());
        this.revertMenuItem.setEnabled(this.controller.canRevert());
        this.backwardMenuItem.setEnabled(
                game.isInspectionOnly() ? game.hasBackward() : game.canUndo()
        );
        this.forwardMenuItem.setEnabled(
                game.isInspectionOnly() ? game.hasForward() : game.canRedo()
        );
        this.resetMenuItem.setEnabled(
                (game.isPaused() || !(game.getPlayerToMove() instanceof Agent))
                        && this.controller.canReset()
        );

        // revalidate undo/redo buttons
        this.gamePane.setNavigationEnabled(
                this.backwardMenuItem.isEnabled(),
                this.forwardMenuItem.isEnabled()
        );
    }

    public void disableNavigation() {
        // disable undo/redo/reset menu items
        this.backwardMenuItem.setEnabled(false);
        this.forwardMenuItem.setEnabled(false);
        this.resetMenuItem.setEnabled(false);

        // disable undo/redo buttons
        this.gamePane.disableNavigation();
    }

    private void configureWindowBehavior() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (GameWindow.this.controller.close()) {
                    GameWindow.this.controller = null;
                }
            }
        });
    }

    private void setupFileAppMenu(JMenuBar menuBar) {
        int commandKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // configure app menu bar
        JMenu fileMenu = menuBar.getMenu(0);

        // separator menu item 1
        fileMenu.add(new JSeparator());

        // Save game menu bar menu item
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', commandKeyMask));
        saveItem.addActionListener(e -> this.controller.save());
        fileMenu.add(saveItem);
        this.saveMenuItem = saveItem;

        // Save game menu bar menu item
        JMenuItem revertItem = new JMenuItem("Revert");
        revertItem.addActionListener(e -> this.controller.revert());
        fileMenu.add(revertItem);
        this.revertMenuItem = revertItem;

        // separator menu item 2
        fileMenu.add(new JSeparator());

        // Close menu item
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setAccelerator(KeyStroke.getKeyStroke('W', commandKeyMask));
        closeItem.addActionListener(e -> {
            WindowEvent closeEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            this.dispatchEvent(closeEvent);
        });
        fileMenu.add(closeItem);

        menuBar.add(fileMenu);
    }

    private void setupGameAppMenu(JMenuBar menuBar) {
        int commandKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenu gameMenu = new JMenu("Game");
        // Undo menu item
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', commandKeyMask));
        undoItem.addActionListener(e -> this.controller.backward());
        gameMenu.add(undoItem);
        this.backwardMenuItem = undoItem;

        // Redo menu item
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke('Z', commandKeyMask | InputEvent.SHIFT_DOWN_MASK));
        redoItem.addActionListener(e -> this.controller.forward());
        gameMenu.add(redoItem);
        this.forwardMenuItem = redoItem;

        // separator menu item 1
        gameMenu.add(new JSeparator());

        // Pause menu item
        JMenuItem pauseItem = new JMenuItem("Pause");
        pauseItem.addActionListener(e -> this.controller.pause());
        gameMenu.add(pauseItem);
        this.pauseMenuItem = pauseItem;

        // Resume menu item
        JMenuItem resumeItem = new JMenuItem("Resume");
        resumeItem.addActionListener(e -> this.controller.resume());
        gameMenu.add(resumeItem);
        this.resumeMenuItem = resumeItem;

        // separator menu item 2
        gameMenu.add(new JSeparator());

        // Reset menu item
        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.setAccelerator(KeyStroke.getKeyStroke('R', commandKeyMask | InputEvent.SHIFT_DOWN_MASK));
        resetItem.addActionListener(e -> this.controller.reset());
        gameMenu.add(resetItem);
        this.resetMenuItem = resetItem;

        // separator menu item 3
        gameMenu.add(new JSeparator());

        // Settings menu item
        JMenuItem settingsItem = new JMenuItem("Settings…");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(';', commandKeyMask));
        settingsItem.addActionListener(e -> this.controller.showSettings());
        gameMenu.add(settingsItem);

        menuBar.add(gameMenu);
    }

    public void close() {
        this.setVisible(false);
        this.dispose();
    }

    public GameController getController() {
        return this.controller;
    }
}
