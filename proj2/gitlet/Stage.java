package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class Stage implements Serializable {
    private HashMap<String, String> added;

    public Stage() {
       added = new HashMap<>();
    }

    public void addFile(String fileName, String id) {
        added.put(fileName, id);
    }

    public void removeFile(String fileName) {
        added.remove(fileName);
    }

    public String getAddedID(String fileName) {
        return added.getOrDefault(fileName, "");
    }
}
