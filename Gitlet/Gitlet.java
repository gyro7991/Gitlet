package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Gitlet implements Serializable {
    private HashSet<Commit> allCommits;
    private List<String> rm;
    private Map<String, Commit> branches;
    private Map<String, String> stage;
    private String currentbranch;

    public Gitlet() {
        allCommits = new HashSet<>();
        rm = new ArrayList<>();
        branches = new HashMap<>();
        stage = new HashMap<>();
        currentbranch = "master";
        Commit commit = new Commit(null, "initial commit", null);
        branches.put(currentbranch, commit);
        allCommits.add(commit);
    }

    public void commit(String message) {
        if (stage.isEmpty() && rm.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit commit = new Commit(branches.get(currentbranch), message, stage);
        branches.put(currentbranch, commit);
        allCommits.add(commit);
        stage.clear();
        rm.clear();
    }

    public void add(String filename) {
        Commit currentcommit = branches.get(currentbranch);
        if (rm.contains(filename)) {
            rm.remove(filename);
            return;
        }
        Path src = new File(filename).toPath(); // convert to path for readAllBytes parameter
        try {
            Blob blob = new Blob(filename, Files.readAllBytes(src)); // readAllBytes: convert the content of the file to byte array
            String hashcode = blob.getHashcode();
            File des = new File(".gitlet/" + hashcode);
            if (currentcommit.getFiles().containsKey(filename) && currentcommit.getFiles().get(filename).equals(hashcode)) {
                return;
            }
            Utils.writeObject(des, blob);
            stage.put(filename, des.getName());
        } catch (IOException e) {
            System.out.println("File does not exist.");
        }
    }

    public void rm(String filename) {
        boolean isCommitted = branches.get(currentbranch).getFiles().containsKey(filename);
        if (!stage.containsKey(filename) && !isCommitted) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (isCommitted) {
            File f = new File(filename);
            f.delete();
            rm.add(filename);
        }
        stage.remove(filename);
    }

    public void log() {
        Commit commit = branches.get(currentbranch);
        while (commit != null) {
            commit.printlog();
            commit = commit.getParent();
        }
    }

    public void globallog() {
        Iterator<Commit> i = allCommits.iterator();
        while (i.hasNext()) {
            i.next().printlog();
        }
    }

    public void find(String commitmsg) {
        int printed = 0;
        Iterator<Commit> i = allCommits.iterator();
        while (i.hasNext()) {
            Commit commit = i.next();
            if (commit.getcommitmsg().equals(commitmsg)) {
                System.out.println(commit.getHashcode());
                printed += 1;
            }
        }
        if (printed == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        List sortedKeys = new ArrayList(branches.keySet());
        if (!branches.isEmpty()) {
            Collections.sort(sortedKeys);
            for (Object keys : sortedKeys) {
                if (keys.equals(currentbranch)) {
                    System.out.println("*" + keys);
                } else {
                    System.out.println(keys);
                }
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        if (!stage.isEmpty()) {
            List sortedFiles = new ArrayList(stage.keySet());
            Collections.sort(sortedFiles);
            for (Object filenames : sortedFiles) {
                System.out.println(filenames);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (rm != null) {
            Collections.sort(rm);
            for (Object filenames : rm) {
                System.out.println(filenames);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        // optional
        System.out.println();
        System.out.println("=== Untracked Files ===");
        // optional
    }

    public void branch(String branchname) {
        if ((branches.containsKey(branchname))) {
            System.out.println("A branch with that name already exists.");
        } else {
            branches.put(branchname, branches.get(currentbranch));
        }
    }

    public void rmBranch(String branchname) {
        if (!branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
        }
        if (branchname.equals(currentbranch)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchname);
        }
    }

    public void checkOut(String... args) {
        if (args.length == 3) {
            if (!branches.get(currentbranch).getFiles().containsKey(args[2])) {
                System.out.println("File does not exist in that commit.");
            } else {
                checkoutFile(args[2]);
            }
        } else if (args.length == 4) {
            Iterator<Commit> i = allCommits.iterator();
            Commit commit = null;
            while (i.hasNext()) {
                Commit c = i.next();
                if (c.getHashcode().equals(args[1])) {
                    commit = c;
                    break;
                }
            }
            if (commit == null) {
                System.out.println("No commit with that id exists.");
            } else if (!commit.getFiles().containsKey(args[3])) {
                System.out.println("File does not exist in that commit.");
            } else {
                checkoutFileByCommit(commit, args[3]);
            }
        } else if (args.length == 2) {
            if (!branches.containsKey(args[1])) {
                System.out.println("No such branch exists.");
            } else if (currentbranch.equals(args[1])) {
                System.out.println("No need to checkout the current branch.");
            } else if (untrackedFilesInTheWay(branches.get(args[1]))) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
            } else {
                checkoutBranch(args[1]);
            }
        }
    }

    public void merge(String givenbranch) {
        if (!stage.isEmpty()) { // To do: check rm here as well.
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!branches.containsKey(givenbranch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (givenbranch.equals(currentbranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!getUntrackedFiles().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it or add it first.");
            return;
        }
        // To do:
        // If merge would generate an error because the commit that it would create would have no changes in it, just
        // let the normal commit error message for this go through.

        //Find split point
        ArrayList<Commit> currentCommits = getAllCommitsInBranch(currentbranch);
        ArrayList<Commit> givenCommits = getAllCommitsInBranch(givenbranch);
        Iterator<Commit> ci = currentCommits.iterator();
        Iterator<Commit> gi = givenCommits.iterator();
        Commit split = null;
        Commit cc;
        Commit gc;
        while (ci.hasNext() && gi.hasNext()) {
            cc = ci.next();
            gc = gi.next();
            if (cc != gc) {
                break;
            } else {
                split = cc;
            }
        }

        Commit currentHead = branches.get(currentbranch);
        Commit givenHead = branches.get(givenbranch);
        if (split == givenHead) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (split == currentHead) {
            branches.put(currentbranch, givenHead);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        boolean conflict = false;
        Map<String, String> modifiedFilesInBranch = getModifiedFiles(split, givenHead);
        Iterator<String> i = modifiedFilesInBranch.keySet().iterator();
        while (i.hasNext()) {
            String modifiedFile = i.next();
            String splitFile = split.getFile(modifiedFile);
            String currentFile = currentHead.getFile(modifiedFile);
            if ((splitFile == null && currentFile == null) ||
                    (splitFile != null && splitFile.equals(currentFile))) {
                stage.put(modifiedFile, modifiedFilesInBranch.get(modifiedFile));
                checkoutFileByCommit(givenHead, modifiedFile);
            } else {
                conflict = true;
                handleConflictFile(modifiedFile, currentFile, givenHead.getFile(modifiedFile));
            }
        }

        Map<String, String> removedFilesInBranch = getRemovedFiles(split, givenHead);
        i = removedFilesInBranch.keySet().iterator();
        while (i.hasNext()) {
            String removedFile = i.next();
            String currentFile = currentHead.getFile(removedFile);
            if (split.getFile(removedFile).equals(currentFile)) {
                // handle removed file here
            } else {
                conflict = true;
                handleConflictFile(removedFile, currentFile, null);
            }
        }

        if (!conflict) {
            commit("Merged " + currentbranch + " with " + givenbranch);
        } else {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public void reset(String commitId) {
        Iterator<Commit> i = allCommits.iterator();
        Commit commit = null;
        while (i.hasNext()) {
            Commit c = i.next();
            if (c.getHashcode().equals(commitId)) {
                commit = c;
                break;
            }
        }
        if (commit == null) {
            System.out.println("No commit with that id exists.");
        } else if (untrackedFilesInTheWay(commit)) {
            System.out.println("There is an untracked file in the way; delete it or add it first.");
        } else {
            branches.put(currentbranch, commit);
            checkoutBranch(currentbranch);
        }
    }

    private boolean untrackedFilesInTheWay(Commit c) {
        boolean b = false;
        List<File> untrackedFiles = getUntrackedFiles();
        for (File f : untrackedFiles) {
            if (c.containsFile(f.getName())) {
                b = true;
                break;
            }
        }
        return b;
    }

    private Map<String, String> getModifiedFiles(Commit oldCommit, Commit newCommit) {
        Map<String, String> ret = new HashMap<String, String>(newCommit.getFiles());
        Map<String, String> oldFiles = oldCommit.getFiles();
        Iterator<String> oldNames = oldFiles.keySet().iterator();
        while (oldNames.hasNext()) {
            String fileName = oldNames.next();
            if (oldFiles.get(fileName).equals(ret.get(fileName))) { // no change
                ret.remove(fileName);
            }
        }
        return ret;
    }

    private Map<String, String> getRemovedFiles(Commit oldCommit, Commit newCommit) {
        Map<String, String> ret = new HashMap<>(oldCommit.getFiles());
        Iterator<String> i = newCommit.getFiles().keySet().iterator();
        while (i.hasNext()) {
            ret.remove(i.next());
        }
        return ret;
    }

    private void handleConflictFile(String fileName, String current, String given) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(new String("<<<<<<< HEAD\n").getBytes());
            File blobFile = new File(".gitlet/" + current);
            Blob b = (Blob) Utils.readObject(blobFile);
            baos.write(b.getContent());
            baos.write(new String("=======\n").getBytes());
            blobFile = new File(".gitlet/" + given);
            b = (Blob) Utils.readObject(blobFile);
            baos.write(b.getContent());
            baos.write(new String(">>>>>>>").getBytes());
            File f = new File(fileName);
            Utils.writeContents(f, baos.toByteArray());
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> getUntrackedFiles() {
        ArrayList<File> untrackedFiles = new ArrayList<File>();
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("."));
            Commit head = branches.get(currentbranch);
            for (Path path : directoryStream) {
                File f = path.toFile();
                if (f.isDirectory() || f.isHidden()) {
                    continue;
                }
                if (!head.containsFile(f.getName()) && !stage.containsKey(f.getName())) {
                    untrackedFiles.add(f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return untrackedFiles;
    }

    private ArrayList<Commit> getAllCommitsInBranch(String branch) {
        ArrayList<Commit> ret = new ArrayList<Commit>();
        Commit c = branches.get(branch);
        while (c != null) {
            ret.add(0, c);
            c = c.getParent();
        }
        return ret;
    }

    private void checkoutFile(String filename) {
        checkoutFileByCommit(branches.get(currentbranch), filename);
    }

    private void checkoutFileByCommit(Commit commit, String filename) {
        String name = commit.getFiles().get(filename);
        if (name != null) {
            File blobFile = new File(".gitlet/" + name);
            Blob b = (Blob) Utils.readObject(blobFile);
            File f = new File(filename);
            Utils.writeContents(f, b.getContent());
        }
    }

    private void checkoutBranch(String branchname) {
        currentbranch = branchname;
        Iterator<File> untrackedFiles = getUntrackedFiles().iterator();
        while (untrackedFiles.hasNext()) {
            File f = untrackedFiles.next();
            f.delete();
        }

        Iterator<String> files = branches.get(currentbranch).getFiles().keySet().iterator();
        while (files.hasNext()) {
            checkoutFile(files.next());
        }
        stage.clear();
    }
}

