package cz.absolutno.sifry.common.alphabet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;


public final class EnglishAlphabet extends Alphabet {

    private final boolean j, k, q, x;

    @SuppressWarnings("unused")
    public EnglishAlphabet() {
        j = true;
        k = true;
        q = true;
        x = true;
    }

    private EnglishAlphabet(boolean j, boolean k, boolean q, boolean x) {
        this.j = j;
        this.k = k;
        this.q = q;
        this.x = x;
    }

    public EnglishAlphabet(SharedPreferences sp) {
        this.j = sp.getBoolean("pref_abc_j", true);
        this.k = true; /* Only for the Tap code */
        this.q = sp.getBoolean("pref_abc_q", true);
        this.x = sp.getBoolean("pref_abc_x", true);
    }

    public static Alphabet getVariantInstance(int maxCount, String variant) {
        switch (maxCount) {
            case 36:
                if (variant.equals("dig"))
                    return new DigitsExtendedAlphabet<>(new PlainEnglishAlphabet());
                else
                    return new PlainEnglishAlphabet();
            case 25:
                switch (variant) {
                    case "J":
                        return new EnglishAlphabet(false, true, true, true);
                    case "K":
                        return new EnglishAlphabet(true, false, true, true);
                    case "X":
                        return new EnglishAlphabet(true, true, true, false);
                    default:
                        return new EnglishAlphabet(true, true, false, true);
                }
            case 24:
                switch (variant) {
                    case "JQ":
                        return new EnglishAlphabet(false, true, false, true);
                    case "JX":
                        return new EnglishAlphabet(false, true, true, false);
                    default:
                        return new EnglishAlphabet(true, true, false, false);
                }
            default:
                return new PlainEnglishAlphabet();
        }
    }

    @Override
    public final int count() {
        return 26 - (j ? 0 : 1) - (k ? 0 : 1) - (q ? 0 : 1) - (x ? 0 : 1);
    }

    @Override
    public String chr(int i) {
        if (i < 0 || i >= count()) return "?";
        i++;
        if (!j && i >= 10) i++;
        if (!k && i >= 11) i++;
        if (!q && i >= 17) i++;
        if (!x && i >= 24) i++;
        return String.valueOf((char) ('A' + i - 1));
    }

    @Override
    @SuppressLint("DefaultLocale")
    public int ord(String c) {
        if (c.length() != 1) return StringParser.ERR;
        else return ord(c.toUpperCase().charAt(0));
    }

    private int ord(char c) {
        if (c < 'A' || c > 'Z' || (c == 'Q' && !q) || (c == 'X' && !x))
            return StringParser.ERR;
        if (!j && c == 'J') c = 'I';
        if (!k && c == 'K') c = 'C';
        int o = c - 'A' + 1;
        if (!x && o >= 24) o--;
        if (!q && o >= 17) o--;
        if (!k && o >= 11) o--;
        if (!j && o >= 10) o--;
        return o - 1;
    }

    @Override
    public StringParser getStringParser(String s) {
        return new EnglishStringParser(s);
    }


    protected final class EnglishStringParser extends StringParser {

        EnglishStringParser(String s) {
            super(s);
        }

        @Override
        protected Ret next() {
            char c = s.charAt(ix++);
            return new Ret(ord(c), c);
        }
    }
}
