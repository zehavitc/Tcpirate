package ADC.AppDigger.Processors;

import ADC.AppDigger.EventProcessorImpl;
import ADC.AppDigger.EventProcessor;
import ADC.Utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 28/03/2005
 * Time: 16:33:40
 * To change this template use File | Settings | File Templates.
 */

public class ClientFeatures extends EventProcessorImpl {

    class Counter {
        public long m_count_sessions = 1;
        public long m_count_requests = 1;

        public String toString() {
            return Long.toString(m_count_sessions) + "\t" + Long.toString(m_count_requests);
        }
    }

    private Map m_user_agent = new HashMap(5000);
    private Map m_accept = new HashMap(5000);
    private Map m_accept_language = new HashMap(5000);
    private Map m_combo = new HashMap(5000);
    private Map m_dual_combo = new HashMap(5000);

    private Collection m_sessions = new HashSet(10000);

    private long m_null_sessions = 0;

    private javax.xml.transform.Transformer m_trans = null;

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
        String type = e.getAttribute("event-type");
        if (!type.equalsIgnoreCase("http"))
            return 0;

        Source xmlSource = new javax.xml.transform.dom.DOMSource(e.getOwnerDocument());
        Document doc = XmlUtils.getInstance().newDocument("event");
        javax.xml.transform.Result result = new javax.xml.transform.dom.DOMResult(doc.getDocumentElement());
        try {
            m_trans.transform(xmlSource, result);
        } catch (TransformerException ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Element el = doc.getDocumentElement();

        NodeList nl = el.getElementsByTagName("session");
        String session = null;
        if (nl.getLength() > 0) {
            session = getElementValue((Element)nl.item(0));
            if (session == null) {
                nl = el.getElementsByTagName("source-ip");
                session = getElementValue((Element)nl.item(0));
                m_null_sessions++;
            }
        }
        else {
            nl = el.getElementsByTagName("source-ip");
            session = getElementValue((Element)nl.item(0));
            m_null_sessions++;
        }

        boolean new_session = !m_sessions.contains(session);
        if (new_session)
            m_sessions.add(session);

        nl = el.getElementsByTagName("user-agent");
        String user_agent = null;
        if (nl.getLength() > 0)
            user_agent = getElementValue((Element)(nl.item(0)));
        else
            user_agent = "<none>";

        Counter c = (Counter)(m_user_agent.get(user_agent));
        if (c == null) {
            m_user_agent.put(user_agent, new Counter());
        } else {
            if (new_session)
                c.m_count_sessions++;
            c.m_count_requests++;
        }

        nl = el.getElementsByTagName("accept");
        String accept = null;
        if (nl.getLength() > 0)
            accept = getElementValue((Element)(nl.item(0)));
        else
            accept = "<none>";

        c = (Counter)(m_accept.get(accept));
        if (c == null) {
            m_accept.put(accept, new Counter());
        } else {
            if (new_session)
                c.m_count_sessions++;
            c.m_count_requests++;
        }


        nl = el.getElementsByTagName("accept-language");
        String accept_language = null;
        if (nl.getLength() > 0)
            accept_language = getElementValue((Element)nl.item(0));
        else
            accept_language = "<none>";

        c = (Counter)(m_accept_language.get(accept_language));
        if (c == null) {
            m_accept_language.put(accept_language, new Counter());
        } else {
            if (new_session)
                c.m_count_sessions++;
            c.m_count_requests++;
        }

        c = (Counter)(m_combo.get(user_agent + "||" + accept + "||" + accept_language));
        if (c == null) {
            m_combo.put(user_agent + "||" + accept + "||" + accept_language, new Counter());
        } else {
            if (new_session)
                c.m_count_sessions++;
            c.m_count_requests++;
        }

        c = (Counter)(m_dual_combo.get(user_agent + "||" + accept_language));
        if (c == null) {
            m_dual_combo.put(user_agent + "||" + accept_language, new Counter());
        } else {
            if (new_session)
                c.m_count_sessions++;
            c.m_count_requests++;
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {
        javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource("client-features.xsl");
        javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance( );

        try {
            m_trans = transFact.newTransformer(xsltSource);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {
        System.out.println("Counted Sessions:" + m_sessions.size());
        System.out.println("Null Sessions:" + m_null_sessions);

        System.out.println("User Agents: " + m_user_agent.size());
        Iterator it = m_user_agent.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(s + "\t\t" + m_user_agent.get(s));
        }

        System.out.println("-----------------------------------------------------");

        System.out.println("Accept: " + m_accept.size());
        it = m_accept.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(s + "\t\t" + m_accept.get(s));
        }

        System.out.println("-----------------------------------------------------");

        System.out.println("Accept-Language: " + m_accept_language.size());
        it = m_accept_language.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(s + "\t\t" + m_accept_language.get(s));
        }

        System.out.println("-----------------------------------------------------");

        System.out.println("Combo: " + m_combo.size());
        it = m_combo.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(s + "\t\t" + m_combo.get(s));
        }

        System.out.println("-----------------------------------------------------");

        System.out.println("Dual Combo: " + m_dual_combo.size());
        it = m_dual_combo.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(s + "\t\t" + m_dual_combo.get(s));
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new ClientFeatures();
    }

    static public String getName() {
        return "Client Feature Statistics";
    }
}
