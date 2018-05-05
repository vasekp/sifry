package cz.absolutno.sifry.substituce;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public final class TranslateFragment extends DialogFragment {

    private OnSelectedListener onSelectedListener = null;
    private Alphabet abc;

    private int colPrimary, colOK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        colPrimary = ContextCompat.getColor(getContext(), R.color.priColor);
        colOK = ContextCompat.getColor(getContext(), R.color.esubsOKColor);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder bld = new Builder(getActivity());
        bld.setCancelable(true);

        final int from = getArguments().getInt(App.VSTUP1);
        int to = getArguments().getInt(App.VSTUP2);
        abc = Alphabet.getPreferentialInstance();

        @SuppressLint("InflateParams")
        View layout = App.getInflater().inflate(R.layout.subst_dialog, null);
        ((TextView) layout.findViewById(R.id.tvSVVPuv)).setText(abc.chr(from));
        GridView grid = layout.findViewById(R.id.gvSVDialog);
        grid.setAdapter(new AlphabetLA(to));

        bld.setView(layout);
        final AlertDialog dlg = bld.create();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
                if (onSelectedListener != null)
                    onSelectedListener.onSelected(from, position);
                dlg.dismiss();
            }
        });

        return dlg;
    }

    public void setOnItemClickListener(OnSelectedListener onItemClickListener) {
        this.onSelectedListener = onItemClickListener;
    }


    private final class AlphabetLA extends BaseAdapter {

        private final int cnt;
        private final int orig;

        AlphabetLA(int orig) {
            this.orig = orig;
            cnt = abc.count();
        }

        public int getCount() {
            return cnt;
        }

        public String getItem(int position) {
            return abc.chr(position);
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
            tv.setTextColor(position == orig ? colOK : colPrimary);
            return tv;
        }

    }

    public interface OnSelectedListener {
        void onSelected(int from, int to);
    }
}
