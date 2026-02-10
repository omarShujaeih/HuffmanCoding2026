package application;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HuffmanCodec {

    public record CompressResult(long originalSize, long compressedSize,
                                 List<HuffmanData> table, String headerText) {}
    public record DecompressResult(long decodedSize) {}

   
    public static CompressResult compress(File input, File output) throws IOException {

        byte[] data = readAllBytes(input);
        int[] freq = buildFrequencies(data);
        HNode root = buildTree(freq);

        String[] codes = new String[256];
        if (root != null) buildCodes(root, "", codes);

        List<HuffmanData> table = new ArrayList<>();
        int symbolsCount = 0;
        for (int b = 0; b < 256; b++) {
            if (freq[b] > 0) {
                symbolsCount++;
                table.add(new HuffmanData(b, freq[b], codes[b]));
            }
        }

        try (FileOutputStream fos = new FileOutputStream(output)) {

            StringBuilder header = new StringBuilder();
            header.append("ORIGINAL_SIZE=").append(data.length).append('\n');
            header.append("SYMBOLS=").append(symbolsCount).append('\n');
            for (int b = 0; b < 256; b++) {
                if (freq[b] > 0) header.append(b).append(' ').append(freq[b]).append('\n');
            }
            header.append("END_HEADER\n");

            fos.write(header.toString().getBytes("UTF-8"));
            fos.flush();

            try (BitOutputStream bout = new BitOutputStream(fos)) {
                for (byte x : data) {
                    int b = x & 0xFF;
                    String code = codes[b];
                    for (int i = 0; i < code.length(); i++) {
                        bout.writeBit(code.charAt(i) == '1' ? 1 : 0);
                    }
                }
            }
        }

        StringBuilder hdr = new StringBuilder();
        hdr.append("ORIGINAL_SIZE=").append(data.length).append("\n");
        hdr.append("SYMBOLS=").append(symbolsCount).append("\n");
        hdr.append("Pairs (byte -> freq):\n");
        for (int b = 0; b < 256; b++) if (freq[b] > 0) hdr.append(b).append(" -> ").append(freq[b]).append("\n");

        return new CompressResult(data.length, output.length(), table, hdr.toString());
    }


    public static DecompressResult decompress(File compressed, File decodedOut) throws IOException {

        try (FileInputStream fis = new FileInputStream(compressed);
             FileOutputStream fos = new FileOutputStream(decodedOut)) {

            // 1) Read TEXT HEADER safely
            HeaderInfo info = readTextHeader(fis);

            int originalSize = info.originalSize;
            int[] freq = info.freq;

            // 2) Rebuild tree
            HNode root = buildTree(freq);
            if (root == null) return new DecompressResult(0);

            // Special case: only one symbol
            if (root.isLeaf()) {
                for (int i = 0; i < originalSize; i++) fos.write(root.value);
                return new DecompressResult(originalSize);
            }

            // 3) Decode bits
            long written = 0;
            HNode cur = root;

            try (BitInputStream bin = new BitInputStream(fis)) {
                while (written < originalSize) {
                    int bit = bin.readBit();
                    if (bit == -1) break;

                    cur = (bit == 0) ? cur.left : cur.right;
                    if (cur.isLeaf()) {
                        fos.write(cur.value);
                        written++;
                        cur = root;
                    }
                }
            }

            return new DecompressResult(written);
        }
    }

    private static class HeaderInfo {
        int originalSize;
        int[] freq = new int[256];
    }

    private static HeaderInfo readTextHeader(InputStream in) throws IOException {
        HeaderInfo info = new HeaderInfo();

        StringBuilder line = new StringBuilder();
        String prevLine = null;

        while (true) {
            int ch = in.read();
            if (ch == -1) throw new IOException("Unexpected EOF while reading header");

            if (ch == '\n') {
                String s = line.toString().trim();
                line.setLength(0);

                if (s.equals("END_HEADER")) break;
                if (s.isEmpty()) continue;

                if (s.startsWith("ORIGINAL_SIZE=")) {
                    info.originalSize = Integer.parseInt(s.split("=")[1]);
                } else if (s.startsWith("SYMBOLS=")) {
                    // مش ضروري نخزنها، بس ممكن تقرأها
                } else {
                    // "byte freq"
                    String[] parts = s.split("\\s+");
                    int symbol = Integer.parseInt(parts[0]);
                    int f = Integer.parseInt(parts[1]);
                    info.freq[symbol] = f;
                }

                prevLine = s;
            } else {
                line.append((char) ch);
            }
        }

        return info;
    }

    // -------- helpers --------

    private static byte[] readAllBytes(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = fis.read(buf)) != -1) bos.write(buf, 0, r);
            return bos.toByteArray();
        }
    }

    private static int[] buildFrequencies(byte[] data) {
        int[] freq = new int[256];
        for (byte b : data) freq[b & 0xFF]++;
        return freq;
    }

    private static HNode buildTree(int[] freq) {
        int count = 0;
        for (int f : freq) if (f > 0) count++;
        if (count == 0) return null;

        MinHeap<HNode> heap = new MinHeap<>(Math.max(1, count * 2 + 5));
        for (int b = 0; b < 256; b++) {
            if (freq[b] > 0) heap.insert(new HNode(b, freq[b]));
        }

        if (heap.size() == 1) return heap.deleteMin();

        while (heap.size() > 1) {
            HNode a = heap.deleteMin();
            HNode b = heap.deleteMin();
            heap.insert(new HNode(a, b));
        }
        return heap.deleteMin();
    }

    private static void buildCodes(HNode node, String path, String[] codes) {
        if (node.isLeaf()) {
            codes[node.value] = path.isEmpty() ? "0" : path;
            return;
        }
        buildCodes(node.left, path + "0", codes);
        buildCodes(node.right, path + "1", codes);
    }
}
