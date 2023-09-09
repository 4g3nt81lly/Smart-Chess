package chess.model;

import chess.model.exceptions.FormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorTest {

    private Color white;

    private Color black;

    @BeforeEach
    void initialize() {
        white = Color.White;
        black = Color.Black;
    }

    @Test
    void testWhite() {
        assertTrue(white.isWhite());
        assertFalse(white.isBlack());
        assertEquals(black, white.opposite());
    }

    @Test
    void testBlack() {
        assertTrue(black.isBlack());
        assertFalse(black.isWhite());
        assertEquals(white, black.opposite());
    }

    @Test
    void testStaticConstructorInvalidColorName() {
        assertThrows(FormatException.class,
                () -> Color.of("gray"));
    }

    @Test
    void testStaticConstructorColorNameIncorrectCase() {
        assertThrows(FormatException.class,
                () -> Color.of("white"));
    }

    @Test
    void testStaticConstructorSuccess() {
        assertEquals(white, Color.of("White"));
        assertEquals(black, Color.of("Black"));

        // leading/trailing blank characters are stripped so the following should pass
        assertEquals(white, Color.of("  \tWhite\n "));
        assertEquals(black, Color.of(" \nBlack\t   "));
    }

}
