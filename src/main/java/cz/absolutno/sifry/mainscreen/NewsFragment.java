package cz.absolutno.sifry.mainscreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class NewsFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String thisVersionName = null;
        try {
            thisVersionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setCancelable(true);
        bld.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        bld.setMessage(Utils.fromHtml(String.format(getString(R.string.patNewVersion), thisVersionName)));
        return bld.create();
    }
}