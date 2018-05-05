package cz.absolutno.sifry.semafor;

import android.os.Bundle;
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

public final class SemaforDFragment extends AbstractDFragment {

    private TextView reseni;
    private ArrayList<Integer> raw = new ArrayList<>();
    private SemaforView sv;
    private LinearLayout llOdhady, llState;
    private TextView tvState;
    private StatefulDecoder sd;
    private int vstup;

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_CLEAR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            raw = savedInstanceState.getIntegerArrayList(App.DATA);
            vstup = savedInstanceState.getInt(App.VSTUP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.semaford_layout, container, false);

        sv = v.findViewById(R.id.svSmDVstup);
        sv.setOnInputListener(inputListener);

        reseni = v.findViewById(R.id.tvRes);
        reseni.setOnClickListener(Utils.copyClickListener);
        llOdhady = v.findViewById(R.id.llSmDNavrhy);
        llState = v.findViewById(R.id.llSmDStav);
        tvState = v.findViewById(R.id.tvSmDStav);
        ImageView ivBsp = v.findViewById(R.id.ivBsp);
        ivBsp.setOnClickListener(bspListener);
        ivBsp.setOnLongClickListener(clearListener);

        return v;
    }

    private final SemaforView.OnInputListener inputListener = new SemaforView.OnInputListener() {
        public void onInput(int x, int c) {
            llOdhady.removeAllViews();
            if (sd == null)
                return;
            vstup = x;
            switch (c) {
                case 0:
                    break;
                case 1:
                    LayoutInflater inflater = App.getInflater();
                    int x2;
                    for (int sx = 0; sx < 8; sx++) {
                        x2 = x | (1 << sx);
                        String s = sd.getDesc(x2);
                        if (!s.equals("?")) {
                            View v = inflater.inflate(R.layout.semaford_item, llOdhady, false);
                            ((SemaforTView) v.findViewById(R.id.image)).setIn(x2);
                            ((TextView) v.findViewById(R.id.text)).setText(s);
                            v.setTag(x2);
                            v.setOnClickListener(clickListener);
                            llOdhady.addView(v);
                        }
                    }
                    break;
                case 2:
                    reseni.append(sd.decode(x));
                    raw.add(x);
                    break;
            }
        }
    };

    private final OnClickListener clickListener = new OnClickListener() {
        public void onClick(View v) {
            int x = (Integer) v.getTag();
            reseni.append(sd.decode(x));
            raw.add(x);
            sv.clear();
            llOdhady.removeAllViews();
        }
    };

    private final OnClickListener bspListener = new OnClickListener() {
        public void onClick(View v) {
            if (sv.isActive()) {
                sv.clear();
                return;
            }
            int sz = raw.size();
            if (sz == 0) return;
            raw.remove(sz - 1);
            reseni.setText(sd.decode(raw));
        }
    };

    private final OnStateChangedListener stateListener = new OnStateChangedListener() {
        public void onStateChanged(String state) {
            llState.setVisibility(state.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            tvState.setText(state);
        }
    };

    @Override
    protected void onClear() {
        sv.clear();
        reseni.setText("");
        raw.clear();
        sd.clearState();
        llOdhady.removeAllViews();
    }

    @Override
    protected String onCopy() {
        return reseni.getText().toString();
    }

    @Override
    public boolean saveData(Bundle data) {
        if (raw.size() == 0)
            return false;
        data.putString(App.VSTUP, reseni.getText().toString());
        data.putIntegerArrayList(App.DATA, raw);
        return true;
    }

    @Override
    public void loadData(Bundle data) {
        raw = data.getIntegerArrayList(App.DATA);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putIntegerArrayList(App.DATA, raw);
        state.putInt(App.VSTUP, vstup);
    }

    @Override
    public void onResume() {
        super.onResume();

        sd = new StatefulDecoder(R.xml.semafor_decoder);
        sd.setOnStateChangedListener(stateListener);
        reseni.setText(sd.decode(raw));
        sv.setVal(vstup);
    }

}
