package psidum.ugt.model;

public interface MetatileSetExport {
    String exportAsHexBlock(MetatileSet paramMetatileSet);

    byte[] exportAsBinaryBlock(MetatileSet paramMetatileSet);
}
