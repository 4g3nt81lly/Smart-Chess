package chess.model.moves;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import org.json.JSONObject;

/**
 * A concrete object that represents an <i>en passant</i> capture, a special type of capture
 * applied only to {@link Pawn}'s.<p>
 * Let player A be White, having X as their Pawn, and player B be Black, having Y as their Pawn,
 * such that X is at a file adjacent to that of Y:
 * <ul>
 *     <li>An en passant capture is offered to player A if player B made an initial two-square
 *         advance with Y which resulted Y being in the same rank as X.</li>
 *     <li>To perform the capture, X is moved to the position directly behind Y and Y is removed
 *         from the chessboard (captured).</li>
 *     <li>En passant captures expire immediately after the turn during which they were offered,
 *         regardless of whether the relevant configurations remain.</li>
 * </ul>
 * Visually, using Y on the LHS:
 * <pre>
 * _ _ _ _ _       _ _ _ _ _       _ _ _ _ _
 * _ _ _ &#9817; _       _ _ &#9823; &#9817; _       _ _ _ _ _
 * _ _ _ _ _   &rarr;   _ _ _ _ _   &rarr;   _ _ &#9817; _ _
 * &#9823; &#9823; &#9823; &#9823; &#9823;       &#9823; &#9823; _ &#9823; &#9823;       &#9823; &#9823; _ &#9823; &#9823;
 * &#9822; &#9821; &#9819; &#9818; &#9821;       &#9822; &#9821; &#9819; &#9818; &#9821;       &#9822; &#9821; &#9819; &#9818; &#9821;
 * </pre>
 * An en passant capture is essentially a capture but with only one subtle nuance regarding the
 * movement itself: the final position to which the target Pawn will be moved is different from the
 * position of the Pawn to be captured.
 * */
public final class EnPassantCapture extends Capture {

    /**
     * Constructs an en passant capture for a subject Pawn and the Pawn to be captured.
     * @param pawn          The target Pawn.
     * @param capturedPawn  The Pawn to be captured.
     * @param finalPosition The final position to which the target Pawn will be moved.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public EnPassantCapture(Pawn pawn, Pawn capturedPawn, Position finalPosition) {
        super(pawn, capturedPawn);
        this.finalPosition = finalPosition.copy();
    }

    /**
     * Constructs an en passant capture from a JSON source containing the required key-value
     * mappings specified in {@link Capture#Capture(JSONObject, Chessboard)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @param chessboard The chessboard from which the Pawn to be captured is resolved.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link Capture#Capture(JSONObject, Chessboard)} or if
     *                         either of the pieces involved is not a Pawn.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see Capture#Capture(JSONObject, Chessboard)
     * */
    EnPassantCapture(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject, chessboard);
        Piece pawnPiece = this.getPieceFrom(chessboard);
        Piece capturedPawnPiece = this.getCapturedPiece(chessboard);
        if (!(pawnPiece instanceof Pawn && capturedPawnPiece instanceof Pawn)) {
            throw new FormatException("En passant capture cannot be applied to non-Pawn pieces.");
        }
    }

}
