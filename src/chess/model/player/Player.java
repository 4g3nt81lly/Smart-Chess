package chess.model.player;

import chess.model.ChessGame;
import chess.model.Chessboard;
import chess.model.Color;
import chess.model.Colored;
import chess.model.moves.PlayerMove;
import chess.model.exceptions.FormatException;

import chess.ui.PlayerLoader;
import chess.ui.throwables.NoPlayersFound;
import org.json.JSONObject;

/**
 * An abstract class that represents a player in a Chess game.<p>
 * There are only two types of player that directly extend this class:
 * <ol>
 *     <li>{@link Agent}: A smart agent (intelligence);<br>
 *         All smart agents must extend {@link Agent} and implement required methods.
 *     </li>
 *     <li>{@link HumanPlayer}: A real human player connected via a user interface.<br>
 *         Currently, {@link HumanPlayer} cannot be extended.
 *     </li>
 * </ol>
 * In a game, {@link ChessGame} queries the current player for a move and the player is
 * responsible for deciding a move during their turn based on the current state of the game (more
 * specifically, the state of the chessboard), which will be provided by {@link ChessGame}.<p>
 * <b>Note:</b> All permitted subtypes of {@code Player} must implement the designated constructor:
 * <pre> Player(String name, Color color, int score) </pre>
 * Only constructors with the <i>exact same type erasure</i> would suffice, otherwise the player
 * will not be loaded.
 * @see Agent
 * @see HumanPlayer
 * */
public sealed abstract class Player extends Colored
        permits Agent, HumanPlayer {

    /** The key string for serializing the player's name. */
    public static final String nameKey = "name";

    /** The key string for serializing the player's score. */
    public static final String scoreKey = "score";

    /** The key string for serializing the player type. */
    public static final String classKey = "class";

    /**
     * Constructs an appropriate concrete subtype of {@link Player} from a JSON object containing
     * the following required key-value mappings:
     * <pre>
     * {
     *     {@value #colorKey}:{@code "<color>"},
     *     <b>{@value classKey}:{@code "<type>"},
     *     {@value nameKey}:{@code "<name>"},
     *     {@value scoreKey}:{@code <score>},
     *     ...</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         the class name of the player returned from {@code this.getClass().getName()};
     *     </li>
     *     <li>{@code <name>}:
     *         the name of the player;
     *     </li>
     *     <li>{@code <score>}:
     *         an integer number of the player's current score.
     *     </li>
     * </ul>
     * <b>Note:</b> Subtypes of {@link Player} may require additional key-value mappings.
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @return An instance of {@link Player} decoded from the given JSON source.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an invalid
     *                                value type.
     * @throws FormatException Rethrown from {@link Color#of(String)} or dynamically from the
     *                         constructors, or if {@code <type>} is invalid.
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * @see #encode()
     * */
    public static Player decode(JSONObject jsonObject) {
        String className = jsonObject.getString(classKey);
        String playerName = jsonObject.getString(nameKey);
        Color playerColor = Color.of(jsonObject.getString(colorKey));
        int score = jsonObject.getInt(scoreKey);
        try {
            return PlayerLoader.generatePlayer(className, playerName, playerColor, score);
        } catch (NoPlayersFound exception) {
            throw new FormatException(exception.getMessage());
        }
    }

    /** The name of this player. */
    protected String name;

    /** The score of this player. */
    private int score;

    /**
     * Constructs this player with a name, color, and an initial score.<p>
     * <b>Note:</b> The player's name must not be empty or contains spaces only, otherwise the name
     * will be set to the player's color by default.
     * @param name  The non-empty name of the player.
     * @param color The color of the player.
     * @param score The initial score of the player.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    protected Player(String name, Color color, int score) {
        super(color);
        this.name = color.name();
        this.setName(name);
        this.score = score;
    }

    /**
     * Serializes this {@link Player} to a JSON object of the following format:
     * <pre>
     * {
     *     {@value #colorKey}:{@code "<color>"},
     *     <b>{@value classKey}:{@code "<type>"},
     *     {@value nameKey}:{@code "<name>"},
     *     {@value scoreKey}:{@code <score>},
     *     ...</b>
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <type>}:
     *         the class name of the player returned from {@code this.getClass().getName()};
     *     </li>
     *     <li>{@code <name>}:
     *         the name of the player;
     *     </li>
     *     <li>{@code <score>}:
     *         an integer number of the player's current score.
     *     </li>
     * </ul>
     * @see Colored#encode()
     * */
    @Override
    public JSONObject encode() {
        return super.encode()
                .put(classKey, this.getClass().getName())
                .put(nameKey, this.name)
                .put(scoreKey, this.score);
    }

    /**
     * Obtains a player move given the current state of the chessboard.
     * @param chessboard The chessboard.
     * @return A {@link PlayerMove} object that describes the player's move.
     * */
    public abstract PlayerMove getPlayerMove(Chessboard chessboard);


    /**
     * @return The name of this player.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Sets the name of this player <i>only if</i> the new name is not empty or contains spaces only.
     * @param name A non-empty new name for this player.
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    public final void setName(String name) {
        name = name.strip();
        if (!name.isEmpty())
            this.name = name;
    }

    /**
     * @return This player's score.
     */
    public final int getScore() {
        return this.score;
    }

    /**
     * Increments the player's score by a non-negative amount.
     * @param increment The increment.
     * @throws IllegalArgumentException If {@code increment < 0}.
     * */
    public final void incrementScore(int increment) {
        if (increment < 0)
            throw new IllegalArgumentException();
        this.score += increment;
    }

    /**
     * Decrements the player's score by an amount. The new score will never be below 0.
     * @param decrement The decrement.
     * */
    public final void decrementScore(int decrement) {
        int newScore = this.score - decrement;
        this.score = Math.max(newScore, 0);
    }

    /**
     * Sets the player's score to a non-negative amount.
     * @param score The new score.
     * @throws IllegalArgumentException If {@code score < 0}.
     * */
    public final void setScore(int score) {
        if (score < 0)
            throw new IllegalArgumentException();
        this.score = score;
    }

    /**
     * @return The string representation of this player.
     * */
    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, this.color);
    }

}
