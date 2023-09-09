package chess.model.pieces;

import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.moves.RegularMove;
import chess.model.exceptions.FormatException;
import chess.util.List;
import org.json.JSONObject;

import java.util.Optional;

/**
 * A concrete subtype of {@link Piece} that represents a Queen ( &#9813; / &#9819; ).<p>
 * A Queen conforms to all the generic rules of a Chess piece and has none of its own.<p>
 * Movements that may be initiated by a Queen:
 * <ol>
 *     <li>{@link RegularMove}: regular non-jumping horizontal, vertical, or diagonal moves;</li>
 *     <li>{@link Capture}: captures.</li>
 * </ol>
 * */
public final class Queen extends Piece {

    /**
     * Constructs a {@link Queen} with a color and a position.
     * @param color    The color of the Queen.
     * @param position The position of the Queen on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Queen(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link Queen} from a JSON object containing the required key-value mappings
     * specified in {@link Piece#Piece(JSONObject)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor is invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    Queen(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard and the following:
     * <pre>
     * x &#8231; x &#8231; x
     * &#8231; x x x &#8231;
     * x x &#9813; x x
     * &#8231; x x x &#8231;
     * x &#8231; x &#8231; x</pre>
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        for (int quadrant : Position.quadrants) {
            Optional<Position> axialPosition = this.position.axialStep(1, quadrant, this);
            Optional<Position> diagonalPosition = this.position.radialStep(1, 1, quadrant, this);
            List<Optional<Position>> positionsInQuadrant = List.of(axialPosition, diagonalPosition);
            for (int i = 0; i < 2; i++) {
                Optional<Position> position = positionsInQuadrant.get(i);
                while (position.isPresent()) {
                    Position newPosition = position.get();
                    Optional<Piece> pieceAtPosition = chessboard.getPieceAt(newPosition);
                    if (pieceAtPosition.isEmpty()) {
                        // vacant position, regular move
                        RegularMove regularMove = new RegularMove(this, newPosition);
                        moves.add(regularMove);
                        // continue moving in the direction
                        if (i == 0) {
                            // along X/Y axis
                            position = newPosition.axialStep(1, quadrant, this);
                        } else {
                            position = newPosition.radialStep(1, 1, quadrant, this);
                        }
                        continue;
                    } else if (pieceAtPosition.get().isOpponentTo(this)) {
                        // enemy piece, threaten to capture
                        Capture capture = new Capture(this, pieceAtPosition.get());
                        moves.add(capture);
                    }
                    break;
                }
            }
        }
        return moves;
    }

    /**
     * A Queen <i>is</i> at its initial position <i>if and only if</i> it is at d1 (White) or d8 (Black).
     * @return A boolean value indicating whether this Queen is at its initial position.
     * */
    public boolean isAtAnInitialPosition() {
        Position initialPosition = Position.at(this.isWhite() ? "d1" : "d8");
        return this.isAt(initialPosition);
    }

}