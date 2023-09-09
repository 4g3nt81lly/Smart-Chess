package chess.model.moves;

import chess.model.Color;
import chess.model.Colored;
import chess.model.Position;
import org.json.JSONObject;
import chess.model.exceptions.FormatException;

import static chess.model.TextSymbols.rightArrow;

/**
 * An abstract, generic, and overarching representation of a Chess movement, defined by:
 * <ol>
 *     <li>The initial position of the piece.</li>
 *     <li>The final position of the piece.</li>
 *     <li>The initiator (White or Black) of the movement.</li>
 * </ol>
 * Upon initializing any subclass instances of the class, the instance keeps its own copy of
 * {@link Position} objects in an effort to preserve a non-referential description of the movement.
 * */
public abstract class Movable extends Colored {

    /** A key string for serializing the movable's initial position. */
    protected static final String initialPositionKey = "initialPosition";

    /** A key string for serializing the movable's final position. */
    protected static final String finalPositionKey = "finalPosition";

    /** The initial position of this movement. */
    protected final Position initialPosition;

    /** The final position of this movement. */
    protected Position finalPosition;

    /**
     * Constructs this {@link Movable} with an initial position, final position, and
     * the color of the initiator (described by {@link Color}).
     * @param initialPosition The initial position of the movement.
     * @param finalPosition   The final position of the movement.
     * @param color           The color of the movement's initiator (White or Black).
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    protected Movable(Position initialPosition, Position finalPosition, Color color) {
        super(color);
        // create a copy rather than a reference to keep the positions so that the move is reversible
        this.initialPosition = initialPosition.copy();
        this.finalPosition = finalPosition.copy();
    }

    /**
     * Constructs this {@link Movable} from a JSON object containing the following required
     * key-value mappings:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     <b>{@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},</b>
     *     ...
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <position>}:
     *         matches the pattern <code>^[a-hA-H][1-8]$</code>.
     *     </li>
     * </ul>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If any of the required keys is missing or maps to a
     *                                non-string value.
     * @throws FormatException Rethrown from {@link Colored#Colored(JSONObject)},
     *                                          or if either of {@code <color>} or
     *                                          {@code <position>} is invalid.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see Colored#Colored(JSONObject)
     * @see Position#at(String)
     * */
    protected Movable(JSONObject jsonObject) {
        super(jsonObject);
        this.initialPosition = Position.at(jsonObject.getString(initialPositionKey));
        this.finalPosition = Position.at(jsonObject.getString(finalPositionKey));
    }

    /**
     * Serializes this {@link Movable} to a JSON object of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     <b>{@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"}</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <position>}:
     *         the string representation of the {@link Position} object, matching the pattern
     *         <code>^[a-hA-H][1-8]$</code>.
     *     </li>
     * </ul>
     * @see Colored#encode()
     * @see Position#toString()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(initialPositionKey, this.initialPosition.toString())
                .put(finalPositionKey, this.finalPosition.toString());
    }

    /**
     * @return The initial position of this movement.
     * */
    public final Position getInitialPosition() {
        return this.initialPosition;
    }

    /**
     * @return The final position of this movement.
     * */
    public final Position getFinalPosition() {
        return this.finalPosition;
    }

    /**
     * The default string representation of this {@link Movable} is of the form:
     * <pre> {@literal <from>} &rarr;{@literal <to>} </pre> where:
     * <ul>
     *     <li>{@code <from>} is of the pattern: &#9823;<code>@^[a-hA-H][1-8]$</code>;</li>
     *     <li>{@code <to>} is of the pattern: <code>^[a-hA-H][1-8]$</code>.</li>
     * </ul>
     * @return The string representation of this movement.
     * */
    @Override
    public String toString() {
        return this.initialPosition + " " + rightArrow + " " + this.finalPosition;
    }

}
