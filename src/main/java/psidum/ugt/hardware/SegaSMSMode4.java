package psidum.ugt.hardware;

import java.util.ArrayList;
import java.util.HashSet;
import psidum.ugt.model.Metatile;
import psidum.ugt.model.MetatileSet;
import psidum.ugt.model.MetatileSetExport;
import psidum.ugt.model.Nametable;
import psidum.ugt.model.NametableEntry;
import psidum.ugt.model.NametableExportAsBlock;
import psidum.ugt.model.NametableToScrolltableExport;
import psidum.ugt.model.Palette;
import psidum.ugt.model.PaletteExportAsBlock;
import psidum.ugt.model.Scrolltable;
import psidum.ugt.model.ScrolltableExport;
import psidum.ugt.model.Tile;
import psidum.ugt.model.TilesetExportAsBlock;
import psidum.ugt.util.GeneralUtil;

public class SegaSMSMode4 extends GraphicFormat implements NametableExportAsBlock, PaletteExportAsBlock, TilesetExportAsBlock, NametableToScrolltableExport, MetatileSetExport, ScrolltableExport {
    private String wordLineAffix = ".dw";

    private String wordValueAffix = "$";

    private String byteLineAffix = ".db";

    private String byteValueAffix = "$";

    public int getPaletteSize() {
        return 16;
    }

    public int getPaletteCount() {
        return 2;
    }

    public int getPaletteMapSize() {
        return 64;
    }

    public int getNumberOfPlanes() {
        return 4;
    }

    public boolean hasTransparentPixel() {
        return false;
    }

    public int convertColorToRGB(int hardwareColor) {
        int pixel = (hardwareColor >> 4 & 0x3) * 85;
        pixel |= (hardwareColor >> 2 & 0x3) * 85 << 8;
        pixel |= (hardwareColor & 0x3) * 85 << 16;
        pixel |= 0xFF000000;
        return pixel;
    }

    public int[] convertToMasterPaletteAndReturnUnique(int[] rgbPixels, int[] masterPalettePixels) {
        HashSet<Integer> uniqueColorsHS = new HashSet<>();
        for (int i = 0; i < rgbPixels.length; i++) {
            masterPalettePixels[i] = rgbPixels[i] >> 22 & 0x3 | rgbPixels[i] >> 12 & 0xC | rgbPixels[i] >> 2 & 0x30;
            if (!uniqueColorsHS.contains(Integer.valueOf(masterPalettePixels[i])) &&
                    rgbPixels[i] != getEmptyTileColor())
                uniqueColorsHS.add(Integer.valueOf(masterPalettePixels[i]));
        }
        return GeneralUtil.convertToPrimitiveArray(uniqueColorsHS);
    }

    public String exportAsHexBlock(ArrayList<Tile> tiles) {
        StringBuilder output = new StringBuilder();
        int newlineCounter = 0;
        for (Tile tile : tiles) {
            for (int shift = 56; shift >= 0; shift -= 8) {
                for (int i = 0; i < (tile.getPixelsAsPlanar()).length; i++) {
                    String value = String.format("%02X", new Object[] { Integer.valueOf((int)(tile.getPixelsAsPlanar()[i] >> shift) & 0xFF) });
                    output.append((newlineCounter % 32 == 0) ? (String.valueOf(System.lineSeparator()) + this.byteLineAffix + " " + this.byteValueAffix + value) : (", " + this.byteValueAffix + value));
                    newlineCounter++;
                }
            }
        }
        return output.toString();
    }

    public String exportAsBitDepthHexBlock(ArrayList<Tile> tiles, int bitDepth) {
        StringBuilder output = new StringBuilder();
        int newlineCounter = 0;
        for (Tile tile : tiles) {
            for (int shift = 56; shift >= 0; shift -= 8) {
                for (int i = 0; i < bitDepth; i++) {
                    String value = String.format("%02X", new Object[] { Integer.valueOf((int)(tile.getPixelsAsPlanar()[i] >> shift) & 0xFF) });
                    output.append((newlineCounter % 32 == 0) ? (String.valueOf(System.lineSeparator()) + this.byteLineAffix + " " + this.byteValueAffix + value) : (", " + this.byteValueAffix + value));
                    newlineCounter++;
                }
            }
        }
        return output.toString();
    }

    public byte[] exportAsBinaryBlock(ArrayList<Tile> tiles) {
        if (tiles.size() == 0)
            return null;
        byte[] data = new byte[tiles.size() * (((Tile)tiles.get(0)).getPixelsAsMasterPalette()).length / 2];
        int byteCounter = 0;
        for (Tile tile : tiles) {
            for (int shift = 56; shift >= 0; shift -= 8) {
                for (int i = 0; i < (tile.getPixelsAsPlanar()).length; i++)
                    data[byteCounter++] = (byte)(int)(tile.getPixelsAsPlanar()[i] >> shift & 0xFFL);
            }
        }
        return data;
    }

    public String exportPalettesAsHexBlock(ArrayList<Palette> palettes) {
        StringBuilder output = new StringBuilder();
        for (Palette palette : palettes) {
            int newlineCounter = 0;
            for (int i = 0; i < getPaletteSize(); i++) {
                if (i < (palette.getColors()).length) {
                    String value = String.format("%02X", new Object[] { Integer.valueOf(palette.getColors()[i]) });
                    output.append((newlineCounter % 64 == 0) ? (String.valueOf(System.lineSeparator()) + this.byteLineAffix + " " + this.byteValueAffix + value) : (", " + this.byteValueAffix + value));
                    newlineCounter++;
                } else {
                    String value = "00";
                    output.append((newlineCounter % 64 == 0) ? (String.valueOf(System.lineSeparator()) + this.byteLineAffix + " " + this.byteValueAffix + value) : (", " + this.byteValueAffix + value));
                    newlineCounter++;
                }
            }
        }
        return output.toString();
    }

    public byte[] exportPalettesAsBinaryBlock(ArrayList<Palette> palettes) {
        int size = 0;
        for (Palette palette : palettes)
            size += (palette.getColors()).length;
        byte[] data = new byte[size];
        int byteCounter = 0;
        for (Palette palette : palettes) {
            for (int i = 0; i < (palette.getColors()).length; i++) {
                if (i < (palette.getColors()).length) {
                    data[byteCounter++] = (byte)palette.getColors()[i];
                } else {
                    data[byteCounter++] = 0;
                }
            }
        }
        return data;
    }

    public int exportEntry(NametableEntry nametableEntry) {
        int entry = 0;
        entry |= nametableEntry.getTileVariant().isHorizontalFlip() ? 512 : 0;
        entry |= nametableEntry.getTileVariant().isVerticalFlip() ? 1024 : 0;
        entry |= nametableEntry.getTileVariant().getBaseTile().getVramLocation().getIndex();
        entry |= nametableEntry.getPalette() << 11;
        entry |= (nametableEntry.getPriority() != 0) ? 4096 : 0;
        entry |= (nametableEntry.getMeta() & 0x7) << 13;
        return entry;
    }

    public String exportAsHexBlock(Nametable nametable) {
        StringBuilder output = new StringBuilder();
        int newlineCounter = 0;
        byte b;
        int i;
        NametableEntry[] arrayOfNametableEntry;
        for (i = (arrayOfNametableEntry = nametable.getNametableEntries()).length, b = 0; b < i; ) {
            NametableEntry nametableEntry = arrayOfNametableEntry[b];
            int entry = 0;
            entry |= nametableEntry.getTileVariant().isHorizontalFlip() ? 512 : 0;
            entry |= nametableEntry.getTileVariant().isVerticalFlip() ? 1024 : 0;
            entry |= nametableEntry.getTileVariant().getBaseTile().getVramLocation().getIndex();
            entry |= nametableEntry.getPalette() << 11;
            entry |= (nametableEntry.getPriority() != 0) ? 4096 : 0;
            entry |= (nametableEntry.getMeta() & 0x7) << 13;
            String value = String.format("%04X", new Object[] { Integer.valueOf(entry) });
            output.append((newlineCounter % 16 == 0) ? (String.valueOf(System.lineSeparator()) + this.wordLineAffix + " " + this.wordValueAffix + value) : (", " + this.wordValueAffix + value));
            newlineCounter++;
            b++;
        }
        return output.toString();
    }

    public byte[] exportAsBinaryBlock(Nametable nametable) {
        byte[] data = new byte[(nametable.getNametableEntries()).length * 2];
        int byteCounter = 0;
        byte b;
        int i;
        NametableEntry[] arrayOfNametableEntry;
        for (i = (arrayOfNametableEntry = nametable.getNametableEntries()).length, b = 0; b < i; ) {
            NametableEntry nametableEntry = arrayOfNametableEntry[b];
            int entry = 0;
            entry |= nametableEntry.getTileVariant().isHorizontalFlip() ? 512 : 0;
            entry |= nametableEntry.getTileVariant().isVerticalFlip() ? 1024 : 0;
            entry |= nametableEntry.getTileVariant().getBaseTile().getVramLocation().getIndex();
            entry |= nametableEntry.getPalette() << 11;
            entry |= (nametableEntry.getPriority() != 0) ? 4096 : 0;
            entry |= (nametableEntry.getMeta() & 0x7) << 13;
            data[byteCounter++] = (byte)(entry & 0xFF);
            data[byteCounter++] = (byte)(entry >> 8 & 0xFF);
            b++;
        }
        return data;
    }

    public int convertRGBToHardwareColor(int rgb) {
        return rgb >> 22 & 0x3 | rgb >> 12 & 0xC | rgb >> 2 & 0x30;
    }

    public String getTileFormationString() {
        return "(0,511)";
    }

    public int[] convertToSystemColors(int[] rgbPixels) {
        int[] systemPixels = new int[rgbPixels.length];
        for (int i = 0; i < rgbPixels.length; i++) {
            systemPixels[i] = (rgbPixels[i] >> 22 & 0x3) * 85 << 16;
            systemPixels[i] = systemPixels[i] | (rgbPixels[i] >> 14 & 0x3) * 85 << 8;
            systemPixels[i] = systemPixels[i] | (rgbPixels[i] >> 6 & 0x3) * 85;
            systemPixels[i] = systemPixels[i] | 0xFF000000;
        }
        return systemPixels;
    }

    public Integer getMaximumPaletteAssociation() {
        return null;
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
        return "Sega Master System Mode 4";
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

    public int exportEntryWithMetadata(NametableEntry nametableEntry, int priority, int metadata) {
        int entry = 0;
        entry |= nametableEntry.getTileVariant().isHorizontalFlip() ? 512 : 0;
        entry |= nametableEntry.getTileVariant().isVerticalFlip() ? 1024 : 0;
        entry |= nametableEntry.getTileVariant().getBaseTile().getVramLocation().getIndex();
        entry |= nametableEntry.getPalette() << 11;
        entry |= (priority != 0) ? 4096 : 0;
        entry |= (metadata & 0x7) << 13;
        return entry;
    }

    public String exportAsHexBlock(Scrolltable scrolltable) {
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getScrolltableArray().size()) }) + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getNametable().getWidthInTiles() / scrolltable.getMetatileSet().getWidth()) }) + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getNametable().getHeightInTiles() / scrolltable.getMetatileSet().getHeight()) }) + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getNametable().getWidthInTiles() * 8) }) + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getNametable().getHeightInTiles() * 8) }) + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf(scrolltable.getNametable().getWidthInTiles() / scrolltable.getMetatileSet().getWidth() * 13) }) + System.lineSeparator());
        output.append(String.valueOf(this.byteLineAffix) + " $" + String.format("%02X", new Object[] { Integer.valueOf(1) }) + System.lineSeparator());
        output.append(System.lineSeparator());
        for (int i = 0; i < scrolltable.getScrolltableArray().size(); i++) {
            int shiftedValue = (((Metatile)scrolltable.getScrolltableArray().get(i)).getId() << 3 & 0xF8) + (((Metatile)scrolltable.getScrolltableArray().get(i)).getId() >> 5 & 0x7);
            String value = String.format("%02X", new Object[] { Integer.valueOf(shiftedValue) });
            output.append((i % 32 == 0) ? (String.valueOf(System.lineSeparator()) + this.byteLineAffix + " $" + value) : (", $" + value));
        }
        output.append(System.lineSeparator());
        return output.toString();
    }

    public byte[] exportAsBinaryBlock(Scrolltable scrolltable) {
        ArrayList<Byte> data = new ArrayList<>();
        int value = 0;
        value = scrolltable.getScrolltableArray().size();
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = scrolltable.getNametable().getWidthInTiles() / scrolltable.getMetatileSet().getWidth();
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = scrolltable.getNametable().getHeightInTiles() / scrolltable.getMetatileSet().getHeight();
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = scrolltable.getNametable().getWidthInTiles() * 8;
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = scrolltable.getNametable().getHeightInTiles() * 8;
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = scrolltable.getNametable().getWidthInTiles() / scrolltable.getMetatileSet().getWidth() * 13;
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        data.add(Byte.valueOf((byte)1));
        for (int i = 0; i < scrolltable.getScrolltableArray().size(); i++) {
            int shiftedValue = (((Metatile)scrolltable.getScrolltableArray().get(i)).getId() << 3 & 0xF8) + (((Metatile)scrolltable.getScrolltableArray().get(i)).getId() >> 5 & 0x7);
            data.add(Byte.valueOf((byte)shiftedValue));
        }
        byte[] output = new byte[data.size()];
        for (int j = 0; j < data.size(); ) {
            output[j] = ((Byte)data.get(j)).byteValue();
            j++;
        }
        return output;
    }

    public String exportAsHexBlock(MetatileSet metatileSet) {
        StringBuilder output = new StringBuilder();
        output.append(String.valueOf(this.wordLineAffix) + " $" + String.format("%04X", new Object[] { Integer.valueOf((metatileSet.getMetatiles().size() + 1) * metatileSet.getWidth() * metatileSet.getHeight() * 2) }) + System.lineSeparator());
        output.append(String.valueOf(this.byteLineAffix) + " $" + String.format("%02X", new Object[] { Integer.valueOf(metatileSet.getCollisionCount()) }) + ", $00" + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $0000" + System.lineSeparator());
        output.append(String.valueOf(this.wordLineAffix) + " $0000");
        int counter = 0;
        for (int i = 0; i < metatileSet.getMetatiles().size(); i++) {
            for (int n = 0; n < metatileSet.getWidth() * metatileSet.getHeight(); n++) {
                String value = String.format("%04X", new Object[] { Integer.valueOf(((Metatile)metatileSet.getMetatiles().get(i)).metatileEntries[n]) });
                output.append((counter++ % 16 == 0) ? (String.valueOf(System.lineSeparator()) + this.wordLineAffix + " $" + value) : (", $" + value));
            }
        }
        output.append(System.lineSeparator());
        return output.toString();
    }

    public byte[] exportAsBinaryBlock(MetatileSet metatileSet) {
        ArrayList<Byte> data = new ArrayList<>();
        int value = 0;
        value = (metatileSet.getMetatiles().size() + 1) * metatileSet.getWidth() * metatileSet.getHeight() * 2;
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
        value = metatileSet.getCollisionCount();
        data.add(Byte.valueOf((byte)(value & 0xFF)));
        data.add(Byte.valueOf((byte)0));
        data.add(Byte.valueOf((byte)0));
        data.add(Byte.valueOf((byte)0));
        data.add(Byte.valueOf((byte)0));
        data.add(Byte.valueOf((byte)0));
        for (int i = 0; i < metatileSet.getMetatiles().size(); i++) {
            for (int n = 0; n < metatileSet.getWidth() * metatileSet.getHeight(); n++) {
                value = ((Metatile)metatileSet.getMetatiles().get(i)).metatileEntries[n];
                data.add(Byte.valueOf((byte)(value & 0xFF)));
                data.add(Byte.valueOf((byte)(value >> 8 & 0xFF)));
            }
        }
        byte[] output = new byte[data.size()];
        for (int j = 0; j < data.size(); ) {
            output[j] = ((Byte)data.get(j)).byteValue();
            j++;
        }
        return output;
    }
}
