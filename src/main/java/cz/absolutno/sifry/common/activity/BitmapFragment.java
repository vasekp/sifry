package cz.absolutno.sifry.common.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class BitmapFragment extends DialogFragment {

    private static final String PROVIDER = "cz.absolutno.sifry.fileprovider";

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final File file = new File(getArguments().getString(App.VSTUP));
        int w = getArguments().getInt(App.VSTUP1);
        int h = getArguments().getInt(App.VSTUP2);
        long sz = file.length();

        @SuppressLint("InflateParams") // exception for AlertDialog
        final View content = App.getInflater().inflate(R.layout.bitmap_dialog, null);
        ((TextView) content.findViewById(R.id.tvBmpDMessage)).setText(String.format(getString(R.string.patBitmapCap), w, h, sz));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        builder.setCancelable(true);
        builder.setNeutralButton(R.string.tShare, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(), PROVIDER, file));
                intent.setType("image/png");
                startActivity(Intent.createChooser(intent, null));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.tClose, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
