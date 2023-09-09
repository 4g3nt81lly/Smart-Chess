package chess.model;

import chess.model.exceptions.FormatException;
import chess.model.exceptions.GameException;
import chess.model.exceptions.IllegalMove;
import chess.model.exceptions.IllegalOperation;
import chess.model.moves.Movement;
import chess.model.moves.PlayerMove;
import chess.model.moves.TwoSquareAdvance;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.player.Agent;
import chess.model.player.HumanPlayer;
import chess.model.player.Player;
import chess.persistence.Codable;
import chess.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import static chess.model.Color.Black;
import static chess.model.Color.White;

/**
 * A class that manages the lifecycle and state of a single Chess game.<p>
 * A Chess game involves a {@link Chessboard}, and two {@link Player} objects.
 * During a game, the two players take turns and make moves. This class manages the lifecycle and
 * states of the {@link Chessboard} and host the game between the two players. In addition, this
 * class also manages game-related states and behaviors that are external to the chessboard (e.g.
 * undo/redo, reset, etc.).<p>
 * This class provides a <i>navigable view</i> of a Chess game, that is, a game can be easily
 * navigated backward/forward in time via a uniform control. The current state of the Chess game is
 * thought of as a composition of transformations with respect to the initial state (setup).
 * Actions like a regular turn, undo, or redo are just nuanced transformations that can be
 * reversed/reproduced. This class merely maintains the relevant transformations and computes the
 * state of the game on the go. For instance, there are three major kinds of transformation,
 * <ul>
 *     <li>A regular turn (a player making a move): a transformation forward in time, which, upon
 *         a move is made, clears all future transformations;</li>
 *     <li>An undo (backward action): a transformation backward in time, the inverse transformation
 *         of a regular turn or a redo/forward action;</li>
 *     <li>A redo (forward action): resembling a regular turn, it is also a transformation forward
 *         in time, except that the transformation is determined.</li>
 * </ul>
 * This class also buffers the current state of the game so that the state can be retrieved without
 * re-computation.<p>
 * In addition to game-related states, this class also manages the following states:
 * <ol>
 *     <li>Read-only: when a game is set to read only, no transformation can be applied (no
 *         undo/redo, forward/backward, or advance turn);</li>
 *     <li>Pause/Resume: when a game is paused, only forward and backward actions are allowed (no
 *         undo/redo or advance turn).</li>
 * </ol>
 * Note that this class does NOT <i>handle</i> the abnormal states of the game in any manner.
 */
public class ChessGame implements Codable {

    /** A key string for serializing the name of the game. */
    public static final String nameKey = "name";

    /** A key string for serializing the chessboard associated with the game. */
    public static final String chessboardKey = "chessboard";

    /** A key string for serializing the current state of the game. */
    public static final String stateKey = "state";

    /** A key string for serializing the White player. */
    private static final String whitePlayerKey = "whitePlayer";

    /** A key string for serializing the Black player. */
    private static final String blackPlayerKey = "blackPlayer";

    /**
     * A key string for serializing the history of the game, i.e. a collection of transformations
     * that occurred before the current state of the game.
     * */
    public static final String historyKey = "history";

    /**
     * A key string for serializing the future of the game, i.e. a collection of transformations
     * that occurred after the current state of the game, if any.
     * */
    public static final String futureKey = "future";

    /** A key string for serializing the Read-Only property of this game. */
    public static final String readOnlyKey = "readOnly";

    /**
     * An enumeration of possible states of the game, which can be conclusive or inconclusive.
     * */
    public enum States {
        /** An inconclusive case in which the game is in a regular state. */
        regular(false),
        /** An inconclusive case in which a player is in check by the other. */
        inCheck(false),
        /** A conclusive case in which a player is checkmated the other. */
        checkmated(true),
        /** A conclusive case in which a player is stalemated the other. */
        stalemated(true),
        /** A conclusive case in which a player offered a draw and the other agreed. */
        agreedDrawn(true);

        /** A boolean flag indicating whether the state is conclusive. */
        private final boolean conclusive;

        /**
         * Constructs a conclusive/inconclusive state.
         * @param conclusive A boolean value indicating whether the state is conclusive.
         * */
        States(boolean conclusive) {
            this.conclusive = conclusive;
        }

        /**
         * Constructs a state from a string value.
         * @param state A string state value.
         * @return A {@link States} value.
         * @throws FormatException If the {@code state} string is not a constant in the enumeration.
         * */
        public static States decode(String state) {
            try {
                return Enum.valueOf(States.class, state);
            } catch (IllegalArgumentException e) {
                throw new FormatException(e.getMessage());
            }
        }
    }

    /**
     * A mutable class managing/capturing the state of the Chess game, which is defined by:
     * <ol>
     *     <li>a state descriptor;</li>
     *     <li>a subject;</li>
     *     <li>a round number.</li>
     * </ol>
     * Any instance of this class serves to describe a specific state of the Chess game at some time.<p>
     * <b>Note: </b> This class is immutable externally and can only be mutated from within
     * {@link ChessGame}.
     * */
    private static final class State implements Codable {

        /** A key string for serializing the name of the state. */
        public static final String typeKey = "type";

        /** A key string for serializing the subject of the state. */
        public static final String subjectKey = "subject";

        /** A key string for serializing the round number of the state. */
        public static final String roundKey = "round";

        /** The state descriptor. */
        private States state = States.regular;

        /** The subject of this state. */
        private Color subject = White;

        /** The round number of this state. */
        private int round = 1;

        /**
         * Decodes and updates the state information from a JSON object containing the following
         * required key-value mappings:
         * <pre>{
         *     {@value typeKey}:{@code "<type>"},
         *     {@value subjectKey}:{@code "<color>"},
         *     {@value roundKey}:{@code <round>}
         * }</pre>
         * where:
         * <ul>
         *     <li>{@code <type>}:
         *         the name of the state enumeration;
         *     </li>
         *     <li>{@code <color>}:
         *         a string {@link Color} value;
         *     </li>
         *     <li>{@code <round>}:
         *         the round number of the current state.
         *     </li>
         * </ul>
         * @param jsonObject The source {@link JSONObject} from which to decode.
         * @throws org.json.JSONException If any of the required keys is missing or maps to an
         *                                invalid value type.
         * @throws FormatException Rethrown from {@link States#decode(String)}.
         * @throws NullPointerException If {@code jsonObject} is {@code null}.
         * */
        private void decode(JSONObject jsonObject) {
            try {
                this.state = States.decode(jsonObject.getString(typeKey));
                this.subject = Color.of(jsonObject.getString(subjectKey));
                this.round = jsonObject.getInt(roundKey);
            } catch (IllegalArgumentException e) {
                throw new FormatException(e.getMessage());
            }
        }

        /**
         * Serializes this game to a JSON object of the following format:
         * <pre>{
         *     {@value typeKey}:{@code "<type>"},
         *     {@value subjectKey}:{@code "<color>"},
         *     {@value roundKey}:{@code <round>}
         * }</pre>
         * where:
         * <ul>
         *     <li>{@code <type>}:
         *         the name of the state enumeration;
         *     </li>
         *     <li>{@code <color>}:
         *         a string {@link Color} value;
         *     </li>
         *     <li>{@code <round>}:
         *         the round number of the current state.
         *     </li>
         * </ul>
         * */
        @Override
        public JSONObject encode() {
            return new JSONObject()
                    .put(typeKey, this.state.toString())
                    .put(subjectKey, this.subject.toString())
                    .put(roundKey, this.round);
        }

        /**
         * Updates the state descriptor with a new one.
         * @param newState A new state descriptor.
         * */
        private void update(States newState) {
            this.state = newState;
        }

        /**
         * Toggles the subject color of this state.
         * */
        private void toggleSubject() {
            this.subject = this.subject.opposite();
        }

        /**
         * Advances this state to the next turn by 1) toggling the subject, and 2) incrementing the
         * round number when applicable.
         * */
        private void nextTurn() {
            this.toggleSubject();
            if (this.subject.isWhite()) {
                this.round++;
            }
        }

        /**
         * Rewinds this state to the previous turn by 1) toggling the subject, and 2) decrementing
         * the round number when applicable.
         * */
        private void previousTurn() {
            this.toggleSubject();
            if (this.subject.isBlack()
                    && this.round > 1) {
                this.round--;
            }
        }

        /**
         * @return A boolean value indicating whether this state is conclusive.
         * */
        private boolean isConclusive() {
            return this.state.conclusive;
        }

        /**
         * @return The description of this state.
         * */
        @Override
        public String toString() {
            return switch (this.state) {
                case regular -> "Game in progress.";
                case inCheck -> this.subject + " is in check!";
                case checkmated -> this.subject.opposite() + " won!";
                case stalemated -> "Draw. " + this.subject + " was stalemated.";
                case agreedDrawn -> "Draw by mutual agreement.";
            };
        }

    }

    /** The name of this game. */
    private String name;

    /** The chessboard associated with this game. */
    private final Chessboard chessboard;

    /** The state object this game. */
    private final State state = new State();

    /** The White player of this game. */
    private Player whitePlayer;

    /** The Black player of this game. */
    private Player blackPlayer;

    /** The history of this game, i.e. a collection of previous transformations. */
    private final Stack<Transformation> historyStack = new Stack<>();

    /** The future of this game, i.e. a collection of proceeding transformations. */
    private final Stack<Transformation> futureStack = new Stack<>();

    /** A boolean flag indicating whether this game is read-only. */
    private boolean readOnly = false;

    /** A boolean flag indicating whether this game is in-progress (not paused). */
    private boolean isPlaying = true;


    // ==================== INITIALIZATIONS ====================


    /**
     * Constructs a Chess game with the default game setup:
     * <ul>
     *     <li>Name: Untitled Game;</li>
     *     <li>standard chessboard setup;</li>
     *     <li>two human players;</li>
     *     <li>White to move;</li>
     * </ul>
     * */
    public ChessGame() {
        this("");
    }

    /**
     * Constructs a Chess game with the default game setup and a given name.
     * If {@code name} is empty, the default name will be used.
     * @param name The name of the game, a non-empty string.
     * @see #ChessGame()
     * */
    public ChessGame(String name) {
        this(name, new HumanPlayer(White), new HumanPlayer(Black));
    }

    /**
     * Constructs a Chess game with the default game setup, a given name, a White player, and a
     * Black player. If {@code name} is empty, the default name will be used.
     * @param name        The name of the game, a non-empty string.
     * @param whitePlayer The White player.
     * @param blackPlayer The Black player.
     * @throws NullPointerException If either of {@code whitePlayer} or {@code blackPlayer} is
     *                              {@code null}.
     * @see #ChessGame()
     * @see #ChessGame(String, Chessboard, Player, Player)
     * */
    public ChessGame(String name, Player whitePlayer, Player blackPlayer) {
        this(name, Chessboard.standard(), whitePlayer, blackPlayer);
    }

    /**
     * Constructs a Chess game with a given name, a chessboard, a White player, and a Black player.
     * If {@code name} is empty, the default name will be used.
     * @param name        The name of the game, a non-empty string.
     * @param chessboard  The chessboard of the game.
     * @param whitePlayer The White player.
     * @param blackPlayer The Black player.
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public ChessGame(String name, Chessboard chessboard,
                        Player whitePlayer, Player blackPlayer) {
        this.setName(name);
        this.chessboard = Objects.requireNonNull(chessboard);
        this.whitePlayer = Objects.requireNonNull(whitePlayer);
        this.blackPlayer = Objects.requireNonNull(blackPlayer);
    }

    /**
     * Constructs a Chess game from a JSON source containing the following required key-value
     * mappings:
     * <pre>
     * {
     *     {@value nameKey}:{@code "<name>"},
     *     {@value chessboardKey}:{@code <chessboard>},
     *     {@value stateKey}:{@code <state>},
     *     {@value whitePlayerKey}: <code>&lt;player-1&gt;</code>,
     *     {@value blackPlayerKey}: <code>&lt;player-2&gt;</code>,
     *     {@value historyKey}: [
     *         ...
     *     ],
     *     {@value futureKey}: [
     *         ...
     *     ]
     * }</pre>
     * where:
     * <ul>
     *     <li>{@code <name>}:
     *         the name of this game;
     *     </li>
     *     <li>{@code <chessboard>}:
     *         the associated chessboard encoded as a JSON object;
     *     </li>
     *     <li>{@code <state>}:
     *         the state object encoded as a JSON object;
     *     </li>
     *     <li><code>&lt;player-1&gt;</code>:
     *         the White player encoded as a JSON object;
     *     </li>
     *     <li><code>&lt;player-2&gt;</code>
     *         the Black player encoded as a JSON object;
     *     </li>
     *     <li>{@code <history>}:
     *         a collection of previous transformations encoded as a JSON array of JSON objects;
     *     </li>
     *     <li>{@code <history>}:
     *         a collection of next transformations encoded as a JSON array of JSON objects.
     *     </li>
     * </ul>
     * @param jsonObject The source {@link JSONObject} from which to decode.
     * @throws org.json.JSONException If any of the required keys is missing or maps to an
     *                                invalid value type.
     * @throws FormatException Rethrown from any of the following method calls (follow to see details):
     *                         <ul>
     *                             <li>{@link Chessboard#Chessboard(JSONObject)};</li>
     *                             <li>{@link State#decode(JSONObject)};</li>
     *                             <li>{@link Player#decode(JSONObject)};</li>
     *                             <li>{@link Transformation#Transformation(JSONObject, Chessboard)}.</li>
     *                         </ul>
     * @throws NullPointerException If {@code jsonObject} is {@code null}.
     * */
    public ChessGame(JSONObject jsonObject) {
        // load game name
        this.setName(jsonObject.getString(nameKey));
        // load chessboard
        this.chessboard = new Chessboard(jsonObject.getJSONObject(chessboardKey));
        // load game state
        this.state.decode(jsonObject.getJSONObject(stateKey));
        // load players
        this.whitePlayer = Player.decode(jsonObject.getJSONObject(whitePlayerKey));
        this.blackPlayer = Player.decode(jsonObject.getJSONObject(blackPlayerKey));
        // get history stack
        JSONArray history = jsonObject.getJSONArray(historyKey);
        for (int i = 0; i < history.length(); i++) {
            JSONObject recordObject = history.getJSONObject(i);
            this.historyStack.push(new Transformation(recordObject, this.chessboard));
        }
        // get future stack
        JSONArray future = jsonObject.getJSONArray(futureKey);
        for (int i = 0; i < future.length(); i++) {
            JSONObject recordObject = future.getJSONObject(i);
            this.futureStack.push(new Transformation(recordObject, this.chessboard));
        }
        this.readOnly = jsonObject.getBoolean(readOnlyKey);
    }

    /**
     * Serializes this Chess game to a JSON object of the following format:
     * <pre>
     * {
     *     {@value nameKey}:{@code "<name>"},
     *     {@value chessboardKey}:{@code <chessboard>},
     *     {@value stateKey}:{@code <state>},
     *     {@value whitePlayerKey}: <code>&lt;player-1&gt;</code>,
     *     {@value blackPlayerKey}: <code>&lt;player-2&gt;</code>,
     *     {@value historyKey}: [
     *         ...
     *     ],
     *     {@value futureKey}: [
     *         ...
     *     ]
     * }</pre>
     * @see #ChessGame(JSONObject)
     * */
    @Override
    public JSONObject encode() {
        // encode move history
        JSONArray history = new JSONArray(this.getHistory()
                .map(Transformation::encode));
        // encode redo moves
        JSONArray future = new JSONArray(this.getFuture()
                .map(Transformation::encode));
        // pack JSON object
        return new JSONObject()
                .put(nameKey, this.name)
                .put(stateKey, this.state.encode())
                .put(chessboardKey, this.chessboard.encode())
                .put(whitePlayerKey, this.whitePlayer.encode())
                .put(blackPlayerKey, this.blackPlayer.encode())
                .put(historyKey, history)
                .put(futureKey, future)
                .put(readOnlyKey, this.readOnly);
    }

    public ChessGame copy() {
        return new ChessGame(this.encode());
    }

    /**
     * Sets the name of this game to a new name, which should be non-empty and will be stripped.<p>
     * If the new name is empty after stripping, this method does nothing.
     * @param name A non-empty new name for this game.
     * */
    public void setName(String name) {
        name = name.strip();
        if (!name.isEmpty())
            this.name = name;
    }

    /**
     * Sets one of the players of this game to a new player.
     * @param newPlayer A new player.
     * @throws NullPointerException If {@code newPlayer} is {@code null}.
     * */
    public synchronized void setPlayer(Player newPlayer) {
        if (newPlayer.isWhite())
            this.whitePlayer = newPlayer;
        else this.blackPlayer = newPlayer;
    }


    // ==================== STATES ====================


    /**
     * <b>Note:</b> This does NOT indicate that the game has ever been concluded before, it merely
     * reflects whether the current state of this game is conclusive.
     * @return A boolean value indicating whether the current state of this game is conclusive.
     * */
    public synchronized boolean hasConcluded() {
        return this.state.isConclusive();
    }

    /**
     * This method returns the effective negation of the method {@link #hasConcluded()}. A game has
     * next turn <i>if and only if</i> the game has not concluded, i.e. one of the players can
     * still make a move. By entailment, if this method evaluates to {@code false}, then the method
     * {@link #hasForward()} also evaluates to {@code false}, but not its inverse or converse. Use
     * this method to decide whether the method {@link #nextTurn()} can be called.
     * @return A boolean value indicating whether this game can be advanced by one of its player
     * making a move.
     * */
    public boolean hasNextTurn() {
        return !this.hasConcluded();
    }

    /**
     * @return A boolean value indicating whether this game has been paused.
     * @see #isReadOnly()
     * @see #isInspectionOnly()
     * */
    public boolean isPaused() {
        return !this.isPlaying;
    }

    /**
     * @return A boolean value indicating whether this game is read only.
     * @see #isPaused()
     * @see #isInspectionOnly()
     * */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * The game may not proceed as normal if it has been paused or set to read-only mode.
     * @return A boolean value indicating whether this game can only be inspected.
     * @see #isPaused()
     * @see #isReadOnly()
     * */
    public boolean isInspectionOnly() {
        return this.isPaused() || this.isReadOnly();
    }

    /**
     * @return The round number of this game.
     * */
    public synchronized int getRoundNumber() {
        return this.state.round;
    }

    /**
     * The color to make a move is the subject of the current state of this game.
     * @return The color to move.
     * */
    public synchronized Color getColorToMove() {
        return this.state.subject;
    }

    /**
     * @return A state description of this game.
     * */
    public synchronized String getStateDescription() {
        return this.state.toString();
    }

    /**
     * Assuming that the initiator of navigating actions is the current player to move (should be a
     * human player since it makes no sense for agents to navigate), and that they have yet to make
     * a move, if the opponent is an agent, i.e. the case in which that player is playing against
     * an agent, then they should really be navigating two turns rather than one. This is because
     * navigating only one turn implies undoing the move that was made by the agent on the previous
     * turn only, after which the agent will make a move on the immediate turn after the navigation
     * is complete, never allowing the human player to navigate any further into the past unless
     * the game is paused or set to read-only.
     * @return The number of turns to navigate depending on whether the opponent is an agent.
     * */
    private int getNumNavigableTurns() {
        return (this.getOpponent() instanceof Agent) ? 2 : 1;
    }

    /**
     * This game can be undone <i>if and only if</i>:
     * <ul>
     *     <li>the game is not in inspection mode (paused or read-only);</li>
     *     <li>the player to move is not an agent or the current state is conclusive;</li>
     *     <li>the game has previous record of transformations.</li>
     * </ul>
     * In the case of the game state being conclusive, allowing undo enables the final player
     * (whether an agent or human) to retract the last move and go for an alternative move.
     * @return A boolean value indicating whether this game can be undone.
     * */
    public synchronized boolean canUndo() {
        if (this.isInspectionOnly()
                || (this.getPlayerToMove() instanceof Agent
                        && !this.hasConcluded())) {
            return false;
        }
        return this.hasBackward(this.getNumNavigableTurns());
    }

    /**
     * This game can be redone <i>if and only if</i>:
     * <ul>
     *     <li>the game is not in inspection mode (paused or read-only);</li>
     *     <li>the current state is inconclusive;</li>
     *     <li>the player to move is not an agent;</li>
     *     <li>the game has record of future transformations.</li>
     * </ul>
     * @return A boolean value indicating whether this game can be redone.
     * */
    public synchronized boolean canRedo() {
        if (this.isInspectionOnly()
                || this.hasConcluded()
                || this.getPlayerToMove() instanceof Agent) {
            return false;
        }
        return this.hasForward(this.getNumNavigableTurns());
    }

    /**
     * This game can be reset <i>if and only if</i> it has been transformed, i.e. this game has
     * recorded transformations either in the past or future.
     * @return A boolean value indicating whether this game can be reset.
     * */
    public synchronized boolean canReset() {
        return !this.historyStack.isEmpty() || !this.futureStack.isEmpty();
    }

    /**
     * This game can be navigated backward N turns <i>if and only if</i> it has >= N recorded
     * transformations in the past, for some positive integer N.
     * @param turns The number of turns as a positive integer.
     * @return A boolean value indicating whether this game can be navigated backward N turns, for
     *         some positive integer N.
     * @throws IllegalArgumentException If {@code turns <= 0}.
     * */
    public synchronized boolean hasBackward(int turns) {
        if (turns <= 0) {
            throw new IllegalArgumentException();
        }
        return this.historyStack.size() >= turns;
    }

    /**
     * @return A boolean value indicating whether this game can be navigated backward for one turn.
     * */
    public boolean hasBackward() {
        return this.hasBackward(1);
    }

    /**
     * This game can be navigated forward N turns <i>if and only if</i> it has >= N recorded
     * transformations in the future, for some positive integer N.
     * @param turns The number of turns as a positive integer.
     * @return A boolean value indicating whether this game can be navigated forward N turns, for
     *         some positive integer N.
     * @throws IllegalArgumentException If {@code turns <= 0}.
     * */
    public synchronized boolean hasForward(int turns) {
        if (turns <= 0) {
            throw new IllegalArgumentException();
        }
        return this.futureStack.size() >= turns;
    }

    /**
     * @return A boolean value indicating whether this game can be navigated forward for one turn.
     * */
    public boolean hasForward() {
        return this.hasForward(1);
    }


    // ==================== OPERATIONS ====================


    /**
     * Advances this game to the next turn. This method calls the current player's
     * {@link Player#getPlayerMove(Chessboard)} method with a copy of the associated chessboard
     * to obtain this player's move. After which, the retrieved move is validated and executed.
     * In the case of a human player, this method should be preceded by an appropriate call to
     * {@link HumanPlayer#registerNextMove(Position, Position)}. Ideally, this method should be
     * called on its own thread (a thread separate from the main application thread) to prevent UI
     * unresponsiveness since this method could be potentially time-consuming (depending on the
     * agent) in which case letting it block the thread may not desirable.
     * @throws IllegalOperation If one or more of the following scenarios occur, i.e. this game:
     *                          <ol>
     *                              <li>is in inspection mode (paused or read-only);</li>
     *                              <li>has no next turn;</li>
     *                              <li>is paused halfway.</li>
     *                          </ol>
     * @throws IllegalMove If the move retrieved from the current player is illegal.
     * @see Player#getPlayerMove(Chessboard)
     * */
    public void nextTurn() throws GameException {
        if (this.isInspectionOnly() || !this.hasNextTurn()) {
            throw new IllegalOperation();
        }
        Player playerToMove = this.getPlayerToMove();
        PlayerMove move = playerToMove.getPlayerMove(this.chessboard.copy());
        if (Thread.interrupted()) {
            throw new IllegalOperation();
        }
        synchronized (this) {
            Movement movement = this.validateMove(move);
            this.chessboard.executeMove(movement);
            Transformation transformation = new Transformation(movement, this.state.state);
            this.historyStack.push(transformation);
            this.futureStack.clear();
            this.state.nextTurn();
            this.updateState();
        }
    }

    /**
     * Validates a given player move and returns an appropriate internal representation of the move.
     * @return An instance of {@link Movement}, representing a legal move.
     * @throws IllegalMove If one or more of the following scenarios occur, i.e. the player move:
     *                     <ol>
     *                         <li>describes a vacant initial position (no piece);</li>
     *                         <li>describes an illegal move for the target piece;</li>
     *                         <li>attempts to move an opponent's piece.</li>
     *                     </ol>
     * */
    private Movement validateMove(PlayerMove playerMove) throws IllegalMove {
        Position initialPosition = playerMove.getInitialPosition();
        Piece piece = this.chessboard.getPieceAt(initialPosition)
                .orElseThrow(() -> new IllegalMove("No piece at " + initialPosition + "."));
        if (piece.isAlliedTo(playerMove)) {
            return piece.getLegalMoves(this.chessboard).findFirst(legalMove -> {
                return playerMove.getFinalPosition().equals(legalMove.getFinalPosition());
            }).orElseThrow(() -> new IllegalMove("Illegal move for " + piece + "."));
        }
        throw new IllegalMove("Attempting to move an opponent's piece " + piece + ".");
    }

    /**
     * Updates the state of this game according to the current configuration of the chessboard.
     * */
    private void updateState() {
        Player playerToMove = this.getPlayerToMove();
        if (this.chessboard.isCheckmated(playerToMove)) {
            // the current player to move is checkmated
            // the opponent wins
            this.state.update(States.checkmated);
        } else if (this.chessboard.isStalemated(playerToMove)) {
            // the current player to move is stalemated, the game is drawn
            this.state.update(States.stalemated);
        } else if (this.chessboard.isInCheck(playerToMove)) {
            // the current player to move is in check
            this.state.update(States.inCheck);
        } else {
            this.state.update(States.regular);
        }
        this.updateEnPassantStatus();
    }

    /**
     * Updates the en-passant states of this game. This method does this by first resetting the
     * en-passant state of all Pawns, and then if the most recent move is a two-square advance, it
     * enables en-passant state for the target pieces of that move.
     * */
    private void updateEnPassantStatus() {
        // reset en passant status for ALL Pawns
        for (Piece piece : this.chessboard) {
            if (piece instanceof Pawn pawn) {
                pawn.setEnPassant(false);
            }
        }
        // check if the most recent move is a two-squared advance
        this.getMostRecentMove().ifPresent(movement -> {
            if (movement instanceof TwoSquareAdvance) {
                ((Pawn) movement.getPieceFrom(this.chessboard)).setEnPassant(true);
            }
        });
    }

    /**
     * Initiated by the current player, this method undoes an appropriate number of turns.
     * @throws IllegalOperation If this game cannot be undone.
     * @see #canUndo()
     * */
    public synchronized void undo() {
        if (!this.canUndo()) {
            throw new IllegalOperation();
        }
        this.backward(this.getNumNavigableTurns());
    }

    /**
     * Based on whether this game is in inspection mode, this method performs the appropriate
     * backward-navigation action.
     * */
    public synchronized void undoOrBackward() {
        if (this.isInspectionOnly()) {
            this.backward();
        } else {
            this.undo();
        }
    }

    /**
     * Initiated by the current player, this method redoes an appropriate number of turns.
     * @throws IllegalOperation If this game cannot be redone.
     * @see #canRedo()
     * */
    public synchronized void redo() {
        if (!this.canRedo()) {
            throw new IllegalOperation();
        }
        this.forward(this.getNumNavigableTurns());
    }

    /**
     * Based on whether this game is in inspection mode, this method performs the appropriate
     * forward-navigation action.
     * */
    public synchronized void redoOrForward() {
        if (this.isInspectionOnly()) {
            this.forward();
        } else {
            this.redo();
        }
    }

    /**
     * Backward-navigates this game N turns for some positive integer N. This is done by repeatedly
     * applying inverse transformations.
     * @param turns The number of turns as a positive integer.
     * @throws IllegalOperation If this game cannot be backward-navigated N turns.
     * @see #hasBackward(int)
     * */
    public synchronized void backward(int turns) {
        if (!this.hasBackward(turns)) {
            throw new IllegalOperation();
        }
        for (int i = 0; i < turns; i++) {
            Transformation previous = this.historyStack.pop();
            this.chessboard.undoMove(previous.movement());
            this.futureStack.push(previous);
            this.state.previousTurn();
            this.updateState();
        }
    }

    /**
     * Backward-navigates this game for one turn.
     * @throws IllegalOperation If this game cannot be backward-navigated.
     * @see #backward(int)
     * @see #hasBackward(int)
     * */
    public void backward() {
        this.backward(1);
    }

    /**
     * Forward-navigates this game N turns for some positive integer N. This is done by repeatedly
     * applying transformations.
     * @param turns The number of turns as a positive integer.
     * @throws IllegalOperation If this game cannot be forward-navigated N turns.
     * @see #hasForward(int)
     * */
    public synchronized void forward(int turns) {
        if (!this.hasForward(turns)) {
            throw new IllegalOperation();
        }
        for (int i = 0; i < turns; i++) {
            Transformation next = this.futureStack.pop();
            this.chessboard.executeMove(next.movement());
            this.historyStack.push(next);
            this.state.nextTurn();
            this.updateState();
        }
    }

    /**
     * Forward-navigates this game for one turn.
     * @throws IllegalOperation If this game cannot be forward-navigated.
     * @see #forward(int)
     * @see #hasForward(int)
     * */
    public void forward() {
        this.forward(1);
    }

    /**
     * Resumes this game immediately.
     * */
    public void resume() {
        if (!this.isPlaying)
            this.isPlaying = true;
    }

    /**
     * Pause this game immediately.
     * */
    public void pause() {
        if (this.isPlaying)
            this.isPlaying = false;
    }

    // TODO: fully support draw by mutual agreement
    /**
     * Marks this game as draw by mutual agreement.<p>
     * <i>NOT FULLY SUPPORTED.</i>
     * */
    public synchronized void markDraw() {
        if (this.getPlayerToMove() instanceof Agent) {
            throw new IllegalOperation();
        }
        this.state.update(States.agreedDrawn);
    }

    /**
     * Resets this game to the initial state. This method does nothing if this game cannot be reset.
     * @see #canReset()
     * */
    public synchronized void reset() {
        if (this.canReset()) {
            while (this.hasBackward()) {
                this.backward();
            }
            this.futureStack.clear();
            this.updateState();
        }
    }

    /**
     * Sets this game to be read-only.
     * @param readOnly A boolean value indicating whether the game is read-only.
     * */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }


    // ==================== GETTER/SETTER METHODS ====================


    /**
     * @return The name of this game.
     * */
    public String getName() {
        return this.name;
    }

    /**
     * @return The chessboard associated with this game.
     * */
    public synchronized Chessboard getChessboard() {
        return this.chessboard;
    }

    /**
     * @param color A color.
     * @return The player of the given color.
     * */
    public Player getPlayer(Color color) {
        return color.isWhite() ? this.whitePlayer : this.blackPlayer;
    }

    /**
     * @return The player to move during this turn.
     * */
    public Player getPlayerToMove() {
        return this.getPlayer(this.getColorToMove());
    }

    /**
     * @return The opponent of the player to move during this turn.
     * */
    public Player getOpponent() {
        return this.getPlayer(this.getColorToMove().opposite());
    }

    /**
     * @return A shallow-copied collection of previous transformations.
     * */
    public synchronized List<Transformation> getHistory() {
        return new List<>(this.historyStack);
    }

    /**
     * @return A collection of previous movements.
     * */
    public List<Movement> getPreviousMoves() {
        return this.getHistory().map(Transformation::movement);
    }

    /**
     * @return An optional describing the most recent transformation, or empty if no previous
     * transformations exist.
     * */
    public synchronized Optional<Transformation> getMostRecentTransformation() {
        if (this.historyStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.historyStack.peek());
    }

    /**
     * @return An optional describing the most recent movement, or empty if no previous
     * transformations exist.
     * */
    public Optional<Movement> getMostRecentMove() {
        return this.getMostRecentTransformation().map(Transformation::movement);
    }

    /**
     * @return A shallow-copied collection of proceeding transformations.
     * */
    public synchronized List<Transformation> getFuture() {
        return new List<>(this.futureStack);
    }

    /**
     * @return A collection of proceeding movements.
     * */
    public List<Movement> getFutureMoves() {
        return this.getFuture().map(Transformation::movement);
    }

}
