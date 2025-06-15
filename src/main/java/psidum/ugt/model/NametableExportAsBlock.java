package psidum.ugt.model;

public interface NametableExportAsBlock {
    String exportAsHexBlock(Nametable paramNametable);

    byte[] exportAsBinaryBlock(Nametable paramNametable);

    int exportEntry(NametableEntry paramNametableEntry);
}
