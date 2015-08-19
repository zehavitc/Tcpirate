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
import java.util.*;

public class AutomaticProcessingTableModel extends AbstractTableModel {

    public static String filterColumn = "Filter";
    public static String filterFunctionColumn = "Filter function";
    public static String offsetColumn = "Offset";
    public static String lengthColumn = "Length";
    public static String baseStringColumn = "Base string";
    public static String actionColumn = "Action";
    public static String variableColumn = "Variable";
    public static String functionColumn = "Function";
    public static String inputColumn = "Input";

    public static LinkedHashMap<String,Integer> columns;

    static {
        columns = new LinkedHashMap<>();
        columns.put(filterColumn,0);
        columns.put(filterFunctionColumn,1);
        columns.put(offsetColumn,2);
        columns.put(baseStringColumn,3);
        columns.put(lengthColumn,4);
        columns.put(actionColumn,5);
        columns.put(variableColumn,6);
        columns.put(functionColumn,7);
        columns.put(inputColumn,8);
    }


    private Vector <AutomaticProcessingRow> rowData;

    public static Integer[] columnsToHideOnRead = new Integer[]{columns.get(functionColumn),columns.get(inputColumn)};
    @Override
    public int getRowCount() {
        return rowData.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < rowData.size()){
            AutomaticProcessingRow row = rowData.get(rowIndex);
            if (columnIndex < columns.size()){
                String cell = row.get(columnIndex);
                return cell;
            }
        }
        return "";
    }

    public AutomaticProcessingRow getRow(int row){
        if (row >= rowData.size()) return null;
        return rowData.get(row);
    }



    @Override
    public void setValueAt(Object value, int row, int col) {

        rowData.get(row).set(col, (String) value);
        fireTableCellUpdated(row, col);
        if (col == columns.get(actionColumn)){
            for (int i=0; i< columnsToHideOnRead.length; i++){
                fireTableCellUpdated(row,columnsToHideOnRead[i]);
            }
        }
    }
    @Override
    public String getColumnName(int col) {
        for (Map.Entry<String, Integer> entry : columns.entrySet()) {
            String columnName = entry.getKey();
            Integer value = entry.getValue();
            if (value == col) return columnName;
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (row >= rowData.size() || col >= columns.size()) return false;
        if (Arrays.asList(columnsToHideOnRead).contains(col) && rowData.get(row).get(columns.get(actionColumn)).equals(AutomaticProcessingRow.Actions.Read.toString())){
            return false;
        }
        return true;
    }

    public void addNewRow(){
        rowData.add(new AutomaticProcessingRow());
        fireTableRowsInserted(0, rowData.size() - 1);
    }

    public void removeRow(int row){
        if (row > rowData.size()) return;
        rowData.remove(row);
        fireTableRowsDeleted(0, rowData.size() - 1);
    }

    public AutomaticProcessingTableModel(){
        rowData = new Vector <AutomaticProcessingRow>();
        rowData.add(new AutomaticProcessingRow());
    }

}
