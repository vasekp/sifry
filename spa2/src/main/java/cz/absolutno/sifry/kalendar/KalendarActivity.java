package cz.absolutno.sifry.kalendar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class KalendarActivity extends BottomBarActivity {

    private static final int JMENO = 0;
    private static final int ZAKLAD = 1;
    private static final int DATUM = 2;

    private int kal;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        updatePrefs();
        if (state == null) {
            setBBar();
            AbstractDFragment frag = new KalendarDFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.content, frag, "D");
            trans.commit();
        }
    }

    private void updatePrefs() {
        String kod = getString(R.string.pref_kal_namedays_default);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp != null)
            kod = sp.getString("pref_kal_namedays", kod);
        if (kod.length() == 0)
            kal = 0;
        else
            kal = getResources().getIdentifier("@array/" + kod, null, getPackageName());
    }

    public int getKalendar() {
        return kal;
    }

    private void setBBar() {
        if (kal != 0) {
            if (getBBar().getEntries() != null)
                return;
            getBBar().setEntries(new String[]{getString(R.string.tKDRefJD), getString(R.string.tKDDef), getString(R.string.tKDRefDJ)}, ZAKLAD);
        } else {
            getBBar().setEntries(null, 0);
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    protected void onBottomBarChange(int curIx, int lastIx) {
        if (kal == 0) return;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        AbstractDFragment frag;
        Bundle args;
        switch (curIx) {
            case JMENO:
            case DATUM:
                args = new Bundle();
                frag = new KalendarRFragment();
                args.putInt(App.SPEC, curIx == JMENO ? KalendarRFragment.SMER_JD : KalendarRFragment.SMER_DJ);
                frag.setArguments(args);
                trans.replace(R.id.content, frag, "R");
                trans.addToBackStack(null);
                trans.commit();
                break;
            case ZAKLAD:
                fm.popBackStack();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("R") != null)
            getBBar().goTo(ZAKLAD);
        else
            super.onBackPressed();
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        updatePrefs();
        setBBar();
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_kalendar;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpKalendar;
    }

}