package ADC.AppDigger;

import org.w3c.dom.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 15/11/2004
 * Time: 09:09:14
 * To change this template use File | Settings | File Templates.
 */
public class AbnormalRequestCounter extends EventProcessorImpl {

    private static final String[] m_command_line_options = {"O"};

    private static final int OPTION_OUTPUT_DIR = 0;

    private String m_output_dir = null;
    private CompressedArchive m_hit_archive = null;
    private CompressedArchive m_miss_archive = null;

    private long m_events_per_source;
    private long m_anomalies_per_source;

    private PrintStream m_output_stream;
    private PrintStream m_error_stream;

    static public EventProcessor getNewInstance(String[] argv) {
        return new AbnormalRequestCounter(argv);
    }

    static public String getName() {
        return "ABCounter";
    }

    static public void printUsage() {
        System.out.println("Usage for module AppDigger.AbnormalRequestCounter:");
        System.out.println("--O <output archive>");
    }

    private AbnormalRequestCounter(String[] argv) {
        scModuleCommandLineArguments command_line = scModuleCommandLineArguments.getInstance();
        command_line.parse(argv, m_command_line_options);

        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_OUTPUT_DIR:
                    if ((m_output_dir != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_output_dir = command_line.getValue(0);
                    break;
            }
        }

        if (error == true) {
            printUsage();
            throw new RuntimeException(this.getClass().getName() + ": Cannot initialize processor");
        }

        if (m_output_dir == null)
            m_output_dir = "results.zip";
    }

    private int prepareOutputDir() {
        File dir = new File(m_output_dir);

        if (!dir.exists()) {
            if (dir.mkdir())
                return 0;
            else
                return -1;
        }

        if (!dir.isDirectory())
            return -1;

        return 0;
    }

    public int handleEvent(Element e) {
        String e_type = e.getAttribute(EventXmlTags.ATTR_EVENT_TYPE);

        if ((e_type != null) && (e_type.equalsIgnoreCase("http-req"))) {
            m_events_per_source++;

            NodeList anomalies = e.getElementsByTagName(EventXmlTags.TAG_ANOMALIES);

            if ((anomalies != null) && (anomalies.getLength() > 0)) {
                String a_mask = ((Element)anomalies.item(0)).getAttribute(EventXmlTags.ATTR_ANOMALIES_MAKS);
                if ((a_mask != null) && (a_mask.length() != 0)) {
                    m_anomalies_per_source++;
                    m_hit_archive.addEvent(e);
                }
                else
                    m_miss_archive.addEvent(e);
            }
        }

        return 0;
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {
        m_output_stream = p_output;
        m_error_stream = p_error;

        m_hit_archive = new CompressedArchive(m_output_dir + ".hit.zip", CompressedArchive.ACCESS_WRITE);
        m_miss_archive = new CompressedArchive(m_output_dir + ".miss.zip", CompressedArchive.ACCESS_WRITE);

        return 0;
    }

    public int handleEndSession() {

        m_hit_archive.close();
        m_miss_archive.close();

        return 0;
    }

    public int handleOpenSource(String source_name) {
        m_events_per_source = 0;
        m_anomalies_per_source = 0;

        return 0;
    }

    public int handleCloseSource(String source_name) {
        if (m_output_stream == null)
            return 0;

        m_output_stream.println("Statisitcs for source: " + source_name);
        m_output_stream.println("\tTotal Requests: " + m_events_per_source);
        m_output_stream.println("\tAnomalies: " + m_anomalies_per_source);

        return 0;
    }

}
