package chess.model;

import chess.model.moves.Movement;
import chess.persistence.Codable;
import org.json.JSONObject;

import java.util.Date;

public record Transformation(Date date, Movement movement, ChessGame.States state) implements Codable {

    private static final String dateKey = "date";

    private static final String moveKey = "move";

    private static final String stateKey = "state";

    public Transformation(Movement movement, ChessGame.States state) {
        this(new Date(), movement, state);
    }

    public Transformation(JSONObject jsonObject, Chessboard chessboard) {
        this(new Date(jsonObject.getLong(dateKey)),
                Movement.decode(jsonObject.getJSONObject(moveKey), chessboard),
                ChessGame.States.decode(jsonObject.getString(stateKey)));
    }

    @Override
    public JSONObject encode() {
        return new JSONObject()
                .put(dateKey, this.date.getTime())
                .put(moveKey, this.movement.encode())
                .put(stateKey, this.state.toString());
    }

}
