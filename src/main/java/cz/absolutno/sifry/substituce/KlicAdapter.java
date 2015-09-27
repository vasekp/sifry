package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class KlicAdapter extends TranslateAdapter {

    private static final int FILL_ABC = 0;
    private static final int FILL_ZYX = 1;
    private static final int FILL_CONT = 2;

    private String klic;

    public KlicAdapter(Alphabet abc) {
        super(abc);
    }

    @Override
    public void clear() {
        super.clear();
        klic = null;
    }

    @Override
    protected boolean isValid() {
        return (super.isValid() && klic != null && klic.length() != 0);
    }

    public void setKlic(String klic, int pos) {
        int[] tab = new int[abc.count()];
        int i, ix = 0, last = 0;
        int cnt = abc.count();
        int ord;
        StringParser sp = abc.getStringParser(klic);
        A:
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                Utils.toast(R.string.tSDChybaHesla);
                this.klic = null;
                return;
            }
            for (i = 0; i < ix; i++)
                if (tab[i] == ord)
                    continue A;
            tab[ix++] = ord;
            last = ord;
        }
        B:
        for (i = 0; i < cnt; i++) {
            switch (pos) {
                case FILL_ABC:
                    ord = i;
                    break;
                case FILL_ZYX:
                    ord = cnt - 1 - i;
                    break;
                case FILL_CONT:
                    ord = (last + i) % cnt;
                    break;
            }
            for (int j = 0; j < ix; j++)
                if (tab[j] == ord)
                    continue B;
            tab[ix++] = ord;
        }
        setTr(tab);
        this.klic = klic;
        notifyDataSetChanged();
    }
}
