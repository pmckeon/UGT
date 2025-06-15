package psidum.ugt.hardware;

import java.util.HashSet;
import psidum.ugt.util.GeneralUtil;

public class SegaSMSMode0 extends GraphicFormat {
    private String wordLineAffix = ".dw";

    private String wordValueAffix = "$";

    private String byteLineAffix = ".db";

    private String byteValueAffix = "$";

    int[][] colors = new int[][] {
            new int[3],
            new int[3], { 33, 200, 66 }, { 94, 220, 120 }, { 84, 85, 237 }, { 125, 118, 252 }, { 212, 82, 77 }, { 66, 235, 245 }, { 252, 85, 84 }, { 255, 121, 120 },
            { 212, 193, 84 }, { 230, 206, 128 }, { 33, 176, 59 }, { 201, 91, 186 }, { 204, 204, 204 }, { 255, 255, 255 } };

    int[] rgbColors = new int[] {
            -16777216,
            -16777216,
            -12666807,
            -9121667,
            -10922528,
            -8358159,
            -4628911,
            -10101777,
            -2398887,
            -30339,
            -3357858,
            -2174841,
            -12934591,
            -4757835,
            -3355444,
            -1 };

    public int convertColorToRGB(int hardwareColor) {
        return this.rgbColors[hardwareColor];
    }

    public int convertRGBToHardwareColor(int rgb) {
        int[] results = new int[16];
        for (int i = 0; i < 16; i++) {
            int n = rgb & 0xFF0000;
            n >>= 16;
            if (n > this.colors[i][0]) {
                results[i] = results[i] + n - this.colors[i][0];
            } else if (n < this.colors[i][0]) {
                results[i] = results[i] + this.colors[i][0] - n;
            }
            n = rgb & 0xFF00;
            n >>= 8;
            if (n > this.colors[i][1]) {
                results[i] = results[i] + n - this.colors[i][1];
            } else if (n < this.colors[i][1]) {
                results[i] = results[i] + this.colors[i][1] - n;
            }
            n = rgb & 0xFF;
            if (n > this.colors[i][2]) {
                results[i] = results[i] + n - this.colors[i][2];
            } else if (n < this.colors[i][2]) {
                results[i] = results[i] + this.colors[i][2] - n;
            }
        }
        int proximity = 0;
        for (int j = 1; j < 16; j++) {
            if (results[j] < results[proximity])
                proximity = j;
        }
        return proximity;
    }

    public int getPaletteSize() {
        return 2;
    }

    public int getPaletteCount() {
        return 32;
    }

    public int getPaletteMapSize() {
        return 16;
    }

    public int getNumberOfPlanes() {
        return 1;
    }

    public boolean hasTransparentPixel() {
        return false;
    }

    public Integer getMaximumPaletteAssociation() {
        return Integer.valueOf(8);
    }

    public int[] convertToMasterPaletteAndReturnUnique(int[] rgbPixels, int[] masterPalettePixels) {
        HashSet<Integer> uniqueColorsHS = new HashSet<>();
        for (int i = 0; i < rgbPixels.length; i++) {
            masterPalettePixels[i] = convertRGBToHardwareColor(rgbPixels[i]);
            if (!uniqueColorsHS.contains(Integer.valueOf(masterPalettePixels[i])))
                uniqueColorsHS.add(Integer.valueOf(masterPalettePixels[i]));
        }
        return GeneralUtil.convertToPrimitiveArray(uniqueColorsHS);
    }

    public int[] convertToSystemColors(int[] rgbPixels) {
        int[] systemPixels = new int[rgbPixels.length];
        for (int i = 0; i < rgbPixels.length; i++)
            systemPixels[i] = this.rgbColors[convertRGBToHardwareColor(rgbPixels[i])];
        return systemPixels;
    }

    public String getTileFormationString() {
        return "(0,255)";
    }

    public String getWordLineAffix() {
        return this.wordLineAffix;
    }

    public String getWordValueAffix() {
        return this.wordValueAffix;
    }

    public String getByteLineAffix() {
        return this.byteLineAffix;
    }

    public String getByteValueAffix() {
        return this.byteValueAffix;
    }

    public String getUIDescriptionString() {
        return "Sega Master System Mode 0";
    }

    public void setWordLineAffix(String affix) {
        this.wordLineAffix = affix;
    }

    public void setWordValueAffix(String affix) {
        this.wordValueAffix = affix;
    }

    public void setByteLineAffix(String affix) {
        this.byteLineAffix = affix;
    }

    public void setByteValueAffix(String affix) {
        this.byteValueAffix = affix;
    }
}
