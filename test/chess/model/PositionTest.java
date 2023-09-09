package chess.model;

import chess.model.exceptions.FormatException;
import chess.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static chess.model.Color.Black;
import static chess.model.Color.White;
import static chess.model.Position.*;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {

    private Position middle;

    private Position bottomLeft;

    private Position bottomRight;

    private Position topLeft;

    private Position topRight;

    @BeforeEach
    void initialize() {
        middle = Position.at("d4");
        bottomLeft = Position.at("a1");
        bottomRight = Position.at("h1");
        topLeft = Position.at("a8");
        topRight = Position.at("h8");
    }

    @Test
    void testConvenienceConstructorInvalidNotation() {
        List<String> invalidNotations = List.of(
                "i1", // invalid file letter
                "a0",           // invalid rank number
                "abc"           // ... it's just wrong
        );
        for (String invalidNotation : invalidNotations) {
            try {
                Position.at(invalidNotation);
                fail("should have thrown a FormatException");
            } catch (FormatException e) {
                assertEquals("Invalid position notation '" + invalidNotation + "'.",
                        e.getMessage());
            } catch (Exception e) {
                fail("was expecting FormatException, not " + e);
            }
        }
    }

    @Test
    void testConvenienceConstructorSuccess() {
        assertDoesNotThrow(() -> {
            Position position = Position.at("a1");
            assertEquals("a", position.getFileLetter());
            assertEquals(1, position.getFile());
            assertEquals(1, position.getRank());

            // case insensitivity
            position = Position.at("H5");
            assertEquals("h", position.getFileLetter());
            assertEquals(8, position.getFile());
            assertEquals(5, position.getRank());
        });
    }

    @Test
    @DisplayName("Position(int, int): invalid file index")
    void testConstructor1InvalidFile() {
        // lower bound: file == 0
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(0, 1));
        // upper bound: file == 9
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(9, 1));
        // negative number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(-3, 1));
        // positive number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(12, 1));
    }

    @Test
    @DisplayName("Position(int, int): invalid rank number")
    void testConstructor1InvalidRank() {
        // lower bound: rank == 0
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(1, 0));
        // upper bound: rank == 9
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(1, 9));
        // negative number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(1, -3));
        // positive number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position(1, 12));
    }

    @Test
    @DisplayName("Position(int, int): success")
    void testConstructor1Success() {
        assertDoesNotThrow(() -> {
            // lower bound: file == 1, rank == 1
            Position position = new Position(1, 1);
            assertEquals("a", position.getFileLetter());
            assertEquals(1, position.getFile());
            assertEquals(1, position.getRank());

            // upper bound: file == 8, rank == 8
            position = new Position(8, 8);
            assertEquals("h", position.getFileLetter());
            assertEquals(8, position.getFile());
            assertEquals(8, position.getRank());

            // ordinary case: file == 3, rank == 4
            position = new Position(3, 4);
            assertEquals("c", position.getFileLetter());
            assertEquals(3, position.getFile());
            assertEquals(4, position.getRank());
        });
    }

    @Test
    @DisplayName("Position(String, int): invalid file letter")
    void testConstructor2InvalidFileLetter() {
        List<String> invalidLetters = List.of(
                "i", // invalid file letter
                "!",           // not even a letter
                "  abc?"       // ... it's just wrong
        );
        for (String invalidLetter : invalidLetters) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Position(invalidLetter, 1));
        }
    }

    @Test
    @DisplayName("Position(String, int): invalid rank number")
    void testConstructor2InvalidRank() {
        // lower bound: rank == 0
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position("a", 0));
        // upper bound: rank == 9
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position("a", 9));
        // negative number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position("a", -3));
        // positive number
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Position("a", 12));
    }

    @Test
    @DisplayName("Position(String, int): success")
    void testConstructor2Success() {
        assertDoesNotThrow(() -> {
            Position position = new Position("a", 1);
            assertEquals("a", position.getFileLetter());
            assertEquals(1, position.getFile());
            assertEquals(1, position.getRank());

            // case insensitivity
            position = new Position("H", 5);
            assertEquals("h", position.getFileLetter());
            assertEquals(8, position.getFile());
            assertEquals(5, position.getRank());
        });
    }

    @Test
    void testCopy() {
        assertNotSame(middle, middle.copy());
    }

    @Test
    void testMoveToInvalidFile() {
        // lower bound: file == 0
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(0, 1));
        // upper bound: file == 9
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(9, 1));
        // negative number
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(-3, 1));
        // positive number
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(12, 1));
    }

    @Test
    void testMoveToInvalidRank() {
        // lower bound: rank == 0
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(1, 0));
        // upper bound: rank == 9
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(1, 9));
        // negative number
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(1, -3));
        // positive number
        assertThrows(IndexOutOfBoundsException.class,
                () -> middle.moveTo(1, 12));
    }

    @Test
    void testMoveToSuccess() {
        assertDoesNotThrow(() -> {
            // lower bound: file == 1, rank == 1
            middle.moveTo(1, 1);
            assertEquals("a", middle.getFileLetter());
            assertEquals(1, middle.getFile());
            assertEquals(1, middle.getRank());

            // upper bound: file == 8, rank == 8
            middle.moveTo(8, 8);
            assertEquals("h", middle.getFileLetter());
            assertEquals(8, middle.getFile());
            assertEquals(8, middle.getRank());

            // ordinary case: file == 3, rank == 4
            middle.moveTo(3, 4);
            assertEquals("c", middle.getFileLetter());
            assertEquals(3, middle.getFile());
            assertEquals(4, middle.getRank());
        });
    }

    // ----- LEFT -----

    @Test
    void testLeftZeroDelta() {
        assertEquals(topLeft,
                topLeft.left(0).orElseThrow());
        assertEquals(topRight,
                topRight.left(0).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.left(0).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.left(0).orElseThrow());
        assertEquals(middle,
                middle.left(0).orElseThrow());
        // Black POV
        assertEquals(topLeft,
                topLeft.left(0, Colored.Black).orElseThrow());
        assertEquals(topRight,
                topRight.left(0, Colored.Black).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.left(0, Colored.Black).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.left(0, Colored.Black).orElseThrow());
        assertEquals(middle,
                middle.left(0, Colored.Black).orElseThrow());
    }

    @Test
    void testLeftPositiveOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topLeft.left(1).isEmpty());
        assertTrue(middle.left(4).isEmpty());
        // Black POV
        assertTrue(topRight.left(1, Colored.Black).isEmpty());
        assertTrue(middle.left(5, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topLeft.left(3).isEmpty());
        assertTrue(middle.left(6).isEmpty());
        // Black POV
        assertTrue(topRight.left(3, Colored.Black).isEmpty());
        assertTrue(middle.left(8, Colored.Black).isEmpty());
    }

    @Test
    void testLeftNegativeOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topLeft.left(-8).isEmpty());
        assertTrue(middle.left(-5).isEmpty());
        // Black POV
        assertTrue(topRight.left(-8, Colored.Black).isEmpty());
        assertTrue(middle.left(-5, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topLeft.left(-10).isEmpty());
        assertTrue(middle.left(-7).isEmpty());
        // Black POV
        assertTrue(topRight.left(-10, Colored.Black).isEmpty());
        assertTrue(middle.left(-7, Colored.Black).isEmpty());
    }

    @Test
    void testLeftPositiveWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a8"),
                topRight.left(7).orElseThrow());
        assertEquals(Position.at("a4"),
                middle.left(3).orElseThrow());
        // Black POV
        assertEquals(Position.at("h8"),
                topLeft.left(7, Colored.Black).orElseThrow());
        assertEquals(Position.at("h4"),
                middle.left(4, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("e8"),
                topRight.left(3).orElseThrow());
        assertEquals(Position.at("b4"),
                middle.left(2).orElseThrow());
        // Black POV
        assertEquals(Position.at("d8"),
                topLeft.left(3, Colored.Black).orElseThrow());
        assertEquals(Position.at("f4"),
                middle.left(2, Colored.Black).orElseThrow());
    }

    @Test
    void testLeftNegativeWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("h8"),
                topLeft.left(-7).orElseThrow());
        assertEquals(Position.at("h4"),
                middle.left(-4).orElseThrow());
        // Black POV
        assertEquals(Position.at("a8"),
                topRight.left(-7, Colored.Black).orElseThrow());
        assertEquals(Position.at("a4"),
                middle.left(-3, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("e8"),
                topLeft.left(-4).orElseThrow());
        assertEquals(Position.at("f4"),
                middle.left(-2).orElseThrow());
        // Black POV
        assertEquals(Position.at("d8"),
                topRight.left(-4, Colored.Black).orElseThrow());
        assertEquals(Position.at("b4"),
                middle.left(-2, Colored.Black).orElseThrow());
    }

    // ----- RIGHT -----

    @Test
    void testRightZeroDelta() {
        assertEquals(topLeft,
                topLeft.right(0).orElseThrow());
        assertEquals(topRight,
                topRight.right(0).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.right(0).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.right(0).orElseThrow());
        assertEquals(middle,
                middle.right(0).orElseThrow());
        // Black POV
        assertEquals(topLeft,
                topLeft.right(0, Colored.Black).orElseThrow());
        assertEquals(topRight,
                topRight.right(0, Colored.Black).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.right(0, Colored.Black).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.right(0, Colored.Black).orElseThrow());
        assertEquals(middle,
                middle.right(0, Colored.Black).orElseThrow());
    }

    @Test
    void testRightPositiveOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topRight.right(1).isEmpty());
        assertTrue(middle.right(5).isEmpty());
        // Black POV
        assertTrue(topLeft.right(1, Colored.Black).isEmpty());
        assertTrue(middle.right(4, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topRight.right(3).isEmpty());
        assertTrue(middle.right(6).isEmpty());
        // Black POV
        assertTrue(topLeft.right(3, Colored.Black).isEmpty());
        assertTrue(middle.right(6, Colored.Black).isEmpty());
    }

    @Test
    void testRightNegativeOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topRight.right(-8).isEmpty());
        assertTrue(middle.right(-4).isEmpty());
        // Black POV
        assertTrue(topLeft.right(-8, Colored.Black).isEmpty());
        assertTrue(middle.right(-5, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topRight.right(-10).isEmpty());
        assertTrue(middle.right(-6).isEmpty());
        // Black POV
        assertTrue(topLeft.right(-10, Colored.Black).isEmpty());
        assertTrue(middle.right(-7, Colored.Black).isEmpty());
    }

    @Test
    void testRightPositiveWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("h8"),
                topLeft.right(7).orElseThrow());
        assertEquals(Position.at("h4"),
                middle.right(4).orElseThrow());
        // Black POV
        assertEquals(Position.at("a8"),
                topRight.right(7, Colored.Black).orElseThrow());
        assertEquals(Position.at("a4"),
                middle.right(3, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("d8"),
                topLeft.right(3).orElseThrow());
        assertEquals(Position.at("f4"),
                middle.right(2).orElseThrow());
        // Black POV
        assertEquals(Position.at("e8"),
                topRight.right(3, Colored.Black).orElseThrow());
        assertEquals(Position.at("b4"),
                middle.right(2, Colored.Black).orElseThrow());
    }

    @Test
    void testRightNegativeWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a8"),
                topRight.right(-7).orElseThrow());
        assertEquals(Position.at("a4"),
                middle.right(-3).orElseThrow());
        // Black POV
        assertEquals(Position.at("h8"),
                topLeft.right(-7, Colored.Black).orElseThrow());
        assertEquals(Position.at("h4"),
                middle.right(-4, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("d8"),
                topRight.right(-4).orElseThrow());
        assertEquals(Position.at("b4"),
                middle.right(-2).orElseThrow());
        // Black POV
        assertEquals(Position.at("e8"),
                topLeft.right(-4, Colored.Black).orElseThrow());
        assertEquals(Position.at("f4"),
                middle.right(-2, Colored.Black).orElseThrow());
    }

    // ----- FORWARD -----

    @Test
    void testForwardZeroDelta() {
        assertEquals(topLeft,
                topLeft.forward(0).orElseThrow());
        assertEquals(topRight,
                topRight.forward(0).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.forward(0).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.forward(0).orElseThrow());
        assertEquals(middle,
                middle.forward(0).orElseThrow());
        // Black POV
        assertEquals(topLeft,
                topLeft.forward(0, Colored.Black).orElseThrow());
        assertEquals(topRight,
                topRight.forward(0, Colored.Black).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.forward(0, Colored.Black).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.forward(0, Colored.Black).orElseThrow());
        assertEquals(middle,
                middle.forward(0, Colored.Black).orElseThrow());
    }

    @Test
    void testForwardPositiveOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topLeft.forward(1).isEmpty());
        assertTrue(middle.forward(5).isEmpty());
        // Black POV
        assertTrue(bottomLeft.forward(1, Colored.Black).isEmpty());
        assertTrue(middle.forward(4, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topLeft.forward(3).isEmpty());
        assertTrue(middle.forward(6).isEmpty());
        // Black POV
        assertTrue(bottomLeft.forward(3, Colored.Black).isEmpty());
        assertTrue(middle.forward(6, Colored.Black).isEmpty());
    }

    @Test
    void testForwardNegativeOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(topLeft.forward(-8).isEmpty());
        assertTrue(middle.forward(-4).isEmpty());
        // Black POV
        assertTrue(bottomLeft.forward(-8, Colored.Black).isEmpty());
        assertTrue(middle.forward(-5, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(topRight.forward(-10).isEmpty());
        assertTrue(middle.forward(-6).isEmpty());
        // Black POV
        assertTrue(bottomLeft.forward(-10, Colored.Black).isEmpty());
        assertTrue(middle.forward(-7, Colored.Black).isEmpty());
    }

    @Test
    void testForwardPositiveWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a8"),
                bottomLeft.forward(7).orElseThrow());
        assertEquals(Position.at("d8"),
                middle.forward(4).orElseThrow());
        // Black POV
        assertEquals(Position.at("a1"),
                topLeft.forward(7, Colored.Black).orElseThrow());
        assertEquals(Position.at("d1"),
                middle.forward(3, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("a4"),
                bottomLeft.forward(3).orElseThrow());
        assertEquals(Position.at("d6"),
                middle.forward(2).orElseThrow());
        // Black POV
        assertEquals(Position.at("a5"),
                topLeft.forward(3, Colored.Black).orElseThrow());
        assertEquals(Position.at("d2"),
                middle.forward(2, Colored.Black).orElseThrow());
    }

    @Test
    void testForwardNegativeWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a1"),
                topLeft.forward(-7).orElseThrow());
        assertEquals(Position.at("d1"),
                middle.forward(-3).orElseThrow());
        // Black POV
        assertEquals(Position.at("a8"),
                bottomLeft.forward(-7, Colored.Black).orElseThrow());
        assertEquals(Position.at("d8"),
                middle.forward(-4, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("a4"),
                topLeft.forward(-4).orElseThrow());
        assertEquals(Position.at("d2"),
                middle.forward(-2).orElseThrow());
        // Black POV
        assertEquals(Position.at("a5"),
                bottomLeft.forward(-4, Colored.Black).orElseThrow());
        assertEquals(Position.at("d6"),
                middle.forward(-2, Colored.Black).orElseThrow());
    }

    // ----- BACKWARD -----

    @Test
    void testBackwardZeroDelta() {
        assertEquals(topLeft,
                topLeft.backward(0).orElseThrow());
        assertEquals(topRight,
                topRight.backward(0).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.backward(0).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.backward(0).orElseThrow());
        assertEquals(middle,
                middle.backward(0).orElseThrow());
        // Black POV
        assertEquals(topLeft,
                topLeft.backward(0, Colored.Black).orElseThrow());
        assertEquals(topRight,
                topRight.backward(0, Colored.Black).orElseThrow());
        assertEquals(bottomLeft,
                bottomLeft.backward(0, Colored.Black).orElseThrow());
        assertEquals(bottomRight,
                bottomRight.backward(0, Colored.Black).orElseThrow());
        assertEquals(middle,
                middle.backward(0, Colored.Black).orElseThrow());
    }

    @Test
    void testBackwardPositiveOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(bottomLeft.backward(1).isEmpty());
        assertTrue(middle.backward(4).isEmpty());
        // Black POV
        assertTrue(topLeft.backward(1, Colored.Black).isEmpty());
        assertTrue(middle.backward(5, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(bottomLeft.backward(3).isEmpty());
        assertTrue(middle.backward(6).isEmpty());
        // Black POV
        assertTrue(topLeft.backward(3, Colored.Black).isEmpty());
        assertTrue(middle.backward(6, Colored.Black).isEmpty());
    }

    @Test
    void testBackwardNegativeOutOfRange() {
        // boundary: just out of range
        // White POV
        assertTrue(bottomLeft.backward(-8).isEmpty());
        assertTrue(middle.backward(-5).isEmpty());
        // Black POV
        assertTrue(topLeft.backward(-8, Colored.Black).isEmpty());
        assertTrue(middle.backward(-4, Colored.Black).isEmpty());

        // ordinary case
        // White POV
        assertTrue(bottomLeft.backward(-10).isEmpty());
        assertTrue(middle.backward(-7).isEmpty());
        // Black POV
        assertTrue(topLeft.backward(-10, Colored.Black).isEmpty());
        assertTrue(middle.backward(-6, Colored.Black).isEmpty());
    }

    @Test
    void testBackwardPositiveWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a1"),
                topLeft.backward(7).orElseThrow());
        assertEquals(Position.at("d1"),
                middle.backward(3).orElseThrow());
        // Black POV
        assertEquals(Position.at("a8"),
                bottomLeft.backward(7, Colored.Black).orElseThrow());
        assertEquals(Position.at("d8"),
                middle.backward(4, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("a5"),
                topLeft.backward(3).orElseThrow());
        assertEquals(Position.at("d2"),
                middle.backward(2).orElseThrow());
        // Black POV
        assertEquals(Position.at("a4"),
                bottomLeft.backward(3, Colored.Black).orElseThrow());
        assertEquals(Position.at("d6"),
                middle.backward(2, Colored.Black).orElseThrow());
    }

    @Test
    void testBackwardNegativeWithinRange() {
        // boundary: just within range
        // White POV
        assertEquals(Position.at("a8"),
                bottomLeft.backward(-7).orElseThrow());
        assertEquals(Position.at("d8"),
                middle.backward(-4).orElseThrow());
        // Black POV
        assertEquals(Position.at("a1"),
                topLeft.backward(-7, Colored.Black).orElseThrow());
        assertEquals(Position.at("d1"),
                middle.backward(-3, Colored.Black).orElseThrow());

        // ordinary case
        // White POV
        assertEquals(Position.at("a5"),
                bottomLeft.backward(-4).orElseThrow());
        assertEquals(Position.at("d6"),
                middle.backward(-2).orElseThrow());
        // Black POV
        assertEquals(Position.at("a4"),
                topLeft.backward(-4, Colored.Black).orElseThrow());
        assertEquals(Position.at("d2"),
                middle.backward(-2, Colored.Black).orElseThrow());
    }

    @Test
    void testAxialStepInvalidQuadrant() {
        // lower bound
        assertThrows(IllegalArgumentException.class,
                () -> middle.axialStep(1, 0));
        // upper bound
        assertThrows(IllegalArgumentException.class,
                () -> middle.axialStep(1, 5));
        // positive ordinary
        assertThrows(IllegalArgumentException.class,
                () -> middle.axialStep(1, 7));
        // negative ordinary
        assertThrows(IllegalArgumentException.class,
                () -> middle.axialStep(1, -2));
    }

    @Test
    void testAxialStepValidQuadrant() {
        assertEquals(middle.right(1),
                middle.axialStep(1, Q1));
        assertEquals(middle.forward(1),
                middle.axialStep(1, Q2));
        assertEquals(middle.left(1),
                middle.axialStep(1, Q3));
        assertEquals(middle.backward(1),
                middle.axialStep(1, Q4));
    }

    @Test
    void testRadialStepInvalidQuadrant() {
        // lower bound
        assertThrows(IllegalArgumentException.class,
                () -> middle.radialStep(1, 1, 0));
        // upper bound
        assertThrows(IllegalArgumentException.class,
                () -> middle.radialStep(1, 1, 5));
        // positive ordinary
        assertThrows(IllegalArgumentException.class,
                () -> middle.radialStep(1, 1, 7));
        // negative ordinary
        assertThrows(IllegalArgumentException.class,
                () -> middle.radialStep(1, 1, -2));
    }

    @Test
    void testRadialStepZeroStep() {
        for (int quadrant : quadrants) {
            assertEquals(middle,
                    middle.radialStep(0, 0, quadrant).orElseThrow());
        }
    }

    @Test
    void testRadialAxialStep() {
        assertEquals(middle.axialStep(1, Q1),
                middle.radialStep(1, 0, Q4));
        assertEquals(middle.axialStep(1, Q1),
                middle.radialStep(1, 0, Q1));

        assertEquals(middle.axialStep(1, Q2),
                middle.radialStep(0, 1, Q1));
        assertEquals(middle.axialStep(1, Q2),
                middle.radialStep(0, 1, Q2));

        assertEquals(middle.axialStep(1, Q3),
                middle.radialStep(1, 0, Q2));
        assertEquals(middle.axialStep(1, Q3),
                middle.radialStep(1, 0, Q3));

        assertEquals(middle.axialStep(1, Q4),
                middle.radialStep(0, 1, Q3));
        assertEquals(middle.axialStep(1, Q4),
                middle.radialStep(0, 1, Q4));
    }

    @Test
    void testRadialStepOutOfRange() {
        // reduced test cases

        // new file boundary: just out of range
        assertTrue(middle.radialStep(5, 1, Q1).isEmpty());
        //          ordinary: way out of range
        assertTrue(middle.radialStep(6, 1, Q1).isEmpty());
        // new rank boundary: out of range
        assertTrue(middle.radialStep(1, 5, Q1).isEmpty());
        //          ordinary: way out of range
        assertTrue(middle.radialStep(1, 6, Q1).isEmpty());
        // both file and rank out of range
        // boundary case:
        assertTrue(middle.radialStep(5, 5, Q1).isEmpty());
        // ordinary case:
        assertTrue(middle.radialStep(7, 7, Q1).isEmpty());
    }

    @Test
    void testRadialStepWithinRange() {
        // reduced test cases

        // boundary: just within range
        assertEquals(Position.at("h8"),
                middle.radialStep(4, 4, Q1).orElseThrow());
        assertEquals(Position.at("a8"),
                middle.radialStep(3, 4, Q2).orElseThrow());
        assertEquals(Position.at("a1"),
                middle.radialStep(3, 3, Q3).orElseThrow());
        assertEquals(Position.at("h1"),
                middle.radialStep(4, 3, Q4).orElseThrow());
        // ordinary
        assertEquals(Position.at("f7"),
                middle.radialStep(2, 3, Q1).orElseThrow());
        assertEquals(Position.at("b5"),
                middle.radialStep(2, 1, Q2).orElseThrow());
        assertEquals(Position.at("a3"),
                middle.radialStep(3, 1, Q3).orElseThrow());
        assertEquals(Position.at("f2"),
                middle.radialStep(2, 2, Q4).orElseThrow());
    }

    @Test
    void testNotEquals() {
        // different position
        assertNotEquals(topLeft, topRight);
        assertNotEquals(topLeft.hashCode(), topRight.hashCode());
        // different notation
        assertNotEquals(topLeft, "a4");
        // different (unrelated) type
        assertNotEquals(topLeft, 1);
        // null
        assertNotEquals(topLeft, null);
    }

    @Test
    void testEquals() {
        // same object
        assertEquals(middle, middle);
        // different object but same position
        Position anotherMiddle = Position.at("d4");
        assertEquals(anotherMiddle, middle);
        assertEquals(anotherMiddle.hashCode(), middle.hashCode());
        // different object and type but same position (notation)
        assertEquals(middle, "d4");
    }

    @Test
    void testAtLastRank() {
        // is at last rank for neither color
        for (Color color : Color.values()) {
            assertFalse(middle.atLastRank(color));
        }
        // is at rank for White but not for Black
        assertTrue(topLeft.atLastRank(White));
        assertFalse(topLeft.atLastRank(Black));
        // is at rank for Black but not for White
        assertFalse(bottomLeft.atLastRank(White));
        assertTrue(bottomLeft.atLastRank(Black));
    }

    @Test
    void testGetSquareColor() {
        Color currentExpectedColor = Black;
        for (int rank = 1; rank <= 8; rank++) {
            for (int file = 1; file <= 8; file++) {
                assertEquals(currentExpectedColor,
                        new Position(file, rank).getSquareColor());
                currentExpectedColor = currentExpectedColor.opposite();
            }
            currentExpectedColor = currentExpectedColor.opposite();
        }
    }

    @Test
    void testStringRepresentation() {
        assertEquals("d4", middle.toString());
        assertEquals("a8", topLeft.toString());
        assertEquals("h8", topRight.toString());
        assertEquals("a1", bottomLeft.toString());
        assertEquals("h1", bottomRight.toString());
    }

}
