package cz.absolutno.sifry.tabulky.malypolsky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.FixedGridLayout;
import cz.absolutno.sifry.tabulky.TabulkyCListAdapter;

public final class MalyPolskyCAdapter extends TabulkyCListAdapter {

    private MalyPolskyKrizDecoder mpk;
    private int[][] sour;
    private final String[] items;
    private final int[] itemIDs;

    public MalyPolskyCAdapter() {
        items = App.getContext().getResources().getStringArray(R.array.saTCMPItems);
        itemIDs = Utils.getIdArray(R.array.iaTCMPItems);
        mpk = null;
    }

    @Override
    protected void setVar(int var, String abcVar) {
        mpk = new MalyPolskyKrizDecoder(var, abcVar);
        notifyDataSetChanged();
    }

    @Override
    protected boolean encode(String input, ArrayList<Integer> raw) {
        boolean err = mpk.encode(input, raw);
        int sz = raw.size();
        sour = new int[sz][];
        for (int i = 0; i < sz; i++)
            sour[i] = MalyPolskyKrizDecoder.parseInt(raw.get(i));
        return err;
    }

    public int getCount() {
        if (mpk != null && anyData())
            return items.length;
        else
            return 0;
    }

    public long getItemId(int position) {
        return itemIDs[position];
    }

    @Override
    public String getItem(int position) {
        if (getItemId(position) == R.id.idTCSour) {
            StringBuilder sb = new StringBuilder();
            int sz = sour.length;
            for (int i = 0; i < sz; i++) {
                if (i > 0) sb.append(", ");
                if (sour[i][3] == 0)
                    sb.append(1 + (sour[i][1] + 1) * 3 + (sour[i][0] + 1));
                else
                    sb.append((char) ('A' + (sour[i][1] + 1) + (sour[i][0] + 1) / 2));
                if (sour[i][2] == 1)
                    sb.append("*");
            }
            return sb.toString();
        } else
            return null;
    }

    @Override
    protected String getItemDesc(int position) {
        return items[position];
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
            for (int[] s : sour) {
                MalyPolskyTView t = (MalyPolskyTView) inflater.inflate(R.layout.mpolskyc_item, fgl, false);
                t.setIn(s);
                fgl.addView(t);
            }
        }
        return v;
    }

}