package cz.absolutno.sifry.cisla;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public final class CislaDFragment extends AbstractDFragment {

    private TextView tvVstup;
    private TextView tvRes;
    private String reseni = "";
    private int vstup = 0;
    private int mod = -1;
    private int itp = -1;
    private int zaklad;
    private int selItem = -1;
    private String text = "";
    private CislaView cvVstup;
    private Spinner spInterp;
    private Alphabet abcPref, abcPerm, abc;
    private boolean nula = false;

    private int[] mody, zaklady, interp;

    @Override
    protected int getMenuCaps() {
        return HAS_COPY | HAS_CLEAR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            reseni = savedInstanceState.getString(App.RESENI);
            vstup = savedInstanceState.getInt(App.VSTUP1);
            text = savedInstanceState.getString(App.VSTUP2);
            selItem = savedInstanceState.getInt(App.VAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cislad_layout, container, false);

        abcPref = Alphabet.getPreferentialInstance();
        abcPerm = Alphabet.getVariantInstance(24, "");
        abc = abcPref;

        mody = Utils.getIdArray(R.array.iaCDSoustavyMody);
        zaklady = getResources().getIntArray(R.array.iaCDSoustavyZaklady);
        interp = Utils.getIdArray(R.array.iaCDInterp);

        mod = mody[0];
        zaklad = zaklady[0];
        itp = interp[0];

        ((Spinner) v.findViewById(R.id.spCDSoust)).setOnItemSelectedListener(new OnItemSelectedListener() {
            @SuppressWarnings("ConstantConditions")
            public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                mod = mody[position];
                zaklad = zaklady[position];
                updateNula();
                cvVstup.setLayout(mod, zaklad, itp);
                if (mod == R.id.idCDPerm1 || mod == R.id.idCDPerm2) {
                    abc = abcPerm;
                    getView().findViewById(R.id.spCDInterp).setVisibility(View.GONE);
                    getView().findViewById(R.id.tvCDInterp).setVisibility(View.GONE);
                } else {
                    abc = abcPref;
                    getView().findViewById(R.id.spCDInterp).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.tvCDInterp).setVisibility(View.VISIBLE);
                }
                if (position != selItem) {
                    text = "";
                    setVstup(0);
                    selItem = position;
                } else updateVstup();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (savedInstanceState == null)
            ((Spinner) v.findViewById(R.id.spCDSoust)).setSelection(3);

        spInterp = v.findViewById(R.id.spCDInterp);
        ArrayAdapter<InterpItem> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, interpList(abc.count()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterp.setAdapter(adapter);
        spInterp.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
                itp = ((InterpItem) parentView.getItemAtPosition(position)).interp;
                cvVstup.setLayout(mod, zaklad, itp);
                updateNula();
                updateVstup();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (savedInstanceState == null)
            spInterp.setSelection(0);

        cvVstup = v.findViewById(R.id.cvCDVstup);
        cvVstup.setOnInputListener(new CislaView.OnInputListener() {
            public void onInput(int tag, String txt) {
                switch (mod) {
                    case R.id.idCDCisla:
                        setVstup(vstup * zaklad + tag);
                        break;
                    case R.id.idCDRim:
                        text = text + txt;
                        setVstup(CislaConv.parseRoman(text));
                        break;
                    case R.id.idCDPerm1:
                    case R.id.idCDPerm2:
                        text = text + txt;
                        setVstup(CislaConv.parsePerm(text, mod));
                        break;
                }
            }
        });

        tvVstup = v.findViewById(R.id.tvCDVstup);
        tvVstup.setOnClickListener(enterListener);

        tvRes = v.findViewById(R.id.tvRes);
        tvRes.setOnClickListener(Utils.copyClickListener);

        ImageView ivBsp = v.findViewById(R.id.ivBsp);
        ivBsp.setOnClickListener(bspListener);
        ivBsp.setOnLongClickListener(clearListener);

        return v;
    }

    @Override
    public boolean saveData(Bundle data) {
        if (reseni.length() == 0)
            return false;
        data.putString(App.VSTUP, reseni);
        return true;
    }

    @Override
    public void loadData(Bundle data) {
        reseni = data.getString(App.VSTUP);
        text = "";
        vstup = 0;
    }

    private String znak() {
        if (vstup < 0) return "?";
        if (vstup == 0 && !nula) return "?";
        switch (itp) {
            case R.id.idCDPrimo1:
            case R.id.idCDPrimo0:
                return abc.chr(vstup - (nula ? 0 : 1));
            case R.id.idCDModulo1:
            case R.id.idCDModulo0:
                return abc.chr((vstup - (nula ? 0 : 1)) % abc.count());
            case R.id.idCDASCII:
                if (vstup >= 32 && vstup <= 127)
                    return String.valueOf((char) vstup);
                else
                    return "?";
            default:
                return "?";
        }
    }

    private void updateNula() {
        nula = (itp == R.id.idCDPrimo0 || itp == R.id.idCDModulo0) && !(mod == R.id.idCDPerm1 || mod == R.id.idCDPerm2);
    }

    private void updateVstup() {
        if (vstup == 0 && !nula) {
            tvVstup.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        switch (mod) {
            case R.id.idCDCisla:
                if (zaklad != 10) {
                    sb.append(prevod(vstup, zaklad)).append(" (").append(zaklad).append(")");
                    sb.append(" = ").append(vstup).append(" (10)");
                } else sb.append(vstup);
                break;
            case R.id.idCDRim:
                sb.append(text);
                if (vstup > 0) sb.append(" = ").append(vstup).append(" (10)");
                else if (text.length() == 0) sb.append("0");
                break;
            case R.id.idCDPerm1:
            case R.id.idCDPerm2:
                sb.append(text);
                break;
        }
        if (itp == R.id.idCDModulo1 && vstup > abc.count())
            sb.append(" ≡ ").append(((vstup - 1) % abc.count()) + 1).append(" (mod ").append(abc.count()).append(")");
        if (itp == R.id.idCDModulo0 && vstup >= abc.count())
            sb.append(" ≡ ").append(vstup % abc.count()).append(" (mod ").append(abc.count()).append(")");
        if (itp == R.id.idCDASCII && vstup >= 32 && vstup <= 127)
            sb.append(" = \"").append(znak()).append("\"");
        else
            sb.append(" = ").append(znak());
        tvVstup.setText(sb.toString());
    }

    private void setVstup(int x) {
        vstup = x;
        if (tvVstup != null)
            updateVstup();
    }

    private String prevod(int x, int zaklad) {
        if (x < 0)
            return null;
        else if(x == 0)
            return "0";
        else {
            StringBuilder sb = new StringBuilder();
            while (x != 0) {
                sb.insert(0, String.format("%X", x % zaklad));
                x /= zaklad;
            }
            return sb.toString();
        }
    }


    private final OnClickListener enterListener = new OnClickListener() {
        public void onClick(View v) {
            if (!(vstup == 0 && !nula))
                reseni += znak();
            tvRes.setText(reseni);
            text = "";
            setVstup(0);
        }
    };

    private final OnClickListener bspListener = new OnClickListener() {
        public void onClick(View v) {
            if (vstup != 0) {
                switch (mod) {
                    case R.id.idCDCisla:
                        setVstup(vstup / zaklad);
                        break;
                    case R.id.idCDRim:
                        text = text.substring(0, text.length() - 1);
                        setVstup(CislaConv.parseRoman(text));
                        break;
                    case R.id.idCDPerm1:
                    case R.id.idCDPerm2:
                        text = text.substring(0, text.length() - 1);
                        setVstup(CislaConv.parsePerm(text, mod));
                        break;
                }
                return;
            }
            if (reseni.length() == 0) return;
            reseni = reseni.substring(0, reseni.length() - 1);
            tvRes.setText(reseni);
        }
    };

    @Override
    protected String onCopy() {
        return reseni;
    }

    @Override
    protected void onClear() {
        reseni = "";
        text = "";
        tvRes.setText("");
        setVstup(0);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(App.RESENI, reseni);
        state.putInt(App.VSTUP1, vstup);
        state.putString(App.VSTUP2, text);
        state.putInt(App.VAR, selItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (abc == abcPref)
            abc = abcPref = Alphabet.getPreferentialInstance();
        else
            abcPref = Alphabet.getPreferentialInstance();

        ArrayAdapter<InterpItem> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, interpList(abc.count()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterp.setAdapter(adapter);

        tvRes.setText(reseni);
        updateVstup();
    }

    private InterpItem[] interpList(int count) {
        String[] elms = getResources().getStringArray(R.array.pataCDInterp);
        InterpItem res[] = new InterpItem[elms.length];
        for (int i = 0; i < elms.length; i++)
            if (elms[i].indexOf('%') >= 0)
                res[i] = new InterpItem(interp[i], String.format(elms[i], interp[i] == R.id.idCDPrimo0 ? count - 1 : count));
            else
                res[i] = new InterpItem(interp[i], elms[i]);
        return res;
    }


    private static final class InterpItem {
        final int interp;
        final String txt;

        InterpItem(int interp, String txt) {
            this.interp = interp;
            this.txt = txt;
        }

        @Override
        public String toString() {
            return txt;
        }
    }

}
