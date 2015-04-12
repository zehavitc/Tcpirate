package ADC.Utils;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 04/11/2004
 * Time: 17:56:29
 * To change this template use File | Settings | File Templates.
 */
public class XmlUtils {

    private static XmlUtils m_instance = null;
    private static DocumentBuilderFactory m_dbf = null;
    private static DocumentBuilder m_docBuilder = null;


    static public String xmlEncode(String xmlStr) {
        if (xmlStr == null) {
            return "";
        }

        char[] xmlBytes = xmlStr.toCharArray();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < xmlBytes.length; i++) {
            char ch = xmlBytes[i];
            switch (ch) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '\n':
                case '\r':
                case '\t':
                    sb.append(ch);
                    break;
                case '"':
                    sb.append("&#34;");
                    break;
                case 0:
                    sb.append("[[null]]");
                    break;
                default:
                    if (xmlBytes[i] >= 128) {
                        sb.append("&#");
                        sb.append((int)xmlBytes[i]);
                        sb.append(';');
                    } else if (xmlBytes[i] < 32) {
                        sb.append("[[");
                        sb.append((int)xmlBytes[i]);
                        sb.append("]]");
                    } else {
                        sb.append(ch);
                    }
             }
        }

        String res = sb.toString();
        return res;
    }

    // Initialize a document builder
    private XmlUtils() {
        try {
            m_dbf = (DocumentBuilderFactory)(Class.forName("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl").newInstance());
            m_docBuilder = m_dbf.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to initialize XML infrastructure", e);
        }
    }

    synchronized public static XmlUtils getInstance() {
        if (m_instance == null)
            m_instance = new XmlUtils();

        return m_instance;
    }

    // Create a DOM event from a string
    synchronized public Element stringToDOM(String s) {
        try {
            // System.out.println(s);
            Document doc = m_docBuilder.parse(new InputSource(new StringReader(s)));
            return doc.getDocumentElement();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(XmlUtils.class.getName() + ": Failed to create event from XML", e);
        }
    }

    synchronized public Document newDocument(String p_rootName) {
        Document doc = m_docBuilder.newDocument();
        Element el = doc.createElement(p_rootName);
        doc.appendChild(el);

        return doc;
    }

    // TO pretty printed XML String
    synchronized public String toXmlString(Element e) {
        StringBuffer sb = new StringBuffer();
        doXmlString(e, sb, "");
        return sb.toString();
    }

    // Create an independent element with a specified tag name and a value
    synchronized public Element createElementWithValue(Document doc, String tag_name, String value) {
        if ((doc == null) || (tag_name == null))
            return null;

        Element el = doc.createElement(tag_name);
        if (value != null) {
            Text tx = doc.createTextNode(value);
            el.appendChild(tx);
        }

        return el;
    }

    // Adds a new element with a specified tag name and a value to another element.
    // Returns the new element
    synchronized public Element appendElementWithValue(Element src, String tag_name, String value) {
        if ((src == null) || (tag_name == null))
            return null;

        Document doc = src.getOwnerDocument();
        Element new_el = createElementWithValue(doc, tag_name, value);
        src.appendChild(new_el);
        return new_el;
    }

    synchronized public Element appendElementWithValue(Element src, String tag_name) {
        return appendElementWithValue(src, tag_name, null);
    }

    private void doXmlString(Element e, StringBuffer sb,  String indent)
    {
        String new_indent = null;

        // sb.append(indent);
        sb.append('<');
        sb.append(e.getNodeName());

        // Add all attributes
        NamedNodeMap attrs = e.getAttributes();
        int num_of_attrs = attrs.getLength();
        for (int index = 0; index < num_of_attrs; index++) {
            Node n = attrs.item(index);
            sb.append(' ');
            sb.append(n.getNodeName());
            sb.append('=');
            sb.append('"');
            sb.append(xmlEncode(n.getNodeValue()));
            sb.append('"');
        }

        sb.append(">");

        // Serialize all child elements
        NodeList children = e.getChildNodes();
        int num_of_children = children.getLength();

        for (int index = 0; index < num_of_children; index++) {
            Node n = children.item(index);
            if (n instanceof Element) {
                doXmlString((Element)n, sb, new_indent);
            } else if (n instanceof Text)
                sb.append(xmlEncode(n.getNodeValue()));

        }

        sb.append("</");
        sb.append(e.getNodeName());
        sb.append('>');
    }

    // Retrieve an XML document from file
    synchronized public Element readXmlFile(String p_filename) {
        try {
            InputStreamReader fr = new InputStreamReader(new FileInputStream(p_filename), "UTF8");

            Document doc = m_docBuilder.parse(new InputSource(fr));
            fr.close();
            return doc.getDocumentElement();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(XmlUtils.class.getName() + ": Failed to create event from XML", ex);
        }
    }


    public Element readXmlFIle(String p_filename) {
        return readXmlFile(p_filename);
    }

    public void writeXmlFile(Element el, String p_filename) {
        if ((el == null) || (p_filename == null))
            return;

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(p_filename), true);
            ps.print(XmlUtils.getInstance().toXmlString(el));
            ps.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to write XML file");
            throw new RuntimeException(this.getClass().getName() + ": Failed to write XML file");
        }
    }
}
