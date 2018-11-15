/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.tests;

import java.awt.Color;

/**
 *
 * @author Jeff
 */
public class TabColor {

    private final int x, y;
    private final Color color;
    private final String info;
    private boolean colored;

    public TabColor(int x, int y, Color color, String info) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.info = info;
        colored = false;
    }

    public boolean isColored() {
        return colored;
    }

    public void setColored(boolean colored) {
        this.colored = colored;
    }

    public String getInfo() {
        return info;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

}
