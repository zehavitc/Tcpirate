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

public class Dict2XmlAruba {
    private static Dict2XmlAruba ourInstance = null;

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

    private static final String QUERY_DICTIONARY_DATA = "select * from tblDictionaries where min_ver in (300,330) and max_ver < 350 order by dict_id";

    private static final String QUERY_DICTIONARY_DATA_FOR_XML =
            "select dict_id Id, dict_name Name, dict_description Description, dict_datecreated Created, dict_dateupdated Updated, case dict_isdefault when 0 then 'false' when 1 then 'true' end isDefault, case dict_isdeleted when 0 then 'false' when 1 then 'true' end isDeleted, dbo.xlatePeTypes(dict_petypes) peTypes from tblDictionaries Dictionary where dict_id = ? for xml auto,elements";

    private static final String QUERY_DICTIONARY_CLASS =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [classes!1!Id!hide],\n" +
            "\tdict_class_enabled as [classes!1!use],\n" +
            "\tnull as [class!2!!element]\n" +
            "from tblDictionaries where dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictClasses.dict_id,\n" +
            "\t'',\n" +
            "\tdict_classe_id\n" +
            "from tblDictionaries, tblDictClasses where tblDictionaries.dict_id = tblDictClasses.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_SERVICE = "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [services!1!Id!hide],\n" +
            "\tdict_service_enabled as [services!1!use],\n" +
            "\tnull as [service!2!!element]\n" +
            "from tblDictionaries where dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictServices.dict_id,\n" +
            "\t'',\n" +
            "\tdict_service\n" +
            "from tblDictionaries, tblDictServices where tblDictionaries.dict_id = tblDictServices.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_COMPLEXITY =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [complexity-levels!1!Id!hide],\n" +
            "\tdict_complexity_enabled as [complexity-levels!1!use],\n" +
            "\tnull as [complexity!2!!element]\n" +
            "from tblDictionaries where dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictComplexity.dict_id,\n" +
            "\t'',\n" +
            "\tdict_complexity\n" +
            "from tblDictionaries, tblDictComplexity where tblDictionaries.dict_id = tblDictComplexity.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_RISK =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [risk-levels!1!Id!hide],\n" +
            "\tdict_risk_enabled as [risk-levels!1!use],\n" +
            "\tnull as [risk!2!!element]\n" +
            "from tblDictionaries where dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictRisks.dict_id,\n" +
            "\t'',\n" +
            "\tdict_risk\n" +
            "from tblDictionaries, tblDictRisks where tblDictionaries.dict_id = tblDictRisks.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_DIRECTION =
            "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [is_client2server!1!Id!hide],\n" +
            "\tdict_direction_enabled as [is_client2server!1!use],\n" +
            "\tdict_direction_isclient2server as [is_client2server!1!is-client2server!element],\n" +
            "\tdict_direction_isserver2client as [is_client2server!1!is-server2client!element]\n" +
            "from tblDictionaries where tblDictionaries.dict_id = ? or 999 = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_FREQUENCY =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [frequency-levels!1!Id!hide],\n" +
            "\tdict_frequency_enabled as [frequency-levels!1!use],\n" +
            "\t'' as [frequency!2!!element]\n" +
            "from tblDictionaries where tblDictionaries.dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictionaries.dict_id,\n" +
            "\tnull,\n" +
            "\tdict_frequency\n" +
            "from tblDictionaries, tblDictFrequency where tblDictionaries.dict_id = tblDictFrequency.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_ACCURACY =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [accuracy-levels!1!Id!hide],\n" +
            "\tdict_accuracy_enabled as [accuracy-levels!1!use],\n" +
            "\t'' as [accuracy!2!!element]\n" +
            "from tblDictionaries where tblDictionaries.dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictionaries.dict_id,\n" +
            "\tnull,\n" +
            "\tdict_accuracy\n" +
            "from tblDictionaries, tblDictAccuracy where tblDictionaries.dict_id = tblDictAccuracy.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_AFFECTED_SYSTEMS = //"select dict_affected_system_name_id from tblDictAffectedSystems where dict_id = ";
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [affected-systems!1!Id!hide],\n" +
            "\tdict_affected_systems_enabled as [affected-systems!1!use],\n" +
            "\tnull as [system!2!sysid!hide],\n" +
            "\tnull as [system!2!name!element],\n" +
            "\tnull as [system!2!version-pos!element],\n" +
            "\tnull as [system!2!version!element]\n" +
            "from tblDictionaries where tblDictionaries.dict_id = ?\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\ttblDictionaries.dict_id,\n" +
            "\tnull,\n" +
            "\tpk,\n" +
            "\tdict_affected_system_name_id,\n" +
            "\tdict_operator,\n" +
            "\trtrim(cast(dict_affected_system_version_float as char(10)))\n" +
            "from tblDictionaries, tblDictAffectedSystems where tblDictionaries.dict_id = tblDictAffectedSystems.dict_id and tblDictionaries.dict_id = ?\n" +
            "order by dict_id, tag";

    private static final String QUERY_DICTIONARY_LOCATION = 
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tdict_id as [location!1!Id!hide],\n" +
            "\tdict_location_enabled as [location!1!use],\n" +
            "\tdict_location_isuri as [location!1!is-uri!element],\n" +
            "\tdict_location_isparameter as [location!1!is-parameters!element],\n" +
            "\tdict_location_isgeneral as [location!1!is-raw!element]\n" +
            "from tblDictionaries where tblDictionaries.dict_id = ? or ? = 999\n";

    private static final String QUERY_FOR_XML_EXPLICIT = " for xml explicit";

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;
    private String m_input_file = null;
    private String m_raw_query = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private String m_dictionary_name = null;
    private String m_result_file = null;

    public static Dict2XmlAruba getInstance() {
        if (ourInstance == null)
            ourInstance = new Dict2XmlAruba();

        return ourInstance;
    }

    private Dict2XmlAruba() {
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

    private void fixXMLDates(Element el) {
        String tag_name = el.getTagName();

        if ((tag_name != null) &&
                ((tag_name.equalsIgnoreCase("Created")) ||
                    (tag_name.equalsIgnoreCase("Updated")))) {
            NodeList nodes = el.getChildNodes();
            for (int index = 0; index < nodes.getLength(); index++) {
                Node n = nodes.item(index);
                if (n.getNodeValue() != null) {
                    SimpleDateFormat sd = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
                    try {
                        java.util.Date d = sd.parse(n.getNodeValue());
                        n.setNodeValue(Long.toString(d.getTime()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            NodeList nodes = el.getChildNodes();
            for (int index = 0; index < nodes.getLength(); index++) {
                Node n = nodes.item(index);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                    fixXMLDates((Element)n);
            }
        }
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

    // Create the filter element of a given dictionary
    private String buildDictionaryFilter(int dict_id, int is_applayer) {
        XmlUtils xml = XmlUtils.getInstance();

        Element xml_filter = xml.newDocument("filter").getDocumentElement();

        try {
            // Collect all attack related criteria
            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_CLASS);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_COMPLEXITY);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_RISK);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_AFFECTED_SYSTEMS);

            // Collect all service related criteria
            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_SERVICE);

            // Collect all signature related criteria
            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_FREQUENCY);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_ACCURACY);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_DIRECTION);

            prepareFilterTag(xml_filter, dict_id, QUERY_DICTIONARY_LOCATION);

            // Add a dummy date element
            Element el = xml.appendElementWithValue(xml_filter, "last-updated", "0");
            el.setAttribute("use", "0");

            el = xml.appendElementWithValue(xml_filter, "applLayer", Integer.toString(is_applayer));
            el.setAttribute("use", "1");
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create filter");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println(xml.toXmlString(xml_filter));

        return xml.toXmlString(xml_filter);
    }

    private Element buildDictionaryXML(PreparedStatement xml_query_ps, ResultSet rs_dictionary) {
        XmlUtils xml = XmlUtils.getInstance();

        try {
            // Retrieve dictionary id
            int dict_id = rs_dictionary.getInt("dict_id");
            int is_applayer = rs_dictionary.getInt("dict_is_applayer");

            xml_query_ps.setInt(1, dict_id);
            ResultSet rs = xml_query_ps.executeQuery();
            StringBuffer sb = new StringBuffer(10000);

            while (rs.next()) {
                sb.append(rs.getString(1));
            }

            Element dict_xml = xml.stringToDOM(sb.toString());

            String tmp = buildDictionaryFilter(dict_id, is_applayer);
            if (tmp == null)
                return null;
            xml.appendElementWithValue(dict_xml, "FilterXML", tmp);

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

        fixXMLDates(dictionaries);

        xmlu.writeXmlFile(dictionaries, m_result_file);

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println("Result is: " + xmlu.toXmlString(dictionaries));
    }

    public static void exportDictionary(String[] argv) {
        Dict2XmlAruba obj = new Dict2XmlAruba();

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
        Dict2XmlAruba obj = getInstance();

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
