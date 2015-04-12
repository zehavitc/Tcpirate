package ADC.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 25/11/2004
 * Time: 09:46:21
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineOptionDef {
    private String m_key;
    private int m_id;

    public CommandLineOptionDef(String key, int id) {
        m_key = key;
        m_id = id;
    }

    public String key() {
        return m_key;
    }

    public int id() {
        return m_id;
    }
}
