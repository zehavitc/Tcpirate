/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 27/07/2005
 * Time: 11:39:25
 * To change this template use File | Settings | File Templates.
 */
package ADC.TCPirate;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPirate {
    private static TCPirate ourInstance = null;

    public final static String DEFAULT_FILE_SUFFIX = ".bin";

    private String m_folder_name = null;
    private String m_file_suffix = null;

    private int m_current_file = 0;

    private String m_host_name = null;
    private int m_port = 0;

    private Socket m_sock = null;
    private OutputStream m_os = null;
    private TCPirateGUI m_dialog = null;
    private TCPirateRelay m_relay = null;

    private boolean m_relay_active = false;

    public static TCPirate getInstance() {
        if (ourInstance == null)
            ourInstance = new TCPirate();

        return ourInstance;
    }

    private void writeError(String error) {
        System.out.println(error);
    }
    public TCPirateRelay getTCPirateRelay()
    {
        return m_relay;
    }

    private void dumpFileToSocket(File p_file, OutputStream p_os) {
        byte[] buffer = new byte[1500];
        int data_size;

        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(p_file));

            data_size = is.read(buffer);

            while (data_size == buffer.length) {
                p_os.write(buffer);
                data_size = is.read(buffer);
            }

            if (data_size != -1)
                p_os.write(buffer, 0, data_size);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public synchronized void go() {
        try {

            File f;

            m_folder_name = m_dialog.getFolderName();
            m_file_suffix = m_dialog.getFileSuffix();

            m_host_name = m_dialog.getHostName();
            m_port = m_dialog.getPortNumber();

            f = new File(m_folder_name);
            if (!f.exists())
                if (!f.mkdir())
                    throw new RuntimeException("Cannot create output folder");

            m_relay = new TCPirateRelay(m_dialog.getRelayPortNumber(), m_dialog.getHostName(), m_dialog.getPortNumber(), m_dialog.getFolderName());
            m_relay.setTrapRequest(m_dialog.getTrapRequest());
            m_relay.setTrapResponse(m_dialog.getTrapResponse());
            m_relay.setUseDBAAService(m_dialog.getUseDBBService());
            m_relay.setDBAAAddress(m_dialog.getDBAAHostName());
            m_relay.setDBAAPort(m_dialog.getDBAAPort());
            m_relay.setDBAASideChannelID(m_dialog.getDBAASideChannelID());
            m_relay.setAutomaticProcessing(m_dialog.getAutomaticProcessing());
            m_relay.setAutomaticProcessingTable(m_dialog.getAutomaticProcessingTable());

            m_relay.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /*
    public synchronized void doNextFile() {
        String file_name = getFirstFileName();
        File file = new File(file_name);

        try {
            if (file.exists())
                // Send next step of the protocol
                dumpFileToSocket(file, m_os);
            else {
                // Terminate sequence
                writeError("Sequence terminated");
                m_Server_listener.interrupt();
                m_sock.close();
                m_os.close();
                //m_dialog.resetSession();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            m_Server_listener.interrupt();
            //m_dialog.resetSession();
            throw new RuntimeException(ex);
        }
    }
    */

    private TCPirate() {
    }

    public void newConnection() {
        File f = new File(getFolderName());
        if (!f.exists())
            if (!f.mkdir())
                throw new RuntimeException("Cannot create output folder");
                
        TCPirateConnection conn = new TCPirateConnection(this);
        conn.start();
    }

    public void stopPirate() {
    }

    private void run() {
//        JFrame frame = new JFrame("TCPirate by Imperva ADC");
//        frame.pack();
//        frame.setVisible(true);

        // JDialog.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        m_dialog = new TCPirateGUI();
        m_dialog.pack();
        m_dialog.initializeForm();
        m_dialog.setVisible(true);
    }

    public String getFolderName() {
        return m_dialog.getFolderName();
    }

    public String getFileSuffix() {
        return m_dialog.getFileSuffix();
    }

    public String getDestinationHost() {
        return m_dialog.getHostName();
    }

    public int getDestinationPort() {
        return m_dialog.getPortNumber();
    }

    public boolean getTrapRequest() {
        return m_dialog.getTrapRequest();
    }

    public boolean getTrapResponse() {
        return m_dialog.getTrapResponse();
    }


    public JFrame getGUI() {
        return m_dialog;
    }

    public static void main(String[] argv) {
        TCPirate obj = getInstance();
        obj.run();
        //System.exit(0);
    }
}
