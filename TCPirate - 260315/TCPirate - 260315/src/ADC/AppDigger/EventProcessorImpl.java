package ADC.AppDigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 24/03/2005
 * Time: 14:35:18
 * To change this template use File | Settings | File Templates.
 */
public abstract class EventProcessorImpl extends EventProcessorFactory implements EventProcessor {

    protected Map m_properties = new HashMap(10);

    public void setProperty(String name, String value) {
        if (name == null)
            throw new RuntimeException("Cannot set property with null name");

        m_properties.put(name, value);
    }

    public String getProperty(String name) {
        if (name == null)
            throw new RuntimeException("Cannot retrieve property with null name");

        return (String)m_properties.get(name);
    }
}
