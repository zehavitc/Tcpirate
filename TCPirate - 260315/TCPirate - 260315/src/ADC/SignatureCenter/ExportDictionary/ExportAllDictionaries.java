/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 29/11/2004
 * Time: 14:30:39
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter.ExportDictionary;

import ADC.Utils.*;

import java.io.*;
import java.sql.ResultSet;

public class ExportAllDictionaries {
    private static ExportAllDictionaries ourInstance = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_OUTPUT_DIR = 8;
    private static final int OPTION_DATABASE_NAME = 10;

    private static final CommandLineOptionDef[] m_command_line_options = {
            new CommandLineOptionDef("o", OPTION_OUTPUT),
            new CommandLineOptionDef("v", OPTION_VERBOSE),
            new CommandLineOptionDef("s", OPTION_SILENT),
            new CommandLineOptionDef("e", OPTION_ERROR),
            new CommandLineOptionDef("Dh", OPTION_DATABASE_HOST),
            new CommandLineOptionDef("Dp", OPTION_DATABASE_PORT),
            new CommandLineOptionDef("Du", OPTION_DATABASE_USER),
            new CommandLineOptionDef("Dx", OPTION_DATABASE_PASSWORD),
            new CommandLineOptionDef("Dn", OPTION_DATABASE_NAME),
            new CommandLineOptionDef("Od", OPTION_OUTPUT_DIR)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final String QUERY_ALL_DICTIONARIES = "select dict_name, dict_is_applayer from tblDictionaries where min_ver in (300,330) and (max_ver < 350 or max_ver is null) and dict_isDeleted = 0";

    public static ExportAllDictionaries getInstance() {
        if (ourInstance == null)
            ourInstance = new ExportAllDictionaries();

        return ourInstance;
    }

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;
    private String m_output_dir = null;

    private ExportAllDictionaries() {
    }

    private int processCommandLineArguments(CommandLineArguments cla) {
        int option;
        boolean error = false;

        while (((option = cla.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_OUTPUT:
                    if ((m_output_file != null) || (m_output_mode == OUTPUT_MODE_SILENT) ||
                            (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_output_file = cla.getValue(0);
                    break;
                // Set output mode to silent
                case OPTION_SILENT:
                    if ((m_output_file != null) || (m_output_mode != OUTPUT_MODE_UNDEFINED) ||
                            (cla.numOfValues() != 0)) {
                        error = true;
                        break;
                    }
                    m_output_mode = OUTPUT_MODE_SILENT;
                    break;
                // Set output mode to verbose
                case OPTION_VERBOSE:
                    if ((m_output_mode != OUTPUT_MODE_UNDEFINED) || (cla.numOfValues() != 0)) {
                        error = true;
                        break;
                    }
                    m_output_mode = OUTPUT_MODE_VERBOSE;
                    break;
                case OPTION_ERROR:
                    if ((m_error_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_error_file = cla.getValue(0);
                    break;
                case OPTION_DATABASE_HOST:
                    if ((m_db_host != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_db_host = cla.getValue(0);
                    break;
                case OPTION_DATABASE_PORT:
                    if ((m_db_port_str != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_db_port_str = cla.getValue(0);
                    break;
                case OPTION_DATABASE_USER:
                    if ((m_db_user != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_db_user = cla.getValue(0);
                    break;
                case OPTION_DATABASE_PASSWORD:
                    if ((m_db_password != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_db_password = cla.getValue(0);
                    break;
                case OPTION_DATABASE_NAME:
                    if ((m_db_name != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_db_name = cla.getValue(0);
                    break;
                case OPTION_OUTPUT_DIR:
                    if ((m_output_dir != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_output_dir = cla.getValue(0);
                    break;
            }
        }

        // Check for errors
        if (error == true)
            return -1;

        if (m_db_host == null)
            return -1;

        // Apply defaults
        if (m_output_mode == OUTPUT_MODE_UNDEFINED)
            m_output_mode = OUTPUT_MODE_NORMAL;

        if (m_db_port_str == null)
            m_db_port_str = "1433";

        if (m_db_user == null)
            m_db_user = "ic";

        if (m_db_password == null)
            m_db_password = "ic";

        if (m_db_name == null)
            m_db_name = "ic";

        if (m_output_dir == null)
            m_output_dir = ".";

        return 0;
    }

    // Open output stream according to command line arguments (or default)
    private int prepareOutput() {
        if (m_output_mode == OUTPUT_MODE_SILENT)
            return 0;

        if (m_output_file == null) {
            return 0;
        }

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(m_output_file), true);
            System.setOut(ps);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot open output path");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        return 0;
    }

    private int prepareError() {
        if (m_error_file == null) {
            return 0;
        }

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(m_error_file), true);
            System.setErr(ps);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot initialize error stream");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return 0;
    }

    private void prepareOutputDir() {
        File dir = new File(m_output_dir);

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                System.err.println("Cannot create output directory - " + m_output_dir);
                throw new RuntimeException("Cannot create output directory - " + m_output_dir);
            }
        } else {
            if (!dir.mkdir()) {
                System.err.println("Cannot create output directory - " + m_output_dir);
                throw new RuntimeException("Cannot create output directory - " + m_output_dir);
            }
        }
    }

    private String replaceSpaces(String input) {
        StringBuffer sb = new StringBuffer(input.length());

        for (int index = 0; index < input.length(); index++) {
            if (input.charAt(index) == ' ')
                sb.append('-');
            else
                sb.append(input.charAt(index));
        }

        return sb.toString();
    }

    private void exportDictionaries() {
        DBHelper dbh = DBHelper.getInstance();

        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);

        ResultSet rs = dbh.directSelect(QUERY_ALL_DICTIONARIES);

        try {
            while (rs.next()) {
                String dict_name = rs.getString("dict_name");
                boolean is_applayer = rs.getBoolean("dict_is_applayer");

                if (m_output_mode != OUTPUT_MODE_SILENT)
                    System.out.println("Processing Dictionary " + dict_name);

                String[] args = null;
                if (is_applayer)
                    args = new String[] {"-v", "-Dh", "iis", "-d", dict_name, "-of", m_output_dir + File.separator + "APP_" + dict_name + ".xml"};
                else
                    args = new String[] {"-v", "-Dh", "iis", "-d", dict_name, "-of", m_output_dir + File.separator + "NET_" + dict_name + ".xml", "-b"};

                File f;

                ExportDictionary.exportDictionary(args);
            }

            rs.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot export dictionary");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("-o <filename>\tRedirect Standard Output");
        System.out.println("-v\t\tVerbose");
        System.out.println("-s\t\tSuppress output");
        System.out.println("-e\t\tRedirect error messages");
        System.out.println("-Dh <hostname>\t\tDatabase host name");
        System.out.println("-Dp <port number>\t\tDatabase port number (1433)");
        System.out.println("-Du <user>\t\tDatabase user name (ic)");
        System.out.println("-Dx <password>\t\tDatabase password (ic)");
        System.out.println("-Dn <dbname>\t\tDatabase name (ic)");
        System.out.println("-Od <dir>\t\tDirectory for output files (default to current)");
    }

    public static void main(String[] argv) {
        ExportAllDictionaries obj = getInstance();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            System.exit(-1);
        }

        if (obj.processCommandLineArguments(cla) < 0) {
            printUsage();
            System.exit(-1);
        }

        obj.prepareOutput();
        obj.prepareError();
        obj.prepareOutputDir();

        obj.exportDictionaries();
    }
}
