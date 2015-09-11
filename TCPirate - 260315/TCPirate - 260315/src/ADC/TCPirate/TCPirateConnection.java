package ADC.TCPirate;

import ADC.DBAAMessages.ChallengeRequest;
import ADC.DBAAMessages.Message;
import ADC.DBAAMessages.ReCAPTCHARequest;
import ADC.DBAAMessages.Status;
import com.sun.deploy.util.ArrayUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
//import java.nio.charset.Charset;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 02/08/2005
 * Time: 11:19:18
 * To change this template use File | Settings | File Templates.
 */
public class TCPirateConnection extends Thread {

    public TCPirateOraclePlugin m_oracle_plugin;
    public TCPirateTrapPlugin m_trap_plugin;

    private TCPirate m_pirate;

    private Socket m_in_socket;
    private Socket m_out_socket;

    private byte[] m_relay_buffer;
    private int m_relay_current_pos;

    private byte[] m_listener_buffer;
    private int m_listener_current_pos;

    private String m_server_host;
    private int m_server_port;

    // ------ Members added for automatic processing -------
    private boolean m_is_state_completed;
    private ArrayList<byte[]> m_state_buffer;
    private boolean m_is_automatic_processing;
    private AutomaticProcessingTableModel m_automatic_processing_table;
    private LinkedHashMap<String,byte[]> m_variables;
    private String m_jar_path;
    private String m_jar_class;
    private Object m_jar_class_instance;
    private byte[] m_end_of_state = new byte[]{1,4,4,0,0};
    private boolean m_still_processing;
    //private byte[] m_processing_buffer;
    private ArrayList<byte[]> m_on_process_packets;
    private ProcessLocation m_location;
    private int m_current_processing_row;
    private int m_needed_length;
    private int m_offset;
    private int m_total_process_length;
    private boolean m_should_rematch_last_packet;


    // ------ Members added for DBAA service -------
    private boolean m_use_dbaa_service;
    private Socket m_dbaa_socket = null;
    private String m_dbaa_host;
    private int m_dbaa_port;    
    private boolean m_dbaa_validated = false;
    private ChallengeRequest m_dbaa_challenge;    
    private String m_dbaa_side_channel_id;
    // ----------------------------------------------

    private TCPirateServerListener m_server_listener;
    private TCPirateClientListener m_client_listener;

    private TCPirateConnectionGUI m_dialog;

    private String m_output_folder_name;
    private String m_file_suffix;

    private int m_current_message;

    private int m_serial_number;

    private boolean m_trap_request;
    private boolean m_trap_response;

    private HexEditorTableModel m_hex_editor;

    private boolean m_relay_mode;

    public TCPirateConnection(Socket sock, String host_addr, int host_port, String output_folder, int serial_number) {
        m_in_socket = sock;

        m_serial_number = serial_number;

        m_relay_mode = true;

        m_server_host = host_addr;
        m_server_port = host_port;

        m_output_folder_name = output_folder;
        File f = new File(m_output_folder_name);
        if (!f.exists()) {
            if (!f.mkdir())
                throw new RuntimeException("Cannot create output folder");
        } else if (!f.isDirectory())
            throw new RuntimeException("Illegal output folder");

        // m_file_prefix = m_pirate.getFilePrefix();
        m_file_suffix = ".bin";

        m_oracle_plugin = new TCPirateOraclePlugin();
        m_oracle_plugin.init(this);

        m_trap_plugin = new TCPirateTrapPlugin();
        m_trap_plugin.init(this);

        //Automatic processing
        m_state_buffer = new ArrayList<>();
        m_on_process_packets = new ArrayList<>();
        m_location = new ProcessLocation();
        m_variables = new LinkedHashMap<>();

    }

    public TCPirateConnection(TCPirate pirate) {
        m_pirate = pirate;

        m_in_socket = null;

        m_trap_request = true;
        m_trap_response = true;

        m_relay_mode = false;

        readPirateConfiguration();

        m_oracle_plugin = new TCPirateOraclePlugin();
        m_oracle_plugin.init(this);

        m_trap_plugin = new TCPirateTrapPlugin();
        m_trap_plugin.init(this);


    }

    private void readPirateConfiguration() {

        m_server_host = m_pirate.getDestinationHost();
        m_server_port = m_pirate.getDestinationPort();

        m_output_folder_name = m_pirate.getFolderName();
        File f = new File(m_output_folder_name);
        if (!f.exists()) {
            if (!f.mkdir())
                throw new RuntimeException("Cannot create output folder");
        } else if (!f.isDirectory())
            throw new RuntimeException("Illegal output folder");

        m_file_suffix = m_pirate.getFileSuffix();
    }

    private void clearRelayBuffer() {
        m_relay_current_pos = 0;
    }

    private void clearListenerBuffer() {
        m_listener_current_pos = 0;
    }

    //Open connection to the server
    private Socket openConnection(String host, int port) {
        try {
            InetAddress host_address =  InetAddress.getByName(host);

            Socket sock = new Socket(host_address, port);

            return sock;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    // A method for receiving data from the relay engine
    public synchronized void consumeClientData(byte[] p_data, int p_len) {
        if (p_len == 0)
            return;

        for (int index = 0; index < p_len; index++) {
            m_relay_buffer[m_relay_current_pos] = p_data[index];
            m_relay_current_pos++;
        }

        if (m_use_dbaa_service && !m_dbaa_validated && dbaaPolicyTriggered(m_relay_buffer))  {
            try {
                //TODO: implement request for challenge and halt connection
                writeError("This connection invoked a Compromised Insider Policy.");

                m_dbaa_challenge = new ReCAPTCHARequest(ChallengeRequest.SideChannelType.IM, m_dbaa_side_channel_id);
                Message.SendMessage(m_dbaa_socket.getOutputStream(), m_dbaa_challenge);
                writeError("Sending client a challenge - " + m_dbaa_challenge.get_requestID());
                writeError("Halting connection until challenge response.");

                Status message = (Status)Message.ReadMessage(m_dbaa_socket.getInputStream());

                switch (message.statusCode)
                {
                    case PassedChallenge:
                        writeError("Passed challenge. Trusting this connection.");
                        m_dbaa_validated = true;
                        break;
                    case FailedChallenge:
                        writeError("Failed challenge. Closing this connection...");
                        this.cleanUp();
                        break;
                    case UserUnavailable:
                        writeError("User unavailable by Lync side channel.");
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e) {
                writeError("Could not reach DBAA server.");
            }
        }

        if (m_trap_request && m_is_automatic_processing){
            m_is_state_completed = getPacketsState();
            if (!m_still_processing){
                m_on_process_packets.clear();
                m_location.PacketIndex = 0;
                m_location.OnPacketIndex = 0;
            }
            m_on_process_packets.add(m_relay_buffer);
            processPacket();
        }

        if (m_trap_request || m_trap_plugin.shouldTrap(m_relay_buffer, TCPirateTrapPlugin.TRAP_DIR_TO_SERVER)) {
            m_hex_editor.loadBuffer(m_relay_buffer, m_relay_current_pos);
            m_hex_editor.fireTableStructureChanged();
            m_dialog.setHexEditorBehavior();
            m_dialog.setToServerStatus(true);
        } else
            sendToServer();

    }


    private void processPacket() {
        if (!m_still_processing){
            m_current_processing_row = getFirstMatch();
            if (m_current_processing_row == -1){
                FinishProcessing();
                return;
            }
        }
        AutomaticProcessingRow row = m_automatic_processing_table.getRow(m_current_processing_row);
        if (!m_still_processing){
            if (!getPacketInputs(row)) return;
            if (m_total_process_length < m_needed_length){
                return;
            }
        }
        else {
            if (m_relay_buffer.length + m_total_process_length <  m_needed_length){
                m_total_process_length += m_relay_buffer.length;
                return;
            }
        }
        //There is enough data to apply action
        ArrayList<Integer> packets_lengths = new ArrayList<>();
        for (int i=0; i< m_on_process_packets.size(); i++){
            packets_lengths.add(m_on_process_packets.get(i).length);
        }
        byte[] process_buffer = m_on_process_packets.get(0).clone();
        for (int i=1; i< m_on_process_packets.size(); i++){
            byte[] temp = process_buffer.clone();
            byte[] packet = m_on_process_packets.get(i);
            process_buffer = new byte[temp.length + packet.length];
            System.arraycopy(temp, 0, process_buffer, 0, temp.length);
            System.arraycopy(packet, 0, process_buffer, 0, packet.length);
        }
        applyAction(row,process_buffer,m_offset,m_needed_length);
        int process_index = 0;
        //Copy process buffer after change (action is not read)
        if (!(row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.actionColumn)).equals(AutomaticProcessingRow.Actions.Read))){
            for (int i=0; i< packets_lengths.size(); i++){
                int length = packets_lengths.get(i);
                byte[] packet = m_on_process_packets.get(i);
                for (int byte_index = 0; byte_index < length; byte_index++,process_index ++){
                    packet[byte_index] = process_buffer[process_index];
                }
            }
        }
        m_should_rematch_last_packet = false;
        if (m_on_process_packets.size() > 1 ){
            if (m_total_process_length + m_relay_buffer.length > m_needed_length){
                m_should_rematch_last_packet = true;
            }
        }
        sendToServer();
        //Check rematch on last packet
        if (m_should_rematch_last_packet){
            byte[] last_packet = m_on_process_packets.get(m_on_process_packets.size() - 1);
            m_on_process_packets.clear();
            m_on_process_packets.add(last_packet);
            m_location.PacketIndex =0;
            m_location.OnPacketIndex = m_needed_length -  m_total_process_length;
            InitializeAutomaticProcessingFields();
            processPacket();
        }
        else{
            m_on_process_packets.clear();
            InitializeAutomaticProcessingFields();
        }





//        String baseString = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.baseStringColumn));
//        String last_packet = new String(m_relay_buffer);
//        String offset_str = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.offsetColumn));
//        int offset = offset_str == "" ? 0 : Integer.parseInt(offset_str);
//        if (baseString != ""){
//            int baseStringIndex = last_packet.indexOf(baseString);
//            if (baseStringIndex == -1){
//                FinishProcessing();
//                return;
//            }
//            if (m_relay_buffer.length - (baseStringIndex+baseString.length() + offset)  < m_needed_length){
//                m_still_processing = true;
//                return;
//            }
//            applyAction(row, baseStringIndex + baseString.length(), m_needed_length);
//            FinishProcessing();
//        }
//        else{
//            if (m_relay_buffer.length - offset < m_needed_length){
//                m_still_processing = true;
//                return;
//            }
//            applyAction(row, offset, m_needed_length);
//            FinishProcessing();

//        }
    }

    //Initialize fields after apply action
    private void InitializeAutomaticProcessingFields() {
        m_current_processing_row = -1;
        m_should_rematch_last_packet = false;
        m_still_processing = false;
        m_needed_length = -1;
        m_offset = -1;
        m_total_process_length = 0;
    }

    private boolean getPacketInputs(AutomaticProcessingRow row) {
        m_needed_length = Integer.parseInt(row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.lengthColumn)));
        String base_string = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.baseStringColumn));
        String last_packet = new String(m_relay_buffer);
        String offset_str = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.offsetColumn));
        m_offset = offset_str == "" ? 0 : Integer.parseInt(offset_str);
        if (base_string != "") {
            int base_string_index = last_packet.indexOf(base_string);
            if (base_string_index == -1) {
                FinishProcessing();
                return false;
            }
            m_offset = base_string_index + base_string.length() + m_offset;
        }
        m_total_process_length = m_relay_buffer.length - m_offset;
        return true;
    }

    private void FinishProcessing() {
        sendToServer();
        if (m_is_state_completed){
            m_state_buffer.clear();
        }
    }

    private void applyAction(AutomaticProcessingRow row,byte[] process_buffer, int startIndex, int length) {
        String variableName = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.variableColumn));
        if (row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.actionColumn)).equals(AutomaticProcessingRow.Actions.Read)){
            byte[] value = new byte[length];
            System.arraycopy(process_buffer,startIndex,value,0,length);
            m_variables.put(variableName,value);
            return;
        }
        if (row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.actionColumn)).equals(AutomaticProcessingRow.Actions.Modify)){
            String input = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.inputColumn));
            String function = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.functionColumn));
            if (function.equals(AutomaticProcessingRow.Functions.And.toString())){
                ApplyBitwiseAnd(process_buffer,startIndex, length, variableName, input);
                return;
            }
            if (function.equals(AutomaticProcessingRow.Functions.Or.toString())){
                ApplyBitwiseOr(process_buffer,startIndex, length, variableName, input);
                return;
            }
            if (function.equals(AutomaticProcessingRow.Functions.Plus.toString())){
                ApplyBitwisePlus(process_buffer,startIndex, length, variableName, input);
                return;
            }
            else{
                //Custom modify
                try {
                    row.functionInstance.invoke(process_buffer,m_is_state_completed, m_state_buffer,m_variables);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void ApplyBitwisePlus(byte[] process_buffer, int startIndex, int length, String variableName, String input) {
        byte[] variableValue = m_variables.get(variableName);
        byte[] inputValue = input.getBytes();
        if (inputValue.length != variableValue.length){
            //TODO: throw exception
        }
        byte[] res = new byte[input.length()];
        for(int i=0; i< input.length(); i++){
            res[i] = (byte) (variableValue[i] + inputValue[i]);
        }
        System.arraycopy(res,0,process_buffer,startIndex,length);
    }

    private void ApplyBitwiseOr(byte[] process_buffer, int startIndex, int length, String variableName, String input) {
        byte[] variableValue = m_variables.get(variableName);
        byte[] inputValue = input.getBytes();
        if (inputValue.length != variableValue.length){
            //TODO: throw exception
        }
        byte[] res = new byte[input.length()];
        for(int i=0; i< input.length(); i++){
            res[i] = (byte) (variableValue[i] | inputValue[i]);
        }
        System.arraycopy(res,0,process_buffer,startIndex,length);
    }

    private void ApplyBitwiseAnd(byte[] process_buffer, int startIndex, int length, String variableName, String input) {
        byte[] variableValue = m_variables.get(variableName);
        byte[] inputValue = input.getBytes();
        if (inputValue.length != variableValue.length){
            //TODO: throw exception
        }
        byte[] res = new byte[input.length()];
        for(int i=0; i< input.length(); i++){
            res[i] = (byte) (variableValue[i] & inputValue[i]);
        }
        System.arraycopy(res,0,process_buffer,startIndex,length);
    }

    private int getFirstMatch() {
        try {
            String packet = new String(m_relay_buffer).substring(m_location.OnPacketIndex,m_relay_buffer.length -1 );
            for (int index = 0; index < m_automatic_processing_table.getRowCount(); index++) {
                AutomaticProcessingRow row = m_automatic_processing_table.getRow(index);
                String filterRegex = row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.filterColumn));
                if (!packet.matches(filterRegex)) continue;
                if (row.get(AutomaticProcessingTableModel.columns.get(AutomaticProcessingTableModel.filterFunctionColumn)) != "") {
                    if (!(boolean) (row.filterFunctionInstance.invoke(m_relay_buffer, m_is_state_completed, m_state_buffer))) {
                        continue;
                    }
                    return index;
                }
            }
            return -1;
        }
        catch (IllegalAccessException e) {
                e.printStackTrace();
        }
        catch (InvocationTargetException e) {
                e.printStackTrace();
        }
        return -1;
    }

//    private void updateProcessingBuffer() {
//        if (m_still_processing){
//            byte[] temp = m_processing_buffer.clone();
//            m_processing_buffer = new byte[temp.length + m_relay_buffer.length];
//            System.arraycopy(temp,0,m_processing_buffer,0,temp.length);
//            System.arraycopy(m_relay_buffer,0,m_processing_buffer, temp.length,m_relay_buffer.length);
//        }
//        else{
//            m_processing_buffer = m_relay_buffer.clone();
//        }
//
//    }

    private boolean getPacketsState(){
//        byte[] temp = m_state_buffer.clone();
//        int newSize = temp.length + m_relay_buffer.length;
//        m_state_buffer = new byte[newSize];
//        if (temp.length != 0){
//            System.arraycopy(temp,0,m_state_buffer,0,temp.length);
//        }
//        System.arraycopy(m_relay_buffer,0,m_state_buffer,temp.length,m_relay_buffer.length);
//        int initialBufferIndex = m_relay_buffer.length - m_end_of_state.length;
//        if (initialBufferIndex + m_end_of_state.length < m_relay_buffer.length || initialBufferIndex < 0) return false;
//        for (int index = 0; index < m_end_of_state.length; index++){
//            if (m_relay_buffer[index+initialBufferIndex]!= m_end_of_state[index])
//                return false;
//        }
//        return true;
        if (m_state_buffer == null){
            m_state_buffer = new ArrayList();
        }
        m_state_buffer.add(m_relay_buffer.clone());
        int initialBufferIndex = m_relay_buffer.length - m_end_of_state.length;
        if (initialBufferIndex + m_end_of_state.length < m_relay_buffer.length || initialBufferIndex < 0) return false;
        for (int index = 0; index < m_end_of_state.length; index++){
            if (m_relay_buffer[index+initialBufferIndex]!= m_end_of_state[index])
                return false;
        }
        return true;
    }


    private boolean dbaaPolicyTriggered(byte[] relay_buffer) {
        try {

            String bufferString = bufferToString(relay_buffer);

            LinkedList<String> rules = new LinkedList<String>();
            //rules.add("User=Tomg");
            //rules.add("CONNECT_DATA");
            rules.add("ALL_USERS");
            rules.add("ALL_TABLES");

            for (String rule : rules) {
                if (bufferString.indexOf(rule) > -1) {
                    return true;
                }
            }

            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    private String bufferToString(byte[] relay_buffer) {

        StringBuilder sb = new StringBuilder();
            for (byte b : relay_buffer) {
                if ((b > 31) && (b < 127))
                    sb.append((char)b);
                else
                    sb.append(" ");
            }

            return sb.toString();
    }

    private String messageFileName(boolean to_server) {
        String tmp = Integer.toString(m_current_message);

        while (tmp.length() < 4)
            tmp = "0" + tmp;

        tmp = m_output_folder_name + File.separator + getSourcePort() + "-" + tmp;

        if (to_server)
            tmp = tmp + "-toServer";
        else
            tmp = tmp + "-toClient";

        tmp = tmp + m_file_suffix;

        return tmp;
    }

    // A method for receiving data from the listener engine
    public synchronized void consumeServerData(byte[] p_data, int p_len) {
        if (p_len == 0)
            return;

        for (int index = 0; index < p_len; index++) {
            m_listener_buffer[m_listener_current_pos] = p_data[index];
            m_listener_current_pos++;
        }

        // If connection is set to trapping server replies or connection is manual or Trap plugin indicates
        // trapping or part of the message was already trapped, the data is
        // copied into the hex editor and the "To Client" button is enabled. Otherwise, data is
        // immediately sent to the client
        if (m_trap_response || !m_relay_mode || m_trap_plugin.shouldTrap(m_listener_buffer, TCPirateTrapPlugin.TRAP_DIR_TO_CLIENT)) {
            m_hex_editor.loadBuffer(m_listener_buffer, m_listener_current_pos);
            m_hex_editor.fireTableStructureChanged();
            m_dialog.setHexEditorBehavior();
            if (m_relay_mode)
                m_dialog.setToClientStatus(true);
            else {
                dumpBufferToFile(m_listener_buffer, m_listener_current_pos, messageFileName(false));
                m_current_message++;
            }
        } else
            sendToClient();
    }

    public void sendToServer() {
        try {
            if (m_is_automatic_processing){
                int packets_count = m_on_process_packets.size();
                for (int i = 0; i< packets_count; i++){
                    if (i == packets_count -1 && m_should_rematch_last_packet) continue;
                    byte[] packet = m_on_process_packets.get(i);
                    int size = packet.length;
                    dumpBufferToSocket(packet, size, m_out_socket.getOutputStream());
                    dumpBufferToFile(packet, size, messageFileName(true));
                }

            }
            if (m_hex_editor.isChanged()) {
                byte[] data = m_hex_editor.getData();
                int size = m_hex_editor.getDataSize();

                dumpBufferToSocket(data, size, m_out_socket.getOutputStream());
                dumpBufferToFile(data, size, messageFileName(true));
                m_hex_editor.resetChange();
            } else {
                dumpBufferToSocket(m_relay_buffer, m_relay_current_pos, m_out_socket.getOutputStream());
                dumpBufferToFile(m_relay_buffer, m_relay_current_pos, messageFileName(true));
            }

            m_current_message++;
            clearRelayBuffer();
            m_dialog.setToServerStatus(false);
            m_dialog.setToClientStatus(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    // Move data from client through relay to destination
    public synchronized void relayToDestination() {
        clearListenerBuffer();

        sendToServer();
    }

    public void sendToClient() {
        try {
            if (m_hex_editor.isChanged()) {
                byte[] data = m_hex_editor.getData();
                int size = m_hex_editor.getDataSize();

                dumpBufferToSocket(data, size, m_in_socket.getOutputStream());
                dumpBufferToFile(data, size, messageFileName(false));
                m_hex_editor.resetChange();
            } else {
                dumpBufferToSocket(m_listener_buffer, m_listener_current_pos, m_in_socket.getOutputStream());
                dumpBufferToFile(m_listener_buffer, m_listener_current_pos, messageFileName(false));
            }
            m_current_message++;
            clearListenerBuffer();
            m_dialog.setToClientStatus(false);
            m_dialog.setToServerStatus(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    // Move data from server through relay back to client
    public synchronized void listenerToRelay() {
        sendToClient();

        if (m_out_socket == null)
            cleanUpWhenServerTerminated();
    }

    public void dumpBufferToSocket(byte[] p_data, int p_len, OutputStream p_os) {
        try {
            p_os.write(p_data, 0, p_len);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void dumpBufferToFile(byte[] p_data, int p_len, String p_name) {
        try {
            FileOutputStream fos = new FileOutputStream(p_name);

            fos.write(p_data, 0, p_len);

            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void loadFile(String p_filename) {
/*
        if (m_relay_mode) {
            int option = JOptionPane.showOptionDialog(m_dialog, "Disconnect Client ?", "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Yes", "No"}, "No");
            if (option == 0) {
                // Terminate original client connection and change mode.
                try {
                    m_in_socket.close();

                    m_relay_mode = false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
*/
        
        // Load data to GUI
        m_hex_editor.loadBuffer(new File(p_filename));

        // If load failed issue a message
        if (!m_hex_editor.isChanged()) {
            writeError("Failed to load message from file " + p_filename);
            return;
        }

        // If load succeeded then show new buffer
        m_hex_editor.fireTableStructureChanged();
        m_dialog.setHexEditorBehavior();
        m_dialog.setToServerStatus(true);
    }

    public synchronized void dropMessage() {
        clearListenerBuffer();
        m_dialog.setToClientStatus(false);
        m_dialog.setToServerStatus(false);
    }

    public void cleanUp() {
        try {
            // Cleanup
            if (m_out_socket != null)
                m_out_socket.close();

            if (m_in_socket != null)
                m_in_socket.close();

            if (m_dbaa_socket != null)
                m_dbaa_socket.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void cleanUpWhenServerTerminated() {
        // Change display
        m_dialog.setConnected(false);

        // Clear connection buffers
        clearListenerBuffer();
        clearRelayBuffer();

        // Clear connection tracking
        m_current_message = 0;

        // Stop client connection
        if (m_in_socket != null) {
            try {
                m_in_socket.close();
            } catch (Exception ex) {
                writeError("Unexpected Error - Closing client connection");
            }

            m_in_socket = null;
        }
    }

    public synchronized void serverDisconnected() {
        m_out_socket = null;

        // if (m_listener_current_pos == 0)
            if (m_in_socket == null)
                cleanUpWhenServerTerminated();
    }

    public synchronized void clientDisconnected() {
        m_in_socket = null;
    }

    public void reconnect() {
        m_out_socket = openConnection(m_server_host, m_server_port);
        m_dialog.displayConnectionData();

        try {
            m_server_listener = new TCPirateServerListener(this, m_out_socket.getInputStream());
            m_server_listener.start();
            m_dialog.setConnected(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }

    public void doTruncate(int size) {
        m_hex_editor.truncateBuffer(size);
        m_hex_editor.fireTableStructureChanged();
        m_dialog.setHexEditorBehavior();
    }

    public void run() {
        try {
            // Prepare network buffers
            m_listener_buffer = new byte[30000];
            clearListenerBuffer();

            m_relay_buffer = new byte[30000];
            clearRelayBuffer();

            m_hex_editor = new HexEditorTableModel(1000000);

            //Prepare  for automatic processing
            if (m_is_automatic_processing) {
                //Load jar
                if (m_jar_path != "" && m_jar_class != "") {
                    File jarFile = new File(m_jar_path);
                    URL jarUrl = jarFile.toURI().toURL();
                    URLClassLoader child = new URLClassLoader(new URL[]{jarUrl}, this.getClass().getClassLoader());
                    Class classToLoad = Class.forName(m_jar_class, true, child);
                    m_jar_class_instance = classToLoad.newInstance();
                }
            }

            // Create a socket to the server
            m_out_socket = openConnection(m_server_host, m_server_port);

            // Create and run GUI
            m_dialog = new TCPirateConnectionGUI(this);
            m_dialog.pack();

            if (m_relay_mode)
                m_dialog.setTitle("Connection - " + m_serial_number);
            else
                m_dialog.setTitle("Local Connection");

            m_dialog.displayConnectionData();
            m_dialog.initializeHexEditor(m_hex_editor);
            m_dialog.setConnected(true);
            m_dialog.setModal(false);
            m_dialog.setVisible(true);

            // Attach a listener to the server socket
            m_server_listener = new TCPirateServerListener(this, m_out_socket.getInputStream());

            // If using DBAA then create a socket to DBAA server
            if (m_use_dbaa_service) {
                m_dbaa_socket = openConnection(m_dbaa_host, m_dbaa_port);
                if (m_dbaa_socket != null)
                    writeError("Connected to DBAA server successfully.");
                else {
                    writeError("Could not connect to DBAA server.");
                    this.setUseDBAAService(false);
                }
            }

            // If relay is running then attach to the relay socket
            if (m_in_socket != null) {
                m_client_listener = new TCPirateClientListener(this, m_in_socket.getInputStream());
            } else
                m_client_listener = null;

            m_server_listener.start();

            if (m_relay_mode)
                m_client_listener.start();

            writeError("Starting");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public JDialog getGUI() {
        return m_dialog;
    }

    public HexEditorTableModel getBuffer() {
        return m_hex_editor;
    }

    public void writeError(String s) {
        m_dialog.writeMessage(s);
    }

    public String getSourceIP() {
        if (m_relay_mode)
            return m_in_socket.getInetAddress().getHostAddress();
        else
            return "Local Host";
    }

    public int getSourcePort() {
        if (m_relay_mode)
            return m_in_socket.getPort();
        else
            return m_out_socket.getLocalPort();
    }

    public String getDestinationIP() {
        return m_out_socket.getInetAddress().getHostAddress();
    }

    public int getDestinationPort() {
        return m_out_socket.getPort();
    }

    public boolean getTrapRequest() {
        return m_trap_request;
    }

    public boolean getTrapResponse() {
        return m_trap_response;
    }

    public void setTrapRequest(boolean flag) {
        m_trap_request = flag;
    }

    public void setTrapResponse(boolean flag) {
        m_trap_response = flag;
    }

    // ------ Automatic processing  -------

    public void setAutomaticProcessing(boolean isAutomaticProcessing){
        m_is_automatic_processing = isAutomaticProcessing;
    }

    public void setAutomaticProcessingTable(AutomaticProcessingTableModel automaticProcessingTable){
        m_automatic_processing_table = automaticProcessingTable;
    }

    public void setJarInformation(String jarPath,String jarClass){
        m_jar_path = jarPath;
        m_jar_class = jarClass;
    }

    // ------ DBAA server connection information  -------
    public void setUseDBAAService(boolean flag) {
        m_use_dbaa_service = flag;
    }

    public void setDBAAHost(String address) {
        m_dbaa_host = address;
    }

    public void setDBAAPort(int port) {
        m_dbaa_port = port;
    }

    public void setDBAAValidated(boolean flag) {
        m_dbaa_validated = flag;
    }

    public void setDBAASideChannelID(String sideChannelID) {
        m_dbaa_side_channel_id = sideChannelID;
    }

    public boolean getUseDBAAService() {
        return m_use_dbaa_service;
    }

    public String getDBAAAddress() {
        return m_dbaa_host;
    }

    public int getDBAAPort() {
        return m_dbaa_port;
    }

    public boolean getDBAAValidated() {
        return m_dbaa_validated;
    }
    // -----------------------------------------------------------------------
}
