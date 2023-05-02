package hashmap;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private static final int DEFAULT_SIZE = 16;
    private static final double DEFAULT_LOAD = 0.75;
    private double maxLoad;
    private int bucketsSize;
    private int size = 0;
    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_SIZE, DEFAULT_LOAD);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        this.maxLoad = maxLoad;
        bucketsSize = initialSize;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        Arrays.fill(table, createBucket());
        return table;
    }

    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), bucketsSize);
    }

    public int size() {
        return size;
    }

    public void clear() {
        Arrays.fill(buckets, null);
        size = 0;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public V get(K key) {
        Optional<Node> result = getNode(key);
        return result.map(node -> node.value).orElse(null);
    }

    public void put(K key, V value) {
        Optional<Node> nodeOption = getNode(key);
        nodeOption.ifPresentOrElse(
                node -> node.value = value,
                () -> {
                    buckets[getIndex(key)].add(createNode(key, value));
                    size += 1;
                    if (checkMaxLoad()) {
                        resize(bucketsSize * 2);
                    }
                }
        );
    }

    public Set<K> keySet() {
        return Arrays.stream(buckets)
                     .flatMap(lst -> lst.stream().map(node -> node.key))
                     .collect(Collectors.toCollection(HashSet::new));
    }

    public V remove(K key) {
        Optional<Node> nodeOption = getNode(key);
        return nodeOption.map(node -> {
            V value = node.value;
            buckets[getIndex(key)].remove(node);
            size -= 1;
            return value;
        }).orElse(null);
    }

    public V remove(K key, V value) {
        Optional<Node> nodeOption = getNode(key);
        return nodeOption.filter(node -> node.key.equals(key) && node.value.equals(value))
                .map(node -> {
                    buckets[getIndex(key)].remove(node);
                    size -= 1;
                    return value;
                }).orElse(null);
    }

    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    private Optional<Node> getNode(K key) {
        int index = getIndex(key);
        Stream<Node> nodeStream = Optional.ofNullable(buckets[index])
                .map(Collection::stream)
                .orElse(Stream.empty());
        Optional<Node> result = nodeStream.filter(node -> node.key.equals(key))
                .findFirst();
        return result;
    }

    private boolean checkMaxLoad() {
        return ((size * 1.0) / bucketsSize) > maxLoad;
    }

    private void resize(int newSize) {
        Collection<Node>[] newBuckets = createTable(newSize);
        bucketsSize = buckets.length;
        for (K key : this) {
            newBuckets[getIndex(key)].add(getNode(key).get());
        }
        buckets = newBuckets;
    }
}
