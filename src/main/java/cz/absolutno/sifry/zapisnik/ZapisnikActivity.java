package cz.absolutno.sifry.zapisnik;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class ZapisnikActivity extends BottomBarActivity {

    public static final String FILE = "zapisnik";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getBBar().setEntries(null, 0);
            AbstractDFragment fragment = new ZapisnikDFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.content, fragment);
            trans.commit();
        }
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_zapisnik;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpZapisnik;
    }

    @Override
    protected void onBottomBarChange(int curIx, int lastIx) {
    }

}
