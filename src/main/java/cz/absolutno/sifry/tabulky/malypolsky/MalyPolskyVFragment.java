package cz.absolutno.sifry.tabulky.malypolsky;

import android.annotation.SuppressLint;
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

public final class MalyPolskyVFragment extends AbstractRFragment {

    private int[][] sour;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<Integer> raw = getArguments().getIntegerArrayList(App.DATA);
        assert raw != null;
        int sz = raw.size();
        sour = new int[sz][];
        for (int i = 0; i < sz; i++)
            sour[i] = MalyPolskyKrizDecoder.parseInt(raw.get(i));

        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        ExpandableListView el = v.findViewById(R.id.main);
        el.setAdapter(new MalyPolskyELA());
        el.setOnChildClickListener(Utils.copyChildClickListener);

        return v;
    }


    private final class MalyPolskyELA extends BaseExpandableListAdapter {

        private final String[] groups;
        private final int[] groupID;
        private final String[] abcVars;
        private final MalyPolskyKrizDecoder[] mpk;

        MalyPolskyELA() {
            groups = App.getContext().getResources().getStringArray(R.array.saTDMPVar);
            groupID = Utils.getIdArray(R.array.iaTDMPVar);
            abcVars = App.getContext().getResources().getStringArray(R.array.saTDMPABCVar);

            mpk = new MalyPolskyKrizDecoder[groups.length];
            for (int i = 0; i < groups.length; i++)
                mpk[i] = new MalyPolskyKrizDecoder(groupID[i], abcVars[i]);
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupID[groupPosition];
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
            return 16;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            int rot = childPosition % 4;
            boolean rX = (childPosition & 4) != 0;
            boolean inv = (childPosition >= 8);
            return filter(groupPosition, rot, rX, inv);
        }

        @SuppressWarnings("UnusedParameters")
        @SuppressLint("DefaultLocale")
        private String getChildDesc(int groupPosition, int childPosition) {
            int rot = childPosition % 4;
            boolean rX = (childPosition & 4) != 0;
            boolean inv = (childPosition >= 8);
            return String.format("R%d%s%s", rot * 90, rX ? " + rX" : "", inv ? ", inv •" : "");
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

        private String filter(int varIx, int rot, boolean zrc, boolean tc) {
            StringBuilder sb = new StringBuilder();
            for (int[] s : sour) {
                switch (s[3] == 1 ? 4 - rot : rot) { /* 2x2 rotujeme naopak než 3x3, protože má levotočivé souř. */
                    case 1:
                        s = new int[]{s[1], -s[0], s[2], s[3]};
                        break;
                    case 2:
                        s = new int[]{-s[0], -s[1], s[2], s[3]};
                        break;
                    case 3:
                        s = new int[]{-s[1], s[0], s[2], s[3]};
                        break;
                    default:
                        s = new int[]{s[0], s[1], s[2], s[3]};
                }
                if (zrc) {
                    if (s[3] == 0) { /* 3x3: jen obrátit X */
                        s[0] = -s[0];
                    } else { /* 2x2: prohodit souřadnice */
                        s = new int[]{s[1], s[0], s[2], s[3]};
                    }
                }
                if (tc) s[2] = 1 - s[2];
                sb.append(mpk[varIx].decode(s));
            }
            return sb.toString();
        }

    }

}
