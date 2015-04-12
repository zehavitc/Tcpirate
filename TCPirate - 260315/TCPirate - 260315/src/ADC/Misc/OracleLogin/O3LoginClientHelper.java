// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   O3LoginClientHelper

package ADC.Misc.OracleLogin;


// Referenced classes of package oracle.security.o3logon:
//            C1

public final class O3LoginClientHelper
{

    public O3LoginClientHelper()
    {
        a = true;
        b = new C1();
    }

    public byte[] getEPasswd(byte abyte0[], byte abyte1[])
    {
        return b.c(abyte0, abyte1);
    }

    public O3LoginClientHelper(boolean flag)
    {
        a = flag;
        b = new C1();
    }

    public byte[] getSessionKey(String s, String s1, byte abyte0[])
    {
        byte abyte1[] = b.m(s, s1, a);
        return b.j(abyte1, abyte0);
    }

    private boolean a;
    private C1 b;
}
