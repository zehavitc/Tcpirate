package ADC.SignatureCenter.GUI;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/03/2005
 * Time: 10:10:40
 * To change this template use File | Settings | File Templates.
 */
public class NetworkService {
    private String m_name = null;
    private String m_protocol = null;
    private int m_id = 0;

    NetworkService(String p_name, int p_protocol, int p_id) {
        m_id = p_id;
        m_name = p_name;
        if (p_protocol == 1)
            m_protocol = "TCP";
        else
            m_protocol = "UDP";
    }

    public String toString() {
        return m_name + " - " + m_protocol;
    }

    public int getId() {
        return m_id;
    }
}
