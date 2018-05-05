package cz.absolutno.sifry.substituce;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.widget.FixedGridLayout;

public final class SubstDFragment extends AbstractDFragment {

    private int[] groupIDs;
    private AbstractSubstAdapter adapter;
    private Alphabet abc;
    private String patKoef;
    private int[] savedTr = null;

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_PASTE | HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.subst_layout, container, false);
        ((Spinner) v.findViewById(R.id.spSDTyp)).setOnItemSelectedListener(itemSelectedListener);
        ((Spinner) v.findViewById(R.id.spSDAKoef)).setOnItemSelectedListener(itemSelectedListener);
        ((Spinner) v.findViewById(R.id.spSDKPokr)).setOnItemSelectedListener(itemSelectedListener);
        ((TextView) v.findViewById(R.id.etSDSifra)).setOnEditorActionListener(editorActionListener);
        ((TextView) v.findViewById(R.id.etSDKlic)).setOnEditorActionListener(editorActionListener);
        ((TextView) v.findViewById(R.id.etSDHeslo)).setOnEditorActionListener(editorActionListener);
        ((ListView) v.findViewById(R.id.lvSDRes)).setOnItemClickListener(Utils.copyItemClickListener);
        v.findViewById(R.id.ivGo).setOnClickListener(goListener);
        v.findViewById(R.id.etSDSifra).setOnTouchListener(interceptListener);

        groupIDs = Utils.getIdArray(R.array.iaSDTypy);
        patKoef = getString(R.string.patSDKoef);

        if (savedInstanceState != null)
            savedTr = savedInstanceState.getIntArray(App.DATA);

        return v;
    }

    private final OnTouchListener interceptListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            v.getParent().requestDisallowInterceptTouchEvent(event.getActionMasked() != MotionEvent.ACTION_UP);
            return false;
        }
    };

    private final OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
        @SuppressWarnings("ConstantConditions")
        public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
            switch (parentView.getId()) {
                case R.id.spSDTyp:
                    updateLayout();
                    break;
                case R.id.spSDAKoef:
                    if (adapter instanceof AfinniAdapter)
                        ((AfinniAdapter) adapter).setCoeff(((KoefItem) parentView.getItemAtPosition(position)).koef);
                    break;
                case R.id.spSDKPokr:
                    if (adapter instanceof KlicAdapter)
                        ((KlicAdapter) adapter).setKlic(((EditText) getView().findViewById(R.id.etSDKlic)).getText().toString(), position);
                    break;
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private final OnEditorActionListener editorActionListener = new OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            zpracuj();
            return true;
        }
    };

    private final OnClickListener goListener = new OnClickListener() {
        public void onClick(View v) {
            zpracuj();
        }
    };

    @SuppressWarnings("ConstantConditions")
    private void zpracuj() {
        if (adapter != null)
            adapter.setInput(((TextView) getView().findViewById(R.id.etSDSifra)).getText().toString());
        if (adapter instanceof HesloAdapter)
            ((HesloAdapter) adapter).setKey(((TextView) getView().findViewById(R.id.etSDHeslo)).getText().toString());
        else if (adapter instanceof KlicAdapter)
            ((KlicAdapter) adapter).setKlic(((TextView) getView().findViewById(R.id.etSDKlic)).getText().toString(),
                    ((Spinner) getView().findViewById(R.id.spSDKPokr)).getSelectedItemPosition());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected String onCopy() {
        return ((EditText) getView().findViewById(R.id.etSDSifra)).getText().toString();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onPaste(String s) {
        ((EditText) getView().findViewById(R.id.etSDSifra)).setText(s);
        adapter.setInput(s);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onClear() {
        ((EditText) getView().findViewById(R.id.etSDSifra)).setText("");
        ((EditText) getView().findViewById(R.id.etSDHeslo)).setText("");
        ((EditText) getView().findViewById(R.id.etSDKlic)).setText("");
        adapter.clear();
        if (adapter instanceof TranslateAdapter)
            updateFGL();
    }

    @SuppressWarnings("ConstantConditions")
    private void loadAbc() {
        abc = Alphabet.getPreferentialInstance();
        int cnt = abc.count();

        ArrayList<KoefItem> entries = new ArrayList<>();
        for (int i = 2; i < cnt - 1; i++) {
            if (gcd(i, cnt) != 1) continue;
            int j;
            for (j = 1; j < cnt; j++)
                if (i * j % cnt == 1) break;
            entries.add(new KoefItem(i, j));
        }
        ArrayAdapter<KoefItem> coeffAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, entries);
        coeffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) getView().findViewById(R.id.spSDAKoef)).setAdapter(coeffAdapter);

        LayoutInflater inflater = App.getInflater();
        FixedGridLayout fgl = getView().findViewById(R.id.fglSDVlastni);
        fgl.removeAllViews();
        for (int i = 0; i < cnt; i++) {
            View v = inflater.inflate(R.layout.subst_item, fgl, false);
            ((TextView) v.findViewById(R.id.puv)).setText(abc.chr(i));
            ((TextView) v.findViewById(R.id.nove)).setText(abc.chr(i));
            v.setTag(i);
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!(adapter instanceof TranslateAdapter))
                        return;
                    int from = (Integer) v.getTag();
                    getView().findViewById(R.id.lvSDRes).requestFocus();
                    setSubs(from);
                }
            });
            fgl.addView(v);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateLayout() {
        String str = ((EditText) getView().findViewById(R.id.etSDSifra)).getText().toString();
        int selItem = groupIDs[((Spinner) getView().findViewById(R.id.spSDTyp)).getSelectedItemPosition()];

        getView().findViewById(R.id.llSDHeslo).setVisibility(selItem == R.id.idSDHeslo ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.llSDAfinni).setVisibility(selItem == R.id.idSDAffini ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.llSDKlic).setVisibility(selItem == R.id.idSDKlic ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.llSDVlastni).setVisibility(selItem == R.id.idSDVlastni ? View.VISIBLE : View.GONE);

        if (adapter instanceof TranslateAdapter)
            savedTr = ((TranslateAdapter) adapter).getTr();

        switch (selItem) {
            case R.id.idSDHeslo:
                adapter = new HesloAdapter(abc);
                ((HesloAdapter) adapter).setKey(((EditText) getView().findViewById(R.id.etSDHeslo)).getText().toString());
                break;
            case R.id.idSDAutoKey:
                adapter = new AutokeyAdapter(abc);
                break;
            case R.id.idSDPozice:
                adapter = new PoziceAdapter(abc);
                break;
            case R.id.idSDCaesar:
                adapter = new PosunyAdapter(abc);
                break;
            case R.id.idSDAtbash:
                adapter = new AtbashAdapter(abc);
                break;
            case R.id.idSDAffini:
                adapter = new AfinniAdapter(abc);
                ((AfinniAdapter) adapter).setCoeff(((KoefItem) ((Spinner) getView().findViewById(R.id.spSDAKoef)).getSelectedItem()).koef);
                break;
            case R.id.idSDKlic:
                adapter = new KlicAdapter(abc);
                ((KlicAdapter) adapter).setKlic(
                        ((EditText) getView().findViewById(R.id.etSDKlic)).getText()
                                .toString(),
                        ((Spinner) getView().findViewById(R.id.spSDKPokr))
                                .getSelectedItemPosition());
                break;
            case R.id.idSDVlastni:
                adapter = new TranslateAdapter(abc);
                if (savedTr != null)
                    ((TranslateAdapter) adapter).setTr(savedTr);
                updateFGL();
                break;
            default:
                throw new IllegalArgumentException();
        }
        ((ListView) getView().findViewById(R.id.lvSDRes)).setAdapter(adapter);
        adapter.setInput(str);
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private void setSubs(int from) {
        TranslateFragment dialog = new TranslateFragment();
        Bundle args = new Bundle();
        args.putSerializable(App.VSTUP1, from);
        args.putSerializable(App.VSTUP2,
                ((TranslateAdapter) adapter).getOne(from));
        dialog.setArguments(args);
        dialog.setOnItemClickListener(setSelectedListener);
        dialog.show(getFragmentManager(), "setSubs");
    }

    private final TranslateFragment.OnSelectedListener setSelectedListener = new TranslateFragment.OnSelectedListener() {
        @SuppressLint("DefaultLocale")
        public void onSelected(int from, int to) {
            ((TranslateAdapter) adapter).setOne(from, to);
            updateFGL();
        }
    };

    @SuppressWarnings("ConstantConditions")
    private void updateFGL() {
        FixedGridLayout fgl = getView().findViewById(R.id.fglSDVlastni);
        savedTr = ((TranslateAdapter) adapter).getTr();
        int cnt = abc.count();
        for (int i = 0; i < cnt; i++) {
            ((TextView) fgl.getChildAt(i).findViewById(R.id.nove)).setText(abc.chr(savedTr[i]));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (abc == null)
            loadAbc();
        updateLayout();
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        loadAbc();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        TranslateFragment fragment;
        fragment = (TranslateFragment) getFragmentManager().findFragmentByTag("setSubs");
        if (fragment != null)
            fragment.setOnItemClickListener(setSelectedListener);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putIntArray(App.DATA, savedTr);
    }

    private final class KoefItem {
        final int koef;
        final int inv;

        KoefItem(int koef, int inv) {
            this.koef = koef;
            this.inv = inv;
        }

        @Override
        public String toString() {
            return String.format(patKoef, koef, inv);
        }
    }

}
