package cz.absolutno.sifry.frekvence;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class FrekvActivity extends BottomBarActivity {

    private static final int REF_L = 0;
    private static final int ANALYZA = 1;
    private static final int ESUBS = 2;
    private static final int REF_P = 3;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getBBar().setEntries(new String[]{getString(R.string.tRef), getString(R.string.tFDAnalyza), getString(R.string.tFDESubs), getString(R.string.tRef)}, ANALYZA);
            AbstractDFragment frag = new FrekvDFragment();
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
            case ANALYZA:
                fm.popBackStack();
                break;
            case ESUBS:
                if (lastIx == ANALYZA) {
                    args = new Bundle();
                    frag = new EmpirSubstFragment();
                    if (getCurrFragment().saveData(args))
                        frag.setArguments(args);
                    else {
                        Utils.toast(R.string.tErrVar);
                        getBBar().goTo(ANALYZA);
                        break;
                    }
                    trans.replace(R.id.content, frag, "ES");
                    trans.addToBackStack(null);
                    trans.commit();
                } else
                    fm.popBackStack();
                break;
            case REF_L:
            case REF_P:
                frag = new FrekvRFragment();
                trans.replace(R.id.content, frag, (curIx == REF_L ? "R1" : "R2"));
                trans.addToBackStack(null);
                trans.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("R2") != null)
            getBBar().goTo(ESUBS);
        else if (fm.findFragmentByTag("ES") != null || fm.findFragmentByTag("R1") != null)
            getBBar().goTo(ANALYZA);
        else
            super.onBackPressed();
    }

    @Override
    protected int getPrefID() {
        return R.xml.pref_parse;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpFrekv;
    }

}