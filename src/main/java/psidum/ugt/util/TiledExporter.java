package psidum.ugt.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import psidum.ugt.model.Nametable;
import psidum.ugt.model.Tileset;

public class TiledExporter {
    static final String TiledShellXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<map version=\"1.0\" tiledversion=\"1.0.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"0\" height=\"0\" tilewidth=\"8\" tileheight=\"8\" nextobjectid=\"1\">\r\n\r\n\t<tileset firstgid=\"1\" name=\"GSETiles\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"0\" columns=\"16\">\r\n\t\t<image source=\"0\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<tileset firstgid=\"4096\" name=\"GSEMeta\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"18\" columns=\"16\">\r\n\t\t<image source=\"GSELibMetaFont.png\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<layer name=\"GSETileLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSEPriorityLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSECollisionLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSEMetaLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n</map>\r\n";

    public static boolean exportToTiled(Nametable nametable, Tileset tileset, int width, int height, int tileWidth, int tileHeight, JFrame frame) {
        JFileChooser fileChooser = new JFileChooser ();
        fileChooser.setDialogTitle("Step 1 of 3: Save Tile Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "png file", "png"));
        //fileChooser.setInitialFileName("*.png");
        if(fileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File file = fileChooser.getSelectedFile();
        BufferedImage tilesetImage = tileset.getTileImage();
        if (file == null)
            return false;
        try {
            ImageIO.write(tileset.getTileImage(), "png", file);
        } catch (IOException e) {
            return false;
        }
        String tileImageFile = file.getName();
        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<map version=\"1.0\" tiledversion=\"1.0.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"0\" height=\"0\" tilewidth=\"8\" tileheight=\"8\" nextobjectid=\"1\">\r\n\r\n\t<tileset firstgid=\"1\" name=\"GSETiles\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"0\" columns=\"16\">\r\n\t\t<image source=\"0\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<tileset firstgid=\"4096\" name=\"GSEMeta\" tilewidth=\"8\" tileheight=\"8\" tilecount=\"18\" columns=\"16\">\r\n\t\t<image source=\"GSELibMetaFont.png\" width=\"128\" height=\"0\"/>\r\n\t</tileset>\r\n\t\r\n\t<layer name=\"GSETileLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSEPriorityLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSECollisionLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n\t<layer name=\"GSEMetaLayer\" width=\"0\" height=\"0\">\r\n\t\t<data encoding=\"csv\">\r\n\r\n\t\t</data>\r\n\t</layer>\r\n\t\r\n</map>\r\n";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = loadXMLFromString(xml);
            doc.getDocumentElement().normalize();
            NodeList tilesetNodes = doc.getElementsByTagName("tileset");
            Element gseTileset = null;
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                String test = ((Element)tilesetNodes.item(i)).getAttribute("name");
                if (((Element)tilesetNodes.item(i)).getAttribute("name").equals("GSETiles")) {
                    gseTileset = (Element)tilesetNodes.item(i);
                    break;
                }
            }
            if (gseTileset == null)
                return false;
            gseTileset.setAttribute("tilecount", String.valueOf(tilesetImage.getWidth() / tileWidth * tilesetImage.getHeight() / tileHeight));
            gseTileset.setAttribute("tilecount", String.valueOf(tilesetImage.getWidth() / tileWidth * tilesetImage.getHeight() / tileHeight));
            ((Element)gseTileset.getElementsByTagName("image").item(0)).setAttribute("source", file.getName());
            NodeList layer = doc.getElementsByTagName("layer");
            Element eLayer = null;
            for (int n = 0; n < layer.getLength(); n++) {
                String test = ((Element)layer.item(n)).getAttribute("name");
                if (((Element)layer.item(n)).getAttribute("name").equals("GSETileLayer")) {
                    eLayer = (Element)layer.item(n);
                    break;
                }
            }
            if (eLayer == null)
                return false;
            StringBuilder stringBuilder3 = new StringBuilder();
            int i1;
            for (i1 = 0; i1 < (nametable.getNametableEntries()).length; i1++) {
                long entryValue = nametable.getNametableEntries()[i1].getTileVariant().getBaseTile().getVramLocation().getIndex();
                if (nametable.getNametableEntries()[i1].getTileVariant().isHorizontalFlip())
                    entryValue += 2147483648L;
                if (nametable.getNametableEntries()[i1].getTileVariant().isVerticalFlip())
                    entryValue += 1073741824L;
                stringBuilder3.append(entryValue + 1L);
                if (i1 + 1 == (nametable.getNametableEntries()).length)
                    break;
                stringBuilder3.append(((i1 + 1) % 32 == 0) ? ("," + System.lineSeparator()) : ",");
            }
            stringBuilder3.append(System.lineSeparator());
            eLayer.getElementsByTagName("data").item(0).setTextContent(stringBuilder3.toString());
            eLayer.setAttribute("height", String.valueOf(height / tileHeight));
            eLayer.setAttribute("width", String.valueOf(width / tileWidth));
            layer = doc.getElementsByTagName("layer");
            eLayer = null;
            for (int m = 0; m < layer.getLength(); m++) {
                String test = ((Element)layer.item(m)).getAttribute("name");
                if (((Element)layer.item(m)).getAttribute("name").equals("GSEPriorityLayer")) {
                    eLayer = (Element)layer.item(m);
                    break;
                }
            }
            if (eLayer == null)
                return false;
            StringBuilder stringBuilder2 = new StringBuilder();
            for (i1 = 0; i1 < (nametable.getNametableEntries()).length; i1++) {
                stringBuilder2.append("0");
                if (i1 + 1 == (nametable.getNametableEntries()).length)
                    break;
                stringBuilder2.append(((i1 + 1) % 32 == 0) ? ("," + System.lineSeparator()) : ",");
            }
            stringBuilder2.append(System.lineSeparator());
            eLayer.getElementsByTagName("data").item(0).setTextContent(stringBuilder2.toString());
            eLayer.setAttribute("height", String.valueOf(height / tileHeight));
            eLayer.setAttribute("width", String.valueOf(width / tileWidth));
            layer = doc.getElementsByTagName("layer");
            eLayer = null;
            for (int k = 0; k < layer.getLength(); k++) {
                String test = ((Element)layer.item(k)).getAttribute("name");
                if (((Element)layer.item(k)).getAttribute("name").equals("GSECollisionLayer")) {
                    eLayer = (Element)layer.item(k);
                    break;
                }
            }
            if (eLayer == null)
                return false;
            StringBuilder stringBuilder1 = new StringBuilder();
            for (i1 = 0; i1 < (nametable.getNametableEntries()).length; i1++) {
                stringBuilder1.append("0");
                if (i1 + 1 == (nametable.getNametableEntries()).length)
                    break;
                stringBuilder1.append(((i1 + 1) % 32 == 0) ? ("," + System.lineSeparator()) : ",");
            }
            stringBuilder1.append(System.lineSeparator());
            eLayer.getElementsByTagName("data").item(0).setTextContent(stringBuilder1.toString());
            eLayer.setAttribute("height", String.valueOf(height / tileHeight));
            eLayer.setAttribute("width", String.valueOf(width / tileWidth));
            layer = doc.getElementsByTagName("layer");
            eLayer = null;
            for (int j = 0; j < layer.getLength(); j++) {
                String test = ((Element)layer.item(j)).getAttribute("name");
                if (((Element)layer.item(j)).getAttribute("name").equals("GSEMetaLayer")) {
                    eLayer = (Element)layer.item(j);
                    break;
                }
            }
            if (eLayer == null)
                return false;
            StringBuilder output = new StringBuilder();
            for (i1 = 0; i1 < (nametable.getNametableEntries()).length; i1++) {
                output.append("0");
                if (i1 + 1 == (nametable.getNametableEntries()).length)
                    break;
                output.append(((i1 + 1) % 32 == 0) ? ("," + System.lineSeparator()) : ",");
            }
            output.append(System.lineSeparator());
            eLayer.getElementsByTagName("data").item(0).setTextContent(output.toString());
            eLayer.setAttribute("height", String.valueOf(height / tileHeight));
            eLayer.setAttribute("width", String.valueOf(width / tileWidth));
            fileChooser = new JFileChooser ();
            fileChooser.setDialogTitle("Step 3 of 3: Save Nametable As");
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "tiled map format", "tmx"));
            //fileChooser.setInitialFileName("*.tmx");
            file = new File(file.getAbsolutePath());
            fileChooser.setCurrentDirectory(file.getParentFile());
            if(fileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = fileChooser.getSelectedFile();
            if (file != null) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty("encoding", "UTF-8");
                DOMSource source = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(new File(file.getAbsolutePath()));
                transformer.transform(source, streamResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
