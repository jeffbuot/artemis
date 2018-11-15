/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.util.DBUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JColorChooser;

/**
 *
 * @author Jefferson
 */
public class AddInstitute extends javax.swing.JPanel {

    Color c;
    Color[] col = {Color.BLACK, Color.BLUE, new Color(102, 51, 0), Color.GRAY, Color.GREEN, Color.ORANGE, Color.RED, Color.WHITE, Color.YELLOW};
    WebPopOver popOver;
    Connection con;
    int added = 0;
    final boolean isUpdate;
    boolean inited = false;

    /**
     * Creates new form AddInstitute
     *
     * @param pop
     * @param con
     */
    public AddInstitute(WebPopOver pop, Connection con) {
        this.con = con;
        popOver = pop;
        c = Color.WHITE;
        isUpdate = false;
        initComponents();
        inited = true;
        initHotkeys();
    }

    public AddInstitute(WebPopOver pop, Connection con, HashMap<String, String> hs) {
        this.con = con;
        popOver = pop;
        c = Color.WHITE;
        isUpdate = true;
        initComponents();
        lblTitle.setText("Institute Data Update");
        btnSubmit.setText("Save");
        txtID.setText(hs.get("id"));
        txtDesc.setText(hs.get("desc"));
        String[] col = hs.get("color").split(":");
        Color iColor = new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
        cmbColor.setSelectedItem(getColorDescription(iColor));
        panColor.setBackground(iColor);
        txtID.setEditable(false);
        cmbStatus.setSelectedItem(hs.get("accessStatus"));
        inited = true;
        exit.setSelected(true);
        exit.setVisible(false);
        initHotkeys();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
    }
    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public static int showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddInstitute a = new AddInstitute(view, con);
        view.add(a);
        view.show(comp);
        return a.getAdded();
    }

    public static int update(Component comp, Connection con, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddInstitute a = new AddInstitute(view, con, hs);
        view.add(a);
        view.show(comp);
        return a.getAdded();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jColorChooser1 = new javax.swing.JColorChooser();
        webButton1 = new com.alee.laf.button.WebButton();
        lblTitle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        panColor = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        cmbColor = new javax.swing.JComboBox();
        btnCancel = new javax.swing.JButton();
        btnSubmit = new javax.swing.JButton();
        exit = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox();

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/cross2.png"))); // NOI18N
        webButton1.setUndecorated(true);
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("Add New Institute");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Color Code:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("ID:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Description:");

        txtID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        panColor.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout panColorLayout = new javax.swing.GroupLayout(panColor);
        panColor.setLayout(panColorLayout);
        panColorLayout.setHorizontalGroup(
            panColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );
        panColorLayout.setVerticalGroup(
            panColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtDesc.setColumns(20);
        txtDesc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        jScrollPane2.setViewportView(txtDesc);

        cmbColor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbColor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BLACK", "BLUE", "GRAY", "GREEN", "ORANGE", "RED", "VIOLET", "WHITE", "YELLOW", "CUSTOM" }));
        cmbColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbColorActionPerformed(evt);
            }
        });

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
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Access Status:");

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Active", "Inactive" }));
        cmbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(txtID, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbColor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(exit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSubmit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTitle)
                    .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbColor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbStatus))
                .addGap(18, 28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSubmit)
                    .addComponent(exit)
                    .addComponent(btnCancel))
                .addGap(10, 10, 10))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        if (isUpdate) {
            added = -1;
        }
        popOver.dispose();
    }//GEN-LAST:event_webButton1ActionPerformed

    private void cmbColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbColorActionPerformed
        if (inited) {
            changeColor();
        }
    }//GEN-LAST:event_cmbColorActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        check(evt);
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (isUpdate) {
            added = -1;
        }
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusActionPerformed

    private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exitActionPerformed
    private void changeColor() {
        switch (cmbColor.getSelectedItem().toString()) {
            case "BLACK":
                panColor.setBackground(Color.BLACK);
                break;
            case "BLUE":
                panColor.setBackground(new Color(0, 102, 255));
                break;
            case "GRAY":
                panColor.setBackground(Color.GRAY);
                break;
            case "GREEN":
                panColor.setBackground(new Color(0, 204, 102));
                break;
            case "ORANGE":
                panColor.setBackground(Color.ORANGE);
                break;
            case "RED":
                panColor.setBackground(new Color(255, 51, 51));
                break;
            case "VIOLET":
                panColor.setBackground(new Color(153, 0, 153));
                break;
            case "WHITE":
                panColor.setBackground(Color.WHITE);
                break;
            case "YELLOW":
                panColor.setBackground(new Color(255, 255, 0));
                break;
            default:
                Color c = JColorChooser.showDialog(null, "Choose a Color", panColor.getBackground());
                //Note it returns null if it is cancelled
                panColor.setBackground(c);
                panColor.validate();
        }
    }

    private String getColorDescription(Color c) {
        if (c.equals(Color.BLACK)) {
            return "BLACK";
        }
        if (c.equals(new Color(0, 102, 255))) {
            return "BLUE";
        }
        if (c.equals(Color.GRAY)) {
            return "GRAY";
        }
        if (c.equals(new Color(0, 204, 102))) {
            return "GREEN";
        }
        if (c.equals(Color.ORANGE)) {
            return "ORANGE";
        }
        if (c.equals(new Color(255, 51, 51))) {
            return "RED";
        }
        if (c.equals(new Color(153, 0, 153))) {
            return "VIOLET";
        }
        if (c.equals(Color.WHITE)) {
            return "WHITE";
        }
        if (c.equals(new Color(255, 255, 0))) {
            return "YELLOW";
        }
        return "CUSTOM";
    }

    private void check(ActionEvent evt) {
        String id = txtID.getText().trim().toUpperCase();
        String desc = txtDesc.getText().trim();
        Color c = panColor.getBackground();
        String color = c.getRed() + ":" + c.getGreen() + ":" + c.getBlue();
        if (id.equals("")) {
            TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "Please input institute id.");
            return;
        }
        if (desc.equals("")) {
            TooltipManager.showOneTimeTooltip(txtDesc, null, IconFactory.infoIcon, "Please input institute description.");
            return;
        }
        try {
            if (isUpdate) {
                String q = String.format(" update institutes set description = '%s', color = '%s',access_status ='%s' where id = '%s'",
                        desc, color, cmbStatus.getSelectedItem().toString(), id);
                con.createStatement().executeUpdate(q);
                added = 1;
                popOver.dispose();
            } else {
                if (DBUtil.toList(con, "call showAllInstitutes", "id").stream().anyMatch(i -> i.equals(id))) {
                    TooltipManager.showOneTimeTooltip(txtID, null, IconFactory.infoIcon, "The institute id already exists.");
                    return;
                }
                con.createStatement().executeUpdate(String.format("Call addInstitute('%s','%s','%s','%s')",
                        id, desc, color, cmbStatus.getSelectedItem().toString()));
                added++;
                if (exit.isSelected()) {
                    popOver.dispose();
                } else {
                    txtID.setText("");
                    txtDesc.setText("");
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.succIcon, "Institute successfully added.");
                }
            }
        } catch (SQLException ex) {
            TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Something went wrong while fetching some data on database.");
            System.out.println(ex);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox cmbColor;
    private javax.swing.JComboBox cmbStatus;
    private javax.swing.JCheckBox exit;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel panColor;
    private javax.swing.JTextArea txtDesc;
    private javax.swing.JTextField txtID;
    private com.alee.laf.button.WebButton webButton1;
    // End of variables declaration//GEN-END:variables

}
