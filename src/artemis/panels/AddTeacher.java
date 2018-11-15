/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import artemis.classes.QueryData;
import artemis.sched_board_module.BoardListUpdate;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.util.DBUtil;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class AddTeacher extends javax.swing.JPanel {

    WebPopOver popOver;
    Connection con;
    int added = 0;
    String id;
    String queryUpdate[] = new String[]{"", ""};
    boolean isUpdate;
    boolean client = false;
    BoardListUpdate blu;

    List<QueryData> queryData = new ArrayList<>();

    /**
     * Creates new form AddSubject
     *
     * @param pop
     * @param con
     */
    //add-server side
    public AddTeacher(WebPopOver pop, Connection con) {
        this.con = con;
        popOver = pop;
        initComponents();
        setInstitutes();
        isUpdate = false;
        initHotkeys();
    }

    //add-client side
    public AddTeacher(WebPopOver pop, BoardListUpdate blu, String inst) {
        this.blu = blu;
        popOver = pop;
        initComponents();
        cmbInstitute.removeAllItems();
        cmbInstitute.addItem(inst);
        isUpdate = false;
        client = true;
        initHotkeys();
        btnSubmit.setText("Save");
        btnCancel.setText("Close");
    }

    //update
    public AddTeacher(WebPopOver pop, Connection con, HashMap<String, String> hs) {
        this.con = con;
        popOver = pop;
        initComponents();
        setInstitutes();
        btnSubmit.setText("Save");
        isUpdate = true;
        lblTitle.setText("Teacher Data Update");
        this.id = hs.get("id");
        txtId.setText(id);
        txtId.setEditable(false);
        txtFn.setText(hs.get("firstname"));
        txtLn.setText(hs.get("lastname"));
        cmbGender.setSelectedItem(hs.get("gender"));
        cmbInstitute.setSelectedItem(hs.get("instituteId"));
        exit.setSelected(true);
        exit.setVisible(false);
        initHotkeys();
    }

    //update client-side
    public AddTeacher(WebPopOver pop, HashMap<String, String> hs) {
        popOver = pop;
        isUpdate = true;
        client = true;
        initComponents();
        cmbInstitute.removeAllItems();
        cmbInstitute.addItem(hs.get("instituteId"));
        btnSubmit.setText("Save");
        btnCancel.setText("Close");
        isUpdate = true;
        lblTitle.setText("Teacher Data Update");
        this.id = hs.get("id");
        txtId.setText(id);
        txtId.setEditable(false);
        txtFn.setText(hs.get("firstname"));
        txtLn.setText(hs.get("lastname"));
        cmbGender.setSelectedItem(hs.get("gender"));
        exit.setSelected(true);
        exit.setVisible(false);
        initHotkeys();
    }

    public static int showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddTeacher a = new AddTeacher(view, con);
        view.add(a);
        view.show(comp);
        return a.getSubject_added();
    }

    public static List<QueryData> clientShowDialog(Component comp, BoardListUpdate blu, String ins) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddTeacher a = new AddTeacher(view, blu, ins);
        view.add(a);
        view.show(comp);
        return a.getQueryData();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, close_btn, Hotkey.ESCAPE, new ButtonHotkeyRunnable(close_btn, 50), TooltipWay.trailing);
    }

    public static int update(Component comp, Connection con, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddTeacher a = new AddTeacher(view, con, hs);
        view.add(a);
        view.show(comp);
        return a.getSubject_added();
    }

    public static String[] updateByClient(Component comp, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddTeacher a = new AddTeacher(view, hs);
        view.add(a);
        view.show(comp);
        return a.getQueryUpdate();
    }

    private void setInstitutesByBlu() {
        cmbInstitute.removeAllItems();
        blu.getListInstitutes().forEach(ins -> cmbInstitute.addItem(ins));
    }

    private void setInstitutes() {
        cmbInstitute.removeAllItems();
        try {
            DBUtil.toList(con, "call showAllInstitutes", "id").forEach(i -> cmbInstitute.addItem(i));
        } catch (SQLException e) {
        }
    }

    public int getSubject_added() {
        return added;
    }

    public List<QueryData> getQueryData() {
        return queryData;
    }

    public String[] getQueryUpdate() {
        return queryUpdate;
    }

    private void check(java.awt.event.ActionEvent evt) {
        id = txtId.getText().trim();
        String fn = txtFn.getText().trim();
        String ln = txtLn.getText().trim();
        if (id.equals("")) {
            TooltipManager.showOneTimeTooltip(txtId, null, IconFactory.infoIcon, "Please enter a teacher's id.");
            return;
        }
        if (fn.equals("")) {
            TooltipManager.showOneTimeTooltip(txtFn, null, IconFactory.infoIcon, "Please input a firstname.");
            return;
        }
        if (ln.equals("")) {
            TooltipManager.showOneTimeTooltip(txtLn, null, IconFactory.infoIcon, "Please input a lastname.");
            return;
        }
        if (cmbInstitute.getSelectedIndex() == -1) {
            TooltipManager.showOneTimeTooltip(cmbInstitute, null, IconFactory.infoIcon, "There is no institute selected.");
            return;
        }
        if (client) {
            if (isUpdate) {
                String q = String.format("update teachers set firstname = '%s', lastname = '%s',gender = '%s',"
                        + " institute_id = '%s' where id = '%s'",
                        fn, ln, cmbGender.getSelectedItem(), cmbInstitute.getSelectedItem(), id);
                this.queryUpdate = new String[]{q, fn + " " + ln};
                popOver.dispose();
            } else {
                if (blu.getListTeachers().stream().map(t -> t.getId()).anyMatch(t -> t.equals(txtId.getText().trim()))) {
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "The teacher you are adding is already registered.");
                    return;
                }
                String q = String.format("call addTeacher('%s','%s','%s','%s','%s')", id,
                        fn, ln, cmbGender.getSelectedItem(), cmbInstitute.getSelectedItem());
                queryData.add(new QueryData(id, "Add new teacher (" + fn + " " + ln + ").", q));
                if (exit.isSelected()) {
                    popOver.dispose();
                } else {
                    txtId.setText("");
                    txtFn.setText("");
                    txtLn.setText("");
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Teacher is added request queue.");
                }
            }
        } else {
            try {
                if (isUpdate) {
                    String q = String.format("update teachers set firstname = '%s', lastname = '%s',gender = '%s',"
                            + "institute_id = '%s' where id = '%s'",
                            fn, ln, cmbGender.getSelectedItem(), cmbInstitute.getSelectedItem(), id);
                    con.createStatement().executeUpdate(q);
                    added = 1;
                    popOver.dispose();
                } else {
                    boolean same_id = DBUtil.toList(con, "call showAllTeachers",
                            "id").stream().anyMatch(i -> i.toUpperCase().equals(id));
                    boolean same_fn = DBUtil.toList(con, "call showAllTeachers",
                            "firstname").stream().anyMatch(i -> i.toUpperCase().equals(fn.toUpperCase()));
                    boolean same_ln = DBUtil.toList(con, "call showAllTeachers",
                            "lastname").stream().anyMatch(i -> i.toUpperCase().equals(ln.toUpperCase()));
                    boolean same_gen = DBUtil.toList(con, "call showAllTeachers",
                            "gender").stream().anyMatch(i -> i.equals(cmbGender.getSelectedItem() + ""));
                    boolean same_inst = DBUtil.toList(con, "call showAllTeachers",
                            "institute_id").stream().anyMatch(i -> i.equals(cmbInstitute.getSelectedItem() + ""));
                    if (same_id & same_fn & same_ln & same_gen & same_inst) {
                        TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "The teacher you are adding is already registered.");
                        return;
                    }
                    con.createStatement().executeUpdate(String.format("call addTeacher('%s','%s','%s','%s','%s')", id,
                            fn, ln, cmbGender.getSelectedItem(), cmbInstitute.getSelectedItem()));
                    added++;
                    if (exit.isSelected()) {
                        popOver.dispose();
                    } else {
                        txtId.setText("");
                        txtFn.setText("");
                        txtLn.setText("");
                        TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Teacher is successfully added.");
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
                TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Something went wrong while fetching some data on database.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtFn = new javax.swing.JTextField();
        exit = new javax.swing.JCheckBox();
        btnSubmit = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        close_btn = new com.alee.laf.button.WebButton();
        jLabel5 = new javax.swing.JLabel();
        txtLn = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        cmbInstitute = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("Add New Teacher");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Firstname:");

        txtFn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        exit.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        exit.setText("Exit after submit");
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });

        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        close_btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/cross2.png"))); // NOI18N
        close_btn.setUndecorated(true);
        close_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                close_btnActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Lastname:");

        txtLn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Gender:");

        cmbGender.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Male", "Female" }));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Institute:");

        cmbInstitute.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Teacher ID:");

        txtId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnSubmit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(exit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbGender, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbInstitute, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(90, 90, 90))
                            .addComponent(txtLn, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtFn, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTitle))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtFn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtLn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbGender)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(exit)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSubmit))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        check(evt);
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (isUpdate) {
            added = -1;
        }
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void close_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_btnActionPerformed
        if (isUpdate) {
            added = -1;
        }
        if (client) {
            queryData.clear();
        }
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
        btnSubmit.setText(exit.isSelected() ? "Save & Exit" : "Save");
    }//GEN-LAST:event_exitActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSubmit;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JComboBox cmbGender;
    private javax.swing.JComboBox cmbInstitute;
    private javax.swing.JCheckBox exit;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextField txtFn;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtLn;
    // End of variables declaration//GEN-END:variables
}
