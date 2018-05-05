package cz.absolutno.sifry.tabulky.ctverec;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public final class CtverecVFragment extends AbstractRFragment {

    private int[][] sour;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<Integer> raw = getArguments().getIntegerArrayList(App.DATA);
        assert raw != null;
        int sz = raw.size();
        sour = new int[sz][2];
        for (int i = 0; i < sz; i++) {
            int x = raw.get(i);
            sour[i][0] = x % 5;
            sour[i][1] = x / 5;
        }

        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        ExpandableListView el = v.findViewById(R.id.main);
        el.setAdapter(new CtverecELA());
        el.setOnChildClickListener(Utils.copyChildClickListener);

        return v;
    }


    private final class CtverecELA extends BaseExpandableListAdapter {

        private final String[] groups;
        private final String[] groupID;
        private final Alphabet[] abc;

        CtverecELA() {
            groups = App.getContext().getResources().getStringArray(R.array.saTDCtvVar);
            groupID = App.getContext().getResources().getStringArray(R.array.saTDCtvABCVar);
            abc = new Alphabet[groups.length];
            for (int i = 0; i < groups.length; i++) {
                int ix = groupID[i].indexOf(':');
                String var = groupID[i].substring(0, ix);
                int sz = Integer.valueOf(groupID[i].substring(ix + 1));
                abc[i] = Alphabet.getVariantInstance(sz * sz, var);
            }
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public String getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parentView) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parentView, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return 10;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @SuppressWarnings("UnusedParameters")
        private String getChildDesc(int groupPosition, int childPosition) {
            if (childPosition == 0) return "0";
            StringBuilder sb = new StringBuilder();
            if (childPosition == 8)
                return App.getContext().getString(R.string.tTDCtvBifidXY);
            else if (childPosition == 9)
                return App.getContext().getString(R.string.tTDCtvBifidYX);
            if ((childPosition & 4) != 0) sb.append("tr");
            if ((childPosition & 1) != 0) sb.append(sb.length() != 0 ? " + " : "").append("rX");
            if ((childPosition & 2) != 0) sb.append(sb.length() != 0 ? " + " : "").append("rY");
            return sb.toString();
        }

        public String getChild(int groupPosition, int childPosition) {
            switch (childPosition) {
                case 8:
                    return bifid(groupPosition, false);
                case 9:
                    return bifid(groupPosition, true);
                default:
                    return filter(groupPosition, (childPosition & 4) != 0, (childPosition & 1) != 0, (childPosition & 2) != 0);
            }
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_list_item, parent, false);
            TextView tvDesc = convertView.findViewById(R.id.desc);
            TextView tvCont = convertView.findViewById(R.id.cont);
            tvDesc.setText(getChildDesc(groupPosition, childPosition));
            tvCont.setText(getChild(groupPosition, childPosition));
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

        private String filter(int varIx, boolean trans, boolean rx, boolean ry) {
            StringBuilder sb = new StringBuilder();
            for (int[] s : sour) {
                if (trans)
                    s = new int[]{s[1], s[0]};
                else
                    s = new int[]{s[0], s[1]};
                if (rx) s[0] = 4 - s[0];
                if (ry) s[1] = 4 - s[1];
                sb.append(abc[varIx].chr(s[1] * 5 + s[0]));
            }
            return sb.toString();
        }

        private String bifid(int varIx, boolean trans) {
            int sz = sour.length;
            int[] stream = new int[2 * sz];
            for (int i = 0; i < sz; i++) {
                stream[2 * i] = sour[i][trans ? 1 : 0];
                stream[2 * i + 1] = sour[i][trans ? 0 : 1];
            }

            StringBuilder sb = new StringBuilder();
            int ord;
            for (int i = 0; i < sz; i++) {
                ord = 5 * stream[trans ? i : i + sz] + stream[trans ? i + sz : i];
                sb.append(abc[varIx].chr(ord));
            }
            return sb.toString();
        }

    }

}
