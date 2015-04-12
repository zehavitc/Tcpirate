package ADC.TCPirate;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/08/2005
 * Time: 11:32:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class TCPiratePluginImpl implements ITCPiratePlugin {
    protected TCPirateConnection m_connection;

    public void init(TCPirateConnection connection) {
        m_connection = connection;
    }
}
