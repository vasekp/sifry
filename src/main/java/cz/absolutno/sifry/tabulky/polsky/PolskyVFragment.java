package cz.absolutno.sifry.tabulky.polsky;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;

public final class PolskyVFragment extends AbstractRFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<Integer> raw = getArguments().getIntegerArrayList(App.DATA);
        assert raw != null;
        int var = Utils.getIdArray(R.array.iaTDVPVar)[getArguments().getInt(App.VSTUP2, 0)];

        final int sz = raw.size();
        int[][] triples = new int[sz][3];
        for (int i = 0; i < sz; i++) {
            int x = raw.get(i);
            triples[i][0] = x / 9;
            triples[i][1] = (x / 3) % 3;
            triples[i][2] = x % 3;
        }

        final PolskyVELA adapter = new PolskyVELA();
        adapter.setData(triples);
        adapter.setVar(var);

        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        ExpandableListView el = v.findViewById(R.id.main);
        el.setAdapter(adapter);
        el.setOnChildClickListener(Utils.copyChildClickListener);

        return v;
    }


    private static final class PolskyVELA extends BaseExpandableListAdapter {

        private final HashMap<String, PolskyKrizDecoder> pkHash = new HashMap<>();
        private int[][] triples;
        private boolean alt;

        private final String[] groups;
        private final int[] groupIDs;
        private final String[] groupVars;
        private final String[] permKlas, permAlt;
        private final String[] reflKlas, reflAlt;

        PolskyVELA() {
            Resources res = App.getContext().getResources();
            groups = res.getStringArray(R.array.saTVVPGroups);
            groupIDs = Utils.getIdArray(R.array.iaTVVPGroups);
            groupVars = res.getStringArray(R.array.saTVVPABCVar);
            permKlas = res.getStringArray(R.array.saTVVPPerm);
            permAlt = res.getStringArray(R.array.saTVVPAltPerm);
            reflKlas = res.getStringArray(R.array.saTVVPRefl);
            reflAlt = res.getStringArray(R.array.saTVVPAltRefl);

            for (String s : groupVars) {
                if (!pkHash.containsKey(s))
                    pkHash.put(s, new PolskyKrizDecoder(s));
            }
        }

        void setData(int[][] triples) {
            this.triples = triples;
            notifyDataSetChanged();
        }

        void setVar(int var) {
            alt = (var == R.id.idTDVPAlt);
        }

        public int getGroupCount() {
            if (triples != null)
                return groups.length;
            else
                return 0;
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
            switch ((int) getGroupId(groupPosition)) {
                case R.id.idTVVProt:
                case R.id.idTVVPtrifid:
                    return 6;
                case R.id.idTVVProtzrc:
                    return 6 * 7;
                default:
                    return 0;
            }
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            switch ((int) getGroupId(groupPosition)) {
                case R.id.idTVVProt:
                    return filter(childPosition, 0, groupVars[groupPosition]);
                case R.id.idTVVProtzrc:
                    return filter(childPosition / 7, (childPosition % 7) + 1, groupVars[groupPosition]);
                case R.id.idTVVPtrifid:
                    return trifid(childPosition, groupVars[groupPosition]);
                default:
                    return null;
            }
        }

        private String getChildDesc(int groupPosition, int childPosition) {
            final String perm[] = (alt ? permAlt : permKlas);
            final String refl[] = (alt ? reflAlt : reflKlas);
            switch ((int) getGroupId(groupPosition)) {
                case R.id.idTVVProt:
                case R.id.idTVVPtrifid:
                    return perm[childPosition];
                case R.id.idTVVProtzrc:
                    StringBuilder sb = new StringBuilder();
                    sb.append(perm[childPosition / 7]).append(" + ");
                    int zrc = (childPosition % 7) + 1;
                    if ((zrc & 1) != 0) sb.append(refl[0]);
                    if ((zrc & 2) != 0) sb.append(refl[1]);
                    if ((zrc & 4) != 0) sb.append(refl[2]);
                    return sb.toString();
                default:
                    return null;
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

        private String filter(int perm, int zrc, String var) {
            PolskyKrizDecoder pk = pkHash.get(var);
            StringBuilder sb = new StringBuilder();
            for (int[] tr : triples) {
                switch (perm) {
                    case 1:
                        tr = new int[]{tr[1], tr[0], tr[2]};
                        break;
                    case 2:
                        tr = new int[]{tr[2], tr[1], tr[0]};
                        break;
                    case 3:
                        tr = new int[]{tr[1], tr[2], tr[0]};
                        break;
                    case 4:
                        tr = new int[]{tr[0], tr[2], tr[1]};
                        break;
                    case 5:
                        tr = new int[]{tr[2], tr[0], tr[1]};
                        break;
                    default:
                        tr = new int[]{tr[0], tr[1], tr[2]};
                }
                if ((zrc & 1) != 0) tr[0] = 2 - tr[0];
                if ((zrc & 2) != 0) tr[1] = 2 - tr[1];
                if ((zrc & 4) != 0) tr[2] = 2 - tr[2];
                sb.append(pk.decode(tr));
            }
            return sb.toString();
        }


        private String trifid(int perm, String var) {
            final int sz = triples.length;
            int[] stream = new int[sz * 3];
            for (int i = 0; i < sz; i++) {
                int[] tr = triples[i];
                switch (perm) {
                    case 1:
                        tr = new int[]{tr[1], tr[0], tr[2]};
                        break;
                    case 2:
                        tr = new int[]{tr[2], tr[1], tr[0]};
                        break;
                    case 3:
                        tr = new int[]{tr[2], tr[0], tr[1]};
                        break;
                    case 4:
                        tr = new int[]{tr[0], tr[2], tr[1]};
                        break;
                    case 5:
                        tr = new int[]{tr[1], tr[2], tr[0]};
                        break;
                    default:
                }
                stream[3 * i] = tr[0];
                stream[3 * i + 1] = tr[1];
                stream[3 * i + 2] = tr[2];
            }

            PolskyKrizDecoder pk = pkHash.get(var);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sz; i++) {
                int[] tr = new int[]{stream[i], stream[i + sz], stream[i + 2 * sz]};
                switch (perm) {
                    case 1:
                        tr = new int[]{tr[1], tr[0], tr[2]};
                        break;
                    case 2:
                        tr = new int[]{tr[2], tr[1], tr[0]};
                        break;
                    case 3:
                        tr = new int[]{tr[1], tr[2], tr[0]};
                        break;
                    case 4:
                        tr = new int[]{tr[0], tr[2], tr[1]};
                        break;
                    case 5:
                        tr = new int[]{tr[2], tr[0], tr[1]};
                        break;
                }
                sb.append(pk.decode(tr));
            }
            return sb.toString();
        }

    }

}
