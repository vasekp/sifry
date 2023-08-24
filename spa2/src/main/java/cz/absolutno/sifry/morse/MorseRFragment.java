package cz.absolutno.sifry.morse;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractRFragment;

public final class MorseRFragment extends AbstractRFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        ((ExpandableListView) v.findViewById(R.id.main)).setAdapter(new MorseELA());
        return v;
    }

    private static final class MorseELA extends BaseExpandableListAdapter {

        private final String[] groups;
        private final String[][] elms = new String[3][];
        private final String[] znaky;
        private final char tecka, carka;

        MorseELA() {
            Resources res = App.getContext().getResources();
            groups = res.getStringArray(R.array.saMDGroups);
            elms[0] = res.getStringArray(R.array.saMDPismena);
            elms[1] = res.getStringArray(R.array.saMDCislice);
            elms[2] = res.getStringArray(R.array.saMDInterpunkce);
            znaky = res.getStringArray(R.array.saMDZnaky);
            tecka = znaky[0].charAt(0);
            carka = znaky[1].charAt(0);
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

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return elms[groupPosition].length;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            return elms[groupPosition][childPosition];
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.morse_item, parent, false);

            String s = getChild(groupPosition, childPosition);
            int i1 = s.indexOf(':');
            int i2 = s.lastIndexOf(':');

            int x = Integer.valueOf(s.substring(0, i1));
            String kod = Integer.toBinaryString(x).replace('0', tecka).replace('1', carka).substring(1);

            ((TextView) convertView.findViewById(R.id.kod)).setText(kod);
            ((TextView) convertView.findViewById(R.id.pis)).setText(s.substring(i1 + 1, i2));
            ((TextView) convertView.findViewById(R.id.info)).setText(s.substring(i2 + 1));
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
    }

}
