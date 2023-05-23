package gitlet;

import java.io.File;
import java.util.Date;
import java.util.*;
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
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** The staging directory. */
    public static final File STAGING = join(GITLET_DIR, "staging");
    /** The refs dir. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The branch heads dir. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** The blobs dir. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The index file. (store stage). */
    public static final File INDEX = join(CWD, "index");
    /** The HEAD file. */
    public static final File HEAD = join(CWD, "HEAD");

//    ├── *blobs*
//    ├── *commits*
//    ├── HEAD
//    ├── index
//    ├── *refs*
//    │ └── *heads*
//    └── *staging*

    /** Default branch name. */
    public static final String DEFAULT_BRANCH = "master";

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        mkdir();
        mkfile(); // create INDEX
        String commitID = initialCommit();
        writeHEAD(DEFAULT_BRANCH);
        writeHeadsBranch(DEFAULT_BRANCH, commitID);
    }

    /** gitlet add file. */
    public static void add(String fileName) {
        File f = getCWDFile(fileName);
        checkFileExists(f);
        Commit head = getHeadCommit();
        String trackedID = head.getTrackedID(fileName);

        Blob blob = new Blob(fileName);
        String blobID = blob.getId();

        Stage stage = Stage.readFromFile();
        String stageAddedID = stage.getAddedID(fileName);
        // head commits file same as blob
        if (trackedID.equals(blobID)) {
            // no need to add
            // if stage already has file, remove it
            removeStagingFile(stageAddedID);
            stage.remove(stageAddedID);
        } else if (!blobID.equals(stageAddedID)) {
            if (!stageAddedID.equals("")) {
                removeStagingFile(stageAddedID);
            }
            blob.save();
            stage.addFile(fileName, blobID);
        }
        stage.save();
    }

    /** gitlet commit */
    public static void commit(String msg) {
        checkMsgEmpty(msg);
        Stage stage = Stage.readFromFile();
        checkStageEmtpy(stage);
        Commit oldHead = getHeadCommit();
        Commit newHead = new Commit(msg, List.of(oldHead), stage);
        Stage.clear(stage);
        newHead.save();
        String commitID = newHead.getId();
        String branchName = getCurrentBranch();
        writeHeadsBranch(branchName, commitID);
    }

    /** gitlet rm */
    public static void remove(String fileName) {

    }

    public static void checkRepository() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static Date now() {
        return new Date();
    }

    private static void mkdir() {
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        STAGING.mkdir();
        BLOBS_DIR.mkdir();
    }

    private static void mkfile() {
        Stage stage = new Stage();
        writeObject(INDEX, stage);
    }

    private static String initialCommit() {
        Commit initial = new Commit();
        initial.save();
        return initial.getId();
    }

    private static void writeHEAD(String name) {
        writeContents(HEAD, name);
    }

    private static void writeHeadsBranch(String name, String id) {
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

    private static Commit getHeadCommit() {
        String name = getCurrentBranch();
        File f = getBranchFile(name);
        return Commit.readFromFile(f);
    }

    private static void removeStagingFile(String name) {
        join(STAGING, name).delete();
    }

    private static void removeHEAD() {
        HEAD.delete();
    }

    private static void checkMsgEmpty(String msg) {
        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
    }

    private static void checkStageEmtpy(Stage stage) {
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static void checkFileExists(File f) {
        System.out.println("File does not exist.");
        System.exit(0);
    }
}
