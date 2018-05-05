package cz.absolutno.sifry.frekvence;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.HashMap;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public final class EmpirSubstDialog extends DialogFragment {

    private OnSelectedListener onSelectedListener = null;
    private Alphabet abc;
    private HashMap<String, Integer> used;
    private boolean expand = false;
    private View layout;

    private int colPrimary, colOK, colColl;
    private String mezera;
    private String nic;
    private String jine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        colPrimary = ContextCompat.getColor(getContext(), R.color.priColor);
        colOK = ContextCompat.getColor(getContext(), R.color.esubsOKColor);
        colColl = ContextCompat.getColor(getContext(), R.color.esubsCollColor);

        mezera = getString(R.string.tFDMezera);
        nic = getString(R.string.tESDNic);
        jine = getString(R.string.tESDJine);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder bld = new Builder(getActivity());
        bld.setCancelable(true);

        final SubsItem item = (SubsItem) getArguments().getSerializable(App.SPEC);
        assert item != null;
        abc = Alphabet.getPreferentialFullInstance();
        used = (HashMap<String, Integer>) getArguments().getSerializable(App.DATA);

        layout = App.getInflater().inflate(R.layout.esubs_dialog, null);
        ((TextView) layout.findViewById(R.id.tvESDPuv)).setText(item.ord >= 0 ? item.orig : Utils.getCharDesc(item.orig.charAt(0), mezera));
        GridView grid = layout.findViewById(R.id.gvESDialog);
        grid.setAdapter(new AlphabetLA(item.repl));

        bld.setView(layout);
        final AlertDialog dlg = bld.create();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
                if (position < abc.count()) {
                    if (onSelectedListener != null)
                        onSelectedListener.onSelected(item.orig, abc.chr(position));
                    dlg.dismiss();
                } else if (position == abc.count() + 1) {
                    if (onSelectedListener != null)
                        onSelectedListener.onSelected(item.orig, null);
                    dlg.dismiss();
                } else {
                    setExpand(true);
                }
            }
        });

        if (item.repl != null)
            ((EditText) layout.findViewById(R.id.etESDJine)).setText(item.repl);
        layout.findViewById(R.id.tvESDBack).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setExpand(false);
            }
        });
        layout.findViewById(R.id.tvESDOK).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onSelectedListener != null) {
                    String str = ((EditText) layout.findViewById(R.id.etESDJine)).getText().toString();
                    if (str.equals("")) str = null;
                    onSelectedListener.onSelected(item.orig, str);
                }
                dlg.dismiss();
            }
        });

        if (savedInstanceState != null)
            setExpand(savedInstanceState.getBoolean(App.VSTUP1));
        else
            setExpand(item.repl != null && abc.ord(item.repl) < 0);

        return dlg;
    }

    private void setExpand(boolean expand) {
        this.expand = expand;
        layout.findViewById(R.id.llESDJine).setVisibility(expand ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.gvESDialog).setVisibility(expand ? View.GONE : View.VISIBLE);
    }

    public void setOnItemClickListener(OnSelectedListener onItemClickListener) {
        this.onSelectedListener = onItemClickListener;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(App.VSTUP1, expand);
    }


    private final class AlphabetLA extends BaseAdapter {

        private final String orig;
        private final int cnt;

        AlphabetLA(String orig) {
            this.orig = orig;
            cnt = abc.count();
        }

        public int getCount() {
            return cnt + 2;
        }

        public String getItem(int position) {
            if (position < cnt)
                return abc.chr(position);
            else if (position == cnt)
                return jine;
            else
                return nic;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null)
                tv = (TextView) App.getInflater().inflate(R.layout.gen_letter_item, parent, false);
            else
                tv = (TextView) convertView;
            String chr = getItem(position);
            tv.setText(chr);
            if (position < cnt)
                tv.setTextColor(chr.equals(orig) ? colOK : (used.containsKey(chr) ? colColl : colPrimary));
            else if (position == cnt)
                tv.setTextColor(orig != null && abc.ord(orig) < 0 ? colOK : colPrimary);
            else
                tv.setTextColor(colPrimary);
            return tv;
        }

    }

    public interface OnSelectedListener {
        void onSelected(String from, String to);
    }
}
