package cz.absolutno.sifry.braille;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractCFragment;
import cz.absolutno.sifry.common.decoder.StatefulDecoder;
import cz.absolutno.sifry.common.widget.FixedGridLayout;

public final class BrailleCFragment extends AbstractCFragment {

    private BrailleCLA adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_list_c_layout, container, false);
        adapter = new BrailleCLA();
        ((ListView) v.findViewById(R.id.lvCVystup)).setAdapter(adapter);
        ((ListView) v.findViewById(R.id.lvCVystup)).setOnItemClickListener(genItemClickListener);
        return v;
    }

    @Override
    public boolean encode(String input) {
        return adapter.load(input);
    }

    @Override
    public void packData(Bundle data) {
        data.putIntegerArrayList(App.DATA, adapter.getData());
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        adapter.reloadPref();
    }


    private static final class BrailleCLA extends BaseAdapter {

        private String input;
        private final ArrayList<Integer> raw;
        private final String[] items;
        private final int[] itemIDs;
        private StatefulDecoder bd;

        BrailleCLA() {
            items = App.getContext().getResources().getStringArray(R.array.saBCItems);
            itemIDs = Utils.getIdArray(R.array.iaBCItems);
            raw = new ArrayList<>();
            bd = new StatefulDecoder(R.xml.braille_decoder);
        }

        void reloadPref() {
            bd = new StatefulDecoder(R.xml.braille_decoder);
            notifyDataSetChanged();
        }

        boolean load(String input) {
            this.input = input.replace(' ', '·');
            notifyDataSetChanged();
            return bd.encode(this.input, raw);
        }

        ArrayList<Integer> getData() {
            return raw;
        }


        public int getCount() {
            if (raw.size() > 0)
                return items.length;
            else
                return 0;
        }

        public long getItemId(int position) {
            return itemIDs[position];
        }

        private String getItemDesc(int position) {
            return items[position];
        }

        public String getItem(int position) {
            StringBuilder sb = new StringBuilder();
            int sz = raw.size();
            switch ((int) getItemId(position)) {
                case R.id.idBCCisla:
                    for (int i = 0; i < sz; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(toNumeric(raw.get(i)));
                    }
                    return sb.toString();
                case R.id.idBCBin:
                    for (int i = 0; i < sz; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(toRevBinary(raw.get(i)));
                    }
                    return sb.toString();
                case R.id.idBCDec:
                    for (int i = 0; i < sz; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(raw.get(i));
                    }
                    return sb.toString();
                default:
                    return null;
            }
        }

        private String toRevBinary(int x) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++)
                sb.append((x >> i) & 1);
            return sb.toString();
        }

        private String toNumeric(int x) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++)
                if ((x & (1 << i)) != 0) sb.append(i + 1);
            return sb.toString();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = App.getInflater();
            if (getItemId(position) == R.id.idBCPrimo) {
                convertView = inflater.inflate(R.layout.grid_list_item, parent, false);
                ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
                FixedGridLayout fgl = convertView.findViewById(R.id.cont);
                for (Integer x : raw) {
                    BrailleTView bt = (BrailleTView) inflater.inflate(R.layout.braillec_item, fgl, false);
                    bt.setIn(x);
                    fgl.addView(bt);
                }
            } else {
                if (convertView == null || convertView.getId() == R.id.itemGrid)
                    convertView = inflater.inflate(R.layout.gen_list_item, parent, false);
                ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
                ((TextView) convertView.findViewById(R.id.cont)).setText(getItem(position));
            }
            return convertView;
        }

    }

}
