/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/03/2005
 * Time: 09:42:29
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter.GUI;

public class SigLocation {

    public static final int LOCATION_STREAM = 1;
    public static final int LOCATION_URL = 2;
    public static final int LOCATION_PARAMETERS = 4;
    public static final int LOCATION_HEADERS = 8;
    public static final int LOCATION_QUERY = 16;
    public static final int LOCATION_PARSEDQUERY = 32;
    public static final int LOCATION_ORIGINALURL = 64;
    public static final int LOCATION_PARAMANDURL = 128;
    public static final int LOCATION_RESPONSECONTENT = 256;

    public static final int LOCATIONS_HTTP = 0x01CE;
    public static final int LOCATIONS_SQL = 0x20;

    public static final SigLocation Stream = new SigLocation(LOCATION_STREAM);
    public static final SigLocation URL = new SigLocation(LOCATION_URL);
    public static final SigLocation Parameters = new SigLocation(LOCATION_PARAMETERS);
    public static final SigLocation Headers = new SigLocation(LOCATION_HEADERS);
    public static final SigLocation Query = new SigLocation(LOCATION_QUERY);
    public static final SigLocation ParsedQuery = new SigLocation(LOCATION_PARSEDQUERY);
    public static final SigLocation OriginalURL = new SigLocation(LOCATION_ORIGINALURL);
    public static final SigLocation ParametersAndURL = new SigLocation(LOCATION_PARAMANDURL);
    public static final SigLocation ResponseContent = new SigLocation(LOCATION_RESPONSECONTENT);

    private final int m_mask; // for debug only

    private SigLocation(int mask) {
        m_mask = mask;
    }

    public int mask() {
        return m_mask;
    }
    
    public String toString() {
        return Integer.toString(m_mask);
    }
}
