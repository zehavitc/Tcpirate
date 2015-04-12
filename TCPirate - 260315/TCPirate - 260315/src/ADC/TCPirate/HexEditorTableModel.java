package ADC.TCPirate;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 03/08/2005
 * Time: 10:09:57
 * To change this template use File | Settings | File Templates.
 */
public class HexEditorTableModel extends AbstractTableModel {
    private byte[] m_buffer;
    private int m_size;
    private int m_columns = 16;
    private int m_rows;
    private boolean data_changed;

    public int getRowCount() {
        return m_rows;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getColumnCount() {
        return 2 * m_columns + 2;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= m_rows)
            return null;

        // Label column
        if (columnIndex == 0) {
            String tmp = Integer.toHexString(rowIndex * m_columns);
            while (tmp.length() < 4)
                tmp = "0" + tmp;
            return tmp;
        }


        if ((columnIndex > 0) && (columnIndex <= m_columns)) {
            // Hexadecimal Represenation
            if (rowIndex * m_columns + columnIndex > m_size)
                return " ";

            String tmp = Integer.toHexString(m_buffer[rowIndex * m_columns + columnIndex - 1] & 0xff);
            if (tmp.length() ==1)
                return "0" + tmp;
            else
                return tmp;
        } else if (columnIndex == m_columns + 1)
            // Separator column
            return "||";
        else if ((columnIndex >= m_columns + 1) && (columnIndex < 2 * m_columns + 2)) {
            // String representation
            if (rowIndex * m_columns + columnIndex - m_columns - 2 >= m_size)
                return " ";

            byte b = m_buffer[rowIndex * m_columns + columnIndex - m_columns - 2];
            String tmp = "";
            if ((b > 31) && (b < 127))
                return tmp + (char)b;
            else
                return ".";
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isCellEditable(int row, int col) {
        if ((col <= 0) || (col == m_columns + 1))
            return false;
        else
            return true;
    }

    private synchronized void doSetValueAt(Object value, int row, int col) {
        if (col <= 0)
            return;

        if (col <= m_columns)
            m_buffer[row * m_columns + col - 1] = (byte)(Integer.parseInt((String)value, 16) & 0xff);
        else {
            if ((value != null) && ((String)value).length() > 0) {
                char ch = ((String)value).charAt(0);
                m_buffer[row * m_columns + col - m_columns - 2] = (byte)(ch & 0x00ff);
            } else
                m_buffer[row * m_columns + col - m_columns - 2] = 0;
        }

        data_changed = true;
    }

    public void setValueAt(Object value, int row, int col) {

        doSetValueAt(value, row, col);

        if (col <= m_columns)
            fireTableCellUpdated(row, col + m_columns + 2);
        else
            fireTableCellUpdated(row, col - m_columns - 2);

    }

    public String getColumnName(int col) {
        return ((col > 0) && (col <= m_columns)) ? Integer.toHexString(col - 1) : "";
    }

    // Routines for loading new data into the buffer.
    public synchronized void loadBuffer(byte[] p_data, int p_len) {
        // Prepare a buffer
        if ((m_buffer == null) || (m_buffer.length < p_len))
            m_buffer = new byte[p_len];

        // Copy data
        for (int index = 0; index < p_len; index++)
            m_buffer[index] = p_data[index];

        m_size = p_len;

        // Calculate size
        if (m_size % m_columns == 0)
            m_rows = m_size / m_columns;
        else
            m_rows = m_size / m_columns + 1;

        data_changed = false;
    }

    public void loadBuffer(File f) {
        byte[] buffer = new byte[30000];
        int data_size;
        int last_pos = 0;

        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));

            data_size = is.read(buffer, 0, buffer.length);

            is.close();

            loadBuffer(buffer,data_size);

            data_changed = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            data_changed = false;
        }
    }

    public void loadBuffer(byte[] p_data) {
        loadBuffer(p_data, p_data.length);
    }

    // A routine for overwritting data in the buffer
    public synchronized void copyToBuffer(byte[] p_data, int dst_offset, int src_offset, int len) {
        if (dst_offset + len > m_size)
            throw new RuntimeException("Copy operation failed");

        System.arraycopy(p_data, src_offset, m_buffer, dst_offset, len);

        data_changed = true;
    }

    public void copyToBuffer(byte[] p_data, int dst_offset) {
        copyToBuffer(p_data, dst_offset, 0, p_data.length);
    }

    public synchronized int getDataSize() {
        return m_size;
    }

    public synchronized byte[] getData(int offset, int len) {
        if (offset + len > m_size)
            throw new RuntimeException("Cannot retrieve data - incorrect request");

        byte[] data = new byte[len];

        System.arraycopy(m_buffer, offset, data, 0, len);
        
        return data;
    }

    public byte[] getData() {
        return getData(0, m_size);
    }

    public synchronized void truncateBuffer(int size) {
        if (size > m_size)
            return;

        m_size = size;

        // Calculate size
        if (m_size % m_columns == 0)
            m_rows = m_size / m_columns;
        else
            m_rows = m_size / m_columns + 1;

        data_changed = true;
    }

    public synchronized boolean isChanged() {
        return data_changed;
    }

    public synchronized void resetChange() {
        data_changed = false;
    }

    public HexEditorTableModel(int initial_capacity) {
        m_buffer = new byte[initial_capacity];
        m_size = 0;
        data_changed = false;
    }


}
