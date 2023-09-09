package chess.ui.gui.views.game.board;

import chess.model.pieces.*;
import chess.model.exceptions.GameException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import static chess.ui.Presets.*;

public class PieceComponent extends JPanel {

    private final Piece piece;

    private final Point anchor;

    private boolean isDragging = false;

    // EFFECTS: constructs a piece component from the given piece model and
    //          the square at which it is located
    //          sets highlighted state to false initially
    public PieceComponent(Piece piece, Square square) {
        super();
        this.piece = piece;
        this.setOpaque(false);

        this.setSize(pieceSize);

        // compute location relative to square
        Point location = square.getLocation();
        location.x += (square.getWidth() - this.getWidth()) / 2;
        location.y += (square.getHeight() - this.getHeight()) / 2;
        this.setLocation(location);
        this.anchor = location;

        this.registerMouseMotionListener();
        this.registerMouseListener(square);
    }

    private void registerMouseMotionListener() {
        PieceComponent pieceComponent = this;
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                isDragging = true;
                // set selected piece
                getChessboardView().gameWindow.getController().setSelectedPiece(piece);
                getChessboardView().renderHint();

                // bring piece component to the front
                pieceComponent.getParent().setComponentZOrder(pieceComponent,
                        0);

                Point point = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(point, pieceComponent.getParent());

                // get underlying square and dispatch mouse entered event for hover effect
                // before point is modified to update piece component's location
                @Nullable Square underlyingSquare = getChessboardView().getSquareAt(point);
                if (underlyingSquare != null) {
                    // has a square beneath current mouse position
                    // first, dispatch mouse exited event to all squares to reset hover effect
                    MouseEvent mouseExitedEvent = new MouseEvent(e.getComponent(), MouseEvent.MOUSE_EXITED,
                            e.getWhen(), e.getModifiersEx(), e.getX(), e.getY(),
                            e.getClickCount(), false);
                    for (Square square : getChessboardView()) {
                        square.dispatchEvent(mouseExitedEvent);
                    }
                    // then, dispatch mouse entered event to the underlying square for hover effect
                    MouseEvent mouseEnteredEvent = new MouseEvent(e.getComponent(), MouseEvent.MOUSE_ENTERED,
                            e.getWhen(), e.getModifiersEx(), e.getX(), e.getY(),
                            e.getClickCount(), false);
                    underlyingSquare.dispatchEvent(mouseEnteredEvent);
                }

                // set location for piece component to mouse position
                point.y -= pieceComponent.getHeight() / 2;
                point.x -= pieceComponent.getWidth() / 2;
                pieceComponent.setLocation(point.x, point.y);
            }
        });
    }

    // MODIFIES: this (component)
    // EFFECTS: registers mouse listener to handle hover effects and dragging behavior
    //          the piece (component) submits move to the parent chessboard view upon
    //            mouse releases, if the move is illegal, the piece's location gets reset
    //          upon clicking, the piece's highlighted state toggles and notifies the
    //            parent chessboard view to render hints for the piece correspondingly
    //          NOTE: since the piece is opaque and hence blocking the mouse events from
    //                reaching the underlying square panels, the hover effect is not
    //                triggered; to fix this, the piece propagates the mouse events
    //                associated with hovering to its corresponding square directly,
    //                achieving the desired hovering effect when hovering over the piece
    private void registerMouseListener(Square square) {
        PieceComponent pieceComponent = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (isDragging) {
                    // submit move only on drag ended
                    isDragging = false;

                    Point point = e.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(point, pieceComponent.getParent());
                    try {
                        getChessboardView().submitMove(point);
                    } catch (GameException gameException) {
                        // illegal move, back to anchor
                        pieceComponent.setLocation(anchor);
                        getChessboardView().gameWindow.getController().setSelectedPiece(null);
                        getChessboardView().renderHint();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                square.dispatchEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                square.dispatchEvent(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!isDragging) {
                    square.dispatchEvent(e);
                }
            }
        });
    }

    // EFFECTS: paints the component and applies piece-specific offsets to their positioning
    @Override
    protected void paintComponent(Graphics g) {
        String textSymbol = String.valueOf(this.piece.getCharacterSymbol(true));
        Color textColor = this.piece.isWhite() ? white : black;

        Font font = g.getFont().deriveFont(pieceFontSize);
        FontMetrics metrics = g.getFontMetrics(font);

        int x = 0;
        int y = metrics.getHeight() - 28;
        // special adjustments
        if (this.piece instanceof Bishop) {
            x -= 2;
            y += 2;
        } else if (this.piece instanceof Queen) {
            x -= 1;
        } else if (this.piece instanceof Rook
                || this.piece instanceof Pawn) {
            x += 1;
        }
        g.setFont(font);
        g.setColor(textColor);
        g.drawString(textSymbol, x, y);
    }

    public Piece getPiece() {
        return this.piece;
    }

    private ChessboardView getChessboardView() {
        return (ChessboardView) this.getParent().getParent();
    }

}
