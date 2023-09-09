package chess.model;

import chess.model.moves.RegularMove;
import chess.model.pieces.*;
import chess.model.exceptions.FormatException;
import chess.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.BiPredicate;

import static chess.model.Color.Black;
import static chess.model.Color.White;
import static org.junit.jupiter.api.Assertions.*;

public class ChessboardTest {

    private Chessboard emptyBoard;

    private Chessboard endgameBoard;

    private Chessboard standardBoard;

    @BeforeEach
    void initialize() {
        emptyBoard = Chessboard.empty();
        standardBoard = Chessboard.standard();
        endgameBoard = Chessboard.empty();
        endgameBoard.placeWhite("King", "e1");
        endgameBoard.placeWhite("Pawn", "a2");
        endgameBoard.placeBlack("King", "e8");
        endgameBoard.placeBlack("Pawn", "b3");
    }

    private final BiPredicate<? super Piece, ? super Piece> pieceEquivalencePredicate = (piece1, piece2) -> {
        return (piece1.getClass() == piece2.getClass())
                && piece1.isAlliedTo(piece2)
                && piece1.isAt(piece2.getPosition());
    };

    private void testPieceAtPosition(Chessboard chessboard,
                                     Position position, Class<? extends Piece> pieceClass, Color color) {
        assertTrue(standardBoard.hasPieceAt(position));
        chessboard.getPieceAt(position).ifPresentOrElse(piece -> {
            assertTrue(pieceClass.isInstance(piece));
            assertTrue(piece.isColor(color));
        }, Assertions::fail);
    }

    private void testIsPiece(Piece piece, Class<? extends Piece> pieceClass, Color color) {
        assertTrue(pieceClass.isInstance(piece));
        assertTrue(piece.isColor(color));
    }

    private void testPieceEquivalence(Piece piece1, Piece piece2) {
        assertTrue(pieceEquivalencePredicate.test(piece1, piece2));
    }

    private void testChessboardConfiguration(Chessboard chessboard,
                                             List<Piece> activePiecesTemplate,
                                             List<Piece> capturedPiecesTemplate) {
        // test active pieces
        List<Piece> activePieces = chessboard.getActivePieces();
        assertEquals(activePiecesTemplate.size(), activePieces.size());
        for (Piece pieceTemplate : activePiecesTemplate) {
            assertEquals(1, activePieces.count(piece -> {
                return pieceEquivalencePredicate.test(piece, pieceTemplate);
            }));
        }

        // test captured pieces
        List<Piece> capturedPieces = chessboard.getCapturedPieces();
        assertEquals(capturedPiecesTemplate.size(), capturedPieces.size());
        for (Piece pieceTemplate : capturedPiecesTemplate) {
            assertEquals(1, capturedPieces.count(piece -> {
                return pieceEquivalencePredicate.test(piece, pieceTemplate);
            }));
        }
    }

    @Test
    void testEmptyBoardSetup() {
        assertTrue(emptyBoard.isEmpty());
        for (Piece p : emptyBoard) {
            fail();
        }
    }

    @Test
    void testStandardBoardSetup() {
        assertEquals(32, standardBoard.getAllPieces().size());
        assertEquals(32, standardBoard.getActivePieces().size());
        assertTrue(standardBoard.getCapturedPieces().isEmpty());

        // test for pawns
        for (String file : Position.files) {
            Position whitePawnPosition = Position.at(file + 2);
            Position blackPawnPosition = Position.at(file + 7);

            testPieceAtPosition(standardBoard, whitePawnPosition, Pawn.class, White);
            testPieceAtPosition(standardBoard, blackPawnPosition, Pawn.class, Black);
        }

        // test for Kings
        Position whiteKingPosition = Position.at("e1");
        testPieceAtPosition(standardBoard, whiteKingPosition, King.class, White);

        Position blackKingPosition = Position.at("e8");
        testPieceAtPosition(standardBoard, blackKingPosition, King.class, Black);

        // test for Queens
        Position whiteQueenPosition = Position.at("d1");
        testPieceAtPosition(standardBoard, whiteQueenPosition, Queen.class, White);

        Position blackQueenPosition = Position.at("d8");
        testPieceAtPosition(standardBoard, blackQueenPosition, Queen.class, Black);

        // test for Bishops, Knights, and Rooks
        List<String> bnrPositions = List.of("cf", "bg", "ah");
        List<Class<? extends Piece>> bnrNames = List.of(
                Bishop.class, Knight.class, Rook.class
        );
        for (int i = 0; i < bnrNames.size(); i++) {
            Class<? extends Piece> pieceClass = bnrNames.get(i);
            String position = bnrPositions.get(i);
            // White
            Position whiteQueenSide = Position.at(position.charAt(0) + "1");
            testPieceAtPosition(standardBoard, whiteQueenSide, pieceClass, White);
            Position whiteKingSide = Position.at(position.charAt(1) + "1");
            testPieceAtPosition(standardBoard, whiteKingSide, pieceClass, White);
            // Black
            Position blackQueenSide = Position.at(position.charAt(0) + "8");
            testPieceAtPosition(standardBoard, blackQueenSide, pieceClass, Black);
            Position blackKingSide = Position.at(position.charAt(1) + "8");
            testPieceAtPosition(standardBoard, blackKingSide, pieceClass, Black);
        }
    }

    @Test
    void testCopy() {
        // no pieces at all
        Chessboard oldEndgameBoard = emptyBoard;
        emptyBoard = emptyBoard.copy();
        assertNotSame(oldEndgameBoard, emptyBoard);
        testEmptyBoardSetup();

        // has active pieces but no captured pieces
        Chessboard oldStandardBoard = standardBoard;
        standardBoard = standardBoard.copy();
        assertNotSame(oldStandardBoard, standardBoard);
        testStandardBoardSetup();

        // has active pieces and captured pieces
        endgameBoard.capturePiece(
                endgameBoard.getPieceAt("b3").orElseThrow()
        );
        oldEndgameBoard = endgameBoard;
        endgameBoard = endgameBoard.copy();
        assertNotSame(oldEndgameBoard, endgameBoard);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        new King(Black, Position.at("e8"))
                ),
                List.of(new Pawn(Black, Position.at("b3")))
        );
    }

    @Test
    void testExecuteMovementNullMovement() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.executeMove(null));
    }

    @Test
    void testExecuteMovement() {
        // reduced test case, thorough cases are specified in Movement Tests
        Piece whitePawn = endgameBoard.getPieceAt("a2").orElseThrow();
        RegularMove regularMove = new RegularMove(whitePawn, Position.at("a3"));
        assertEquals(0, endgameBoard.executeMove(regularMove));
        assertEquals(regularMove.getFinalPosition(), whitePawn.getPosition());
    }

    @Test
    void testUndoMovementNullMovement() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.undoMove(null));
    }

    @Test
    void testUndoMovement() {
        // reduced test case, thorough cases are specified in Movement Tests
        Piece whitePawn = endgameBoard.getPieceAt("a2").orElseThrow();
        RegularMove regularMove = new RegularMove(whitePawn, Position.at("a3"));
        endgameBoard.executeMove(regularMove);
        assertEquals(0, endgameBoard.undoMove(regularMove));
        assertEquals(regularMove.getInitialPosition(), whitePawn.getPosition());
    }

    @Test
    void testClearAlreadyCleared() {
        assertFalse(emptyBoard.clear());
        assertTrue(emptyBoard.isEmpty());
    }

    @Test
    void testClearHasPieces() {
        assertTrue(endgameBoard.clear());
        assertTrue(endgameBoard.isEmpty());
    }

    @Test
    void testPlacePieceNullPiece() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.placePiece(null));
    }

    @Test
    void testPlacePieceUnoccupied() {
        Piece newPawn = new Pawn(White, Position.at("h2"));
        endgameBoard.placePiece(newPawn);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        newPawn,
                        new King(Black, Position.at("e8")),
                        new Pawn(Black, Position.at("b3"))
                ),
                List.of()
        );
    }

    @Test
    void testPlacePieceOccupied() {
        Piece newBishop = new Bishop(Black, Position.at("b3"));
        endgameBoard.placePiece(newBishop);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        newBishop,
                        new King(Black, Position.at("e8"))
                ),
                List.of()
        );
    }

    @Test
    @DisplayName("placePiece(String, Color, Position): invalid piece name")
    void testConvenientPlacePieceInvalidName() {
        assertThrows(IllegalArgumentException.class,
                () -> emptyBoard.placePiece("abc", White, Position.at("b2")));
    }

    @Test
    @DisplayName("placePiece(String, Color, Position): null arguments")
    void testConvenientPlacePieceNullArguments() {
        Position newPosition = Position.at("b2");
        // null piece name
        assertThrows(NullPointerException.class,
                () -> emptyBoard.placePiece(null, White, newPosition));
        // null color
        assertThrows(NullPointerException.class,
                () -> emptyBoard.placePiece("Pawn", null, newPosition));
        // null position
        assertThrows(NullPointerException.class,
                () -> emptyBoard.placePiece("Pawn", White, null));
    }

    @Test
    @DisplayName("placePiece(String, Color, Position): success")
    void testConvenientPlacePieceSuccess() {
        // reduced test case
        Piece newPiece = new Pawn(White, Position.at("b2"));
        Piece placedPiece = endgameBoard.placePiece("Pawn", White, Position.at("b2"));
        testPieceEquivalence(newPiece, placedPiece);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        newPiece,
                        new King(Black, Position.at("e8")),
                        new Pawn(Black, Position.at("b3"))
                ),
                List.of()
        );
    }

    @Test
    void testPlaceWhite() {
        Piece newPiece = new Pawn(White, Position.at("b2"));
        Piece placedPiece = endgameBoard.placeWhite("Pawn", "b2");
        testPieceEquivalence(newPiece, placedPiece);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        newPiece,
                        new King(Black, Position.at("e8")),
                        new Pawn(Black, Position.at("b3"))
                ),
                List.of()
        );
    }

    @Test
    void testPlaceBlack() {
        Piece newPiece = new Pawn(Black, Position.at("c3"));
        Piece placedPiece = endgameBoard.placeBlack("Pawn", "c3");
        testPieceEquivalence(newPiece, placedPiece);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        newPiece,
                        new King(Black, Position.at("e8")),
                        new Pawn(Black, Position.at("b3"))
                ),
                List.of()
        );
    }

    @Test
    void testVacatePositionNullPosition() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.vacatePosition(null));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.vacatePositionAt(null));
    }

    @Test
    void testVacatePositionPieceDoesNotExist() {
        assertFalse(endgameBoard.vacatePositionAt("a8"));
    }

    @Test
    void testVacatePositionPieceExists() {
        assertTrue(endgameBoard.vacatePositionAt("a2"));
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new King(White, Position.at("e1")),
                        new King(Black, Position.at("e8")),
                        new Pawn(Black, Position.at("b3"))
                ),
                List.of()
        );
    }

    @Test
    @DisplayName("vacatePositionAt(String): invalid position notation")
    void testConvenientVacatePositionAtInvalidPosition() {
        assertThrows(FormatException.class,
                () -> endgameBoard.vacatePositionAt("a9"));
    }

    @Test
    void testCapturePieceNullPiece() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.capturePiece(null));
    }

    @Test
    void testCapturePieceDoesNotExist() {
        assertThrows(NoSuchElementException.class,
                () -> endgameBoard.capturePiece(
                        new Pawn(White, Position.at("a2"))
                ));
    }

    @Test
    void testCapturePieceExists() {
        Piece capturedPiece = endgameBoard.getPieceAt("b3").orElseThrow();
        endgameBoard.capturePiece(capturedPiece);
        testChessboardConfiguration(endgameBoard,
                List.of(
                        new Pawn(White, Position.at("a2")),
                        new King(White, Position.at("e1")),
                        new King(Black, Position.at("e8"))
                ),
                List.of(
                        new Pawn(Black, Position.at("b3"))
                )
        );
    }

    @Test
    void testUndoCapturePieceNullPiece() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.undoCapturePiece(null));
    }

    @Test
    void testUndoCapturePieceDoesNotExistOrNotCaptured() {
        // does not exist in chessboard
        assertThrows(NoSuchElementException.class,
                () -> endgameBoard.undoCapturePiece(
                        new Pawn(Black, Position.at("b3"))
                ));
        // not captured
        Piece capturedPiece = endgameBoard.getPieceAt("b3").orElseThrow();
        assertThrows(NoSuchElementException.class,
                () -> endgameBoard.undoCapturePiece(capturedPiece));
    }

    @Test
    void testGetPieceAtNullPosition() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.getPieceAt((Position) null));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.getPieceAt((String) null));
    }

    @Test
    @DisplayName("getPieceAt(String): invalid position notation")
    void testConvenientGetPieceAtInvalidPosition() {
        assertThrows(FormatException.class,
                () -> endgameBoard.getPieceAt("a9"));
    }

    @Test
    void testGetPieceAtPositionNoPiece() {
        assertTrue(endgameBoard.getPieceAt(Position.at("a3")).isEmpty());
        assertTrue(endgameBoard.getPieceAt("a3").isEmpty());
    }

    @Test
    void testGetPieceWithNullId() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.getPieceWith(null));
    }

    @Test
    void testGetPieceWithIdDoesNotExist() {
        String nonexistentId = UUID.randomUUID().toString();
        assertTrue(endgameBoard.getPieceWith(nonexistentId).isEmpty());
    }

    @Test
    void testGetPieceWithIdSuccess() {
        Piece whitePawn = endgameBoard.getPieceAt("a2").orElseThrow();
        String id = whitePawn.getIdentifier();
        assertEquals(whitePawn,
                endgameBoard.getPieceWith(id).orElseThrow());
    }

    @Test
    void testIsInCheckNullArguments() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isInCheck((Color) null));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isInCheck((Colored) null));
    }

    @Test
    void testIsInCheck() {
        assertFalse(endgameBoard.isInCheck(Colored.White));
        assertFalse(endgameBoard.isInCheck(Colored.Black));

        // White in check setup
        endgameBoard.placeBlack("Rook", "e5");
        assertTrue(endgameBoard.isInCheck(Colored.White));
        assertFalse(endgameBoard.isInCheck(Colored.Black));

        // Black in check setup
        endgameBoard.placeWhite("Rook", "e5");
        assertFalse(endgameBoard.isInCheck(Colored.White));
        assertTrue(endgameBoard.isInCheck(Colored.Black));
    }

    @Test
    void testIsInCheckmatedNullArguments() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isCheckmated((Color) null));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isCheckmated((Colored) null));
    }

    @Test
    void testIsCheckmated() {
        // reduced test cases: White only
        assertFalse(endgameBoard.isCheckmated(Colored.White));
        assertFalse(endgameBoard.isCheckmated(Colored.Black));

        // White in check but not checkmated
        endgameBoard.placeBlack("Rook", "e5");
        assertFalse(endgameBoard.isCheckmated(Colored.White));
        // White is checkmated
        endgameBoard.placeBlack("Queen", "e2");
        assertTrue(endgameBoard.isCheckmated(Colored.White));
        assertFalse(endgameBoard.isCheckmated(Colored.Black));
    }

    @Test
    void testIsStalematedNullArguments() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isStalemated((Color) null));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isStalemated((Colored) null));
    }

    @Test
    void testIsStalemated() {
        // reduced test cases: White only
        assertFalse(endgameBoard.isStalemated(Colored.White));
        assertFalse(endgameBoard.isStalemated(Colored.Black));

        // White in check, not stalemated
        Piece blackRook = endgameBoard.placeBlack("Rook", "e5");
        assertFalse(endgameBoard.isStalemated(Colored.White));

        // White not in check and stalemated
        blackRook.moveTo(Position.at("d5"));
        endgameBoard.placeBlack("Rook", "f5");
        endgameBoard.placeBlack("Queen", "e7");
        endgameBoard.placeBlack("Pawn", "e2");
        endgameBoard.vacatePositionAt("a2");
        assertTrue(endgameBoard.isStalemated(Colored.White));
    }

    @Test
    void testIsPositionUnderAttackNullArguments() {
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isPositionUnderAttack(null, Colored.White));
        assertThrows(NullPointerException.class,
                () -> endgameBoard.isPositionUnderAttack(Position.at("c1"), null));
    }

    @Test
    void testIsPositionUnderAttackOccupied() {
        assertFalse(endgameBoard.isPositionUnderAttack(Position.at("a2"), Colored.White));
    }

    @Test
    void testIsPositionUnderAttackUnoccupied() {
        // is not under attack
        assertFalse(endgameBoard.isPositionUnderAttack(Position.at("f1"), Colored.White));
        assertFalse(endgameBoard.isPositionUnderAttack(Position.at("f8"), Colored.Black));

        // is under attack
        assertTrue(endgameBoard.isPositionUnderAttack(Position.at("e7"), Colored.White));
        assertTrue(endgameBoard.isPositionUnderAttack(Position.at("e2"), Colored.Black));
    }

    @Test
    void testGetPiecesCopied() {
        // all pieces
        List<Piece> pieces = standardBoard.getAllPieces();
        assertNotSame(standardBoard.getAllPieces(), pieces);
        pieces.remove(0);
        testStandardBoardSetup();

        // all active pieces
        pieces = standardBoard.getActivePieces();
        assertNotSame(standardBoard.getActivePieces(), pieces);
        pieces.remove(0);
        testStandardBoardSetup();

        // all captured pieces
        pieces = standardBoard.getCapturedPieces();
        assertNotSame(standardBoard.getCapturedPieces(), pieces);
        pieces.add(new Pawn(White, Position.at("a2")));
        testStandardBoardSetup();

        // all White active pieces
        pieces = standardBoard.getActivePieces(Colored.White);
        assertNotSame(standardBoard.getActivePieces(Colored.White), pieces);
        pieces.remove(0);
        testStandardBoardSetup();

        // all Black active pieces
        pieces = standardBoard.getActivePieces(Colored.Black);
        assertNotSame(standardBoard.getActivePieces(Colored.Black), pieces);
        pieces.remove(0);
        testStandardBoardSetup();
    }

}
