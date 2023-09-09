package chess.ui.cli.commands;

import chess.model.Position;

import chess.util.List;

public class ChessCommand extends CommandGroup {

    private Position position;

    private Position targetPosition;

    public ChessCommand() {
        super();
    }

    @Override
    public List<Command> availableCommands() {
        return List.of(
                new Command("/hint", 1, "/hint [position]",
                        "Show candidate moves for the piece at the given position."),
                new Command("/history", "/history",
                        "Show move history."),
                new Command("/move", 2, "/move [position1] [position2]",
                        "Move the piece at [position1] to [position2]."),
                new Command("/undo", "/undo",
                        "Undo the most recent move."),
                new Command("/redo", "/redo",
                        "Redo the most recently undone move."),
                new Command("/save", "/save",
                        "Save the current game and discard the previous checkpoint."),
                new Command("/revert", "/revert",
                        "Revert game to the most recent save."),
                new Command("/reset", "/reset",
                        "Reset the game, discarding all moves."),
                new Command("/draw", "/draw",
                        "Offer a draw to the opponent."),
                new Command("/exit", "/exit",
                        "Abort the game and exit."),
                new Command("/help", "/help",
                        "Show this help.")
        );
    }

    @Override
    protected void parse() throws Exception {
        switch (this.command.getKeyword()) {
            case "/hint":
                this.initializeHint();
                break;
            case "/move":
                this.initializeMove();
                break;
        }
    }

    private void initializeHint() throws Exception {
        // get first argument as position
        String hintPosition = this.getArgument(0);
        this.position = Position.at(hintPosition);
    }

    private void initializeMove() throws Exception {
        // move, read the next two arguments in which:
        // the first is the position of the piece to be moved
        // the second is the destination position to which the piece is moved
        String from = this.getArgument(0);
        String to = this.getArgument(1);
        this.position = Position.at(from);
        this.targetPosition = Position.at(to);
    }

    public Position getPosition() {
        return this.position;
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

}
