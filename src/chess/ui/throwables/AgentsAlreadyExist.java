package chess.ui.throwables;

import chess.model.player.Agent;
import chess.ui.PlayerLoader;
import chess.util.List;

public class AgentsAlreadyExist extends LoadAgentsException {

    private final List<Class<? extends Agent>> duplicateAgents;

    public AgentsAlreadyExist(List<Class<? extends Agent>> duplicateAgents) {
        this.duplicateAgents = duplicateAgents;
    }

    public List<Class<? extends Agent>> getDuplicateAgents() {
        return this.duplicateAgents.copy();
    }

    @Override
    public String getMessage() {
        return "Agents with the same display/class names as the following have already been loaded:\n"
                + String.join("\n", this.duplicateAgents.map(PlayerLoader::getPlayerDisplayName));
    }
}
