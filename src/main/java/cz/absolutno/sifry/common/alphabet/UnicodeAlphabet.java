package cz.absolutno.sifry.common.alphabet;


@SuppressWarnings("unused")
public final class UnicodeAlphabet extends Alphabet {

    public UnicodeAlphabet() {
    }

    @Override
    public final int count() {
        return -1;
    }

    @Override
    public String chr(int i) {
        return Character.toString((char) i);
    }

    @Override
    public int ord(String s) {
        if (Character.codePointCount(s, 0, s.length()) != 1)
            return -1;
        else
            return Character.codePointAt(s, 0);
    }

    @Override
    public StringParser getStringParser(String s) {
        return new UnicodeStringParser(s);
    }


    public static final class UnicodeStringParser extends StringParser {

        public UnicodeStringParser(String s) {
            super(s);
        }

        @Override
        protected Ret next() {
            return new Ret(Character.codePointAt(s, ix++));
        }
    }
}