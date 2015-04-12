package ADC.TCPirate;

import javax.swing.*;
import java.awt.event.*;

public class TCPirateRelayGUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JTextField textSourcePort;
    private JTextField textDestAddress;
    private JTextField textDestPort;
    private JCheckBox checkBoxTrapRequest;
    private JCheckBox checkBoxTrapResponse;
    private JCheckBox checkBoxDBAAService;
    private JTextField textDBAAAddress;
    private JTextField textDBAAPort;

    private TCPirateRelay m_relay;

    public TCPirateRelayGUI(TCPirateRelay relay) {
        super(TCPirate.getInstance().getGUI());
        
        setContentPane(contentPane);

        checkBoxTrapRequest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleTrapRequest();
            }
        });

        checkBoxTrapResponse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleTrapResponse();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        m_relay = relay;

        // Initialize display
        checkBoxTrapRequest.setSelected(m_relay.getTrapRequest());
        checkBoxTrapResponse.setSelected(m_relay.getTrapResponse());
        checkBoxDBAAService.setSelected(m_relay.getUseDBAAService());

        textSourcePort.setText(Integer.toString(m_relay.getSourcePort()));
        textDestAddress.setText(m_relay.getServerAddress());
        textDestPort.setText(Integer.toString(m_relay.getServerPort()));
        textDBAAAddress.setText(m_relay.getDBAAAddress());
        textDBAAPort.setText(Integer.toString(m_relay.getDBAAPort()));
    }

    private void onCancel() {
        // add your code here if necessary
        m_relay.killRelay();
        dispose();
    }

    private void handleTrapRequest() {
        m_relay.setTrapRequest(checkBoxTrapRequest.isSelected());
    }

    private void handleTrapResponse() {
        m_relay.setTrapResponse(checkBoxTrapResponse.isSelected());
    }

    public static void main(String[] args) {
    }
}
