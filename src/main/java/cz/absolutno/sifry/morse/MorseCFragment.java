package cz.absolutno.sifry.morse;

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

public final class MorseCFragment extends AbstractCFragment {

    private MorseCLA adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_list_c_layout, container, false);
        adapter = new MorseCLA();
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
        data.putString(App.VSTUP1, adapter.getVstup());
        data.putIntegerArrayList(App.VSTUP2, adapter.getData());
        data.putStringArrayList(App.DATA, adapter.getBin());
    }


    private static final class MorseCLA extends BaseAdapter {

        private String in;
        private final String[] items;
        private final String[] znaky;
        private final int[] itemIDs;
        private final ArrayList<String> bin = new ArrayList<>();
        private final ArrayList<Integer> trits = new ArrayList<>();

        private final MorseDecoder md;

        MorseCLA() {
            items = App.getContext().getResources().getStringArray(R.array.saMCItems);
            itemIDs = Utils.getIdArray(R.array.iaMCItems);
            znaky = App.getContext().getResources().getStringArray(R.array.saMDZnaky);
            md = new MorseDecoder();
        }

        boolean load(String in) {
            ArrayList<Integer> raw = new ArrayList<>();
            boolean res = md.encode(in, raw);
            this.in = md.decode(raw);
            bin.clear();
            trits.clear();
            for (Integer i : raw) {
                String s = Integer.toBinaryString(i).substring(1);
                bin.add(s);
                int len = s.length();
                for (int j = 0; j < len; j++)
                    trits.add(Integer.valueOf(s.substring(j, j + 1)));
                trits.add(2);
            }
            if (trits.size() > 0)
                trits.remove(trits.size() - 1);
            notifyDataSetChanged();
            return res;
        }

        ArrayList<Integer> getData() {
            return trits;
        }

        String getVstup() {
            return in;
        }

        ArrayList<String> getBin() {
            return bin;
        }


        public int getCount() {
            if (trits.size() > 0)
                return items.length;
            else
                return 0;
        }

        public String getItem(int position) {
            StringBuilder sb = new StringBuilder();
            switch ((int) getItemId(position)) {
                case R.id.idMCPrimo:
                    for (Integer i : trits)
                        sb.append(znaky[i]);
                    return sb.toString();
                case R.id.idMC120:
                    for (Integer i : trits)
                        sb.append(Integer.toString(i < 2 ? i + 1 : 0));
                    return sb.toString();
                case R.id.idMCSignal:
                    int last = -1;
                    for (Integer i : trits) {
                        if (last >= 0)
                            sb.append("0");
                        // lint thinks this can never happen
                        //noinspection ConstantConditions
                        if (last == 2 && i == 2)
                            sb.append("00");
                        switch (i) {
                            case 0:
                                sb.append("1");
                                break;
                            case 1:
                                sb.append("111");
                                break;
                            case 2:
                                sb.append("0");
                                break;
                        }
                        last = i;
                    }
                    return sb.toString();
                default:
                    return null;
            }
        }

        private String getItemDesc(int position) {
            return items[position];
        }

        public long getItemId(int position) {
            return itemIDs[position];
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_list_item, parent, false);
            ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
            ((TextView) convertView.findViewById(R.id.cont)).setText(getItem(position));
            return convertView;
        }

    }

}
