package cz.absolutno.sifry.regexp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import cz.absolutno.sifry.App;

public final class QuestionFragment extends DialogFragment {
	
	private boolean dieOnCancel;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		dieOnCancel = getArguments().getBoolean(App.VAR);
		bld.setMessage(getArguments().getInt(App.VSTUP));
		bld.setCancelable(true);
        bld.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Fragment frag = new DownloadFragment();
		        frag.setArguments(getArguments());
		        FragmentTransaction trans = getFragmentManager().beginTransaction();
				trans.add(frag, "DL");
				trans.commit();
			}
		});
        bld.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if(dieOnCancel)
					getActivity().finish();
			}
		});
		return bld.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if(dieOnCancel) 
			getActivity().finish();
	}
}