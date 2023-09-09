package chess.model.exceptions;

public class IllegalMove extends GameException {

    public IllegalMove() {
        super();
    }

    public IllegalMove(String message) {
        super(message);
    }

}
