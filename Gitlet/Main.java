package gitlet;

import java.io.File;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
            case "init":
                doInit();
                break;
            case "add":
                doAdd(args);
                break;
            case "commit":
                doCommit(args);
                break;
            case "rm":
                doRm(args);
                break;
            case "log":
                doLog();
                break;
            case "global-log":
                doGlobalLog();
                break;
            case "find":
                doFind(args);
                break;
            case "status":
                doStatus();
                break;
            case "checkout":
                doCheckout(args);
                break;
            case "branch":
                doBranch(args);
                break;
            case "rm-branch":
                doRmBranch(args);
                break;
            case "merge":
                doMerge(args);
                break;
            case "reset":
                doReset(args);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }

    private static void doInit() {
        if (loadGitlet() != null) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
        } else {
            Gitlet gitlet = new Gitlet();
            saveGitlet(gitlet);
        }
    }


    private static Gitlet loadGitlet() { //deserialize
        Gitlet gitlet = null;
        File dir = new File(".gitlet");
        if (dir.exists()) {
            File inFile = new File(".gitlet/gitlet");
            gitlet = (Gitlet) Utils.readObject(inFile);
        }
        return gitlet;
    }

    private static void saveGitlet(Gitlet gitlet) { //serialize
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outfile = new File(".gitlet/gitlet");
        Utils.writeObject(outfile, gitlet);
    }

    private static void doAdd(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.add(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doCommit(String... args) {
        if (args.length < 2 || args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.commit(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doRm(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.rm(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doLog() {
        Gitlet gitlet = loadGitlet();
        if (gitlet == null) {
            System.out.println("Not in an initialized directory.");
        } else {
            gitlet.log();
        }
    }

    private static void doGlobalLog() {
        Gitlet gitlet = loadGitlet();
        if (gitlet == null) {
            System.out.println("Not in an initialized directory.");
        } else {
            gitlet.globallog();
        }
    }

    private static void doFind(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.find(args[1]);
            }
        }
    }

    private static void doStatus() {
        Gitlet gitlet = loadGitlet();
        if (gitlet == null) {
            System.out.println("Not in an initialized gitlet directory.");
        } else {
            gitlet.status();
        }
    }

    private static void doBranch(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.branch(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doRmBranch(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.rmBranch(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doMerge(String... args) {
        if (args.length < 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.merge(args[1]);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doCheckout(String... args) {
        if (args.length > 4) {
            System.out.println("Incorrect operands.");
        }
        if (args.length == 4 && !args[2].equals("--")) {
            System.out.println("Incorrect operands.");
        }
        if (args.length == 3 && !args[1].equals("--")) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.checkOut(args);
                saveGitlet(gitlet);
            }
        }
    }

    private static void doReset(String... args) {
        if (args.length < 2) {
            System.out.println("Incorrect operands.");
        } else {
            Gitlet gitlet = loadGitlet();
            if (gitlet == null) {
                System.out.println("Not in an initialized gitlet directory.");
            } else {
                gitlet.reset(args[1]);
                saveGitlet(gitlet);
            }
        }
    }
}
