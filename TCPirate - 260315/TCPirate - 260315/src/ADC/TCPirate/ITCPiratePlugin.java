package ADC.TCPirate;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/08/2005
 * Time: 09:50:10
 * To change this template use File | Settings | File Templates.
 */
public interface ITCPiratePlugin {
    void handleEvent(int action_code, ActionEvent e);

    void init(TCPirateConnection connection);

    void hook(JDialog dialog);
}
