package cz.absolutno.sifry.zapisnik;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractDFragment;

public final class ZapisnikDFragment extends AbstractDFragment {

    private ListView lvMain;

    private static boolean cislovat;
    private static String patNazev;
    private static ArrayList<Stanoviste> stan;

    @Override
    protected int getMenuCaps() {
        return HAS_CLEAR;
    }

    @SuppressWarnings("unchecked")
    // reading an ArrayList serialized in an input stream in onDestroy() below
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        lvMain = (ListView) inflater.inflate(R.layout.gen_list_layout, container, false);

        try {
            FileInputStream fis = App.getContext().openFileInput(ZapisnikActivity.FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            stan = (ArrayList<Stanoviste>) ois.readObject();
        } catch (Exception e) {
            //e.printStackTrace();
            stan = new ArrayList<>();
        }

        patNazev = getString(R.string.patZDDefault);

        if (savedInstanceState != null)
            stan = savedInstanceState.getParcelableArrayList(App.DATA);

        lvMain.setOnItemClickListener(itemClickListener);
        lvMain.setAdapter(new ZapisnikLA());
        registerForContextMenu(lvMain);
        return lvMain;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            FileOutputStream fos = App.getContext().openFileOutput(ZapisnikActivity.FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(stan);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private final OnItemClickListener itemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
            if (id == -1)
                prichod();
            else
                zmena(stan.get(position), false);
        }
    };

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (info.id == -1)
            return;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.zapisnik_ctx, menu);
        Stanoviste s = stan.get(info.position);
        if (s.odchod != null)
            menu.findItem(R.id.mZCtxOdchod).setVisible(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()) {
            case R.id.mZCtxOdchod:
                zmena(stan.get(position), false);
                return true;
            case R.id.mZCtxZmena:
                zmena(stan.get(position), true);
                return true;
            case R.id.mZCtxSmaz:
                stan.remove(position);
                lvMain.invalidateViews();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void prichod() {
        ZapisnikSFragment dialog = new ZapisnikSFragment();
        Bundle args = new Bundle();
        args.putSerializable(App.DATA, new Stanoviste(dalsiStan()));
        args.putSerializable(App.SPEC, ZapisnikSFragment.Mod.PRICHOD);
        dialog.setArguments(args);
        dialog.setOnPositiveButtonListener(prichodListener);
        dialog.show(getFragmentManager(), "prichod");
    }

    private final ZapisnikSFragment.OnPositiveButtonListener prichodListener = new ZapisnikSFragment.OnPositiveButtonListener() {
        public void onPositiveButton(Stanoviste s) {
            stan.add(s);
            lvMain.invalidateViews();
        }
    };

    private void zmena(Stanoviste s, boolean force) {
        ZapisnikSFragment dialog = new ZapisnikSFragment();
        Bundle args = new Bundle();
        args.putSerializable(App.DATA, s);
        args.putSerializable(App.SPEC, (s.odchod != null || force) ? ZapisnikSFragment.Mod.ZMENA : ZapisnikSFragment.Mod.ODCHOD);
        dialog.setArguments(args);
        dialog.setOnPositiveButtonListener(zmenaListener);
        dialog.show(getFragmentManager(), "zmena");
    }

    private final ZapisnikSFragment.OnPositiveButtonListener zmenaListener = new ZapisnikSFragment.OnPositiveButtonListener() {
        public void onPositiveButton(Stanoviste s) {
            lvMain.invalidateViews();
        }
    };

    @Override
    protected void onClear() {
        SmazVseFragment dialog = new SmazVseFragment();
        dialog.setOnPositiveButtonListener(clearListener);
        dialog.show(getFragmentManager(), "clear");
    }

    private final SmazVseFragment.OnPositiveButtonListener clearListener = new SmazVseFragment.OnPositiveButtonListener() {
        public void onPositiveButton() {
            stan.clear();
            lvMain.invalidateViews();
        }
    };

    private static String dalsiStan() {
        if (!cislovat)
            return "";
        int ss = patNazev.indexOf("%d");
        if (ss < 0)
            return patNazev;
        Pattern p = Pattern.compile(patNazev.replace("%d", "(\\d+)"));
        int max = 0;
        for (Stanoviste s : stan) {
            Matcher m = p.matcher(s.nazev);
            if (m.matches())
                max = Math.max(max, Integer.valueOf(m.group(1)));
        }
        return String.format(patNazev, max + 1);
    }

    @Override
    public boolean saveData(Bundle data) {
        if (stan.size() == 0)
            return false;
        data.putParcelableArrayList(App.DATA, stan);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList(App.DATA, stan);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cislovat = (sp == null || sp.getBoolean("pref_zap_auto", true));
        lvMain.invalidateViews();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        FragmentManager fm = getFragmentManager();
        Fragment fragment;
        fragment = fm.findFragmentByTag("prichod");
        if (fragment != null)
            ((ZapisnikSFragment) fragment).setOnPositiveButtonListener(prichodListener);
        fragment = fm.findFragmentByTag("zmena");
        if (fragment != null)
            ((ZapisnikSFragment) fragment).setOnPositiveButtonListener(zmenaListener);
        fragment = fm.findFragmentByTag("clear");
        if (fragment != null)
            ((SmazVseFragment) fragment).setOnPositiveButtonListener(clearListener);
    }


    private static final class ZapisnikLA extends BaseAdapter {

        private static final int TYPE_STAN = 0;
        private static final int TYPE_NEW = 1;

        public int getCount() {
            return stan.size() + 1;
        }

        public Stanoviste getItem(int position) {
            if (position == stan.size())
                return null;
            return stan.get(position);
        }

        public long getItemId(int position) {
            if (position == stan.size())
                return -1;
            else
                return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < stan.size())
                return TYPE_STAN;
            else
                return TYPE_NEW;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (convertView == null || type != TYPE_STAN || convertView.getId() != R.id.zdItem)
                convertView = App.getInflater().inflate(
                        type == TYPE_STAN ? R.layout.zapisnik_item : R.layout.zapisnik_item_new, parent, false);
            if (type == TYPE_STAN) {
                Stanoviste s = getItem(position);
                assert s != null;
                ((TextView) convertView.findViewById(R.id.tvZStan)).setText(s.nazev);
                ((TextView) convertView.findViewById(R.id.tvZCas)).setText(s.fmtCas());
                ((TextView) convertView.findViewById(R.id.tvZRes)).setText(s.reseni);
                convertView.findViewById(R.id.tvZPozn).setVisibility(
                        (s.pozn.length() == 0 && s.upres.length() == 0) ? View.GONE : View.VISIBLE);
            }
            return convertView;
        }

    }

}
