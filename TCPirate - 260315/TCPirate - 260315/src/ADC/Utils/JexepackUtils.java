/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 06/09/2006
 * Time: 10:30:26
 * To change this template use File | Settings | File Templates.
 */
package ADC.Utils;

import java.io.File;

public class JexepackUtils {
	private static JexepackUtils ourInstance = new JexepackUtils();

	public static JexepackUtils getInstance() {
		return ourInstance;
	}

	private JexepackUtils() {
	}

	public static File cwd() {
		  return new File(System.getProperty("user.dir","."));
  	}

	public static File resDir() {
        return resDir("");
  	}

    public static File resDir(String dir) {
        String p = System.getProperty("jexepack.resdir");
        return (p!=null) ? new File(p) : new File(cwd(), dir);       
    }

    public static File exeDir() {
  		String p = System.getProperty("jexepack.exe");
  		return (p!=null) ? new File(new File(p).getParent()) : cwd();
  	}	

}
