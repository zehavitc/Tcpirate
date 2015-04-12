package ADC.DBAAMessages;

import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Created by IntelliJ IDEA.
 * User: tomg
 * Date: 09/09/2012
 * Time: 14:13:14
 * To change this template use File | Settings | File Templates.
 */

public class Hash {

    public enum HashType {MD5, SHA1, SHA256, SHA512}

    public static String GetHash(String text, HashType hashType)
    {
        try {
            MessageDigest algorithm = null;
            switch (hashType)
            {
                case MD5:
                    algorithm = MessageDigest.getInstance("MD5");
                    break;
                case SHA1:
                    algorithm = MessageDigest.getInstance("SHA-1");
                    break;
                case SHA256:
                    algorithm = MessageDigest.getInstance("SHA-256");
                    break;
                case SHA512:
                    algorithm = MessageDigest.getInstance("SHA-512");
                    break;
            }

            byte[] bytes = text.getBytes("UTF-8");
            algorithm.update(bytes);
            byte[] hash_tmp = algorithm.digest();
            byte[] hash = new byte[hash_tmp.length / 2];
            System.arraycopy(hash_tmp, 0, hash, 0, hash.length);

            String hashString =  bytesToHexString(hash);

            return hashString;
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }


    public static boolean CheckHash(String original, String hashString, HashType hashType)
    {
        String originalHash = GetHash(original, hashType);
        return (originalHash == hashString);
    }

}