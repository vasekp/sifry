package cz.absolutno.sifry.tabulky;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;
import cz.absolutno.sifry.tabulky.ctverec.CtverecVFragment;
import cz.absolutno.sifry.tabulky.malypolsky.MalyPolskyVFragment;
import cz.absolutno.sifry.tabulky.polsky.PolskyVFragment;

public final class TabulkyActivity extends BottomBarActivity {

    private static final int ENCODE = 0;
    private static final int DECODE = 1;
    private static final int VARIANTS = 2;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getBBar().setEntries(new String[]{getString(R.string.tEncode), getString(R.string.tDecode), getString(R.string.tVar)}, DECODE);

        if (state == null) {
            AbstractDFragment frag = new TabulkyDFragment();
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
        boolean res;
        switch (curIx) {
            case ENCODE:
                args = new Bundle();
                frag = new TabulkyCFragment();
                getCurrFragment().saveData(args);
                frag.setArguments(args);
                trans.replace(R.id.content, frag, "C");
                trans.addToBackStack(null);
                trans.commit();
                break;
            case DECODE:
                frag = getCurrFragment();
                args = new Bundle();
                res = frag.getTag().equals("C") && frag.saveData(args);
                fm.popBackStackImmediate();
                if (res)
                    getCurrFragment().loadData(args);
                break;
            case VARIANTS:
                args = new Bundle();
                res = getCurrFragment().saveData(args);
                int selLayout = args.getInt(App.VSTUP1);
                if (selLayout == R.id.idTDMobil) {
                    Utils.toast(R.string.tTDErrMobilVar);
                    getBBar().goTo(DECODE);
                    break;
                }
                if (res) {
                    Class<? extends AbstractDFragment> c;
                    switch (selLayout) {
                        case R.id.idTDVPolsky:
                            c = PolskyVFragment.class;
                            break;
                        case R.id.idTDMPolsky:
                            c = MalyPolskyVFragment.class;
                            break;
                        case R.id.idTDCtverec:
                            c = CtverecVFragment.class;
                            break;
                        default:
                            return;
                    }
                    try {
                        frag = c.newInstance();
                        frag.setArguments(args);
                        trans.replace(R.id.content, frag, "V");
                        trans.addToBackStack(null);
                        trans.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.toast(R.string.tErrVar);
                    getBBar().goTo(DECODE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("C") != null || fm.findFragmentByTag("V") != null)
            getBBar().goTo(DECODE);
        else
            super.onBackPressed();
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_tabulky;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpTabulky;
    }

}
