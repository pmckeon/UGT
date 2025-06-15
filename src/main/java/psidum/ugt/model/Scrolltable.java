package psidum.ugt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.util.HashKey;

public class Scrolltable {
    ArrayList<Metatile> scrolltableArray;

    MetatileSet metatileSet;

    Nametable nametable;

    Tileset tileset;

    public Scrolltable(GraphicFormat graphicFormat, Nametable nametable, Tileset tileset, int metatileWidth, int metatileHeight) throws UGTException {
        this.scrolltableArray = new ArrayList<>();
        this.metatileSet = null;
        this.nametable = null;
        this.tileset = null;
        ArrayList<Metatile> metatiles = new ArrayList<>();
        this.nametable = nametable;
        this.tileset = tileset;
        if (nametable.getHeightInTiles() % metatileHeight != 0 || nametable.getWidthInTiles() % metatileWidth != 0)
            throw new UGTException("Error: scrolltable dimensions must evenly match metatiles!");
        NametableExportAsBlock nametableExporter = (NametableExportAsBlock)graphicFormat;
        Map<HashKey, Metatile> metatileLookup = new HashMap<>();
        int collisionCount = 0;
        for (int y = 0; y < nametable.getHeightInTiles(); y += 2) {
            for (int x = 0; x < nametable.getWidthInTiles(); x += 2) {
                int[] metatileEntries = new int[metatileWidth * metatileHeight];
                int[] metatileKeyData = new int[metatileWidth * metatileHeight + 1];
                int[] priorityInfo = new int[metatileWidth * metatileHeight];
                int[] metaDataInfo = new int[metatileWidth * metatileHeight];
                TileVariant[] tileVariants = new TileVariant[4];
                int entryCount = 0;
                for (int innerY = 0; innerY < metatileHeight; innerY++) {
                    for (int innerX = 0; innerX < metatileHeight; innerX++) {
                        NametableEntry[] nametableEntries = nametable.getNametableEntries();
                        int i = y * nametable.widthInTiles + innerY * nametable.widthInTiles + x + innerX;
                        if (nametableEntries[i].getCollision() != 0)
                            metatileKeyData[metatileWidth * metatileHeight] = 1;
                        metatileEntries[entryCount] = nametableExporter.exportEntry(nametableEntries[i]);
                        metatileKeyData[entryCount] = nametableExporter.exportEntry(nametableEntries[i]);
                        priorityInfo[entryCount] = nametableEntries[i].getPriority();
                        metaDataInfo[entryCount] = nametableEntries[i].getMeta();
                        tileVariants[innerY * 2 + innerX] = (nametable.getNametableEntries()[i]).tileVariant;
                        entryCount++;
                    }
                }
                HashKey hashKey = new HashKey(metatileKeyData);
                Metatile metatile = metatileLookup.get(hashKey);
                if (metatile == null) {
                    metatile = new Metatile(metatileEntries, priorityInfo, metaDataInfo, metatileKeyData[metatileWidth * metatileHeight], tileVariants);
                    metatileLookup.put(hashKey, metatile);
                    if (metatileKeyData[metatileWidth * metatileHeight] != 0)
                        collisionCount++;
                }
                this.scrolltableArray.add(metatile);
            }
        }
        metatiles = new ArrayList<>(metatileLookup.values());
        metatiles.sort(Collections.reverseOrder(Comparator.comparing(Metatile::getCollision)));
        int index = 1;
        for (Metatile metatile : metatiles)
            metatile.setId(index++);
        this.metatileSet = new MetatileSet(metatiles, collisionCount + 1, metatileWidth, metatileHeight);
        StringBuilder output = new StringBuilder();
    }

    public Scrolltable(GraphicFormat graphicFormat, Nametable nametable, Tileset tileset, int metatileWidth, int metatileHeight, TiledMetaData tiledMetaData) throws UGTException {
        int[] collisionData, priorityData, metaData;
        this.scrolltableArray = new ArrayList<>();
        this.metatileSet = null;
        this.nametable = null;
        this.tileset = null;
        ArrayList<Metatile> metatiles = new ArrayList<>();
        this.nametable = nametable;
        this.tileset = tileset;
        if (nametable.getHeightInTiles() % metatileHeight != 0 || nametable.getWidthInTiles() % metatileWidth != 0)
            throw new UGTException("Error: scrolltable dimensions must evenly match metatiles!");
        NametableToScrolltableExport nametableExporter = (NametableToScrolltableExport)graphicFormat;
        Map<HashKey, Metatile> metatileLookup = new HashMap<>();
        int collisionCount = 0;
        if (tiledMetaData != null) {
            collisionData = tiledMetaData.GSECollisionLayer;
            priorityData = tiledMetaData.GSEPriorityLayer;
            metaData = tiledMetaData.GSEMetaLayer;
        } else {
            collisionData = new int[nametable.nametableEntries.length];
            priorityData = new int[nametable.nametableEntries.length];
            metaData = new int[nametable.nametableEntries.length];
        }
        for (int y = 0; y < nametable.getHeightInTiles(); y += 2) {
            for (int x = 0; x < nametable.getWidthInTiles(); x += 2) {
                int[] metatileEntries = new int[metatileWidth * metatileHeight];
                int[] metatileKeyData = new int[metatileWidth * metatileHeight + 1];
                int[] priorityInfo = new int[metatileWidth * metatileHeight];
                int[] metaDataInfo = new int[metatileWidth * metatileHeight];
                TileVariant[] tileVariants = new TileVariant[4];
                int entryCount = 0;
                for (int innerY = 0; innerY < metatileHeight; innerY++) {
                    for (int innerX = 0; innerX < metatileHeight; innerX++) {
                        int i = y * nametable.widthInTiles + innerY * nametable.widthInTiles + x + innerX;
                        if (collisionData[i] != 0)
                            metatileKeyData[metatileWidth * metatileHeight] = 1;
                        metatileEntries[entryCount] = nametableExporter.exportEntryWithMetadata(nametable.getNametableEntries()[i], priorityData[i], metaData[i]);
                        metatileKeyData[entryCount] = nametableExporter.exportEntryWithMetadata(nametable.getNametableEntries()[i], priorityData[i], metaData[i]);
                        priorityInfo[entryCount] = priorityData[i];
                        metaDataInfo[entryCount] = metaData[i];
                        tileVariants[innerY * 2 + innerX] = (nametable.getNametableEntries()[i]).tileVariant;
                        entryCount++;
                    }
                }
                HashKey hashKey = new HashKey(metatileKeyData);
                Metatile metatile = metatileLookup.get(hashKey);
                if (metatile == null) {
                    metatile = new Metatile(metatileEntries, priorityInfo, metaDataInfo, metatileKeyData[metatileWidth * metatileHeight], tileVariants);
                    metatileLookup.put(hashKey, metatile);
                    if (metatileKeyData[metatileWidth * metatileHeight] != 0)
                        collisionCount++;
                }
                this.scrolltableArray.add(metatile);
            }
        }
        metatiles = new ArrayList<>(metatileLookup.values());
        metatiles.sort(Collections.reverseOrder(Comparator.comparing(Metatile::getCollision)));
        int index = 1;
        for (Metatile metatile : metatiles)
            metatile.setId(index++);
        this.metatileSet = new MetatileSet(metatiles, collisionCount + 1, metatileWidth, metatileHeight);
        StringBuilder output = new StringBuilder();
    }

    public String ExportAsHex() {
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.scrolltableArray.size()) }) + System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.nametable.widthInTiles / this.metatileSet.getWidth()) }) + System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.nametable.heightInTiles / this.metatileSet.getHeight()) }) + System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.nametable.widthInTiles * 8) }) + System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.nametable.heightInTiles * 8) }) + System.lineSeparator());
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf(this.nametable.widthInTiles / this.metatileSet.getWidth() * 13) }) + System.lineSeparator());
        output.append(".db %00000001" + System.lineSeparator());
        output.append(System.lineSeparator());
        for (int i = 0; i < this.scrolltableArray.size(); i++) {
            int shiftedValue = (((Metatile)this.scrolltableArray.get(i)).getId() << 3 & 0xF8) + (((Metatile)this.scrolltableArray.get(i)).getId() >> 5 & 0x7);
            String value = String.format("%02X", new Object[] { Integer.valueOf(shiftedValue) });
            output.append((i % 32 == 0) ? (String.valueOf(System.lineSeparator()) + ".db $" + value) : (", $" + value));
        }
        output.append(System.lineSeparator());
        return output.toString();
    }

    public MetatileSet getMetatileSet() {
        return this.metatileSet;
    }

    public ArrayList<Metatile> getScrolltableArray() {
        return this.scrolltableArray;
    }

    public Nametable getNametable() {
        return this.nametable;
    }
}
