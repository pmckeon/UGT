package psidum.ugt.hardware;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ParsedImage {
    int width;

    int height;

    int[] rgbPixels;

    int[] systemPixels;

    int[] masterPalettePixels = null;

    int[] uniqueColors = null;

    private GraphicFormat graphicFormat;

    public GraphicFormat getGraphicFormat() {
        return this.graphicFormat;
    }

    public int[] getRgbPixels() {
        return this.rgbPixels;
    }

    public int[] getSystemPixels() {
        return this.systemPixels;
    }

    public int[] getUniqueColors() {
        return this.uniqueColors;
    }

    public int[] getMasterPalettePixels() {
        return this.masterPalettePixels;
    }

    public BufferedImage getSystemBufferedImage() {
        return populateBufferedImage(this.systemPixels);
    }

    private BufferedImage populateBufferedImage(int[] data) {
        BufferedImage image = new BufferedImage(this.width, this.height, 2);
        int[] imgData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, imgData, 0, data.length);
        return image;
    }

    public ParsedImage(BufferedImage importImage, GraphicFormat graphicFormat) {
        this.graphicFormat = graphicFormat;
        this.width = importImage.getWidth();
        this.height = importImage.getHeight();
        this.rgbPixels = importImage.getRGB(0, 0, this.width, this.height, null, 0, this.width);
        this.masterPalettePixels = new int[this.rgbPixels.length];
        this.uniqueColors = graphicFormat.convertToMasterPaletteAndReturnUnique(this.rgbPixels, this.masterPalettePixels);
        this.systemPixels = graphicFormat.convertToSystemColors(this.rgbPixels);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
