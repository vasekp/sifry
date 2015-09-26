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
		File f = new File(App.getContext().getFilesDir(), filename);
		if(f.exists()) {
			if(f.length() != getResources().getInteger(R.integer.iRDFilesize))
				showQuestionDialog(App.getContext().getFilesDir(), filename, R.string.tRDAktualizace, false);
			re = new RegExpNative(f.getAbsolutePath());			
		} else {
			String state = Environment.getExternalStorageState();
			if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
				File extDir = new File(new File(new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data"),
						App.getContext().getPackageName()), "files");
				f = new File(extDir, filename);
				if(f.exists()) {
					if(f.length() != getResources().getInteger(R.integer.iRDFilesize) && state.equals(Environment.MEDIA_MOUNTED))
						showQuestionDialog(extDir, filename, R.string.tRDAktualizace, false);
					re = new RegExpNative(f.getAbsolutePath());
				} else {
					if(state.equals(Environment.MEDIA_MOUNTED))
						showQuestionDialog(extDir, filename, R.string.tRDStahnout, true);
					else
						showQuestionDialog(App.getContext().getFilesDir(), filename, R.string.tRDStahnoutInterni, true);
				}
			} else {
				showQuestionDialog(App.getContext().getFilesDir(), filename, R.string.tRDStahnoutInterni, true);
			}
		}
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
	
	private void showQuestionDialog(File filesDir, String filename, int question, boolean dieOnCancel) {
		QuestionFragment dialog = new QuestionFragment();
		Bundle args = new Bundle();
		args.putInt(App.VSTUP, question);
		args.putString(App.VSTUP1, filesDir.toString());
		args.putString(App.VSTUP2, filename);
		args.putBoolean(App.VAR, dieOnCancel);
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "question");
	}
	
}