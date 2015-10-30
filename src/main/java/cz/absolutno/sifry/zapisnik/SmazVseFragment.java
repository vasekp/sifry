package cz.absolutno.sifry.zapisnik;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import cz.absolutno.sifry.R;

public final class SmazVseFragment extends DialogFragment {

    private OnPositiveButtonListener onPositiveButtonListener = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.tZClearQ);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (onPositiveButtonListener != null)
                    onPositiveButtonListener.onPositiveButton();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public void setOnPositiveButtonListener(OnPositiveButtonListener opbl) {
        this.onPositiveButtonListener = opbl;
    }

    public interface OnPositiveButtonListener {
        void onPositiveButton();
    }

}