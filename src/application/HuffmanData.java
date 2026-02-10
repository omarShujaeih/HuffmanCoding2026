package application;

public class HuffmanData {
    private final int byteUnsigned;
    private final int frequency;
    private final String code;

    public HuffmanData(int byteUnsigned, int frequency, String code) {
        this.byteUnsigned = byteUnsigned;
        this.frequency = frequency;
        this.code = code;
    }

    public int getByteUnsigned() { return byteUnsigned; }
    public int getFrequency() { return frequency; }
    public String getCode() { return code; }

    public String getPrintableChar() {
        int b = byteUnsigned;
        if (b >= 32 && b <= 126) return "'" + (char) b + "'";
        if (b == 10) return "\\n";
        if (b == 13) return "\\r";
        if (b == 9)  return "\\t";
        return "-";
    }
}
