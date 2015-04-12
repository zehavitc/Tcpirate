package ADC.TCPirate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 24/01/2006
 * Time: 13:32:34
 * To change this template use File | Settings | File Templates.
 */
public class TCPirateTrapPlugin extends TCPiratePluginImpl {

    private static final int TRAP_MODE_NONE = 0;
    private static final int TRAP_MODE_ORACLE_TTI_FUNC = 1;

    public static final int TRAP_DIR_TO_CLIENT = 0;
    public static final int TRAP_DIR_TO_SERVER = 1;

    private int m_trap_mode = TRAP_MODE_NONE;

    private byte m_tti_func_trap = 0;

    public void init(TCPirateConnection connection) {
        super.init(connection);
    }

    public void handleEvent(int action_code, ActionEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void hook(JDialog dialog) {
        TCPirateConnectionGUI gui = (TCPirateConnectionGUI)dialog;

        gui.checkBoxTrapTTIcode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleChangeTrapMode(TRAP_MODE_ORACLE_TTI_FUNC);
            }
        });
    }

    public boolean shouldTrap(byte[] buffer, int trap_dir) {
        switch (m_trap_mode) {
            case TRAP_MODE_NONE:
                return false;
            case TRAP_MODE_ORACLE_TTI_FUNC:
                if (trap_dir == TRAP_DIR_TO_SERVER)
                    return isOracleTTITrap(buffer);
                else
                    return false;
            default:
                return false;
        }
    }

    private void handleChangeTrapMode(int new_mode) {
        TCPirateConnectionGUI dialog = (TCPirateConnectionGUI)m_connection.getGUI();

        switch (new_mode) {
            case TRAP_MODE_ORACLE_TTI_FUNC:
                if (dialog.checkBoxTrapTTIcode.isSelected()) {
                    m_trap_mode = new_mode;
                } else
                    m_trap_mode = TRAP_MODE_NONE;
                break;
        }
    }

    private boolean isOracleTTITrap(byte[] buffer) {
        TCPirateConnectionGUI dialog = (TCPirateConnectionGUI)m_connection.getGUI();
        try {
            m_tti_func_trap = Byte.parseByte(dialog.textTrapTTIcode.getText(), 16);
        } catch (Exception ex) {
            m_tti_func_trap = 0;
        }

        if ((buffer[11] == m_tti_func_trap) && (buffer[10] == 3))
            return true;
        else
            return false;
    }
}