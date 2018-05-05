package cz.absolutno.sifry.zapisnik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

@SuppressWarnings("deprecation")
public final class ZapisnikSFragment extends DialogFragment {

    private EditText etNazev;
    private EditText etHeslo;
    private EditText etRes;
    private EditText etUpres;
    private EditText etPozn;

    private View mainView;
    private CheckBox cbPrichod, cbOdchod;
    private TextView tvPrichodDatum, tvOdchodDatum;
    private Calendar cPrichod, cOdchod;
    private TimePicker tpPrichod, tpOdchod;
    private DatePicker dpPrichod, dpOdchod;

    public enum Mod {
        PRICHOD,
        ODCHOD,
        ZMENA
    }

    private Stanoviste s;

    private OnPositiveButtonListener onPositiveButtonListener = null;


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onSave();
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        mainView = App.getInflater().inflate(R.layout.zapisnik_dialog, null);
        etNazev = mainView.findViewById(R.id.etZDNazev);
        etHeslo = mainView.findViewById(R.id.etZDHeslo);
        etRes = mainView.findViewById(R.id.etZDRes);
        etUpres = mainView.findViewById(R.id.etZDUpres);
        etPozn = mainView.findViewById(R.id.etZDPozn);
        cbPrichod = mainView.findViewById(R.id.cbZDPrichod);
        cbOdchod = mainView.findViewById(R.id.cbZDOdchod);
        tpPrichod = mainView.findViewById(R.id.tpZDPrichod);
        tpOdchod = mainView.findViewById(R.id.tpZDOdchod);
        dpPrichod = mainView.findViewById(R.id.dpZDPrichod);
        dpOdchod = mainView.findViewById(R.id.dpZDOdchod);
        tvPrichodDatum = mainView.findViewById(R.id.tvZDPrichodDatum);
        tvOdchodDatum = mainView.findViewById(R.id.tvZDOdchodDatum);

        cbPrichod.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tpPrichod.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                tvPrichodDatum.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                dpPrichod.setVisibility(View.GONE);
                if (isChecked)
                    tpPrichod.requestFocus();
            }
        });
        cbOdchod.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tpOdchod.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                tvOdchodDatum.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                dpOdchod.setVisibility(View.GONE);
                if (isChecked)
                    tpOdchod.requestFocus();
            }
        });

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);

        tvPrichodDatum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dpPrichod.setVisibility(View.VISIBLE);
                dpPrichod.requestFocus();
                v.setVisibility(View.GONE);
            }
        });
        tvOdchodDatum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dpOdchod.setVisibility(View.VISIBLE);
                dpOdchod.requestFocus();
                v.setVisibility(View.GONE);
            }
        });

        tpPrichod.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getActivity()));
        tpOdchod.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getActivity()));

        s = (Stanoviste) (getArguments().getSerializable(App.DATA));
        Mod mod = (Mod) (getArguments().getSerializable(App.SPEC));
        assert mod != null;

        boolean showPrichod = false, showOdchod = false;
        switch (mod) {
            case PRICHOD:
                builder.setTitle(R.string.cptZDAdd);
                Selection.setSelection(etNazev.getText(), etNazev.length());
                cbPrichod.setChecked(true);
                cbOdchod.setChecked(false);
                showPrichod = true;
                showOdchod = false;
                break;
            case ODCHOD:
                builder.setTitle(R.string.cptZDModify);
                cbPrichod.setChecked(s.prichod != null);
                cbOdchod.setChecked(true);
                showPrichod = false;
                showOdchod = true;
                etRes.requestFocus();
                break;
            case ZMENA:
                builder.setTitle(R.string.cptZDModify);
                cbPrichod.setChecked(s.prichod != null);
                cbOdchod.setChecked(s.odchod != null);
                showPrichod = true;
                showOdchod = true;
                break;
        }
        mainView.findViewById(R.id.llZDPrichod1).setVisibility(showPrichod ? View.VISIBLE : View.GONE);
        mainView.findViewById(R.id.llZDPrichod2).setVisibility(showPrichod ? View.VISIBLE : View.GONE);
        mainView.findViewById(R.id.llZDOdchod1).setVisibility(showOdchod ? View.VISIBLE : View.GONE);
        mainView.findViewById(R.id.llZDOdchod2).setVisibility(showOdchod ? View.VISIBLE : View.GONE);

        final Button btSipkaPrichod = mainView.findViewById(R.id.btZDSipkaPrichod);
        final Button btSipkaOdchod = mainView.findViewById(R.id.btZDSipkaOdchod);
        btSipkaPrichod.setVisibility(showPrichod ? View.GONE : View.VISIBLE);
        btSipkaOdchod.setVisibility(showOdchod ? View.GONE : View.VISIBLE);
        btSipkaPrichod.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mainView.findViewById(R.id.llZDPrichod1).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.llZDPrichod2).setVisibility(View.VISIBLE);
                btSipkaPrichod.setVisibility(View.GONE);
                getDialog().setTitle(R.string.cptZDModify);
            }
        });
        btSipkaOdchod.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mainView.findViewById(R.id.llZDOdchod1).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.llZDOdchod2).setVisibility(View.VISIBLE);
                btSipkaOdchod.setVisibility(View.GONE);
                getDialog().setTitle(R.string.cptZDModify);
            }
        });

        etNazev.setText(s.nazev);
        etHeslo.setText(s.heslo);
        etRes.setText(s.reseni);
        etUpres.setText(s.upres);
        etPozn.setText(s.pozn);

        if (savedInstanceState != null)
            cPrichod = (Calendar) savedInstanceState.getSerializable(App.VSTUP1);
        else {
            cPrichod = Calendar.getInstance();
            if (s.prichod != null)
                cPrichod.setTime(s.prichod);
        }
        assert cPrichod != null;
        tvPrichodDatum.setText(dateFormatter.format(cPrichod.getTime()));
        dpPrichod.updateDate(cPrichod.get(Calendar.YEAR), cPrichod.get(Calendar.MONTH), cPrichod.get(Calendar.DAY_OF_MONTH));
        tpPrichod.setCurrentHour(cPrichod.get(Calendar.HOUR_OF_DAY));
        tpPrichod.setCurrentMinute(cPrichod.get(Calendar.MINUTE));

        if (savedInstanceState != null)
            cOdchod = (Calendar) savedInstanceState.getSerializable(App.VSTUP2);
        else {
            cOdchod = Calendar.getInstance();
            if (s.odchod != null)
                cOdchod.setTime(s.odchod);
        }
        tvOdchodDatum.setText(dateFormatter.format(cOdchod.getTime()));
        dpOdchod.updateDate(cOdchod.get(Calendar.YEAR), cOdchod.get(Calendar.MONTH), cOdchod.get(Calendar.DAY_OF_MONTH));
        tpOdchod.setCurrentHour(cOdchod.get(Calendar.HOUR_OF_DAY));
        tpOdchod.setCurrentMinute(cOdchod.get(Calendar.MINUTE));

        builder.setIcon(0);
        builder.setView(mainView);
        return builder.create();
    }

    private void onSave() {
        s.nazev = etNazev.getText().toString();
        s.heslo = etHeslo.getText().toString();
        s.reseni = etRes.getText().toString();
        s.upres = etUpres.getText().toString();
        s.pozn = etPozn.getText().toString();
        if (cbPrichod.isChecked()) {
            cPrichod.set(Calendar.YEAR, dpPrichod.getYear());
            cPrichod.set(Calendar.MONTH, dpPrichod.getMonth());
            cPrichod.set(Calendar.DAY_OF_MONTH, dpPrichod.getDayOfMonth());
            cPrichod.set(Calendar.HOUR_OF_DAY, tpPrichod.getCurrentHour());
            cPrichod.set(Calendar.MINUTE, tpPrichod.getCurrentMinute());
            s.prichod = cPrichod.getTime();
        } else
            s.prichod = null;
        if (cbOdchod.isChecked()) {
            cOdchod.set(Calendar.YEAR, dpOdchod.getYear());
            cOdchod.set(Calendar.MONTH, dpOdchod.getMonth());
            cOdchod.set(Calendar.DAY_OF_MONTH, dpOdchod.getDayOfMonth());
            cOdchod.set(Calendar.HOUR_OF_DAY, tpOdchod.getCurrentHour());
            cOdchod.set(Calendar.MINUTE, tpOdchod.getCurrentMinute());
            s.odchod = cOdchod.getTime();
        } else
            s.odchod = null;
        if (onPositiveButtonListener != null)
            onPositiveButtonListener.onPositiveButton(s);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        cPrichod.set(Calendar.YEAR, dpPrichod.getYear());
        cPrichod.set(Calendar.MONTH, dpPrichod.getMonth());
        cPrichod.set(Calendar.DAY_OF_MONTH, dpPrichod.getDayOfMonth());
        cOdchod.set(Calendar.YEAR, dpOdchod.getYear());
        cOdchod.set(Calendar.MONTH, dpOdchod.getMonth());
        cOdchod.set(Calendar.DAY_OF_MONTH, dpOdchod.getDayOfMonth());
        bundle.putSerializable(App.VSTUP1, cPrichod);
        bundle.putSerializable(App.VSTUP2, cOdchod);
    }


    public void setOnPositiveButtonListener(OnPositiveButtonListener opbl) {
        this.onPositiveButtonListener = opbl;
    }

    public interface OnPositiveButtonListener {
        void onPositiveButton(Stanoviste s);
    }
}
