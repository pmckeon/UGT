package psidum.ugt.model;

public class NametableEntry {
    TileVariant tileVariant;

    int palette;

    int priority;

    int meta;

    int nameTableAddressOffset = 0;

    int collision = 0;

    public NametableEntry(TileVariant tileVariant, int palette, int priority, int meta) {
        this.tileVariant = tileVariant;
        this.palette = palette;
        this.priority = priority;
        this.meta = meta;
    }

    public TileVariant getTileVariant() {
        return this.tileVariant;
    }

    public int getPalette() {
        return this.palette;
    }

    public int getNameTableAddressOffset() {
        return this.nameTableAddressOffset;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getMeta() {
        return this.meta;
    }

    public int getCollision() {
        return this.collision;
    }
}
