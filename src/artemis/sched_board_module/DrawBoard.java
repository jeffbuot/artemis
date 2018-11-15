/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.A;
import com.jeff.graphics.Canvas;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

/**
 *
 * @author Jefferson
 */
public abstract class DrawBoard extends Canvas {

    DrawBoardModel drawBoardModel;
    JScrollPane parent;
    boolean showTooltip = false;
    private final Image popup = new ImageIcon(getClass().getResource("/artemis/img/pop.png")).getImage();
    private final Image popup2 = new ImageIcon(getClass().getResource("/artemis/img/pop2.png")).getImage();
    private final Image bg = new ImageIcon(getClass().getResource("/artemis/img/schedboard_bg.jpg")).getImage();

    public DrawBoard(List<SerializableModel> serializableModel, JScrollPane parent) {
        this.drawBoardModel = new DrawBoardModel(serializableModel);
        this.parent = parent;
        refreshScroll();
    }

    public DrawBoard(JScrollPane parent) {
        this.drawBoardModel = new DrawBoardModel();
        this.parent = parent;
        refreshScroll();
    }

    public void setSerializableModel(List<SerializableModel> sm) {
        drawBoardModel.setSerializableModel(sm);
        hy = parent.getVerticalScrollBar().getValue();
        shx = 0;
        refreshScroll();
    }

    public boolean hasSelectedCell() {
        return drawBoardModel.getRooms().stream().anyMatch(rooms -> rooms.hasSelectedRow());
    }

    public Cell getSelectedCell() {
        return drawBoardModel.getSelectedCell();
    }

    public String getSelectedRoomTitle() {
        return drawBoardModel.getSelectedRoom().getHeaderValue();
    }

    public Room getSelectedRoom() {
        return drawBoardModel.getSelectedRoom();
    }

    public void refreshScroll() {
        if (!drawBoardModel.getRooms().isEmpty()) {
            int height = drawBoardModel.getRowCount() * Cell.HEIGHT + A.MARGIN * 2 + drawBoardModel.getRowCount();
            int width = drawBoardModel.getColumnCount() * Cell.WIDTH + A.MARGIN * 2 + drawBoardModel.getColumnCount();
            setPreferredSize(new Dimension(width, height));
        } else {
            setPreferredSize(new Dimension(0, 0));
        }
        validate();
        parent.setViewportView(this);
        parent.validate();
    }

    Cell hoveredCell;

    @Override
    public void canvasMouseMoved(MouseEvent evt) {
        Cell temp = hoveredCell;
        drawBoardModel.getRooms().parallelStream().forEach(room -> {
            room.getTime().forEach(t -> t.setHovered(t.bounds(evt.getX(), evt.getY())));
            hoveredCell = getSelectedCell();
        });
        if (temp != null & hoveredCell != null) {
            if (!temp.equals(hoveredCell)) {
                selectionChange();
            }
        }
        if (!drawBoardModel.getRooms().stream().anyMatch(r -> r.hasSelectedRow())) {
            selectionChange();
        }
        repaint();
    }

    public void setShowTooltip(boolean showTooltip) {
        this.showTooltip = showTooltip;
    }

    public abstract void selectionChange();

    @Override
    public void canvasMousePressed(MouseEvent evt) {
        if (!evt.isMetaDown() && evt.getButton() != 2) {
            drawBoardModel.getRooms().parallelStream().forEach(room -> {
                room.getTime().stream().filter(cell -> cell.isHovered()).forEach(cell -> cell.setClicked(true));
            });
            repaint();
        }
    }

    @Override
    public void canvasMouseReleased(MouseEvent evt) {
        Cell temp = hoveredCell;
        drawBoardModel.getRooms().parallelStream().forEach(room -> {
            room.getTime().stream().forEach(cell -> cell.setClicked(false));
        });
        drawBoardModel.getRooms().parallelStream().forEach(room -> {
            room.getTime().forEach(t -> t.setHovered(t.bounds(evt.getX(), evt.getY())));
            hoveredCell = getSelectedCell();
        });
        if (temp != null & hoveredCell != null) {
            if (!temp.equals(hoveredCell)) {
                selectionChange();
            }
        }
        if (!drawBoardModel.getRooms().stream().anyMatch(r -> r.hasSelectedRow())) {
            selectionChange();
        }
        repaint();
    }

    public void changeHeaderPos(int y) {
        this.hy = y;
    }

    public void changeSideHeaderPos(int x) {
        this.shx = x;
    }
    int hx = 0, hy = 0;
    int shx = 0;

    @Override
    public void draw(Graphics2D g2d) {
        g2d.drawImage(bg, 0, 0, getWidth(), getHeight() + 50, null);
        boolean hasRoom = drawBoardModel.getRooms().isEmpty();
        if (drawBoardModel != null) {
            if (!hasRoom) {
                drawBoardModel.draw(g2d);
            } else {
                g2d.setColor(Color.white);
                AlphaComposite acomp = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.7f);
                g2d.setComposite(acomp);
                g2d.fillRect(20, 20, getWidth() - 40, getHeight() - 40);
                g2d.setFont(new Font("Segoe UI Semibold", 0, 18));
                g2d.setColor(Color.BLACK);
                FontMetrics f = g2d.getFontMetrics();
                String message = "There are no rooms to show.";
                g2d.drawString(message, 20 + (getWidth() / 2) - f.stringWidth(message) / 2, (getHeight() / 2));
            }
        }
        if (!hasRoom) {
            drawDynamicHeader(g2d);
        }
        //draw pop-up schedule hover

        if (drawBoardModel.getRooms().stream().anyMatch(r -> r.hasSelectedRow())) {
            if (showTooltip) {
                drawTimeTooltip(g2d, drawBoardModel.getSelectedCell().getRowTime(),
                        drawBoardModel.getSelectedCell().getX(),
                        drawBoardModel.getSelectedCell().getY()
                        + drawBoardModel.getSelectedCell().getHeight() / 2);
                drawRoomTooltip(g2d, drawBoardModel.getSelectedRoom().getHeaderValue(),
                        drawBoardModel.getSelectedCell().getX() + drawBoardModel.getSelectedCell().getWidth() / 2,
                        drawBoardModel.getSelectedCell().getY() + drawBoardModel.getSelectedCell().getHeight());
            }
            drawInfoTooltip(g2d);
        }

    }

    public void drawInfoTooltip(Graphics2D g2d) {
        g2d.setFont(new Font("Segoe UI Semibold", 0, 14));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        FontMetrics fm2 = g2d.getFontMetrics(new Font("Segoe UI", 0, 12));
        drawBoardModel.getRooms().stream().filter(r -> r.hasSelectedRow()).forEach(room -> {
            room.getTime().stream().filter(t -> t.isHovered()).forEach(cell -> {
                int px;
                int py = cell.getY() - popup.getHeight(null) + 5;
                if (cell.getSchedule() != null) {
                    //the info cone frome the cell's sched
                    String inf[] = cell.getSchedInfo().split("#");
                    int largest = 0;
                    for (String e : inf) {
                        largest = fm.stringWidth(e) > largest ? fm.stringWidth(e) : largest;
                    }
                    largest += 10;
                    int POP_WIDTH;

                    if (py < hy) {
                        py = cell.getY() + cell.getHeight();
                        POP_WIDTH = largest > popup2.getWidth(null) ? largest + 35 : popup2.getWidth(null) + 20;
                        px = cell.getX() + ((cell.getWidth() - POP_WIDTH) / 2);
                        g2d.drawImage(popup2, px, py, POP_WIDTH, popup2.getHeight(null), this);
                    } else {
                        POP_WIDTH = largest > popup.getWidth(null) ? largest + 35 : popup.getWidth(null) + 20;
                        px = cell.getX() + ((cell.getWidth() - POP_WIDTH) / 2);
                        g2d.drawImage(popup, px, py, POP_WIDTH, popup.getHeight(null), this);
                        py -= 10;
                    }
                    //draw schedule info
                    int sy = py + fm.getHeight() + 10;
                    for (String e : inf) {
                        g2d.drawString(e, px + ((POP_WIDTH - fm.stringWidth(e)) / 2), sy);
                        sy += fm.getHeight() + 1;
                    }
                    g2d.setFont(new Font("Segoe UI", 0, 12));
                    g2d.drawString(cell.getSchedule().getSchoolYear(),
                            px + ((POP_WIDTH - fm2.stringWidth(cell.getSchedule().getSchoolYear())) / 2), sy);
                }
            });
        });
    }

    public void drawTimeTooltip(Graphics2D g2d, String tooltip, int xorigin, int yorigin) {
        setOpacity(g2d, 0.7f);
        int[] xpoints = new int[]{xorigin, xorigin - 10, xorigin - 10};
        int[] ypoints = new int[]{yorigin, yorigin - 4, yorigin + 4};
        Polygon polygon = new Polygon(xpoints, ypoints, 3);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Segoe UI Semibold", 0, 12));
        FontMetrics fntmt = g2d.getFontMetrics();
        g2d.fill(polygon);
        int boxWidth = fntmt.stringWidth(tooltip + 10);
        int boxHeight = fntmt.getHeight();
        g2d.fillRoundRect(xorigin - 10 - boxWidth, yorigin - boxHeight / 2, boxWidth, boxHeight, 5, 5);
        g2d.setColor(Color.WHITE);
        setOpacity(g2d, 1f);
        g2d.drawString(tooltip, xorigin - 5 - boxWidth, (yorigin + fntmt.getHeight() / 2) - 4);
    }

    public void drawRoomTooltip(Graphics2D g2d, String tooltip, int xorigin, int yorigin) {
        setOpacity(g2d, 0.7f);
        int[] xpoints = new int[]{xorigin, xorigin - 5, xorigin + 5};
        int[] ypoints = new int[]{yorigin, yorigin + 5, yorigin + 5};
        Polygon polygon = new Polygon(xpoints, ypoints, 3);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Segoe UI Semibold", 0, 12));
        FontMetrics fntmt = g2d.getFontMetrics();
        g2d.fill(polygon);
        int boxWidth = fntmt.stringWidth(tooltip + 10);
        int boxHeight = fntmt.getHeight();
        g2d.fillRoundRect(xorigin - boxWidth / 2, yorigin + 5, boxWidth, boxHeight, 5, 5);
        g2d.setColor(Color.WHITE);
        setOpacity(g2d, 1f);
        g2d.drawString(tooltip, xorigin - fntmt.stringWidth(tooltip) / 2, yorigin + fntmt.getHeight() + 1);
    }

    private void setOpacity(Graphics2D g2d, float opacity) {
        AlphaComposite acomp = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity);
        g2d.setComposite(acomp);
    }

    private void drawDynamicHeader(Graphics2D g2d) {

        int t = A.MARGIN, s = A.MARGIN;
        //draw time header
        if (shx >= A.MARGIN) {
            drawBoardModel.time.stream().forEach(cell -> cell.setX(shx));
            s = shx;
        } else {
            drawBoardModel.time.stream().forEach(cell -> cell.setX(cell.getDefX()));
        }

        g2d.setColor(new Color(51, 153, 255));
        g2d.fillRect(s, A.MARGIN, Cell.WIDTH, (Cell.HEIGHT * 11));
        g2d.setColor(new Color(0, 102, 153));
        g2d.fillRect(s, A.MARGIN + (Cell.HEIGHT * 11), Cell.WIDTH, (Cell.HEIGHT * 12));
        g2d.setColor(new Color(0, 51, 102));
        g2d.fillRect(s, A.MARGIN + (Cell.HEIGHT * 23), Cell.WIDTH, (Cell.HEIGHT * 8));

        if (!drawBoardModel.getRooms().isEmpty()) {
            drawBoardModel.time.stream().forEach(cell -> cell.draw(g2d));
        }
        //draw scroll headers
        if (hy >= A.MARGIN) {
            //draw top header(adjusted)
            g2d.setColor(new Color(91, 91, 91));
            g2d.fillRect(A.MARGIN, hy, A.MARGIN + Cell.WIDTH * drawBoardModel.getRooms().size(), Cell.HEIGHT);
            drawBoardModel.getRooms().stream().map(room -> room.getHeader()).forEach(cell -> cell.setY(hy));
            t = hy;
        } else {
            //draw top header(default)
            g2d.setColor(new Color(91, 91, 91));
            g2d.fillRect(A.MARGIN, A.MARGIN, A.MARGIN + Cell.WIDTH * drawBoardModel.getRooms().size(), Cell.HEIGHT);
            drawBoardModel.getRooms().stream().map(room -> room.getHeader()).forEach(cell -> cell.setY(cell.getDefY()));
        }
        drawBoardModel.getRooms().stream().map(room -> room.getHeader()).forEach(cell -> cell.draw(g2d));
        new Cell(s, t, "Time").draw(g2d);
    }
}
