package psidum.ugt.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.util.GeneralUtil;
import psidum.ugt.util.HashKey;

public class Palette {
    GraphicFormat graphicsFormat = null;

    int[] colors;

    int[] rgbColors;

    HashSet<Integer> uniqueColors;

    Integer[] paletteLookUp;

    int associatedTilesCount = 0;

    public Palette(int[] colors, GraphicFormat graphicsFormat) throws UGTException {
        if (colors.length > graphicsFormat.getPaletteSize())
            throw new UGTException("Error : image uses too many colours for single palette!");
        int paletteIndex = 0;
        this.graphicsFormat = graphicsFormat;
        this.colors = new int[colors.length];
        this.paletteLookUp = new Integer[graphicsFormat.getPaletteMapSize()];
        this.uniqueColors = new HashSet<>();
        if (graphicsFormat.hasTransparentPixel()) {
            this.colors[0] = graphicsFormat.getTransparentPixel();
            this.paletteLookUp[graphicsFormat.getTransparentPixel()] = Integer.valueOf(0);
            this.uniqueColors.add(Integer.valueOf(graphicsFormat.getTransparentPixel()));
            paletteIndex++;
        }
        for (int i = 0; i < colors.length; i++) {
            if (!this.uniqueColors.contains(Integer.valueOf(colors[i]))) {
                this.colors[paletteIndex] = colors[i];
                this.uniqueColors.add(Integer.valueOf(colors[i]));
                this.paletteLookUp[colors[i]] = Integer.valueOf(paletteIndex);
                paletteIndex++;
            }
        }
    }

    public static Palette[] generateOptimizedPalettes(Tile[] tiles, GraphicFormat graphicsFormat) throws UGTException {
        HashMap<HashKey, int[]> colorSets = (HashMap)new HashMap<>();
        int pixelCount = (tiles[0].getPixelsAsRGB()).length;
        for (int i = 0; i < tiles.length; i++) {
            HashSet<Integer> colors = new HashSet<>();
            for (int pixel = 0; pixel < pixelCount; pixel++) {
                if (!colors.contains(Integer.valueOf(tiles[i].getPixelsAsMasterPalette()[pixel])))
                    colors.add(Integer.valueOf(tiles[i].getPixelsAsMasterPalette()[pixel]));
            }
            int[] sortedColors = GeneralUtil.convertToPrimitiveArray(colors);
            Arrays.sort(sortedColors);
            HashKey hashKey = new HashKey(sortedColors);
            if (!colorSets.containsKey(hashKey))
                colorSets.put(hashKey, sortedColors);
        }
        HashSet[] colorPalettes = new HashSet[graphicsFormat.getPaletteCount()];
        for (int j = 0; j < colorPalettes.length; j++) {
            colorPalettes[j] = new HashSet();
            if (graphicsFormat.hasTransparentPixel())
                colorPalettes[j].add(Integer.valueOf(graphicsFormat.getTransparentPixel()));
        }
        int[][] colorSetsAsArrays = (int[][])colorSets.values().toArray((Object[])new int[colorSets.size()][]);
        colorPalettes = (HashSet[])paletteFill((HashSet<Integer>[])colorPalettes, colorSetsAsArrays, graphicsFormat);
        if (colorPalettes == null)
            return null;
        int paletteCount = 0;
        for (int k = 0; k < colorPalettes.length; k++) {
            if (colorPalettes[k].size() != 0 && (
                    colorPalettes[k].size() != 1 || !colorPalettes[k].contains(Integer.valueOf(graphicsFormat.getTransparentPixel()))) && (
                    colorPalettes[k].size() != 1 || !colorPalettes[k].contains(Integer.valueOf(graphicsFormat.getTransparentPixel()))))
                paletteCount++;
        }
        Palette[] optimizedPalettes = new Palette[paletteCount];
        for (int m = 0; m < paletteCount; m++)
            optimizedPalettes[m] = new Palette(GeneralUtil.convertToPrimitiveArray(colorPalettes[m]), graphicsFormat);
        return optimizedPalettes;
    }

    private static HashSet<Integer>[] paletteFill(HashSet[] hardwarePalettes, int[][] colorSetsAsArrays, GraphicFormat graphicsFormat) {
        if (colorSetsAsArrays.length == 0)
            return null;
        Stack<PaletteRecursionStatus> stack = new Stack<>();
        for (int i = hardwarePalettes.length - 1; i >= 0; i--) {
            HashSet[] hardwarePalettesCopy = new HashSet[hardwarePalettes.length];
            for (int n = 0; n < hardwarePalettes.length; n++)
                hardwarePalettesCopy[n] = new HashSet(hardwarePalettes[n]);
            for (int colorIndex = 0; colorIndex < (colorSetsAsArrays[0]).length; colorIndex++) {
                if (!hardwarePalettesCopy[i].contains(Integer.valueOf(colorSetsAsArrays[0][colorIndex])))
                    hardwarePalettesCopy[i].add(Integer.valueOf(colorSetsAsArrays[0][colorIndex]));
            }
            if (hardwarePalettesCopy[i].size() <= graphicsFormat.getPaletteSize())
                stack.push(new PaletteRecursionStatus((HashSet<Integer>[])hardwarePalettesCopy, 1));
        }
        while (!stack.isEmpty()) {
            PaletteRecursionStatus paletteRecursionStatus = stack.pop();
            if (paletteRecursionStatus.colorSetIndex == colorSetsAsArrays.length)
                return paletteRecursionStatus.palettes;
            for (int j = hardwarePalettes.length - 1; j >= 0; j--) {
                HashSet[] hardwarePalettesCopy = new HashSet[hardwarePalettes.length];
                for (int n = 0; n < hardwarePalettes.length; n++)
                    hardwarePalettesCopy[n] = new HashSet<>(paletteRecursionStatus.palettes[n]);
                for (int colorIndex = 0; colorIndex < (colorSetsAsArrays[paletteRecursionStatus.colorSetIndex]).length; colorIndex++) {
                    if (!hardwarePalettesCopy[j].contains(Integer.valueOf(colorSetsAsArrays[paletteRecursionStatus.colorSetIndex][colorIndex])))
                        hardwarePalettesCopy[j].add(Integer.valueOf(colorSetsAsArrays[paletteRecursionStatus.colorSetIndex][colorIndex]));
                }
                if (hardwarePalettesCopy[j].size() <= graphicsFormat.getPaletteSize())
                    stack.push(new PaletteRecursionStatus((HashSet<Integer>[])hardwarePalettesCopy, paletteRecursionStatus.colorSetIndex + 1));
            }
        }
        return null;
    }

    public static BufferedImage getPaletteImage(ArrayList<Palette> palettes, GraphicFormat graphicFormat) {
        int width = 256;
        int height = palettes.size() * 32;
        int colorWidth = 256 / graphicFormat.getPaletteSize();
        int[] data = new int[width * height];
        for (int paletteNumber = 0; paletteNumber < palettes.size(); paletteNumber++) {
            int startY = paletteNumber * 32 * width;
            for (int paletteIndex = 0; paletteIndex < ((Palette)palettes.get(paletteNumber)).colors.length; paletteIndex++) {
                int startX = colorWidth * paletteIndex;
                for (int y = startY; y < startY + width * 32; y += width) {
                    for (int x = startX; x < startX + colorWidth; x++)
                        data[x + y] = graphicFormat.convertColorToRGB(((Palette)palettes.get(paletteNumber)).colors[paletteIndex]);
                }
            }
        }
        BufferedImage image = new BufferedImage(width, height, 2);
        int[] imgData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, imgData, 0, data.length);
        return image;
    }

    public static String getPaletteSummary(Tileset tileSet, GraphicFormat graphicFormat) throws UGTException {
        StringBuilder summary = new StringBuilder();
        ArrayList<Palette> palettes = tileSet.getPalettes();
        summary.append("Palettes Summary" + System.lineSeparator() + System.lineSeparator());
        summary.append("Palettes Type = " + tileSet.getPaletteType().toString() + System.lineSeparator());
        summary.append("Palettes Used = " + palettes.size() + System.lineSeparator() + System.lineSeparator());
        summary.append("Palette As Hardware Values (HEX)" + System.lineSeparator());
        int palette;
        for (palette = 0; palette < palettes.size(); palette++) {
            summary.append("(");
            if (((Palette)palettes.get(palette)).colors.length != 0) {
                for (int i = 0; i < ((Palette)palettes.get(palette)).colors.length - 1; i++) {
                    summary.append("$" + String.format("%02X", new Object[] { Integer.valueOf(((Palette)palettes.get(palette)).colors[i]) }));
                    summary.append(",");
                }
                summary.append("$" + String.format("%02X", new Object[] { Integer.valueOf(((Palette)palettes.get(palette)).colors[((Palette)palettes.get(palette)).colors.length - 1]) }));
            }
            summary.append(")");
            summary.append(System.lineSeparator());
        }
        summary.append(System.lineSeparator());
        summary.append("Palette As ARGB Values (HEX)" + System.lineSeparator());
        for (palette = 0; palette < palettes.size(); palette++) {
            summary.append("(");
            if (((Palette)palettes.get(palette)).colors.length != 0) {
                for (int i = 0; i < ((Palette)palettes.get(palette)).colors.length - 1; i++) {
                    summary.append("$" + String.format("%08X", new Object[] { Integer.valueOf(graphicFormat.convertColorToRGB(((Palette)palettes.get(palette)).colors[i])) }));
                    summary.append(",");
                }
                summary.append("$" + String.format("%08X", new Object[] { Integer.valueOf(graphicFormat.convertColorToRGB(((Palette)palettes.get(palette)).colors[((Palette)palettes.get(palette)).colors.length - 1])) }));
            }
            summary.append(")");
            summary.append(System.lineSeparator());
        }
        return summary.toString();
    }

    public static String getPaletteAsRGBString(Tileset tileSet, GraphicFormat graphicFormat) {
        StringBuilder summary = new StringBuilder();
        ArrayList<Palette> palettes = tileSet.getPalettes();
        for (int palette = 0; palette < palettes.size(); palette++) {
            summary.append("(");
            if (((Palette)palettes.get(palette)).colors.length != 0) {
                for (int i = 0; i < ((Palette)palettes.get(palette)).colors.length - 1; i++) {
                    if (i != 0 && i % 4 == 0)
                        summary.append(System.lineSeparator());
                    summary.append("0x" + String.format("%06X", new Object[] { Integer.valueOf(graphicFormat.convertColorToRGB(((Palette)palettes.get(palette)).colors[i]) & 0xC0C0C0) }));
                    summary.append(", ");
                }
                summary.append("0x" + String.format("%06X", new Object[] { Integer.valueOf(graphicFormat.convertColorToRGB(((Palette)palettes.get(palette)).colors[((Palette)palettes.get(palette)).colors.length - 1]) & 0xC0C0C0) }));
            }
            summary.append(")");
            summary.append(System.lineSeparator());
            summary.append(System.lineSeparator());
        }
        return summary.toString();
    }

    public static Palette[] processCustomPalette(String paletteText, GraphicFormat graphicFormat) throws UGTException {
        if (paletteText == null)
            return null;
        Palette[] palettes = null;
        paletteText = paletteText.replaceAll("[^\\d^,^(^a-f^A-F]", "");
        paletteText = paletteText.replaceAll("0x", "");
        String[] textSplit = paletteText.split("[(]");
        if (textSplit.length <= 1)
            throw new UGTException("Error: custom palette string is invalid!");
        palettes = new Palette[textSplit.length - 1];
        for (int paletteIndex = 1; paletteIndex < textSplit.length; paletteIndex++) {
            String[] paletteValues = textSplit[paletteIndex].split("[,]");
            if (paletteValues.length == 0 || paletteValues.length > graphicFormat.getPaletteSize())
                throw new UGTException("Error: custom palette string is invalid!");
            if (paletteValues.length == 1 && paletteValues[0].isEmpty()) {
                palettes[paletteIndex - 1] = new Palette(new int[0], graphicFormat);
            } else {
                int[] colors = new int[paletteValues.length];
                for (int i = 0; i < paletteValues.length; i++)
                    colors[i] = graphicFormat.convertRGBToHardwareColor((int)Long.parseLong(paletteValues[i].substring(1), 16));
                try {
                    palettes[paletteIndex - 1] = new Palette(colors, graphicFormat);
                } catch (Exception e) {
                    throw new UGTException("Error: custom palette string is invalid!");
                }
            }
        }
        return palettes;
    }

    public enum PaletteType {
        basic, optimized, custom;
    }

    public int[] getColors() {
        return this.colors;
    }

    public int[] getRgbColors() {
        return this.rgbColors;
    }

    public HashSet<Integer> getUniqueColors() {
        return this.uniqueColors;
    }

    public Integer[] getPaletteLookUp() {
        return this.paletteLookUp;
    }

    public int getAssociatedTilesCount() {
        return this.associatedTilesCount;
    }

    public void setAssociatedTilesCount(int associatedTilesCount) {
        this.associatedTilesCount = associatedTilesCount;
    }

    public void incrementAssociatedTileCount() {
        this.associatedTilesCount++;
    }
}
