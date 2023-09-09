package chess.ui;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Position;
import chess.model.moves.Capture;
import chess.model.moves.Castling;
import chess.model.moves.Movement;
import chess.model.pieces.Piece;
import chess.model.player.Agent;
import chess.util.List;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import static chess.ui.Presets.*;

public class Test implements Printing {

    public static void main(String[] args) {
//        Test test = new Test(new ChessGame(
//                "Test", Chessboard.empty(),
//                new Minimax1("A", Color.White, 0),
//                new Minimax1("B", Color.Black, 0)
//        ));
//        Chessboard chessboard = test.game.getChessboard();
//        chessboard.placeWhite("King", "h4").incrementMoveCount();
//        chessboard.placeBlack("King", "h1").incrementMoveCount();
//        chessboard.placeWhite("Bishop", "b1");
//        chessboard.placeBlack("Knight", "a2");
//        chessboard.placeBlack("Rook", "a3");
//        chessboard.placeBlack("Bishop", "c2");
//        test.printChessboard(null);
//
//        test.game.nextTurn();
//        test.printChessboard(null);

        File file = new File("/Users/agentbilly/Desktop/Test/out/production/Test/");
        if (!file.exists()) return;

        try {
            // Convert File to a URL
            URL url = file.toURI().toURL();
            URL[] urls = new URL[] { url };



            try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                // https://stackoverflow.com/a/18424773
                ConfigurationBuilder configBuilder = new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forClassLoader(classLoader))
                        .addClassLoaders(classLoader);
                Reflections reflections = new Reflections(configBuilder);

                Set<Class<? extends Agent>> availableAgents = reflections.getSubTypesOf(Agent.class);
                System.out.println(availableAgents);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final ChessGame game;

    private Test(ChessGame game) {
        this.game = game;
    }

    void printChessboard(Position hintPosition) {
        Chessboard chessboard = this.game.getChessboard();
        // get move hints if applicable
        List<Movement> moveHints = new List<>();
        Piece hintedPiece;
        if (hintPosition != null) {
            // get candidate movements of the piece at position
            hintedPiece = chessboard.getPieceAt(hintPosition).orElseThrow();
            moveHints = hintedPiece.getLegalMoves(chessboard);
        }
        // print files
        println("   " + String.join("  ", Position.files));
        for (int rank = 8; rank >= 1; rank--) {
            // print ranks
            print(rank + " ");

            for (int file = 1; file <= 8; file++) {
                Position position = new Position(file, rank);
                java.awt.Color squareColor = position.getSquareColor().isWhite() ? whiteSquare : blackSquare;
                print(getEscapeSequence(squareColor, FillMode.background) + "\033[1m");

                // print hints if applicable
                if (hintPosition != null) {
                    // hint is requested
                    if (position.equals(hintPosition)) {
                        // piece within current square is being hinted
                        // blink the current text symbol (NOT supported in IDEs and some terminals)
                        print("\033[5m");
                    }
                    for (Movement move : moveHints) {
                        Position highlightPosition = move.getFinalPosition();
                        if (move instanceof Castling) {
                            // highlight Rook piece instead of final position
                            highlightPosition = ((Castling) move).getRook(chessboard).getPosition();
                        }
                        // is current position
                        if (position.equals(highlightPosition)) {
                            // print background sequence depending on the associated movement
                            java.awt.Color highlightColor = green;
                            if (move instanceof Capture) {
                                // capture or en passant capture, red background
                                highlightColor = red;
                            } else if (move instanceof Castling) {
                                // castling, blue background
                                highlightColor = blue;
                            }
                            // change to purple background if the move checks the enemy King
                            if (move.willCheckOpponent()) {
                                highlightColor = purple;
                            }
                            print(getEscapeSequence(highlightColor, FillMode.background));
                            break;
                        }
                        // not current position, keep looking
                    }
                }
                // print chess piece at position
                chessboard.getPieceAt(position).ifPresentOrElse(piece -> {
                    // print chess piece
                    char textSymbol = piece.getCharacterSymbol(true);
                    java.awt.Color pieceColor = piece.isWhite() ? white : black;
                    print(getEscapeSequence(pieceColor, FillMode.foreground) + " " + textSymbol + " ");
                }, () -> {
                    // empty square
                    print("   ");
                });
                // turn off blinking if applicable
                print("\033[25m");
            }
            // reset sequence: reset foreground/background
            print("\033[0;0m");
            // print ranks
            println(" " + rank);
        }
        // print files
        println("   " + String.join("  ", Position.files));
    }

}
