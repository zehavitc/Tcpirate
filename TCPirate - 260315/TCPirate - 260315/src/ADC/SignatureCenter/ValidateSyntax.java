/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 06/03/2005
 * Time: 10:46:59
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;

import com.mprv.secsph.mng.system.signatures.*;
import ADC.Utils.*;

public class ValidateSyntax {
    private static ValidateSyntax ourInstance = null;
    private static ValidateSyntaxDlg m_dlg = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_DATABASE_NAME = 10;
    private static final int OPTION_VALIDATE_GUI = 11;
    private static final int OPTION_VALIDATE_BATCH = 12;

    private static final CommandLineOptionDef[] m_command_line_options = {
            new CommandLineOptionDef("o", OPTION_OUTPUT),
            new CommandLineOptionDef("v", OPTION_VERBOSE),
            new CommandLineOptionDef("s", OPTION_SILENT),
            new CommandLineOptionDef("e", OPTION_ERROR),
            new CommandLineOptionDef("Dh", OPTION_DATABASE_HOST),
            new CommandLineOptionDef("Dp", OPTION_DATABASE_PORT),
            new CommandLineOptionDef("Du", OPTION_DATABASE_USER),
            new CommandLineOptionDef("Dx", OPTION_DATABASE_PASSWORD),
            new CommandLineOptionDef("mg", OPTION_VALIDATE_GUI),
            new CommandLineOptionDef("mb", OPTION_VALIDATE_BATCH),
            new CommandLineOptionDef("Dn", OPTION_DATABASE_NAME)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final int VALIDATE_MODE_UNDEFINED = 0;
    private static final int VALIDATE_MODE_GUI = 1;
    private static final int VALIDATE_MODE_BATCH = 2;

    private static final String SIG_QUERY = "select sig_id, sig_signature, sig_location from tblSignatures where sig_deleted = 0 and sig_goes2product = 1 and sig_minver <= 350 and sig_maxver is null";
    private static final int LOCATION_STREAM = 1;

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private int m_validate_mode = VALIDATE_MODE_UNDEFINED;

    public static ValidateSyntax getInstance() {
        if (ourInstance == null) {
            System.load(resDir().getAbsolutePath() + File.separatorChar + "sensorutil.dll");

            ourInstance = new ValidateSyntax();
        }

        return ourInstance;
    }

    private ValidateSyntax() {
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
                case OPTION_VALIDATE_GUI:
                    if (m_validate_mode == VALIDATE_MODE_BATCH) {
                        error = true;
                        break;
                    }
                    m_validate_mode = VALIDATE_MODE_GUI;
                    break;
                case OPTION_VALIDATE_BATCH:
                    if (m_validate_mode == VALIDATE_MODE_GUI) {
                        error = true;
                        break;
                    }
                    m_validate_mode = VALIDATE_MODE_BATCH;
                    break;
            }
        }

        // Check for errors
        if (error == true)
            return -1;

        if ((m_db_host == null) && (m_validate_mode == VALIDATE_MODE_BATCH))
            return -1;

        // Apply defaults
        if (m_output_mode == OUTPUT_MODE_UNDEFINED)
            m_output_mode = OUTPUT_MODE_NORMAL;

        if (m_validate_mode == VALIDATE_MODE_UNDEFINED)
            m_validate_mode = VALIDATE_MODE_GUI;

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

    public static void onOK() {
        if (SignatureValidation.validateSignature(m_dlg.sigToValidate(), false, false)) {
            // System.out.println("Valid Stream Signature");
            m_dlg.setResult("Valid Stream Signature");
            m_dlg.clearSignature();
        }
        else if (SignatureValidation.validateSignature(m_dlg.sigToValidate(), false, true)) {
            // System.out.println("Valid Application Signature");
            m_dlg.setResult("Valid Application Signature");
            m_dlg.clearSignature();
        }
        else
            // System.out.println("Bad bad bad bad");
            m_dlg.setResult("Bad bad bad bad");

    }

    public static void onCancel() {
        if (m_dlg != null) {
            m_dlg.dispose();
            m_dlg = null;
        }
    }

    public static File cwd() {
        return new File(System.getProperty("user.dir","."));
    }

    public static File resDir() {
        String p = System.getProperty("jexepack.resdir");
        return (p!=null) ? new File(p) : cwd();
    }

    public void doValidate() {
        if (m_validate_mode == VALIDATE_MODE_GUI) {
            ValidateSyntaxDlg dialog = new ValidateSyntaxDlg();
            m_dlg = dialog;
            dialog.pack();
            dialog.show();
            System.exit(0);
        }

        int bad_counter = 0;
        DBHelper dbh = DBHelper.getInstance();
        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);
        ResultSet rs = dbh.directSelect(SIG_QUERY);

        try {
            while (rs.next()) {
                String sig = rs.getString("sig_signature");
                boolean is_applayer;
                if ((rs.getInt("sig_location") & LOCATION_STREAM) != 0)
                    is_applayer = false;
                else
                    is_applayer = true;

                if (!SignatureValidation.validateSignature(sig, false, is_applayer)) {
                    bad_counter++;
                    System.out.print(rs.getLong("sig_id") + " - \t");
                    if (is_applayer)
                        System.out.print("Bad applayer signature - \t");
                    else
                        System.out.print("Bad stream signature - \t");
                    System.out.println(sig);
                }
            }

            rs.close();
        } catch (Exception e) {
            System.err.println("Failed to process a signature");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (bad_counter == 0) {
            System.out.println("All signatures are valid");
            System.exit(0);
        } else {
            System.out.println("Total bad signatures: " + bad_counter);
            System.exit(-1);
        }
    }

    public boolean isSignatureValid(String p_pattern, boolean isCaseSensitive, boolean isApplayer) {
        return SignatureValidation.validateSignature(p_pattern, isCaseSensitive, isApplayer);        
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
        System.out.println("-mg \t\tGUI mode (default)");
        System.out.println("-mb \t\tBatch mode");
    }

    public static void main(String[] argv) {

        ValidateSyntax obj = ValidateSyntax.getInstance();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            ValidateSyntax.printUsage();
            System.exit(-1);
        }

        if (obj.processCommandLineArguments(cla) < 0) {
            ValidateSyntax.printUsage();
            System.exit(-1);
        }

        obj.prepareOutput();
        obj.prepareError();

        obj.doValidate();
    }
}
