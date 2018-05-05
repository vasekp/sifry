package cz.absolutno.sifry.common.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.BottomBarView;

@SuppressWarnings("deprecation")
public abstract class BottomBarActivity extends FragmentActivity implements OnBackStackChangedListener {

    private static final int HAS_COPY = 0x1;
    private static final int HAS_PASTE = 0x2;
    private static final int HAS_CLEAR = 0x4;

    private BottomBarView bbar;

    protected abstract int getPrefID();

    protected abstract int getHelpID();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        App.updateLocale();
        setContentView(R.layout.bottom_bar_layout);
        bbar = findViewById(R.id.bottom_bar);
        bbar.setOnChangeListener(new BottomBarView.OnChangeListener() {
            public void onChange(int curIx, int lastIx) {
                onBottomBarChange(curIx, lastIx);
            }
        });
    }

    public BottomBarView getBBar() {
        return bbar;
    }

    protected AbstractDFragment getCurrFragment() {
        return (AbstractDFragment) getSupportFragmentManager().findFragmentById(R.id.content);
    }

    protected abstract void onBottomBarChange(int curIx, int lastIx);

    public void onBackStackChanged() {
        ActivityCompat.invalidateOptionsMenu(this);
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.komponenty_menu, menu);

        AbstractDFragment currFragment = getCurrFragment();
        boolean hasCopy, hasPaste, hasClear;
        if (currFragment != null) {
            int caps = currFragment.getMenuCaps();
            hasCopy = ((caps & HAS_COPY) != 0);
            hasPaste = ((caps & HAS_PASTE) != 0);
            hasClear = ((caps & HAS_CLEAR) != 0);
        } else {
            hasCopy = false;
            hasPaste = false;
            hasClear = false;
        }

        menu.findItem(R.id.mCtxSettings).setVisible(getPrefID() != 0);
        menu.findItem(R.id.mCtxCopy).setVisible(hasCopy);
        menu.findItem(R.id.mCtxPaste).setVisible(hasPaste);
        menu.findItem(R.id.mCtxClear).setVisible(hasClear);
        return true;
    }

    @SuppressLint("InlinedApi") /* EXTRA_*, using the value of EXTRA_SHOW_FRAGMENT_ARGUMENTS in SettingsActivity */
    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        AbstractDFragment currFragment = getCurrFragment(); /* Can't be null if we're in Copy, Cut, Paste */
        switch (item.getItemId()) {
            case R.id.mCtxSettings:
                intent = new Intent(this, SettingsActivity.class);
                Bundle b = new Bundle();
                b.putInt(App.SPEC, getPrefID());
                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                /* Class does not exist in Gingerbread ⇒ getName() results in a NoClassDefFoundError */
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, "cz.absolutno.sifry.common.activity.SettingsFragment");
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, b);
                startActivity(intent);
                return true;
            case R.id.mCtxHelp:
                intent = new Intent(this, HelpActivity.class);
                intent.putExtra(App.SPEC, getHelpID());
                startActivity(intent);
                return true;
            case R.id.mCtxCopy:
                String s = currFragment.onCopy();
                if (s != null && !s.equals(""))
                    //noinspection ConstantConditions
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setText(s.replace('·', ' '));
                else
                    Utils.toast(R.string.tErrCopy);
                return true;
            case R.id.mCtxPaste:
                //noinspection ConstantConditions
                CharSequence cs = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).getText();
                if (cs != null && cs.length() != 0)
                    currFragment.onPaste(cs.toString());
                else
                    Utils.toast(R.string.tErrPaste);
                return true;
            case R.id.mCtxClear:
                currFragment.onClear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp == null) return;
        if (sp.getBoolean("pref_changed", false)) {
            onPreferencesChanged();
            getCurrFragment().onPreferencesChanged();
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("pref_changed", false);
        editor.apply();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    protected void onPreferencesChanged() {
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        AbstractDFragment currFragment = getCurrFragment();
        if (currFragment != null)
            currFragment.onResumeFragments();
    }

}
