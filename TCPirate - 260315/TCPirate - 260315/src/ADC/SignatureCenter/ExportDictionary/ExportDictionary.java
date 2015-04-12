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

import ADC.Utils.*;
import org.w3c.dom.*;
//import org.apache.crimson.tree.TextNode;

public class ExportDictionary {
    private static ExportDictionary ourInstance = null;

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
    private static final String QUERY_DICTIONARY_CLASS = "select dict_classe_id from tblDictClasses where dict_id = ";
    private static final String QUERY_DICTIONARY_SERVICE = "select dict_service from tblDictServices where dict_id = ";
    private static final String QUERY_DICTIONARY_COMPLEXITY = "select dict_complexity from tblDictComplexity where dict_id = ";
    private static final String QUERY_DICTIONARY_RISK = "select dict_risk from tblDictRisks where dict_id = ";
    private static final String QUERY_DICTIONARY_FREQUENCY = "select dict_frequency from tblDictFrequency where dict_id = ";
    private static final String QUERY_DICTIONARY_ACCURACY = "select dict_accuracy from tblDictAccuracy where dict_id = ";
    private static final String QUERY_DICTIONARY_AFFECTED_SYSTEMS = "select dict_affected_system_name_id from tblDictAffectedSystems where dict_id = ";
    private static final String QUERY_SIGNATURE_SERVICES = "select signatureId, serviceId from tblSignatureService order by signatureId asc";

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

    public static ExportDictionary getInstance() {
        if (ourInstance == null)
            ourInstance = new ExportDictionary();

        return ourInstance;
    }

    private ExportDictionary() {
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

    // Construct an IN clause for the SELECT statement based on a record set
    private String buildINclause(ResultSet rs) {
        String tmp = null;
        boolean isFirst = true;

        try {
            while (rs.next()) {
                if (isFirst) {
                    isFirst = false;
                    tmp = "in (";
                } else
                    tmp = tmp + ", ";

                tmp = tmp + rs.getString(1);
            }

            if (tmp != null)
                tmp = tmp + ")";
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to retrieve data for IN clause");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        return tmp;
    }

    private String prepareINclause(int p_dictionary, ResultSet rs, String flag_name, String p_statement) {
        try {
            boolean flag = rs.getBoolean(flag_name);
            if (flag) {
                ResultSet rs1 = DBHelper.getInstance().directSelect(p_statement + p_dictionary);
                String tmp = buildINclause(rs1);
                rs1.close();
                return tmp;
            } else
                return null;
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create criteria");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
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
        signature_criteria = signature_criteria + " and sig_minver <= 300 and (sig_maxver is null or sig_maxver = 330)";

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

            String tmp = null;
            boolean flag = false;

            // Collect all attack related criteria
            tmp = prepareINclause(dict_id, rs_dictionary, "dict_class_enabled", QUERY_DICTIONARY_CLASS);
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_class " + tmp;
            }

            tmp = prepareINclause(dict_id, rs_dictionary, "dict_complexity_enabled", QUERY_DICTIONARY_COMPLEXITY);
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_complexity_id " + tmp;
            }

            tmp = prepareINclause(dict_id, rs_dictionary, "dict_risk_enabled", QUERY_DICTIONARY_RISK);
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_risk " + tmp;
            }

            tmp = prepareINclause(dict_id, rs_dictionary, "dict_affected_systems_enabled", QUERY_DICTIONARY_AFFECTED_SYSTEMS);
            if (tmp != null) {
                if (attack_criteria != "")
                    attack_criteria = attack_criteria + " and ";
                attack_criteria = attack_criteria + "attack_id in (select attack_id from tblAffectedSystmes where system_name_id " + tmp + ")";
            }

            // Collect all service related criteria
            tmp = prepareINclause(dict_id, rs_dictionary, "dict_service_enabled", QUERY_DICTIONARY_SERVICE);
            if (tmp != null) {
                service_criteria = service_criteria + "serviceId " + tmp;
            }

            // Collect all signature related criteria
            tmp = prepareINclause(dict_id, rs_dictionary, "dict_frequency_enabled", QUERY_DICTIONARY_FREQUENCY);
            if (tmp != null) {
                if (signature_criteria != "")
                    signature_criteria = signature_criteria + " and ";
                signature_criteria = signature_criteria + "sig_frequency " + tmp;
            }

            tmp = prepareINclause(dict_id, rs_dictionary, "dict_accuracy_enabled", QUERY_DICTIONARY_ACCURACY);
            if (tmp != null) {
                if (signature_criteria != "")
                    signature_criteria = signature_criteria + " and ";
                signature_criteria = signature_criteria + "sig_accuracy " + tmp;
            }

            flag = rs_dictionary.getBoolean("dict_direction_enabled");
            if (flag) {
                flag = rs_dictionary.getBoolean("dict_direction_isClient2Server");
                if (flag) {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_dirClient2Server = 1";
                }
                flag = rs_dictionary.getBoolean("dict_direction_isServer2Client");
                if (flag) {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_dirServer2Client = 1";
                }
            }

            flag = rs_dictionary.getBoolean("dict_location_enabled");
            if (flag) {
                flag = rs_dictionary.getBoolean("dict_location_isuri");
                if (flag) {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_isURL = 1";
                }
                flag = rs_dictionary.getBoolean("dict_location_isparameter");
                if (flag) {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_isParam = 1";
                }
                flag = rs_dictionary.getBoolean("dict_location_isgeneral");
                if (flag) {
                    if (signature_criteria != "")
                        signature_criteria = signature_criteria + " and ";
                    signature_criteria = signature_criteria + "sig_isRaw = 1";
                }
            }

            flag = rs_dictionary.getBoolean("dict_is_applayer");
            if (signature_criteria != "")
                signature_criteria = signature_criteria + " and ";
            if (flag)
                signature_criteria = signature_criteria + "sig_isAppLayer = 1";
            else
                signature_criteria = signature_criteria + "sig_isAppLayer = 0";

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
                service.appendChild(doc.createTextNode(Integer.toString(services.getInt("serviceId"))));
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

    private void addSignature(Element sigs_element, ResultSet signatures, ResultSet services, boolean shouldBlock) {
        Document doc = sigs_element.getOwnerDocument();
        Element el = doc.createElement("signature");

        sigs_element.appendChild(el);

        try {
            el.setAttribute("id", Long.toString(signatures.getLong("sig_id")));
            el.setAttribute("is-app-layer", Boolean.toString(signatures.getBoolean("sig_isAppLayer")));
            el.setAttribute("is-decoded", Boolean.toString(signatures.getBoolean("sig_isDecoded")));
            el.setAttribute("is-case-sensitive", Boolean.toString(signatures.getBoolean("sig_caseSensitive")));
            el.setAttribute("is-regexp", Boolean.toString(signatures.getBoolean("sig_isregexp")));

            Element pattern = doc.createElement("pattern");
            pattern.appendChild(doc.createTextNode(signatures.getString("sig_signature")));
            el.appendChild(pattern);

            Element direction = doc.createElement("direction");
            if (signatures.getBoolean("sig_dirClient2Server"))
                direction.appendChild(doc.createTextNode("client-to-server"));
            else
                direction.appendChild(doc.createTextNode("server-to-client"));
            el.appendChild(direction);

            Element locations = doc.createElement("locations");
            if (signatures.getBoolean("sig_isRaw")) {
                Element dir = doc.createElement("location");
                dir.appendChild(doc.createTextNode("full"));
                locations.appendChild(dir);
            }
            if (signatures.getBoolean("sig_isURL")) {
                Element dir = doc.createElement("location");
                dir.appendChild(doc.createTextNode("url"));
                locations.appendChild(dir);
            }
            if (signatures.getBoolean("sig_isParam")) {
                Element dir = doc.createElement("location");
                dir.appendChild(doc.createTextNode("parameters"));
                locations.appendChild(dir);
            }
            el.appendChild(locations);

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
        ExportDictionary obj = new ExportDictionary();

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
        ExportDictionary obj = getInstance();

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
