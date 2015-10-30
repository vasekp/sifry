package cz.absolutno.sifry.zapisnik;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class ZapisnikActivity extends BottomBarActivity {

    protected static final int DEFAULT = 0;
    protected static final int EXPORT = 1;

    public static final String FILE = "zapisnik";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getBBar().setEntries(new String[]{getString(R.string.tZDefault), getString(R.string.tExport)}, DEFAULT);
            AbstractDFragment frag = new ZapisnikDFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.content, frag, "D");
            trans.commit();
        }
    }

    @Override
    protected void onBottomBarChange(int curIx, int lastIx) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        AbstractDFragment frag;
        Bundle args;
        switch (curIx) {
            case DEFAULT:
                fm.popBackStack();
                break;
            case EXPORT:
                String state = Environment.getExternalStorageState();
                if (!state.equals(Environment.MEDIA_MOUNTED)) {
                    Utils.toast(R.string.tZEErrStorage);
                    getBBar().goTo(DEFAULT);
                    break;
                }
                frag = new ZapisnikEFragment();
                args = new Bundle();
                if (!getCurrFragment().saveData(args)) {
                    Utils.toast(R.string.tZEErrEmpty);
                    getBBar().goTo(DEFAULT);
                    break;
                }
                frag.setArguments(args);
                trans.replace(R.id.content, frag, "E");
                trans.addToBackStack(null);
                trans.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("E") != null || fm.findFragmentByTag("SZ") != null)
            getBBar().goTo(DEFAULT);
        else
            super.onBackPressed();
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_zapisnik;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpZapisnik;
    }

}
