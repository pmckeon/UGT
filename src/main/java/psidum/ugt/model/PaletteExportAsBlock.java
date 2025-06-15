package psidum.ugt.model;

import java.util.ArrayList;

public interface PaletteExportAsBlock {
    String exportPalettesAsHexBlock(ArrayList<Palette> paramArrayList);

    byte[] exportPalettesAsBinaryBlock(ArrayList<Palette> paramArrayList);
}
