package cz.absolutno.sifry.semafor;

import android.annotation.SuppressLint;
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
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.decoder.StatefulDecoder;

public final class SemaforVFragment extends AbstractRFragment {

    private ArrayList<Integer> src;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        src = getArguments().getIntegerArrayList(App.DATA);
        View v = inflater.inflate(R.layout.gen_list_layout, container, false);
        ListView lv = v.findViewById(R.id.main);
        lv.setAdapter(new SemaforLA());
        lv.setOnItemClickListener(Utils.copyItemClickListener);
        return v;
    }


    private final class SemaforLA extends BaseAdapter {

        private final StatefulDecoder sd;

        SemaforLA() {
            sd = new StatefulDecoder(R.xml.semafor_decoder);
        }

        public int getCount() {
            return 16;
        }

        public String getItem(int position) {
            StringBuilder dst = new StringBuilder();
            sd.clearState();
            int len = src.size();
            for (int i = 0; i < len; i++) {
                int x = src.get(i);
                if (position >= 8)
                    x = (x & 17) + ((x & 2) << 6) + ((x & 4) << 4) + ((x & 8) << 2)
                            + ((x & 32) >> 2) + ((x & 64) >> 4) + ((x & 128) >> 6);
                x <<= (position % 8);
                x = (x & 255) | ((x & 0xFF00) >> 8);
                dst.append(sd.decode(x));
            }
            return dst.toString();
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("StringFormatMatches") /* LINT error: says we supply 3 arguments instead of 3 */
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_list_item, parent, false);
            TextView tvDesc = convertView.findViewById(R.id.desc);
            TextView tvCont = convertView.findViewById(R.id.cont);
            tvDesc.setText(String.format(getString(R.string.patSmVPosun), (position >= 8 ? getString(R.string.tSmVInv) : ""), position % 8,
                    (position % 8) == 0 ? 0 : (8 - (position % 8))));
            tvCont.setText(getItem(position));
            return convertView;
        }

    }
}
