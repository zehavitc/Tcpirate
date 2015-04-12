package ADC.TCPirate;

import ADC.Utils.JexepackUtils;
import org.ini4j.Ini;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class TCPirateGUI extends JFrame {

    private static final String FILENAME_TCPIRATE_LOGO = "Pirate-Flag-32.gif";
    
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFolderName;
    private JTextField textFileSuffix;
    private JTextField textHostName;
    private JTextField textPortNumber;

    private JButton buttonFolder;
    private JTextField textRelayPort;

    private boolean m_in_progress = false;

    private TCPirate m_pirate;
    private JCheckBox checkBoxTrapRequest;
    private JCheckBox checkBoxTrapResponse;
    private JButton buttonNewConnection;
    private JCheckBox checkBoxDBAAService;
    private JTextField textDBAAHostName;
    private JTextField textDBAAPort;
    private JTextField textDBAASideChannel;

    public TCPirateGUI() {
        super();

        m_pirate = TCPirate.getInstance();

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        setTitle("TCPirate - Interactive TCP Relay");

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(false);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon scuba_logo = new ImageIcon(JexepackUtils.resDir("resource") + File.separator + FILENAME_TCPIRATE_LOGO);
        this.setIconImage(scuba_logo.getImage());

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        buttonNewConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pirate.newConnection();
            }
        });

        checkBoxDBAAService.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchDBAAFieldsState();
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
    }

    private void switchDBAAFieldsState() {
        if (textDBAAHostName.isEnabled() || textDBAAPort.isEnabled()) {
            textDBAAHostName.setEnabled(false);
            textDBAAPort.setEnabled(false);
            textDBAASideChannel.setEnabled(false);
        }
        else {
            textDBAAHostName.setEnabled(true);
            textDBAAPort.setEnabled(true);
            textDBAASideChannel.setEnabled(true);
        }
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        chooser.setDialogTitle("Choose trace folder");
        if ((textFolderName.getText() != null) && (textFolderName.getText().length() > 0))
            chooser.setCurrentDirectory(new File(textFolderName.getText()));
        else
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir",".")));

        int returnVal = chooser.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION)
            textFolderName.setText(chooser.getSelectedFile().getAbsolutePath());
    }

    private boolean isFormValid() { //TODO: Add validity checks for DBAA fields
        String host = textHostName.getText();
        String port = textPortNumber.getText();
        String folder = textFolderName.getText();
        String file_suffix = textFileSuffix.getText();
        String relay_port = textRelayPort.getText();

        StringBuffer error = new StringBuffer(1000);

        if ((host == null) || (host.length() == 0))
            error.append("Missing Host Name\n");
        if ((port == null) || (port.length() == 0))
            error.append("Missing Port Number\n");
        else
            try {
                int port_number = Integer.parseInt(port);
                if (port_number < 1)
                    error.append("Port number out of range\n");
            } catch (Exception ex) {
                error.append("Port number must be a number\n");
            }

        if ((relay_port == null) || (relay_port.length() == 0))
                error.append("Missing Relay Port Number\n");
        else
            try {
                int relay_port_number = Integer.parseInt(relay_port);
                if (relay_port_number < 1)
                    error.append("Relay port number out of range\n");
            } catch (Exception ex) {
                error.append("Relay port number must be a number\n");
            }

        if (error.length() > 0) {
            JOptionPane.showMessageDialog(this, error, "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } else
            return true;
    }

    private void startProgress() {
        if (!isFormValid())
            return;

        m_pirate.go();
    }


    // Same button used for starting a session and progressing
    private void onOK() {
        startProgress();
    }

    public void initializeForm() {
        File f = null, configFile = null;
        ConnectionProps props = new ConnectionProps();
        try
        {
         configFile = new File("TCPirate.ini");
        }
        catch(Exception e)
        {

        }
        if(configFile != null && configFile.exists())
         {
             try
             {
                props.setParamsFromIni(new Ini(configFile));
             }catch(Exception e)
             {
                props.resetToDefault();
             }
          }
         if(!props.validateIni())
         {
             props.resetToDefault();
         }
        textFolderName.setText(props.DestFolder);
        textPortNumber.setText(props.destPort);
        textRelayPort.setText(props.relayPort);
        textHostName.setText(props.host);
        checkBoxTrapRequest.setSelected(props.trapRequest);
        checkBoxTrapResponse.setSelected(props.trapResponse);

        textFileSuffix.setText(TCPirate.DEFAULT_FILE_SUFFIX);


    }

    private void onCancel() {
        m_pirate.stopPirate();

        System.exit(0);
    }

    // Publish information from the form.
    public String getFolderName() {
        return textFolderName.getText();
    }

    public String getFileSuffix() {
        return textFileSuffix.getText();
    }

    public String getHostName() {
        return textHostName.getText();
    }

    public int getPortNumber() {
        return Integer.parseInt(textPortNumber.getText());
    }

    public int getRelayPortNumber() {
        return Integer.parseInt(textRelayPort.getText());
    }

    public boolean getTrapRequest() {
        return checkBoxTrapRequest.isSelected();
    }

    public boolean getTrapResponse() {
        return checkBoxTrapResponse.isSelected();
    }

    /////// DBAA server connection information ///////////
    public String getDBAAHostName() {
        return textDBAAHostName.getText();
    }

    public int getDBAAPort() {
        return Integer.parseInt(textDBAAPort.getText());
    }

    public boolean getUseDBBService() {
        return checkBoxDBAAService.isSelected();
    }

    public String getDBAASideChannelID() {
        return textDBAASideChannel.getText();
    }

    ///////////////////////////////////////////

    public static void main(String[] args) {
        //TCPirateGUI dialog = new TCPirateGUI();
        //dialog.pack();
        System.exit(0);
    }


}