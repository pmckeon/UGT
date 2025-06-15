package psidum.ugt.model;

import java.util.ArrayList;

public interface TilesetExportAsBlock {
    String exportAsHexBlock(ArrayList<Tile> paramArrayList);

    byte[] exportAsBinaryBlock(ArrayList<Tile> paramArrayList);
}
