package cz.absolutno.sifry.tabulky;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractCFragment;
import cz.absolutno.sifry.tabulky.ctverec.CtverecCAdapter;
import cz.absolutno.sifry.tabulky.malypolsky.MalyPolskyCAdapter;
import cz.absolutno.sifry.tabulky.mobil.MobilCAdapter;
import cz.absolutno.sifry.tabulky.polsky.PolskyCAdapter;

public final class TabulkyCFragment extends AbstractCFragment {

    private TabulkyCListAdapter adapter;

    private Spinner spVar, spVPVar, spMPVar, spCtvVar;

    private int[] tabulkyID, vpVarID, mpVarID;
    private String[] vpVarABC, mpVarABC, ctvVarABC;
    private int selItem = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tabulkyc_layout, container, false);

        tabulkyID = Utils.getIdArray(R.array.iaTDTabulky);
        vpVarID = Utils.getIdArray(R.array.iaTDVPVar);
        mpVarID = Utils.getIdArray(R.array.iaTDMPVar);
        vpVarABC = getResources().getStringArray(R.array.saTDVPABCVar);
        mpVarABC = getResources().getStringArray(R.array.saTDMPABCVar);
        ctvVarABC = getResources().getStringArray(R.array.saTDCtvABCVar);

        spVar = v.findViewById(R.id.spTDTab);
        spVPVar = v.findViewById(R.id.spTDVPVar);
        spMPVar = v.findViewById(R.id.spTDMPVar);
        spCtvVar = v.findViewById(R.id.spTDCtvAbeceda);

        int var1 = getArguments().getInt(App.VSTUP1, 0);
        int var2 = getArguments().getInt(App.VSTUP2, 0);
        spVar.setSelection(find(tabulkyID, var1));
        switch (var1) {
            case R.id.idTDVPolsky:
                spVPVar.setSelection(var2);
                break;
            case R.id.idTDMPolsky:
                spMPVar.setSelection(var2);
                break;
            case R.id.idTDCtverec:
                spCtvVar.setSelection(var2);
                break;
        }

        adapter = new PolskyCAdapter();
        ((ListView) v.findViewById(R.id.lvCVystup)).setAdapter(adapter);
        ((ListView) v.findViewById(R.id.lvCVystup)).setOnItemClickListener(genItemClickListener);

        return v;
    }

    private int find(int[] arr, int item) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == item)
                return i;
        return -1;
    }

    @Override
    public boolean encode(String input) {
        return adapter.load(input);
    }

    @Override
    public void packData(Bundle data) {
        if (selItem == R.id.idTDMobil)
            data.putString(App.VSTUP, ((MobilCAdapter) adapter).getInputProcessed());
        data.putInt(App.VSTUP1, selItem);
        Spinner s = null;
        switch (selItem) {
            case R.id.idTDVPolsky:
                s = spVPVar;
                break;
            case R.id.idTDMPolsky:
                s = spMPVar;
                break;
            case R.id.idTDCtverec:
                s = spCtvVar;
                break;
        }
        if (s != null)
            data.putInt(App.VSTUP2, s.getSelectedItemPosition());
        data.putIntegerArrayList(App.DATA, adapter.getData());
    }

    @Override
    public void onResume() {
        super.onResume();
        spVar.setOnItemSelectedListener(varListener);
        spVPVar.setOnItemSelectedListener(varListener);
        spMPVar.setOnItemSelectedListener(varListener);
        spCtvVar.setOnItemSelectedListener(varListener);
        updateAdapter();
    }

    private final OnItemSelectedListener varListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
            updateAdapter();
        }

        public void onNothingSelected(AdapterView<?> parentView) {
        }
    };

    @SuppressWarnings("ConstantConditions")
    private void updateAdapter() {
        int item = tabulkyID[spVar.getSelectedItemPosition()];
        if (item != selItem) {
            getView().findViewById(R.id.spTDVPVar).setVisibility(item == R.id.idTDVPolsky ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.spTDMPVar).setVisibility(item == R.id.idTDMPolsky ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.spTDCtvAbeceda).setVisibility(item == R.id.idTDCtverec ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.tvTDVar).setVisibility(item == R.id.idTDMobil ? View.GONE : View.VISIBLE);
            switch (item) {
                case R.id.idTDVPolsky:
                    adapter = new PolskyCAdapter();
                    break;
                case R.id.idTDMPolsky:
                    adapter = new MalyPolskyCAdapter();
                    break;
                case R.id.idTDCtverec:
                    adapter = new CtverecCAdapter();
                    break;
                case R.id.idTDMobil:
                    adapter = new MobilCAdapter();
                    break;
            }
        }
        int var;
        switch (item) {
            case R.id.idTDVPolsky:
                var = spVPVar.getSelectedItemPosition();
                adapter.setVar(vpVarID[var], vpVarABC[var]);
                break;
            case R.id.idTDMPolsky:
                var = spMPVar.getSelectedItemPosition();
                adapter.setVar(mpVarID[var], mpVarABC[var]);
                break;
            case R.id.idTDCtverec:
                var = spCtvVar.getSelectedItemPosition();
                adapter.setVar(0, ctvVarABC[var]);
                break;
        }
        selItem = item;
        ((ListView) getView().findViewById(R.id.lvCVystup)).setAdapter(adapter);
        zpracuj();
    }

}
