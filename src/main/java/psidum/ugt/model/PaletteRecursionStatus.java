package psidum.ugt.model;

import java.util.HashSet;

public class PaletteRecursionStatus {
    public HashSet<Integer>[] palettes;

    public int colorSetIndex;

    public PaletteRecursionStatus(HashSet[] palettes, int colorSetIndex) {
        this.palettes = (HashSet<Integer>[])palettes;
        this.colorSetIndex = colorSetIndex;
    }
}
