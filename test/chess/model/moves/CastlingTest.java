package chess.model.moves;

import chess.model.Position;
import chess.model.pieces.King;
import chess.model.pieces.Piece;
import chess.model.pieces.Rook;
import chess.model.exceptions.FormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static chess.model.Colored.colorKey;
import static chess.model.TextSymbols.leftArrow;
import static chess.model.moves.Castling.rookMoveKey;
import static chess.model.moves.Movement.pieceIdKey;
import static org.junit.jupiter.api.Assertions.*;

public class CastlingTest extends RegularMoveTest {

    private Rook targetRook;

    private RegularMove rookMove;

    private Castling castling;

    @Override
    @BeforeEach
    protected void initialize() {
        super.initialize();
        // a King-side castling setup: empty passage
        chessboard.vacatePositionAt("f1");
        chessboard.vacatePositionAt("g1");

        initialPosition = Position.at("e1");
        targetPiece = chessboard.getPieceAt(initialPosition).orElseThrow();
        finalPosition = Position.at("g1");

        Position rookPosition = Position.at("h1");
        targetRook = (Rook) chessboard.getPieceAt(rookPosition).orElseThrow();
        Position rookFinalPosition = Position.at("f1");
        castling = new Castling((King) targetPiece, finalPosition, targetRook, rookFinalPosition);
        rookMove = new RegularMove(targetRook, rookFinalPosition);

        // RegularMove tests
        regularMove = castling;
        // Movement tests
        movement = castling;
        // Movable tests
        movable = castling;
        // Colored tests
        whiteObject = castling;
        // setup for Black's King-side castling
        chessboard.vacatePositionAt("f8");
        chessboard.vacatePositionAt("g8");
        King opponentsKing = (King) chessboard.getPieceAt("e8").orElseThrow();
        Rook opponentsRook = (Rook) chessboard.getPieceAt("h8").orElseThrow();
        blackObject = new Castling(opponentsKing, Position.at("g8"),
                opponentsRook, Position.at("f8"));
    }

    @Test
    void testCastlingConstructor() {
        assertEquals(rookMove.toString(chessboard), castling.rookMove.toString(chessboard));
        assertEquals(targetRook, castling.getRook(chessboard));

        Position rookFinalPosition = rookMove.getFinalPosition();

        // king is null
        assertThrows(NullPointerException.class,
                () -> new Castling(null, finalPosition, targetRook, rookFinalPosition));

        // kingFinalPosition is null
        assertThrows(NullPointerException.class,
                () -> new Castling((King) targetPiece, null, targetRook, rookFinalPosition));

        // rook is null
        assertThrows(NullPointerException.class,
                () -> new Castling((King) targetPiece, finalPosition, null, rookFinalPosition));

        // rookFinalPosition is null
        assertThrows(NullPointerException.class,
                () -> new Castling((King) targetPiece, finalPosition, targetRook, null));
    }

    @Test
    @Override
    protected void testExecute() {
        super.testExecute();
        assertTrue(castling.getRook(chessboard).isAt(rookMove.getFinalPosition()));
    }

    @Test
    @Override
    protected void testUndo() {
        super.testUndo();
        assertTrue(castling.getRook(chessboard).isAt(Position.at("h1")));
    }

    @Test
    @Override
    protected void testMovementDescriptor() {
        assertEquals(String.valueOf(leftArrow), castling.getDescriptor());
    }

    @Test
    @Override
    protected void testEncode() {
        super.testEncode();
        JSONObject castlingEncoded = castling.encode();

        assertTrue(castlingEncoded.has(rookMoveKey));
        assertEquals(rookMove.encode().toString(),
                castlingEncoded.getJSONObject(rookMoveKey).toString());
    }

    @Test
    void testDecodeMissingRookMove() {
        JSONObject invalidJSON = castling.encode();
        invalidJSON.remove(rookMoveKey);

        assertThrows(JSONException.class,
                () -> new Castling(invalidJSON, chessboard));
    }

    @Test
    void testDecodeInvalidRookMove() {
        JSONObject invalidJSON = castling.encode()
                .put(rookMoveKey, "not a JSON object");

        assertThrows(JSONException.class,
                () -> new Castling(invalidJSON, chessboard));
    }

    @Test
    void testDecodeTargetKingIsNotKing() {
        Piece anotherPiece = chessboard.getPieceAt("b1").orElseThrow();
        JSONObject invalidJSON = castling.encode()
                .put(pieceIdKey, anotherPiece.getIdentifier());

        try {
            new Castling(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("Castling cannot be applied to " + anotherPiece + ".",
                    e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeTargetRookNotFound() {
        String nonexistentId = UUID.randomUUID().toString();
        JSONObject invalidJSON = castling.encode()
                .getJSONObject(rookMoveKey).put(pieceIdKey, nonexistentId);

        try {
            new Castling(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("No piece found with ID '" + nonexistentId + "'.", e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeTargetRookIsNotRook() {
        Piece anotherPiece = chessboard.getPieceAt("h2").orElseThrow();
        JSONObject invalidJSON = castling.encode();
        invalidJSON.getJSONObject(rookMoveKey).put(pieceIdKey, anotherPiece.getIdentifier());

        try {
            new Castling(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("Castling cannot be applied to " + anotherPiece + ".",
                    e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    void testDecodeOpponentsRookMove() {
        Piece opponentsRook = chessboard.getPieceAt("h8").orElseThrow();
        JSONObject invalidJSON = castling.encode();
        invalidJSON.getJSONObject(rookMoveKey)
                .put(pieceIdKey, opponentsRook.getIdentifier())
                .put(colorKey, opponentsRook.getColor().toString());

        try {
            new Castling(invalidJSON, chessboard);
            fail("should have thrown a FormatException");
        } catch (FormatException e) {
            assertEquals("Castling cannot be applied to " + opponentsRook + ".",
                    e.getMessage());
        } catch (Exception e) {
            fail("was expecting FormatException, not " + e);
        }
    }

    @Test
    @Override
    protected void testDecodeSuccessful() {
        super.testDecodeSuccessful();

        assertDoesNotThrow(() -> {
            Castling decoded = new Castling(castling.encode(), chessboard);

            assertEquals(rookMove.toString(chessboard), decoded.rookMove.toString(chessboard));
            assertEquals(targetRook, decoded.getRook(chessboard));
            assertFalse(decoded.rookMove.willCheckOpponent());
        });
    }

}
