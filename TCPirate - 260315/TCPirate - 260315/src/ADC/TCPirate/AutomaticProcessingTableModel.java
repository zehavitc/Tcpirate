package ADC.TCPirate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;


import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Vector;

public class AutomaticProcessingTableModel extends AbstractTableModel {

    public static String[] columnNames = new String[]{
            "Filter",
            "Filter Function",
            "Offset",
            "Length",
            "Base string",
            "Action",
            "Variable",
            "Function",
            "Input"
    };

    private Vector <AutomaticProcessingRow> rowData;
    public static int actionColumn = 5;
    public static int functionColumn = 7;
    public static int functionInputColumn = 8;
    public static Integer[] columnsToHideOnRead = new Integer[]{functionColumn,functionInputColumn};
    @Override
    public int getRowCount() {
        return rowData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < rowData.size()){
            AutomaticProcessingRow row = rowData.get(rowIndex);
            if (columnIndex < columnNames.length){
                String cell = row.get(columnIndex);
                return cell;
            }
        }
        return "";
    }



    @Override
    public void setValueAt(Object value, int row, int col) {

        rowData.get(row).set(col, (String) value);
        fireTableCellUpdated(row, col);
        if (col == actionColumn){
            for (int i=0; i< columnsToHideOnRead.length; i++){
                fireTableCellUpdated(row,columnsToHideOnRead[i]);
            }
        }
    }
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (row >= rowData.size() || col >= columnNames.length) return false;
        if (Arrays.asList(columnsToHideOnRead).contains(col) && rowData.get(row).get(actionColumn).equals(AutomaticProcessingRow.Actions.Read.toString())){
            return false;
        }
        return true;
    }

    public void addNewRow(){
        rowData.add(new AutomaticProcessingRow());
        fireTableRowsInserted(0,rowData.size()-1);
    }

    public void removeRow(int row){
        if (row >= rowData.size()) return;
        rowData.remove(row);
        fireTableRowsDeleted(0,rowData.size()-1);
    }

    public AutomaticProcessingTableModel(){
        rowData = new Vector <AutomaticProcessingRow>();
        rowData.add(new AutomaticProcessingRow());
    }
}
