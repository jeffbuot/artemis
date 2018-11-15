/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.client_module;

import artemis.A;
import artemis.chat_module.ChatMessageContainer;
import artemis.chat_module.SerializableImage;
import artemis.classes.ClientAccount;
import artemis.classes.Data;
import artemis.classes.IconFactory;
import artemis.classes.InstituteFilter;
import artemis.classes.LoggerFile;
import artemis.classes.Message;
import artemis.classes.Notification;
import artemis.classes.Packet;
import artemis.classes.PermissionRequest;
import artemis.classes.QueryData;
import artemis.classes.ReportGenerator;
import artemis.classes.Tip;
import artemis.dialogs.ArtemisConfirm;
import artemis.dialogs.ArtemisMessage;
import artemis.dialogs.ArtemisPrompt;
import artemis.dialogs.ErrorDialog;
import artemis.panels.AddProgram;
import artemis.panels.AddRoom;
import artemis.panels.AddSubject;
import artemis.panels.AddTeacher;
import artemis.panels.QueryResultViewer;
import artemis.sched_board_module.ChatMessage;
import artemis.sched_board_module.ScheduleBoardUpdater;
import artemis.sched_board_module.ScheduleRequestData;
import artemis.sched_board_module.SchedulingBoard;
import artemis.view_teachers_load.InstituteTeacherLoad;
import artemis.view_teachers_load.TeacherLoadView;
import com.alee.extended.image.WebImage;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.jeff.graphics.Canvas;
import com.jeff.util.ChromieDB;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Jeff
 */
public class ClientDashboard extends javax.swing.JFrame {

    /**
     * Creates new form ClientDashboard
     *
     * @param socket
     */
    // <editor-fold defaultstate="collapsed" desc="Properties">         
    ServerReceiver sr;
    int port;
    String host;
    boolean greet = true;
    boolean online;
    private ClientAccount account;
    SchedulingBoard board;
    ArtemisPrompt load;
    ChatMessageContainer chatContainer;
    BufferedImage profilePic;
    private final String schoolYear;
    ScheduleBoardUpdater scheduleBoardUpdater;
    ArrayList<PermissionRequest> sent;
    ArrayList<PermissionRequest> received;
    InstituteTeacherLoad itl;
    File sentRequestFile;
    File receivedRequestFile;

    //</editor-fold>
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public ClientDashboard(ObjectOutputStream out, ObjectInputStream in, int port, String host, ClientAccount account) {
        initComponents();
        this.sentRequestFile = new File(account.getId() + "_" + account.getUsername()
                + "_sent_requests.xml");
        this.receivedRequestFile = new File(account.getId() + "_"
                + account.getUsername() + "_received_requests.xml");
        this.scheduleBoardUpdater = new ScheduleBoardUpdater();
        this.account = account;
        this.sent = ChromieDB.loadListData(sentRequestFile, PermissionRequest.class);
        this.received = ChromieDB.loadListData(receivedRequestFile, PermissionRequest.class);
        initProfilePic();
        reinitializeReceivedPermission();
        reinitializeSentPermission();
        this.schoolYear = account.getSy();
        chatContainer = new ChatMessageContainer(scrollChat);
        initMenu();
        setLocationRelativeTo(null);
        btnReconnect.setVisible(false);
        setTitle(account.getInstitute() + " - " + account.getName() + "(" + this.schoolYear + ") - Artemis Client");
        lblWelcome.setText("Welcome " + account.getFirstname() + "!");
        regHotkey();
        load = new ArtemisPrompt(this, "Connecting please wait...");
        board = new SchedulingBoard(this, account.getInstitute(), account.getSy(), scheduleBoardUpdater) {

            @Override
            public void addScheduleRequest(List<ScheduleRequestData> srd, boolean isOwner, String owner, String selectedRoom) {
                try {
                    if (isOwner) {
                        sr.send(new Data(Packet.CLIENT_PIN_REQUEST, srd, 0));
                    } else {
                        System.out.println("Sending request...");
                        String perm_id = account.getId() + "$"
                                + String.format("%tT\n", Calendar.getInstance()) + "$"
                                + String.format("%tF\n", Calendar.getInstance()) + "$"
                                + sent.size();
                        String perm_date = String.format("%1$ta, %1$tb %1$te, %1$ty\n", Calendar.getInstance());
                        String perm_note = account.getUsername() + "(" + account.getInstitute() + ")"
                                + " requests to plot a schedule in " + selectedRoom + "(" + owner + ")";
                        List<PermissionRequest> pr = new ArrayList<>();
                        pr.add(new PermissionRequest(perm_id, perm_date, perm_note, account, owner, srd));
                        sr.send(new Data(Packet.PERMISSION_REQUEST_LIST, pr, true));
                        TooltipManager.showOneTimeTooltip(btnNotifications, null, IconFactory.remindIcon, "Request successfuly sent.");
                        sent.add(0, pr.get(0));
                        reinitializeSentPermission();
                    }
                } catch (IOException ex) {
                    TooltipManager.showOneTimeTooltip(board, null, IconFactory.infoIcon, ex.getMessage());
                    Logger.getLogger(ClientDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void deleteSchedule(List<QueryData> qd) {
                try {
                    sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, qd, account));
                } catch (IOException ex) {
                    TooltipManager.showOneTimeTooltip(btnAddSubject, null, IconFactory.infoIcon, ex.getMessage());
                    System.out.println(ex);
                }
            }
        };
        panBoard.add(board);
        try {
            this.port = port;
            this.host = host;
            sr = new ServerReceiver(out, in);
            sr.start();
            sr.send(new Data(Packet.REQUEST_SCHED_BOARD_UPDATE));
        } catch (IOException ex) {
            ErrorDialog.showDialog(ex);
        }
    }

    private synchronized void appendReceivedRequest(List<PermissionRequest> list) {
        list.forEach(l -> l.setReceiver(account));
        list.forEach(l -> received.add(0, l));
        btnNotifications.setText("Notifications (" + list.size() + ")");
        tabPermissions.setTitleAt(0, "Received (" + list.size() + ")");
        Notification.showPopup("You have received " + list.size() + " permission"
                + " request(s) from " + list.get(0).getSender().getAdminName(), LoggerFile.notifyIcon);
    }

    private synchronized void responseReceived(List<PermissionRequest> list) {
        list.forEach(l -> {
            sent.forEach(s -> {
                if (s.getId().equals(l.getId())) {
                    s.setReceiver(l.getReceiver());
                    s.setQd(l.getQd());
                    s.setStatus(l.getStatus());
                }
            });
        });
        btnNotifications.setText("Notifications (" + list.size() + ")");
        tabPermissions.setTitleAt(1, "Sent (" + list.size() + ")");
        Notification.showPopup("You have received " + list.size() + " permission response"
                + " request(s) from " + list.get(0).getSender().getAdminName(), LoggerFile.notifyIcon);
    }

    private synchronized void reinitializeSentPermission() {
        ChromieDB.saveListData(sentRequestFile, sent, PermissionRequest.class);
        sent = ChromieDB.loadListData(sentRequestFile, PermissionRequest.class);
        tableSent.setModel(Notification.getNoteTableModel(sent));
    }

    private synchronized void reinitializeReceivedPermission() {
        ChromieDB.saveListData(receivedRequestFile, received, PermissionRequest.class);
        received = ChromieDB.loadListData(receivedRequestFile, PermissionRequest.class);
        tableReceived.setModel(Notification.getReceivedNoteTableModel(received));
    }

    private void initProfilePic() {
        try {
            File f = new File(account.getUsername() + "_profile.png");
            if (!f.exists()) {
                profilePic = ImageIO.read(getClass().getResourceAsStream("/artemis/img/default_profile.png"));
            } else {
                profilePic = ImageIO.read(f);
            }
            panProfilePic.add(new Canvas() {

                @Override
                public void draw(Graphics2D g2d) {
                    g2d.drawImage(profilePic, 0, 0, 62, 62, null);
                }
            });
        } catch (IOException | NullPointerException ex) {
            System.out.println(ex);
        }
    }

    private void initMenu() {
        panMain.add(panStart);

        btnStart.setSelected(true);
        WebButtonGroup btg;
        if (account.getAccessType().equals("View Only")) {
            btg = new WebButtonGroup(true, btnStart, btnLogs, btnMessages, btnBoard);
        } else {
            btg = new WebButtonGroup(true, btnStart, btnLogs, btnMessages, btnNotifications, btnViews, btnBoard);
        }
        btg.setButtonsDrawFocus(false);
        panMenu.add(new JLabel(new ImageIcon(getClass().getResource("/artemis/img/Beverage-Coffee-03.png"))));
        panMenu.add(btg);
        panMenu.add(new JLabel(new ImageIcon(getClass().getResource("/artemis/img/Beverage-Coffee-03.png"))));

        TooltipManager.setTooltip(btnRoom, "Rooms List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnTeacher, "Teachers List View", TooltipWay.up, 0);
        TooltipManager.setTooltip(btnSubject, "Subjects List View", TooltipWay.up, 0);
    }

    private void regHotkey() {
        KeyStroke k = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK);
        menuAddRoom.setAccelerator(null);
        menuAddSubject.setAccelerator(null);
        menuAddTeacher.setAccelerator(null);
        menuAddProgram.setAccelerator(null);
        if (btnTeacher.isSelected()) {
            menuAddTeacher.setAccelerator(k);
        } else if (btnSubject.isSelected()) {
            menuAddSubject.setAccelerator(k);
        } else if (btnRoom.isSelected()) {
            menuAddRoom.setAccelerator(k);
        } else if (btnProgram.isSelected()) {
            menuAddProgram.setAccelerator(k);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Action Events / Generated Code">
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panConnectionLogs = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        panPlottingBoard = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        panBoard = new javax.swing.JPanel();
        panMessaging = new javax.swing.JPanel();
        txtMsg = new com.alee.laf.text.WebTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        scrollChat = new javax.swing.JScrollPane();
        panNotification = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        tabPermissions = new javax.swing.JTabbedPane();
        jPanel16 = new javax.swing.JPanel();
        scrollReceived = new javax.swing.JScrollPane();
        tableReceived = new javax.swing.JTable();
        btnDecideReceived = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        scrollSent = new javax.swing.JScrollPane();
        tableSent = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        btnVRL = new javax.swing.JButton();
        panStart = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        panProfilePic = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        panProfileInfo = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        lblInfo = new javax.swing.JLabel();
        panDataView = new javax.swing.JPanel();
        panDataViewMenu = new javax.swing.JPanel();
        btnTeacher = new com.alee.laf.button.WebToggleButton();
        btnSubject = new com.alee.laf.button.WebToggleButton();
        btnRoom = new com.alee.laf.button.WebToggleButton();
        btnProgram = new com.alee.laf.button.WebToggleButton();
        panDataViewMain = new javax.swing.JPanel();
        panSubject = new javax.swing.JPanel();
        btnAddSubject = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableSubjects = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        txtSearch4 = new com.alee.laf.text.WebTextField();
        btnFilter4 = new com.alee.laf.button.WebButton();
        btnInfo3 = new com.alee.laf.button.WebButton();
        panTeacher = new javax.swing.JPanel();
        btnAddTeacher = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableTeachers = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        txtSearch3 = new com.alee.laf.text.WebTextField();
        btnFilter3 = new com.alee.laf.button.WebButton();
        btnInfo2 = new com.alee.laf.button.WebButton();
        btnTeachLoad = new javax.swing.JButton();
        btnTeachLoad1 = new javax.swing.JButton();
        panRoom = new javax.swing.JPanel();
        btnAddRoom = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableRooms = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        txtSearch5 = new com.alee.laf.text.WebTextField();
        btnFilter5 = new com.alee.laf.button.WebButton();
        btnInfo4 = new com.alee.laf.button.WebButton();
        panProgram = new javax.swing.JPanel();
        btnAddSubject1 = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablePrograms = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        txtSearch6 = new com.alee.laf.text.WebTextField();
        btnFilter6 = new com.alee.laf.button.WebButton();
        btnInfo5 = new com.alee.laf.button.WebButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        btnStart = new com.alee.laf.button.WebToggleButton();
        btnLogs = new com.alee.laf.button.WebToggleButton();
        btnMessages = new com.alee.laf.button.WebToggleButton();
        btnNotifications = new com.alee.laf.button.WebToggleButton();
        btnViews = new com.alee.laf.button.WebToggleButton();
        btnBoard = new com.alee.laf.button.WebToggleButton();
        btnReconnect = new javax.swing.JButton();
        panMenu = new javax.swing.JPanel();
        panMain = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        menuAddTeacher = new javax.swing.JMenuItem();
        menuAddSubject = new javax.swing.JMenuItem();
        menuAddRoom = new javax.swing.JMenuItem();
        menuAddProgram = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        menuChangePic = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        chkMenuHide = new javax.swing.JCheckBoxMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        txtLog.setEditable(false);
        txtLog.setBackground(new java.awt.Color(51, 51, 51));
        txtLog.setColumns(20);
        txtLog.setFont(new java.awt.Font("Monospaced", 1, 16)); // NOI18N
        txtLog.setForeground(new java.awt.Color(0, 204, 102));
        txtLog.setRows(5);
        txtLog.setText("##Client Network Service Initialized...\n");
        jScrollPane2.setViewportView(txtLog);

        jPanel3.setBackground(new java.awt.Color(91, 91, 91));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Connection Logs");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panConnectionLogsLayout = new javax.swing.GroupLayout(panConnectionLogs);
        panConnectionLogs.setLayout(panConnectionLogsLayout);
        panConnectionLogsLayout.setHorizontalGroup(
            panConnectionLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panConnectionLogsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panConnectionLogsLayout.setVerticalGroup(
            panConnectionLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panConnectionLogsLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBackground(new java.awt.Color(91, 91, 91));

        jLabel12.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Plotting Board");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel12))
        );

        panBoard.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout panPlottingBoardLayout = new javax.swing.GroupLayout(panPlottingBoard);
        panPlottingBoard.setLayout(panPlottingBoardLayout);
        panPlottingBoardLayout.setHorizontalGroup(
            panPlottingBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panBoard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panPlottingBoardLayout.setVerticalGroup(
            panPlottingBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panPlottingBoardLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panBoard, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
        );

        txtMsg.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtMsg.setHideInputPromptOnFocus(false);
        txtMsg.setInputPrompt("Enter your message here...");
        txtMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMsgActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(91, 91, 91));

        jLabel10.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Messaging");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel10))
        );

        javax.swing.GroupLayout panMessagingLayout = new javax.swing.GroupLayout(panMessaging);
        panMessaging.setLayout(panMessagingLayout);
        panMessagingLayout.setHorizontalGroup(
            panMessagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panMessagingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(scrollChat, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        panMessagingLayout.setVerticalGroup(
            panMessagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panMessagingLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrollChat, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(91, 91, 91));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Scheduling Board Requests");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel11))
        );

        tabPermissions.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabPermissions.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPermissionsStateChanged(evt);
            }
        });

        tableReceived.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tableReceived.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Note/Request", "Sender"
            }
        ));
        tableReceived.setRowHeight(28);
        tableReceived.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableReceived.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableReceivedMouseClicked(evt);
            }
        });
        scrollReceived.setViewportView(tableReceived);
        if (tableReceived.getColumnModel().getColumnCount() > 0) {
            tableReceived.getColumnModel().getColumn(0).setPreferredWidth(100);
            tableReceived.getColumnModel().getColumn(1).setPreferredWidth(400);
        }

        btnDecideReceived.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnDecideReceived.setText("Decide For Selected Item");
        btnDecideReceived.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDecideReceivedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollReceived, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnDecideReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(btnDecideReceived)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollReceived, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE))
        );

        tabPermissions.addTab("Received", new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Message-Mail.png")), jPanel16); // NOI18N

        tableSent.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tableSent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Note/Request", "Sender"
            }
        ));
        tableSent.setRowHeight(28);
        tableSent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSentMouseClicked(evt);
            }
        });
        scrollSent.setViewportView(tableSent);
        if (tableSent.getColumnModel().getColumnCount() > 0) {
            tableSent.getColumnModel().getColumn(0).setPreferredWidth(100);
            tableSent.getColumnModel().getColumn(1).setPreferredWidth(400);
        }

        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jButton3.setText("Delete Selected");

        btnVRL.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnVRL.setText("View Request Log");
        btnVRL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVRLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollSent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVRL, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(btnVRL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollSent, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE))
        );

        tabPermissions.addTab("Sent", new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Mail - Sent.png")), jPanel17); // NOI18N

        javax.swing.GroupLayout panNotificationLayout = new javax.swing.GroupLayout(panNotification);
        panNotification.setLayout(panNotificationLayout);
        panNotificationLayout.setHorizontalGroup(
            panNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panNotificationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPermissions)
                .addContainerGap())
        );
        panNotificationLayout.setVerticalGroup(
            panNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panNotificationLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabPermissions)
                .addContainerGap())
        );

        panStart.setBackground(new java.awt.Color(0, 204, 153));

        lblWelcome.setFont(new java.awt.Font("Segoe UI Semilight", 0, 24)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(255, 255, 255));
        lblWelcome.setText("Welcome [name]!");

        panProfilePic.setLayout(new java.awt.BorderLayout());

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

        javax.swing.GroupLayout panStartLayout = new javax.swing.GroupLayout(panStart);
        panStart.setLayout(panStartLayout);
        panStartLayout.setHorizontalGroup(
            panStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panStartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panStartLayout.createSequentialGroup()
                        .addComponent(panProfilePic, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 694, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panStartLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3))
        );
        panStartLayout.setVerticalGroup(
            panStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panStartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblWelcome, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                    .addComponent(panProfilePic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                .addComponent(jLabel3))
        );

        jPanel7.setBackground(new java.awt.Color(0, 204, 102));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        lblInfo.setFont(new java.awt.Font("Segoe UI Light", 0, 16)); // NOI18N
        lblInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblInfo.setText("jLabel1");

        javax.swing.GroupLayout panProfileInfoLayout = new javax.swing.GroupLayout(panProfileInfo);
        panProfileInfo.setLayout(panProfileInfoLayout);
        panProfileInfoLayout.setHorizontalGroup(
            panProfileInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panProfileInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                .addContainerGap())
        );
        panProfileInfoLayout.setVerticalGroup(
            panProfileInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panProfileInfoLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addContainerGap())
        );

        buttonGroup1.add(btnTeacher);
        btnTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Teacher-03.png"))); // NOI18N
        btnTeacher.setSelected(true);
        btnTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeacherActionPerformed(evt);
            }
        });
        panDataViewMenu.add(btnTeacher);

        buttonGroup1.add(btnSubject);
        btnSubject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Book-Closed-WF.png"))); // NOI18N
        btnSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubjectActionPerformed(evt);
            }
        });
        panDataViewMenu.add(btnSubject);

        buttonGroup1.add(btnRoom);
        btnRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Meeting.png"))); // NOI18N
        btnRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRoomActionPerformed(evt);
            }
        });
        panDataViewMenu.add(btnRoom);

        buttonGroup1.add(btnProgram);
        btnProgram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/program.png"))); // NOI18N
        btnProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProgramActionPerformed(evt);
            }
        });
        panDataViewMenu.add(btnProgram);

        panDataViewMain.setBackground(new java.awt.Color(153, 153, 153));
        panDataViewMain.setLayout(new java.awt.BorderLayout());
        panDataViewMain.add(panTeacher);

        javax.swing.GroupLayout panDataViewLayout = new javax.swing.GroupLayout(panDataView);
        panDataView.setLayout(panDataViewLayout);
        panDataViewLayout.setHorizontalGroup(
            panDataViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDataViewLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(panDataViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panDataViewMain, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                    .addComponent(panDataViewMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );
        panDataViewLayout.setVerticalGroup(
            panDataViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDataViewLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(panDataViewMain, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(panDataViewMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        jPanel8.setBackground(new java.awt.Color(81, 81, 81));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Subjects List View");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
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
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panSubjectLayout.createSequentialGroup()
                        .addComponent(btnAddSubject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panSubjectLayout.setVerticalGroup(
            panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSubjectLayout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddSubject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddTeacher.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddTeacher.setText("New Teacher");
        btnAddTeacher.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
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

        jPanel9.setBackground(new java.awt.Color(81, 81, 81));

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Teachers List View");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
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

        btnTeachLoad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnTeachLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Business-Man.png"))); // NOI18N
        btnTeachLoad.setText("Vew Load");
        btnTeachLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeachLoadActionPerformed(evt);
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
                        .addComponent(btnAddTeacher)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTeachLoad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTeachLoad1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panTeacherLayout.setVerticalGroup(
            panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTeacherLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panTeacherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddTeacher, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTeachLoad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTeachLoad1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
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
        jScrollPane4.setViewportView(tableRooms);

        jPanel10.setBackground(new java.awt.Color(81, 81, 81));

        jLabel13.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Rooms List View");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel13))
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
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 737, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panRoomLayout.createSequentialGroup()
                        .addComponent(btnAddRoom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panRoomLayout.setVerticalGroup(
            panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panRoomLayout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddSubject1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddSubject1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Document-Add-01.png"))); // NOI18N
        btnAddSubject1.setText("New Programs");
        btnAddSubject1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAddSubject1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSubject1ActionPerformed(evt);
            }
        });

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
        tableSubjects.getTableHeader().setReorderingAllowed(false);
        tablePrograms.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableProgramsMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tablePrograms);

        jPanel14.setBackground(new java.awt.Color(81, 81, 81));

        jLabel14.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Programs List View");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel14))
        );

        txtSearch6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch6.setInputPrompt("Search....");
        txtSearch6.setPreferredSize(new java.awt.Dimension(285, 30));
        txtSearch6.setRound(12);
        txtSearch6.setMargin(0,0,0,2);
        txtSearch6.setTrailingComponent(new WebImage(new ImageIcon(getClass().getResource("/artemis/img/Magnifying-Glass.png"))));
        jPanel15.add(txtSearch6);

        btnFilter6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Filter-Settings.png"))); // NOI18N
        btnFilter6.setUndecorated(true);
        btnFilter6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter6ActionPerformed(evt);
            }
        });
        jPanel15.add(btnFilter6);

        btnInfo5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Electric Bulb-wf.png"))); // NOI18N
        btnInfo5.setUndecorated(true);
        btnInfo5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInfo5ActionPerformed(evt);
            }
        });
        jPanel15.add(btnInfo5);

        javax.swing.GroupLayout panProgramLayout = new javax.swing.GroupLayout(panProgram);
        panProgram.setLayout(panProgramLayout);
        panProgramLayout.setHorizontalGroup(
            panProgramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panProgramLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panProgramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panProgramLayout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panProgramLayout.createSequentialGroup()
                        .addComponent(btnAddSubject1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panProgramLayout.setVerticalGroup(
            panProgramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panProgramLayout.createSequentialGroup()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panProgramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddSubject1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnStart.setText("Start Page");
        btnStart.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnLogs.setText("Network Logs");
        btnLogs.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogsActionPerformed(evt);
            }
        });

        btnMessages.setText("Messaging");
        btnMessages.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMessagesActionPerformed(evt);
            }
        });

        btnNotifications.setText("Notifications");
        btnNotifications.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnNotifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNotificationsActionPerformed(evt);
            }
        });

        btnViews.setText("Programs & Facilities");
        btnViews.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnViews.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewsActionPerformed(evt);
            }
        });

        btnBoard.setText("Scheduling Board");
        btnBoard.setFont(new java.awt.Font("Segoe UI Light", 0, 14)); // NOI18N
        btnBoard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBoardActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnReconnect.setBackground(new java.awt.Color(255, 0, 0));
        btnReconnect.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        btnReconnect.setText("Server connection lost. Click here to reconnect.");
        btnReconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReconnectActionPerformed(evt);
            }
        });

        panMenu.setBackground(new java.awt.Color(0, 204, 153));

        panMain.setBackground(new java.awt.Color(51, 51, 51));
        panMain.setLayout(new java.awt.BorderLayout());

        jMenu1.setText("File");

        jMenu3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuNew.png"))); // NOI18N
        jMenu3.setText("New");

        menuAddTeacher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu2.png"))); // NOI18N
        menuAddTeacher.setText("Teachers");
        menuAddTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddTeacherActionPerformed(evt);
            }
        });
        jMenu3.add(menuAddTeacher);

        menuAddSubject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu6.png"))); // NOI18N
        menuAddSubject.setText("Subjects");
        menuAddSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddSubjectActionPerformed(evt);
            }
        });
        jMenu3.add(menuAddSubject);

        menuAddRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menu5.png"))); // NOI18N
        menuAddRoom.setText("Rooms");
        menuAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddRoomActionPerformed(evt);
            }
        });
        jMenu3.add(menuAddRoom);

        menuAddProgram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/program_menu.png"))); // NOI18N
        menuAddProgram.setText("Programs");
        menuAddProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddProgramActionPerformed(evt);
            }
        });
        jMenu3.add(menuAddProgram);

        jMenu1.add(jMenu3);
        jMenu1.add(jSeparator1);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/menuLogout.png"))); // NOI18N
        jMenuItem1.setText("Logout");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("Edit");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/Chat - Cancel.png"))); // NOI18N
        jMenuItem2.setText("Clear Chat Messages");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem2);

        menuChangePic.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        menuChangePic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/ID-Information.png"))); // NOI18N
        menuChangePic.setText("Change Profile Picture");
        menuChangePic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuChangePicActionPerformed(evt);
            }
        });
        jMenu4.add(menuChangePic);

        jMenuBar1.add(jMenu4);

        jMenu2.setText("View");

        chkMenuHide.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        chkMenuHide.setSelected(true);
        chkMenuHide.setText("Show Main Menu");
        chkMenuHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMenuHideActionPerformed(evt);
            }
        });
        jMenu2.add(chkMenuHide);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Data Request Logger");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnReconnect, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnReconnect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panMain, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMsgActionPerformed
        if (txtMsg.getText().trim().equals("")) {
            return;
        }
        try {
            Message m = new Message(account.getFirstname(), txtMsg.getText(), A.getDateTime(),
                    new SerializableImage(profilePic), account.getId());
            sr.send(new Data(Packet.MESSAGE, m));
            appendMsg(m, false);
            txtMsg.setText("");
        } catch (IOException ex) {
            TooltipManager.showOneTimeTooltip(txtMsg, null, IconFactory.infoIcon, ex.getMessage());
            System.out.println(ex);
        }
    }//GEN-LAST:event_txtMsgActionPerformed

    private void btnReconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReconnectActionPerformed
        try {
            load.showDialog();
            Socket socket = new Socket(host, port);
            ObjectOutputStream outt = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inn = new ObjectInputStream(socket.getInputStream());
            //sending credentials
            load.closeDialog();
            outt.writeObject(new String[]{account.getUsername(), account.getPassword()});
            outt.flush();

            //map validity.id.fn.ln.un.accessType.accessStatus
            ClientAccount acc = (ClientAccount) inn.readObject();
            if (acc == null) {
                ArtemisMessage.showDialog(this, "Your username/password does not exist.");
                return;
            } else if (acc.getAccessStatus().equals("Locked")) {
                ArtemisMessage.showDialog(this, "Your account is locked, please contact your administrator.");
                return;
            } else if (acc.getAccessStatus().equals("Draft")) {
                ArtemisMessage.showDialog(this, "Please be notice that your account has been removed by the administrator");
                return;
            }
            sr = new ServerReceiver(outt, inn);
            sr.start();
            log("You are now reconnected.");
            ArtemisMessage.showDialog(this, "Reconnected!");
            btnReconnect.setVisible(false);
        } catch (IOException | ClassNotFoundException ex) {
            load.closeDialog();
            ArtemisMessage.showDialog(this, "Error connecting.");
        }
    }//GEN-LAST:event_btnReconnectActionPerformed

    private void chkMenuHideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMenuHideActionPerformed
        panMenu.setVisible(chkMenuHide.isSelected());
    }//GEN-LAST:event_chkMenuHideActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        chatContainer.clearChat();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void menuChangePicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuChangePicActionPerformed
        JFileChooser jf = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image File", "jpg", "png", "bmp", "gif");
        jf.setFileFilter(filter);
        jf.showDialog(null, "Select");
        try {
            BufferedImage img = ImageIO.read(jf.getSelectedFile());
            File f = new File(account.getUsername() + "_profile.png");
            ImageIO.write(img, "PNG", f);
            initProfilePic();
        } catch (IOException ex) {
            Logger.getLogger(ClientDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_menuChangePicActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        panMain.removeAll();
        panMain.add(panStart);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogsActionPerformed
        panMain.removeAll();
        panMain.add(panConnectionLogs);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnLogsActionPerformed

    private void btnMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMessagesActionPerformed
        panMain.removeAll();
        panMain.add(panMessaging);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnMessagesActionPerformed

    private void btnNotificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotificationsActionPerformed
        panMain.removeAll();
        btnNotifications.setText("Notifications");
        panMain.add(panNotification);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnNotificationsActionPerformed

    private void btnViewsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewsActionPerformed
        panMain.removeAll();
        panMain.add(panDataView);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnViewsActionPerformed

    private void btnBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBoardActionPerformed
        panMain.removeAll();
        panMain.add(panPlottingBoard);
        panMain.validate();
        repaint();
    }//GEN-LAST:event_btnBoardActionPerformed

    private void btnSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubjectActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panSubject);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnSubjectActionPerformed

    private void btnTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeacherActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panTeacher);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnTeacherActionPerformed

    private void btnRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRoomActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panRoom);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnRoomActionPerformed

    private void btnAddSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSubjectActionPerformed
        addSubject();
    }//GEN-LAST:event_btnAddSubjectActionPerformed

    private void tableSubjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSubjectsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableSubjects.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 0).toString());
                hash.put("desc", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 1).toString());
                hash.put("lec_units", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 2).toString());
                hash.put("lab_units", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 3).toString());
                hash.put("program_facilitator", tableSubjects.getValueAt(tableSubjects.getSelectedRow(), 4).toString());
                String data[] = AddSubject.clientUpdate(this, hash);
                if (!data[0].isEmpty()) {
                    List<QueryData> queries = new ArrayList<>();
                    queries.add(new QueryData(hash.get("id"), "Update subject (" + data[1] + ").", data[0]));
                    try {
                        sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
                    } catch (IOException ex) {
                        TooltipManager.showOneTimeTooltip(tableSubjects, null, IconFactory.infoIcon, ex.getMessage());
                    }
                }
            }
        }
    }//GEN-LAST:event_tableSubjectsMouseClicked

    private void btnFilter4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter4ActionPerformed

    private void btnInfo3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo3ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo3ActionPerformed

    private void btnAddTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTeacherActionPerformed
        addTeacher();
    }//GEN-LAST:event_btnAddTeacherActionPerformed

    private void tableTeachersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableTeachersMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableTeachers.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 0).toString());
                hash.put("firstname", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 1).toString());
                hash.put("lastname", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 2).toString());
                hash.put("gender", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 3).toString());
                hash.put("instituteId", tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 4).toString());
                String[] data = AddTeacher.updateByClient(this, hash);
                if (!data[0].isEmpty()) {
                    List<QueryData> queries = new ArrayList<>();
                    queries.add(new QueryData(hash.get("id"), "Update teacher (" + data[1] + ").", data[0]));
                    try {
                        sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
                    } catch (IOException ex) {
                        TooltipManager.showOneTimeTooltip(tableTeachers, null, IconFactory.infoIcon, ex.getMessage());
                    }
                }
            }
        }
    }//GEN-LAST:event_tableTeachersMouseClicked

    private void btnFilter3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter3ActionPerformed

    private void btnInfo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo2ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo2ActionPerformed

    private void btnAddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRoomActionPerformed
        addRoom();
    }//GEN-LAST:event_btnAddRoomActionPerformed

    private void tableRoomsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableRoomsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableRooms.getSelectedRow() != -1) {
                if (!tableRooms.getValueAt(tableRooms.getSelectedRow(), 3).toString().equals("Draft")) {

                    HashMap<String, String> hash = new HashMap<>();
                    hash.put("id", tableRooms.getValueAt(tableRooms.getSelectedRow(), 0).toString());
                    hash.put("desc", tableRooms.getValueAt(tableRooms.getSelectedRow(), 1).toString());
                    hash.put("facilitator", tableRooms.getValueAt(tableRooms.getSelectedRow(), 2).toString());
                    hash.put("accessStatus", tableRooms.getValueAt(tableRooms.getSelectedRow(), 3).toString());
                    String data[] = AddRoom.clientUpdate(this, hash);
                    if (!data[0].isEmpty()) {
                        List<QueryData> queries = new ArrayList<>();
                        queries.add(new QueryData(hash.get("id"), "Update room (" + data[1] + ").", data[0]));
                        try {
                            sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
                        } catch (IOException ex) {
                            TooltipManager.showOneTimeTooltip(tableRooms, null, IconFactory.infoIcon, ex.getMessage());
                        }
                    }
                } else {
                    ArtemisMessage.showDialog(this, "Unable To Update", "Drafts cannot be modified anymore.", ArtemisMessage.WARNING);
                }
            }
        }
    }//GEN-LAST:event_tableRoomsMouseClicked

    private void btnFilter5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter5ActionPerformed

    private void btnInfo4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo4ActionPerformed
        Tip.showTip(this, evt, Tip.DEF_TIP);
    }//GEN-LAST:event_btnInfo4ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        RequestLogViewer.showDialog(this);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void menuAddTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddTeacherActionPerformed
        addTeacher();
    }//GEN-LAST:event_menuAddTeacherActionPerformed

    private void menuAddSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddSubjectActionPerformed
        addSubject();
    }//GEN-LAST:event_menuAddSubjectActionPerformed

    private void menuAddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddRoomActionPerformed
        addRoom();
    }//GEN-LAST:event_menuAddRoomActionPerformed

    private void btnProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProgramActionPerformed
        panDataViewMain.removeAll();
        panDataViewMain.add(panProgram);
        panDataViewMain.validate();
        regHotkey();
        repaint();
    }//GEN-LAST:event_btnProgramActionPerformed

    private void btnAddSubject1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSubject1ActionPerformed
        addProgram();
    }//GEN-LAST:event_btnAddSubject1ActionPerformed

    private void tableProgramsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableProgramsMouseClicked
        if (evt.getClickCount() == 2) {
            if (tablePrograms.getSelectedRow() != -1) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("id", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 0).toString());
                hash.put("desc", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 1).toString());
                hash.put("facilitator", tablePrograms.getValueAt(tablePrograms.getSelectedRow(), 2).toString());
                String data[] = AddProgram.clientUpdate(this, hash, scheduleBoardUpdater.getBlu());
                if (!data[0].isEmpty()) {
                    List<QueryData> queries = new ArrayList<>();
                    queries.add(new QueryData(hash.get("id"), "Update program (" + data[1] + ").", data[0]));
                    try {
                        sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
                    } catch (IOException ex) {
                        TooltipManager.showOneTimeTooltip(tablePrograms, null, IconFactory.infoIcon, ex.getMessage());
                    }
                }
            }
        }
    }//GEN-LAST:event_tableProgramsMouseClicked

    private void btnFilter6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFilter6ActionPerformed

    private void btnInfo5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfo5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnInfo5ActionPerformed

    private void menuAddProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddProgramActionPerformed
        addProgram();
    }//GEN-LAST:event_menuAddProgramActionPerformed

    private void tableSentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSentMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableSent.getSelectedRow() != -1) {
                if (sent.get(tableSent.getSelectedRow()).getQd() != null) {
                    QueryResultViewer.showDialog(this, sent.get(tableSent.getSelectedRow()).getQd());
                } else {
                    TooltipManager.showOneTimeTooltip(tableSent, null, IconFactory.infoIcon,
                            "There is empty request log on the selected item.");
                }
            }
        }
    }//GEN-LAST:event_tableSentMouseClicked

    private void tabPermissionsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabPermissionsStateChanged
        tabPermissions.setTitleAt(0, "Received");
    }//GEN-LAST:event_tabPermissionsStateChanged

    private void tableReceivedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableReceivedMouseClicked
        if (evt.getClickCount() == 2) {
            if (tableReceived.getSelectedRow() != -1) {
                decideReceived();
            } else {
                TooltipManager.showOneTimeTooltip(tableReceived, null, IconFactory.infoIcon,
                        "Please select an item in the table.");
            }
        }
    }//GEN-LAST:event_tableReceivedMouseClicked

    private void btnDecideReceivedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDecideReceivedActionPerformed
        if (tableReceived.getSelectedRow() != -1) {
            decideReceived();
        } else {
            TooltipManager.showOneTimeTooltip(btnDecideReceived, null, IconFactory.infoIcon,
                    "Please select an item in the table.");
        }
    }//GEN-LAST:event_btnDecideReceivedActionPerformed

    private void btnVRLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVRLActionPerformed
        if (sent.get(tableSent.getSelectedRow()).getQd() != null) {
            QueryResultViewer.showDialog(this, sent.get(tableSent.getSelectedRow()).getQd());
        } else {
            TooltipManager.showOneTimeTooltip(btnVRL, null, IconFactory.infoIcon,
                    "There is empty request log on the selected item.");
        }
    }//GEN-LAST:event_btnVRLActionPerformed

    private void btnTeachLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeachLoadActionPerformed
        if (tableTeachers.getSelectedRow() != -1) {
            String teachName = (String) tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 2) + ", "
                    + (String) tableTeachers.getValueAt(tableTeachers.getSelectedRow(), 1);
            TeacherLoadView.showDialog(this, itl.getTeachersLoad(tableTeachers.
                    getValueAt(tableTeachers.getSelectedRow(), 0).toString()).getTableModel(), teachName, account.getSy());
        } else {
            TooltipManager.showOneTimeTooltip(btnTeachLoad, null, IconFactory.infoIcon,
                    "Please select a teachern below.");
        }
    }//GEN-LAST:event_btnTeachLoadActionPerformed

    private void btnTeachLoad1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeachLoad1ActionPerformed
        new ReportGenerator(account.getSy(), account.getInstitute(), this, itl.getTeacherLoads()).start();
    }//GEN-LAST:event_btnTeachLoad1ActionPerformed
// </editor-fold>

    private void decideReceived() {
        if (!received.get(tableReceived.getSelectedRow()).getStatus().equals("Pending")) {
            return;
        }
        int conf = ArtemisConfirm.showDialog(this, "Permission Confirm", "What do you "
                + "want to do to this request?", "Accept", "Decline");
        if (conf == 0) {
            received.get(tableReceived.getSelectedRow()).setStatus("Confirmed");
        } else if (conf == 1) {
            received.get(tableReceived.getSelectedRow()).setStatus("Declined");
        } else {
            System.out.println("Closed");
        }
        PermissionRequest response = received.get(tableReceived.getSelectedRow());
        List<PermissionRequest> pr = new ArrayList<>();
        pr.add(response);
        try {
            sr.send(new Data(Packet.PERMISSION_RESPONSE_LIST, pr, true));
            reinitializeReceivedPermission();
            Notification.showPopup("Response sent.", IconFactory.messageIcon);
        } catch (IOException ex) {
            TooltipManager.showOneTimeTooltip(tableReceived, null, IconFactory.infoIcon, ex.getMessage());
            Logger.getLogger(ClientDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addProgram() {
        List<QueryData> queries = AddProgram.clientShowDialog(this, scheduleBoardUpdater.getBlu(), account.getInstitute());
        if (queries.size() > 0) {
            try {
                sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
            } catch (IOException ex) {
                TooltipManager.showOneTimeTooltip(btnAddTeacher, null, IconFactory.infoIcon, ex.getMessage());
                System.out.println(ex);
            }
        }
    }

    private void addTeacher() {
        List<QueryData> queries = AddTeacher.clientShowDialog(this, scheduleBoardUpdater.getBlu(), account.getInstitute());
        if (queries.size() > 0) {
            try {
                sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
            } catch (IOException ex) {
                TooltipManager.showOneTimeTooltip(btnAddTeacher, null, IconFactory.infoIcon, ex.getMessage());
                System.out.println(ex);
            }
        }
    }

    private void addSubject() {
        List<QueryData> queries = AddSubject.clientShowDialog(this, scheduleBoardUpdater.getBlu(), account.getInstitute());
        if (queries.size() > 0) {
            try {
                sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
            } catch (IOException ex) {
                TooltipManager.showOneTimeTooltip(btnAddSubject, null, IconFactory.infoIcon, ex.getMessage());
                System.out.println(ex);
            }
        }
    }

    private void addRoom() {
        List<QueryData> queries = AddRoom.clientShowDialog(this, scheduleBoardUpdater.getBlu(), account.getInstitute());
        if (queries.size() > 0) {
            try {
                sr.send(new Data(Packet.CLIENT_QUERY_REQUEST, queries, account));
            } catch (IOException ex) {
                TooltipManager.showOneTimeTooltip(btnAddRoom, null, IconFactory.infoIcon, ex.getMessage());
                System.out.println(ex);
            }
        }
    }

    private synchronized void refreshDataTables() {
        //update the board
        board.updateScheduleBoardUpdater(scheduleBoardUpdater);
        //filter rooms by institute id
        tableRooms.setModel(InstituteFilter.filterModel(scheduleBoardUpdater.getBlu().getModelRooms(), "institute_id", account.getInstitute()));
        tableRooms.validate();
        //filter subjects by institute id
        tableSubjects.setModel(InstituteFilter.filterModel(scheduleBoardUpdater.getBlu().getModelSubjects(), "program_facilitator", account.getInstitute()));
        tableSubjects.validate();
        //filter teachers by institute id
        tableTeachers.setModel(InstituteFilter.filterModel(scheduleBoardUpdater.getBlu().getModelTeachers(), "institute_id", account.getInstitute()));
        tableTeachers.validate();
        //filter programs by institute id
        tablePrograms.setModel(InstituteFilter.filterModel(scheduleBoardUpdater.getBlu().getModelPrograms(), "facilitator", account.getInstitute()));
        tablePrograms.validate();
    }

    private void addRequestDataLog(List<QueryData> qd) {
        ArrayList<RequestLogData> list = ChromieDB.loadListData(LoggerFile.LOG_FILE, RequestLogData.class);
        list.add(new RequestLogData(qd, String.format("%1$ta, %1$tb %1$te, %1$ty\n", Calendar.getInstance()),
                String.format("%tr\n", Calendar.getInstance()), qd.get(0).getDesc()));
        ChromieDB.saveListData(LoggerFile.LOG_FILE, list, RequestLogData.class);
        Notification.notifyWithLogger(this, qd, "Data update received.",qd.stream().allMatch(q->q.isSuccess()));
    }

    private synchronized void log(String t) {
        txtLog.append(String.format("%tr> %s\n", Calendar.getInstance(), t));
        txtLog.setCaretPosition(txtLog.getText().length());
    }

    private synchronized void appendMsg(Message msg, boolean isNotMe) {
        if (msg.getUserId() != account.getId() && !btnMessages.isSelected()) {
            beep();
        }
        Message m = new Message(msg.getSender(), msg.getMessage(), msg.getTimeInfo(), msg.getSenderImage(), msg.getUserId());
        chatContainer.appendMessage(new ChatMessage(m, isNotMe));
    }

    private synchronized void beep() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/artemis/other/beep_7.wav")));
            clip.start();
        } catch (NullPointerException | LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
        }
    }

    @Override
    public void dispose() {
        int choose = ArtemisConfirm.showDialog(this, "Logout", "Are you sure to logout?");
        if (choose == 0) {
            try {
                sr.send(new Data(-1));
                sr.close();
            } catch (IOException ex) {
            } finally {
                System.exit(0);
            }
        }
    }

    private Component getMe() {
        return this;
    }

    private class ServerReceiver extends Thread {

        ObjectOutputStream out;
        ObjectInputStream in;
        boolean receiving;

        public ServerReceiver(ObjectOutputStream out, ObjectInputStream in) throws IOException {
            this.out = out;
            this.in = in;
            online = true;
            receiving = true;
        }

        public void send(Data obj) throws IOException {
            out.writeObject(obj);
            out.flush();
            out.reset();
        }

        private void close() {
            try {
                out.close();
                in.close();
                online = false;
                receiving = false;
                btnReconnect.setVisible(true);
            } catch (IOException ex) {
            }
        }

        @Override
        public void run() {
            while (receiving) {
                try {
                    Data data = (Data) in.readObject();
                    switch (data.getType()) {
                        case Packet.MESSAGE:
                            appendMsg(data.getMessage(), true);
                            break;
                        case Packet.SERVER_QUERY_RESULT:
                            addRequestDataLog(data.getQueryData());
                            refreshDataTables();
                            log("A request has been worked out from the server received.");
                            break;
                        case Packet.SCHEDULE_BOARD_UPDATE:
                            if (greet) {
                                Notification.showPopup("Welcome " + account.getFirstname() + "!", IconFactory.happyIcon);
                                greet = false;
                            }
                            scheduleBoardUpdater = data.getClientUpdater();
                            itl = data.getInstituteTeacherLoad();
                            refreshDataTables();
                            log("Schedule model has been updated by the server.");
                            break;
                        case Packet.PERMISSION_REQUEST_LIST:
                            appendReceivedRequest(data.getPRList());
                            reinitializeReceivedPermission();
                            break;
                        case Packet.PERMISSION_RESPONSE_LIST:
                            responseReceived(data.getPRList());
                            reinitializeSentPermission();
                            break;
                        case -1:
                            //close connection
                            log("You have been disconnected from the server's network.");
//                            ArtemisMessage.showDialog(getMe(), "You are disconnected from the server.");
                            ArtemisMessage.showDialog(getMe(), "Warning",
                                    "You are disconnected from the server.", ArtemisMessage.WARNING);
                            close();
                            break;
                    }
                } catch (IOException ex) {
                    ErrorDialog.showDialog(ex);
                    if (StreamCorruptedException.class.equals(ex.getClass())) {
                        ArtemisMessage.showDialog(getMe(), "Info for IT staff only", "Stream corruption occured.");
                    } else if (ClassCastException.class.equals(ex.getClass())) {
                        ArtemisMessage.showDialog(getMe(), "Info for IT staff only", "Class Cast Exception occured");
                    } else {
                        ArtemisMessage.showDialog(getMe(), "Warning",
                                "You are disconnected from the server for "
                                + "a sudden network error occured.", ArtemisMessage.WARNING);
                        close();
                        break;
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClientDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Declartions">         
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRoom;
    private javax.swing.JButton btnAddSubject;
    private javax.swing.JButton btnAddSubject1;
    private javax.swing.JButton btnAddTeacher;
    private com.alee.laf.button.WebToggleButton btnBoard;
    private javax.swing.JButton btnDecideReceived;
    private com.alee.laf.button.WebButton btnFilter3;
    private com.alee.laf.button.WebButton btnFilter4;
    private com.alee.laf.button.WebButton btnFilter5;
    private com.alee.laf.button.WebButton btnFilter6;
    private com.alee.laf.button.WebButton btnInfo2;
    private com.alee.laf.button.WebButton btnInfo3;
    private com.alee.laf.button.WebButton btnInfo4;
    private com.alee.laf.button.WebButton btnInfo5;
    private com.alee.laf.button.WebToggleButton btnLogs;
    private com.alee.laf.button.WebToggleButton btnMessages;
    private com.alee.laf.button.WebToggleButton btnNotifications;
    private com.alee.laf.button.WebToggleButton btnProgram;
    private javax.swing.JButton btnReconnect;
    private com.alee.laf.button.WebToggleButton btnRoom;
    private com.alee.laf.button.WebToggleButton btnStart;
    private com.alee.laf.button.WebToggleButton btnSubject;
    private javax.swing.JButton btnTeachLoad;
    private javax.swing.JButton btnTeachLoad1;
    private com.alee.laf.button.WebToggleButton btnTeacher;
    private javax.swing.JButton btnVRL;
    private com.alee.laf.button.WebToggleButton btnViews;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBoxMenuItem chkMenuHide;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JMenuItem menuAddProgram;
    private javax.swing.JMenuItem menuAddRoom;
    private javax.swing.JMenuItem menuAddSubject;
    private javax.swing.JMenuItem menuAddTeacher;
    private javax.swing.JMenuItem menuChangePic;
    private javax.swing.JPanel panBoard;
    private javax.swing.JPanel panConnectionLogs;
    private javax.swing.JPanel panDataView;
    private javax.swing.JPanel panDataViewMain;
    private javax.swing.JPanel panDataViewMenu;
    private javax.swing.JPanel panMain;
    private javax.swing.JPanel panMenu;
    private javax.swing.JPanel panMessaging;
    private javax.swing.JPanel panNotification;
    private javax.swing.JPanel panPlottingBoard;
    private javax.swing.JPanel panProfileInfo;
    private javax.swing.JPanel panProfilePic;
    private javax.swing.JPanel panProgram;
    private javax.swing.JPanel panRoom;
    private javax.swing.JPanel panStart;
    private javax.swing.JPanel panSubject;
    private javax.swing.JPanel panTeacher;
    private javax.swing.JScrollPane scrollChat;
    private javax.swing.JScrollPane scrollReceived;
    private javax.swing.JScrollPane scrollSent;
    private javax.swing.JTabbedPane tabPermissions;
    private javax.swing.JTable tablePrograms;
    private javax.swing.JTable tableReceived;
    private javax.swing.JTable tableRooms;
    private javax.swing.JTable tableSent;
    private javax.swing.JTable tableSubjects;
    private javax.swing.JTable tableTeachers;
    private javax.swing.JTextArea txtLog;
    private com.alee.laf.text.WebTextField txtMsg;
    private com.alee.laf.text.WebTextField txtSearch3;
    private com.alee.laf.text.WebTextField txtSearch4;
    private com.alee.laf.text.WebTextField txtSearch5;
    private com.alee.laf.text.WebTextField txtSearch6;
    // End of variables declaration//GEN-END:variables
// </editor-fold>         
}
