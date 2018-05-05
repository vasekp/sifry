package cz.absolutno.sifry.mainscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVGBuilder;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.braille.BrailleActivity;
import cz.absolutno.sifry.cisla.CislaActivity;
import cz.absolutno.sifry.common.activity.HelpActivity;
import cz.absolutno.sifry.common.activity.SettingsActivity;
import cz.absolutno.sifry.common.widget.FixedGridLayout;
import cz.absolutno.sifry.frekvence.FrekvActivity;
import cz.absolutno.sifry.kalendar.KalendarActivity;
import cz.absolutno.sifry.morse.MorseActivity;
import cz.absolutno.sifry.regexp.RegExpActivity;
import cz.absolutno.sifry.semafor.SemaforActivity;
import cz.absolutno.sifry.substituce.SubstActivity;
import cz.absolutno.sifry.tabulky.TabulkyActivity;
import cz.absolutno.sifry.transpozice.TransActivity;
import cz.absolutno.sifry.vlajky.VlajkyActivity;
import cz.absolutno.sifry.zapisnik.ZapisnikActivity;

public final class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.updateLocale();
        setContentView(R.layout.main_layout);

        Aktivita[] aktivity = new Aktivita[]{
                new Aktivita(MorseActivity.class, R.string.cptMorse, R.raw.ic_morse),
                new Aktivita(BrailleActivity.class, R.string.cptBraille, R.raw.ic_braille),
                new Aktivita(CislaActivity.class, R.string.cptCisla, R.raw.ic_cisla),
                new Aktivita(SemaforActivity.class, R.string.cptSemafor, R.raw.ic_semafor),
                new Aktivita(TabulkyActivity.class, R.string.cptPolskyTabulky, R.raw.ic_polsky),
                new Aktivita(VlajkyActivity.class, R.string.cptVlajky, R.raw.ic_vlajky),
                new Aktivita(SubstActivity.class, R.string.cptSubst, R.raw.ic_subst),
                new Aktivita(TransActivity.class, R.string.cptTrans, R.raw.ic_trans),
                new Aktivita(FrekvActivity.class, R.string.cptFrekv, R.raw.ic_frekv),
                new Aktivita(KalendarActivity.class, R.string.cptKalendar, R.raw.ic_kalendar),
                new Aktivita(ZapisnikActivity.class, R.string.cptZapisnik, R.raw.ic_zapisnik),
                new Aktivita(RegExpActivity.class, R.string.cptRegExp, R.raw.ic_regexp)
        };

        int tint = ContextCompat.getColor(this, R.color.tintColor);
        FixedGridLayout glMain = findViewById(R.id.glMain);
        LayoutInflater inflater = App.getInflater();
        for (Aktivita a : aktivity) {
            View v = inflater.inflate(R.layout.main_item, glMain, false);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(a.icon);
            ((AlphaBitmapCacheView) v.findViewById(R.id.icon)).setColor(tint);
            ((TextView) v.findViewById(R.id.text)).setText(a.caption);
            v.setTag(a);
            v.setOnClickListener(listener);
            glMain.addView(v);
        }

        try {
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            int thisVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            int lastVersion = sp.getInt("last_version", 0);
			if(lastVersion > 0 && lastVersion < 2100 && thisVersion >= 2100)
				new NewsFragment().show(getSupportFragmentManager(), "news");
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("last_version", thisVersion);
            editor.apply();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private final OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            Aktivita a = (Aktivita) (v.getTag());
            Intent intent = new Intent(MainActivity.this, a.cls);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mMainSettings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.mMainHelp:
                intent = new Intent(this, HelpActivity.class);
                intent.putExtra(App.SPEC, R.string.tHelpMain);
                startActivity(intent);
                return true;
            case R.id.mMainAbout:
                new AboutFragment().show(getSupportFragmentManager(), "about");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp == null) return;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("pref_changed", false);
        editor.apply();
    }


    private static final class Aktivita {
        final Class<?> cls;
        final String caption;
        final Drawable icon;

        Aktivita(Class<?> cls, int cpt, int icon) {
            this.cls = cls;
            this.caption = App.getContext().getString(cpt);
            this.icon = new SVGBuilder().readFromResource(App.getContext().getResources(), icon).build().getDrawable();
        }

        @Override
        public String toString() {
            return caption;
        }
    }

}