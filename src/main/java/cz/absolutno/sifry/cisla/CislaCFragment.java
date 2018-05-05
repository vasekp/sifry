package cz.absolutno.sifry.cisla;

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
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractCFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.AsciiAlphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class CislaCFragment extends AbstractCFragment {

    private CislaCELA adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cislac_layout, container, false);
        adapter = new CislaCELA();
        ((ExpandableListView) v.findViewById(R.id.elCCVystup)).setAdapter(adapter);
        ((ExpandableListView) v.findViewById(R.id.elCCVystup)).setOnChildClickListener(Utils.copyChildClickListener);
        return v;
    }

    @Override
    protected boolean encode(String input) {
        adapter.load(input);
        return false;
    }

    @Override
    protected void packData(Bundle data) {
        data.putString(App.VSTUP, adapter.getInput());
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        adapter.reloadPref();
    }


    private static final class CislaCELA extends BaseExpandableListAdapter {

        private String input;
        private Alphabet abcPref;
        private final Alphabet abcAscii;
        private final Alphabet abcPerm;

        private final String[] groups;
        private final int[] groupIDs;
        private final String[] itemsPrimo;
        private final int[] itemsPrimoMod;
        private final int[] itemsPrimoZakl;
        private final String[] itemsASCII;
        private final int[] itemsASCIIMod;
        private final int[] itemsASCIIZakl;

        CislaCELA() {
            Resources res = App.getContext().getResources();
            groups = res.getStringArray(R.array.saCCGroups);
            groupIDs = Utils.getIdArray(R.array.iaCCGroups);
            itemsPrimo = res.getStringArray(R.array.saCCSoustavyPrimo);
            itemsPrimoMod = Utils.getIdArray(R.array.iaCCSoustavyPrimoMody);
            itemsPrimoZakl = res.getIntArray(R.array.iaCCSoustavyPrimoZaklady);
            itemsASCII = res.getStringArray(R.array.saCCSoustavyASCII);
            itemsASCIIMod = Utils.getIdArray(R.array.iaCCSoustavyASCIIMody);
            itemsASCIIZakl = res.getIntArray(R.array.iaCCSoustavyASCIIZaklady);

            abcPref = Alphabet.getPreferentialInstance();
            abcAscii = new AsciiAlphabet();
            abcPerm = Alphabet.getVariantInstance(24, "");
            input = "";
        }

        void reloadPref() {
            abcPref = Alphabet.getPreferentialInstance();
            notifyDataSetChanged();
        }

        void load(String input) {
            this.input = input;
            notifyDataSetChanged();
        }

        String getInput() {
            return abcPref.filter(input);
        }


        public int getGroupCount() {
            if (input.length() > 0)
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
                case R.id.idCCOd0:
                case R.id.idCCOd1:
                    return itemsPrimo.length;
                case R.id.idCCASCII:
                    return itemsASCII.length;
                default:
                    return 0;
            }
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public String getChild(int groupPosition, int childPosition) {
            boolean ascii = (getGroupId(groupPosition) == R.id.idCCASCII);
            int mod = (ascii ? itemsASCIIMod : itemsPrimoMod)[childPosition];
            boolean perm = (mod == R.id.idCDPerm1 || mod == R.id.idCDPerm2);
            int zakl = (ascii ? itemsASCIIZakl : itemsPrimoZakl)[childPosition];
            StringBuilder sb = new StringBuilder();
            Alphabet abc = (ascii ? abcAscii : (perm ? abcPerm : abcPref));
            StringParser sp = abc.getStringParser(input);
            int ord = sp.getNextOrd();
            while (ord != StringParser.EOF) {
                if (ord >= 0) {
                    if (getGroupId(groupPosition) == R.id.idCCOd1 && !perm) ord++;
                    switch (mod) {
                        case R.id.idCDCisla:
                            switch (zakl) {
                                case 2:
                                    sb.append(CislaConv.toBinary(ord, ascii ? 8 : 5));
                                    break;
                                case 3:
                                    sb.append(CislaConv.toTernary(ord));
                                    break;
                                case 8:
                                    sb.append(String.format(ascii ? "%03o" : "%02o", ord));
                                    break;
                                case 10:
                                    sb.append(ord);
                                    break;
                                case 16:
                                    sb.append(String.format("%02X", ord));
                                    break;
                            }
                            break;
                        case R.id.idCDRim:
                            sb.append(ord == 0 ? 0 : CislaConv.toRoman(ord));
                            break;
                        case R.id.idCDPerm1:
                        case R.id.idCDPerm2:
                            sb.append(CislaConv.toPerm(ord));
                            break;
                    }
                } else
                    sb.append("?");
                ord = sp.getNextOrd();
                if (ord != StringParser.EOF)
                    sb.append(", ");
            }
            return sb.toString();
        }

        private String getChildDesc(int groupPosition, int childPosition) {
            boolean ascii = (getGroupId(groupPosition) == R.id.idCCASCII);
            return (ascii ? itemsASCII : itemsPrimo)[childPosition];
        }

        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_list_item, parent, false);
            ((TextView) convertView.findViewById(R.id.desc)).setText(getChildDesc(groupPosition, childPosition));
            ((TextView) convertView.findViewById(R.id.cont)).setText(getChild(groupPosition, childPosition));
            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

}
