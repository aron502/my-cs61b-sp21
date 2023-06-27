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

    public void clear() {
        added = new HashMap<>();
        removed = new HashSet<>();
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

    public Set<String> getRemoved() {
        return removed;
    }

}