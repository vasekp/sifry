package cz.absolutno.sifry.regexp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public final class ReferenceFragment extends Fragment {

    private RegExpNative re = new RegExpNative();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        re.free();
    }

    public RegExpNative getRE() {
        return re;
    }

}