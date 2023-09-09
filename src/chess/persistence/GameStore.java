package chess.persistence;

import chess.model.ChessGame;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

// A persistent storage manager for a Chess game that handles both reading and writing.
public final class GameStore {

    public static final String defaultFileName = "~/new_game";

    public static final String fileExtension = ".chess";

    private File fileSource;

    private ChessGame checkpoint;

    private boolean unsavedChanges;

    // EFFECTS: constructs a persistent storage manager with a given file path
    //          if filePath is null, then a file path to a new file is generated
    //          otherwise if filePath is provided, then a file handle is created
    public GameStore(String filePath) {
        if (filePath == null) {
            // no file path specified, generate default file
            // (creating a new file at CWD)
            int id = 1;
            File fileObject = new File(defaultFileName + fileExtension);
            while (fileObject.exists()) {
                fileObject = new File(defaultFileName + id + fileExtension);
                id++;
            }
            this.fileSource = fileObject;
        } else {
            // a file path was specified
            // (reading from a file or creating a new file at the path)
            // perform home path expansion
            filePath = filePath.replaceAll("^~", System.getProperty("user.home"));
            if (!filePath.endsWith(fileExtension)) {
                filePath += fileExtension;
            }
            this.fileSource = new File(filePath);
        }
        this.unsavedChanges = false;
    }

    // EFFECTS: constructs a persistent storage manager with no source file path
    //          (i.e. creates new file path)
    public GameStore() {
        this(null);
    }

    // MODIFIES: this
    // EFFECTS: sets the source of the persistent store to a new path,
    //          and performs all necessary preprocessing:
    //            1. home path expansion
    //            2. appending file extension if not already
    public void setNewFilePath(String newPath) {
        newPath = newPath.replaceAll("^~", System.getProperty("user.home"));
        if (!newPath.endsWith(fileExtension)) {
            newPath += fileExtension;
        }
        this.fileSource = new File(newPath);
    }

    // MODIFIES: this
    // EFFECTS: reads the content of the file and loads a game object with the
    //          current file source, with an interface object that is used to
    //          initialize console players;
    //          throws Exceptions on:
    //            1. IOException: failure to read from the source file;
    //            2. other Exceptions: failure to decode and load the object as a game object
    //               due to format issues;
    public ChessGame read() throws IOException {
        String jsonSource = Files.readString(this.fileSource.toPath(),
                StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(jsonSource);
        this.checkpoint = new ChessGame(jsonObject);
        return this.checkpoint.copy();
    }

    // MODIFIES: this
    // EFFECTS: saves the given game object to the current source file;
    //          updates the checkpoint with a copy of the given game object, this will be
    //            used to revert to the most-recent saved game state;
    //          resets has unsaved changes flag (similar to a change counter, but a boolean);
    //          throws Exceptions on:
    //            1. IOException: failure to write to the source file;
    //            2. other Exceptions: failure to create a copy of the game object as checkpoint;
    public void saveCheckpoint(ChessGame game) throws IOException {
        try {
            // create a copy of the game object as restorable checkpoint
            ChessGame newCheckpoint = game.copy();
            try (FileWriter writer = new FileWriter(this.fileSource,
                    StandardCharsets.UTF_8, false)) {
                writer.write(game.encode().toString(4));
            }
            // successfully created a checkpoint AND saved to file
            this.checkpoint = newCheckpoint;
            // reset flag for whether there exists unsaved changes
            this.unsavedChanges = false;
        } catch (IOException err) {
            // exceptions with file writing
            // propagate IOExceptions thrown from FileWriter
            throw err;
        } catch (Exception err) {
            // exceptions with creating checkpoint or something else
            // rethrow (thrown when creating a new checkpoint) as IOExceptions
            throw new IOException("Unable to create a checkpoint, save aborted.\n"
                    + err.getMessage());
        }
    }

    // REQUIRES: invoker has checked using hasCheckpoint() before retrieving,
    //           otherwise this may throw a NullPointerException
    // MODIFIES: this
    // EFFECTS: retrieves (a deep copy of) the most recently saved checkpoint;
    //          resets has unsaved changes flag to false;
    //          throws on failure to create a copy of the checkpoint;
    public ChessGame retrieveCheckpoint() {
        // update unsaved changes state
        ChessGame copiedCheckpoint = this.checkpoint.copy();
        // update unsaved changes flag ONLY IF the copy is made
        this.unsavedChanges = false;
        return copiedCheckpoint;
    }

    // EFFECTS: returns the file source object
    public File getFileSource() {
        return this.fileSource;
    }

    // EFFECTS: returns the absolute file source path
    public String getFilePath() {
        return this.fileSource.getPath();
    }

    public String getFileName() {
        return this.fileSource.getName();
    }

    // MODIFIES: this
    // EFFECTS: marks the game store object that there are unsaved changes
    public void markUnsavedChanges() {
        this.unsavedChanges = true;
    }

    // EFFECTS: returns true if the source file is a new file, otherwise false
    public boolean isNewFile() {
        return !this.fileSource.exists();
    }

    // EFFECTS: returns true if there is a recently saved checkpoint, otherwise false
    public boolean hasCheckpoint() {
        return this.checkpoint != null;
    }

    // EFFECTS: returns true if there are unsaved changes, otherwise false
    public boolean hasUnsavedChanges() {
        return this.unsavedChanges;
    }

}
