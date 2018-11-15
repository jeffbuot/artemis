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
public class InstituteSerialModel implements Serializable {

    private final String instituteId;
    private final List<SerializableModel> monday;
    private final List<SerializableModel> tuesday;
    private final List<SerializableModel> wednesday;
    private final List<SerializableModel> thursday;
    private final List<SerializableModel> friday;
    private final List<SerializableModel> saturday;

    public InstituteSerialModel(String instituteId, List<SerializableModel> monday,
            List<SerializableModel> tuesday, List<SerializableModel> wednesday,
            List<SerializableModel> thursday, List<SerializableModel> friday,
            List<SerializableModel> saturday) {
        this.instituteId = instituteId;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public List<SerializableModel> getMonday() {
        return monday;
    }

    public List<SerializableModel> getTuesday() {
        return tuesday;
    }

    public List<SerializableModel> getWednesday() {
        return wednesday;
    }

    public List<SerializableModel> getThursday() {
        return thursday;
    }

    public List<SerializableModel> getFriday() {
        return friday;
    }

    public List<SerializableModel> getSaturday() {
        return saturday;
    }

}
