package chess.ui.gui.views;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;

public class ViewUtils {

    // EFFECTS: shows an alert dialog
    public static void alert(Component owner, String title, String message, int type) {
        JOptionPane.showMessageDialog(owner, message, title, type);
    }

    // EFFECTS: shows the confirmation dialog
    public static int confirm(Component owner, String title, String message, int optionType) {
        return JOptionPane.showConfirmDialog(owner, message, title,
                optionType, JOptionPane.QUESTION_MESSAGE);
    }

    // EFFECTS: shows the file selection dialog (loading file)
    public static Optional<File> selectFile(Window owner, String title,
                                            FilenameFilter filenameFilter) {
        return selectFile(owner, title, FileDialog.LOAD, null, filenameFilter, false);
    }

    public static Optional<File> selectDirectory(Window owner, String title) {
        return selectFile(owner, title, FileDialog.LOAD, null, null, true);
    }

    // EFFECTS: shows the file selection dialog
    private static Optional<File> selectFile(Window owner, String title, int mode, String fileName,
                                             FilenameFilter filenameFilter, boolean selectDirectories) {
        if (selectDirectories) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
        }
        FileDialog fileChooser;
        if (owner instanceof Frame frame) {
            fileChooser = new FileDialog(frame, title, mode);
        } else if (owner instanceof Dialog dialog) {
            fileChooser = new FileDialog(dialog, title, mode);
        } else {
            throw new IllegalArgumentException();
        }
        if (filenameFilter == null && (owner instanceof FilenameFilter)) {
            filenameFilter = (FilenameFilter) owner;
        }
        if (fileName != null) {
            fileChooser.setFile(fileName);
        }
        fileChooser.setFilenameFilter(filenameFilter);
        fileChooser.setVisible(true);
        if (selectDirectories) {
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
        }
        try {
            return Optional.of(fileChooser.getFiles()[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            // user cancelled file chooser
            return Optional.empty();
        }
    }

    // EFFECTS: shows the file save dialog (saving file)
    public static Optional<File> saveAsFile(Frame owner, String title, String fileName,
                                            FilenameFilter filenameFilter) {
        return selectFile(owner, title, FileDialog.SAVE, fileName, filenameFilter, false);
    }

    public static ImageIcon scaleImageIcon(ImageIcon imageIcon, int width, int height) {
        Image image = imageIcon.getImage();
        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    // EFFECTS: creates a grid bag constraint
    public static GridBagConstraints createGridBagConstraints(int x, int y,
                                                              int spanX, int spanY,
                                                              Insets insets, int anchor) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = spanX;
        constraints.gridheight = spanY;
        constraints.insets = insets;
        constraints.anchor = anchor;
        return constraints;
    }

    // EFFECTS: creates a grid bag constraint with a center anchor
    public static GridBagConstraints createGridBagConstraints(int x, int y,
                                                              int spanX, int spanY,
                                                              Insets insets) {
        return createGridBagConstraints(x, y, spanX, spanY, insets,
                GridBagConstraints.CENTER);
    }

    private ViewUtils() {}

}
