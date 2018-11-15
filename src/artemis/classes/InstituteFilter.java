/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jefferson
 */
public class InstituteFilter {

    public static DefaultTableModel filterModel(DefaultTableModel m, String column, String filter) {
        DefaultTableModel model = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        //set columns
        int index = 0;
        for (int j = 0; j < m.getColumnCount(); j++) {
            model.addColumn(m.getColumnName(j));
            if (m.getColumnName(j).trim().replaceAll(" ", "_").toLowerCase().equals(column)) {
                index = j;
            }
        }
        //identify the index of filter column
        for (int row = 0; row < m.getRowCount(); row++) {
            String val[] = new String[m.getColumnCount()];
            if (m.getValueAt(row, index).toString().equals(filter)) {
                for (int col = 0; col < m.getColumnCount(); col++) {
                    val[col] = m.getValueAt(row, col).toString();
                }
                model.addRow(val);
            }
        }
        return model;
    }
}
