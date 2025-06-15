package psidum.ugt.model;

public class TileVRAMLocation {
    int index;

    int vramLocation;

    public TileVRAMLocation(int index, int vramLocation) {
        this.index = index;
        this.vramLocation = vramLocation;
    }

    public int getIndex() {
        return this.index;
    }

    public int getVramLocation() {
        return this.vramLocation;
    }
}
