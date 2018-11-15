/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import artemis.classes.QueryData;
import artemis.dialogs.ErrorDialog;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class AddProgram extends javax.swing.JPanel {

    WebPopOver popOver;
    BoardListUpdate blu;
    Connection con;
    int subject_added = 0;
    final boolean isUpdate;
    boolean client = false;
    String queryUpdate[] = new String[]{"", ""};
    List<QueryData> queryData = new ArrayList<>();

    /**
     * Creates new form AddSubject
     *
     * @param pop
     * @param con
     */
    // <editor-fold defaultstate="collapsed" desc="Server side dialog">                         
    //add
    public AddProgram(WebPopOver pop, Connection con) {
        this.con = con;
        popOver = pop;
        isUpdate = false;
        initComponents();
        initInstitutes();
        initHotkeys();
    }

    //update
    public AddProgram(WebPopOver pop, Connection con, HashMap<String, String> hs) {
        this.con = con;
        popOver = pop;
        isUpdate = true;
        initComponents();
        initInstitutes();
        btnSubmit.setText("Save");
        btnImport.setVisible(false);
        exit.setSelected(true);
        exit.setVisible(false);
        lblTitle.setText("Program Data Update");
        txtID.setEditable(false);
        txtID.setText(hs.get("id"));
        txtDesc.setText(hs.get("desc"));
        cmbProgramFacilitator.setSelectedItem(hs.get("facilitator"));
        initHotkeys();
    }

    public static int showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddProgram a = new AddProgram(view, con);
        view.add(a);
        view.show(comp);
        return a.getSubject_added();
    }

    public static int update(Component comp, Connection con, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddProgram a = new AddProgram(view, con, hs);
        view.add(a);
        view.show(comp);
        return a.getSubject_added();
    }
// </editor-fold> 

    //client addSubject
    public AddProgram(WebPopOver pop, BoardListUpdate blu, String ins) {
        this.blu = blu;
        popOver = pop;
        isUpdate = false;
        client = true;
        initComponents();
        cmbProgramFacilitator.removeAllItems();
        cmbProgramFacilitator.addItem(ins);
        initHotkeys();
        btnSubmit.setText("Save");
        btnCancel.setText("Close");
    }

    //client update
    public AddProgram(WebPopOver pop, HashMap<String, String> hs, BoardListUpdate blu) {
        this.blu = blu;
        popOver = pop;
        isUpdate = true;
        client = true;
        initComponents();

        setInstitutesByBlu();
        cmbProgramFacilitator.setSelectedItem(hs.get("program_facilitator"));

        btnSubmit.setText("Save");
        btnImport.setVisible(false);
        exit.setSelected(true);
        exit.setVisible(false);
        lblTitle.setText("Program Data Update");
        txtID.setEditable(false);
        txtID.setText(hs.get("id"));
        txtDesc.setText(hs.get("desc"));
        cmbProgramFacilitator.setSelectedItem(hs.get("facilitator"));
        initHotkeys();
    }

    public static String[] clientUpdate(Component comp, HashMap<String, String> hs, BoardListUpdate blu) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddProgram a = new AddProgram(view, hs, blu);
        view.add(a);
        view.show(comp);
        return a.getQueryUpdate();
    }

    public static List<QueryData> clientShowDialog(Component comp, BoardListUpdate blu, String ins) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddProgram a = new AddProgram(view, blu, ins);
        view.add(a);
        view.show(comp);
        return a.getQueryData();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, close_btn, Hotkey.ESCAPE, new ButtonHotkeyRunnable(close_btn, 50), TooltipWay.trailing);
    }

    private void initInstitutes() {
        cmbProgramFacilitator.removeAllItems();
        try {
            ResultSet rs = con.createStatement().executeQuery("Call showAllInstitutes");
            while (rs.next()) {
                cmbProgramFacilitator.addItem(rs.getString("id"));
            }
        } catch (SQLException e) {
            ErrorDialog.showDialog(e);
        }
    }

    private void setInstitutesByBlu() {
        cmbProgramFacilitator.removeAllItems();
        blu.getListInstitutes().forEach(ins -> cmbProgramFacilitator.addItem(ins));
    }

    public int getSubject_added() {
        return subject_added;
    }

    public List<QueryData> getQueryData() {
        return queryData;
    }

    private void check(java.awt.event.ActionEvent evt) {
        String id = txtID.getText().trim().toUpperCase();
        String desc = txtDesc.getText().trim();
        if (id.equals("")) {
            TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "Please input a program id.");
            return;
        }
        if (desc.equals("")) {
            TooltipManager.showOneTimeTooltip(txtDesc, null, IconFactory.infoIcon, "Please input a description.");
            return;
        }
        if (client) {
            if (isUpdate) {
                String q = String.format(" update programs set description = '%s',"
                        + "facilitator='%s' where id = '%s'",
                        desc, cmbProgramFacilitator.getSelectedItem(), txtID.getText());
                this.queryUpdate = new String[]{q, desc};
                popOver.dispose();
            } else {
                if (blu.getListSubjects().stream().map(s -> s.getId()).anyMatch(s -> s.equals(id))) {
                    TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "The program id already exists.");
                    return;
                }

                String q = String.format("CALL addProgram('%s','%s','%s')",
                        id, desc, cmbProgramFacilitator.getSelectedItem());
                queryData.add(new QueryData(id, "Add new Program/Course (" + desc + ").", q));
                if (exit.isSelected()) {
                    popOver.dispose();
                } else {
                    txtID.setText("");
                    txtDesc.setText("");
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Program is added to request queue.");
                }
            }
        } else {
            try {
                if (isUpdate) {
                    String q = String.format("update programs set description = '%s',"
                            + "facilitator='%s' where id = '%s'",
                            desc, cmbProgramFacilitator.getSelectedItem(), txtID.getText());
                    con.createStatement().executeUpdate(q);
                    subject_added = 1;
                    popOver.dispose();
                } else {
                    if (DBUtil.toList(con, "call showAllPrograms", "id").stream().anyMatch(i -> i.equals(id))) {
                        TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "The program id already exists.");
                        return;
                    }
                    con.createStatement().executeUpdate(String.format("CALL "
                            + "addProgram('%s','%s','%s')",
                            id, desc, cmbProgramFacilitator.getSelectedItem()));
                    subject_added++;
                    if (exit.isSelected()) {
                        popOver.dispose();
                    } else {
                        txtID.setText("");
                        txtDesc.setText("");
                        TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Program successfully added.");
                    }
                }
            } catch (SQLException e) {
                TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, e.getMessage());
            }
        }
    }

    public String[] getQueryUpdate() {
        return queryUpdate;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupClassSpecification = new javax.swing.ButtonGroup();
        lblTitle = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        exit = new javax.swing.JCheckBox();
        btnSubmit = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        close_btn = new com.alee.laf.button.WebButton();
        btnImport = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        cmbProgramFacilitator = new javax.swing.JComboBox();

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("Add New Program / Course");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("ID/Abbreviation:");

        txtID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtDesc.setColumns(20);
        txtDesc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        jScrollPane2.setViewportView(txtDesc);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Description:");

        exit.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        exit.setText("Exit after submit");

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

        btnImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Import.png"))); // NOI18N
        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Program Facilitator:");

        cmbProgramFacilitator.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addComponent(txtID)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(exit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(142, 142, 142))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnImport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSubmit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnCancel))
                            .addComponent(cmbProgramFacilitator, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTitle))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbProgramFacilitator))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(exit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSubmit)
                    .addComponent(btnImport))
                .addGap(10, 10, 10))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        check(evt);
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (isUpdate) {
            subject_added = -1;
        }
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void close_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_btnActionPerformed
        if (isUpdate) {
            subject_added = -1;
        }
        if (client) {
            queryData.clear();
        }
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnImportActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnSubmit;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JComboBox cmbProgramFacilitator;
    private javax.swing.JCheckBox exit;
    private javax.swing.ButtonGroup groupClassSpecification;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextArea txtDesc;
    private javax.swing.JTextField txtID;
    // End of variables declaration//GEN-END:variables
}
