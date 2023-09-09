package chess.model;

import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.moves.RegularMove;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.exceptions.FormatException;
import chess.persistence.Codable;
import chess.util.List;
import chess.util.KeyValuePair;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static chess.model.Color.Black;
import static chess.model.Color.White;

/**
 * A class that represents a chessboard in a Chess game and manages the Chess pieces that are
 * associated with it.<p>
 * Simply, a chessboard is essentially a collection of Chess pieces, therefore it maintains no
 * states other than the pieces.<p>
 * This class implements all the core behaviors and operations of a chessboard, including but not
 * limited to manipulating Chess pieces and providing insights to the current configuration of the
 * chessboard.<p>
 * <b>Note:</b> The chessboard is unaware of and has no access to the game that it is a part of.
 * */
public class Chessboard implements Iterable<Piece>, Codable {

    /** The key string for serializing active Chess pieces. */
    private static final String activePiecesKey = "active";

    /** The key string for serializing captured Chess pieces. */
    private static final String capturedPiecesKey = "captured";

    /**
     * Constructs an empty chessboard with no Chess pieces.
     * @return An empty chessboard.
     * */
    public static Chessboard empty() {
        return new Chessboard(List.of());
    }

    /**
     * Constructs a chessboard with the standard setup of a Chess game.
     * @return A standard chessboard setup.
     * */
    public static Chessboard standard() {
        Chessboard chessboard = Chessboard.empty();
        // add white/black Pawns at their initial positions
        for (String file : Position.files) {
            chessboard.placeWhite("Pawn", file + 2);
            chessboard.placeBlack("Pawn", file + 7);
        }
        // add Kings at their initial positions
        chessboard.placeWhite("King", "e1");
        chessboard.placeBlack("King", "e8");
        // add Queens at their initial positions
        chessboard.placeWhite("Queen", "d1");
        chessboard.placeBlack("Queen", "d8");

        List<String> bnrPositions = List.of("cf", "bg", "ah");
        List<String> bnrNames = List.of("Bishop", "Knight", "Rook");
        for (int i = 0; i < bnrNames.size(); i++) {
            String name = bnrNames.get(i);
            String position = bnrPositions.get(i);
            // White
            chessboard.placeWhite(name, position.charAt(0) + "1");  // queen-side
            chessboard.placeWhite(name, position.charAt(1) + "1");  // king-side
            // Black
            chessboard.placeBlack(name, position.charAt(0) + "8"); // queen-side
            chessboard.placeBlack(name, position.charAt(1) + "8");  // king-side
        }

        return chessboard;
    }

    /** A collection of active (i.e. not captured) pieces. */
    private final List<Piece> activePieces;

    /** A collection of captured pieces. */
    private final List<Piece> capturedPieces;

    /**
     * Constructs a chessboard with a given list of Chess pieces.<p>
     * Initially, the chessboard has no captured pieces.
     * @param pieces A list of Chess pieces.
     * @throws NullPointerException If {@code pieces} is {@code null}.
     * */
    private Chessboard(List<Piece> pieces) {
        this.activePieces = Objects.requireNonNull(pieces);
        this.capturedPieces = new List<>();
    }

    /**
     * Constructs a chessboard from a JSON object containing the following required key-value
     * mappings:
     * <pre>
     * {
     *     {@value #activePiecesKey}: [
     *         ...
     *     ],
     *     {@value #capturedPiecesKey}: [
     *         ...
     *     ]
     * }</pre>
     * where both keys store a {@link JSONArray}, containing relevant Chess pieces.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If any of the required keys is missing or maps to a non-array
     *                                value type.
     * @throws FormatException Rethrown from {@link Colored#Colored(JSONObject)}, or
     *                         {@link Piece#decode(JSONObject)}.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see Piece#decode(JSONObject)
     * */
    public Chessboard(JSONObject jsonObject) {
        this(new List<>());
        // get active pieces
        JSONArray activePieces = jsonObject.getJSONArray(activePiecesKey);
        for (int i = 0; i < activePieces.length(); i++) {
            JSONObject pieceObject = activePieces.getJSONObject(i);
            this.activePieces.add(Piece.decode(pieceObject));
        }
        // get captured pieces
        JSONArray capturedPieces = jsonObject.getJSONArray(capturedPiecesKey);
        for (int i = 0; i < capturedPieces.length(); i++) {
            JSONObject pieceObject = capturedPieces.getJSONObject(i);
            this.capturedPieces.add(Piece.decode(pieceObject));
        }
    }

    /**
     * Serializes this chessboard to a JSON object of the following format:
     * <pre>
     * {
     *     {@value #activePiecesKey}: [
     *         ...
     *     ],
     *     {@value #capturedPiecesKey}: [
     *         ...
     *     ]
     * }</pre>
     * where both keys store a {@link JSONArray}, containing relevant Chess pieces.
     * @see Piece#encode()
     * */
    @Override
    public JSONObject encode() {
        JSONObject chessboardEncoded = new JSONObject();
        // encode active pieces
        JSONArray activePieces = new JSONArray();
        for (Piece piece : this) {
            activePieces.put(piece.encode());
        }
        chessboardEncoded.put(activePiecesKey, activePieces);
        // encode captured pieces
        JSONArray capturedPieces = new JSONArray();
        for (Piece piece : this.capturedPieces) {
            capturedPieces.put(piece.encode());
        }
        chessboardEncoded.put(capturedPiecesKey, capturedPieces);
        return chessboardEncoded;
    }

    /**
     * Creates a deep copy of this chessboard via JSON serialization.
     * @return A copy of this chessboard.
     * */
    public Chessboard copy() {
        return new Chessboard(this.encode());
    }

    /**
     * @return An iterator that iterates over the active pieces of this chessboard.
     * */
    @Override
    public @NotNull Iterator<Piece> iterator() {
        return this.activePieces.iterator();
    }

    /**
     * Executes a movement in this chessboard.
     * @param movement The movement to execute.
     * @return The number of points associated with the movement.
     * @throws NullPointerException If {@code movement} is {@code null}.
     * */
    public int executeMove(Movement movement) {
        return movement.execute(this);
    }

    /**
     * Undoes a movement in this chessboard.
     * @param movement The movement to undo.
     * @return The number of points associated with the movement.
     * @throws NullPointerException If {@code movement} is {@code null}.
     * */
    public int undoMove(Movement movement) {
        return movement.undo(this);
    }

    /**
     * Clears this chessboard, i.e. removes all the Chess pieces of this chessboard.<p>
     * This method does nothing if there are no pieces on this chessboard.
     * @return A boolean value indicating whether this chessboard started out empty.
     * */
    public boolean clear() {
        if (this.isEmpty()) {
            return false;
        }
        this.activePieces.clear();
        this.capturedPieces.clear();
        return true;
    }

    /**
     * Places a piece on the chessboard.
     * @param piece The piece to be placed.
     * @throws NullPointerException If {@code piece} is {@code null}.
     * */
    public void placePiece(Piece piece) {
        // remove the existing piece
        this.getPieceAt(piece.getPosition()).ifPresent(this::removePiece);
        // add the given piece
        this.activePieces.add(piece);
    }

    /**
     * Creates and places a piece of the given type, color, and position.
     * @param pieceName The class name of the piece to be created (<i>case-sensitive</i>).
     * @param color     The color of the piece.
     * @param position  The position of the piece.
     * @return The piece that was created.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws IllegalArgumentException If {@code pieceName} is invalid.
     * @see Piece#of(String, Color, Position)
     * */
    public Piece placePiece(String pieceName, Color color, Position position) {
        // throw an exception (unchecked)
        Piece newPiece = Piece.of(pieceName, color, position)
                .orElseThrow(IllegalArgumentException::new);
        this.placePiece(newPiece);
        return newPiece;
    }

    /**
     * Creates and places a <i>White</i> piece of the given type and position.
     * @param pieceName The class name of the piece to be created (<i>case-sensitive</i>).
     * @param position  The position of the piece.
     * @return The White piece that was created.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws IllegalArgumentException If {@code pieceName} is invalid.
     * @see #placePiece(String, Color, Position)
     * */
    public Piece placeWhite(String pieceName, Position position) {
        return this.placePiece(pieceName, White, position);
    }

    /**
     * Creates and places a <i>White</i> piece of the given type and position notation.<p>
     * The position notation must be of the pattern {@value Position#pattern}.
     * @param pieceName The class name of the piece to be created (<i>case-sensitive</i>).
     * @param position  The string notation of the piece's position.
     * @return The White piece that was created.
     * @throws FormatException If {@code position} is invalid.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws IllegalArgumentException If {@code pieceName} is invalid.
     * @see #placePiece(String, Color, Position)
     * */
    public Piece placeWhite(String pieceName, String position) {
        return this.placeWhite(pieceName, Position.at(position));
    }

    /**
     * Creates and places a <i>Black</i> piece of the given type and position.
     * @param pieceName The class name of the piece to be created (<i>case-sensitive</i>).
     * @param position  The position of the piece.
     * @return The Black piece that was created.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws IllegalArgumentException If {@code pieceName} is invalid.
     * @see #placePiece(String, Color, Position)
     * */
    public Piece placeBlack(String pieceName, Position position) {
        return this.placePiece(pieceName, Black, position);
    }

    /**
     * Creates and places a <i>Black</i> piece of the given type and position notation.<p>
     * The position notation must be of the pattern {@value Position#pattern}.
     * @param pieceName The class name of the piece to be created (<i>case-sensitive</i>).
     * @param position  The string notation of the piece's position.
     * @return The Black piece that was created.
     * @throws FormatException If {@code position} is invalid.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws IllegalArgumentException If {@code pieceName} is invalid.
     * @see #placePiece(String, Color, Position)
     * */
    public Piece placeBlack(String pieceName, String position) {
        return this.placeBlack(pieceName, Position.at(position));
    }

    /**
     * Vacates the position, removing the occupant piece if any.
     * @param position The position to vacate.
     * @return A boolean value indicating whether a piece was removed to vacate the position.
     * @throws NullPointerException If {@code position} is {@code null}.
     * */
    public boolean vacatePosition(Position position) {
        Optional<Piece> pieceAtPosition = this.getPieceAt(
                Objects.requireNonNull(position)
        );
        if (pieceAtPosition.isPresent()) {
            this.removePiece(pieceAtPosition.get());
            return true;
        }
        return false;
    }

    /**
     * Vacates the position, removing the occupant piece if any.
     * @param position The string notation of the position to be vacated.
     * @return A boolean value indicating whether a piece was removed to vacate the position.
     * @throws NullPointerException If {@code position} is {@code null}.
     * @throws FormatException Rethrown from {@link Position#at(String)}.
     * */
    public boolean vacatePositionAt(String position) {
        return this.vacatePosition(Position.at(position));
    }

    /**
     * Removes the given piece from the active pieces of this chessboard.
     * @param piece The piece to be removed.
     * @return An optional piece that was removed, which is empty if {@code piece} did not exist.
     * */
    private Optional<Piece> removePiece(Piece piece) {
        return this.activePieces.removeFirst(piece);
    }

    /**
     * Captures the given piece by performing the following:
     * <ol>
     *     <li>Removes the captured piece from the collection of active pieces;</li>
     *     <li>Adds the captured piece to the collection of captured pieces.</li>
     * </ol>
     * <b>Precondition:</b> {@code capturedPiece} must exist on the chessboard and be active
     * (i.e. not already captured).<p>
     * <b>Note:</b> This method is meant to be called by {@link Capture} only, for it requires
     * the presence of the captured piece with absolute certainty.
     * @param capturedPiece The piece to be captured.
     * @throws NullPointerException If {@code capturedPiece} is {@code null}.
     * @throws java.util.NoSuchElementException If {@code capturedPiece} does not exist on the
     *                                          chessboard or has already been captured.
     * */
    public void capturePiece(Piece capturedPiece) {
        Objects.requireNonNull(capturedPiece);
        this.removePiece(capturedPiece).orElseThrow();
        this.capturedPieces.add(capturedPiece);
    }

    /**
     * Undoes capturing the given piece by performing the following:
     * <ol>
     *     <li>Removes the captured piece from the collection of captured pieces;</li>
     *     <li>Adds the captured piece to the collection of active pieces.</li>
     * </ol>
     * <b>Precondition:</b> {@code capturedPiece} must exist on the chessboard and be captured
     * (i.e. not active), and the chessboard must not already have another piece at
     * {@code capturedPiece}'s position.<p>
     * <b>Note:</b> This method is meant to be called by {@link Capture} only, for it must be
     * preceded by a call to {@link #capturePiece(Piece)}.
     * @param capturedPiece The piece that was captured.
     * @throws java.util.NoSuchElementException If {@code capturedPiece} does not exist on the
     *                                          chessboard or is already active.
     * @throws NullPointerException If {@code capturedPiece} is {@code null}.
     * */
    public void undoCapturePiece(Piece capturedPiece) {
        this.capturedPieces.removeFirst(
                Objects.requireNonNull(capturedPiece)
        ).orElseThrow();
        this.placePiece(capturedPiece);
    }

    /**
     * Finds and returns the piece at the given position, if any.
     * @param position The position.
     * @return An optional piece at the given position, which is empty if no piece exists at the
     *         position.
     * @throws NullPointerException If {@code position} is {@code null}.
     * */
    public Optional<Piece> getPieceAt(Position position) {
        return this.activePieces.findFirst(piece -> piece.isAt(
                Objects.requireNonNull(position)
        ));
    }

    /**
     * Finds and returns the piece at the given position, if any.
     * @param position The string notation of the position.
     * @return An optional piece at the given position, which is empty if no piece exists at the
     *         given position.
     * @throws NullPointerException If {@code position} is {@code null}.
     * @throws FormatException Rethrown from {@link Position#at(String)}.
     * */
    public Optional<Piece> getPieceAt(String position) {
        return this.getPieceAt(Position.at(position));
    }

    /**
     * @param position A position.
     * @return A boolean value indicating whether there exists a piece at the given position.
     * */
    public boolean hasPieceAt(Position position) {
        return this.getPieceAt(position).isPresent();
    }

    /**
     * Finds and returns the piece (active or captured) with the given identifier string.
     * @param identifier A Version 4 UUID string.
     * @return An optional piece with {@code identifier}, which is empty if no piece on this
     *         chessboard has {@code identifier}.
     * @throws NullPointerException If {@code identifier} is {@code null}.
     * */
    public Optional<Piece> getPieceWith(String identifier) {
        return this.getAllPieces().findFirst(piece -> {
            // get piece with same identifier
            return piece.getIdentifier().equals(
                    Objects.requireNonNull(identifier)
            );
        });
    }

    /**
     * A color is in check <i>if and only if</i> one of the opponent's non-King pieces can capture
     * the color's King.
     * @param color The color to be checked.
     * @return A boolean value indicating whether the given color is in check.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public boolean isInCheck(Color color) {
        // if any non-King enemy piece has any move that checks the enemy (this side)
        return this.getOpponentPieces(color).orMap(enemyPiece -> {
            // since a King won't ever be checked by another King
            // avoid self/mutual recursion between Kings
            if (!(enemyPiece instanceof King)) {
                return enemyPiece.getCandidateMoves(this).orMap(move -> {
                    // the enemy piece has a capture move that checks the enemy (this side)
                    return (move instanceof Capture) && ((Capture) move).isCheckingOpponent(this);
                });
            }
            return false;
        });
    }

    /**
     * A {@link Colored} object is in check <i>if and only if</i> one of the opponent's non-King
     * pieces can capture the object's King.
     * @param colored The colored object to be checked.
     * @return A boolean value indicating whether the color of {@code colored} is in check.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public boolean isInCheck(Colored colored) {
        return this.isInCheck(colored.color);
    }

    /**
     * A color is checkmated <i>if and only if</i> it is in check <i>and</i> has no legal moves.
     * @param color The color to be checked.
     * @return A boolean value indicating whether the given color has been checkmated.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public boolean isCheckmated(Color color) {
        if (!this.isInCheck(color)) {
            return false;
        }
        // check if no allied pieces have legal moves
        return this.getActivePieces(color).andMap(piece -> {
            // if the piece has no legal moves
            return piece.getLegalMoves(this).isEmpty();
        });
    }

    /**
     * A {@link Colored} object is checkmated <i>if and only if</i> it is in check <i>and</i> has
     * no legal moves.
     * @param colored The colored object to be checked.
     * @return A boolean value indicating whether the color of {@code colored} has been checkmated.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public boolean isCheckmated(Colored colored) {
        return this.isCheckmated(colored.color);
    }

    /**
     * A color is stalemated <i>if and only if</i> it is not in check <i>and</i> has no legal moves.
     * @param color The color to be checked.
     * @return A boolean value indicating whether the given color has been stalemated.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public boolean isStalemated(Color color) {
        // one is stalemated iff it (is NOT in check) AND (has no legal move)
        if (this.isInCheck(color)) {
            return false;
        }
        // not in check, check if no allied pieces have legal moves
        return this.getActivePieces(color).andMap(piece -> {
            // if the piece has no legal moves
            return piece.getLegalMoves(this).isEmpty();
        });
    }

    /**
     * A {@link Colored} object is stalemated <i>if and only if</i> it is not in check <i>and</i>
     * has no legal moves.
     * @param colored The colored object to be checked.
     * @return A boolean value indicating whether the given color has been stalemated.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public boolean isStalemated(Colored colored) {
        return this.isStalemated(colored.color);
    }

    /**
     * A position is under attack <i>if and only if</i> no piece occupies it <i>and</i> at least
     * one of the opponent's non-Pawn's regular moves threatens it.
     * @param position The position to be checked.
     * @param colored  The reference {@link Colored} object.
     * @return A boolean indicating whether {@code position} is under attack from {@code colored}'s
     *         point of view.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public boolean isPositionUnderAttack(Position position, Colored colored) {
        if (this.hasPieceAt(position)) {
            return false;
        }
        return this.getOpponentPieces(colored).orMap(opponentPiece -> {
            return opponentPiece.getCandidateMoves(this).orMap(move -> {
                if (move.getClass() == RegularMove.class) {
                    Piece piece = move.getPieceFrom(this);
                    return !(piece instanceof Pawn)
                            && move.getFinalPosition().equals(position);
                }
                return false;
            });
        });
    }

    /**
     * Returns a collection of all the pieces associated with this chessboard.
     * @return A shallow-copied collection of pieces.
     * */
    public List<Piece> getAllPieces() {
        List<Piece> allPieces = this.activePieces.copy();
        allPieces.addAll(this.capturedPieces);
        return allPieces;
    }

    /**
     * Returns a collection of active pieces associated with this chessboard.
     * @return A shallow-copied collection of active pieces.
     * */
    public List<Piece> getActivePieces() {
        return this.activePieces.copy();
    }

    /**
     * Returns a collection of the given color's active pieces associated with this chessboard.
     * @param color The color.
     * @return A shallow-copied collection of {@code color}'s active pieces.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public List<Piece> getActivePieces(Color color) {
        return this.activePieces.filtered(piece -> piece.isColor(color));
    }

    /**
     * Returns a collection of the given {@link Colored} object's active pieces associated with
     * this chessboard.
     * @param colored The colored object.
     * @return A shallow-copied collection of {@code colored}'s active pieces.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public List<Piece> getActivePieces(Colored colored) {
        return this.getActivePieces(colored.color);
    }

    /**
     * Returns a collection of captured pieces associated with this chessboard.
     * @return A shallow-copied collection of captured pieces.
     * */
    public List<Piece> getCapturedPieces() {
        return this.capturedPieces.copy();
    }

    /**
     * Returns a collection of the opponent's (relative to the given color) active pieces
     * associated with this chessboard.
     * @param color The color of reference.
     * @return A shallow-copied collection of {@code color}'s opponent's active pieces.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public List<Piece> getOpponentPieces(Color color) {
        return this.getActivePieces(color.opposite());
    }

    /**
     * Returns a collection of the opponent's (relative to the given colored object) active pieces
     * associated with this chessboard.
     * @param colored The {@link Colored} object of reference.
     * @return A shallow-copied collection of {@code colored}'s opponent's active pieces.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public List<Piece> getOpponentPieces(Colored colored) {
        return this.getOpponentPieces(colored.color);
    }

    /**
     * Computes and returns all legal candidate movements for the given color according to the
     * current configuration of the chessboard.
     * @param color The color.
     * @return A list of legal movements.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public List<Movement> getAllLegalMoves(Color color) {
        List<Movement> allMoves = new List<>();
        this.getActivePieces(color)
                .forEach(piece -> allMoves.addAll(piece.getLegalMoves(this)));
        return allMoves;
    }

    /**
     * Computes and returns all legal candidate movements for the given color according to the
     * current configuration of the chessboard.
     * @param colored A {@link Colored} object.
     * @return A list of legal movements.
     * @throws NullPointerException If {@code colored} is {@code null}.
     * */
    public List<Movement> getAllLegalMoves(Colored colored) {
        return this.getAllLegalMoves(colored.color);
    }

    /**
     * A generator that generates the next possible states of this chessboard as an iterator.<p>
     * Each legal candidate movement corresponds to a unique state of the chessboard in the future.
     * This class produces a {@link KeyValuePair} object consisting of a legal movement and its
     * corresponding chessboard during each iteration. <p>
     * During each iteration, this chessboard is copied and a candidate movement is executed on the
     * newly-generated copy. Therefore, any attempt to modify those chessboards does not affect
     * this chessboard.
     * @see #stateGenerator(Color)
     */
    private class StateGenerator implements Iterator<KeyValuePair<Movement, Chessboard>> {

        private final Iterator<Movement> allMovesIterator;

        private StateGenerator(Color color) {
            this.allMovesIterator = Chessboard.this.getAllLegalMoves(color).iterator();
        }

        @Override
        public boolean hasNext() {
            return this.allMovesIterator.hasNext();
        }

        @Override
        public KeyValuePair<Movement, Chessboard> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            Movement nextMove = this.allMovesIterator.next();
            Chessboard newState = Chessboard.this.copy();
            newState.executeMove(nextMove);
            return new KeyValuePair<>(nextMove, newState);
        }
    }

    /**
     * A more computationally-efficient version of the method {@link #getNextStates(Color)} where
     * the next states are generated on demand.
     * @param color The color of the initiator.
     * @return An iterable object that uses {@link StateGenerator} as its iterator.
     * @throws NullPointerException If {@code color} is {@code null}.
     * @see StateGenerator
     * */
    public Iterator<KeyValuePair<Movement, Chessboard>> stateGenerator(Color color) {
        return new StateGenerator(color);
    }

    /**
     * Returns an iterable object that returns a two-value tuple consisting of legal candidate
     * movements and their corresponding states (chessboards) during each iteration.
     * @param color The color of the initiator.
     * @return An iterable object that iterates over next states of this chessboard.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public Iterable<KeyValuePair<Movement, Chessboard>> getNextStates(Color color) {
        return () -> this.stateGenerator(color);
    }

    /**
     * @return A boolean value indicating whether this chessboard is empty.
     * */
    public boolean isEmpty() {
        return this.getAllPieces().isEmpty();
    }

}
