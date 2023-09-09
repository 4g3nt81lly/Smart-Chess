package chess.model;

import chess.model.exceptions.FormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static chess.model.Color.Black;
import static chess.model.Color.White;
import static chess.model.Colored.colorKey;
import static org.junit.jupiter.api.Assertions.*;

public class ColoredTest {

    protected Colored whiteObject;

    protected Colored blackObject;

    @BeforeEach
    protected void initialize() {
        whiteObject = Colored.White;
        blackObject = Colored.Black;
    }

    @Test
    void testColoredConstructor() {
        assertEquals(White, whiteObject.color);
        assertEquals(Black, blackObject.color);
        assertThrows(NullPointerException.class,
                () -> new Colored((Color) null) {});
    }

    @Test
    void testGetColor() {
        assertEquals(White, whiteObject.getColor());
        assertEquals(Black, blackObject.getColor());
    }

    @Test
    void testGetOppositeColor() {
        assertEquals(Black, whiteObject.getOppositeColor());
        assertEquals(White, blackObject.getOppositeColor());
    }

    @Test
    void testIsColor() {
        assertTrue(whiteObject.isWhite());
        assertFalse(whiteObject.isBlack());
        assertTrue(whiteObject.isColor(White));
        assertFalse(whiteObject.isColor(Black));

        assertFalse(blackObject.isWhite());
        assertTrue(blackObject.isBlack());
        assertFalse(blackObject.isColor(White));
        assertTrue(blackObject.isColor(Black));
    }

    @Test
    void testIsAlliedTo() {
        assertTrue(whiteObject.isAlliedTo(Colored.White));
        assertFalse(whiteObject.isAlliedTo(blackObject));

        assertTrue(blackObject.isAlliedTo(Colored.Black));
        assertFalse(blackObject.isAlliedTo(whiteObject));
    }

    @Test
    void testIsOpponentTo() {
        assertFalse(whiteObject.isOpponentTo(Colored.White));
        assertTrue(whiteObject.isOpponentTo(blackObject));

        assertFalse(blackObject.isOpponentTo(Colored.Black));
        assertTrue(blackObject.isOpponentTo(whiteObject));
    }

    @Test
    protected void testEncode() {
        JSONObject whiteEncoded = whiteObject.encode();
        assertTrue(whiteEncoded.has(colorKey));
        assertEquals(White.toString(), whiteEncoded.getString(colorKey));

        JSONObject blackEncoded = blackObject.encode();
        assertTrue(blackEncoded.has(colorKey));
        assertEquals(Black.toString(), blackEncoded.getString(colorKey));
    }

    @Test
    void testDecodeNullObject() {
        assertThrows(NullPointerException.class,
                () -> new Colored((JSONObject) null) {});
    }

    @Test
    void testDecodeMissingColorKey() {
        JSONObject missingColorKeyJSON = new JSONObject();
        assertThrows(JSONException.class,
                () -> new Colored(missingColorKeyJSON) {});
    }

    @Test
    void testDecodeNonStringColorKey() {
        JSONObject invalidColorKeyJSON = new JSONObject()
                .put(colorKey, 1);
        assertThrows(JSONException.class,
                () -> new Colored(invalidColorKeyJSON) {});
    }

    @Test
    void testDecodeInvalidColor() {
        JSONObject invalidColorJSON = new JSONObject()
                .put(colorKey, "gray");
        assertThrows(FormatException.class,
                () -> new Colored(invalidColorJSON) {});

        // test for case-sensitivity
        invalidColorJSON.put(colorKey, "white");
        assertThrows(FormatException.class,
                () -> new Colored(invalidColorJSON) {});
    }

    @Test
    protected void testDecodeSuccessful() {
        JSONObject validJSON = whiteObject.encode();

        assertDoesNotThrow(() -> {
            Colored decoded = new Colored(validJSON) {};
            assertEquals(White, decoded.color);
        });
    }

}
