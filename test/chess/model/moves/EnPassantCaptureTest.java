package chess.model.moves;

import chess.model.Position;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static chess.model.moves.Capture.capturedPieceIdKey;
import static chess.model.moves.Movement.pieceIdKey;
import static org.junit.jupiter.api.Assertions.*;

// NOTE: some of the moves used in the tests below, in a real game, are illegal;
//       here they are used for testing purposes only
//       for movements do not care whether they are legal
public class EnPassantCaptureTest extends CaptureTest {

    private static final String nonPawnExceptionMessage = "En passant capture cannot be applied to non-Pawn pieces.";

    private EnPassantCapture enPassantCapture;

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        // simple (illegal in game) setup for en passant capture
        initialPosition = Position.at("a5");
        finalPosition = Position.at("b6");
        capturedPiece = chessboard.getPieceAt("b7").orElseThrow();
        capturedPiece.moveTo(Position.at("b5"));
        targetPiece = chessboard.getPieceAt("a2").orElseThrow();
        targetPiece.moveTo(initialPosition);

        enPassantCapture = new EnPassantCapture((Pawn) targetPiece, (Pawn) capturedPiece, finalPosition);
        // Capture tests
        capture = enPassantCapture;
        // RegularMove tests
        regularMove = enPassantCapture;
        // Movement tests
        movement = enPassantCapture;
        // Movable tests
        movable = enPassantCapture;
        // Colored tests
        whiteObject = enPassantCapture;
        // this is impossible in a game, it's merely for brevity
        blackObject = new EnPassantCapture((Pawn) capturedPiece, (Pawn) targetPiece, finalPosition);
    }

    @Test
    @Override
    protected void testCaptureConstructor() {
        super.testCaptureConstructor();
        assertFalse(capture.getFinalPosition().equals(capturedPiece.getPosition()));
    }

    @Test
    void testDecodeTargetPieceIsNotPawn() {
        Piece nonPawnPiece = chessboard.getPieceAt("b1").orElseThrow();
        JSONObject invalidJSON = capture.encode()
                .put(pieceIdKey, nonPawnPiece.getIdentifier());

        try {
            new EnPassantCapture(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals(nonPawnExceptionMessage, e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeCapturedPieceIsNotPawn() {
        Piece nonPawnPiece = chessboard.getPieceAt("b8").orElseThrow();
        JSONObject invalidJSON = capture.encode()
                .put(capturedPieceIdKey, nonPawnPiece.getIdentifier());

        try {
            new EnPassantCapture(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals(nonPawnExceptionMessage, e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    @Override
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();

        assertDoesNotThrow(() -> new EnPassantCapture(enPassantCapture.encode(), chessboard));
    }

}
