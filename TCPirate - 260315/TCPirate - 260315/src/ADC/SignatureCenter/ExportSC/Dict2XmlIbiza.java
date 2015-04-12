package ADC.SignatureCenter.ExportSC;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 25/11/2004
 * Time: 08:42:06
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.w3c.dom.*;

import ADC.Utils.*;

public class Dict2XmlIbiza {
    private static Dict2XmlIbiza ourInstance = null;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_VERBOSE = 1;
    private static final int OPTION_SILENT = 2;
    private static final int OPTION_ERROR = 3;
    private static final int OPTION_DATABASE_HOST = 4;
    private static final int OPTION_DATABASE_PORT = 5;
    private static final int OPTION_DATABASE_USER = 6;
    private static final int OPTION_DATABASE_PASSWORD = 7;
    private static final int OPTION_DICTIONARY = 8;
    private static final int OPTION_FILE = 9;
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
            new CommandLineOptionDef("d", OPTION_DICTIONARY),
            new CommandLineOptionDef("of", OPTION_FILE),
            new CommandLineOptionDef("Dn", OPTION_DATABASE_NAME)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final String QUERY_DICTIONARY_DATA = "select * from tblDictionaries where min_ver >= 350 order by dict_id";

    private static final String QUERY_DICTIONARY_DATA_FOR_XML =
            "select dict_id Id, dict_name Name, dict_description Description, dict_datecreated Created, dict_dateupdated Updated, case dict_isdefault when 0 then 'false' when 1 then 'true' end isDefaultEnable, case dict_isdeleted when 0 then 'false' when 1 then 'true' end isDeleted, dict_defaultsevirity defaultSeverity, case defaultimmidiateblock when 0 then 'false' when 1 then 'true' end isDefaultImmediateBlock, defaultFollowedActionPolicy defaultFollowedActionPolicy from tblDictionaries Dictionary where dict_id = ? for xml auto,elements";


    private static final String QUERY_PE_TYPES =
            "select dbo.xlateNewPeTypes(?)";
    
    private static final String QUERY_FOR_XML_EXPLICIT = " for xml explicit";

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;
    private String m_input_file = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private String m_result_file = null;

    public static Dict2XmlIbiza getInstance() {
        if (ourInstance == null)
            ourInstance = new Dict2XmlIbiza();

        return ourInstance;
    }

    private Dict2XmlIbiza() {
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
                case OPTION_FILE:
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

        if (m_result_file == null)
            if (m_input_file != null)
                m_result_file = m_input_file;
            else
                m_result_file = "dict.xml";

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

    // This method uses FOR XML queries to extract XML formatted text from the database
    // The retrieved text is parsed into DOM and appended to the XML filter document
    private void prepareFilterTag(Element filter_xml, int p_dictionary, String p_statement) {
        try {
            XmlUtils xml = XmlUtils.getInstance();

            PreparedStatement ps = DBHelper.getInstance().prepare(p_statement + QUERY_FOR_XML_EXPLICIT);
            ps.setInt(1, p_dictionary);
            ps.setInt(2, p_dictionary);

            ResultSet rs1 = ps.executeQuery();
            rs1.next();
            String tmp = rs1.getString(1);
            rs1.close();
            ps.close();

            Element el = xml.stringToDOM(tmp);
            el = (Element)filter_xml.getOwnerDocument().importNode(el, true);
            filter_xml.appendChild(el);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create criteria");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    private void addLocationToFilter(Element xml_filter, int location_enabled, int location) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("<locations>");
        sb.append("</locations>");

        Element el = XmlUtils.getInstance().stringToDOM(sb.toString());
        if (location_enabled == 0)
            el.setAttribute("use", "0");
        else
            el.setAttribute("use", "1");

        Document doc = xml_filter.getOwnerDocument();
        el = (Element)doc.importNode(el, true);
        xml_filter.appendChild(el);
    };

    // Create the filter element of a given dictionary
    private void buildDictionaryFilter(Element xml_dict, String str_filter) {
        XmlUtils xml = XmlUtils.getInstance();

        Element xml_filter = xml.stringToDOM(str_filter);
        Element el = (Element)xml_dict.getOwnerDocument().importNode(xml_filter, true);
        xml_dict.appendChild(el);
    }

    private void addPeTypes(Element dict_xml, String pes) {
        String pe_string = null;

        PreparedStatement ps = DBHelper.getInstance().prepare(QUERY_PE_TYPES);

        try {
            ps.setString(1,pes);
            ResultSet rs = ps.executeQuery();
            rs.next();
            pe_string = rs.getString(1);
            rs.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": failed to create PE type element");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Element el = XmlUtils.getInstance().stringToDOM("<peTypes>" + pe_string + "</peTypes>");
        Document doc = dict_xml.getOwnerDocument();
        el = (Element)doc.importNode(el, true);
        dict_xml.appendChild(el);
    }

    private Element buildDictionaryXML(PreparedStatement xml_query_ps, ResultSet rs_dictionary) {
        XmlUtils xml = XmlUtils.getInstance();

        try {
            // Retrieve dictionary id
            int dict_id = rs_dictionary.getInt("dict_id");

            // Build initial XML document
            xml_query_ps.setInt(1, dict_id);
            ResultSet rs = xml_query_ps.executeQuery();
            String tmp = "";
            while (rs.next())
                tmp = tmp + rs.getString(1);

            Element dict_xml = xml.stringToDOM(tmp);

            // Add pe types
            addPeTypes(dict_xml, rs_dictionary.getString("dict_petypes"));

            // Create filter
            buildDictionaryFilter(dict_xml, rs_dictionary.getString("dict_xmlFilter"));

            rs.close();

            return dict_xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readDictionary() {
        DBHelper dbh = DBHelper.getInstance();
        XmlUtils xmlu = XmlUtils.getInstance();

        Element dictionaries = xmlu.newDocument("Dictionaries").getDocumentElement();

        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);

        ResultSet rs_dictionary = dbh.directSelect(QUERY_DICTIONARY_DATA);

        PreparedStatement ps = dbh.prepare(QUERY_DICTIONARY_DATA_FOR_XML);

        try {
            while (rs_dictionary.next()) {
                Element el = buildDictionaryXML(ps, rs_dictionary);
                el = (Element)dictionaries.getOwnerDocument().importNode(el, true);
                dictionaries.appendChild(el);
            }

            rs_dictionary.close();
            ps.close();

        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create dictionaries");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        xmlu.writeXmlFile(dictionaries, m_result_file);

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println("Result is: " + xmlu.toXmlString(dictionaries));
    }

    public static void exportDictionary(String[] argv) {
        Dict2XmlIbiza obj = new Dict2XmlIbiza();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            System.exit(-1);
        }

        obj.processCommandLineArguments(cla);
        obj.prepareOutput();
        obj.prepareError();

        obj.readDictionary();
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
        System.out.println("-of <filename>\t\tOutput file name (default to input)");
    }

    public static void main(String[] argv) {
        Dict2XmlIbiza obj = getInstance();

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

        obj.readDictionary();
    }
}
