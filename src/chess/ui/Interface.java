package chess.ui;

public interface Interface extends Printing {

    String appName = "Smart Chess";

    static void sleep(double duration) {
        try {
            Thread.sleep((long) (duration * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void run();

}
