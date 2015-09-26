package cz.absolutno.sifry.mainscreen;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.HelpActivity;

public final class AboutFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder bld = new Builder(getActivity());
		PackageInfo info = null;
		try {
			info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	bld.setMessage(Utils.fromHtml(String.format(getString(R.string.patAbout), info.versionName)));
    	bld.setCancelable(true);
    	bld.setPositiveButton(R.string.tNews, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
	        	Intent intent = new Intent(getActivity(), HelpActivity.class);
	        	intent.putExtra(App.SPEC, R.string.tHelpNews);
	        	startActivity(intent);
				dialog.dismiss();
			}
		});
    	bld.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	bld.setNeutralButton(R.string.tLicence, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
	        	Intent intent = new Intent(getActivity(), LicenceActivity.class);
	        	startActivity(intent);
				dialog.dismiss();
			}
		});
    	bld.setTitle(R.string.tAbout);
    	bld.setIcon(R.drawable.icon);
    	return bld.create();
	}
}