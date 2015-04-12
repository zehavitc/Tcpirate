package ADC.Utils;
import java.io.File;
import java.io.FileFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Ludaa on 3/10/14.
 */


public class runScript {

    private static String script_location = "";
    private static String file_extension = ".sql";
    private static ProcessBuilder processBuilder =null;

public static void main (String[] args) {
        try {
        File file = new File("C:/Script_folder");
        File [] list_files= file.listFiles(new FileFilter() {

public boolean accept(File f) {
        if (f.getName().toLowerCase().endsWith(file_extension))
        return true;
        return false;
        }
        });
        for (int i = 0; i<list_files.length;i++){
        script_location = "@" + list_files[i].getAbsolutePath();//ORACLE
        ProcessBuilder processBuilder = new ProcessBuilder("sqlplus",        "system/barbapapa@database_name", script_location); //ORACLE
        //script_location = "-i" + list_files[i].getAbsolutePath();
        //  processBuilder = new ProcessBuilder("sqlplus", "-Udeep-Pdumbhead-Spc-de-deep\\sqlexpress-de_com",script_location);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String currentLine = null;
        while ((currentLine = in.readLine()) != null) {
        System.out.println(" "  + currentLine);
        }
        }
        } catch (IOException e) {
        e.printStackTrace();
        }catch(Exception ex){
        ex.printStackTrace();
        }
        }
}