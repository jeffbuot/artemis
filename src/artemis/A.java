/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis;

import java.awt.Color;
import java.io.File;
import java.util.Calendar;

/**
 *
 * @author Jefferson
 */
public class A {

    public static String DB_NAME = "artemis_db";
    public final static String INVALID = "~$~invalid";
    public final static String VALID = "valid~$~";
    public final static String LOCKED = "~$~locked~$~";
    public static final int MARGIN = 20;

    public static Color getForegroundFor(Color background) {
        float[] hsb = Color.RGBtoHSB(background.getRed(), background.getGreen(), background.getBlue(), null);
        float brightness = hsb[2];
        if (brightness < 0.5) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    public static void main(String[] args) {
//        for (int t = 7; t < 22; t++) {
//            String st = prop(t) + ":00 " + amp(t) + "-" + prop(t) + ":30 " + amp(t);
//            System.out.println(st);
//            System.out.println("Start: " + st.split("-")[0] + " End: " + st.split("-")[1]);
//            System.out.println(prop(t) + ":30 " + amp(t) + "-" + prop(t + 1) + ":00 " + amp(t + 1));
//        }

//        Scanner in = new Scanner(System.in);
//        int s = 4, e = 6;
//        while (true) {
//            System.out.print("starting range: ");
//            int rs = Integer.parseInt(in.next());
//            System.out.print("ending range: ");
//            int re = Integer.parseInt(in.next());
//            if (rs <= s & re <= s || rs >= e & re >= e) {
//                System.out.println("Not conflict");
//            } else {
//                System.out.println("Conflict");
//            }
//        }
//        String u = "JeffersonsBuots";
//        for (int i = u.length() - 1; i > 0; i--) {
//            String left = u.substring(0, i) + "-";
//            String right = u.substring(i);
//            System.out.println("left = " + left + ", right = " + right);
//        }
        System.out.println(new File("jeff_sprofile.png").exists());
        Calendar dateTime = Calendar.getInstance();
        String time = String.format("%tr", dateTime);
        String date = String.format("%1$ta, %1$tb %1$te, %1$ty ", dateTime);
        System.out.println(date + "(" + time + ")");
//        JFileChooser jf = new JFileChooser();
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image File", "jpg", "png", "bmp", "gif");
//        jf.setFileFilter(filter);
//        jf.showDialog(null, "Select");
////        BufferedImage img = ImageIO.read(jf.getSelectedFile().getAbsoluteFile());
//        
//        System.out.println(ImageIO.write(img, "jpg", clients.get(0).out));
    }

    public static String getDateTime() {
        Calendar dateTime = Calendar.getInstance();
        String date = String.format("%1$ta, %1$tb %1$te, %1$ty ", dateTime);
        String time = String.format("%tr", dateTime);
        return date + "  " + time;
    }

    private static String[] getTime(String w) {
        return w.split("-");
    }

    private static int prop(int t) {
        int hour = t % 12;
        if (hour == 0) {
            hour = 12;
        }
        return hour;
    }

    private static String amp(int t) {
        return t < 12 ? "AM" : "PM";
    }

    //proper 10:00 AM, 10:00 PM
    //database 10:00:00, 22:00:00
    public static String toDatabaseTime(String properTime) {
        String[] a = properTime.split(" ");
        String[] b = a[0].split(":");
        int h = Integer.parseInt(b[0]);
        int m = Integer.parseInt(b[1]);
        int hh = (a[1].equals("PM") & h != 12 ? h + 12 : h);
        return (hh < 10 ? "0" + hh : hh) + ":" + (m == 0 ? "00" : m) + ":00";
    }

    public static String toProper(String databaseTime) {
        String a[] = databaseTime.split(":");
        int h = Integer.parseInt(a[0]);
        int m = Integer.parseInt(a[1]);
        String amp = h >= 12 ? "PM" : "AM";
        return (h > 12 ? h - 12 : h) + ":" + (m == 0 ? "00" : m) + " " + amp;
    }
}
