package cz.absolutno.sifry.braille;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.decoder.StatefulDecoder;
import cz.absolutno.sifry.common.decoder.StatefulDecoder.OnStateChangedListener;
import cz.absolutno.sifry.common.widget.FixedGridLayout;

public final class BrailleDFragment extends AbstractDFragment {

    private TextView reseni;
    private TextView pism;
    private BrailleView bvVstup;
    private BrailleTView bt;
    private LinearLayout thumb;
    private FixedGridLayout fglVstup;
    private StatefulDecoder bd;

    private boolean space = false;
    private boolean tah = false;

    private ArrayList<Integer> raw = new ArrayList<>();

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_CLEAR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            raw = savedInstanceState.getIntegerArrayList(App.DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.brailled_layout, container, false);

        bvVstup = v.findViewById(R.id.bvBDVstup);
        reseni = v.findViewById(R.id.tvRes);
        reseni.setOnClickListener(Utils.copyClickListener);
        pism = v.findViewById(R.id.tvBDPism);
        bt = v.findViewById(R.id.btBDVstup);
        thumb = v.findViewById(R.id.llBDThumb);
        fglVstup = v.findViewById(R.id.fglBDVstup);

        bvVstup.setOnChangeListener(brailleListener);
        v.findViewById(R.id.llBDThumb).setOnClickListener(thumbListener);

        ImageView ivBsp = v.findViewById(R.id.ivBsp);
        ivBsp.setOnClickListener(bspListener);
        ivBsp.setOnLongClickListener(clearListener);

        return v;
    }

    private final OnStateChangedListener stateListener = new OnStateChangedListener() {
        @SuppressWarnings("ConstantConditions")
        public void onStateChanged(String state) {
            getView().findViewById(R.id.llBDStav).setVisibility(state.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            ((TextView) getView().findViewById(R.id.tvBDStav)).setText(state);
        }
    };

    private final BrailleView.OnChangeListener brailleListener = new BrailleView.OnChangeListener() {
        public void onChange(int x, boolean down) {
            bt.setIn(x);
            if (bd == null) return;
            String desc = bd.getDesc(x);
            if (space)
                pism.setText(desc != null ? desc : "?");
            else {
                thumb.setVisibility((down && x != 0) ? View.VISIBLE : View.INVISIBLE);
                pism.setText((down && x != 0) ? (desc != null ? desc : "?") : "");
            }
            if (tah) if (!down && x != 0) submit();
        }
    };

    private final OnClickListener thumbListener = new OnClickListener() {
        public void onClick(View v) {
            submit();
        }
    };

    private void submit() {
        int x = bvVstup.getVal();
        if (x == 0 && !space) return;
        BrailleTView v = (BrailleTView) App.getInflater().inflate(R.layout.braillec_item, fglVstup, false);
        v.setIn(x);
        fglVstup.addView(v);
        raw.add(x);
        reseni.append(bd.decode(x));
        bvVstup.clear();
    }

    private final OnClickListener bspListener = new OnClickListener() {
        public void onClick(View v) {
            if (!tah)
                if (bvVstup.getVal() != 0) {
                    bvVstup.clear();
                    return;
                }
            if (raw.size() == 0) return;
            raw.remove(raw.size() - 1);
            fglVstup.removeViewAt(fglVstup.getChildCount() - 1);
            reseni.setText(bd.decode(raw));
        }
    };

    @Override
    public void loadData(Bundle data) {
        raw = data.getIntegerArrayList(App.DATA);
    }

    @Override
    public boolean saveData(Bundle data) {
        if (raw.size() == 0)
            return false;
        data.putString(App.VSTUP, reseni.getText().toString());
        return true;
    }

    @Override
    protected String onCopy() {
        return reseni.getText().toString();
    }

    @Override
    protected void onClear() {
        if (bd != null) bd.clearState();
        bvVstup.clear();
        reseni.setText("");
        fglVstup.removeAllViews();
        raw.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putIntegerArrayList(App.DATA, raw);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        bd = new StatefulDecoder(R.xml.braille_decoder);
        bd.setOnStateChangedListener(stateListener);
        reseni.setText(bd.decode(raw));
        brailleListener.onChange(bvVstup.getVal(), true);

        tah = (sp != null && sp.getBoolean("pref_brl_tah", false));
        ((TextView) getView().findViewById(R.id.tvBDVse)).setText(getString(tah ? R.string.tBDVseTah : R.string.tBDVse));
        getView().findViewById(R.id.ivGo).setVisibility(tah ? View.INVISIBLE : View.VISIBLE);
        bvVstup.setTah(tah);

        fglVstup.removeAllViews();
        for (int i = 0; i < raw.size(); i++) {
            BrailleTView v = (BrailleTView) App.getInflater().inflate(R.layout.braillec_item, fglVstup, false);
            v.setIn(raw.get(i));
            fglVstup.addView(v);
        }

        space = (sp != null && sp.getBoolean("pref_brl_spc", false));
        if (bvVstup.getVal() == 0) {
            thumb.setVisibility(space ? View.VISIBLE : View.INVISIBLE);
            pism.setText(space ? bd.getDesc(0) : "");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bd = null;
    }

}
