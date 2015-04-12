// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   C0

package ADC.Misc.OracleLogin;

import java.io.PrintStream;

public class C0
{

    private int[] a(int ai[])
    {
        int ai3[] = new int[32];
        int ai1[] = ai3;
        int ai2[] = ai;
        int i1 = 0;
        boolean flag = false;
        int k1 = 0;
        int l1 = 0;
        while(i1 < 16)
        {
            int j1 = k1++;
            ai1[l1] = (ai2[j1] & 0xfc0000) << 6;
            ai1[l1] |= (ai2[j1] & 0xfc0) << 10;
            ai1[l1] |= (ai[k1] & 0xfc0000) >> 10;
            ai1[l1] |= (ai[k1] & 0xfc0) >> 6;
            l1++;
            ai1[l1] = (ai2[j1] & 0x3f000) << 12;
            ai1[l1] |= (ai2[j1] & 0x3f) << 16;
            ai1[l1] |= (ai[k1] & 0x3f000) >> 4;
            ai1[l1] |= ai[k1] & 0x3f;
            l1++;
            i1++;
            k1++;
        }
        return ai1;
    }

    public byte[] b(byte abyte0[], byte abyte1[], int i1)
    {
        l(abyte0);
        byte abyte2[] = new byte[8];
        int j1 = 8 - abyte1.length % 8;
        byte abyte3[] = new byte[abyte1.length + j1];
        System.arraycopy(abyte1, 0, abyte3, 0, abyte1.length);
        for(int k1 = abyte1.length; k1 < abyte3.length; k1++)
            abyte3[k1] = (byte)j1;

        for(int l1 = 0; l1 < abyte3.length / 8; l1++)
        {
            for(int i2 = 0; i2 < 8; i2++)
                abyte2[i2] = abyte3[l1 * 8 + i2];

            e(abyte2, 1);
            System.arraycopy(abyte2, 0, abyte3, l1 * 8, 8);
        }

        return abyte3;
    }

    private void c(int ai[], byte abyte0[])
    {
        int i1 = 0;
        abyte0[i1] = (byte)(ai[0] >> 24 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[0] >> 16 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[0] >> 8 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[0] & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[1] >> 24 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[1] >> 16 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[1] >> 8 & 0xff);
        i1++;
        abyte0[i1] = (byte)(ai[1] & 0xff);
    }

    public void d(byte abyte0[], byte abyte1[], int i1)
    {
        if(abyte1 == null || abyte1.length == 0)
            return;
        int ai[] = new int[2];
        byte abyte2[] = abyte1;
        for(int j1 = 0; j1 < i1; j1++)
        {
            k(abyte2, ai);
            j(ai, r(abyte0, (short)1));
            c(ai, abyte2);
        }

    }

    public C0()
    {
        J = null;
        B = null;
    }

    public void e(byte abyte0[], int i1)
    {
        if(abyte0 == null || abyte0.length == 0)
            return;
        int ai[] = new int[2];
        byte abyte1[] = abyte0;
        for(int j1 = 0; j1 < i1; j1++)
        {
            k(abyte1, ai);
            j(ai, J);
            c(ai, abyte1);
        }

    }

    private void f(byte byte0)
    {
        System.out.print((char)g((byte)((byte0 & 0xf0) >> 4)));
        System.out.print((char)g((byte)(byte0 & 0xf)));
        System.out.print(" ");
    }

    private byte g(byte byte0)
    {
        byte0 &= 0xf;
        return (byte)(byte0 >= 10 ? (byte0 - 10) + 65 : byte0 + 48);
    }

    public byte[] h(byte abyte0[], byte abyte1[])
    {
        l(abyte0);
        byte abyte2[] = new byte[8];
        byte abyte3[] = new byte[abyte1.length];
        for(int i1 = 0; i1 < abyte1.length / 8; i1++)
        {
            for(int j1 = 0; j1 < 8; j1++)
                abyte2[j1] = abyte1[i1 * 8 + j1];

            o(abyte2, 1);
            System.arraycopy(abyte2, 0, abyte3, i1 * 8, 8);
        }

        byte abyte4[] = new byte[abyte3.length - abyte3[abyte3.length - 1]];
        System.arraycopy(abyte3, 0, abyte4, 0, abyte4.length);
        return abyte4;
    }

    private void i()
    {
        J = null;
        B = null;
    }

    private void j(int ai[], int ai1[])
    {
        boolean flag = false;
        int j2 = 0;
        int l1 = ai[0];
        int k1 = ai[1];
        int j1 = (l1 >>> 4 ^ k1) & 0xf0f0f0f;
        k1 ^= j1;
        l1 ^= j1 << 4;
        j1 = (l1 >>> 16 ^ k1) & 0xffff;
        k1 ^= j1;
        l1 ^= j1 << 16;
        j1 = (k1 >>> 2 ^ l1) & 0x33333333;
        l1 ^= j1;
        k1 ^= j1 << 2;
        j1 = (k1 >>> 8 ^ l1) & 0xff00ff;
        l1 ^= j1;
        k1 ^= j1 << 8;
        k1 = (k1 << 1 | k1 >>> 31 & 1) & -1;
        j1 = (l1 ^ k1) & 0xaaaaaaaa;
        l1 ^= j1;
        k1 ^= j1;
        l1 = (l1 << 1 | l1 >>> 31 & 1) & -1;
        for(int i2 = 0; i2 < 8; i2++)
        {
            j1 = k1 << 28 | k1 >>> 4;
            long l2 = 0L;
            l2 = ai1[j2] | 0;
            j1 ^= ai1[j2];
            j2++;
            int i1 = v[j1 & 0x3f];
            i1 |= x[j1 >>> 8 & 0x3f];
            i1 |= A[j1 >>> 16 & 0x3f];
            i1 |= D[j1 >>> 24 & 0x3f];
            j1 = k1 ^ ai1[j2];
            j2++;
            i1 |= E[j1 & 0x3f];
            i1 |= F[j1 >>> 8 & 0x3f];
            i1 |= K[j1 >>> 16 & 0x3f];
            i1 |= L[j1 >>> 24 & 0x3f];
            l1 ^= i1;
            j1 = l1 << 28 | l1 >>> 4;
            j1 ^= ai1[j2];
            j2++;
            i1 = v[j1 & 0x3f];
            i1 |= x[j1 >>> 8 & 0x3f];
            i1 |= A[j1 >>> 16 & 0x3f];
            i1 |= D[j1 >>> 24 & 0x3f];
            j1 = l1 ^ ai1[j2];
            j2++;
            i1 |= E[j1 & 0x3f];
            i1 |= F[j1 >>> 8 & 0x3f];
            i1 |= K[j1 >>> 16 & 0x3f];
            i1 |= L[j1 >>> 24 & 0x3f];
            k1 ^= i1;
        }

        k1 = k1 << 31 | k1 >>> 1;
        j1 = (l1 ^ k1) & 0xaaaaaaaa;
        l1 ^= j1;
        k1 ^= j1;
        l1 = l1 << 31 | l1 >>> 1;
        j1 = (l1 >>> 8 ^ k1) & 0xff00ff;
        k1 ^= j1;
        l1 ^= j1 << 8;
        j1 = (l1 >>> 2 ^ k1) & 0x33333333;
        k1 ^= j1;
        l1 ^= j1 << 2;
        j1 = (k1 >>> 16 ^ l1) & 0xffff;
        l1 ^= j1;
        k1 ^= j1 << 16;
        j1 = (k1 >>> 4 ^ l1) & 0xf0f0f0f;
        l1 ^= j1;
        k1 ^= j1 << 4;
        ai[0] = k1;
        ai[1] = l1;
    }

    private void k(byte abyte0[], int ai[])
    {
        int i1 = 0;
        ai[0] = (abyte0[i1] & 0xff) << 24;
        i1++;
        ai[0] |= (abyte0[i1] & 0xff) << 16;
        i1++;
        ai[0] |= (abyte0[i1] & 0xff) << 8;
        i1++;
        ai[0] |= abyte0[i1] & 0xff;
        i1++;
        ai[1] = (abyte0[i1] & 0xff) << 24;
        i1++;
        ai[1] |= (abyte0[i1] & 0xff) << 16;
        i1++;
        ai[1] |= (abyte0[i1] & 0xff) << 8;
        i1++;
        ai[1] |= abyte0[i1] & 0xff;
    }

    public void l(byte abyte0[])
    {
        J = r(abyte0, (short)0);
        B = r(abyte0, (short)1);
    }

    private void m(byte abyte0[])
    {
        J = r(abyte0, (short)0);
    }

    public void n(byte abyte0[], byte abyte1[], int i1)
    {
        if(abyte1 == null || abyte1.length == 0)
            return;
        int ai[] = new int[2];
        byte abyte2[] = abyte1;
        for(int j1 = 0; j1 < i1; j1++)
        {
            k(abyte2, ai);
            j(ai, r(abyte0, (short)0));
            c(ai, abyte2);
        }

    }

    public void o(byte abyte0[], int i1)
    {
        if(abyte0 == null || abyte0.length == 0)
            return;
        int ai[] = new int[2];
        byte abyte1[] = abyte0;
        for(int j1 = 0; j1 < i1; j1++)
        {
            k(abyte1, ai);
            j(ai, B);
            c(ai, abyte1);
        }

    }

    private void p()
    {
        J = null;
    }

    public byte[] q(byte abyte0[], boolean flag)
    {
        byte abyte1[] = new byte[8];
        byte abyte2[] = new byte[abyte0.length];
        for(int i1 = 0; i1 < abyte0.length / 8; i1++)
        {
            for(int j1 = 0; j1 < 8; j1++)
                abyte1[j1] ^= abyte0[i1 * 8 + j1];

            e(abyte1, 1);
            if(!flag)
                System.arraycopy(abyte1, 0, abyte2, i1 * 8, 8);
        }

        if(flag)
            return abyte1;
        else
            return abyte2;
    }

    private int[] r(byte abyte0[], short word0)
    {
        byte abyte1[] = new byte[56];
        byte abyte2[] = new byte[56];
        int ai[] = new int[32];
        for(int j1 = 0; j1 < 56; j1++)
        {
            byte byte0 = y[j1];
            int l2 = byte0 & 7;
            abyte1[j1] = (byte)((abyte0[byte0 >> 3] & G[l2]) == 0 ? 0 : 1);
        }

        for(int i1 = 0; i1 < 16; i1++)
        {
            int i3;
            if(word0 == 1)
                i3 = 15 - i1 << 1;
            else
                i3 = i1 << 1;
            int j3 = i3 + 1;
            ai[i3] = ai[j3] = 0;
            for(int k1 = 0; k1 < 28; k1++)
            {
                int j2 = k1 + C[i1];
                if(j2 < 28)
                    abyte2[k1] = abyte1[j2];
                else
                    abyte2[k1] = abyte1[j2 - 28];
            }

            for(int l1 = 28; l1 < 56; l1++)
            {
                int k2 = l1 + C[i1];
                if(k2 < 56)
                    abyte2[l1] = abyte1[k2];
                else
                    abyte2[l1] = abyte1[k2 - 28];
            }

            for(int i2 = 0; i2 < 24; i2++)
            {
                if(abyte2[H[i2]] != 0)
                    ai[i3] |= I[i2];
                if(abyte2[H[i2 + 24]] != 0)
                    ai[j3] |= I[i2];
            }

        }

        return a(ai);
    }

    private void s()
    {
        B = null;
    }

    private void t(byte abyte0[])
    {
        B = r(abyte0, (short)1);
    }

    static final byte u = 1;
    private static final int v[] = {
        0x200000, 0x4200002, 0x4000802, 0, 2048, 0x4000802, 0x200802, 0x4200800, 0x4200802, 0x200000,
        0, 0x4000002, 2, 0x4000000, 0x4200002, 2050, 0x4000800, 0x200802, 0x200002, 0x4000800,
        0x4000002, 0x4200000, 0x4200800, 0x200002, 0x4200000, 2048, 2050, 0x4200802, 0x200800, 2,
        0x4000000, 0x200800, 0x4000000, 0x200800, 0x200000, 0x4000802, 0x4000802, 0x4200002, 0x4200002, 2,
        0x200002, 0x4000000, 0x4000800, 0x200000, 0x4200800, 2050, 0x200802, 0x4200800, 2050, 0x4000002,
        0x4200802, 0x4200000, 0x200800, 0, 2, 0x4200802, 0, 0x200802, 0x4200000, 2048,
        0x4000002, 0x4000800, 2048, 0x200002
    };
    static final byte w = 0;
    private static final int x[] = {
        256, 0x2080100, 0x2080000, 0x42000100, 0x80000, 256, 0x40000000, 0x2080000, 0x40080100, 0x80000,
        0x2000100, 0x40080100, 0x42000100, 0x42080000, 0x80100, 0x40000000, 0x2000000, 0x40080000, 0x40080000, 0,
        0x40000100, 0x42080100, 0x42080100, 0x2000100, 0x42080000, 0x40000100, 0, 0x42000000, 0x2080100, 0x2000000,
        0x42000000, 0x80100, 0x80000, 0x42000100, 256, 0x2000000, 0x40000000, 0x2080000, 0x42000100, 0x40080100,
        0x2000100, 0x40000000, 0x42080000, 0x2080100, 0x40080100, 256, 0x2000000, 0x42080000, 0x42080100, 0x80100,
        0x42000000, 0x42080100, 0x2080000, 0, 0x40080000, 0x42000000, 0x80100, 0x2000100, 0x40000100, 0x80000,
        0, 0x40080000, 0x2080100, 0x40000100
    };
    private static final byte y[] = {
        56, 48, 40, 32, 24, 16, 8, 0, 57, 49,
        41, 33, 25, 17, 9, 1, 58, 50, 42, 34,
        26, 18, 10, 2, 59, 51, 43, 35, 62, 54,
        46, 38, 30, 22, 14, 6, 61, 53, 45, 37,
        29, 21, 13, 5, 60, 52, 44, 36, 28, 20,
        12, 4, 27, 19, 11, 3
    };
    private final boolean z = false;
    private static final int A[] = {
        520, 0x8020200, 0, 0x8020008, 0x8000200, 0, 0x20208, 0x8000200, 0x20008, 0x8000008,
        0x8000008, 0x20000, 0x8020208, 0x20008, 0x8020000, 520, 0x8000000, 8, 0x8020200, 512,
        0x20200, 0x8020000, 0x8020008, 0x20208, 0x8000208, 0x20200, 0x20000, 0x8000208, 8, 0x8020208,
        512, 0x8000000, 0x8020200, 0x8000000, 0x20008, 520, 0x20000, 0x8020200, 0x8000200, 0,
        512, 0x20008, 0x8020208, 0x8000200, 0x8000008, 512, 0, 0x8020008, 0x8000208, 0x20000,
        0x8000000, 0x8020208, 8, 0x20208, 0x20200, 0x8000008, 0x8020000, 0x8000208, 520, 0x8020000,
        0x20208, 8, 0x8020008, 0x20200
    };
    private int B[];
    private static final byte C[] = {
        1, 2, 4, 6, 8, 10, 12, 14, 15, 17,
        19, 21, 23, 25, 27, 28
    };
    private static final int D[] = {
        0x1010400, 0, 0x10000, 0x1010404, 0x1010004, 0x10404, 4, 0x10000, 1024, 0x1010400,
        0x1010404, 1024, 0x1000404, 0x1010004, 0x1000000, 4, 1028, 0x1000400, 0x1000400, 0x10400,
        0x10400, 0x1010000, 0x1010000, 0x1000404, 0x10004, 0x1000004, 0x1000004, 0x10004, 0, 1028,
        0x10404, 0x1000000, 0x10000, 0x1010404, 4, 0x1010000, 0x1010400, 0x1000000, 0x1000000, 1024,
        0x1010004, 0x10000, 0x10400, 0x1000004, 1024, 4, 0x1000404, 0x10404, 0x1010404, 0x10004,
        0x1010000, 0x1000404, 0x1000004, 1028, 0x10404, 0x1010400, 1028, 0x1000400, 0x1000400, 0,
        0x10004, 0x10400, 0, 0x1010004
    };
    private static final int E[] = {
        0x10001040, 4096, 0x40000, 0x10041040, 0x10000000, 0x10001040, 64, 0x10000000, 0x40040, 0x10040000,
        0x10041040, 0x41000, 0x10041000, 0x41040, 4096, 64, 0x10040000, 0x10000040, 0x10001000, 4160,
        0x41000, 0x40040, 0x10040040, 0x10041000, 4160, 0, 0, 0x10040040, 0x10000040, 0x10001000,
        0x41040, 0x40000, 0x41040, 0x40000, 0x10041000, 4096, 64, 0x10040040, 4096, 0x41040,
        0x10001000, 64, 0x10000040, 0x10040000, 0x10040040, 0x10000000, 0x40000, 0x10001040, 0, 0x10041040,
        0x40040, 0x10000040, 0x10040000, 0x10001000, 0x10001040, 0, 0x10041040, 0x41000, 0x41000, 4160,
        4160, 0x40040, 0x10000000, 0x10041000
    };
    private static final int F[] = {
        0x20000010, 0x20400000, 16384, 0x20404010, 0x20400000, 16, 0x20404010, 0x400000, 0x20004000, 0x404010,
        0x400000, 0x20000010, 0x400010, 0x20004000, 0x20000000, 16400, 0, 0x400010, 0x20004010, 16384,
        0x404000, 0x20004010, 16, 0x20400010, 0x20400010, 0, 0x404010, 0x20404000, 16400, 0x404000,
        0x20404000, 0x20000000, 0x20004000, 16, 0x20400010, 0x404000, 0x20404010, 0x400000, 16400, 0x20000010,
        0x400000, 0x20004000, 0x20000000, 16400, 0x20000010, 0x20404010, 0x404000, 0x20400000, 0x404010, 0x20404000,
        0, 0x20400010, 16, 16384, 0x20400000, 0x404010, 16384, 0x400010, 0x20004010, 0,
        0x20404000, 0x20000000, 0x400010, 0x20004010
    };
    private static final short G[] = {
        128, 64, 32, 16, 8, 4, 2, 1
    };
    private static final byte H[] = {
        13, 16, 10, 23, 0, 4, 2, 27, 14, 5,
        20, 9, 22, 18, 11, 3, 25, 7, 15, 6,
        26, 19, 12, 1, 40, 51, 30, 36, 46, 54,
        29, 39, 50, 44, 32, 47, 43, 48, 38, 55,
        33, 52, 45, 41, 49, 35, 28, 31
    };
    private static final int I[] = {
        0x800000, 0x400000, 0x200000, 0x100000, 0x80000, 0x40000, 0x20000, 0x10000, 32768, 16384,
        8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16,
        8, 4, 2, 1
    };
    private int J[];
    private static final int K[] = {
        0x802001, 8321, 8321, 128, 0x802080, 0x800081, 0x800001, 8193, 0, 0x802000,
        0x802000, 0x802081, 129, 0, 0x800080, 0x800001, 1, 8192, 0x800000, 0x802001,
        128, 0x800000, 8193, 8320, 0x800081, 1, 8320, 0x800080, 8192, 0x802080,
        0x802081, 129, 0x800080, 0x800001, 0x802000, 0x802081, 129, 0, 0, 0x802000,
        8320, 0x800080, 0x800081, 1, 0x802001, 8321, 8321, 128, 0x802081, 129,
        1, 8192, 0x800001, 8193, 0x802080, 0x800081, 8193, 8320, 0x800000, 0x802001,
        128, 0x800000, 8192, 0x802080
    };
    private static final int L[] = {
        0x80108020, 0x80008000, 32768, 0x108020, 0x100000, 32, 0x80100020, 0x80008020, 0x80000020, 0x80108020,
        0x80108000, 0x80000000, 0x80008000, 0x100000, 32, 0x80100020, 0x108000, 0x100020, 0x80008020, 0,
        0x80000000, 32768, 0x108020, 0x80100000, 0x100020, 0x80000020, 0, 0x108000, 32800, 0x80108000,
        0x80100000, 32800, 0, 0x108020, 0x80100020, 0x100000, 0x80008020, 0x80100000, 0x80108000, 32768,
        0x80100000, 0x80008000, 32, 0x80108020, 0x108020, 32, 32768, 0x80000000, 32800, 0x80108000,
        0x100000, 0x80000020, 0x100020, 0x80008020, 0x80000020, 0x100020, 0x108000, 0, 0x80008000, 32800,
        0x80000000, 0x80100020, 0x80108020, 0x108000
    };
    private final boolean M = false;

}
