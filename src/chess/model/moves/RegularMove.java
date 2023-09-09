package chess.model.moves;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;

import org.json.JSONObject;

/**
 * A concrete subtype of {@link Movement} that represents a regular move.<p>
 * A regular move is a simple movement defined solely by a piece's position change where the final
 * position is an empty position (no piece).<p>
 * A regular move serves as a base movement for all other movement variants, including
 * {@link Castling}, which consists of two basic regular moves.<p>
 * A regular move can be applied to all concrete subclasses of {@link Piece}.
 * */
public class RegularMove extends Movement {

    /**
     * Constructs a regular move for a subject piece and a final position.
     * @param piece         The target piece of the regular move.
     * @param finalPosition The final position of the regular move.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public RegularMove(Piece piece, Position finalPosition) {
        super(piece, finalPosition);
    }

    /**
     * Constructs a regular move from a JSON object containing the required key-value mappings
     * specified in {@link Movement#Movement(JSONObject, Chessboard)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @param chessboard The chessboard from which the target piece is resolved.
     * @throws org.json.JSONException Rethrown from
     *                                {@link Movement#Movement(JSONObject, Chessboard)}.
     * @throws FormatException Rethrown from
     *                                          {@link Movement#Movement(JSONObject, Chessboard)}.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @see Movement#Movement(JSONObject, Chessboard)
     * */
    protected RegularMove(JSONObject jsonObject, Chessboard chessboard) {
        super(jsonObject, chessboard);
    }

    /**
     * Executes this regular move on the given chessboard and increments the move count of the
     * target piece.<p>
     * As a result, the target piece is moved to the final position of this regular move.
     * @param chessboard The chessboard on which this regular move is executed.
     * @return 0 points, since a regular move is associated with no points.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int execute(Chessboard chessboard) {
        super.execute(chessboard);
        this.getPieceFrom(chessboard).moveTo(this.finalPosition);
        return 0;
    }

    /**
     * Undoes this regular move on the given chessboard and decrements the move count of the target
     * piece.<p>
     * As a result, the target piece is moved back to the initial position of this regular move.
     * @param chessboard The chessboard on which this regular move is undone.
     * @return 0 points, since a regular move is associated with no points.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public int undo(Chessboard chessboard) {
        super.undo(chessboard);
        this.getPieceFrom(chessboard).moveTo(this.initialPosition);
        return 0;
    }

}
