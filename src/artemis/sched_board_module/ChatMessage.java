/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.Message;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Jefferson
 */
public class ChatMessage {

    private final Message message;
    private final boolean isSender;
    private boolean hovered = false;
    private BufferedImage defaultProfile;
    private boolean hasProfile = true;

    public ChatMessage(Message message, boolean isSender) {
        this.message = message;
        this.isSender = isSender;
        try {
            defaultProfile = ImageIO.read(getClass().getResource("/artemis/img/default_profile.png"));
        } catch (IOException ex) {
            hasProfile = false;
        }
    }

    public Message getMessage() {
        return message;
    }

    public boolean isIsSender() {
        return isSender;
    }

    public boolean bounds(int x, int y) {
        hovered = x >= this.xx && x <= this.xx + 50 && y >= this.yy && y <= this.yy + 50;
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    int xx = 0, yy = 0;

    //Karaw coding mode
    public synchronized int draw(Graphics2D g, int containerWidth, int currentY) {
        int containerPadding = 4, boxPadding = 7;
        xx = 2;
        yy = currentY + containerPadding;
        if (isSender) {
            try {
                g.drawImage(message.getSenderImage().getImage(), 2, currentY + containerPadding, 50, 50, null);
            } catch (NullPointerException ex) {
                if (hasProfile) {
                    g.drawImage(defaultProfile, 2, currentY + containerPadding, 50, 50, null);
                }
            }
            g.setColor(hovered ? Color.CYAN : Color.DARK_GRAY);
            Stroke s = new BasicStroke(2);
            g.setStroke(s);
            g.drawRect(2, currentY + containerPadding, 50, 50);
        }
        String chopped[] = message.getMessage().split(" ");
        g.setFont(new Font("Segoe UI Semibold", 0, 14));
        FontMetrics fm = g.getFontMetrics();
        int stringHeight = fm.getHeight() / 2;
        int my = containerPadding, mx = containerPadding;
        int by = my + currentY, bx = mx + 53;
        int stringX = bx + boxPadding, stringY = by + boxPadding + stringHeight;
        int pLine = 0;
        int vw = containerWidth - (containerPadding * 2 + boxPadding * 2 + 53);
        List<String> lines = new ArrayList<>();
        // <editor-fold defaultstate="collapsed" desc="Identifying the box size">
        for (int j = 0; j < chopped.length; j++) {
            String u = chopped[j];
            if (fm.stringWidth(u) > vw) {
                while (u.length() > 0) {
                    String left = u;
                    String right = "";
                    for (int i = u.length() - 1; i > 0 & fm.stringWidth(left) > vw; i--) {
                        left = u.substring(0, i);
                        right = u.substring(i);
                    }
                    u = right;
                    lines.add(left);
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
                lines.add(u);
            }
        }
        pLine += 2;
        lines.add(message.getSender() + " says:");
        lines.add(message.getTimeInfo());
        // </editor-fold>   
        int maximumBoxWidth = containerWidth - (containerPadding * 2 + 53 + boxPadding);
        int maxString = lines.stream().mapToInt(b -> fm.stringWidth(b)).summaryStatistics().getMax();
        maxString += containerPadding * 2 + boxPadding;
        maxString = maxString > maximumBoxWidth ? maximumBoxWidth : maxString;
        int bh = pLine * (stringHeight + 4) + boxPadding * 2, bw = maxString;
        g.setColor(Color.GRAY);
        if (isSender) {
            g.fillPolygon(new Polygon(new int[]{45 - 2, 65 - 2, 62 - 2}, new int[]{by, by, by + 16}, 3));
        } else {
//            g.fillPolygon(new Polygon(new int[]{45 - 2, 65 - 2, 62 - 2}, new int[]{by, by, by + 16}, 3));
        }
        g.fillRoundRect(isSender ? bx - 2 : containerWidth - bw - containerPadding - 2, by + 2, bw, bh, 16, 16);
        g.setColor(isSender ? new Color(0, 204, 153) : new Color(0, 153, 204));
        //box color list: [255,255,102],0, 204, 153,[0,153,204]
        if (isSender) {
            g.fillPolygon(new Polygon(new int[]{45, 65, 62}, new int[]{by, by, by + 16}, 3));
        } else {
            g.fillPolygon(new Polygon(new int[]{containerWidth, containerWidth - 15, containerWidth - 4}, new int[]{by, by, by + 16}, 3));
        }
        g.fillRoundRect(isSender ? bx : containerWidth - bw - containerPadding, by, bw, bh, 16, 16);
        g.setColor(new Color(51, 51, 51));
        // <editor-fold defaultstate="collapsed" desc="Responsive text drawing">
        String sender = isSender ? message.getSender() + " says:" : "Me";
        g.drawString(sender, isSender ? stringX : containerWidth - fm.stringWidth(sender) - containerPadding - boxPadding, stringY);
        stringY += stringHeight + 4;
        for (int j = 0; j < chopped.length; j++) {
            String u = chopped[j];
            if (fm.stringWidth(u) > vw) {
                while (u.length() > 0) {
                    String left = u;
                    String right = "";
                    for (int i = u.length() - 1; i > 0 & fm.stringWidth(left) > vw; i--) {
                        left = u.substring(0, i);
                        right = u.substring(i);
                    }
                    u = right;
                    g.drawString(left, isSender ? stringX : containerWidth - fm.stringWidth(left) - containerPadding - boxPadding, stringY);
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
                g.drawString(u, isSender ? stringX : containerWidth - fm.stringWidth(u) - containerPadding - boxPadding, stringY);
                stringY += stringHeight + 4;
            }
        }
        // </editor-fold>  

        g.setFont(new Font("Segoe UI Semilight", 0, 12));
        stringY += stringHeight + 4;
        g.drawString(message.getTimeInfo(), isSender ? stringX : containerWidth
                - fm.stringWidth(message.getTimeInfo()) - containerPadding - boxPadding, stringY - 8);

        int minimumHeight = containerPadding * 2 + 53;
        return stringY + containerPadding < minimumHeight ? minimumHeight : stringY + containerPadding;
    }

}
