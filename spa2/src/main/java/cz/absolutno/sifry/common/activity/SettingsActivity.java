package cz.absolutno.sifry.common.activity;

import static android.content.pm.PackageManager.GET_META_DATA;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.List;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

@SuppressWarnings("deprecation")
public final class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.updateLocale();

        try {
            setTitle(getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            // leave title as it is
        }

        String action = getIntent().getAction();
        if (action != null && action.equals(getPackageName() + ".PREF_CLEAR")) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
            finish();
            App.restart();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(App.localizedContext(newBase));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_changed")) return;
        updateSummary(findPreference(key));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("pref_changed", true);
        editor.commit();
        if (key.equals("pref_locale"))
            App.restart();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    private void updateSummary(Preference pref) {
        if (pref instanceof ListPreference)
            pref.setSummary(((ListPreference) pref).getEntry().toString());
    }

}
