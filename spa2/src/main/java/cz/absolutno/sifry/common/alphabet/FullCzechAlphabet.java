package cz.absolutno.sifry.common.alphabet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;


public final class FullCzechAlphabet extends FullAlphabet {

    private static final String abeceda = "AÁBCČDĎEÉĚFGHIÍJKLMNŇOÓPQRŘSŠTŤUÚŮVWXYÝZŽ";
    private final boolean ch;

    @SuppressWarnings("unused")
    public FullCzechAlphabet() {
        super(abeceda);
        this.ch = false;
    }

    public FullCzechAlphabet(boolean ch) {
        super(abeceda);
        this.ch = ch;
    }

    @SuppressWarnings("unused")
    public FullCzechAlphabet(SharedPreferences sp) {
        super(abeceda);
        this.ch = sp.getBoolean("pref_abc_ch", false);
    }

    @Override
    public int count() {
        return super.count() + (ch ? 1 : 0);
    }

    @Override
    public String chr(int i) {
        if (i < 0 || i >= count()) return "?";
        if (ch) {
            if (i == 13) return "CH";
            else if (i > 13) i--;
        }
        return super.chr(i);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int ord(String c) {
        c = c.toUpperCase();
        if (c.equals("CH") && ch) return 13;
        int o = super.ord(c);
        if (ch && o >= 13) o++;
        return o;
    }

    @Override
    public StringParser getStringParser(String s) {
        return new CzechStringParser(s);
    }


    protected final class CzechStringParser extends StringParser {

        CzechStringParser(String s) {
            super(s);
        }

        @Override
        protected Ret next() {
            char c = s.charAt(ix++);
            if (ch) {
                if (c == 'C' && ix < len && s.charAt(ix) == 'H') {
                    ix++;
                    return new Ret(13);
                }
            }
            int o = ord(c);
            if (ch && o >= 13) o++;
            return new Ret(o >= 0 ? o : ERR, c);
        }
    }
}
