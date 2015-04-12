package ADC.AppDigger;

import org.w3c.dom.Element;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 15/11/2004
 * Time: 09:13:15
 * To change this template use File | Settings | File Templates.
 */
public interface EventProcessor {
    public int handleEvent(Element e);
    public int handleStartSession(PrintStream p_output, PrintStream p_error);
    public int handleEndSession();
    public int handleOpenSource(String source_name);
    public int handleCloseSource(String source_name);

    public void setProperty(String name, String value);
    public String getProperty(String name);

}
