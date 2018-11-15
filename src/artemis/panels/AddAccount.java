/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.panels;

import artemis.classes.IconFactory;
import artemis.dialogs.ErrorDialog;
import com.alee.extended.window.WebPopOver;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.util.Cipher;
import com.jeff.util.DBUtil;
import java.awt.Component;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JMenuBar;

/**
 *
 * @author Jefferson
 */
public class AddAccount extends javax.swing.JPanel {

    WebPopOver popOver;
    Connection con;
    int added = 0;
    final boolean isUpdate;
    String id;

    /**
     * Creates new form AddSubject
     *
     * @param pop
     * @param con
     */
    public AddAccount(WebPopOver pop, Connection con) {
        this.con = con;
        popOver = pop;
        initComponents();
        initInstitutes();
        isUpdate = false;
        JMenuBar menu = new JMenuBar();
        chkPass.setVisible(false);
        initHotkeys();
    }

    public AddAccount(WebPopOver pop, Connection con, HashMap<String, String> hs) {
        this.con = con;
        popOver = pop;
        initComponents();
        isUpdate = true;
        initInstitutes();
        txtConfirmPassword.setEnabled(false);
        txtPassword.setEnabled(false);
        this.id = hs.get("id");
        lblTitle.setText("User Data Update");
        txtFirstname.setText(hs.get("firstname"));
        txtLastname.setText(hs.get("lastname"));
        cmbGender.setSelectedItem(hs.get("gender"));
        cmbAccessType.setSelectedItem(hs.get("accessType"));
        if (cmbAccessType.getSelectedItem().equals("Institute Admin")) {
            cmbInstitute.setEnabled(true);
            cmbInstitute.setSelectedItem(hs.get("institute"));
        }
        cmbAccessStatus.setSelectedItem(hs.get("accessStatus"));
        txtUsername.setText(hs.get("username"));
        lblPw.setText("New Password:");
        btnSubmit.setText("Save");
        initHotkeys();
    }

    private void initHotkeys() {
        HotkeyManager.registerHotkey(this, btnSubmit, Hotkey.CTRL_ENTER, new ButtonHotkeyRunnable(btnSubmit, 50), TooltipWay.trailing);
        HotkeyManager.registerHotkey(this, btnCancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(btnCancel, 50), TooltipWay.trailing);
    }

    public static int showDialog(Component comp, Connection con) {
        WebPopOver view = new WebPopOver(comp);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddAccount a = new AddAccount(view, con);
        view.add(a);
        view.show(comp);
        return a.getAdded();
    }

    public static int update(Component parent, Connection con, HashMap<String, String> hs) {
        WebPopOver view = new WebPopOver(parent);
        view.setCloseOnFocusLoss(false);
        view.setModal(true);
        view.setMargin(0);
        AddAccount a = new AddAccount(view, con, hs);
        view.add(a);
        view.show(parent);
        return a.getAdded();
    }

    public int getAdded() {
        return added;
    }

    private void initInstitutes() {
        cmbInstitute.removeAllItems();
        try {
            ResultSet rs = con.createStatement().executeQuery("Call showAllInstitutes");
            while (rs.next()) {
                cmbInstitute.addItem(rs.getString("id"));
            }
        } catch (SQLException e) {
            ErrorDialog.showDialog(e);
        }
    }

    private void check(java.awt.event.ActionEvent evt) {
        String firstname = txtFirstname.getText().trim();
        String lastname = txtLastname.getText().trim();
        String gender = cmbGender.getSelectedItem().toString();
        String officialPosition = cmbAccessType.getSelectedItem().toString();
        String accessStatus = cmbAccessStatus.getSelectedItem().toString();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String institute = cmbInstitute.isEnabled() ? cmbInstitute.getSelectedItem().toString() : "N/A";

        if (firstname.equals("")) {
            TooltipManager.showOneTimeTooltip(txtFirstname, null, IconFactory.infoIcon, "Please input the user's firstname.");
            return;
        }
        if (lastname.equals("")) {
            TooltipManager.showOneTimeTooltip(txtLastname, null, IconFactory.infoIcon, "Please input a lastname.");
            return;
        }
        if (username.equals("")) {
            TooltipManager.showOneTimeTooltip(txtUsername, null, IconFactory.infoIcon, "Please input a username.");
            return;
        }
        if (txtPassword.isEnabled()) {
            if (!password.equals(confirmPassword)) {
                TooltipManager.showOneTimeTooltip(txtConfirmPassword, null, IconFactory.infoIcon, "Your password does not match.");
                return;
            }
            if (password.length() < 8) {
                TooltipManager.showOneTimeTooltip(txtPassword, null, IconFactory.infoIcon, "Your password must contain atleast 8 characters.");
                return;
            }
        }
        //check for user conflict
        try {
            if (isUpdate) {
                String q;
                if (chkPass.isSelected()) {
                    q = String.format("update user_accounts set firstname = '%s',lastname='%s'"
                            + ",gender='%s', access_type='%s', institute = '%s',access_status='%s',username='%s',user_password='%s' where id = %s",
                            firstname, lastname, gender, officialPosition, institute, accessStatus, username, Cipher.encrypt(password), id);
                } else {
                    q = String.format("update user_accounts set firstname = '%s',lastname='%s'"
                            + ",gender='%s', access_type='%s', institute = '%s',access_status='%s',username='%s' where id = %s",
                            firstname, lastname, gender, officialPosition, institute, accessStatus, username, id);
                }
                System.out.println(q);
                con.createStatement().executeUpdate(q);
                added = 1;
                popOver.dispose();
            } else {
                if (DBUtil.toList(con, "call showAllUserAccounts", "username").stream().anyMatch(u -> u.equals(username))) {
                    TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Your already exist please use other username.");
                    return;
                }
                String update = String.format("call addUserAccount('%s','%s','%s','%s','%s','%s','%s','%s')",
                        firstname, lastname, gender, officialPosition, institute, accessStatus, username, Cipher.encrypt(password));
                con.createStatement().executeUpdate(update);
                added++;
                popOver.dispose();
            }
        } catch (SQLException e) {
            TooltipManager.showOneTimeTooltip(btnSubmit, null, IconFactory.infoIcon, "Something went wrong while fetching some data on database.");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        close_btn = new com.alee.laf.button.WebButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblPw = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtFirstname = new javax.swing.JTextField();
        txtLastname = new javax.swing.JTextField();
        cmbGender = new javax.swing.JComboBox();
        cmbAccessType = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        cmbAccessStatus = new javax.swing.JComboBox();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmPassword = new javax.swing.JPasswordField();
        chkPass = new javax.swing.JCheckBox();
        lblInstitute = new javax.swing.JLabel();
        cmbInstitute = new javax.swing.JComboBox();

        lblTitle.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblTitle.setText("Register New Client User");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Firstname:");

        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSubmit.setText("Register");
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

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Lastname:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Gender:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Access Type:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Access Status:");

        lblPw.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPw.setText("Password:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Confirm Password:");

        txtFirstname.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        txtLastname.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        cmbGender.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Male", "Female" }));

        cmbAccessType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbAccessType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "View Only", "Institute Admin", "Main Administrator" }));
        cmbAccessType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbAccessTypeActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Username:");

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        cmbAccessStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbAccessStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Active", "Locked", "Draft" }));

        chkPass.setText("Change password");
        chkPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPassActionPerformed(evt);
            }
        });

        lblInstitute.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblInstitute.setText("Institute:");
        lblInstitute.setEnabled(false);

        cmbInstitute.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbInstitute.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "View Only", "Institute Admin", "Main Administrator" }));
        cmbInstitute.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(close_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblInstitute, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPw, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(chkPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnSubmit)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtFirstname, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtLastname, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtUsername, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(cmbAccessStatus, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cmbAccessType, javax.swing.GroupLayout.Alignment.LEADING, 0, 187, Short.MAX_VALUE)
                                        .addComponent(cmbGender, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(0, 205, Short.MAX_VALUE))
                                .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtConfirmPassword, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(cmbInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 48, Short.MAX_VALUE))))
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
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFirstname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLastname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cmbAccessType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cmbAccessStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPw, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkPass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnCancel)
                        .addComponent(btnSubmit)))
                .addGap(10, 10, 10))
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
        popOver.dispose();
    }//GEN-LAST:event_close_btnActionPerformed

    private void chkPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPassActionPerformed
        txtConfirmPassword.setEnabled(chkPass.isSelected());
        txtPassword.setEnabled(chkPass.isSelected());
    }//GEN-LAST:event_chkPassActionPerformed

    private void cmbAccessTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAccessTypeActionPerformed
        if (cmbAccessType.getSelectedIndex() != -1) {
            cmbInstitute.setEnabled(cmbAccessType.getSelectedItem().toString().equals("Institute Admin"));
            lblInstitute.setEnabled(cmbAccessType.getSelectedItem().toString().equals("Institute Admin"));
        }
    }//GEN-LAST:event_cmbAccessTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JCheckBox chkPass;
    private com.alee.laf.button.WebButton close_btn;
    private javax.swing.JComboBox cmbAccessStatus;
    private javax.swing.JComboBox cmbAccessType;
    private javax.swing.JComboBox cmbGender;
    private javax.swing.JComboBox cmbInstitute;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblInstitute;
    private javax.swing.JLabel lblPw;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtFirstname;
    private javax.swing.JTextField txtLastname;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
