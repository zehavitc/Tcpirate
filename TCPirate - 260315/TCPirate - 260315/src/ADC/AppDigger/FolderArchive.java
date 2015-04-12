package ADC.AppDigger;

import org.w3c.dom.Element;

import java.io.File;
import java.util.LinkedList;

import ADC.Utils.XmlUtils;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 06/04/2005
 * Time: 09:37:17
 * To change this template use File | Settings | File Templates.
 */
public class FolderArchive implements EventSource {

    File m_root = null;
    File[] m_files = null;
    int m_current = 0;

    FolderArchive (String p_folder) {
        m_root = new File(p_folder);
        if (!m_root.exists() || !m_root.isDirectory())
            throw new RuntimeException("Invalid source");

        m_files = m_root.listFiles();
    }

    public Element getNextEvent() {
        if (m_current < m_files.length) {
            try {
                Element el = XmlUtils.getInstance().readXmlFIle(m_files[m_current].getAbsolutePath());
                m_current++;
                return el;
            } catch (Exception ex) {
                System.err.println("Failed to parse file " + m_files[m_current].getName());
                ex.printStackTrace();
                return null;
            }
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
