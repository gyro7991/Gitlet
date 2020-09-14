package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Commit implements Serializable {
    private Commit parent;
    private Date date;
    private Map<String, String> files;
    private String commitmsg;
    private String hashcode;

    public Commit(Commit parent, String commitmsg, Map<String, String> newfiles) {
        this.parent = parent;
        this.commitmsg = commitmsg;
        this.date = new Date();

        if (parent != null) {
            this.files = new HashMap<>(parent.getFiles());
        } else {
            this.files = new HashMap<>();
        }
        if (newfiles != null) {
            files.putAll(newfiles);
        }
        hashcode = Utils.sha1(this.toByteArray());
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public Commit getParent() {
        return parent;
    }

    public String getcommitmsg() {
        return commitmsg;
    }

    public String getHashcode() {
        return hashcode;
    }

    public boolean containsFile(String fileName) {
        return files.containsKey(fileName);
    }

    public void printlog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("===");
        System.out.println("Commit " + hashcode);
        System.out.println(sdf.format(date));
        System.out.println(commitmsg);
        System.out.println();
    }

    public String getFile(String fileName) {
        return files.get(fileName);
    }

    private byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] ret = null;

        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            ret = bos.toByteArray();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
