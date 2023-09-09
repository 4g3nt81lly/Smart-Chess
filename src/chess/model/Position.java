package chess.model;

import chess.model.exceptions.FormatException;
import chess.util.List;
import java.util.Optional;

import static chess.model.Color.*;

/**
 * A class that represents a position in the chessboard, defined by:
 * <ol>
 *     <li>a file letter, the horizontal position, represented by an integer index in the range
 *         [1, 8];</li>
 *     <li>a rank number, the vertical position, an integer in the range [1, 8].</li>
 * </ol>
 * This class defines all operations associated with positions.
 * */
public final class Position {

    /** The file characters as a list of strings. */
    public static final List<String> files = List.of("a", "b", "c", "d", "e", "f", "g", "h");

    /** The regex pattern by which a position notation is matched. */
    public static final String pattern = "^[a-h][1-8]$";

    public static final int Q1 = 1, Q2 = 2, Q3 = 3, Q4 = 4;

    public static final List<Integer> quadrants = List.of(Q1, Q2, Q3, Q4);

    /**
     * A convenient method to create a {@link Position} object using string notation, which is of
     * the pattern {@value pattern}.
     * @param notation A two-character string notation that describes a valid position in the
     *                 chessboard, matching {@value pattern}.
     * @return A {@link Position} object.
     * @throws FormatException If the {@code notation} is invalid, i.e. does not match the pattern.
     * */
    public static Position at(String notation) {
        notation = notation.toLowerCase();
        if (notation.matches(pattern)) {
            String fileLetter = String.valueOf(notation.charAt(0));
            int rankNumber = Character.getNumericValue(notation.charAt(1));
            return new Position(fileLetter, rankNumber);
        }
        throw new FormatException("Invalid position notation '" + notation + "'.");
    }

    /**
     * @return A boolean value indicating whether the given integer is within the range [1, 8].
     * */
    private static boolean isOutOfRange(int number) {
        return number < 1 || number > 8;
    }

    /** The file index of this position. */
    private int file;

    /** The rank number of this position. */
    private int rank;

    /**
     * Constructs a {@link Position} object from a file index and a rank number.
     * @param file The integer file index of the position in the range [1, 8].
     * @param rank The integer rank number of the position in the range [1, 8].
     * @throws IndexOutOfBoundsException If the given file index or rank number is out of the range.
     * */
    public Position(int file, int rank) {
        if (isOutOfRange(file) || isOutOfRange(rank)) {
            throw new IndexOutOfBoundsException();
        }
        this.file = file;
        this.rank = rank;
    }

    /**
     * Constructs a {@link Position} object from a file letter and a rank number.
     * @param file The file letter of the position as a single-letter string
     *             (<i>case-insensitive</i>).<br>
     *             It should be one of {@link #files}.
     * @param rank The integer rank number of the position in the range [1, 8].
     * @throws IndexOutOfBoundsException Rethrown from {@link #Position(int, int)}.
     * @throws IllegalArgumentException  If the given file string is invalid.
     * */
    public Position(String file, int rank) {
        this(1, rank);
        int index = files.indexOf(file.toLowerCase());
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        this.file = index + 1;
    }

    /**
     * @return A deep copy of this {@link Position} object.
     * */
    public Position copy() {
        return new Position(this.file, this.rank);
    }

    /**
     * Moves this position to the given file index and rank number.
     * @param file The integer file index of the new position in the range [1, 8].
     * @param rank The integer rank number of the new position in the range [1, 8].
     * @throws IndexOutOfBoundsException If the given file index or rank number is out of range.
     * */
    public void moveTo(int file, int rank) {
        if (isOutOfRange(file) || isOutOfRange(rank)) {
            throw new IndexOutOfBoundsException();
        }
        this.file = file;
        this.rank = rank;
    }

    /**
     * Returns a new position that is a number of squares to the left of this position from the
     * given colored object's point of view.
     * @param deltaFile The integer number of squares to step in the range [-7, 7].
     * @param colored   The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> left(int deltaFile, Colored colored) {
        return this.axialStep(deltaFile, 3, colored);
    }

    /**
     * Returns a new position that is a number of squares to the left of this position from White's
     * point of view, the default POV of the chessboard setup.
     * @param deltaFile The integer number of squares to step in the range [-7, 7].
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * */
    public Optional<Position> left(int deltaFile) {
        return this.axialStep(deltaFile, 3, Colored.White);
    }

    /**
     * Returns a new position that is a number of squares to the right of this position from the
     * given colored object's point of view.
     * @param deltaFile The integer number of squares to step in the range [-7, 7].
     * @param colored   The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> right(int deltaFile, Colored colored) {
        return this.axialStep(deltaFile, Q1, colored);
    }

    /**
     * Returns a new position that is a number of squares to the right of this position from White's
     * point of view, the default POV of the chessboard setup.
     * @param deltaFile The integer number of squares to step in the range [-7, 7].
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * */
    public Optional<Position> right(int deltaFile) {
        return this.axialStep(deltaFile, Q1);
    }

    /**
     * Returns a new position that is a number of squares forward from this position from the
     * given colored object's point of view.
     * @param deltaRank The integer number of squares to step in the range [-7, 7].
     * @param colored   The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> forward(int deltaRank, Colored colored) {
        return this.axialStep(deltaRank, Q2, colored);
    }

    /**
     * Returns a new position that is a number of squares forward from this position from White's
     * point of view, the default POV of the chessboard setup.
     * @param deltaRank The integer number of squares to step in the range [-7, 7].
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * */
    public Optional<Position> forward(int deltaRank) {
        return this.axialStep(deltaRank, Q2);
    }

    /**
     * Returns a new position that is a number of squares backward from this position from the
     * given colored object's point of view.
     * @param deltaRank The integer number of squares to step in the range [-7, 7].
     * @param colored   The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> backward(int deltaRank, Colored colored) {
        return this.axialStep(deltaRank, Q4, colored);
    }

    /**
     * Returns a new position that is a number of squares backward from this position from White's
     * point of view, the default POV of the chessboard setup.
     * @param deltaRank The integer number of squares to step in the range [-7, 7].
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * */
    public Optional<Position> backward(int deltaRank) {
        return this.axialStep(deltaRank, Q4);
    }

    /**
     * Returns a new position that is a number of steps away from this position in one of the
     * following directions:
     * <pre>
     *            Q2 (maxY)
     *                │
     *                │
     * Q3 (minX) ─────┼───── Q1 (maxX)
     *                │
     *                │
     *            Q4 (minY)</pre>
     * The directions in which the steps may take with respect to the origin (i.e. the current
     * position) are referred to as quadrants, as labeled above. The four quadrants, independently,
     * are as follows:
     * <pre>
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ x ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ x ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧
     * ‧ ‧ @ x x    ‧ ‧ @ ‧ ‧    x x @ ‧ ‧    ‧ ‧ @ ‧ ‧
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ x ‧ ‧
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ x ‧ ‧
     *    Q1          Q2          Q3          Q4</pre>
     * @param delta    The number of steps away from this position.
     * @param quadrant An integer ranging from 1-4 representing the quadrant (direction) of this
     *                 axial movement.
     * @param colored  The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws IllegalArgumentException If {@code quadrant} is invalid.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> axialStep(int delta, int quadrant, Colored colored) {
        int deltaFile = 0, deltaRank = 0;
        switch (quadrant) {
            case Q1, Q3 -> deltaFile = delta;
            case Q2, Q4 -> deltaRank = delta;
            default -> throw new IllegalArgumentException();
        }
        return this.radialStep(deltaFile, deltaRank, quadrant, colored);
    }

    /**
     * Returns a new position that is a number of steps away from this position in one of the
     * four directions set out in {@link #axialStep(int, int, Colored)}, with respect to White's
     * point of view, the default POV of the chessboard setup.
     * @param delta    The number of steps away from this position.
     * @param quadrant An integer ranging from 1-4 representing the quadrant (direction) of this
     *                 axial movement.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws IllegalArgumentException If {@code quadrant} is invalid.
     * */
    public Optional<Position> axialStep(int delta, int quadrant) {
        return this.axialStep(delta, quadrant, Colored.White);
    }

    /**
     * Returns a new position that is a number of steps away from this position in any linear path
     * that lies on or in-between the X and Y axes of a 2D Cartesian plane:
     * <pre>
     *        Y
     *        │
     *     Q2 │ Q1
     * X ─────┼───── X
     *     Q3 │ Q4
     *        │
     *        Y</pre>
     * For example, here is a group of linear paths that each lie in a different quadrant:
     * <pre>
     * ‧ ‧ ‧ ‧ x    x ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧
     * ‧ ‧ ‧ x ‧    ‧ x ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧
     * ‧ ‧ @ ‧ ‧    ‧ ‧ @ ‧ ‧    ‧ ‧ @ ‧ ‧    ‧ ‧ @ ‧ ‧
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    ‧ x ‧ ‧ ‧    ‧ ‧ ‧ x ‧
     * ‧ ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ ‧    x ‧ ‧ ‧ ‧    ‧ ‧ ‧ ‧ x
     *    Q1          Q2          Q3          Q4</pre>
     * @param deltaFile The number of horizontal steps away from this position.
     * @param deltaRank The number of vertical steps away from this position.
     * @param quadrant  An integer ranging from 1-4 representing the quadrant in which the linear
     *                  path resides.
     * @param colored   The colored object of the POV.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws IllegalArgumentException If {@code quadrant} is invalid.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public Optional<Position> radialStep(int deltaFile, int deltaRank,
                                         int quadrant, Colored colored) {
        if (!quadrants.contains(quadrant))
            throw new IllegalArgumentException();
        if (List.of(Q2, Q3).contains(quadrant))
            deltaFile *= -1;
        if (List.of(Q3, Q4).contains(quadrant))
            deltaRank *= -1;
        // flip coordinate system if the POV is Black
        if (colored.isBlack()) {
            deltaFile *= -1;
            deltaRank *= -1;
        }
        int newFile = this.file + deltaFile;
        int newRank = this.rank + deltaRank;
        try {
            return Optional.of(new Position(newFile, newRank));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a new position that is a number of steps away from this position in any linear path
     * that lies on or in-between the X and Y axes of a 2D Cartesian plane, with respect to White's
     * point of view, the default POV of the chessboard setup.
     * @param deltaFile The number of horizontal steps away from this position.
     * @param deltaRank The number of vertical steps away from this position.
     * @param quadrant  An integer ranging from 1-4 representing the quadrant in which the linear
     *                  path resides.
     * @return An optional new {@link Position} object, which is empty if the new position is
     *         outside the chessboard.
     * @throws IllegalArgumentException If {@code quadrant} is invalid.
     * */
    public Optional<Position> radialStep(int deltaFile, int deltaRank, int quadrant) {
        return this.radialStep(deltaFile, deltaRank, quadrant, Colored.White);
    }

    /**
     * Two positions are considered equal <i>if and only if</i> both positions are equal in their
     * file index <i>and</i> rank number.
     * @param object The position to compare to.
     * @return A boolean value indicating whether this position is equal to the given position.
     * */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Position position) {
            return (this.file == position.file) && (this.rank == position.rank);
        }
        if (object instanceof String notation) {
            return this.equals(Position.at(notation));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * this.file + this.rank;
    }

    /**
     * @return The file index of this position.
     * */
    public int getFile() {
        return this.file;
    }

    /**
     * @return The file letter of this position.
     * */
    public String getFileLetter() {
        return files.get(this.file - 1);
    }

    /**
     * @return The rank number of this position.
     * */
    public int getRank() {
        return this.rank;
    }

    /**
     * The last rank is 8 for White and 1 for Black.
     * @param color The color of the POV.
     * @return A boolean value indicating whether this position is at the last rank in the given
     *         color's point of view.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public boolean atLastRank(Color color) {
        return this.rank == (color.isWhite() ? 8 : 1);
    }

    /**
     * @return The color of the square at this position.
     * */
    public Color getSquareColor() {
        return ((this.rank % 2 == 0) == (this.file % 2 != 0)) ? White : Black;
    }

    /**
     * @return The two-character string notation of this position that matches the regex pattern
     *         {@value pattern}.
     * */
    @Override
    public String toString() {
        return this.getFileLetter() + this.rank;
    }

}
