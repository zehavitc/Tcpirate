package ADC.TCPirate;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 31/07/2005
 * Time: 12:30:41
 * To change this template use File | Settings | File Templates.
 */
public class TCPirateServerListener extends Thread {
    private BufferedInputStream m_is = null;
    private TCPirateConnection m_connection = null;

    public TCPirateServerListener(TCPirateConnection pirate_connection, InputStream is) {
        m_connection = pirate_connection;

        this.setDaemon(true);

        try {
            m_is = new BufferedInputStream(is);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void run() {
        byte[] buffer = new byte[1500];

        int data_len = 0;

        try {

            data_len = m_is.read(buffer);

            while (data_len > -1) {
                m_connection.consumeServerData(buffer, data_len);
                data_len = m_is.read(buffer);
            }

            m_is.close();

            m_connection.writeError("ServerListener - Server Disconnected Gracefully");
            m_connection.serverDisconnected();
        } catch (Exception ex) {
            m_connection.writeError("ServerListener - Server Disconnected with Exception");
            m_connection.serverDisconnected();
        }
    }

}
