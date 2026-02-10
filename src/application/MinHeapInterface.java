package application;

public interface MinHeapInterface<T extends Comparable<T>> {
    void insert(T item);
    T deleteMin();
    int size();
    boolean isEmpty();
}
