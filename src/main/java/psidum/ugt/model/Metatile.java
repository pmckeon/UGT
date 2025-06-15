package psidum.ugt.model;

public class Metatile {
    public int[] metatileEntries;

    public int[] priorityData;

    int[] metaData;

    int id = 0;

    int collision = 0;

    public TileVariant[] tileVariants;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParsedID() {
        return (this.id << 3 & 0xF8) + (this.id >> 5 & 0x7);
    }

    public Metatile(int[] metatileEntries, int[] priorityData, int[] metaData, int collision, TileVariant[] tileVariants) {
        this.metatileEntries = metatileEntries;
        this.collision = collision;
        this.tileVariants = tileVariants;
        this.priorityData = priorityData;
        this.metaData = metaData;
    }

    public int getCollision() {
        return this.collision;
    }
}
