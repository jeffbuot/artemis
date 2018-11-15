/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import java.io.File;
import javax.swing.ImageIcon;

/**
 *
 * @author Jefferson
 */
public class LoggerFile {

    public static File LOG_FILE = new File("request_query_logs.xml");
    public static File SCHED_LOG_FILE = new File("schedule_request_query_logs.xml");
    public static File PERMISSIONS_LOG_FILE = new File("permissions.xml");
    public static File RESPONSES_LOG_FILE = new File("responses.xml");
    public static ImageIcon notifyIcon = new ImageIcon(LoggerFile.class.getResource("/artemis/img/notify_log.png"));
}
