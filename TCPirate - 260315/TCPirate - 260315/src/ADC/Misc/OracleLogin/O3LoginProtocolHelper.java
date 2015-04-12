// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   O3LoginProtocolHelper

package ADC.Misc.OracleLogin;

import java.security.SecureRandom;

// Referenced classes of package oracle.security.o3logon:
//            C1, C0

public final class O3LoginProtocolHelper
{

    public byte[] getChallenge(byte abyte0[])
    {
        SecureRandom securerandom = null;
        securerandom = new SecureRandom(abyte0);
        c += System.currentTimeMillis();
        securerandom.setSeed(c);
        securerandom.setSeed(b);
        securerandom.nextBytes(f);
        C0 c0 = new C0();
        byte abyte1[] = c0.b(b, f, f.length);
        return abyte1;
    }

    public static byte[] getResponse(String s, String s1, byte abyte0[])
    {
        if(e == null)
            e = new C1();
        byte abyte1[] = e.g(s, s1);
        C0 c0 = new C0();
        byte abyte3[] = c0.h(abyte1, abyte0);
        byte abyte4[] = s1.getBytes();
        byte byte0;
        if(abyte4.length % 8 > 0)
            byte0 = (byte)(8 - abyte4.length % 8);
        else
            byte0 = 0;
        byte abyte2[] = new byte[abyte4.length + byte0];
        System.arraycopy(abyte4, 0, abyte2, 0, abyte4.length);
        byte abyte5[] = c0.b(abyte3, abyte2, abyte2.length);
        byte abyte6[] = new byte[abyte5.length + 1];
        System.arraycopy(abyte5, 0, abyte6, 0, abyte5.length);
        abyte6[abyte6.length - 1] = byte0;
        return abyte6;
    }

    public byte[] getVerifier(String s, String s1)
    {
        if(e == null)
            e = new C1();
        return e.g(s, s1);
    }

    public String getPassword(byte abyte0[])
    {
        byte byte0 = -1;
        C0 c0 = new C0();
        byte byte1 = abyte0[abyte0.length - 1];
        byte abyte1[] = new byte[abyte0.length - 1];
        System.arraycopy(abyte0, 0, abyte1, 0, abyte1.length);
        byte abyte2[] = null;
        try
        {
            abyte2 = c0.h(f, abyte1);
        }
        catch(Exception exception)
        {
            return null;
        }
        byte abyte3[] = new byte[abyte2.length - byte1];
        System.arraycopy(abyte2, 0, abyte3, 0, abyte3.length);
        String s = (new String(abyte3)).toUpperCase();
        return s;
    }

    public boolean authenticate(String s, String s1)
    {
        try
        {
            Thread.sleep(a * 1000);
        }
        catch(InterruptedException interruptedexception) { }
        if(e == null)
            e = new C1();
        byte abyte0[] = e.g(s, s1);
        if(b.length != abyte0.length)
        {
            a++;
            return false;
        }
        for(int i = 0; i < abyte0.length; i++)
            if(abyte0[i] != b[i])
            {
                a++;
                return false;
            }

        return true;
    }

    public O3LoginProtocolHelper()
    {
        f = new byte[8];
        b = null;
    }

    public O3LoginProtocolHelper(byte abyte0[])
    {
        f = new byte[8];
        b = abyte0;
    }

    private static int a = 0;
    private final byte b[];
    private static long c = System.currentTimeMillis();
    private static final boolean d = false;
    private static C1 e;
    private final byte f[];

}
