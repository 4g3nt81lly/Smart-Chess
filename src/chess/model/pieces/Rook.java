package chess.model.pieces;

import chess.model.moves.Capture;
import chess.model.moves.Castling;
import chess.model.moves.Movement;
import chess.model.moves.RegularMove;
import chess.model.exceptions.FormatException;
import chess.model.Color;
import chess.model.Position;
import chess.model.Chessboard;

import org.json.JSONObject;
import chess.util.List;
import java.util.Optional;

/**
 * A concrete subtype of {@link Piece} that represents a Rook ( &#9814; / &#9820; ).<p>
 * A Rook conforms to all the generic rules of a Chess piece and has none of its own.<p>
 * Rooks may <i>participate</i> in Castling (initiated by the King), during which Rooks jump over
 * the King.<p>
 * Movements that may be initiated by a Rook:
 * <ol>
 *     <li>{@link RegularMove}: regular non-jumping horizontal/vertical moves;</li>
 *     <li>{@link Capture}: captures.</li>
 * </ol>
 * @see Castling
 * */
public final class Rook extends Piece {

    /**
     * Constructs a {@link Rook} with a color and a position.
     * @param color    The color of the Rook.
     * @param position The position of the Rook on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Rook(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link Rook} from a JSON object containing the required key-value mappings
     * specified in {@link Piece#Piece(JSONObject)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor is invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    Rook(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard and the following:
     * <pre>
     * &#8231; &#8231; x &#8231; &#8231;
     * &#8231; &#8231; x &#8231; &#8231;
     * x x &#9814; x x
     * &#8231; &#8231; x &#8231; &#8231;
     * &#8231; &#8231; x &#8231; &#8231;</pre>
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        for (int quadrant : Position.quadrants) {
            Optional<Position> positionAlongAxis = this.position.axialStep(1, quadrant, this);
            // repeat while movable along the straight line in quadrant
            while (positionAlongAxis.isPresent()) {
                Position square = positionAlongAxis.get();
                Optional<Piece> pieceAlongAxis = chessboard.getPieceAt(square);
                if (pieceAlongAxis.isEmpty()) {
                    // vacant square
                    RegularMove regularMove = new RegularMove(this, square);
                    moves.add(regularMove);
                    // move along the axis (straight line)
                    positionAlongAxis = square.axialStep(1, quadrant, this);
                    continue;
                } else if (this.isOpponentTo(pieceAlongAxis.get())) {
                    // enemy piece, threaten to capture
                    Capture capture = new Capture(this, pieceAlongAxis.get());
                    moves.add(capture);
                }
                break;
            }
        }
        return moves;
    }

    /**
     * A Rook <i>may</i> be at its initial position <i>only if</i> it is at one of the following
     * positions:
     * <ul>
     *     <li>White: a1 (Queen-side), h1 (King-side)</li>
     *     <li>Black: a8 (Queen-side), h8 (King-side)</li>
     * </ul>
     * @return A boolean value indicating whether this Rook is at one of the initial positions above.
     * */
    @Override
    public boolean isAtAnInitialPosition() {
        List<String> positions = this.isWhite()
                ? List.of("a1", "h1")
                : List.of("a8", "h8");
        return positions.orMap(position -> this.isAt(Position.at(position)));
    }

}
