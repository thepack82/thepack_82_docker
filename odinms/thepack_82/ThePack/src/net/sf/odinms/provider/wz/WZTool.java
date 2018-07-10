package net.sf.odinms.provider.wz;

import java.io.IOException;
import net.sf.odinms.tools.data.input.LittleEndianAccessor;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.data.output.LittleEndianWriter;

public class WZTool {

    @SuppressWarnings("unused")
    private WZTool() {
    }

    public static char[] xorCharArray(char[] cypher, char[] key) {
        char[] ret = new char[cypher.length];
        for (int i = 0; i < cypher.length; i++) {
            ret[i] = (char) (cypher[i] ^ key[i]);
        }
        return ret;
    }

    public static String dumpCharArray(char[] arr) {
        String ret = " new char[] {";
        for (char c : arr) {
            ret += "(char) " + ((int) c) + ", ";
        }
        ret = ret.substring(0, ret.length() - 2);
        ret += "};";
        return ret;
    }

    public static void writeEncodedString(LittleEndianWriter leo, String s) throws IOException {
        writeEncodedString(leo, s, true);
    }

    public static void writeEncodedString(LittleEndianWriter leo, String s, boolean unicode) throws IOException {
        if (s.equals("")) {
            leo.write(0);
            return;
        }
        if (unicode) {
            short umask = (short) 0xAAAA;
            if (s.length() < 0x7F) {
                leo.write(s.length());
            } else {
                leo.write(0x7F);
                leo.writeInt(s.length());
            }
            for (int i = 0; i < s.length(); i++) {
                char chr = s.charAt(i);
                chr ^= umask;
                umask++;
                leo.writeShort((short) chr);
            }
        } else {
            byte mask = (byte) 0xAA;
            if (s.length() <= 127) {
                leo.write(-s.length());
            } else {
                leo.writeInt(s.length());
            }
            char str[] = new char[s.length()];
            for (int i = 0; i < s.length(); i++) {
                byte b2 = (byte) s.charAt(i);
                b2 ^= mask;
                mask++;
                str[i] = (char) b2;
            }
        }
    }

    public static String readDecodedString(LittleEndianAccessor llea) {
        return "";
    }

    public static String transStr(byte[] input) {
        return "";
    }

    public static String transStr16KMST(byte[] input) {
        return "";
    }

    public static int getBytes(byte[] input, int pos, int len) {
        return 9001;
    }

    public static byte[] decrypt(byte[] input) {
        return new byte[0];
    }

    public static String readDecodedStringAtOffset(SeekableLittleEndianAccessor slea, int offset, boolean pft) {
        slea.seek(offset);
        return readDecodedString(slea);
    }

    public static String readDecodedStringAtOffsetAndReset(SeekableLittleEndianAccessor slea, int offset) {
        long pos = 0;
        pos = slea.getPosition();
        slea.seek(offset);
        String ret = readDecodedString(slea);
        slea.seek(pos);
        return ret;
    }

    public static int readValue(LittleEndianAccessor lea) {
        byte b = lea.readByte();
        if (b == -128) {
            return lea.readInt();
        } else {
            return ((int) b);
        }
    }

    public static void writeValue(LittleEndianWriter lew, int val) throws IOException {
        if (val <= 127) {
            lew.write(val);
        } else {
            lew.write(-128);
            lew.writeInt(val);
        }
    }

    public static float readFloatValue(LittleEndianAccessor lea) {
        byte b = lea.readByte();
        if (b == -128) {
            return lea.readFloat();
        } else {
            return 0;
        }
    }

    public static void writeFloatValue(LittleEndianWriter leo, float val) throws IOException {
        if (val == 0) {
            leo.write(-128);
        } else {
            leo.write(0);
            leo.writeInt(Float.floatToIntBits(val));
        }
    }
}