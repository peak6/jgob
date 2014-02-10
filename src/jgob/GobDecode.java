package jgob;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    public static final Map<Integer, GType> types = new HashMap<>();
    public static final int MinTypeDef = 64;

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("/home/dbudworth/gob.bin"));
        int start = 0;
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        try {
            System.out.println(HexDump.dump(bytes));
            int count = 1;
            while (bb.remaining() > 0) {
                start = bb.position();
                int sz = (int) decodeUInt(bb);
                int type = (int) decodeLong(bb);
                bb.get(); // skip leading zero??
                System.out.println("Record: " + (count++) + ", start: " + String.format("0x%04X", start) + ", field size: " + sz + ", type: " + type + ", int offset: " + start);
                if (sz < 1)
                    throw new IllegalStateException("size can't be: " + sz);
                if (type < 0) {
                    defType(-type, bb);
                } else {
                    switch (type) {
                        case BOOL:
                            System.out.println(" bool: " + (bb.get() == 1));
                            break;
                        case INT:
                            System.out.println("  int: " + decodeLong(bb));
                            break;
                        case UINT:
                            System.out.println(" uint: " + decodeUInt(bb));
                            break;
                        case STRING:
                            String s = decodeString(bb);
                            System.out.println(String.format("(%3d): %s", s.length(), s));
                            break;
                        case FLOAT:
                            System.out.println(String.format("float: %f", decodeDouble(bb)));
                            break;
                        case BYTE_ARR:
                            System.out.println("DECODED BYTES: " + HexDump.dump(decodeBytes(bb)));
                            break;
                        case SLICETYPE:
                            System.out.println(Arrays.toString(decodeSlice(bb)));
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Unknown type: %d (%x)", type, type));
                    }
                }
                int newPosition = 1 + start + sz;
                if (bb.position() != newPosition)
                    System.out.println("newpos: " + newPosition + ", bb:" + bb);
                bb.position(newPosition);
            }
        } catch (Throwable th) {
            th.printStackTrace(System.out);
            System.out.println("Current buffer:" + bb + "\n" + HexDump.dump(bytes, start));
        }
    }

    private static void defType(int type, ByteBuffer bb) {
        if (type < MinTypeDef)
            throw new IllegalArgumentException("Can't define type: " + type + " as it's less than: " + MinTypeDef);
        if (types.containsKey(type))
            throw new IllegalArgumentException("Can't redefine type: " + type + ", current val: " + types.get(type));

        System.out.println("Defining type: " + type);
        WireType wt = new WireType(bb);
//        long realType = decodeLong(bb);
        System.out.println("real type: " + wt);
    }

    private static class WireType {
        ArrayType arrayType;
        SliceType sliceType;
        StructType structType;
        //        MapType mapType;


        public WireType(ByteBuffer bb) {
            int delta = (int) decodeUInt(bb);
            System.out.println("WT:STARTING DELTA: " + delta + " / " + bb);
            int idx = 0;
            while (delta != 0) {
                idx += delta;
                switch (idx) {
                    case 0:
                        arrayType = new ArrayType(bb);
                        System.out.println("AT: " + arrayType);
                        break;
                    case 1:
                        sliceType = new SliceType(bb);
                        System.out.println("ST: " + sliceType);
                        break;
                    case 2:
                        structType = new StructType(bb);
                        System.out.println("STRCT:" + structType);
                        break;
                    case 3:
//                        arrayType = new ArrayType(bb);
//                        System.out.println("AT: "+arrayType);
                        break;
                    default:
                        throw new IllegalArgumentException("WTF: delta: " + delta + ", idx: " + idx);

                }
                delta = (int) decodeUInt(bb);
                System.out.println("WT:next delta: " + delta + " new idx: " + (delta + idx));
            }
        }

        @Override
        public String toString() {
            return "WireType{" +
                    "arrayType=" + arrayType +
                    ", sliceType=" + sliceType +
                    '}';
        }
    }

    public static class SliceType {
        String name;
        int id;
        int elemType;

        public SliceType(ByteBuffer bb) {
            int delta = (int) decodeUInt(bb);
            System.out.println("ST:STARTING DELTA: " + delta + " / " + bb);
            int idx = 0;
            while (delta != 0) {
                idx += delta;
                switch (idx) {
                    case 0:
                        name = decodeString(bb);
                        break;
                    case 1:
                        id = decodeInt(bb);
                        break;
                    case 2:
                        elemType = decodeInt(bb);
                        break;
                    default:
                        throw new IllegalArgumentException("WTF: delta: " + delta + ", idx: " + idx);
                }
                delta = (int) decodeUInt(bb);
                System.out.println("ST:next delta: " + delta + " new idx: " + (delta + idx));
            }
        }

        @Override
        public String toString() {
            return "SliceType{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    ", elemType=" + elemType +
                    '}';
        }
    }

    public static class ArrayType {
        String name;
        int id;
        int elemType;
        int len;

        @Override
        public String toString() {
            return "ArrayType{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    ", elemType=" + elemType +
                    ", len=" + len +
                    '}';
        }

        public ArrayType(ByteBuffer bb) {
            int delta = (int) decodeUInt(bb);
            System.out.println("AT:STARTING DELTA: " + delta + " / " + bb);
            int idx = 0;
            while (delta != 0) {
                idx += delta;
                switch (idx) {
                    case 0:
                        name = decodeString(bb);
                        break;
                    case 1:
                        id = decodeInt(bb);
                        break;
                    case 2:
                        elemType = decodeInt(bb);
                        break;
                    case 3:
                        len = decodeInt(bb);
                        break;
                    default:
                        throw new IllegalArgumentException("WTF: delta: " + delta + ", idx: " + idx);
                }
                delta = (int) decodeUInt(bb);
                System.out.println("AT:next delta: " + delta + " new idx: " + (delta + idx));
            }
        }
    }

    public static class StructType {
        String name;
        int id;
        List<FieldType> fields = new ArrayList<>();

        public StructType(ByteBuffer bb) {
            int delta = (int) decodeUInt(bb);
            System.out.println("STRT:STARTING DELTA: " + delta + " / " + bb);
            int idx = 0;
            while (delta != 0) {
                idx += delta;
                switch (idx) {
                    case 0:
                        name = decodeString(bb);
                        break;
                    case 1:
                        id = decodeInt(bb);
                        break;
                    case 2:
                        System.out.println("elems? " + decodeUInt(bb));
                        break;
                    default:
                        throw new IllegalArgumentException("WTF: delta: " + delta + ", idx: " + idx);
                }
                delta = (int) decodeUInt(bb);
                System.out.println("STRT:next delta: " + delta + " new idx: " + (delta + idx));
            }

        }
    }

    private static class FieldType {
        private FieldType(ByteBuffer bb) {
            throw new IllegalStateException("Field type not done");
        }
    }


    private static Object[] decodeSlice(ByteBuffer bb) {
        long count = decodeUInt(bb);
        System.out.println("count: " + count);
        return null;
    }

    private static byte[] decodeBytes(ByteBuffer bb) {
        int ssz = (int) decodeUInt(bb);
        byte[] tmp = new byte[ssz];
        bb.get(tmp, 0, ssz);
        return tmp;
    }

    private static Double decodeDouble(ByteBuffer bb) {
        long l = decodeUInt(bb);
        l = Long.reverseBytes(l);
//        l = Long.reverse(l);
        return Double.longBitsToDouble(l);
    }

    private static String decodeString(ByteBuffer bb) {
        int ssz = (int) decodeUInt(bb);
        System.out.println("ssz:" + ssz);
        byte[] tmp = new byte[ssz];
        bb.get(tmp, 0, ssz);
        return new String(tmp);
    }

    private static int decodeInt(ByteBuffer bb) {
        long l = decodeLong(bb);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE)
            return (int) l;
        throw new IllegalArgumentException("Cannot convert long(" + l + ") to integer");
    }

    private static long decodeLong(ByteBuffer bb) {
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

    public static final class GType {
        String[] fields;

    }
}


