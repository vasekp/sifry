package cz.absolutno.sifry.common.alphabet;

import android.content.SharedPreferences;

import java.util.Locale;


public final class CzechAlphabet extends Alphabet {
    private final boolean ch, j, q, w, x;
    private static final Locale locale = new Locale("cs");

    private CzechAlphabet() {
        ch = false;
        j = true;
        q = true;
        w = true;
        x = true;
    }

    @SuppressWarnings("SameParameterValue")
    private CzechAlphabet(boolean ch) {
        this.ch = ch;
        j = true;
        q = true;
        w = true;
        x = true;
    }

    @SuppressWarnings("SameParameterValue")
    private CzechAlphabet(boolean ch, boolean j, boolean q, boolean w, boolean x) {
        this.ch = ch;
        this.j = j;
        this.q = q;
        this.w = w;
        this.x = x;
    }

    public CzechAlphabet(SharedPreferences sp) {
        this.ch = sp.getBoolean("pref_abc_ch", false);
        this.j = true;
        this.q = sp.getBoolean("pref_abc_q", true);
        this.w = sp.getBoolean("pref_abc_w", true);
        this.x = sp.getBoolean("pref_abc_x", true);
    }

    public static Alphabet getVariantInstance(int maxCount, String variant) {
        switch (maxCount) {
            case 36:
                if (variant.equals("dig"))
                    return new DigitsExtendedAlphabet<>(new PlainEnglishAlphabet());
                else
                    return new CzechAlphabet();
            case 27:
                if (variant.equals("-Ch"))
                    return new CzechAlphabet();
                else
                    return new CzechAlphabet(true);
            case 26:
                return new CzechAlphabet();
            case 25:
                switch (variant) {
                    case "J":
                        return new CzechAlphabet(false, false, true, true, true);
                    case "W":
                        return new CzechAlphabet(false, true, true, false, true);
                    case "X":
                        return new CzechAlphabet(false, true, true, true, false);
                    default:
                        return new CzechAlphabet(false, true, false, true, true);
                }
            case 24:
                switch (variant) {
                    case "WX":
                        return new CzechAlphabet(false, true, true, false, false);
                    case "QX":
                        return new CzechAlphabet(false, true, false, true, false);
                    default:
                        return new CzechAlphabet(false, true, false, false, true);
                }
            default:
                switch (variant) {
                    case "Full+Ch":
                        return new FullCzechAlphabet(true);
                    case "Full-Ch":
                        return new FullCzechAlphabet(false);
                    case "+Ch":
                        return new CzechAlphabet(true);
                    default:
                        return new CzechAlphabet();
                }
        }
    }

    @Override
    public final int count() {
        return 26 - (j ? 0 : 1) - (q ? 0 : 1) - (w ? 0 : 1) - (x ? 0 : 1) + (ch ? 1 : 0);
    }

    @Override
    public String chr(int i) {
        if (i < 0 || i >= count()) return "?";
        i++;
        if (ch) {
            if (i == 9) return "CH";
            else if (i > 9) i--;
        }
        if (!j && i >= 10) i++;
        if (!q && i >= 17) i++;
        if (!w && i >= 23) i++;
        if (!x && i >= 24) i++;
        return String.valueOf((char) ('A' + i - 1));
    }

    @Override
    public int ord(String c) {
        c = normalize(c);
        if (c.equals("CH") && ch) return 8;
        else if (c.length() != 1) return -1;
        else return ord(c.charAt(0));
    }

    private int ord(char c) {
        if (c < 'A' || c > 'Z' || (c == 'Q' && !q) || (c == 'W' && !w) || (c == 'X' && !x))
            return -1;
        if (!j && c == 'J') c = 'I';
        int o = c - 'A' + 1;
        if (!x && o >= 24) o--;
        if (!w && o >= 23) o--;
        if (!q && o >= 17) o--;
        if (!j && o >= 10) o--;
        if (ch && o >= 9) o++;
        return o - 1;
    }

    @Override
    public StringParser getStringParser(String s) {
        return new CzechStringParser(s);
    }

    public static String normalize(String s) {
        return s.toUpperCase(locale)
                .replaceAll("[ÁÂÄ]", "A")
                .replaceAll("[ĆÇČ]", "C")
                .replaceAll("Ď", "D")
                .replaceAll("[ÉĚËĘ]", "E")
                .replaceAll("[ÍÎ]", "I")
                .replaceAll("[ĹĽŁ]", "L")
                .replaceAll("[ŃŇ]", "N")
                .replaceAll("[ÓÔÖŐ]", "O")
                .replaceAll("Ř", "R")
                .replaceAll("[ŚŠ]", "S")
                .replaceAll("Ť", "T")
                .replaceAll("[ÚŮÜŰ]", "U")
                .replaceAll("Ý", "Y")
                .replaceAll("[ŹŽ]", "Z");
    }


    protected final class CzechStringParser extends StringParser {

        CzechStringParser(String s) {
            super(normalize(s));
        }

        @Override
        protected Ret next() {
            char c = s.charAt(ix++);
            if (ch) {
                if (c == 'C' && ix < len && s.charAt(ix) == 'H') {
                    ix++;
                    return new Ret(8);
                }
            }
            return new Ret(ord(c), c);
        }
    }
}
