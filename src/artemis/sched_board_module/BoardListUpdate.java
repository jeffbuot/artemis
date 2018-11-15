/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.Classroom;
import artemis.classes.Program;
import artemis.classes.Subject;
import artemis.classes.Teacher;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jefferson
 */
@SuppressWarnings("FieldMayBeFinal")
public class BoardListUpdate implements Serializable {

    private DefaultTableModel modelTeachers;
    private DefaultTableModel modelRooms;
    private DefaultTableModel modelSubjects;
    private DefaultTableModel modelPrograms;
    private List<String> listInstitutes;
    private List<Teacher> listTeachers;
    private List<Subject> listSubjects;
    private List<Classroom> listRooms;
    private List<Program> listPrograms;

    public BoardListUpdate() {
        modelTeachers = new DefaultTableModel();
        modelSubjects = new DefaultTableModel();
        modelRooms = new DefaultTableModel();
        listInstitutes = new ArrayList<>();
        listRooms = new ArrayList<>();
        listSubjects = new ArrayList<>();
        listTeachers = new ArrayList<>();
    }

    public BoardListUpdate(DefaultTableModel modelTeachers, DefaultTableModel modelRooms,
            DefaultTableModel modelSubjects, DefaultTableModel modelPrograms,
            List<String> listInstitutes, List<Teacher> listTeachers, List<Subject> listSubjects,
            List<Classroom> listRooms, List<Program> listPrograms) {
        this.modelTeachers = modelTeachers;
        this.modelRooms = modelRooms;
        this.modelSubjects = modelSubjects;
        this.modelPrograms = modelPrograms;
        this.listInstitutes = listInstitutes;
        this.listTeachers = listTeachers;
        this.listSubjects = listSubjects;
        this.listRooms = listRooms;
        this.listPrograms = listPrograms;
    }

    public DefaultTableModel getModelPrograms() {
        return modelPrograms;
    }

    public List<Program> getListPrograms() {
        return listPrograms;
    }

    public DefaultTableModel getModelTeachers() {
        return modelTeachers;
    }

    public DefaultTableModel getModelRooms() {
        return modelRooms;
    }

    public DefaultTableModel getModelSubjects() {
        return modelSubjects;
    }

    public List<String> getListInstitutes() {
        return listInstitutes;
    }

    public List<Teacher> getListTeachers() {
        return listTeachers;
    }

    public List<Subject> getListSubjects() {
        return listSubjects;
    }

    public List<Classroom> getListRooms() {
        return listRooms;
    }

}
