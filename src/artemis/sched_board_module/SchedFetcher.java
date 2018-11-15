/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.SchedId;
import artemis.classes.Teacher;
import com.jeff.util.DBUtil;
import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class SchedFetcher {

    public static List<Schedule> fetchSchedule(Connection con, String query) throws SQLException {
        List<Schedule> scheds = new ArrayList<>();
        ResultSet rs = con.createStatement().executeQuery(query);
        while (rs.next()) {
            String startTime = rs.getString("start_time");
            String endTime = rs.getString("end_time");
            String days = rs.getString("day");
            String section = rs.getString("section");
            String sessionType = rs.getString("session_type");
            String subject = rs.getString("subject_id");
            String room = rs.getString("room_id");
            String teacherId = rs.getString("teacher_id");
            String institute = rs.getString("institute_id");
            String sy = rs.getString("sy");
            ResultSet rs2 = con.createStatement().executeQuery("select color from institutes where id = '" + institute + "'");
            rs2.next();
            Color col = toColor(rs2.getString("color"));
            ResultSet teach = con.createStatement().executeQuery("select * from teachers where id ='" + teacherId + "'");
            teach.next();
            Teacher teacher = new Teacher(teacherId, teach.getString("firstname")
                    + " " + teach.getString("lastname"), teach.getString("institute_id"));
            String sql = String.format("select id ,`day` from class_sched where room_id ='%s'"
                    + " and subject_id = '%s' and start_time = '%s' and end_time = '%s' "
                    + "and teacher_id = '%s' and institute_id = '%s' and sy = '%s' ",
                    room, subject, startTime, endTime, teacher.getId(), institute, sy);
            StringBuilder seqD = new StringBuilder();
            List<SchedId> ids = new ArrayList<>();
            List<String> backup = new ArrayList<>();
            DBUtil.getListData(con, sql).stream().forEach(hash -> {
                String d = hash.get("day").toString();
                String v;
                if (d.startsWith("Tu")) {
                    v = "T";
                } else if (d.startsWith("Th")) {
                    v = "Th";
                } else {
                    v = d.charAt(0) + "";
                }
                ids.add(new SchedId((int) hash.get("id"), v));
                seqD.append(v);
                String bq = String.format("CALL addSchedule('%s','%s','%s',"
                        + "'%s','%s','%s','%s','%s','%s','%s')",
                        startTime, endTime, d, section, subject, room, teacherId,
                        institute, sy, sessionType);
                backup.add(bq);
            });
            scheds.add(new Schedule(backup,ids, startTime, endTime, days, section, sessionType,
                    subject, room, teacher, institute, sy, col, seqD.toString()));
        }
        return scheds;
    }

    public static Color toColor(String c) {
        String a[] = c.split(":");
        int r = Integer.parseInt(a[0]), g = Integer.parseInt(a[1]), b = Integer.parseInt(a[2]);
        return new Color(r, g, b);
    }
}
