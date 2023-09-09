package chess.model.pieces;

import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.exceptions.FormatException;
import chess.model.Color;
import chess.model.Chessboard;

import chess.model.moves.RegularMove;

import org.json.JSONObject;
import chess.util.List;
import java.util.Optional;

/**
 * A concrete subtype of {@link Piece} that represents a Bishop ( &#9815; / &#9821; ).<p>
 * A Bishop conforms to all the generic rules of a Chess piece and has none of its own.<p>
 * Movements that may be initiated by a Bishop:
 * <ol>
 *     <li>{@link RegularMove}: regular non-jumping diagonal moves;</li>
 *     <li>{@link Capture}: captures.</li>
 * </ol>
 * */
public final class Bishop extends Piece {

    /**
     * Constructs a {@link Bishop} with a color and a position.
     * @param color    The color of the Bishop.
     * @param position The position of the Bishop on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link Bishop} from a JSON object containing the required key-value mappings
     * specified in {@link Piece#Piece(JSONObject)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor is invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    Bishop(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard and the following:
     * <pre>
     * x &#8231; &#8231; &#8231; x
     * &#8231; x &#8231; x &#8231;
     * &#8231; &#8231; &#9815; &#8231; &#8231;
     * &#8231; x &#8231; x &#8231;
     * x &#8231; &#8231; &#8231; x</pre>
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        for (int quadrant : Position.quadrants) {
            Optional<Position> diagonal = this.position.radialStep(1, 1, quadrant, this);
            // repeat while movable along the diagonal line in quadrant
            while (diagonal.isPresent()) {
                Position diagonalSquare = diagonal.get();
                Optional<Piece> diagonalPiece = chessboard.getPieceAt(diagonalSquare);
                if (diagonalPiece.isEmpty()) {
                    // vacant square
                    RegularMove regularMove = new RegularMove(this, diagonalSquare);
                    moves.add(regularMove);
                    // move along the diagonal
                    diagonal = diagonalSquare.radialStep(1, 1, quadrant, this);
                    continue;
                } else if (this.isOpponentTo(diagonalPiece.get())) {
                    // enemy piece, threaten to capture
                    Capture capture = new Capture(this, diagonalPiece.get());
                    moves.add(capture);
                }
                break;
            }
        }
        return moves;
    }

    /**
     * A Bishop <i>may</i> be at its initial position <i>only if</i> it is at one of the following
     * positions:
     * <ul>
     *     <li>White: c1 (Queen-side), f1 (King-side)</li>
     *     <li>Black: c8 (Queen-side), f8 (King-side)</li>
     * </ul>
     * @return A boolean value indicating whether this Bishop is at one of the initial positions above.
     * */
    @Override
    public boolean isAtAnInitialPosition() {
        List<String> positions = this.isWhite()
                ? List.of("c1", "f1")
                : List.of("c8", "f8");
        return positions.orMap(position -> this.isAt(Position.at(position)));
    }

}
