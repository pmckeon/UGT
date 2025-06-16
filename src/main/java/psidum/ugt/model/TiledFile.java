package psidum.ugt.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import psidum.ugt.hardware.UGTException;

public class TiledFile {
    private Document xmlDoc = null;

    private File file;

    static final String TiledShellXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<map version=\"1.0\" tiledversion=\"1.0.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"0\" height=\"0\" tilewidth=\"8\" tileheight=\"8\" nextobjectid=\"1\">\r\n\r\n\t<tileset firstgid=\"1\" name=\"GSLTiles\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"0\" columns=\"16\">\r\n\t\t<image source=\"0\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<tileset firstgid=\"4096\" name=\"GSLMeta\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"18\" columns=\"16\">\r\n\t\t<image source=\"GSLMetaFont.png\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<layer name=\"GSLTileLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLPriorityLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLCollisionLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLMetaLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n</map>\r\n";

    public static TiledFile openTiledFile(JFrame frame, JFileChooser fileChooser) throws UGTException {
        fileChooser.resetChoosableFileFilters();
        fileChooser.setDialogTitle("Open Tiled File");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "tiled map (*.tmx)", "tmx"));
        if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File tiledDocument = fileChooser.getSelectedFile();
        if (tiledDocument == null)
            return null;
        fileChooser.setCurrentDirectory(tiledDocument.getParentFile());
        return new TiledFile(tiledDocument);
    }

    public TiledFile(File file) throws UGTException {
        this.file = file;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            this.xmlDoc = dBuilder.parse(file);
            this.xmlDoc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new UGTException("Error: could not open tiled file!");
        }
    }

    public TiledFile(Nametable nametable, File tileFile, BufferedImage tileImage) throws UGTException {
        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<map version=\"1.0\" tiledversion=\"1.0.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"0\" height=\"0\" tilewidth=\"8\" tileheight=\"8\" nextobjectid=\"1\">\r\n\r\n\t<tileset firstgid=\"1\" name=\"GSLTiles\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"0\" columns=\"16\">\r\n\t\t<image source=\"0\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<tileset firstgid=\"4096\" name=\"GSLMeta\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"18\" columns=\"16\">\r\n\t\t<image source=\"GSLMetaFont.png\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<layer name=\"GSLTileLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLPriorityLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLCollisionLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSLMetaLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n</map>\r\n";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            this.xmlDoc = loadXMLFromString(xml);
        } catch (Exception e) {
            throw new UGTException("Error: could not load TiledShellXML!");
        }
        setMapWidth(nametable.widthInTiles);
        setMapHeight(nametable.heightInTiles);
        long[] output = new long[(nametable.getNametableEntries()).length];
        for (int i = 0; i < (nametable.getNametableEntries()).length; i++) {
            long entryValue = nametable.getNametableEntries()[i].getTileVariant().getBaseTile().getVramLocation().getIndex();
            if (nametable.getNametableEntries()[i].getTileVariant().isHorizontalFlip())
                entryValue += 2147483648L;
            if (nametable.getNametableEntries()[i].getTileVariant().isVerticalFlip())
                entryValue += 1073741824L;
            entryValue++;
            output[i] = entryValue;
        }
        setGSLTileLayer(output);
        output = new long[(nametable.getNametableEntries()).length];
        setGSLCollisionLayer(output);
        setGSLMetaLayer(output);
        setGSLPriorityLayer(output);
        setTileImage(tileFile, tileImage);
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public BufferedImage getFullImage(Tileset tileset) throws UGTException {
        long[] gslTileLayer = getGSLTileLayer();
        int startID = getTilesetStartID();
        int widthInTiles = getMapWidth();
        int heightInTiles = getMapHeight();
        int tileCounter = 0;
        int pixelCounter = 0;
        int[] imageData = new int[widthInTiles * heightInTiles * 64];
        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                long id = gslTileLayer[tileCounter++] - startID + 1L;
                if ((id & 0xFFFFFFFF80000000L) == 0L && (id & 0x40000000L) == 0L) {
                    int intID = (int) (id & 0xFFFFL);
                    Tile tile = tileset.tilesByID.get(Integer.valueOf(intID - 1));
                    if (tile == null)
                        throw new UGTException("Error: Tiled file references a tile id beyond the size of included tiles!");
                    byte b;
                    int j, arrayOfInt[];
                    for (j = (arrayOfInt = tile.getPixelsAsRGB()).length, b = 0; b < j; ) {
                        int pixel = arrayOfInt[b];
                        imageData[pixelCounter++] = pixel;
                        b++;
                    }
                } else if ((id & 0xFFFFFFFF80000000L) == 0L && (id & 0x40000000L) != 0L) {
                    int intID = (int) (id & 0xFFFFL);
                    Tile tile = tileset.tilesByID.get(Integer.valueOf(intID - 1));
                    if (tile == null)
                        throw new UGTException("Error: Tiled file references a tile id beyond the size of included tiles!");
                    for (int row = 56; row >= 0; row -= 8) {
                        for (int pixel = 0; pixel < 8; pixel++)
                            imageData[pixelCounter++] = tile.getPixelsAsRGB()[row + pixel];
                    }
                } else if ((id & 0xFFFFFFFF80000000L) != 0L && (id & 0x40000000L) != 0L) {
                    int intID = (int) (id & 0xFFFFL);
                    Tile tile = tileset.tilesByID.get(Integer.valueOf(intID - 1));
                    if (tile == null)
                        throw new UGTException("Error: Tiled file references a tile id beyond the size of included tiles!");
                    for (int row = 56; row >= 0; row -= 8) {
                        for (int pixel = 7; pixel >= 0; pixel--)
                            imageData[pixelCounter++] = tile.getPixelsAsRGB()[row + pixel];
                    }
                } else if ((id & 0xFFFFFFFF80000000L) != 0L && (id & 0x40000000L) == 0L) {
                    int intID = (int) (id & 0xFFFFL);
                    Tile tile = tileset.tilesByID.get(Integer.valueOf(intID - 1));
                    if (tile == null)
                        throw new UGTException("Error: Tiled file references a tile id beyond the size of included tiles!");
                    for (int row = 0; row <= 56; row += 8) {
                        for (int pixel = 7; pixel >= 0; pixel--)
                            imageData[pixelCounter++] = tile.getPixelsAsRGB()[row + pixel];
                    }
                }
            }
        }
        int[] imageDataFixed = new int[imageData.length];
        int imageWidth = widthInTiles * 8;
        int pixelCount = 0;
        for (int i = 0; i < imageData.length; i += imageWidth * 8) {
            for (int x = 0; x < imageWidth; x += 8) {
                for (int fineY = 0; fineY < imageWidth * 8; fineY += imageWidth) {
                    for (int fineX = 0; fineX < 8; fineX++)
                        imageDataFixed[i + x + fineY + fineX] = imageData[pixelCount++];
                }
            }
        }
        BufferedImage image = new BufferedImage(widthInTiles * 8, heightInTiles * 8, 2);
        int[] imgData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(imageDataFixed, 0, imgData, 0, imageDataFixed.length);
        return image;
    }

    public BufferedImage getTileImage() throws UGTException {
        BufferedImage importImage = null;
        try {
            NodeList tilesets = this.xmlDoc.getElementsByTagName("tileset");
            for (int i = 0; i < tilesets.getLength(); i++) {
                Node currenttileset = tilesets.item(i);
                if (currenttileset.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) currenttileset;
                    String name = element.getAttribute("name");
                    if (name.equals("GSLTiles")) {
                        Element imageElement = (Element) element.getElementsByTagName("image").item(0);
                        File imageLocation = new File(this.file.getAbsoluteFile().getParent(), imageElement.getAttribute("source"));
                        importImage = ImageIO.read(imageLocation);
                    }
                }
            }
            if (importImage == null)
                throw new UGTException("Error: 'GSLTiles' tileset missing from tiled file!");
        } catch (IOException e) {
            throw new UGTException("Error: tileset image associated with tiled file could not be opened!");
        }
        return importImage;
    }

    public void setTileImage(File tileImageFile, BufferedImage tilesetProcessedImage) throws UGTException {
        this.xmlDoc.getDocumentElement().normalize();
        try {
            NodeList tilesetNodes = this.xmlDoc.getElementsByTagName("tileset");
            Element gseTileset = null;
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                String test = ((Element) tilesetNodes.item(i)).getAttribute("name");
                if (test.equals("GSLTiles")) {
                    gseTileset = (Element) tilesetNodes.item(i);
                    break;
                }
            }
            if (gseTileset == null)
                throw new UGTException("Error: internally stored XML Doc is malformed!");
            gseTileset.setAttribute("tilecount", String.valueOf(tilesetProcessedImage.getHeight() / 8 * 16));
            ((Element) gseTileset.getElementsByTagName("image").item(0)).setAttribute("source", tileImageFile.getName());
            ((Element) gseTileset.getElementsByTagName("image").item(0)).setAttribute("height",
                    String.valueOf(tilesetProcessedImage.getHeight()));
        } catch (Exception e) {
            throw new UGTException("Error: could not construct tiled xml doc. Issue with tileset image!");
        }
    }

    public long[] getGSLTileLayer() throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        Element gslTileLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLTileLayer")) {
                    gslTileLayer = element;
                    break;
                }
            }
        }
        if (gslTileLayer == null)
            throw new UGTException("Error: Tiled xml file does not contain a GSLTileLayer!");
        String data = gslTileLayer.getElementsByTagName("data").item(0).getTextContent();
        data = data.replaceAll(",", " ");
        data = data.replaceAll("[^0-9 ]", "");
        String[] tileIDString = data.split(" ");
        long[] layerData = new long[tileIDString.length];
        try {
            for (int j = 0; j < layerData.length; j++)
                layerData[j] = Long.parseLong(tileIDString[j]);
        } catch (NumberFormatException e) {
            throw new UGTException("Error: GSLTileLayer contains invalid elements!");
        }
        return layerData;
    }

    public void setGSLTileLayer(long[] layerData) throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        int widthInTiles = getMapWidth();
        int heightInTiles = getMapHeight();
        Element gslTileLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLTileLayer")) {
                    gslTileLayer = element;
                    break;
                }
            }
        }
        StringBuilder output = new StringBuilder();
        int x = 0;
        for (int j = 0; j < layerData.length; j++) {
            output.append(layerData[j]);
            if (j != layerData.length - 1)
                output.append(((j + 1) % 32 == 0) ? ("," + System.lineSeparator()) : ",");
            x++;
        }
        output.append(System.lineSeparator());
        gslTileLayer.getElementsByTagName("data").item(0).setTextContent(output.toString());
        gslTileLayer.setAttribute("height", String.valueOf(heightInTiles));
        gslTileLayer.setAttribute("width", String.valueOf(widthInTiles));
    }

    public long[] getGSLPriorityLayer() throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        Element gslPriorityLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLPriorityLayer")) {
                    gslPriorityLayer = element;
                    break;
                }
            }
        }
        if (gslPriorityLayer == null)
            return new long[getMapWidth() * getMapHeight()];
        String data = gslPriorityLayer.getElementsByTagName("data").item(0).getTextContent();
        data = data.replaceAll(",", " ");
        data = data.replaceAll("[^0-9 ]", "");
        String[] tileIDString = data.split(" ");
        long[] layerData = new long[tileIDString.length];
        try {
            for (int j = 0; j < layerData.length; j++)
                layerData[j] = Long.parseLong(tileIDString[j]);
        } catch (NumberFormatException e) {
            throw new UGTException("Error: GSLPriorityLayer contains invalid elements!");
        }
        return layerData;
    }

    public void setGSLPriorityLayer(long[] layerData) throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        int widthInTiles = getMapWidth();
        int heightInTiles = getMapHeight();
        Element gslTileLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLPriorityLayer")) {
                    gslTileLayer = element;
                    break;
                }
            }
        }
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        for (int j = 0; j < layerData.length; j++) {
            output.append(layerData[j]);
            if (j != layerData.length - 1)
                output.append((j != 0 && j % 32 == 0) ? ("," + System.lineSeparator()) : ",");
        }
        gslTileLayer.getElementsByTagName("data").item(0).setTextContent(output.toString());
        gslTileLayer.setAttribute("height", String.valueOf(heightInTiles));
        gslTileLayer.setAttribute("width", String.valueOf(widthInTiles));
    }

    public long[] getGSLCollisionLayer() throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        Element gslCollisionLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLCollisionLayer")) {
                    gslCollisionLayer = element;
                    break;
                }
            }
        }
        if (gslCollisionLayer == null)
            return new long[getMapWidth() * getMapHeight()];
        String data = gslCollisionLayer.getElementsByTagName("data").item(0).getTextContent();
        data = data.replaceAll(",", " ");
        data = data.replaceAll("[^0-9 ]", "");
        String[] tileIDString = data.split(" ");
        long[] layerData = new long[tileIDString.length];
        try {
            for (int j = 0; j < layerData.length; j++)
                layerData[j] = Long.parseLong(tileIDString[j]);
        } catch (NumberFormatException e) {
            throw new UGTException("Error: GSLCollisionLayer contains invalid elements!");
        }
        return layerData;
    }

    public void setGSLCollisionLayer(long[] layerData) throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        int widthInTiles = getMapWidth();
        int heightInTiles = getMapHeight();
        Element gslTileLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLCollisionLayer")) {
                    gslTileLayer = element;
                    break;
                }
            }
        }
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        for (int j = 0; j < layerData.length; j++) {
            output.append(layerData[j]);
            if (j != layerData.length - 1)
                output.append((j != 0 && j % 32 == 0) ? ("," + System.lineSeparator()) : ",");
        }
        gslTileLayer.getElementsByTagName("data").item(0).setTextContent(output.toString());
        gslTileLayer.setAttribute("height", String.valueOf(heightInTiles));
        gslTileLayer.setAttribute("width", String.valueOf(widthInTiles));
    }

    public long[] getGSLMetaLayer() throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        Element gslMetaLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLMetaLayer")) {
                    gslMetaLayer = element;
                    break;
                }
            }
        }
        if (gslMetaLayer == null)
            return new long[getMapWidth() * getMapHeight()];
        String data = gslMetaLayer.getElementsByTagName("data").item(0).getTextContent();
        data = data.replaceAll(",", " ");
        data = data.replaceAll("[^0-9 ]", "");
        String[] tileIDString = data.split(" ");
        long[] layerData = new long[tileIDString.length];
        try {
            for (int j = 0; j < layerData.length; j++)
                layerData[j] = Long.parseLong(tileIDString[j]);
        } catch (NumberFormatException e) {
            throw new UGTException("Error: GSLMetaLayer contains invalid elements!");
        }
        return layerData;
    }

    public void setGSLMetaLayer(long[] layerData) throws UGTException {
        NodeList layers = this.xmlDoc.getElementsByTagName("layer");
        int widthInTiles = getMapWidth();
        int heightInTiles = getMapHeight();
        Element gslTileLayer = null;
        for (int i = 0; i < layers.getLength(); i++) {
            Node layer = layers.item(i);
            if (layer.getNodeType() == 1) {
                Element element = (Element) layer;
                if (element.getAttribute("name").equals("GSLMetaLayer")) {
                    gslTileLayer = element;
                    break;
                }
            }
        }
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        for (int j = 0; j < layerData.length; j++) {
            output.append(layerData[j]);
            if (j != layerData.length - 1)
                output.append((j != 0 && j % 32 == 0) ? ("," + System.lineSeparator()) : ",");
        }
        output.append(System.lineSeparator());
        gslTileLayer.getElementsByTagName("data").item(0).setTextContent(output.toString());
        gslTileLayer.setAttribute("height", String.valueOf(heightInTiles));
        gslTileLayer.setAttribute("width", String.valueOf(widthInTiles));
    }

    public int getTilesetStartID() throws UGTException {
        try {
            NodeList tilesets = this.xmlDoc.getElementsByTagName("tileset");
            for (int i = 0; i < tilesets.getLength(); i++) {
                Node currenttileset = tilesets.item(i);
                if (currenttileset.getNodeType() == 1) {
                    Element element = (Element) currenttileset;
                    if (element.getAttribute("name").equals("GSLTiles"))
                        return Integer.parseInt(element.getAttribute("firstgid"));
                }
            }
        } catch (Exception e) {
            throw new UGTException(
                    "Error: tiled document has an error, could not get firstgid property from GSLTiles layer!");
        }
        throw new UGTException(
                "Error: tiled document has an error, could not get firstgid property from GSLTiles layer!");
    }

    public int getMetaInformationStartID() throws UGTException {
        try {
            NodeList tilesets = this.xmlDoc.getElementsByTagName("tileset");
            for (int i = 0; i < tilesets.getLength(); i++) {
                Node currenttileset = tilesets.item(i);
                if (currenttileset.getNodeType() == 1) {
                    Element element = (Element) currenttileset;
                    if (element.getAttribute("name").equals("GSLMeta"))
                        return Integer.parseInt(element.getAttribute("firstgid"));
                }
            }
        } catch (Exception e) {
            throw new UGTException(
                    "Error: tiled document has an error, could not get firstgid property from GSLMeta layer!");
        }
        throw new UGTException(
                "Error: tiled document has an error, could not get firstgid property from GSLMeta layer!");
    }

    public int getMapWidth() throws UGTException {
        try {
            NodeList map = this.xmlDoc.getElementsByTagName("map");
            return Integer.parseInt(((Element) map.item(0)).getAttribute("width"));
        } catch (NumberFormatException e) {
            throw new UGTException("Error: tiled document has an error, map-width property is invalid!");
        }
    }

    public void setMapWidth(int width) {
        NodeList map = this.xmlDoc.getElementsByTagName("map");
        ((Element) map.item(0)).setAttribute("width", String.valueOf(width));
    }

    public int getMapHeight() throws UGTException {
        try {
            NodeList map = this.xmlDoc.getElementsByTagName("map");
            return Integer.parseInt(((Element) map.item(0)).getAttribute("height"));
        } catch (NumberFormatException e) {
            throw new UGTException("Error: tiled document has an error, map-height property is invalid!");
        }
    }

    public void setMapHeight(int height) {
        NodeList map = this.xmlDoc.getElementsByTagName("map");
        ((Element) map.item(0)).setAttribute("height", String.valueOf(height));
    }

    public Document getXmlDoc() {
        return this.xmlDoc;
    }
}
