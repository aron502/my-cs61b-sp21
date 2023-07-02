package gitlet;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
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
    /** The blobs dir (not store commits). */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The index file. (store stage). */
    public static final File INDEX = join(GITLET_DIR, "index");
    /** The HEAD file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** Store commits dir. */
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("""
                A Gitlet version-control system already
                 exists in the current directory.
                """);
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);
        mkdir(OBJECTS_DIR);
        mkdir(COMMITS_DIR);
        new Stage().save();
        var id = initialCommit();
        writeHEAD(DEFAULT_BRANCH);
        writeHeadBranch(DEFAULT_BRANCH, id);
    }

    /** gitlet add fileName */
    public static void add(String fileName) {
        var file = join(CWD, fileName);
        checkFileExists(file);

        var blob = new Blob(file);
        var stage = Stage.readFromFile();
        var id = new TrackBlobStageId(blob, stage, fileName);

        if (id.trackId.equals(id.blobId)) {
            if (!id.blobId.equals(id.stageId)) {
                rm(getObjectFile(id.stageId));
                stage.delete(fileName);
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
        commit(msg, List.of(getHeadCommit()), stage);
    }

    private static void commit(String msg, List<Commit> parents, Stage stage) {
        var newCommit = new Commit(msg, parents, stage);
        newCommit.save();
        var id = newCommit.getId();
        writeHeadBranch(getCurrentBranch(), id);
        stage.clear()
             .save();
    }

    /** gitlet rm fileName */
    public static void remove(String fileName) {
        var file = join(CWD, fileName);

        var blob = new Blob(file);
        var stage = Stage.readFromFile();
        var id = new TrackBlobStageId(blob, stage, fileName);

        checkStageIdAndHeadId(id.stageId, id.trackId);
        if (!id.stageId.isEmpty()) {
            stage.getAdded().remove(fileName);
        } else {
            // stageId is empty, headId not empty
            stage.getRemoved().add(fileName);
        }

        if (blob.exists() && id.blobId.equals(id.trackId)) {
            restrictedDelete(file);
        }
        stage.save();
    }

    /** gitlet log */
    public static void log() {
        var sb = new StringBuilder();
        var head = getHeadCommit();
        while (head != null) {
            sb.append("%s\n".formatted(head));
            head = Commit.readFromFile(head.getFirstParent());
        }
        System.out.print(sb);
    }

    /** gitlet global-log */
    public static void globalLog() {
        System.out.println(
            myPlainFilenamesIn(COMMITS_DIR).stream()
                .map(Commit::readFromFile)
                .map(commit -> commit.toString())
                .collect(Collectors.joining("\n"))
        );
    }

    /** gitlet find */
    public static void find(String msg) {
        var result = myPlainFilenamesIn(COMMITS_DIR).stream()
                        .map(Commit::readFromFile)
                        .filter(commit -> commit.getMessage().equals(msg))
                        .map(Commit::getId)
                        .collect(Collectors.joining("\n"));
        if (result.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println(result);
    }

    /** gitlet status */
    public static void status() {
        var sb = new StringBuilder();
        var headName = readContentsAsString(HEAD);

        var branchNames = myPlainFilenamesIn(HEADS_DIR).stream()
                             .sorted()
                             .map(name -> name.equals(headName)
                                 ? "*" + name : name)
                             .collect(Collectors.joining("\n"));

        var stage = Stage.readFromFile();
        var addedNames = stage.getAdded().keySet().stream()
          .sorted()
          .collect(Collectors.joining("\n"));
        var removedNames = stage.getRemoved().stream()
          .sorted()
          .collect(Collectors.joining("\n"));
        var modifiedNames = "";
        var unTrackedNames = String.join("\n",
                                getUnTrackedFiles(getHeadCommit().getTrackedFileNames()));

        sb.append("=== Branches ===\n")
          .append(branchNames)
          .append("\n\n=== Staged Files ===\n")
          .append(addedNames.isEmpty() ? addedNames : addedNames + "\n")
          .append("\n=== Removed Files ===\n")
          .append(removedNames.isEmpty() ? removedNames : removedNames + "\n")
          .append("\n=== Modifications Not Staged For Commit ===\n")
          .append(modifiedNames.isEmpty() ? modifiedNames : modifiedNames + "\n")
          .append("\n=== Untracked Files ===\n")
          .append(unTrackedNames.isEmpty() ? unTrackedNames : unTrackedNames + "\n");

        System.out.println(sb);
    }

    /** gitlet checkout */
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
        checkIfBranchExists(branchName);
        checkIfCurrentBranch(branchName);
        var tracked = Commit.readFromFile(getBranchId(branchName))
                                                  .getTracked();
        checkUnTrackedFileExists(tracked.keySet());
        Stage.readFromFile().clear().save();
        clearCWD();
        addFilesToCWD(tracked);
        writeHEAD(branchName);
    }

    private static void checkoutFile(String fileName) {
        var fileId = getHeadCommit().getTrackedId(fileName);
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

    /** gitlet branch branchName */
    public static void branch(String branchName) {
        var file = join(HEADS_DIR, branchName);
        checkBranchFile(file);
        writeContents(file, getHeadCommit().getId());
    }

    /** gitlet rm-branch branchName */
    public static void rmBranch(String branchName) {
        var file = join(HEADS_DIR, branchName);
        checkBranchExists(file);
        checkIfBranchCanRemove(file);
        rm(file);
    }

    /** gitlet reset commitId */
    public static void reset(String commitId) {
        var commit = Commit.readFromFile(commitId);

        clearCWD();
        addFilesToCWD(commit.getTracked());
        Stage.readFromFile().clear().save();
        writeHeadBranch(readContentsAsString(HEAD), commit.getId());
    }

    /** gitlet merge branchName */
    public static void merge(String branchName) {
        var file = join(HEADS_DIR, branchName);
        checkBranchExists(file);

        var stage = Stage.readFromFile();
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        var headName = getCurrentBranch();
        if (headName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        var branchCommit = Commit.readFromFile(readContentsAsString(file));
        var headCommit = getHeadCommit();
        var ancestor = getSplitPonintCommit(branchCommit, headCommit);

        if (ancestor.equals(branchCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (ancestor.equals(headCommit)) {
            checkoutBranch(headName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        merge(branchCommit, headCommit, ancestor);
        commit("Merged %s into %s.".formatted(branchName, headName),
            List.of(branchCommit, headCommit),
                     stage
        );
    }

    private static void merge(Commit branch, Commit head, Commit ancestor) {
        var modified = new HashSet<String>();
        var removed = new HashSet<String>();
        var conflicted = new HashSet<String>();

        var fileNames = getFileNames(branch, head, ancestor);
        for (var fileName : fileNames) {
            var branchId = branch.getTracked().getOrDefault(fileName, "");
            var headId = head.getTracked().getOrDefault(fileName, "");
            var ancestorId = ancestor.getTracked().getOrDefault(fileName, "");

            if (headId.equals(branchId) || ancestorId.equals(branchId)) {
                // head's tracked same with branch's tracked
                // branch's tracked same with ancestor's tracked
                continue;
            }
            if (ancestorId.equals(headId)) {
                if (branchId.isEmpty()) {
                    removed.add(fileName);
                } else {
                    modified.add(fileName);
                }
            } else {
                conflicted.add(fileName);
            }
        }

        var untrackedFiles = getUnTrackedFiles(head.getTrackedFileNames());
        for (var fileName : untrackedFiles) {
            if (removed.contains(fileName)
                || modified.contains(fileName)
                || conflicted.contains(fileName)) {
                System.out.println("""
                                    There is an untracked file in the way;\
                                     delete it, or add and commit it first.
                                    """);
                System.exit(0);
            }
        }

        if (!removed.isEmpty()) {
            removed.forEach(Repository::remove);
        }

        if (!modified.isEmpty()) {
            modified.forEach(name -> {
                writeContents(join(CWD, name),
                    Blob.readFromFile(branch.getTrackedId(name)).getContent());
                add(name);
            });
        }

        if (!conflicted.isEmpty()) {
            conflicted.forEach(name -> {
                var headContent = readContentsAsString(
                    getObjectFile(head.getTrackedId(name))
                );
                var branchContent = readContentsAsString(
                    getObjectFile(branch.getTrackedId(name))
                );
                writeContents(join(CWD, name),
                              "<<<<<<< HEAD\n" + headContent + "\n=======\n"
                              + branchContent + "\n>>>>>>>");
                System.out.println("Encountered a merge conflict.");
            });
        }
    }

    private static List<String> getUnTrackedFiles(Set<String> headTracked) {
        var stage = Stage.readFromFile();
        return myPlainFilenamesIn(CWD).stream().sorted()
              .filter(name -> !headTracked.contains(name) && !stage.contains(name))
              .collect(Collectors.toList());
    }


    private static Set<String> getFileNames(Commit branch, Commit head, Commit ancestor) {
        return Stream.of(branch.getTrackedFileNames(),
                        head.getTrackedFileNames(),
                        ancestor.getTrackedFileNames())
                     .flatMap(Set::stream)
                     .collect(Collectors.toSet());
    }

    public static void dfs(Commit cmt, Set<String> set) {
        if (cmt == null) {
            return;
        }
        set.add(cmt.getId());
        dfs(Commit.readFromFile(cmt.getFirstParent()), set);
        dfs(Commit.readFromFile(cmt.getSecondParent()), set);
    }

    private static String getBranchId(String currentName) {
        return readContentsAsString(join(HEADS_DIR, currentName));
    }

    private static Commit getSplitPonintCommit(Commit branchCommit, Commit currentCommit) {
        var set = new HashSet<String>();
        dfs(branchCommit, set);
        Queue<Commit> queue = new LinkedList<>();
        queue.add(currentCommit);
        while (!queue.isEmpty()) {
            var cmt = queue.poll();
            if (set.contains(cmt.getId())) {
                return cmt;
            }
            var firstParent = cmt.getFirstParent();
            var secondParent = cmt.getSecondParent();
            if (firstParent.isEmpty()) {
                queue.add(Commit.readFromFile(firstParent));
            }
            if (secondParent.isEmpty()) {
                queue.add(Commit.readFromFile(secondParent));
            }
        }
        return new Commit();
    }

    private static void checkIfBranchCanRemove(File file) {
        if (readContentsAsString(HEAD).equals(file.getName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
    }

    private static void checkBranchExists(File file) {
        if (!file.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    private static void checkBranchFile(File file) {
        if (file.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    public static void checkRepository() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static String initialCommit() {
        var initial = new Commit();
        initial.save();
        return initial.getId();
    }

    private static Commit getHeadCommit() {
        var branch = getCurrentBranch();
        var id = readContentsAsString(join(HEADS_DIR, branch));
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    private static void writeHEAD(String branchName) {
        writeContents(HEAD, branchName);
    }

    private static void writeHeadBranch(String branchName, String id) {
        var f = join(HEADS_DIR, branchName);
        writeContents(f, id);
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

    private static void checkUnTrackedFileExists(Set<String> trakced) {
        if (!getUnTrackedFiles(trakced).isEmpty()) {
            System.out.println("""
                               There is an untracked file in the way; \
                               delete it, or add and commit it first.
                                """);
            System.exit(0);
        }
    }

    private static void checkIfBranchExists(String branchName) {
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
        var files = CWD.listFiles((dir, name) -> !name.equals(".gitlet"));
        if (files != null) {
            for (var file : files) {
                deleteFiles(file);
            }
        }
    }

    /** add all tracked files to CWD. */
    private static void addFilesToCWD(Map<String, String> tracked) {
        for (var entry : tracked.entrySet()) {
            writeContents(
                join(CWD, entry.getKey()),
                Blob.readFromFile(entry.getValue()).getContent()
            );
        }
    }

    private static List<String> myPlainFilenamesIn(File file) {
        var list = plainFilenamesIn(file);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private static class TrackBlobStageId {
        final String trackId;
        final String blobId;
        final String stageId;

        TrackBlobStageId(Blob blob, Stage stage, String fileName) {
            trackId = getHeadCommit()
                    .getTracked()
                    .getOrDefault(fileName, "");
            blobId = blob.getId();
            stageId = stage.getAdded().getOrDefault(fileName, "");
        }
    }
}
