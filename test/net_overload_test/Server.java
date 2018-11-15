/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package net_overload_test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeff
 */
public class Server extends javax.swing.JFrame {

    /**
     * Creates new form Server
     */
    public Server() {
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtCon = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        btnSt = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Communications", jPanel1);

        txtCon.setEditable(false);
        txtCon.setColumns(20);
        txtCon.setRows(5);
        jScrollPane2.setViewportView(txtCon);

        jScrollPane3.setViewportView(list);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Clients", jPanel2);

        btnSt.setText("Start");
        btnSt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(btnSt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSt)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStActionPerformed
        if (btnSt.isSelected()) {
            startServer();
        } else {
            stopServer();
        }
    }//GEN-LAST:event_btnStActionPerformed

    private void startServer() {
        btnSt.setText("Stop");
        log("Server started.");
        try {
            new Receiver(48787).start();
        } catch (IOException ex) {
        }
    }

    private void stopServer() {
        btnSt.setText("Start");
    }

    private synchronized void log(String str) {
        txtLog.append(str + "\n");
        txtLog.setCaretPosition(txtLog.getText().length());
    }

    private void con(String str) {
        txtCon.setText(str + "\n");
        txtCon.setCaretPosition(txtCon.getText().length());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Server().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnSt;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList list;
    private javax.swing.JTextArea txtCon;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables

    ArrayList<Handler> clients = new ArrayList<>();
    int count = 0;

    public class Receiver extends Thread {

        private final ServerSocket serverSocket;
        private boolean running;

        public Receiver(int port) throws IOException {
            serverSocket = new ServerSocket(port);
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    String n = "Client " + clients.size();
                    ObjectInputStream i = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
                    Handler h = new Handler(n, socket.getInetAddress().getHostName(), i, o);
                    clients.add(h);
                    h.start();
                    log(n + " received.");
                } catch (IOException e) {
                    log(e.toString());
                }
            }
            log("Server has stopped receiving clients.");
        }
    }

    public class Handler extends Thread {

        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private final String ip;
        private final String clientName;
        private boolean running;

        public Handler(String cn, String ip, ObjectInputStream in, ObjectOutputStream out) {
            this.in = in;
            this.out = out;
            this.ip = ip;
            this.clientName = cn;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        private void reply() throws IOException {
            out.writeObject(new Data("Packet received: packet count: "+count, "Server"));
            out.flush();
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    count++;
                    Data d = (Data) in.readObject();
                    log(clientName+" requests packet: sending packet count request: "+count);
                    reply();
                } catch (IOException | ClassNotFoundException ex) {
                    log(clientName + " : data receiving failed");
                    running = false;
                    break;
                }
            }
        }
    }
}
