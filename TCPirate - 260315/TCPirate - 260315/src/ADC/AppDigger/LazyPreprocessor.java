package ADC.AppDigger;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 16/11/2004
 * Time: 08:51:55
 * To change this template use File | Settings | File Templates.
 */
public class LazyPreprocessor extends EventPreprocessorFactory implements EventPreprocessor {

    class XmlFilter implements FileFilter {
        public boolean accept(File a_file) {
            if (a_file.getName().endsWith(".xml"))
                return true;
            else
                return false;
        }
    }
    
    private static final int BUF_SIZE = 10000;
    private static final String[] m_command_line_options = {"D"};

    private static final int OPTION_SOURCE_DIR = 0;

    PrintStream m_error_stream = null;
    private byte[] m_buf = new byte[BUF_SIZE];
    private StringBuffer m_sb = new StringBuffer(BUF_SIZE);
    private String m_source_dir = null;

    static public EventPreprocessor getNewInstance(String[] argv) {
        return new LazyPreprocessor(argv);
    }

    static public String getName() {
        return "LazyGateway30";
    }

    static public void printUsage() {
        System.out.println("Usage for module LazyGateway30:");
        System.out.println("--D <folder name>: Folder that contains source XML files");
    }

    private LazyPreprocessor(String[] argv) {
        scModuleCommandLineArguments command_line = scModuleCommandLineArguments.getInstance();
        command_line.parse(argv, m_command_line_options);

        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_SOURCE_DIR:
                    if ((m_source_dir != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_source_dir = command_line.getValue(0);
                    break;
            }
        }

        if ((error == true) || (m_source_dir == null)) {
            printUsage();
            throw new RuntimeException(this.getClass().getName() + ": Missing configuration parameters");
        }
    }

    String readSingleFile(File file) {
        InputStreamReader isr = null;
        m_sb.setLength(0);

        if (file == null)
            return null;

        try {
            isr = new InputStreamReader(new FileInputStream(file), "UTF8");
        } catch (Exception e) {
            m_error_stream.println(this.getClass().getName() + ": Failed to open event file - " + file.getName());
            e.printStackTrace(m_error_stream);
            return null;
        }

        int ch;

        try {
            ch = isr.read();
            while (ch > 0) {
                m_sb.append((char)ch);
                ch = isr.read();
            }
        } catch (Exception e) {
            m_error_stream.println(this.getClass().getName() + ": Failed to read event data from file - " + file.getName());
            e.printStackTrace(m_error_stream);
        }


        try {
            isr.close();
        } catch (Exception e) {
            m_error_stream.println(this.getClass().getName() + ": Failed to close file - " + file.getName());
            e.printStackTrace(m_error_stream);
        }

        return m_sb.toString();
    }

    // Take a list of event files and sort them by the sequential number in the name
    private File[] sortFilesByNumber(File[] p_list) {
        TreeMap tm = new TreeMap();

        for (int index = 0; index < p_list.length; index++) {
            String filename = p_list[index].getName();
            int start = filename.indexOf('_') + 1;
            int end = filename.indexOf('.', start);
            Long num = new Long(filename.substring(start, end));
            tm.put(num, p_list[index]);
        }

        File[] sorted_list = new File[p_list.length];

        Iterator it = tm.values().iterator();

        for (int index = 0; it.hasNext(); index++)
            sorted_list[index] = (File)(it.next());

        return sorted_list;
    }

    public long prepareEvents(AppDigger ad, CompressedArchive ca, RelationalArchive ra, PrintStream p_output, PrintStream p_error) {
        m_error_stream = p_error;

        long num_of_events = 0;

        XmlFilter xml_filter = new XmlFilter();

        File dir = new File(m_source_dir);

        File[] list = dir.listFiles(xml_filter);

        if ((list == null) || (list.length == 0))
            return 0;

        File[] sorted_list = sortFilesByNumber(list);

        // Loop through all the files, read a file, add it to archive and remove it.
        for (int index = 0; index < sorted_list.length; index++) {
            File a_file = sorted_list[index];
            try {
                String s = readSingleFile(a_file);
                ca.addEvent(s);
                a_file.delete();
                num_of_events++;
            } catch (Exception e) {
                e.printStackTrace(p_error);
            }
        }

        return num_of_events;
    }

}
