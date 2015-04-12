package ADC.SignatureCenter.GUI;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/03/2005
 * Time: 13:32:11
 * To change this template use File | Settings | File Templates.
 */
public class SigAttack {
    public String m_name;
    public int m_id;

    public SigAttack(int p_id, String p_name) {
        m_name = p_name;
        m_id = p_id;
    }

    public String toString() {
        return m_name;
    }
}
