package application;

public class HNode implements Comparable<HNode> {
    public int freq;
    public int value;   // 0..255 leaf, -1 internal
    public HNode left, right;

    public HNode(int value, int freq) {
        this.value = value;
        this.freq = freq;
    }

    public HNode(HNode a, HNode b) {
        this.value = -1;
        this.freq = a.freq + b.freq;
        this.left = a;
        this.right = b;
    }

    public boolean isLeaf() { return left == null && right == null; }

    @Override
    public int compareTo(HNode o) {
        return Integer.compare(this.freq, o.freq);
    }
}
