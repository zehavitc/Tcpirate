package ADC.AppDigger;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 15/11/2004
 * Time: 09:12:16
 * To change this template use File | Settings | File Templates.
 */
public class EventProcessorFactory {
    static public EventProcessor getNewInstance(String[] argv) {
        throw new RuntimeException("This is an abstract class");
    }

    static public String getName() {
        throw new RuntimeException("This is an abstract class");
    }
}
