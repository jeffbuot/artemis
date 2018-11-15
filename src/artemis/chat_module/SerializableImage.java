/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.chat_module;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 *
 *
 * @author Jefferson
 */
public class SerializableImage implements Serializable {

    private final int width;
    private final int height;
    private final int pixels[];

    public SerializableImage(BufferedImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = img.getRGB(0, 0, width, height, new int[width * height], 0, width);
    }

    public BufferedImage getImage() {
        BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        b.setRGB(0, 0, width, height, pixels, 0, width);
            return b;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

}
