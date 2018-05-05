package cz.absolutno.sifry.morse;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.activity.BottomBarActivity;

public final class MorseActivity extends BottomBarActivity {

    private static final int ENCODE = 0;
    private static final int DECODE = 1;
    private static final int REFERENCE = 2;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            getBBar().setEntries(new String[]{getString(R.string.tEncode), getString(R.string.tDecode), getString(R.string.tRef)}, DECODE);
            AbstractDFragment frag = new MorseDFragment();
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
            case ENCODE:
                frag = getCurrFragment();
                if (frag.getTag().equals("D")) {
                    args = new Bundle();
                    frag = new MorseCFragment();
                    if (getCurrFragment().saveData(args))
                        frag.setArguments(args);
                    trans.replace(R.id.content, frag, "C");
                    trans.addToBackStack(null);
                    trans.commit();
                } else if (frag.getTag().equals("T"))
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
            case REFERENCE:
                frag = new MorseRFragment();
                trans.replace(R.id.content, frag, "R");
                trans.addToBackStack(null);
                trans.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("C") != null || fm.findFragmentByTag("R") != null)
            getBBar().goTo(DECODE);
        else
            super.onBackPressed();
    }

    @Override
    protected int getPrefID() {
        return 0;
    }

    @Override
    protected int getHelpID() {
        return R.string.tHelpMorse;
    }

}