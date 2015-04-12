/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 04/11/2004
 * Time: 12:55:20
 * To change this template use File | Settings | File Templates.
 */
package ADC.AppDigger;

import org.w3c.dom.Element;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import ADC.Utils.CommandLineOptionDef;
import ADC.Utils.CommandLineArguments;


public class AppDigger {
    private static AppDigger ourInstance = null;

    private static final int RUN_MODE_UNDEFINED = -1;
    private static final int RUN_MODE_PREPROCESS = 0;
    private static final int RUN_MODE_ANALYZE = 1;

    private static final int OUTPUT_MODE_UNDEFINED = 0;
    private static final int OUTPUT_MODE_NORMAL = 1;
    private static final int OUTPUT_MODE_SILENT = 2;
    private static final int OUTPUT_MODE_VERBOSE = 3;

    private static final int PREPROCESSING_MODE_UNDEFINED = 0;
    private static final int PREPROCESSING_MODE_FULL = 1;
    private static final int PREPROCESSING_MODE_PARTIAL = 2;
    private static final int PREPROCESSING_MODE_LATE = 3;

    private static final int PROCESSING_SOURCE_UNDEFINED = 0;
    private static final int PROCESSING_SOURCE_COMPRESSED = 1;
    private static final int PROCESSING_SOURCE_RELATIONAL = 2;
    private static final int PROCESSING_SOURCE_TLA = 3;
    private static final int PROCESSING_SOURCE_FOLDER = 4;

    private static final int OPTION_OUTPUT = 0;
    private static final int OPTION_ERROR = 1;
    private static final int OPTION_VERBOSE = 2;
    private static final int OPTION_SILENT = 3;
    private static final int OPTION_PREPROCESSOR_CLASS = 4;
    private static final int OPTION_PROCESSOR_CLASS = 5;
    private static final int OPTION_RARCHIVE_USERID = 6;
    private static final int OPTION_RARCHIVE_PWD = 7;
    private static final int OPTION_RARCHIVE_CS = 8;
    private static final int OPTION_CARCHIVE_NAME = 9;
    private static final int OPTION_PREPROCESS_FULL = 10;
    private static final int OPTION_PROCESS_SOURCE_COMPRESSED = 11;
    private static final int OPTION_PREPROCESS_PARTIAL = 12;
    private static final int OPTION_PROCESS_SOURCE_RELATIONAL = 13;
    private static final int OPTION_PREPROCESS_LATE = 14;
    private static final int OPTION_PREPROCESSOR_NAME = 15;
    private static final int OPTION_PROCESSOR_NAME = 16;
    private static final int OPTION_PROCESS_SOURCE_TLA = 17;
    private static final int OPTION_PROPERTY = 18;
    private static final int OPTION_CARCHIVE_FOLDER = 19;
    private static final int OPTION_PROCESS_SOURCE_FOLDER = 20;

    private static final String DEFAULT_USERID = "research";
    private static final String DEFAULT_PASSWORD = "research";
    private static final String DEFAULT_OUTPUT_NAME = "out.zip";

    private static final String APPDIGGER_VERSION = "2.0";

    private int m_run_mode = RUN_MODE_UNDEFINED;
    private String m_output_file = null;
    private int m_output_mode = OUTPUT_MODE_UNDEFINED;
    private String m_error_file = null;
    private String m_rarchive_userid = null;
    private String m_rarchive_password = null;
    private String m_rarchive_cs = null;
    private String m_carchive_name = null;
    private int m_preprocessing_mode = PREPROCESSING_MODE_UNDEFINED;
    private String m_preprocessor_class = null;
    private String[] m_preprocessor_args = null;
    private String m_processor_class = null;
    private int m_processing_source = PROCESSING_SOURCE_UNDEFINED;
    private String[] m_processor_args = null;
    private Map m_properties = new HashMap(10);
    private String m_carchive_folder = null;

    private String m_preprocessor_name = null;
    private String m_processor_name = null;

    private static final CommandLineOptionDef[] m_preprocessor_options = {
            new CommandLineOptionDef("o", OPTION_OUTPUT),
            new CommandLineOptionDef("v", OPTION_VERBOSE),
            new CommandLineOptionDef("s", OPTION_SILENT),
            new CommandLineOptionDef("e", OPTION_ERROR),
            new CommandLineOptionDef("pc", OPTION_PREPROCESSOR_CLASS),
            new CommandLineOptionDef("Du", OPTION_RARCHIVE_USERID),
            new CommandLineOptionDef("Dp", OPTION_RARCHIVE_PWD),
            new CommandLineOptionDef("Dc", OPTION_RARCHIVE_CS),
            new CommandLineOptionDef("Cn", OPTION_CARCHIVE_NAME),
            new CommandLineOptionDef("Pf", OPTION_PREPROCESS_FULL),
            new CommandLineOptionDef("Pl", OPTION_PREPROCESS_LATE),
            new CommandLineOptionDef("Pp", OPTION_PREPROCESS_PARTIAL),
            new CommandLineOptionDef("pn", OPTION_PREPROCESSOR_NAME),
            new CommandLineOptionDef("preproc", 999)};

    private static final CommandLineOptionDef[] m_processor_options = {
            new CommandLineOptionDef("o", OPTION_OUTPUT),
            new CommandLineOptionDef("v", OPTION_VERBOSE),
            new CommandLineOptionDef("s", OPTION_SILENT),
            new CommandLineOptionDef("e", OPTION_ERROR),
            new CommandLineOptionDef("pc", OPTION_PROCESSOR_CLASS),
            new CommandLineOptionDef("Du", OPTION_RARCHIVE_USERID),
            new CommandLineOptionDef("Dp", OPTION_RARCHIVE_PWD),
            new CommandLineOptionDef("Dc", OPTION_RARCHIVE_CS),
            new CommandLineOptionDef("Cn", OPTION_CARCHIVE_NAME),
            new CommandLineOptionDef("Cd", OPTION_CARCHIVE_FOLDER),
            new CommandLineOptionDef("pn", OPTION_PROCESSOR_NAME),
            new CommandLineOptionDef("Pc", OPTION_PROCESS_SOURCE_COMPRESSED),
            new CommandLineOptionDef("Pr", OPTION_PROCESS_SOURCE_RELATIONAL),
            new CommandLineOptionDef("Pb", OPTION_PROCESS_SOURCE_TLA),
            new CommandLineOptionDef("r", OPTION_PROPERTY),
            new CommandLineOptionDef("Pf", OPTION_PROCESS_SOURCE_FOLDER),
            new CommandLineOptionDef("proc", 999)};

    private long m_start_time;
    private long m_total_events;

    public static AppDigger getInstance() {
        if (ourInstance == null)
            ourInstance = new AppDigger();

        return ourInstance;
    }

    public static void printUsage() {
        System.out.println("Usage: Appdigger -preproc:");
        System.out.println("-s	Suppress output");
        System.out.println("-o	Output file name");
        System.out.println("-v	Verbose output");
        System.out.println("-e	Error file name");
        System.out.println("-pc Class name");
        System.out.println("-pn Module name");
        System.out.println("-Du User id for relational source");
        System.out.println("-Dp Password for relational source");
        System.out.println("-Dc Connection string for relational source");
        System.out.println("-Cn Archive name for results");
        System.out.println("-Pf Full preprocessing");
        System.out.println("-Pp Partial preprocessing");
        System.out.println("-Pl Late preprocessing");

        System.out.println();
        System.out.println("Usage: Appdigger -proc:");
        System.out.println("-s	Suppress output");
        System.out.println("-o	Output file name");
        System.out.println("-v	Verbose output");
        System.out.println("-e	Error file name");
        System.out.println("-pc Class name");
        System.out.println("-pn Module name");
        System.out.println("-Pc Processing source is archive");
        System.out.println("-Pr Processing source is relational");
        System.out.println("-Pb Processing source is tar in zbip2");
        System.out.println("-Du User id for relational source");
        System.out.println("-Dp Password for relational source");
        System.out.println("-Dc Connection string for relational source");
        System.out.println("-Cn Archive name for results");
    }

    public String getBuiltinPreprocessor(String p_name) {
        if (p_name.equalsIgnoreCase(LazyPreprocessor.getName()))
            return LazyPreprocessor.class.getName();
        else if (p_name.equalsIgnoreCase("Audit30"))
            return "";
        else
            return null;
    }

    public String getBuiltinProcessor(String p_name) {
        return null;
    }

    private int processGeneralArguments(CommandLineArguments command_line) {

        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_OUTPUT:
                    if ((m_output_file != null) || (m_output_mode == OUTPUT_MODE_SILENT) ||
                            (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_output_file = command_line.getValue(0);
                    break;
                // Set output mode to silent
                case OPTION_SILENT:
                    if ((m_output_file != null) || (m_output_mode != OUTPUT_MODE_UNDEFINED) ||
                            (command_line.numOfValues() != 0)) {
                        error = true;
                        break;
                    }
                    m_output_mode = OUTPUT_MODE_SILENT;
                    break;
                // Set output mode to verbose
                case OPTION_VERBOSE:
                    if ((m_output_mode != OUTPUT_MODE_UNDEFINED) || (command_line.numOfValues() != 0)) {
                        error = true;
                        break;
                    }
                    m_output_mode = OUTPUT_MODE_VERBOSE;
                    break;
                case OPTION_ERROR:
                    if ((m_error_file != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }
                    m_error_file = command_line.getValue(0);
                    break;
            }
        }

        if (error == true)
            return -1;

        // Apply defaults
        if (m_output_mode == OUTPUT_MODE_UNDEFINED)
            m_output_mode = OUTPUT_MODE_NORMAL;

        return 0;
    }

    private int processPreprocessorArguments(String[] argv) {
        CommandLineArguments command_line = CommandLineArguments.getInstance();
        int num_of_args = command_line.parse(argv, m_preprocessor_options);

        if (num_of_args < 1)
            return -1;

        if (processGeneralArguments(command_line) < 0)
            return -1;

        command_line.restart();
        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_RARCHIVE_USERID:
                    if ((m_rarchive_userid != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_rarchive_userid = command_line.getValue(0);
                    break;
               case OPTION_RARCHIVE_PWD:
                    if ((m_rarchive_password != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_rarchive_password = command_line.getValue(0);
                    break;
                case OPTION_RARCHIVE_CS:
                     if ((m_rarchive_cs != null) || (command_line.numOfValues() != 1)) {
                         error = true;
                         break;
                     }

                     m_rarchive_cs = command_line.getValue(0);
                     break;
                case OPTION_CARCHIVE_NAME:
                     if ((m_carchive_name != null) || (command_line.numOfValues() != 1)) {
                         error = true;
                         break;
                     }

                     m_carchive_name = command_line.getValue(0);
                     break;
                case OPTION_PREPROCESS_FULL:
                    if (m_preprocessing_mode != PREPROCESSING_MODE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_preprocessing_mode = PREPROCESSING_MODE_FULL;
                    break;
                case OPTION_PREPROCESS_PARTIAL:
                    if (m_preprocessing_mode != PREPROCESSING_MODE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_preprocessing_mode = PREPROCESSING_MODE_PARTIAL;
                    break;
                case OPTION_PREPROCESS_LATE:
                    if ((m_preprocessing_mode != PREPROCESSING_MODE_UNDEFINED) || (m_preprocessor_class != null)) {
                        error = true;
                        break;
                    }

                    m_preprocessing_mode = PREPROCESSING_MODE_LATE;
                    break;
                case OPTION_PREPROCESSOR_NAME:
                    if ((command_line.numOfValues() < 1) || (m_preprocessor_class != null) || (m_preprocessing_mode == PREPROCESSING_MODE_LATE)) {
                        error = true;
                        break;
                    }

                    m_preprocessor_class = getBuiltinPreprocessor(command_line.getValue(0));
                    if (m_preprocessor_class == null) {
                        error = true;
                        return -1;
                    }

                    if (command_line.numOfValues() > 1) {
                        m_preprocessor_args = new String[command_line.numOfValues() - 1];
                        for (int index = 1; index < command_line.numOfValues(); index++)
                            m_preprocessor_args[index - 1] = command_line.getValue(index);
                    }

                    break;
                case OPTION_PREPROCESSOR_CLASS:
                    if ((command_line.numOfValues() < 1) || (m_preprocessor_class != null) || (m_preprocessing_mode == PREPROCESSING_MODE_LATE)) {
                        error = true;
                        break;
                    }

                    m_preprocessor_class = command_line.getValue(0);

                    if (command_line.numOfValues() > 1) {
                        m_preprocessor_args = new String[command_line.numOfValues() - 1];
                        for (int index = 1; index < command_line.numOfValues(); index++)
                            m_preprocessor_args[index - 1] = command_line.getValue(index);
                    }

                    break;
            }
        }

        if (error == true)
            return -1;

        if (((m_preprocessing_mode != PREPROCESSING_MODE_LATE) && (m_preprocessor_class == null)) ||
                ((m_preprocessing_mode != PREPROCESSING_MODE_PARTIAL) && (m_rarchive_cs == null)))
            return -1;

        // Apply Defaults
        if (m_preprocessing_mode == PREPROCESSING_MODE_UNDEFINED)
            m_preprocessing_mode = PREPROCESSING_MODE_FULL;

        if (m_rarchive_userid == null)
            m_rarchive_userid = DEFAULT_USERID;

        if (m_rarchive_password == null)
            m_rarchive_password = DEFAULT_PASSWORD;

        if (m_carchive_name == null)
            m_carchive_name = DEFAULT_OUTPUT_NAME;

        if (m_preprocessing_mode == PREPROCESSING_MODE_LATE)
            m_preprocessor_class = "";

        return 0;
    }

    private int processProcessorArguments(String[] argv) {
        CommandLineArguments command_line = CommandLineArguments.getInstance();
        int num_of_args = command_line.parse(argv, m_processor_options);

        if (num_of_args < 1)
            return -1;

        if (processGeneralArguments(command_line) < 0)
            return -1;

        command_line.restart();
        int option;
        boolean error = false;

        while (((option = command_line.nextOption()) != -1) && !error) {
            switch (option) {
                case OPTION_RARCHIVE_USERID:
                    if ((m_rarchive_userid != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_rarchive_userid = command_line.getValue(0);
                    break;
               case OPTION_RARCHIVE_PWD:
                    if ((m_rarchive_password != null) || (command_line.numOfValues() != 1)) {
                        error = true;
                        break;
                    }

                    m_rarchive_password = command_line.getValue(0);
                    break;
                case OPTION_RARCHIVE_CS:
                     if ((m_rarchive_cs != null) || (command_line.numOfValues() != 1)) {
                         error = true;
                         break;
                     }

                     m_rarchive_cs = command_line.getValue(0);
                     break;
                case OPTION_CARCHIVE_NAME:
                     if ((m_carchive_folder != null) || (m_carchive_name != null) || (command_line.numOfValues() != 1)) {
                         error = true;
                         break;
                     }

                     m_carchive_name = command_line.getValue(0);
                     break;
                case OPTION_CARCHIVE_FOLDER:
                     if ((m_carchive_folder != null) || (m_carchive_name != null) || (command_line.numOfValues() != 1)) {
                         error = true;
                         break;
                     }

                     m_carchive_folder = command_line.getValue(0);
                     break;
                case OPTION_PROCESSOR_NAME:
                    if ((command_line.numOfValues() < 1) || (m_processor_class != null) || (m_preprocessing_mode == PREPROCESSING_MODE_LATE)) {
                        error = true;
                        break;
                    }

                    m_processor_class = getBuiltinProcessor(command_line.getValue(0));
                    if (m_processor_class == null) {
                        error = true;
                        return -1;
                    }

                    if (command_line.numOfValues() > 1) {
                        m_processor_args = new String[command_line.numOfValues() - 1];
                        for (int index = 1; index < command_line.numOfValues(); index++)
                            m_processor_args[index - 1] = command_line.getValue(index);
                    }

                    break;
                case OPTION_PROCESSOR_CLASS:
                    if ((command_line.numOfValues() < 1) || (m_processor_class != null) || (m_preprocessing_mode == PREPROCESSING_MODE_LATE)) {
                        error = true;
                        break;
                    }

                    m_processor_class = command_line.getValue(0);

                    if (command_line.numOfValues() > 1) {
                        m_processor_args = new String[command_line.numOfValues() - 1];
                        for (int index = 1; index < command_line.numOfValues(); index++)
                            m_processor_args[index - 1] = command_line.getValue(index);
                    }

                    break;
                case OPTION_PROCESS_SOURCE_COMPRESSED:
                    if (m_processing_source != PROCESSING_SOURCE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_processing_source = PROCESSING_SOURCE_COMPRESSED;
                    break;
                case OPTION_PROCESS_SOURCE_RELATIONAL:
                    if (m_processing_source != PROCESSING_SOURCE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_processing_source = PROCESSING_SOURCE_RELATIONAL;
                    break;
                case OPTION_PROCESS_SOURCE_TLA:
                    if (m_processing_source != PROCESSING_SOURCE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_processing_source = PROCESSING_SOURCE_TLA;
                    break;
                case OPTION_PROCESS_SOURCE_FOLDER:
                    if (m_processing_source != PROCESSING_SOURCE_UNDEFINED) {
                        error = true;
                        break;
                    }

                    m_processing_source = PROCESSING_SOURCE_FOLDER;
                    break;
                case OPTION_PROPERTY:
                    if (command_line.numOfValues() != 2) {
                        error = true;
                        break;
                    }
                    m_properties.put(command_line.getValue(0), command_line.getValue(1));
                    break;
            }
        }

        if (error == true)
            return -1;

        if ((m_processing_source == PROCESSING_SOURCE_UNDEFINED) ||
                (m_processor_class == null) ||
                ((m_processing_source == PROCESSING_SOURCE_COMPRESSED) && (m_carchive_name == null) && (m_carchive_folder == null)) ||
                ((m_processing_source == PROCESSING_SOURCE_RELATIONAL) && (m_rarchive_cs == null)) ||
                ((m_processing_source == PROCESSING_SOURCE_FOLDER) && (m_carchive_name == null)))
            return -1;

        // Apply Defaults
        if (m_rarchive_userid == null)
            m_rarchive_userid = DEFAULT_USERID;

        if (m_rarchive_password == null)
            m_rarchive_password = DEFAULT_PASSWORD;

        return 0;
    }

    // Open output stream according to command line arguments (or default)
    private int prepareOutput() {
        if (m_output_mode == OUTPUT_MODE_SILENT)
            return 0;

        if (m_output_file == null) {
            return 0;
        }

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(m_output_file), true);
            System.setOut(ps);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot open output path");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        return 0;
    }

    private int prepareError() {
        if (m_error_file == null) {
            return 0;
        }

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(m_error_file), true);
            System.setErr(ps);
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Cannot initialize error stream");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return 0;
    }

    private void printSessionHeader() {
        System.out.println("AppDigger Analysis Tool Version - " + APPDIGGER_VERSION);
        System.out.println("Copyright: Imperva Inc.");
        System.out.print("Session started on ");
        System.out.println(new Date(System.currentTimeMillis()));

        System.out.println("Arguments:");
        System.out.println(CommandLineArguments.getInstance());
    }

    private void printSessionTrailer() {
        long processing_time = (System.currentTimeMillis() - m_start_time) / 1000;

        System.out.println("Total events processed - " + m_total_events);
        System.out.println("Total processing time - " + processing_time);
    }

    private void doPreprocessing(String[] argv) {
        EventPreprocessor ep = null;

        if (processPreprocessorArguments(argv) < 0) {
            printUsage();
            return;
        }

        prepareError();

        prepareOutput();

        printSessionHeader();

        m_start_time = System.currentTimeMillis();

        // Create a compressed archive
        CompressedArchive arch = null;
        if (m_preprocessing_mode != PREPROCESSING_MODE_LATE)
            arch = new CompressedArchive(m_carchive_name, CompressedArchive.ACCESS_WRITE);

        // Instantiate the preprocessor
        try {
            Class pp_class = Class.forName(m_preprocessor_class);
            Method me = pp_class.getMethod("getNewInstance", new Class[] {String[].class});
            ep = (EventPreprocessor)me.invoke(pp_class, new Object[] {m_preprocessor_args});
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Cannot instantiate preprocessor - " + m_preprocessor_name, e);
        }

        // Preprocess events
        m_total_events = ep.prepareEvents(this, arch, null, System.out, System.err);

        arch.close();

        printSessionTrailer();
    }

    private void processSingleSource(EventProcessor ep, String p_source_name) {
        EventSource es;

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println("Start Processing Source: " + p_source_name);


        try {
            // Open an event source
            if (m_processing_source == PROCESSING_SOURCE_COMPRESSED) {
                es = new CompressedArchive(p_source_name, CompressedArchive.ACCESS_READ);
            } else if (m_processing_source == PROCESSING_SOURCE_TLA) {
                es = new TLArchive(p_source_name);
            } else if (m_processing_source == PROCESSING_SOURCE_FOLDER) {
                es = new FolderArchive(p_source_name);
            } else
                es = null;
        } catch (Exception ex) {
            System.err.println("Failed to open event source " + p_source_name);
            ex.printStackTrace();
            return;
        }

        try {
            ep.handleOpenSource(p_source_name);

            Element e = es.getNextEvent();

            while (e != null) {
                m_total_events++;

                if (m_output_mode == OUTPUT_MODE_VERBOSE) {
                    if (m_total_events % 100 == 0)
                        System.out.println('.');
                    else
                        System.out.print('.');
                }

                try {
                    ep.handleEvent(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                e = es.getNextEvent();
            }

        } catch (Exception ex) {
            System.err.println("Failed to process event source " + p_source_name);
            ex.printStackTrace();
        }

        es.close();

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println();

        ep.handleCloseSource(p_source_name);

        if (m_output_mode == OUTPUT_MODE_VERBOSE)
            System.out.println("Finished Processing Source: " + p_source_name);
    }

    private void processSingleFolder(EventProcessor ep, File folder) {
        File[] files = folder.listFiles();

        for (int index = 0; index < files.length; index++) {
            if (files[index].isFile()) {
                String f_name = files[index].getAbsolutePath();
                processSingleSource(ep, f_name);
            }
        }

        for (int index = 0; index < files.length; index++) {
            if (files[index].isDirectory())
                processSingleFolder(ep, files[index]);
        }
    }

    private void doProcessing(String[] argv) {
        EventSource es = null;
        EventProcessor ep = null;
        String source_name = null;

        if (processProcessorArguments(argv) < 0) {
            printUsage();
            return;
        }

        prepareError();

        prepareOutput();

        printSessionHeader();

        m_start_time = System.currentTimeMillis();

        // Instantiate the processor
        try {
            Class pp_class = Class.forName(m_processor_class);
            Method me = pp_class.getMethod("getNewInstance", new Class[] {String[].class});
            ep = (EventProcessor)me.invoke(pp_class, new Object[] {m_processor_args});

            Iterator it = m_properties.keySet().iterator();

            while (it.hasNext()) {
                String name = (String)it.next();
                String value = (String)m_properties.get(name);

                ep.setProperty(name, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + ": Cannot instantiate processor - " + m_processor_name, e);
        }

        m_total_events = 0;

        ep.handleStartSession(System.out, System.err);

        if (m_carchive_name != null) {
            processSingleSource(ep, m_carchive_name);
        } else {
            File folder = new File(m_carchive_folder);
            processSingleFolder(ep, folder);
        }

        ep.handleEndSession();

        printSessionTrailer();

    }

    private AppDigger() {
    }

    public static void main(String[] argv) {
        AppDigger obj = getInstance();

        if (argv.length < 1) {
            printUsage();
            System.exit(-1);
        }

        if (argv[0].equalsIgnoreCase("-preproc"))
            obj.doPreprocessing(argv);
        else if (argv[0].equalsIgnoreCase("-proc"))
            obj.doProcessing(argv);

        System.exit(0);
    }
}
