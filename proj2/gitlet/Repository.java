package gitlet;

import java.io.File;
import java.util.List;

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

    /** Default branch name. */
    public static final String DEFAULT_BRANCH = "master";
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The refs dir. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The branch heads dir. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** The blobs dir. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The index file. (store stage). */
    public static final File INDEX = join(GITLET_DIR, "index");
    /** The HEAD file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);
        mkdir(OBJECTS_DIR);
        mkstage();
        String id = initialCommit();
        writeHEAD(DEFAULT_BRANCH);
        writeHeadBranch(DEFAULT_BRANCH, id);
    }

    /** gitlet add file. */
    public static void add(String fileName) {
        File file = join(CWD, fileName);
        checkFileExists(file);

        var blob = new Blob(file);
        var stage = Stage.readFromFile();
        var id = new TripleId(blob, stage, fileName);

        if (id.trackId.equals(id.blobId)) {
            if (!id.stageId.isEmpty()) {
                restrictedDelete(getObjectFile(id.stageId));
                stage.getAdded().remove(fileName);
                stage.getRemoved().remove(fileName);
            }
        } else if (!id.blobId.equals(id.stageId)) {
            if (!id.stageId.isEmpty()) {
                restrictedDelete(getObjectFile(id.stageId));
            }
            blob.save();
            stage.addFile(fileName, id.blobId);
        }
        stage.save();
    }

    /** gitlet commit */
    public static void commit(String msg) {
        checkMsgEmpty(msg);
        var stage = Stage.readFromFile();
        checkStageEmpty(stage);
        var newCommit = new Commit(msg, List.of(getHeadCommit()), stage);
        newCommit.save();
        String id = newCommit.getId();
        writeHeadBranch(getCurrentBranch(), id);
        stage.clear();
    }

    /** gitlet rm */
    public static void remove(String fileName) {
        File file = join(CWD, fileName);

        var blob = new Blob(file);
        var stage = Stage.readFromFile();
        var id = new TripleId(blob, stage, fileName);

        checkStageIdAndHeadId(id.stageId, id.trackId);
        if (!id.stageId.isEmpty()) {
            stage.getAdded().remove(fileName);
        } else {
            // stageId is empty, headId not empty
            stage.getRemoved().add(fileName);
        }

        if (blob.exists() && id.blobId.equals(id.trackId)) {
            restrictedDelete(getObjectFile(id.blobId));
        }
        stage.save();
    }

    public static void log() {
        var sb = new StringBuilder();
        var head = getHeadCommit();
        while (head != null) {
            sb.append(head);
            head = Commit.readFromFile(head.getParents().get(0));
        }
        System.out.print(sb);
    }

    public static void globalLog() {
        var commit = getHeadCommit();
        printAllCommit(commit);
    }

    public static void checkRepository() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static String initialCommit() {
        Commit initial = new Commit();
        initial.save();
        return initial.getId();
    }

    private static Commit getHeadCommit() {
        String branch = getCurrentBranch();
        String id = readContentsAsString(join(HEADS_DIR, branch));
        return readObject(getObjectFile(id), Commit.class);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    private static void writeHEAD(String branchName) {
        writeContents(HEAD, branchName);
    }

    private static void writeHeadBranch(String branchName, String id) {
        File f = join(HEADS_DIR, branchName);
        writeContents(f, id);
    }

    private static void mkstage() {
        Stage staging = new Stage();
        staging.save();
    }

    private static void checkStageIdAndHeadId(String stageId, String headId) {
        if (stageId.isEmpty() && headId.isEmpty()) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static void checkMsgEmpty(String msg) {
        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
    }

    private static void checkStageEmpty(Stage stage) {
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static void checkFileExists(File f) {
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    private static void printAllCommit(Commit cmt) {
        if (cmt == null) {
            return;
        }
        System.out.print(cmt);
        String first = cmt.getParents().get(0);
        String second = cmt.getParents().get(1);
        if (first != null) {
            printAllCommit(Commit.readFromFile(first));
        }
        if (second != null) {
            printAllCommit(Commit.readFromFile(second));
        }
    }

    private static class TripleId {
        final String trackId;
        final String blobId;
        final String stageId;

        TripleId(Blob blob, Stage stage, String fileName) {
            trackId = getHeadCommit()
                    .getTracked()
                    .getOrDefault(fileName, "");
            blobId = blob.getId();
            stageId = stage.getAdded().getOrDefault(fileName, "");
        }
    }
}
