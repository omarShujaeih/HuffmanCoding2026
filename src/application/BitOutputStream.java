package application;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements Closeable {
    private final OutputStream out;
    private int currentByte = 0;
    private int numBitsFilled = 0;

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    public void writeBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) throw new IllegalArgumentException("bit must be 0 or 1");
        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;
        if (numBitsFilled == 8) flushCurrentByte();
    }

    public void writeInt(int x) throws IOException {
        out.write((x >>> 24) & 0xFF);
        out.write((x >>> 16) & 0xFF);
        out.write((x >>> 8) & 0xFF);
        out.write(x & 0xFF);
    }

    public void writeByte(int b) throws IOException {
        out.write(b & 0xFF);
    }

    private void flushCurrentByte() throws IOException {
        out.write(currentByte);
        currentByte = 0;
        numBitsFilled = 0;
    }

    public void flush() throws IOException {
        if (numBitsFilled > 0) {
            currentByte <<= (8 - numBitsFilled); 
            flushCurrentByte();
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
}
