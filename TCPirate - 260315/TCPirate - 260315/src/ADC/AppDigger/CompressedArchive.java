package ADC.AppDigger;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 04/11/2004
 * Time: 13:05:28
 * To change this template use File | Settings | File Templates.
 */

import java.util.zip.*;
import java.io.*;
import java.nio.charset.Charset;

import org.w3c.dom.*;
import ADC.Utils.*;

public class CompressedArchive implements EventSource {

    public static final int ACCESS_READ = 0;
    public static final int ACCESS_WRITE = 1;
    public static final int ACCESS_APPEND = 2;

    private String m_filename = null;
    private int m_access_mode = 0;
    private ZipOutputStream m_output_stream = null;
    private ZipInputStream m_input_stream = null;
    private ZipEntry m_ze = null;
    private StringBuffer m_readBuffer = null;
    private InputStreamReader m_isr = null;
    private OutputStreamWriter m_osw = null;

    // Open a compressed event archive for reading or writing
    public CompressedArchive(String p_filename, int p_access_mode) {

        if (p_access_mode == ACCESS_READ) {
            try {
                m_input_stream = new ZipInputStream(new FileInputStream(p_filename));
                m_ze = m_input_stream.getNextEntry();
                m_readBuffer = new StringBuffer(10000);
                m_isr = new InputStreamReader(m_input_stream, "UTF8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (p_access_mode == ACCESS_WRITE) {
            try {
                m_output_stream = new ZipOutputStream(new FileOutputStream(p_filename));
                m_output_stream.setMethod(ZipOutputStream.DEFLATED);
                m_ze = new ZipEntry("Events");
                m_output_stream.putNextEntry(m_ze);
                m_osw = new OutputStreamWriter(m_output_stream, "UTF8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            throw new RuntimeException(this.getClass().getName() + ": Illegal file access mode");

        m_access_mode = p_access_mode;
    }

    // Methods for adding events to a compressed archive
    public int addEvent(Element e) {

        if (e != null)
            addEvent(XmlUtils.getInstance().toXmlString(e));
        return 0;
    }

    // Write a string representation of an event into the archive. The event is delimited by a null byte
    public int addEvent(String s) {

        if (m_access_mode == ACCESS_READ) {
            throw new RuntimeException(this.getClass().getName() + ": Compressed archive is not accessible for writing");
        }

        if (s == null)
            return 0;

        try {
            m_osw.write(s);
            m_osw.write(0);
            m_osw.flush();
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to write event into archive", e);
        }

        return 0;
    }

    public int addEvent(StringBuffer sb) {
        if (sb != null)
            addEvent(sb.toString());

        return 0;
    }

    // Retrieve the next event from a compressed archive or null
    public Element getNextEvent() {
        m_readBuffer.setLength(0);
        int ch;

        try {
            ch = m_isr.read();
            while (ch > 0) {
                m_readBuffer.append((char)ch);
                ch = m_isr.read();
            }
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to read from compressed archiver", e);
        }

        if (m_readBuffer.length() == 0)
            return null;

        try {
            return XmlUtils.getInstance().stringToDOM(m_readBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(m_readBuffer.toString());
            return null;
        }
    }

    // Finish processing of compressed archive
    public void close() {
        if ((m_access_mode == ACCESS_WRITE) || (m_access_mode == ACCESS_APPEND)) {
            try {
                m_output_stream.closeEntry();;
                m_output_stream.close();
            } catch (Exception e) {
                throw new RuntimeException(this.getClass().getName() + ": Failed to close compressed archive", e);
            }
        } else {
            try {
                m_input_stream.closeEntry();
                m_input_stream.close();
            } catch (Exception e) {
                throw new RuntimeException(this.getClass().getName() + ": Failed to close compressed archive", e);
            }
        }
    }

    public static void print(String archive_name, String destination_folder) {

        File dest_dir = null;

        if (archive_name == null)
            return;

        if (destination_folder != null) {
            dest_dir = new File(destination_folder);
            if (dest_dir.exists() && !dest_dir.isDirectory())
                throw new RuntimeException(CompressedArchive.class.getName() + ": Failed to create destination folder");

            if (!dest_dir.exists())
                if (!dest_dir.mkdir())
                    throw new RuntimeException(CompressedArchive.class.getName() + ": Failed to create destination folder");
        } else
            dest_dir = new File(".");

        CompressedArchive ca = new CompressedArchive(archive_name, ACCESS_READ);

        Element event = null;

        while ((event = ca.getNextEvent()) != null) {
            String eid = event.getAttribute(EventXmlTags.ATTR_EVENT_ID);

            try {
                PrintStream ps = new PrintStream(new FileOutputStream(new File(dest_dir, eid + ".xml")),true);
                ps.print(XmlUtils.getInstance().toXmlString(event));
                ps.close();
            } catch (Exception e) {
                throw new RuntimeException(CompressedArchive.class.getName() + ": Failed to write event");
            }
        }

        ca.close();
    }
}
