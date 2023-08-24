package cz.absolutno.sifry.common.alphabet;

@SuppressWarnings("unused")
public class DigitsExtendedAlphabet<T extends Alphabet> extends Alphabet {

    private final T abcOrig;
    private final int cntOrig;

    public DigitsExtendedAlphabet(T abc) {
        this.abcOrig = abc;
        cntOrig = abc.count();
    }

    @Override
    public int count() {
        return cntOrig + 10;
    }

    @Override
    public String chr(int i) {
        if (i < cntOrig)
            return abcOrig.chr(i);
        else {
            i -= cntOrig;
            if (i < 10)
                return String.valueOf((char) ('0' + i));
            else
                return null;
        }
    }

    @Override
    public int ord(String c) {
        if (c.length() == 1) {
            char cv = c.charAt(0);
            if (cv >= '0' && cv <= '9')
                return cntOrig + cv - '0';
        }
        return abcOrig.ord(c);
    }

    public boolean isDigit(int ord) {
        return (ord >= cntOrig && ord < cntOrig + 10);
    }

    /* Omits a second check and returns nonsense for not-digit ords. */
    public int getDigit(int ord) {
        return ord - cntOrig;
    }

    @Override
    public StringParser getStringParser(String s) {
        return new NumberAddedStringParser(s);
    }


    public final class NumberAddedStringParser extends StringParser {

        private final StringParser spOrig;

        public NumberAddedStringParser(String s) {
            super("");
            len = 1; /* aby getNextOrd() nevracela EOF */
            spOrig = abcOrig.getStringParser(s);
        }

        @Override
        protected Ret next() {
            int ord = spOrig.getNextOrd();
            if (ord != ERR)
                return new Ret(ord);
            else {
                char c = spOrig.getLastChar();
                if (c >= '0' && c <= '9')
                    return new Ret(cntOrig + c - '0');
                else
                    return new Ret(ERR, c);
            }
        }
    }

}
