package jgob;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by: dbudworth @ 2/4/14 4:16 AM
 */
public class GobDecode {
    /*
        public static final byte[] DATA = {
                0x03, 0x04, 0x00, 0x18, 0x03, 0x04, 0x00, 0x1A, 0x05, 0x04, 0x00, 0xFE, 0x02, 0x00
        };
    */
    public static final int BOOL = 1;
    public static final int INT = 2;
    public static final int UINT = 3;
    public static final int FLOAT = 4;
    public static final int BYTE_ARR = 5;
    public static final int STRING = 6;
    public static final int COMPLEX = 7;
    public static final int INTERFACe = 8;
    // gap for reserved ids.
    public static final int WIRETYPE = 16;
    public static final int ARRAYTYPE = 17;
    public static final int COMMONTYPE = 18;
    public static final int SLICETYPE = 19;
    public static final int STRUCTTYPE = 20;
    public static final int FIELDTYPE = 21;
    // 22 is slice of fieldtype.
    public static final int MAPTYPE = 23;

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("/home/dbudworth/gob.bin"));
        try {
            System.out.println(HexDump.dump(bytes));
            ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
            int count = 1;
            byte[] tmp = new byte[1024];
            while (bb.remaining() > 0) {
                int start = bb.position();
                int sz = (int) decodeUInt(bb);
                int type = (int) decodeInt(bb);
                bb.get(); // skip leading zero??
                System.out.println("Record: " + (count++) + ", start: " + String.format("0x%04X", start) + ", field size: " + sz + ", type: " + type + ", int offset: " + start);
                if (sz < 1)
                    throw new IllegalStateException("size can't be: " + sz);
                switch (type) {
                    case BOOL:
                        System.out.println(" bool: " + (bb.get() == 1));
                        break;
                    case INT:
                        System.out.println("  int: " + decodeInt(bb));
                        break;
                    case UINT:
                        System.out.println(" uint: " + decodeUInt(bb));
                        break;
                    case STRING:
                        int ssz = (int) decodeUInt(bb);
                        bb.get(tmp, 0, ssz);
                        System.out.println(String.format("(%3d): %s", ssz, new String(tmp, 0, ssz)));
                        break;
                    default:
                        System.out.println(String.format("Unknown type: %d (%x)", type, type));
                        break;
                }
                bb.position(1 + start + sz);
            }
        }
        catch (Throwable th) {
            th.printStackTrace(System.out);
        }
        System.out.println(HexDump.dump(bytes));

    }

    private static long decodeInt(ByteBuffer bb) {
        long raw = decodeUInt(bb);
        long temp = (((raw << 63) >> 63) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1L << 63));
    }

    private static long decodeUInt(ByteBuffer bb) {
        int b1 = bb.get() & 0xff;
        if (b1 <= 0x7f) {
            return b1;
        }
        int sz = -((byte) b1);
        if (sz > 8 || sz < 1)
            throw new IllegalArgumentException(String.format("Illegal size: %d (%x) at location: %d (%04x)", sz, sz, bb.position(), bb.position()));
        long ret = 0;
        while (sz-- != 0)
            ret = (ret << 8) | bb.get() & 0xFF;
        return ret;
    }
}


