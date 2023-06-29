package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
                rm(getObjectFile(id.stageId));
                stage.getAdded().remove(fileName);
                stage.getRemoved().remove(fileName);
            }
        } else if (!id.blobId.equals(id.stageId)) {
            if (!id.stageId.isEmpty()) {
                rm(getObjectFile(id.stageId));
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
        stage.clear()
             .save();
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
            rm(getObjectFile(id.blobId));
        }
        stage.save();
    }

    public static void log() {
        var sb = new StringBuilder();
        var head = getHeadCommit();
        while (head != null) {
            sb.append("%s\n".formatted(head));
            head = Commit.readFromFile(head.getFirstParent());
        }
        System.out.print(sb);
    }

    public static void globalLog() {
        printAllCommit(getHeadCommit());
    }

    public static void find(String msg) {
        printAllId(getHeadCommit(), msg);
    }

    public static void status() {
        var sb = new StringBuilder();
        String headName = readContentsAsString(HEAD);

        String branchNames = plainFilenamesIn(HEADS_DIR).stream()
          .map(name -> name.equals(headName) ? "*" + name : name)
          .collect(Collectors.joining("\n"));

        var stage = Stage.readFromFile();
        String addedNames = String.join("\n", stage.getAdded().keySet());
        String removedNames = String.join("\n", stage.getRemoved());

        sb.append("=== Branches ===\n")
          .append(branchNames)
          .append("\n\n=== Staged Files ===\n")
          .append(addedNames)
          .append("\n\n=== Removed Files ===\n")
          .append(removedNames);

        System.out.println(sb);
    }

    public static void checkout(String[] args) {
        if (args.length == 2) {
            checkoutBranch(args[1]);
        } else if (args.length == 3) {
            checkoutFile(args[2]);
        } else if (args.length == 4) {
            checkoutIdAndFile(args[1], args[3]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void checkoutBranch(String branchName) {
        checkBranchExists(branchName);
        checkIfCurrentBranch(branchName);
        var stage = Stage.readFromFile();
        var tracked = Commit.readFromFile(readContentsAsString(join(HEADS_DIR, branchName)))
                                                  .getTracked();
        checkUnTrackedFile(tracked);
        stage.clear()
             .save();
        clearCWD();
        addFilesToCWD(tracked);
        writeHEAD(branchName);
    }

    private static void addFilesToCWD(Map<String, String> tracked) {
        for (var entry : tracked.entrySet()) {
            writeContents(
              join(CWD, entry.getKey()),
              Blob.readFromFile(entry.getValue()).getContent()
            );
        }
    }

    private static void checkoutFile(String fileName) {
        String fileId = getHeadCommit().getTrackedId(fileName);
        writeContents(
          join(CWD, fileName),
          Blob.readFromFile(fileId).getContent()
        );
    }

    private static void checkoutIdAndFile(String id, String fileName) {
        var commit = Commit.readFromFile(id);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        writeContents(
          join(CWD, fileName),
          Blob.readFromFile(commit.getTrackedId((fileName)))
                   .getContent()
        );
    }

    public static void branch(String branchName) {

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
        processCommit(cmt, System.out::print);
    }

    private static void printAllId(Commit cmt, String msg) {
        processCommit(cmt, commit -> {
            if (commit.getMessage().equals(msg)) {
                System.out.println(commit.getId());
            }
        });
    }

    private static void processCommit(Commit cmt, Consumer<Commit> action) {
        if (cmt == null) {
            return;
        }

        action.accept(cmt);

        processCommit(Commit.readFromFile(cmt.getFirstParent()), action);
        processCommit(Commit.readFromFile(cmt.getSecondParent()), action);
    }

    private static void checkUnTrackedFile(Map<String, String> trakced) {
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!trakced.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private static void checkBranchExists(String branchName) {
        if (!join(HEADS_DIR, branchName).exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }

    private static void checkIfCurrentBranch(String branchName) {
        if (readContentsAsString(HEAD).equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
    }

    private static void clearCWD() {
        var files = CWD.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(".gitlet");
            }
        });
        for (var file : files) {
            rm(file);
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
