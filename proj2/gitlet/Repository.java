package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The refs dir. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The branch heads dir. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** The index file. (staging). */
    public static final File INDEX = join(GITLET_DIR, "index");
    /** The HEAD file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /* .gitlet/ folder structure
     * ├── HEAD
     * ├── index
     * ├── objects
     * └── refs
     *     └── heads
     */
    /** Default branch name. */
    public static final String DEFAULT_BRANCH = "master";
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        mkdir();
        String CommitID = initialCommit();
        setHEAD(DEFAULT_BRANCH);
        setHeadsBranch(DEFAULT_BRANCH, CommitID);
    }

    public static void add(String fileName) {

    }

    private static Date now() {
        return new Date(System.currentTimeMillis());
    }

    private static void mkdir() {
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    private static String initialCommit() {
        Commit initial = new Commit("initial commit.", "", new Date(0L), new HashMap<>());
        initial.saveToFile();
        return initial.id();
    }

    private static void setHEAD(String name) {
        writeContents(HEAD, name);
    }

    private static void setHeadsBranch(String name, String id) {
        File f = getHeadsBranch(name);
        writeContents(f, id);
    }

    private static File getHeadsBranch(String name) {
        return join(HEADS_DIR, name);
    }
}
