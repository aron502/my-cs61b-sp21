package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author aron502
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                checkArgumentsNum(args, 1);
                Repository.init();
            }
            case "add" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.add(args[1]);
            }
            case "commit" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.commit(args[1]);
            }
            case "rm" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.remove(args[1]);
            }
            case "log" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 1);
                Repository.log();
            }
            case "global-log" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 1);
                Repository.globalLog();
            }
            case "find" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.find(args[1]);
            }
            case "status" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 1);
                Repository.status();
            }
            case "checkout" -> {
                Repository.checkRepository();
                Repository.checkout(args);
            }
            case "branch" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.branch(args[1]);
            }
            case "rm-branch" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.rmBranch(args[1]);
            }
            case "reset" -> {
                Repository.checkRepository();
                checkArgumentsNum(args, 2);
                Repository.reset(args[1]);
            }
            default -> {
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
        }
    }

    private static void checkArgumentsNum(String[] args, int expected) {
        if (args.length != expected) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
