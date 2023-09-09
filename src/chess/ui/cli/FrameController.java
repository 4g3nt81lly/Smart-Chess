package chess.ui.cli;

import chess.model.Position;
import chess.ui.Interface;
import chess.ui.Printing;

import java.util.function.Consumer;

/*
* +-------------------
* |  Document Title
* |  Title
* |  [Chessboard]
* |  Info
* |  Message ...
* |  [CMD Input] >
* |
* */
public final class FrameController implements Printing {

    private String title;

    private String header;

    private String info;

    private String message;

    private boolean shouldRenderChessboard;

    private Position hintPosition;

    private double delay;

    public FrameController() {
        this.title = "";
        this.header = "";
        this.info = "";
        this.message = "";
        this.shouldRenderChessboard = false;
        this.hintPosition = null;
        this.delay = 0;
    }

    public void render() {
        this.clearScreen();
        // print document title
        if (!this.title.isEmpty()) {
            println(this.title);
        }
        // print header
        if (!this.header.isEmpty()) {
            println(this.header);
        }
        // print chessboard if enabled, possibly with hints
        if (this.shouldRenderChessboard) {
            ChessCLI.getInstance().printChessboard(this.hintPosition);
        }
        // print info
        if (!this.info.isEmpty()) {
            println(this.info);
        }
        // print message
        if (!this.message.isEmpty()) {
            println(this.message);
        }
        Interface.sleep(this.delay);
        this.delay = 0;
    }

    public void clearScreen() {
        print("\033[H\033[2J\033[3J");
    }

    public FrameController reset() {
        this.title = "";
        this.header = "";
        this.info = "";
        this.message = "";
        this.shouldRenderChessboard = false;
        this.hintPosition = null;
        return this;
    }

    public FrameController setTitle(String title) {
        this.title = title;
        return this;
    }

    public FrameController setHeader(String header) {
        this.header = header;
        return this;
    }

    public FrameController setInfo(String info) {
        this.info = info;
        return this;
    }

    public void beginTemporaryInfo(String info, Consumer<FrameController> block) {
        String currentInfo = this.info;
        this.setInfo(info);
        block.accept(this);
        this.setInfo(currentInfo);
    }

    public FrameController clearInfo() {
        this.setInfo("");
        return this;
    }

    public String getInfo() {
        return this.info;
    }

    public FrameController setMessage(String message) {
        this.message = message;
        return this;
    }

    public void renderTemporaryMessage(String message, double duration) {
        String currentMessage = this.message;
        this.setMessage(message).setDelay(duration).render();
        this.setMessage(currentMessage).render();
    }

    public FrameController clearMessage() {
        return this.setMessage("");
    }

    public FrameController setDelay(double delay) {
        this.delay = delay;
        return this;
    }

    public FrameController setChessboardDisplay(boolean shouldDisplay, Position hintPosition) {
        this.shouldRenderChessboard = shouldDisplay;
        this.hintPosition = hintPosition;
        return this;
    }

    public FrameController setChessboardHint(Position hintPosition) {
        return this.setChessboardDisplay(true, hintPosition);
    }

}
