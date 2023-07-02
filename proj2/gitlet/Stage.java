package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

public class Stage implements Serializable {
    private Map<String, String> added = new HashMap<>();
    private Set<String> removed = new HashSet<>();

    public void save() {
        writeObject(Repository.INDEX, this);
    }

    public Stage clear() {
        added = new HashMap<>();
        removed = new HashSet<>();
        return this;
    }

    public static Stage readFromFile() {
        return readObject(Repository.INDEX, Stage.class);
    }
    public void addFile(String filePath, String id) {
        added.put(filePath, id);
        removed.remove(filePath);
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty();
    }

    public Map<String, String> getAdded() {
        return added;
    }

    /** return fileId in staging area, if it doesn't exist, return empty string. */
    public String getAddedFileID(String fileName) {
        return added.getOrDefault(fileName, "");
    }

    public Set<String> getRemoved() {
        return removed;
    }

    /**
     * Delete file from staging area.
     */
    public void delete(String fileName) {
        added.remove(fileName);
        removed.remove(fileName);
    }

    public boolean contains(String fileName) {
        return added.containsKey(fileName) || removed.contains(fileName);
    }

}
