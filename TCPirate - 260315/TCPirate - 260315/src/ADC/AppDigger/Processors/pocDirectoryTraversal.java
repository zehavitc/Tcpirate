package ADC.AppDigger.Processors;

import ADC.AppDigger.EventProcessorImpl;
import ADC.AppDigger.EventProcessor;
import ADC.Utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.PrintStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 06/04/2005
 * Time: 09:59:34
 * To change this template use File | Settings | File Templates.
 */
public class pocDirectoryTraversal extends EventProcessorImpl {

    final private static String PROPERTY_DESTINATION_FOLDER = "destination-folder";

    final private static int VIOLATION_NONE = 0;
    final private static int VIOLATION_BAD_FOLDER = 1;
    final private static int VIOLATION_CURRENT_FOLDER = 2;
    final private static int VIOLATION_PARENT_FOLDER_SEQ = 3;
    final private static int VIOLATION_PARENT_FOLDER = 4;
    final private static int VIOLATION_ESCAPE_ROOT = 5;
    final private static int VIOLATION_SEPARATOR = 6;
    final private static int VIOLATION_ESCAPE_ROOT_FAST = 7;
    final private static int VIOLATION_SIGNATURE = 8;
    final private static int VIOLATION_ABSOLUTE_PATH = 9;

    final private static int DEFAULT_THRESHOLD_BAD_FOLDER_LEN = 10;
    final private static int DEFAULT_THRESHOLD_CURRENT_FOLDER = 2;
    final private static int DEFAULT_THRESHOLD_PARENT_FOLDER = 3;
    final private static int DEFAULT_THRESHOLD_PARENT_FOLDER_SEQ = 3;
    final private static int DEFAULT_THRESHOLD_SEPARATOR = 10;

    final private static int ANOMALY_REDUNDANT_UTF8_PATH = 54;
    final private static int ANOMALY_DOUBLE_ENCODING_PATH = 53;
    final private static int ANOMALY_BAD_ENCODING_PATH = 43;

    final private static int MODE_NORMAL = 0;
    final private static int MODE_SEPARATOR = 1;
    final private static int MODE_FOLDER = 2;
    final private static int MODE_INITIAL = 3;

    final private static String[] m_signatures = {"winnt", "win2000", "system32", "boot.ini", "win.ini", "5C", "/etc/passwd", "cmd.exe"};
    final private static String[] m_allow_path = {"fname", "upload_file", "cvattachment", "filename", "htmlfile", "logofile"};
    final private static String[] m_allow_folder = {"backto"};

    private static int m_threshold_bad_folder_len = DEFAULT_THRESHOLD_BAD_FOLDER_LEN;
    private static int m_threshold_current_folder = DEFAULT_THRESHOLD_CURRENT_FOLDER;
    private static int m_threshold_parent_folder = DEFAULT_THRESHOLD_PARENT_FOLDER;
    private static int m_threshold_parent_folder_seq = DEFAULT_THRESHOLD_PARENT_FOLDER_SEQ;
    private static int m_threshold_separator = DEFAULT_THRESHOLD_SEPARATOR;

    private String m_destination_folder = null;
    private PrintStream m_urls_file = null;
    private PrintStream m_urls_candidate_file = null;
    private PrintStream m_params_file = null;
    private PrintStream m_params_candidate_file = null;

    private int m_violation = VIOLATION_NONE;
    private long[] m_violation_counters = new long[9]; // Counter for each violation type;
    private long m_total_hits = 0;
    private long m_total_candidates = 0;

    private long[] m_param_violation_counters = new long[10]; // Counter for each violation type;
    private long m_loose_candidates = 0;
    private long m_strong_candidates = 0;
    private long m_param_hits = 0;
    private long m_total_params = 0;

    private String doUTF8(String p_original, BitSet source, BitSet dest) {
        int size = p_original.length();
        int pos = 0;
        int mode = 0;
        int dst_pos = 0;
        int utf_mask = 0;

        StringBuffer result = new StringBuffer(size);

        while (pos < size) {
            int a_char = (p_original.charAt(pos) & 0x00FF);
            if (mode == 0) {
                // Normal Mode
                if ((a_char == 0x00C0) || (a_char == 0x00C1)) {
                    // Statrt a 2 byte sequence
                    mode = 1;
                    if (a_char == 0x00C0)
                        utf_mask = 0x0040;
                    else
                        utf_mask = 0x0000;
                }
                else if (a_char == 0x00E0) {
                    // Start a 3 byte sequence
                    mode = 2;
                    utf_mask = 0x0000;
                }
                else {
                    // Normal character
                    result.append(p_original.charAt(pos));
                    if ((source != null) && (source.get(pos)))
                        dest.set(dst_pos);
                    dst_pos++;
                }
            } else if (mode == 2) {
                // Second byte of a three byte sequence
                if (a_char == 0x0080)
                    // Go to third byte in sequence
                    mode = 1;
                else {
                    // Not actually redundant
                    result.append(p_original.charAt(pos - 1));
                    result.append(p_original.charAt(pos));
                    if ((source != null) && (source.get(pos - 1)))
                        dest.set(dst_pos);
                    dst_pos++;
                    if ((source != null) && (source.get(pos)))
                        dest.set(dst_pos);
                    dst_pos++;
                    mode = 0;
                }
            } else  { // Last byte of a sequence
                a_char = a_char & 0x003F;
                a_char = a_char | utf_mask;
                result.append((char)a_char);
                dest.set(dst_pos);
                mode = 0;
                dst_pos++;
            }

            pos++;
        }

        return result.toString();
    }

    private String doUrlDecoding(String p_original, BitSet source, BitSet dest) {
        int size = p_original.length();
        int pos = 0;
        int mode = 0;
        int dst_pos = 0;
        int new_char = 0;

        StringBuffer result = new StringBuffer(size);

        while (pos < size) {
            char a_char = p_original.charAt(pos);
            if (mode == 0) {
                // Normal Mode
                if (a_char == '%')
                    mode = 2;
                else {
                    result.append(a_char);
                    if ((source != null) && (source.get(pos)))
                        dest.set(dst_pos);
                    dst_pos++;
                }
            } else if (mode == 2) {
                if ((a_char >= '0') && (a_char <= '9'))
                    new_char = (a_char - '0') * 16;
                else if ((a_char >= 'a') && (a_char <= 'f'))
                    new_char = ((a_char - 'a') + 10) * 16;
                else if ((a_char >= 'A') && (a_char <= 'F'))
                    new_char = ((a_char - 'A') + 10) * 16;
                else
                    new_char = 0;

                mode = 1;
            } else {
                if ((a_char >= '0') && (a_char <= '9'))
                    new_char += (a_char - '0');
                else if ((a_char >= 'a') && (a_char <= 'f'))
                    new_char += (a_char - 'a') + 10;
                else if ((a_char >= 'A') && (a_char <= 'F'))
                    new_char += (a_char - 'A') + 10;
                else
                    new_char = 0;

                mode = 0;

                result.append((char)new_char);
                dest.set(dst_pos);
                dst_pos++;
            }

            pos++;
        }

        return result.toString();
    }

    private String processPath(String p_original, boolean is_bad_utf8, boolean is_double_encoding) {

        BitSet bs1 = new BitSet(p_original.length());
        BitSet bs2 = new BitSet(p_original.length());
        String tmp = null;

        // Process illegal encodings
        tmp = doUTF8(p_original, null, bs1);
        tmp = doUrlDecoding(tmp, bs1, bs2);

        int size = tmp.length();
        StringBuffer result = new StringBuffer(size);

        int current_mode = MODE_INITIAL;
        LinkedList separators = new LinkedList();
        int pos = 0;
        char a_char;
        int folder_len = 0;
        boolean decoded = false;
        int current_folder_num = 0;
        int separator_len = 0;
        int parent_folder_num = 0;
        int parent_folder_seq = 0;

        while (pos < size) {
            a_char = tmp.charAt(pos);
            if (current_mode == MODE_INITIAL) {
                // At the beginning of string
                if ((a_char == '.') || (a_char == '\\')) {
                    // A URL beginning with a period!
                    m_violation = VIOLATION_ESCAPE_ROOT_FAST;
                    return null;
                } else if (a_char == '/') {
                    result.append('/');
                    separators.add(new Integer(result.length()));
                    separator_len = 1;
                    current_mode = MODE_SEPARATOR;
                } else {
                    current_mode = MODE_NORMAL;
                    result.append(a_char);
                }
            } else if (current_mode == MODE_NORMAL) {
                // In normal sequence
                if ((a_char == '/') || (a_char == '\\')) {
                    result.append('/');
                    separators.add(new Integer(result.length()));
                    separator_len = 1;
                    current_mode = MODE_SEPARATOR;
                } else {
                    result.append(a_char);
                }
            } else if (current_mode == MODE_FOLDER) {
                // Within a sequence of '.' characters
                if (a_char == '.') {
                    // The sequence continues
                    folder_len++;
                    if (folder_len == m_threshold_bad_folder_len) {
                        // Too long bad folder sequence
                        m_violation = VIOLATION_BAD_FOLDER;
                        return null;
                    }
                } else if ((a_char == '/') || (a_char == '\\')) {
                    // The sequence is terminated with a separator
                    if (folder_len == 1) {
                        // Current folder replacement
                        current_folder_num++;
                        parent_folder_seq = 0;
                        if (current_folder_num == m_threshold_current_folder) {
                            // Too many current folder occurences
                            m_violation = VIOLATION_CURRENT_FOLDER;
                            return null;
                        }

                        current_mode = MODE_SEPARATOR;
                    } else {
                        // Parent or bad folder replacement
                        if (separators.size() == 1) {
                            // Escape root...
                            m_violation = VIOLATION_ESCAPE_ROOT;
                            return null;
                        }

                        parent_folder_seq++;
                        if (parent_folder_seq == m_threshold_parent_folder_seq) {
                            // Too many parent folder in sequence
                            m_violation = VIOLATION_PARENT_FOLDER_SEQ;
                            return null;
                        }

                        parent_folder_num++;
                            // Too many parent folder sequences in total
                        if (parent_folder_num == m_threshold_parent_folder) {
                            // Too many parent folder in sequence
                            m_violation = VIOLATION_PARENT_FOLDER;
                            return null;
                        }

                        // Cancel a folder
                        int new_pos = ((Integer)separators.getLast()).intValue();
                        separators.removeLast();
                        result.setLength(new_pos);

                        current_mode = MODE_SEPARATOR;
                    }
                } else {
                    // Normal characters
                    current_mode = MODE_NORMAL;
                    folder_len = 0;
                    parent_folder_seq = 0;
                    result.append(a_char);
                }
            } else {
                // Mode separator
                if ((a_char == '/') || (a_char == '\\')) {
                    // Continue in current mode
                    separator_len++;
                    if (separator_len == m_threshold_separator) {
                        // Too many consecutive separators.
                        m_violation = VIOLATION_SEPARATOR;
                        return null;
                    }
                } else {
                    // End of separator sequence
                    separator_len = 0;

                    if (a_char == '.') {
                        // Start a folder sequence
                        current_mode = MODE_FOLDER;
                        folder_len = 1;
                    } else {
                        // Simple character
                        parent_folder_seq = 0;
                        current_mode = MODE_NORMAL;
                        result.append(a_char);
                    }
                }
            }

            pos++;
        }

        return result.toString();
    }

    private boolean matchSignature(String p_candidate) {
        String tmp = p_candidate.toLowerCase();
        for (int index = 0; index < m_signatures.length; index++) {
            if (tmp.indexOf(m_signatures[index]) != -1)
                return true;
        }

        return false;
    }

    private void handleUrlCandidate(String p_url, boolean is_bad_utf8, boolean is_double_encoding) {
        m_violation = VIOLATION_NONE;

        String normalized_url = processPath(p_url, is_bad_utf8, is_double_encoding);

        if ( (m_violation == VIOLATION_NONE) && (normalized_url == null))
            System.out.println(p_url);

        if ((m_violation == VIOLATION_NONE) && matchSignature(normalized_url))
            m_violation = VIOLATION_SIGNATURE;
    }

    static private boolean existAnomaly(String p_mask, int p_anomaly) {
        return (p_mask.charAt(63 - p_anomaly) == '1');
    }

    private String isUrlCandidate(Element el) {
        // Extract the URL from the event (before normalization)
        NodeList nl = el.getElementsByTagName("full-url");

        Node n = ((Element)nl.item(0)).getFirstChild();
        String val = null;

        while (n != null) {
            if (n.getNodeType() == Node.TEXT_NODE) {
                if (val == null)
                    val = n.getNodeValue();
                else
                    val = val + n.getNodeValue();
            }
            n = n.getNextSibling();
        }

        String url = val;

        if (url == null)
            return null;

        // Check for normalization issues
        if ((url.indexOf("..") != -1) || (url.indexOf("//") != -1) ||
                (url.indexOf("./") != -1) || (url.indexOf("\\") != -1))
            return url;

        // Look for anomalies
        nl = el.getElementsByTagName("irregulars");
        if (nl.getLength() == 0)
            return null;

        String mask = ((Element)nl.item(0)).getAttribute("mask");

        if (existAnomaly(mask, ANOMALY_REDUNDANT_UTF8_PATH) || existAnomaly(mask, ANOMALY_DOUBLE_ENCODING_PATH) ||
            existAnomaly(mask, ANOMALY_BAD_ENCODING_PATH))
            return url;

        return null;
    }

    private void handleUrls(Element el) {
        String eid = el.getAttribute("id");

        String url = isUrlCandidate(el);

        if (url != null) {
            m_total_candidates++;
            handleUrlCandidate(url, true, true);
            m_violation_counters[m_violation]++;

            if (m_violation != VIOLATION_NONE) {
                m_urls_file.print(m_violation + "\t");
                m_urls_file.println(url);
                m_total_hits++;
            } else {
                m_urls_candidate_file.println(url);
                // XmlUtils.getInstance().writeXmlFile(el, m_destination_folder + File.separator + "urls" + File.separator + eid + ".xml");
            }
        }
    }

    private String processParamValue(String p_param, String p_name, boolean is_bad_utf8, boolean is_double_encoding) {
        BitSet bs1 = new BitSet(p_param.length());
        BitSet bs2 = new BitSet(p_param.length());
        String tmp = null;

        // Process illegal encodings
        tmp = doUTF8(p_param, null, bs1);
        tmp = doUrlDecoding(tmp, bs1, bs2);

        boolean allow_folder = allowFolder(p_name);

        // Check for absolute path
        if ((tmp.charAt(1) == ':') && (tmp.charAt(2) == '\\')) {
            char ch = tmp.charAt(0);
            if (((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'))) {
                m_violation = VIOLATION_ABSOLUTE_PATH;
                return null;
            }
        }

        int size = tmp.length();
        StringBuffer result = new StringBuffer(size);

        int current_mode = MODE_INITIAL;
        LinkedList separators = new LinkedList();
        int pos = 0;
        char a_char;
        int folder_len = 0;
        boolean decoded = false;
        int parent_folder_num = 0;

        while (pos < size) {
            a_char = tmp.charAt(pos);
            if (current_mode == MODE_INITIAL) {
                // At the beginning of string
                if (a_char == '.') {
                    // A Parameters beginning with a period!
                    current_mode = MODE_FOLDER;
                    folder_len = 1;

                    return null;
                } else if ((a_char == '/') || (a_char == '\\')) {
                    result.append('/');
                    separators.add(new Integer(result.length()));
                    current_mode = MODE_SEPARATOR;
                } else {
                    current_mode = MODE_NORMAL;
                    result.append(a_char);
                }
            } else if (current_mode == MODE_NORMAL) {
                // In normal sequence
                if ((a_char == '/') || (a_char == '\\')) {
                    result.append('/');
                    separators.add(new Integer(result.length()));
                    current_mode = MODE_SEPARATOR;
                } else {
                    result.append(a_char);
                }
            } else if (current_mode == MODE_FOLDER) {
                // Within a sequence of '.' characters
                if (a_char == '.') {
                    // The sequence continues
                    folder_len++;
                } else if ((a_char == '/') || (a_char == '\\')) {
                    // The sequence is terminated with a separator
                    if (folder_len == 1) {
                        // Current folder replacement
                        current_mode = MODE_SEPARATOR;
                    } else {
                        parent_folder_num++;
                        if ((parent_folder_num == m_threshold_parent_folder) || !allow_folder) {
                            // Too many parent folder sequences in total
                            m_violation = VIOLATION_PARENT_FOLDER;
                            return null;
                        }

                        // Cancel a folder
                        int new_pos = 0;
                        if (separators.size() > 0) {
                            new_pos = ((Integer)separators.getLast()).intValue();
                            separators.removeLast();
                        }

                        result.setLength(new_pos);

                        current_mode = MODE_SEPARATOR;
                    }
                } else {
                    // Normal character
                    current_mode = MODE_NORMAL;
                    result.append(a_char);
                    folder_len = 0;
                }
            } else {
                // Mode separator
                if ((a_char == '/') || (a_char == '\\')) {
                    // Continue in current mode
                } else {
                    // End of separator sequence

                    if (a_char == '.') {
                        // Start a folder sequence
                        current_mode = MODE_FOLDER;
                        folder_len = 1;
                    } else {
                        // Simple character
                        current_mode = MODE_NORMAL;
                        result.append(a_char);
                    }
                }
            }

            pos++;
        }

        return result.toString();
    }

    private void handleParameterCandidate(String p_param, String p_name, boolean is_bad_utf8, boolean is_double_encoding) {
        m_violation = VIOLATION_NONE;

        if (allowPath(p_name))
            return;

        String normalized_param = processParamValue(p_param, p_name, is_bad_utf8, is_double_encoding);

        if ( (m_violation == VIOLATION_NONE) && (normalized_param == null))
            System.out.println(p_param);

        if ((m_violation == VIOLATION_NONE) && matchSignature(normalized_param))
            m_violation = VIOLATION_SIGNATURE;
     }

    private boolean allowPath(String p_name) {
        for (int index = 0; index < m_allow_path.length; index++) {
            if (p_name.equalsIgnoreCase(m_allow_path[index]))
                return true;
        }

        return false;
    }

    private boolean allowFolder(String p_name) {
        for (int index = 0; index < m_allow_folder.length; index++) {
            if (p_name.equalsIgnoreCase(m_allow_folder[index]))
                return true;
        }

        return false;
    }

    private void handleParameters(Element el) {

        // Get list of parameters
        NodeList nl = el.getElementsByTagName("param-item");

        Node n = null;
        String val = null;
        String name = null;

        boolean param_flag = false;

        for (int index = 0; index < nl.getLength(); index++) {

            // Extract the value of the parameter
            Element param = (Element)nl.item(index);

            NodeList nl1 = param.getElementsByTagName("value");

            n = ((Element)nl1.item(0)).getFirstChild();
            val = null;

            while (n != null) {
                if (n.getNodeType() == Node.TEXT_NODE) {
                    if (val == null)
                        val = n.getNodeValue();
                    else
                        val = val + n.getNodeValue();
                }
                n = n.getNextSibling();
            }

            if (val == null)
                return;

            m_total_params++;

            // Extract the name of the parameter
            nl1 = param.getElementsByTagName("name");

            n = ((Element)nl1.item(0)).getFirstChild();
            name = null;

            while (n != null) {
                if (n.getNodeType() == Node.TEXT_NODE) {
                    if (name == null)
                        name = n.getNodeValue();
                    else
                        name = name + n.getNodeValue();
                }
                n = n.getNextSibling();
            }

            // Look for candidates
            if (((val.indexOf('.') != -1) || (val.indexOf(':') != -1)) &&
                    ((val.indexOf('/') != -1) || (val.indexOf('\\') != -1))) {
                m_loose_candidates++;

                // Differentiate between accurate selection and coarse selection
                if ((val.indexOf("../") != -1) || (val.indexOf("..\\") != -1) ||
                    (val.indexOf(":\\") != -1))
                    m_strong_candidates++;

                handleParameterCandidate(val, name, true, true);

                if (m_violation != VIOLATION_NONE) {
                    m_param_hits++;
                    m_params_file.print(m_violation + "\t");
                    m_params_file.println(val);
                } else {
                    m_params_candidate_file.println(val);
                }

                m_param_violation_counters[m_violation]++;
            }
        }
    }

    public int handleEvent(Element e) {
        String type = e.getAttribute("event-type");
        if (!type.equalsIgnoreCase("http"))
            return 0;

        handleUrls(e);
        handleParameters(e);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {
        m_destination_folder = getProperty(PROPERTY_DESTINATION_FOLDER);
        if (m_destination_folder == null)
            m_destination_folder = ".";

        File folder = new File(m_destination_folder);
        if (!folder.exists() && !folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        File url_folder = new File(m_destination_folder + File.separator + "urls");
        if (!url_folder.exists() && !url_folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        File param_folder = new File(m_destination_folder + File.separator + "params");
        if (!param_folder.exists() && !param_folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        try {
            m_urls_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + "url-hits.txt"));
            m_urls_candidate_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + "url-candidates.txt"));
            m_params_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + "param-hits.txt"));
            m_params_candidate_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + "param-candidates.txt"));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create results file", ex);
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {
        m_urls_file.close();

        System.out.println("Total URL candidates: " + m_total_candidates);
        System.out.println("Total URL hits: " + m_total_hits);
        System.out.println("Hit breakdown by type:");

        for (int index = 1; index < 9; index++)
            System.out.println(index + ":\t" + m_violation_counters[index]);

        System.out.println("Total Parameters: " + m_total_params);
        System.out.println("Total Parameter Candidates: " + m_loose_candidates);
        System.out.println("Total Parameter Strong Candidates: " + m_strong_candidates);
        System.out.println("Total Parameter Hits: " + m_param_hits);
        System.out.println("Hit breakdown by type:");

        for (int index = 1; index < 10; index++)
            System.out.println(index + ":\t" + m_param_violation_counters[index]);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new pocDirectoryTraversal();
    }

    static public String getName() {
        return "Directory Traversal";
    }

    static public void main(String[] argv) {
        pocDirectoryTraversal proc = (pocDirectoryTraversal)getNewInstance(argv);



        proc.handleUrlCandidate("/scripts/..%5c../..%5c../..%5c../winnt/system32/cmd.exe", true, true);

        if (proc.m_violation != VIOLATION_NONE) {
            System.out.println("Violation:" + proc.m_violation);
        }
    }
}
