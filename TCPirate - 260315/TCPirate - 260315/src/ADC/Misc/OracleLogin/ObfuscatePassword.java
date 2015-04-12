/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 17/08/2005
 * Time: 18:16:44
 * To change this template use File | Settings | File Templates.
 */
package ADC.Misc.OracleLogin;

import oracle.jdbc.ttc7.*;
import oracle.jdbc.util.RepConversion;
import oracle.security.o3logon.*;

public class ObfuscatePassword {
    private static ObfuscatePassword ourInstance = null;

    public static ObfuscatePassword getInstance() {
        if (ourInstance == null)
            ourInstance = new ObfuscatePassword();

        return ourInstance;
    }

    private ObfuscatePassword() {
    }

    public static byte[] obfuscate(String user, String pwd, byte byte_key[]) {
        boolean multi_byte = false;

        TTCConversion conv;

        try {
            conv = new TTCConversion((short)31, (short)-1, (short)8030, (short)31);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

//        byte[] key = new byte[len];

//        byte[] byte_key = new byte[key_string.length()];
//        for (int index = 0; index < key_string.length(); index++)
//            byte_key[index] = (byte)((int)(key_string.charAt(index) & 0xFF));

        //byte[] key = RepConversion.nibbles2bArray(byte_key);
        byte[] key = byte_key;

        O3LoginClientHelper O3LHelper = new O3LoginClientHelper(multi_byte);
        byte[] sessionKey = O3LHelper.getSessionKey(user, pwd, key);

        byte passwordNet[];

        try {
            passwordNet = conv.StringToNetworkRep(pwd);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

        byte pwdPadLen;
        if(passwordNet.length % 8 > 0)
            pwdPadLen = (byte)(8 - passwordNet.length % 8);
        else
            pwdPadLen = 0;
        byte paddedPwd[] = new byte[passwordNet.length + pwdPadLen];
        System.arraycopy(passwordNet, 0, paddedPwd, 0, passwordNet.length);

        byte ePwdOnSessKey[] = O3LHelper.getEPasswd(sessionKey, paddedPwd);

        byte[] password = new byte[2 * paddedPwd.length + 1];
        if(password.length < 2 * ePwdOnSessKey.length)
            throw new RuntimeException("Problem with password len");
        RepConversion.bArray2nibbles(ePwdOnSessKey, password);

        password[password.length - 1] = RepConversion.nibbleToHex(pwdPadLen);

        return password;
    }

    public static void obfuscate(String user, String pwd, String key_string) {
        boolean multi_byte = false;

        TTCConversion conv;

        try {
            conv = new TTCConversion((short)31, (short)-1, (short)8030, (short)31);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

//        byte[] key = new byte[len];

        byte[] byte_key = new byte[key_string.length()];
        for (int index = 0; index < key_string.length(); index++)
            byte_key[index] = (byte)((int)(key_string.charAt(index) & 0xFF));

//        byte[] key = RepConversion.nibbles2bArray(byte_key);
        byte[] key = byte_key;

        O3LoginClientHelper O3LHelper = new O3LoginClientHelper(multi_byte);
        byte[] sessionKey = O3LHelper.getSessionKey(user, pwd, key);

        byte passwordNet[];

        try {
            passwordNet = conv.StringToNetworkRep(pwd);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

        byte pwdPadLen;
        if(passwordNet.length % 8 > 0)
            pwdPadLen = (byte)(8 - passwordNet.length % 8);
        else
            pwdPadLen = 0;
        byte paddedPwd[] = new byte[passwordNet.length + pwdPadLen];
        System.arraycopy(passwordNet, 0, paddedPwd, 0, passwordNet.length);

        byte ePwdOnSessKey[] = O3LHelper.getEPasswd(sessionKey, paddedPwd);

        byte[] password = new byte[2 * paddedPwd.length + 1];
        if(password.length < 2 * ePwdOnSessKey.length)
            throw new RuntimeException("Problem with password len");
        RepConversion.bArray2nibbles(ePwdOnSessKey, password);

        password[password.length - 1] = RepConversion.nibbleToHex(pwdPadLen);

        for (int index = 0; index < password.length; index++)
            System.out.print((char)(password[index]));

        System.out.println();
    }

    public static void main(String[] argv) {
        ObfuscatePassword obj = getInstance();

        obfuscate(argv[0], argv[1], argv[2]);
    }
}
