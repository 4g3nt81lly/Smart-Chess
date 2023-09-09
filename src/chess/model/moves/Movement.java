package chess.model.moves;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Colored;
import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import chess.ui.Interface;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static chess.model.TextSymbols.rightArrow;

/**
 * An abstract and generic <i>internal</i> representation of a movement.<p>
 * The concrete subclasses of this abstract class are primarily used to represent:
 * <ol>
 *     <li>a <i>legal</i> candidate movement for a Chess piece used internally to validate
 *     player-initiated movements;</li>
 *     <li>a <i>reversible (undo)</i> and <i>reproducible (redo)</i> transformation that leads to
 *         a certain state of the Chess game.</li>
 * </ol>
 * In addition to the information defined in {@link Movable}, this entity captures the following
 * additional information to describes a movement:
 * <ol>
 *     <li>The target piece of the movement.</li>
 *     <li>Whether the movement will check the opponent.</li>
 * </ol>
 * All concrete subclasses of this abstract class should <b>only</b> be instantiated by subclasses
 * of {@link Piece}, maintained and managed by {@link ChessGame}, and used by built-in user
 * interfaces (i.e. any subtype of {@link Interface}).
 * */
public abstract class Movement extends Movable {

    /** A key string for serializing the movement type. */
    protected static final String movementTypeKey = "type";

    /** A key string for serializing the identifier string of the movement's target piece. */
    protected static final String pieceIdKey = "pieceId";

    /** A key string for serializing {@link #willCheckOpponent}. */
    protected static final String willCheckOpponentKey = "willCheckOpponent";

    /** The qualifier of the concrete movement types used for dynamic instantiation. */
    private static final String moveTypeQualifier = "chess.model.moves.";

    /**
     * Constructs an appropriate concrete subtype of {@link Movement} from a JSON object containing
     * the following required key-value mappings:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     {@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},
     *     <b>{@value movementTypeKey}:{@code "<type>"},
     *     {@value pieceIdKey}:{@code "<id>"},
     *     {@value willCheckOpponentKey}:{@code <true/false>},
     *     ...</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         one of the class names of all the concrete subtypes of {@link Movement}
     *         (returned from {@code this.getClass().getSimpleName()});
     *     </li>
     *     <li>{@code <id>}:
     *         the Version 4 UUID string of the movement's target piece;
     *     </li>
     * </ul>
     * <b>Note:</b> Specific movement types such as {@link Capture} and {@link Castling} require
     * additional key-value mappings which are just as compulsory as those listed above.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @param chessboard The chessboard from which the pieces are to be referenced.<br>
     *                   This will be passed into subtype constructors for validation purposes.
     * @return An instance of {@link Movement} decoded from the given JSON source.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException If one of the following scenarios occurs:
     *                         <ol>
     *                             <li>failed to resolve a piece with {@code <id>} in
     *                                 {@code chessboard};</li>
     *                             <li>{@code type} is invalid;</li>
     *                             <li>{@link TwoSquareAdvance} or {@link EnPassantCapture} got a
     *                                 non-Pawn target piece;</li>
     *                             <li>{@link Castling} got a non-King target piece or a non-Rook
     *                                 piece for its target Rook;</li>
     *                             <li>any other exception-al condition outlined in the subtypes is
     *                                 satisfied.</li>
     *                         </ol>
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see #encode()
     * @see RegularMove#RegularMove(JSONObject, Chessboard)
     * @see TwoSquareAdvance#TwoSquareAdvance(JSONObject, Chessboard)
     * @see Capture#Capture(JSONObject, Chessboard)
     * @see EnPassantCapture#EnPassantCapture(JSONObject, Chessboard)
     * @see Castling#Castling(JSONObject, Chessboard)
     * */
    public static Movement decode(JSONObject jsonObject, Chessboard chessboard) {
        String movementType = jsonObject.getString(movementTypeKey);
        try {
            // throws ClassNotFoundException
            Class<?> movementClass = Class.forName(moveTypeQualifier + movementType);
            // throws NoSuchMethodException
            Constructor<?> movementConstructor = movementClass.getDeclaredConstructor(
                    JSONObject.class, Chessboard.class
            );
            // throws ClassCastException
            return (Movement) movementConstructor.newInstance(jsonObject, chessboard);
        } catch (InvocationTargetException e) {
            throw new FormatException(e.getCause().getMessage());
        } catch (ReflectiveOperationException e) {
            throw new FormatException("Unknown movement type '" + movementType + "'."
                    + "\nMessage: " + e.getMessage());
        }
    }

    /** The Version 4 UUID string of the target piece of this movement. */
    private final String pieceId;

    /**
     * A boolean flag indicating whether this movement will lead to a state in which the opponent
     * is in check.
     * <blockquote>
     *     This flag is managed externally, hence it should always be properly updated by
     *     subclasses of {@link Piece} via {@link #setWillCheckOpponent(boolean)} to
     *     indicate whether the movement checks the opponent, for this is an integral part of the
     *     game state.
     * </blockquote>
     * By default, this flag initializes to {@code false}.
     * */
    private boolean willCheckOpponent = false;

    /**
     * Constructs this movement for a subject piece and a final position.
     * @param piece         The target piece of the movement.
     * @param finalPosition The final position of the movement.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Movement(Piece piece, Position finalPosition) {
        super(piece.getPosition(), finalPosition, piece.getColor());
        this.pieceId = piece.getIdentifier();
    }

    /**
     * Constructs this {@link Movement} from a JSON source containing the following required
     * key-value mappings:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     {@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},
     *     <b>{@value movementTypeKey}:{@code "<type>"},
     *     {@value pieceIdKey}:{@code "<id>"},
     *     {@value willCheckOpponentKey}:{@code <true/false>},
     *     ...</b>
     * }</pre>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @param chessboard The chessboard from which the target piece is resolved, which is used
     *                   to validate the target piece.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link Movable#Movable(JSONObject)}, or if either of
     *                         the following scenarios occurs:
     *                         <ol>
     *                             <li>If no piece with {@code <id>} is found on
     *                                 {@code chessboard};</li>
     *                             <li>If the piece referenced by {@code <id>} belongs to the
     *                                 opponent.</li>
     *                         </ol>
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see Movable#Movable(JSONObject)
     * */
    protected Movement(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject);
        String pieceId = jsonObject.getString(pieceIdKey);
        // piece validation
        Piece piece = chessboard.getPieceWith(pieceId)
                .orElseThrow(() -> new FormatException("No piece found with ID '" + pieceId + "'."));
        if (piece.isAlliedTo(this)) {
            this.pieceId = pieceId;
            this.willCheckOpponent = jsonObject.getBoolean(willCheckOpponentKey);
        } else {
            throw new FormatException("Incompatible: " + this.color + "'s move and "
                    + piece.getColor() + "'s piece.");
        }
    }

    /**
     * Serializes this {@link Movement} to a JSON object of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     {@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},
     *     <b>{@value movementTypeKey}:{@code "<type>"},
     *     {@value pieceIdKey}:{@code "<id>"},
     *     {@value willCheckOpponentKey}: {@code <true/false>}</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         the class name of this movement returned from {@code this.getClass().getSimpleName()};
     *     </li>
     *     <li>{@code <id>}:
     *         the Version 4 UUID string of the target piece of this movement.
     *     </li>
     * </ul>
     * @see Movable#encode()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(movementTypeKey, this.getClass().getSimpleName())
                .put(pieceIdKey, this.pieceId)
                .put(willCheckOpponentKey, this.willCheckOpponent);
    }

    /**
     * Executes/reproduces the receiver movement on a given chessboard and increments the move
     * count of the target piece.<p>
     * The default template method only increments the target piece's move count and returns 0
     * points.<p>
     * It should be overridden by the concrete subclasses of this abstract class to specify
     * movement-specific undo/reverse logic and the number of points associated with this movement.
     * @param chessboard The chessboard on which the movement is executed.
     * @return 0 points by default.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * @throws java.util.NoSuchElementException If no piece with {@link #pieceId} is found on
     *                                          {@code chessboard}.
     * */
    public int execute(Chessboard chessboard) {
        this.getPieceFrom(chessboard).incrementMoveCount();
        return 0;
    }

    /**
     * Undoes/reverses the receiver movement on a given chessboard and decrements the move
     * count of the target piece.<p>
     * The default template method only decrements the target piece's move count and returns 0
     * points.<p>
     * It should be overridden by the concrete subclasses of this abstract class to specify
     * movement-specific undo/reverse logic and the number of points associated with this movement.
     * @param chessboard The chessboard on which the movement is undone/reversed.
     * @return 0 points by default.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * @throws java.util.NoSuchElementException If no piece with {@link #pieceId} is found on
     *                                          {@code chessboard}.
     * */
    public int undo(Chessboard chessboard) {
        this.getPieceFrom(chessboard).decrementMoveCount();
        return 0;
    }

    /**
     * @param chessboard The chessboard.
     * @return The target piece of this movement on the given chessboard.
     * @throws java.util.NoSuchElementException If no piece with {@link #pieceId} is found on
     *                                          {@code chessboard}.
     * */
    public final Piece getPieceFrom(Chessboard chessboard) {
        return chessboard.getPieceWith(this.pieceId).orElseThrow();
    }

    /**
     * @return {@link #willCheckOpponent}.
     * */
    public final boolean willCheckOpponent() {
        return this.willCheckOpponent;
    }

    /**
     * @param newValue A new boolean value to which the {@link #willCheckOpponent} is set.
     * */
    public final void setWillCheckOpponent(boolean newValue) {
        this.willCheckOpponent = newValue;
    }

    /**
     * Returns a movement-specific descriptor text.<p>
     * This method is to be overridden by the concrete subclasses of this abstract class to
     * generate a string representation that is specific to the movement.
     * @return A movement-specific descriptor text that is inserted as part of the movement's
     *         string representation.
     * */
    protected String getDescriptor() {
        return "";
    }

    /**
     * Returns a movement-specific destination string.<p>
     * This method can be overridden by concrete subclasses of this abstract class to generate
     * a string representations that is specific to the movement.
     * @return A movement-specific string representation of the movement's final position that is
     *         to be inserted as part of the movement's string representation.
     * */
    protected String getDestinationString(Chessboard chessboard) {
        return this.finalPosition.toString();
    }

    /**
     * The string representation of this movement is of the form:
     * <pre> {@literal <from>}{@literal <descriptor>}&rarr;{@literal <to>} </pre> where:
     * <ul>
     *     <li>{@code <from>} is in the form: &#9823;<code>@{@literal <position>}</code>;</li>
     *     <li>{@code <descriptor>} is the movement-specific descriptor text returned from
     *         {@link #getDescriptor()};</li>
     *     <li>{@code <to>} is the movement-specific destination representation returned from
     *         {@link #getDestinationString(Chessboard)}.</li>
     * </ul>
     * @param chessboard The chessboard from which the target piece is resolved.
     * @return The string representation of this movement.
     * */
    public final String toString(Chessboard chessboard) {
        String moveDescriptor = this.getDescriptor();
        String from = this.getPieceFrom(chessboard).getCharacterSymbol()
                + "@" + this.initialPosition.toString();
        String to = this.getDestinationString(chessboard);
        return String.format("%s %s%s %s", from, moveDescriptor, rightArrow, to);
    }

    /**
     * @return The Version 4 UUID string of the target piece of this movement.
     * */
    public String getPieceId() {
        return this.pieceId;
    }

}
