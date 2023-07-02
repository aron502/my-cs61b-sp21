package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
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
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z",
                                                              Locale.ENGLISH);
    public static final String INITIAL_MSG = "initial commit";


    public Commit() {
        message = INITIAL_MSG;
        date = new Date(0);
        parents = new ArrayList<>(2);
        tracked = new HashMap<>();
        id = generateID();
        file = join(Repository.COMMITS_DIR, id);
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
        file = join(Repository.COMMITS_DIR, id);
    }

    public static Commit readFromFile(String id) {
        if (id.isEmpty()) {
            return null;
        }
        var file = join(Repository.COMMITS_DIR, id);
        if (!file.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(file, Commit.class);
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

    public String getFirstParent() {
        if (parents.isEmpty()) {
            return "";
        }
        return parents.get(0);
    }

    public String getSecondParent() {
        if (parents.size() < 2) {
            return "";
        }
        return parents.get(1);
    }

    public String getMessage() {
        return message;
    }

    public String getTrackedId(String fileName) {
        var fileId = tracked.get(fileName);
        if (fileId == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        return fileId;
    }

    public Set<String> getTrackedFileNames() {
        return tracked.keySet();
    }


    private String generateID() {
        return sha1(date.toString(), message, parents.toString(), tracked.toString());
    }

    @Override
    public String toString() {
        return "===\n"
               + "commit %s\n".formatted(id)
               + (parents.size() == 2
                 ? "Merge: %s %s\n".formatted(parents.get(0).substring(0, 7),
                                              parents.get(1).substring(0, 7))
                 : "")
               + "Date: %s\n".formatted(getTimeStamp())
               + "%s\n".formatted(message);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        return ((Commit) other).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
