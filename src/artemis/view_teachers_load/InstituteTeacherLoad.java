/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.view_teachers_load;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class InstituteTeacherLoad implements Serializable{

    private final String instituteId;
    private final List<TeacherLoad> teacherLoads;

    public InstituteTeacherLoad(String instituteId, List<TeacherLoad> teacherLoads) {
        this.instituteId = instituteId;
        this.teacherLoads = teacherLoads;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public List<TeacherLoad> getTeacherLoads() {
        return teacherLoads;
    }

    public TeacherLoad getTeachersLoad(String teacherId) {
        for (TeacherLoad tl : teacherLoads) {
            if (tl.getTeacherId().equals(teacherId)) {
                return tl;
            }
        }
        return null;
    }
}
