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
public class FindSignatureHits extends EventProcessorImpl {

    private static final String[] m_command_line_options = {"O"};

    private static final int OPTION_OUTPUT_FILE = 0;

    private static final String DEFAULT_OUTPUT_FILE = "results.zip";

    private String m_output_file = null;
    private CompressedArchive m_hit_archive = null;
    private CompressedArchive m_miss_archive = null;

    private long m_events_per_source;
    private long m_anomalies_per_source;

    private PrintStream m_output_stream;
    private PrintStream m_error_stream;

    static public EventProcessor getNewInstance(String[] argv) {
        return new FindSignatureHits(argv);
    }

    static public String getName() {
        return "ABCounter";
    }

    static public void printUsage() {
        System.out.println("Usage for module AppDigger.AbnormalRequestCounter:");
        System.out.println("--O <output archive>");
    }

    private FindSignatureHits(String[] argv) {
        scModuleCommandLineArguments command_line = scModuleCommandLineArguments.getInstance();
        command_line.parse(argv, m_command_line_options);

        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_OUTPUT_FILE:
                    if ((m_output_file != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_output_file = command_line.getValue(0);
                    break;
            }
        }

        if (error == true) {
            printUsage();
            throw new RuntimeException(this.getClass().getName() + ": Cannot initialize processor");
        }

        if (m_output_file == null)
            m_output_file = DEFAULT_OUTPUT_FILE;
    }

    public int handleEvent(Element e) {
        m_events_per_source++;

        NodeList anomalies = e.getElementsByTagName(EventXmlTags.TAG_ANOMALIES);

        if ((anomalies != null) && (anomalies.getLength() > 0)) {
            NodeList ibs = ((Element)anomalies.item(0)).getChildNodes();

            boolean found = false;

            for (int index = 0; (index < ibs.getLength()) && !found; index++) {
                Node n = ibs.item(index);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element ib = (Element)n;

                    if (ib.getAttribute("type").equalsIgnoreCase("suspicious-pattern"))
                        found = true;
                }
            }

            if (found) {
                m_anomalies_per_source++;
                m_hit_archive.addEvent(e);
            }
            else
                m_miss_archive.addEvent(e);
        }

        return 0;
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {
        m_output_stream = p_output;
        m_error_stream = p_error;

        m_hit_archive = new CompressedArchive(m_output_file + ".hit.zip", CompressedArchive.ACCESS_WRITE);
        m_miss_archive = new CompressedArchive(m_output_file + ".miss.zip", CompressedArchive.ACCESS_WRITE);

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
        m_output_stream.println("\tTotal Events: " + m_events_per_source);
        m_output_stream.println("\tTotal Signature Hits: " + m_anomalies_per_source);

        return 0;
    }

}
