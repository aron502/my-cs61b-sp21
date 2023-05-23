package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class Stage implements Serializable {
    private final HashMap<String, String> added;
    private final HashSet<String> removed;

    public void save() {
        writeObject(Repository.INDEX, this);
    }

    public static Stage readFromFile() {
        return readObject(Repository.INDEX, Stage.class);
    }

    public static void clear(Stage stage) {

    }

    public Stage() {
       added = new HashMap<>();
       removed = new HashSet<>();
    }

    public void addFile(String fileName, String id) {
        added.put(fileName, id);
        removed.remove(fileName);
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty();
    }

    public void removeFile(String fileName) {
        added.remove(fileName);
        removed.add(fileName);
    }

    public void remove(String fileName) {
        added.remove(fileName);
        removed.remove(fileName);
    }


    public String getAddedID(String fileName) {
        return added.getOrDefault(fileName, "");
    }

    public HashMap<String, String> getAdded() {
        return added;
    }

    public HashSet<String> getRemoved() {
        return removed;
    }
}
