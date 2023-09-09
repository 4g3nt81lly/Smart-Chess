package chess.ui.gui.controllers;

import chess.model.ChessGame;
import chess.model.Position;
import chess.model.moves.Movement;
import chess.model.pieces.Piece;
import chess.model.player.Agent;
import chess.model.player.HumanPlayer;
import chess.model.player.Player;
import chess.model.exceptions.GameException;
import chess.model.exceptions.IllegalMove;
import chess.model.exceptions.IllegalOperation;
import chess.persistence.GameStore;
import chess.ui.gui.ChessGUI;
import chess.ui.gui.views.GameConfigView;
import chess.ui.gui.views.ViewUtils;
import chess.ui.gui.views.game.GameWindow;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.swing.JOptionPane.*;

public final class GameController {

    private ChessGame game;

    private final GameStore persistentStore;

    private GameWindow gameWindow;

    private Piece selectedPiece;

    private Thread agentsTask = null;

    public GameController(ChessGame game, GameStore persistentStore) {
        this.game = game;
        this.gameWindow = new GameWindow(this);
        this.persistentStore = persistentStore;
    }

    public void start() {
        this.gameWindow.setVisible(true);
        this.nextTurn();
    }

    public synchronized void nextTurn() {
        this.gameWindow.render();
        this.gameWindow.updateInterface();

        if (this.game.hasNextTurn()
                && !this.game.isInspectionOnly()) {
            // game in progress, move on to next turn
            Player playerToMove = this.game.getPlayerToMove();
            if (playerToMove instanceof HumanPlayer) {
                // the player to move is a human player
                this.beginHumansTurn();
            } else {
                // the player to move is an agent
                this.beginAgentsTurn();
            }
        }
    }

    private void beginHumansTurn() {
        this.gameWindow.updateUserActions();
    }

    private void beginAgentsTurn() {
        // disable user interaction
        this.gameWindow.disableNavigation();
        // send agent's process to background thread

        this.agentsTask = new Thread(() -> {
            GameController controller = GameController.this;
            try {
                controller.game.nextTurn();
                // the agent made a correct move
                // notify unsaved changes if applicable
                controller.registerChanges();
                // update the game controller
                controller.nextTurn();
            } catch (IllegalOperation exception) {
                // game is paused, do nothing
            } catch (IllegalMove exception) {
                // something wrong with the agent
                System.exit(1);
            }
        });
        this.agentsTask.start();
    }

    public void submitMove(Position finalPosition) throws GameException {
        Player playerToMove = this.game.getPlayerToMove();
        // get selected position as the initial position
        Piece selectedPiece = this.selectedPiece;
        if (!(playerToMove instanceof HumanPlayer)
                || selectedPiece == null) {
            throw new GameException();
        }
        ((HumanPlayer) playerToMove)
                .registerNextMove(selectedPiece.getPosition(), finalPosition);
        this.game.nextTurn();
        // the move is successful
        // notify unsaved changes
        this.registerChanges();
        // deselect selected position
        this.selectedPiece = null;
        // proceed to next turn
        this.nextTurn();
    }

    public void backward() {
        this.game.undoOrBackward();
        this.registerChanges();
        this.nextTurn();
    }

    public void forward() {
        this.game.redoOrForward();
        this.registerChanges();
        this.nextTurn();
    }

    public void navigateTo(Movement movement) {
        // if the opponent of the player who made the selected movement is an Agent,
        // then pause the game before navigating backward/forward
        Player opponent = this.game.getPlayer(movement.getOppositeColor());
        if (!this.game.isInspectionOnly()
                && opponent instanceof Agent) {
            this.pause();
        }
        Runnable navigationAction = this.game.getPreviousMoves().contains(movement)
                ? this.game::undoOrBackward : this.game::redoOrForward;
        // get the game's most recent move
        Optional<Movement> mostRecentMovement = this.game.getMostRecentMove();
        while (mostRecentMovement.isEmpty()
                || !mostRecentMovement.get().equals(movement)) {
            navigationAction.run();
            mostRecentMovement = this.game.getMostRecentMove();
        }
        this.registerChanges();
        this.gameWindow.render();
        this.gameWindow.updateInterface();
    }

    public void pause() {
        this.game.pause();
        if (this.agentsTask != null)
            this.agentsTask.interrupt();
        this.gameWindow.updateInterface();
    }

    public void resume() {
        this.game.resume();
        this.nextTurn();
    }

    public boolean canReset() {
        return this.game.canReset();
    }

    public void reset() {
        int userResponse = ViewUtils.confirm(this.gameWindow, "Confirm",
                "The game will be reset and this cannot be undone, proceed?",
                YES_NO_OPTION);
        if (userResponse == YES_OPTION) {
            // user confirmed resetting the game
            this.game.reset();
            this.registerChanges();
            this.nextTurn();
        }
    }

    public boolean canRevert() {
        return this.persistentStore.hasCheckpoint()
                && this.persistentStore.hasUnsavedChanges();
    }

    public void revert() {
        int userResponse = ViewUtils.confirm(this.gameWindow, "Revert Game",
                "The game will be reverted to last saved.\n"
                        + "All unsaved changes will be lost and this cannot be undone.", YES_NO_OPTION);
        if (userResponse == YES_OPTION) {
            this.game = this.persistentStore.retrieveCheckpoint();
            this.nextTurn();
        }
    }

    private void registerChanges() {
        if (!this.game.isReadOnly()) {
            this.persistentStore.markUnsavedChanges();
        }
    }

    public void showSettings() {
        GameConfigView gameConfigView = new GameConfigView(this.gameWindow, this.game, "Update");
        boolean shouldPause = !this.game.isPaused() && !this.game.isReadOnly();
        if (shouldPause) this.pause();
        if (gameConfigView.present()) {
            // a change was made to game config
            this.gameWindow.updateInterface();
            this.registerChanges();
        }
        if (shouldPause) this.resume();
    }

    public boolean canSave() {
        return this.persistentStore.isNewFile()
                || this.persistentStore.hasUnsavedChanges();
    }

    public boolean save() {
        if (this.persistentStore.isNewFile()) {
            // is a new file
            return this.saveAs();
        }
        // loaded game
        return this.saveAs(null);
    }

    private boolean saveAs() {
        // use AtomicBoolean to avoid error reassigning variable in closure
        AtomicBoolean success = new AtomicBoolean(false);
        String defaultFileName = this.game.getName() + GameStore.fileExtension;
        ViewUtils.saveAsFile(this.gameWindow, "Saving Game", defaultFileName,
                (file, name) -> name.endsWith(GameStore.fileExtension)
        ).ifPresent(file -> {
            try {
                success.set(this.saveAs(file.getCanonicalPath()));
            } catch (IOException error) {
                ViewUtils.alert(this.gameWindow, "Error: Save",
                        "Failed to save: " + error.getMessage(), ERROR_MESSAGE);
            }
        });
        return success.get();
    }

    private boolean saveAs(String savePath) {
        if (savePath != null) {
            // user specified save path, set new path
            this.persistentStore.setNewFilePath(savePath);
        }
        try {
            this.persistentStore.saveCheckpoint(this.game);
        } catch (IOException error) {
            // failed to save
            ViewUtils.alert(this.gameWindow, "Error: Save",
                    "Failed to save: " + error.getMessage(), ERROR_MESSAGE);
            return false;
        }
        this.gameWindow.updateInterface();
        return true;
    }

    public boolean close() {
        if (!this.game.isInspectionOnly()) {
            this.pause();
        }
        if (this.persistentStore.hasUnsavedChanges()) {
            // the game has unsaved changes
            int userResponse = ViewUtils.confirm(this.gameWindow, "Warning",
                    "You have unsaved changes, would you like to save it?",
                    YES_NO_CANCEL_OPTION);
            if (userResponse == YES_OPTION
                    && !this.save()) {
                // failed or cancelled saving
                userResponse = ViewUtils.confirm(this.gameWindow, "Warning",
                        "You have not saved unsaved changes, exit anyway?\n"
                                + "You will lose your game.", YES_NO_OPTION);
                if (userResponse == NO_OPTION)
                    return false;
            } else if (userResponse == CANCEL_OPTION
                    || userResponse == CLOSED_OPTION) {
                return false;
            }
        }
        this.gameWindow.close();
        this.gameWindow = null;
        ChessGUI.getInstance().releaseController(this);
        return true;
    }

    public void setSelectedPiece(@Nullable Piece piece) {
        if (piece == null) {
            this.selectedPiece = null;
        } else if (piece.isAlliedTo(this.game.getPlayerToMove())) {
            // the piece belongs to the current player
            this.selectedPiece = piece;
        }
    }

    public ChessGame getGame() {
        return this.game;
    }

    public @Nullable Piece getSelectedPiece() {
        return this.selectedPiece;
    }

}
