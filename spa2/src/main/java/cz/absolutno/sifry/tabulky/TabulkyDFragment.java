package cz.absolutno.sifry.tabulky;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.widget.ButtonView;
import cz.absolutno.sifry.tabulky.ctverec.CtverecView;
import cz.absolutno.sifry.tabulky.malypolsky.MalyPolskyView;
import cz.absolutno.sifry.tabulky.mobil.MobilView;
import cz.absolutno.sifry.tabulky.polsky.PolskyKlasView;
import cz.absolutno.sifry.tabulky.polsky.PolskyTransView;
import cz.absolutno.sifry.tabulky.polsky.PolskyView;

public final class TabulkyDFragment extends AbstractDFragment {

    private ArrayList<Integer> raw = new ArrayList<>();
    private TextView tvRes;
    private TextView tvSour;
    private String reseni, sour;
    private int selLayout = 0;

    private PolskyView polsky;
    private MalyPolskyView maly;
    private MobilView mobil;
    private CtverecView ctverec;

    private Spinner spVar, spVPVar, spMPVar, spCtvVar;

    private int[] tabulkyID, vpVarID, mpVarID;
    private String[] vpVarABC, mpVarABC, ctvVarABC;
    private final Handler h = new Handler();

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tabulkyd_layout, container, false);

        tabulkyID = Utils.getIdArray(R.array.iaTDTabulky);
        vpVarID = Utils.getIdArray(R.array.iaTDVPVar);
        mpVarID = Utils.getIdArray(R.array.iaTDMPVar);
        vpVarABC = getResources().getStringArray(R.array.saTDVPABCVar);
        mpVarABC = getResources().getStringArray(R.array.saTDMPABCVar);
        ctvVarABC = getResources().getStringArray(R.array.saTDCtvABCVar);

        spVar = v.findViewById(R.id.spTDTab);
        spVar.setOnItemSelectedListener(varListener);

        polsky = (PolskyKlasView) v.findViewById(R.id.avTDPolskyKlas);
        polsky.setOnInputListener(inputListener);
        polsky = (PolskyTransView) v.findViewById(R.id.avTDPolskyTrans);
        polsky.setOnInputListener(inputListener);
        spVPVar = v.findViewById(R.id.spTDVPVar);
        spVPVar.setOnItemSelectedListener(varListener);

        maly = v.findViewById(R.id.avTDMaly);
        maly.setOnInputListener(inputListener);
        spMPVar = v.findViewById(R.id.spTDMPVar);
        spMPVar.setOnItemSelectedListener(varListener);

        mobil = v.findViewById(R.id.avTDMobil);
        mobil.setOnInputListener(inputListener);

        ctverec = v.findViewById(R.id.avTDCtverec);
        ctverec.setOnInputListener(inputListener);
        spCtvVar = v.findViewById(R.id.spTDCtvAbeceda);
        spCtvVar.setOnItemSelectedListener(varListener);

        tvRes = v.findViewById(R.id.tvRes);
        tvRes.setOnClickListener(Utils.copyClickListener);
        reseni = "";

        tvSour = v.findViewById(R.id.tvTDSour);
        ((ButtonView) v.findViewById(R.id.bvTDSour)).setOnInputListener(digitListener);
        tvSour.setOnClickListener(digitViewListener);
        sour = "";

        ImageView ivBsp = v.findViewById(R.id.ivBsp);
        ivBsp.setOnClickListener(bspListener);
        ivBsp.setOnLongClickListener(clearListener);

        if (savedInstanceState != null) {
            raw = savedInstanceState.getIntegerArrayList(App.DATA);
            selLayout = savedInstanceState.getInt(App.VAR);
            reseni = savedInstanceState.getString(App.VSTUP);
            sour = savedInstanceState.getString(App.VSTUP1);
        }

        return v;
    }

    private final OnItemSelectedListener varListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
            updateLayout();
            switch (selLayout) {
                case R.id.idTDVPolsky:
                    polsky.setVar(vpVarABC[spVPVar.getSelectedItemPosition()]);
                    break;
                case R.id.idTDMPolsky:
                    maly.setVar(mpVarID[spMPVar.getSelectedItemPosition()], mpVarABC[spMPVar.getSelectedItemPosition()]);
                    break;
                case R.id.idTDCtverec:
                    ctverec.setVar(ctvVarABC[spCtvVar.getSelectedItemPosition()]);
                    break;
            }
            updateReseni();
        }

        public void onNothingSelected(AdapterView<?> parentView) {
        }
    };

    @SuppressWarnings("ConstantConditions")
    private void updateLayout() {
        if (tabulkyID[spVar.getSelectedItemPosition()] != selLayout) {
            onClear();
            selLayout = tabulkyID[spVar.getSelectedItemPosition()];
        }

        getView().findViewById(R.id.spTDVPVar).setVisibility(selLayout == R.id.idTDVPolsky ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.spTDMPVar).setVisibility(selLayout == R.id.idTDMPolsky ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.spTDCtvAbeceda).setVisibility(selLayout == R.id.idTDCtverec ? View.VISIBLE : View.GONE);

        if (selLayout == R.id.idTDVPolsky) {
            int selVar = vpVarID[spVPVar.getSelectedItemPosition()];
            getView().findViewById(R.id.alTDPolskyKlas).setVisibility(selVar == R.id.idTDVPKlas ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.alTDPolskyTrans).setVisibility(selVar == R.id.idTDVPAlt ? View.VISIBLE : View.GONE);
            polsky = getView().findViewById(selVar == R.id.idTDVPKlas ? R.id.avTDPolskyKlas : R.id.avTDPolskyTrans);
        } else {
            getView().findViewById(R.id.alTDPolskyKlas).setVisibility(View.GONE);
            getView().findViewById(R.id.alTDPolskyTrans).setVisibility(View.GONE);
        }
        getView().findViewById(R.id.alTDMaly).setVisibility(selLayout == R.id.idTDMPolsky ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.alTDMobil).setVisibility(selLayout == R.id.idTDMobil ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.alTDCtverec).setVisibility(selLayout == R.id.idTDCtverec ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.tvTDVar).setVisibility(selLayout != R.id.idTDMobil ? View.VISIBLE : View.GONE);

        switch (selLayout) {
            case R.id.idTDVPolsky:
                ((ButtonView) getView().findViewById(R.id.bvTDSour)).setSize(1, 3, 3);
                getView().findViewById(R.id.llTVSour).setVisibility(View.VISIBLE);
                break;
            case R.id.idTDCtverec:
                ((ButtonView) getView().findViewById(R.id.bvTDSour)).setSize(1, 5, 5);
                getView().findViewById(R.id.llTVSour).setVisibility(View.VISIBLE);
                break;
            default:
                getView().findViewById(R.id.llTVSour).setVisibility(View.GONE);
        }

    }

    private final AidView.OnInputListener inputListener = new AidView.OnInputListener() {
        public void onInput(int x, String s) {
            reseni += s;
            sour = "";
            tvRes.setText(reseni);
            tvSour.setText("");
            raw.add(x);
        }
    };

    private final Runnable polskySourRunnable = new Runnable() {
        public void run() {
            if (sour.length() != 3)
                return;
            int x = (sour.charAt(0) - '1') * 9 + (sour.charAt(1) - '1') * 3 + (sour.charAt(2) - '1');
            reseni += polsky.chr(x);
            sour = "";
            tvRes.setText(reseni);
            tvSour.setText("");
            raw.add(x);
        }
    };

    private final Runnable ctverecSourRunnable = new Runnable() {
        public void run() {
            if (sour.length() != 2)
                return;
            int x = (sour.charAt(0) - '1') * 5 + (sour.charAt(1) - '1');
            reseni += ctverec.chr(x);
            sour = "";
            tvRes.setText(reseni);
            tvSour.setText("");
            raw.add(x);
        }
    };

    private final OnClickListener digitViewListener = new OnClickListener() {
        public void onClick(View v) {
            switch (selLayout) {
                case R.id.idTDVPolsky:
                    polskySourRunnable.run();
                    h.removeCallbacks(polskySourRunnable);
                    break;
                case R.id.idTDCtverec:
                    ctverecSourRunnable.run();
                    h.removeCallbacks(ctverecSourRunnable);
                    break;
            }
        }
    };

    private final ButtonView.OnInputListener digitListener = new ButtonView.OnInputListener() {
        public void onInput(int tag, String text) {
            switch (selLayout) {
                case R.id.idTDVPolsky:
                    if (sour.length() == 3) {
                        polskySourRunnable.run();
                        h.removeCallbacks(polskySourRunnable);
                    }
                    sour += text;
                    tvSour.setText(sour);
                    if (sour.length() == 3)
                        h.postDelayed(polskySourRunnable, 1000);
                    break;
                case R.id.idTDCtverec:
                    if (sour.length() == 2) {
                        ctverecSourRunnable.run();
                        h.removeCallbacks(ctverecSourRunnable);
                    }
                    sour += text;
                    tvSour.setText(sour);
                    if (sour.length() == 2)
                        h.postDelayed(ctverecSourRunnable, 1000);
                    break;
            }
        }
    };

    private final OnClickListener bspListener = new OnClickListener() {
        public void onClick(View v) {
            if (sour.length() != 0) {
                h.removeCallbacks(polskySourRunnable);
                h.removeCallbacks(ctverecSourRunnable);
                sour = sour.substring(0, sour.length() - 1);
                tvSour.setText(sour);
                return;
            }
            if (raw.size() == 0) return;
            raw.remove(raw.size() - 1);
            switch (selLayout) {
                case R.id.idTDVPolsky: /* Zvlášť, protože může obsahovat CH */
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < raw.size(); i++)
                        sb.append(polsky.chr(raw.get(i)));
                    reseni = sb.toString();
                    tvRes.setText(reseni);
                    break;
                default:
                    reseni = reseni.substring(0, reseni.length() - 1);
                    tvRes.setText(reseni);
            }
        }
    };

    private void updateReseni() {
        if (selLayout == R.id.idTDMobil)
            return;
        final int sz = raw.size();
        StringBuilder sb = new StringBuilder();
        switch (selLayout) {
            case R.id.idTDVPolsky:
                for (int i = 0; i < sz; i++)
                    sb.append(polsky.chr(raw.get(i)));
                break;
            case R.id.idTDMPolsky:
                for (int i = 0; i < sz; i++)
                    sb.append(maly.chr(raw.get(i)));
                break;
            case R.id.idTDCtverec:
                for (int i = 0; i < sz; i++)
                    sb.append(ctverec.chr(raw.get(i)));
                break;
        }
        reseni = sb.toString();
        tvRes.setText(reseni);
    }

    @Override
    public boolean saveData(Bundle data) {
        data.putString(App.VSTUP, reseni);
        data.putInt(App.VSTUP1, selLayout);
        data.putIntegerArrayList(App.DATA, raw);
        Spinner s;
        switch (selLayout) {
            case R.id.idTDVPolsky:
                s = spVPVar;
                break;
            case R.id.idTDMPolsky:
                s = spMPVar;
                break;
            case R.id.idTDCtverec:
                s = spCtvVar;
                break;
            default:
                return true;
        }
        data.putInt(App.VSTUP2, s.getSelectedItemPosition());
        return (raw.size() > 0);
    }

    @Override
    public void loadData(Bundle data) {
        selLayout = data.getInt(App.VSTUP1, 0);
        int var2 = data.getInt(App.VSTUP2, 0);
        spVar.setSelection(find(tabulkyID, selLayout));
        switch (selLayout) {
            case R.id.idTDVPolsky:
                spVPVar.setSelection(var2);
                break;
            case R.id.idTDMPolsky:
                spMPVar.setSelection(var2);
                break;
            case R.id.idTDCtverec:
                spCtvVar.setSelection(var2);
                break;
            case R.id.idTDMobil:
                reseni = data.getString(App.VSTUP);
                tvRes.setText(reseni);
                break;
        }
        raw = data.getIntegerArrayList(App.DATA);
    }

    private int find(int[] arr, int item) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == item)
                return i;
        return -1;
    }

    @Override
    protected String onCopy() {
        return reseni;
    }

    @Override
    protected void onClear() {
        reseni = "";
        sour = "";
        tvRes.setText("");
        tvSour.setText("");
        raw.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(App.VSTUP, reseni);
        state.putIntegerArrayList(App.DATA, raw);
        state.putString(App.VSTUP1, sour);
        state.putInt(App.VAR, selLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        tvRes.setText(reseni);
        tvSour.setText(sour);
        polsky.updateActiveFlag();
        maly.updateActiveFlag();
        mobil.updateActiveFlag();
        ctverec.updateActiveFlag();
    }

}
