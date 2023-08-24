package cz.absolutno.sifry.tabulky.mobil;

import java.util.ArrayList;

import cz.absolutno.sifry.common.alphabet.PlainEnglishAlphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;
import cz.absolutno.sifry.common.decoder.Decoder;

@SuppressWarnings("PointlessBitwiseExpression")
public final class MobilDecoder extends Decoder {

    public static final int KEY_SHIFT = 4;
    public static final int KEY_MASK = 0xF0;
    public static final int REP_SHIFT = 0;
    public static final int REP_MASK = 0x0F;

    @Override
    public String decode(int x) {
        int key = (x & KEY_MASK) >> KEY_SHIFT;
        int rep = (x & REP_MASK) >> REP_SHIFT;
        return getLetter(key, rep);
    }

    @Override
    protected boolean encodeInternal(String s, ArrayList<Integer> list) {
        PlainEnglishAlphabet abc = new PlainEnglishAlphabet();
        StringParser sp = abc.getStringParser(s);
        boolean err = false;
        int ord;
        A:
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                err = true;
                continue;
            }
            int ix;
            for (int j = 0; j < klav.length; j++)
                if ((ix = klav[j].indexOf(abc.chr(ord))) >= 0) {
                    list.add((j << KEY_SHIFT) | (ix << REP_SHIFT));
                    continue A;
                }
            /* Not found */
            err = true;
        }
        return err;
    }

    public int getNumLetters(int key) {
        return klav[key].length();
    }

    public String getLetter(int key, int rep) {
        if (key < 0 || key >= 9)
            return null;
        if (rep >= getNumLetters(key))
            return null;
        else
            return klav[key].substring(rep, rep + 1);
    }

    public String[] getLetters(int key) {
        int num = getNumLetters(key);
        String[] arr = new String[num];
        for (int i = 0; i < num; i++)
            arr[i] = klav[key].substring(i, i + 1);
        return arr;
    }

    private static final String[] klav = new String[]{
            "", "ABC", "DEF",
            "GHI", "JKL", "MNO",
            "PQRS", "TUV", "WXYZ"
    };

}
