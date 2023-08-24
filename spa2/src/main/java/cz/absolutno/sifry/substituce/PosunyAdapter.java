package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public class PosunyAdapter extends AbstractSubstAdapter {

    private final String patList;

    public PosunyAdapter(Alphabet abc) {
        super(abc);
        patList = App.getContext().getString(R.string.patSDList);
    }

    @Override
    public final int getCountValid() {
        return cnt;
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
                dst.append(abc.chr(encode(ord, position)));
        }
        return dst.toString();
    }

    @Override
    public String getItemDesc(int position) {
        return String.format(patList, abc.chr(encode(0, position)), position, position == 0 ? 0 : (cnt - position));
    }

    int encode(int ord, int position) {
        return (ord + position) % cnt;
    }

}
