package chess.model.moves;

import org.junit.jupiter.api.BeforeEach;

import static chess.model.Color.Black;
import static chess.model.Color.White;

public class PlayerMoveTest extends MovableTest {

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        movable = new PlayerMove(initialPosition, finalPosition, White);
        // Colored tests
        whiteObject = movable;
        blackObject = new PlayerMove(initialPosition, finalPosition, Black);
    }

}
