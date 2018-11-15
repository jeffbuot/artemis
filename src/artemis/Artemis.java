/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Jeff
 */
public class Artemis {

    /**
     * p a b c d k
     *
     * @param args the command line arguments 42 1 23 4 8 10
     */
    static JFrame j = new JFrame();

    static StringBuilder bob = new StringBuilder();

    static {
        j.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        j.setTitle("Hello");
        j.add(new JLabel("<html>Hello<br/>World</html>") {
            {
                setFont(new Font("Segoe UI Light", 0, 16));
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        j.setSize(600, 400);
        j.setLocationRelativeTo(null);
        bob.append("Hello");
        bob.append("Hello");
        bob.append("Hello");
    }

    static class Something {

        private final String nothing;

        public Something(String nothing) {
            this.nothing = nothing;
        }

        public String getNothing() {
            return nothing;
        }

    }

    public static void main(String[] args) {
        List<Something> so = new ArrayList<>();
        so.add(new Something(""));
        so.add(new Something(""));
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(so);
        HashMap map = new HashMap();
        map.put("name", "Jeff");
        try {
            File com = new File("Oh.jasper");
            JasperCompileManager.compileReportToFile("Oh.jrxml", "Oh.jasper");
            JasperPrint jp = JasperFillManager.fillReport("Oh.jasper", map);
            JasperViewer jv = new JasperViewer(jp, false);
            jv.setTitle("Billing Statement View");
            jv.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
        }
//        System.out.println(FileUtils.getU);
//        String html = "<html>Hello<br/>World</html>";
//        ArtemisConfirm.showDialog(null, html);
        String i = "ejfw:e:g";
        System.out.println(bob.toString());
        j.setVisible(true);
        List<String> tf = new ArrayList<>();
        tf.add("1a");
        tf.add("2b");
        tf.add("3c");
        tf.add("4d");
        List<String> s = new ArrayList<>();
        s.add("b");
        s.add("c");
        s.add("d");
        s.add("e");
        s.add(0, "a");
        s.addAll(0, tf);

        s.forEach(System.out::println);

        // TODO code application logic here
        List<SchedTime> ts = new ArrayList<>();
        ts.add(new SchedTime(7.5, 9));
        ts.add(new SchedTime(8, 9.5));
        ts.add(new SchedTime(10, 11));
        ts.add(new SchedTime(11.5, 12));
        ts.add(new SchedTime(1, 2));
        ts.add(new SchedTime(2, 3));
        ts.add(new SchedTime(3, 5));

        double st = 11;
        double et = 13;
        ts.stream().filter(t -> !(t.getEndTime() <= st || t.getStartTime() >= et)).forEach(t -> t.print());
    }

    static class SchedTime {

        double startTime;
        double endTime;

        public SchedTime(double startTime, double endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public void print() {
            System.out.println("Start: " + startTime + "\tEnd: " + endTime);
        }

        public double getStartTime() {
            return startTime;
        }

        public double getEndTime() {
            return endTime;
        }

    }
}
