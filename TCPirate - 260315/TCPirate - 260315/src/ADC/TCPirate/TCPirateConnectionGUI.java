package ADC.TCPirate;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class TCPirateConnectionGUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonReconnect;
    private JButton buttonCancel;
    private JButton buttonToClient;
    private JButton buttonToServer;

    private TCPirateConnection m_connection;
    private JTextField textSourceIP;
    private JTextField textSourcePort;
    private JTextField textDestinationIP;
    private JTextField textDestinationPort;
    private JCheckBox checkBoxTrapRequest;
    private JCheckBox checkBoxTrapResponse;
    private JTable tableHexEditor;
    private JButton buttonLoadFile;
//    private JTextField textFileName;
    private JButton buttonChooseFile;
    private JTextField textTruncateAt;
    private JButton buttonTruncate;
    private JList listMessages;
    private JTabbedPane tabbedPane1;

    public JTextField textOraUserName;
    public JTextField textOraPassword;
    public JTextField textOraKOffset;
    public JTextField textOraKLen;
    public JButton buttonOraGenHash;
    public JTextField textOraPOffset;
    public JTextField textOraPLen;
    public JButton buttonOraInsertPwd;
    private JButton buttonDropMessage;
    private JComboBox comboFilename;

    private static final int DEFAULT_COMBO_SIZE = 20;
    public JTextField textTrapTTIcode;
    public JCheckBox checkBoxTrapTTIcode;
    private JTextField textDBAAIP;
    private JTextField textDBAAPort;
    private JCheckBox checkBoxDBAAService;

    public TCPirateConnectionGUI(TCPirateConnection connection) {
        super(TCPirate.getInstance().getGUI());

        m_connection = connection;

        setContentPane(contentPane);             
        
        listMessages.setModel(new DefaultListModel());
        listMessages.setVisibleRowCount(5);
        
        buttonReconnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_connection.reconnect();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonToClient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleToClient();
            }
        });

        buttonToServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleToServer();
            }
        });

        buttonDropMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleDropMessage();
            }
        });

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

        buttonChooseFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        buttonLoadFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });

        buttonTruncate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                truncateMessage();
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

        m_connection.m_oracle_plugin.hook(this);
        m_connection.m_trap_plugin.hook(this);
    }

    public void displayConnectionData() {
        textSourceIP.setText(m_connection.getSourceIP());
        textSourcePort.setText(Integer.toString(m_connection.getSourcePort()));
        textDestinationIP.setText(m_connection.getDestinationIP());
        textDestinationPort.setText(Integer.toString(m_connection.getDestinationPort()));
        textDBAAIP.setText(m_connection.getDBAAAddress());
        textDBAAPort.setText(Integer.toString(m_connection.getDBAAPort()));

        checkBoxTrapRequest.setSelected(m_connection.getTrapRequest());
        checkBoxTrapResponse.setSelected(m_connection.getTrapResponse());
        checkBoxDBAAService.setSelected(m_connection.getUseDBAAService());
    }

    public void initializeHexEditor(HexEditorTableModel table_model) {
        tableHexEditor.setModel(table_model);
    }

    public void setHexEditorBehavior() {
        tableHexEditor.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableHexEditor.getColumnModel().getColumn(0).setMinWidth(50);

        for (int index = 1; index <= (tableHexEditor.getColumnCount() - 2) / 2; index++) {
            tableHexEditor.getColumnModel().getColumn(index).setPreferredWidth(25);
            tableHexEditor.getColumnModel().getColumn(index).setMinWidth(25);
            tableHexEditor.getColumnModel().getColumn(index).setMaxWidth(25);
        }

        for (int index = (tableHexEditor.getColumnCount() - 2) / 2 + 2; index < tableHexEditor.getColumnCount(); index++) {
            tableHexEditor.getColumnModel().getColumn(index).setPreferredWidth(15);
            tableHexEditor.getColumnModel().getColumn(index).setMinWidth(15);
            tableHexEditor.getColumnModel().getColumn(index).setMaxWidth(15);
        }

        tableHexEditor.setAutoscrolls(true);
    }

    private void onCancel() {
        // add your code here if necessary
        m_connection.cleanUp();
        dispose();
    }

    private void handleToServer() {
        m_connection.relayToDestination();
    }

    private void handleToClient() {
        m_connection.listenerToRelay();
    }

    private void handleDropMessage() {
        m_connection.dropMessage();
    }

    public void setToClientStatus(boolean enabled) {
        buttonToClient.setEnabled(enabled);
        buttonDropMessage.setEnabled(enabled);
    }

    public void setToServerStatus(boolean enabled) {
        buttonToServer.setEnabled(enabled);
        buttonDropMessage.setEnabled(enabled);
    }

    public void setConnected(boolean is_connected) {
        buttonCancel.setEnabled(is_connected);
        buttonReconnect.setEnabled(!is_connected);
    }

    public void handleTrapRequest() {
        m_connection.setTrapRequest(checkBoxTrapRequest.isSelected());
    }

    public void handleTrapResponse() {
        m_connection.setTrapResponse(checkBoxTrapResponse.isSelected());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();

        String old_name = (String)comboFilename.getSelectedItem();

        chooser.setDialogTitle("Choose message to load");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if ((old_name != null) && (old_name.length() > 0))
            chooser.setCurrentDirectory(new File(old_name));
        else
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir",".")));

        int returnVal = chooser.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String fname = chooser.getSelectedFile().getAbsolutePath();
            comboFilename.setSelectedItem(fname);
        }
    }

    private void updateFileList(String fname) {

        // Look for the filename in the list
        DefaultComboBoxModel model = (DefaultComboBoxModel)comboFilename.getModel();
        int index = model.getIndexOf(fname);

        // If file name is already in the list then do nothing
        if (index >= 0)
            return;

        // Add the file name to the list
        model.addElement(fname);

        // If list is too big loose one element
        if (model.getSize() > DEFAULT_COMBO_SIZE)
            model.removeElementAt(0);
    }

    private void loadFile() {
        String fname = (String)comboFilename.getSelectedItem();
        if (comboFilename.getSelectedIndex() == -1)
            updateFileList(fname);

        if ((fname != null) && (fname.length() > 0)) {
            m_connection.loadFile(fname);
            checkBoxTrapResponse.setSelected(true);
            m_connection.setTrapResponse(true);
        }
    }

    private void truncateMessage() {
        String tmp = textTruncateAt.getText();

        if ((tmp == null) || (tmp.length() == 0))
            return;

        int size = 0;
        try {
            size = Integer.parseInt(tmp, 16);
        } catch (Exception ex) {
            return;
        }

        m_connection.doTruncate(size);
    }

    public void writeMessage(String s) {
        DefaultListModel model = (DefaultListModel)listMessages.getModel();
        model.addElement(s);
        int size = model.getSize();
        listMessages.ensureIndexIsVisible(size - 1);
    }
}
