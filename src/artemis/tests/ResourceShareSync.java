/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.tests;

import com.jeff.jdbc.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe class resource sharing
 *
 * @author Jefferson
 */
public class ResourceShareSync {

    class Paper extends Thread {

        String paperId;
        int type;

        Paper(String id, int type) {
            this.paperId = id;
            this.type = type;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                if (type == 0) {
                    log(paperId + ": print counts " + (i + 1));
                } else if (type == 1) {
                    lag(paperId + ": print counts " + (i + 1));
                } else {
                    leg(paperId + ": print counts " + (i + 1));
                }
            }
        }
    }

    private synchronized void log(String s) {
        try {
            con.createStatement().executeUpdate("insert into logger values('(log) "+s+"')");
            System.out.println("insert into logger values('(log) "+s+"')");
        } catch (SQLException ex) {
            Logger.getLogger(ResourceShareSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void lag(String s) {
        try {
            con.createStatement().executeUpdate("insert into logger values('(lag) "+s+"')");
            System.out.println("insert into logger values('(lag) "+s+"')");
        } catch (SQLException ex) {
            Logger.getLogger(ResourceShareSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void leg(String s) {
        try {
            con.createStatement().executeUpdate("insert into logger values('(leg) "+s+"')");
            System.out.println("insert into logger values('(leg) "+s+"')");
        } catch (SQLException ex) {
            Logger.getLogger(ResourceShareSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    Connection con;
    List<Paper> papers;

    public ResourceShareSync() throws ClassNotFoundException {
        try {
            this.con = Database.connect("root", "", "localhost", "test");
        } catch (SQLException ex) {
            Logger.getLogger(ResourceShareSync.class.getName()).log(Level.SEVERE, null, ex);
        }
        papers = new ArrayList<>();
    }

    public void startThreads() {
        for (int i = 0; i < 100; i++) {
            papers.add(new Paper("Paper " + (i + 1), i % 3));
        }
        papers.forEach(p -> p.start());
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ResourceShareSync rss = new ResourceShareSync();
        rss.startThreads();
    }
}
