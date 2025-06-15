package psidum.ugt.model;

public interface ScrolltableExport {
    String exportAsHexBlock(Scrolltable paramScrolltable);

    byte[] exportAsBinaryBlock(Scrolltable paramScrolltable);
}
