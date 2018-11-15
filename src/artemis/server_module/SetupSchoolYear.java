/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.server_module;

import artemis.dialogs.ArtemisMessage;
import artemis.panels.AddSY;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.util.TabModel;
import java.awt.Component;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;

/**
 *
 * @author Jefferson
 */
public class SetupSchoolYear extends javax.swing.JPanel {

    WebPopOver popOver;
    Connection con;
    int added = 0;
    boolean insideServer;
    ImageIcon infoIcon = new ImageIcon(getClass().getResource("/artemis/img/Message-Warning.png"));

    /**
     * Creates new form AddSubject
     *
     * @param pop
     * @param con
     * @param insideServer
     */
    public SetupSchoolYear(WebPopOver pop, Connection con, boolean insideServer) {
        this.insideServer = insideServer;
        this.con = con;
        popOver = pop;
        if (insideServer) {
            TooltipManager.showOneTimeTooltip(btnActivate, null, infoIcon, "There is no active school year, please settle it up.");
        }
        initComponents();
        refreshTable();
    }

    private void refreshTable() {
        try {
            tableSY.setModel(TabModel.getDefaulTableModel(con.createStatement().executeQuery("Select * from school_year where access_status='Locked' or access_status='Published'")));
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public static void showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        SetupSchoolYear a = new SetupSchoolYear(view, con, comp != null);
        view.add(a);
        if (comp == null) {
            int x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - (int) view.getPreferredSize().getWidth() / 2;
            int y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) view.getPreferredSize().getHeight() / 2;
            view.show(x, y);
        } else {
            view.show(comp);
        }
    }

    public int getAdded() {
        return added;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        close_btn = new com.alee.laf.button.WebButton();
        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableSY = new javax.swing.JTable();
        btnActivate = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(91, 91, 91));

        close_btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/exit.png"))); // NOI18N
        close_btn.setUndecorated(true);
        close_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                close_btnActionPerformed(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle.setText("Setup Sem/School Year");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(close_btn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tableSY.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "SY Sem", "Access Status"
            }
        ));
        tableSY.setRowHeight(26);
        jScrollPane1.setViewportView(tableSY);

        btnActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Ignition switch warning.png"))); // NOI18N
        btnActivate.setText("Set As Current Active SY");
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/new sy.png"))); // NOI18N
        btnAdd.setText("Add School Year");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnCancel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 112, Short.MAX_VALUE)
                        .addComponent(btnActivate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnActivate)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        popOver.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void close_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_btnActionPerformed
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void btnActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateActionPerformed
        if (tableSY.getSelectedRow() != -1) {
            try {
                //activate a school year
                String selectedSY = tableSY.getValueAt(tableSY.getSelectedRow(), 0).toString();
                ResultSet rs = con.createStatement().executeQuery("call showAllSchoolYear");
                while (rs.next()) {
                    String tempSY = rs.getString("sy_sem");
                    String tempAS = rs.getString("access_status");
                    if (!selectedSY.equals(tempSY) & tempAS.equals("Active")) {
                        String q = "update school_year set access_status = 'Locked' where sy_sem='" + tempSY + "'";
                        con.createStatement().executeUpdate(q);
                    } else if (selectedSY.equals(tempSY)) {
                        String q = "update school_year set access_status = 'Active' where sy_sem='" + tempSY + "'";
                        con.createStatement().executeUpdate(q);
                    }
                }
                popOver.dispose();
                if (!insideServer) {
                    java.awt.EventQueue.invokeLater(() -> {
                        new MainServer(selectedSY).setVisible(true);
                    });
                }
            } catch (SQLException ex) {
                ArtemisMessage.showDialog(popOver, "Something went wrong while fetching some data on database.");
                System.out.println(ex);
            }
        } else {
            TooltipManager.showOneTimeTooltip(btnActivate, null, infoIcon, "There is no selected row on the table.");
        }
    }//GEN-LAST:event_btnActivateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        AddSY.showDialog(popOver, con);
        refreshTable();
    }//GEN-LAST:event_btnAddActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tableSY;
    // End of variables declaration//GEN-END:variables
}
