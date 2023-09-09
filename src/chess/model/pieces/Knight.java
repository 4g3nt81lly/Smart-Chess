package chess.model.pieces;

import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.exceptions.FormatException;
import chess.model.Color;
import chess.model.Position;
import chess.model.Chessboard;

import chess.model.moves.RegularMove;

import org.json.JSONObject;
import chess.util.List;

/**
 * A concrete subtype of {@link Piece} that represents a Knight ( &#9816; / &#9822; ).<p>
 * A Knight conforms to all the generic rules of a Chess piece and has none of its own.<p>
 * Movements that may be initiated by a Knight:
 * <ol>
 *     <li>{@link RegularMove}: a regular <i>jumping</i> move specified in
 *         {@link #getCandidateMoves(Chessboard)};</li>
 *     <li>{@link Capture}: a capture.</li>
 * </ol>
 * */
public final class Knight extends Piece {

    /**
     * Constructs a {@link Knight} with a color and a position.
     * @param color    The color of the Knight.
     * @param position The position of the Knight on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link Knight} from a JSON object containing the required key-value mappings
     * specified in {@link Piece#Piece(JSONObject)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor is invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    Knight(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard and the following:
     * <pre>
     * &#8231; x &#8231; x &#8231;
     * x &#8231; &#8231; &#8231; x
     * &#8231; &#8231; &#9816; &#8231; &#8231;
     * x &#8231; &#8231; &#8231; x
     * &#8231; x &#8231; x &#8231;</pre>
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        for (int quadrant : Position.quadrants) {
            // for each quadrant: (deltaFile, deltaRank) = (1, 2), (2, 1)
            for (int i = 1, j = 2; i <= 2; i++, j--) {
                this.position.radialStep(i, j, quadrant, this).ifPresent(diagonal -> {
                    chessboard.getPieceAt(diagonal).ifPresentOrElse(diagonalPiece -> {
                        if (diagonalPiece.isOpponentTo(this)) {
                            // enemy piece, threaten to capture
                            Movement captureMove = new Capture(this, diagonalPiece);
                            moves.add(captureMove);
                        }
                    }, () -> {
                        // vacant position, regular move
                        Movement regularMove = new RegularMove(this, diagonal);
                        moves.add(regularMove);
                    });
                });
            }
        }
        return moves;
    }

    /**
     * A Knight <i>may</i> be at its initial position <i>only if</i> it is at one of the following
     * positions:
     * <ul>
     *     <li>White: b1 (Queen-side), g1 (King-side)</li>
     *     <li>Black: b8 (Queen-side), g8 (King-side)</li>
     * </ul>
     * @return A boolean value indicating whether this Knight is at one of the initial positions above.
     * */
    @Override
    public boolean isAtAnInitialPosition() {
        List<String> positions = this.isWhite()
                ? List.of("b1", "g1")
                : List.of("b8", "g8");
        return positions.orMap(position -> this.isAt(Position.at(position)));
    }

}