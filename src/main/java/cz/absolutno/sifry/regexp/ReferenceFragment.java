package cz.absolutno.sifry.regexp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import cz.absolutno.sifry.R;

public final class ReferenceFragment extends Fragment {

    private RegExpNative re = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        re = new RegExpNative(getFilename());
    }

    public void updateFilename() {
        re.switchFilename(getFilename());
    }

    private String getFilename() {
        String filename = "";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp != null)
            filename = sp.getString("pref_re_dictionary", filename);
        if (filename.length() == 0)
            filename = getString(R.string.pref_re_dictionary_default);
        return filename;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((RegExpActivity) getActivity()).loadRE(re);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (re != null)
            re.free();
    }

    @SuppressWarnings("unused")
    public RegExpNative getRE() {
        return re;
    }

}