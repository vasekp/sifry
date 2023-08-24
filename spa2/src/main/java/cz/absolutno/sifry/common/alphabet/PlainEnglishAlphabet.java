package cz.absolutno.sifry.common.alphabet;


public final class PlainEnglishAlphabet extends Alphabet {

    public PlainEnglishAlphabet() {
    }

    @Override
    public final int count() {
        return 26;
    }

    @Override
    public String chr(int i) {
        if (i < 0 || i >= 26) return "?";
        return String.valueOf((char) ('A' + i));
    }

    @Override
    public int ord(String s) {
        if (s.length() != 1)
            return -1;
        else {
            char c = Character.toUpperCase(s.charAt(0));
            if (c < 'A' || c > 'Z')
                return -1;
            else
                return c - 'A';
        }
    }

    @Override
    public StringParser getStringParser(String s) {
        return new PlainEnglishStringParser(s);
    }


    protected static final class PlainEnglishStringParser extends StringParser {

        PlainEnglishStringParser(String s) {
            super(s);
        }

        @Override
        protected Ret next() {
            char c = s.charAt(ix++);
            if (c >= 'A' && c <= 'Z')
                return new Ret(c - 'A');
            else
                return new Ret(ERR, c);
        }
    }
}