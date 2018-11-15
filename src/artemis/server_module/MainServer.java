/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.server_module;

import artemis.A;
import artemis.chat_module.ChatMessageContainer;
import artemis.chat_module.SerializableImage;
import artemis.classes.BLUFactory;
import artemis.classes.ClientAccount;
import artemis.classes.DBAccount;
import artemis.classes.Data;
import artemis.classes.IconFactory;
import static artemis.classes.IconFactory.infoIcon;
import artemis.classes.LoggerFile;
import artemis.classes.Message;
import artemis.classes.NetCon;
import artemis.classes.Notification;
import artemis.classes.Packet;
import artemis.classes.PermissionRequest;
import artemis.classes.QueryData;
import artemis.classes.ReportGenerator;
import artemis.classes.Tip;
import artemis.classes.XMLParser;
import artemis.client_module.RequestLogData;
import artemis.client_module.RequestLogViewer;
import artemis.dialogs.ArtemisConfirm;
import artemis.dialogs.ArtemisMessage;
import artemis.dialogs.ArtemisPrompt;
import artemis.dialogs.ErrorDialog;
import artemis.panels.AddAccount;
import artemis.panels.AddInstitute;
import artemis.panels.AddProgram;
import artemis.panels.AddRoom;
import artemis.panels.AddSY;
import artemis.panels.AddSubject;
import artemis.panels.AddTeacher;
import artemis.panels.DatabaseConfig;
import artemis.panels.Developers;
import artemis.panels.ErrorDatabase;
import artemis.panels.PortConfig;
import artemis.panels.UpdateSy;
import artemis.sched_board_module.BoardListUpdate;
import artemis.sched_board_module.ChatMessage;
import artemis.sched_board_module.InstituteChooser;
import artemis.sched_board_module.InstituteSerialModel;
import artemis.sched_board_module.ScheduleBoardUpdater;
import artemis.sched_board_module.ScheduleRequestData;
import artemis.sched_board_module.SchedulingBoard;
import artemis.sched_board_module.SerializableModelFactory;
import artemis.view_teachers_load.InstituteTeacherLoad;
import artemis.view_teachers_load.TeacherLoad;
import artemis.view_teachers_load.TeacherLoadView;
import com.alee.extended.image.WebImage;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.WebLookAndFeel;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.jdbc.Database;
import com.jeff.util.ChromieDB;
import com.jeff.util.Cipher;
import com.jeff.util.DBUtil;
import com.jeff.util.TabModel;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * @author Jeff
 */
public class MainServer extends javax.swing.JFrame {

    /**
     * Creates new form MainServer
     */
    private int port = 22777;
    private String SCHOOL_YEAR;
    private final SchedulingBoard board;
    ScheduleBoardUpdater scheduleBoardUpdater;
    private final ChatMessageContainer chatContainer;
    private ArrayList<PermissionRequest> permissions;
    private ArrayList<PermissionRequest> responses;
    private List<InstituteTeacherLoad> insttituteTeacherLoad;

    public MainServer(String sy) {
        if (!AdminLogin.showDialog(null)) {
            System.exit(0);
        }
        this.SCHOOL_YEAR = sy;
        this.permissions = ChromieDB.loadListData(LoggerFile.PERMISSIONS_LOG_FILE, PermissionRequest.class);
        this.responses = ChromieDB.loadListData(LoggerFile.RESPONSES_LOG_FILE, PermissionRequest.class);
        try {
            this.serverProfilePic = ImageIO.read(getClass().getResource("/artemis/img/server_profile.png"));
        } catch (IOException ex) {
            System.out.println(ex);
        }
        initComponents();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.getWidth() * 0.85), (int) (screen.getHeight() * 0.85));
        panTab.add(panStartup);
        panDataViewMain.add(panUser);
        setLocationRelativeTo(null);
        load = new ArtemisPrompt(this, "Loading please wait...");
        receiver = new Receiver();
        startService();
        setTitle("Artemis Server - " + sy);
        scheduleBoardUpdater = getClientUpdate();
        board = new SchedulingBoard(this, "ADMIN", SCHOOL_YEAR, scheduleBoardUpdater) {

            @Override
            public void addScheduleRequest(List<ScheduleRequestData> srd, boolean isOwner, String owner, String room) {
                List<QueryData> l = checkSchedConflict(srd, "$~Server~$");
                if (!l.isEmpty()) {
                    refreshAllData();
                }
            }

            @Override
            public void deleteSchedule(List<QueryData> qd) {
                deleteBatchSchedule(qd);
                refreshAllData();
            }
        };
        this.chatContainer = new ChatMessageContainer(scrollChat);
        panPlottingBoard.add(board);
        setMenuDataview();
        refreshAllData();
        board.refreshBoard();
    }

    // <editor-fold defaultstate="collapsed" desc="Action Events">
    private synchronized List<QueryData> deleteBatchSchedule(List<QueryData> qd) {
        qd.stream().forEach((q) -> {
            try {
                connection.createStatement().executeUpdate(q.getQuery());
                q.setRemarks("Deleted successfuly");
                q.setSuccess(true);
            } catch (SQLException ex) {
                q.setRemarks("Error occured while deleting");
                Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Notification.notifyWithLogger(this, qd, qd.stream().allMatch(i -> i.isSuccess()) ? "A schedule has been successfuly deleted"
                : "Some schedule cannot be deleted.", qd.stream().allMatch(q -> q.isSuccess()));
        return qd;
    }

    private synchronized List<QueryData> checkSchedConflict(List<ScheduleRequestData> srd, String user) {
        try {
            boolean passed = true;
            for (ScheduleRequestData sq : srd) {
                //check day by day
                String query1 = String.format("SELECT * FROM class_sched cs, rooms r"
                        + " WHERE  cs.room_id = r.id AND room_id = '%s' AND `day` = '%s' AND !((end_time <= '%s') OR (start_time >= '%s')) "
                        + "AND sy = '%s' AND r.access_status = 'Active';",
                        sq.getRoomId(), sq.getDay(), sq.getStartTime(), sq.getEndTime(), sq.getSchoolYear());
                ResultSet rs;
                rs = connection.createStatement().executeQuery(query1);
                boolean conflictDetected = false;
                while (rs.next()) {
                    //"Conflict detected; [day] in room [room], [subject] ([time])")
                    sq.appendRemarks(String.format("Conflict detected; %s in room %s, %s (%s).",
                            rs.getString("day"), rs.getString("room_id"), rs.getString("subject_id"),
                            A.toProper(rs.getString("start_time")) + "-" + A.toProper(rs.getString("end_time"))), false);

                    conflictDetected = true;
                    passed = false;
                }
                if (!conflictDetected) {
                    sq.appendRemarks(String.format("(Check 1) Day: %s Room: %s; No conflict detected within the time span (%s).",
                            sq.getDay(), sq.getRoomId(), A.toProper(sq.getStartTime()) + "-" + A.toProper(sq.getEndTime())), true);
                }
                //check instructor's sched
                query1 = String.format("SELECT * FROM class_sched cs,rooms r "
                        + "WHERE cs.room_id = r.id AND `day`='%s' AND !((end_time <= '%s') OR (start_time >= '%s'))"
                        + " AND sy = '%s' AND r.access_status = 'Active' AND cs.teacher_id = '%s';",
                        sq.getDay(), sq.getStartTime(), sq.getEndTime(), sq.getSchoolYear(), sq.getTeacherId());

                rs = connection.createStatement().executeQuery(query1);
                conflictDetected = false;
                while (rs.next()) {
                    sq.appendRemarks(String.format("Teacher's schedule conflict detected; "
                            + "Room: %s,Subject: %s,Time: %s", rs.getString("room_id"),
                            rs.getString("subject_id"), A.toProper(rs.getString("start_time"))
                            + "-" + A.toProper(rs.getString("end_time"))), false);
                    conflictDetected = true;
                    passed = false;
                }
                if (!conflictDetected) {
                    sq.appendRemarks(String.format("(Check 2) Teacher Id: %s ; No conflict detected within the instructor's schedule.",
                            sq.getTeacherId()), true);
                }
                //check section's sched
                query1 = String.format("SELECT * FROM class_sched cs,rooms r "
                        + "WHERE cs.room_id = r.id AND `day`='%s' AND !((end_time <= '%s') OR (start_time >= '%s'))"
                        + " AND sy = '%s' AND r.access_status = 'Active' AND cs.section = '%s';",
                        sq.getDay(), sq.getStartTime(), sq.getEndTime(), sq.getSchoolYear(), sq.getSection());

                rs = connection.createStatement().executeQuery(query1);
                conflictDetected = false;
                while (rs.next()) {
                    sq.appendRemarks(String.format("Section's schedule conflict detected; "
                            + "Room: %s,Subject: %s,Time: %s", rs.getString("room_id"),
                            rs.getString("subject_id"), A.toProper(rs.getString("start_time"))
                            + "-" + A.toProper(rs.getString("end_time"))), false);
                    conflictDetected = true;
                    passed = false;
                }
                if (!conflictDetected) {
                    sq.appendRemarks(String.format("(Final Check) Section: %s ; No conflict detected within the section's schedule.",
                            sq.getSection()), true);
                }
            }

            if (passed) {
                for (ScheduleRequestData esq : srd) {
                    connection.createStatement().executeUpdate(esq.getQuery());
                }
            }

//                
            //Convert sq to request log data
            List<QueryData> queryData = new ArrayList<>();
            srd.forEach(s -> {
                s.getRemarks().forEach(r -> {
                    queryData.add(new QueryData(user, s.getDesc(), s.getQuery(), r.getRemarks(), r.isSuccess()));
                });
            });
            String ok = user.equals("$~Server~$") ? "Schedule has been successfuly"
                    + " added to database." : "User Client: " + user + " pinned a schedule.";
            String fail = user.equals("$~Server~$") ? "Conflict has detected, "
                    + "saving schedule declined." : "User Client: " + user + " failed to pin a sched";
            ArrayList<RequestLogData> list = ChromieDB.loadListData(LoggerFile.SCHED_LOG_FILE, RequestLogData.class);
            list.add(new RequestLogData(queryData, String.format("%1$ta, %1$tb %1$te, %1$ty\n", Calendar.getInstance()),
                    String.format("%tr\n", Calendar.getInstance()), queryData.get(0).getDesc()));
            ChromieDB.saveListData(LoggerFile.SCHED_LOG_FILE, list, RequestLogData.class);
            Notification.notifyWithLogger(this, queryData, passed
                    ? ok : fail, passed);
            return queryData;
        } catch (SQLException ex) {
            System.out.println(ex);
            return new ArrayList<>();
        }
    }

    private synchronized List<InstituteTeacherLoad> getAllTeachersLoad() {
        List<InstituteTeacherLoad> r = new ArrayList<>();
        try {
            DBUtil.toList(connection, "CALL showAllInstitutes()", "id").forEach(inst -> {
                try {
                    List<TeacherLoad> tl = new ArrayList<>();
                    DBUtil.getListData(connection, "CALL showAllTeachersByInstitute('" + inst + "')").forEach(hash -> {
                        String teachId = hash.get("id").toString();
                        String teachName = hash.get("lastname").toString() + ", " + hash.get("firstname").toString();
                        tl.add(new TeacherLoad(teachId, teachName, getTeacherLoad(teachId)));
                    });
                    r.add(new InstituteTeacherLoad(inst, tl));
                } catch (SQLException ex) {
                    Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            return r;
        } catch (SQLException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    private void setMenuDataview() {
        //set the tooltips
        TooltipManager.setTooltip(btnUser, "User Accounts List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnInstitute, "Institutes List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnTeacher, "Teachers List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnSubject, "Subjects List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnRoom, "Rooms List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnSY, "School Year List View", TooltipWay.up, 0);
        WebButtonGroup btg = new WebButtonGroup(true, btnUser, btnInstitute, btnTeacher, btnSubject, btnRoom, btnSY);
        btg.setButtonsDrawFocus(false);
        btnUser.setSelected(true);
        regHotkey();
        panMenuDataView.add(btg);
    }

    private void regHotkey() {
        KeyStroke k = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK);
        menuAddUser.setAccelerator(null);
        menuAddInstitute.setAccelerator(null);
        menuAddRoom.setAccelerator(null);
        menuAddSchoolYear.setAccelerator(null);
        menuAddSubject.setAccelerator(null);
        menuAddTeacher.setAccelerator(null);
        if (btnUser.isSelected()) {
            menuAddUser.setAccelerator(k);
        } else if (btnInstitute.isSelected()) {
            menuAddInstitute.setAccelerator(k);
        } else if (btnTeacher.isSelected()) {
            menuAddTeacher.setAccelerator(k);
        } else if (btnSubject.isSelected()) {
            menuAddSubject.setAccelerator(k);
        } else if (btnRoom.isSelected()) {
            menuAddRoom.setAccelerator(k);
        } else if (btnSY.isSelected()) {
            menuAddSchoolYear.setAccelerator(k);
        }
//        tableInstitutes.addColumn(null);
        TableColumn tabCol = new TableColumn();
    }

    private synchronized ScheduleBoardUpdater getClientUpdate() {
        List<InstituteSerialModel> institutes = new ArrayList<>();
        BoardListUpdate blu = BLUFactory.generate(connection);
        institutes.add(new InstituteSerialModel("Over All",
                SerializableModelFactory.generate(connection, "Over All", "Monday", SCHOOL_YEAR),
                SerializableModelFactory.generate(connection, "Over All", "Tuesday", SCHOOL_YEAR),
                SerializableModelFactory.generate(connection, "Over All", "Wednesday", SCHOOL_YEAR),
                SerializableModelFactory.generate(connection, "Over All", "Thursday", SCHOOL_YEAR),
                SerializableModelFactory.generate(connection, "Over All", "Friday", SCHOOL_YEAR),
                SerializableModelFactory.generate(connection, "Over All", "Saturday", SCHOOL_YEAR)));
        blu.getListInstitutes().forEach(instId -> {
            institutes.add(new InstituteSerialModel(instId,
                    SerializableModelFactory.generate(connection, instId, "Monday", SCHOOL_YEAR),
                    SerializableModelFactory.generate(connection, instId, "Tuesday", SCHOOL_YEAR),
                    SerializableModelFactory.generate(connection, instId, "Wednesday", SCHOOL_YEAR),
                    SerializableModelFactory.generate(connection, instId, "Thursday", SCHOOL_YEAR),
                    SerializableModelFactory.generate(connection, instId, "Friday", SCHOOL_YEAR),
                    SerializableModelFactory.generate(connection, instId, "Saturday", SCHOOL_YEAR)));
        });
        return new ScheduleBoardUpdater(blu, institutes);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panConnectionLogs = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        listOnline = new javax.swing.JList();
        btnDCChat = new javax.swing.JButton();
        panMessaging = new javax.swing.JPanel();
        txtMsg = new com.alee.laf.text.WebTextField();
        scrollChat = new javax.swing.JScrollPane();
        panDataListView = new javax.swing.JPanel();
        panDataViewMain = new javax.swing.JPanel();
        panMenuDataView = new javax.swing.JPanel();
        btnUser = new com.alee.laf.button.WebToggleButton();
        btnInstitute = new com.alee.laf.button.WebToggleButton();
        btnTeacher = new com.alee.laf.button.WebToggleButton();
        btnSubject = new com.alee.laf.button.WebToggleButton();
        btnRoom = new com.alee.laf.button.WebToggleButton();
        btnSY = new com.alee.laf.button.WebToggleButton();
        panPlottingBoard = new javax.swing.JPanel();
        panStartup = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        panUser = new javax.swing.JPanel();
        btnAddAccout = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        tableAccounts = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtSearch7 = new com.alee.laf.text.WebTextField();
        btnFilter1 = new com.alee.laf.button.WebButton();
        btnInfo = new com.alee.laf.button.WebButton();
        panInstitute = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tableInstitutes = new javax.swing.JTable();
        btnAddInstitute = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        txtSearch8 = new com.alee.laf.text.WebTextField();
        btnFilter7 = new com.alee.laf.button.WebButton();
        btnInfo6 = new com.alee.laf.button.WebButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablePrograms = new javax.swing.JTable();
        btnAddInstitute1 = new javax.swing.JButton();
        panTeacher = new javax.swing.JPanel();
        btnAddTeacher = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableTeachers = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        txtSearch3 = new com.alee.laf.text.WebTextField();
        btnFilter3 = new com.alee.laf.button.WebButton();
        btnInfo2 = new com.alee.laf.button.WebButton();
        btnView = new javax.swing.JButton();
        btnTeachLoad1 = new javax.swing.JButton();
        panSubject = new javax.swing.JPanel();
        btnAddSubject = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableSubjects = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        txtSearch4 = new com.alee.laf.text.WebTextField();
        btnFilter4 = new com.alee.laf.button.WebButton();
        btnInfo3 = new com.alee.laf.button.WebButton();
        panRoom = new javax.swing.JPanel();
        btnAddRoom = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableRooms = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        txtSearch5 = new com.alee.laf.text.WebTextField();
        btnFilter5 = new com.alee.laf.button.WebButton();
        btnInfo4 = new com.alee.laf.button.WebButton();
        panSY = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tableSY = new javax.swing.JTable();
        btnAddSY = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        btnSetAcvtiveSY = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        txtSearch6 = new com.alee.laf.text.WebTextField();
        btnFilter6 = new com.alee.laf.button.WebButton();
        btnInfo5 = new com.alee.laf.button.WebButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        panMain = new javax.swing.JPanel();
        panTab = new javax.swing.JPanel();
        panMenu = new javax.swing.JPanel();
        btnStartup = new com.alee.laf.button.WebToggleButton();
        btnConnectionLogs = new com.alee.laf.button.WebToggleButton();
        btnMessages = new com.alee.laf.button.WebToggleButton();
        btnViews = new com.alee.laf.button.WebToggleButton();
        btnSchedBoard = new com.alee.laf.button.WebToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        menuAddUser = new javax.swing.JMenuItem();
        menuAddInstitute = new javax.swing.JMenuItem();
        menuAddTeacher = new javax.swing.JMenuItem();
        menuAddSubject = new javax.swing.JMenuItem();
        menuAddRoom = new javax.swing.JMenuItem();
        menuAddSchoolYear = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        menuView = new javax.swing.JCheckBoxMenuItem();
        menuSchedModLogs = new javax.swing.JMenuItem();
        menuLogger = new javax.swing.JMenuItem();
        menuLogger1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuConnectionConfig = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        menuAdminAccount = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        menuDevelopers = new javax.swing.JMenuItem();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server Network Connection Logs ", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12))); // NOI18N

        txtLog.setEditable(false);
        txtLog.setBackground(new java.awt.Color(51, 51, 51));
        txtLog.setColumns(20);
        txtLog.setFont(new java.awt.Font("Monospaced", 1, 16)); // NOI18N
        txtLog.setForeground(new java.awt.Color(0, 204, 102));
        txtLog.setRows(5);
        txtLog.setText("##Server Initialized...\n");
        jScrollPane1.setViewportView(txtLog);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Currently Connected", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12))); // NOI18N

        listOnline.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        listOnline.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane4.setViewportView(listOnline);

        btnDCChat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Chat - User Delete - 01.png"))); // NOI18N
        btnDCChat.setText("Disconnect Selected");
        btnDCChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDCChatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panConnectionLogsLayout = new javax.swing.GroupLayout(panConnectionLogs);
        panConnectionLogs.setLayout(panConnectionLogsLayout);
        panConnectionLogsLayout.setHorizontalGroup(
            panConnectionLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panConnectionLogsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panConnectionLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnDCChat, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panConnectionLogsLayout.setVerticalGroup(
            panConnectionLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panConnectionLogsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnDCChat)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );

        txtMsg.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtMsg.setHideInputPromptOnFocus(false);
        txtMsg.setInputPrompt("Enter your message here...");
        txtMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMsgActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panMessagingLayout = new javax.swing.GroupLayout(panMessaging);
        panMessaging.setLayout(panMessagingLayout);
        panMessagingLayout.setHorizontalGroup(
            panMessagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMessagingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 822, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(scrollChat, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        panMessagingLayout.setVerticalGroup(
            panMessagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMessagingLayout.createSequentialGroup()
                .addComponent(scrollChat, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panDataViewMain.setBackground(new java.awt.Color(255, 255, 255));
        panDataViewMain.setLayout(new java.awt.BorderLayout());

        btnUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/users.png"))); // NOI18N
        btnUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnUser);

        btnInstitute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/House-11.png"))); // NOI18N
        btnInstitute.setText(" ");
        btnInstitute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInstituteActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnInstitute);

        btnTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Teacher-03.png"))); // NOI18N
        btnTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeacherActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnTeacher);

        btnSubject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Book-Closed-WF.png"))); // NOI18N
        btnSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubjectActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnSubject);

        btnRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Meeting.png"))); // NOI18N
        btnRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRoomActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnRoom);

        btnSY.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Calendar-02.png"))); // NOI18N
        btnSY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSYActionPerformed(evt);
            }
        });
        panMenuDataView.add(btnSY);

        javax.swing.GroupLayout panDataListViewLayout = new javax.swing.GroupLayout(panDataListView);
        panDataListView.setLayout(panDataListViewLayout);
        panDataListViewLayout.setHorizontalGroup(
            panDataListViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDataListViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panDataListViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panMenuDataView, javax.swing.GroupLayout.DEFAULT_SIZE, 863, Short.MAX_VALUE)
                    .addComponent(panDataViewMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panDataListViewLayout.setVerticalGroup(
            panDataListViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDataListViewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panDataViewMain, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(panMenuDataView, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panPlottingBoard.setLayout(new java.awt.BorderLayout());

        panStartup.setBackground(new java.awt.Color(0, 204, 153));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI Light", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Hello!, Welcome to Artemis!");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(91, 91, 91));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/artemis_logo.PNG"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI Light", 0, 30)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Advanced Real-Time Electronic Management Information System");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Author.png"))); // NOI18N

        javax.swing.GroupLayout panStartupLayout = new javax.swing.GroupLayout(panStartup);
        panStartup.setLayout(panStartupLayout);
        panStartupLayout.setHorizontalGroup(
            panStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panStartupLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panStartupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panStartupLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel3)))
                .addContainerGap())
        );
        panStartupLayout.setVerticalGroup(
            panStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panStartupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(49, 49, 49)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
        );

        btnAddAccout.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnAddAccout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddAccout.setText("New User Client");
        btnAddAccout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAccoutActionPerformed(evt);
            }
        });

        tableAccounts.setModel(new javax.swing.table.DefaultTableModel(
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
        tableAccounts.getTableHeader().setReorderingAllowed(false);
        tableAccounts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableAccountsMouseClicked(evt);
            }
        });
        tableAccounts.setAutoCreateRowSorter(true);
        jScrollPane9.setViewportView(tableAccounts);

        jPanel3.setBackground(new java.awt.Color(81, 81, 81));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("User Acounts List View");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        txtSearch7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch7.setInputPrompt("Search....");
        txtSearch7.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch7.setRound(12);
        txtSearch7.setMargin(0,0,0,2);
        txtSearch7.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel4.add(txtSearch7);

        btnFilter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter1.setUndecorated(true);
        btnFilter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter1ActionPerformed(evt);
            }
        });
        jPanel4.add(btnFilter1);

        btnInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo.setUndecorated(true);
        btnInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfoActionPerformed(evt);
            }
        });
        jPanel4.add(btnInfo);

        javax.swing.GroupLayout panUserLayout = new javax.swing.GroupLayout(panUser);
        panUser.setLayout(panUserLayout);
        panUserLayout.setHorizontalGroup(
            panUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panUserLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panUserLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9)
                    .addGroup(panUserLayout.createSequentialGroup()
                        .addComponent(btnAddAccout)
                        .addGap(0, 606, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panUserLayout.setVerticalGroup(
            panUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUserLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddAccout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addContainerGap())
        );

        tableInstitutes.setModel(new javax.swing.table.DefaultTableModel(
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
        tableInstitutes.getTableHeader().setReorderingAllowed(false);
        tableInstitutes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableInstitutesMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tableInstitutes);

        btnAddInstitute.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnAddInstitute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddInstitute.setText("New Institute");
        btnAddInstitute.setPreferredSize(new java.awt.Dimension(145, 33));
        btnAddInstitute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInstituteActionPerformed(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(81, 81, 81));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Institutes List View");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel11))
        );

        txtSearch8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch8.setInputPrompt("Search....");
        txtSearch8.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch8.setRound(12);
        txtSearch8.setMargin(0,0,0,2);
        txtSearch8.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel10.add(txtSearch8);

        btnFilter7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter7.setUndecorated(true);
        btnFilter7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter7ActionPerformed(evt);
            }
        });
        jPanel10.add(btnFilter7);

        btnInfo6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo6.setUndecorated(true);
        btnInfo6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo6ActionPerformed(evt);
            }
        });
        jPanel10.add(btnInfo6);

        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N

        jPanel15.setLayout(new java.awt.BorderLayout());

        tablePrograms.setModel(new javax.swing.table.DefaultTableModel(
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
        tablePrograms.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableProgramsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tablePrograms);

        jPanel15.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Programs Offered", jPanel15);

        btnAddInstitute1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnAddInstitute1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/program.png"))); // NOI18N
        btnAddInstitute1.setText("New Program");
        btnAddInstitute1.setPreferredSize(new java.awt.Dimension(145, 33));
        btnAddInstitute1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInstitute1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panInstituteLayout = new javax.swing.GroupLayout(panInstitute);
        panInstitute.setLayout(panInstituteLayout);
        panInstituteLayout.setHorizontalGroup(
            panInstituteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panInstituteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panInstituteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panInstituteLayout.createSequentialGroup()
                        .addComponent(btnAddInstitute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddInstitute1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panInstituteLayout.createSequentialGroup()
                        .addGroup(panInstituteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7))
                        .addContainerGap())))
        );
        panInstituteLayout.setVerticalGroup(
            panInstituteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panInstituteLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panInstituteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnAddInstitute, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(btnAddInstitute1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddTeacher.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddTeacher.setText("New Teacher");
        btnAddTeacher.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAddTeacher.setPreferredSize(new java.awt.Dimension(145, 33));
        btnAddTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTeacherActionPerformed(evt);
            }
        });

        tableTeachers.setModel(new javax.swing.table.DefaultTableModel(
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
        tableTeachers.getTableHeader().setReorderingAllowed(false);
        tableTeachers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableTeachersMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tableTeachers);

        jPanel5.setBackground(new java.awt.Color(81, 81, 81));

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Teachers List View");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel7))
        );

        txtSearch3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch3.setInputPrompt("Search....");
        txtSearch3.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch3.setRound(12);
        txtSearch3.setMargin(0,0,0,2);
        txtSearch3.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel11.add(txtSearch3);

        btnFilter3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter3.setUndecorated(true);
        btnFilter3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter3ActionPerformed(evt);
            }
        });
        jPanel11.add(btnFilter3);

        btnInfo2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo2.setUndecorated(true);
        btnInfo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo2ActionPerformed(evt);
            }
        });
        jPanel11.add(btnInfo2);

        btnView.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Business-Man.png"))); // NOI18N
        btnView.setText("View Load");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        btnTeachLoad1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnTeachLoad1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/summary_load.png"))); // NOI18N
        btnTeachLoad1.setText("Load Summary");
        btnTeachLoad1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeachLoad1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panTeacherLayout = new javax.swing.GroupLayout(panTeacher);
        panTeacher.setLayout(panTeacherLayout);
        panTeacherLayout.setHorizontalGroup(
            panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTeacherLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panTeacherLayout.createSequentialGroup()
                        .addComponent(jScrollPane6)
                        .addContainerGap())
                    .addGroup(panTeacherLayout.createSequentialGroup()
                        .addComponent(btnAddTeacher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTeachLoad1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panTeacherLayout.setVerticalGroup(
            panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTeacherLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddTeacher, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTeachLoad1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddSubject.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddSubject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddSubject.setText("New Subject");
        btnAddSubject.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAddSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSubjectActionPerformed(evt);
            }
        });

        tableSubjects.setModel(new javax.swing.table.DefaultTableModel(
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
        tableSubjects.getTableHeader().setReorderingAllowed(false);
        tableSubjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSubjectsMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tableSubjects);

        jPanel6.setBackground(new java.awt.Color(81, 81, 81));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Subjects List View");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel8))
        );

        txtSearch4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch4.setInputPrompt("Search....");
        txtSearch4.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch4.setRound(12);
        txtSearch4.setMargin(0,0,0,2);
        txtSearch4.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel12.add(txtSearch4);

        btnFilter4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter4.setUndecorated(true);
        btnFilter4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter4ActionPerformed(evt);
            }
        });
        jPanel12.add(btnFilter4);

        btnInfo3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo3.setUndecorated(true);
        btnInfo3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo3ActionPerformed(evt);
            }
        });
        jPanel12.add(btnInfo3);

        javax.swing.GroupLayout panSubjectLayout = new javax.swing.GroupLayout(panSubject);
        panSubject.setLayout(panSubjectLayout);
        panSubjectLayout.setHorizontalGroup(
            panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSubjectLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panSubjectLayout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panSubjectLayout.createSequentialGroup()
                        .addComponent(btnAddSubject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panSubjectLayout.setVerticalGroup(
            panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSubjectLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddSubject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddRoom.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddRoom.setText("New Room");
        btnAddRoom.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRoomActionPerformed(evt);
            }
        });

        tableRooms.setModel(new javax.swing.table.DefaultTableModel(
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
        tableRooms.getTableHeader().setReorderingAllowed(false);
        tableRooms.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableRoomsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tableRooms);

        jPanel7.setBackground(new java.awt.Color(81, 81, 81));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Rooms List View");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel9))
        );

        txtSearch5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch5.setInputPrompt("Search....");
        txtSearch5.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch5.setRound(12);
        txtSearch5.setMargin(0,0,0,2);
        txtSearch5.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel13.add(txtSearch5);

        btnFilter5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter5.setUndecorated(true);
        btnFilter5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter5ActionPerformed(evt);
            }
        });
        jPanel13.add(btnFilter5);

        btnInfo4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo4.setUndecorated(true);
        btnInfo4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo4ActionPerformed(evt);
            }
        });
        jPanel13.add(btnInfo4);

        javax.swing.GroupLayout panRoomLayout = new javax.swing.GroupLayout(panRoom);
        panRoom.setLayout(panRoomLayout);
        panRoomLayout.setHorizontalGroup(
            panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panRoomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panRoomLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 737, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panRoomLayout.createSequentialGroup()
                        .addComponent(btnAddRoom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panRoomLayout.setVerticalGroup(
            panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panRoomLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );

        tableSY.setModel(new javax.swing.table.DefaultTableModel(
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
        tableSY.getTableHeader().setReorderingAllowed(false);
        tableSY.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSYMouseClicked(evt);
            }
        });
        jScrollPane8.setViewportView(tableSY);

        btnAddSY.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddSY.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddSY.setText("New School Year");
        btnAddSY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSYActionPerformed(evt);
            }
        });

        jPanel8.setBackground(new java.awt.Color(81, 81, 81));

        jLabel10.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("School Year List View");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel10))
        );

        btnSetAcvtiveSY.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSetAcvtiveSY.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Submit-01.png"))); // NOI18N
        btnSetAcvtiveSY.setText("Set As Current Active SY");
        btnSetAcvtiveSY.setToolTipText("Click this button to set the selected school year as current active school year.");
        btnSetAcvtiveSY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetAcvtiveSYActionPerformed(evt);
            }
        });

        txtSearch6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch6.setInputPrompt("Search....");
        txtSearch6.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch6.setRound(12);
        txtSearch6.setMargin(0,0,0,2);
        txtSearch6.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel14.add(txtSearch6);

        btnFilter6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter6.setUndecorated(true);
        btnFilter6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter6ActionPerformed(evt);
            }
        });
        jPanel14.add(btnFilter6);

        btnInfo5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo5.setUndecorated(true);
        btnInfo5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo5ActionPerformed(evt);
            }
        });
        jPanel14.add(btnInfo5);

        javax.swing.GroupLayout panSYLayout = new javax.swing.GroupLayout(panSY);
        panSY.setLayout(panSYLayout);
        panSYLayout.setHorizontalGroup(
            panSYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panSYLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panSYLayout.createSequentialGroup()
                        .addComponent(btnAddSY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetAcvtiveSY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        panSYLayout.setVerticalGroup(
            panSYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panSYLayout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panSYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSetAcvtiveSY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddSY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Artemis Server");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        panMain.setBackground(new java.awt.Color(0, 153, 102));

        panTab.setBackground(new java.awt.Color(51, 51, 51));
        panTab.setLayout(new java.awt.BorderLayout());

        panMenu.setBackground(new java.awt.Color(0, 204, 153));

        buttonGroup1.add(btnStartup);
        btnStartup.setSelected(true);
        btnStartup.setText("Start Page");
        btnStartup.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartupActionPerformed(evt);
            }
        });

        buttonGroup1.add(btnConnectionLogs);
        btnConnectionLogs.setText("Connection Logs");
        btnConnectionLogs.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnConnectionLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectionLogsActionPerformed(evt);
            }
        });

        buttonGroup1.add(btnMessages);
        btnMessages.setText("Messaging");
        btnMessages.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMessagesActionPerformed(evt);
            }
        });

        buttonGroup1.add(btnViews);
        btnViews.setText("Data Views");
        btnViews.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnViews.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewsActionPerformed(evt);
            }
        });

        buttonGroup1.add(btnSchedBoard);
        btnSchedBoard.setText("Scheduling Board");
        btnSchedBoard.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnSchedBoard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSchedBoardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panMenuLayout = new javax.swing.GroupLayout(panMenu);
        panMenu.setLayout(panMenuLayout);
        panMenuLayout.setHorizontalGroup(
            panMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMenuLayout.createSequentialGroup()
                .addContainerGap(111, Short.MAX_VALUE)
                .addComponent(btnStartup, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btnConnectionLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btnMessages, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btnViews, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btnSchedBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
        );
        panMenuLayout.setVerticalGroup(
            panMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panMenuLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(panMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnSchedBoard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnViews, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnStartup, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnConnectionLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnMessages, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout panMainLayout = new javax.swing.GroupLayout(panMain);
        panMain.setLayout(panMainLayout);
        panMainLayout.setHorizontalGroup(
            panMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMainLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(panMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );
        panMainLayout.setVerticalGroup(
            panMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMainLayout.createSequentialGroup()
                .addComponent(panMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panTab, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenu5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuNew.png"))); // NOI18N
        jMenu5.setText("New");

        menuAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu3.png"))); // NOI18N
        menuAddUser.setText("User Account");
        menuAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddUserActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddUser);

        menuAddInstitute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu1.png"))); // NOI18N
        menuAddInstitute.setText("Institute");
        menuAddInstitute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddInstituteActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddInstitute);

        menuAddTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu2.png"))); // NOI18N
        menuAddTeacher.setText("Teachers");
        menuAddTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddTeacherActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddTeacher);

        menuAddSubject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu6.png"))); // NOI18N
        menuAddSubject.setText("Subjects");
        menuAddSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddSubjectActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddSubject);

        menuAddRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu5.png"))); // NOI18N
        menuAddRoom.setText("Rooms");
        menuAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddRoomActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddRoom);

        menuAddSchoolYear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu4.png"))); // NOI18N
        menuAddSchoolYear.setText("School Year");
        menuAddSchoolYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddSchoolYearActionPerformed(evt);
            }
        });
        jMenu5.add(menuAddSchoolYear);

        jMenu1.add(jMenu5);
        jMenu1.add(jSeparator1);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuLogout.png"))); // NOI18N
        jMenuItem3.setText("Logout");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu6.setText("Edit");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Clear All Formating.png"))); // NOI18N
        jMenuItem4.setText("Clear Chat Messages");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem4);

        jMenuBar1.add(jMenu6);

        jMenu4.setText("View");

        menuView.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        menuView.setSelected(true);
        menuView.setText("Show Main Menu");
        menuView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuViewActionPerformed(evt);
            }
        });
        jMenu4.add(menuView);

        menuSchedModLogs.setText("Schedule Modification Logs");
        menuSchedModLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSchedModLogsActionPerformed(evt);
            }
        });
        jMenu4.add(menuSchedModLogs);

        menuLogger.setText("Show Stored Permissions");
        menuLogger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoggerActionPerformed(evt);
            }
        });
        jMenu4.add(menuLogger);

        menuLogger1.setText("Show Stored Responses");
        menuLogger1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLogger1ActionPerformed(evt);
            }
        });
        jMenu4.add(menuLogger1);

        jMenuBar1.add(jMenu4);

        jMenu2.setText("Settings");

        mnuConnectionConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        mnuConnectionConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuDB.png"))); // NOI18N
        mnuConnectionConfig.setText("Connection Config");
        mnuConnectionConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuConnectionConfigActionPerformed(evt);
            }
        });
        jMenu2.add(mnuConnectionConfig);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuPort.png"))); // NOI18N
        jMenuItem1.setText("Port Configuration");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        menuAdminAccount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/admin.png"))); // NOI18N
        menuAdminAccount.setText("Admin Account Settings");
        menuAdminAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAdminAccountActionPerformed(evt);
            }
        });
        jMenu2.add(menuAdminAccount);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        menuDevelopers.setText("About Developers");
        menuDevelopers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDevelopersActionPerformed(evt);
            }
        });
        jMenu3.add(menuDevelopers);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(panMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        int p = PortConfig.showDialog(this, port);
        port = p != -1 ? p : port;
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void mnuConnectionConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuConnectionConfigActionPerformed
        db = XMLParser.loadDBAccount(new File("db.xml"));
        db = DatabaseConfig.showDialog(this, db);
        XMLParser.saveData(new File("db.xml"), db);
        System.out.println("Host: " + db.getHost() + "  Username: " + db.getUsername() + " Password: " + db.getPassword());
        resetConnection();
    }//GEN-LAST:event_mnuConnectionConfigActionPerformed

    private void txtMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMsgActionPerformed
//        sendToAll(new Data(Packet.MESSAGE, new Message("Server", txtMsg.getText())));
        if (txtMsg.getText().trim().equals("")) {
            return;
        }
        Message m = new Message("Server", txtMsg.getText(), A.getDateTime(), new SerializableImage(serverProfilePic), -888);
        appendChat(m, false);
        txtMsg.setText("");
    }//GEN-LAST:event_txtMsgActionPerformed

    private void btnAddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRoomActionPerformed
        addRoom();
    }//GEN-LAST:event_btnAddRoomActionPerformed

    private void btnAddSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSubjectActionPerformed
        addSubject();
    }//GEN-LAST:event_btnAddSubjectActionPerformed

    private void btnAddTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTeacherActionPerformed
        addTeacher();
    }//GEN-LAST:event_btnAddTeacherActionPerformed

    private void btnAddSYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSYActionPerformed
        addSchoolYear();
    }//GEN-LAST:event_btnAddSYActionPerformed

    private void btnAddInstituteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInstituteActionPerformed
        addInstitute();
    }//GEN-LAST:event_btnAddInstituteActionPerformed

    private void btnAddAccoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAccoutActionPerformed
        addUser();
    }//GEN-LAST:event_btnAddAccoutActionPerformed

    private void btnInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfoActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfoActionPerformed

    private void btnInfo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo2ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo2ActionPerformed

    private void btnInfo3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo3ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo3ActionPerformed

    private void btnInfo4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo4ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo4ActionPerformed

    private void btnInfo5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo5ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo5ActionPerformed

    private void btnFilter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter1ActionPerformed

    private void btnFilter3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter3ActionPerformed

    private void btnFilter4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter4ActionPerformed

    private void btnFilter5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter5ActionPerformed

    private void btnFilter6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter6ActionPerformed

    private void tableAccountsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableAccountsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableAccounts.getSelectedRow() != -1) {
                if (!tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 5).toString().equals("Draft")) {
                    HashMap<String, String> hash = new HashMap<>();
                    hash.put("id", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 0).toString());
                    hash.put("firstname", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 1).toString());
                    hash.put("lastname", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 2).toString());
                    hash.put("gender", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 3).toString());
                    hash.put("accessType", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 4).toString());
                    hash.put("institute", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 5).toString());
                    hash.put("accessStatus", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 6).toString());
                    hash.put("username", tableAccounts.getValueAt(tableAccounts.getSelectedRow(), 7).toString());
                    if (AddAccount.update(this, connection, hash) != -1) {
                        Notification.showPopup("User has been updated.", IconFactory.messageIcon);
                        refreshAllData();
                    }
                } else {
                    ArtemisMessage.showDialog(this, "Unable To Update", "Drafts cannot be modified anymore", ArtemisMessage.WARNING);
                }
            }
        }
    }//GEN-LAST:event_tableAccountsMouseClicked

    private void tableInstitutesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableInstitutesMouseClicked
        if (tableInstitutes.getSelectedRow() != -1) {
            if (evt.getClickCount() == 2) {
                if (!tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 3).toString().equals("Draft")) {
                    HashMap<String, String> hash = new HashMap<>();
                    hash.put("id", tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 0).toString());
                    hash.put("desc", tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 1).toString());
                    hash.put("color", tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 2).toString());
                    hash.put("accessStatus", tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 3).toString());
                    if (AddInstitute.update(this, connection, hash) != -1) {
                        Notification.showPopup("Institute has been updated.", IconFactory.messageIcon);
                        refreshAllData();
                    }
                } else {
                    ArtemisMessage.showDialog(this, "Unable To Update", "Drafts cannot be modified anymore", ArtemisMessage.WARNING);
                }
            } else {
                try {
                    tablePrograms.setModel(TabModel.getDefaulTableModel(connection.createStatement().executeQuery("SELECT * FROM programs "
                            + "WHERE facilitator = '" + tableInstitutes.getValueAt(tableInstitutes.getSelectedRow(), 0).toString() + "'")));
                } catch (SQLException ex) {
                    Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_tableInstitutesMouseClicked

    private void tableTeachersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableTeachersMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableTeachers.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 0).toString());
                hash.put("firstname", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 1).toString());
                hash.put("lastname", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 2).toString());
                hash.put("gender", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 3).toString());
                hash.put("instituteId", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 4).toString());
                if (AddTeacher.update(this, connection, hash) != -1) {
                    Notification.showPopup("Teacher has been updated.", IconFactory.messageIcon);
                    refreshAllData();
                }
            }
        }
    }//GEN-LAST:event_tableTeachersMouseClicked

    private void tableSubjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSubjectsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableSubjects.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 0).toString());
                hash.put("desc", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 1).toString());
                hash.put("lect_units", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 2).toString());
                hash.put("lab_units", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 3).toString());
                hash.put("program_facilitator", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 4).toString());
                if (AddSubject.update(this, connection, hash) != -1) {
                    Notification.showPopup("Subject has been updated.", IconFactory.messageIcon);
                    refreshAllData();
                }
            }
        }
    }//GEN-LAST:event_tableSubjectsMouseClicked

    private void tableRoomsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableRoomsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableRooms.getSelectedRow() != -1) {
                if (!tableRooms.getValueAt(tableRooms.getSelectedRow(), 3).toString().equals("Draft")) {
                    HashMap<String, String> hash = new HashMap<>();
                    hash.put("id", tableRooms.getValueAt(tableRooms.getSelectedRow(), 0).toString());
                    hash.put("desc", tableRooms.getValueAt(tableRooms.getSelectedRow(), 1).toString());
                    hash.put("facilitator", tableRooms.getValueAt(tableRooms.getSelectedRow(), 2).toString());
                    hash.put("accessStatus", tableRooms.getValueAt(tableRooms.getSelectedRow(), 3).toString());
                    if (AddRoom.update(this, connection, hash) != -1) {
                        Notification.showPopup("Room has been updated.", IconFactory.messageIcon);
                        refreshAllData();
                    }
                } else {
                    ArtemisMessage.showDialog(this, "Unable To Update", "Drafts cannot be modified anymore", ArtemisMessage.WARNING);
                }
            }
        }
    }//GEN-LAST:event_tableRoomsMouseClicked

    private void tableSYMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSYMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableSY.getSelectedRow() != -1) {
                String sy = tableSY.getValueAt(tableSY.getSelectedRow(), 0).toString();
                String st = tableSY.getValueAt(tableSY.getSelectedRow(), 1).toString();
                if (!st.equals("Draft")) {
                    if (UpdateSy.update(this, connection, sy, st) != -1) {
                        Notification.showPopup("School year has been updated.", IconFactory.messageIcon);
                        resetTableViews();
                        while (!isThereActiveSY()) {
                            if (ArtemisConfirm.showDialog(this, "There is no active sy.Y to close server, N to proceed to setup.") == 0) {
                                SetupSchoolYear.showDialog(this, connection);
                                resetTableViews();
                            } else {
                                stopService();
                                System.exit(0);
                                return;
                            }
                        }
                        String selectedSY = "";
                        for (int i = 0; i < tableSY.getRowCount(); i++) {
                            if (tableSY.getValueAt(i, 1).toString().equals("Active")) {
                                selectedSY = tableSY.getValueAt(i, 0).toString();
                            }
                        }
                        this.SCHOOL_YEAR = selectedSY;
                        Notification.showPopup(selectedSY + " has been set as the current school year.", IconFactory.messageIcon);
                        setTitle("Artemis Server - " + selectedSY);
                    }
                } else {
                    ArtemisMessage.showDialog(this, "Unable To Update", "Drafts cannot be modified anymore", ArtemisMessage.WARNING);
                }
            }
        }
    }//GEN-LAST:event_tableSYMouseClicked

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void menuAddTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddTeacherActionPerformed
        addTeacher();
    }//GEN-LAST:event_menuAddTeacherActionPerformed

    private void menuAddInstituteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddInstituteActionPerformed
        addInstitute();
    }//GEN-LAST:event_menuAddInstituteActionPerformed

    private void menuAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddUserActionPerformed
        addUser();
    }//GEN-LAST:event_menuAddUserActionPerformed

    private void menuAddSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddSubjectActionPerformed
        addSubject();
    }//GEN-LAST:event_menuAddSubjectActionPerformed

    private void menuAddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddRoomActionPerformed
        addRoom();
    }//GEN-LAST:event_menuAddRoomActionPerformed

    private void menuAddSchoolYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddSchoolYearActionPerformed
        addSchoolYear();
    }//GEN-LAST:event_menuAddSchoolYearActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
//        Credits.showDialog(this);
    }//GEN-LAST:event_formWindowOpened

    private void menuViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuViewActionPerformed
        panMenu.setVisible(menuView.isSelected());
    }//GEN-LAST:event_menuViewActionPerformed

    private void btnDCChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDCChatActionPerformed
        if (listOnline.getSelectedIndex() != -1) {
            if (ArtemisConfirm.showDialog(this, "Are you sure to disconnect "
                    + listOnline.getSelectedValue().toString().split("~")[0] + "?") != 0) {
                return;
            }
            int selectedId = Integer.parseInt(listOnline.getSelectedValue().toString().split("~")[1]);
            for (ClientHandler c : clients) {
                if (c.getAccount().getId() == selectedId) {
                    try {
                        c.send(new Data(-1));
                        c.running = false;
                    } catch (IOException ex) {
                    }
                    System.out.println(clients.remove(c));
                    Notification.showPopup(c.getAccount().getFirstname() + "(" + c.ipAdddress + ") has disconnected.", IconFactory.messageIcon);
                    appendLog(c.getAccount().getUsername() + " (" + c.ipAdddress + ") network communication closed.");
                    refreshList();
                    break;
                }
            }
        }
    }//GEN-LAST:event_btnDCChatActionPerformed

    private void btnSetAcvtiveSYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetAcvtiveSYActionPerformed
        if (tableSY.getSelectedRow() != -1) {
            try {
                //activate a school year

                String selectedSY = tableSY.getValueAt(tableSY.getSelectedRow(), 0).toString();
                String selectedAS = tableSY.getValueAt(tableSY.getSelectedRow(), 1).toString();
                if (selectedAS.equals("Active")) {
                    TooltipManager.showOneTimeTooltip(btnSetAcvtiveSY, null, IconFactory.infoIcon, "The row you selected is already active.");
                    return;
                }
                if (selectedAS.equals("Draft")) {
                    TooltipManager.showOneTimeTooltip(btnSetAcvtiveSY, null, IconFactory.infoIcon, "Drafts cannot be modified anymore.");
                    return;
                }
                if (ArtemisConfirm.showDialog(this, "Are you sure to set the school year as current?") != 0) {
                    return;
                }
                ResultSet rs = connection.createStatement().executeQuery("call showAllSchoolYear");
                while (rs.next()) {
                    String tempSY = rs.getString("sy_sem");
                    String tempAS = rs.getString("access_status");
                    if (!selectedSY.equals(tempSY) & tempAS.equals("Active")) {
                        String q = "update school_year set access_status = 'Locked' where sy_sem='" + tempSY + "'";
                        connection.createStatement().executeUpdate(q);
                    } else if (selectedSY.equals(tempSY)) {
                        String q = "update school_year set access_status = 'Active' where sy_sem='" + tempSY + "'";
                        connection.createStatement().executeUpdate(q);
                    }
                }
                this.SCHOOL_YEAR = selectedSY;
                Notification.showPopup(selectedSY + " has been set as the current school year.", IconFactory.messageIcon);
                setTitle("Artemis Server - " + selectedSY);
                resetTableViews();
            } catch (SQLException ex) {
                ArtemisMessage.showDialog(this, "Something went wrong while fetching some data on database.");
                System.out.println(ex);
            }
        } else {
            TooltipManager.showOneTimeTooltip(btnSetAcvtiveSY, null, infoIcon, "There is no selected row on the table.");
        }
    }//GEN-LAST:event_btnSetAcvtiveSYActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        chatContainer.clearChat();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void btnFilter7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter7ActionPerformed

    private void btnInfo6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnInfo6ActionPerformed

    private void btnStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartupActionPerformed
        panTab.removeAll();
        panTab.add(panStartup);
        panTab.validate();
        repaint();
    }//GEN-LAST:event_btnStartupActionPerformed

    private void btnConnectionLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectionLogsActionPerformed
        panTab.removeAll();
        panTab.add(panConnectionLogs);
        panTab.validate();
        repaint();
    }//GEN-LAST:event_btnConnectionLogsActionPerformed

    private void btnMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMessagesActionPerformed
        panTab.removeAll();
        panTab.add(panMessaging);
        panTab.validate();
        repaint();
    }//GEN-LAST:event_btnMessagesActionPerformed

    private void btnViewsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewsActionPerformed
        panTab.removeAll();
        panTab.add(panDataListView);
        panTab.validate();
        repaint();
    }//GEN-LAST:event_btnViewsActionPerformed

    private void btnSchedBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSchedBoardActionPerformed
        panTab.removeAll();
        panTab.add(panPlottingBoard);
        panTab.validate();
        repaint();
    }//GEN-LAST:event_btnSchedBoardActionPerformed

    private void btnUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panUser);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnUserActionPerformed

    private void btnInstituteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInstituteActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panInstitute);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnInstituteActionPerformed

    private void btnTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeacherActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panTeacher);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnTeacherActionPerformed

    private void btnSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubjectActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panSubject);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnSubjectActionPerformed

    private void btnRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRoomActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panRoom);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnRoomActionPerformed

    private void btnSYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSYActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panSY);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnSYActionPerformed

    private void btnAddInstitute1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInstitute1ActionPerformed
        int c = AddProgram.showDialog(this, connection);
        if (c > 0) {
            Notification.showPopup(c + " program(s) has been successfully added.", IconFactory.messageIcon);
            refreshAllData();
        }
    }//GEN-LAST:event_btnAddInstitute1ActionPerformed

    private void tableProgramsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProgramsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tablePrograms.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 0).toString());
                hash.put("desc", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 1).toString());
                hash.put("facilitator", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 2).toString());
                if (AddProgram.update(this, connection, hash) != -1) {
                    Notification.showPopup("Program has been updated.", IconFactory.messageIcon);
                    refreshAllData();
                }
            }
        }
    }//GEN-LAST:event_tableProgramsMouseClicked

    private void menuSchedModLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSchedModLogsActionPerformed
        RequestLogViewer.showDialog(this, LoggerFile.SCHED_LOG_FILE);
    }//GEN-LAST:event_menuSchedModLogsActionPerformed

    private void menuDevelopersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDevelopersActionPerformed
        Developers.showDialog(this);
    }//GEN-LAST:event_menuDevelopersActionPerformed

    private void menuLoggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoggerActionPerformed
        PermissionsLogViewer.showDialog(this);
    }//GEN-LAST:event_menuLoggerActionPerformed

    private void menuLogger1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLogger1ActionPerformed
        PermissionsLogViewer.showResponseLogs(this);
    }//GEN-LAST:event_menuLogger1ActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        if (tableTeachers.getSelectedRow() == -1) {
            TooltipManager.showOneTimeTooltip(btnView, null, IconFactory.infoIcon,
                    "Please select a teacher below.");
        } else {
            String id = (String) tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 0);
            String name = (String) tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 2) + ", "
                    + (String) tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 1);
            TeacherLoadView.showDialog(this, getTeacherLoad(id), name, SCHOOL_YEAR);
        }
    }//GEN-LAST:event_btnViewActionPerformed

    private void menuAdminAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAdminAccountActionPerformed
        AccountSetup.showDialog(this);
    }//GEN-LAST:event_menuAdminAccountActionPerformed

    private void btnTeachLoad1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeachLoad1ActionPerformed
        String inst = InstituteChooser.showDialog(this, scheduleBoardUpdater.getBlu().getListInstitutes());
        for (InstituteTeacherLoad it : insttituteTeacherLoad) {
            if (it.getInstituteId().equals(inst)) {
                new ReportGenerator(SCHOOL_YEAR, "Server Admin Generated (" + inst + ")", this, it.getTeacherLoads()).start();
                break;
            }
        }
    }//GEN-LAST:event_btnTeachLoad1ActionPerformed
//</editor-fold>

    BufferedImage serverProfilePic;
    NetCon nc;
    DBAccount db;
    Connection connection;
    ArtemisPrompt load;
    Receiver receiver;

    private void startService() {
        appendLog("Server service is now onine.");
        Notification.showPopup("Server is now accepting clients at port " + port, IconFactory.messageIcon);
        db = XMLParser.loadDBAccount(new File("db.xml"));
        resetConnection();
        receiver.start();
    }

    private void resetConnection() {
        try {
            connection = Database.connect(db.getUsername(), db.getPassword(), db.getHost(), A.DB_NAME);
            appendLog("Database connection test passed.");
            resetTableViews();
        } catch (ClassNotFoundException | SQLException ex) {
            appendLog("Database connection test failed.");
            ErrorDatabase.showDialog(this);
            System.exit(0);
//            ErrorDialog.showDialog(ex);
        }
    }

    //get the Teacher's Subject Load
    private synchronized DefaultTableModel getTeacherLoad(String id) {
        try {
            String query = "SELECT s.id as `Subject Id`,\n"
                    + "cs.section,\n"
                    + "cs.session_type,\n"
                    + "s.lect_units,\n"
                    + "s.lab_units,\n"
                    + "concat(cs.start_time,'-',cs.end_time) as `Time`,\n"
                    + "GROUP_CONCAT(DISTINCT cs.`day`)as'day',\n"
                    + "cs.room_id\n"
                    + "FROM class_sched cs,subjects s\n"
                    + "WHERE s.id=cs.subject_id\n"
                    + "AND cs.teacher_id='" + id + "'\n"
                    + "GROUP BY cs.section,cs.session_type";
            ResultSet rs = connection.createStatement().executeQuery(query);
            DefaultTableModel dtm = TabModel.getDefaulTableModel(rs);
            for (int i = 0; i < dtm.getRowCount(); i++) {
                if (dtm.getValueAt(i, 2).toString().equals("LAB")) {
                    dtm.setValueAt("0", i, 3);
                } else {
                    dtm.setValueAt("0", i, 4);
                }
                String start = dtm.getValueAt(i, 5).toString().split("-")[0];
                String end = dtm.getValueAt(i, 5).toString().split("-")[1];
                dtm.setValueAt(A.toProper(start) + "-" + A.toProper(end), i, 5);
            }
            return dtm;
        } catch (SQLException ex) {
            System.out.println(ex);
            return new DefaultTableModel();
        }
    }

    private void resetTableViews() {
        try {
            ResultSet rs = connection.createStatement().executeQuery("Call showAllRooms");
            tableRooms.setModel(TabModel.getDefaulTableModel(rs));
            if (tableAccounts.getColumnModel().getColumnCount() > 0) {
                tableAccounts.getColumnModel().getColumn(0).setPreferredWidth(50);
            }
            rs = connection.createStatement().executeQuery("Call showAllInstitutes");
            tableInstitutes.setModel(TabModel.getDefaulTableModel(rs));
            rs = connection.createStatement().executeQuery("Call showAllSubjects");
            tableSubjects.setModel(TabModel.getDefaulTableModel(rs));
            rs = connection.createStatement().executeQuery("Call showAllTeachers");
            tableTeachers.setModel(TabModel.getDefaulTableModel(rs));
            rs = connection.createStatement().executeQuery("Call showAllSchoolYear");
            tableSY.setModel(TabModel.getDefaulTableModel(rs));
            rs = connection.createStatement().executeQuery("Call showAllUserAccounts");
            tableAccounts.setModel(TabModel.getDefaulTableModel(rs));
            rs = connection.createStatement().executeQuery("Call showAllPrograms");
            tablePrograms.setModel(TabModel.getDefaulTableModel(rs));
        } catch (SQLException e) {
            ErrorDialog.showDialog(e);
        }
    }

    private void refreshAllData() {
        //update scheduleBoardUpdater
        this.scheduleBoardUpdater = getClientUpdate();
        //update teachers load by institute
        this.insttituteTeacherLoad = getAllTeachersLoad();
        //update local schedule board
        board.updateScheduleBoardUpdater(scheduleBoardUpdater);
        //update table data views
        resetTableViews();
        //update clients

        clients.forEach(client -> {
            try {
                InstituteTeacherLoad itl = null;
                if (!client.getAccount().getInstitute().equals("N/A")) {
                    for (InstituteTeacherLoad i : insttituteTeacherLoad) {
                        if (i.getInstituteId().equals(client.getAccount().getInstitute())) {
                            itl = i;
                            break;
                        }
                    }
                }
                client.send(new Data(Packet.SCHEDULE_BOARD_UPDATE, scheduleBoardUpdater, itl));
            } catch (IOException ex) {
                ErrorDialog.showDialog(ex);
            }
        });
    }

    private boolean isThereActiveSY() {
        for (int i = 0; i < tableSY.getRowCount(); i++) {
            if (tableSY.getValueAt(i, 1).toString().equals("Active")) {
                return true;
            }
        }
        return false;
    }

    private void addUser() {
        int inst = AddAccount.showDialog(this, connection);
        if (inst > 0) {
            Notification.showPopup(inst + " new user has been successfully added.", IconFactory.messageIcon);
            resetTableViews();
        }
    }

    private void addInstitute() {
        int inst = AddInstitute.showDialog(this, connection);
        if (inst > 0) {
            Notification.showPopup(inst + " institute(s) has been successfully added.", IconFactory.messageIcon);
            refreshAllData();
        }
    }

    private void addTeacher() {
        int tchrs = AddTeacher.showDialog(this, connection);
        if (tchrs > 0) {
            Notification.showPopup(tchrs + " teacher(s) has been successfully added.", IconFactory.messageIcon);
            refreshAllData();
        }
    }

    private void addSubject() {
        int subjects = AddSubject.showDialog(this, connection);
        if (subjects > 0) {
            Notification.showPopup(subjects + " subject(s) has been successfully added.", IconFactory.messageIcon);
            refreshAllData();
        }
    }

    private void addRoom() {
        int rooms = AddRoom.showDialog(this, connection);
        if (rooms > 0) {
            Notification.showPopup(rooms + " room(s) has been successfully added.", IconFactory.messageIcon);
            refreshAllData();
        }
    }

    private void addSchoolYear() {
        int inst = AddSY.showDialog(this, connection);
        if (inst > 0) {
            Notification.showPopup("A school year has been successfully added.", IconFactory.messageIcon);
            resetTableViews();
        }
    }

    private void stopService() {
        receiver.close();
    }

    private synchronized void sendPermissionDatagram(List<PermissionRequest> pr) {
        //find the institute admin for this request
        for (ClientHandler ch : clients) {
            if (ch.getAccount().getInstitute().equals(pr.get(0).getInstituteToBeSent())) {
                try {
                    ch.send(new Data(Packet.PERMISSION_REQUEST_LIST, pr, true));
                    System.out.println("Client is online");
                    return;
                } catch (IOException ex) {
                    appendLog("An error occured while server is trying to send a"
                            + " permission request from " + pr.get(0).getSender().getAdminName()
                            + " to " + pr.get(0).getInstituteToBeSent() + ".");
                    Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //when there is no institute admin for receiver online
        System.out.println("Client is offline");
        pr.forEach(p -> permissions.add(0, p));
        ChromieDB.saveListData(LoggerFile.PERMISSIONS_LOG_FILE, permissions, PermissionRequest.class);
        permissions = ChromieDB.loadListData(LoggerFile.PERMISSIONS_LOG_FILE, PermissionRequest.class);
    }

    private synchronized void sendResponseDatagram(List<PermissionRequest> pr) {
        //check if the status is confirmed
        if (pr.get(0).getStatus().equals("Confirmed")) {
            pr.get(0).setQd(checkSchedConflict(pr.get(0).getSrd(), pr.get(0).getSender().getUsername()));
            refreshAllData();
        }

        //find the sender of this request if online
        for (ClientHandler client : clients) {
            if (client.getAccount().getId() == pr.get(0).getSender().getId()) {
                try {
                    client.send(new Data(Packet.PERMISSION_RESPONSE_LIST, pr, true));
                    System.out.println("Client is online");
                    return;
                } catch (IOException ex) {
                    appendLog("An error occured while server is trying to send a"
                            + " permission request from " + pr.get(0).getSender().getAdminName()
                            + " to " + pr.get(0).getInstituteToBeSent() + ".");
                    Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //store if the sender is offline
        System.out.println("Client is offline");
        pr.forEach(p -> responses.add(0, p));
        ChromieDB.saveListData(LoggerFile.RESPONSES_LOG_FILE, responses, PermissionRequest.class);
        responses = ChromieDB.loadListData(LoggerFile.RESPONSES_LOG_FILE, PermissionRequest.class);
    }

    private synchronized void refreshList() {
        DefaultListModel m = new DefaultListModel();
        clients.forEach(c -> m.addElement(c.getAccount().getName() + " (" + c.ipAdddress + ")~" + c.getAccount().getId()));
        listOnline.setModel(m);
    }

    private synchronized void appendChat(Message msg, boolean isClient) {
        Message mm = new Message(msg.getSender(), msg.getMessage(), msg.getTimeInfo(), msg.getSenderImage(), msg.getUserId());
        chatContainer.appendMessage(new ChatMessage(mm, isClient));
        if (!btnMessages.isSelected()) {
            Notification.showPopup(msg.getSender() + " has a message.", IconFactory.chatIcon, Notification.SHORT);
        }
        if (!isClient) {
            sendToAll(new Data(Packet.MESSAGE, msg));
        }
    }

    private synchronized void sendToAll(Data d) {
        clients.forEach(client -> {
            try {
                client.send(d);
            } catch (IOException ex) {
                ErrorDialog.showDialog(ex);
            }
        });
    }

    private synchronized void sendToAll(Data d, int idExcluded) {
        clients.stream().filter(cl -> cl.getAccount().getId() != idExcluded).forEach(client -> {
            try {
                client.send(d);
            } catch (IOException ex) {
                ErrorDialog.showDialog(ex);
            }
        });
    }

    private synchronized ClientAccount accessUser(String username, String password) {
        String q = String.format("select * from user_accounts where username = '%s'"
                + " and user_password='%s'", username, Cipher.encrypt(password));
        try {
            List<HashMap<String, Object>> accnt = DBUtil.getListData(connection, q).stream().collect(Collectors.toList());
            if (accnt.size() > 0) {
                HashMap<String, Object> h = accnt.get(0);
                //map valdty.id.fn.ln.un.accessType.accessStatus
                String q2 = "Select `description` from institutes where id = '" + h.get("institute").toString() + "'";
                String e = "";
                for (String iDesc : DBUtil.toList(connection, q2, "description")) {
                    e = iDesc;
                }
                return new ClientAccount((int) h.get("id"), h.get("firstname").toString(),
                        h.get("lastname").toString(), h.get("gender").toString(),
                        h.get("access_type").toString(), h.get("institute").toString(), h.get("access_status").toString(),
                        h.get("username").toString(), Cipher.decrypt(h.get("user_password").toString()), SCHOOL_YEAR, e);

            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return null;
        }
        return null;
    }

    private synchronized void workQueryRequest(List<QueryData> qd, ClientAccount sender) {
        qd.stream().forEach((d) -> {
            try {
                connection.createStatement().executeUpdate(d.getQuery());
                d.setRemarks("Successfuly updated the database.");
                d.setSuccess(true);
            } catch (SQLException ex) {
                d.setRemarks(ex.getMessage());
            }
        });
        for (ClientHandler ch : clients) {
            if (ch.getAccount().getId() == sender.getId()) {
                try {
                    ch.send(new Data(Packet.SERVER_QUERY_RESULT, qd, sender));
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                break;
            }
        }
        refreshAllData();

    }

    public synchronized void workScheduleRequest(List<ScheduleRequestData> srd, ClientAccount account) {
        List<QueryData> lqd = checkSchedConflict(srd, account.getUsername());

        for (ClientHandler ch : clients) {
            if (ch.getAccount().getId() == account.getId()) {
                try {
                    ch.send(new Data(Packet.SERVER_QUERY_RESULT, lqd, account));
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                break;
            }
        }
        if (!lqd.isEmpty()) {
            refreshAllData();
        }
    }

    public synchronized void appendLog(String txt) {
        Calendar dateTime = Calendar.getInstance();
        String time = String.format("%tr> ", dateTime);
        txtLog.append(time + txt + "\n");
        txtLog.setCaretPosition(txtLog.getText().length());
    }

    synchronized void disconnectClient(ClientHandler client) {
        Notification.showPopup(client.getAccount().getFirstname() + "(" + client.ipAdddress + ") has disconnected.", IconFactory.messageIcon);
        appendLog(client.getAccount().getUsername() + " (" + client.ipAdddress + ") network communication closed.");
        client.running = false;
        try {
            client.out.close();
            client.in.close();
        } catch (IOException ex) {
            //no log for this
        }
        System.out.println(clients.remove(client));
        refreshList();
    }

    private synchronized boolean clientQueryUpdate(String clientName, String query) {
        try {
            connection.createStatement().executeUpdate(query);
            appendLog(clientName + " updated the database.");
            return true;
        } catch (SQLException ex) {
            appendLog("Database denied " + clientName + "'s request.");
            return false;
        }
    }

    @Override
    public void dispose() {
        int choose = ArtemisConfirm.showDialog(this, "Are you sure you want to close server?");
        if (choose == 0) {
            stopService();
            System.exit(0);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            WebLookAndFeel.install();
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            ErrorDialog.showDialog(ex);
        }

        DBAccount d = XMLParser.loadDBAccount(new File("db.xml"));
        String schoolYear;
        try {
            Connection con = Database.connect(d.getUsername(), d.getPassword(), d.getHost(), A.DB_NAME);
            ResultSet r = con.createStatement().executeQuery("call showActiveSchoolYear");
            if (r.next()) {
                schoolYear = r.getString("sy_sem");
                java.awt.EventQueue.invokeLater(() -> {
                    new MainServer(schoolYear).setVisible(true);
                });
            } else {
                //show Schoole Year setup initialization
                SetupSchoolYear.showDialog(null, con);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ErrorDatabase.showDialog(null);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Plotting Board here">
    // </editor-fold>   
    // <editor-fold defaultstate="collapsed" desc="Variables declaration">   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAccout;
    private javax.swing.JButton btnAddInstitute;
    private javax.swing.JButton btnAddInstitute1;
    private javax.swing.JButton btnAddRoom;
    private javax.swing.JButton btnAddSY;
    private javax.swing.JButton btnAddSubject;
    private javax.swing.JButton btnAddTeacher;
    private com.alee.laf.button.WebToggleButton btnConnectionLogs;
    private javax.swing.JButton btnDCChat;
    private com.alee.laf.button.WebButton btnFilter1;
    private com.alee.laf.button.WebButton btnFilter3;
    private com.alee.laf.button.WebButton btnFilter4;
    private com.alee.laf.button.WebButton btnFilter5;
    private com.alee.laf.button.WebButton btnFilter6;
    private com.alee.laf.button.WebButton btnFilter7;
    private com.alee.laf.button.WebButton btnInfo;
    private com.alee.laf.button.WebButton btnInfo2;
    private com.alee.laf.button.WebButton btnInfo3;
    private com.alee.laf.button.WebButton btnInfo4;
    private com.alee.laf.button.WebButton btnInfo5;
    private com.alee.laf.button.WebButton btnInfo6;
    private com.alee.laf.button.WebToggleButton btnInstitute;
    private com.alee.laf.button.WebToggleButton btnMessages;
    private com.alee.laf.button.WebToggleButton btnRoom;
    private com.alee.laf.button.WebToggleButton btnSY;
    private com.alee.laf.button.WebToggleButton btnSchedBoard;
    private javax.swing.JButton btnSetAcvtiveSY;
    private com.alee.laf.button.WebToggleButton btnStartup;
    private com.alee.laf.button.WebToggleButton btnSubject;
    private javax.swing.JButton btnTeachLoad1;
    private com.alee.laf.button.WebToggleButton btnTeacher;
    private com.alee.laf.button.WebToggleButton btnUser;
    private javax.swing.JButton btnView;
    private com.alee.laf.button.WebToggleButton btnViews;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList listOnline;
    private javax.swing.JMenuItem menuAddInstitute;
    private javax.swing.JMenuItem menuAddRoom;
    private javax.swing.JMenuItem menuAddSchoolYear;
    private javax.swing.JMenuItem menuAddSubject;
    private javax.swing.JMenuItem menuAddTeacher;
    private javax.swing.JMenuItem menuAddUser;
    private javax.swing.JMenuItem menuAdminAccount;
    private javax.swing.JMenuItem menuDevelopers;
    private javax.swing.JMenuItem menuLogger;
    private javax.swing.JMenuItem menuLogger1;
    private javax.swing.JMenuItem menuSchedModLogs;
    private javax.swing.JCheckBoxMenuItem menuView;
    private javax.swing.JMenuItem mnuConnectionConfig;
    private javax.swing.JPanel panConnectionLogs;
    private javax.swing.JPanel panDataListView;
    private javax.swing.JPanel panDataViewMain;
    private javax.swing.JPanel panInstitute;
    private javax.swing.JPanel panMain;
    private javax.swing.JPanel panMenu;
    private javax.swing.JPanel panMenuDataView;
    private javax.swing.JPanel panMessaging;
    private javax.swing.JPanel panPlottingBoard;
    private javax.swing.JPanel panRoom;
    private javax.swing.JPanel panSY;
    private javax.swing.JPanel panStartup;
    private javax.swing.JPanel panSubject;
    private javax.swing.JPanel panTab;
    private javax.swing.JPanel panTeacher;
    private javax.swing.JPanel panUser;
    private javax.swing.JScrollPane scrollChat;
    private javax.swing.JTable tableAccounts;
    private javax.swing.JTable tableInstitutes;
    private javax.swing.JTable tablePrograms;
    private javax.swing.JTable tableRooms;
    private javax.swing.JTable tableSY;
    private javax.swing.JTable tableSubjects;
    private javax.swing.JTable tableTeachers;
    private javax.swing.JTextArea txtLog;
    private com.alee.laf.text.WebTextField txtMsg;
    private com.alee.laf.text.WebTextField txtSearch3;
    private com.alee.laf.text.WebTextField txtSearch4;
    private com.alee.laf.text.WebTextField txtSearch5;
    private com.alee.laf.text.WebTextField txtSearch6;
    private com.alee.laf.text.WebTextField txtSearch7;
    private com.alee.laf.text.WebTextField txtSearch8;
    // End of variables declaration//GEN-END:variables
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Network Thread Classes">  
    ArrayList<ClientHandler> clients = new ArrayList<>();

    private class Receiver extends Thread {

        ServerSocket serverSocket;
        boolean receiving;

        public Receiver() {
        }

        public void close() {
            receiving = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                //closes all connected clients
                clients.stream().forEach(c -> {
                    try {
                        c.close();
                    } catch (IOException ex) {
                        //no log for this when server is exiting
                    }
                });
            } catch (IOException ex) {
                //no log for this when server is exiting
            }
            //clean clients after closing
            clients.clear();
        }

        public void setReceiving(boolean receiving) {
            this.receiving = receiving;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                receiving = true;
                while (receiving) {
                    Socket socket = serverSocket.accept();
                    String ip = socket.getInetAddress().getHostAddress();

                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    //receiving credentials
                    String[] cred = (String[]) in.readObject();
                    ClientAccount account = accessUser(cred[0], cred[1]);
                    if (account != null) {
                        final int accId = account.getId();
                        if (clients.stream().anyMatch(client -> client.getAccount().getId() == accId)) {
                            account.setCanLogin(false);
                        }
                    }
                    out.writeObject(account);
                    out.flush();
                    if (account == null) {
                        //failed to login
                        Notification.showPopup("User client " + cred[0] + " has failed to login.", IconFactory.messageIcon, Notification.SHORT);
                        appendLog(cred[0] + " (" + ip + ") login failed: Cause - wrong credential/not registered.");
                        continue;
                    } else if (account.getAccessStatus().equals("Locked")) {
                        //the client's account is locked
                        Notification.showPopup(cred[0] + " attempt to login a locked account", IconFactory.messageIcon, Notification.SHORT);
                        appendLog(cred[0] + " (" + ip + ") login failed: Cause - account locked.");
                        continue;
                    } else if (account.getAccessStatus().equals("Draft")) {
                        Notification.showPopup("A client attempted to access a drafted account.", IconFactory.messageIcon, Notification.SHORT);
                        appendLog(cred[0] + " (" + ip + ") login failed: Cause - unknown account/drafted.");
                        continue;
                    } else if (!account.isCanLogin()) {
                        Notification.showPopup("User client " + cred[0] + " has already login.", IconFactory.messageIcon, Notification.SHORT);
                        appendLog(cred[0] + " (" + ip + ") login failed: Cause - online account conflict.");
                        continue;
                    }
                    clients.add(new ClientHandler(ip, in, out, account));
                    clients.get(clients.size() - 1).start();
                    refreshList();
                    Notification.showPopup(cred[0] + " (" + ip + ") has Connected!", IconFactory.messageIcon, Notification.SHORT);
                    appendLog(cred[0] + " (" + ip + ") access granted.");
                }
            } catch (IOException | ClassNotFoundException ex) {
                ErrorDialog.showDialog(ex);
                appendLog("Exception error occured: " + ex);
            }
        }

    }

    private class ClientHandler extends Thread {

        //map valdty.id.fn.ln.un.accessType.accessStatus
        private final String ipAdddress;
        private final ClientAccount account;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private boolean running;

        public ClientHandler(String ipAdddress, ObjectInputStream in, ObjectOutputStream out, ClientAccount account) {
            this.ipAdddress = ipAdddress;
            this.in = in;
            this.out = out;
            this.running = true;
            this.account = account;
        }

        public ClientAccount getAccount() {
            return account;
        }

        public void send(Data d) throws IOException {
            out.writeObject(d);
            out.flush();
            out.reset();
        }

        public void send(Object d) throws IOException {
            out.writeObject(d);
            out.flush();
            out.reset();
        }

        public void close() throws IOException {
            send(new Data(-1));
            running = false;
            out.close();
            in.close();
        }

        @Override
        public void run() {
            try {
                InstituteTeacherLoad itl = null;
                if (!account.getInstitute().equals("N/A")) {
                    for (InstituteTeacherLoad i : insttituteTeacherLoad) {
                        if (i.getInstituteId().equals(account.getInstitute())) {
                            itl = i;
                            break;
                        }
                    }
                }
                send(new Data(Packet.SCHEDULE_BOARD_UPDATE, scheduleBoardUpdater, itl));
                //check if this client has pending requests
                if (permissions.stream().anyMatch(pr -> pr.getInstituteToBeSent().equals(account.getInstitute()))) {
                    System.out.println(account.getInstitute() + " has permission to be recieved");
                    List<PermissionRequest> prs
                            = permissions.stream().filter(p -> p.getInstituteToBeSent()
                                    .equals(account.getInstitute())).collect(Collectors.toList());
                    //remove
                    while (permissions.stream().anyMatch(p -> p.getInstituteToBeSent()
                            .equals(account.getInstitute()))) {
                        for (int i = 0; i < permissions.size(); i++) {
                            if (permissions.get(i).getInstituteToBeSent().equals(account.getInstitute())) {
                                permissions.remove(i);
                                break;
                            }
                        }
                    }
                    System.out.println("Request sent to");
                    send(new Data(Packet.PERMISSION_REQUEST_LIST, prs, true));
                    ChromieDB.saveListData(LoggerFile.PERMISSIONS_LOG_FILE, permissions, PermissionRequest.class);
                    permissions = ChromieDB.loadListData(LoggerFile.PERMISSIONS_LOG_FILE, PermissionRequest.class);
                }
                //check if this client has pending redponses
                if (responses.stream().anyMatch(res -> res.getSender().getId() == account.getId())) {
                    System.out.println(account.getInstitute() + " has response to be recieved");
                    List<PermissionRequest> prs
                            = responses.stream().filter(re -> re.getSender().getId()
                                    == account.getId()).collect(Collectors.toList());
                    while (responses.stream().anyMatch(re -> re.getSender().getId()
                            == account.getId())) {
                        for (int i = 0; i < responses.size(); i++) {
                            if (responses.get(i).getSender().getId() == account.getId()) {
                                responses.remove(i);
                                break;
                            }
                        }
                    }
                    System.out.println("Request sent to");
                    send(new Data(Packet.PERMISSION_RESPONSE_LIST, prs, true));
                    ChromieDB.saveListData(LoggerFile.RESPONSES_LOG_FILE, responses, PermissionRequest.class);
                    responses = ChromieDB.loadListData(LoggerFile.RESPONSES_LOG_FILE, PermissionRequest.class);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
            while (running) {
                try {
                    Data data = (Data) in.readObject();
                    switch (data.getType()) {
                        case Packet.MESSAGE:
                            sendToAll(data, account.getId());
                            appendChat(data.getMessage(), true);
                            break;
                        case Packet.CLIENT_QUERY_UPDATE:
                            //client requests to update a query
                            send(new Data(clientQueryUpdate(account.getUsername() + "(" + account.getInstitute() + ")", data.getQuery())
                                    ? Packet.QUERY_SUCCESS : Packet.QUERY_FAILED));
                            break;
                        case Packet.CLIENT_QUERY_REQUEST:
                            appendLog("CLIENT_QUERY_REQUEST from " + data.getAccount().getUsername());
                            workQueryRequest(data.getQueryData(), data.getAccount());
                            break;
                        case Packet.CLIENT_PIN_REQUEST:
                            appendLog(account.getUsername() + " requested a schedule modification.");
                            workScheduleRequest(data.getSrd(), account);
                            break;
                        case Packet.PERMISSION_REQUEST_LIST:
                            appendLog("Server received a permission request data from " + account.getAdminName() + ".");
                            sendPermissionDatagram(data.getPRList());
                            break;
                        case Packet.PERMISSION_RESPONSE_LIST:
                            appendLog("Server received a permission response data from " + account.getAdminName() + ".");
                            sendResponseDatagram(data.getPRList());
                            break;
                        case -1:
                            //cliet has disconnected
                            disconnectClient(this);
                            break;
                        default:
                            System.out.println("Server received an unknown packet protocol. Type: " + data.getType());
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ErrorDialog.showDialog(ex);
                    disconnectClient(this);
                    break;
                }
            }
        }
    }
    // </editor-fold>
}
