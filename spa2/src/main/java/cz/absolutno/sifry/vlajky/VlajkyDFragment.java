package cz.absolutno.sifry.vlajky;

import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStore;
import android.gesture.Prediction;
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


public final class VlajkyDFragment extends AbstractDFragment implements GestureOverlayView.OnGesturePerformedListener {

    private GestureLibrary gest;
    private TextView tvRes;
    private LinearLayout llOdhady;
    private VlajkySVGs svgs;
    private String reseni = "";

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_CLEAR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            reseni = savedInstanceState.getString(App.RESENI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vlajkyd_layout, container, false);

        GestureOverlayView gestures = v.findViewById(R.id.gestVD);
        gestures.addOnGesturePerformedListener(this);
        gestures.setGestureStrokeWidth(getResources().getDimension(R.dimen.vlajkyOkraj));
        gest = GestureLibraries.fromRawResource(getActivity(), R.raw.vlajky);
        gest.setOrientationStyle(8);
        gest.setSequenceType(GestureStore.SEQUENCE_INVARIANT);
        if (!gest.load()) getActivity().finish();

        svgs = VlajkySVGs.getInstance();
        llOdhady = v.findViewById(R.id.listVDOdhady);

        tvRes = v.findViewById(R.id.tvRes);
        tvRes.setOnClickListener(Utils.copyClickListener);

        ImageView ivBsp = v.findViewById(R.id.ivBsp);
        ivBsp.setOnClickListener(bspListener);
        ivBsp.setOnLongClickListener(clearListener);

        return v;
    }

    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gest.recognize(gesture);
        llOdhady.removeAllViews();

        if (predictions.size() > 0) {
            double ms = predictions.get(0).score;
            int c = 0;
            for (int i = 0; i < predictions.size() && c < 10; i++) {
                Prediction pr = predictions.get(i);
                if (pr.score > ms / 2) {
                    LayoutInflater inflater = App.getInflater();
                    A:
                    for (int j = 0; j < pr.name.length(); j++) {
                        Character chr = Character.toUpperCase(pr.name.charAt(j));
                        String schr = String.valueOf(chr);
                        for (int k = 0; k < c; k++)
                            if (((TextView) (llOdhady.getChildAt(k).findViewById(R.id.text))).getText().equals(schr))
                                continue A;
                        View v = inflater.inflate(R.layout.vlajkyd_item, llOdhady, false);
                        ((ImageView) v.findViewById(R.id.image)).setImageDrawable(svgs.getChar(chr).getDrawable());
                        ((TextView) v.findViewById(R.id.text)).setText(schr);
                        v.setTag(String.valueOf(chr));
                        v.setOnClickListener(listener);
                        llOdhady.addView(v);
                        c++;
                    }
                }
            }
        }
    }

    private final OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            reseni += (String) v.getTag();
            tvRes.setText(reseni);
            llOdhady.removeAllViews();
        }
    };

    private final OnClickListener bspListener = new OnClickListener() {
        public void onClick(View v) {
            if (reseni.length() == 0) return;
            reseni = reseni.substring(0, reseni.length() - 1);
            tvRes.setText(reseni);
        }
    };

    public boolean saveData(Bundle data) {
        if (reseni.length() == 0)
            return false;
        data.putString(App.VSTUP, reseni);
        return true;
    }

    @Override
    public void loadData(Bundle data) {
        reseni = data.getString(App.VSTUP);
    }

    @Override
    protected String onCopy() {
        return reseni;
    }

    @Override
    protected void onClear() {
        reseni = "";
        tvRes.setText(reseni);
        llOdhady.removeAllViews();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        tvRes.setText(reseni);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp != null)
            ((GestureOverlayView) getView().findViewById(R.id.gestVD)).setFadeOffset(sp.getInt("pref_vlajky_delay", getResources().getInteger(R.integer.pref_vlajky_delay_default)));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(App.RESENI, reseni);
    }

}
