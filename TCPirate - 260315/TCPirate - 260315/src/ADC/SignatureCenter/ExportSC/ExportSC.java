/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 20/12/2004
 * Time: 14:24:26
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter.ExportSC;

import java.io.*;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import ADC.Utils.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ExportSC {
    private static ExportSC ourInstance = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_DATABASE_NAME = 8;
    private static final int OPTION_PROPERTY = 9;
    private static final int OPTION_EXPORT_CLASS = 10;
    private static final int OPTION_QUERY = 11;
    private static final int OPTION_INPUT_FILE = 12;
    private static final int OPTION_MODE_PATCH = 13;

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
            new CommandLineOptionDef("p", OPTION_PROPERTY),
            new CommandLineOptionDef("if", OPTION_INPUT_FILE),
            new CommandLineOptionDef("q", OPTION_QUERY),
            new CommandLineOptionDef("mp", OPTION_MODE_PATCH),
            new CommandLineOptionDef("xc", OPTION_EXPORT_CLASS)};


    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final int MODE_UNDEFINED = 0;
    private static final int MODE_EXPORT = 1;
    private static final int MODE_PATCH = 2;

    private static final String PROPERTY_QUERY = "QUERY";

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;
    private String m_export_class = null;
    private int m_operation_mode = MODE_UNDEFINED;
    private String m_query = null;
    private String m_original_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private Map m_properties = new HashMap(10);

    public static ExportSC getInstance() {
        if (ourInstance == null)
            ourInstance = new ExportSC();

        return ourInstance;
    }

    private ExportSC() {
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
                case OPTION_PROPERTY:
                    if (cla.numOfValues() != 2) {
                        error = true;
                        break;
                    }
                    m_properties.put(cla.getValue(0), cla.getValue(1));
                    break;
                case OPTION_EXPORT_CLASS:
                    if ((m_export_class != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_export_class = cla.getValue(0);
                    break;
                case OPTION_QUERY:
                    if ((m_query != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_query = cla.getValue(0);
                    break;
                case OPTION_INPUT_FILE:
                    if ((m_original_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_original_file = cla.getValue(0);
                    break;
                case OPTION_MODE_PATCH:
                    if (m_operation_mode != MODE_UNDEFINED) {
                        error = true;
                        break;
                    }
                    m_operation_mode = MODE_PATCH;
                    break;
            }
        }

        // Check for errors
        if (error == true)
            return -1;

        if ((m_db_host == null) || (m_export_class == null))
            return -1;

        if ((m_operation_mode == MODE_PATCH) && ((m_original_file == null) || (m_query == null)))
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

        if (m_operation_mode == MODE_UNDEFINED)
            m_operation_mode = MODE_EXPORT;

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

    private String patchXML(String patch) {
        XmlUtils xmlu = XmlUtils.getInstance();

        Element original_xml = xmlu.readXmlFIle(m_original_file);
        NodeList nodes = original_xml.getElementsByTagName("Signatures");
        Element signatures = (Element)nodes.item(0);

        Element new_xml = xmlu.stringToDOM(patch);
        nodes = new_xml.getElementsByTagName("Signature");

        for (int index = 0; index < nodes.getLength(); index++) {
            Element el = (Element)nodes.item(index);
            el = (Element)signatures.getOwnerDocument().importNode(el, true);
            signatures.appendChild(el);
        }

        return xmlu.toXmlString(original_xml);
    }

    private void exportSignatureCenter() {
        DBHelper dbh = DBHelper.getInstance();

        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);

        dbh.exec("USE " + m_db_name);

        IExportSC exporter = null;

        try {
            Class cls = Class.forName(m_export_class);

            exporter = (IExportSC)cls.newInstance();

            Iterator it = m_properties.keySet().iterator();

            while (it.hasNext()) {
                String name = (String)it.next();
                String value = (String)m_properties.get(name);

                exporter.setProperty(name, value);
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to initialze exporter");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        if (m_operation_mode == MODE_EXPORT)
            System.out.println(exporter.export());
        else {
            exporter.setProperty(PROPERTY_QUERY, m_query);
            String new_sig = exporter.exportForPatch();
            System.out.println(patchXML(new_sig));
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
        System.out.println("-p <property name> <property value>\t\tset a property for exporter");
        System.out.println("-xc <class name>\t\tname of export class");
        System.out.println("-mp \t\tun in patch mode");
        System.out.println("-if <input file>\t\ta valid signature file (unencrypted)");
        System.out.println("-q <query>\t\tThe database query that determines the additional signatures");
    }

    public static void main(String[] argv) {
        ExportSC obj = getInstance();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            printUsage();
            System.exit(-1);
        }

        if (obj.processCommandLineArguments(cla) < 0) {
            printUsage();
            System.exit(-1);
        }

        obj.prepareOutput();
        obj.prepareError();
        obj.exportSignatureCenter();
    }

}
