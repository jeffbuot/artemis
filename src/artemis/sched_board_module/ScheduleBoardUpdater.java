/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class ScheduleBoardUpdater implements Serializable {

    BoardListUpdate blu;
    List<InstituteSerialModel> institutes;
    

    public ScheduleBoardUpdater() {
        blu = new BoardListUpdate();
        institutes = new ArrayList<>();
    }

    public ScheduleBoardUpdater(BoardListUpdate blu, List<InstituteSerialModel> institutes) {
        this.blu = blu;
        this.institutes = institutes;
    }

    public BoardListUpdate getBlu() {
        return blu;
    }

    public List<InstituteSerialModel> getInstitutes() {
        return institutes;
    }

    public List<SerializableModel> getSerializableModel(String institute, String day) {
        List<SerializableModel> in = new ArrayList<>();
        for (InstituteSerialModel i : institutes) {
            if (i.getInstituteId().equals(institute)) {
                switch (day) {
                    case "Monday":
                        in = i.getMonday();
                        break;
                    case "Tuesday":
                        in = i.getTuesday();
                        break;
                    case "Wednesday":
                        in = i.getWednesday();
                        break;
                    case "Thursday":
                        in = i.getThursday();
                        break;
                    case "Friday":
                        in = i.getFriday();
                        break;
                    default:
                        in = i.getSaturday();
                        break;
                }
                break;
            }
        }
        return in;
    }
}
