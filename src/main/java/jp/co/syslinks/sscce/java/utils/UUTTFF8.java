package jp.co.syslinks.sscce.java.utils;

import static java.lang.Character.MAX_SURROGATE;
import static java.lang.Character.MIN_SURROGATE;

/**
 * https://k11i.biz/blog/2018/01/31/count-number-of-bytes-in-encoded-string/
 * UTF-8 エンコーディングしたときのバイト数を知りたければ、Guava の Utf8.encodedLength() を使うのが最適だよ
 * https://github.com/google/guava/blob/fd919e54a55ba169dc7d9f54b7b3485aa7fa0970/guava/src/com/google/common/base/Utf8.java#L50-L70
 */
public class UUTTFF8 {

    public static void main(String[] args) {
        String s = "日本語";
        System.out.println(encodedLength(s));
    }

    public static int encodedLength(CharSequence sequence) {
        // Warning to maintainers: this implementation is highly optimized.
        int utf16Length = sequence.length();
        int utf8Length = utf16Length;
        int i = 0;

        // This loop optimizes for pure ASCII.
        while (i < utf16Length && sequence.charAt(i) < 0x80) {
            i++;
        }

        // This loop optimizes for chars less than 0x800.
        for (; i < utf16Length; i++) {
            char c = sequence.charAt(i);
            if (c < 0x800) {
                utf8Length += ((0x7f - c) >>> 31); // branch free!
            } else {
                utf8Length += encodedLengthGeneral(sequence, i);
                break;
            }
        }

        if (utf8Length < utf16Length) {
            // Necessary and sufficient condition for overflow because of maximum 3x expansion
            throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (utf8Length + (1L << 32)));
        }
        return utf8Length;
    }

    private static int encodedLengthGeneral(CharSequence sequence, int start) {
        int utf16Length = sequence.length();
        int utf8Length = 0;
        for (int i = start; i < utf16Length; i++) {
            char c = sequence.charAt(i);
            if (c < 0x800) {
                utf8Length += (0x7f - c) >>> 31; // branch free!
            } else {
                utf8Length += 2;
                // jdk7+: if (Character.isSurrogate(c)) {
                if (MIN_SURROGATE <= c && c <= MAX_SURROGATE) {
                    // Check that we have a well-formed surrogate pair.
                    if (Character.codePointAt(sequence, i) == c) {
                        throw new IllegalArgumentException(unpairedSurrogateMsg(i));
                    }
                    i++;
                }
            }
        }
        return utf8Length;
    }

    private static String unpairedSurrogateMsg(int i) {
        return "Unpaired surrogate at index " + i;
    }

}
