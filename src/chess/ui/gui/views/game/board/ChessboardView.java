package chess.ui.gui.views.game.board;

import chess.model.Chessboard;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Castling;
import chess.model.moves.Movement;
import chess.model.pieces.Piece;
import chess.model.exceptions.GameException;
import chess.model.exceptions.IllegalMove;
import chess.ui.gui.views.game.GameWindow;
import chess.util.List;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

import static chess.ui.Presets.*;

public class ChessboardView extends JPanel implements Iterable<Square> {

    final GameWindow gameWindow;

    private final JPanel boardLayer;

    private final JPanel piecesLayer;

    private final List<Square> squares = new List<>();

    public ChessboardView(GameWindow gameWindow) {
        super();
        this.gameWindow = gameWindow;

        this.setLayout(new OverlayLayout(this));
        this.boardLayer = new JPanel(new GridLayout(10, 10));

        this.addFileAxis();

        for (int rank = 8; rank >= 1; rank--) {
            this.addRankLabel(rank);

            for (int file = 1; file <= 8; file++) {
                Position position = new Position(file, rank);
                Square square = new Square(position, null);
                this.boardLayer.add(square);
                this.squares.add(square);
            }

            this.addRankLabel(rank);
        }

        this.addFileAxis();

        this.piecesLayer = new JPanel(null);
        this.piecesLayer.setOpaque(false);

        this.add(this.piecesLayer);
        this.add(this.boardLayer);
    }

    // EFFECTS: creates an empty square panel of the constant size
    private JPanel createEmptySquare() {
        JPanel emptySquare = new JPanel();
        emptySquare.setPreferredSize(squareSize);
        return emptySquare;
    }

    // MODIFIES: this
    // EFFECTS: adds file axes to the board
    private void addFileAxis() {
        this.boardLayer.add(this.createEmptySquare());
        for (String file : Position.files) {
            JLabel fileLabel = new JLabel(file);
            fileLabel.setPreferredSize(squareSize);
            fileLabel.setFont(fileLabel.getFont()
                    .deriveFont(Font.BOLD)
                    .deriveFont(axisLabelFontSize));
            fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.boardLayer.add(fileLabel);
        }
        this.boardLayer.add(this.createEmptySquare());
    }

    // MODIFIES: this
    // EFFECTS: adds rank label with the given rank number to the board
    private void addRankLabel(int rank) {
        JLabel rankLabel = new JLabel(Integer.toString(rank));
        rankLabel.setPreferredSize(squareSize);
        rankLabel.setFont(rankLabel.getFont()
                .deriveFont(Font.BOLD)
                .deriveFont(axisLabelFontSize));
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.boardLayer.add(rankLabel);
    }

    // EFFECTS: returns the position at the given location (a point),
    //          otherwise returns null if:
    //          1. no component is found at the given location
    //          2. the component found is not of a Square panel (out of bound)
    //          NOTE: the location is relative to the view's coordinate space (NOT on screen)
    public @Nullable Position getPositionAt(Point location) {
        try {
            Square squareAtLocation = (Square) this.boardLayer.findComponentAt(location);
            return squareAtLocation.getPosition();
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

    // REQUIRES: the mapping between squares and position is properly initialized
    // EFFECTS: returns the square panel corresponding to the given position
    //          otherwise returns null
    //          NOTE: this method should never return null once the squares have been
    //                properly initialized, given that the position is a valid position
    private Square getSquareAt(Position position) {
        for (Square square : this.squares) {
            if (square.getPosition().equals(position)) {
                return square;
            }
        }
        throw new RuntimeException();
    }

    public @Nullable Square getSquareAt(Point location) {
        @Nullable Position position = this.getPositionAt(location);
        return position != null ? this.getSquareAt(position) : null;
    }

    // REQUIRES: the mapping between pieces and position is properly initialized
    // EFFECTS: returns the piece (component) at the given position if any
    //          otherwise returns null (no piece at the given position)
    private @Nullable PieceComponent getPieceAt(Position position) {
        return this.getSquareAt(position).getPieceComponent();
    }

    // MODIFIES: this
    // EFFECTS: re-initializes the pieces layer from the chessboard
    public void render() {
        this.clearAllHints();

        this.clearAllPieces();
        this.piecesLayer.removeAll();
        for (Piece piece : this.getChessboard()) {
            Position position = piece.getPosition();
            Square square = this.getSquareAt(position);
            PieceComponent pieceComponent = new PieceComponent(piece, square);
            this.piecesLayer.add(pieceComponent);
            square.setPieceComponent(pieceComponent);
        }

        this.revalidate();
        this.repaint();
    }

    public void renderHint() {
        this.clearAllHints();
        // get the selected piece
        @Nullable Piece selectedPiece = this.gameWindow.getController().getSelectedPiece();
        if (selectedPiece != null) {
            // highlight all candidate positions
            for (Movement move : selectedPiece.getLegalMoves(this.getChessboard())) {
                Position highlightPosition = move.getFinalPosition();
                Square highlightSquare = this.getSquareAt(highlightPosition);
                Color highlightColor = green;
                if (move instanceof Capture) {
                    highlightColor = red;
                } else if (move instanceof Castling) {
                    highlightColor = blue;
                }
                if (move.willCheckOpponent()) {
                    highlightColor = purple;
                }
                highlightSquare.highlight(highlightColor);
            }
        }
        this.boardLayer.revalidate();
        this.boardLayer.repaint();
    }

    private void clearAllPieces() {
        this.squares.forEach(square -> square.setPieceComponent(null));
    }

    // MODIFIES: this
    // EFFECTS: clears all the hints currently rendered in the view
    private void clearAllHints() {
        this.squares.forEach(square -> square.highlight(null));
//        this.boardLayer.revalidate();
//        this.boardLayer.repaint();
    }

    // EFFECTS: interprets a move made by the given piece and the final drag destination
    //            and submits the move in the form of an interpreted initial/final-position pair
    //            to the parent game window to be further passed along the hierarchy
    //            (for further processing if needed) and eventually reach the game controller
    //            which takes care of the actual movement validation and execution
    //          throws GameException:
    //            - IllegalMove
    //              1. attempting to move a piece when the chessboard is not editable
    //              2. attempting to move a piece outside the chessboard
    //            - GameException: thrown from GameWindow::submitMove(Position, Position),
    //                indicating an illegal move was made
    public void submitMove(Point finalPoint) throws GameException {
        Position finalPosition = this.getPositionAt(finalPoint);
        if (finalPosition == null)
            // out of chessboard
            throw new IllegalMove();
        // legal move, pass to game window
        this.gameWindow.getController().submitMove(finalPosition);
    }

    // https://stackoverflow.com/a/7012365
    // EFFECTS: overrides to disable optimized drawing in order to prevent
    //          re-layering (changing Z-order) from occurring during repaints
    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    public Chessboard getChessboard() {
        return this.gameWindow.getController().getGame().getChessboard();
    }

    @Override
    public Iterator<Square> iterator() {
        return this.squares.iterator();
    }
}
