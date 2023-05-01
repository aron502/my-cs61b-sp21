package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.TimeZone;

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
    private String message;
    // commit id
    private String id;
    private String parentID;
    private String date;
    // store file name and their hash
    private HashMap<String, String> blobs;
    private File file;

    /* TODO: fill in the rest of this class. */
    public Commit(String msg, String pID, Date d, HashMap<String, String> map) {
        message = msg;
        parentID = pID;
        date = sdf.format(d);
        blobs = map;
        id = sha1(message, parentID, date, serialize(blobs));
        file = getCommitFile(id);
    }

    public void saveToFile() {
        writeObject(file, this);
    }

    public String id() {
        return id;
    }

    public String message() {
        return message;
    }

    private File getCommitFile(String id) {
        // git folder format.
        File folder = join(Repository.OBJECTS_DIR, id.substring(0, 2));
        if (!folder.exists()) {
            folder.mkdir();
        }
        return join(folder, id.substring(2));
    }

}
