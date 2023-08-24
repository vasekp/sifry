package cz.absolutno.sifry.common.activity;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public final class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = 0;

        String action = getArguments().getString("action");
        if (action != null) {
            String prefix = getActivity().getPackageName() + ".R.xml.";
            if (action.startsWith(prefix)) {
                try {
                    id = R.xml.class.getField(action.substring(prefix.length())).getInt(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        id = getArguments().getInt(App.SPEC, id);
        addPreferencesFromResource(id);

        int cnt = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < cnt; i++)
            updateSummary(getPreferenceScreen().getPreference(i));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_changed")) return;
        updateSummary(findPreference(key));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("pref_changed", true);
        editor.apply();
        if (key.equals("pref_locale"))
            App.restart();
    }

    private void updateSummary(Preference pref) {
        if (pref instanceof ListPreference)
            pref.setSummary(((ListPreference) pref).getEntry().toString());
    }

}