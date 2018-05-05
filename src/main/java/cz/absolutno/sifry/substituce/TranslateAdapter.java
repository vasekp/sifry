package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public class TranslateAdapter extends AbstractSubstAdapter {

    private static final int ENCRYPT = 1;
    private static final int DECRYPT = 0;

    private final int[] tr, trInv;
    private final String enc, dec;

    public TranslateAdapter(Alphabet abc) {
        super(abc);
        tr = new int[cnt];
        trInv = new int[cnt];
        enc = App.getContext().getResources().getString(R.string.tSDEnc);
        dec = App.getContext().getResources().getString(R.string.tSDDec);
        for (int i = 0; i < cnt; i++)
            tr[i] = trInv[i] = i;
    }

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < cnt; i++)
            tr[i] = trInv[i] = i;
        notifyDataSetChanged();
    }

    final void setTr(int[] tr) {
        if (tr.length != cnt)
            return;
        for (int i = 0; i < cnt; i++)
            trInv[i] = -1;
        for (int i = 0; i < cnt; i++) {
            this.tr[i] = tr[i];
            this.trInv[tr[i]] = i;
        }
        for (int i = 0; i < cnt; i++)
            if (trInv[i] == -1)
                throw new IllegalArgumentException();
        notifyDataSetChanged();
    }

    final void setOne(int from, int to) {
        int oImage = tr[from];
        int oPre = trInv[to];
        tr[from] = to;
        trInv[to] = from;
        tr[oPre] = oImage;
        trInv[oImage] = oPre;
        notifyDataSetChanged();
    }

    final int[] getTr() {
        return tr;
    }

    final int getOne(int from) {
        return tr[from];
    }

    @Override
    public final int getCountValid() {
        return 2;
    }

    @Override
    public final String getItem(int position) {
        StringBuilder dst = new StringBuilder();
        StringParser sp = abc.getStringParser(str);
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord < 0)
                dst.append(sp.getLastChar());
            else
                dst.append(abc.chr((position == ENCRYPT ? tr : trInv)[ord]));
        }
        return dst.toString();
    }

    @Override
    public String getItemDesc(int position) {
        switch (position) {
            case ENCRYPT:
                return enc;
            case DECRYPT:
                return dec;
            default:
                return null;
        }
    }

}