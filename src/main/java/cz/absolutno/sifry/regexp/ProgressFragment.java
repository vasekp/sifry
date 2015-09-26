package cz.absolutno.sifry.regexp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class ProgressFragment extends DialogFragment {
	
	private boolean dieOnCancel;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dieOnCancel = getArguments().getBoolean(App.VAR);
		ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(getString(R.string.tRDStahovani));
		pd.setIndeterminate(false);
		pd.setMax(100);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setCancelable(true);
		return pd;
	}
	
	public void update(int value) {
		ProgressDialog dialog = (ProgressDialog)getDialog();
		if(dialog != null)
			dialog.setProgress(value);
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if(dieOnCancel)
			getActivity().finish();
	}

}