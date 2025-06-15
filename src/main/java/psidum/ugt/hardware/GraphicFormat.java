package psidum.ugt.hardware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import psidum.ugt.model.Palette;
import psidum.ugt.model.Tile;
import psidum.ugt.model.TileVRAMLocation;
import psidum.ugt.model.TileVariant;
import psidum.ugt.model.Tileset;
import psidum.ugt.util.HashKey;

public abstract class GraphicFormat {
    private Integer emptyTileColor = Integer.valueOf(-261892);

    private Integer transparentPixel = Integer.valueOf(-524040);

    public void updateTileset(Tileset tileset, ArrayList<Palette> palettes, ArrayList<Tile> tiles, Map<HashKey, TileVariant> tileLookup, boolean isImport) throws UGTException {
        extractTiles(tileset, tiles, tileLookup, isImport);
        allocateTilesToVRAM(tileset.getVramTileFormation(), tiles);
        switch (tileset.getPaletteType()) {
            case basic:
                palettes.add(new Palette(tileset.getParsedImage().getUniqueColors(), this));
                break;
            case custom:
                palettes.addAll(Arrays.asList(Palette.processCustomPalette(tileset.getPaletteFormation(), this)));
                if (palettes.size() == 0)
                    throw new UGTException("Error: custom palette string is invalid!");
                break;
            case optimized:
                palettes.addAll(Arrays.asList(Palette.generateOptimizedPalettes(tiles.<Tile>toArray(new Tile[tiles.size()]), this)));
                if (palettes.size() == 0)
                    throw new UGTException("Error: color usage exceeds hardware palettes!");
                break;
            default:
                throw new RuntimeException("Error Processing palette(s)!");
        }
        allocateTilesToPalette(tiles, palettes);
    }

    private void allocateTilesToPalette(ArrayList<Tile> tiles, ArrayList<Palette> palettes) throws UGTException {
        for (Palette palette : palettes)
            palette.setAssociatedTilesCount(0);
        for (int i = 0; i < tiles.size(); i++) {
            boolean paletteFound = false;
            for (int n = 0; n < palettes.size(); n++) {
                if (((Palette)palettes.get(n)).getUniqueColors().containsAll(((Tile)tiles.get(i)).getUniqueColors())) {
                    ((Tile)tiles.get(i)).setPaletteNumber(n);
                    ((Tile)tiles.get(i)).updatePalettePixels(palettes.get(n));
                    ((Palette)palettes.get(n)).incrementAssociatedTileCount();
                    paletteFound = true;
                    break;
                }
            }
            if (!paletteFound)
                throw new UGTException("Image cannot be realised using palette!");
        }
    }

    private void extractTiles(Tileset tileset, ArrayList<Tile> tiles, Map<HashKey, TileVariant> tileLookup, boolean isImport) throws UGTException {
        int tileWidth = tileset.getTileWidth();
        int tileHeight = tileset.getTileHeight();
        int widthInPixels = tileset.getParsedImage().getWidth();
        int heightInPixels = tileset.getParsedImage().getHeight();
        int tileTotalPixels = tileWidth * tileHeight;
        int nametableTotalPixels = widthInPixels * heightInPixels;
        int pixelsInTileRow = widthInPixels * tileHeight;
        int[] masterPalettePixels = tileset.getParsedImage().getMasterPalettePixels();
        int[] rgbPixels = tileset.getParsedImage().getRgbPixels();
        int[] systemPixels = tileset.getParsedImage().getSystemPixels();
        int tileCount = 0;
        Integer layout = null;
        StringBuilder layoutString = new StringBuilder();
        for (int y = 0; y < nametableTotalPixels; y += pixelsInTileRow) {
            for (int x = 0; x < widthInPixels; x += tileset.getTileWidth()) {
                int[] pixelsAsMasterPalette = new int[tileTotalPixels];
                int[] pixelsAsRGB = new int[tileTotalPixels];
                int[] pixelsAsSystem = new int[tileTotalPixels];
                HashSet<Integer> uniqueColors = new HashSet<>();
                int pixelCounter = 0;
                for (int pixelY = 0; pixelY < pixelsInTileRow; pixelY += widthInPixels) {
                    for (int pixelX = 0; pixelX < tileset.getTileWidth(); pixelX++) {
                        pixelsAsMasterPalette[pixelCounter] = masterPalettePixels[x + pixelX + y + pixelY];
                        pixelsAsRGB[pixelCounter] = rgbPixels[x + pixelX + y + pixelY];
                        pixelsAsSystem[pixelCounter] = systemPixels[x + pixelX + y + pixelY];
                        if (!uniqueColors.contains(Integer.valueOf(masterPalettePixels[x + pixelX + y + pixelY])))
                            uniqueColors.add(Integer.valueOf(masterPalettePixels[x + pixelX + y + pixelY]));
                        pixelCounter++;
                    }
                }
                if (uniqueColors.size() == 1 && pixelsAsRGB[0] == getEmptyTileColor()) {
                    if (layout != null) {
                        layoutString.append("(" + layout + "," + (tileCount - 1) + ")");
                        layout = null;
                    }
                    tileCount++;
                } else {
                    if (layout == null)
                        layout = Integer.valueOf(tileCount);
                    tileCount++;
                    if (uniqueColors.size() > getPaletteSize())
                        throw new UGTException("Image cannot be realised using specified hardware. Too many colors per tile!");
                    HashKey hashKey = null;
                    TileVariant tileVariant = null;
                    if (tileset.isUniqueTiles()) {
                        hashKey = new HashKey(pixelsAsMasterPalette);
                        tileVariant = tileLookup.get(hashKey);
                    }
                    if (tileVariant == null) {
                        Tile tile = new Tile(tileWidth, tileHeight, uniqueColors,
                                tileset.isHorizontalMirroring(), tileset.isVerticalMirroring(),
                                pixelsAsRGB, pixelsAsMasterPalette, pixelsAsSystem,
                                tileLookup, hashKey, this);
                        tiles.add(tile);
                    }
                }
            }
        }
        if (layout != null) {
            layoutString.append("(" + layout + "," + (tileCount - 1) + ")");
            layout = null;
        }
        if (isImport)
            tileset.setVramTileFormation(layoutString.toString());
    }

    private Stack<TileVRAMLocation> allocateTilesToVRAM(String vramTileFormation, ArrayList<Tile> tiles) throws UGTException {
        HashSet<Integer> uniqueNumbers = new HashSet<>();
        ArrayList<TileVRAMLocation> vram = new ArrayList<>();
        Stack<TileVRAMLocation> vramStack = new Stack<>();
        int tileSizeInBytes = (((Tile)tiles.get(0)).getPixelsAsMasterPalette()).length;
        String tileLayoutString = vramTileFormation.replaceAll("[^\\d^,^)]", "");
        tileLayoutString = tileLayoutString.replaceAll("[)]", ",");
        String[] vramLayout = tileLayoutString.split(",");
        if (vramLayout.length == 0 || vramLayout.length % 2 != 0)
            throw new UGTException("Error: Tile VRAM formation string is invalid!" + System.lineSeparator());
        int i;
        for (i = 0; i < vramLayout.length; i += 2) {
            int start;
            int end;
            try {
                start = Integer.parseInt(vramLayout[i]);
                end = Integer.parseInt(vramLayout[i + 1]);
            } catch (Exception e) {
                throw new UGTException("Error: Tile VRAM formation string is invalid!" + System.lineSeparator());
            }
            if (end < start)
                throw new UGTException("Error: Tile VRAM formation string is invalid! Start index greater than end index!" + System.lineSeparator());
            for (; start <= end; start++) {
                if (uniqueNumbers.contains(Integer.valueOf(start)))
                    throw new UGTException("Error: Tile VRAM formation string is invalid! Overlapping VRAM ranges!");
                uniqueNumbers.add(Integer.valueOf(start));
                vram.add(new TileVRAMLocation(start, start * tileSizeInBytes));
            }
        }
        for (i = vram.size() - 1; i >= 0; ) {
            vramStack.push(vram.get(i));
            i--;
        }
        if (tiles.size() > vram.size())
            throw new UGTException("Not enough VRAM allocated to store this image!");
        for (Tile n : tiles)
            n.setVramLocation(vramStack.pop());
        return vramStack;
    }

    public int getTransparentPixel() {
        return this.transparentPixel.intValue();
    }

    public void setTransparentPixel(int color) {
        this.transparentPixel = Integer.valueOf(color);
    }

    public int getEmptyTileColor() {
        return this.emptyTileColor.intValue();
    }

    public void setEmptyTileColor(int color) {
        this.emptyTileColor = Integer.valueOf(color);
    }

    public abstract int getPaletteSize();

    public abstract int getPaletteCount();

    public abstract int getPaletteMapSize();

    public abstract int getNumberOfPlanes();

    public abstract boolean hasTransparentPixel();

    public abstract Integer getMaximumPaletteAssociation();

    public abstract int convertColorToRGB(int paramInt);

    public abstract int convertRGBToHardwareColor(int paramInt);

    public abstract int[] convertToMasterPaletteAndReturnUnique(int[] paramArrayOfint1, int[] paramArrayOfint2);

    public abstract int[] convertToSystemColors(int[] paramArrayOfint);

    public abstract String getTileFormationString();

    public abstract String getWordLineAffix();

    public abstract void setWordLineAffix(String paramString);

    public abstract String getWordValueAffix();

    public abstract void setWordValueAffix(String paramString);

    public abstract String getByteLineAffix();

    public abstract void setByteLineAffix(String paramString);

    public abstract String getByteValueAffix();

    public abstract void setByteValueAffix(String paramString);

    public abstract String getUIDescriptionString();
}
