package psidum.ugt.util;

public class MurmurHash3 {
    public static int seed = -1286432146;

    public static final class LongPair {
        public long val1;

        public long val2;
    }

    public static final int fmix32(int h) {
        h ^= h >>> 16;
        h *= -2048144789;
        h ^= h >>> 13;
        h *= -1028477387;
        h ^= h >>> 16;
        return h;
    }

    public static final long fmix64(long k) {
        k ^= k >>> 33L;
        k *= -49064778989728563L;
        k ^= k >>> 33L;
        k *= -4265267296055464877L;
        k ^= k >>> 33L;
        return k;
    }

    public static final long getLongLittleEndian(byte[] buf, int offset) {
        return buf[offset + 7] << 56L | (
                buf[offset + 6] & 0xFFL) << 48L | (
                buf[offset + 5] & 0xFFL) << 40L | (
                buf[offset + 4] & 0xFFL) << 32L | (
                buf[offset + 3] & 0xFFL) << 24L | (
                buf[offset + 2] & 0xFFL) << 16L | (
                buf[offset + 1] & 0xFFL) << 8L |
                buf[offset] & 0xFFL;
    }

    public static int murmurhash3_x86_32(byte[] data, int offset, int len, int seed) {
        int c1 = -862048943;
        int c2 = 461845907;
        int h1 = seed;
        int roundedEnd = offset + (len & 0xFFFFFFFC);
        for (int i = offset; i < roundedEnd; i += 4) {
            int j = data[i] & 0xFF | (data[i + 1] & 0xFF) << 8 | (data[i + 2] & 0xFF) << 16 | data[i + 3] << 24;
            j *= -862048943;
            j = j << 15 | j >>> 17;
            j *= 461845907;
            h1 ^= j;
            h1 = h1 << 13 | h1 >>> 19;
            h1 = h1 * 5 + -430675100;
        }
        int k1 = 0;
        switch (len & 0x3) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xFF) << 16;
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xFF) << 8;
            case 1:
                k1 |= data[roundedEnd] & 0xFF;
                k1 *= -862048943;
                k1 = k1 << 15 | k1 >>> 17;
                k1 *= 461845907;
                h1 ^= k1;
                break;
        }
        h1 ^=

                len;
        h1 ^= h1 >>> 16;
        h1 *= -2048144789;
        h1 ^= h1 >>> 13;
        h1 *= -1028477387;
        h1 ^= h1 >>> 16;
        return h1;
    }

    public static int murmurhash3_x86_32(int[] data, int offset, int seed) {
        int c1 = -862048943;
        int c2 = 461845907;
        int h1 = seed;
        for (int i = offset; i < data.length; i++) {
            int k1 = data[i];
            k1 *= -862048943;
            k1 = k1 << 15 | k1 >>> 17;
            k1 *= 461845907;
            h1 ^= k1;
            h1 = h1 << 13 | h1 >>> 19;
            h1 = h1 * 5 + -430675100;
        }
        h1 ^= h1 >>> 16;
        h1 *= -2048144789;
        h1 ^= h1 >>> 13;
        h1 *= -1028477387;
        h1 ^= h1 >>> 16;
        return h1;
    }

    public static int murmurhash3_x86_32(CharSequence data, int offset, int len, int seed) {
        int c1 = -862048943;
        int c2 = 461845907;
        int h1 = seed;
        int pos = offset;
        int end = offset + len;
        int k1 = 0;
        int k2 = 0;
        int shift = 0;
        int bits = 0;
        int nBytes = 0;
        while (pos < end) {
            int code = data.charAt(pos++);
            if (code < 128) {
                k2 = code;
                bits = 8;
            } else if (code < 2048) {
                k2 = 0xC0 | code >> 6 | (
                        0x80 | code & 0x3F) << 8;
                bits = 16;
            } else if (code < 55296 || code > 57343 || pos >= end) {
                k2 = 0xE0 | code >> 12 | (
                        0x80 | code >> 6 & 0x3F) << 8 | (
                        0x80 | code & 0x3F) << 16;
                bits = 24;
            } else {
                int utf32 = data.charAt(pos++);
                utf32 = (code - 55232 << 10) + (utf32 & 0x3FF);
                k2 = 0xFF & (0xF0 | utf32 >> 18) | (
                        0x80 | utf32 >> 12 & 0x3F) << 8 | (
                        0x80 | utf32 >> 6 & 0x3F) << 16 | (
                        0x80 | utf32 & 0x3F) << 24;
                bits = 32;
            }
            k1 |= k2 << shift;
            shift += bits;
            if (shift >= 32) {
                k1 *= -862048943;
                k1 = k1 << 15 | k1 >>> 17;
                k1 *= 461845907;
                h1 ^= k1;
                h1 = h1 << 13 | h1 >>> 19;
                h1 = h1 * 5 + -430675100;
                shift -= 32;
                if (shift != 0) {
                    k1 = k2 >>> bits - shift;
                } else {
                    k1 = 0;
                }
                nBytes += 4;
            }
        }
        if (shift > 0) {
            nBytes += shift >> 3;
            k1 *= -862048943;
            k1 = k1 << 15 | k1 >>> 17;
            k1 *= 461845907;
            h1 ^= k1;
        }
        h1 ^= nBytes;
        h1 ^= h1 >>> 16;
        h1 *= -2048144789;
        h1 ^= h1 >>> 13;
        h1 *= -1028477387;
        h1 ^= h1 >>> 16;
        return h1;
    }

    public static void murmurhash3_x64_128(byte[] key, int offset, int len, int seed, LongPair out) {
        long h1 = seed & 0xFFFFFFFFL;
        long h2 = seed & 0xFFFFFFFFL;
        long c1 = -8663945395140668459L;
        long c2 = 5545529020109919103L;
        int roundedEnd = offset + (len & 0xFFFFFFF0);
        for (int i = offset; i < roundedEnd; i += 16) {
            long l1 = getLongLittleEndian(key, i);
            long l2 = getLongLittleEndian(key, i + 8);
            l1 *= -8663945395140668459L;
            l1 = Long.rotateLeft(l1, 31);
            l1 *= 5545529020109919103L;
            h1 ^= l1;
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5L + 1390208809L;
            l2 *= 5545529020109919103L;
            l2 = Long.rotateLeft(l2, 33);
            l2 *= -8663945395140668459L;
            h2 ^= l2;
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5L + 944331445L;
        }
        long k1 = 0L;
        long k2 = 0L;
        switch (len & 0xF) {
            case 15:
                k2 = (key[roundedEnd + 14] & 0xFFL) << 48L;
            case 14:
                k2 |= (key[roundedEnd + 13] & 0xFFL) << 40L;
            case 13:
                k2 |= (key[roundedEnd + 12] & 0xFFL) << 32L;
            case 12:
                k2 |= (key[roundedEnd + 11] & 0xFFL) << 24L;
            case 11:
                k2 |= (key[roundedEnd + 10] & 0xFFL) << 16L;
            case 10:
                k2 |= (key[roundedEnd + 9] & 0xFFL) << 8L;
            case 9:
                k2 |= key[roundedEnd + 8] & 0xFFL;
                k2 *= 5545529020109919103L;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= -8663945395140668459L;
                h2 ^= k2;
            case 8:
                k1 = key[roundedEnd + 7] << 56L;
            case 7:
                k1 |= (key[roundedEnd + 6] & 0xFFL) << 48L;
            case 6:
                k1 |= (key[roundedEnd + 5] & 0xFFL) << 40L;
            case 5:
                k1 |= (key[roundedEnd + 4] & 0xFFL) << 32L;
            case 4:
                k1 |= (key[roundedEnd + 3] & 0xFFL) << 24L;
            case 3:
                k1 |= (key[roundedEnd + 2] & 0xFFL) << 16L;
            case 2:
                k1 |= (key[roundedEnd + 1] & 0xFFL) << 8L;
            case 1:
                k1 |= key[roundedEnd] & 0xFFL;
                k1 *= -8663945395140668459L;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= 5545529020109919103L;
                h1 ^= k1;
                break;
        }
        h1 ^=

                len;
        h2 ^= len;
        h1 += h2;
        h2 += h1;
        h1 = fmix64(h1);
        h2 = fmix64(h2);
        h1 += h2;
        h2 += h1;
        out.val1 = h1;
        out.val2 = h2;
    }
}
