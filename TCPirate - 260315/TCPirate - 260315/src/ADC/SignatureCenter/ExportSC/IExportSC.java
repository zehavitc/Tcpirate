package ADC.SignatureCenter.ExportSC;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 27/12/2004
 * Time: 09:21:59
 * To change this template use File | Settings | File Templates.
 */
public interface IExportSC {
    public String export();
    public String exportForPatch();
    public String versionString();
    public void setProperty(String name, String value);
}
