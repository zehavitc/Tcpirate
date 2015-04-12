/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/03/2005
 * Time: 05:20:12
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter.GUI;

import ADC.Utils.DBHelper;
import ADC.SignatureCenter.SSVersion;
import ADC.Utils.CommandLineOptionDef;
import ADC.Utils.CommandLineArguments;

import javax.swing.*;
import java.awt.*;

public class SignatureEntry {
    private static SignatureEntry ourInstance = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_DATABASE_NAME = 10;
    private static final int OPTION_MODE_ARUBA = 11;
    private static final int OPTION_MODE_IBIZA_FP = 12;

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
            new CommandLineOptionDef("fp", OPTION_MODE_IBIZA_FP),
            new CommandLineOptionDef("ar", OPTION_MODE_ARUBA)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;
    private boolean m_mode_aruba = false;
    private boolean m_mode_ibiza_fp = false;

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
                case OPTION_MODE_ARUBA:
                    if (m_mode_ibiza_fp) {
                        error = true;
                        break;
                    }
                    m_mode_aruba = true;
                    break;
                case OPTION_MODE_IBIZA_FP:
                    if (m_mode_aruba) {
                        error = true;
                        break;
                    }
                    m_mode_ibiza_fp = true;
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

        return 0;
    }

    public static SignatureEntry getInstance() {
        if (ourInstance == null)
            ourInstance = new SignatureEntry();

        return ourInstance;
    }

    private SignatureEntry() {
    }

    public void addSignatures() {
        DBHelper dbh = DBHelper.getInstance();
        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password, true);

        JFrame.setDefaultLookAndFeelDecorated(true);

//        Dialog dlg = new NewSignatureDlg();
        int version = SSVersion.IBIZA;
        if (m_mode_aruba)
            version = SSVersion.ANCIENT;
        else if (m_mode_ibiza_fp)
            version = SSVersion.IBIZA_FP;

        Frame dlg = new NewSignatureDlg(version);

        dlg.pack();
        dlg.setVisible(true);
//        dlg.show();
    }

    public static void main(String[] argv) {
        SignatureEntry obj = getInstance();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            System.exit(-1);
        }

        if (obj.processCommandLineArguments(cla) < 0) {
            System.exit(-1);
        }


        obj.addSignatures();

        // System.exit(0);
    }

}
