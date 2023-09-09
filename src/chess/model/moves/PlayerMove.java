package chess.model.moves;

import chess.model.ChessGame;
import chess.model.Color;
import chess.model.Position;

/**
 * A raw, "type-less" representation of moves made by any player that is to be interpreted,
 * validated, and executed by {@link ChessGame}, serving as an input packet bearing only
 * minimal information necessary to describe a movement initiated by some player.<p>
 * This class encodes information about some player's move in the simplest form possible. Any
 * additional information is considered inconsequential and any player should keep only what is
 * defined in {@link Movable} and discard everything else when describing a movement.
 * */
public final class PlayerMove extends Movable {

    /**
     * Constructs a player's move with an initial position, final position,
     * and the color of the initiator (described by {@link Color}).
     * @param initialPosition The initial position of the movement.
     * @param finalPosition The final position of the movement.
     * @param color The color of the movement's initiator (White or Black).
     * @throws NullPointerException If any of the arguments is {@code null}.
     * */
    public PlayerMove(Position initialPosition, Position finalPosition, Color color) {
        super(initialPosition, finalPosition, color);
    }

}
