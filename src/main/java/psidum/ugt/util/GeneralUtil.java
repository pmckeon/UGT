package psidum.ugt.util;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;

public class GeneralUtil {
    public static int[] convertToPrimitiveArray(HashSet<Integer> hashSet) {
        int[] primitiveArray = new int[hashSet.size()];
        int index = 0;
        for (Iterator<Integer> i = hashSet.iterator(); i.hasNext(); primitiveArray[index++] = ((Integer)i.next()).intValue());
        return primitiveArray;
    }

    public static void convertToPrimitiveArray(HashSet<Integer> hashSet, int[] primitiveArray) {
        int index = 0;
        for (Iterator<Integer> i = hashSet.iterator(); i.hasNext(); primitiveArray[index++] = ((Integer)i.next()).intValue());
    }

    public static BufferedImage resample(BufferedImage input, int scaleFactor) {
        int W = (int)input.getWidth();
        int H = (int)input.getHeight();
        int S = scaleFactor;
        BufferedImage output = new BufferedImage(W * S, H * S, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = input.getRGB(x, y);
                for (int dy = 0; dy < S; dy++) {
                    for (int dx = 0; dx < S; dx++)
                        output.setRGB(x * S + dx, y * S + dy, argb);
                }
            }
        }
        return output;
    }

    public static boolean isInteger(String str) {
        if (str == null)
            return false;
        int length = str.length();
        if (length == 0)
            return false;
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1)
                return false;
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9')
                return false;
        }
        return true;
    }
}
