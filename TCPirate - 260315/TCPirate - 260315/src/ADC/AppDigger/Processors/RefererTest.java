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
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 10/04/2005
 * Time: 16:09:59
 * To change this template use File | Settings | File Templates.
 */
public class RefererTest extends EventProcessorImpl {

    class RefererCounter {
        long m_with_referer = 0;
        long m_no_referer = 0;
        String m_key;

        RefererCounter(String p_key) {
            m_key = p_key;
        }
    }

    private static final String PROPERTY_DESTINATION_FOLDER = "destination-folder";

    private String m_result_folder = null;
    private PrintStream m_result_file = null;

    private long m_no_referer_counter = 0;
    private Map m_urls = null;

    private String getElementValue(Element e) {
        Node n = e.getFirstChild();
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

        return val;
    }

    public int handleEvent(Element e) {
        NodeList nl = e.getElementsByTagName("header");
        NodeList nl1 = null;
        String name = null;
        boolean found = false;
        String response = null;
        int response_code;

        if (!e.getAttribute("event-type").equalsIgnoreCase("http"))
            return 0;

        String eid = e.getAttribute("id");
        String server_group = e.getAttribute("server-group-id");

        // Find a header field with the name "Referer"
        for (int index = 0; !found && (index < nl.getLength()); index++) {
            nl1 = ((Element)nl.item(index)).getElementsByTagName("name");
            if (nl1.getLength() > 0) {
                name = getElementValue((Element)(nl1.item(0)));
                if ((name != null) && name.equalsIgnoreCase("Referer")) {
                    found = true;
                }
            }
        }

        // If no referer than check the response code. Disregard any of the following
        // responses: 404, 500, 403, 400
        if (!found) {
            nl = e.getElementsByTagName("response-code");
            if (nl.getLength() > 0)
                response = getElementValue((Element)(nl.item(0)));
            else
                return 0;

            response_code = Integer.parseInt(response);
            if (((response_code == 404) || (response_code == 500)) || (response_code == 403) || (response_code == 400))
                return 0;

            m_no_referer_counter++;
//            XmlUtils.getInstance().writeXmlFile(e, m_result_folder + File.separator + "events" + File.separator + eid + ".xml");
        }

        String key = null;

        // Retrieve URL
        nl = e.getElementsByTagName("full-url");
        if (nl.getLength() > 0)
            key = server_group + "||" + getElementValue((Element)(nl.item(0)));

        // Retrieve Method
        nl = e.getElementsByTagName("method");
        if (nl.getLength() > 0)
            key = key + "||" + getElementValue((Element)(nl.item(0)));

        RefererCounter rc = (RefererCounter)m_urls.get(key);


        // Update counters for each URL.
        if (rc == null) {
            rc = new RefererCounter(key);
            m_urls.put(key, rc);
        }

        if (found)
            rc.m_with_referer++;
        else
            rc.m_no_referer++;

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {

        m_result_folder = getProperty(PROPERTY_DESTINATION_FOLDER);
        if (m_result_folder == null)
            m_result_folder = ".";

        File folder = new File(m_result_folder);
        if (!folder.exists() && !folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        folder = new File(m_result_folder + File.separator + "events");
        if (!folder.exists() && !folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        try {
            m_result_file = new PrintStream(new FileOutputStream(m_result_folder + File.separator + "referers.txt"));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create results file", ex);
        }

        m_urls = new HashMap(50000);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {
        Iterator it = m_urls.values().iterator();

        while (it.hasNext()) {
            RefererCounter rc = (RefererCounter)it.next();
            m_result_file.println(rc.m_with_referer + "\t" + rc.m_no_referer + "\t" + rc.m_key);
        }

        m_result_file.close();

        System.out.println("Events with no referer: " + m_no_referer_counter);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new RefererTest();
    }

    static public String getName() {
        return "Referer";
    }

}
