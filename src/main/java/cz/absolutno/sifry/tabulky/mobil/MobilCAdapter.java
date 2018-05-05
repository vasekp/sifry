package cz.absolutno.sifry.tabulky.mobil;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.PlainEnglishAlphabet;
import cz.absolutno.sifry.tabulky.TabulkyCListAdapter;

@SuppressWarnings("PointlessBitwiseExpression")
public final class MobilCAdapter extends TabulkyCListAdapter {

    private final MobilDecoder md;
    private int[][] sour;
    private final String[] items;
    private final int[] itemIDs;
    private String input;

    public MobilCAdapter() {
        md = new MobilDecoder();
        items = App.getContext().getResources().getStringArray(R.array.saTCMobilItems);
        itemIDs = Utils.getIdArray(R.array.iaTCMobilItems);
    }

    @Override
    protected boolean encode(String input, ArrayList<Integer> raw) {
        this.input = input;
        boolean err = md.encode(input, raw);
        int sz = raw.size();
        sour = new int[sz][2];
        for (int i = 0; i < sz; i++) {
            int x = raw.get(i);
            sour[i][0] = ((x & MobilDecoder.KEY_MASK) >> MobilDecoder.KEY_SHIFT) + 1;
            sour[i][1] = ((x & MobilDecoder.REP_MASK) >> MobilDecoder.REP_SHIFT) + 1;
        }
        return err;
    }

    public String getInputProcessed() {
        return new PlainEnglishAlphabet().filter(input);
    }

    public int getCount() {
        if (anyData())
            return items.length;
        else
            return 0;
    }

    public long getItemId(int position) {
        return itemIDs[position];
    }

    @Override
    public String getItem(int position) {
        StringBuilder sb = new StringBuilder();
        switch ((int) getItemId(position)) {
            case R.id.idTCMobilPocet:
                for (int i = 0; i < sour.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(sour[i][0]).append(sour[i][1]);
                }
                return sb.toString();
            case R.id.idTCMobilMulti:
                for (int i = 0; i < sour.length; i++) {
                    if (i > 0) sb.append(", ");
                    char[] arr = new char[sour[i][1]];
                    Arrays.fill(arr, (char) ('0' + sour[i][0]));
                    sb.append(arr);
                }
                return sb.toString();
        }
        return null;
    }

    @Override
    protected String getItemDesc(int position) {
        return items[position];
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_TEXT;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewHelper(position, convertView, parent);
    }

}
