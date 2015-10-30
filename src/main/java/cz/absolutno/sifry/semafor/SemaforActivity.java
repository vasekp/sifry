package cz.absolutno.sifry.semafor;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class SemaforActivity extends BottomBarActivity {

    private static final int REFERENCE = 0;
    private static final int ENCODE = 1;
    private static final int DECODE = 2;
    private static final int VARIANTS = 3;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getBBar().setEntries(new String[]{getString(R.string.tRef), getString(R.string.tEncode), getString(R.string.tDecode), getString(R.string.tVar)}, DECODE);

        if (state == null) {
            AbstractDFragment frag = new SemaforDFragment();
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
            case REFERENCE:
                frag = new SemaforRFragment();
                trans.replace(R.id.content, frag, "R");
                trans.addToBackStack(null);
                trans.commit();
                break;
            case ENCODE:
                if (lastIx == DECODE) {
                    args = new Bundle();
                    frag = new SemaforCFragment();
                    if (getCurrFragment().saveData(args))
                        frag.setArguments(args);
                    trans.replace(R.id.content, frag, "C");
                    trans.addToBackStack(null);
                    trans.commit();
                } else
                    fm.popBackStack();
                break;
            case DECODE:
                frag = getCurrFragment();
                args = new Bundle();
                if (frag.getTag().equals("C"))
                    if (frag.saveData(args))
                        ((AbstractDFragment) fm.findFragmentByTag("D")).loadData(args);
                fm.popBackStack();
                break;
            case VARIANTS:
                args = new Bundle();
                if (getCurrFragment().saveData(args)) {
                    frag = new SemaforVFragment();
                    frag.setArguments(args);
                    trans.replace(R.id.content, frag, "V");
                    trans.addToBackStack(null);
                    trans.commit();
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
        if (fm.findFragmentByTag("R") != null)
            getBBar().goTo(ENCODE);
        else if (fm.findFragmentByTag("C") != null || fm.findFragmentByTag("V") != null)
            getBBar().goTo(DECODE);
        else
            super.onBackPressed();
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_semafor;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpSemafor;
    }

}
