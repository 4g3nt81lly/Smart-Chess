package chess.model.player;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Position;
import chess.model.moves.PlayerMove;

import java.util.Objects;

/**
 * A concrete subtype of {@link Player} that represents a human player that makes move via a user
 * interface.<p>
 * The workflow is as follows:
 * <ol>
 *     <li>A human player interacts with a user interface;</li>
 *     <li>The user interface interprets and registers the player's move by calling
 *         {@link #registerNextMove(Position, Position)};</li>
 *     <li>The user interface invokes {@link ChessGame#nextTurn()} which queries the
 *         {@link HumanPlayer} object for the next move by calling {@link #getPlayerMove(Chessboard)}.
 *     </li>
 * </ol>
 * Hence, the call to {@link ChessGame#nextTurn()} must be preceded by a call to the method
 * {@link #registerNextMove(Position, Position)}.
 * @see Player
 * @see ChessGame
 * */
@AgentInfo(displayName = "Human Player")
public final class HumanPlayer extends Player {

    /** The next move of this human player. */
    private PlayerMove nextMove;

    /**
     * Constructs this player with a name, color, and an initial score.<p>
     * <b>Note:</b> The player's name must not be empty or contains spaces only.
     * @param name  The non-empty name of the player.
     * @param color The color of the player.
     * @param score The initial score of the player.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public HumanPlayer(String name, Color color, int score) {
        super(name, color, score);
    }

    /**
     * Constructs this player with the default name, a color, and an initial score.
     * @param color The color of the player.
     * @throws NullPointerException If {@code color} is {@code null}.
     * */
    public HumanPlayer(Color color) {
        this("", color, 0);
    }

    /**
     * Returns the registered next move and resets the next move to {@code null}.<p>
     * <b>Note:</b> Any call to this method must be preceded by an appropriate call to
     * {@link #registerNextMove(Position, Position)} to ensure a move has been registered.
     * @param chessboard The chessboard.
     * @return A {@link PlayerMove} object that describes the human player's move.
     * @throws NullPointerException If no next move was registered.
     * */
    @Override
    public PlayerMove getPlayerMove(Chessboard chessboard) {
        PlayerMove nextMove = Objects.requireNonNull(this.nextMove);
        this.nextMove = null;
        return nextMove;
    }

    /**
     * Registers the next move for this human player.
     * @param initialPosition The initial position of the move.
     * @param finalPosition   The final position of the move.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public void registerNextMove(Position initialPosition, Position finalPosition) {
        this.nextMove = new PlayerMove(initialPosition, finalPosition, this.color);
    }

}
