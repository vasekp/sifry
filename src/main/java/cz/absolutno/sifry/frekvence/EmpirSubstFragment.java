package cz.absolutno.sifry.frekvence;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;
import cz.absolutno.sifry.common.widget.ColorChunkTextView;
import cz.absolutno.sifry.common.widget.ColorChunkTextView.ColorChunk;
import cz.absolutno.sifry.common.widget.ColorChunkTextView.OnChunkClickListener;

public final class EmpirSubstFragment extends AbstractDFragment {

    private String s;
    private ColorChunkTextView ctv;
    private LinearLayout llTabulka;
    private Alphabet abc;
    private String format;
    private int len;
    private int sort;

    private HashMap<String, SubsItem> hash;
    private HashMap<String, Integer> used;

    private int colPrimary, colNT, colOK, colColl;
    private String mezera;

    private static final int SORT_FREQ = 0;
    private static final int SORT_ABC = 1;

    @Override
    protected int getMenuCaps() {
        return HAS_CLEAR | HAS_COPY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        colPrimary = ContextCompat.getColor(getContext(), R.color.priColor);
        colNT = ContextCompat.getColor(getContext(), R.color.esubsNTColor);
        colOK = ContextCompat.getColor(getContext(), R.color.esubsOKColor);
        colColl = ContextCompat.getColor(getContext(), R.color.esubsCollColor);

        format = getString(R.string.patFDRes);
        mezera = getString(R.string.tFDMezera);

        s = getArguments().getString(App.VSTUP);

        abc = Alphabet.getPreferentialFullInstance();
        rebuildHash();

        if (savedInstanceState != null) {
            hash = (HashMap<String, SubsItem>) savedInstanceState.getSerializable(App.DATA);
            sort = savedInstanceState.getInt(App.SPEC);
        } else
            sort = SORT_FREQ;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.esubs_layout, container, false);
        v.findViewById(R.id.smaz).setVisibility(View.GONE);

        ctv = v.findViewById(R.id.ctvESMain);
        ctv.setOnChunkClickListener(chunkListener);
        v.findViewById(R.id.tbESSort).setOnClickListener(sortListener);

        llTabulka = v.findViewById(R.id.llESTabulka);

        return v;
    }

    private final OnClickListener sortListener = new OnClickListener() {
        public void onClick(View v) {
            rebuildList(((ToggleButton) v).isChecked() ? SORT_FREQ : SORT_ABC);
        }
    };

    private final OnChunkClickListener chunkListener = new OnChunkClickListener() {
        public void onChunkClick(int ix) {
            StringParser sp = abc.getStringParser(s);
            for (int i = 0; i < ix; i++)
                sp.getNextOrd();
            int ord = sp.getNextOrd();
            String chr = (ord >= 0 ? abc.chr(ord) : String.valueOf(sp.getLastChar()));
            if (hash.containsKey(chr))
                setSubs(hash.get(chr));
        }
    };

    @Override
    public void onClear() {
        for (SubsItem item : hash.values())
            item.repl = null;
        rebuildList(sort);
        updateSubs();
    }

    @Override
    protected String onCopy() {
        return ctv.getText();
    }

    private void updateSubs() {
        StringParser sp = abc.getStringParser(s);
        ArrayList<ColorChunk> chunks = new ArrayList<>();
        int ord;

        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            String chr;
            if (ord >= 0)
                chr = abc.chr(ord);
            else
                chr = String.valueOf(sp.getLastChar());
            if (hash.containsKey(chr)) {
                SubsItem item = hash.get(chr);
                int color;
                if (item.repl == null)
                    color = colNT;
                else if (used.get(item.repl) == 1)
                    color = colOK;
                else
                    color = colColl;
                chunks.add(new ColorChunk(item.repl != null ? item.repl : chr, color));
            } else
                chunks.add(new ColorChunk(chr, colPrimary));
        }
        ctv.setData(chunks);
    }

    private void rebuildList(int sort) {
        int cnt = hash.size();

        ArrayList<SubsItem> frekvence = new ArrayList<>(hash.values());

        switch (sort) {
            case SORT_ABC:
                Collections.sort(frekvence, new SubsItem.AlphComparator());
                break;
            case SORT_FREQ:
                Collections.sort(frekvence, new SubsItem.FreqComparator());
                break;
            default:
                throw new IllegalArgumentException();
        }
        this.sort = sort;

        if (llTabulka.getChildCount() != cnt) {
            llTabulka.removeAllViews();
            LayoutInflater inflater = App.getInflater();
            for (int i = 0; i < cnt; i++)
                inflater.inflate(R.layout.esubs_item, llTabulka);
        }
        used.clear();
        for (SubsItem item : frekvence)
            used.put(item.repl, used.containsKey(item.repl) ? used.get(item.repl) + 1 : 1);
        for (int i = 0; i < cnt; i++) {
            View v = llTabulka.getChildAt(i);
            SubsItem item = frekvence.get(i);
            v.setTag(item);
            ((TextView) v.findViewById(R.id.puv)).setText(item.ord >= 0 ? item.orig : Utils.getCharDesc(item.orig.charAt(0), mezera));
            ((TextView) v.findViewById(R.id.proc)).setText(len != 0 ? String.format(format, item.cnt, (float) item.cnt * 100 / len) : "­–");
            ((TextView) v.findViewById(R.id.nove)).setText(item.repl == null ? "" : item.repl);
            ((TextView) v.findViewById(R.id.nove)).setTextColor(used.get(item.repl) == 1 ? colOK : colColl);
            v.setOnClickListener(setClickListener);
            View kriz = v.findViewById(R.id.smaz);
            kriz.setVisibility(item.repl == null ? View.GONE : View.VISIBLE);
            kriz.setTag(item);
            kriz.setOnClickListener(resetClickListener);
        }
    }

    private final OnClickListener setClickListener = new OnClickListener() {
        public void onClick(final View v) {
            setSubs((SubsItem) v.getTag());
        }
    };

    private final OnClickListener resetClickListener = new OnClickListener() {
        public void onClick(View v) {
            resetSubs((SubsItem) v.getTag());
        }
    };

    private final EmpirSubstDialog.OnSelectedListener setSelectedListener = new EmpirSubstDialog.OnSelectedListener() {
        @SuppressLint("DefaultLocale")
        public void onSelected(String from, String to) {
            if (hash.containsKey(from))
                hash.get(from).repl = (to != null ? to.toUpperCase() : null);
            rebuildList(sort);
            updateSubs();
        }
    };

    private void setSubs(SubsItem item) {
        EmpirSubstDialog dialog = new EmpirSubstDialog();
        Bundle args = new Bundle();
        args.putSerializable(App.SPEC, item);
        args.putSerializable(App.DATA, used);
        dialog.setArguments(args);
        dialog.setOnItemClickListener(setSelectedListener);
        dialog.show(getFragmentManager(), "setSubs");
    }

    private void resetSubs(final SubsItem item) {
        item.repl = null;
        rebuildList(sort);
        updateSubs();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        EmpirSubstDialog fragment;
        fragment = (EmpirSubstDialog) getFragmentManager().findFragmentByTag("setSubs");
        if (fragment != null) {
            fragment.setOnItemClickListener(setSelectedListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(App.SPEC, sort);
        state.putSerializable(App.DATA, hash);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuildList(sort);
        updateSubs();
    }

    private void rebuildHash() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean ignore = (shp != null && shp.getBoolean("pref_parse_np", false));
        StringParser sp = abc.getStringParser(s);
        int ord;

        len = 0;
        hash = new HashMap<>();
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord < 0 && ignore) continue;
            len++;
            String chr;
            if (ord >= 0)
                chr = abc.chr(ord);
            else
                chr = String.valueOf(sp.getLastChar());
            if (!hash.containsKey(chr))
                hash.put(chr, new SubsItem(ord, chr, 1));
            else
                hash.get(chr).cnt++;
        }
        used = new HashMap<>(hash.size());
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        abc = Alphabet.getPreferentialFullInstance();
        rebuildHash();
    }

}
