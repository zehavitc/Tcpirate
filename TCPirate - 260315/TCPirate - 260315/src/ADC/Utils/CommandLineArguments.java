package ADC.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 20/10/2004
 * Time: 15:33:42
 * To change this template use File | Settings | File Templates.
 */

public class CommandLineArguments {

    // A class to store information about the parsed options
    private class SingleOption {
            public int option;
            public int start_from;
            public int num_of_values;

            SingleOption(int p_option, int p_start_from, int p_num_of_values) {
                option = p_option;
                start_from = p_start_from;
                num_of_values = p_num_of_values;
            }
    }

    private static CommandLineArguments ourInstance = new CommandLineArguments();

    private Map m_option_symbols = null;

    // A list of parsed options
    private LinkedList m_options = null;
    private Iterator m_option_iterator = null;
    private SingleOption m_active_option = null;

    private String m_args[];

    public static CommandLineArguments getInstance() {
        return ourInstance;
    }

    private CommandLineArguments() {
    }

    private int lookupSymbol(String p_symbol) {
        Integer index = (Integer)m_option_symbols.get(p_symbol);

        if (index != null)
            return index.intValue();

        return -1;
    }

    // Parse and store the command line arguments
    public int parse(String argv[], CommandLineOptionDef allowed_options[]) {
        if ((argv == null) || (allowed_options == null))
            return -1;

        int num_of_arguments = argv.length;

        // Check for no arguments
        if (num_of_arguments == 0)
            return 0;

        m_option_symbols = new HashMap(100);
        for (int index = 0; index < allowed_options.length; index++) {
            m_option_symbols.put(allowed_options[index].key(), new Integer(allowed_options[index].id()));
        }

        m_options = new LinkedList();
        m_args = argv;

        String curr_action = argv[0];

        // Check that first argument is an option symbol
        if ((curr_action.charAt(0) != '-') || (curr_action.length() < 2))
            return -1;

        // Check that first argument is a valid option
        int last_option = lookupSymbol(curr_action.substring(1));
        if (last_option < 0)
            return -1;

        int index = 1;
        int last_start = 0;
        boolean error = false;

        while ((index < num_of_arguments) && !error) {
            curr_action = argv[index];

            // Hanlde new option
            if ((curr_action.charAt(0) == '-') && (curr_action.length() > 1) && (curr_action.charAt(1) != '-')) {
                m_options.add(new SingleOption(last_option, last_start, index - last_start - 1));

                last_start = index;
                last_option = lookupSymbol(curr_action.substring(1));
                if (last_option < 0)
                    error = true;

            }

            index++;
        }

         // Add last option
        m_options.add(new SingleOption(last_option, last_start, index - last_start - 1));

        m_option_iterator = m_options.iterator();
        m_active_option = null;
        
        return m_options.size();
    }

    // Retrieve the next option from the list
    public int nextOption() {
        if (m_option_iterator == null)
            return -1;

        if (m_option_iterator.hasNext()) {
            m_active_option = (SingleOption)(m_option_iterator.next());
            return m_active_option.option;
        }

        return -1;
    }

    // Retrieve the number of values for the current option
    public int numOfValues() {
        if (m_active_option != null)
            return m_active_option.num_of_values;

        return -1;
    }

    // Return a value of the current option
    public String getValue(int p_index) {
        if ((m_active_option == null) || (p_index < 0) || (p_index >= m_active_option.num_of_values))
            return null;

        return m_args[m_active_option.start_from + p_index + 1];
    }

    // Return to the beginning of the option list
    public void restart() {
        if (m_options != null) {
            m_active_option = null;
            m_option_iterator = m_options.iterator();
        }
    }

    public String toString() {
        if ((m_args == null) || (m_options == null))
            return null;

        StringBuffer buf = new StringBuffer(1000);

        Iterator it = m_options.iterator();

        while (it.hasNext()) {
            SingleOption option = (SingleOption)(it.next());

            buf.append(m_args[option.start_from] + "\t\t");
            for (int index = 0; index < option.num_of_values; index++)
                buf.append(m_args[option.start_from + index + 1] + " ");
            buf.append("\n");
        }

        return buf.toString();
    }
}
