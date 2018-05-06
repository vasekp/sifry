package cz.absolutno.sifry.common.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.FileOutputStream;
import java.io.IOException;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.DrawOnWhiteView;

public abstract class AbstractCFragment extends AbstractDFragment {

    protected abstract boolean encode(String input);

    protected abstract void packData(Bundle data);

    @Override
    protected final int getMenuCaps() {
        return HAS_COPY | HAS_PASTE | HAS_CLEAR;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            loadData(args);
        view.findViewById(R.id.ivGo).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                zpracuj(true);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private EditText vstup() {
        return (EditText) getView().findViewById(R.id.etCVstup);
    }

    @Override
    public final boolean saveData(Bundle data) {
        if (vstup().getText().length() == 0)
            return false;
        zpracuj();
        packData(data);
        return true;
    }

    @Override
    public final void loadData(Bundle data) {
        vstup().setText(data.getString(App.VSTUP));
        zpracuj();
    }

    @Override
    public void onResume() {
        super.onResume();
        vstup().setOnEditorActionListener(inputListener);
        zpracuj(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        //noinspection ConstantConditions
        ((android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((BottomBarActivity) getActivity()).getBBar().getWindowToken(), 0);
    }

    private final OnEditorActionListener inputListener = new OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            zpracuj(true);
            return true;
        }
    };

    private void zpracuj(boolean novy) {
        boolean err = encode(vstup().getText().toString());
        if (err && novy)
            Utils.toast(R.string.tErrEncode);
    }

    protected final void zpracuj() {
        zpracuj(false);
    }

    @Override
    protected final String onCopy() {
        return vstup().getText().toString();
    }

    @Override
    protected final void onPaste(String s) {
        vstup().setText(s);
        zpracuj(true);
    }

    @Override
    protected final void onClear() {
        vstup().setText("");
        zpracuj(false);
    }


    @SuppressLint("WorldReadableFiles") // temporary data intended for the public
    @SuppressWarnings("deprecation")
    protected final void copyImage(ViewGroup vg) {
        int sz = vg.getChildCount();
        if (sz == 0) return;
        int hMax = 0, wTot = 0;
        for (int i = 0; i < sz; i++) {
            View v = vg.getChildAt(i);
            if (v.getVisibility() == View.GONE) continue;
            int w = v.getWidth();
            int h = v.getHeight() - v.getPaddingTop() - v.getPaddingBottom();
            if (h > hMax) hMax = h;
            wTot += w;
        }
        if (wTot == 0 || hMax == 0) return;
        Bitmap bmp = Bitmap.createBitmap(wTot, hMax, Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        Drawable drw = vg.getBackground();
        if (drw != null) drw.draw(c);
        for (int i = 0; i < sz; i++) {
            View v = vg.getChildAt(i);
            if (v.getVisibility() == View.GONE) continue;
            int w = v.getWidth();
            int h = v.getHeight() - v.getPaddingTop() - v.getPaddingBottom();
            int d = c.getSaveCount();
            c.save();
            c.translate(0, (hMax - h) / 2 - v.getPaddingTop());
            c.clipRect(0, 0, w, h + v.getPaddingTop() + v.getPaddingBottom());
            if (v instanceof DrawOnWhiteView)
                ((DrawOnWhiteView) v).drawOnWhite(c);
            else
                v.draw(c);
            c.restoreToCount(d);
            c.translate(w, 0);
        }
        try {
            final String fname = getContext().getString(R.string.tmpBitmapName);
            FileOutputStream fos;
            fos = getActivity().openFileOutput(fname, Context.MODE_PRIVATE);
            bmp.compress(CompressFormat.PNG, 100, fos);
            fos.close();
            Bundle args = new Bundle();
            args.putString(App.VSTUP, getActivity().getFileStreamPath(fname).toString());
            args.putInt(App.VSTUP1, bmp.getWidth());
            args.putInt(App.VSTUP2, bmp.getHeight());
            BitmapFragment dialog = new BitmapFragment();
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "copy");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final OnItemClickListener genItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
            View v = childView.findViewById(R.id.cont);
            if (v instanceof TextView)
                Utils.copyExact(getActivity(), ((TextView) v).getText().toString());
            else if (v instanceof ViewGroup)
                copyImage((ViewGroup) v);
        }
    };

}
