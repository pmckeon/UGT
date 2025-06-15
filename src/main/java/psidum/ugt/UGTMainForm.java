/*
 * Created by JFormDesigner on Fri Jun 06 00:18:46 AEST 2025
 */

package psidum.ugt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.ParsedImage;
import psidum.ugt.hardware.SegaSMSMode4;
import psidum.ugt.hardware.UGTException;
import psidum.ugt.model.MetatileSet;
import psidum.ugt.model.MetatileSetExport;
import psidum.ugt.model.Nametable;
import psidum.ugt.model.NametableExportAsBlock;
import psidum.ugt.model.Palette;
import psidum.ugt.model.PaletteExportAsBlock;
import psidum.ugt.model.Scrolltable;
import psidum.ugt.model.ScrolltableExport;
import psidum.ugt.model.Spritetable;
import psidum.ugt.model.Tile;
import psidum.ugt.model.TiledFile;
import psidum.ugt.model.TiledMetaData;
import psidum.ugt.model.Tileset;
import psidum.ugt.model.TilesetExportAsBlock;
import psidum.ugt.util.GeneralUtil;

/**
 * @author pmcke
 */
public class UGTMainForm extends JFrame {
    BufferedImage nametableBufferedImage;

    BufferedImage spritetableBufferedImage;

    BufferedImage scrolltableBufferedImage;

    BufferedImage tilesetBufferedImage;

    ParsedImage nametableParsedImage;

    ParsedImage spritetableParsedImage;

    ParsedImage scrolltableParsedImage;

    ParsedImage tilesetParsedImage;

    TiledMetaData tiledMetaData;

    Spritetable spritetable;

    Scrolltable scrolltable;

    String tiledFilePath;

    int tileWidth;

    int tileHeight;

    String previousDirectory;

    JFileChooser fileChooser;

    ArrayList<String> nametableRecentDir;

    ArrayList<String> nametableRecentName;

    ArrayList<String> tilesetRecentDir;

    ArrayList<String> tilesetRecentName;

    TiledFile tiledFile;

    Nametable nametable;

    Tileset tileset;

    final File folder;

    final int emptyBarrelScanline = -65306;

    int[][] colorTable;

    public static boolean useNativeLookAndFeel;

    public UGTMainForm() {
        initComponents();

        this.tiledFilePath = "";
        this.tileWidth = 0;
        this.tileHeight = 0;
        this.previousDirectory = null;
        this.fileChooser = new JFileChooser();
        this.nametableRecentDir = new ArrayList<>();
        this.nametableRecentName = new ArrayList<>();
        this.tilesetRecentDir = new ArrayList<>();
        this.tilesetRecentName = new ArrayList<>();
        this.tiledFile = null;
        this.nametable = null;
        this.tileset = null;
        this.folder = new File("C:/tmp");
        this.colorTable = new int[][] {
                { 16, 16 }, { 16, 17 }, { 17, 17 }, { 17, 18 }, { 18, 18 }, { 18, 22 }, { 22, 22 }, { 22, 22 }, { 16, 16 }, { 0, 16 },
                new int[2], new int[2] };

        // Initialize Begin
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            GraphicFormatManager.Initialize(prop);
            useNativeLookAndFeel = Boolean.parseBoolean(prop.getProperty("UseNativeLookAndFeel", "false"));
            cbMenuChangeLookAndFeel.setState(useNativeLookAndFeel);
            setNativeLookAndFeel(useNativeLookAndFeel);
        } catch (Exception e) {
            GraphicFormatManager.Initialize();
            useNativeLookAndFeel = false;
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException iOException) {}
        }
        ButtonGroup rdoPaletteOptions = new ButtonGroup();
        rdoPaletteOptions.add(this.rdbSinglePalette);
        rdoPaletteOptions.add(this.rdbOptimizedPalettes);
        rdoPaletteOptions.add(this.rdbCustomPalette);
        DocumentFilter numericFilter = new DocumentFilter() {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };

        ((AbstractDocument)this.txtTileWidth.getDocument()).setDocumentFilter(numericFilter);
        ((AbstractDocument)this.txtTileHeight.getDocument()).setDocumentFilter(numericFilter);
        this.lblGraphicFormat.setText(GraphicFormatManager.currentGraphicFormat.getUIDescriptionString());
        updateGraphicFormatSpecificFields();
        // Initialize End
    }

    void clearException() {
        this.lblError.setText("");
    }

    void nullifyCoreFields() {
        this.nametable = null;
        this.tileset = null;
        this.spritetable = null;
        this.scrolltable = null;
        this.nametableBufferedImage = null;
        this.spritetableBufferedImage = null;
        this.scrolltableBufferedImage = null;
        this.tilesetBufferedImage = null;
        this.tiledMetaData = null;
        this.nametableParsedImage = null;
        this.spritetableParsedImage = null;
        this.scrolltableParsedImage = null;
        this.tilesetParsedImage = null;
    }

    void updateGraphicFormatSpecificFields() {
        this.txtTileLayout.setText(GraphicFormatManager.currentGraphicFormat.getTileFormationString());
        this.txtTileHeight.setText("8");
        this.txtTileWidth.setText("8");
        this.cbxTileRemoveDuplicate.setSelected(true);
        this.cbxTileVerticalFlip.setSelected(true);
        this.cbxTileHorizontalFlip.setSelected(true);
    }

    void updateUIForException() {
        this.nametable = null;
        this.tileset = null;
        this.spritetable = null;
        this.scrolltable = null;
        this.imgNametable.setIcon(null);
        this.imgPalettes.setIcon(null);
    }

    void updateUI() {
        if (this.nametableParsedImage != null)
            this.imgNametable.setIcon(new ImageIcon(this.nametableParsedImage.getSystemBufferedImage().getScaledInstance(imgNametable.getWidth(), imgNametable.getHeight(), Image.SCALE_REPLICATE)));
        if (this.tileset != null)
            this.imgTileset.setIcon(new ImageIcon(GeneralUtil.resample(this.tileset.getTileImage(), 2)));
        this.imgPalettes.setIcon(new ImageIcon(Palette.getPaletteImage(this.tileset.getPalettes(), GraphicFormatManager.currentGraphicFormat)));
        this.txtCustomPalette.setText(Palette.getPaletteAsRGBString(this.tileset, GraphicFormatManager.currentGraphicFormat));
        if (!this.txtTileWidth.getText().equals("8") || !this.txtTileHeight.getText().equals("8")) {
            this.txtMetatileSummary.setText("tile dimensions must be 8*8 for scrolltable!");
            this.scrolltable = null;
        } else {
            try {
                this.scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, this.nametable, this.tileset, 2, 2);
                this.txtMetatileSummary.setText(this.scrolltable.getMetatileSet().getSummary());
            } catch (UGTException e) {
                this.txtMetatileSummary.setText(e.getMessage());
            }
        }
        StringBuilder summary = new StringBuilder();
        summary.append("Dimensions: " + this.nametableParsedImage.getWidth() + " * " + this.nametableParsedImage.getHeight() + System.lineSeparator());
        summary.append("Tile Count: " + this.tileset.getTiles().size() + System.lineSeparator());
        summary.append("Palette Count: " + this.tileset.getPalettes().size() + System.lineSeparator());
        this.txtNametableSummary.setText(summary.toString());
        if (this.scrolltable != null) {
            this.imgMetatiles.setIcon(new ImageIcon(this.scrolltable.getMetatileSet().getMetatileImage()));
        } else {
            this.imgMetatiles.setIcon(null);
        }
    }

    boolean validateUIFields() {
        if (!this.txtTileWidth.getText().matches("\\d*") || !this.txtTileHeight.getText().matches("\\d*")) {
            this.lblError.setText("Error: tile dimensions are invalid!");
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        this.tileWidth = Integer.parseInt(this.txtTileWidth.getText());
        this.tileHeight = Integer.parseInt(this.txtTileHeight.getText());
        if (this.tileWidth == 0 || this.tileHeight == 0) {
            this.lblError.setText("Error: tile dimensions are invalid!");
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        if (this.nametableBufferedImage != null && (this.nametableBufferedImage.getWidth() % this.tileWidth != 0 || this.nametableBufferedImage.getHeight() % this.tileHeight != 0)) {
            this.lblError.setText("Error: Image Dimensions need to be exact multiples of Tile Dimensions!");
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return true;
    }

    private void update(ActionEvent event) {
        if (this.nametableBufferedImage == null)
            return;
        update(false);
    }

    void update(boolean isImport) {
        this.lblError.setText("");
        if (!validateUIFields()) {
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (!updateTileset(isImport)) {
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (!updateNametable()) {
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        updateUI();
    }

    boolean updateNametable() {
        try {
            if (this.tiledFile == null) {
                this.nametable = new Nametable(this.nametableParsedImage, this.tileset);
            } else {
                this.nametable = new Nametable(this.nametableParsedImage, this.tileset, this.tiledFile);
            }
        } catch (UGTException e) {
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return true;
    }

    boolean updateTileset(boolean isImport) {
        if (this.cbxTileUseSuperImage.isSelected()) {
            this.tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, this.nametableParsedImage);
        } else {
            if (this.tilesetParsedImage == null) {
                this.lblError.setText("Error: 'use same image as nametable /scroll' option unchecked but no sprite image loaded!");
                return false;
            }
            this.tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, this.tilesetParsedImage);
        }
        this.tileset.setPaletteFormation(this.txtCustomPalette.getText());
        this.tileset.setVramTileFormation(this.txtTileLayout.getText());
        this.tileset.setPreserveFormation(true);
        this.tileset.setTileHeight(this.tileHeight);
        this.tileset.setTileWidth(this.tileWidth);
        this.tileset.setUniqueTiles(this.cbxTileRemoveDuplicate.isSelected());
        this.tileset.setVerticalMirroring(this.cbxTileVerticalFlip.isSelected());
        this.tileset.setHorizontalMirroring(this.cbxTileHorizontalFlip.isSelected());
        if (this.rdbSinglePalette.isSelected())
            this.tileset.setPaletteType(Palette.PaletteType.basic);
        if (this.rdbOptimizedPalettes.isSelected())
            this.tileset.setPaletteType(Palette.PaletteType.optimized);
        if (this.rdbCustomPalette.isSelected())
            this.tileset.setPaletteType(Palette.PaletteType.custom);
        try {
            this.tileset.update(isImport);
        } catch (UGTException e) {
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return true;
    }

    private void menuChangeToMode0(ActionEvent event) {
        GraphicFormatManager.currentGraphicFormat = GraphicFormatManager.graphicFormats[0][1];
    }

    private void menuChangeToMode4(ActionEvent event) {
        GraphicFormatManager.currentGraphicFormat = GraphicFormatManager.graphicFormats[0][0];
    }

    private void menu_OpenImageFile(ActionEvent event) {
        updateGraphicFormatSpecificFields();
        validateUIFields();
        TiledFile tiledFile = null;
        BufferedImage tilesetBufferedImage = null;
        ParsedImage tilesetParsedImage = null;
        Tileset tileset = null;
        BufferedImage nametableBufferedImage = null;
        ParsedImage nametableParsedImage = null;
        Nametable nametable = null;
        Scrolltable scrolltable = null;
        MetatileSet metatileSet = null;
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Open Image File");
        if(this.fileChooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File imageFile = this.fileChooser.getSelectedFile();
        if (imageFile == null)
            return;
        this.fileChooser.setCurrentDirectory(imageFile.getParentFile());
        try {
            nametableBufferedImage = ImageIO.read(new File(imageFile.getAbsolutePath()));
        } catch (IOException e) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (nametableBufferedImage == null) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.currentGraphicFormat);
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
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (!this.txtTileWidth.getText().equals("8") || !this.txtTileHeight.getText().equals("8")) {
            this.txtMetatileSummary.setText("tile dimensions must be 8*8 for scrolltable!");
        } else {
            try {
                scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, nametable, tileset, 2, 2);
                metatileSet = scrolltable.getMetatileSet();
                this.txtMetatileSummary.setText(scrolltable.getMetatileSet().getSummary());
            } catch (UGTException e) {
                this.txtMetatileSummary.setText(e.getMessage());
            }
        }
        this.cbxTileUseSuperImage.setSelected(true);
        this.tiledFile = tiledFile;
        this.tilesetParsedImage = tilesetParsedImage;
        this.tilesetBufferedImage = tilesetBufferedImage;
        this.tileset = tileset;
        this.nametableBufferedImage = nametableBufferedImage;
        this.nametableParsedImage = nametableParsedImage;
        this.nametable = nametable;
        this.scrolltable = scrolltable;
        this.cbxTileUseSuperImage.setSelected(true);
        this.rdbOptimizedPalettes.setSelected(true);
        clearException();
        updateUI();
    }

    private void menu_OpenTilesetImage(ActionEvent event) {
        updateGraphicFormatSpecificFields();
        validateUIFields();
        TiledFile tiledFile = null;
        BufferedImage tilesetBufferedImage = null;
        ParsedImage tilesetParsedImage = null;
        Tileset tileset = null;
        BufferedImage nametableBufferedImage = null;
        ParsedImage nametableParsedImage = null;
        Nametable nametable = null;
        Scrolltable scrolltable = null;
        MetatileSet metatileSet = null;
        if (this.nametableBufferedImage == null) {
            this.lblError.setText("Error: you must load a nametable image before loading a custom tileset image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Open Tile Image");
        if(this.fileChooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File imageFile = this.fileChooser.getSelectedFile();
        if (imageFile == null)
            return;
        this.fileChooser.setCurrentDirectory(imageFile.getParentFile());
        try {
            tilesetBufferedImage = ImageIO.read(new File(imageFile.getAbsolutePath()));
        } catch (IOException e) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            tilesetParsedImage = new ParsedImage(tilesetBufferedImage, GraphicFormatManager.currentGraphicFormat);
            tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, tilesetParsedImage);
            tileset.setPaletteFormation(this.txtCustomPalette.getText());
            tileset.setPreserveFormation(true);
            tileset.setTileHeight(this.tileHeight);
            tileset.setTileWidth(this.tileWidth);
            tileset.setUniqueTiles(this.cbxTileRemoveDuplicate.isSelected());
            tileset.setVerticalMirroring(this.cbxTileVerticalFlip.isSelected());
            tileset.setHorizontalMirroring(this.cbxTileHorizontalFlip.isSelected());
            if (this.rdbSinglePalette.isSelected())
                tileset.setPaletteType(Palette.PaletteType.basic);
            if (this.rdbOptimizedPalettes.isSelected())
                tileset.setPaletteType(Palette.PaletteType.optimized);
            if (this.rdbCustomPalette.isSelected())
                tileset.setPaletteType(Palette.PaletteType.custom);
            tileset.update(true);
            tileset.createIDHashMap();
            nametableBufferedImage = this.nametableBufferedImage;
            nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.currentGraphicFormat);
            nametable = new Nametable(nametableParsedImage, tileset);
        } catch (UGTException e) {
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, nametable, tileset, 2, 2);
            metatileSet = scrolltable.getMetatileSet();
            this.txtMetatileSummary.setText(scrolltable.getMetatileSet().getSummary());
        } catch (UGTException e) {
            this.txtMetatileSummary.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        }
        this.tiledFile = tiledFile;
        this.tilesetParsedImage = tilesetParsedImage;
        this.tilesetBufferedImage = tilesetBufferedImage;
        this.tileset = tileset;
        this.nametableBufferedImage = nametableBufferedImage;
        this.nametableParsedImage = nametableParsedImage;
        this.nametable = nametable;
        this.scrolltable = scrolltable;
        this.cbxTileUseSuperImage.setSelected(false);
        updateUI();
        clearException();
        this.txtTileLayout.setText(tileset.getVramTileFormation());
    }

    private void menu_OpenTiledFile(ActionEvent event) {
        updateGraphicFormatSpecificFields();
        validateUIFields();
        BufferedImage tilesetBufferedImage = null;
        ParsedImage tilesetParsedImage = null;
        Tileset tileset = null;
        BufferedImage nametableBufferedImage = null;
        ParsedImage nametableParsedImage = null;
        Nametable nametable = null;
        Scrolltable scrolltable = null;
        MetatileSet metatileSet = null;
        try {
            this.tiledFile = TiledFile.openTiledFile((JFrame) SwingUtilities.getWindowAncestor(this.btnUpdate), this.fileChooser);
            if (this.tiledFile == null)
                return;
            tilesetBufferedImage = this.tiledFile.getTileImage();
            tilesetParsedImage = new ParsedImage(tilesetBufferedImage, GraphicFormatManager.currentGraphicFormat);
            tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, tilesetParsedImage);
            tileset.setPreserveFormation(true);
            tileset.setTileHeight(8);
            tileset.setTileWidth(8);
            tileset.setUniqueTiles(false);
            tileset.setVerticalMirroring(true);
            tileset.setHorizontalMirroring(true);
            tileset.update(true);
            tileset.createIDHashMap();
            nametableBufferedImage = this.tiledFile.getFullImage(tileset);
            nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.currentGraphicFormat);
            nametable = new Nametable(nametableParsedImage, tileset, this.tiledFile);
        } catch (UGTException e) {
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            scrolltable = new Scrolltable(GraphicFormatManager.currentGraphicFormat, nametable, tileset, 2, 2);
            metatileSet = scrolltable.getMetatileSet();
            this.txtMetatileSummary.setText(scrolltable.getMetatileSet().getSummary());
        } catch (UGTException e) {
            this.txtMetatileSummary.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        }
        this.tiledFile = this.tiledFile;
        this.tilesetParsedImage = tilesetParsedImage;
        this.tilesetBufferedImage = tilesetBufferedImage;
        this.tileset = tileset;
        this.nametableBufferedImage = nametableBufferedImage;
        this.nametableParsedImage = nametableParsedImage;
        this.nametable = nametable;
        this.scrolltable = scrolltable;
        this.cbxTileUseSuperImage.setSelected(false);
        clearException();
        updateUI();
    }

    private void menu_SaveNametable(ActionEvent event) {
        OutputStreamWriter writer;
        byte[] data;
        if (this.nametable == null) {
            this.lblError.setText("Error: no nametable to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Save Nametable");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("binary (*.bin)", "*.bin" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png file (*.png)", "*.png" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        switch (this.fileChooser.getFileFilter().getDescription()) {
            case "include (*.inc)":
                writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
                    writer.write(((NametableExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.nametable));
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                break;
            case "png file (*.png)":
                try {
                    ImageIO.write(this.nametableBufferedImage, "png", file);
                } catch (IOException e) {
                    this.lblError.setText("Error: image could not be saved! " + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
            case "binary (*.bin)":
                data = ((NametableExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.nametable);
                try {
                    DataOutputStream stream = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                    stream.write(data);
                    stream.close();
                } catch (IOException iOException) {
                    this.lblError.setText("Error: file could not be saved!" + iOException.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
        }
        clearException();
    }

    private void menu_SaveTiles(ActionEvent event) {
        OutputStreamWriter writer;
        if (this.tileset == null) {
            this.lblError.setText("Error: no tileset to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Save Tiles");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("binary (*.bin)", "*.bin" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png file (*.png)", "*.png" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        switch (this.fileChooser.getFileFilter().getDescription()) {
            case "include (*.inc)":
                writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
                    writer.write(((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.tileset.getTiles()));
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                break;
            case "png file (*.png)":
                try {
                    ImageIO.write(this.tileset.getTileImage(), "png", file);
                } catch (IOException e) {
                    this.lblError.setText("Error: image could not be saved! " + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                }
                break;
            case "binary (*.bin)":
                try {
                    byte[] data = ((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.tileset.getTiles());
                    DataOutputStream stream = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                    stream.write(data);
                    stream.close();
                } catch (IOException iOException) {
                    System.out.println(iOException.getMessage());
                    this.lblError.setText("Error: file could not be saved!" + iOException.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
        }
        clearException();
    }

    private void menu_SavePalette(ActionEvent event) {
        OutputStreamWriter writer;
        byte[] data;
        if (this.tileset == null) {
            this.lblError.setText("Error: no palette to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Save Palette");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("binary (*.bin)", "*.bin" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        switch (this.fileChooser.getFileFilter().getDescription()) {
            case "include (*.inc)":
                writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
                    writer.write(((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsHexBlock(this.tileset.getPalettes()));
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                break;
            case "binary (*.bin)":
                data = ((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsBinaryBlock(this.tileset.getPalettes());
                try {
                    DataOutputStream stream = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                    stream.write(data);
                    stream.close();
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
        }
        clearException();
    }

    private void menu_SaveScrolltable(ActionEvent event) {
        OutputStreamWriter writer;
        byte[] data;
        if (this.scrolltable == null) {
            this.lblError.setText("Error: no scrolltable to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Save scrolltable");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("binary (*.bin)", "*.bin" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        switch (this.fileChooser.getFileFilter().getDescription()) {
            case "include (*.inc)":
                writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
                    writer.write(((ScrolltableExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable));
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                break;
            case "binary (*.bin)":
                data = ((ScrolltableExport)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.scrolltable);
                try {
                    DataOutputStream stream = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                    stream.write(data);
                    stream.close();
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
        }
        clearException();
    }

    private void menu_SaveMetatiles(ActionEvent event) {
        OutputStreamWriter writer;
        byte[] data;
        if (this.scrolltable == null) {
            this.lblError.setText("Error: no metatiles to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Save Metatiles");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("binary (*.bin)", "*.bin" ));
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png file (*.png)", "*.png" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        String str;
        switch ((str = this.fileChooser.getFileFilter().getDescription())) {
            case "include (*.inc)":
                writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
                    writer.write(((MetatileSetExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable.getMetatileSet()));
                } catch (IOException e) {
                    this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                break;
            case "png file (*.png)":
                try {
                    ImageIO.write(GeneralUtil.resample(this.scrolltable.getMetatileSet().getMetatileImage(), 0), "png", file);
                } catch (IOException e) {
                    this.lblError.setText("Error: image could not be saved! " + e.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                }
                break;
            case "binary (*.bin)":
                data = ((MetatileSetExport)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.scrolltable.getMetatileSet());
                try {
                    DataOutputStream stream = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile()));
                    stream.write(data);
                    stream.close();
                } catch (IOException iOException) {
                    this.lblError.setText("Error: file could not be saved!" + iOException.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                break;
        }
        clearException();
    }

    private void menu_SaveTiledFile(ActionEvent event) {
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File("*.png"));
        this.fileChooser.setDialogTitle("Step 1 of 2: Save Tile Image");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png file (*.png)", "*.png" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        BufferedImage tilesetImage = this.tileset.getTileImage();
        if (file == null)
            return;
        try {
            ImageIO.write(this.tileset.getTileImage(), "png", file);
        } catch (IOException e) {
            this.lblError.setText("Error: could not save tile image!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (this.tiledFile == null) {
            try {
                this.tiledFile = new TiledFile(this.nametable, file, this.tileset.getTileImage());
            } catch (UGTException e) {
                this.lblError.setText(e.getMessage());
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        } else {
            try {
                this.tiledFile.setTileImage(file, this.tileset.getTileImage());
            } catch (UGTException e) {
                this.lblError.setText(e.getMessage());
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File("*.tmx"));
        this.fileChooser.setDialogTitle("Step 2 of 2: Save Nametable As");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("tiled map format", "*.tmx" ));
        file = new File(file.getAbsolutePath());
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            DOMSource source = new DOMSource(this.tiledFile.getXmlDoc());
            StreamResult streamResult = new StreamResult(new File(file.getAbsolutePath()));
            transformer.transform(source, streamResult);
        } catch (Exception e) {
            this.lblError.setText("Error: could not save Tiled Document!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            String path = file.getParent();
            file = new File(String.valueOf(path) + "/GSLMetaFont.png");
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream input = classLoader.getResourceAsStream("GSLMetaFont.png");
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            OutputStream outStream = new FileOutputStream(file);
            outStream.write(buffer);
        } catch (IOException e) {
            this.lblError.setText("Error: could not write Meta Image!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        clearException();
    }

    private void menu_BatchExportGSLib(ActionEvent event) {
        if (this.scrolltable == null) {
            this.lblError.setText("Error: no scrolltable to save!");
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        String message = "Warning, this will overwrite existing files!\n\nEnter base name for files:";
        String baseName = JOptionPane.showInputDialog(null, message, "Batch Export for GSLib", JOptionPane.QUESTION_MESSAGE);
        if ((baseName == null) || (baseName.isEmpty()))
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle("Select Folder for Batch Export");
        chooser.setCurrentDirectory(this.fileChooser.getCurrentDirectory());
        if(chooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedDirectory = chooser.getSelectedFile();
        byte[] data = null;
        try {
            data = ((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.tileset.getTiles());
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(selectedDirectory.getAbsolutePath() + "/" + baseName + "_tiles.bin"));
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        data = ((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsBinaryBlock(this.tileset.getPalettes());
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(String.valueOf(selectedDirectory.getAbsolutePath()) + "/" + baseName + "_palette.bin"));
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        data = ((ScrolltableExport)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.scrolltable);
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(String.valueOf(selectedDirectory.getAbsolutePath()) + "/" + baseName + "_scrolltable.bin"));
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        data = ((MetatileSetExport)GraphicFormatManager.currentGraphicFormat).exportAsBinaryBlock(this.scrolltable.getMetatileSet());
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(String.valueOf(selectedDirectory.getAbsolutePath()) + "/" + baseName + "_metatiles.bin"));
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
    }

    private void menu_ToClipboardNametable(ActionEvent event) {
        if (this.nametable == null)
            return;
        String output = ((NametableExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.nametable);
        StringSelection stringSelection = new StringSelection(output);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_ToClipboardTiles(ActionEvent event) {
        if (this.nametable == null)
            return;
        String output = ((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.tileset.getTiles());
        StringSelection stringSelection = new StringSelection(output);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_ToClipboardPalette(ActionEvent event) {
        if (this.nametable == null)
            return;
        String output = ((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsHexBlock(this.tileset.getPalettes());
        StringSelection stringSelection = new StringSelection(output);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_ToClipboardScrolltable(ActionEvent event) {
        if (this.scrolltable == null)
            return;
        String output = ((ScrolltableExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable);
        StringSelection stringSelection = new StringSelection(output);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_ToClipboardMetatiles(ActionEvent event) {
        if (this.scrolltable == null)
            return;
        String output = ((MetatileSetExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable.getMetatileSet());
        StringSelection stringSelection = new StringSelection(output);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_QuickSaveNametable(ActionEvent event) {
        if (this.nametable == null)
            return;
        String message = "Enter title for data segments.\n\nTitle (no spaces):";
        String titleName = JOptionPane.showInputDialog(null, message, "", JOptionPane.QUESTION_MESSAGE);
        if ((titleName == null) || (titleName.isEmpty()))
            return;
        StringBuilder output = new StringBuilder();
        output.append(".section \"" + titleName + " Nametable Data\" superfree" + System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Nametable:" + System.lineSeparator());
        output.append(((NametableExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.nametable));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_NametableEnd:" + System.lineSeparator());
        output.append(".ends" + System.lineSeparator());
        output.append(System.lineSeparator());
        output.append(".section \"" + titleName + " Tile Data\" superfree" + System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Tiles:" + System.lineSeparator());
        output.append(((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.tileset.getTiles()));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_TilesEnd:" + System.lineSeparator());
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Palette:" + System.lineSeparator());
        output.append(((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsHexBlock(this.tileset.getPalettes()));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_PaletteEnd:" + System.lineSeparator());
        output.append(".ends" + System.lineSeparator());
        output.append(System.lineSeparator());
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Quick Save Nametable");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
            writer.write(output.toString());
        } catch (IOException e) {
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }
        clearException();
    }

    private void menu_QuickSaveScrolltable(ActionEvent event) {
        if (this.scrolltable == null)
            return;
        String message = "Enter title for data segments.\n\nTitle (no spaces):";
        String titleName = JOptionPane.showInputDialog(null, message, "", JOptionPane.QUESTION_MESSAGE);
        if ((titleName == null) || (titleName.isEmpty()))
            return;
        StringBuilder output = new StringBuilder();
        output.append(".section \"" + titleName + " Scrolltable Data\" superfree" + System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Scrolltable:" + System.lineSeparator());
        output.append(((ScrolltableExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_ScrolltableEnd:" + System.lineSeparator());
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Metatiles:" + System.lineSeparator());
        output.append(((MetatileSetExport)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.scrolltable.getMetatileSet()));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_MetatilesEnd:" + System.lineSeparator());
        output.append(".ends" + System.lineSeparator());
        output.append(System.lineSeparator());
        output.append(".section \"" + titleName + " Tile Data\" superfree" + System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Tiles:" + System.lineSeparator());
        output.append(((TilesetExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportAsHexBlock(this.tileset.getTiles()));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_TilesEnd:" + System.lineSeparator());
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_Palette:" + System.lineSeparator());
        output.append(((PaletteExportAsBlock)GraphicFormatManager.currentGraphicFormat).exportPalettesAsHexBlock(this.tileset.getPalettes()));
        output.append(System.lineSeparator());
        output.append(String.valueOf(titleName) + "_PaletteEnd:" + System.lineSeparator());
        output.append(".ends" + System.lineSeparator());
        output.append(System.lineSeparator());
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Quick Save Scrolltable");
        this.fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("include (*.inc)", "*.inc" ));
        if(this.fileChooser.showSaveDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = this.fileChooser.getSelectedFile();
        if (file == null)
            return;
        this.fileChooser.setCurrentDirectory(file.getParentFile());
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.US_ASCII);
            writer.write(output.toString());
        } catch (IOException e) {
            this.lblError.setText("Error: file could not be saved!" + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                this.lblError.setText("Error: file could not be saved!" + e.getMessage());
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }
        clearException();
    }

    private void menu_SMSMulticolorPacked(ActionEvent event) {
        GraphicFormat mode0 = GraphicFormatManager.graphicFormats[0][1];
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Open Image File");
        if(this.fileChooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File imageFile = this.fileChooser.getSelectedFile();
        this.fileChooser.setCurrentDirectory(imageFile.getParentFile());
        try {
            this.nametableBufferedImage = ImageIO.read(new File(imageFile.getAbsolutePath()));
        } catch (IOException e) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (this.nametableBufferedImage == null) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        ParsedImage parsedImage = new ParsedImage(this.nametableBufferedImage, mode0);
        int[] packedPixels = new int[(parsedImage.getMasterPalettePixels()).length];
        for (int i = 0; i < (parsedImage.getMasterPalettePixels()).length; i++) {
            int pixel = parsedImage.getMasterPalettePixels()[i];
            pixel |= pixel << 4;
            packedPixels[i] = pixel;
        }
        StringBuilder output = new StringBuilder();
        for (int j = 0; j < packedPixels.length; j++) {
            if (j % 128 == 0) {
                output.append(String.valueOf(System.lineSeparator()) + ".db $" + String.format("%02X", new Object[] { Integer.valueOf(packedPixels[j]) }));
            } else {
                output.append(", $" + String.format("%02X", new Object[] { Integer.valueOf(packedPixels[j]) }));
            }
        }
        StringSelection stringSelection = new StringSelection(output.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        clearException();
    }

    private void menu_BarrelAnimation(ActionEvent event) {
        StringBuilder output = new StringBuilder();
        ArrayList<Integer> scanlinePixels = new ArrayList<>();
        ArrayList<Byte> data = new ArrayList<>();
        int[] frameColors = new int[96];
        int[] frameXLoc = new int[96];
        int page = 1;
        int frame = 0;
        byte b;
        int i;
        File[] arrayOfFile;
        for (i = (arrayOfFile = this.folder.listFiles()).length, b = 0; b < i; ) {
            File fileEntry = arrayOfFile[b];
            if (!fileEntry.isDirectory()) {
                BufferedImage image;
                try {
                    image = ImageIO.read(fileEntry);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                for (int j = 0; j < frameXLoc.length; ) {
                    frameXLoc[j] = -1;
                    j++;
                }
                for (int y = 0; y < 96; y++) {
                    scanlinePixels.clear();
                    float r = -1.0F;
                    float g = -1.0F;
                    float f1 = -1.0F;
                    int grey = 0;
                    for (int x = 0; x < 248; x++) {
                        int c = image.getRGB(x, y);
                        if (c != -65306)
                            scanlinePixels.add(Integer.valueOf(c));
                        if (c != -65306 && frameXLoc[y] == -1)
                            frameXLoc[y] = x;
                    }
                    if (scanlinePixels.size() > 0) {
                        for (int m = 0; m < scanlinePixels.size(); m++) {
                            r += ((((Integer)scanlinePixels.get(m)).intValue() & 0xFF0000) >> 16);
                            g += ((((Integer)scanlinePixels.get(m)).intValue() & 0xFF00) >> 8);
                            f1 += (((Integer)scanlinePixels.get(m)).intValue() & 0xFF);
                        }
                        r /= scanlinePixels.size();
                        g /= scanlinePixels.size();
                        f1 /= scanlinePixels.size();
                        grey = (int)(Math.round(r * 0.2989D + g * 0.587D + f1 * 0.114D) & 0xFFL);
                    }
                    if (scanlinePixels.size() == 0 || frameXLoc[y] > 108) {
                        frameColors[y] = 11;
                        frameXLoc[y] = 109;
                    } else {
                        frameColors[y] = (int)(grey / 42.5D);
                    }
                }
                int color = frameColors[0];
                int xloc = frameXLoc[0];
                int rle = 0;
                for (int k = 0; k < 96; k++) {
                    if (frameColors[k] == color && frameXLoc[k] == xloc) {
                        rle++;
                    } else {
                        data.add(Byte.valueOf((byte)rle));
                        data.add(Byte.valueOf((byte)xloc));
                        data.add(Byte.valueOf((byte)this.colorTable[color][1]));
                        data.add(Byte.valueOf((byte)this.colorTable[color][0]));
                        color = frameColors[k];
                        xloc = frameXLoc[k];
                        rle = 1;
                    }
                }
                rle += 2;
                if (xloc == -1 || xloc > 108) {
                    xloc = 109;
                    color = 11;
                }
                data.add(Byte.valueOf((byte)rle));
                data.add(Byte.valueOf((byte)xloc));
                data.add(Byte.valueOf((byte)this.colorTable[color][1]));
                data.add(Byte.valueOf((byte)this.colorTable[color][0]));
            }
            if (data.size() > 16000) {
                output.append(";end frame = " + frame + System.lineSeparator());
                output.append(".define BarrelP" + page + "End " + (frame + 1) + System.lineSeparator());
                output.append(".slot 1" + System.lineSeparator());
                output.append(".section \"Barrel Data" + page + "\" superfree" + System.lineSeparator());
                output.append("BarrelData" + page + ":" + System.lineSeparator());
                for (int j = 0; j < data.size(); j++) {
                    if (j % 16 == 0) {
                        output.append(String.valueOf(System.lineSeparator()) + ".db $" + String.format("%02X", new Object[] { data.get(j) }));
                    } else {
                        output.append(", $" + String.format("%02X", new Object[] { data.get(j) }));
                    }
                }
                output.append(System.lineSeparator());
                output.append(".ends" + System.lineSeparator());
                output.append(System.lineSeparator());
                page++;
                data.clear();
            }
            frame++;
            b++;
        }
        if (data.size() > 0) {
            output.append(";end frame = " + frame + System.lineSeparator());
            output.append(".define BarrelPLEnd " + (frame + 1) + System.lineSeparator());
            output.append(".slot 1" + System.lineSeparator());
            output.append(".section \"Barrel Data" + page + "\" superfree" + System.lineSeparator());
            output.append("BarrelData" + page + ":" + System.lineSeparator());
            for (int j = 0; j < data.size(); j++) {
                if (j % 16 == 0) {
                    output.append(String.valueOf(System.lineSeparator()) + ".db $" + String.format("%02X", new Object[] { data.get(j) }));
                } else {
                    output.append(", $" + String.format("%02X", new Object[] { data.get(j) }));
                }
            }
            output.append(System.lineSeparator());
            output.append(".ends" + System.lineSeparator());
            output.append(System.lineSeparator());
            page++;
            data.clear();
        }
        StringSelection stringSelection = new StringSelection(output.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private void menu_LegacyAnimation(ActionEvent event) {
        updateGraphicFormatSpecificFields();
        validateUIFields();
        TiledFile tiledFile = null;
        BufferedImage tilesetBufferedImage = null;
        ParsedImage tilesetParsedImage = null;
        Tileset tileset = null;
        BufferedImage nametableBufferedImage = null;
        ParsedImage nametableParsedImage = null;
        Nametable nametable = null;
        Scrolltable scrolltable = null;
        MetatileSet metatileSet = null;
        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setSelectedFile(new File(""));
        this.fileChooser.setDialogTitle("Open Image File");
        if(this.fileChooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File imageFile = this.fileChooser.getSelectedFile();
        this.fileChooser.setCurrentDirectory(imageFile.getParentFile());
        try {
            nametableBufferedImage = ImageIO.read(new File(imageFile.getAbsolutePath()));
        } catch (IOException e) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (nametableBufferedImage == null) {
            this.lblError.setText("Error: selected file was not a valid image!");
            updateUIForException();
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            nametableParsedImage = new ParsedImage(nametableBufferedImage, GraphicFormatManager.graphicFormats[0][1]);
            tileset = new Tileset(GraphicFormatManager.graphicFormats[0][1], nametableParsedImage);
            tileset.setPaletteType(Palette.PaletteType.optimized);
            tileset.setPreserveFormation(true);
            tileset.setTileHeight(8);
            tileset.setTileWidth(8);
            tileset.setUniqueTiles(true);
            tileset.setVerticalMirroring(false);
            tileset.setHorizontalMirroring(false);
            tileset.update(true);
            tileset.createIDHashMap();
            nametable = new Nametable(nametableParsedImage, tileset);
        } catch (UGTException e) {
            this.lblError.setText(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < 768; i++) {
            Tile tile = nametable.getNametableEntries()[i].getTileVariant().getBaseTile();
            output.append(".db");
            for (int shift = 56; shift >= 0; ) {
                output.append(" $" + String.format("%02X", new Object[] { Long.valueOf(tile.getPixelsAsPlanar()[0] >> shift & 0xFFL) }));
                shift -= 8;
            }
            output.append(System.lineSeparator());
        }
        StringSelection stringSelection = new StringSelection(output.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private void menu_Pixel3D(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle("JavaFX Projects");
        chooser.setCurrentDirectory(this.fileChooser.getCurrentDirectory());
        if(chooser.showOpenDialog(this.btnUpdate.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedDirectory = chooser.getSelectedFile();
        StringBuilder output = new StringBuilder();
        byte b;
        int i;
        File[] arrayOfFile;
        for (i = (arrayOfFile = selectedDirectory.listFiles()).length, b = 0; b < i; ) {
            File fileEntry = arrayOfFile[b];
            if (!fileEntry.isDirectory()) {
                BufferedImage bufferedImage;
                try {
                    bufferedImage = ImageIO.read(new File(fileEntry.getAbsolutePath()));
                } catch (IOException e) {
                    this.lblError.setText("Error: selected file was not a valid image!");
                    updateUIForException();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                ParsedImage parsedImage = new ParsedImage(bufferedImage, GraphicFormatManager.currentGraphicFormat);
                this.tileset = new Tileset(GraphicFormatManager.currentGraphicFormat, parsedImage);
                this.tileset.setPaletteFormation("(0x000000, 0xC00000, 0x804000, 0xC08000)");
                this.tileset.setPaletteType(Palette.PaletteType.custom);
                this.tileset.setPreserveFormation(true);
                this.tileset.setTileHeight(8);
                this.tileset.setTileWidth(8);
                this.tileset.setUniqueTiles(false);
                this.tileset.setVerticalMirroring(false);
                this.tileset.setHorizontalMirroring(false);
                try {
                    this.tileset.update(true);
                } catch (UGTException e) {
                    e.printStackTrace();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                output.append(((SegaSMSMode4)GraphicFormatManager.currentGraphicFormat).exportAsBitDepthHexBlock(this.tileset.getTiles(), 2));
            }
            b++;
        }
        StringSelection stringSelection = new StringSelection(output.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private void disableCustomPaletteText(ActionEvent event) {
        this.txtCustomPalette.setEnabled(false);
    }

    private void enableCustomPaletteText(ActionEvent event) {
        this.txtCustomPalette.setEnabled(true);
    }

    private void menu_ChangeLookAndFeel(ActionEvent event) {
        JCheckBoxMenuItem selectedMenuItem = (JCheckBoxMenuItem) event.getSource();
        String optionText = selectedMenuItem.getText();

        if (selectedMenuItem.isSelected()) {
            setNativeLookAndFeel(true);
            useNativeLookAndFeel = true;
        } else {
            setNativeLookAndFeel(false);
            useNativeLookAndFeel = false;
        }
    }

    private void setNativeLookAndFeel(boolean enable) {
        try {
            if (enable) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
                this.pack();
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
                this.pack();
            }
        } catch (Exception e) {}
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        menuBar = new JMenuBar();
        menu1 = new JMenu();
        menu8 = new JMenu();
        menuItem2 = new JMenuItem();
        menuItem3 = new JMenuItem();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        menu9 = new JMenu();
        menuItem6 = new JMenuItem();
        menu2 = new JMenu();
        menuItem7 = new JMenuItem();
        menuItem8 = new JMenuItem();
        menuItem9 = new JMenuItem();
        menu3 = new JMenu();
        menuItem10 = new JMenuItem();
        menuItem11 = new JMenuItem();
        menuItem12 = new JMenuItem();
        menuItem13 = new JMenuItem();
        menuItem14 = new JMenuItem();
        menuItem15 = new JMenuItem();
        menuItem16 = new JMenuItem();
        menu4 = new JMenu();
        menuItem17 = new JMenuItem();
        menuItem18 = new JMenuItem();
        menuItem19 = new JMenuItem();
        menuItem20 = new JMenuItem();
        menuItem21 = new JMenuItem();
        menu5 = new JMenu();
        menuItem22 = new JMenuItem();
        menuItem23 = new JMenuItem();
        menu6 = new JMenu();
        menuItem24 = new JMenuItem();
        menu7 = new JMenu();
        menuItem25 = new JMenuItem();
        menuItem26 = new JMenuItem();
        menuItem27 = new JMenuItem();
        menuItem28 = new JMenuItem();
        cbMenuChangeLookAndFeel = new JCheckBoxMenuItem();
        lblError = new JLabel();
        lblGraphicFormat = new JLabel();
        btnUpdate = new JButton();
        tabPane = new JTabbedPane();
        tabNametable = new JPanel();
        imgNametable = new JLabel();
        scrollPaneNametableSummary = new JScrollPane();
        txtNametableSummary = new JTextArea();
        tabTiles = new JPanel();
        scrollPaneTileset = new JScrollPane();
        imgTileset = new JLabel();
        panel3 = new JPanel();
        cbxTileRemoveDuplicate = new JCheckBox();
        cbxTileVerticalFlip = new JCheckBox();
        cbxTileHorizontalFlip = new JCheckBox();
        cbxTileUseSuperImage = new JCheckBox();
        scrollPaneTileLayout = new JScrollPane();
        txtTileLayout = new JTextPane();
        panel4 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        txtTileWidth = new JTextField();
        txtTileHeight = new JTextField();
        label4 = new JLabel();
        label5 = new JLabel();
        tabMetatiles = new JPanel();
        scrollPaneMetatiles = new JScrollPane();
        imgMetatiles = new JLabel();
        scrollPaneMetatileSummary = new JScrollPane();
        txtMetatileSummary = new JTextArea();
        tabPalette = new JPanel();
        scrollPanePalettes = new JScrollPane();
        imgPalettes = new JLabel();
        scrollPaneCustomPalette = new JScrollPane();
        txtCustomPalette = new JTextArea();
        panel5 = new JPanel();
        rdbSinglePalette = new JRadioButton();
        rdbOptimizedPalettes = new JRadioButton();
        rdbCustomPalette = new JRadioButton();
        panel6 = new JPanel();
        label6 = new JLabel();
        label7 = new JLabel();

        //======== this ========
        Container contentPane = getContentPane();

        //======== menuBar ========
        {

            //======== menu1 ========
            {
                menu1.setText("Graphics Format");
                menu1.setVisible(false);

                //======== menu8 ========
                {
                    menu8.setText("Sega Master System`");

                    //---- menuItem2 ----
                    menuItem2.setText("Mode 4");
                    menuItem2.addActionListener(e -> menuChangeToMode4(e));
                    menu8.add(menuItem2);

                    //---- menuItem3 ----
                    menuItem3.setText("Graphics 1");
                    menuItem3.addActionListener(e -> menuChangeToMode0(e));
                    menu8.add(menuItem3);

                    //---- menuItem4 ----
                    menuItem4.setText("Graphics 2");
                    menu8.add(menuItem4);

                    //---- menuItem5 ----
                    menuItem5.setText("Multicolor");
                    menu8.add(menuItem5);
                }
                menu1.add(menu8);

                //======== menu9 ========
                {
                    menu9.setText("Sega Megadrive");

                    //---- menuItem6 ----
                    menuItem6.setText("Mode 5");
                    menu9.add(menuItem6);
                }
                menu1.add(menu9);
            }
            menuBar.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("Import");

                //---- menuItem7 ----
                menuItem7.setText("Open Image File");
                menuItem7.addActionListener(e -> menu_OpenImageFile(e));
                menu2.add(menuItem7);

                //---- menuItem8 ----
                menuItem8.setText("Open Image File as Tileset (Advanced)");
                menuItem8.addActionListener(e -> menu_OpenTilesetImage(e));
                menu2.add(menuItem8);

                //---- menuItem9 ----
                menuItem9.setText("Open Tiled File");
                menuItem9.addActionListener(e -> menu_OpenTiledFile(e));
                menu2.add(menuItem9);
            }
            menuBar.add(menu2);

            //======== menu3 ========
            {
                menu3.setText("Export");

                //---- menuItem10 ----
                menuItem10.setText("Save Nametable");
                menuItem10.addActionListener(e -> menu_SaveNametable(e));
                menu3.add(menuItem10);

                //---- menuItem11 ----
                menuItem11.setText("Save Tiles");
                menuItem11.addActionListener(e -> menu_SaveTiles(e));
                menu3.add(menuItem11);

                //---- menuItem12 ----
                menuItem12.setText("Save Palette");
                menuItem12.addActionListener(e -> menu_SavePalette(e));
                menu3.add(menuItem12);

                //---- menuItem13 ----
                menuItem13.setText("Save Scrolltable");
                menuItem13.addActionListener(e -> menu_SaveScrolltable(e));
                menu3.add(menuItem13);

                //---- menuItem14 ----
                menuItem14.setText("Save Metatiles");
                menuItem14.addActionListener(e -> menu_SaveMetatiles(e));
                menu3.add(menuItem14);

                //---- menuItem15 ----
                menuItem15.setText("Save Tiled File");
                menuItem15.addActionListener(e -> menu_SaveTiledFile(e));
                menu3.add(menuItem15);

                //---- menuItem16 ----
                menuItem16.setText("Batch Export for GSLib");
                menuItem16.addActionListener(e -> menu_BatchExportGSLib(e));
                menu3.add(menuItem16);
            }
            menuBar.add(menu3);

            //======== menu4 ========
            {
                menu4.setText("Clipboard");

                //---- menuItem17 ----
                menuItem17.setText("Send Nametable to Clipboard");
                menuItem17.addActionListener(e -> menu_ToClipboardNametable(e));
                menu4.add(menuItem17);

                //---- menuItem18 ----
                menuItem18.setText("Send Tiles to Clipboard");
                menuItem18.addActionListener(e -> menu_ToClipboardTiles(e));
                menu4.add(menuItem18);

                //---- menuItem19 ----
                menuItem19.setText("Send Palette to Clipboard");
                menuItem19.addActionListener(e -> menu_ToClipboardPalette(e));
                menu4.add(menuItem19);

                //---- menuItem20 ----
                menuItem20.setText("Send Scrolltable to Clipboard");
                menuItem20.addActionListener(e -> menu_ToClipboardScrolltable(e));
                menu4.add(menuItem20);

                //---- menuItem21 ----
                menuItem21.setText("Send Metatiles to Clipboard");
                menuItem21.addActionListener(e -> menu_ToClipboardMetatiles(e));
                menu4.add(menuItem21);
            }
            menuBar.add(menu4);

            //======== menu5 ========
            {
                menu5.setText("Quick Save");

                //---- menuItem22 ----
                menuItem22.setText("Quick Save Nametable");
                menuItem22.addActionListener(e -> menu_QuickSaveNametable(e));
                menu5.add(menuItem22);

                //---- menuItem23 ----
                menuItem23.setText("Quick Save Scrolltable");
                menuItem23.addActionListener(e -> menu_QuickSaveScrolltable(e));
                menu5.add(menuItem23);
            }
            menuBar.add(menu5);

            //======== menu6 ========
            {
                menu6.setText("Help");
                menu6.setVisible(false);

                //---- menuItem24 ----
                menuItem24.setText("About");
                menu6.add(menuItem24);
            }
            menuBar.add(menu6);

            //======== menu7 ========
            {
                menu7.setText("Misc");

                //---- menuItem25 ----
                menuItem25.setText("SMS Multicolor Packed");
                menuItem25.setVisible(false);
                menuItem25.addActionListener(e -> menu_SMSMulticolorPacked(e));
                menu7.add(menuItem25);

                //---- menuItem26 ----
                menuItem26.setText("Barrel Animation");
                menuItem26.setVisible(false);
                menuItem26.addActionListener(e -> menu_BarrelAnimation(e));
                menu7.add(menuItem26);

                //---- menuItem27 ----
                menuItem27.setText("Legacy Animation");
                menuItem27.setVisible(false);
                menuItem27.addActionListener(e -> menu_LegacyAnimation(e));
                menu7.add(menuItem27);

                //---- menuItem28 ----
                menuItem28.setText("Pixel 3D");
                menuItem28.setVisible(false);
                menuItem28.addActionListener(e -> menu_Pixel3D(e));
                menu7.add(menuItem28);

                //---- cbMenuChangeLookAndFeel ----
                cbMenuChangeLookAndFeel.setText("Use Native Look & Feel");
                cbMenuChangeLookAndFeel.addActionListener(e -> menu_ChangeLookAndFeel(e));
                menu7.add(cbMenuChangeLookAndFeel);
            }
            menuBar.add(menu7);
        }
        setJMenuBar(menuBar);

        //---- lblError ----
        lblError.setForeground(new Color(0xa80000));
        lblError.setPreferredSize(new Dimension(987, 17));

        //---- lblGraphicFormat ----
        lblGraphicFormat.setPreferredSize(new Dimension(817, 17));

        //---- btnUpdate ----
        btnUpdate.setText("Update");
        btnUpdate.setPreferredSize(new Dimension(151, 25));
        btnUpdate.addActionListener(e -> update(e));

        //======== tabPane ========
        {
            tabPane.setPreferredSize(new Dimension(1024, 508));

            //======== tabNametable ========
            {

                //---- imgNametable ----
                imgNametable.setPreferredSize(new Dimension(618, 458));

                //======== scrollPaneNametableSummary ========
                {
                    scrollPaneNametableSummary.setPreferredSize(new Dimension(344, 200));

                    //---- txtNametableSummary ----
                    txtNametableSummary.setEditable(false);
                    scrollPaneNametableSummary.setViewportView(txtNametableSummary);
                }

                GroupLayout tabNametableLayout = new GroupLayout(tabNametable);
                tabNametable.setLayout(tabNametableLayout);
                tabNametableLayout.setHorizontalGroup(
                    tabNametableLayout.createParallelGroup()
                        .addGroup(tabNametableLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(scrollPaneNametableSummary, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                            .addComponent(imgNametable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
                tabNametableLayout.setVerticalGroup(
                    tabNametableLayout.createParallelGroup()
                        .addGroup(tabNametableLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabNametableLayout.createParallelGroup()
                                .addComponent(scrollPaneNametableSummary, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(imgNametable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }
            tabPane.addTab("Nametable", tabNametable);

            //======== tabTiles ========
            {
                tabTiles.setPreferredSize(new Dimension(1024, 180));

                //======== scrollPaneTileset ========
                {
                    scrollPaneTileset.setPreferredSize(new Dimension(272, 461));

                    //---- imgTileset ----
                    imgTileset.setVerticalAlignment(SwingConstants.TOP);
                    scrollPaneTileset.setViewportView(imgTileset);
                }

                //======== panel3 ========
                {
                    panel3.setPreferredSize(new Dimension(339, 124));
                    panel3.setLayout(new GridLayout(4, 0));

                    //---- cbxTileRemoveDuplicate ----
                    cbxTileRemoveDuplicate.setText("Remove Duplicates");
                    cbxTileRemoveDuplicate.setSelected(true);
                    cbxTileRemoveDuplicate.setRolloverSelectedIcon(null);
                    panel3.add(cbxTileRemoveDuplicate);

                    //---- cbxTileVerticalFlip ----
                    cbxTileVerticalFlip.setText("Vertical Flip");
                    cbxTileVerticalFlip.setSelected(true);
                    panel3.add(cbxTileVerticalFlip);

                    //---- cbxTileHorizontalFlip ----
                    cbxTileHorizontalFlip.setText("Horizontal Flip");
                    cbxTileHorizontalFlip.setSelected(true);
                    panel3.add(cbxTileHorizontalFlip);

                    //---- cbxTileUseSuperImage ----
                    cbxTileUseSuperImage.setText("Use Same Image as Nametable / Scrolltable");
                    cbxTileUseSuperImage.setSelected(true);
                    panel3.add(cbxTileUseSuperImage);
                }

                //======== scrollPaneTileLayout ========
                {
                    scrollPaneTileLayout.setPreferredSize(new Dimension(325, 53));

                    //---- txtTileLayout ----
                    txtTileLayout.setText("(0,2048)");
                    scrollPaneTileLayout.setViewportView(txtTileLayout);
                }

                //======== panel4 ========
                {
                    panel4.setPreferredSize(new Dimension(309, 71));
                    panel4.setLayout(new GridLayout(3, 0));

                    //---- label1 ----
                    label1.setText("Tile Layout (Advanced!)");
                    label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD));
                    panel4.add(label1);

                    //---- label2 ----
                    label2.setText("- Set range as (start index, end index)");
                    panel4.add(label2);

                    //---- label3 ----
                    label3.setText("- You can chain ranges... (0,448) (506, 507)");
                    panel4.add(label3);
                }

                //---- txtTileWidth ----
                txtTileWidth.setText("8");
                txtTileWidth.setPreferredSize(new Dimension(118, 31));

                //---- txtTileHeight ----
                txtTileHeight.setText("8");
                txtTileHeight.setPreferredSize(new Dimension(118, 31));

                //---- label4 ----
                label4.setText("Tile Width In Pixels:");

                //---- label5 ----
                label5.setText("Tile Height In Pixels:");

                GroupLayout tabTilesLayout = new GroupLayout(tabTiles);
                tabTiles.setLayout(tabTilesLayout);
                tabTilesLayout.setHorizontalGroup(
                    tabTilesLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, tabTilesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabTilesLayout.createParallelGroup()
                                .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(scrollPaneTileLayout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGroup(tabTilesLayout.createSequentialGroup()
                                    .addGroup(tabTilesLayout.createParallelGroup()
                                        .addComponent(label4)
                                        .addComponent(label5))
                                    .addGap(94, 94, 94)
                                    .addGroup(tabTilesLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(txtTileWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtTileHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scrollPaneTileset, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
                tabTilesLayout.setVerticalGroup(
                    tabTilesLayout.createParallelGroup()
                        .addGroup(tabTilesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabTilesLayout.createParallelGroup()
                                .addGroup(tabTilesLayout.createSequentialGroup()
                                    .addGroup(tabTilesLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label4)
                                        .addComponent(txtTileWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(tabTilesLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label5)
                                        .addComponent(txtTileHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(scrollPaneTileLayout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addComponent(scrollPaneTileset, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }
            tabPane.addTab("Tiles", tabTiles);

            //======== tabMetatiles ========
            {
                tabMetatiles.setPreferredSize(new Dimension(1024, 180));

                //======== scrollPaneMetatiles ========
                {
                    scrollPaneMetatiles.setPreferredSize(new Dimension(272, 461));
                    scrollPaneMetatiles.setViewportView(imgMetatiles);
                }

                //======== scrollPaneMetatileSummary ========
                {
                    scrollPaneMetatileSummary.setPreferredSize(new Dimension(344, 200));

                    //---- txtMetatileSummary ----
                    txtMetatileSummary.setEditable(false);
                    scrollPaneMetatileSummary.setViewportView(txtMetatileSummary);
                }

                GroupLayout tabMetatilesLayout = new GroupLayout(tabMetatiles);
                tabMetatiles.setLayout(tabMetatilesLayout);
                tabMetatilesLayout.setHorizontalGroup(
                    tabMetatilesLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, tabMetatilesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(scrollPaneMetatileSummary, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 383, Short.MAX_VALUE)
                            .addComponent(scrollPaneMetatiles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
                tabMetatilesLayout.setVerticalGroup(
                    tabMetatilesLayout.createParallelGroup()
                        .addGroup(tabMetatilesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabMetatilesLayout.createParallelGroup()
                                .addComponent(scrollPaneMetatileSummary, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(scrollPaneMetatiles, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                            .addContainerGap())
                );
            }
            tabPane.addTab("Metatiles", tabMetatiles);

            //======== tabPalette ========
            {
                tabPalette.setPreferredSize(new Dimension(1024, 180));

                //======== scrollPanePalettes ========
                {
                    scrollPanePalettes.setPreferredSize(new Dimension(272, 461));

                    //---- imgPalettes ----
                    imgPalettes.setVerticalAlignment(SwingConstants.TOP);
                    scrollPanePalettes.setViewportView(imgPalettes);
                }

                //======== scrollPaneCustomPalette ========
                {
                    scrollPaneCustomPalette.setPreferredSize(new Dimension(344, 314));

                    //---- txtCustomPalette ----
                    txtCustomPalette.setEnabled(false);
                    scrollPaneCustomPalette.setViewportView(txtCustomPalette);
                }

                //======== panel5 ========
                {
                    panel5.setPreferredSize(new Dimension(241, 76));
                    panel5.setLayout(new GridLayout(3, 0));

                    //---- rdbSinglePalette ----
                    rdbSinglePalette.setText("Single Palette");
                    rdbSinglePalette.addActionListener(e -> disableCustomPaletteText(e));
                    panel5.add(rdbSinglePalette);

                    //---- rdbOptimizedPalettes ----
                    rdbOptimizedPalettes.setText("Optimize for Multple Palettes");
                    rdbOptimizedPalettes.setSelected(true);
                    rdbOptimizedPalettes.addActionListener(e -> disableCustomPaletteText(e));
                    panel5.add(rdbOptimizedPalettes);

                    //---- rdbCustomPalette ----
                    rdbCustomPalette.setText("Custom Palette");
                    rdbCustomPalette.addActionListener(e -> enableCustomPaletteText(e));
                    panel5.add(rdbCustomPalette);
                }

                //======== panel6 ========
                {
                    panel6.setPreferredSize(new Dimension(309, 49));
                    panel6.setLayout(new GridLayout(2, 0));

                    //---- label6 ----
                    label6.setText("Custom Palette (Advanced!)");
                    label6.setFont(label6.getFont().deriveFont(label6.getFont().getStyle() | Font.BOLD));
                    panel6.add(label6);

                    //---- label7 ----
                    label7.setText("- You can use multiple palettes.. (palette1)(palette2)");
                    panel6.add(label7);
                }

                GroupLayout tabPaletteLayout = new GroupLayout(tabPalette);
                tabPalette.setLayout(tabPaletteLayout);
                tabPaletteLayout.setHorizontalGroup(
                    tabPaletteLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, tabPaletteLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabPaletteLayout.createParallelGroup()
                                .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(scrollPaneCustomPalette, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(panel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 383, Short.MAX_VALUE)
                            .addComponent(scrollPanePalettes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
                tabPaletteLayout.setVerticalGroup(
                    tabPaletteLayout.createParallelGroup()
                        .addGroup(tabPaletteLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(tabPaletteLayout.createParallelGroup()
                                .addGroup(tabPaletteLayout.createSequentialGroup()
                                    .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(panel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(scrollPaneCustomPalette, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addComponent(scrollPanePalettes, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                            .addContainerGap())
                );
            }
            tabPane.addTab("Palette", tabPalette);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(lblError, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(lblGraphicFormat, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(tabPane, GroupLayout.DEFAULT_SIZE, 1011, Short.MAX_VALUE))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblGraphicFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblError, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(tabPane, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar;
    private JMenu menu1;
    private JMenu menu8;
    private JMenuItem menuItem2;
    private JMenuItem menuItem3;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JMenu menu9;
    private JMenuItem menuItem6;
    private JMenu menu2;
    private JMenuItem menuItem7;
    private JMenuItem menuItem8;
    private JMenuItem menuItem9;
    private JMenu menu3;
    private JMenuItem menuItem10;
    private JMenuItem menuItem11;
    private JMenuItem menuItem12;
    private JMenuItem menuItem13;
    private JMenuItem menuItem14;
    private JMenuItem menuItem15;
    private JMenuItem menuItem16;
    private JMenu menu4;
    private JMenuItem menuItem17;
    private JMenuItem menuItem18;
    private JMenuItem menuItem19;
    private JMenuItem menuItem20;
    private JMenuItem menuItem21;
    private JMenu menu5;
    private JMenuItem menuItem22;
    private JMenuItem menuItem23;
    private JMenu menu6;
    private JMenuItem menuItem24;
    private JMenu menu7;
    private JMenuItem menuItem25;
    private JMenuItem menuItem26;
    private JMenuItem menuItem27;
    private JMenuItem menuItem28;
    private JCheckBoxMenuItem cbMenuChangeLookAndFeel;
    private JLabel lblError;
    private JLabel lblGraphicFormat;
    private JButton btnUpdate;
    private JTabbedPane tabPane;
    private JPanel tabNametable;
    private JLabel imgNametable;
    private JScrollPane scrollPaneNametableSummary;
    private JTextArea txtNametableSummary;
    private JPanel tabTiles;
    private JScrollPane scrollPaneTileset;
    private JLabel imgTileset;
    private JPanel panel3;
    private JCheckBox cbxTileRemoveDuplicate;
    private JCheckBox cbxTileVerticalFlip;
    private JCheckBox cbxTileHorizontalFlip;
    private JCheckBox cbxTileUseSuperImage;
    private JScrollPane scrollPaneTileLayout;
    private JTextPane txtTileLayout;
    private JPanel panel4;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JTextField txtTileWidth;
    private JTextField txtTileHeight;
    private JLabel label4;
    private JLabel label5;
    private JPanel tabMetatiles;
    private JScrollPane scrollPaneMetatiles;
    private JLabel imgMetatiles;
    private JScrollPane scrollPaneMetatileSummary;
    private JTextArea txtMetatileSummary;
    private JPanel tabPalette;
    private JScrollPane scrollPanePalettes;
    private JLabel imgPalettes;
    private JScrollPane scrollPaneCustomPalette;
    private JTextArea txtCustomPalette;
    private JPanel panel5;
    private JRadioButton rdbSinglePalette;
    private JRadioButton rdbOptimizedPalettes;
    private JRadioButton rdbCustomPalette;
    private JPanel panel6;
    private JLabel label6;
    private JLabel label7;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
