/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import artemis.classes.Subject;
import artemis.classes.Teacher;
import com.alee.extended.window.WebPopOver;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;

/**
 *
 * @author Jefferson
 */
public class SuggestList extends javax.swing.JPanel {

    /**
     * Creates new form SuggestList
     */
    List<Teacher> teachers;
    ;
    List<String> teach_id;
    List<Subject> subjects;
    String[] values = new String[]{"", ""};
    WebPopOver view;
    //for type property
    //0 for subject
    //1 for teachers
    int type;

    public SuggestList(WebPopOver view, List list, Class type) {
        this.view = view;
        initComponents();
        if (type == Subject.class) {
            this.type = 0;
            this.subjects = list;
            txtSearch.setInputPrompt("Search subject's description here..");
        } else {
            this.type = 1;
            this.teachers = list;
        }
        searchList("");
    }

    public static String[] showDialog(Component com, List list, Class type) {
        WebPopOver view = new WebPopOver(com);
        view.setMargin(0);
        view.setCloseOnFocusLoss(true);
        view.setModal(true);
        SuggestList l = new SuggestList(view, list, type);
        view.add(l);
        view.show(com);
        return l.getValues();
    }

    public String[] getValues() {
        return this.values;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        txtSearch = new com.alee.laf.text.WebTextField();
        webButton1 = new com.alee.laf.button.WebButton();

        setBackground(new java.awt.Color(255, 255, 255));

        list.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        list.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        list.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(list);

        txtSearch.setDrawBorder(false);
        txtSearch.setDrawFocus(false);
        txtSearch.setDrawShade(false);
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch.setHideInputPromptOnFocus(false);
        txtSearch.setInputPrompt("Search teacher's name here..");
        txtSearch.setRound(0);
        txtSearch.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtSearchCaretUpdate(evt);
            }
        });

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/artemis/img/cross2.png"))); // NOI18N
        webButton1.setUndecorated(true);
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtSearchCaretUpdate
        searchList(txtSearch.getText().trim().toLowerCase());
    }//GEN-LAST:event_txtSearchCaretUpdate

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked
        if (list.getSelectedIndex() != -1) {
            if (!evt.isMetaDown() && evt.getClickCount() == 2) {
                if (type == 0) {
                    this.values = new String[]{list.getSelectedValue().toString(), ""};
                } else {
                    this.values = new String[]{teach_id.get(list.getSelectedIndex()), list.getSelectedValue().toString()};
                }
                view.dispose();
            }
        }
    }//GEN-LAST:event_listMouseClicked

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        view.dispose();
    }//GEN-LAST:event_webButton1ActionPerformed

    private void searchList(String filter) {
        DefaultListModel model = new DefaultListModel();
        teach_id = new ArrayList<>();
        if (type == 0) {
            //subject search
            subjects.stream().filter(subj -> subj.getDescription().toLowerCase().
                    contains(filter) | subj.getId().toLowerCase().contains(filter)).forEach(subj -> model.addElement(subj.getId()));
        } else {
            //teacher search
            teachers.stream().filter(teach -> teach.getName().toLowerCase().contains(filter)).
                    forEach(teach -> model.addElement(teach.getName() + " (" + teach.getId() + ")"));
            teach_id = teachers.stream().filter(teach -> teach.getName().
                    toLowerCase().contains(filter)).map(t -> t.getId()).collect(Collectors.toList());
        }
        list.setModel(model);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    private com.alee.laf.text.WebTextField txtSearch;
    private com.alee.laf.button.WebButton webButton1;
    // End of variables declaration//GEN-END:variables
}
