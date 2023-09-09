package chess.model.moves;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.model.Position;
import chess.model.pieces.Piece;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegularMoveTest extends MovementTest {

    protected RegularMove regularMove;

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        regularMove = new RegularMove(targetPiece, finalPosition);
        // Movement tests
        movement = regularMove;
        // Movable tests
        movable = regularMove;
        // Colored tests
        whiteObject = regularMove;
        Piece blackPawn = chessboard.getPieceAt("a7").orElseThrow();
        blackObject = new RegularMove(blackPawn, Position.at("a5"));
    }

    @Test
    @Override
    protected void testExecute() {
        super.testExecute();
        assertTrue(targetPiece.isAt(finalPosition));
    }

    @Test
    @Override
    protected void testUndo() {
        super.testUndo();
        regularMove.execute(chessboard);
        regularMove.undo(chessboard);
        assertTrue(targetPiece.isAt(initialPosition));
    }

    @Test
    @Override
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();
        assertDoesNotThrow(() -> new RegularMove(regularMove.encode(), chessboard));
    }

}
