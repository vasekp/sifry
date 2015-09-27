package cz.absolutno.sifry.braille;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.widget.FixedGridLayout;

public final class BrailleRFragment extends AbstractRFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_exp_list_layout, null);
        ((ExpandableListView) v.findViewById(R.id.main)).setAdapter(new BrailleELA());
        return v;
    }

    private static final class BrailleELA extends BaseExpandableListAdapter {

        private String groups[];
        private int groupIDs[];
        private String[][] elms;

        public BrailleELA() {
            groups = App.getContext().getResources().getStringArray(R.array.saBRGroups);
            groupIDs = Utils.getIdArray(R.array.iaBRGroups);
            elms = new String[groups.length][];
            for (int i = 0; i < groups.length; i++)
                elms[i] = App.getContext().getResources().getStringArray(groupIDs[i]);
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupIDs[groupPosition];
        }

        public String getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, null);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        public int getChildrenCountFull(int groupPosition) {
            return elms[groupPosition].length;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public Item getChild(int groupPosition, int childPosition) {
            String s = elms[groupPosition][childPosition];
            int i1 = s.indexOf(':');
            int i2 = s.indexOf(':', i1 + 1);
            if (i2 >= 0)
                return new Item(Integer.parseInt(s.substring(0, i1)), s.substring(i1 + 1, i2));
            else
                return new Item(Integer.parseInt(s.substring(0, i1)), s.substring(i1 + 1));
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater inflater = App.getInflater();
            FixedGridLayout fgl;
            if (convertView == null)
                fgl = (FixedGridLayout) inflater.inflate(R.layout.gen_grid_item, null);
            else
                fgl = (FixedGridLayout) convertView;
            for (int i = fgl.getChildCount(); i < getChildrenCountFull(groupPosition); i++) {
                View v = inflater.inflate(R.layout.brailler_item, null);
                fgl.addView(v);
            }
            for (int i = 0; i < getChildrenCountFull(groupPosition); i++) {
                View v = fgl.getChildAt(i);
                Item it = getChild(groupPosition, i);
                ((TextView) v.findViewById(R.id.text)).setText(it.desc);
                ((BrailleTView) v.findViewById(R.id.braille)).setIn(it.x);
                fgl.getChildAt(i).setVisibility(View.VISIBLE);
            }
            for (int i = getChildrenCountFull(groupPosition); i < fgl.getChildCount(); i++) {
                fgl.getChildAt(i).setVisibility(View.GONE);
            }
            return fgl;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public boolean hasStableIds() {
            return true;
        }
    }

    private static final class Item {
        public int x;
        public String desc;

        public Item(int x, String desc) {
            this.x = x;
            this.desc = desc;
        }
    }
};