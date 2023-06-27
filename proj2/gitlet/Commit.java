package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author aron502
 */
public class Commit implements Serializable {
    /*
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    // commit id
    private final String id;
    private final Date date;
    // store filePath and blob's id
    private final List<String> parents;
    private final Map<String, String> tracked;
    // commit blob file
    private final File file;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss 'UTC', EEEE, d MMMM yyyy", Locale.ENGLISH);


    public Commit() {
        message = "initial commit.";
        date = new Date(0);
        parents = new LinkedList<>();
        tracked = new HashMap<>();
        id = generateID();
        file = getObjectFile(id);
    }

    public Commit(String msg, List<Commit> parents, Stage stage) {
        message = msg;
        date = new Date();
        this.parents = new ArrayList<>(2);
        parents.forEach(p -> this.parents.add(p.getId()));
        tracked = parents.get(0).getTracked();
        tracked.putAll(stage.getAdded());
        stage.getRemoved().forEach(tracked::remove);
        id = generateID();
        file = getObjectFile(id);
    }

    public static Commit readFromFile(String id) {
        return readObject(getObjectFile(id), Commit.class);
    }

    public void save() {
        saveObject(file, this);
    }

    public String getId() {
        return id;
    }

    public String getTimeStamp() {
        return sdf.format(date);
    }

    public Map<String, String> getTracked() {
        return tracked;
    }

    private String generateID() {
        return sha1(date.toString(), message, parents.toString(), tracked.toString());
    }
}
