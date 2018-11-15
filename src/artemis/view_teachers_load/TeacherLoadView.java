/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.view_teachers_load;

import artemis.classes.IconFactory;
import artemis.dialogs.ArtemisMessage;
import artemis.dialogs.ArtemisPrompt;
import com.alee.managers.tooltip.TooltipManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Jefferson
 */
public class TeacherLoadView extends javax.swing.JDialog {

    /**
     * Creates new form TeacherLoadView
     *
     * @param parent
     * @param model
     */
    DefaultTableModel tSched;
    String name;
    String sy;

    public TeacherLoadView(Component parent, DefaultTableModel model, String name, String sy) {
        tSched = model;
        this.name = name;
        this.sy = sy;
        initComponents();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension((int) (d.getWidth() * 0.85), (int) (d.getHeight() * 0.7)));
//        setModal(true);
        setLocationRelativeTo(parent);
        tableSched.setModel(tSched);
        lblName.setText("Faculty Name: " + this.name);
        lblTotalLoad.setText("TotalLoad: " + totalLoad());
    }

    private int totalLoad() {
        int t = 0;
        int lec = 0;
        int lab = 0;
        for (int x = 0; x < tableSched.getRowCount(); x++) {
            lec += Integer.parseInt((String) tableSched.getValueAt(x, 3));
            lab += Integer.parseInt((String) tableSched.getValueAt(x, 4));
        }
        return t = lec + lab;
    }

    public static void showDialog(Component comp, DefaultTableModel mod, String name, String sy) {
        TeacherLoadView tlv = new TeacherLoadView(comp, mod, name, sy);
        tlv.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableSched = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        lblTotalLoad = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblName.setText("[Teacher's Info]");

        tableSched.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tableSched.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tableSched);

        btnClose.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/adding_cancel.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnPrint.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Print - 01.png"))); // NOI18N
        btnPrint.setText("Print");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        lblTotalLoad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalLoad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalLoad.setText("[Summary]");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
                    .addComponent(lblTotalLoad, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(lblName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalLoad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
      if(tableSched.getRowCount()==0){
                      TooltipManager.showOneTimeTooltip(btnPrint, null, IconFactory.infoIcon,
                    "The schedule is empty.");
          return;
      }
        new InitReport().start();
    }//GEN-LAST:event_btnPrintActionPerformed

    private Component me() {
        return this;
    }

    private class InitReport extends Thread {

        @Override
        public void run() {
            ArtemisPrompt load = new ArtemisPrompt(me(), "Initializing report...", IconFactory.initReport);
            load.showDialog();
            printReport();
            load.closeDialog();
        }
    }

    private void printReport() {
        ArrayList<TeacherLoadDataSource> dataList = new ArrayList<>();
        for (int i = 0; i < tableSched.getRowCount(); i++) {
            dataList.add(new TeacherLoadDataSource("",tableSched.getValueAt(i, 0).toString(),
                    tableSched.getValueAt(i, 1).toString(),
                    tableSched.getValueAt(i, 3).toString(), tableSched.getValueAt(i, 4).toString(),
                    tableSched.getValueAt(i, 2).toString(),
                    tableSched.getValueAt(i, 5).toString(), tableSched.getValueAt(i, 6).toString(),
                    tableSched.getValueAt(i, 7).toString(),""));
        }

        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
        Map parameters = new HashMap();
        parameters.put("img_url", "doscst.png");
        parameters.put("sy", sy);
        parameters.put("teacher_name", this.name);
        parameters.put("totalLoad", totalLoad() + "");

        if (!new File("doscst.png").exists()) {
            try {
                writeImage();
            } catch (Exception ex) {
                ArtemisMessage.showDialog(this, "Error generating the school logo.");
            }
        }

        if (!new File("TeacherLoad.jrxml").exists()) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(getClass().
                    getResourceAsStream("/artemis/print_forms/TeacherLoad.jrxml")));
            try {
                PrintWriter writer = new PrintWriter("TeacherLoad.jrxml", "UTF-8");
                String line = "";
                while ((line = buf.readLine()) != null) {
                    writer.println(line);
                }
                writer.close();
            } catch (IOException ex) {
                ArtemisMessage.showDialog(this, "Fatal error occured while accessig report.");
            }
        }
        try {
            File com = new File("TeacherLoad.jasper");
            if (!com.exists()) {
                JasperCompileManager.compileReportToFile("TeacherLoad.jrxml", "TeacherLoad.jasper");
            }
            JasperPrint jp = JasperFillManager.fillReport("TeacherLoad.jasper", parameters, beanColDataSource);
            JasperViewer.viewReport(jp, false);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    public void writeImage() throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
            stream = getClass().getResourceAsStream("/artemis/img/doscst.png");//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if (stream == null) {
                throw new Exception("Cannot get resource doscst.png from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream("doscst.png");
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            stream.close();
            resStreamOut.close();
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPrint;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblTotalLoad;
    private javax.swing.JTable tableSched;
    // End of variables declaration//GEN-END:variables
}
