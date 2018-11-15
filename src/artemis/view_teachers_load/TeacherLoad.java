/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.view_teachers_load;

import java.io.Serializable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jefferson
 */
public class TeacherLoad implements Serializable {

    private final String teacherId;
    private final String teacherName;
    private final DefaultTableModel tableModel;

    public TeacherLoad(String teacherId, String teacherName, DefaultTableModel tableModel) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.tableModel = tableModel;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public int getTotalFacultyLoad() {
        if(tableModel.getRowCount()==0){return -1;}
        int sum = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (tableModel.getColumnName(j).toLowerCase().startsWith("lab")
                        || tableModel.getColumnName(j).toLowerCase().startsWith("lect")) {
                    sum += Integer.parseInt(tableModel.getValueAt(i, j).toString());
                }
            }
        }
        return sum;
    }

    public String getTeacherId() {
        return teacherId;
    }

}
