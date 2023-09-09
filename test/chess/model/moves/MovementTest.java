package chess.model.moves;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static chess.model.TextSymbols.rightArrow;
import static chess.model.moves.Movement.*;
import static org.junit.jupiter.api.Assertions.*;

public class MovementTest extends MovableTest {

    protected Chessboard chessboard;

    protected Piece targetPiece;

    protected Movement movement;

    @Override
    @BeforeEach
    protected void initialize() {
        chessboard = Chessboard.standard();
        initialPosition = Position.at("a2");
        finalPosition = Position.at("a4");
        targetPiece = chessboard.getPieceAt(initialPosition).orElseThrow();
        movement = new Movement(targetPiece, finalPosition) {};
        // Movable tests
        movable = movement;
        // Colored tests
        whiteObject = movement;
        Piece blackPawn = chessboard.getPieceAt("a7").orElseThrow();
        blackObject = new Movement(blackPawn, Position.at("a5")) {};
    }

    @Test
    void testMovementConstructor() {
        assertEquals(targetPiece.getColor(), movement.getColor());
        assertEquals(targetPiece.getIdentifier(), movement.getPieceId());
        assertEquals(movement.getFinalPosition(), finalPosition);
        assertFalse(movement.willCheckOpponent());
    }

    @Test
    protected void testExecute() {
        assertEquals(0, movement.execute(chessboard));
        assertEquals(1, targetPiece.getMoveCount());

        // chessboard is null
        assertThrows(NullPointerException.class,
                () -> movement.execute(null));

        // piece not found on chessboard
        assertThrows(NoSuchElementException.class,
                () -> movement.execute(Chessboard.empty()));
    }

    @Test
    protected void testUndo() {
        movement.execute(chessboard);

        assertEquals(0, movement.undo(chessboard));
        assertEquals(0, targetPiece.getMoveCount());

        // chessboard is null
        assertThrows(NullPointerException.class,
                () -> movement.undo(null));

        // piece not found on chessboard
        assertThrows(NoSuchElementException.class,
                () -> movement.undo(Chessboard.empty()));
    }

    @Test
    void testGetPieceFromChessboard() {
        assertEquals(targetPiece, movement.getPieceFrom(chessboard));

        // piece not found on chessboard
        assertThrows(NoSuchElementException.class,
                () -> movement.getPieceFrom(Chessboard.empty()));
    }

    @Test
    void testSetWillCheckOpponent() {
        movement.setWillCheckOpponent(true);
        assertTrue(movement.willCheckOpponent());

        movement.setWillCheckOpponent(false);
        assertFalse(movement.willCheckOpponent());
    }

    @Test
    protected void testMovementDescriptor() {
        assertEquals("", movement.getDescriptor());
    }

    @Test
    protected void testDestinationString() {
        assertEquals(finalPosition.toString(), movement.getDestinationString(chessboard));
    }

    @Test
    @Override
    protected void testStringRepresentation() {
        String expected = targetPiece.getCharacterSymbol() + "@" + initialPosition.toString() + " "
                + movement.getDescriptor() + rightArrow + " "
                + movement.getDestinationString(chessboard);
        assertEquals(expected, movement.toString(chessboard));
    }

    @Test
    @Override
    protected void testEncode() {
        super.testEncode();
        JSONObject movementEncoded = movement.encode();

        assertTrue(movementEncoded.has(movementTypeKey));
        assertEquals(movement.getClass().getSimpleName(), movementEncoded.getString(movementTypeKey));

        assertTrue(movementEncoded.has(pieceIdKey));
        assertEquals(movement.getPieceId(), movementEncoded.getString(pieceIdKey));

        assertTrue(movementEncoded.has(willCheckOpponentKey));
        assertEquals(movement.willCheckOpponent(), movementEncoded.getBoolean(willCheckOpponentKey));
    }

    @Test
    void testDecodeNullChessboard() {
        assertThrows(NullPointerException.class,
                () -> new Movement(movement.encode(), null) {});
    }

    @Test
    void testDecodeMissingPieceId() {
        JSONObject invalidJSON = movement.encode();
        invalidJSON.remove(pieceIdKey);

        assertThrows(JSONException.class,
                () -> new Movement(invalidJSON, chessboard) {});
    }

    @Test
    void testDecodeInvalidPieceId() {
        JSONObject invalidJSON = movement.encode()
                .put(pieceIdKey, 1);

        assertThrows(JSONException.class,
                () -> new Movement(invalidJSON, chessboard) {});
    }

    @Test
    void testDecodeMissingWillCheckOpponent() {
        JSONObject invalidJSON = movement.encode();
        invalidJSON.remove(willCheckOpponentKey);

        assertThrows(JSONException.class,
                () -> new Movement(invalidJSON, chessboard) {});
    }

    @Test
    void testDecodeInvalidWillCheckOpponent() {
        JSONObject invalidJSON = movement.encode()
                .put(willCheckOpponentKey, 1);

        assertThrows(JSONException.class,
                () -> new Movement(invalidJSON, chessboard) {});
    }

    @Test
    void testDecodeTargetPieceNotFound() {
        String nonexistentId = UUID.randomUUID().toString();
        JSONObject invalidJSON = movement.encode()
                .put(pieceIdKey, nonexistentId);

        try {
            new Movement(invalidJSON, chessboard) {};
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("No piece found with ID '" + nonexistentId + "'.", e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeOpponentTargetPiece() {
        Piece opponentsPiece = chessboard.getPieceAt("a7").orElseThrow();
        JSONObject invalidJSON = movement.encode()
                .put(pieceIdKey, opponentsPiece.getIdentifier());

        try {
            new Movement(invalidJSON, chessboard) {};
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("Incompatible: " + movement.getColor() + "'s move and "
                    + opponentsPiece.getColor() + "'s piece.",
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
            Movement decoded = new Movement(movement.encode(), chessboard) {};

            assertEquals(targetPiece.getIdentifier(), decoded.getPieceId());
            assertEquals(movement.willCheckOpponent(), decoded.willCheckOpponent());
        });
    }

    @Test
    void testFactoryDecodeMissingMovementType() {
        JSONObject invalidJSON = movement.encode();
        invalidJSON.remove(movementTypeKey);

        assertThrows(JSONException.class,
                () -> Movement.decode(invalidJSON, chessboard));
    }

    @Test
    void testFactoryDecodeInvalidMovementType() {
        JSONObject invalidJSON = movement.encode()
                .put(movementTypeKey, 1);

        assertThrows(JSONException.class,
                () -> Movement.decode(invalidJSON, chessboard));
    }

    @Test
    void testFactoryDecodeUnknownMovementType() {
        String nonexistentMovementType = "NonexistentMove";
        JSONObject invalidJSON = movement.encode()
                .put(movementTypeKey, nonexistentMovementType);
        try {
            Movement.decode(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertTrue(e.getMessage().startsWith("Unknown movement type '"
                    + nonexistentMovementType + "'.\nMessage: "));
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testFactoryDecodeConstructorException() {
        // do not run for MovementTest as it always fails for the wrong reason
        if (this.getClass().isAssignableFrom(MovementTest.class))
            return;

        // trigger missing key exception to test for invocation target exception
        JSONObject invalidJSON = movement.encode();
        invalidJSON.remove(pieceIdKey);

        try {
            Movement.decode(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertTrue(e.getMessage().matches("JSONObject\\[\"pieceId\"\\] not found."));
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

}
