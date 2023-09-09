package chess.model.pieces;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.moves.TwoSquareAdvance;
import chess.model.exceptions.FormatException;
import chess.model.Color;

import chess.model.moves.RegularMove;
import chess.model.moves.EnPassantCapture;

import org.json.JSONObject;
import chess.util.List;

import static chess.model.Position.*;

/**
 * A concrete subtype of {@link Piece} that represents a Pawn ( &#9817; / &#9823; ).<p>
 * In addition to some of the generic behaviors of a Chess piece, a Pawn also:
 * <ol>
 *     <li>stores the state of whether it is on its initial two-square advance
 *         (i.e. whether it offers en passant capture to the opponent; such Pawn is considered
 *         <i>"in passing / en passant"</i> thereinafter);</li>
 *     <li>determines whether it can initiate an en passant capture on an opponent's Pawn;</li>
 *     <li>cannot move backward;</li>
 *     <li>determines if it is eligible for a Pawn Promotion (<i>not implemented</i>).</li>
 * </ol>
 * Movements that may be initiated by a Pawn:
 * <ol>
 *     <li>{@link RegularMove}: a regular one-square forward advance;</li>
 *     <li>{@link TwoSquareAdvance}: a two-square forward advance on the first move;</li>
 *     <li>{@link Capture}: diagonally-forward regular captures;</li>
 *     <li>{@link EnPassantCapture}: diagonally-forward en passant captures, refer to
 *         {@link EnPassantCapture} for details.</li>
 * </ol>
 * */
public final class Pawn extends Piece {

    /** A key string for serializing {@link #enPassant}. */
    private static final String enPassantKey = "enPassant";

    /** A boolean flag indicating whether this Pawn is "in passing." */
    private boolean enPassant = false;

    /**
     * Constructs a {@link Pawn} with a color and a position.
     * @param color    The color of the Pawn.
     * @param position The position of the Pawn on a chessboard.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    /**
     * Constructs a {@link Pawn} from a JSON object containing the following required key-value
     * mappings:
     * <pre>
     * {
     *     {@value colorKey}:{@code "<color>"},
     *     {@value pieceTypeKey}:{@code "<type>"},
     *     {@value positionKey}:{@code "<position>"},
     *     {@value idKey}:{@code "<id>"},
     *     {@value moveCountKey}:{@code "<count>"},
     *     <b>{@value enPassantKey}:{@code <true/false>}</b>
     * }</pre>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link Piece#Piece(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @implNote This constructor should <i>only</i> be invoked <i>dynamically</i> by
     *           {@link Movement#decode(JSONObject, Chessboard)} via reflection.
     * @see Piece#Piece(JSONObject)
     * */
    Pawn(JSONObject jsonObject) {
        super(jsonObject);
        this.enPassant = jsonObject.getBoolean(enPassantKey);
    }

    /**
     * Serializes this {@link Pawn} to a JSON object of the following format:
     * <pre>
     * {
     *     {@value colorKey}:{@code "<color>"},
     *     {@value pieceTypeKey}:{@code "<type>"},
     *     {@value positionKey}:{@code "<position>"},
     *     {@value idKey}:{@code "<id>"},
     *     {@value moveCountKey}:{@code "<count>"},
     *     <b>{@value enPassantKey}:{@code <true/false>}</b>
     * }</pre>
     * @see Piece#encode()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(enPassantKey, this.enPassant);
    }

    /**
     * Computes and returns a list of candidate movements according to the current configuration of
     * the chessboard and the following:
     * <pre>
     * &#8231; &#8231; o &#8231; &#8231;
     * &#8231; X O X &#8231;
     * &#8231; x &#9817; x &#8231;
     * &#8231; &#8231; &#8231; &#8231; &#8231;</pre>
     * where:
     * <ul>
     *     <li>{@code O} is a regular one-square advance;</li>
     *     <li>{@code o} is the initial two-square advance;</li>
     *     <li>{@code X}'s are a potential capture;</li>
     *     <li>{@code x}'s are {@link EnPassantCapture}'s, refer to it for details.</li>
     * </ul>
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * @see TwoSquareAdvance
     * @see EnPassantCapture
     * */
    @Override
    public List<Movement> getCandidateMoves(Chessboard chessboard) {
        List<Movement> moves = new List<>();
        if (this.isEligibleForPromotion()) {
            // Pawn reached the end of the chessboard
            return moves;
        }
        this.position.forward(1, this).ifPresent(advanceOne -> {
            if (!chessboard.hasPieceAt(advanceOne)) {
                // no piece in front, regular move
                moves.add(new RegularMove(this, advanceOne));
                // if two-square advance is allowed
                if (!this.hasMovedBefore()) {
                    // since it's the first move, the second square definitely exists
                    Position advanceTwo = advanceOne.forward(1, this).orElseThrow();
                    if (!chessboard.hasPieceAt(advanceTwo)) {
                        moves.add(new TwoSquareAdvance(this, advanceTwo));
                    }
                }
            }
        });
        // get capture moves
        moves.addAll(this.getCaptures(chessboard));
        return moves;
    }

    /**
     * Generates a list of captures according to the current configuration of the board.<p>
     * Refer to {@link #getCandidateMoves(Chessboard)} for details.
     * @param chessboard The enclosing chessboard.
     * @return A list of captures.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * @see #getCandidateMoves(Chessboard)
     * */
    private List<Capture> getCaptures(Chessboard chessboard) {
        List<Capture> captures = new List<>();
        // check for captures
        /*   _ ? _ ? _
             _ P @ P _   */
        for (int quadrant = Q1; quadrant <= Q2; quadrant++) {
            final int finalQuadrant = quadrant;
            this.position.radialStep(1, 1, quadrant, this).ifPresent(diagonal -> {
                // valid position
                chessboard.getPieceAt(diagonal).ifPresentOrElse(diagonalPiece -> {
                    if (diagonalPiece.isOpponentTo(this)) {
                        // has an enemy piece, threaten to capture
                        captures.add(new Capture(this, diagonalPiece));
                    }
                }, () -> {
                    // empty square diagonally, check for en passant capture
                    // check left/right passing positions depending on quadrant
                    int enPassantQuadrant = (finalQuadrant == Q2) ? Q3 : Q1;
                    // the passing position is guaranteed to be valid since
                    // a valid diagonal position implies a valid left/right position
                    Position passingPosition = this.position.axialStep(1, enPassantQuadrant, this).orElseThrow();
                    chessboard.getPieceAt(passingPosition).ifPresent(passingPiece -> {
                        if (passingPiece instanceof Pawn
                                && passingPiece.isOpponentTo(this)
                                && ((Pawn) passingPiece).isEnPassant()) {
                            // passing piece is an enemy Pawn on its initial 2-square advance
                            // an en passant capture is offered
                            captures.add(new EnPassantCapture(this, (Pawn) passingPiece, diagonal));
                        }
                    });
                });
            });
        }
        return captures;
    }

    /**
     * A Pawn <i>may</i> be at its initial position <i>only if</i> it is at rank 2 (White) or 7 (Black).
     * @return A boolean value indicating whether this Pawn is at one of the possible initial positions.
     * */
    @Override
    public boolean isAtAnInitialPosition() {
        int initialRank = this.isWhite() ? 2 : 7;
        return this.position.getRank() == initialRank;
    }

    /**
     * @return A boolean value indicating whether this Pawn is "in passing."
     * */
    public boolean isEnPassant() {
        return this.enPassant;
    }

    /**
     * Sets the boolean flag that indicates whether this Pawn is "in passing."<p>
     * This method:
     * <ul>
     *     <li>does nothing if this Pawn is not eligible for being "in passing,"
     *         i.e. checks if this Pawn is on rank 4 (White) or 5 (Black);</li>
     *     <li>sets the flag to {@code false} without any check.</li>
     * </ul>
     * @param enPassant A new boolean value.
     * */
    public void setEnPassant(boolean enPassant) {
        int enPassantRank = this.isWhite() ? 4 : 5;
        if (!enPassant || this.position.getRank() == enPassantRank) {
            this.enPassant = enPassant;
        }
    }

    /**
     * A Pawn is eligible for Pawn Promotion <i>if and only if</i> it has reached the last rank.
     * @return A boolean value indicating whether this Pawn is eligible for Pawn Promotion.
     * */
    public boolean isEligibleForPromotion() {
        return this.position.atLastRank(this.color);
    }

}
