package ADC.AppDigger;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 08/11/2004
 * Time: 14:51:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class EventPreprocessorFactory {
    static public EventPreprocessor getNewInstance(String[] argv) {
        throw new RuntimeException("This is an abstract class");
    }

    static public String getName() {
        throw new RuntimeException("This is an abstract class");        
    }
}
