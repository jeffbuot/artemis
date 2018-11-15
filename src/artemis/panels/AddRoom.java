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
public class AddRoom extends javax.swing.JPanel {

    WebPopOver popOver;
    Connection con;
    int roomAdded = 0;
    final boolean isUpdate;
    boolean isClient = false;
    BoardListUpdate blu;
    List<QueryData> queryData = new ArrayList<>();
    String queryUpdate[] = new String[]{"", ""};

    /**
     * Creates new form AddRoom
     *
     * @param pop
     * @param con
     */
    // <editor-fold defaultstate="collapsed" desc="Server Type Constructors">          
    public AddRoom(WebPopOver pop, Connection con) {
        this.con = con;
        popOver = pop;
        initComponents();
        initInstitutes();
        isUpdate = false;
        initHotkeys();
    }

    public AddRoom(WebPopOver pop, Connection con, HashMap<String, String> hs) {
        this.con = con;
        popOver = pop;
        initComponents();
        initInstitutes();
        isUpdate = true;
        btnImport.setVisible(false);
        lblTitle.setText("Room Data Update");
        btnSubmit.setText("Save");
        exit.setSelected(true);
        exit.setVisible(false);
        txtID.setEditable(false);
        txtID.setText(hs.get("id"));
        txtDesc.setText(hs.get("desc"));
        cmbOwner.setSelectedItem(hs.get("facilitator"));
        cmbStatus.setSelectedItem(hs.get("accessStatus"));
        initHotkeys();
    }
     // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Client Type Constructors">   
    // client add room
    public AddRoom(WebPopOver pop, BoardListUpdate blu, String institute) {
        popOver = pop;
        this.blu = blu;
        initComponents();
        isClient = true;
        isUpdate = false;
        cmbOwner.removeAllItems();
        cmbOwner.addItem(institute);
        initHotkeys();
    }

    //client update room
    public AddRoom(WebPopOver pop, HashMap<String, String> hs) {
        popOver = pop;
        initComponents();
        cmbOwner.removeAllItems();
        cmbOwner.addItem(hs.get("facilitator"));
        isUpdate = true;
        isClient = true;

        btnImport.setVisible(false);
        lblTitle.setText("Room Data Update");
        btnSubmit.setText("Save");
        exit.setSelected(true);
        exit.setVisible(false);
        txtID.setEditable(false);
        txtID.setText(hs.get("id"));
        txtDesc.setText(hs.get("desc"));
        cmbOwner.setSelectedItem(hs.get("facilitator"));
        cmbStatus.setSelectedItem(hs.get("accessStatus"));
        initHotkeys();
    }
// </editor-fold> 

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
    }

    public static List<QueryData> clientShowDialog(Component comp, BoardListUpdate blu, String ins) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddRoom a = new AddRoom(view, blu, ins);
        view.add(a);
        view.show(comp);
        return a.getQueryData();
    }

    public static String[] clientUpdate(Component comp, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddRoom a = new AddRoom(view, hs);
        view.add(a);
        view.show(comp);
        return a.getQueryUpdate();
    }

    public static int showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddRoom a = new AddRoom(view, con);
        view.add(a);
        view.show(comp);
        return a.getRoomAdded();
    }

    public static int update(Component comp, Connection con, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddRoom a = new AddRoom(view, con, hs);
        view.add(a);
        view.show(comp);
        return a.getRoomAdded();
    }

    private void initInstitutes() {
        cmbOwner.removeAllItems();
        try {
            ResultSet rs = con.createStatement().executeQuery("Call showAllInstitutes");
            while (rs.next()) {
                cmbOwner.addItem(rs.getString("id"));
            }
        } catch (SQLException e) {
            ErrorDialog.showDialog(e);
        }
    }

    public int getRoomAdded() {
        return roomAdded;
    }

    public String[] getQueryUpdate() {
        return queryUpdate;
    }

    public List<QueryData> getQueryData() {
        return queryData;
    }

    private void check(java.awt.event.ActionEvent evt) {
        if (txtID.getText().trim().equals("")) {
            TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "Please input a room id.");
            return;
        }
        if (txtDesc.getText().trim().equals("")) {
            TooltipManager.showOneTimeTooltip(txtDesc, null, IconFactory.infoIcon, "Please input a description.");
            return;
        }
        String room_id = txtID.getText().toUpperCase();
        if (isClient) {
            if (isUpdate) {
                String q = String.format("update rooms set description = '%s', institute_id = '%s',access_status = '%s' where id = '%s'",
                        txtDesc.getText(), cmbOwner.getSelectedItem(), cmbStatus.getSelectedItem(), room_id);
                queryUpdate = new String[]{q,txtDesc.getText()};
                popOver.dispose();
            } else {
                if (blu.getListInstitutes().stream().anyMatch(i -> i.equals(room_id))) {
                    TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "The room id already exists.");
                    return;
                }
                String q = String.format("Call addRoom('%s','%s','%s','%s')",
                        room_id, txtDesc.getText(), cmbOwner.getSelectedItem(), cmbStatus.getSelectedItem().toString());
                queryData.add(new QueryData(room_id, "Add subject (" + txtDesc.getText() + ")", q));
                if (exit.isSelected()) {
                    popOver.dispose();
                } else {
                    txtID.setText("");
                    txtDesc.setText("");
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Room successfully added.");
                }
            }
        } else {
            try {
                if (isUpdate) {
                    String q = String.format("update rooms set description = '%s', institute_id = '%s',access_status = '%s' where id = '%s'",
                            txtDesc.getText(), cmbOwner.getSelectedItem(), cmbStatus.getSelectedItem(), room_id);
                    con.createStatement().executeUpdate(q);
                    roomAdded = 1;
                    popOver.dispose();
                } else {
                    if (DBUtil.toList(con, "call showAllRooms", "id").stream().anyMatch(id -> id.equals(room_id))) {
                        TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "The room id already exists.");
                        return;
                    }
                    con.createStatement().executeUpdate(String.format("Call addRoom('%s','%s','%s','%s')",
                            room_id, txtDesc.getText(), cmbOwner.getSelectedItem(), cmbStatus.getSelectedItem().toString()));
                    roomAdded++;
                    if (exit.isSelected()) {
                        popOver.dispose();
                    } else {
                        txtID.setText("");
                        txtDesc.setText("");
                        TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Room successfully added.");
                    }
                }
            } catch (SQLException e) {
                TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Something went wrong while fetching some data on database.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        close_btn = new com.alee.laf.button.WebButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        cmbOwner = new javax.swing.JComboBox();
        btnCancel = new javax.swing.JButton();
        btnSubmit = new javax.swing.JButton();
        exit = new javax.swing.JCheckBox();
        btnImport = new javax.swing.JButton();
        cmbStatus = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("Add New Room");

        close_btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/cross2.png"))); // NOI18N
        close_btn.setUndecorated(true);
        close_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                close_btnActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Room ID:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Description:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Facilitator:");

        txtID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtDesc.setColumns(20);
        txtDesc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        jScrollPane2.setViewportView(txtDesc);

        cmbOwner.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        exit.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        exit.setText("Exit after submit");

        btnImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Import.png"))); // NOI18N
        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Active", "Inactive" }));
        cmbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Access Status:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtID)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                            .addComponent(cmbOwner, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnImport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSubmit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnCancel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(exit)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbOwner))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(exit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSubmit)
                    .addComponent(btnImport))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void close_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_btnActionPerformed
        if (isUpdate) {
            roomAdded = -1;
        }
        if (isClient) {
            queryData.clear();
        }
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (isUpdate) {
            roomAdded = -1;
        }
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        check(evt);
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed

    }//GEN-LAST:event_btnImportActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnSubmit;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JComboBox cmbOwner;
    private javax.swing.JComboBox cmbStatus;
    private javax.swing.JCheckBox exit;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextArea txtDesc;
    private javax.swing.JTextField txtID;
    // End of variables declaration//GEN-END:variables
}
