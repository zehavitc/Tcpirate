package ADC.TCPirate;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.ini4j.Ini;

/**
 * User: Aharonf
 * Date: Dec 11, 2011
 * Time: 1:34:37 PM
 */
public class ConnectionProps
{
    String  host = null;
    String  destPort = null;
    String  relayPort = null;
    String  DestFolder = null;
    boolean trapRequest;
    boolean trapResponse;

    
    public boolean validateIni()
    {
        if(
                host == null        ||
                destPort == null    ||
                relayPort == null   ||
                DestFolder == null
            )
        {
            return false;
        }
        return true;
    }

    public void setParamsFromIni(Ini ini)
    {
       try
      {
           DestFolder =     (String)ini.get("dump",  "FolderName", String.class);
           host       =     (String)ini.get("conn",  "HostName",   String.class);
           destPort   =     (String)ini.get("conn",  "DestinationPort",   String.class);
           relayPort  =     (String)ini.get("conn",  "RelayPort",   String.class);
           trapRequest =    (Boolean)ini.get("dump",  "TrapRequest", boolean.class);
           trapResponse =   (Boolean)ini.get("dump",  "TrapResponse", boolean.class);

      }
      catch(Exception e)
      {

      } 
    }

    public void resetToDefault()
    {
      DestFolder = System.getProperty("user.dir",".");
      destPort = "1521";
      relayPort = "1523";
      host = "10.1.2.202";
      //destPort = "1434";
      //relayPort = "1430";
      //host = "10.1.2.153";
      //destPort = "2433";
      //relayPort = "2432";
      //host = "10.1.2.165";
      trapRequest = false;
      trapResponse = false;        
    }

    public ConnectionProps()
    {
       resetToDefault(); 
    }

    
}
