package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author aron502
 */
public class Repository {
     /*
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "objects");
    /** The staging directory. */
    public static final File STAGING = join(GITLET_DIR, "staging");
    /** The refs dir. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The branch heads dir. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The index file. (staging). */
    public static final File INDEX = join(CWD, "index");
    /** The HEAD file. */
    public static final File HEAD = join(CWD, "HEAD");

//    ├── commits
//    ├── HEAD
//    ├── index
//    ├── refs
//    │   └── heads
//    └── staging

    /** Default branch name. */
    public static final String DEFAULT_BRANCH = "master";
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        mkdir();
        String commitID = initialCommit();
        setHEAD(DEFAULT_BRANCH);
        setHeadsBranch(DEFAULT_BRANCH, commitID);
    }

    /** gitlet add file. */
    public static void add(String fileName) {
        File f = getCWDFile(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Commit head = getHead();
        String headID = head.getBlobID(fileName);
        Blob blob = new Blob(fileName);
        String blobID = blob.getId();
        Stage stage = readStage();
        String stageID = stage.getAddedID(fileName);
        // head commit's file same as blob
        if (headID.equals(blobID)) {
            // remove stage file
            removeStagingFile(stageID);
            stage.removeFile(fileName);
            writeStage(stage);
        } else {
            if (stageID.equals(blobID)) {
                return;
            }
            if (!stageID.equals("")) {
                removeStagingFile(stageID);
            }
            blob.saveToFile();
            stage.addFile(fileName, blobID);
            writeStage(stage);
        }
    }

    public static void checkRepository() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static Date now() {
        return new Date(System.currentTimeMillis());
    }

    private static void mkdir() {
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        STAGING.mkdir();
        BLOBS_DIR.mkdir();
    }

    private static String initialCommit() {
        Commit initial = new Commit("initial commit.", "", new Date(0L), new HashMap<>());
        initial.saveToFile();
        return initial.getId();
    }

    private static void setHEAD(String name) {
        writeContents(HEAD, name);
    }

    private static void setHeadsBranch(String name, String id) {
        File f = getBranchFile(name);
        writeContents(f, id);
    }

    private static File getBranchFile(String name) {
        return join(HEADS_DIR, name);
    }

    private static File getCWDFile(String name) {
        return join(CWD, name);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    private static Commit getCommitFromFile(File f) {
        return readObject(f, Commit.class);
    }

    private static Commit getHead() {
        String name = getCurrentBranch();
        File f = getBranchFile(name);
        return getCommitFromFile(f);
    }

    private static Stage readStage() {
        return readObject(INDEX, Stage.class);
    }

    private static void removeStagingFile(String name) {
        join(STAGING, name).delete();
    }

    private static void writeStage(Stage stage) {
        writeObject(INDEX, stage);
    }
}
