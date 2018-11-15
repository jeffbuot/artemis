/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.dialogs.ArtemisMessage;
import com.jeff.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jefferson
 */
public class SerializableModelFactory {

    public static List<SerializableModel> generate(Connection connection, String instituteId, String day, String sy) {

        List<SerializableModel> serial = new ArrayList<>();
        try {
            String r = (instituteId.equals("Over All") | instituteId.equals("")
                    ? "showAllActiveRooms" : "showActiveRoomsByInstitute('" + instituteId + "')");
            DBUtil.toList(connection, "call " + r, "id").stream().forEach(id -> {
                try {
                    //Note: institute is not yet been filtered here
//                    String e = instituteId.equals("Over All") | instituteId.equals("") ? "" : " and institute_id = '" + instituteId + "'";
                    String q = String.format("select * from class_sched where room_id ='%s' "
                            + "and `day` = '%s' and sy = '%s'", id, day.substring(0, 3) + "", sy);
                    List<Schedule> scheds = SchedFetcher.fetchSchedule(connection, q);
                    serial.add(new SerializableModel(id, scheds));
                } catch (SQLException ex) {
                    Logger.getLogger(SerializableModelFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            );
        } catch (SQLException ex) {
            System.out.println(ex);
            ArtemisMessage.showDialog(null, "Error occured while fetching some data.", 1);
        }
        return serial;
    }
}
