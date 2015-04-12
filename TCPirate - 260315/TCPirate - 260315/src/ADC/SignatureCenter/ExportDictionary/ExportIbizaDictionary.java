package ADC.SignatureCenter.ExportDictionary;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 25/11/2004
 * Time: 08:42:06
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.sql.ResultSet;
import java.util.HashMap;

import ADC.Utils.*;
import org.w3c.dom.*;

public class ExportIbizaDictionary {
    private static ExportIbizaDictionary ourInstance = null;

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
    private static final int OPTION_SHOULD_BLOCK = 11;
    private static final int OPTION_INPUT_FILE = 12;
    private static final int OPTION_TEST_ONLY = 13;
    private static final int OPTION_RAW_QUERY = 14;
    private static final int OPTION_TEST_MIXED = 15;
    private static final int OPTION_TEST_NONE = 16;
    private static final int OPTION_FEATURE_PACK = 17;

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
            new CommandLineOptionDef("Dn", OPTION_DATABASE_NAME),
            new CommandLineOptionDef("b", OPTION_SHOULD_BLOCK),
            new CommandLineOptionDef("if", OPTION_INPUT_FILE),
            new CommandLineOptionDef("tx", OPTION_TEST_ONLY),
            new CommandLineOptionDef("tm", OPTION_TEST_MIXED),
            new CommandLineOptionDef("tn", OPTION_TEST_NONE),
            new CommandLineOptionDef("fp", OPTION_FEATURE_PACK),
            new CommandLineOptionDef("q", OPTION_RAW_QUERY)};

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final int TEST_MODE_UNDEFINED = 0;
    private static final int TEST_MODE_NONE = 1;
    private static final int TEST_MODE_MIXED = 2;
    private static final int TEST_MODE_EXCLUSIVE = 3;

    private static final String QUERY_DICTIONARY_DATA = "select * from tblDictionaries where dict_name = ";
    private static final String QUERY_SIGNATURE_SERVICES = "select signatureId, serviceId, name from tblSignatureService, tblService where id = serviceid order by signatureId asc";

    private static final String[] locations = {"stream", "url", "parameters", "headers", "query", "parsed-query", "non-normalized-url", "url-and-parameters", "response-content"};

    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;
    private String m_input_file = null;
    private int m_test_mode = TEST_MODE_UNDEFINED;
    private String m_raw_query = null;

    private String m_db_host = null;
    private String m_db_port_str = null;
    private String m_db_user = null;
    private String m_db_password = null;
    private String m_db_name = null;

    private String m_dictionary_name = null;
    private String m_result_file = null;
    private boolean m_shouldBlock = false;
    private boolean m_no_more_services = false;
    private boolean m_ibiza_fp = false;

    public static ExportIbizaDictionary getInstance() {
        if (ourInstance == null)
            ourInstance = new ExportIbizaDictionary();

        return ourInstance;
    }

    private ExportIbizaDictionary() {
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
                case OPTION_DICTIONARY:
                    if ((m_raw_query != null) || (m_dictionary_name != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_dictionary_name = cla.getValue(0);
                    break;
                case OPTION_FILE:
                    if ((m_result_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_result_file = cla.getValue(0);
                    break;
                case OPTION_INPUT_FILE:
                    if ((m_input_file != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_input_file = cla.getValue(0);
                    break;
                case OPTION_SHOULD_BLOCK:
                    m_shouldBlock = true;
                    break;
                case OPTION_TEST_ONLY:
                case OPTION_TEST_NONE:
                case OPTION_TEST_MIXED:
                    if (m_test_mode != TEST_MODE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    if (option == OPTION_TEST_ONLY)
                        m_test_mode = TEST_MODE_EXCLUSIVE;
                    else if (option == OPTION_TEST_NONE)
                        m_test_mode = TEST_MODE_NONE;
                    else if (option == OPTION_TEST_MIXED)
                        m_test_mode = TEST_MODE_MIXED;

                    break;
                case OPTION_RAW_QUERY:
                    if ((m_raw_query != null) || (m_dictionary_name != null) || (cla.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_raw_query = cla.getValue(0);
                    break;
                case OPTION_FEATURE_PACK:
                    m_ibiza_fp = true;
                    break;
            }
        }

        // Check for errors
        if (error == true)
            return -1;

        if ((m_db_host == null) || ((m_dictionary_name == null) && (m_raw_query == null)))
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

        if (m_test_mode == TEST_MODE_UNDEFINED)
            m_test_mode = TEST_MODE_NONE;

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

    private String getElementValue(Element p_element) {
        NodeList nl = p_element.getChildNodes();
        if (nl.getLength() == 0)
            return null;

        for (int index = 0; index < nl.getLength(); index++) {
            Node n = nl.item(index);
            if (n.getNodeType() == Node.TEXT_NODE)
                return n.getNodeValue();
        }

        return null;
    }

    private Element getFirstWithName(Element p_root, String p_element) {
        NodeList nl = p_root.getElementsByTagName(p_element);
        if (nl.getLength() == 0)
            return null;

        return (Element)nl.item(0);
    }

    // Construct an IN clause for the SELECT statement based on a record set
    private String buildINclause(NodeList p_nl) {
        String tmp = null;
        boolean isFirst = true;

        for (int index = 0; index < p_nl.getLength(); index++)
        {
            if (isFirst) {
                isFirst = false;
                tmp = "in (";
            } else
                tmp = tmp + ", ";

            tmp = tmp + getElementValue((Element)(p_nl.item(index)));
        }

        if (tmp != null)
            tmp = tmp + ")";

        return tmp;
    }

    private String prepareINclause(Element xml_filter, String p_element, String p_item) {
        if (isFilterEnabled(xml_filter, p_element)) {
            Element el = getFirstWithName(xml_filter, p_element);
            NodeList nl = el.getElementsByTagName(p_item);
            String tmp = buildINclause(nl);
            return tmp;
        } else
            return null;
    }

    private boolean isFilterEnabled(Element p_filter, String p_element) {
        NodeList nl = p_filter.getElementsByTagName(p_element);
        if (nl.getLength() == 0)
            return false;

        Element el = (Element)nl.item(0);
        String attr = el.getAttribute("use");
        if (Integer.parseInt(attr) == 0)
            return false;
        else
            return true;
    }

    private int location_mask(String location) {
        for (int index = 0; index < locations.length; index++) {
            if (locations[index].equalsIgnoreCase(location)) {
                return 1 << index;
            }
        }

        return 0;
    }

    private int buildLocationValue(Element p_location) {
        NodeList nl = p_location.getElementsByTagName("location");
        int res = 0;
        for (int index = 0; index < nl.getLength(); index++) {
            String loc = getElementValue((Element)nl.item(index));
            int loc_mask = location_mask(loc);
            res |= loc_mask;
        }

        return res;
    }

    private String buildDictionaryQuery() {
        DBHelper dbh = DBHelper.getInstance();
        String attack_criteria = "";
        String service_criteria = "";
        String signature_criteria = null;

        if (m_test_mode == TEST_MODE_NONE)
            signature_criteria = "sig_goes2product = 1";
        else if (m_test_mode == TEST_MODE_EXCLUSIVE)
            signature_criteria = "sig_goes2product = 0 and sig_istesting = 1";
        else if (m_test_mode == TEST_MODE_MIXED)
            signature_criteria = "(sig_goes2product = 1 or sig_istesting = 1)";


        signature_criteria = signature_criteria + " and sig_deleted = 0 and sig_isCustomerSpecific = 0";

        // Added to distinguish old signatures
        // Changed to support Ibiza FP
        if (m_ibiza_fp)
            signature_criteria = signature_criteria + " and sig_minver <= 360 and (sig_maxver is null or sig_maxver >= 360)";
        else
            signature_criteria = signature_criteria + " and sig_minver <= 350 and (sig_maxver is null or sig_maxver >= 350)";


        // Retrieve information for dictionary
        ResultSet rs_dictionary = dbh.directSelect(QUERY_DICTIONARY_DATA + "'" + m_dictionary_name + "'");

        try {
            if (!rs_dictionary.next())
                return null;
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to retrieve data for dictionary");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        try {
            // Retrieve dictionary id
            int dict_id = rs_dictionary.getInt("dict_id");
            XmlUtils xmlu = XmlUtils.getInstance();
            Element xml_filter = xmlu.stringToDOM(rs_dictionary.getString("dict_xmlFilter"));

            String tmp = null;
            boolean flag = false;

            // Collect all attack related criteria
            tmp = prepareINclause(xml_filter, "classes", "class");
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_class " + tmp;
            }

            tmp = prepareINclause(xml_filter, "complexity-levels", "complexity");
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_complexity_id " + tmp;
            }

            tmp = prepareINclause(xml_filter, "risk-levels", "risk");
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_risk " + tmp;
            }

            tmp = prepareINclause(xml_filter, "affected-systems", "system/name");
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_id in (select attack_id from tblAffectedSystmes where system_name_id " + tmp + ")";
            }

            // Collect all service related criteria
            tmp = prepareINclause(xml_filter, "services", "service");
            if (tmp != null) {
                service_criteria = service_criteria + "serviceId " + tmp;
            }

            // Collect all signature related criteria
            tmp = prepareINclause(xml_filter, "frequency-levels", "frequency");
            if (tmp != null) {
                if (signature_criteria != "")
                    signature_criteria = signature_criteria + " and ";
                signature_criteria = signature_criteria + "sig_frequency " + tmp;
            }

            tmp = prepareINclause(xml_filter, "accuracy-levels", "accuracy");
            if (tmp != null) {
                if (signature_criteria != "")
                    signature_criteria = signature_criteria + " and ";
                signature_criteria = signature_criteria + "sig_accuracy " + tmp;
            }

            flag = isFilterEnabled(xml_filter, "is_client2server");
            if (flag) {
                String tmp_str = getElementValue(getFirstWithName(getFirstWithName(xml_filter, "is_client2server"), "is-client2server"));
                if (tmp_str == "true") {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_dirClient2Server = 1";
                }

                tmp_str = getElementValue(getFirstWithName(getFirstWithName(xml_filter, "is_client2server"), "is-server2client"));
                if (tmp_str == "true") {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_dirServer2Client = 1";
                }
            }

            flag = isFilterEnabled(xml_filter, "locations");
            if (flag) {
                int tmp_loc = buildLocationValue(getFirstWithName(xml_filter, "locations"));

                if (signature_criteria != "")
                    signature_criteria = signature_criteria + " and ";

                signature_criteria = signature_criteria + "sig_location & " + tmp_loc + " > 0";
            }

            rs_dictionary.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create select statement for signatures");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }


        String query = "select * from tblSignatures where ";
        if (signature_criteria.length() > 0)
            query = query + signature_criteria;

        if (attack_criteria.length() > 0)
            query = query + " and sig_attacksuperclass in (select attack_id from tblAttacks where " + attack_criteria + ")";

        if (service_criteria.length() > 0)
            query = query + " and sig_id in (select signatureId from tblSignatureService where " + service_criteria + ")";

        query = query + " order by sig_id asc";

        return query;
    }

    private void addServices(Document doc, Element service_list, ResultSet services, long signature) {

        try {
            while ((services != null) && (services.getLong("signatureId") == signature)) {
                Element service = doc.createElement("service");
                service.appendChild(doc.createTextNode(services.getString("name")));
                service_list.appendChild(service);

                if (!services.next()) {
                    m_no_more_services = true;
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println(this.getClass().getName() + ": Failed to create list of services for signature " + signature);
            ex.printStackTrace(System.err);
            throw new RuntimeException(ex);
        }
    }

    private Element locationElement(int location) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("<locations>");

        int mask = 1;

        for (int index = 0; index < locations.length; index++) {
            if ((location & mask) != 0) {
                String name = locations[index];
                sb.append("<location>");
                sb.append(name);
                sb.append("</location>");
            }
            mask <<= 1;
        }

        sb.append("</locations>");

        return XmlUtils.getInstance().stringToDOM(sb.toString());
    }


    private void addSignature(Element sigs_element, ResultSet signatures, ResultSet services, boolean shouldBlock) {
        Document doc = sigs_element.getOwnerDocument();
        Element el = doc.createElement("signature");

        sigs_element.appendChild(el);

        try {
            el.setAttribute("id", Long.toString(signatures.getLong("sig_id")));
            el.setAttribute("is-case-sensitive", Boolean.toString(signatures.getBoolean("sig_caseSensitive")));

            Element pattern = doc.createElement("pattern");
            pattern.appendChild(doc.createTextNode(signatures.getString("sig_signature")));
            el.appendChild(pattern);

            Element direction = doc.createElement("direction");
            if (signatures.getBoolean("sig_dirClient2Server"))
                direction.appendChild(doc.createTextNode("client-to-server"));
            else
                direction.appendChild(doc.createTextNode("server-to-client"));
            el.appendChild(direction);

            int location = signatures.getInt("sig_location");
            Element locations = locationElement(location);
            Element loc = (Element)el.getOwnerDocument().importNode(locations, true);
            el.appendChild(loc);

            Element action = doc.createElement("action");
            if (shouldBlock)
                action.appendChild(doc.createTextNode("block"));
            else
                action.appendChild(doc.createTextNode("alert"));
            el.appendChild(action);

            Element user_data = doc.createElement("user-data");
            user_data.appendChild(doc.createTextNode("0"));
            el.appendChild(user_data);

            Element service_list = doc.createElement("services");
            if ((services != null) && (services.getLong("signatureId") == signatures.getLong("sig_id")))
                addServices(doc, service_list, services, signatures.getLong("sig_id"));
            el.appendChild(service_list);
        } catch (Exception ex) {
            System.err.println(this.getClass().getName() + ": Failed to create signature");
            ex.printStackTrace(System.err);
            throw new RuntimeException(ex);
        }
    }

    private void readDictionary() {
        DBHelper dbh = DBHelper.getInstance();
        XmlUtils xmlu = XmlUtils.getInstance();
        Document doc = null;
        Element sigs_element = null;

        // Initialize the results document from a file or from scratch.
        if (m_input_file != null) {
            Element el = xmlu.readXmlFIle(m_input_file);
            NodeList nodes = el.getElementsByTagName("signatures");
            Element el1 = (Element)nodes.item(0);
            el.removeChild(el1);
            doc = el.getOwnerDocument();
            sigs_element = doc.createElement("signatures");
            doc.getDocumentElement().appendChild(sigs_element);
        }
        else {
            doc = xmlu.newDocument("signatures");
            sigs_element = doc.getDocumentElement();
        }


        dbh.connect(m_db_host, m_db_port_str, m_db_name, m_db_user, m_db_password);

        String query = null;
        if (m_raw_query == null)
            query = buildDictionaryQuery();
        else
            query = m_raw_query;

        ResultSet rs = dbh.directSelect(query);

        ResultSet srv_rs = dbh.directSelect(QUERY_SIGNATURE_SERVICES);

        long sig_service_id = 0;

        try {
            while (rs.next()) {
                long sig_id = rs.getLong("sig_id");

                while ((sig_service_id < sig_id) && srv_rs.next())
                    sig_service_id = srv_rs.getLong("signatureId");

                if (sig_service_id != sig_id) {
                    sig_service_id = Long.MAX_VALUE;
                    srv_rs.close();
                    srv_rs = null;
                    System.err.println("Failed to generate dictionary - Signature does not have serivces: " + sig_id);
                    throw new RuntimeException("Failed to generate dictionary - Signature does not have serivces: " + sig_id);
                }

                addSignature(sigs_element, rs, srv_rs, m_shouldBlock);

                if ((srv_rs != null) && (srv_rs.getWarnings() == null) && (!m_no_more_services))
                    sig_service_id = srv_rs.getLong("signatureId");
                else {
                    sig_service_id = Long.MAX_VALUE;
                    srv_rs = null;
                }
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create signature list");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        xmlu.writeXmlFile(doc.getDocumentElement(), m_result_file);

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println("Query is: " + query);
    }

    public static void exportDictionary(String[] argv) {
        ExportIbizaDictionary obj = new ExportIbizaDictionary();

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
        System.out.println("-if <filename>\t\tInput file name");
        System.out.println("-of <filename>\t\tOutput file name (default to input)");
        System.out.println("-d <dir name>\t\tName of dictionary to export");
        System.out.println("-b\t\tSet signature action to block");
    }

    public static void main(String[] argv) {
        ExportIbizaDictionary obj = getInstance();

        CommandLineArguments cla = CommandLineArguments.getInstance();
        if (cla.parse(argv, m_command_line_options) < 0) {
            System.exit(-1);
        }

        if (obj.processCommandLineArguments(cla) < 0) {
            System.exit(-1);
        }

        obj.prepareOutput();
        obj.prepareError();

        obj.readDictionary();
    }
}
