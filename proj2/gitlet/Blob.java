package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private final String id;
    private final String fileName;
    private final byte[] content;
    public Blob(String name) {
        fileName = name;
        File file = join(Repository.CWD, name);
        if (file.exists()) {
            content = readContents(file);
            id = sha1(fileName, content);
        } else {
            content = null;
            id = sha1(fileName);
        }
    }

    public byte[] getContent() {
        return content;
    }

    public void save() {
        writeObject(join(Repository.BLOBS_DIR, id), this);
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }
}
