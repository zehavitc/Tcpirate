package ADC.AppDigger;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 08/11/2004
 * Time: 14:21:54
 * To change this template use File | Settings | File Templates.
 */
public interface EventPreprocessor {
    public long prepareEvents(AppDigger ad, CompressedArchive ca, RelationalArchive ra, PrintStream p_output, PrintStream p_error);
}
