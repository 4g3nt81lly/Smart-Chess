package chess.ui.cli;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Castling;
import chess.model.moves.Movement;
import chess.model.pieces.Bishop;
import chess.model.pieces.King;
import chess.model.pieces.Piece;
import chess.model.player.HumanPlayer;
import chess.model.player.Player;
import chess.ui.PlayerLoader;
import chess.ui.throwables.ExitGame;
import chess.ui.throwables.GameAction;
import chess.model.exceptions.GameException;
import chess.ui.throwables.RefreshGame;
import chess.persistence.GameStore;
import chess.ui.Interface;
import chess.ui.cli.commands.*;
import chess.util.List;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static chess.model.Color.Black;
import static chess.model.Color.White;
import static chess.ui.Presets.*;

public final class ChessCLI implements Interface {

    public static void main(String[] args) {
        ChessCLI cli = ChessCLI.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(cli.frame::clearScreen));
        cli.run();
    }

    private static ChessCLI cli;

    public static ChessCLI getInstance() {
        if (cli == null) {
            cli = new ChessCLI();
        }
        return cli;
    }

//    private static final List<String> gameModes = List.of(
//            "1. New Game",
//            "2. Load Game",
//            "3. Exit"
//    );

    private final Map<String, Runnable> gameModes = new LinkedHashMap<>(); {
        gameModes.put("1. New Game", this::startNewGame);
        gameModes.put("2. Load Game", this::loadGame);
        gameModes.put("3. Exit", () -> System.exit(0));
    }

    private final String menu =
            "Welcome to Smart Chess!\n"
                    + String.join("\n", gameModes.keySet()) + "\n";

    private final Scanner scanner;

    private ChessGame game;

    private final FrameController frame;

    private GameStore persistentStore;

    private final ChessCommand chessCommand;

    private final BoardCommand boardCommand;

    private final ViewerCommand viewerCommand;

    // EFFECTS: constructs a chess CLI with a new classic chess game and
    //          a scanner for reading standard input
    private ChessCLI() {
        this.scanner = new Scanner(System.in);
        this.frame = new FrameController();
        this.chessCommand = new ChessCommand();
        this.boardCommand = new BoardCommand();
        this.viewerCommand = new ViewerCommand();
    }


    // ===============  MAIN  ===============


    @Override
    public void run() {
        while (true) {
            this.frame.reset()
                    .setTitle(menu)
                    .render();
            this.mainMenu();
        }
    }

    private void mainMenu() {
        int mode = this.getIntegerSelection("Please select: ", gameModes.size());
        new List<>(this.gameModes.values()).get(mode).run();
    }


    // ===============  NEW GAME (PLAY)  ===============


    private void startNewGame() {
        // create new store for new game
        this.persistentStore = new GameStore();

        this.frame.reset()
                .setHeader(this.getSeparator()
                        + "\n\033[1mCreate New Game\033[22m").render();
        String gameName = this.getUserInput("Name: ");

        // set up game
        this.game = new ChessGame(gameName);
        this.configurePlayers();
        this.persistentStore.markUnsavedChanges();

        // begin game cycle
        this.startGameCycle();
    }

    private void startGameCycle() {
        // reset frame to create a new view
        this.frame.reset()
                .setTitle("\033[1m" + this.game.getName()
                        + " (" + this.persistentStore.getFileName() + ")\n"
                        + this.game.getPlayer(White).getName() + " vs. "
                        + this.game.getPlayer(Black).getName() + "\n"
                        + this.getSeparator() + "\033[22m")
                .setChessboardDisplay(true, null);

        // MAIN GAME CYCLE
        this.updateScreenWithGameState();

        while (this.game.hasNextTurn()) {
            this.frame.render();
            Player playerToMove = this.game.getPlayerToMove();
            try {
                if (playerToMove instanceof HumanPlayer) {
                    this.runChessCommand();
                }
                this.game.nextTurn();
            } catch (ExitGame exitGame) {
                // user initiated exit
                break;
            } catch (RefreshGame refreshGame) {
                // game requires refresh before next turn
                this.updateScreenWithGameState();
                continue;
            } catch (GameAction otherAction) {
                // unknown game actions (should NEVER get here anyway)
                throw new RuntimeException("Unknown game action thrown.");
            } catch (GameException gameException) {
                this.frame.renderTemporaryMessage(gameException.getMessage(), 1.5);
                continue;
            }
            // a move was made successfully
            this.frame.setChessboardHint(null);
            // notify the persistent store that unsaved changes were made
            this.persistentStore.markUnsavedChanges();

            this.updateScreenWithGameState();
        }

        if (this.game.hasConcluded()) {
            this.startGameViewer();
        }
        // GAME CYCLE ENDS, SAVE PROMPT
        this.saveGamePrompt(true, true);
    }

    private void runChessCommand() throws GameAction {
        while (true) {
            try {
                // read for command
                this.chessCommand.initialize();
                // execute the command and return command
                if (this.executeChessCommand()) {
                    break;
                }
                // end of a command cycle, re-render the frame
                this.frame.render();
            } catch (Exception exception) {
                this.frame.renderTemporaryMessage(exception.getMessage(), 1);
            }
        }
    }

    private boolean executeChessCommand() throws GameAction {
        ChessCommand command = this.chessCommand;
        // have player object handle move, hint, and exit
        switch (command.getKeyword()) {
            case "/move" -> {
                HumanPlayer playerToMove = (HumanPlayer) this.game.getPlayerToMove();
                Position initialPosition = command.getPosition();
                Position finalPosition = command.getTargetPosition();
                // register next move to ConsolePlayer
                playerToMove.registerNextMove(initialPosition, finalPosition);
            }
            case "/undo" -> {
                if (this.undo()) {
                    throw new RefreshGame();
                }
                return false;
            }
            case "/redo" -> {
                if (this.redo()) {
                    throw new RefreshGame();
                }
                return false;
            }
            case "/hint" -> {
                this.showHint(command.getPosition());
                return false;
            }
            case "/history" -> {
                this.printMoveHistory();
                return false;
            }
            case "/reset" -> {
                this.resetGame();
                return false;
            }
            case "/draw" -> {
                if (this.drawGame()) {
                    throw new RefreshGame();
                }
                return false;
            }
            case "/exit" -> throw new ExitGame();
            case "/save" -> {
                this.saveGame();
                return false;
            }
            case "/revert" -> {
                if (this.revertGame()) {
                    throw new RefreshGame();
                }
                return false;
            }
            case "/help" -> {
                this.frame.setMessage(this.getCommandHelp(command));
                return false;
            }
        }
        return true;
    }

    private boolean undo() {
        if (this.game.canUndo()) {
            this.game.undo();
            // successfully undid a move
            // notify the persistent store that unsaved changes were made
            this.persistentStore.markUnsavedChanges();
            // update round info AND reset message AND hint position (if applicable)
            this.frame.clearMessage().setChessboardHint(null);
            return true;
        }
        // no move to undo, present message
        this.frame.renderTemporaryMessage("No move to undo", 1);
        return false;
    }

    private boolean redo() {
        if (this.game.canRedo()) {
            this.game.redo();
            // successfully redid a move
            // notify the persistent store that unsaved changes were made
            this.persistentStore.markUnsavedChanges();
            // update round info AND reset message AND hint position (if applicable)
            this.frame.clearMessage().setChessboardHint(null);
            return true;
        }
        // no move to redo, set message
        this.frame.renderTemporaryMessage("No move to redo.", 1);
        return false;
    }

    private void resetGame() {
        this.frame.setMessage("Reset: The game will be reset.\n"
                + "Unsaved moves will be lost.").render();
        this.frame.clearMessage();
        if (this.confirm("Reset? ")) {
            // user confirmed resetting game
            if (this.game.canReset()) {
                this.game.reset();
                // successfully reset
                // notify the persistent store that unsaved changes were made
                this.persistentStore.markUnsavedChanges();
                this.updateScreenWithGameState()
                        .setChessboardHint(null)
                        .renderTemporaryMessage("Game reset.", 1);
            } else {
                this.frame.renderTemporaryMessage("Nothing to reset.", 1);
            }
        }
    }

    private boolean drawGame() {
        Player opponent = this.game.getOpponent();
        // if the opponent is a human player, then prompt for acceptance
        if (!(opponent instanceof HumanPlayer) || confirm(opponent + ": Accept draw? ")) {
            // mark the game as draw
            this.game.markDraw();
            // notify the persistent store that unsaved changes were made
            this.persistentStore.markUnsavedChanges();
            this.frame.clearMessage();
            return true;
        }
        return false;
    }

    private boolean revertGame() {
        if (this.persistentStore.hasCheckpoint()) {
            if (this.persistentStore.hasUnsavedChanges()) {
                this.frame.setMessage("The game will be reverted to last saved. "
                        + "All unsaved changes will be lost and this cannot be undone.").render();
                this.frame.clearMessage();
                if (this.confirm("Revert? ")) {
                    // revert to the last saved checkpoint
                    this.game = this.persistentStore.retrieveCheckpoint();
                    this.frame.setChessboardHint(null)
                            .renderTemporaryMessage("Game reverted.", 1.0);
                    return true;
                }
                // user did not confirm revert
            }
            // no changes were made
            this.frame.renderTemporaryMessage("Cannot revert: no changes were made.", 2.0);
        } else {
            // new game: previous checkpoint does not exist, fall through to reset
            this.frame.renderTemporaryMessage("Cannot revert: no checkpoint. "
                    + "Did you mean '/reset'?", 2.0);
        }
        return false;
    }

    private void showHint(Position hintPosition) {
        this.game.getChessboard().getPieceAt(hintPosition).ifPresentOrElse(hintedPiece -> {
            if (hintedPiece.isOpponentTo(this.game.getPlayerToMove())) {
                this.frame.setMessage("Illegal: " + hintedPiece + " is an enemy piece.");
            } else {
                // piece exists and is an ally
                // hint is allowed for the current player
                this.frame.clearMessage()
                        .setChessboardHint(hintPosition);
            }
        }, () -> {
            // piece does not exist at the specified position
            this.frame.setMessage("No piece at " + hintPosition + ".");
        });
    }

    private void printMoveHistory() {
        List<Movement> moves = this.game.getPreviousMoves();
        if (moves.isEmpty()) {
            this.frame.renderTemporaryMessage("No previous move.", 1.0);
        } else {
            StringBuilder historyEntries = new StringBuilder();
            int numOfAllMoves = moves.size();
            if (moves.size() > 5) {
                // print truncation ellipsis
                historyEntries.append("...\n");
                // get last 5 moves only
                moves = new List<>(moves.subList(moves.size() - 5, moves.size()));
            }
            // moves.size() <= 5
            for (int i = 0; i < moves.size(); i++) {
                int realIndex = numOfAllMoves - (moves.size() - i) + 1;
                historyEntries.append(realIndex).append(". ")
                        .append(moves.get(i)).append("\n");
            }
            this.frame.beginTemporaryInfo("History:", frame -> {
                frame.setMessage(historyEntries.toString().strip()).render();
            });
        }
    }


    // ===============  LOAD GAME  ===============


    private void loadGame() {
        // create a new view by resetting frame
        this.frame.reset()
                .setHeader(this.getSeparator()
                        + "\n\033[1mLoad Game From File\033[22m");
        while (true) {
            // display frame
            this.frame.render();
            String filePath = this.getUserInput("Source: ");
            if (filePath.isEmpty()) {
                // no path specified, ask if user wants to start a new game
                this.frame.setMessage("No file specified, start a new game?")
                        .render();
                if (this.confirm("")) {
                    this.startNewGame();
                }
                // user denied starting a new game, back to main menu
                break;
            }
            // user specified a path to load the game, attempt to read from path
            this.persistentStore = new GameStore(filePath);
            try {
                this.game = this.persistentStore.read();
                this.configurePlayers();
                this.startGameCycle();
                break;
            } catch (NoSuchFileException e) {
                // FileNotFoundException: file does not exist
                this.frame.setMessage("File '" + filePath + "' does not exist.");
            } catch (IOException e) {
                // other IOException: reading error
                this.frame.setMessage("An error occurred while reading '" + filePath + "'.");
            } catch (Exception exception) {
                // other exceptions: file format error
                this.frame.setMessage("Unable to load game: Incorrect file format.\n"
                        + "Debugging Info: " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        }
    }

    private void configurePlayers() {
        String header = "\033[1m" + this.getSeparator()
                + "\nGame Name: " + this.game.getName() + "\n";
        for (Color color : chess.model.Color.values()) {
            Player player = this.game.getPlayer(color);
            this.frame.setHeader(header + player + "\033[22m")
                    .clearMessage().render();

            String name = this.getUserInput("Name: ");
            player.setName(name);
            String playerName = player.getName();
            this.frame.setHeader(header + player + "\033[22m")
                    .setMessage(this.getPlayerOptions()).render();
            int playerType = this.getIntegerSelection("Player type: ", PlayerLoader.getLoadedPlayers().size());
            this.game.setPlayer(PlayerLoader.generatePlayer(playerName, color, 0, playerType));
        }
    }

    private String getPlayerOptions() {
        List<String> loadedAgentNames = PlayerLoader.getLoadedPlayerNames();
        for (int i = 0; i < loadedAgentNames.size(); i++) {
            loadedAgentNames.set(i, (i + 1) + ". " + loadedAgentNames.get(i));
        }
        return String.join("\n", loadedAgentNames) + "\n";
    }

    private void startGameViewer() {
        this.updateScreenWithGameState()
                .setChessboardHint(null);;
        while (true) {
            this.frame.render();
            try {
                // read for command
                this.viewerCommand.initialize();
                if (this.executeViewerCommand()) {
                    // should exit
                    break;
                }
            } catch (Exception error) {
                this.frame.setMessage(error.getMessage());
            }
        }
        while (this.game.hasForward()) {
            this.game.forward();
        }
    }

    private boolean executeViewerCommand() {
        ViewerCommand command = this.viewerCommand;
        int step = command.getStep();
        switch (command.getKeyword()) {
            case "/replay" -> {
                while (this.game.hasForward()) {
                    Interface.sleep(command.getSecondsPerFrame());
                    this.game.forward();
                    this.updateScreenWithGameState().render();
                }
                this.frame.setMessage("Replay complete!");
                return false;
            }
            case "/next" -> {
                if (this.game.getFutureMoves().isEmpty()) {
                    this.frame.setMessage("No next moves.");
                } else {
                    while (this.game.hasForward() && step > 0) {
                        this.game.forward();
                        step--;
                    }
                    this.updateScreenWithGameState();
                }
                return false;
            }
            case "/prev" -> {
                if (this.game.getPreviousMoves().isEmpty()) {
                    this.frame.setMessage("No previous moves.");
                } else {
                    while (this.game.hasBackward() && step > 0) {
                        this.game.backward();
                        step--;
                    }
                    this.updateScreenWithGameState();
                }
                return false;
            }
            case "/start" -> {
                if (this.game.getPreviousMoves().isEmpty()) {
                    this.frame.setMessage("Already at the start.");
                } else {
                    while (this.game.hasBackward()) {
                        this.game.backward();
                    }
                    this.updateScreenWithGameState();
                }
                return false;
            }
            case "/end" -> {
                if (this.game.getFutureMoves().isEmpty()) {
                    this.frame.setMessage("Already at the end.");
                } else {
                    while (this.game.hasForward()) {
                        this.game.forward();
                    }
                    this.updateScreenWithGameState();
                }
                return false;
            }
            case "/exit" -> {
                return true;
            }
            case "/help" -> {
                this.frame.setMessage(this.getCommandHelp(command));
                return false;
            }
        }
        return true;
    }


    // ===============  DESIGN GAME  ===============


    private void designGame() {
        // create new store for new game
        this.persistentStore = new GameStore();
        // configure (dummy) players
        Player whitePlayer = PlayerLoader.generatePlayer("Player 1", White, 0, 0);
        Player blackPlayer = PlayerLoader.generatePlayer("Player 2", Black, 0, 0);
        // set up game with empty chessboard
        this.game = new ChessGame("New Game", Chessboard.empty(), whitePlayer, blackPlayer);
        // begin editor cycle
        this.frame.reset()
                .setHeader("Design Game:")
                .setChessboardDisplay(true, null);
        while (true) {
            this.frame.render();
            try {
                // read for command
                this.boardCommand.initialize();
                // execute the command and return command
                Boolean shouldExit = this.executeBoardCommand();
                // if shouldExit == null, then exit without saving
                if (shouldExit) {
                    // command confirmed or successfully executed, and should save
                    if (!this.game.getChessboard().isEmpty()) {
                        this.saveGamePrompt(true, true);
                    }
                    break;
                }
            } catch (Exception error) {
                this.frame.setMessage(error.getMessage());
            }
        }
    }

    private Boolean executeBoardCommand() {
        BoardCommand command = this.boardCommand;
        Chessboard chessboard = this.game.getChessboard();
        Position position = command.getPosition();
        switch (command.getKeyword()) {
            case "/white" -> {
                try {
                    this.placePieceOnBoard(command.getNewPieceName(), White, position);
                    // notify the persistent store that unsaved changes were made
                    this.persistentStore.markUnsavedChanges();
                } catch (Exception err) {
                    this.frame.setMessage(err.getMessage());
                }
            }
            case "/black" -> {
                try {
                    this.placePieceOnBoard(command.getNewPieceName(), Black, position);
                    // notify the persistent store that unsaved changes were made
                    this.persistentStore.markUnsavedChanges();
                } catch (Exception err) {
                    this.frame.setMessage(err.getMessage());
                }
            }
            case "/remove" -> {
                if (chessboard.vacatePosition(position)) {
                    // a piece was removed as a result of this
                    this.frame.setMessage("Removed piece at " + position + ".");
                    // notify the persistent store that unsaved changes were made
                    this.persistentStore.markUnsavedChanges();
                } else {
                    this.frame.setMessage("No piece at " + position + " to remove.");
                }
            }
            case "/clear" -> {
                if (chessboard.clear()) {
                    // the chessboard is cleared as a result of this
                    this.frame.setMessage("Chessboard cleared.");
                    // notify the persistent store that unsaved changes were made
                    this.persistentStore.markUnsavedChanges();
                } else {
                    this.frame.setMessage("Chessboard is already empty.");
                }
            }
            case "/set" -> {
            }
//                Optional<Piece> possiblePiece = chessboard.getPieceAt(position);
//                if (possiblePiece.isPresent()) {
//                    // get target piece, key, and value
//                    Piece piece =  possiblePiece.get();
//                    String key = command.getKey();
//                    String value = command.getValue();
//                    // there exists a piece at the position
//                    if (piece instanceof King || piece instanceof Rook) {
//                        // King or Rook, set onFirstMove
//                        if (key.equals("first-move")) {
//                            boolean firstMoveValue = Boolean.parseBoolean(value);
//                            if (((CastlingPiece) piece).setNotMovedBefore(firstMoveValue)) {
//                                String message = piece + ": " + key + " set to " + firstMoveValue + ".";
//                                this.frame.setMessage(message);
//                                // notify the persistent store that unsaved changes were made
//                                this.persistentStore.markUnsavedChanges();
//                            } else {
//                                this.frame.setMessage(piece + " not at an initial position.");
//                            }
//                        } else {
//                            this.frame.setMessage("Unknown key '" + key + "' for " + piece + ".");
//                        }
//                    } else if (piece instanceof Pawn) {
//                        // configure en passant
//                        if (key.equals("en-passant")) {
//                            boolean enPassant = Boolean.parseBoolean(value);
//                            if (((Pawn) piece).setCapturableByEnPassant(enPassant)) {
//                                String message = piece + ": " + key + " set to " + enPassant + ".";
//                                this.frame.setMessage(message);
//                                // notify the persistent store that unsaved changes were made
//                                this.persistentStore.markUnsavedChanges();
//                            } else {
//                                String message = "En passant capture is not allowed for " + piece
//                                        + " in the current configuration.";
//                                this.frame.setMessage(message);
//                            }
//                        } else {
//                            String message = "Unknown key '" + key + "' for " + piece + ".";
//                            this.frame.setMessage(message);
//                        }
//                    }
//                } else {
//                    this.frame.setMessage("No piece at " + position + ".");
//                }
            case "/finish" -> {
                if (!chessboard.isEmpty()) {
                    // non-empty chessboard, check if both sides have a King
                    for (Color color : Color.values()) {
                        if (chessboard.getActivePieces(color).count(piece -> (piece instanceof King)) != 1) {
                            // the side is missing a King
                            this.frame.setMessage(color + " is missing a King.");
                            return false;
                        }
                    }
                }
                this.frame.clearMessage().render();
                // check for conclusion (game state) and infer next player
//                boolean shouldSaveOnExit = this.game.finalizeDesign(state -> {
//                    switch (state) {
//                        case inProgress:
//                            // nothing special, in the middle of the game, prompt for which side to move
//                            while (true) {
//                                this.frame.render();
//                                String nextColor = this.getUserInput("Player to move (White/Black): ");
//                                if (nextColor.equals("")) {
//                                    // no input provided, assuming that a draw override is intended
//                                    this.frame.setMessage("No player specified.").render();
//                                    this.frame.clearMessage();
//                                    if (this.confirm("Mark as draw? ")) {
//                                        // user confirmed draw override
//                                        this.game.overrideStateAsDraw();
//                                        return White;
//                                    }
//                                    // user did not confirm draw, should exit without saving?
//                                    if (this.confirm("Exit (and discard edit)? ")) {
//                                        // exit regardless (discard edits)
//                                        return null;
//                                    }
//                                } else if (List.of("White", "Black").contains(nextColor)) {
//                                    // a valid color is provided
//                                    return Colored.Color.of(nextColor);
//                                } else {
//                                    this.frame.setMessage("Unknown color '" + nextColor + "'.").render();
//                                    this.frame.clearMessage();
//                                }
//                            }
//                        case inCheck:
//                            // infer next player to be the player to move
//                            Colored.Color inferredNextColor = (Colored.Color) state.getObject();
//                            String message = inferredNextColor + " is in check, " + inferredNextColor + " to move.";
//                            this.frame.setMessage(message).render();
//                            return inferredNextColor;
//                        case draw:
//                            // stalemated, doesn't matter who the next player is (game over)
//                            // regardless, infer next player to be the one who is stalemated
//                            return (Colored.Color) state.getObject();
//                        case finished:
//                            // checkmated, doesn't matter who the next player is (game over)
//                            // regardless, infer next player to be the opponent of the winner
//                            return ((Colored.Color) state.getObject()).opposite();
//                    }
//                    // NOTE: state 'aborted' is not possible
//                    return White;
//                });
//                return shouldSaveOnExit ? true : null;
                return true;
            }
            case "/help" -> this.frame.setMessage(this.getCommandHelp(command));
        }
        return false;
    }

    private void placePieceOnBoard(String pieceName, chess.model.Color color, Position position) throws Exception {
        Chessboard chessboard = this.game.getChessboard();
        Optional<Piece> currentPiece = chessboard.getPieceAt(position);
        HashMap<String, Integer> allowedCounts = new HashMap<>(Map.of(
                "Pawn", 8,
                "Rook", 2, "Knight", 2, "Bishop", 2,
                "Queen", 1, "King", 1
        ));
        int maxAllowed = allowedCounts.get(pieceName);
        // get current count for the target piece
        int count = chessboard.getActivePieces(color).count(piece -> {
            // count all pieces that have the same class name (same type of piece)
            return piece.getClass().getSimpleName().equals(pieceName);
        });
        // check if it already is (or, trivially, exceeds) the maximum number allowed
        if (count >= maxAllowed) {
            // the new piece cannot be placed on the chessboard
            throw new Exception("Too many " + pieceName + "s (" + maxAllowed + " max).");
        }
        // check Pawns
        if (pieceName.equals("Pawn") && position.atLastRank(color.opposite())) {
            // user attempted to add a Pawn to an illegal position (first/last rank)
            throw new Exception("Illegal position '" + position + "' for a Pawn.");
        }
        // check Bishops (placing both Bishops on black/white-square is not allowed)
        if (pieceName.equals("Bishop")) {
            // check if the Bishop is placed legally
            Optional<Piece> otherBishopPiece = chessboard.getActivePieces(color).findFirst(piece -> {
                // get the other Bishop
                return (piece instanceof Bishop);
            });
            if (otherBishopPiece.isPresent()
                    && otherBishopPiece.get().getPosition().getSquareColor() == position.getSquareColor()) {
                // user attempted to add another Bishop to an illegal position (same color square)
                throw new Exception("Illegal position '" + position + "' for a Bishop.");
            }
        }
        Piece placedPiece = chessboard.placePiece(pieceName, color, position);
        // check Kings (Kings cannot be placed near each other's reachable range)
        if (placedPiece instanceof King) {
            for (Position reachablePosition : ((King) placedPiece).reachablePositions()) {
                Optional<Piece> piece = chessboard.getPieceAt(reachablePosition);
                if (piece.isPresent()) {
                    Piece reachablePiece = piece.get();
                    if (reachablePiece.isOpponentTo(placedPiece) && (reachablePiece instanceof King)) {
                        // found an enemy King near its vicinity
                        // undo place piece, and throw not allowed exception
                        chessboard.vacatePosition(position);
                        throw new Exception("Illegal position for " + placedPiece + ".");
                    }
                }
            }
        }
        String message = "Placed " + placedPiece;
        if (currentPiece.isPresent()) {
            // had a piece, update message to say "replaced"
            message += ", replacing " + currentPiece;
        }
        this.frame.setMessage(message + ".");
    }


    // ===============  GAME SAVING  ===============


    // EFFECTS: prompts the user to same the game, if needsConfirm is true, prompt
    //            to confirm saving before proceeding; if doubleConfirm is true,
    //            the user will be asked once again before losing their changes;
    //          returns true if the game is saved successfully,
    //          otherwise if the game failed to save and/or the user gave up saving, OR
    //            nothing is saved (nothing unsaved to be saved), returns false
    private boolean saveGamePrompt(boolean needsConfirm, boolean doubleConfirm) {
        if (!this.persistentStore.hasUnsavedChanges()) {
            // no unsaved changes, do nothing and exit
            return false;
        }
        // if confirmation is needed, then confirm with user
        if (!needsConfirm || this.confirm("Save? ")) {
            if (!this.persistentStore.isNewFile()) {
                // attempt to save (overwrite) to an existing game
                if (this.saveGame(null)) {
                    // success, exit
                    return true;
                }
                // failed to save, fallback to alternate location saving
            }
            // is a new file, start new file save prompt cycle
            while (true) {
                this.frame.clearMessage().render();
                String savePath = this.getUserInput("Save as: ");
                if (savePath.isEmpty()) {
                    // user did not specify a location to save
                    // ask if user wants to save at default location
                    this.frame.setMessage("Save as '"
                            + this.persistentStore.getFilePath() + "'?").render();
                    if (this.confirm("")) {
                        // user confirmed saving at default location
                        if (this.saveGame(null)) {
                            // saved successfully, exit save cycle
                            return true;
                        }
                        // error occurred while saving, continue cycle
                    } else if (doubleConfirm) {
                        // user denied to save as default
                        // double confirm with the user if they wish to lose the game
                        this.frame.setMessage("Proceed to lose the game? "
                                + "This cannot be undone.").render();
                        if (this.confirm("Confirm? ")) {
                            // user confirmed losing the game, exit save cycle
                            return false;
                        }
                        // user decide not to lose the game, continue save cycle
                    } else {
                        // does not double confirm, quit without save
                        return false;
                    }
                } else {
                    // user did provide a location to save
                    GameStore tempStore = new GameStore(savePath);
                    if (!tempStore.isNewFile()) {
                        // the file already exists at the location
                        // ask if user wants to overwrite it
                        this.frame.setMessage("File '"
                                + tempStore.getFilePath() + "' exists.").render();
                        if (!this.confirm("Overwrite? ")) {
                            // user denied overwriting, exit save
                            if (!doubleConfirm) {
                                // end when no double confirm is enabled
                                // allowing the user to exit the save cycle
                                // unless when the game is exiting and the save cycle demands an action
                                return false;
                            }
                            // user did not want to overwrite,
                            // continue the retry loop to ask for another location
                            continue;
                        }
                        // user confirmed overwriting
                    }
                    // save new game and exit
                    if (this.saveGame(savePath)) {
                        return true;
                    }
                    // failed to save game, continue save cycle to retry
                }
            }
        }
        // user did not confirm saving
        return false;
    }

    private boolean saveGame(String savePath) {
        if (savePath != null) {
            // user specified save path, set new path
            this.persistentStore.setNewFilePath(savePath);
        }
        try {
            this.persistentStore.saveCheckpoint(this.game);
            this.frame.renderTemporaryMessage("Save Success!", 1.0);
            return true;
        } catch (Exception error) {
            this.frame.renderTemporaryMessage("Failed to save: " + error.getMessage(), 1.5);
            return false;
        }
    }

    private void saveGame() {
        if (this.persistentStore.hasUnsavedChanges()) {
            this.frame.beginTemporaryInfo("\033[1mSaving Game: \033[22m", frame -> {
                this.saveGamePrompt(false, false);
            });
        } else {
            // no unsaved changes to be saved
            this.frame.renderTemporaryMessage("Nothing to save.", 1.0);
        }
        this.frame.clearMessage();
    }


    // ===============  GAME STATES (DELEGATE)  ===============


    private FrameController updateScreenWithGameState() {
        String header;
        if (!this.game.hasConcluded()) {
            // update header (new line): round information + player to move
            // NOTE: this is mutually-exclusive to whether the game has concluded
            //       this is because the player to move was not updated before the
            //       game concluded, hence it would give the wrong player
            header = "Round " + this.game.getRoundNumber() + " - "
                    + this.game.getPlayerToMove() + " to move";
        } else {
            // game concluded, get conclusion as the header
            header = "\033[1mGame Over: " + this.game.getStateDescription() + "\033[22m";
        }
        this.frame.clearInfo().clearMessage()
                .setHeader(header);

        // update info: previous move done by the opponent
        this.game.getMostRecentMove().ifPresent(previousMove -> {
            Player playerToMove = this.game.getPlayerToMove();
            Player previousPlayer = this.game.getOpponent();
            String moveDescription = previousPlayer + ": " + previousMove;
            if (previousMove.willCheckOpponent()) {
                moveDescription += ", " + playerToMove + " is in check!";
            }
            this.frame.setInfo(moveDescription);
        });

        return this.frame;
    }


    // ===============  HELPERS  ===============


    // REQUIRES: hintPosition represents a valid position on the chessboard in which there is a piece and not vacant, OR
    //           hintPosition is null, indicating no move hints are shown
    // EFFECTS: prints the chessboard via standard output with/without move hints,
    //          if hintPosition is null, then no move hints are displayed
    void printChessboard(Position hintPosition) {
        Chessboard chessboard = this.game.getChessboard();

        // get move hints if applicable
        List<Movement> moveHints = new List<>();
        Piece hintedPiece;
        if (hintPosition != null) {
            // get candidate movements of the piece at position
            hintedPiece = chessboard.getPieceAt(hintPosition).orElseThrow();
            moveHints = hintedPiece.getLegalMoves(chessboard);
        }
        // print files
        println("   " + String.join("  ", Position.files));
        for (int rank = 8; rank >= 1; rank--) {
            // print ranks
            print(rank + " ");

            for (int file = 1; file <= 8; file++) {
                Position position = new Position(file, rank);
                java.awt.Color squareColor = position.getSquareColor().isWhite() ? whiteSquare : blackSquare;
                print(getEscapeSequence(squareColor, FillMode.background) + "\033[1m");

                // print hints if applicable
                if (hintPosition != null) {
                    // hint is requested
                    if (position.equals(hintPosition)) {
                        // piece within current square is being hinted
                        // blink the current text symbol (NOT supported in IDEs and some terminals)
                        print("\033[5m");
                    }
                    for (Movement move : moveHints) {
                        Position highlightPosition = move.getFinalPosition();
                        if (move instanceof Castling) {
                            // highlight Rook piece instead of final position
                            highlightPosition = ((Castling) move).getRook(chessboard).getPosition();
                        }
                        // is current position
                        if (position.equals(highlightPosition)) {
                            // print background sequence depending on the associated movement
                            java.awt.Color highlightColor = green;
                            if (move instanceof Capture) {
                                // capture or en passant capture, red background
                                highlightColor = red;
                            } else if (move instanceof Castling) {
                                // castling, blue background
                                highlightColor = blue;
                            }
                            // change to purple background if the move checks the enemy King
                            if (move.willCheckOpponent()) {
                                highlightColor = purple;
                            }
                            print(getEscapeSequence(highlightColor, FillMode.background));
                            break;
                        }
                        // not current position, keep looking
                    }
                }
                // print chess piece at position
                chessboard.getPieceAt(position).ifPresentOrElse(piece -> {
                    // print chess piece
                    char textSymbol = piece.getCharacterSymbol(true);
                    java.awt.Color pieceColor = piece.isWhite() ? white : black;
                    print(getEscapeSequence(pieceColor, FillMode.foreground) + " " + textSymbol + " ");
                }, () -> {
                    // empty square
                    print("   ");
                });
                // turn off blinking if applicable
                print("\033[25m");
            }
            // reset sequence: reset foreground/background
            print("\033[0;0m");
            // print ranks
            println(" " + rank);
        }
        // print files
        println("   " + String.join("  ", Position.files));
    }

    private String getCommandHelp(CommandGroup commandGroup) {
        StringBuilder helpText = new StringBuilder("Available Commands:\n");
        for (Command command : commandGroup.availableCommands()) {
            helpText.append(command.getHelpMessage()).append("\n");
        }
        helpText.append("Note: Extra arguments passed to the commands will be ignored.");
        return helpText.toString();
    }

    private String getSeparator() {
        return String.valueOf(separator).repeat(30);
    }

    // EFFECTS: prompts for a confirmation dialogue and returns true if the user confirms,
    //          otherwise false
    private boolean confirm(String message) {
        while (true) {
            print(message);
            String response = this.getUserInput("(y/n) ").toLowerCase();
            switch (response) {
                case "y" -> {
                    return true;
                }
                case "n" -> {
                    return false;
                }
                default -> this.frame.renderTemporaryMessage("Invalid option '" + response + "'.", 1.0);
            }
        }
    }

    private int getIntegerSelection(String prompt, int bound) {
        while (true) {
            int mode = 0;
            try {
                String userInput = this.getUserInput(prompt);
                mode = Integer.parseInt(userInput);
                if (mode > bound) {
                    throw new IndexOutOfBoundsException();
                }
                return mode - 1;
            } catch (NumberFormatException e) {
                this.frame.renderTemporaryMessage("Invalid input.", 1.0);
            } catch (IndexOutOfBoundsException e) {
                this.frame.renderTemporaryMessage(String.format("Invalid selection %d.", mode), 1.0);
            }
        }
    }

    private String getUserInput() {
        return this.scanner.nextLine().strip();
    }

    private String getUserInput(String prompt) {
        print(prompt);
        return this.getUserInput();
    }

}
