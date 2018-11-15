/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemis.classes;

import artemis.sched_board_module.Schedule;
import com.jeff.util.FileUtil;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Jeff
 * @author Capslock
 */
public class XMLParser {

    public static void saveData(File file, NetCon netCon) {
        XStream xstream = new XStream();
        xstream.alias("NetCon", NetCon.class);
        try {
            String xml = xstream.toXML(netCon);
            FileUtil.saveFile(xml, file);
        } catch (IOException e) {
        }
    }

    public static NetCon loadNetCon(File file) {
        XStream xstream = new XStream();
        xstream.alias("NetCon", NetCon.class);

        try {
            String xml = FileUtil.readFile(file);
            return (NetCon) xstream.fromXML(xml);
        } catch (IOException ex) {
            //save the default account if config does not exist
            NetCon n = new NetCon("localhost", 47888);
            saveData(file, n);
            return n;
        }
    }

    public static void saveData(File file, DBAccount db) {
        XStream xstream = new XStream();
        xstream.alias("DBAccount", DBAccount.class);
        try {
            String xml = xstream.toXML(db);
            FileUtil.saveFile(xml, file);
        } catch (IOException e) {
        }
    }

    public static DBAccount loadDBAccount(File file) {
        XStream xstream = new XStream();
        xstream.alias("DBAccount", DBAccount.class);

        try {
            String xml = FileUtil.readFile(file);
            return (DBAccount) xstream.fromXML(xml);
        } catch (IOException ex) {
            //save the default account if config does not exist
            DBAccount db = new DBAccount("", "root", "localhost");
            saveData(file, db);
            return db;
        }
    }

    public synchronized static void appendSched(File file, Schedule s) {
        ArrayList<Schedule> s1 = loadSched(file);
        s1.add(s);
        saveData(file, s1);
    }

    public static void saveData(File file, ArrayList<Schedule> sc) {
        XStream xstream = new XStream();
        xstream.alias("Schedule", Schedule.class);
        try {
            String xml = xstream.toXML(sc);
            FileUtil.saveFile(xml, file);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static ArrayList<Schedule> loadSched(File file) {
        XStream xstream = new XStream();
        xstream.alias("Schedule", Schedule.class);

        try {
            String xml = FileUtil.readFile(file);
            return (ArrayList<Schedule>) xstream.fromXML(xml);
        } catch (IOException ex) {
            //save the default account if config does not exist
            saveData(file, new ArrayList<>());
            return new ArrayList<>();
        }
    }
}
