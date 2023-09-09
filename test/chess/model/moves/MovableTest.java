package chess.model.moves;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.model.ColoredTest;
import chess.model.Position;
import chess.model.exceptions.FormatException;
import org.json.JSONException;
import org.json.JSONObject;

import static chess.model.Color.Black;
import static chess.model.Color.White;
import static chess.model.TextSymbols.rightArrow;
import static chess.model.moves.Movable.finalPositionKey;
import static chess.model.moves.Movable.initialPositionKey;
import static org.junit.jupiter.api.Assertions.*;

public class MovableTest extends ColoredTest {

    protected Position initialPosition;

    protected Position finalPosition;

    protected Movable movable;

    @Override
    @BeforeEach
    protected void initialize() {
        initialPosition = Position.at("a1");
        finalPosition = Position.at("a2");
        movable = new Movable(initialPosition, finalPosition, White) {};
        // Colored tests
        whiteObject = movable;
        blackObject = new Movable(initialPosition, finalPosition, Black) {};
    }

    @Test
    void testMovableConstructor() {
        assertNotSame(initialPosition, movable.getInitialPosition());
        assertEquals(movable.getInitialPosition(), initialPosition);

        assertNotSame(finalPosition, movable.getFinalPosition());
        assertEquals(movable.getFinalPosition(), finalPosition);

        // null initial position
        assertThrows(NullPointerException.class,
                () -> new Movable(null, finalPosition, White) {});
        // null final position
        assertThrows(NullPointerException.class,
                () -> new Movable(initialPosition, null, White) {});
        // null color
        assertThrows(NullPointerException.class,
                () -> new Movable(initialPosition, finalPosition, null) {});
    }

    @Test
    protected void testStringRepresentation() {
        String expected = initialPosition.toString() + " " + rightArrow + " " + finalPosition.toString();
        assertEquals(expected, movable.toString());
    }

    @Override
    @Test
    protected void testEncode() {
        super.testEncode();
        JSONObject movableEncoded = movable.encode();

        assertTrue(movableEncoded.has(initialPositionKey));
        assertEquals(initialPosition.toString(), movableEncoded.getString(initialPositionKey));

        assertTrue(movableEncoded.has(finalPositionKey));
        assertEquals(finalPosition.toString(), movableEncoded.getString(finalPositionKey));
    }

    @Test
    void testDecodeMissingInitialPosition() {
        JSONObject invalidJSON = movable.encode();
        invalidJSON.remove(initialPositionKey);

        assertThrows(JSONException.class,
                () -> new Movable(invalidJSON) {});
    }

    @Test
    void testDecodeMissingFinalPosition() {
        JSONObject invalidJSON = movable.encode();
        invalidJSON.remove(finalPositionKey);

        assertThrows(JSONException.class,
                () -> new Movable(invalidJSON) {});
    }

    @Test
    void testDecodeInvalidInitialPosition() {
        JSONObject invalidJSON = movable.encode()
                .put(initialPositionKey, "i1");

        assertThrows(FormatException.class,
                () -> new Movable(invalidJSON) {});
    }

    @Test
    void testDecodeInvalidFinalPosition() {
        JSONObject invalidJSON = movable.encode()
                .put(finalPositionKey, "i1");

        assertThrows(FormatException.class,
                () -> new Movable(invalidJSON) {});
    }

    @Test
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();
        JSONObject encoded = movable.encode();

        assertDoesNotThrow(() -> {
            Movable decoded = new Movable(encoded) {};

            assertEquals(decoded.getInitialPosition(), initialPosition);
            assertEquals(decoded.getFinalPosition(), finalPosition);
        });
    }

}
