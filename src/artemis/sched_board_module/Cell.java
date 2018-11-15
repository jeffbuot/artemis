/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.A;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.Serializable;
import javax.swing.ImageIcon;

/**
 *
 * @author Jefferson
 */
public class Cell implements Serializable {

    //statics
    public final static int HEIGHT =42, WIDTH = 142;
    //vars
    private Font font = new Font("Segoe UI Semibold", 0, 14);
    private Font small_font = new Font("Segoe UI Semibold", 0, 12);
    private final Image back = new ImageIcon(Cell.class.getResource("/artemis/img/back.PNG")).getImage();
    private final Image add = new ImageIcon(Cell.class.getResource("/artemis/img/add_schedule.png")).getImage();
    private int x, y, width = Cell.WIDTH, height = Cell.HEIGHT;
    private boolean header = false, hovered = false, clicked = false;
    private String value, rowTime;
    private Color background;
    private Color foreground;
    private Schedule schedule;
    private final int defX, defY;

    //means its not header
    public Cell(int x, int y, String value, String rowTime) {
        this.x = x;
        this.y = y;
        this.defX = x;
        this.defY = y;
        this.rowTime = rowTime;
        this.value = value;
        background = Color.WHITE;
        foreground = A.getForegroundFor(background);
    }

    public Cell(int x, int y, int height, Schedule sched, String rowTime) {
        this.x = x;
        this.y = y;
        this.defX = x;
        this.defY = y;
        this.height = height;
        this.rowTime = rowTime;
        this.schedule = sched;
        this.value = "~";
        background = sched.getColor();
        foreground = A.getForegroundFor(background);
    }

    public Cell(int x, int y, String value) {
        this.x = x;
        this.y = y;
        this.defX = x;
        this.defY = y;
        this.header = true;
        this.value = value;
        background = new Color(91, 91, 91);
        foreground = Color.WHITE;
    }

    public Cell(int x, int y, String value, Color bg) {
        this.x = x;
        this.y = y;
        this.defX = x;
        this.defY = y;
        this.header = true;
        this.value = value;
        background = bg;
        foreground = Color.WHITE;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean bounds(int x, int y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }

    public String getSchedInfo() {
        if (schedule != null) {
            return A.toProper(schedule.getStartTime()) + " - "
                    + A.toProper(schedule.getEndTime()) + "#[" + schedule.getSeqDays() + "] " + schedule.getSessionType() + "#"
                    + schedule.getSubjectId() + "(" + schedule.getSection() + ")#"
                    + schedule.getInstituteId() + " (" + schedule.getRoomId() + ")#" + schedule.getTeacher().getName();
        }
        return "";
    }

    public int getDefX() {
        return defX;
    }

    public int getDefY() {
        return defY;
    }

    public synchronized void draw(Graphics2D g) {
        g.setFont(height > Cell.HEIGHT ? font : !isHeader() ? small_font : font);
        g.setColor(background);
        FontMetrics fm = g.getFontMetrics();
        if (!isHeader()) {
            if (clicked) {
                g.fillRect(x + 2, y + 2, width - 4, height - 4);
            } else {
                g.fillRect(x, y, width, height);
            }
        } else {
            //if its a header
            g.setColor(background);
            g.fillRect(x, y, width, height);

        }
        if (hovered) {
            if (clicked) {
                g.fillRect(x + 2, y + 2, width - 4, height - 4);
                g.setColor(new Color(51, 51, 51));
                g.drawRect(x + 2, y + 2, width - 4, height - 4);
            } else {
                g.fillRect(x, y, width, height);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, width, height);
            }
        }
        g.setColor(foreground);
        if (value.equals("~") && !hasSchedule()) {
            //if the cell has no value yet
            if (clicked) {
                g.drawImage(add, x + width / 2 - (add.getWidth(null) - 2) / 2, y
                        + height / 2 - (add.getHeight(null) - 2) / 2, add.getWidth(null) - 2, add.getHeight(null) - 2, null);
            } else {
                g.drawImage(add, x + width / 2 - add.getWidth(null) / 2, y + height / 2 - add.getHeight(null) / 2, null);
            }
        } else {
            if (isHeader()) {
                g.drawString(value, x + width / 2 - fm.stringWidth(value) / 2, y + height / 2 + 4);
            } else {
                String dys = "[" + schedule.getSeqDays() + "]";
                String subj = schedule.getSubjectId() + " - " + schedule.getSessionType();
                String sect = "(" + schedule.getSection() + ")";
                g.drawString(dys, x + width / 2 - fm.stringWidth(dys) / 2, (y + height / 2) - (fm.getHeight() / 2));
                g.drawString(sect, x + width / 2 - fm.stringWidth(sect) / 2, (y + height / 2) + (fm.getHeight() / 4));
                g.drawString(subj, x + width / 2 - fm.stringWidth(subj) / 2, (y + height / 2) + (fm.getHeight()));
            }
        }
        if (!isHeader()) {
            g.drawImage(back, x, y, width, height, null);
        }
    }

    public synchronized boolean hasSchedule() {
        return schedule != null;
    }

    public synchronized Schedule getSchedule() {
        return schedule;
    }

    public synchronized void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public synchronized String getRowTime() {
        return rowTime;
    }

    public synchronized void setRowTime(String rowTime) {
        this.rowTime = rowTime;
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized void setX(int x) {
        this.x = x;
    }

    public synchronized int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public synchronized int getHeight() {
        return height;
    }

    public synchronized int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public synchronized boolean isHeader() {
        return header;
    }

    public synchronized boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public synchronized String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public synchronized Color getBackground() {
        return background;
    }

    public synchronized void setBackgroundColor(Color backgroundColor) {
        this.background = backgroundColor;
    }

    public synchronized Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

}
