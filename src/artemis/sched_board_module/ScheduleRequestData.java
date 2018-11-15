/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.Remark;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class ScheduleRequestData implements Serializable {

    private final String id = "N/A";
    private boolean success = false;
    private final String desc;
    private final String query;
    private List<Remark> remarks = new ArrayList<>();

    private final String startTime;
    private final String endTime;
    private final String day;
    private final String subjectId;
    private final String roomId;
    private final String teacherId;
    private final String section;
    private final String schoolYear;

    public ScheduleRequestData(String desc, String query, String startTime, 
            String endTime, String day, String subjectId, String roomId, 
            String teacherId, String section, String schoolYear) {
        this.desc = desc;
        this.query = query;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.subjectId = subjectId;
        this.roomId = roomId;
        this.teacherId = teacherId;
        this.section = section;
        this.schoolYear = schoolYear;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void appendRemarks(String remarks,boolean flag) {
        this.remarks.add(new Remark(remarks,flag));
    }

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDesc() {
        return desc;
    }

    public String getQuery() {
        return query;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDay() {
        return day;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public List<Remark> getRemarks() {
        return remarks;
    }

    public String getSection() {
        return section;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

}
