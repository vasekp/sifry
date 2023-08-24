package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class HesloAdapter extends AbstractSubstAdapter {

    private static final int PLUS1 = 0;
    private static final int MINUS1 = 1;
    private static final int MINUSR1 = 2;
    private static final int PLUS0 = 3;
    private static final int MINUS0 = 4;
    private static final int MINUSR0 = 5;
    private static final int BAPLUS1 = 6;
    private static final int BAMINUS1 = 7;
    private static final int BAMINUSR1 = 8;
    private static final int BAPLUS0 = 9;
    private static final int BAMINUS0 = 10;
    private static final int BAMINUSR0 = 11;
    private static final int BVPLUS1 = 12;
    private static final int BVMINUS1 = 13;
    private static final int BVMINUSR1 = 14;
    private static final int BVPLUS0 = 15;
    private static final int BVMINUS0 = 16;
    private static final int BVMINUSR0 = 17;
    private static final int SKOK1 = 18;
    private static final int SKOK2 = 19;

    private final String[] items;
    private String key, strProc;

    public HesloAdapter(Alphabet abc) {
        super(abc);
        items = App.getContext().getResources().getStringArray(R.array.saSDsHeslem);
    }

    @Override
    public void clear() {
        super.clear();
        key = null;
        strProc = null;
    }

    @Override
    public int getCountValid() {
        return items.length;
    }

    @Override
    protected boolean isValid() {
        return (super.isValid() && key != null && key.length() != 0);
    }

    @Override
    public void setInput(String str) {
        super.setInput(str);
        StringParser sp = abc.getStringParser(str);
        StringBuilder sbProc = new StringBuilder();
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF)
            if (ord != StringParser.ERR)
                sbProc.append(abc.chr(ord));
        strProc = sbProc.toString();
        notifyDataSetChanged();
    }

    public void setKey(String key) {
        StringParser sp = abc.getStringParser(key);
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF)
            if (ord == StringParser.ERR) {
                Utils.toast(R.string.tSDChybaHesla);
                return;
            }
        this.key = key;
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int position) {
        StringBuilder dst = new StringBuilder();
        StringParser spa = abc.getStringParser(str);
        StringParser spb = abc.getStringParser(key);
        StringBuilder sv = new StringBuilder();
        int oa, ob;
        while ((oa = spa.getNextOrd()) != StringParser.EOF) {
            if (oa < 0) {
                dst.append(spa.getLastChar());
                continue;
            }
            if ((ob = spb.getNextOrd()) < 0) {
                switch (position) {
                    case BAPLUS0:
                    case BAMINUS0:
                    case BAMINUSR0:
                    case BAPLUS1:
                    case BAMINUS1:
                    case BAMINUSR1:
                        spb = abc.getStringParser(strProc);
                        break;
                    case BVPLUS0:
                    case BVMINUS0:
                    case BVMINUSR0:
                    case BVPLUS1:
                    case BVMINUS1:
                    case BVMINUSR1:
                        spb = abc.getStringParser(sv.toString());
                        sv = new StringBuilder();
                        break;
                    default:
                        spb.restart();
                }
                ob = spb.getNextOrd();
            }
            int c;
            switch (position) {
                case PLUS0:
                case BAPLUS0:
                case BVPLUS0:
                    c = oa + ob;
                    break;
                case MINUS0:
                case BAMINUS0:
                case BVMINUS0:
                    c = oa - ob;
                    break;
                case MINUSR0:
                case BAMINUSR0:
                case BVMINUSR0:
                    c = ob - oa;
                    break;
                case PLUS1:
                case BAPLUS1:
                case BVPLUS1:
                    c = oa + ob + 1;
                    break;
                case MINUS1:
                case BAMINUS1:
                case BVMINUS1:
                    c = oa - ob - 1;
                    break;
                case MINUSR1:
                case BAMINUSR1:
                case BVMINUSR1:
                    c = ob - oa - 1;
                    break;
                case SKOK1:
                    c = 2 * oa - ob;
                    break;
                case SKOK2:
                    c = 2 * ob - oa;
                    break;
                default:
                    c = 0;
            }
            if (c < 0)
                c += abc.count();
            c %= abc.count();
            dst.append(abc.chr(c));
            sv.append(abc.chr(c));
        }
        return dst.toString();
    }

    @Override
    public String getItemDesc(int position) {
        return items[position];
    }

}