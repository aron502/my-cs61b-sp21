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
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss 'UTC', EEEE, d MMMM yyyy");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private final String message;
    // commit id
    private final String id;
    private List<String> parents;
    private final String date;
    // store file name and their hash
    private HashMap<String, String> tracked;
    private File file;

    public Commit() {
        message = "initial commit.";
        date = sdf.format(new Date(0));
        id = sha1(message, date);
        file = join(Repository.COMMITS_DIR, id);
        parents = new LinkedList<>();
        tracked = new HashMap<>();
    }

    public Commit(String msg, List<Commit> parents, Stage stage) {
        message = msg;
        this.parents = parents.stream()
                .map(Commit::getId)
                .collect(Collectors.toCollection(() -> new ArrayList<>(2)));
        date = sdf.format(new Date());
        id = sha1(message, parents.toString(), date, tracked.toString());
        file = join(Repository.COMMITS_DIR, id);

        // store stage changes to tracked.
        tracked = parents.get(0).getTracked();
        tracked.putAll(stage.getAdded());
        stage.getRemoved().forEach(tracked::remove);
    }

    public static Commit readFromFile(File f) {
        return readObject(f, Commit.class);
    }

    public void save() {
        writeObject(file, this);
    }

    public String getId() {
        return id;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, String> getTracked() {
        return tracked;
    }

    public String getTrackedID(String name) {
        return tracked.getOrDefault(name, "");
    }

}
