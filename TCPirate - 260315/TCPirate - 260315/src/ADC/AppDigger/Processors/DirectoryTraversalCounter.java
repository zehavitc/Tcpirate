package ADC.AppDigger.Processors;

import ADC.AppDigger.EventProcessor;
import ADC.AppDigger.EventProcessorFactory;
import ADC.AppDigger.EventProcessorImpl;
import ADC.Utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.PrintStream;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 24/03/2005
 * Time: 13:32:12
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryTraversalCounter extends EventProcessorImpl {

    private static final String PROPERTY_DESTINATION_FOLDER = "destination-folder";
    private static final String PROPERTY_URLS_FILE = "urls-file";
    private static final String PROPERTY_PARAMS_FILE = "params-file";

    private static final String DEFAULT_URL_FILE = "dturls.txt";
    private static final String DEFAULT_PARAMS_FILE = "dtparams.txt";

    private long m_suspicious_urls = 0;
    private long m_suspicious_params = 0;
    private String m_destination_folder = null;
    private PrintStream m_urls_file = null;
    private PrintStream m_params_file = null;

    public int handleEvent(Element e) {
        String type = e.getAttribute("event-type");
        if (!type.equalsIgnoreCase("http"))
            return 0;

        String eid = e.getAttribute("id");

        NodeList nl = e.getElementsByTagName("full-url");

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

        if (url != null) {
            if ((url.indexOf("..") != -1) || (url.indexOf("//") != -1) ||
                    (url.indexOf("./") != -1) || (url.indexOf("\\") != -1) ||
                    (url.indexOf("%25") != -1)) {
                m_urls_file.println(url);
                XmlUtils.getInstance().writeXmlFile(e, m_destination_folder + File.separator + "urls" + File.separator + eid + ".xml");
                m_suspicious_urls++;
            }
        }

        nl = e.getElementsByTagName("param-item");

        boolean param_flag = false;

        for (int index = 0; index < nl.getLength(); index++) {
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

            if ((val != null) && (
                    (val.indexOf("../") != -1) || (val.indexOf("..\\") != -1) ||
                    (val.indexOf(":\\") != -1))) {
                m_params_file.println(val);
                param_flag = true;
                m_suspicious_params++;
            }
        }

        if (param_flag)
            XmlUtils.getInstance().writeXmlFile(e, m_destination_folder + File.separator + "params" + File.separator + eid + ".xml");

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

        String url_file = getProperty(PROPERTY_URLS_FILE);
        if (url_file == null)
            url_file = DEFAULT_URL_FILE;

        try {
            m_urls_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + url_file));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create results file", ex);
        }

        String param_file = getProperty(PROPERTY_PARAMS_FILE);
        if (param_file == null)
            param_file = DEFAULT_PARAMS_FILE;

        try {
            m_params_file = new PrintStream(new FileOutputStream(m_destination_folder + File.separator + param_file));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create results file", ex);
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {

        m_urls_file.close();
        m_params_file.close();

        System.out.println("Total Suspicious URLS: " + m_suspicious_urls);
        System.out.println("Total Suspicious PARAMS: " + m_suspicious_params);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new DirectoryTraversalCounter();
    }

    static public String getName() {
        return "Directory Traversal";
    }

}
