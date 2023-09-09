package chess.model.player;

import chess.model.Chessboard;
import chess.model.Color;
import chess.model.moves.Capture;
import chess.model.moves.Movement;
import chess.model.moves.PlayerMove;
import chess.model.exceptions.FormatException;
import chess.util.KeyValuePair;

import org.jetbrains.annotations.Nullable;

/**
 * An implementation of the most basic N-depth Minimax Search autonomous Chess-playing agent with
 * alpha-beta pruning.<p>
 * This algorithm is shown to be very slow and resource inefficient given a search depth of N > 4
 * or even just a large search space.<p>
 * This variant of the implementation also uses the most basic valuation mechanism possible, i.e.
 * the agent, on any given search path:
 * <ol>
 *     <li>accumulates positive minimax value upon capturing an adversary's piece;</li>
 *     <li>accumulates negative minimax value upon its pieces being captured by the adversary.</li>
 * </ol>
 * The minimax value is the number of points associated with the captured piece. Except when the
 * adversarial search reaches some terminal state (i.e. checkmate or stalemate), in which case a
 * significant amount is added/deducted from the accumulated minimax value to incentivize/discourage
 * to attempt a move that would likely result in mating the opponent/themselves. Note that the
 * algorithm only awards a small amount for stalemating the opponent and does not impose significant
 * punishment upon getting themselves stalemated.
 * */
@AgentInfo(displayName = "Minimax αβ4")
public final class Minimax1 extends Agent {

    /** The max search depth of the Minimax algorithm. */
    private static final int maxDepth = 4;

    /**
     * Constructs this Minimax agent with a name, color, and an initial score.<p>
     * <b>Note:</b> The player's name must not be empty or contains spaces only.
     *
     * @param name  The name of the agent.
     * @param color The color of the agent.
     * @param score The initial score of the agent.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * @throws FormatException If the stripped name is empty or contains spaces
     *                                          only.
     */
    public Minimax1(String name, Color color, int score) {
        super(name, color, score);
    }

    /**
     * Generates a legal move using the Minimax search algorithm.
     * @param chessboard The chessboard.
     * @return A {@link PlayerMove} object that describes the agent's move.
     * @throws NullPointerException If {@code chessboard} is {@code null}.
     * */
    @Override
    public PlayerMove getPlayerMove(Chessboard chessboard) {
        Movement nextMove = this.minimax(
                new KeyValuePair<>(null, chessboard), this.color,
                maxDepth, 0,
                Integer.MIN_VALUE, Integer.MAX_VALUE
        ).getKey();
        return new PlayerMove(
                nextMove.getInitialPosition(),
                nextMove.getFinalPosition(),
                this.color
        );
    }

    private KeyValuePair<Movement, Integer> minimax(KeyValuePair<Movement, Chessboard> state, Color color,
                                                    int depth, int accumulatedScore,
                                                    int alpha, int beta) {
        @Nullable Movement movement = state.getKey();
        Chessboard currentBoard = state.getValue();
        accumulatedScore += this.evaluate(state);
        // terminal nodes (checkmated): return utility
        if (depth == 0) {
            return new KeyValuePair<>(movement, accumulatedScore);
        }
        if (currentBoard.isCheckmated(color)) {
            int value = 1000;
            if (this.isColor(color))
                value *= -1;
            return new KeyValuePair<>(movement, accumulatedScore + value);
        }
        if (currentBoard.isStalemated(color)) {
            int value = 500;
            if (this.isColor(color))
                value *= -10;
            // lesser punishment
            return new KeyValuePair<>(movement, accumulatedScore + value);
        }
        // recursive clause
        boolean maximize = this.isColor(color);
        if (maximize) {
            KeyValuePair<Movement, Integer> maximalState = new KeyValuePair<>(null, Integer.MIN_VALUE);
            // for each next state
            for (KeyValuePair<Movement, Chessboard> nextState : currentBoard.getNextStates(color)) {
                // evaluate recursively the next state
                KeyValuePair<Movement, Integer> nextEvaluation = this.minimax(
                        nextState, color.opposite(), depth - 1, accumulatedScore,
                        alpha, beta
                );
                int nextValue = nextEvaluation.getValue();
                // update maximal state if applicable
                if (nextValue > maximalState.getValue()) {
                    maximalState = new KeyValuePair<>(nextState.getKey(), nextValue);
                }
                alpha = Integer.max(alpha, nextValue);
                if (beta <= alpha) {
                    break;
                }
            }
            return maximalState;
        } else {
            KeyValuePair<Movement, Integer> minimalState = new KeyValuePair<>(null, Integer.MAX_VALUE);
            // for each next state
            for (KeyValuePair<Movement, Chessboard> nextState : currentBoard.getNextStates(color)) {
                // evaluate recursively the next state
                KeyValuePair<Movement, Integer> nextEvaluation = this.minimax(
                        nextState, color.opposite(), depth - 1, accumulatedScore,
                        alpha, beta
                );
                int nextValue = nextEvaluation.getValue();
                // update minimal state if applicable
                if (nextValue < minimalState.getValue()) {
                    minimalState = new KeyValuePair<>(nextState.getKey(), nextValue);
                }
                beta = Integer.min(beta, nextValue);
                if (beta <= alpha) {
                    break;
                }
            }
            return minimalState;
        }
    }

    private int evaluate(KeyValuePair<Movement, Chessboard> state) {
        @Nullable Movement nextMove = state.getKey();
        Chessboard nextBoard = state.getValue();
        int deltaScore = 0;
        if (nextMove instanceof Capture capture) {
            deltaScore = capture.getCapturedPiece(nextBoard).getPoints();
            if (this.isOpponentTo(nextMove)) {
                deltaScore *= -1;
            }
        }
        return deltaScore;
    }

}
