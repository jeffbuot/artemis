/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis;

/**
 *
 * @author Jefferson
 */
import artemis.server_module.MainServer;
import com.jeff.graphics.Canvas;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class OneRing extends Canvas {

    BufferedImage imageBG;

    OneRing(BufferedImage imageBG) {
        this.imageBG = imageBG;
        setMinimumSize(new Dimension(300, 600));
    }

    public static void main(String[] args) throws Exception {
        JFrame j = new JFrame();
        j.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        j.setSize(400, 600);
        j.setLocationRelativeTo(null);
        try {
            BufferedImage img = ImageIO.read(new File("C:\\Users\\Jefferson\\Desktop\\IMG20151231201915.jpg"));
            j.add(new OneRing(img));
            j.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // presumes the images are identical in size BNI

        Ellipse2D.Double ellipse = new Ellipse2D.Double(2, 24, 50, 50);
        Area circle = new Area(ellipse);
        g.setClip(circle);
        g.drawImage(imageBG, 2, 24, 50, 50, null);
        g.draw(circle);
        g.setClip(null);
        Stroke s = new BasicStroke(2);
        g.setStroke(s);
        g.draw(circle);
        String p = "A subreport is a report included inside another report."
                + " This allows the creation of very complex layouts with different "
                + "portions of a single document filled using different data sources and reports.\n"
                + "In this tutorial we want to create an address book printing for"
                + " each person in the address book the name, the list of phone "
                + "numbers and the list of email addresses.";
        String chopped[] = p.split(" ");
        g.setFont(new Font("Segoe UI Semibold", 0, 14));
        FontMetrics fm = g.getFontMetrics();
        int stringHeight = fm.getHeight() / 2;
        int containerPadding = 4, boxPadding = 7;
        int my = containerPadding, mx = containerPadding;
        int by = my, bx = mx + 53;
        int stringX = bx + boxPadding, stringY = by + boxPadding + stringHeight;
        int pLine = 0;
        int vw = getWidth() - (containerPadding * 2 + boxPadding * 2 + 53);
        // <editor-fold defaultstate="collapsed" desc="Identifying the box size">
        for (int j = 0; j < chopped.length; j++) {
            String u = chopped[j];
            System.out.println(u);
            if (fm.stringWidth(u) > vw) {
                while (u.length() > 0) {
                    String left = u;
                    String right = "";
                    for (int i = u.length() - 1; i > 0 & fm.stringWidth(left) > vw; i--) {
                        left = u.substring(0, i);
                        right = u.substring(i);
                        System.out.print("left: " + left);
                        System.out.println("right: " + right);
                    }
                    u = right;
                    pLine++;
                }
            } else {
                String next = j + 1 == chopped.length ? "" : chopped[j + 1];
                while (fm.stringWidth(u + next) < vw) {
                    u += " " + next;
                    j++;
                    if (j == chopped.length) {
                        break;
                    }
                    next = j + 1 == chopped.length ? "" : chopped[j + 1];
                }
                pLine++;
            }
        }
        // </editor-fold>      
        int bh = pLine * (stringHeight + 4) + boxPadding * 2, bw = getWidth() - (containerPadding * 2 + 53);
        g.setColor(Color.GRAY);
        g.fillRoundRect(bx - 2, by + 2, bw, bh, 20, 20);
        g.setColor(new Color(0, 204, 153));
        g.fillRoundRect(bx, by, bw, bh, 20, 20);
        g.setColor(Color.black);
        // <editor-fold defaultstate="collapsed" desc="Responsive text drawing">
        for (int j = 0; j < chopped.length; j++) {
            String u = chopped[j];
            System.out.println(u);
            if (fm.stringWidth(u) > vw) {
                while (u.length() > 0) {
                    String left = u;
                    String right = "";
                    for (int i = u.length() - 1; i > 0 & fm.stringWidth(left) > vw; i--) {
                        left = u.substring(0, i);
                        right = u.substring(i);
                        System.out.print("left: " + left);
                        System.out.println("right: " + right);
                    }
                    u = right;
                    g.drawString(left,getWidth()-fm.stringWidth(u)-containerPadding-boxPadding, stringY);
                    stringY += stringHeight + 4;
                }
            } else {
                String next = j + 1 == chopped.length ? "" : chopped[j + 1];
                while (fm.stringWidth(u + next) < vw) {
                    u += " " + next;
                    j++;
                    if (j == chopped.length) {
                        break;
                    }
                    next = j + 1 == chopped.length ? "" : chopped[j + 1];
                }
                g.drawString(u, getWidth()-fm.stringWidth(u)-containerPadding-boxPadding, stringY);
                stringY += stringHeight + 4;
            }
        }
        // </editor-fold>  
    }
}
