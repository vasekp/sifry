package cz.absolutno.sifry.common.decoder;

import java.util.ArrayList;

import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public class AlphabetDecoder extends Decoder {

    private Alphabet abc;

    public AlphabetDecoder(Alphabet abc) {
        this.abc = abc;
    }

    @SuppressWarnings("SameParameterValue")
    protected AlphabetDecoder(int cnt, String var) {
        abc = Alphabet.getVariantInstance(cnt, var);
    }

    @SuppressWarnings("SameParameterValue")
    protected final void setVar(int cnt, String var) {
        abc = Alphabet.getVariantInstance(cnt, var);
    }

    @Override
    public final String decode(int x) {
        return abc.chr(x);
    }

    @Override
    protected final boolean encodeInternal(String s, ArrayList<Integer> list) {
        StringParser sp = abc.getStringParser(s);
        boolean err = false;
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                err = true;
                continue;
            }
            list.add(ord);
        }
        return err;
    }

}
