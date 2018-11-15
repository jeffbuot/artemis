/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.panels.QueryResultViewer;
import com.alee.extended.panel.CenterPanel;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.time.ClockType;
import com.alee.extended.time.WebClock;
import com.alee.laf.button.WebButton;
import com.alee.managers.notification.NotificationManager;
import com.alee.managers.notification.WebNotificationPopup;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jeff
 */
public class Notification {

    public static int LONG = 5000;
    public static int SHORT = 2500;

    public static void showPopup(String message, ImageIcon icon) {
        final WebNotificationPopup notificationPopup = new WebNotificationPopup();
        notificationPopup.setIcon(icon);
        notificationPopup.setDisplayTime(LONG);

        final WebClock clock = new WebClock();
        clock.setClockType(ClockType.timer);
        clock.setTimeLeft(7000);
//                clock.setTimePattern ( "'This notification will close in' ss 'seconds'" );
        clock.setTimePattern("'" + message + "'");
        notificationPopup.setContent(new GroupPanel(clock));

        NotificationManager.showNotification(notificationPopup);
        clock.start();
    }

    public static void showPopup(String message, ImageIcon icon, int time) {
        final WebNotificationPopup notificationPopup = new WebNotificationPopup();
        notificationPopup.setIcon(icon);
        notificationPopup.setDisplayTime(time);

        final WebClock clock = new WebClock();
        clock.setClockType(ClockType.timer);
        clock.setTimeLeft(7000);
//                clock.setTimePattern ( "'This notification will close in' ss 'seconds'" );
        clock.setTimePattern("'" + message + "'");
        notificationPopup.setContent(new GroupPanel(clock));

        NotificationManager.showNotification(notificationPopup);
        clock.start();
    }

    public static void notifyWithLogger(Component comp, List<QueryData> qd, String message,boolean p) {
        final WebNotificationPopup notificationPopup = new WebNotificationPopup();
        notificationPopup.setIcon(LoggerFile.notifyIcon);
        if(p){
        notificationPopup.setDisplayTime(LONG);
        }
        final JLabel label = new JLabel(message);
        final WebButton button = new WebButton("View Log");
        button.setRolloverDecoratedOnly(true);
        button.setDrawFocus(false);
        button.setLeftRightSpacing(0);
        button.setBoldFont();
        button.addActionListener((ActionEvent e) -> {
            notificationPopup.hidePopup();
            QueryResultViewer.showDialog(comp, qd);

        }
        /**
         * {@inheritDoc}
         */
        );
        final CenterPanel centerPanel = new CenterPanel(button, false, true);
        notificationPopup.setContent(new GroupPanel(2, label, centerPanel));

        NotificationManager.showNotification(notificationPopup);
    }

    public static DefaultTableModel getNoteTableModel(List<PermissionRequest> list) {
        DefaultTableModel dtm = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        dtm.addColumn("Date");
        dtm.addColumn("Note/Request");
        dtm.addColumn("Received By");
        dtm.addColumn("Status");
        list.forEach(l -> {
            String r = l.getReceiver() == null ? "Not yet conirmed" : l.getReceiver().getAdminName();
            dtm.addRow(new String[]{l.getDate(), l.getNote(), r, l.getStatus()});
        }
        );
        return dtm;
    }

    public static DefaultTableModel getReceivedNoteTableModel(List<PermissionRequest> list) {
        DefaultTableModel dtm = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        dtm.addColumn("Date");
        dtm.addColumn("Note/Request");
        dtm.addColumn("Sender");
        dtm.addColumn("Status");
        list.forEach(l -> dtm.addRow(new String[]{l.getDate(), l.getNote(), l.getSender().getAdminName(), l.getStatus()}));
        return dtm;
    }

}
