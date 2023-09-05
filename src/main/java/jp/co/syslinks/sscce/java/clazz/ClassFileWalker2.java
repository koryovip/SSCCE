package jp.co.syslinks.sscce.java.clazz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class ClassFileWalker2 {

    public static void main(String[] args) throws Exception {
        new ClassFileWalker2().read(new File("bin/java8/Clazz2.class"));
    }

    public void read(File file) throws Exception {
        int len = 16;
        byte[] buff = new byte[len];
        HashMap<Integer, Object> constantPool = new HashMap<>();
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));) {
            {
                /*int read =*/ in.read(buff, 0, 4); // cafebabe
            }
            {
                /*int read =*/ in.read(buff, 0, 4); // minorVersion,majorVersion
                int minor_version = buff[0] + buff[1];
                int major_version = buff[2] + buff[3];
                System.out.println(minor_version + "," + major_version);
            }
            // ConstantPool
            {
                /*int read =*/ in.read(buff, 0, 2); // constantPoolCount
                int constantPoolCount = u2(buff[0], buff[1]);
                System.out.println(constantPoolCount);
                for (int ii = 1; ii < constantPoolCount; ii++) {
                    /*int read =*/ in.read(buff, 0, 1);
                    int constantType = buff[0];
                    System.out.print(String.format("%05d %2d ", ii, constantType));
                    if (constantType == 7) { // CONSTANT_Class
                        in.read(buff, 0, 2);
                        System.out.print(u2(buff[0], buff[1]));
                    } else if (constantType == 8) { // CONSTANT_String
                        in.read(buff, 0, 2);
                    } else if (constantType == 1) { // CONSTANT_Utf8
                        in.read(buff, 0, 2);
                        int size = u2(buff[0], buff[1]);
                        if (size == 0) {
                            constantPool.put(ii, "");
                        } else {
                            byte[] string = new byte[size];
                            in.read(string, 0, size);
                            String value = new String(string, "UTF-8");
                            constantPool.put(ii, value);
                            System.out.print(String.format("%s", value));
                        }
                    } else if (constantType == 3) { // CONSTANT_Integer
                        in.read(buff, 0, 4);
                        System.out.print(String.format("%02X %02X %02X %02X", buff[0], buff[1], buff[2], buff[3]));
                    } else if (constantType == 4) { // CONSTANT_Float
                        in.read(buff, 0, 4);
                    } else if (constantType == 5) { // CONSTANT_Long
                        in.read(buff, 0, 8);
                    } else if (constantType == 6) { // CONSTANT_Double
                        in.read(buff, 0, 8);
                    } else if (constantType == 9) { // CONSTANT_Fieldref
                        in.read(buff, 0, 4);
                    } else if (constantType == 10) { // CONSTANT_Methodref
                        in.read(buff, 0, 4);
                    } else if (constantType == 12) { // CONSTANT_NameAndType
                        in.read(buff, 0, 4);
                    } else {
                        throw new RuntimeException("Not Support Constant Type : " + constantType);
                    }
                    System.out.println();
                }
            }
            System.out.println("===== Interfaces =====");
            {
                in.read(buff, 0, 2); // accessFlags
                in.read(buff, 0, 2); // class java8/Clazz2
                in.read(buff, 0, 2); // class java/lang/Object
                in.read(buff, 0, 2); // interfaceCount
                int interfaceCount = u2(buff[0], buff[1]);
                System.out.println("interfaceCount:" + interfaceCount);
                for (int ii = 0; ii < interfaceCount; ii++) {
                    in.read(buff, 0, 2); //
                }
            }
            System.out.println("===== Fields =====");
            {
                in.read(buff, 0, 2); // fieldsCount
                int fieldsCount = u2(buff[0], buff[1]);
                System.out.println("fieldsCount:" + fieldsCount);
                for (int ii = 0; ii < fieldsCount; ii++) {
                    in.read(buff, 0, 2); // accessFlags
                    in.read(buff, 0, 2); // utf8 valInt
                    in.read(buff, 0, 2); // utf8 I
                    in.read(buff, 0, 2); // attributesCount
                    int attributesCount = u2(buff[0], buff[1]);
                    for (int jj = 0; jj < attributesCount; jj++) {
                        in.read(buff, 0, 2); //
                        in.read(buff, 0, 4); // attributeLength
                        int attributeLength = u4(buff[0], buff[1], buff[2], buff[3]);
                        if (attributeLength > 0) {
                            in.read(buff, 0, attributeLength); // attribute bytes
                        }
                    }
                }
            }
            System.out.println("===== Methods =====");
            {
                in.read(buff, 0, 2); // methodsCount
                int methodsCount = u2(buff[0], buff[1]);
                System.out.println("methodsCount:" + methodsCount);
                for (int ii = 0; ii < methodsCount; ii++) {
                    in.read(buff, 0, 2); // accessFlags
                    in.read(buff, 0, 2); // utf8 <init> method name
                    int methodNameIndex = u2(buff[0], buff[1]);
                    System.out.println(constantPool.get(methodNameIndex));
                    in.read(buff, 0, 2); // utf8 ()V method parameter
                    in.read(buff, 0, 2); // attributesCount
                    int attributesCount = u2(buff[0], buff[1]);
                    for (int jj = 0; jj < attributesCount; jj++) {
                        in.read(buff, 0, 2); // attribute type (Exceptions,RuntimeVisibleAnnotations,Code)
                        int attributeType = u2(buff[0], buff[1]);
                        Object type = constantPool.get(attributeType);
                        if ("Code".equals(type)) {
                            in.read(buff, 0, 4); // attributeLength
                            int attributeLength = u4(buff[0], buff[1], buff[2], buff[3]);
                            byte[] tmp = new byte[attributeLength];
                            in.read(tmp, 0, attributeLength); // Code bytes
                            int codeLength = u4(tmp[4], tmp[5], tmp[6], tmp[7]);
                            System.out.println(String.format("codeLength:%d", codeLength));
                        } else if ("Exceptions".equals(type)) {
                            in.read(buff, 0, 4); // attributeLength
                            int attributeLength = u4(buff[0], buff[1], buff[2], buff[3]);
                            // in.read(buff, 0, 2); // numberOfExceptions
                            byte[] tmp = new byte[attributeLength];
                            in.read(tmp, 0, attributeLength); // Exception bytes
                            int numberOfExceptions = u2(tmp[0], tmp[1]);
                            int index = 2;
                            for (int kk = 0; kk < numberOfExceptions; kk++) {
                                System.out.println(String.format("Exception:%d", u2(tmp[index], tmp[++index])));
                                index++;
                            }
                        } else if ("RuntimeVisibleAnnotations".equals(type)) {
                            in.read(buff, 0, 4); // attributeLength
                            int attributeLength = u4(buff[0], buff[1], buff[2], buff[3]);
                            byte[] tmp = new byte[attributeLength];
                            in.read(tmp, 0, attributeLength); // RuntimeVisibleAnnotation bytes
                        }
                    }
                }
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

}
