/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.dialogs;

/**
 *
 * @author Jeff
 */
public class ErrorDialog extends javax.swing.JDialog {

    /**
     * Creates new form ErrorDialog
     *
     * @param ex
     */
    public ErrorDialog(Exception ex) {
        initComponents();
        setLocationRelativeTo(null);
        setTitle(ex.getClass().getSimpleName());
        for (StackTraceElement e : ex.getStackTrace()) {
            txtMsg.append(e.toString() + "\n");
        }
    }

    public static void showDialog(Exception ex) {
        new ErrorDialog(ex).setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txtMsg = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        lblReminder = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Info for IT staff troubleshooting.");

        txtMsg.setEditable(false);
        txtMsg.setBackground(new java.awt.Color(71, 71, 71));
        txtMsg.setColumns(20);
        txtMsg.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        txtMsg.setForeground(new java.awt.Color(255, 102, 102));
        txtMsg.setRows(5);
        jScrollPane1.setViewportView(txtMsg);

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblReminder.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblReminder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/reminder.png"))); // NOI18N
        lblReminder.setText("Reminder: This dialog is for IT staff troubleshooting purpose.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblReminder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(lblReminder))
                .addGap(4, 4, 4))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblReminder;
    private javax.swing.JTextArea txtMsg;
    // End of variables declaration//GEN-END:variables
}
