/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.sched_board_module.BoardListUpdate;
import com.jeff.util.DBUtil;
import com.jeff.util.TabModel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class BLUFactory {

    public static BoardListUpdate generate(Connection con) {
        if (con == null) {
            return new BoardListUpdate();
        } else {
            try {
                return new BoardListUpdate(
                        TabModel.getDefaulTableModel(con.createStatement().executeQuery("Call showAllTeachers")),
                        TabModel.getDefaulTableModel(con.createStatement().executeQuery("Call showAllRooms")),
                        TabModel.getDefaulTableModel(con.createStatement().executeQuery("Call showAllSubjects")),
                        TabModel.getDefaulTableModel(con.createStatement().executeQuery("Call showAllPrograms")),
                        DBUtil.toList(con, "Call showAllActiveInstitutes", "id"),
                        toTeacherList(con),
                        toSubjectList(con),
                        toRoomList(con),
                        toProgramList(con));
            } catch (SQLException ex) {
                return new BoardListUpdate();
            }
        }
    }

    public static List<Program> toProgramList(Connection c) throws SQLException {
        List<Program> data = new ArrayList<>();
        DBUtil.getListData(c, "Call showAllPrograms").forEach(hash -> {
            data.add(new Program(hash.get("id").toString(),
                    hash.get("description").toString(), hash.get("facilitator").toString()));
        });
        return data;
    }

    public static List<Teacher> toTeacherList(Connection c) throws SQLException {
        List<Teacher> data = new ArrayList<>();
        DBUtil.getListData(c, "Call showAllTeachers").forEach(hash -> {
            data.add(new Teacher(hash.get("id").toString(),
                    hash.get("firstname") + " " + hash.get("lastname"), hash.get("institute_id").toString()));
        });
        return data;
    }

    public static List<Subject> toSubjectList(Connection c) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        DBUtil.getListData(c, "Call showAllSubjects").forEach(hash -> {
            subjects.add(new Subject(hash.get("id").toString(),
                    hash.get("description").toString(), hash.get("program_facilitator").toString(),
                    (int) hash.get("lect_units"), (int) hash.get("lab_units")));
        });
        return subjects;
    }

    public static List<Classroom> toRoomList(Connection c) throws SQLException {
        List<Classroom> rooms = new ArrayList<>();
        DBUtil.getListData(c, "Call showAllRooms").forEach(hash -> {
            rooms.add(new Classroom(hash.get("id").toString(),
                    hash.get("description").toString(), hash.get("institute_id").toString()));
        });
        return rooms;
    }
}
