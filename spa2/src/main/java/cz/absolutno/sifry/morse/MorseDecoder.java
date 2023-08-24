package cz.absolutno.sifry.morse;

import android.util.SparseArray;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.decoder.Decoder;

public final class MorseDecoder extends Decoder {

    private final SparseArray<String> add;

    public MorseDecoder() {
        super();

        add = new SparseArray<>();

        addFields(R.array.saMDPismena);
        addFields(R.array.saMDCislice);
        addFields(R.array.saMDInterpunkce);
        add.put(1, "·");
    }

    private void addFields(int resId) {
        String[] elms = App.getContext().getResources().getStringArray(resId);
        for (String e : elms) {
            int i1 = e.indexOf(':');
            int i2 = e.lastIndexOf(':');
            int x = Integer.valueOf(e.substring(0, i1));
            String s = e.substring(i1 + 1, i2);
            add.put(x, s);
        }
    }

    @Override
    public String decode(int x) {
        String s = add.get(x);
        if (s != null)
            return s;
        else
            return "?";
    }

    @Override
    protected boolean encodeInternal(String s, ArrayList<Integer> list) {
        final int sz = add.size();
        final int len = s.length();
        boolean err = false;
        int ix;
        for (ix = 0; ix < len; ix++) {
            int best = -1;
            int bestLen = -1;
            final String chr = s.substring(ix, ix + 1);
            if (chr.equals(" ")) {
                list.add(1);
                continue;
            }
            for (int i = 0; i < sz; i++) {
                final String sa = add.valueAt(i);
                int l2 = sa.length();
                if (l2 > bestLen && len - ix >= l2 && sa.equals(l2 == 1 ? chr : s.substring(ix, ix + l2))) {
                    best = add.keyAt(i);
                    bestLen = l2;
                }
            }
            if (best < 0) {
                err = true;
                continue;
            }
            list.add(best);
            ix += bestLen - 1;
        }
        return err;
    }
}