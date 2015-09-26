package cz.absolutno.sifry.common.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class BitmapFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final File file = new File(getArguments().getString(App.VSTUP));
		int w = getArguments().getInt(App.VSTUP1);
		int h = getArguments().getInt(App.VSTUP2);
		long sz = file.length();
		
		final View content = App.getInflater().inflate(R.layout.bitmap_dialog, null);
		((TextView)content.findViewById(R.id.tvBmpDMessage)).setText(String.format(getString(R.string.patBitmapCap), w, h, sz));
		String date = Utils.normalizeFN(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date()));
		((TextView)content.findViewById(R.id.etBmpDFName)).setText(String.format(getString(R.string.patBitmapFN), date));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(content);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.tSave, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = ((TextView)content.findViewById(R.id.etBmpDFName)).getText().toString();
				File outputDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.tExportDir));
				outputDir.mkdirs();
				File output = new File(outputDir, name);
				try {
					FileInputStream fis = new FileInputStream(file);
					FileOutputStream fos = new FileOutputStream(output);
					FileChannel in = fis.getChannel();
					FileChannel out = fos.getChannel();
					in.transferTo(0, in.size(), out);
					fis.close();
					fos.close();
				} catch (IOException e) {
					Utils.toast(R.string.tErrFile);
				}
				dialog.dismiss();
			}
		});
		builder.setNeutralButton(R.string.tSend, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
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
