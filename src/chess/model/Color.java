package chess.model;

import chess.model.exceptions.FormatException;

/**
 * A binary color enumeration: White or Black.
 * */
public enum Color {

    /** A value that represents <i>White</i>. */
    White,
    /** A value that represents <i>Black</i>. */
    Black;

    /**
     * Constructs a color from a string value.
     * @param color A string color value.
     * @return A {@link Color} value.
     * @throws FormatException If the {@code color} string is not a constant in the enumeration.
     * */
    public static Color of(String color) {
        try {
            return Enum.valueOf(Color.class, color.strip());
        } catch (IllegalArgumentException e) {
            throw new FormatException(e.getMessage());
        }
    }

    /**
     * @return A {@link Color} value opposite to the current value.
     * */
    public final Color opposite() {
        return (this == White) ? Black : White;
    }

    /**
     * @return A boolean value indicating whether this value is {@link #White}.
     * */
    public final boolean isWhite() {
        return this == White;
    }

    /**
     * @return A boolean value indicating whether this value is {@link #Black}.
     * */
    public final boolean isBlack() {
        return !this.isWhite();
    }

}
