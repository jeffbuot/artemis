/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.SchedId;
import artemis.classes.Teacher;
import java.awt.Color;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class Schedule implements Serializable {

    private List<SchedId> id;
    private String startTime;
    private String endTime;
    private String day;
    private String subjectId;
    private String roomId;
    private Teacher teacher;
    private String instituteId;
    private String schoolYear;
    private Color color;
    private String seqDays;
    private String section;
    private String sessionType;
    private List<String> backupQueries;

    public Schedule(List<String> backupQueries,List<SchedId> id, String startTime, 
            String endTime, String day, String section, String sessionType, String subjectId,
            String roomId, Teacher teacherId, String instituteId, String schoolYear,
            Color col, String sd) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.section = section;
        this.subjectId = subjectId;
        this.roomId = roomId;
        this.teacher = teacherId;
        this.instituteId = instituteId;
        this.schoolYear = schoolYear;
        this.color = col;
        this.seqDays = sd;
        this.sessionType = sessionType;
        this.backupQueries = backupQueries;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSeqDays() {
        return seqDays;
    }

    public Color getColor() {
        return color;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public List<SchedId> getId() {
        return id;
    }

    public List<String> getBackupQueries() {
        return backupQueries;
    }

}
