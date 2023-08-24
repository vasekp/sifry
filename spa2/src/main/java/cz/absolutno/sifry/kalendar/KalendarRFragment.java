package cz.absolutno.sifry.kalendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.alphabet.CzechAlphabet;

public final class KalendarRFragment extends AbstractRFragment {

    public static final int SMER_JD = 1;
    public static final int SMER_DJ = 2;

    private int kalendar;
    private int smer;
    private CharSequence[][] db;

    private BaseExpandableListAdapter ela = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gen_exp_list_layout, container, false);
        smer = getArguments().getInt(App.SPEC, SMER_JD);
        kalendar = ((KalendarActivity) getActivity()).getKalendar();
        db = Utils.load2DStringArray(kalendar);
        if (ela == null)
            ela = (smer == SMER_JD ? new JmenoDatumELA() : new DatumJmenoELA());
        ((ExpandableListView) v.findViewById(R.id.main)).setAdapter(ela);
        setRetainInstance(true);
        return v;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        int nKalendar = ((KalendarActivity) getActivity()).getKalendar();
        if (nKalendar != kalendar) {
            kalendar = nKalendar;
            db = Utils.load2DStringArray(((KalendarActivity) getActivity()).getKalendar());
            ela = (smer == SMER_JD ? new JmenoDatumELA() : new DatumJmenoELA());
            ((ExpandableListView) getView().findViewById(R.id.main)).setAdapter(ela);
        }
    }


    private final class JmenoDatumELA extends BaseExpandableListAdapter {

        @SuppressWarnings("unchecked") // Can't create an array of ArrayList<String>'s
        private final ArrayList<String>[] pismena = new ArrayList[26];
        private final int[] indexy = new int[26];
        private int used;

        JmenoDatumELA() {
            for (int i = 0; i < 26; i++)
                pismena[i] = new ArrayList<>();
            for (int m = 0; m < db.length; m++)
                for (int d = 0; d < db[m].length; d++) {
                    String jmena = db[m][d].toString();
                    if (jmena.substring(0, 1).equals("*")) continue;
                    for (String jmeno : jmena.split(", ")) {
                        String n = CzechAlphabet.normalize(jmeno);
                        pismena[n.charAt(0) - 'A'].add(String.format(Locale.ROOT, "%s:%d. %d.", jmeno, d + 1, m + 1));
                    }
                }
            used = 0;
            for (int i = 0; i < 26; i++) {
                if (pismena[i].size() > 0) {
                    Collections.sort(pismena[i], Collator.getInstance());
                    indexy[used++] = i;
                }
            }
        }

        public int getGroupCount() {
            return used;
        }

        public long getGroupId(int groupPosition) {
            return indexy[groupPosition];
        }

        public String getGroup(int groupPosition) {
            return Character.valueOf((char) ('A' + indexy[groupPosition])).toString();
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return pismena[indexy[groupPosition]].size();
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            String item = pismena[indexy[groupPosition]].get(childPosition);
            return item.substring(item.indexOf(":") + 1);
        }

        private String getChildDesc(int groupPosition, int childPosition) {
            String item = pismena[indexy[groupPosition]].get(childPosition);
            return item.substring(0, item.indexOf(":"));
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
    }


    private final class DatumJmenoELA extends BaseExpandableListAdapter {

        private final String[] mesice = App.getContext().getResources().getStringArray(R.array.saKDMesice);

        public String getChild(int groupPosition, int childPosition) {
            return db[groupPosition][childPosition].toString();
        }

        @SuppressLint("DefaultLocale")
        private String getChildDesc(int groupPosition, int childPosition) {
            return String.format("%d. %d.", childPosition + 1, groupPosition + 1);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return db[groupPosition].length;
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

        public String getGroup(int groupPosition) {
            return mesice[groupPosition];
        }

        public int getGroupCount() {
            return 12;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
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
