package cz.absolutno.sifry.semafor;

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

public final class SemaforCFragment extends AbstractCFragment {

    private SemaforCLA adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_list_c_layout, container, false);
        adapter = new SemaforCLA();
        ((ListView) v.findViewById(R.id.lvCVystup)).setAdapter(adapter);
        ((ListView) v.findViewById(R.id.lvCVystup)).setOnItemClickListener(genItemClickListener);
        return v;
    }

    @Override
    public boolean encode(String input) {
        return adapter.load(input);
    }

    @Override
    protected void packData(Bundle data) {
        data.putIntegerArrayList(App.DATA, adapter.getData());
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        adapter.reloadPref();
    }


    private static final class SemaforCLA extends BaseAdapter {

        private final ArrayList<Integer> raw;
        private final String[] items;
        private final int[] itemIDs;
        StatefulDecoder sd;

        SemaforCLA() {
            items = App.getContext().getResources().getStringArray(R.array.saSCItems);
            itemIDs = Utils.getIdArray(R.array.iaSCItems);
            raw = new ArrayList<>();
            sd = new StatefulDecoder(R.xml.semafor_decoder);
        }

        void reloadPref() {
            sd = new StatefulDecoder(R.xml.semafor_decoder);
            notifyDataSetChanged();
        }

        boolean load(String input) {
            raw.clear();
            notifyDataSetChanged();
            return sd.encode(input, raw);
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
                case R.id.idSCDvoj:
                    for (int i = 0; i < sz; i++) {
                        if (i > 0) sb.append(", ");
                        int x = raw.get(i);
                        for (int j = 0; j < 8; j++)
                            if ((x & (1 << j)) != 0)
                                sb.append(j + 1);
                    }
                    return sb.toString();
                default:
                    return null;
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = App.getInflater();
            if (getItemId(position) == R.id.idSCPrimo) {
                convertView = inflater.inflate(R.layout.grid_list_item, parent, false);
                ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
                FixedGridLayout fgl = convertView.findViewById(R.id.cont);
                for (Integer x : raw) {
                    SemaforTView st = (SemaforTView) inflater.inflate(R.layout.semaforc_item, fgl, false);
                    st.setIn(x);
                    fgl.addView(st);
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
