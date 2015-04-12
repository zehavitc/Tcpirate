package ADC.AppDigger;

import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 15/11/2004
 * Time: 11:03:27
 * To change this template use File | Settings | File Templates.
 */
public interface EventSource {
    public Element getNextEvent();
    public void close();
}
