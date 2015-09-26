package cz.absolutno.sifry.common.activity;

import java.util.List;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

@SuppressWarnings("deprecation")
public final class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.updateLocale();
        
        String action = getIntent().getAction();
        if(action != null && action.equals(getPackageName() + ".PREF_CLEAR")) {
        	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        	SharedPreferences.Editor editor = sp.edit();
        	editor.clear();
        	editor.commit();
        	finish();
        	App.restart();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	return;

    	int id = R.xml.pref_headers_legacy;
        
        if(action != null) {
	        String prefix = getPackageName() + ".R.xml.";
	        if(action.startsWith(prefix)) {
	        	try {
					id = R.xml.class.getField(action.substring(prefix.length())).getInt(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
        }
        
        Bundle extras = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS); 
        if(extras != null) id = extras.getInt(App.SPEC, id);

    	addPreferencesFromResource(id);
        int cnt = getPreferenceScreen().getPreferenceCount();
        for(int i = 0; i < cnt; i++)
        	updateSummary(getPreferenceScreen().getPreference(i));
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> target) {
	   loadHeadersFromResource(R.xml.pref_headers, target);
	}
	
	@Override
	public void onPause() {
		super.onPause();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	return;
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	return;
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("pref_changed")) return;
		updateSummary(findPreference(key));
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("pref_changed", true);
		editor.commit();
		if(key.equals("pref_locale"))
			App.restart();
	}
		
	@Override
	protected boolean isValidFragment(String fragmentName) {
		return SettingsFragment.class.getName().equals(fragmentName);
	}
	
	protected void updateSummary(Preference pref) {
		if(pref instanceof ListPreference)
			pref.setSummary(((ListPreference) pref).getEntry().toString());
	}
	
}
