package cz.absolutno.sifry.common.alphabet;

import java.util.Locale;

public abstract class StringParser {
    protected CharSequence s;
    protected int ix;
    protected int len;
    protected char lastChar;

    public static final int ERR = -1;
    public static final int EOF = -2;

    public StringParser(String s) {
        this.s = s.toUpperCase(new Locale("C"));
        len = s.length();
        ix = 0;
        lastChar = 0;
    }

    public final int getNextOrd() {
        lastChar = 0;
        if (ix >= len) return EOF;
        Ret ret = next();
        if (ret.ord != ERR) {
            lastChar = 0;
            return ret.ord;
        } else {
            lastChar = ret.last;
            return ERR;
        }
    }

    protected abstract Ret next();

    public final char getLastChar() {
        return lastChar;
    }

    public final void restart() {
        ix = 0;
        lastChar = 0;
    }


    public static final class Ret {
        public int ord;
        public char last;

        public Ret(int ord) {
            this.ord = ord;
            this.last = 0;
        }

        public Ret(int ord, char last) {
            this.ord = ord;
            this.last = last;
        }
    }

}