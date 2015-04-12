/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 18/11/2004
 * Time: 10:09:26
 * To change this template use File | Settings | File Templates.
 */
package ADC.AppDigger;

public class PrintArchive {
    private static PrintArchive ourInstance = null;

    public static PrintArchive getInstance() {
        if (ourInstance == null)
            ourInstance = new PrintArchive();

        return ourInstance;
    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("PrintArchive <archive-name> <destination-folder>");
    }

    private PrintArchive() {
    }

    public static void main(String[] argv) {
        PrintArchive obj = getInstance();

        if (argv.length < 2) {
            printUsage();
            System.exit(-1);
        }

        CompressedArchive.print(argv[0], argv[1]);
    }
}
