package psidum.ugt.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.ParsedImage;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.util.HashKey;

public class Tileset {
    ParsedImage parsedImage = null;

    GraphicFormat graphicFormat = null;

    BufferedImage bufferedImage = null;

    ArrayList<Palette> palettes;

    ArrayList<Tile> tiles = null;

    Map<HashKey, TileVariant> tileLookup = null;

    public Map<Integer, Tile> tilesByID = null;

    int tileWidth = 8;

    int tileHeight = 8;

    boolean horizontalMirroring = true;

    boolean verticalMirroring = true;

    boolean uniqueTiles = false;

    boolean preserveFormation = false;

    String vramTileFormation = null;

    Integer invertMask = null;

    String paletteFormation = null;

    Palette.PaletteType paletteType = Palette.PaletteType.basic;

    public Tileset(GraphicFormat graphicFormat, ParsedImage parsedImage) {
        if (graphicFormat == null || parsedImage == null)
            throw new RuntimeException("Attempting to create tileset with null graphicFormat, parsedImage");
        this.graphicFormat = graphicFormat;
        this.parsedImage = parsedImage;
        this.tiles = new ArrayList<>();
        this.tileLookup = new HashMap<>();
        this.palettes = new ArrayList<>();
    }

    public void update(boolean isImport) throws UGTException {
        this.tiles.clear();
        this.tileLookup.clear();
        this.palettes.clear();
        this.graphicFormat.updateTileset(this, this.palettes, this.tiles, this.tileLookup, isImport);
    }

    public void createIDHashMap() {
        this.tilesByID = new HashMap<>();
        for (Tile tile : this.tiles)
            this.tilesByID.put(Integer.valueOf(tile.vramLocation.index), tile);
    }

    public String getSummary() throws UGTException {
        StringBuilder summary = new StringBuilder();
        summary.append("TileSet Summary" + System.lineSeparator() + System.lineSeparator());
        summary.append("Unique Tiles = " + this.uniqueTiles + System.lineSeparator());
        summary.append("Tile Count = " + this.tiles.size() + System.lineSeparator() + System.lineSeparator());
        summary.append("Horizontal Flip = " + this.horizontalMirroring + System.lineSeparator());
        summary.append("Vertical Flip = " + this.verticalMirroring + System.lineSeparator());
        return summary.toString();
    }

    public Map<HashKey, TileVariant> getTileLookup() {
        return this.tileLookup;
    }

    public BufferedImage getTileImage() {
        int width;
        if (this.tiles.size() == 0)
            return null;
        int emptyTileColor = this.parsedImage.getGraphicFormat().getEmptyTileColor();
        this.tiles.sort(Comparator.comparing(Tile::getVRAMIndex));
        int lastTileIndex = ((Tile)this.tiles.get(this.tiles.size() - 1)).getVRAMIndex() + 1;
        if (lastTileIndex > 16) {
            width = 16 * this.tileWidth;
        } else {
            width = lastTileIndex * this.tileWidth;
        }
        int height = lastTileIndex / 16 * this.tileHeight;
        if (lastTileIndex % 16 != 0)
            height += this.tileHeight;
        int[] data = new int[width * height];
        for (int i = 0; i < data.length; ) {
            data[i] = emptyTileColor;
            i++;
        }
        int currentTile = 0;
        for (int j = 0; j < lastTileIndex; j++) {
            if (((Tile)this.tiles.get(currentTile)).getVRAMIndex() == j) {
                Tile tile = this.tiles.get(currentTile);
                int startY = j / 16 * this.tileHeight * width;
                int startX = j % 16 * this.tileWidth;
                int pixel = 0;
                for (int y = startY; y < startY + width * this.tileHeight; y += width) {
                    for (int x = startX; x < startX + this.tileWidth; x++)
                        data[x + y] = tile.pixelsAsSystem[pixel++];
                }
                currentTile++;
            } else {
                int startY = j / 16 * this.tileHeight * width;
                int startX = j % 16 * this.tileWidth;
                for (int y = startY; y < startY + width * this.tileHeight; y += width) {
                    for (int x = startX; x < startX + this.tileWidth; x++)
                        data[x + y] = emptyTileColor;
                }
            }
        }
        BufferedImage image = new BufferedImage(width, height, 2);
        int[] imgData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, imgData, 0, data.length);
        return image;
    }

    public String getPaletteFormation() {
        return this.paletteFormation;
    }

    public void setPaletteFormation(String paletteFormation) {
        this.paletteFormation = paletteFormation;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public boolean isHorizontalMirroring() {
        return this.horizontalMirroring;
    }

    public void setHorizontalMirroring(boolean horizontalMirroring) {
        this.horizontalMirroring = horizontalMirroring;
    }

    public boolean isVerticalMirroring() {
        return this.verticalMirroring;
    }

    public void setVerticalMirroring(boolean verticalMirroring) {
        this.verticalMirroring = verticalMirroring;
    }

    public boolean isUniqueTiles() {
        return this.uniqueTiles;
    }

    public void setUniqueTiles(boolean duplicates) {
        this.uniqueTiles = duplicates;
    }

    public ParsedImage getParsedImage() {
        return this.parsedImage;
    }

    public Palette.PaletteType getPaletteType() {
        return this.paletteType;
    }

    public void setPaletteType(Palette.PaletteType paletteType) {
        this.paletteType = paletteType;
    }

    public boolean isPreserveFormation() {
        return this.preserveFormation;
    }

    public void setPreserveFormation(boolean preserveFormation) {
        this.preserveFormation = preserveFormation;
    }

    public String getVramTileFormation() {
        return this.vramTileFormation;
    }

    public void setVramTileFormation(String vramTileFormation) {
        this.vramTileFormation = vramTileFormation;
    }

    public Integer getInvertMask() {
        return this.invertMask;
    }

    public void setInvertMask(Integer invertMask) {
        this.invertMask = invertMask;
    }

    public ArrayList<Palette> getPalettes() {
        return this.palettes;
    }

    public ArrayList<Tile> getTiles() {
        return (ArrayList<Tile>)this.tiles.clone();
    }

    public void setParsedImage(ParsedImage parsedImage) {
        this.parsedImage = parsedImage;
    }

    public BufferedImage getBufferedImage() {
        return this.bufferedImage;
    }
}
