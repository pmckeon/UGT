package psidum.ugt.model;

public class TileVariant {
    Tile baseTile;

    boolean horizontalFlip;

    boolean verticalFlip;

    public TileVariant(Tile baseTile, boolean horizontalFlip, boolean verticalFlip) {
        this.baseTile = baseTile;
        this.horizontalFlip = horizontalFlip;
        this.verticalFlip = verticalFlip;
    }

    public Tile getBaseTile() {
        return this.baseTile;
    }

    public void setBaseTile(Tile tile) {
        this.baseTile = tile;
    }

    public boolean isHorizontalFlip() {
        return this.horizontalFlip;
    }

    public boolean isVerticalFlip() {
        return this.verticalFlip;
    }

    public int[] getTilePixels() {
        int[] pixels = new int[this.baseTile.pixelsAsMasterPalette.length];
        if (this.horizontalFlip) {
            if (!this.verticalFlip) {
                int i = 0;
                for (int j = 0; j <= 56; j += 8) {
                    for (int pixel = 7; pixel >= 0; pixel--)
                        pixels[i++] = this.baseTile.getPixelsAsRGB()[j + pixel];
                }
                return pixels;
            }
            int pixelCounter = 0;
            for (int row = 56; row >= 0; row -= 8) {
                for (int pixel = 7; pixel >= 0; pixel--)
                    pixels[pixelCounter++] = this.baseTile.getPixelsAsRGB()[row + pixel];
            }
            return pixels;
        }
        if (this.verticalFlip) {
            int pixelCounter = 0;
            for (int row = 56; row >= 0; row -= 8) {
                for (int pixel = 0; pixel < 8; pixel++)
                    pixels[pixelCounter++] = this.baseTile.getPixelsAsRGB()[row + pixel];
            }
            return pixels;
        }
        return this.baseTile.pixelsAsSystem;
    }
}
