/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import artemis.classes.Subject;
import artemis.classes.Teacher;
import artemis.sched_board_module.BoardListUpdate;
import artemis.sched_board_module.ScheduleRequestData;
import artemis.sched_board_module.SuggestList;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jeff
 */
public class AddSchedule extends javax.swing.JPanel {

    /**
     * Creates new form Add
     *
     * @param time
     * @param room
     * @param d
     */
    WebPopOver p;
    BoardListUpdate blu;
    String instituteId;
    String sy, startTime, room, subject = "", teacher_id = "";
    List<ScheduleRequestData> srd = new ArrayList<>();

    public AddSchedule(WebPopOver p, String time, List<String> ends, String room,
            String d, String inst, String sy, BoardListUpdate blu, boolean isOwner, String instOwner) {
        this.p = p;
        this.sy = sy;
        this.room = room;
        this.startTime = time.split("-")[0];
        this.instituteId = inst;
        this.blu = blu;
        initComponents();
        lblReminder.setVisible(!isOwner);
        if (!isOwner) {
            btnAdd.setText("Request To " + instOwner);
            lblReminder.setText("Reminder: You cannot directly pin a schedule in this room. "
                    + "You must request to the owner (" + instOwner + ").");
        }
        cmbEnd.removeAllItems();
        lblInstitute.setText(inst);
        cmbProgram.removeAllItems();
        blu.getListPrograms().stream().filter(pr -> pr.getInstituteId().equals(inst)).forEach(pr -> cmbProgram.addItem(pr.getId()));
        ends.forEach(e -> cmbEnd.addItem(e));
        lblTitle.setText("Add " + room + " Class Schedule");
        lblStartTime.setText("From: " + time.split("-")[0] + "    To:");
        HotkeyManager.registerHotkey(p, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
        selectDay(d);
    }

    private void save() {
        String start = convertTime(startTime);
        String end = convertTime(cmbEnd.getSelectedItem().toString());
        String section = cmbProgram.getSelectedItem().toString() + "-" + cmbYearLevel.getSelectedItem().toString() + spinLetter.getValue();
        String desc = "Pin schedule in room " + room + " (" + startTime + " - " + cmbEnd.getSelectedItem().toString() + ")";
        String session = optLect.isSelected() ? "LEC" : "LAB";
        //call addSchedule("7:30","9:00","Thu","A","IT113","MS1","2","IBPA","2015-2016 1st Semester");
        if (d1.isSelected()) {
            //for monday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Mon", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Mon", subject, room, teacher_id, section, sy));
        }
        if (d2.isSelected()) {
            //for tuesday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Tue", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Tue", subject, room, teacher_id, section, sy));
        }
        if (d3.isSelected()) {
            //for wednesday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Wed", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Wed", subject, room, teacher_id, section, sy));
        }
        if (d4.isSelected()) {
            //for thursday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Thu", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Thu", subject, room, teacher_id, section, sy));
        }
        if (d5.isSelected()) {
            //for friday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Fri", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Fri", subject, room, teacher_id, section, sy));
        }
        if (d6.isSelected()) {
            //for saturday
            String q = String.format("call addSchedule('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    start, end, "Sat", section, subject, room, teacher_id, instituteId, sy, session);
            srd.add(new ScheduleRequestData(desc, q, start, end, "Sat", subject, room, teacher_id, section, sy));
        }
        p.dispose();
    }

    public List<ScheduleRequestData> getSrd() {
        return srd;
    }

    private boolean areDaysSelected() {
        return d1.isSelected() | d2.isSelected() | d3.isSelected()
                | d4.isSelected() | d5.isSelected() | d6.isSelected();
    }

    private void selectDay(String d) {
        if (d.equals("Monday")) {
            d1.setSelected(true);
        }
        if (d.equals("Tuesday")) {
            d2.setSelected(true);
        }
        if (d.equals("Wednesday")) {
            d3.setSelected(true);
        }
        if (d.equals("Thursday")) {
            d4.setSelected(true);
        }
        if (d.equals("Friday")) {
            d5.setSelected(true);
        }
        if (d.equals("Saturday")) {
            d6.setSelected(true);
        }
    }

    public static List<ScheduleRequestData> showDialog(Component owner, String time, List<String> ends,
            String room, String day, String institute, String sy, BoardListUpdate blu, boolean isOwner, String instOwner) {
        final WebPopOver popOver = new WebPopOver(owner);
        popOver.setCloseOnFocusLoss(false);
        popOver.setModal(true);
        popOver.setMargin(0);
        popOver.setLayout(new VerticalFlowLayout());
        AddSchedule a = new AddSchedule(popOver, time, ends, room, day, institute, sy, blu, isOwner, instOwner);
        popOver.add(a);
        popOver.show(owner);
        return a.getSrd();
    }

    public String convertTime(String time) {
        int h = Integer.parseInt(time.split(":")[0]);
        String amp = time.split(":")[1];
        String t;
        if (amp.endsWith("PM")) {
            if (h == 12) {
                t = "12:" + amp.split(" ")[0];
            } else {
                t = h + 12 + ":" + amp.split(" ")[0];
            }
        } else {
            if (h == 12) {
                t = "00:" + amp.split(" ")[0];
            } else {
                t = h + ":" + amp.split(" ")[0];
            }
        }
        return t;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        cmbEnd = new javax.swing.JComboBox();
        lblStartTime = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        webButton1 = new com.alee.laf.button.WebButton();
        lblInstitute = new javax.swing.JLabel();
        btnInstructor = new javax.swing.JButton();
        btnSubject = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        cmbYearLevel = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        d1 = new javax.swing.JCheckBox();
        d2 = new javax.swing.JCheckBox();
        d3 = new javax.swing.JCheckBox();
        d4 = new javax.swing.JCheckBox();
        d5 = new javax.swing.JCheckBox();
        d6 = new javax.swing.JCheckBox();
        btnCheck1 = new javax.swing.JButton();
        btnCheck2 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        cmbProgram = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        spinLetter = new javax.swing.JSpinner();
        lblReminder = new javax.swing.JLabel();
        optLect = new javax.swing.JRadioButton();
        optLab = new javax.swing.JRadioButton();

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Subject:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Instructor:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Time:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Institute:");

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnAdd.setText("Submit");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Day:");

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        cmbEnd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbEnd.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblStartTime.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStartTime.setText("From: 10:30 AM    To:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Section:      (");

        jPanel1.setBackground(new java.awt.Color(91, 91, 91));

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("[room] Class Schedule");

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/exit.png"))); // NOI18N
        webButton1.setUndecorated(true);
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(webButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        lblInstitute.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblInstitute.setText("[institute id]");

        btnInstructor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnInstructor.setText("--Select a teacher--");
        btnInstructor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInstructor.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        btnInstructor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInstructorActionPerformed(evt);
            }
        });

        btnSubject.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSubject.setText("--Select a subject--");
        btnSubject.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSubject.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        btnSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubjectActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Year Level:");

        cmbYearLevel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbYearLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));
        cmbYearLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearLevelActionPerformed(evt);
            }
        });

        d1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d1.setText("Mon");
        jPanel2.add(d1);

        d2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d2.setText("Tue");
        jPanel2.add(d2);

        d3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d3.setText("Wed");
        jPanel2.add(d3);

        d4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d4.setText("Thu");
        jPanel2.add(d4);

        d5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d5.setText("Fri");
        jPanel2.add(d5);

        d6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        d6.setText("Sat");
        jPanel2.add(d6);

        btnCheck1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        btnCheck1.setText("MWF");
        btnCheck1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheck1ActionPerformed(evt);
            }
        });
        jPanel2.add(btnCheck1);

        btnCheck2.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        btnCheck2.setText("TTH");
        btnCheck2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheck2ActionPerformed(evt);
            }
        });
        jPanel2.add(btnCheck2);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Program:");

        cmbProgram.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbProgram.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText(")");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Sec:");

        spinLetter.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        List<String> chars = new ArrayList<>();

        for(char l = 'A';l<='Z';l++){
            chars.add(l+"");
        }
        spinLetter.setModel(new javax.swing.SpinnerListModel(chars.toArray()));

        lblReminder.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblReminder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/reminder.png"))); // NOI18N
        lblReminder.setText("Reminder:");

        buttonGroup1.add(optLect);
        optLect.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        optLect.setSelected(true);
        optLect.setText("Lecture");

        buttonGroup1.add(optLab);
        optLab.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        optLab.setText("Laboratory");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblReminder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(optLect, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(optLab))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnAdd)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnCancel))
                                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnInstructor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSubject, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(lblStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cmbEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel12)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cmbProgram, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel11)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cmbYearLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel14)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(spinLetter, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(5, 5, 5)))
                                    .addComponent(jLabel13))
                                .addComponent(lblInstitute, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(20, 24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbYearLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbProgram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinLetter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInstructor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSubject))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optLab)
                    .addComponent(optLect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(lblReminder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnCancel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (cmbProgram.getItemCount() < 1) {
            TooltipManager.showOneTimeTooltip(cmbProgram, null, IconFactory.infoIcon, "There is no program to select.");
            return;
        }
        if (!areDaysSelected()) {
            TooltipManager.showOneTimeTooltip(d1, null, IconFactory.infoIcon, "Please select a day.");
            return;
        }
        if (teacher_id.trim().isEmpty()) {
            TooltipManager.showOneTimeTooltip(btnInstructor, null, IconFactory.infoIcon, "Please select a teacher.");
            return;
        }
        if (subject.trim().isEmpty()) {
            TooltipManager.showOneTimeTooltip(btnSubject, null, IconFactory.infoIcon, "Please select a subject.");
            return;
        }
        save();
        p.dispose();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        p.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        p.dispose();
    }//GEN-LAST:event_webButton1ActionPerformed

    private void btnInstructorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInstructorActionPerformed
        List<Teacher> te = blu.getListTeachers().stream().filter(t -> t.getInstituteId().equals(instituteId)).collect(Collectors.toList());
        String[] val = SuggestList.showDialog(btnInstructor, te, Teacher.class);
        teacher_id = val[0];
        btnInstructor.setText(teacher_id.isEmpty() ? "--Select a teacher--" : val[1]);
    }//GEN-LAST:event_btnInstructorActionPerformed

    private void btnSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubjectActionPerformed
        List<Subject> te = blu.getListSubjects().stream().filter(s -> s.getInstituteId().equals(instituteId)).collect(Collectors.toList());
        String[] val = SuggestList.showDialog(btnSubject, te, Subject.class);
        subject = val[0];
        btnSubject.setText(subject.isEmpty() ? "--Select a subject--" : subject);
        blu.getListSubjects().stream().filter(sub -> sub.getId().equals(subject)).
                forEach(s -> {
                    optLect.setEnabled(s.hasLect());
                    optLab.setEnabled(s.hasLab());
                    optLab.setSelected(s.hasLab());
                    optLect.setSelected(s.hasLect());
                });
    }//GEN-LAST:event_btnSubjectActionPerformed

    private void cmbYearLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbYearLevelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbYearLevelActionPerformed

    private void btnCheck1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheck1ActionPerformed
        d1.setSelected(true);
        d2.setSelected(false);
        d3.setSelected(true);
        d4.setSelected(false);
        d5.setSelected(true);
        d6.setSelected(false);
    }//GEN-LAST:event_btnCheck1ActionPerformed

    private void btnCheck2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheck2ActionPerformed
        d1.setSelected(false);
        d2.setSelected(true);
        d3.setSelected(false);
        d4.setSelected(true);
        d5.setSelected(false);
        d6.setSelected(false);
    }//GEN-LAST:event_btnCheck2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCheck1;
    private javax.swing.JButton btnCheck2;
    private javax.swing.JButton btnInstructor;
    private javax.swing.JButton btnSubject;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cmbEnd;
    private javax.swing.JComboBox cmbProgram;
    private javax.swing.JComboBox cmbYearLevel;
    private javax.swing.JCheckBox d1;
    private javax.swing.JCheckBox d2;
    private javax.swing.JCheckBox d3;
    private javax.swing.JCheckBox d4;
    private javax.swing.JCheckBox d5;
    private javax.swing.JCheckBox d6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblInstitute;
    private javax.swing.JLabel lblReminder;
    private javax.swing.JLabel lblStartTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JRadioButton optLab;
    private javax.swing.JRadioButton optLect;
    private javax.swing.JSpinner spinLetter;
    private com.alee.laf.button.WebButton webButton1;
    // End of variables declaration//GEN-END:variables
}
