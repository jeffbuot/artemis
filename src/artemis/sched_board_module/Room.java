/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class Room implements Serializable{

    private List<Cell> time;
    private Cell header;

    public Room(List<Cell> time, Cell header) {
        this.time = time;
        this.header = header;
    }

    public synchronized List<Cell> getTime() {
        return time;
    }

    public boolean hasSelectedRow() {
        return time.stream().anyMatch(t -> t.isHovered());
    }

    public Cell getSelectedCell() {
        for (Cell s : time) {
            if (s.isHovered()) {
                return s;
            }
        }
        return null;
    }

    public void setTime(List<Cell> time) {
        this.time = time;
    }

    public synchronized Cell getHeader() {
        return header;
    }

    public String getHeaderValue() {
        return header.getValue();
    }

    public void setHeader(Cell header) {
        this.header = header;
    }

}
