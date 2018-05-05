package cz.absolutno.sifry.cisla;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractRFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public final class CislaRFragment extends AbstractRFragment {

    private CislaLA adapter;
    private View headerOrd, headerASCII;
    private int[] tabIDs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cislar_layout, container, false);

        tabIDs = Utils.getIdArray(R.array.iaCRGroups);

        ((Spinner) v.findViewById(R.id.spCRSeznam)).setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                boolean ascii, alt;
                switch (tabIDs[position]) {
                    case R.id.idCRAbeceda1:
                        ascii = false;
                        alt = false;
                        break;
                    case R.id.idCRAbeceda0:
                        ascii = false;
                        alt = true;
                        break;
                    case R.id.idCRASCIIVelke:
                        ascii = true;
                        alt = false;
                        break;
                    case R.id.idCRASCIIMale:
                        ascii = true;
                        alt = true;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                selectList(ascii, alt);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        headerOrd = v.findViewById(R.id.crHeaderOrd);
        headerASCII = v.findViewById(R.id.crHeaderASCII);

        adapter = new CislaLA();
        ((ListView) v.findViewById(R.id.lvCRMain)).setAdapter(adapter);
        selectList(false, false);

        return v;
    }

    private void selectList(boolean ascii, boolean mala) {
        headerOrd.setVisibility(ascii ? View.GONE : View.VISIBLE);
        headerASCII.setVisibility(ascii ? View.VISIBLE : View.GONE);
        adapter.set(ascii, mala);
    }


    private static final class CislaLA extends BaseAdapter {

        private final Alphabet abc, abcPerm;
        private boolean ascii = false;
        private boolean alt = false;

        CislaLA() {
            super();
            abc = Alphabet.getPreferentialInstance();
            abcPerm = Alphabet.getVariantInstance(24, "");
        }

        void set(boolean ascii, boolean alt) {
            this.ascii = ascii;
            this.alt = alt;
            notifyDataSetChanged();
        }

        public int getCount() {
            return ascii ? 26 : abc.count();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getId() != (ascii ? R.id.crItemASCII : R.id.crItemOrd))
                convertView = App.getInflater().inflate(ascii ? R.layout.cisla_item_ascii : R.layout.cisla_item, parent, false);
            String pism;
            int x;
            if (ascii) {
                x = position + (alt ? 'a' : 'A');
                pism = String.valueOf((char) x);
            } else {
                x = position + (alt ? 0 : 1);
                pism = abc.chr(position);
            }
            ((TextView) convertView.findViewById(R.id.pis)).setText(pism);
            ((TextView) convertView.findViewById(R.id.s10)).setText(String.format(Locale.ROOT, "%d", x));
            ((TextView) convertView.findViewById(R.id.s16)).setText(String.format("%02X", x));
            if (ascii) {
                ((TextView) convertView.findViewById(R.id.s8)).setText(String.format("%03o", x));
                ((TextView) convertView.findViewById(R.id.s2)).setText(CislaConv.toBinary(x, 8));
            } else {
                ((TextView) convertView.findViewById(R.id.s8)).setText(String.format("%02o", x));
                ((TextView) convertView.findViewById(R.id.s2)).setText(CislaConv.toBinary(x, 5));
                ((TextView) convertView.findViewById(R.id.s3)).setText(CislaConv.toTernary(x));
                ((TextView) convertView.findViewById(R.id.rim)).setText(CislaConv.toRoman(x));
                ((TextView) convertView.findViewById(R.id.perm)).setText(CislaConv.toPerm(abcPerm.ord(pism)));
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
