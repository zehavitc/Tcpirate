package ADC.AppDigger;

import ADC.Utils.XmlUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 24/03/2005
 * Time: 09:29:58
 * To change this template use File | Settings | File Templates.
 */
public class TLArchive implements EventSource {

    private TarInputStream m_input_stream = null;
    private TarEntry m_te = null;
    private StringBuffer m_readBuffer = null;
    private InputStreamReader m_isr = null;

    public TLArchive(String p_filename) {
        try {
            FileInputStream fis = new FileInputStream(p_filename);
            BufferedInputStream bis = new BufferedInputStream(fis);

            int b = bis.read();

            if (b != 'B') {
                throw new RuntimeException("Not a valid file");
            }
            b = bis.read();
            if (b != 'Z') {
                throw new RuntimeException("Not a valid file");
            }


            CBZip2InputStream bzip_input_stream = new CBZip2InputStream(bis);
            m_input_stream = new TarInputStream(bzip_input_stream);
            m_readBuffer = new StringBuffer(10000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(this.getClass().getName() + ": Cannot open archive");
        }

    }

    public Element getNextEvent() {
        try {
            m_te = m_input_stream.getNextEntry();
        } catch (Exception ex) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to read from compressed archive", ex);
        }

        m_readBuffer.setLength(0);


        try {
            m_isr = new InputStreamReader(m_input_stream, "UTF8");

            int ch = m_isr.read();

            while (ch > 0) {
                m_readBuffer.append((char)ch);
                ch = m_isr.read();
            }
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to read from compressed archive", e);
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

    public void close() {
        try {
            m_input_stream.close();
        } catch (Exception ex) {
            throw new RuntimeException(this.getClass().getName() + ": Failed to close compressed archive", ex);
        }
    }

}
