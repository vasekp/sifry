package cz.absolutno.sifry.regexp;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class DownloadFragment extends Fragment {
	
	private File filesDir;
	private String filename;
	private String fnPart;
	private DownloadTask dlTask = null;
	private boolean dieOnCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle args = getArguments();
		filesDir = new File(args.getString(App.VSTUP1));
		filename = args.getString(App.VSTUP2);
		dieOnCancel = args.getBoolean(App.VAR);
		fnPart = filename + ".part";
        if(!filesDir.exists())
        	filesDir.mkdirs();

        File f = new File(filesDir, fnPart);
		String downloadUrl = App.getContext().getString(R.string.tRDDownloadDirURL) + filename;
		
		dlTask = new DownloadTask(updater, downloadUrl, f.getAbsolutePath());
		dlTask.execute();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ProgressFragment frag = new ProgressFragment();
		frag.setArguments(getArguments());
		frag.show(getFragmentManager(), "progress");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private final ProgressUpdater updater = new ProgressUpdater() {
		public void update(int value) {
			FragmentManager fm = getFragmentManager();
			if(fm == null)
				return;
			ProgressFragment prog = (ProgressFragment)fm.findFragmentByTag("progress");
			if(prog != null)
				prog.update(value);
		}
		
		public void close(boolean success) {
			if(success) {
	        	File fPart = new File(filesDir, fnPart);
	        	File f = new File(filesDir, filename);
	        	fPart.renameTo(f);
	        	if(getActivity() != null)
	        		((RegExpActivity)getActivity()).reloadRE();
				FragmentManager fm = getFragmentManager();
				if(fm == null)
					return;
				ProgressFragment prog = (ProgressFragment)fm.findFragmentByTag("progress");
	        	if(prog != null)
	        		prog.dismiss();
	        	FragmentTransaction trans = getFragmentManager().beginTransaction();
				trans.remove(DownloadFragment.this);
				trans.commit();
			} else {
	    		File fPart = new File(filesDir, fnPart);
	    		fPart.delete();
	        	if(dieOnCancel && getActivity() != null)
	        		getActivity().finish();
			}
		}
	};
	
	
	public static interface ProgressUpdater {
		public void update(int progress);
		public void close(boolean success);		
	}

}