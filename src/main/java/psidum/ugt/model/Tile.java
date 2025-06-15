package psidum.ugt.model;

import java.util.HashSet;
import java.util.Map;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.util.HashKey;

public class Tile {
    GraphicFormat graphicFormat;

    long[] pixelsAsPlanar;

    int[] pixelsAsRGB;

    int[] pixelsAsMasterPalette;

    int[] pixelsAsSystem;

    int[] pixelsAsPalette;

    HashSet<Integer> uniqueColors;

    boolean packed;

    int newField = 0;

    TileVRAMLocation vramLocation = null;

    HashKey hash;

    TileVariant originalVariant;

    int paletteNumber = 0;

    public int useCount = 0;

    public Tile replacementTile = null;

    public int temp_Int1;

    public Tile(int tileWidth, int tileHeight, HashSet<Integer> uniqueColors, boolean allowHorizontalFlip, boolean allowVerticalFlip, int[] pixelsAsRGB, int[] pixelsAsMasterPalette, int[] pixelsAsSystem, Map<HashKey, TileVariant> tileLookUp, HashKey hash, GraphicFormat graphicFormat) {
        this.graphicFormat = graphicFormat;
        this.pixelsAsRGB = pixelsAsRGB;
        this.pixelsAsMasterPalette = pixelsAsMasterPalette;
        this.pixelsAsSystem = pixelsAsSystem;
        this.hash = hash;
        this.uniqueColors = uniqueColors;
        int width = tileWidth;
        int height = tileHeight;
        int sizeInPixels = width * height;
        HashKey variantHashKey = new HashKey(pixelsAsMasterPalette);
        this.originalVariant = new TileVariant(this, false, false);
        tileLookUp.put(variantHashKey, this.originalVariant);
        if (allowVerticalFlip | allowHorizontalFlip) {
            if (allowHorizontalFlip) {
                int pixelCounter = 0;
                int[] horiontalFlippedPixels = new int[sizeInPixels];
                for (int y = 0; y < sizeInPixels; y += 8) {
                    for (int x = 7; x >= 0; x--)
                        horiontalFlippedPixels[pixelCounter++] = pixelsAsMasterPalette[x + y];
                }
                variantHashKey = new HashKey(horiontalFlippedPixels);
                if (!tileLookUp.containsKey(variantHashKey))
                    tileLookUp.put(variantHashKey, new TileVariant(this, true, false));
                if (allowVerticalFlip) {
                    pixelCounter = 0;
                    int[] verticalFlippedPixels = new int[sizeInPixels];
                    for (int i = sizeInPixels - 8; i >= 0; i -= 8) {
                        for (int x = 0; x < 8; x++)
                            verticalFlippedPixels[pixelCounter++] = horiontalFlippedPixels[x + i];
                    }
                    variantHashKey = new HashKey(verticalFlippedPixels);
                    if (!tileLookUp.containsKey(variantHashKey))
                        tileLookUp.put(variantHashKey, new TileVariant(this, true, true));
                }
            }
            if (allowVerticalFlip) {
                int pixelCounter = 0;
                int[] verticalFlippedPixels = new int[sizeInPixels];
                for (int y = sizeInPixels - 8; y >= 0; y -= 8) {
                    for (int x = 0; x < 8; x++)
                        verticalFlippedPixels[pixelCounter++] = pixelsAsMasterPalette[x + y];
                }
                variantHashKey = new HashKey(verticalFlippedPixels);
                if (!tileLookUp.containsKey(variantHashKey))
                    tileLookUp.put(variantHashKey, new TileVariant(this, false, true));
            }
        }
    }

    public void updatePalettePixels(Palette palette) {
        this.pixelsAsPalette = new int[this.pixelsAsRGB.length];
        for (int i = 0; i < this.pixelsAsPalette.length; i++)
            this.pixelsAsPalette[i] = palette.getPaletteLookUp()[this.pixelsAsMasterPalette[i]].intValue();
        this.pixelsAsPlanar = new long[this.graphicFormat.getNumberOfPlanes()];
        for (int pixelIndex = 0; pixelIndex < this.pixelsAsPalette.length; pixelIndex++) {
            for (int planeIndex = 0; planeIndex < this.graphicFormat.getNumberOfPlanes(); planeIndex++)
                this.pixelsAsPlanar[planeIndex] = this.pixelsAsPlanar[planeIndex] << 1L | (this.pixelsAsPalette[pixelIndex] >> planeIndex & 0x1);
        }
    }

    public int getPaletteNumber() {
        return this.paletteNumber;
    }

    public void setPaletteNumber(int paletteNumber) {
        this.paletteNumber = paletteNumber;
    }

    public TileVRAMLocation getVramLocation() {
        return this.vramLocation;
    }

    public void setVramLocation(TileVRAMLocation vramLocation) {
        this.vramLocation = vramLocation;
    }

    public HashSet<Integer> getUniqueColors() {
        return this.uniqueColors;
    }

    public int[] getPixelsAsMasterPalette() {
        return this.pixelsAsMasterPalette;
    }

    public int[] getPixelsAsRGB() {
        return this.pixelsAsRGB;
    }

    public long[] getPixelsAsPlanar() {
        return this.pixelsAsPlanar;
    }

    public int getVRAMIndex() {
        return this.vramLocation.index;
    }

    public int[] getPixelsAsPalette() {
        return this.pixelsAsPalette;
    }

    public int getUseCount() {
        return this.useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public void incrementUseCount() {
        this.useCount++;
    }
}
