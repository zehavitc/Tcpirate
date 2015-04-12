/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/08/2005
 * Time: 10:28:25
 * To change this template use File | Settings | File Templates.
 */
package ADC.TCPirate;

import ADC.Misc.OracleLogin.ObfuscatePassword;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TCPirateOraclePlugin extends TCPiratePluginImpl {

    static final int ACTION_GENERATE_HASH = 1;
    static final int ACTION_INSERT_HASH = 2;

    byte[] m_hash = null;

    public void init(TCPirateConnection connection) {
        super.init(connection);

        m_hash = null;
    }

    public void handleEvent(int action_code, ActionEvent e) {
        switch (action_code) {
            case ACTION_GENERATE_HASH:
                generateHash();
                break;

            case ACTION_INSERT_HASH:
                insertHash();
                break;
        }
    }

    public void hook(JDialog dialog) {
        TCPirateConnectionGUI gui = (TCPirateConnectionGUI)dialog;

        gui.buttonOraGenHash.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateHash();
            }
        });

        gui.buttonOraInsertPwd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertHash();
            }
        });
    }

    private void generateHash() {
        TCPirateConnectionGUI dialog = (TCPirateConnectionGUI)m_connection.getGUI();

        String uname = dialog.textOraUserName.getText();
        String pwd = dialog.textOraPassword.getText();

        String offset_str = dialog.textOraKOffset.getText();
        String len_str = dialog.textOraKLen.getText();

        int offset;
        int len;

        try {
            offset = Integer.parseInt(offset_str);
        } catch (Exception ex) {
            m_connection.writeError("Incorrect number format for Oracle Key Offset: \"" + offset_str + "\"");
            return;
        }

        try {
            len = Integer.parseInt(len_str);
        } catch (Exception ex) {
            m_connection.writeError("Incorrect number format for Oracle Key Length: \"" + len_str + "\"");
            return;
        }

        byte[] buffer = m_connection.getBuffer().getData(offset, len);

        try {
            m_hash = ObfuscatePassword.obfuscate(uname, pwd, buffer);
        } catch (Exception ex) {
            ex.printStackTrace();
            m_connection.writeError("Hash generation failed - incorrect data");
            return;
        }

        m_connection.writeError("Oracle Hash generated for user \"" + uname + "\" password \"" + pwd);
    }

    private void insertHash() {
        TCPirateConnectionGUI dialog = (TCPirateConnectionGUI)m_connection.getGUI();

        String offset_str = dialog.textOraPOffset.getText();

        int offset;

        try {
            offset = Integer.parseInt(offset_str);
        } catch (Exception ex) {
            m_connection.writeError("Incorrect number format for Oracle Key Offset: \"" + offset_str + "\"");
            return;
        }

        m_connection.getBuffer().copyToBuffer(m_hash, offset);

        m_connection.getBuffer().fireTableDataChanged();

        m_connection.writeError("Oracle Hash Written to buffer");
    }
}
