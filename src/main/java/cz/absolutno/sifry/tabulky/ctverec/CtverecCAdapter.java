package cz.absolutno.sifry.tabulky.ctverec;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.decoder.AlphabetDecoder;
import cz.absolutno.sifry.common.decoder.Decoder;
import cz.absolutno.sifry.common.widget.FixedGridLayout;
import cz.absolutno.sifry.tabulky.TabulkyCListAdapter;

public final class CtverecCAdapter extends TabulkyCListAdapter {

    private Alphabet abc;
    private int[][] sour;
    private final String[] items;
    private final int[] itemIDs;

    public CtverecCAdapter() {
        abc = null;
        items = App.getContext().getResources().getStringArray(R.array.saTCCtvItems);
        itemIDs = Utils.getIdArray(R.array.iaTCCtvItems);
    }

    @Override
    protected void setVar(int var, String abcVar) {
        abc = Alphabet.getVariantInstance(25, abcVar);
        notifyDataSetChanged();
    }

    @Override
    protected boolean encode(String input, ArrayList<Integer> raw) {
        Decoder d = new AlphabetDecoder(abc);
        boolean err = d.encode(input, raw);
        int sz = raw.size();
        sour = new int[sz][2];
        for (int i = 0; i < sz; i++) {
            int x = raw.get(i);
            sour[i][0] = x % 5;
            sour[i][1] = x / 5;
        }
        return err;
    }

    public int getCount() {
        if (abc != null && anyData())
            return items.length;
        else
            return 0;
    }

    public long getItemId(int position) {
        return itemIDs[position];
    }

    @Override
    public String getItem(int position) {
        int item = (int) getItemId(position);
        switch (item) {
            case R.id.idTCSour:
                StringBuilder sb = new StringBuilder();
                int sz = sour.length;
                for (int i = 0; i < sz; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(1 + sour[i][1]).append(1 + sour[i][0]);
                }
                return sb.toString();
            case R.id.idTCCtvBifidXY:
            case R.id.idTCCtvBifidYX:
                return bifid(item);
            default:
                return null;
        }
    }

    @Override
    protected String getItemDesc(int position) {
        return items[position];
    }

    @Override
    public int getItemViewType(int position) {
        return (getItemId(position) == R.id.idTCCtv5x5 || getItemId(position) == R.id.idTCCtv3x3) ? TYPE_GRID : TYPE_TEXT;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        boolean graf = (getItemId(position) == R.id.idTCCtv5x5 || getItemId(position) == R.id.idTCCtv3x3);
        View v = getViewHelper(position, convertView, parent);
        if (graf) {
            boolean alt = (getItemId(position) == R.id.idTCCtv3x3);
            FixedGridLayout fgl = v.findViewById(R.id.cont);
            LayoutInflater inflater = App.getInflater();
            for (int[] s : sour) {
                CtverecTView t = (CtverecTView) inflater.inflate(R.layout.ctverecc_item, fgl, false);
                t.setIn(s[0], s[1], alt);
                fgl.addView(t);
            }
        }
        return v;
    }

    private String bifid(int var) {
        int sz = sour.length;
        boolean trans = (var == R.id.idTCCtvBifidYX);
        int[] stream = new int[2 * sz];
        for (int i = 0; i < sz; i++) {
            stream[i] = sour[i][trans ? 1 : 0];
            stream[i + sz] = sour[i][trans ? 0 : 1];
        }

        StringBuilder sb = new StringBuilder();
        int ord;
        for (int i = 0; i < sz; i++) {
            ord = 5 * stream[trans ? 2 * i : 2 * i + 1] + stream[trans ? 2 * i + 1 : 2 * i];
            sb.append(abc.chr(ord));
        }
        return sb.toString();
    }
}