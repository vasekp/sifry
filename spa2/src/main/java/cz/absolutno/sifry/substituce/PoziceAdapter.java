package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class PoziceAdapter extends AbstractSubstAdapter {

    private static final int PLUS1 = 0;
    private static final int MINUS1 = 1;
    private static final int MINUSR1 = 2;
    private static final int PLUS0 = 3;
    private static final int MINUS0 = 4;
    private static final int MINUSR0 = 5;
    private static final int SKOK1 = 6;
    private static final int SKOK2 = 7;

    private final String[] items;

    public PoziceAdapter(Alphabet abc) {
        super(abc);
        items = App.getContext().getResources().getStringArray(R.array.saSDsPozici);
    }

    @Override
    public int getCountValid() {
        return items.length;
    }

    @Override
    public String getItem(int position) {
        StringBuilder dst = new StringBuilder();
        StringParser spa = abc.getStringParser(str);
        int oa, ix = 0;
        while ((oa = spa.getNextOrd()) != StringParser.EOF) {
            if (oa < 0) {
                dst.append(spa.getLastChar());
                continue;
            }
            int c;
            switch (position) {
                case PLUS0:
                    c = oa + ix;
                    break;
                case MINUS0:
                    c = oa - ix;
                    break;
                case MINUSR0:
                    c = ix - oa;
                    break;
                case PLUS1:
                    c = oa + ix + 1;
                    break;
                case MINUS1:
                    c = oa - ix - 1;
                    break;
                case MINUSR1:
                    c = ix - oa - 1;
                    break;
                case SKOK1:
                    c = 2 * oa - ix;
                    break;
                case SKOK2:
                    c = 2 * ix - oa;
                    break;
                default:
                    c = 0;
            }
            if (c < 0)
                c += abc.count();
            c %= abc.count();
            dst.append(abc.chr(c));
            ix++;
            if (ix == cnt) ix = 0;
        }
        return dst.toString();
    }

    @Override
    public String getItemDesc(int position) {
        return items[position];
    }

}