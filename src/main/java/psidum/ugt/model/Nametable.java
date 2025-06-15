package psidum.ugt.model;

import java.util.HashSet;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.ParsedImage;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.util.HashKey;

public class Nametable {
    ParsedImage nametableImage = null;

    GraphicFormat graphicFormat = null;

    NametableEntry[] nametableEntries;

    int widthInTiles;

    int heightInTiles;

    Tileset tileset = null;

    public Nametable(ParsedImage image, Tileset tileset, TiledFile tiledFile) throws UGTException {
        this(image, tileset);
        long[] gslMetaLayer = tiledFile.getGSLMetaLayer();
        long[] gslPriorityLayer = tiledFile.getGSLPriorityLayer();
        long[] gslCollisionLayer = tiledFile.getGSLCollisionLayer();
        int startid = tiledFile.getMetaInformationStartID();
        for (int i = 0; i < this.nametableEntries.length; i++) {
            (this.nametableEntries[i]).collision = (gslCollisionLayer[i] == 0L) ? 0 : ((int)gslCollisionLayer[i] - startid + 1);
            (this.nametableEntries[i]).priority = (gslPriorityLayer[i] == 0L) ? 0 : ((int)gslPriorityLayer[i] - startid + 1);
            (this.nametableEntries[i]).meta = (gslMetaLayer[i] == 0L) ? 0 : ((int)gslMetaLayer[i] - startid + 1);
        }
    }

    public Nametable(ParsedImage image, Tileset tileset) throws UGTException {
        this.nametableImage = image;
        this.tileset = tileset;
        this.graphicFormat = this.nametableImage.getGraphicFormat();
        this.widthInTiles = this.nametableImage.getWidth() / tileset.getTileWidth();
        this.heightInTiles = this.nametableImage.getHeight() / tileset.getTileHeight();
        int tileWidth = tileset.getTileWidth();
        int tileHeight = tileset.getTileHeight();
        int widthInPixels = this.nametableImage.getWidth();
        int heightInPixels = this.nametableImage.getHeight();
        int tileTotalPixels = tileWidth * tileHeight;
        int nametableTotalPixels = widthInPixels * heightInPixels;
        int pixelsInTileRow = widthInPixels * tileHeight;
        int[] masterPalettePixels = this.nametableImage.getMasterPalettePixels();
        int[] rgbPixels = this.nametableImage.getRgbPixels();
        this.nametableEntries = new NametableEntry[widthInPixels / tileWidth * heightInPixels / tileHeight];
        int nametableEntryCounter = 0;
        for (int y = 0; y < nametableTotalPixels; y += pixelsInTileRow) {
            for (int x = 0; x < widthInPixels; x += tileWidth) {
                int[] pixelsAsMasterPalette = new int[tileTotalPixels];
                int[] pixelsAsRGB = new int[tileTotalPixels];
                HashSet<Integer> uniqueColors = new HashSet<>();
                int pixelCounter = 0;
                for (int pixelY = 0; pixelY < pixelsInTileRow; pixelY += widthInPixels) {
                    for (int pixelX = 0; pixelX < tileWidth; pixelX++) {
                        pixelsAsMasterPalette[pixelCounter] = masterPalettePixels[x + pixelX + y + pixelY];
                        pixelsAsRGB[pixelCounter] = rgbPixels[x + pixelX + y + pixelY];
                        if (!uniqueColors.contains(Integer.valueOf(masterPalettePixels[x + pixelX + y + pixelY])))
                            uniqueColors.add(Integer.valueOf(masterPalettePixels[x + pixelX + y + pixelY]));
                        pixelCounter++;
                    }
                }
                if (uniqueColors.size() == 1 && pixelsAsRGB[0] == this.graphicFormat.getEmptyTileColor())
                    throw new UGTException("Error: this images uses illegal colors. RGB color " +
                            this.graphicFormat.getEmptyTileColor() + " is used as fill color in tilset export image");
                HashKey hashKey = new HashKey(pixelsAsMasterPalette);
                TileVariant tileVariant = tileset.getTileLookup().get(hashKey);
                if (tileVariant == null)
                    throw new UGTException("Error: nametable could not be constructed using current tileset!");
                this.nametableEntries[nametableEntryCounter] = new NametableEntry(tileVariant, 0, 0, 0);
                (this.nametableEntries[nametableEntryCounter]).palette = (this.nametableEntries[nametableEntryCounter]).tileVariant.getBaseTile().getPaletteNumber();
                nametableEntryCounter++;
                tileVariant.getBaseTile().incrementUseCount();
            }
        }
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Nametable Summary" + System.lineSeparator() + System.lineSeparator());
        summary.append("Width = " + this.nametableImage.getWidth() + System.lineSeparator());
        summary.append("Width in Tiles = " + this.widthInTiles + System.lineSeparator() + System.lineSeparator());
        summary.append("Height = " + this.nametableImage.getHeight() + System.lineSeparator());
        summary.append("Height in Tiles = " + this.heightInTiles + System.lineSeparator() + System.lineSeparator());
        summary.append("Total Nametable Entries = " + this.nametableEntries.length + System.lineSeparator());
        return summary.toString();
    }

    public NametableEntry[] getNametableEntries() {
        return this.nametableEntries;
    }

    public int getWidthInTiles() {
        return this.widthInTiles;
    }

    public int getHeightInTiles() {
        return this.heightInTiles;
    }
}
