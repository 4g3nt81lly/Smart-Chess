package chess.model;

import chess.model.moves.Movable;
import chess.model.pieces.Piece;
import chess.model.player.Player;
import chess.model.exceptions.FormatException;
import chess.persistence.Codable;

import org.json.JSONObject;

import java.util.Objects;

/**
 * An abstract entity that is to be inherited by any object that plays a color-dependent role in
 * a Chess game, including but may not be limited to all concrete subtypes of
 * {@link Player}, {@link Piece}, and {@link Movable}.<p>
 * Any <i>colored</i> object possesses a binary color property, i.e. either White or Black,
 * represented by {@link Color}.
 * @see Color
 * */
public abstract class Colored implements Codable {

    /** A key string for serializing the color of the object. */
    public static final String colorKey = "color";

    /** The color value of the object. */
    protected final Color color;

    /** An anonymous instance of {@link Colored} of the color White. */
    public static final Colored White = new Colored(Color.White) {};

    /** An anonymous instance of {@link Colored} of the color Black. */
    public static final Colored Black = new Colored(Color.Black) {};

    /**
     * Constructs this {@link Colored} object with the given {@code color} value.
     * @param color The color of the object.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public Colored(Color color) {
        this.color = Objects.requireNonNull(color);
    }

    /**
     * Constructs this {@link Colored} object from a JSON object containing the following required
     * key-value mappings:
     * <pre>
     * {
     *     <b>{@value Colored#colorKey}:{@code "<color>"}</b>,
     *     ...
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <color>}:
     *         a string {@link Color} value, one of {@code "White"} or {@code "Black"};
     *     </li>
     * </ul>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If the key {@value colorKey} is missing or maps to a
     *                                non-string value.
     * @throws FormatException If {@code <color>} is invalid.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see Color#of(String)
     * */
    public Colored(JSONObject jsonObject) {
        this(Color.of(jsonObject.getString(colorKey)));
    }

    /**
     * Serializes this {@link Colored} object to a JSON object of the following format:
     * <pre>
     * {
     *     <b>{@value Colored#colorKey}:{@code "<color>"}</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <color>}:
     *         a string {@link Color} value, one of {@code "White"} or {@code "Black"};
     *     </li>
     * </ul>
     * @see Color
     * */
    @Override
    public JSONObject encode() {
        return new JSONObject()
                .put(colorKey, this.color.toString());
    }

    /** @return The color value of this object. */
    public final Color getColor() {
        return this.color;
    }

    /** @return The color value opposite to that of this object. */
    public final Color getOppositeColor() {
        return this.color.opposite();
    }

    /** @return A boolean value indicating whether this has the color value {@link Color#White}. */
    public boolean isWhite() {
        return this.color.isWhite();
    }

    /** @return A boolean value indicating whether this has the color value {@link Color#Black}. */
    public boolean isBlack() {
        return this.color.isBlack();
    }

    /**
     * @param color A color value.
     * @return A boolean value indicating whether this has the given color value.
     * */
    public boolean isColor(Color color) {
        return this.color == color;
    }

    /**
     * This {@link Colored} object is considered <b>allied to</b> another {@link Colored} object
     * <i>if and only if</i> this object has the same color value as the other object.<p>
     * This method is the logical negation of {@link #isOpponentTo(Colored)}.
     * @param colored Another {@link Colored} object.
     * @return A boolean value indicating whether this is allied to the given {@link Colored} object.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public boolean isAlliedTo(Colored colored) {
        return this.color == colored.color;
    }

    /**
     * This {@link Colored} object is considered <b>opposite to</b> another {@link Colored} object
     * <i>if and only if</i> this object has a different color value than the other object.<p>
     * This method is the logical negation of {@link #isAlliedTo(Colored)}.
     * @param colored Another {@link Colored} object.
     * @return A boolean value indicating whether this is opposite to the given {@link Colored} object.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public boolean isOpponentTo(Colored colored) {
        return !this.isAlliedTo(colored);
    }

}
