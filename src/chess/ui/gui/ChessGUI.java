package chess.ui.gui;

import chess.model.ChessGame;
import chess.persistence.GameStore;
import chess.ui.Interface;
import chess.ui.gui.controllers.GameController;
import chess.ui.gui.views.*;
import chess.util.List;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ChessGUI implements Interface {

    public static void main(String[] args) {
        ChessGUI.getInstance().run();
    }

    private static ChessGUI gui;

    public static final ImageIcon appIcon;

    static {
        // https://stackoverflow.com/a/8956715/
        // https://stackoverflow.com/q/8918826/
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", appName);

        appIcon = new ImageIcon(getResource("/images/AppIcon.png"));
        if (Taskbar.isTaskbarSupported())
            Taskbar.getTaskbar().setIconImage(appIcon.getImage());
    }

    public static URL getResource(String path) {
        @Nullable URL url = Interface.class.getResource(path);
        if (url == null) {
            throw new RuntimeException();
        }
        return url;
    }

    public static ChessGUI getInstance() {
        if (gui == null) {
            gui = new ChessGUI();
        }
        return gui;
    }

    private final MainMenuWindow mainMenuWindow;

    private final AboutWindow aboutWindow;

    private final PreferencesWindow preferencesWindow;

    private final Set<GameController> gameControllers;

    private int untitledNameCounter = 0;

    private ChessGUI() {
        this.mainMenuWindow = new MainMenuWindow();
        this.aboutWindow = new AboutWindow();
        this.preferencesWindow = new PreferencesWindow();
        this.gameControllers = new HashSet<>();
        this.configureAboutHandler();
        this.configurePreferencesHandler();
        this.configureQuitHandler();
    }

    @Override
    public void run() {
        if (this.gameControllers.isEmpty()) {
            this.mainMenuWindow.setVisible(true);
        }
    }

    public void newGame() {
        // create new store for new game
        GameStore store = new GameStore();

        String defaultName = "Untitled Game";
        if (this.untitledNameCounter > 0) {
            defaultName += " " + this.untitledNameCounter;
        }
        ChessGame game = new ChessGame(defaultName);
        this.configureGame(game, store).ifPresent(controller -> {
            // user confirmed creating new game
            this.mainMenuWindow.setVisible(false);
            this.gameControllers.add(controller);
            controller.start();
            this.untitledNameCounter++;
        });
    }

    public void loadGame() {
        ViewUtils.selectFile(this.mainMenuWindow,
                "Choose a game file to load.",
                (file, name) -> name.endsWith(GameStore.fileExtension)
        ).ifPresent(file -> {
            try {
                String path = file.getCanonicalPath();
                GameStore store = new GameStore(path);
                ChessGame game = store.read();
                // successfully loaded a game from file
                this.configureGame(game, store).ifPresent(controller -> {
                    // user confirmed loading game
                    this.mainMenuWindow.setVisible(false);
                    this.gameControllers.add(controller);
                    controller.start();
                });
            } catch (NoSuchFileException e) {
                // file does not exist (this should NOT occur)
                ViewUtils.alert(this.mainMenuWindow, "Error",
                        "File '" + file + "' could not be reached.", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                // file reading error
                ViewUtils.alert(this.mainMenuWindow, "Error",
                        "An error occurred while reading '" + file + "':\n"
                                + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                // other exceptions: file format error
                ViewUtils.alert(this.mainMenuWindow, "Error",
                        "Unable to load game: Incorrect file format.\n"
                                + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private Optional<GameController> configureGame(ChessGame game, GameStore store) {
        String confirmButton = store.isNewFile() ? "Create" : "Load";
        GameConfigView configView = new GameConfigView(this.mainMenuWindow, game, confirmButton);
        return configView.present()
                ? Optional.of(new GameController(game, store))
                : Optional.empty();
    }

    public void releaseController(GameController gameController) {
        this.gameControllers.remove(gameController);
        this.run();
    }

    private void configureAboutHandler() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().setAboutHandler(event -> {
                this.aboutWindow.display();
            });
        }
    }

    private void configurePreferencesHandler() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().setPreferencesHandler(event -> {
                if (GameConfigView.numInstances <= 0) {
                    this.preferencesWindow.display();
                }
            });
        }
    }

    private void configureQuitHandler() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().setQuitHandler((event, response) -> {
                for (GameController controller : new List<>(this.gameControllers)) {
                    if (!controller.close()) {
                        response.cancelQuit();
                        return;
                    }
                }
                response.performQuit();
            });
        }
    }

    public static JMenuBar createMenuBar() {
        int commandKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        // configure app menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        // New Game menu bar menu item
        JMenuItem newGameItem = new JMenuItem("New Game…");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke('N', commandKeyMask));
        newGameItem.addActionListener(e -> ChessGUI.getInstance().newGame());
        fileMenu.add(newGameItem);

        // Load Game menu bar menu item
        JMenuItem loadGameItem = new JMenuItem("Load Game…");
        loadGameItem.setAccelerator(KeyStroke.getKeyStroke('O', commandKeyMask));
        loadGameItem.addActionListener(e -> ChessGUI.getInstance().loadGame());
        fileMenu.add(loadGameItem);

        menuBar.add(fileMenu);
        return menuBar;
    }

}
