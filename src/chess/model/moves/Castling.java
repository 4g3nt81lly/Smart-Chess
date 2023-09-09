package chess.model.moves;

import chess.model.Chessboard;
import chess.model.Colored;
import chess.model.Position;
import chess.model.pieces.King;
import chess.model.pieces.Piece;
import chess.model.pieces.Rook;
import chess.model.exceptions.FormatException;
import org.json.JSONObject;

import static chess.model.TextSymbols.leftArrow;

/**
 * A subcategory of the regular move that represents a Castling, with a King being the
 * subject piece of the main movement and a target Rook making a subsequent regular move.<p>
 * Castling will be offered <i>if and only if</i>:
 * <ol>
 *     <li>The King is not in check (the Rook, however, can be threatened);</li>
 *     <li>The passage between the King and the target Rook are vacant;</li>
 *     <li>The passage between the King and the target Rook are not under attack;</li>
 *     <li>Both the King and the target Rook are on their first move
 *         (i.e. have not been moved previously).</li>
 * </ol>
 * A King may castle with either Rooks (King-side or Queen-side) so long as these preconditions
 * (in no particular order) are satisfied.<p>
 * During the move:
 * <ol>
 *     <li>The King is moved two squares toward the target Rook.</li>
 *     <li>The target Rook is moved to the other side of the King.</li>
 * </ol>
 * Visually, King-side Castling:
 * <pre>
 * _ _ _ _ _ _ _ _       _ _ _ _ _ _ _ _
 * _ _ _ _ &#9818; _ _ &#9820;   &rarr;   _ _ _ _ _ &#9820; &#9818; _
 * </pre>
 * Queen-side Castling:
 * <pre>
 * _ _ _ _ _ _ _ _       _ _ _ _ _ _ _ _
 * &#9820; _ _ _ &#9818; _ _ _   &rarr;   _ _ &#9818; &#9820; _ _ _ _
 * </pre>
 * Castling can only be applied to {@link King}'s and not {@link Rook}'s.
 * */
public final class Castling extends RegularMove {

    /** A key string for serializing the target Rook's regular move. */
    static final String rookMoveKey = "rookMove";

    /** The target Rook's regular move. */
    final RegularMove rookMove;

    /**
     * Constructs a Castling for a subject King and a target Rook.
     * @param king              The target King.
     * @param kingFinalPosition The final position of the target King.
     * @param rook              The target Rook (King-side or Queen-side).
     * @param rookFinalPosition The final position of the target Rook.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Castling(King king, Position kingFinalPosition,
                    Rook rook, Position rookFinalPosition) {
        super(king, kingFinalPosition);
        this.rookMove = new RegularMove(rook, rookFinalPosition);
    }

    /**
     * Constructs a Castling from a JSON source of the following format:
     * <pre>
     * {
     *     {@value Colored#colorKey}:{@code "<king-color>"},
     *     {@value initialPositionKey}:{@code "<king-position>"},
     *     {@value finalPositionKey}:{@code "<king-position>"},
     *     {@value movementTypeKey}:{@code "Castling"},
     *     {@value pieceIdKey}:{@code "<king-id>"},
     *     {@value willCheckOpponentKey}:{@code <true/false>},
     *     <b>{@value rookMoveKey}: {</b>
     *         {@value Colored#colorKey}:{@code "<rook-color>"},
     *         {@value initialPositionKey}:{@code "<rook-position>"},
     *         {@value finalPositionKey}:{@code "<rook-position>"},
     *         {@value movementTypeKey}: {@code "RegularMove"},
     *         {@value pieceIdKey}:{@code "<rook-id>"},
     *         {@value willCheckOpponentKey}:{@code false},
     *     <b>}</b>
     * }</pre>
     * @param jsonObject The top-level {@link JSONObject} of the structure above from which a
     *                   Castling is decoded.
     * @param chessboard The chessboard from which the target Rook is resolved, which is used to
     *                   validate the King and the Rook.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link RegularMove#RegularMove(JSONObject, Chessboard)},
     *                         or if one of the following scenarios occurs:
     *                         <ol>
     *                             <li>failed to resolve a piece with {@code <rook-id>} in
     *                                 {@code chessboard};</li>
     *                             <li>{@code <rook-id>} refers to a non-Rook piece or the piece
     *                                 belongs to the opponent.</li>
     *                         </ol>
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see RegularMove#RegularMove(JSONObject, Chessboard)
     * */
    Castling(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject, chessboard);
        Piece kingPiece = this.getPieceFrom(chessboard);
        if (kingPiece instanceof King) {
            JSONObject rookMoveObject = jsonObject.getJSONObject(rookMoveKey);
            RegularMove rookMove = new RegularMove(rookMoveObject, chessboard);
            Piece rookPiece = rookMove.getPieceFrom(chessboard);
            if (rookPiece instanceof Rook && rookPiece.isAlliedTo(kingPiece)) {
                this.rookMove = rookMove;
                this.rookMove.setWillCheckOpponent(false);
            } else {
                throw new FormatException("Castling cannot be applied to " + rookPiece + ".");
            }
        } else {
            throw new FormatException("Castling cannot be applied to " + kingPiece + ".");
        }
    }

    /**
     * Serializes this Castling to a JSON object of the following structure:
     * <pre>
     * {
     *     [..1..],
     *     <b>{@value rookMoveKey}: {
     *         [..2..]
     *     }</b>
     * }</pre>
     * where the placeholders:
     * <ul>
     *     <li><code>[..1..]</code> is where the info for King's regular move is stored;</li>
     *     <li><code>[..2..]</code> is where the info for Rook's regular move is stored.</li>
     * </ul>
     * See {@link #Castling(JSONObject, Chessboard)} for details.
     * @see RegularMove#encode()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(rookMoveKey, this.rookMove.encode());
    }

    /**
     * Executes this Castling on the given chessboard and increments the move count of both the
     * King and the Rook.<p>
     * As a result, the King is moved to its specified final position, followed by the Rook also
     * moving to its specified final position.
     * @param chessboard The chessboard on which this Castling is executed.
     * @return 0 points, since a Castling is associated with no points.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int execute(Chessboard chessboard) {
        super.execute(chessboard);
        this.rookMove.execute(chessboard);
        return 0;
    }

    /**
     * Undoes this Castling on the given chessboard and decrements the move count of both the King
     * and the Rook.<p>
     * As a result, the Rook's move is undone and the King is moved back to its initial position.
     * @param chessboard The chessboard on which this Castling is undone.
     * @return 0 points, since a Castling is associated with no points.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int undo(Chessboard chessboard) {
        // move Rook and then King back to its initial position
        this.rookMove.undo(chessboard);
        super.undo(chessboard);
        return 0;
    }

    /**
     * @return The target Rook of this Castling.
     * */
    public Rook getRook(Chessboard chessboard) {
        return (Rook) this.rookMove.getPieceFrom(chessboard);
    }

    /**
     * Returns a left arrow as the descriptor text for capture.<br>
     * @return A left arrow (←, {@code \}{@code u2190}).
     * */
    @Override
    public String getDescriptor() {
        return String.valueOf(leftArrow);
    }

}
