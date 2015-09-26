package cz.absolutno.sifry.regexp;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class ReferenceFragment extends Fragment {
	
	RegExpNative re = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		String filename = getString(R.string.tRDFilename);
		re = new RegExpNative(filename);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((RegExpActivity)getActivity()).loadRE(re);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(re != null)
			re.free();
	}
	
	public RegExpNative getRE() {
		return re;
	}
	
}