/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import java.awt.Component;

/**
 *
 * @author Jeff
 */
public class PortConfig extends javax.swing.JPanel {

    /**
     * Creates new form ConnectionConfiguration
     */
    WebPopOver p;
    int port;

    public PortConfig(WebPopOver p, int port) {
        this.p = p;
        this.port = port;
        initComponents();
        txtPort.setText(port + "");
        initHotkeys();
    }

    public int getPort() {
        return this.port;
    }

    public static int showDialog(Component owner, int port) {
        final WebPopOver popOver = new WebPopOver(owner);
        popOver.setCloseOnFocusLoss(false);
        popOver.setModal(true);
        popOver.setMargin(0);
        popOver.setLayout(new VerticalFlowLayout());
        PortConfig ca = new PortConfig(popOver, port);
        popOver.add(ca);
        popOver.show(owner);
        //returns -1 if the cancel button is clicked
        return ca.getPort();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSave, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSave, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        txtPort = new com.alee.laf.text.WebTextField();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        webButton1 = new com.alee.laf.button.WebButton();

        jButton2.setText("jButton2");

        txtPort.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPort.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        txtPort.setHideInputPromptOnFocus(false);
        txtPort.setInputPrompt("Input server's port...");

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel1.setText("Set Server's Port");

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/cross2.png"))); // NOI18N
        webButton1.setUndecorated(true);
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancel)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                        .addGap(24, 24, 24)
                        .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(webButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSave))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        port = -1;
        p.dispose();
    }//GEN-LAST:event_webButton1ActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        port = -1;
        p.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            port = Integer.parseInt(txtPort.getText());
            if (port < 1000 || port > 65000) {
                throw new NumberFormatException();
            } else {
                p.dispose();
            }
        } catch (NumberFormatException e) {
            TooltipManager.showOneTimeTooltip(txtPort, null, IconFactory.infoIcon, "Invalid port.(Port protocol is 1000 < port <65000 )");
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private com.alee.laf.text.WebTextField txtPort;
    private com.alee.laf.button.WebButton webButton1;
    // End of variables declaration//GEN-END:variables
}
