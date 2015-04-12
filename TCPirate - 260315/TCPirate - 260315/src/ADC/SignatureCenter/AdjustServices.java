/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 28/11/2004
 * Time: 13:34:50
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter;

import ADC.Utils.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import org.w3c.dom.*;

public class AdjustServices {
    private static AdjustServices ourInstance = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_INPUT_FILE = 8;
    private static final int OPTION_RESULT_FILE = 9;
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
            new CommandLineOptionDef("if", OPTION_INPUT_FILE),
            new CommandLineOptionDef("Dn", OPTION_DATABASE_NAME),
            new CommandLineOptionDef("rf", OPTION_RESULT_FILE)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final String QUERY_SERVICES_XML =
            "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tid as [service!1!id],\n" +
            "\tname as [service!1!name],\n" +
            "\tcase protocolID when 1 then 'tcp' when 2 then 'udp' end as [service!1!protocol],\n" +
            "\tisencrypted as [service!1!is-encrypted],\n" +
            "\tisdynamic as [service!1!is-dynamic],\n" +
            "\tisprofiled as [service!1!is-profiled],\n" +
            "\tconnection_timeout as [service!1!connection-timeout],\n" +
            "\t'false' as [service!1!is-open-mode],\n" +
            "\t0 as [service!1!open-mode-timeout],\n" +
            "\tnull as [ports!2!!element],\n" +
            "\tnull as [port!3!!element],\n" +
            "\tnull as [port-range!4!!element],\n" +
            "\tnull as [port-range!4!from],\n" +
            "\tnull as [port-range!4!to]\n" +
            "from tblService\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\tid,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull\n" +
            "from tblService\n" +
            "union all\n" +
            "select 3 as tag,\n" +
            "\t2 as parent,\n" +
            "\ttblService.id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tport,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull\n" +
            "from tblService, tblServicePorts where tblService.id = service_id and port is not null\n" +
            "union all\n" +
            "select 4 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblService.id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tport_range_from,\n" +
            "\tport_range_to\n" +
            "from tblService, tblServicePorts where tblService.id = service_id and port_range_from is not null\t\n" +
            "order by 3, 1\n" +
            "for xml explicit";

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private String m_result_file = null;
    private String m_input_file = null;

    public static AdjustServices getInstance() {
        if (ourInstance == null)
            ourInstance = new AdjustServices();

        return ourInstance;
    }

    private AdjustServices() {
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
                case OPTION_INPUT_FILE:
                    if ((m_input_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_input_file = cla.getValue(0);
                    break;
                case OPTION_RESULT_FILE:
                    if ((m_result_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_result_file = cla.getValue(0);
                    break;
            }
        }

        // Check for errors
        if (error == true)
            return -1;

        if ((m_input_file == null) || (m_db_host == null))
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

        if (m_result_file == null)
            m_result_file = m_input_file;

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

    private Element createServicesTag() {
        DBHelper dbh = DBHelper.getInstance();
        XmlUtils xmlu = XmlUtils.getInstance();


        // Collect all services from database
        try {
            dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);

            ResultSet rs = dbh.directSelect(QUERY_SERVICES_XML);
            StringBuffer sb = new StringBuffer(100000);
            sb.append("<services>");

            while (rs.next())
                sb.append(rs.getString(1));

            rs.close();

            sb.append("</services>");

            Element el = xmlu.stringToDOM(sb.toString());

            return el;
        } catch (Exception ex) {
            System.err.println(this.getClass().getName() + ": Failed to create service tag");
            ex.printStackTrace(System.err);
            throw new RuntimeException(ex);
        }
    }

    private void convertServices() {
        Element new_services = createServicesTag();

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println(XmlUtils.getInstance().toXmlString(new_services));
        // Read original file
        Element root = XmlUtils.getInstance().readXmlFIle(m_input_file);

        // Find the "services" tag
        Document doc = root.getOwnerDocument();
        NodeList nodes = root.getElementsByTagName("services");
        if ((nodes == null) || (nodes.getLength() == 0)) {
            System.err.println("Error: Source file does not contain any services");
            return;
        }

        // Traverese all service and change their ID
        new_services = (Element)doc.importNode(new_services, true);

        Element services = (Element)nodes.item(0);

        doc.getDocumentElement().replaceChild(new_services, services);

        // Write output file
        XmlUtils.getInstance().writeXmlFile(root, m_result_file);
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
        System.out.println("-if <filename>\t\tInput file name");
        System.out.println("-rf <filename>\t\tOutput file name (default to input)");
    }

    public static void main(String[] argv) {
        AdjustServices obj = getInstance();

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

        obj.convertServices();
    }
}
