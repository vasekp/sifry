package cz.absolutno.sifry.vlajky;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;

public final class VlajkyRFragment extends AbstractRFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        ((ExpandableListView) v.findViewById(R.id.main)).setAdapter(new VlajkyELA());
        return v;
    }

    private static final class VlajkyELA extends BaseExpandableListAdapter {

        private final VlajkySVGs svgs;
        private final String[] groups;
        private final int[] groupIDs;
        private final String[] pismena;
        private final String[] cisla;

        VlajkyELA() {
            Resources res = App.getContext().getResources();
            groups = res.getStringArray(R.array.saVRGroups);
            groupIDs = Utils.getIdArray(R.array.iaVRGroups);
            pismena = res.getStringArray(R.array.saVRPismena);
            cisla = res.getStringArray(R.array.saVRCisla);
            svgs = VlajkySVGs.getInstance();
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
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return (getGroupId(groupPosition) == R.id.idVRPismena ? pismena : cisla).length;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            return (getGroupId(groupPosition) == R.id.idVRPismena ? pismena : cisla)[childPosition];
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.vlajkyr_item, parent, false);
            int id = (int) getGroupId(groupPosition);
            ImageView iw = convertView.findViewById(R.id.image);
            iw.setImageDrawable(svgs.get(id, childPosition).getDrawable());

            String s = getChild(groupPosition, childPosition);
            int i1 = s.indexOf(':', 1);
            int i2 = s.indexOf(':', i1 + 1);

            if (i2 > 0) {
                convertView.findViewById(R.id.vyznam).setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.pis)).setText(s.substring(0, i1));
                ((TextView) convertView.findViewById(R.id.slovo)).setText(s.substring(i1 + 1, i2));
                ((TextView) convertView.findViewById(R.id.vyznam)).setText(s.substring(i2 + 1));
            } else {
                convertView.findViewById(R.id.vyznam).setVisibility(View.GONE);
                ((TextView) convertView.findViewById(R.id.pis)).setText(s.substring(0, i1));
                ((TextView) convertView.findViewById(R.id.slovo)).setText(s.substring(i1 + 1));
            }
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

}
