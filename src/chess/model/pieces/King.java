package chess.model.pieces;

import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Castling;
import chess.model.moves.Movement;
import chess.model.moves.RegularMove;
import chess.model.exceptions.FormatException;
import chess.util.List;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Optional;

import static chess.model.Position.*;

/**
 * A concrete subtype of {@link Piece} that represents a King ( &#9812; / &#9818; ).<p>
 * A Rook conforms to all the generic rules of a Chess piece and has none of its own.<p>
 * Rooks may <i>initiate</i> Castling (participated by a Rook), during which the King moves two
 * squares toward the participant Rook.<p>
 * Movements that may be initiated by a King:
 * <ol>
 *     <li>{@link RegularMove}: regular moves to the surrounding squares;</li>
 *     <li>{@link Capture}: captures;</li>
 *     <li>{@link Castling}: a Castling with either one of the Rooks, refer to
 *         {@link Castling} for details.</li>
 * </ol>
 * @see Castling
 * */
public final class King extends Piece {

    /**
     * Constructs a {@link King} with a color and a position.
     * @param color    The color of the King.
     * @param position The position of the King on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public King(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link King} from a JSON object containing the required key-value mappings
     * specified in {@link Piece#Piece(JSONObject)}.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor is invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    King(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * A King's reachable positions are as follows:
     * <pre>
     * &#8231; &#8231; &#8231; &#8231; &#8231;
     * &#8231; x x x &#8231;
     * &#8231; x &#9813; x &#8231;
     * &#8231; x x x &#8231;
     * &#8231; &#8231; &#8231; &#8231; &#8231;</pre>
     * @return A list of positions reachable by this King.
     * */
    public List<Position> reachablePositions() {
        List<Position> positions = new List<>();
        for (int quadrant : Position.quadrants) {
            this.position.axialStep(1, quadrant, this)
                    .ifPresent(positions::add);
            this.position.radialStep(1, 1, quadrant, this)
                    .ifPresent(positions::add);
        }
        return positions;
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard. See {@link #reachablePositions()} for King's movements.<p>
     * <b>Note:</b> The list of movements this method returns does NOT contain Castling.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        for (Position newPosition : this.reachablePositions()) {
            chessboard.getPieceAt(newPosition).ifPresentOrElse(piece -> {
                if (piece.isOpponentTo(this)) {
                    // enemy piece, threaten to capture
                    moves.add(new Capture(this, piece));
                }
            }, () -> {
                // vacant position, regular move
                moves.add(new RegularMove(this, newPosition));
            });
        }
        // remove if the new position is reachable by an enemy King
        moves.removeIf(move -> {
            // if any enemy King can reach the final position of the move
            return chessboard.getOpponentPieces(this).orMap(enemyPiece -> {
                return (enemyPiece instanceof King)
                        && ((King) enemyPiece).reachablePositions().orMap(position -> {
                            return move.getFinalPosition().equals(position);
                        });
            });
        });
        return moves;
    }

    /**
     * Computes and adds Castling(s) to a given list of movements according to the current
     * configuration of the chessboard. See {@link Castling} for detail rules regarding
     * Castling. This method does nothing if any of the conditions are not satisfied.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    void addCastlingMoves(List<Movement> moves, Chessboard chessboard) {
        Objects.requireNonNull(moves);
        if (!this.hasMovedBefore() && !chessboard.isInCheck(this)) {
            // NOTE: since initial positions are symmetric among White and Black
            //       queen-/king-side castling performs in the same direction for both sides (white)

            forBothDirections: for (int quadrant = Q1, distance = 3;
                                    quadrant <= Q3; quadrant += 2, distance++) {
                // queen-(left)/king(right)-side castling: (Q3, distance = 4), (Q1, distance = 3)
                /*   _ _ _ _ _ _ _ _
                     R _ _ Q K _ _ R   */
                //
                Position rookPosition = this.position.axialStep(distance, quadrant).orElseThrow();
                Optional<Piece> piece = chessboard.getPieceAt(rookPosition);
                if (piece.isPresent()) {
                    Piece rook = piece.get();
                    if (rook instanceof Rook
                            && rook.isAlliedTo(this)
                            && !rook.hasMovedBefore()) {
                        // found suitable rook at expected position
                        Position passageSquare = this.position.axialStep(1, quadrant).orElseThrow();
                        // check if the passage is clear AND not under attack
                        // move towards the rook position
                        while (!passageSquare.equals(rookPosition)) {
                            if (chessboard.hasPieceAt(passageSquare)
                                    || chessboard.isPositionUnderAttack(passageSquare, this)) {
                                // Castling towards the current direction is illegal
                                continue forBothDirections;
                            }
                            passageSquare = passageSquare.axialStep(1, quadrant).orElseThrow();
                        }
                        // all preconditions satisfied, Castling offered
                        Position kingFinalPosition = this.position.axialStep(2, quadrant).orElseThrow();
                        // compute final rook position
                        // - queen-side (left): to the right (quadrant 1)
                        // - king-side (right): to the left (quadrant 3)
                        int otherQuadrant = (quadrant == 3) ? 1 : 3;
                        Position rookFinalPosition = kingFinalPosition.axialStep(1, otherQuadrant).orElseThrow();
                        Castling castling = new Castling(this, kingFinalPosition,
                                (Rook) rook, rookFinalPosition);
                        moves.add(castling);
                    }
                }
            }
        }
    }

    /**
     * A King <i>is</i> at its initial position <i>if and only if</i> it is at e1 (White) or e8 (Black).
     * @return A boolean value indicating whether this King is at its initial position.
     * */
    @Override
    public boolean isAtAnInitialPosition() {
        Position initialPosition = Position.at(this.isWhite() ? "e1" : "e8");
        return this.isAt(initialPosition);
    }

}