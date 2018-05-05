package cz.absolutno.sifry.kalendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.alphabet.CzechAlphabet;

public final class KalendarDFragment extends AbstractDFragment {

    private AutoCompleteTextView jmeno;
    private CharSequence[][] db;
    private KalendarView kalMain;
    private EditText etDay, etMonth, etYear, etSince;
    private TextView tvSince;
    private DateFormat fmt;
    private boolean termLoading = false;

    @Override
    protected int getMenuCaps() {
        return HAS_CLEAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.kalendar_layout, container, false);
        kalMain = v.findViewById(R.id.kalKDMain);

        fmt = DateFormat.getDateInstance(DateFormat.SHORT);

        etDay = v.findViewById(R.id.etKDDenRoku);
        etMonth = v.findViewById(R.id.etKDMesic);
        etYear = v.findViewById(R.id.etKDRok);
        etSince = v.findViewById(R.id.etKDDniOd);
        tvSince = v.findViewById(R.id.tvKDDniOd);

        v.findViewById(R.id.btKDMesicMinus).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                kalMain.add(Calendar.MONTH, -1);
            }
        });
        v.findViewById(R.id.btKDMesicPlus).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                kalMain.add(Calendar.MONTH, 1);
            }
        });
        v.findViewById(R.id.btKDRokMinus).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                kalMain.add(Calendar.YEAR, -1);
            }
        });
        v.findViewById(R.id.btKDRokPlus).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                kalMain.add(Calendar.YEAR, 1);
            }
        });

        etMonth.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    kalMain.set(Calendar.MONTH, Integer.valueOf(etMonth.getText().toString()) - 1);
                } catch (NumberFormatException e) {
                    kalMain.requestListener();
                }
                kalMain.requestFocus();
                return false;
            }
        });
        etYear.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    kalMain.set(Calendar.YEAR, Integer.valueOf(etYear.getText().toString()));
                } catch (NumberFormatException e) {
                    kalMain.requestListener();
                }
                kalMain.requestFocus();
                return false;
            }
        });
        etDay.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    kalMain.set(Calendar.DAY_OF_YEAR, Integer.valueOf(etDay.getText().toString()));
                } catch (NumberFormatException e) {
                    kalMain.requestListener();
                }
                kalMain.requestFocus();
                return false;
            }
        });
        etSince.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    kalMain.setSince(Integer.valueOf(etSince.getText().toString()));
                } catch (NumberFormatException e) {
                    kalMain.requestListener();
                }
                kalMain.requestFocus();
                return false;
            }
        });

        jmeno = v.findViewById(R.id.etKDJmeno);
        jmeno.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                hledej();
                return true;
            }
        });

        jmeno.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
                final String s = jmeno.getAdapter().getItem(position).toString();
                if (s.indexOf(':') >= 0)
                    jmeno.setText(s.substring(s.indexOf(':') + 2));
                else
                    jmeno.setText(s);
                hledej();
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        termLoading = true;
    }

    @Override
    protected void onClear() {
        kalMain.clear();
    }

    private final KalendarView.OnChangeListener kalendarListener = new KalendarView.OnChangeListener() {
        @SuppressLint("SetTextI18n")
        public void onChange(int year, int month, int day, int dayYear, int daysSince) {
            etMonth.setText(Integer.toString(month + 1));
            etYear.setText(Integer.toString(year));
            etDay.setText(Integer.toString(dayYear));
            etSince.setText(Integer.toString(daysSince));
            if (db != null)
                jmeno.setText(db[month][day - 1]);
        }

        @SuppressLint("SetTextI18n")
        public void onChangeAnchor(Date anchor, int daysSince) {
            tvSince.setText(String.format(getString(R.string.patKDDniOd), fmt.format(anchor)));
            etSince.setText(Integer.toString(daysSince));
        }
    };

    private void hledej() {
        boolean nalezeno = false;
        String jm = jmeno.getText().toString();
        if (db == null) return;
        for (int m = 0; m < db.length; m++)
            for (int d = 0; d < db[m].length; d++) {
                String jmena = db[m][d].toString();
                for (String jmeno : jmena.split(", "))
                    if (jmeno.equals(jm)) {
                        kalMain.setDate(m, d + 1);
                        nalezeno = true;
                    }
            }
        if (!nalezeno)
            Utils.toast(R.string.tNotFound);
        kalMain.requestFocus();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();

        int kal = ((KalendarActivity) getActivity()).getKalendar();
        getView().findViewById(R.id.llKDSvatky).setVisibility(kal != 0 ? View.VISIBLE : View.GONE);
        if (kal != 0)
            loadKalendar(kal);

        kalMain.setOnChangeListener(kalendarListener);
        kalMain.requestListener();
    }

    @SuppressWarnings("ConstantConditions")
    private void loadKalendar(int kal) {
        getView().findViewById(R.id.pbKDLoading).setVisibility(View.VISIBLE);
        db = Utils.load2DStringArray(kal);

        final ArrayList<JmenoItem> flat = new ArrayList<>();

        final Runnable loadingFinished = new Runnable() {
            public void run() {
                jmeno.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.kalendar_ac_item, flat));
                getView().findViewById(R.id.pbKDLoading).setVisibility(View.GONE);
            }
        };

        final Runnable loadAC = new Runnable() {
            public void run() {
                A:
                for (CharSequence[] x : db)
                    for (CharSequence y : x) {
                        String jmena = y.toString();
                        if (jmena.subSequence(0, 1).equals("*")) continue;
                        for (String jmeno : jmena.split(", "))
                            flat.add(new JmenoItem(CzechAlphabet.normalize(jmeno) + " : " + jmeno));
                        if (termLoading)
                            break A;
                    }
                if (termLoading) return;
                Collections.sort(flat);
                getActivity().runOnUiThread(loadingFinished);
            }
        };

        termLoading = false;
        new Thread(loadAC).start();

        kalMain.requestListener();
    }


    private static final class JmenoItem implements Comparable<JmenoItem> {
        private final String data;

        JmenoItem(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            if (data.indexOf(':') >= 0)
                return data.substring(data.indexOf(':') + 2);
            else
                return data;
        }

        public int compareTo(@NonNull JmenoItem another) {
            return data.compareTo(another.data);
        }
    }


}
