package jgob;

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

    public static void main(String[] args) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("/home/dbudworth/gob.bin"));
            System.out.println(HexDump.dump(bytes));
            ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
            while (bb.remaining() > 0) {
                int start = bb.position();
                int sz = bb.get();
                int type = decodeVarInt(bb);
                System.out.println("start: " + start + ", field size: " + sz + ", type: " + type);
                if (sz == 0)
                    throw new IllegalStateException("size can't be zero");
                switch (type) {
                    case 2:
                        bb.get();//skip a leading zero

                        System.out.println("int at(" + bb.position() + "): " + decodeVarInt(bb));
                        break;
                    case 3:
                        bb.get();//skip a leading zero
                        System.out.println("uint: " + decodeVarUInt(bb));
                        break;
                    default:
                        System.out.println("Unknown type: " + type);
                }
                bb.position(1 + start + sz);
            }
        } catch (Throwable th) {
            th.printStackTrace(System.out);
        }
    }

    private static int decodeVarInt(ByteBuffer bb) {
        int ret = decodeVarUInt(bb);
        if ((ret & 1) == 1) {
            return -(ret >> 1);
        } else {
            return ret >> 1;
        }
    }

    private static int decodeVarUInt(ByteBuffer bb) {
        int b1 = bb.get() & 0xff;
        if (b1 < 128) {
            return b1;
        }
        b1 = b1 & 0x7f;
        System.out.println("SIZE: " + b1 + " or: " + (-b1) + " - " + String.format("%x", b1));
        int ret = 0;
        for (int x = 0; x < b1; x++) {
            ret <<= 8;
            ret &= (bb.get() & 0xff);
        }
        System.out.println("big int, returning: " + ret);
        return ret;
    }
}


