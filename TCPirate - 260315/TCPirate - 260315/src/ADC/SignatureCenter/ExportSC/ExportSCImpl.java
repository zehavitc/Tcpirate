package ADC.SignatureCenter.ExportSC;

import ADC.Utils.XmlUtils;
import ADC.Utils.DBHelper;

import java.sql.*;
import java.util.*;
import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 27/12/2004
 * Time: 09:42:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class ExportSCImpl implements IExportSC {
    private static final String QUERY_XML = " FOR XML AUTO, ELEMENTS";
    private static final String QUERY_XML_EXPLICIT = " FOR XML EXPLICIT";

    private Map m_properties = new HashMap(10);

    protected StringBuffer createXMLStringFromRS(String tag_name, ResultSet rs) {
        StringBuffer sb = new StringBuffer(10000);
        sb.append("<" + tag_name + ">");
        try {
            while (rs.next()) {
                sb.append(rs.getString(1));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Failed to read data");
            throw new RuntimeException(e);
        }

        sb.append("</" + tag_name +">");

        return sb;
    }

    protected StringBuffer exportSimpleXML(String query, String tag_name) {
        return exportSimpleXML(query, tag_name, false);
    }

    protected StringBuffer exportSimpleXML(String query, String tag_name, boolean is_explicit) {
        DBHelper dbh = DBHelper.getInstance();

        String sql_query = null;

        if (is_explicit)
            sql_query = query + QUERY_XML_EXPLICIT;
        else
            sql_query = query + QUERY_XML;

        ResultSet rs = dbh.directSelect(sql_query);

        return createXMLStringFromRS(tag_name, rs);
    }

    protected String addXmlFile(String file_name) {
        Element el = XmlUtils.getInstance().readXmlFIle(file_name);
        return XmlUtils.getInstance().toXmlString(el);
    }

    public void setProperty(String name, String value) {
        if (name == null)
            throw new RuntimeException("Cannot set property with null name");

        // Object obj = m_properties.get(name);
        //if (obj == null)
            m_properties.put(name, value);
    }

    public String getProperty(String name) {
        if (name == null)
            throw new RuntimeException("Cannot retrieve property with null name");

        return (String)m_properties.get(name);
    }
}
