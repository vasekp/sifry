package cz.absolutno.sifry.regexp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.regexp.RegExpNative.Report;

public final class RegExpDFragment extends AbstractDFragment {

    private static final int progressDelay = 30;
    private static final int matchesDelay = 100;

    private RegExpNative re;
    private TextView tvProgress;
    private ProgressBar pbProgress;
    private RegExpExpListAdapter adapter;

    @Override
    protected int getMenuCaps() {
        return HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.regexp_layout, container, false);
        tvProgress = v.findViewById(R.id.tvRDPocet);
        pbProgress = v.findViewById(R.id.pbRDProgress);
        ((ExpandableListView) v.findViewById(R.id.elRDResults)).setOnChildClickListener(Utils.copyChildClickListener);
        v.findViewById(R.id.btRDGo).setOnClickListener(goListener);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvProgress = null;
        pbProgress = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        re = ((ReferenceFragment)getFragmentManager().findFragmentByTag("ref")).getRE();
        adapter = new RegExpExpListAdapter(re);
        ((ExpandableListView) getView().findViewById(R.id.elRDResults)).setAdapter(adapter);
        if (re.isRunning())
            launchRefresh();
        final Report report = re.getProgress();
        if (report.matches > 0) {
            int matches = Math.min(report.matches, RegExpNative.MaxListResults);
            tvProgress.setText(String.valueOf(report.matches));
            adapter.update(matches);
        }
    }

    private final OnClickListener goListener = new OnClickListener() {
        @SuppressWarnings("ConstantConditions")
        public void onClick(View v) {
            if (re.isRunning()) {
                re.stopThread();
                updateGoButton();
                return;
            }
            String zad[] = new String[3];
            for (int i = 0; i < 3; i++) {
                zad[i] = (((ToggleButton) getView().findViewById(idCB[i])).isChecked() ? "" : "!") +
                        ((EditText) getView().findViewById(idET[i])).getText().toString();
            }
            adapter.clear();
            re.startThread(getContext().getAssets(), getFilename(), zad);
            launchRefresh();
        }
    };

    private String getFilename() {
        String filename = "";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp != null)
            filename = sp.getString("pref_re_dictionary", filename);
        if (filename.length() == 0)
            filename = getString(R.string.pref_re_dictionary_default);
        return "raw/" + filename;
    }

    @SuppressWarnings("ConstantConditions")
    private void updateGoButton() {
        ((Button) getView().findViewById(R.id.btRDGo)).setText(re.isRunning() ? R.string.tRDStop : R.string.tRDHledej);
    }

    private void launchRefresh() {
        final Handler h = new Handler();
        Runnable rMain = new Runnable() {
            public void run() {
                if (!isVisible()) {
                    h.postDelayed(this, matchesDelay);
                    return;
                }
                final Report report = re.getProgress();
                if (report.error) {
                    Utils.toast(re.getError());
                    tvProgress.setText("");
                    re.stopThread();
                    updateGoButton();
                    return;
                }
                int matches = Math.min(report.matches, RegExpNative.MaxListResults);
                if (report.running || matches > 0 || tvProgress.length() > 0)
                    tvProgress.setText(String.valueOf(report.matches));
                if (report.running)
                    h.postDelayed(this, matchesDelay);
                adapter.update(matches);
                updateGoButton();
            }
        };
        Runnable rProgress = new Runnable() {
            public void run() {
                if (!isVisible()) {
                    h.postDelayed(this, matchesDelay);
                    return;
                }
                final Report report = re.getProgress();
                pbProgress.setProgress((int) (report.progress * 1000));
                if (report.running)
                    h.postDelayed(this, progressDelay);
                else
                    pbProgress.setVisibility(View.GONE);
            }
        };
        h.postDelayed(rMain, matchesDelay);
        h.post(rProgress);
        pbProgress.setProgress(0);
        pbProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onClear() {
        re.free();
        adapter.clear();
        for (int i = 0; i < 3; i++) {
            ((EditText) getView().findViewById(idET[i])).setText("");
            ((ToggleButton) getView().findViewById(idCB[i])).setChecked(true);
        }
        tvProgress.setText("");
        updateGoButton();
    }

    private final int[] idET = {R.id.etRDFiltr1, R.id.etRDFiltr2, R.id.etRDFiltr3};
    private final int[] idCB = {R.id.cbRDFiltr1, R.id.cbRDFiltr2, R.id.cbRDFiltr3};
}
