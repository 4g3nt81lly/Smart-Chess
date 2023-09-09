package chess.model.moves;

import chess.model.Chessboard;
import chess.model.Colored;
import chess.model.pieces.King;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import org.json.JSONObject;

import static chess.model.TextSymbols.X;

/**
 * A concrete object that represents a capture.<p>
 * A capture is based on a regular move except that an opponent's piece is present at the final
 * position and will be removed from the chessboard upon capture.<p>
 * In addition to the information captured by a regular move, a capture encodes the following
 * additional information regarding the movement:
 * <ol>
 *     <li>The piece to be captured.</li>
 * </ol>
 * A capture can be applied to all concrete subclasses of {@link Piece}.
 * */
public class Capture extends RegularMove {

    /** A key string for serializing the identifier string of the piece to be captured. */
    protected static final String capturedPieceIdKey = "capturedPieceId";

    /** The Version 4 UUID string of the piece to be captured. */
    private final String capturedPieceId;

    /**
     * Constructs a capture for a subject piece and the piece to be captured.
     * @param piece         The target piece of the capture.
     * @param capturedPiece The piece to be captured by the target piece.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Capture(Piece piece, Piece capturedPiece) {
        super(piece, capturedPiece.getPosition());
        this.capturedPieceId = capturedPiece.getIdentifier();
    }

    /**
     * Constructs a capture from a JSON source of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     {@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},
     *     {@value movementTypeKey}:{@code "Capture"},
     *     {@value pieceIdKey}:{@code "<id-1>"},
     *     <b>{@value capturedPieceIdKey}:{@code "<id-2>"}</b>,
     *     {@value willCheckOpponentKey}:{@code <true/false>}
     * }</pre>
     * where:
     * <ul>
     *     <li><code>&lt;id-2&gt;</code>:
     *         the Version 4 UUID string of the piece to be captured.<br>
     *     </li>
     * </ul>
     * @param jsonObject The top-level {@link JSONObject} of the structure above from which a
     *                   capture is decoded.
     * @param chessboard The chessboard from which the piece to be captured is resolved, which is
     *                   used to validate the captured piece.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link RegularMove#RegularMove(JSONObject, Chessboard)},
     *                         or if one of the following scenarios occurs:
     *                         <ol>
     *                             <li>failed to resolve the piece to be captured with
     *                                 <code>&lt;id-2&gt;</code> on {@code chessboard};</li>
     *                             <li><code>&lt;id-2&gt;</code> refers to an allied piece.</li>
     *                         </ol>
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see RegularMove#RegularMove(JSONObject, Chessboard)
     * */
    protected Capture(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject, chessboard);
        String capturedPieceId = jsonObject.getString(capturedPieceIdKey);
        // captured piece validation
        Piece capturedPiece = chessboard.getPieceWith(capturedPieceId)
                .orElseThrow(() -> new FormatException("No piece found with ID '" + capturedPieceId + "'."));
        if (capturedPiece.isOpponentTo(this)) {
            this.capturedPieceId = capturedPieceId;
        } else {
            throw new FormatException("Incompatible: Capturing an allied piece " + capturedPiece + ".");
        }
    }

    /**
     * Serializes this capture to a JSON object of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<color>"},
     *     {@value initialPositionKey}:{@code "<position>"},
     *     {@value finalPositionKey}:{@code "<position>"},
     *     {@value movementTypeKey}:{@code "Capture"},
     *     {@value pieceIdKey}:{@code "<id-1>"},
     *     <b>{@value capturedPieceIdKey}:{@code "<id-2>"},</b>
     *     {@value willCheckOpponentKey}: {@code <true/false>}
     * }</pre>
     * where:
     * <ul>
     *     <li><code>&lt;id-2&gt;</code>:
     *         the Version 4 UUID string of the piece to be captured.<br>
     *     </li>
     * </ul>
     * @see RegularMove#encode()
     * */
    @Override
    public final JSONObject encode() {
        return super.encode()
                .put(capturedPieceIdKey, this.capturedPieceId);
    }

    /**
     * Executes this capture on the given chessboard and increments the move count of the
     * target piece.<p>
     * As a result, the piece to be captured is removed from the chessboard and the target piece is
     * moved to the final position.
     * @param chessboard The chessboard on which this capture is executed.
     * @return The number of points associated with the captured piece.
     * @throws java.util.NoSuchElementException If no piece with {@link #capturedPieceId} is found
     *                                          on {@code chessboard}.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public final int execute(Chessboard chessboard) {
        Piece capturedPiece = this.getCapturedPiece(chessboard);
        chessboard.capturePiece(capturedPiece);
        // move current piece to final position and get points
        return super.execute(chessboard) + capturedPiece.getPoints();
    }

    /**
     * Undoes this capture on the given chessboard and decrements the move count of the target
     * piece.<p>
     * As a result, the target piece is moved back to the initial position of this regular move and the captured piece is added back to the chessboard.
     * @param chessboard The chessboard on which this capture is undone.
     * @return The number of points associated with the captured piece.
     * @throws java.util.NoSuchElementException If no piece with {@link #capturedPieceId} is found
     *                                          on {@code chessboard}.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public final int undo(Chessboard chessboard) {
        // move the captured piece to initial position
        super.undo(chessboard);
        // add the captured piece back to the chessboard
        Piece capturedPiece = this.getCapturedPiece(chessboard);
        chessboard.undoCapturePiece(capturedPiece);
        return capturedPiece.getPoints();
    }

    /**
     * This method is used primarily for forward-checking. As a candidate movement, if the piece to
     * be captured is a {@link King}, then the move that led to this capture should be
     * considered as an unsafe and illegal move, for no player is allowed to make a move that puts
     * their own King in check.<p>
     * The negation of this method is the precondition for {@link #execute(Chessboard)}, since a
     * King will never be captured in a Chess game.
     * @param chessboard The chessboard from which the piece to be captured is resolved.
     * @return A boolean value indicating whether the piece to be captured is a
     * {@link King}.
     * @throws java.util.NoSuchElementException If no piece with {@link #capturedPieceId} is found
     *                                          on {@code chessboard}.
     * */
    public final boolean isCheckingOpponent(Chessboard chessboard) {
        return (this.getCapturedPiece(chessboard) instanceof King);
    }

    /**
     * @param chessboard The chessboard from which the piece to be captured is resolved.
     * @return The piece to be captured by this capture on the given chessboard.
     * @throws java.util.NoSuchElementException If no piece with {@link #capturedPieceId} is found
     *                                          on {@code chessboard}.
     * */
    public final Piece getCapturedPiece(Chessboard chessboard) {
        return chessboard.getPieceWith(this.capturedPieceId).orElseThrow();
    }

    /**
     * Returns an X mark as the descriptor text for capture.<br>
     * @return An X mark (✗, {@code \}{@code u2717}).
     * */
    @Override
    protected final String getDescriptor() {
        return String.valueOf(X);
    }

    /**
     * Since an opponent's piece will be present at a capture's final position, a capture's
     * destination string is of the form:
     * <pre> &#9823;@[position], e.g. &#9823;@a2 </pre>
     * This shows not only the final position but also the piece to be captured.
     * @return A string representation of the above-mentioned form.
     * */
    @Override
    public final String getDestinationString(Chessboard chessboard) {
        return this.getCapturedPiece(chessboard).getCharacterSymbol()
                + "@" + this.getFinalPosition();
    }

    /**
     * @return The Version 4 UUID string of the piece to be captured.
     * */
    public String getCapturedPieceId() {
        return this.capturedPieceId;
    }

}
