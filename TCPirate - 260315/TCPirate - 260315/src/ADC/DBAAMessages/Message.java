package ADC.DBAAMessages; /**
 * Created by IntelliJ IDEA.
 * User: tomg
 * Date: 09/09/2012
 * Time: 12:35:47
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


public abstract class Message {

    public abstract void Execute();

        public static void SendMessage(OutputStream m_os, Message message)
        {
            try {

                BufferedOutputStream stream = new BufferedOutputStream(m_os);
                
                // serialize object to json string
                Gson gson = new Gson();
                String json =  gson.toJson(message, message.getClass());

                // The following two functions adjust GSON parsing for JSON.Net parsing
                json = appendType(json,message);
                json = fixSideChannelType(json);
                //json = addSlashes(json);

                // compute message length in bytes and build header
                // Total header length: 10 bytes
                // message length: 2 bytes
                byte[] header = new byte[10];
                byte[] futureBytes = new byte[8];

                int messageSizeInt = json.length();
                header[0] = (byte)(messageSizeInt & 0xff);
                header[1] = (byte)((messageSizeInt >> 8) & 0xff);

                //byte[] messageSizeBytes = BigInteger.valueOf(messageSizeInt).toByteArray();
                //System.out.println(String.format("{0:x2}", messageSizeBytes));

                //System.arraycopy(messageSizeBytes, 0, header, 0, messageSizeBytes.length);
                System.arraycopy(futureBytes, 0, header, 2, futureBytes.length);

                // send header and then message
                stream.write(header, 0, header.length);
                stream.write(json.getBytes(Charset.forName("UTF-8")));
                stream.flush();
            }

            catch (Exception e) {
                System.out.println(e.toString());            }

        }

    private static String fixSideChannelType(String json) {
        json = json.replaceFirst("\"IM\"","1");
        return json;
    }

    private static String fixStatusCode(String json) {
        //String[] parts = json.split("statusCode\":")
        int indexOfStatusCode = json.indexOf("\"statusCode\":")+13;
        if (json.charAt(indexOfStatusCode) == '0')
            json = json.substring(0,indexOfStatusCode) + "\"PassedChallenge\"" + json.substring(indexOfStatusCode+1,json.length());
        else if (json.charAt(indexOfStatusCode) == '1')
            json = json.substring(0,indexOfStatusCode) + "\"FailedChallenge\"" + json.substring(indexOfStatusCode+1,json.length());
        return json;
    }

    private static String addSlashes(String json) {

        String[] parts = json.split("\\\"");

        StringBuilder sb = new StringBuilder();

        for (int i=0; i<parts.length-1; i++) {
            sb.append(parts[i]);
            sb.append("\\\"");
        }
        sb.append(parts[parts.length-1]);
        return sb.toString();
    }

    private static String appendType(String json, Message message) {

        String class_name = message.getClass().getName();
        String[] parts = class_name.split("\\.");

        json = "{\"$type\":\"DBAA.Messages." + parts[parts.length-1] + ", DBAAMessages\"," + json.substring(1);

        return json;
    }

    public static Message ReadMessage(InputStream m_is)
        {
            try {

                BufferedInputStream stream = new BufferedInputStream(m_is);
                
                // Read header
                byte[] header = new byte[10];
                int bytesRead = 0;
                bytesRead = stream.read(header, 0, 10);

                // Extract message size from header
                //byte[] messageSizeBytes = new byte[2];
                byte lb = header[0];
                byte hb = header[1];
                ByteBuffer messageSizeBytes = ByteBuffer.wrap(new byte[] {hb, lb});
                Short messageSizeInt = messageSizeBytes.getShort();

                int bytesLeftToRead = messageSizeInt;

                byte[] buffer = new byte[1024];

                StringBuilder stringBuilder = new StringBuilder();

                while (bytesLeftToRead > 0)
                {
                    bytesRead = stream.read(buffer, 0, Math.min(buffer.length, bytesLeftToRead));
                    bytesLeftToRead = bytesLeftToRead - bytesRead;
                    //stringBuilder.append(Encoding.UTF8.GetString(buffer, 0, bytesRead));
                    stringBuilder.append(new String(buffer,0,bytesRead,"UTF-8"));
                }

                String json = stringBuilder.toString();

                json = fixStatusCode(json);
                Gson gson = new Gson();

                return gson.fromJson(json,Status.class);
            }
            catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        }

}
