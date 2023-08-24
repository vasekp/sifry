package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class AutokeyAdapter extends AbstractSubstAdapter {

    private static final int PLUSPR1 = 0;
    private static final int MINUSPR1 = 1;
    private static final int PRMINUS1 = 2;
    private static final int PLUSPR0 = 3;
    private static final int MINUSPR0 = 4;
    private static final int PRMINUS0 = 5;
    private static final int PLUSRUN1 = 6;
    private static final int MINUSRUN1 = 7;
    private static final int RUNMINUS1 = 8;
    private static final int PLUSRUN0 = 9;
    private static final int MINUSRUN0 = 10;
    private static final int RUNMINUS0 = 11;
    private static final int SKOK = 12;

    private final String[] items;

    public AutokeyAdapter(Alphabet abc) {
        super(abc);
        items = App.getContext().getResources().getStringArray(R.array.saSDbezHesla);
    }

    @Override
    public int getCountValid() {
        return items.length;
    }

    @Override
    public String getItem(int position) {
        StringBuilder dst = new StringBuilder();
        StringParser sp = abc.getStringParser(str);
        int l = 0, r = 0, o;
        boolean prvni = true;
        while ((o = sp.getNextOrd()) != StringParser.EOF) {
            if (o < 0) {
                dst.append(sp.getLastChar());
                continue;
            }
            switch (position) {
                case PLUSPR1:
                case MINUSPR1:
                case PRMINUS1:
                case PLUSRUN1:
                case MINUSRUN1:
                case RUNMINUS1:
                    o++;
                    break;
            }
            switch (position) {
                case PLUSPR0:
                case MINUSPR0:
                case PRMINUS0:
                case PLUSPR1:
                case MINUSPR1:
                case PRMINUS1:
                case SKOK:
                    r = l;
                    l = o;
            }
            if (!prvni) {
                switch (position) {
                    case SKOK:
                        o = 2 * o - r;
                        break;
                    case PLUSPR0:
                    case PLUSPR1:
                    case PLUSRUN0:
                    case PLUSRUN1:
                        o += r;
                        break;
                    case MINUSPR0:
                    case MINUSPR1:
                    case MINUSRUN0:
                    case MINUSRUN1:
                        o -= r;
                        break;
                    case PRMINUS0:
                    case PRMINUS1:
                    case RUNMINUS0:
                    case RUNMINUS1:
                        o = r - o;
                        break;
                }
            }
            if (o < 0)
                o += abc.count();
            o %= abc.count();
            switch (position) {
                case PLUSRUN0:
                case MINUSRUN0:
                case RUNMINUS0:
                case PLUSRUN1:
                case MINUSRUN1:
                case RUNMINUS1:
                    r = o;
            }
            switch (position) {
                case PLUSPR1:
                case MINUSPR1:
                case PRMINUS1:
                case PLUSRUN1:
                case MINUSRUN1:
                case RUNMINUS1:
                    o--;
            }
            if (o == -1) o = abc.count() - 1;
            dst.append(abc.chr(o));
            prvni = false;
        }
        return dst.toString();
    }

    @Override
    public String getItemDesc(int position) {
        return items[position];
    }

}