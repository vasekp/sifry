package cz.absolutno.sifry.frekvence;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class FrekvRFragment extends AbstractRFragment {

    private FrekvRELA adapter;

    private int[] sortIDs;
    private String[] abcVar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frekvr_layout, container, false);
        sortIDs = Utils.getIdArray(R.array.iaFRSort);
        adapter = new FrekvRELA();
        ((Spinner) v.findViewById(R.id.spFRSort)).setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                adapter.sortItems(sortIDs[position]);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        ((ExpandableListView) v.findViewById(R.id.elFRMain)).setAdapter(adapter);
        return v;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        abcVar = getResources().getStringArray(R.array.saAbcVarKody);
        if (abcVar.length <= 1) {
            getView().findViewById(R.id.llFRAbcVar).setVisibility(View.GONE);
            adapter.buildList(Alphabet.getVariantInstance(0, ""));
        } else {
            ((Spinner) getView().findViewById(R.id.spFRAbcVar)).setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                    adapter.buildList(Alphabet.getVariantInstance(0, abcVar[position]));
                    adapter.sortItems(sortIDs[((Spinner) getView().findViewById(R.id.spFRSort)).getSelectedItemPosition()]);
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String pref = (sp != null ? sp.getString("pref_parse_abc", "") : "");
            if (abcVar[0].equals(pref)) {
                adapter.buildList(Alphabet.getVariantInstance(0, pref));
            } else {
                int ix = Arrays.asList(abcVar).indexOf(pref);
                if (ix >= 0)
                    ((Spinner) getView().findViewById(R.id.spFRAbcVar)).setSelection(ix);
            }
        }
    }


    private static final class FrekvRELA extends BaseExpandableListAdapter {

        private final String[] groups;
        private final int[] groupIDs;
        private final FrekvRItem[][] elms;
        private final String patProc, patRel;

        FrekvRELA() {
            Resources res = App.getContext().getResources();
            groups = res.getStringArray(R.array.saFRGroups);
            groupIDs = Utils.getIdArray(R.array.iaFRGroups);
            elms = new FrekvRItem[groups.length][];
            patProc = res.getString(R.string.patFRProc);
            patRel = res.getString(R.string.patFRRel);
        }

        private void buildList(Alphabet abc) {
            Resources res = App.getContext().getResources();
            for (int i = 0; i < groups.length; i++) {
                String[] in = res.getStringArray(groupIDs[i]);
                ArrayList<FrekvRItem> list = new ArrayList<>(in.length);
                for (String a : in) {
                    int ix = a.indexOf(':');
                    float frekv = Float.valueOf(a.substring(ix + 1));
                    if (groupIDs[i] == R.array.saFRFrekvencePis) {
                        StringParser sp = abc.getStringParser(a.substring(0, ix));
                        int ord;
                        int len = 0;
                        while ((sp.getNextOrd()) != StringParser.EOF)
                            len++;
                        sp.restart();
                        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
                            if (ord >= 0)
                                addItem(list, abc.chr(ord), frekv / len);
                        }
                    } else
                        addItem(list, abc.filter(a.substring(0, ix)), frekv);
                }
                elms[i] = list.toArray(new FrekvRItem[list.size()]);
            }
            notifyDataSetChanged();
        }

        private void addItem(ArrayList<FrekvRItem> list, String str, float frekv) {
            for (FrekvRItem item : list)
                if (item.str.equals(str)) {
                    item.frekv += frekv;
                    return;
                }
            list.add(new FrekvRItem(str, frekv));
        }

        private void sortItems(int sort) {
            for (FrekvRItem[] arr : elms)
                Arrays.sort(arr, sort == R.id.idFRSortAbc ? new AlphComparator() : new FreqComparator());
            notifyDataSetChanged();
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
            return elms[groupPosition] != null ? elms[groupPosition].length : 0;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public FrekvRItem getChild(int groupPosition, int childPosition) {
            return elms[groupPosition][childPosition];
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.frekvr_item, parent, false);
            ((TextView) convertView.findViewById(R.id.pism)).setText(getChild(groupPosition, childPosition).str);
            switch (groupIDs[groupPosition]) {
                case R.array.saFRFrekvenceBi:
                case R.array.saFRFrekvenceTri:
                    ((TextView) convertView.findViewById(R.id.frekv)).setText(String.format(patRel, getChild(groupPosition, childPosition).frekv));
                    break;
                default:
                    ((TextView) convertView.findViewById(R.id.frekv)).setText(String.format(patProc, getChild(groupPosition, childPosition).frekv));
            }
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
    }


    private static final class FrekvRItem {
        final String str;
        float frekv;

        FrekvRItem(String str, float frekv) {
            this.str = str;
            this.frekv = frekv;
        }
    }

    private static class AlphComparator implements Comparator<FrekvRItem> {
        final Collator c = Collator.getInstance();

        public int compare(FrekvRItem lhs, FrekvRItem rhs) {
            return c.compare(lhs.str, rhs.str);
        }
    }

    private static class FreqComparator extends AlphComparator {
        @Override
        public int compare(FrekvRItem lhs, FrekvRItem rhs) {
            if (lhs.frekv != rhs.frekv)
                return (int) Math.signum(rhs.frekv - lhs.frekv);
            else
                return c.compare(lhs.str, rhs.str);
        }
    }


}
