/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.view_teachers_load;

/**
 *
 * @author Jefferson
 */
public class TeacherLoadDataSource {

    private final String faculty;
    private final String subject;
    private final String section;
    private final String lec;
    private final String lab;
    private final String session;
    private final String time;
    private final String days;
    private final String room;
    private final String totalUnitLoad;

    public TeacherLoadDataSource(String faculty, String subject, String section,
            String lec, String lab, String session, String time, String days, 
            String room,String totalUnitLoad) {
        this.faculty = faculty;
        this.subject = subject;
        this.section = section;
        this.lec = lec;
        this.lab = lab;
        this.session = session;
        this.time = time;
        this.days = days;
        this.room = room;
        this.totalUnitLoad = totalUnitLoad;
    }

    public String getTotalUnitLoad() {
        return totalUnitLoad;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getSubject() {
        return subject;
    }

    public String getSection() {
        return section;
    }

    public String getLec() {
        return lec;
    }

    public String getLab() {
        return lab;
    }

    public String getSession() {
        return session;
    }

    public String getTime() {
        return time;
    }

    public String getDays() {
        return days;
    }

    public String getRoom() {
        return room;
    }


}
