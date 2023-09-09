package chess.model.moves;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.model.Position;
import chess.model.pieces.Pawn;

import static org.junit.jupiter.api.Assertions.*;

public class TwoSquareAdvanceTest extends RegularMoveTest {

    private TwoSquareAdvance twoSquareAdvance;

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        twoSquareAdvance = new TwoSquareAdvance((Pawn) targetPiece, finalPosition);
        // RegularMove tests
        regularMove = twoSquareAdvance;
        // Movement tests
        movement = twoSquareAdvance;
        // Movable tests
        movable = twoSquareAdvance;
        // Colored tests
        whiteObject = twoSquareAdvance;
        Pawn blackPawn = (Pawn) chessboard.getPieceAt("a7").orElseThrow();
        blackObject = new TwoSquareAdvance(blackPawn, Position.at("a5"));
    }

    @Test
    @Override
    protected void testExecute() {
        super.testExecute();
        assertEquals(0, twoSquareAdvance.execute(chessboard));
        assertTrue(((Pawn) targetPiece).isEnPassant());
    }

    @Test
    @Override
    protected void testUndo() {
        super.testUndo();
        twoSquareAdvance.execute(chessboard);
        assertEquals(0, twoSquareAdvance.undo(chessboard));
        assertFalse(((Pawn) targetPiece).isEnPassant());
    }

    @Test
    @Override
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();
        assertDoesNotThrow(() -> new TwoSquareAdvance(twoSquareAdvance.encode(), chessboard));
    }

}
