package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String id;
    private String fileName;
    private byte[] content;
    public Blob(String name) {
        fileName = name;
        File file = join(Repository.CWD, name);
        content = readContents(file);
        id = sha1(content);
    }

    public void saveToFile() {
        writeObject(join(Repository.BLOBS_DIR, id), this);
    }
    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }
}
