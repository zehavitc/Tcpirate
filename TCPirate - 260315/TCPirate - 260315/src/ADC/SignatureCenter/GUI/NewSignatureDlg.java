package ADC.SignatureCenter.GUI;

import ADC.Utils.DBHelper;
import ADC.SignatureCenter.ValidateSyntax;
import ADC.SignatureCenter.SSVersion;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;

public class NewSignatureDlg extends JFrame {

    private static final String QUERY_SERVICES = "select id, displayname, protocolid from tblService order by displayname, protocolid";
    private static final String QUERY_ATTACKS = "select attack_id, attack_name from tblAttacks order by attack_name";
    private static final String QUERY_MAX_ID = "select max(sig_id) from tblSignatures";
    private static final String QUERY_ADD_SIGNATURE = "insert into tblSignatures (" +
            "sig_id, attack_id, sig_attacksuperclass, name, sig_signature, sig_isDecoded, sig_caseSensitive" +
            ", sig_dirClient2Server, sig_dirServer2Client, sig_accuracy, sig_source_key, sig_location" +
            ", sig_frequency, sig_datepublished, sig_dateupdated, sig_deleted, sig_goes2product, sig_istesting" +
            ", sig_iscustomerspecific, sig_minver, sig_maxver, sig_isregexp, sig_updatedby, sig_source_revision) values " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, getdate(), getdate(), 0, 1, 0, 0, ?, ?, 0, 'N/A', '0')";
    private static final String QUERY_ADD_SIGNATURE_ARUBA = "insert into tblSignatures (" +
            "sig_id, attack_id, sig_attacksuperclass, name, sig_signature, sig_isDecoded, sig_caseSensitive" +
            ", sig_dirClient2Server, sig_dirServer2Client, sig_accuracy, sig_source_key, sig_location" +
            ", sig_frequency, sig_datepublished, sig_dateupdated, sig_deleted, sig_goes2product, sig_istesting" +
            ", sig_iscustomerspecific, sig_minver, sig_maxver, sig_isapplayer, sig_isurl, sig_isparam, sig_israw, sig_isbinary, sig_isregexp, sig_updatedby, sig_source_revision) values " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, getdate(), getdate(), 0, 1, 0, 0, ?, ?, 0, 0, 0, 1, 0, 0, 'N/A', '0')";
    private static final String QUERY_ADD_SERVICE = "insert into tblSignatureService (signatureid, serviceid, pk) values (?, ?, 1)";
    private static final String QUERY_DEL_SIGNATURE = "delete from tblSignatures where sig_id = ?";
    private static final String QUERY_DEL_SERVICES = "delete from tblSignatureService where signatureid = ?";
    private static final String QUERY_SINGLE_SIGNATURE = "select * from tblSignatures where sig_id = ?";
    private static final String QUERY_SERVICES_FOR_SIGNATURE = "select serviceid from tblSignatureService where signatureid = ? order by serviceid";
    private static final String QUERY_SET_DELETED = "update tblSignatures set sig_deleted = ? where sig_id = ?";

    private static final int FORM_STATUS_NEW = 0;
    private static final int FORM_STATUS_UPDATE = 1;

    private static final int VERSION_IBIZA = 350;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textSigId;
    private JTextArea textAreaPattern;
    private JCheckBox checkBoxLocStream;
    private JCheckBox checkBoxLocParams;
    private JCheckBox checkBoxLocHeaders;
    private JCheckBox checkBoxLocQuery;
    private JCheckBox checkBoxLocParsedQuery;
    private JCheckBox checkBoxCaseSensitive;
    private JCheckBox checkBoxDecoded;
    private JComboBox comboAccuracy;
    private JRadioButton radioButtonDir2Server;
    private JRadioButton radioButtonDir2Client;
    private JTextArea textAreaName;
    private JComboBox comboBoxAttack;
    private JList listAvailable;
    private JList listSelected;
    private JCheckBox checkBoxLocURL;
    private JButton buttonToAvailable;
    private JButton buttonToSelected;
    private JButton buttonReset;
    private JTextField textSnortID;
    private JButton buttonCopy;
    private JButton buttonSearch;
    private JList listSideSignatures;
    private JTextArea textAreaSQL;
    private JButton buttonSQL;
    private JLabel labelArubaMode;
    private JCheckBox checkBoxLocNURL;
    private JCheckBox checkBoxLocParamURL;
    private JCheckBox checkBoxLocResponse;
    private JButton buttonDelete;
    private JLabel labelModeFP;

    private int m_minver;
    private int m_maxver;
    private int m_defaultver;
    private boolean m_deleted;

    private LinkedList m_available_services = null;
    private Vector m_attacks = null;
    private HashSet m_http_services = null;
    private HashSet m_sql_services = null;
    private boolean m_search_mode = false;
    private boolean m_mode_aruba = false;
    private boolean m_mode_fp = false;

    private int m_form_status = FORM_STATUS_NEW;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean intersectServices(DefaultListModel lm, Collection services) {
        boolean found = false;

        if (lm == null)
            return false;

        for (int index = 0; (index < lm.size()) && !found; index++) {
            NetworkService ns = (NetworkService)lm.get(index);
            if (services.contains(new Integer(ns.getId())))
                found = true;
        }

        return found;
    }

    private boolean allServicesIn(DefaultListModel lm, Collection services) {
        boolean bad_service = false;

        if (lm == null)
            return true;

        for (int index = 0; (index < lm.size()) && !bad_service; index++) {
            NetworkService ns = (NetworkService)lm.get(index);
            if (!services.contains(new Integer(ns.getId())))
                bad_service = true;
        }

        return !bad_service;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Form data preparation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Load Application Services
    private void loadApplicationServices() {
        m_http_services = new HashSet(10);
        m_http_services.add(new Integer(1));
        m_http_services.add(new Integer(2));

        m_sql_services = new HashSet(10);
        m_sql_services.add(new Integer(3));
        m_sql_services.add(new Integer(4));
        m_sql_services.add(new Integer(263));
        m_sql_services.add(new Integer(264));
    }

    // Fill values into the accuracy combo box
    private void fillAccuracy() {
        comboAccuracy.addItem("Low");
        comboAccuracy.addItem("Medium");
        comboAccuracy.addItem("High");
    }

    private void loadAvailableServices() {
        m_available_services = new LinkedList();

        ResultSet rs = DBHelper.getInstance().directSelect(QUERY_SERVICES);

        try {
            while (rs.next()) {
                m_available_services.add(new NetworkService(rs.getString("displayname"), rs.getInt("protocolid"), rs.getInt("id")));
            }

            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to retrieve list of services", ex);
        }
    }

    private void fillAvailableServices() {
        DefaultListModel lm = new DefaultListModel();

        Iterator it = m_available_services.iterator();

        while (it.hasNext()) {
            lm.addElement(it.next());
        }

        listAvailable.setModel(lm);
    }

    private void prepareSelectedServices() {
        listSelected.setModel(new DefaultListModel());
    }

    // Get the list of attacks from the database
    private void loadAttacks() {
        m_attacks = new Vector(3000);

        ResultSet rs = DBHelper.getInstance().directSelect(QUERY_ATTACKS);

        try {
            while (rs.next()) {
                m_attacks.add(new SigAttack(rs.getInt("attack_id"), rs.getString("attack_name")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load attacks", ex);
        }
    }

    // Create a display list of all attacks
    private void fillAttacks() {
        Iterator it = m_attacks.iterator();

        while (it.hasNext())
            comboBoxAttack.addItem(it.next());
    }

    // Generate an automatic signature identifier.
    private void setAutoID() {
        long new_id;

        ResultSet rs = DBHelper.getInstance().directSelect(QUERY_MAX_ID);

        try {
            if (rs.next())
                textSigId.setText(Long.toString(rs.getLong(1) + 1));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private void resetSideSignatures() {
        listSideSignatures.removeAll();
        listSideSignatures.setModel(new DefaultListModel());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GUI Handling
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setLocationControls(int location) {
        if (m_mode_fp || m_mode_aruba)  {
            // In the special mode, restrict the location controls
            checkBoxLocStream.setEnabled(false);
            checkBoxLocURL.setEnabled(false);
            checkBoxLocParams.setEnabled(false);
            checkBoxLocHeaders.setEnabled(false);
            checkBoxLocNURL.setEnabled(false);
            checkBoxLocParamURL.setEnabled(false);
            checkBoxLocResponse.setEnabled(false);
            checkBoxLocParsedQuery.setEnabled(false);
        } else {
            // In normal mode allow location settings
            // If Stream is already selected then disable the rest
            checkBoxLocStream.setEnabled(true);

            if ((location & SigLocation.LOCATION_STREAM) == 0) {
                checkBoxLocURL.setEnabled(true);
                checkBoxLocParams.setEnabled(true);
                checkBoxLocHeaders.setEnabled(true);
                checkBoxLocNURL.setEnabled(true);
                checkBoxLocParamURL.setEnabled(true);
                checkBoxLocResponse.setEnabled(true);
                checkBoxLocParsedQuery.setEnabled(true);
            } else {
                checkBoxLocURL.setEnabled(false);
                checkBoxLocParams.setEnabled(false);
                checkBoxLocHeaders.setEnabled(false);
                checkBoxLocNURL.setEnabled(false);
                checkBoxLocParamURL.setEnabled(false);
                checkBoxLocResponse.setEnabled(false);
                checkBoxLocParsedQuery.setEnabled(false);
            }
        }

        if (location == 0) {
            // Called on new signature
            checkBoxLocStream.setSelected(false);
            checkBoxLocURL.setSelected(false);
            checkBoxLocParams.setSelected(false);
            checkBoxLocHeaders.setSelected(false);
            checkBoxLocNURL.setSelected(false);
            checkBoxLocParamURL.setSelected(false);
            checkBoxLocResponse.setSelected(false);
            checkBoxLocParsedQuery.setSelected(false);

            if (m_mode_aruba)
                checkBoxLocStream.setSelected(true);
            if (m_mode_fp)
                checkBoxLocParsedQuery.setSelected(true);
        } else {
            checkBoxLocStream.setSelected(((location & SigLocation.LOCATION_STREAM) != 0));
            checkBoxLocURL.setSelected(((location & SigLocation.LOCATION_URL) != 0));
            checkBoxLocParams.setSelected(((location & SigLocation.LOCATION_PARAMETERS) != 0));
            checkBoxLocHeaders.setSelected(((location & SigLocation.LOCATION_HEADERS) != 0));
            checkBoxLocNURL.setSelected(((location & SigLocation.LOCATION_ORIGINALURL) != 0));
            checkBoxLocParamURL.setSelected(((location & SigLocation.LOCATION_PARAMANDURL) != 0));
            checkBoxLocResponse.setSelected(((location & SigLocation.LOCATION_RESPONSECONTENT) != 0));
            checkBoxLocParsedQuery.setSelected(((location & SigLocation.LOCATION_PARSEDQUERY) != 0));
        }
    }

    private boolean loadSignature(long sig_id) {
        PreparedStatement ps1 = DBHelper.getInstance().prepare(QUERY_SINGLE_SIGNATURE);

        try {
            // Retrieve data of signature from database
            ps1.setLong(1, sig_id);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next())
                return false;

            m_minver = rs.getInt("sig_minver");
            m_maxver = rs.getInt("sig_maxver");

            // Set GUI controls according to information from database
            textSigId.setText(Long.toString(sig_id));
            textAreaName.setText(rs.getString("name"));
            textAreaPattern.setText(rs.getString("sig_signature"));

            int attack_id = rs.getInt("sig_attacksuperclass");

            int index;
            for (index = 0; index < m_attacks.size(); index++)
                if (((SigAttack)m_attacks.get(index)).m_id == attack_id)
                    break;

            if (index < m_attacks.size())
                comboBoxAttack.setSelectedIndex(index);
            else
                comboBoxAttack.setSelectedIndex(0);

            setLocationControls(rs.getInt("sig_location"));

            comboAccuracy.setSelectedIndex(rs.getInt("sig_accuracy") - 1);

            if (rs.getInt("sig_casesensitive") == 1)
                checkBoxCaseSensitive.setSelected(true);
            else
                checkBoxCaseSensitive.setSelected(false);

            if (rs.getInt("sig_isdecoded") == 1)
                checkBoxDecoded.setSelected(true);
            else
                checkBoxDecoded.setSelected(false);

            if (rs.getInt("sig_dirserver2client") == 1)
                flipDirection(null, false);
            else
                flipDirection(null, true);

            textSnortID.setText(rs.getString("sig_source_key"));

            m_deleted = rs.getBoolean("sig_deleted");
            drawDeleteButton(m_deleted, true);

            fillAvailableServices();
            prepareSelectedServices();

            rs.close();
            ps1.close();

            ps1 = DBHelper.getInstance().prepare(QUERY_SERVICES_FOR_SIGNATURE);
            ps1.setLong(1, sig_id);

            rs = ps1.executeQuery();

            DefaultListModel lm = (DefaultListModel)listAvailable.getModel();
            while (rs.next()) {
                int service_id = rs.getInt("serviceid");
                index = 0;

                while (((NetworkService)lm.get(index)).getId() != service_id)
                    index++;

                listAvailable.addSelectionInterval(index, index);
            }

            handleServiceChange(true);

            rs.close();
            ps1.close();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot load signature " + sig_id, ex);
        }
    }

    private void searchAction() {
        if (!m_search_mode) {
            textSigId.setEnabled(true);
            textSigId.setEditable(true);
            textSigId.setText("");
            buttonSearch.setText("Go");
            buttonOK.setEnabled(false);
            buttonReset.setEnabled(false);
            buttonCopy.setEnabled(false);
            m_search_mode = true;
        } else {
            long search_id;

            try {
                search_id = Long.parseLong(textSigId.getText());
            } catch (Exception ex) {
                textSigId.setText("");
                return;
            }

            if (loadSignature(search_id)) {
                textSigId.setEnabled(false);
                textSigId.setEditable(false);
                buttonSearch.setText("Search");
                buttonOK.setEnabled(true);
                buttonReset.setEnabled(true);
                m_search_mode = false;
                changeFormStatus(FORM_STATUS_UPDATE);
            }
        }
    }

    // Implement the direction check-boxes
    private void flipDirection(ActionEvent e, boolean client2server) {
        if (client2server) {
            radioButtonDir2Client.setSelected(false);
            radioButtonDir2Server.setSelected(true);
        }
        else {
            radioButtonDir2Server.setSelected(false);
            radioButtonDir2Client.setSelected(true);
        }
    }

    // Make the Stream location mutually exclusive to all others.
    private void handleStreamLocation(ActionEvent e, SigLocation loc) {
        if (!checkBoxLocStream.isSelected()) {
            checkBoxLocURL.setEnabled(true);
            checkBoxLocParams.setEnabled(true);
            checkBoxLocHeaders.setEnabled(true);
            checkBoxLocNURL.setEnabled(true);
            checkBoxLocParamURL.setEnabled(true);
            checkBoxLocResponse.setEnabled(true);
            checkBoxLocParsedQuery.setEnabled(true);
            checkBoxDecoded.setEnabled(false);
        } else {
            checkBoxLocURL.setEnabled(false);
            checkBoxLocParams.setEnabled(false);
            checkBoxLocHeaders.setEnabled(false);
            checkBoxLocNURL.setEnabled(false);
            checkBoxLocParamURL.setEnabled(false);
            checkBoxLocResponse.setEnabled(false);
            checkBoxLocParsedQuery.setEnabled(false);
            checkBoxLocURL.setSelected(false);
            checkBoxLocParams.setSelected(false);
            checkBoxLocHeaders.setSelected(false);
            checkBoxLocNURL.setSelected(false);
            checkBoxLocParamURL.setSelected(false);
            checkBoxLocResponse.setSelected(false);
            checkBoxLocParsedQuery.setSelected(false);
            checkBoxDecoded.setEnabled(true);
        }
    }

    // Change the status of the service movement buttons according to the state of the selection list
    private void handleServiceListSelection(boolean isAvailable) {
        JList list = null;

        if (isAvailable) {
            if (listAvailable.isSelectionEmpty())
                buttonToSelected.setEnabled(false);
            else
                buttonToSelected.setEnabled(true);
        }
        else {
            if (listSelected.isSelectionEmpty())
                buttonToAvailable.setEnabled(false);
            else
                buttonToAvailable.setEnabled(true);
        }
    }

    // Move service entries from one list to another
    private void handleServiceChange(boolean isAvailable) {
        JList src_list = null;
        JList dst_list = null;

        if (isAvailable) {
            src_list = listAvailable;
            dst_list = listSelected;
        } else {
            dst_list = listAvailable;
            src_list = listSelected;
        }

        DefaultListModel src_model = (DefaultListModel)src_list.getModel();
        DefaultListModel dst_model = (DefaultListModel)dst_list.getModel();

        int[] selected_items = src_list.getSelectedIndices();

        int merge_index = 0;
        boolean merge_at_end = false;

        for (int index = 0; index < selected_items.length; index++) {
            int item = selected_items[index];

            Object obj = src_model.get(item);

            if (merge_at_end)
                dst_model.addElement(obj);
            else {
                while ((merge_index < dst_model.size()) && ((obj.toString()).compareTo(dst_model.get(merge_index).toString()) > 0))
                    merge_index++;

                if (merge_index == dst_model.size()) {
                    merge_at_end = true;
                    dst_model.addElement(obj);
                } else {
                    dst_model.insertElementAt(obj, merge_index);
                    merge_index++;
                }
            }
        }

        src_list.clearSelection();

        for (int index = 0; index < selected_items.length; index++) {
            int item = selected_items[index] - index;
            src_model.remove(item);
        }
    }

    private int getSigLocation() {
        int loc = 0;

        if (checkBoxLocStream.isSelected())
            loc |= SigLocation.LOCATION_STREAM;

        if (checkBoxLocURL.isSelected())
            loc |= SigLocation.LOCATION_URL;

        if (checkBoxLocParams.isSelected())
            loc |= SigLocation.LOCATION_PARAMETERS;

        if (checkBoxLocHeaders.isSelected())
            loc |= SigLocation.LOCATION_HEADERS;

        if (checkBoxLocNURL.isSelected())
            loc |= SigLocation.LOCATION_ORIGINALURL;

        if (checkBoxLocParamURL.isSelected())
            loc |= SigLocation.LOCATION_PARAMANDURL;

        if (checkBoxLocResponse.isSelected())
            loc |= SigLocation.LOCATION_RESPONSECONTENT;

        if (checkBoxLocQuery.isSelected())
            loc |= SigLocation.LOCATION_QUERY;

        if (checkBoxLocParsedQuery.isSelected())
            loc |= SigLocation.LOCATION_PARSEDQUERY;

        return loc;
    }

    private void doAddSignature() {

        Connection conn = DBHelper.getInstance().getDedicatedConnection();

        int max_ver = 0;

        if (m_mode_aruba) {

            // If the GUI is in Aruba mode than allow signatures to be saved as
            // Aruba only or Aruba and Ibiza
            Object[] options = {"Aruba ONLY", "Aruba / Ibiza", "Cancel"};
            int n = JOptionPane.showOptionDialog(this, "Select Signature Type",
                        "Store Signature",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

            if (n == 0)
                max_ver = SSVersion.ARUBA;
        } else if (!m_mode_fp && intersectServices((DefaultListModel)listSelected.getModel(), m_sql_services))
            // Set maxver for signature to Ibiza if the services are SQL and not feature pack mode
            max_ver = SSVersion.IBIZA;

        // Allow only SQL signatures to be entered in FP mode
        if (m_mode_fp && !allServicesIn((DefaultListModel)listSelected.getModel(), m_sql_services)) {
            JOptionPane.showMessageDialog(this, "Only SQL signature can be added in FP mode\nUse Copy and then Add", "Bad Action", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1;

            if (!m_mode_aruba)
                ps1 = conn.prepareStatement(QUERY_ADD_SIGNATURE);
            else
                ps1 = conn.prepareStatement(QUERY_ADD_SIGNATURE_ARUBA);

            PreparedStatement ps2 = conn.prepareStatement(QUERY_ADD_SERVICE);

            // Signature id
            ps1.setLong(1, Long.parseLong(textSigId.getText()));

            // Attack for signature
            SigAttack attack = (SigAttack)(comboBoxAttack.getSelectedItem());
            ps1.setInt(2, attack.m_id);
            ps1.setInt(3, attack.m_id);

            // Signature name
            ps1.setString(4, textAreaName.getText());

            // Pattern
            ps1.setString(5, textAreaPattern.getText());

            // Is Decoded
            if (checkBoxDecoded.isSelected())
                ps1.setInt(6, 1);
            else
                ps1.setInt(6, 0);

            // Is Case Sensitive
            if (checkBoxCaseSensitive.isSelected())
                ps1.setInt(7, 1);
            else
                ps1.setInt(7, 0);

            // Direction indicators
            if (radioButtonDir2Server.isSelected())
                ps1.setInt(8, 1);
            else
                ps1.setInt(8, 0);

            if (radioButtonDir2Client.isSelected())
                ps1.setInt(9, 1);
            else
                ps1.setInt(9, 0);

            // Accuracy
            String accuracy = (String)comboAccuracy.getSelectedItem();
            if (accuracy.equalsIgnoreCase("low"))
                ps1.setInt(10, 1);
            else if (accuracy.equalsIgnoreCase("medium"))
                ps1.setInt(10, 2);
            else
                ps1.setInt(10, 3);

            // Snort ID
            if (textSnortID.getText().length() == 0)
                ps1.setString(11, "0");
            else
                ps1.setString(11, textSnortID.getText());


            // Location
            ps1.setInt(12, getSigLocation());

            // Set version fields
            ps1.setInt(13, m_minver);

            if (max_ver > 0)
                ps1.setInt(14, max_ver);
            else
                ps1.setNull(14, java.sql.Types.INTEGER);

            ps1.executeUpdate();

            DefaultListModel lm = (DefaultListModel)listSelected.getModel();
            for (int index = 0; index < lm.size(); index++) {
                ps2.setLong(1, Long.parseLong(textSigId.getText()));
                ps2.setInt(2, ((NetworkService)(lm.get(index))).getId());
                ps2.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Signature Added Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

            ps1.close();
            ps2.close();

            conn.close();

            drawDeleteButton(m_deleted, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception e) {
                    System.out.println("Cannot rollback connection");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            throw new RuntimeException("Failed to store signature", ex);
        }
    }

    private void doUpdateSignature() {
        Connection conn = DBHelper.getInstance().getDedicatedConnection();

        int max_ver = 0;

        if (m_minver != m_defaultver) {
            // Do not allow update to Aruba signatures from Ibiza GUI mode
            JOptionPane.showMessageDialog(this, "Original signature is of wrong version.\nSignature cannot be updated\nUse Copy and then Add", "Bad Action", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (m_mode_aruba) {
            // Allow the user to select the version for Aruba signatures
            Object[] options = {"Aruba ONLY", "Aruba / Ibiza", "Cancel"};
            int n = JOptionPane.showOptionDialog(this, "Select Signature Type",
                        "Store Signature",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

            if (n == 0)
                max_ver = SSVersion.ARUBA;
        } else if (!m_mode_fp && intersectServices((DefaultListModel)listSelected.getModel(), m_sql_services))
        // Set maxver for signature to Ibiza if the services are SQL and not feature pack mode
            max_ver = SSVersion.IBIZA;

        // Allow only SQL signatures to be entered in FP mode
        if (m_mode_fp && !allServicesIn((DefaultListModel)listSelected.getModel(), m_sql_services)) {
            JOptionPane.showMessageDialog(this, "Only SQL signature can be updated in FP mode\nUse Copy and then Add", "Bad Action", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1;

            if (!m_mode_aruba)
                ps1 = conn.prepareStatement(QUERY_ADD_SIGNATURE);
            else
                ps1 = conn.prepareStatement(QUERY_ADD_SIGNATURE_ARUBA);

            PreparedStatement ps2 = conn.prepareStatement(QUERY_ADD_SERVICE);
            PreparedStatement ps3 = conn.prepareStatement(QUERY_DEL_SIGNATURE);
            PreparedStatement ps4 = conn.prepareStatement(QUERY_DEL_SERVICES);

            ps4.setLong(1, Long.parseLong(textSigId.getText()));
            ps4.executeUpdate();

            ps3.setLong(1, Long.parseLong(textSigId.getText()));
            ps3.executeUpdate();

            // Signature id
            ps1.setLong(1, Long.parseLong(textSigId.getText()));

            // Attack for signature
            SigAttack attack = (SigAttack)(comboBoxAttack.getSelectedItem());
            ps1.setInt(2, attack.m_id);
            ps1.setInt(3, attack.m_id);

            // Signature name
            ps1.setString(4, textAreaName.getText());

            // Pattern
            ps1.setString(5, textAreaPattern.getText());

            // Is Decoded
            if (checkBoxDecoded.isSelected())
                ps1.setInt(6, 1);
            else
                ps1.setInt(6, 0);

            // Is Case Sensitive
            if (checkBoxCaseSensitive.isSelected())
                ps1.setInt(7, 1);
            else
                ps1.setInt(7, 0);

            // Direction indicators
            if (radioButtonDir2Server.isSelected())
                ps1.setInt(8, 1);
            else
                ps1.setInt(8, 0);

            if (radioButtonDir2Client.isSelected())
                ps1.setInt(9, 1);
            else
                ps1.setInt(9, 0);

            // Accuracy
            String accuracy = (String)comboAccuracy.getSelectedItem();
            if (accuracy.equalsIgnoreCase("low"))
                ps1.setInt(10, 1);
            else if (accuracy.equalsIgnoreCase("medium"))
                ps1.setInt(10, 2);
            else
                ps1.setInt(10, 3);

            // Snort ID
            ps1.setString(11, textSnortID.getText());


            // Location
            ps1.setInt(12, getSigLocation());

            // For aruba signatures, should set max_ver too.
            ps1.setInt(13, m_minver);

            if (max_ver > 0)
                ps1.setInt(14, max_ver);
            else
                ps1.setNull(14, java.sql.Types.INTEGER);

            ps1.executeUpdate();

            DefaultListModel lm = (DefaultListModel)listSelected.getModel();
            for (int index = 0; index < lm.size(); index++) {
                ps2.setLong(1, Long.parseLong(textSigId.getText()));
                ps2.setInt(2, ((NetworkService)(lm.get(index))).getId());
                ps2.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Signature Updated Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception e) {
                    System.out.println("Cannot rollback connection");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            throw new RuntimeException("Failed to store signature", ex);
        }
    }

    private String validateSignatureData() {
        StringBuffer sb = new StringBuffer(1000);

        if (textAreaName.getText().length() == 0)
            sb.append("Signature name missing.\r\n");

        if (textAreaPattern.getText().length() == 0)
            sb.append("Pattern is missing.\r\n");
        else {
            String pattern = textAreaPattern.getText();
            boolean is_valid = ValidateSyntax.getInstance().isSignatureValid(pattern, checkBoxCaseSensitive.isSelected(), !checkBoxLocStream.isSelected());
            if (!is_valid)
                sb.append("Pattern is not valid\r\n");
        }

        if (getSigLocation() == 0)
            sb.append("Location not selected\r\n");

        if ((getSigLocation() != SigLocation.LOCATION_STREAM) && (radioButtonDir2Client.isSelected()))
            sb.append("Bad direction\r\n");

        DefaultListModel lm = (DefaultListModel)listSelected.getModel();
        if (lm.getSize() == 0)
            sb.append("No services selected\r\n");
        else if (!checkBoxLocStream.isSelected()) {
            int location = getSigLocation();

            Collection required_services = null;

            if ((location & SigLocation.LOCATIONS_HTTP) != 0)
                required_services = m_http_services;
            else if ((location & SigLocation.LOCATIONS_SQL) != 0)
                required_services = m_sql_services;

            if (!allServicesIn(lm, required_services))
                sb.append("Services do not match location\r\n");
        }

        if (sb.length() > 0)
            return sb.toString();
        else
            return null;
    }

    private void changeFormStatus(int new_status) {
        m_form_status = new_status;

        if (new_status == FORM_STATUS_NEW) {
            buttonOK.setText("Add");
            buttonCopy.setEnabled(false);
        } else {
            buttonOK.setText("Update");
            buttonCopy.setEnabled(true);
        }
    }

    private void newSignature() {
        String error = validateSignatureData();
        if (error == null) {
            if (m_form_status == FORM_STATUS_NEW) {
                doAddSignature();
                changeFormStatus(FORM_STATUS_UPDATE);
            } else
                doUpdateSignature();
        }
        else
            JOptionPane.showMessageDialog(this, error, "Signature Error", JOptionPane.ERROR_MESSAGE);
    }

    private void resetMinVersion() {
        if (m_mode_aruba)
            m_minver = SSVersion.ANCIENT;
        else if (m_mode_fp)
            m_minver = SSVersion.IBIZA_FP;
        else
            m_minver = SSVersion.IBIZA;
    }

    private void copySignature() {
        // Create a new unique identifier
        setAutoID();

        // Change GUI mode to "new record"
        changeFormStatus(FORM_STATUS_NEW);

        // Reset location control if original signature is of different version
        if (m_minver != m_defaultver)
            setLocationControls(0);

        // Force new signature to the version appropriate to the GUI mode
        resetMinVersion();

        // Handle the "deleted" status
        m_deleted = false;
        drawDeleteButton(m_deleted, false);
    }

    public void loadStaticData() {
        loadApplicationServices();
        loadAvailableServices();
        loadAttacks();

        fillAccuracy();
        fillAttacks();
    }

    private void resetForm() {
        resetMinVersion();

        textSigId.setText("");
        textAreaName.setText("");
        textAreaPattern.setText("");

        if (m_mode_aruba)
            labelArubaMode.setVisible(true);
        else
            labelArubaMode.setVisible(false);

        if (m_mode_fp)
            labelModeFP.setVisible(true);
        else
            labelModeFP.setVisible(false);


        setLocationControls(0);

        checkBoxCaseSensitive.setSelected(false);
        checkBoxDecoded.setSelected(true);

        if (!m_mode_aruba)
            checkBoxDecoded.setEnabled(false);
        else
            checkBoxDecoded.setEnabled(true);

        radioButtonDir2Server.setSelected(true);
        radioButtonDir2Client.setSelected(false);

        textSnortID.setText("");

        fillAvailableServices();
        prepareSelectedServices();

        m_deleted = false;
        drawDeleteButton(m_deleted, false);
    }

    // Choose a signature from the side list
    private void selectSingnatureFromList() {
        String sig_id = (String)listSideSignatures.getSelectedValue();
        if (sig_id != null)
            if (loadSignature(Long.parseLong(sig_id))) {
                textSigId.setEnabled(false);
                textSigId.setEditable(false);
                buttonSearch.setText("Search");
                buttonOK.setEnabled(true);
                buttonReset.setEnabled(true);
                m_search_mode = false;
                changeFormStatus(FORM_STATUS_UPDATE);
            }
    }

    private void searchSqlQuery() {
        String sql_query = textAreaSQL.getText();

        if ((sql_query != null) && (sql_query.length() > 0)) {
            resetSideSignatures();
            String full_query = "select sig_id from tblSignatures where " +
                                    sql_query +
                                " order by sig_id";
            try {
                ResultSet rs = DBHelper.getInstance().directSelect(full_query);
                DefaultListModel lm = (DefaultListModel)listSideSignatures.getModel();

                while (rs.next()) {
                    lm.addElement(Long.toString(rs.getLong("sig_id")));
                }

                rs.close();
            } catch (Exception ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid Query", "Search Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    // Format the delete button
    private void drawDeleteButton(boolean state, boolean enable) {
        buttonDelete.setText(state ? "Resurect" : "Delete");
        buttonDelete.setEnabled(enable);
    }

    // Handle deletion and resurection
    private void onDelete() {
        String caption = m_deleted ? "Resurect" : "Delete";

        Object[] options = {caption, "Cancel"};
        int n = JOptionPane.showOptionDialog(this, "Are you sure ?",
                    "Delete / Resurect",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

        if (n != 0)
            return;

        try {
            PreparedStatement ps = DBHelper.getInstance().prepare(QUERY_SET_DELETED);
            ps.setBoolean(1, !m_deleted);
            ps.setLong(2, Long.parseLong(textSigId.getText()));
            ps.executeUpdate();
            ps.close();

            m_deleted = !m_deleted;

            drawDeleteButton(m_deleted, true);
        } catch (Exception ex) {
            System.err.println("Failed to delete / resurect signature");
            ex.printStackTrace();
        }
    }

    public NewSignatureDlg(int p_defaultver) {

        setContentPane(contentPane);
        //setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        m_defaultver = p_defaultver;
        m_mode_aruba = (m_defaultver == SSVersion.ANCIENT);
        m_mode_fp = (m_defaultver == SSVersion.IBIZA_FP);

        loadStaticData();
        resetForm();

        setAutoID();
        changeFormStatus(FORM_STATUS_NEW);

        radioButtonDir2Server.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flipDirection(e, true);
            }
        });

        radioButtonDir2Client.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flipDirection(e, false);
            }
        });

        checkBoxLocStream.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleStreamLocation(e, SigLocation.Stream);
            }
        });

        listAvailable.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleServiceListSelection(true);
            }
        });

        listSelected.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleServiceListSelection(false);
            }
        });

        buttonToSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleServiceChange(true);
            }
        });

        buttonToAvailable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleServiceChange(false);
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newSignature();
            }
        });

        buttonCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copySignature();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetForm();
                changeFormStatus(FORM_STATUS_NEW);
                setAutoID();
            }
        });

        buttonSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchAction();
            }
        });

        listSideSignatures.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectSingnatureFromList();
            }
        });

        buttonSQL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchSqlQuery();
            }
        });

        buttonDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDelete();
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

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        NewSignatureDlg dialog = new NewSignatureDlg(SSVersion.IBIZA);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }
}
