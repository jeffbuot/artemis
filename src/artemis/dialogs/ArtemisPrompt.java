/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.dialogs;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.WebPopOver;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 *
 * @author Jeff
 */
public class ArtemisPrompt extends javax.swing.JPanel {

    /**
     * Creates new form ArtemisPrompt
     *
     * @param prompt
     */
    final WebPopOver popOver;
    final Component parent;

    public ArtemisPrompt(Component owner, String prompt) {
        //setting the popup
        this.parent = owner;
        popOver = new WebPopOver(owner);
        popOver.setCloseOnFocusLoss(false);
        popOver.setMargin(0);
        popOver.setMovable(false);
        popOver.setLayout(new VerticalFlowLayout());
        popOver.add(this);

        setBackground(Color.white);
        initComponents();
        lblPrompt.setText(prompt);
        int w = prompt.length() * 6 + 50;
        setPreferredSize(new Dimension(w < 170 ? 170 : w, 78));
    }

    public ArtemisPrompt(Component owner, String prompt, ImageIcon icon) {
        //setting the popup
        this.parent = owner;
        popOver = new WebPopOver(owner);
        popOver.setCloseOnFocusLoss(false);
        popOver.setMargin(0);
        popOver.setMovable(false);
        popOver.setLayout(new VerticalFlowLayout());
        popOver.add(this);

        setBackground(Color.white);
        initComponents();
        lblIcon.setIcon(icon);
        lblPrompt.setText(prompt);
        int w = prompt.length() * 6 + 50;
        setPreferredSize(new Dimension(w < 170 ? 170 : w, 78));
    }

    public void showDialog() {
        if (parent == null) {
            int x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - (int) popOver.getPreferredSize().getWidth() / 2;
            int y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) popOver.getPreferredSize().getHeight() / 2;
            popOver.show(x, y);
        } else {
            popOver.show(parent);
        }
    }

    public void closeDialog() {
        popOver.dispose();
    }

    public void showAutoClose() {
        if (parent == null) {
            int x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - (int) popOver.getPreferredSize().getWidth() / 2;
            int y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) popOver.getPreferredSize().getHeight() / 2;
            popOver.show(x, y);
        } else {
            popOver.show(parent);
        }
        new E().start();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblPrompt = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();

        lblPrompt.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblPrompt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPrompt.setText("Connecting...");

        lblIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Hand-03.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblPrompt, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
            .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(lblPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        ArtemisPrompt a = new ArtemisPrompt(null, "HIhlekh");
        a.showDialog();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblPrompt;
    // End of variables declaration//GEN-END:variables
private class E extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            closeDialog();
        }
    }
}
