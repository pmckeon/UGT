package psidum.ugt;

import java.util.Properties;
import psidum.ugt.hardware.GraphicFormat;
import psidum.ugt.hardware.SegaSMSMode0;
import psidum.ugt.hardware.SegaSMSMode4;
import psidum.ugt.util.GeneralUtil;

public class GraphicFormatManager {
    public static GraphicFormat[][] graphicFormats;

    public static GraphicFormat currentGraphicFormat = null;

    public static void Initialize(Properties prop) {
        graphicFormats = new GraphicFormat[1][];
        graphicFormats[0] = new GraphicFormat[]{new SegaSMSMode4(), new SegaSMSMode0()};
        currentGraphicFormat = graphicFormats[0][0];
        graphicFormats[0][0].setByteLineAffix(prop.getProperty("SMSMode4_ByteLineAffix", graphicFormats[0][0].getByteLineAffix()));
        graphicFormats[0][0].setByteValueAffix(prop.getProperty("SMSMode4_ByteValueAffix", graphicFormats[0][0].getByteValueAffix()));
        graphicFormats[0][0].setWordLineAffix(prop.getProperty("SMSMode4_WordLineAffix", graphicFormats[0][0].getWordLineAffix()));
        graphicFormats[0][0].setWordValueAffix(prop.getProperty("SMSMode4_WordValueAffix", graphicFormats[0][0].getWordValueAffix()));
        int temp = GeneralUtil.isInteger(prop.getProperty("SMSMode4_TransparentPixel")) ? Integer.parseInt(prop.getProperty("SMSMode4_TransparentPixel")) : graphicFormats[0][0].getTransparentPixel();
        graphicFormats[0][0].setTransparentPixel(temp);
        temp = GeneralUtil.isInteger(prop.getProperty("SMSMode4_EmptyTileColor")) ? Integer.parseInt(prop.getProperty("SMSMode4_EmptyTileColor")) : graphicFormats[0][0].getEmptyTileColor();
        graphicFormats[0][0].setEmptyTileColor(temp);
    }

    public static void Initialize() {
        graphicFormats = new GraphicFormat[1][];
        graphicFormats[0] = new GraphicFormat[]{new SegaSMSMode4(), new SegaSMSMode0()};
        currentGraphicFormat = graphicFormats[0][0];
    }
}
