package ADC.TCPirate;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 01/08/2005
 * Time: 14:56:55
 * To change this template use File | Settings | File Templates.
 */
public interface DataConsumer {
    public void consumeData(byte[] buffer, int len);
}
