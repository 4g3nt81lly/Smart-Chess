package chess.model.exceptions;

public class GameException extends RuntimeException {

    public GameException() {
        super();
    }

    public GameException(String message) {
        super(message);
    }

}
