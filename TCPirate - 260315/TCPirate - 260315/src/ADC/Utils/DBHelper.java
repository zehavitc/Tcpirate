/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 25/11/2004
 * Time: 11:28:30
 * To change this template use File | Settings | File Templates.
 */
package ADC.Utils;

import java.sql.*;


public class DBHelper {
    private static DBHelper ourInstance = new DBHelper();

    private static final String DRIVER_CLASS = "com.microsoft.jdbc.sqlserver.SQLServerDriver";

    private static String m_connection_string = null;
    private static Connection m_connection = null;

    public static DBHelper getInstance() {
        return ourInstance;
    }

    private DBHelper() {
        try {
            java.sql.Driver driver = (java.sql.Driver)Class.forName(DRIVER_CLASS).newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            System.err.println(DBHelper.class.getName() + ": Cannot initialzie JDBC drive");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    public ResultSet directSelect(String query) {
        PreparedStatement ps = null;
        try {
            ps = m_connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot prepare query");
            System.err.println("Query is: " + query);
            throw new RuntimeException(e);
        }

        try {
            ResultSet rs = ps.executeQuery();
            return rs;
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to execute query");
            System.err.println("Query is: " + query);
            throw new RuntimeException(e);
        }
    }

    public void exec(String query) {
        PreparedStatement ps = null;
        try {
            ps = m_connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot prepare query");
            System.err.println("Query is: " + query);
            throw new RuntimeException(e);
        }

        try {
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to execute query");
            System.err.println("Query is: " + query);
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement prepare(String query) {
        PreparedStatement ps = null;
        try {
            ps = m_connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot prepare query");
            System.err.println("Query is: " + query);
            throw new RuntimeException(e);
        }

        return ps;
    }

    public Connection getDedicatedConnection() {
        if (m_connection != null) {
            try {
                Connection conn = DriverManager.getConnection(m_connection_string);
                return conn;
            } catch (Exception e) {
                System.err.println(this.getClass().getName() + ": Cannot open database connection drive");
                e.printStackTrace(System.err);
                return null;
            }
        }

        return null;
    }

    public int connect(String p_host, String p_port, String p_db_name, String p_user, String p_pwd) {
        return connect(p_host, p_port, p_db_name, p_user, p_pwd, false);
    }

    public int connect(String p_host, String p_port, String p_db_name, String p_user, String p_pwd, boolean cursorMode) {
        try {
            if ((m_connection != null) && !m_connection.isClosed())
                return 0;
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot open database connection drive");
            e.printStackTrace(System.err);
            return -1;
        }

        String cursor_mode = null;
        if (cursorMode)
            cursor_mode = "SelectMethod=Cursor";
        else
            cursor_mode = "SelectMethod=Direct";

        m_connection_string = "jdbc:microsoft:sqlserver://" + p_host + ":" + p_port +
                ";DatabaseName=" + p_db_name + ";" + cursor_mode + ";User=" + p_user + ";Password=" + p_pwd;

        try {
            m_connection = DriverManager.getConnection(m_connection_string);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot open database connection drive");
            e.printStackTrace(System.err);
            return -1;
        }

        return 0;

    }

}
