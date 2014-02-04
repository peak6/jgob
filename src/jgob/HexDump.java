package jgob;

/**
 * Created by: dbudworth @ 2/4/14 4:18 AM
 */
public class HexDump {
    public static final String dump(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        StringBuilder ascii = new StringBuilder();
        int offset = 0;
        while (offset < bytes.length){
            sb.append(String.format("%04X ", offset));
            ascii.setLength(0);
            offset = dump8(offset,bytes,sb,ascii);
            sb.append(' ');
            ascii.append(' ');
            offset = dump8(offset,bytes,sb,ascii);
            sb.append(" ").append(ascii).append('\n');
        }
        return sb.toString();
    }

    private static int dump8(int offset, byte[] bytes, StringBuilder bBuff, StringBuilder aBuff) {
        for (int idx = 0; idx < 8; idx++) {
            if (offset < bytes.length) {
                int b = bytes[offset++];
                bBuff.append(String.format("%02X", b&0xFF));
                if (Character.isAlphabetic(b))
                    aBuff.append((char) b);
                else
                    aBuff.append('.');
            } else {
                bBuff.append("  ");
            }
            bBuff.append(" ");
        }
        return offset;
    }
}
