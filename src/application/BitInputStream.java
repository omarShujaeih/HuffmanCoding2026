package application;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements Closeable {
    private final InputStream in;
    private int currentByte = 0;
    private int numBitsRemaining = 0;

    public BitInputStream(InputStream in) {
        this.in = in;
    }

    public int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) return -1;
            numBitsRemaining = 8;
        }
        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

    public int readInt() throws IOException {
        int b1 = in.read(), b2 = in.read(), b3 = in.read(), b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) throw new IOException("Unexpected EOF while reading int");
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public int readByte() throws IOException {
        int b = in.read();
        if (b < 0) throw new IOException("Unexpected EOF while reading byte");
        return b;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
