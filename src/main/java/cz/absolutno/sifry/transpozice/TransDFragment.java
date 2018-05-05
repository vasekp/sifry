package cz.absolutno.sifry.transpozice;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.widget.ColorChunkTextView;
import cz.absolutno.sifry.transpozice.TransView.OnInputListener;

public final class TransDFragment extends AbstractDFragment {

    private TransView vstup;
    private EditText text;
    private TextView tvRes;
    private String reseni;
    private int selItem = -1;
    private int[] tvarIDs;

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_PASTE | HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.transd_layout, container, false);

        tvarIDs = Utils.getIdArray(R.array.iaTTvary);

        text = v.findViewById(R.id.etTVstup);
        text.setOnEditorActionListener(new OnEditorActionListener() {
            @SuppressWarnings("ConstantConditions")
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                zpracuj();
                getView().findViewById(R.id.flTFocus).requestFocus();
                return true;
            }
        });

        v.findViewById(R.id.ivGo).setOnClickListener(new OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            public void onClick(View v) {
                zpracuj();
                getView().findViewById(R.id.flTFocus).requestFocus();
            }
        });

        tvRes = v.findViewById(R.id.tvRes);
        tvRes.setOnClickListener(Utils.copyClickListener);
        reseni = "";

        ((LineTransView) v.findViewById(R.id.trvTVstupLine)).setChunkTextView((ColorChunkTextView) v.findViewById(R.id.ctvTLine));

        ((Spinner) v.findViewById(R.id.spTTvar)).setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                setVstup(tvarIDs[position]);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        v.findViewById(R.id.tvTButRevX).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vstup.onRevX();
            }
        });
        v.findViewById(R.id.tvTButRevY).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vstup.onRevY();
            }
        });
        v.findViewById(R.id.tvTButRevDiag).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vstup.onRevDiag();
            }
        });
        v.findViewById(R.id.ivTButClearTrf).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vstup.clearTrf();
            }
        });
        v.findViewById(R.id.ivTClearSol).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                reseni = "";
                tvRes.setText("");
                vstup.clearMarks();
            }
        });

        if (savedInstanceState != null) {
            reseni = savedInstanceState.getString(App.RESENI);
            selItem = savedInstanceState.getInt(App.VAR);
        }

        return v;
    }

    @SuppressWarnings("ConstantConditions")
    private void setVstup(int id) {
        getView().findViewById(R.id.ctvTLine).setVisibility(id == R.id.idTNic ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.trvTVstupObdelnik).setVisibility(id == R.id.idTObdelnik ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.trvTVstupTrojuhelnik).setVisibility(id == R.id.idTTrojuhelnik ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.trvTVstupRTrojuhelnik).setVisibility(id == R.id.idTRTrojuhelnik ? View.VISIBLE : View.GONE);

        getView().findViewById(R.id.llTRev).setVisibility(id == R.id.idTNic ? View.GONE : View.VISIBLE);

        switch (id) {
            case R.id.idTNic:
                vstup = getView().findViewById(R.id.trvTVstupLine);
                break;
            case R.id.idTObdelnik:
                vstup = getView().findViewById(R.id.trvTVstupObdelnik);
                break;
            case R.id.idTTrojuhelnik:
                vstup = getView().findViewById(R.id.trvTVstupTrojuhelnik);
                break;
            case R.id.idTRTrojuhelnik:
                vstup = getView().findViewById(R.id.trvTVstupRTrojuhelnik);
                break;
        }
        vstup.setOnInputListener(new OnInputListener() {
            public void onInput(String in) {
                reseni += in;
                tvRes.setText(reseni);
            }
        });
        vstup.reloadPreferences();

        if (id != selItem) zpracuj();
        selItem = id;
    }

    private void zpracuj() {
        reseni = "";
        tvRes.setText("");
        vstup.setText(text.getText().toString());
    }

    @Override
    protected String onCopy() {
        return text.getText().toString();
    }

    @Override
    protected void onPaste(String s) {
        text.setText(s);
        zpracuj();
    }

    @Override
    protected void onClear() {
        reseni = "";
        text.setText("");
        vstup.setText("");
        tvRes.setText("");
        vstup.clearMarks();
        vstup.clearTrf();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(App.RESENI, reseni);
        state.putInt(App.VAR, selItem);
    }

    @Override
    public void onResume() {
        super.onResume();
        tvRes.setText(reseni);
    }


    @Override
    protected void onPreferencesChanged() {
        super.onPreferencesChanged();
        if (vstup != null) {
            reseni = "";
            tvRes.setText("");
            vstup.reloadPreferences();
        }
    }

}
