package cz.absolutno.sifry.common.alphabet;

public final class AsciiAlphabet extends Alphabet {

    @Override
    public int count() {
        return 128;
    }

    @Override
    public String chr(int i) {
        if (i >= 32 && i < 128)
            return String.valueOf((char) i);
        else
            return "?";
    }

    @Override
    public int ord(String c) {
        if (c.length() != 1)
            return -1;
        char ch = c.charAt(0);
        if (ch >= 32 && ch < 128)
            return ch;
        else
            return -1;
    }

    @Override
    public StringParser getStringParser(String s) {
        return new AsciiStringParser(s);
    }


    protected static final class AsciiStringParser extends StringParser {

        AsciiStringParser(String s) {
            super(s);
            this.s = s; /* skip toUpperCase() */
        }

        @Override
        protected Ret next() {
            char c = s.charAt(ix++);
            return new Ret((c >= 32 && c < 128) ? (int) c : ERR, c);
        }

    }

}
