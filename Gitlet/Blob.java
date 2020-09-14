package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Blob implements Serializable {
    private String name;
    private String hashcode;
    private byte[] content;

    public Blob(String name, byte[] content) {
        this.name = name;
        this.content = content;
        this.hashcode = Utils.sha1(this.toByteArray());
    }

    public String getHashcode() {
        return hashcode;
    }

    public byte[] getContent() {
        return content;
    }

    // convert object to byte array
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
