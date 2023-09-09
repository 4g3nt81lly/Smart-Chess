package chess.ui;

import chess.model.Color;
import chess.model.player.Agent;
import chess.model.player.AgentInfo;
import chess.model.player.HumanPlayer;
import chess.model.player.Player;
import chess.ui.throwables.AgentsAlreadyExist;
import chess.ui.throwables.LoadAgentsException;
import chess.ui.throwables.NoPlayersFound;
import chess.util.List;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.function.Consumer;

public final class PlayerLoader {

    public static final List<Class<? extends Agent>> externalAgents;
    private static final List<Class<? extends Player>> internalPlayers;

    static {
        Reflections reflections = new Reflections("chess.model.player");
        List<Class<? extends Player>> builtinPlayers = loadPlayers(reflections, Player.class);
        // reordering the classes such that HumanPlayer is at 1st, and Agent is at 2nd
        internalPlayers = List.of(HumanPlayer.class, Agent.class);
        for (Class<? extends Player> builtinPlayer : builtinPlayers) {
            if (!internalPlayers.contains(builtinPlayer)) {
                internalPlayers.add(builtinPlayer);
            }
        }

        externalAgents = new List<>();
    }

    public static List<Class<? extends Player>> getLoadedPlayers() {
        List<Class<? extends Player>> loadedPlayers = new List<>(internalPlayers);
        loadedPlayers.addAll(externalAgents);
        return loadedPlayers;
    }

    public static List<String> getLoadedPlayerNames() {
        return getLoadedPlayers().map(PlayerLoader::getPlayerDisplayName);
    }

    private static <T extends Player> List<Class<? extends T>> loadPlayers(Reflections reflections,
                                                                           Class<T> type) {
        List<Class<? extends T>> players = new List<>();
        Set<Class<? extends T>> accessiblePlayers = reflections.getSubTypesOf(type);
        for (Class<? extends T> player : accessiblePlayers) {
            // test if the player has the designated constructor
            try {
                player.getConstructor(String.class, Color.class, int.class);
            } catch (NoSuchMethodException e) {
                // designated constructor not found, skip
                continue;
            }
            // test if the player specifies a non-empty display name
            String displayName = getPlayerDisplayName(player);
            if (!displayName.isEmpty()) {
                players.add(player);
            }
        }
        return players;
    }

    public static String getPlayerDisplayName(Class<? extends Player> playerClass) {
        if (playerClass.isAnnotationPresent(AgentInfo.class)) {
            AgentInfo annotation = playerClass.getDeclaredAnnotation(AgentInfo.class);
            return annotation.displayName().strip();
        }
        return playerClass.getName();
    }

    public static String getPlayerDescription(Class<? extends Player> playerClass) {
        String description = getPlayerDisplayName(playerClass);
        if (description.equals(playerClass.getName())) {
            return description;
        }
        return description + " (" + playerClass.getSimpleName() + ")";
    }

    public static void loadNewAgents(File file, Consumer<List<Class<? extends Agent>>> handler)
            throws IOException, LoadAgentsException {
        if (!file.exists())
            throw new FileNotFoundException();
        URL url = file.toURI().toURL();
        URL[] urls = { url };
        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
            // https://stackoverflow.com/a/18424773
            ConfigurationBuilder configuration = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forClassLoader(classLoader))
                    .addClassLoaders(classLoader);
            Reflections reflections = new Reflections(configuration);
            List<Class<? extends Agent>> agentClasses = loadPlayers(reflections, Agent.class);
            if (agentClasses.isEmpty()) {
                throw new NoPlayersFound("No loadable agents found.");
            }
            handler.accept(agentClasses);
            checkForDuplicates(agentClasses);
            externalAgents.addAll(agentClasses);
        }
    }

    private static void checkForDuplicates(List<Class<? extends Agent>> agentClasses) throws AgentsAlreadyExist {
        List<String> loadedAgentDisplayNames = getLoadedPlayerNames();
        List<String> loadedAgentClassNames = getLoadedPlayers().map(Class::getSimpleName);

        List<Class<? extends Agent>> duplicateAgents = new List<>();
        for (Class<? extends Agent> agentClass : agentClasses) {
            String agentDisplayName = getPlayerDisplayName(agentClass);
            String agentClassName = agentClass.getSimpleName();
            if (loadedAgentDisplayNames.contains(agentDisplayName)
                    || loadedAgentClassNames.contains(agentClassName)) {
                duplicateAgents.add(agentClass);
            }
        }
        if (!duplicateAgents.isEmpty()) {
            throw new AgentsAlreadyExist(duplicateAgents);
        }
    }

    public static void offloadPlayer(Class<? extends Player> agentClass) {
        if (!canOffload(agentClass)) {
            throw new UnsupportedOperationException();
        }
        externalAgents.remove(agentClass);
    }

    public static boolean canOffload(Class<? extends Player> playerClass) {
        return externalAgents.contains(playerClass);
    }

    public static Player generatePlayer(String type, String name, Color color, int score) throws NoPlayersFound {
        int index = getLoadedPlayers().map(Class::getName).indexOf(type);
        if (index > -1) {
            return generatePlayer(name, color, score, index);
        }
        throw new NoPlayersFound("Player of type '" + type + "' has not been loaded.");
    }

    public static Player generatePlayer(String name, Color color, int score, int index) {
        Class<?> playerClass = getLoadedPlayers().get(index);
        try {
            // throws NoSuchMethodException
            Constructor<?> playerConstructor = playerClass.getConstructor(String.class, Color.class, int.class);
            // throws ClassCastException
            return (Player) playerConstructor.newInstance(name, color, score);
        } catch (Exception error) {
            throw new RuntimeException("An error occurred while loading '" + playerClass + "'.\n"
                    + "Debugging Info: " + error.getMessage());
        }
    }

    private PlayerLoader() {}

}
