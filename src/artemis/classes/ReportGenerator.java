/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.dialogs.ArtemisMessage;
import artemis.dialogs.ArtemisPrompt;
import artemis.view_teachers_load.TeacherLoad;
import artemis.view_teachers_load.TeacherLoadDataSource;
import java.awt.Component;
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
import java.util.List;
import java.util.Map;
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
public class ReportGenerator extends Thread {

    final String schoolYear;
    final String institute;
    final Component parent;
    final List<TeacherLoad> load;

    public ReportGenerator(String schoolYear, String institute, Component parent, List<TeacherLoad> load) {
        this.schoolYear = schoolYear;
        this.institute = institute;
        this.parent = parent;
        this.load = load;
    }

    @Override
    public void run() {
        ArtemisPrompt load = new ArtemisPrompt(parent, "Initializing report...", IconFactory.initReport);
        load.showDialog();
        printReport();
        load.closeDialog();
    }

    private void printReport() {
        ArrayList<TeacherLoadDataSource> dataList = new ArrayList<>();
        load.forEach(teach -> {
            for (int i = 0; i < teach.getTableModel().getRowCount(); i++) {
                dataList.add(new TeacherLoadDataSource(teach.getTeacherName()
                        + " (" + teach.getTeacherId() + ")",
                        teach.getTableModel().getValueAt(i, 0).toString(),
                        teach.getTableModel().getValueAt(i, 1).toString(),
                        teach.getTableModel().getValueAt(i, 3).toString(),
                        teach.getTableModel().getValueAt(i, 4).toString(),
                        teach.getTableModel().getValueAt(i, 2).toString(),
                        teach.getTableModel().getValueAt(i, 5).toString(),
                        teach.getTableModel().getValueAt(i, 6).toString(),
                        teach.getTableModel().getValueAt(i, 7).toString(),
                        teach.getTotalFacultyLoad() + ""));
            }
        });

        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
        Map parameters = new HashMap();
        parameters.put("img_url", "doscst.png");
        parameters.put("sy", this.schoolYear);
        parameters.put("institute", institute);

        if (!new File("doscst.png").exists()) {
            try {
                writeImage();
            } catch (Exception ex) {
                ArtemisMessage.showDialog(parent, "Error generating the school logo.");
            }
        }

        if (!new File("SummaryLoad.jrxml").exists()) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(getClass().
                    getResourceAsStream("/artemis/print_forms/SummaryLoad.jrxml")));
            try {
                PrintWriter writer = new PrintWriter("SummaryLoad.jrxml", "UTF-8");
                String line;
                while ((line = buf.readLine()) != null) {
                    writer.println(line);
                }
                writer.close();
            } catch (IOException ex) {
                ArtemisMessage.showDialog(parent, "Fatal error occured while accessig report.");
            }
        }
        try {
            File com = new File("SummaryLoad.jasper");
            if (!com.exists()) {
                JasperCompileManager.compileReportToFile("SummaryLoad.jrxml", "SummaryLoad.jasper");
            }
            JasperPrint jp = JasperFillManager.fillReport("SummaryLoad.jasper", parameters, beanColDataSource);
            JasperViewer j = new JasperViewer(jp, false);
            j.setTitle("Teaching Load Summary");
            j.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private void writeImage() throws Exception {
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
}
