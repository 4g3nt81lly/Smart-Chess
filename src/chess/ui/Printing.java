package chess.ui;

public interface Printing {

    default void print(Object object) {
        System.out.print(object);
    }

    default void println(Object object) {
        System.out.println(object);
    }

    default void println() {
        System.out.println();
    }

}
