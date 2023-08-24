package cz.absolutno.sifry.tabulky.polsky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.FixedGridLayout;
import cz.absolutno.sifry.tabulky.TabulkyCListAdapter;

public final class PolskyCAdapter extends TabulkyCListAdapter {

    private final PolskyKrizDecoder pk;
    private boolean alt;
    private final String[] itemsKlas, itemsAlt;
    private final int[] itemIDsKlas, itemIDsAlt;
    private int[][] triples;

    public PolskyCAdapter() {
        pk = new PolskyKrizDecoder();
        itemsKlas = App.getContext().getResources().getStringArray(R.array.saTCVPItems);
        itemIDsKlas = Utils.getIdArray(R.array.iaTCVPItems);
        itemsAlt = App.getContext().getResources().getStringArray(R.array.saTCVPAltItems);
        itemIDsAlt = Utils.getIdArray(R.array.iaTCVPAltItems);
    }

    @Override
    protected void setVar(int var, String abcVar) {
        alt = (var == R.id.idTDVPAlt);
        pk.setVar(abcVar);
        notifyDataSetChanged();
    }

    @Override
    protected boolean encode(String input, ArrayList<Integer> raw) {
        boolean err = pk.encode(input, raw);
        int sz = raw.size();
        triples = new int[sz][3];
        for (int i = 0; i < sz; i++) {
            int x = raw.get(i);
            triples[i][0] = x / 9;
            triples[i][1] = (x / 3) % 3;
            triples[i][2] = x % 3;
        }
        return err;
    }

    public int getCount() {
        if (anyData())
            return (alt ? itemsAlt : itemsKlas).length;
        else
            return 0;
    }

    public long getItemId(int position) {
        return (alt ? itemIDsAlt : itemIDsKlas)[position];
    }

    @Override
    public String getItem(int position) {
        switch ((int) getItemId(position)) {
            case R.id.idTCSour:
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < triples.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(triples[i][0] + 1).append(triples[i][1] + 1).append(triples[i][2] + 1);
                }
                return sb.toString();
            case R.id.idTCVPTrifid931:
            case R.id.idTCVPTrifid913:
            case R.id.idTCVPTrifid391:
                return trifid((int) getItemId(position));
            default:
                return null;
        }
    }

    @Override
    protected String getItemDesc(int position) {
        return (alt ? itemsAlt : itemsKlas)[position];
    }

    @Override
    public int getItemViewType(int position) {
        return (getItemId(position) == R.id.idTCPrimo ? TYPE_GRID : TYPE_TEXT);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = getViewHelper(position, convertView, parent);
        if (getItemId(position) == R.id.idTCPrimo) {
            FixedGridLayout fgl = v.findViewById(R.id.cont);
            LayoutInflater inflater = App.getInflater();
            for (int[] triple : triples) {
                PolskyTView t = (PolskyTView) inflater.inflate(R.layout.polskyc_item, fgl, false);
                if (alt)
                    t.setIn(triple[2], triple[1], triple[0], true);
                else
                    t.setIn(triple[1], triple[0], triple[2], false);
                fgl.addView(t);
            }
        }
        return v;
    }

    private String trifid(int perm) {
        final int sz = triples.length;
        int[] stream = new int[sz * 3];
        for (int i = 0; i < sz; i++) {
            int[] tr = triples[i];
            switch (perm) {
                case R.id.idTCVPTrifid391:
                    tr = new int[]{tr[1], tr[0], tr[2]};
                    break;
                case R.id.idTCVPTrifid913:
                    tr = new int[]{tr[0], tr[2], tr[1]};
                    break;
                case R.id.idTCVPTrifid931:
                    break;
            }
            stream[i] = tr[0];
            stream[i + sz] = tr[1];
            stream[i + 2 * sz] = tr[2];
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sz; i++) {
            int[] tr;
            tr = new int[]{stream[i * 3], stream[i * 3 + 1], stream[i * 3 + 2]};
            switch (perm) {
                case R.id.idTCVPTrifid391:
                    tr = new int[]{tr[1], tr[0], tr[2]};
                    break;
                case R.id.idTCVPTrifid913:
                    tr = new int[]{tr[0], tr[2], tr[1]};
                    break;
                case R.id.idTCVPTrifid931:
                    break;
            }
            sb.append(pk.decode(tr));
        }
        return sb.toString();
    }

}