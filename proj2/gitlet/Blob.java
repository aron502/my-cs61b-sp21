package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private final String id;
    private final byte[] content;
    private final File file;
    public Blob(File f) {
        if (f.exists()) {
            content = readContents(f);
            id = sha1(f.getName(), content);
        } else {
            content = null;
            id = sha1(f.getName());
        }
        file = getObjectFile(id);
    }

    public static Blob readFromFile(String id) {
        return readObject(getObjectFile(id), Blob.class);
    }

    public void save() {
        saveObject(file, this);
    }

    public boolean exists() {
        return content != null;
    }

    public String getId() {
        return id;
    }

    public byte[] getContent() {
        return content;
    }

}
