package cz.absolutno.sifry.common.decoder;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import cz.absolutno.sifry.App;

public final class StringDecoder extends ChainDecoder {

    private SparseArray<String> desc;
    private SparseArray<String> add;

    @SuppressLint("DefaultLocale")
    public StringDecoder(int resId) {
        super();

        if (resId == 0) return;
        String[] elms = App.getContext().getResources().getStringArray(resId);
        desc = new SparseArray<>(elms.length);
        add = new SparseArray<>(elms.length);
        for (String e : elms) {
            int i1 = e.indexOf(':');
            int i2 = e.indexOf(':', i1 + 1);
            int x = Integer.parseInt(e.substring(0, i1));
            String s = (i2 >= 0 ? e.substring(i2 + 1) : e.substring(i1 + 1));
            if (s.length() > 0) add.put(x, s.toUpperCase());
            else continue;
            if (desc.get(x) != null)
                desc.put(x, desc.get(x) + " / " + (i2 >= 0 ? e.substring(i1 + 1, i2) : e.substring(i1 + 1)));
            else
                desc.put(x, (i2 >= 0 ? e.substring(i1 + 1, i2) : e.substring(i1 + 1)));
        }
    }

    @Override
    public String decode(int x) {
        return add.get(x);
    }

    @Override
    public String getDesc(int x) {
        return desc.get(x);
    }

    @Override
    public boolean isTemp() {
        return true;
    }

    @Override
    public EncodeResult encodeSingle(String s, int ix, boolean prefix) {
        final int sz = add.size();
        final String ss = s.substring(ix);
        final int len = ss.length();
        final String chr = ss.substring(0, 1);
        int best = -1;
        int bestLen = -1;
        for (int i = 0; i < sz; i++) {
            final String sa = add.valueAt(i);
            int l2 = sa.length();
            if (l2 <= len && l2 > bestLen && sa.equals(l2 == 1 ? chr : ss.substring(0, l2))) {
                best = add.keyAt(i);
                bestLen = l2;
            }
        }
        if (best < 0) return null;
        else return new EncodeResult(best, bestLen);
    }

}