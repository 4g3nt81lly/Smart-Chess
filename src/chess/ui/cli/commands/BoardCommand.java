package chess.ui.cli.commands;

import chess.model.Position;
import chess.model.Color;

import chess.util.List;

public class BoardCommand extends CommandGroup {

    private String newPieceName;

    private Color color;

    private Position position;

    private String key;

    private String value;

    @Override
    public List<Command> availableCommands() {
        return List.of(
                new Command("/white", 2,
                        "/white [Piece] [position]",
                        "Place a White [Piece] at [position]."),
                new Command("/black", 2,
                        "/black [Piece] [position]",
                        "Place a Black [Piece] at [position]."),
                new Command("/remove", 1,
                        "/remove [position]",
                        "Remove the piece at [position]."),
                new Command("/clear", "/clear",
                        "Clear the chessboard."),
                new Command("/set", 3,
                        "/set [position] [key] [value]",
                        "Set the state of the piece at [position]."),
                new Command("/finish", "/finish",
                        "Finish customization and exit the chessboard editor."),
                new Command("/help", "/help",
                        "Show this help.")
        );
    }

    @Override
    protected void parse() throws Exception {
        switch (this.command.getKeyword()) {
            case "/white":
            case "/black":
                this.initializePlace();
                break;
            case "/remove":
                this.initializeRemove();
                break;
            case "/set":
                this.initializeSet();
                break;
        }
    }

    private void initializePlace() throws Exception {
        // initialize color
        if (this.command.getKeyword().equals("/white")) {
            this.color = Color.White;
        } else {
            this.color = Color.Black;
        }
        // parse arguments [Piece] [position]
        String pieceName = this.getArgument(0).toLowerCase();
        String position = this.getArgument(1);
        // validate piece name
        if (List.of("pawn", "rook", "knight", "bishop", "queen", "king").contains(pieceName)) {
            // piece is valid
            // set piece name to the title case of the lower-cased argument
            String capitalizedFirstChar = String.valueOf(pieceName.charAt(0)).toUpperCase();
            this.newPieceName = capitalizedFirstChar + pieceName.substring(1);
            this.position = Position.at(position);
        } else {
            throw new Exception("Unknown piece '" + pieceName + "'.");
        }
    }

    private void initializeRemove() throws Exception {
        // get first argument as the position to be removed
        String position = this.getArgument(0);
        this.position = Position.at(position);
    }

    private void initializeSet() throws Exception {
        // set a key-value for a given piece
        // [position] [key] [value]
        String position = this.getArgument(0);
        String key = this.getArgument(1).toLowerCase();
        String value = this.getOptionalArgument(2);
        if (List.of("en-passant", "first-move").contains(key)) {
            // key is valid, set key, value, and position
            this.key = key;
            this.value = value;
            this.position = Position.at(position);
        } else {
            throw new Exception("Invalid key '" + key + "'.");
        }
    }

    public String getNewPieceName() {
        return this.newPieceName;
    }

    public Color getColor() {
        return this.color;
    }

    public Position getPosition() {
        return this.position;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

}
