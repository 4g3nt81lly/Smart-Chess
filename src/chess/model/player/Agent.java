package chess.model.player;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Color;
import chess.model.moves.Movement;
import chess.model.moves.PlayerMove;
import chess.model.exceptions.FormatException;
import chess.util.List;

import java.util.Random;

/**
 * A concrete subtype of {@link Player} that represents a Chess-playing agent that makes move
 * "intelligently" and fully-autonomously.<p>
 * This class serves as a base class for all agents. Its default implementation naïvely selects
 * random (and legal) movements from the set of available ones depending on the current state of
 * the chessboard.<p>
 * An agent need not be aware of the state of the entire game, which is why only the chessboard is
 * provided. An implication of this being that, in the case of a stochastic agent (i.e. randomness
 * is involved in its decision-making process), it can never tell if the current state they receive
 * is a result of a regular turn or an undo/redo action (initiated by a human player). If an agent
 * needs to maintain any non-volatile state information, they can do so internally.
 * @apiNote All agents <b>must</b> extend this class and implement the required method
 * {@link #getPlayerMove(Chessboard)}, which serves as a supplier method to {@link ChessGame}.
 * Beware that, generally, one <i>can but should not</i> make calls to the default implementation
 * of this method, unless one has a very good reason to do so.<p>
 * Agents can be annotated with {@link AgentInfo} to customize the display name of the agent, see
 * {@link AgentInfo} for details.<p>
 * Subtypes of {@link Agent} may implement their own custom methods and maintain their own states
 * that are crucial to their functioning. Note that agents will be held responsible for managing
 * persistence, i.e. serializing and deserializing their custom states, if applicable.
 * @see Player
 * @see ChessGame
 * */
@AgentInfo(displayName = "Randomizer")
public non-sealed class Agent extends Player {

    /**
     * Constructs this agent with a name, color, and an initial score.<p>
     * <b>Note:</b> The player's name must not be empty or contains spaces only.
     * @param name  The name of the agent.
     * @param color The color of the agent.
     * @param score The initial score of the agent.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws FormatException If the stripped name is empty or contains spaces only.
     * */
    public Agent(String name, Color color, int score) {
        super(name, color, score);
    }

    /**
     * Generates a random and legal move according given the current state of the chessboard.
     * @param chessboard The chessboard.
     * @return A {@link PlayerMove} object that describes the agent's move.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public PlayerMove getPlayerMove(Chessboard chessboard) {
        List<Movement> allPossibleMoves = chessboard.getAllLegalMoves(this);
        // make a random move
        int randomIndex = (new Random()).nextInt(allPossibleMoves.size());
        Movement randomMove = allPossibleMoves.get(randomIndex);
        this.delay(1.5);
        return new PlayerMove(
                randomMove.getInitialPosition(),
                randomMove.getFinalPosition(),
                this.color
        );
    }

    /**
     * Delay code execution of the current thread for a certain period of time.
     * This is used primarily for manually extending the computation duration and should <i>not</i>
     * be used for other purposes.
     * @param duration The duration of the delay.
     * */
    protected final void delay(double duration) {
        try {
            Thread.sleep((long) (duration * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
