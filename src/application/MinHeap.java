package application;

@SuppressWarnings("unchecked")
public class MinHeap<T extends Comparable<T>> implements MinHeapInterface<T> {

    private T[] heap; 
    private int n;

    public MinHeap(int capacity) {
        heap = (T[]) new Comparable[capacity + 1];
        n = 0;
    }

    @Override
    public void insert(T item) {
        if (n + 1 == heap.length) grow();
        heap[++n] = item;
        swim(n);
    }

    @Override
    public T deleteMin() {
        if (n == 0) return null;
        T min = heap[1];
        heap[1] = heap[n];
        heap[n] = null;
        n--;
        sink(1);
        return min;
    }

    @Override
    public int size() { return n; }

    @Override
    public boolean isEmpty() { return n == 0; }

    private void swim(int k) {
        while (k > 1 && less(k, k / 2)) {
            swap(k, k / 2);
            k /= 2;
        }
    }

    private void sink(int k) {
        while (2 * k <= n) {
            int j = 2 * k;
            if (j < n && less(j + 1, j)) j++;
            if (!less(j, k)) break;
            swap(k, j);
            k = j;
        }
    }

    private boolean less(int i, int j) {
        return heap[i].compareTo(heap[j]) < 0;
    }

    private void swap(int i, int j) {
        T tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void grow() {
        T[] bigger = (T[]) new Comparable[heap.length * 2];
        System.arraycopy(heap, 0, bigger, 0, heap.length);
        heap = bigger;
    }
}
