package chess.ui.cli.commands;

public class Command {

    private final String keyword;

    private final int numberOfArgs;

    private final String usage;

    private final String description;

    public Command(String keyword, int numberOfArgs, String usage, String description) {
        this.keyword = keyword;
        this.numberOfArgs = numberOfArgs;
        this.usage = usage;
        this.description = description;
    }

    public Command(String keyword, String usage, String description) {
        this(keyword, 0, usage, description);
    }

    public String getKeyword() {
        return this.keyword;
    }

    public int getNumberOfArgs() {
        return this.numberOfArgs;
    }

    public String getUsage() {
        return "Usage: " + this.usage;
    }

    public String getDescription() {
        return this.description;
    }

    public String getHelpMessage() {
        return "- " + this.usage + "\n  " + this.description;
    }
}
