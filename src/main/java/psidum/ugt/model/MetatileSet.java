package psidum.ugt.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Comparator;

public class MetatileSet {
    ArrayList<Metatile> metatiles = null;

    int collisionCount = 0;

    int width = 0;

    int height = 0;

    public MetatileSet(ArrayList<Metatile> metatiles, int collisionCount, int width, int height) {
        this.metatiles = metatiles;
        this.collisionCount = collisionCount;
        this.width = width;
        this.height = height;
    }

    public String ExportAsHex() {
        StringBuilder output = new StringBuilder();
        output.append(".dw $" + String.format("%04X", new Object[] { Integer.valueOf((this.metatiles.size() + 1) * this.width * this.height * 2) }) + System.lineSeparator());
        output.append(".db $" + String.format("%02X", new Object[] { Integer.valueOf(this.collisionCount) }) + " $00" + System.lineSeparator());
        output.append(".dw $0000" + System.lineSeparator());
        output.append(".dw $0000");
        int counter = 0;
        for (int i = 0; i < this.metatiles.size(); i++) {
            for (int n = 0; n < this.width * this.height; n++) {
                String value = String.format("%04X", new Object[] { Integer.valueOf(((Metatile)this.metatiles.get(i)).metatileEntries[n]) });
                output.append((counter++ % 16 == 0) ? (String.valueOf(System.lineSeparator()) + ".dw $" + value) : (", $" + value));
            }
        }
        output.append(System.lineSeparator());
        return output.toString();
    }

    public String getSummary() {
        StringBuilder output = new StringBuilder();
        output.append("Number of Metatiles: ");
        output.append(this.metatiles.size());
        return output.toString();
    }

    public BufferedImage getMetatileImage() {
        if (this.metatiles.size() == 0)
            return null;
        this.metatiles.sort(Comparator.comparing(Metatile::getParsedID));
        int width = 128;
        int height = this.metatiles.size() * 32;
        int[] data = new int[width * height];
        int modifiedCollisionCount = this.collisionCount - 1;
        int index = 0;
        int backPixel = -2033416;
        while (index < modifiedCollisionCount * 32 * width) {
            data[index] = backPixel;
            index++;
        }
        int priorityPixel = -261892;
        int currentPriorityPixel = 0;
        int priorityOffset = 16 * width;
        backPixel = Integer.MAX_VALUE;
        while (index < data.length) {
            data[index] = backPixel;
            index++;
        }
        for (int i = 0; i < this.metatiles.size(); i++) {
            int[] pixels = ((Metatile)this.metatiles.get(i)).tileVariants[0].getTilePixels();
            int startY = i * 32 * width;
            int startX = 0;
            int pixel = 0;
            currentPriorityPixel = (((Metatile)this.metatiles.get(i)).priorityData[0] == 0) ? backPixel : priorityPixel;
            int y;
            for (y = startY; y < startY + 8 * width; y += width) {
                for (int x = startX; x < startX + 8; x++) {
                    data[x + y] = pixels[pixel++];
                    data[x + y + priorityOffset] = currentPriorityPixel;
                }
            }
            pixels = ((Metatile)this.metatiles.get(i)).tileVariants[1].getTilePixels();
            startY = i * 32 * width;
            startX = 8;
            pixel = 0;
            currentPriorityPixel = (((Metatile)this.metatiles.get(i)).priorityData[1] == 0) ? backPixel : priorityPixel;
            for (y = startY; y < startY + 8 * width; y += width) {
                for (int x = startX; x < startX + 8; x++) {
                    data[x + y] = pixels[pixel++];
                    data[x + y + priorityOffset] = currentPriorityPixel;
                }
            }
            pixels = ((Metatile)this.metatiles.get(i)).tileVariants[2].getTilePixels();
            startY = i * 32 * width + 8 * width;
            startX = 0;
            pixel = 0;
            currentPriorityPixel = (((Metatile)this.metatiles.get(i)).priorityData[2] == 0) ? backPixel : priorityPixel;
            for (y = startY; y < startY + 8 * width; y += width) {
                for (int x = startX; x < startX + 8; x++) {
                    data[x + y] = pixels[pixel++];
                    data[x + y + priorityOffset] = currentPriorityPixel;
                }
            }
            pixels = ((Metatile)this.metatiles.get(i)).tileVariants[3].getTilePixels();
            startY = i * 32 * width + 8 * width;
            startX = 8;
            pixel = 0;
            currentPriorityPixel = (((Metatile)this.metatiles.get(i)).priorityData[3] == 0) ? backPixel : priorityPixel;
            for (y = startY; y < startY + 8 * width; y += width) {
                for (int x = startX; x < startX + 8; x++) {
                    data[x + y] = pixels[pixel++];
                    data[x + y + priorityOffset] = currentPriorityPixel;
                }
            }
        }
        BufferedImage image = new BufferedImage(width, height, 2);
        int[] imgData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, imgData, 0, data.length);
        BufferedImage scaledImage = new BufferedImage(image.getWidth() * 3, image.getHeight() * 3, 2);
        Graphics graphics = scaledImage.getGraphics();
        graphics.drawImage(image, 0, 0, image.getWidth() * 3, image.getHeight() * 3, null);
        image = scaledImage;
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial Black", Font.PLAIN, 18));
        for (int j = 0; j < this.metatiles.size(); j++) {
            int startY = j * 32 * 3 + 20;
            int startX = 50;
            graphics.setFont(new Font("Arial Black", Font.PLAIN, 18));
            graphics.drawString("Metatile " + ((Metatile)this.metatiles.get(j)).getParsedID() + " ($" + String.format("%02X", new Object[] { Integer.valueOf(((Metatile)this.metatiles.get(j)).getParsedID()) }) + ")", startX, startY);
            graphics.setFont(new Font("Arial Black", Font.PLAIN, 15));
            graphics.drawString(String.valueOf(((Metatile)this.metatiles.get(j)).metaData[0]), startX, startY + 25);
            graphics.drawString(String.valueOf(((Metatile)this.metatiles.get(j)).metaData[1]), startX + 50, startY + 25);
            graphics.drawString(String.valueOf(((Metatile)this.metatiles.get(j)).metaData[2]), startX, startY + 50);
            graphics.drawString(String.valueOf(((Metatile)this.metatiles.get(j)).metaData[3]), startX + 50, startY + 50);
        }
        graphics.dispose();
        this.metatiles.sort(Comparator.comparing(Metatile::getId));
        return image;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ArrayList<Metatile> getMetatiles() {
        return this.metatiles;
    }

    public int getCollisionCount() {
        return this.collisionCount - 1;
    }
}
