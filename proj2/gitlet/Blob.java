package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String id;
    private String fileName;
    public Blob(String name) {
        fileName = name;
        File file = join(Repository.CWD, name);
        id = sha1(readContents(file));
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
