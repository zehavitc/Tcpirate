package ADC.AppDigger.Processors;

import ADC.AppDigger.EventProcessorImpl;
import ADC.AppDigger.EventProcessor;
import ADC.Utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import java.io.PrintStream;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 29/03/2005
 * Time: 10:26:03
 * To change this template use File | Settings | File Templates.
 */
public class SearchEvents extends EventProcessorImpl {

    public final static String PROPERTY_SEARCH_TEMPLATE = "template";
    public final static String PROPERTY_OUTPUT_FOLDER = "output-folder";

    private final static String DEFAULT_TEMPLATE = "search.xsl";
    private final static String DEFAULT_OUTPUT_FOLDER = "results";

    private javax.xml.transform.Transformer m_trans = null;
    private String m_search_template = null;
    private String m_output_folder = null;
    private long m_matches = 0;

    public int handleEvent(Element e) {
        Source xmlSource = new javax.xml.transform.dom.DOMSource(e.getOwnerDocument());
        javax.xml.transform.Result result = new javax.xml.transform.dom.DOMResult();

        try {
            m_trans.transform(xmlSource, result);
        } catch (TransformerException ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(ex);
        }

        Node n = ((DOMResult)result).getNode();
        Element el = ((Document)n).getDocumentElement();

        if (el != null) {
            String eid = e.getAttribute("id");
            XmlUtils.getInstance().writeXmlFile(el, m_output_folder + File.separator + eid + ".xml");
            m_matches++;
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {
        m_search_template = getProperty(PROPERTY_SEARCH_TEMPLATE);
        if (m_search_template == null)
            m_search_template = DEFAULT_TEMPLATE;

        javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(m_search_template);
        javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance( );

        try {
            m_trans = transFact.newTransformer(xsltSource);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }

        m_output_folder = getProperty(PROPERTY_OUTPUT_FOLDER);
        if (m_output_folder == null)
            m_output_folder = DEFAULT_OUTPUT_FOLDER;

        File folder = new File(m_output_folder);
        if (!folder.exists() && !folder.mkdir())
            throw new RuntimeException("Cannot create destination folder");

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {

        System.out.println("Total Matches: " + m_matches);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new SearchEvents();
    }

    static public String getName() {
        return "XPath Search";
    }
}
