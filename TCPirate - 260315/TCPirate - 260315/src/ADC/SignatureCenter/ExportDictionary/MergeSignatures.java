/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 30/11/2004
 * Time: 09:16:53
 * To change this template use File | Settings | File Templates.
 */
package ADC.SignatureCenter.ExportDictionary;

import ADC.Utils.*;
import org.w3c.dom.*;

public class MergeSignatures {
    private static MergeSignatures ourInstance = null;

    public static MergeSignatures getInstance() {
        if (ourInstance == null)
            ourInstance = new MergeSignatures();

        return ourInstance;
    }

    private MergeSignatures() {
    }

    public static void main(String[] argv) {
        MergeSignatures obj = getInstance();

        if (argv.length != 2) {
            System.out.println("usage: MergeSignatures <sig-file> <config-file>");
            System.exit(-1);
        }

        XmlUtils xmlu = XmlUtils.getInstance();

        Element input_el = xmlu.readXmlFIle(argv[0]);
        Element output_el = xmlu.readXmlFIle(argv[1]);

        NodeList list = output_el.getElementsByTagName("signatures");
        if ((list == null) && (list.getLength() == 0)) {
            System.out.println("Config file is damaged");
            System.exit(-1);
        }

        Element original_signatures = (Element)list.item(0);
        Document doc = output_el.getOwnerDocument();
        Element new_signatures =  (Element)doc.importNode(input_el, true);
        output_el.replaceChild(new_signatures, original_signatures);

        xmlu.writeXmlFile(output_el, argv[1]);
    }
}
