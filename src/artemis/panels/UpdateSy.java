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
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Jefferson
 */
public class UpdateSy extends javax.swing.JPanel {

    WebPopOver popOver;
    Connection con;
    int added = -1;

    /**
     * Creates new form AddSubject
     *
     * @param pop
     * @param con
     * @param sy
     * @param st
     */
    public UpdateSy(WebPopOver pop, Connection con, String sy, String st) {
        this.con = con;
        popOver = pop;
        initComponents();
        lblSY.setText(sy);
        cmbStatus.setSelectedItem(st);
        initHotkeys();
    }

    public static int update(Component comp, Connection con, String sy, String st) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        UpdateSy a = new UpdateSy(view, con, sy, st);
        view.add(a);
        view.show(comp);
        return a.getAdded();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
    }
    public int getAdded() {
        return added;
    }

    private void check(java.awt.event.ActionEvent evt) {
        try {
            con.createStatement().executeUpdate(String.format("update school_year set access_status = "
                    + "'%s' where sy_sem='%s'", cmbStatus.getSelectedItem(), lblSY.getText()));
            added = 1;
            popOver.dispose();
        } catch (SQLException e) {
            System.out.println(e);
            TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Something went wrong while fetching some data on database.");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        close_btn = new com.alee.laf.button.WebButton();
        lblSY = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox();

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("School Year Data Update");

        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSubmit.setText("Save");
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

        lblSY.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSY.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSY.setText(" ");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Set Access Status:");

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Published", "Locked", "Draft" }));
        cmbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSubmit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel)))
                .addGap(18, 18, 18)
                .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(58, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTitle))
                .addGap(18, 18, 18)
                .addComponent(lblSY, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 33, Short.MAX_VALUE)
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
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void close_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_btnActionPerformed
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSubmit;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JComboBox cmbStatus;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblSY;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables
}
