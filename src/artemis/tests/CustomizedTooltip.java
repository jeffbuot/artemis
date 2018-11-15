/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.tests;

import com.jeff.graphics.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author Jefferson
 */
public class CustomizedTooltip extends Canvas {

    int xorigin, yorigin;

    @Override
    public void draw(Graphics2D g2d) {
        int[] xpoints = new int[]{xorigin, xorigin - 5, xorigin + 5};
        int[] ypoints = new int[]{yorigin, yorigin + 5, yorigin + 5};
        Polygon polygon = new Polygon(xpoints, ypoints, 3);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Segoe UI Semibold", 0, 12));
        String tooltip = "Hello this is a toolsdgsegwegsdrgtip";
        FontMetrics fntmt = g2d.getFontMetrics();
        g2d.fill(polygon);
        int boxWidth = fntmt.stringWidth(tooltip + 10);
        int boxHeight = fntmt.getHeight();
        g2d.fillRoundRect(xorigin - boxWidth / 2, yorigin + 5, boxWidth, boxHeight, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.drawString(tooltip, xorigin - fntmt.stringWidth(tooltip) / 2, yorigin + fntmt.getHeight()+1);
    }

    @Override
    public void canvasMouseMoved(MouseEvent evt) {
        xorigin = evt.getX() - 50;
        yorigin = evt.getY() - 50;
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Customized Tooltip");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new CustomizedTooltip());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
