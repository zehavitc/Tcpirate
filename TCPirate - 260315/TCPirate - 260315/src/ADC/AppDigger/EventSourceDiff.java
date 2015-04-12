/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 18/11/2004
 * Time: 08:12:41
 * To change this template use File | Settings | File Templates.
 */
package ADC.AppDigger;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;

import ADC.Utils.*;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

public class EventSourceDiff {

    private static final int OPTION_SOURCE_TYPE = 0;
    private static final int OPTION_SOURCE_NAME = 1;
    private static final int OPTION_DESTINATION_TYPE = 2;
    private static final int OPTION_DESTINATION_NAME = 3;
    private static final int OPTION_RESULT_SOURCE = 4;
    private static final int OPTION_RESULT_DESTINATION = 5;
    private static final int OPTION_IBIZA_MODE = 6;
    private static final int OPTION_XSL_FILE = 7;

    private static final CommandLineOptionDef[] m_command_line_options = {
            new CommandLineOptionDef("St", OPTION_SOURCE_TYPE),
            new CommandLineOptionDef("Dt", OPTION_DESTINATION_TYPE),
            new CommandLineOptionDef("Sn", OPTION_SOURCE_NAME),
            new CommandLineOptionDef("Dn", OPTION_DESTINATION_NAME),
            new CommandLineOptionDef("Rs", OPTION_RESULT_SOURCE),
            new CommandLineOptionDef("Rd", OPTION_RESULT_DESTINATION),
            new CommandLineOptionDef("i", OPTION_IBIZA_MODE),
            new CommandLineOptionDef("x", OPTION_XSL_FILE)};


    private static final int SOURCE_TYPE_UNDEFINED = 0;
    private static final int SOURCE_TYPE_COMPRESSED = 1;
    private static final int SOURCE_TYPE_RELATIONAL = 2;

    private static final String SOURCE_TYPE_STR_COMPRESSED = "zip";
    private static final String SOURCE_TYPE_STR_RELATIONAL = "db";

    private static final int DIFF_NONE = 0;
    private static final int DIFF_SOURCE = 1;
    private static final int DIFF_DEST = 2;
    private static final int DIFF_BOTH = 3;

    private static final String DEFAULT_XSL_FILE = "event-diff.xsl";

    private static EventSourceDiff ourInstance = null;

    private int m_source_type = 0;
    private String m_source_name = null;
    private int m_dest_type = 0;
    private String m_dest_name = null;
    private String m_res_source_name = null;
    private String m_res_dest_name = null;
    private boolean m_ibiza_mode = false;
    private javax.xml.transform.Transformer m_trans = null;
    private String m_xsl_file = null;

    public static EventSourceDiff getInstance() {
        if (ourInstance == null)
            ourInstance = new EventSourceDiff();

        return ourInstance;
    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("-St zip|db : Source archive type. Default is zip");
        System.out.println("-Sn <source-name> : Source archive name");
        System.out.println("-Dt zip|db : Destination archive type. Default is zip");
        System.out.println("-Dn <dest-name> : Destination archive name");
        System.out.println("-Rs <filename> : Name of archive to hold items from source that are not found in destination default is source-name.res.zip");
        System.out.println("-Rd <filename> : Name of archive to hold items from destination that are not found in source default is dest-name.res.zip");
        System.out.println("-i : Compare events in Ibiza mode");
        System.out.println("-x <filename> : Name of XSL file that defines comparison string");
        System.out.println("If -Dn is omitted then result archive contains all distinct items (by raw-data) from source");
    }

    private int processArguments(String[] argv) {
        CommandLineArguments command_line = CommandLineArguments.getInstance();
        int num_of_args = command_line.parse(argv, m_command_line_options);

        if (num_of_args < 1)
            return -1;

        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_SOURCE_TYPE:
                    if ((m_source_type != SOURCE_TYPE_UNDEFINED) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    String tmp = command_line.getValue(0);
                    if (tmp.equalsIgnoreCase(SOURCE_TYPE_STR_COMPRESSED))
                        m_source_type = SOURCE_TYPE_COMPRESSED;
                    else if (tmp.equalsIgnoreCase(SOURCE_TYPE_STR_RELATIONAL))
                        m_source_type = SOURCE_TYPE_RELATIONAL;
                    else
                        error = true;

                    break;
                case OPTION_SOURCE_NAME:
                    if ((m_source_name != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_source_name = command_line.getValue(0);
                    break;
                case OPTION_DESTINATION_TYPE:
                    if ((m_dest_type != SOURCE_TYPE_UNDEFINED) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    String tmp1 = command_line.getValue(0);
                    if (tmp1.equalsIgnoreCase(SOURCE_TYPE_STR_COMPRESSED))
                        m_dest_type = SOURCE_TYPE_COMPRESSED;
                    else if (tmp1.equalsIgnoreCase(SOURCE_TYPE_STR_RELATIONAL))
                        m_dest_type = SOURCE_TYPE_RELATIONAL;
                    else
                        error = true;

                    break;
                case OPTION_DESTINATION_NAME:
                    if ((m_dest_name != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_dest_name = command_line.getValue(0);
                    break;
                case OPTION_RESULT_SOURCE:
                    if ((m_res_source_name != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_res_source_name = command_line.getValue(0);
                    break;
                case OPTION_RESULT_DESTINATION:
                    if ((m_res_dest_name != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_res_dest_name = command_line.getValue(0);
                    break;
                case OPTION_IBIZA_MODE:
                    m_ibiza_mode = true;
                    break;
                case OPTION_XSL_FILE:
                    if ((m_xsl_file != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_xsl_file = command_line.getValue(0);
                    break;
            }

        }

        if ((error == true) || (m_source_name == null))
            return -1;

        if ((m_dest_type != SOURCE_TYPE_UNDEFINED) && (m_dest_name == null))
            return -1;

        if (m_source_type == SOURCE_TYPE_UNDEFINED)
            m_source_type = SOURCE_TYPE_COMPRESSED;

        if ((m_dest_type == SOURCE_TYPE_UNDEFINED) && (m_dest_name != null))
            m_dest_type = SOURCE_TYPE_COMPRESSED;

        if (m_res_source_name == null)
            m_res_source_name = m_source_name + ".res.zip";

        if (m_res_dest_name == null)
            m_res_dest_name = m_dest_name + ".res.zip";

        if (m_xsl_file == null)
            m_xsl_file = DEFAULT_XSL_FILE;

        return 0;
    }

    private EventSourceDiff() {
    }

    private void createDir(String dir_name) {

        File dir = new File(dir_name);

        if (dir.exists() && !dir.isDirectory())
            throw new RuntimeException("Cannot create folder");

        if (!dir.exists())
            if (!dir.mkdir())
                throw new RuntimeException("Cannot create folder");
    }

    private String eventString(Element p_event) {
        // If old events are compared then use the raw-data element to compare them
        if (!m_ibiza_mode) {
            NodeList list = p_event.getElementsByTagName(EventXmlTags.TAG_RAWDATA);
            if ((list != null) && (list.getLength() != 0)) {
                Element rawDataElm = (Element)list.item(0);
                String rawData = null;
                if (rawDataElm.getFirstChild() != null)
                    rawData = rawDataElm.getFirstChild().getNodeValue();
                return rawData;
            } else
                return null;
        }

        Source xmlSource = new javax.xml.transform.dom.DOMSource(p_event.getOwnerDocument());
        javax.xml.transform.Result result = new javax.xml.transform.dom.DOMResult();
        try {
            m_trans.transform(xmlSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Node n = ((DOMResult)result).getNode();
        Element el = ((Document)n).getDocumentElement();

        if (el != null)
            return XmlUtils.getInstance().toXmlString(el);
        else
            return null;
    }

    private int diff() {
        EventSource es1 = null;
        EventSource es2 = null;
        CompressedArchive res1 = null;
        CompressedArchive res2 = null;

        // prepare XSLT
        if (m_ibiza_mode) {
            javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(m_xsl_file);
            javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance( );
            try {
                m_trans = transFact.newTransformer(xsltSource);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if (m_source_type == SOURCE_TYPE_COMPRESSED)
            es1 = new CompressedArchive(m_source_name, CompressedArchive.ACCESS_READ);

        createDir(m_source_name + ".tmpdir");
        File tmp_dir = new File(m_source_name + ".tmpdir");

        HashMap source_events = new HashMap(10000);

        Element event = null;

        long src_counter = 0;

        System.out.println("Reading Source Events");

        // Read events from the source and write them into temporary directory
        // create a hash index of the events by their raw data.
        while ((event = es1.getNextEvent()) != null) {
            String rawData = eventString(event);
            // System.out.println(rawData);
            src_counter++;

            if (src_counter % 100 == 0)
                System.out.println('.');
            else
                System.out.print('.');

            String eid = event.getAttribute(EventXmlTags.ATTR_EVENT_ID);
            if (source_events.get(rawData) == null) {
                source_events.put(rawData, eid);

                try {
                    PrintStream ps = new PrintStream(new FileOutputStream(new File(tmp_dir, eid + ".xml")), true);
                    ps.print(XmlUtils.getInstance().toXmlString(event));
                    ps.close();
                } catch (Exception ex) {
                    throw new RuntimeException(this.getClass().getName() + ": Cannot write to temporary storage");
                }
            } else {
                // System.out.println(rawData);
            }
        }


        es1.close();

        System.out.println("");
        System.out.println("Source Events: " + src_counter);
        long dst_counter = 0;

        // If a comparison is requested then read events from the destination.
        // If an events exists in the source it is disregarded and removed from the tomprary
        // directory. Else it is written into a results file
        if (m_dest_type != SOURCE_TYPE_UNDEFINED) {
            if (m_dest_type == SOURCE_TYPE_COMPRESSED)
                es2 = new CompressedArchive(m_dest_name, CompressedArchive.ACCESS_READ);

            res2 = new CompressedArchive(m_res_dest_name, CompressedArchive.ACCESS_WRITE);

            String tmp_id = null;

            System.out.println("Reading Destination Events");

            src_counter = 0;

            while ((event = es2.getNextEvent()) != null) {
                String rawData = eventString(event);

                // Display progress
                src_counter++;
                if (src_counter % 100 == 0)
                    System.out.println('.');
                else
                    System.out.print('.');

                if ((rawData != null) && (rawData.length() != 0)) {
                    if ((tmp_id = (String)source_events.get(rawData)) == null) {
                        res2.addEvent(event);
                        dst_counter++;
                    }
                    else {
                        source_events.remove(rawData);
                        File f = new File(tmp_dir, tmp_id + ".xml");
                        f.delete();
                    }
                }
            }

            es2.close();
            res2.close();

            System.out.println("");
            System.out.println("Unique Destination Events: " + dst_counter);
        }

        // Traverse the remaining events from the source and store them in an archive
        // finally remove the temoprary folder.
        Iterator it = source_events.values().iterator();

        StringBuffer sb = new StringBuffer(10000);

        res1 = new CompressedArchive(m_res_source_name, CompressedArchive.ACCESS_WRITE);

        long src_unique_counter = 0;

        System.out.println("Writing Unique Source Events");

        while (it.hasNext()) {
            sb.setLength(0);
            String eid = (String)it.next();

            File f = new File(tmp_dir, eid + ".xml");

            try {
                FileInputStream is = new FileInputStream(f);
                int ch;

                while ((ch = is.read()) >= 0)
                    sb.append((char)ch);

                is.close();
            } catch (Exception e) {
                System.err.println("");
                System.err.println("Missing file:" + eid);
                throw new RuntimeException(this.getClass().getName() + ": Failed to read temporary file");
            }

            res1.addEvent(sb);


            src_unique_counter++;

            // Display progress
            if (src_unique_counter % 100 == 0)
                System.out.println('.');
            else
                System.out.print('.');

            f.delete();
        }

        res1.close();

        tmp_dir.delete();

        System.out.println("");
        System.out.println("Different source events: " + src_unique_counter);

        if ((src_unique_counter == 0) && (dst_counter == 0))
            return DIFF_NONE;
        else if ((src_unique_counter > 0) && (dst_counter > 0))
            return DIFF_BOTH;
        else if (src_unique_counter > 0)
            return DIFF_SOURCE;
        else
            return DIFF_DEST;
    }

    public static void main(String[] argv) {
        EventSourceDiff obj = getInstance();

        if (obj.processArguments(argv) != 0) {
            printUsage();
            System.exit(-1);
        }

        int diff_result = obj.diff();

        System.exit(diff_result);
    }
}
