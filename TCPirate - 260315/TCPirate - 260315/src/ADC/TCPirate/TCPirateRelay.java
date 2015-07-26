package ADC.TCPirate;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 01/08/2005
 * Time: 14:11:00
 * To change this template use File | Settings | File Templates.
 */
public class TCPirateRelay extends Thread {
    private ServerSocket m_sock;

    private int m_active_connections;

    private String m_dst_address;
    private int m_dst_port;
    private String m_output_folder;

    private boolean m_trap_request = false;
    private boolean m_trap_response = false;

    // ------ Members added for Automatic processing -------

    private boolean m_automatic_processing = false;
    private AutomaticProcessingTableModel m_automatic_processing_table;

    // ------ Members added for DBAA service -------
    private String m_dbaa_address;
    private int m_dbaa_port;
    private String m_dbaa_side_channel_id;
    private boolean m_use_dbaa_service = false;
    // ---------------------------------------------

    // Create a server socket to listen on
    TCPirateRelay(int src_port, String dst_address, int dst_port, String output_folder) {
        m_dst_address = dst_address;
        m_dst_port = dst_port;
        m_output_folder = output_folder;

        this.setDaemon(true);
        
        try {
            m_sock = new ServerSocket(src_port);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        m_active_connections = 0;
    }

    public void killRelay() {
        try {
            m_sock.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void run() {
        Socket clientSocket = null;

        TCPirateRelayGUI gui = new TCPirateRelayGUI(this);
        gui.pack();
        gui.setModal(false);
        gui.setVisible(true);

        // Wait for a client connection
        try {
            while (true) {
                // Complete handshake and create regular socket
                clientSocket = m_sock.accept();

                clientSocket.setKeepAlive(true);

                // Create a relay and activate it
                m_active_connections++;
                TCPirateConnection connection = new TCPirateConnection(clientSocket, m_dst_address, m_dst_port, m_output_folder, m_active_connections);
                connection.setTrapRequest(m_trap_request);
                connection.setTrapResponse(m_trap_response);
                connection.setUseDBAAService(m_use_dbaa_service);
                connection.setDBAAHost(m_dbaa_address);
                connection.setDBAAPort(m_dbaa_port);
                connection.setDBAASideChannelID(m_dbaa_side_channel_id);
                connection.start();
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            // throw new RuntimeException(ex);
            System.out.println("Closing Relay");
        }
    }

    // Trapping related information
    public void setTrapRequest(boolean trap) {
        m_trap_request = trap;
    }

    public void setTrapResponse(boolean trap) {
        m_trap_response = trap;
    }

    public void setAutomaticProcessing(boolean automaticProcessing){
        m_automatic_processing = automaticProcessing;
    }

    public void setAutomaticProcessingTable(AutomaticProcessingTableModel automaticProcessingTable){
        m_automatic_processing_table = automaticProcessingTable;
    }

    public boolean getTrapRequest() {
        return m_trap_request;
    }

    public boolean getTrapResponse() {
        return m_trap_response;
    }

    public boolean getAutomaticProcessing() {
        return m_automatic_processing;
    }

    public AutomaticProcessingTableModel getAutomaticProcssingTable(){
        return m_automatic_processing_table;
    }

    // Connection information
    public int getSourcePort() {
        return m_sock.getLocalPort();
    }

    public String getServerAddress() {
        return m_dst_address;
    }

    public int getServerPort() {
        return m_dst_port;
    }

    // DBAA server connection information
    public void setUseDBAAService(boolean DBAA) {
        m_use_dbaa_service = DBAA;
    }

    public void setDBAAAddress(String address) {
        m_dbaa_address = address;
    }

    public void setDBAAPort(int port) {
        m_dbaa_port = port;
    }

    public void setDBAASideChannelID(String dbaaSideChannelID) {
        m_dbaa_side_channel_id = dbaaSideChannelID;
    }

    public boolean getUseDBAAService() {
        return m_use_dbaa_service;
    }

    public String getDBAAAddress() {
        return m_dbaa_address;
    }

    public int getDBAAPort() {
        return m_dbaa_port;
    }


}
