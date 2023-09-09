package chess.ui;

import java.awt.*;

public class Presets {


    // =============== TEXT SYMBOLS ===============

    public static final char separator = 0x2501;


    // =============== SIZES ===============


    public static final Dimension squareSize = new Dimension(55, 55);
    public static final Dimension pieceSize = new Dimension(40, 45);
    public static final float axisLabelFontSize = 22;
    public static final float headingFontSize = 22;
    public static final float subheadingFontSize = 16;
    public static final float pieceFontSize = 51;
    public static final int squareHoverBorder = 8;
    public static final int gamePanelMaxWidth = 200;
    public static final int gameHistoryListHeight = 240;


    // =============== COLORS ===============


    public static final Color whiteSquare = new Color(255, 206, 158);
    public static final Color whiteSquareHover = new Color(229, 176, 130);
    public static final Color blackSquare = new Color(209, 139, 71);
    public static final Color blackSquareHover = new Color(185, 124, 60);

    public static final Color white = Color.white;
    public static final Color lightGray = Color.lightGray;
    public static final Color gray = Color.gray;
    public static final Color black = Color.black;
    public static final Color red = new Color(250, 110, 110);
    public static final Color green = new Color(146, 192, 137);
    public static final Color blue = new Color(88, 166, 169);
    public static final Color purple = new Color(201, 146, 225);

    public static final Color disabledChevronButtonColor = new Color(206, 206, 206);


    // =============== MISCELLANEOUS ===============





    // =============== ANSI UTILITIES ===============


    public enum FillMode {
        background, foreground;
    }

    public static String getEscapeSequence(Color color, FillMode mode) {
        int sequenceMode = (mode == FillMode.background) ? 48 : 38;
        return String.format("\033[%d;2;%d;%d;%dm", sequenceMode, color.getRed(), color.getGreen(), color.getBlue());
    }

    private Presets() {}

}
