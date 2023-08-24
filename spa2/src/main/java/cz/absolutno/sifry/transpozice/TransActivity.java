package cz.absolutno.sifry.transpozice;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class TransActivity extends BottomBarActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getBBar().setEntries(null, 0);
            AbstractDFragment fragment = new TransDFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.content, fragment);
            trans.commit();
        }
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_parse;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpTrans;
    }

    @Override
    protected void onBottomBarChange(int curIx, int lastIx) {
    }
}
