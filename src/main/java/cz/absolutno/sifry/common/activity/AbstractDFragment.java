package cz.absolutno.sifry.common.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnLongClickListener;

public abstract class AbstractDFragment extends Fragment {

    protected static final int HAS_COPY = 0x1;
    protected static final int HAS_PASTE = 0x2;
    protected static final int HAS_CLEAR = 0x4;

    protected abstract int getMenuCaps();

    protected String onCopy() {
        return null;
    }

    protected void onPaste(String s) {
    }

    protected void onClear() {
    }

    protected void onPreferencesChanged() {
    }

    protected void onResumeFragments() {
    }

    public boolean saveData(Bundle data) {
        return false;
    }

    public void loadData(Bundle data) {
    }

    @Override
    public void onPause() {
        super.onPause();
        //noinspection ConstantConditions
        ((android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((BottomBarActivity) getActivity()).getBBar().getWindowToken(), 0);
    }

    /* Convenience for subclasses */
    protected final OnLongClickListener clearListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            onClear();
            return true;
        }
    };

}
