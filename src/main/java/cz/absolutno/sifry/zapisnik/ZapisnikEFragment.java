package cz.absolutno.sifry.zapisnik;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;

public final class ZapisnikEFragment extends AbstractDFragment {

    private EditText tv;
    private LinearLayout ll;
    private CheckBox cbExt;
    private int cnt;
    private ArrayList<Stanoviste> stan;

    @Override
    protected int getMenuCaps() {
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = App.getInflater().inflate(R.layout.zapisnik_export_layout, container, false);

        String name = Utils.normalizeFN(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(new Date()));
        tv = content.findViewById(R.id.etZENazev);
        tv.setText(String.format(getString(R.string.patZENazev), name));

        ll = content.findViewById(R.id.llZEPole);
        cnt = ll.getChildCount();
        for (int i = 0; i < cnt; i++) {
            CheckBox v = (CheckBox) ll.getChildAt(i);
            String orig = v.getText().toString();
            orig = orig.substring(0, orig.lastIndexOf(':'));
            v.setText(orig);
        }
        cbExt = content.findViewById(R.id.cbZEExt);

        stan = getArguments().getParcelableArrayList(App.DATA);

        content.findViewById(R.id.btZEGo).setOnClickListener(goListener);
        return content;
    }

    private final OnClickListener goListener = new OnClickListener() {
        public void onClick(View vw) {
            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                Utils.toast(R.string.tZEErrStorage);
                return;
            }

            int cntChecked = 0;
            for (int i = 0; i < cnt; i++) {
                CheckBox v = (CheckBox) ll.getChildAt(i);
                if (v.isChecked()) cntChecked++;
            }
            int[] fields = new int[cntChecked];
            int ix = 0;
            for (int i = 0; i < cnt; i++) {
                CheckBox v = (CheckBox) ll.getChildAt(i);
                if (v.isChecked())
                    fields[ix++] = v.getId();
            }
            boolean ext = cbExt.isChecked();

            try {
                File outputDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.tExportDir));
                if(!outputDir.mkdirs())
                    throw new IOException();
                File output = new File(outputDir, tv.getText().toString());
                FileWriter writer = new FileWriter(output);

                String[] arr = Stanoviste.getHeaders(fields);
                for (int i = 0; i < fields.length; i++) {
                    if (i > 0) writer.append(",");
                    writer.append('\"');
                    writer.append(arr[i].replace("\"", "\"\""));
                    writer.append('\"');
                }
                writer.append('\n');

                for (Stanoviste s : stan) {
                    arr = s.toStringArray(fields);
                    for (int i = 0; i < arr.length; i++) {
                        if (i > 0) writer.append(",");
                        writer.append('\"');
                        writer.append(arr[i].replace("\"", "\"\""));
                        writer.append('\"');
                    }
                    writer.append('\n');
                }
                writer.close();
                if (ext) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(output));
                    intent.setType("text/csv");
                    startActivity(Intent.createChooser(intent, null));
                } else {
                    Utils.toast(String.format(getString(R.string.patZESuccess), output.getAbsolutePath()));
                    ((ZapisnikActivity) getActivity()).getBBar().goTo(ZapisnikActivity.DEFAULT);
                }
            } catch (IOException e) {
                Utils.toast(R.string.tErrFile);
            }
        }
    };

}
