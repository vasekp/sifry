package cz.absolutno.sifry.common.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.ClipboardManager;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

@SuppressWarnings("deprecation")
public final class CopyFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String str = getArguments().getString(App.VSTUP);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.tCopy, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //noinspection ConstantConditions
                ((ClipboardManager) getActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE)).setText(str);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.tShare, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, str);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, null));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.tClose, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(String.format(getString(R.string.patCopyCap), str));

        return builder.create();
    }

}
