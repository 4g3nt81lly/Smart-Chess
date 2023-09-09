package chess.model.moves;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;
import java.util.UUID;

import static chess.model.TextSymbols.X;
import static chess.model.moves.Capture.capturedPieceIdKey;
import static org.junit.jupiter.api.Assertions.*;

// NOTE: some of the moves used in the tests below, in a real game, are illegal;
//       here they are used for testing purposes only
//       for movements do not care whether they are legal
public class CaptureTest extends RegularMoveTest {

    protected Piece capturedPiece;

    protected Capture capture;

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        initialPosition = Position.at("a1");
        finalPosition = Position.at("a7");

        targetPiece = chessboard.getPieceAt(initialPosition).orElseThrow();
        capturedPiece = chessboard.getPieceAt(finalPosition).orElseThrow();
        capture = new Capture(targetPiece, capturedPiece);

        // RegularMove tests
        regularMove = capture;
        // Movement tests
        movement = capture;
        // Movable tests
        movable = capture;
        // Colored tests
        whiteObject = capture;
        blackObject = new Capture(
                chessboard.getPieceAt("h8").orElseThrow(),
                chessboard.getPieceAt("h2").orElseThrow()
        );
    }

    @Test
    protected void testCaptureConstructor() {
        assertEquals(capturedPiece.getIdentifier(), capture.getCapturedPieceId());
        assertFalse(capture.isCheckingOpponent(chessboard));

        // capturedPiece is null
        assertThrows(NullPointerException.class,
                () -> new Capture(targetPiece, null));
    }

    @Test
    @Override
    protected void testExecute() {
        assertEquals(capturedPiece.getPoints(), capture.execute(chessboard));
        assertEquals(1, targetPiece.getMoveCount());
        assertTrue(targetPiece.isAt(finalPosition));
        assertFalse(chessboard.getActivePieces().contains(capturedPiece));

        // chessboard is null
        assertThrows(NullPointerException.class,
                () -> capture.execute(null));

        // piece not found on chessboard
        assertThrows(NoSuchElementException.class,
                () -> capture.execute(Chessboard.empty()));
    }

    @Test
    @Override
    protected void testUndo() {
        capture.execute(chessboard);

        assertEquals(capturedPiece.getPoints(), capture.undo(chessboard));
        assertEquals(0, targetPiece.getMoveCount());
        assertTrue(targetPiece.isAt(initialPosition));
        assertTrue(chessboard.getActivePieces().contains(capturedPiece));

        // chessboard is null
        assertThrows(NullPointerException.class,
                () -> capture.undo(null));

        // piece not found on chessboard
        assertThrows(NoSuchElementException.class,
                () -> capture.undo(Chessboard.empty()));
    }

    @Test
    @Override
    protected void testMovementDescriptor() {
        assertEquals(String.valueOf(X), capture.getDescriptor());
    }

    @Test
    @Override
    protected void testDestinationString() {
        assertEquals(capturedPiece.getCharacterSymbol() + "@" + finalPosition,
                capture.getDestinationString(chessboard));
    }

    @Test
    @Override
    protected void testEncode() {
        super.testEncode();
        JSONObject captureEncoded = capture.encode();

        assertTrue(captureEncoded.has(capturedPieceIdKey));
        assertEquals(capture.getCapturedPieceId(), captureEncoded.getString(capturedPieceIdKey));
    }

    @Test
    void testDecodeMissingCapturedPieceId() {
        JSONObject invalidJSON = capture.encode();
        invalidJSON.remove(capturedPieceIdKey);

        assertThrows(JSONException.class,
                () -> new Capture(invalidJSON, chessboard));
    }

    @Test
    void testDecodeInvalidCapturedPieceId() {
        JSONObject invalidJSON = capture.encode()
                .put(capturedPieceIdKey, 1);

        assertThrows(JSONException.class,
                () -> new Capture(invalidJSON, chessboard));
    }

    @Test
    void testDecodeCapturedPieceNotFound() {
        String nonexistentId = UUID.randomUUID().toString();
        JSONObject invalidJSON = capture.encode()
                .put(capturedPieceIdKey, nonexistentId);

        try {
            new Capture(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("No piece found with ID '" + nonexistentId + "'.", e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeAlliedCapturedPiece() {
        Piece alliedPiece = chessboard.getPieceAt("b1").orElseThrow();
        JSONObject invalidJSON = capture.encode()
                .put(capturedPieceIdKey, alliedPiece.getIdentifier());

        try {
            new Capture(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("Incompatible: Capturing an allied piece " + alliedPiece + ".",
                    e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    @Override
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();
        assertDoesNotThrow(() -> {
            Capture decoded = new Capture(capture.encode(), chessboard);

            assertEquals(capturedPiece.getIdentifier(), decoded.getCapturedPieceId());
        });
    }

}
