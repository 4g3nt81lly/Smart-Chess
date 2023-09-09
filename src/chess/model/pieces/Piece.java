package chess.model.pieces;

import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Colored;
import chess.model.Position;
import chess.model.moves.Movement;
import chess.model.exceptions.FormatException;
import chess.util.List;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.*;

import static chess.model.TextSymbols.*;

/**
 * An abstract and generic representation of a Chess piece, defined by:
 * <ol>
 *     <li>The {@link Color} (White or Black) to which the piece belongs.</li>
 *     <li>The position of the piece in the chessboard.</li>
 * </ol>
 * All Chess pieces:
 * <ol>
 *     <li>have a globally-unique identifier.</li>
 *     <li>maintain its move count.</li>
 * </ol>
 * In addition, this class also serves to dynamically instantiate specific types of Chess pieces
 * and define the generic behaviors of all Chess pieces.
 * */
public sealed abstract class Piece extends Colored
        permits Pawn, Rook, Knight, Bishop, Queen, King {

    /** The key string for serializing the type of the piece. */
    public static final String pieceTypeKey = "piece";

    /** The key string for serializing the position of the piece. */
    public static final String positionKey = "position";

    /** The key string for serializing the identifier of the piece. */
    public static final String idKey = "id";

    /** The key string for serializing the move count of the piece. */
    public static final String moveCountKey = "moveCount";

    /** The qualifier of the concrete piece types used for dynamic instantiation. */
    private static final String pieceTypeQualifier = "chess.model.pieces.";

    /**
     * Constructs a Chess piece of the given name, color, and position.
     * @param name     The class name of the piece.<br>
     *                 Available options: {@code "Pawn"}, {@code "Rook"}, {@code "Knight"},
     *                                    {@code "Bishop"}, {@code "Queen"}, and {@code "King"}.
     * @param color    The color of the piece.
     * @param position The position of the piece.
     * @return An optional new piece, which is empty if {@code name} is invalid.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see Piece#Piece(Color, Position)
     * */
    public static Optional<Piece> of(String name, Color color, Position position) {
        try {
            // throws ClassNotFoundException
            Class<?> pieceClass = Class.forName(pieceTypeQualifier
                    + Objects.requireNonNull(name));
            // throws NoSuchMethodException
            Constructor<?> pieceConstructor = pieceClass.getConstructor(Color.class, Position.class);
            // throws ClassCastException
            Piece newPiece = (Piece) pieceConstructor.newInstance(
                    Objects.requireNonNull(color),
                    Objects.requireNonNull(position)
            );
            return Optional.of(newPiece);
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

    /**
     * Constructs an appropriate concrete subtype of {@link Piece} from a JSON object containing
     * the following required key-value mappings:
     * <pre>
     * {
     *     {@value #colorKey}:{@code "<color>"},
     *     <b>{@value pieceTypeKey}:{@code "<type>"},
     *     {@value positionKey}:{@code "<position>"},
     *     {@value idKey}:{@code "<id>"},
     *     {@value moveCountKey}:{@code <count>},
     *     ...</b>
     * }</pre>
     * See {@link #Piece(JSONObject)} for details.<p>
     * <b>Note:</b> {@link Pawn} requires an additional key-value mapping, see
     * {@link Pawn#encode()} for details.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @return An instance {@link Piece} decoded from the given JSON source.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown dynamically from the constructors, or if {@code <type>} is
     *                         invalid.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see #encode()
     * @see #Piece(JSONObject)
     * */
    public static Piece decode(JSONObject jsonObject) {
        String pieceType = jsonObject.getString(pieceTypeKey);
        try {
            // throws ClassNotFoundException
            Class<?> pieceClass = Class.forName(pieceTypeQualifier + pieceType);
            // throws NoSuchMethodException
            Constructor<?> pieceConstructor = pieceClass.getDeclaredConstructor(JSONObject.class);
            // throws ClassCastException
            return (Piece) pieceConstructor.newInstance(jsonObject);
        } catch (ReflectiveOperationException e) {
            throw new FormatException("Unknown piece '" + pieceType + "'.");
        }
    }

    /** The Version 4 UUID string that uniquely identifies the piece. */
    private final String identifier;

    /**
     * The position of the piece.<p>
     * <b>Note:</b> This is a <b>read-only</b> field. To modify it (i.e. move the piece), use the
     * {@code public} method {@link #moveTo(Position)} instead.
     * */
    protected final Position position;

    /** The move count of the piece, i.e. the number of times the piece is moved, initially at 0,
     * since the start of a Chess game. */
    private int moveCount;

    /**
     * Constructs this {@link Piece} with a color and a position at which it is placed.<p>
     * Upon instantiating, the piece's move count initializes to 0 and a Version 4 UUID that
     * uniquely identifies the piece is generated.
     * @param color    The color of the piece.
     * @param position The position of the piece on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Piece(Color color, Position position) {
        super(color);
        this.identifier = UUID.randomUUID().toString();
        this.position = position.copy();
        this.moveCount = 0;
    }

    /**
     * Constructs this {@link Piece} from a JSON object containing the following required key-value
     * mappings:
     * <pre>
     * {
     *     {@value #colorKey}:{@code "<color>"},
     *     <b>{@value pieceTypeKey}:{@code "<type>"},
     *     {@value positionKey}:{@code "<position>"},
     *     {@value idKey}:{@code "<id>"},
     *     {@value moveCountKey}:{@code <count>},</b>
     *     ...
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         the class name of this piece returned from {@code this.getClass().getSimpleName()};
     *     </li>
     *     <li>{@code <position>}:
     *         the string representation of the {@link Position} object, matching the pattern
     *         <code>^[a-hA-H][1-8]$</code>;
     *     </li>
     *     <li>{@code <id>}:
     *         the Version 4 UUID string of this piece;
     *     </li>
     *     <li>{@code <count>}:
     *         an integer number of this piece's move count.
     *     </li>
     * </ul>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link Colored#Colored(JSONObject)}, or if
     *                         {@code <position>} is invalid.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see Colored#Colored(JSONObject)
     * @see Position#at(String)
     * */
    protected Piece(JSONObject jsonObject) {
        super(jsonObject);
        this.identifier = jsonObject.getString(idKey);
        this.position = Position.at(jsonObject.getString(positionKey));
        this.moveCount = jsonObject.getInt(moveCountKey);
    }

    /**
     * Serializes this {@link Piece} to a JSON object of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     <b>{@value pieceTypeKey}:{@code "<type>"},
     *     {@value positionKey}:{@code "<position>"},
     *     {@value idKey}:{@code "<id>"},
     *     {@value moveCountKey}:{@code <count>}</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         the class name of this piece returned from {@code this.getClass().getSimpleName()};
     *     </li>
     *     <li>{@code <position>}:
     *         the string representation of the {@link Position} object, matching the pattern
     *         <code>^[a-hA-H][1-8]$</code>;
     *     </li>
     *     <li>{@code <id>}:
     *         the Version 4 UUID string of this piece;
     *     </li>
     *     <li>{@code <count>}:
     *         an integer number of this piece's move count.
     *     </li>
     * </ul>
     * @see Colored#encode()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(pieceTypeKey, this.getClass().getSimpleName())
                .put(positionKey, this.position.toString())
                .put(idKey, this.identifier)
                .put(moveCountKey, this.moveCount);
    }

    /**
     * Creates a deep copy of this piece via JSON serialization.
     * @return A copy of this piece.
     * */
    public Piece copy() {
        return Piece.decode(this.encode());
    }

    /**
     * Returns the character symbol that corresponds to this piece.
     * <table style="table-layout: fixed; width: 175px;">
     *     <caption>Piece Character Symbols</caption>
     *     <tr>
     *         <th>Piece</th> <th>White</th> <th>Black</th>
     *     </tr>
     *     <tr>
     *         <td>{@link Pawn}</td>
     *         <td>&#9817; ({@code \}{@code u2659})</td>
     *         <td>&#9823; ({@code \}{@code u265f})</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Rook}</td>
     *         <td>&#9814; ({@code \}{@code u2656})</td>
     *         <td>&#9820; ({@code \}{@code u265c})</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Knight}</td>
     *         <td>&#9816; ({@code \}{@code u2658})</td>
     *         <td>&#9822; ({@code \}{@code u265e})</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Bishop}</td>
     *         <td>&#9815; ({@code \}{@code u2657})</td>
     *         <td>&#9821; ({@code \}{@code u265d})</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Queen}</td>
     *         <td>&#9813; ({@code \}{@code u2655})</td>
     *         <td>&#9819; ({@code \}{@code u265b})</td>
     *     </tr>
     *     <tr>
     *         <td>{@link King}</td>
     *         <td>&#9812; ({@code \}{@code u2654})</td>
     *         <td>&#9820; ({@code \}{@code u265a})</td>
     *     </tr>
     * </table>
     * @param solid A boolean flag indicating whether this method should always return the Black
     *              version of the character symbol (the ones with solid glyph) regardless of the
     *              color of this piece.
     * @return The unicode code point of the character symbol of this piece.
     * */
    public final char getCharacterSymbol(boolean solid) {
        Map<Class<? extends Piece>, List<Character>> textSymbols = Map.of(
                Pawn.class, List.of(whitePawn, blackPawn),
                Rook.class, List.of(whiteRook, blackRook),
                Knight.class, List.of(whiteKnight, blackKnight),
                Bishop.class, List.of(whiteBishop, blackBishop),
                Queen.class, List.of(whiteQueen, blackQueen),
                King.class, List.of(whiteKing, blackKing)
        );
        int index = (solid || this.isBlack()) ? 1 : 0;
        return textSymbols.get(this.getClass()).get(index);
    }

    /**
     * An overloaded version of {@link #getCharacterSymbol(boolean)} that sets {@code solid} to
     * {@code false}.
     * @return The unicode code point of the character symbol of this piece.
     * */
    public final char getCharacterSymbol() {
        return this.getCharacterSymbol(false);
    }

    /**
     * Returns the number of points associated with this piece.
     * <table style="table-layout: fixed; width: 100px;">
     *     <caption>Piece Values</caption>
     *     <tr>
     *         <th>Piece</th> <th>Points</th>
     *     </tr>
     *     <tr>
     *         <td>{@link Pawn}</td> <td>1</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Rook}</td> <td>5</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Knight}</td> <td>3</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Bishop}</td> <td>3</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Queen}</td> <td>9</td>
     *     </tr>
     *     <tr>
     *         <td>{@link King}</td> <td>0</td>
     *     </tr>
     * </table>
     * @return The number of points of this piece according to the table above.
     * */
    public final int getPoints() {
        Map<Class<? extends Piece>, Integer> points = Map.of(
                Pawn.class, 1,
                Rook.class, 5,
                Knight.class, 3,
                Bishop.class, 3,
                Queen.class, 9,
                King.class, 0
        );
        return points.get(this.getClass());
    }

    /**
     * Computes and returns a list of candidate movements according to the rules of this piece
     * and the current configuration of the chessboard.<p>
     * The movements returned from this method may be <b>safe</b> or <b>unsafe</b>, that is, the
     * movements may or may not render their own King in check.
     * @param chessboard The enclosing chessboard.
     * @return A list of safe and unsafe candidate movements.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    public abstract List<Movement> getCandidateMoves(Chessboard chessboard);

    /**
     * Computes a list of <b>safe</b> (hence legal) candidate movements according to the rules of
     * this piece and the current configuration of the chessboard, and updates the "checking"
     * statuses of all candidate movements, i.e. updates the flag
     * {@link Movement#willCheckOpponent()} to authentically reflect whether the movements would
     * check the opponent.<p>
     * The process is as follows:
     * <ol>
     *     <li>candidate movements are executed to obtain hypothetical states of the chessboard;</li>
     *     <li>{@link Chessboard#isInCheck(Color)} is invoked to determine:
     *         <ol>
     *             <li>if the allied King is in check;</li>
     *             <li>if the opponent is in check.</li>
     *         </ol>
     *     </li>
     *     <li>movements that left their own King in check are removed;</li>
     *     <li>movements that check the opponent will have their flags updated to {@code true};</li>
     *     <li>movements are undone and the chessboard is restored to its original state;</li>
     *     <li>if this is King, add Castling moves to the list.</li>
     * </ol>
     * @param chessboard The enclosing chessboard.
     * @return A list of safe candidate movements only.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    public final List<Movement> getLegalMoves(Chessboard chessboard) {
        List<Movement> allMoves = this.getCandidateMoves(chessboard);
        Iterator<Movement> iterator = allMoves.iterator();
        while (iterator.hasNext()) {
            Movement movement = iterator.next();
            // execute the move to obtain a hypothetical state
            chessboard.executeMove(movement);
            // first, check if doing so would leave the King in check
            boolean isUnsafe = chessboard.isInCheck(this);
            if (isUnsafe) {
                iterator.remove();
            } else {
                // second, check if doing so checks the opponent
                Color opponentColor = this.getOppositeColor();
                if (chessboard.isInCheck(opponentColor)) {
                    movement.setWillCheckOpponent(true);
                }
            }
            // IMPORTANT: undo the move to restore the state
            chessboard.undoMove(movement);
        }
        // finally, if this is King, add Castling moves if applicable
        if (this instanceof King) {
            ((King) this).addCastlingMoves(allMoves, chessboard);
        }
        return allMoves;
    }

    /**
     * <b>Note:</b> This method returning {@code true} does NOT necessarily imply this piece IS at
     * its initial position, unless this piece has only one possible initial position
     * (e.g. {@link Queen} and {@link King}).
     * @return A boolean value indicating whether this piece <i>may</i> be at an initial position.
     * */
    public abstract boolean isAtAnInitialPosition();

    /**
     * Moves this piece to a new position.
     * @param newPosition A new position to which this piece will be moved.
     * @throws NullPointerException If {@code newPosition} is {@code null}.
     * */
    public final void moveTo(Position newPosition) {
        this.position.moveTo(newPosition.getFile(), newPosition.getRank());
    }

    /**
     * @param position A position to be checked.
     * @return A boolean value indicating whether this piece is at the given position.
     * @throws NullPointerException If {@code position} is {@code null}.
     * */
    public final boolean isAt(Position position) {
        return this.position.equals(position);
    }

    /**
     * @return The Version 4 UUID string of this piece.
     * */
    public final String getIdentifier() {
        return this.identifier;
    }

    /**
     * @return The position of this piece.
     * */
    public final Position getPosition() {
        return this.position;
    }

    /**
     * @return The move count of the piece.
     * */
    public final int getMoveCount() {
        return this.moveCount;
    }

    /**
     * @return A boolean value indicating whether this piece has moved before in a Chess game.
     * */
    public final boolean hasMovedBefore() {
        return this.moveCount > 0;
    }

    /** Increments the move count of this piece by one. */
    public final void incrementMoveCount() {
        this.moveCount++;
    }

    /**
     * Decrements the move count of this piece by one.<p>
     * Does nothing when the move count is already 0.
     * */
    public final void decrementMoveCount() {
        if (this.moveCount > 0) {
            this.moveCount--;
        }
    }

    /** Resets the move count of this piece to 0. */
    public final void resetMoveCount() {
        this.moveCount = 0;
    }

    /**
     * This piece is considered equal to another object <i>if and only if</i> the other object is
     * also an instance of the same type <i>and</i> has the same identifier string.
     * @param obj An object to compare to.
     * @return A boolean value indicating whether this piece is equal to another object.
     * */
    @Override
    public final boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return this.identifier.equals(((Piece) obj).identifier);
    }

    @Override
    public final int hashCode() {
        return this.identifier.hashCode();
    }

    /**
     * The string representation of a {@link Piece} is of the form:
     * <pre> {@literal <symbol>@<position>} </pre> where:
     * <ul>
     *     <li>{@code <symbol>} is the character symbol returned from {@link #getCharacterSymbol()};</li>
     *     <li>{@code <position>} is of the pattern: <code>^[a-hA-H][1-8]$</code>.</li>
     * </ul>
     * @return The string representation of this piece.
     * */
    @Override
    public final String toString() {
        return this.getCharacterSymbol() + "@" + this.position.toString();
    }

}
