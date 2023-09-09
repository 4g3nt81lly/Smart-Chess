package chess.model.moves;

import chess.model.exceptions.FormatException;
import chess.model.Position;
import chess.model.Chessboard;

import chess.model.pieces.Pawn;

import org.json.JSONObject;

/**
 * A subcategory of the regular move that represents a Pawn's initial two-square advance.
 * <blockquote>
 *     The behavioral differences between this class and {@link RegularMove} are trivial.
 * </blockquote>
 * A two-square advance can only be applied to {@link Pawn}'s.
 * */
public final class TwoSquareAdvance extends RegularMove {

    /**
     * Constructs a two-square advance for a subject Pawn and a final position.
     * @param pawn          The target Pawn.
     * @param finalPosition The final position of the regular move.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public TwoSquareAdvance(Pawn pawn, Position finalPosition) {
        super(pawn, finalPosition);
    }

    /**
     * Constructs a two-square advance from a JSON source containing the required key-value
     * mappings specified in {@link Movement#Movement(JSONObject, Chessboard)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @param chessboard The chessboard from which the target piece is resolved.
     * @throws org.json.JSONException           If any of the required keys is missing or contains
     *                                          invalid value type.
     * @throws FormatException Rethrown from
     *                                          {@link RegularMove#RegularMove(JSONObject, Chessboard)}.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see RegularMove#RegularMove(JSONObject, Chessboard)
     * */
    TwoSquareAdvance(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject, chessboard);
    }

    /**
     * Executes this two-square advance on the given chessboard, increments the move count of the
     * target Pawn, and sets the target Pawn's en passant status to {@code true}.<p>
     * As a result, the target Pawn is moved to the final position of this two-square advance.
     * @param chessboard The chessboard on which this two-square advance is executed.
     * @return 0, the number of points associated with a regular move.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int execute(Chessboard chessboard) {
        super.execute(chessboard);
        ((Pawn) this.getPieceFrom(chessboard)).setEnPassant(true);
        return 0;
    }

    /**
     * Undoes this two-square advance on the given chessboard, decrements the move count of the
     * target Pawn, and sets the target Pawn's en passant status to {@code false}.<p>
     * As a result, the target Pawn is moved back to the initial position of this two-square advance.
     * @param chessboard The chessboard on which this two-square advance is undone.
     * @return 0 points, the number of points associated with a regular move.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int undo(Chessboard chessboard) {
        ((Pawn) this.getPieceFrom(chessboard)).setEnPassant(false);
        return super.undo(chessboard);
    }
}
