package cz.absolutno.sifry.frekvence;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;

public final class FrekvDFragment extends AbstractDFragment {

    private FrekvExpListAdapter adapter;
    private EditText vstup;

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_PASTE | HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frekvd_layout, container, false);

        vstup = v.findViewById(R.id.etFDVstup);
        vstup.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                zpracuj();
                return true;
            }
        });

        v.findViewById(R.id.ivGo).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                zpracuj();
            }
        });

        v.findViewById(R.id.tgFDAuto).setOnClickListener(autoListener);

        adapter = new FrekvExpListAdapter();
        ((ExpandableListView) v.findViewById(R.id.elFDFrekv)).setAdapter(adapter);

        return v;
    }

    private void zpracuj() {
        adapter.go(vstup.getText().toString());
    }

    @Override
    public boolean saveData(Bundle data) {
        if (vstup.length() == 0)
            return false;
        data.putString(App.VSTUP, vstup.getText().toString());
        return true;
    }

    @SuppressLint("InlinedApi")
    private final OnClickListener autoListener = new OnClickListener() {
        public void onClick(View v) {
            if (((ToggleButton) v).isChecked())
                vstup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                        InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            else
                vstup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
    };

    @Override
    protected String onCopy() {
        return vstup.getText().toString();
    }

    @Override
    protected void onPaste(String s) {
        vstup.setText(s);
        zpracuj();
    }

    @Override
    protected void onClear() {
        vstup.setText("");
        zpracuj();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.reloadPref();
        zpracuj();
    }

}
