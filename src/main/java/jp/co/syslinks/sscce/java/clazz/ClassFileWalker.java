package jp.co.syslinks.sscce.java.clazz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4
 * https://qiita.com/niwasawa/items/a6c29cb323fb9d027cb2
 * https://qiita.com/uint256_t/items/58f62577dad71f05aee1
 */
public class ClassFileWalker {

    private enum Status {
        cafebabe, javaVersion, constantPool, constantPoolIn
    }

    public void read(File file, boolean hex) throws IOException {
        Status status = Status.cafebabe;
        int len = 16;
        byte[] buff = new byte[len];
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));) {
            int constant_pool_count = 0;
            int constant_pool_count_index = 1;
            while (in.available() > 0) {
                if (status == Status.cafebabe) {
                    int read = in.read(buff, 0, 4);
                    if (!(buff[0] == -54 && buff[1] == -2 && buff[2] == -70 && buff[3] == -66)) { // cafebabe
                        throw new RuntimeException("Not a .class file");
                    }
                    status = Status.javaVersion;
                }
                if (status == Status.javaVersion) {
                    int read = in.read(buff, 0, 4);
                    int minor_version = buff[0] + buff[1];
                    int major_version = buff[2] + buff[3];
                    System.out.println(minor_version + "," + major_version);
                    status = Status.constantPool;
                }
                if (status == Status.constantPool) {
                    int read = in.read(buff, 0, 2);
                    constant_pool_count = u2(buff[0], buff[1]);
                    System.out.println(constant_pool_count);
                    status = Status.constantPoolIn;
                }
                if (status == Status.constantPoolIn) {
                    int read = in.read(buff, 0, 1);
                    int constantType = buff[0];
                    if (constantType == 7) { // CONSTANT_Class
                        in.read(buff, 0, 2);
                        this.out(constantType, buff[0], buff[1], hex, constant_pool_count_index);
                    } else if (constantType == 1) { // CONSTANT_Utf8
                        in.read(buff, 0, 2);
                        int size = u2(buff[0], buff[1]);
                        if (size == 0) {
                            this.outStr(null, hex, constant_pool_count_index);
                        } else {
                            byte[] string = new byte[size];
                            in.read(string, 0, size);
                            this.outStr(string, hex, constant_pool_count_index);
                        }
                    } else if (constantType == 3) { // CONSTANT_Integer
                        in.read(buff, 0, 4);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], hex, constant_pool_count_index);
                    } else if (constantType == 4) { // CONSTANT_Float
                        in.read(buff, 0, 4);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], hex, constant_pool_count_index);
                    } else if (constantType == 5) { // CONSTANT_Long
                        in.read(buff, 0, 8);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], buff[4], buff[5], buff[6], buff[7], hex, constant_pool_count_index);
                        constant_pool_count_index++; // ★★★
                    } else if (constantType == 6) { // CONSTANT_Double
                        in.read(buff, 0, 8);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], buff[4], buff[5], buff[6], buff[7], hex, constant_pool_count_index);
                        constant_pool_count_index++; // ★★★
                    } else if (constantType == 8) { // CONSTANT_String
                        in.read(buff, 0, 2);
                        this.out(constantType, buff[0], buff[1], hex, constant_pool_count_index);
                    } else if (constantType == 9) { // CONSTANT_Fieldref
                        in.read(buff, 0, 4);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], hex, constant_pool_count_index);
                    } else if (constantType == 10) { // CONSTANT_Methodref
                        in.read(buff, 0, 4);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], hex, constant_pool_count_index);
                    } else if (constantType == 12) { // CONSTANT_NameAndType
                        in.read(buff, 0, 4);
                        this.out(constantType, buff[0], buff[1], buff[2], buff[3], hex, constant_pool_count_index);
                    } else {
                        throw new RuntimeException("Not Support Constant Type : " + constantType);
                    }
                    constant_pool_count_index++;
                    if (constant_pool_count_index == constant_pool_count) {
                        break;
                    }
                    // System.out.println(constant_pool_count_index);
                }
            }
        }
    }

    private void out(int constantType, byte b1, byte b2, boolean hex, int index) {
        this.out(constantType, b1, b2, (byte) 0, (byte) 0, hex, index);
    }

    private void out(int constantType, byte b1, byte b2, byte b3, byte b4, boolean hex, int index) {
        this.out(constantType, b1, b2, b3, b4, (byte) 0, (byte) 0, (byte) 0, (byte) 0, hex, index);
    }

    private void out(int constantType, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8, boolean hex, int index) {
        switch (constantType) {
        case 3:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X %02X %02X", b1, b2, b3, b4));
            } else {
                System.out.println(u4(b1, b2, b3, b4));
            }
            break;
        case 4:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X %02X %02X", b1, b2, b3, b4));
            } else {
                System.out.println(u4Float(b1, b2, b3, b4));
            }
            break;
        case 5:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X %02X %02X %02X %02X %02X %02X", b1, b2, b3, b4, b5, b6, b7, b8));
            } else {
                System.out.println(u8Long(b1, b2, b3, b4, b5, b6, b7, b8));
            }
            break;
        case 6:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X %02X %02X %02X %02X %02X %02X", b1, b2, b3, b4, b5, b6, b7, b8));
            } else {
                System.out.println(u8Double(b1, b2, b3, b4, b5, b6, b7, b8));
            }
            break;
        case 7:
        case 8:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X", b1, b2));
            } else {
                System.out.println("#" + u2(b1, b2));
            }
            break;
        case 9:
        case 10:
        case 12:
            System.out.print(String.format("% 5d | %02X : ", index, constantType));
            if (hex) {
                System.out.println(String.format("%02X %02X %02X %02X", b1, b2, b3, b4));
            } else {
                System.out.println("#" + u2(b1, b2) + "," + u2(b3, b4));
            }
            break;
        default:
            throw new RuntimeException("Not Support Constant Type : " + constantType);
        }
    }

    private void outStr(byte[] string, boolean hex, int index) {
        System.out.print(String.format("% 5d | %02X : ", index, 1)); // CONSTANT_Utf8
        if (string == null) {
            System.out.println();
        } else {
            if (hex) {
                for (byte bb : string) {
                    System.out.print(String.format("%02X ", bb));
                }
                System.out.println();
            } else {
                System.out.println(new String(string));
            }
        }
    }

    private int u1(byte b1) {
        return b1 < 0 ? (256 + b1) : b1;
    }

    private int u2(byte b1, byte b2) {
        return u1(b1) * 256 + u1(b2);
    }

    private int u4(byte b1, byte b2, byte b3, byte b4) {
        return u1(b1) * 256 * 256 * 256 + u1(b2) * 256 * 256 + u1(b3) * 256 + u1(b4);
    }

    private float u4Float(byte b1, byte b2, byte b3, byte b4) {
        return u1(b1) * 256 * 256 * 256 + u1(b2) * 256 * 256 + u1(b3) * 256 + u1(b4);
    }

    private long u8Long(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        int high_bytes = u4(b1, b2, b3, b4);
        int low_bytes = u4(b5, b6, b7, b8);
        return ((long) high_bytes << 32) + low_bytes;
    }

    private double u8Double(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        int high_bytes = u4(b1, b2, b3, b4);
        int low_bytes = u4(b5, b6, b7, b8);
        return ((long) high_bytes << 32) + low_bytes;
    }

    public static void main(String[] args) throws IOException {
        ClassFileWalker walker = new ClassFileWalker();
        walker.read(new File("target/classes/jp/co/syslinks/sscce/java/clazz/ClassFileSample.class"), false);
    }
}
