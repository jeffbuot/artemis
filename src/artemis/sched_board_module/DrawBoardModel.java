/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.A;
import artemis.classes.SchedId;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jefferson
 */
public class DrawBoardModel implements Serializable {

    public final List<Cell> time;
    private final List<Room> rooms;
    private List<SerializableModel> serializableModel;

    //Rooms are not set yet
    public DrawBoardModel(List<SerializableModel> serializableModel) {
        time = new ArrayList<>();
        this.serializableModel = serializableModel;
        rooms = new ArrayList<>();
        initTime();
    }

    public DrawBoardModel() {
        time = new ArrayList<>();
        this.serializableModel = new ArrayList<>();
        rooms = new ArrayList<>();
        initTime();
    }

    public int getRowCount() {
        return time.size();
    }

    public int getColumnCount() {
        return 1 + rooms.size();
    }

    private void initTime() {
        int x = A.MARGIN, y = A.MARGIN;
        time.add(new Cell(x, y, "Time"));
        y += Cell.HEIGHT + 1;
        for (int i = 7; i < 22; i++) {

            String t1 = prop(i) + ":00 " + amp(i) + "-" + prop(i) + ":30 " + amp(i);
            String t2 = prop(i) + ":30 " + amp(i) + "-" + prop(i + 1) + ":00 " + amp(i + 1);
            Color col;
            if (amp(i).equals("AM")) {
                col = new Color(51, 153, 255);
            } else {
                if (i < 18) {
                    col = new Color(0, 102, 153);
                } else {
                    col = new Color(0, 51, 102);
                }
            }
            time.add(new Cell(x, y, t1, col));
            y += Cell.HEIGHT + 1;
            time.add(new Cell(x, y, t2, col));
            y += Cell.HEIGHT + 1;
        }
        refreshModelDraw();
    }

    private int prop(int t) {
        int hour = t % 12;
        if (hour == 0) {
            hour = 12;
        }
        return hour;
    }

    private String amp(int t) {
        return t < 12 ? "AM" : "PM";
    }

    public synchronized Cell getSelectedCell() {
        if (!rooms.isEmpty()) {
            List<Room> rms = rooms.stream().filter(room -> room.hasSelectedRow()).collect(Collectors.toList());
            if (rms.isEmpty()) {
                return null;
            }
            Room r = rms.get(0);
            for (Cell c : r.getTime()) {
                if (c.isHovered()) {
                    return c;
                }
            }
        }
        return null;
    }

    public Room getSelectedRoom() {
        List<Room> rms = rooms.stream().filter(room -> room.hasSelectedRow()).collect(Collectors.toList());
        if (rms.isEmpty()) {
            return null;
        }
        return rms.get(0);
    }

    public List<SerializableModel> getSerializableModel() {
        return serializableModel;
    }

    public void setSerializableModel(List<SerializableModel> serializableModel) {
        this.serializableModel = serializableModel;
        refreshModelDraw();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    private List<SerializableModel> cloneModel() {

        List<SerializableModel> clone = new ArrayList<>();
        serializableModel.forEach(model -> {
            List<Schedule> sched = new ArrayList<>();
            model.getScheds().forEach(s -> {
                List<SchedId> schedIdList = new ArrayList<>();
                List<String> backup = new ArrayList<>();
                s.getId().forEach(i -> schedIdList.add(new SchedId(i.getId(), i.getDay())));
                s.getBackupQueries().forEach(b -> backup.add(b));
                sched.add(new Schedule(backup, schedIdList, s.getStartTime(), s.getEndTime(), s.getDay(),
                        s.getSection(), s.getSessionType(), s.getSubjectId(), s.getRoomId(), s.getTeacher(),
                        s.getInstituteId(), s.getSchoolYear(), s.getColor(), s.getSeqDays()));
            });

            clone.add(new SerializableModel(model.getRoomId(), sched));
        });
        return clone;
    }

    //some challenging code here..i'm blessed i'm pretty tough
    private synchronized void refreshModelDraw() {
        rooms.clear();
        cloneModel().stream().forEach(model -> {
            List<Cell> room_rows = new ArrayList<>();
            int x = Cell.WIDTH * getColumnCount() + A.MARGIN + getColumnCount();
            int y = Cell.HEIGHT + A.MARGIN + 1;
            for (int i = 1; i < time.size(); i++) {
                //for starting time
                Cell t = time.get(i);
                int merge = 1;
                Schedule temp = null;
                String st = t.getValue().split("-")[0];
                String et = t.getValue().split("-")[1];
                //get the schedule if the schedule is equals to the time
                for (Schedule s : model.getScheds()) {
                    if (A.toProper(s.getStartTime()).equals(st)) {
                        temp = s;
                        break;
                    }
                }
                //if there is schedule
                if (temp != null) {
                    int start = i;
                    int startY = y;
                    while (!et.equals(A.toProper(temp.getEndTime()))) {
                        merge++;
                        i++;
                        t = time.get(i);
                        et = t.getValue().split("-")[1];
                    }
                    room_rows.add(new Cell(x, startY, Cell.HEIGHT * merge + merge - 1, temp, time.get(i).getValue()));
                    y += ((Cell.HEIGHT + 1) * (i - start));
                    model.getScheds().remove(temp);
                } else {
                    room_rows.add(new Cell(x, y, "~", time.get(i).getValue()));
                }
                y += Cell.HEIGHT + 1;
            }
            rooms.add(new Room(room_rows, new Cell(x, A.MARGIN, model.getRoomId())));
        });
    }

    public synchronized void draw(Graphics2D g) {
//        time.stream().forEach(cell -> cell.draw(g));
        rooms.stream().forEach((room) -> {
            //            room.getHeader().draw(g);
            room.getTime().stream().forEach((c) -> {
                c.draw(g);
            });
        });
    }
}
