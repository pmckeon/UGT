package psidum.ugt;

import com.beust.jcommander.JCommander;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.imageio.ImageIO;

import psidum.ugt.hardware.ParsedImage;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.model.MetatileSet;
import psidum.ugt.model.MetatileSetExport;
import psidum.ugt.model.Nametable;
import psidum.ugt.model.Palette;
import psidum.ugt.model.PaletteExportAsBlock;
import psidum.ugt.model.Scrolltable;
import psidum.ugt.model.ScrolltableExport;
import psidum.ugt.model.TiledFile;
import psidum.ugt.model.Tileset;
import psidum.ugt.model.TilesetExportAsBlock;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                            JOptionPane.showMessageDialog(null, throwable.getMessage(), "Exception Dialog", JOptionPane.WARNING_MESSAGE);
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            throwable.printStackTrace(pw);
                            String exceptionText = sw.toString();
                            JLabel label = new JLabel("The exception stacktrace was:");
                            JTextArea textArea = new JTextArea(exceptionText);
                            textArea.setEditable(false);
                            JScrollPane scrollPane = new JScrollPane();
                            scrollPane.setViewportView(textArea);
                            JPanel expContent = new JPanel(new GridBagLayout());
                            GridBagConstraints c = new GridBagConstraints();
                            c.fill = GridBagConstraints.HORIZONTAL;
                            c.gridx = 0;
                            c.gridy = 0;
                            expContent.add(label, c);
                            c.fill = GridBagConstraints.BOTH;
                            c.gridx = 0;
                            c.gridy = 1;
                            expContent.add(scrollPane, c);
                            JOptionPane.showMessageDialog(null, expContent);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    UGTMainForm form = new UGTMainForm();
                    form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    form.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            saveConfig();
                        }
                    });
                    form.setTitle("UGT by Psidum - v0.18");
                    form.setVisible(true);
                }
            });
        } else {
            RunCommandline(args);
        }
    }

    public static void RunCommandline(String[] args) {
        CustomArgs cArgs = new CustomArgs();
        JCommander.newBuilder()
                .addObject(cArgs)
                .build()
                .parse(args);
        String batchTiled = cArgs.getBatchTiled();
        String batchImage = cArgs.getBatchImage();
        String destination = cArgs.getDestination();
        String baseName = cArgs.getName();
        if ((batchTiled == null && batchImage == null) || destination == null || baseName == null) {
            System.out.println("Missing parameters. Must provide -tiledfile or -imagefill, -destination, -name");
            return;
        }
        GraphicFormatManager.Initialize();
        if (batchImage != null) {
            BufferedImage nametableBufferedImage;
            Tileset tileset;
            Nametable nametable;
            Scrolltable scrolltable;
            try {
                nametableBufferedImage = ImageIO.read(new File(batchImage));
            } catch (IOException e) {
                System.out.println("Error: file was not a valid image!");
                return;
            }
            if (nametableBufferedImage == null) {
                System.out.println("Error: file was not a valid image!");
                return;
            }
            try {
                ParsedImage nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.currentGraphicFormat);
                tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, nametableParsedImage);
                tileset.setPaletteType(Palette.PaletteType.optimized);
                tileset.setPreserveFormation(true);
                tileset.setTileHeight(8);
                tileset.setTileWidth(8);
                tileset.setUniqueTiles(true);
                tileset.setVerticalMirroring(true);
                tileset.setHorizontalMirroring(true);
                tileset.update(true);
                tileset.createIDHashMap();
                nametable = new Nametable(nametableParsedImage, tileset);
            } catch (UGTException e) {
                System.out.println(e.getMessage());
                return;
            }
            try {
                scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, nametable, tileset, 2, 2);
                MetatileSet metatileSet = scrolltable.getMetatileSet();
            } catch (UGTException e) {
                System.out.println(e.getMessage());
                return;
            }
            byte[] data = null;
            try {
                data = ((TilesetExportAsBlock) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(tileset.getTiles());
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_tiles.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((PaletteExportAsBlock) GraphicFormatManager.currentGraphicFormat).exportPalettesAsBinaryBlock(tileset.getPalettes());
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_palette.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((ScrolltableExport) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(scrolltable);
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_scrolltable.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((MetatileSetExport) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(scrolltable.getMetatileSet());
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_metatiles.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        if ((((batchImage == null) ? 1 : 0) & ((batchTiled != null) ? 1 : 0)) != 0) {
            Tileset tileset;
            Nametable nametable;
            Scrolltable scrolltable;
            try {
                TiledFile tiledFile = new TiledFile(new File(batchTiled));
                if (tiledFile == null)
                    return;
                BufferedImage tilesetBufferedImage = tiledFile.getTileImage();
                ParsedImage tilesetParsedImage = new ParsedImage(tilesetBufferedImage, GraphicFormatManager.currentGraphicFormat);
                tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, tilesetParsedImage);
                tileset.setPreserveFormation(true);
                tileset.setTileHeight(8);
                tileset.setTileWidth(8);
                tileset.setUniqueTiles(false);
                tileset.setVerticalMirroring(true);
                tileset.setHorizontalMirroring(true);
                tileset.update(true);
                tileset.createIDHashMap();
                BufferedImage nametableBufferedImage = tiledFile.getFullImage(tileset);
                ParsedImage nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.currentGraphicFormat);
                nametable = new Nametable(nametableParsedImage, tileset, tiledFile);
            } catch (UGTException e) {
                System.out.println(e.getMessage());
                return;
            }
            try {
                scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, nametable, tileset, 2, 2);
                MetatileSet metatileSet = scrolltable.getMetatileSet();
            } catch (UGTException e) {
                System.out.println(e.getMessage());
                return;
            }
            byte[] data = null;
            try {
                data = ((TilesetExportAsBlock) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(tileset.getTiles());
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_tiles.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((PaletteExportAsBlock) GraphicFormatManager.currentGraphicFormat).exportPalettesAsBinaryBlock(tileset.getPalettes());
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_palette.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((ScrolltableExport) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(scrolltable);
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_scrolltable.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            data = ((MetatileSetExport) GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(scrolltable.getMetatileSet());
            try {
                DataOutputStream stream = new DataOutputStream(Files.newOutputStream(Paths.get(destination, baseName + "_metatiles.bin")));
                stream.write(data);
                stream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    public static void saveConfig() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = Files.newOutputStream(Paths.get("config.properties"));
            prop.setProperty("SMSMode4_ByteLineAffix", GraphicFormatManager.graphicFormats[0][0].getByteLineAffix());
            prop.setProperty("SMSMode4_ByteValueAffix", GraphicFormatManager.graphicFormats[0][0].getByteValueAffix());
            prop.setProperty("SMSMode4_WordLineAffix", GraphicFormatManager.graphicFormats[0][0].getWordLineAffix());
            prop.setProperty("SMSMode4_WordValueAffix", GraphicFormatManager.graphicFormats[0][0].getWordValueAffix());
            prop.setProperty("SMSMode4_TransparentPixel", String.valueOf(GraphicFormatManager.graphicFormats[0][0].getTransparentPixel()));
            prop.setProperty("SMSMode4_EmptyTileColor", String.valueOf(GraphicFormatManager.graphicFormats[0][0].getEmptyTileColor()));
            prop.setProperty("UseNativeLookAndFeel", String.valueOf(UGTMainForm.useNativeLookAndFeel));
            prop.store(output, (String) null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}

