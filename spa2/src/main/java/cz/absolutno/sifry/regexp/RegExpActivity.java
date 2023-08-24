package cz.absolutno.sifry.regexp;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class RegExpActivity extends BottomBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getBBar().setEntries(null, 0);

            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.add(new ReferenceFragment(), "ref");
            trans.replace(R.id.content, new RegExpDFragment(), "D");
            trans.commit();
        }
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_regexp;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpRegExp;
    }

    @Override
    protected void onBottomBarChange(int curIx, int lastIx) {
    }

    public void loadRE(RegExpNative re) {
        RegExpDFragment frag = (RegExpDFragment) getSupportFragmentManager().findFragmentByTag("D");
        if (frag != null)
            frag.loadRE(re);
    }

    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        ReferenceFragment frag = (ReferenceFragment) getSupportFragmentManager().findFragmentByTag("ref");
        if (frag != null)
            frag.updateFilename();
    }

}