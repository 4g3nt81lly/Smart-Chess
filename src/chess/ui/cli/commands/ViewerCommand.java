package chess.ui.cli.commands;

import chess.util.List;

public class ViewerCommand extends CommandGroup {

    private int step;

    private double seconds;

    @Override
    public List<Command> availableCommands() {
        return List.of(
                new Command("/replay", 1, "/replay <seconds?>",
                        "Replay the game with <seconds> seconds per frame."),
                new Command("/next", 1, "/next <no.?>",
                        "Navigate <no.> move(s) forward."),
                new Command("/prev", 1, "/prev <no.?>",
                        "Navigate <no.> move(s) backward."),
                new Command("/start", "/start",
                        "Jump to the start of the game."),
                new Command("/end", "/end",
                        "Jump to the end of the game."),
                new Command("/exit", "/exit", "Exit the viewer."),
                new Command("/help", "/help", "Show this help.")
        );
    }

    @Override
    protected void parse() throws Exception {
        switch (this.command.getKeyword()) {
            case "/replay":
                try {
                    this.seconds = Double.parseDouble(this.getOptionalArgument(0));
                } catch (NumberFormatException e) {
                    this.seconds = 1;
                }
                break;
            case "/next":
            case "/prev":
                try {
                    this.step = Integer.parseInt(this.getOptionalArgument(0));
                } catch (NumberFormatException e) {
                    this.step = 1;
                }
                break;
        }
    }

    public int getStep() {
        return this.step;
    }

    public double getSecondsPerFrame() {
        return this.seconds;
    }

}
