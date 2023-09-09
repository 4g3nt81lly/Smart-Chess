package chess.ui.gui.views.game.board;

import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.exceptions.GameException;
import org.jetbrains.annotations.Nullable;

import static chess.ui.Presets.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class Square extends JPanel {

    private final Color color;

    private final Position position;

    private @Nullable PieceComponent pieceComponent;

    private boolean isHighlighted = false;

    // EFFECTS: constructs a square panel of the given color
    //          (the square color corresponds to a preset color for rendering);
    //          adds relevant mouse listener to handle hover effects
    public Square(Position position, @Nullable PieceComponent pieceComponent) {
        super();
        this.position = position;
        this.pieceComponent = pieceComponent;
        this.setPreferredSize(squareSize);
        chess.model.Color color = position.getSquareColor();
        this.color = color.isWhite() ? whiteSquare : blackSquare;
        this.setBackground(this.color);

        Color highlightColor = color.isWhite() ? whiteSquareHover : blackSquareHover;
        this.configureMouseListener(highlightColor);
    }

    private void configureMouseListener(Color highlightColor) {
        Square square = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isHighlighted) {
                    setBorder(new LineBorder(highlightColor, squareHoverBorder));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isHighlighted) {
                    setBorder(new EmptyBorder(0, 0, 0, 0));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!square.isEmpty()) {
                    // clicked on a piece
                    // get selected piece
                    @Nullable Piece selectedPiece = getChessboardView().gameWindow.getController().getSelectedPiece();
                    assert pieceComponent != null;
                    Piece piece = pieceComponent.getPiece();
                    @Nullable Piece newPiece = (selectedPiece != piece) ? piece : null;
                    getChessboardView().gameWindow.getController().setSelectedPiece(newPiece);
                    getChessboardView().renderHint();
                }
                Point point = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(point, square.getParent());
                try {
                    getChessboardView().submitMove(point);
                } catch (GameException exception) {
                    // illegal move or not selected a position
                }
            }
        });
    }

    // MODIFIES: this
    // EFFECTS: highlights the current square by the given color (background),
    //          this is done by adding a border, while toggling the state
    public void highlight(@Nullable Color color) {
        this.isHighlighted = true;
        if (color == null) {
            this.isHighlighted = false;
            color = this.color;
        }
        this.setBackground(color);
        this.setBorder(new LineBorder(this.color, squareHoverBorder));
    }

    public boolean isEmpty() {
        return this.pieceComponent == null;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPieceComponent(@Nullable PieceComponent pieceComponent) {
        this.pieceComponent = pieceComponent;
    }

    public @Nullable PieceComponent getPieceComponent() {
        return this.pieceComponent;
    }

    private ChessboardView getChessboardView() {
        return (ChessboardView) this.getParent().getParent();
    }

}
