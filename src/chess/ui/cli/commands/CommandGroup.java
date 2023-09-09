package chess.ui.cli.commands;

import java.util.Optional;
import java.util.Scanner;
import chess.util.List;

public abstract class CommandGroup {

    protected Scanner scanner;

    protected Command command;

    protected List<String> args;

    protected Object data;

    public CommandGroup() {
        this.scanner = new Scanner(System.in);
        this.args = new List<>();
    }

    public abstract List<Command> availableCommands();

    public void initialize() throws Exception {
        System.out.print("> ");
        String commandLine = this.scanner.nextLine().strip();
        List<String> components = List.of(commandLine.split("\\s+"));
        String command = components.remove(0);
        // set arguments
        this.args = components;
        findCommand: {
            for (Command c : this.availableCommands()) {
                if (c.getKeyword().equals(command)) {
                    this.command = c;
                    break findCommand;
                }
            }
            throw new Exception("Unknown command '" + command + "'.");
        }
        // parse command
        this.parse();
    }

    protected abstract void parse() throws Exception;

    public String getKeyword() {
        return this.command.getKeyword();
    }

    private String getArgument(int index, boolean optional) throws Exception {
        Optional<String> arg = this.args.at(index);
        if (!optional && arg.isEmpty()) {
            index++;
            throw new Exception("Missing required argument at position " + index + ".\n"
                    + this.command.getUsage());
        }
        return arg.orElse("");
    }

    protected String getArgument(int index) throws Exception {
        return this.getArgument(index, false);
    }

    protected String getOptionalArgument(int index) {
        try {
            return this.getArgument(index, true);
        } catch (Exception err) {
            return "";
        }
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
