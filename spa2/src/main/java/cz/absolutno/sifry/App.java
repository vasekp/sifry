package cz.absolutno.sifry;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import android.os.Build;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;

import java.util.Locale;

import cz.absolutno.sifry.mainscreen.SplashActivity;

public final class App extends Application {

    public static final String XMLNS = "http://schemas.android.com/apk/res-auto";

    public static final String VSTUP = "cz.absolutno.sifry.VSTUP";
    public static final String VSTUP1 = "cz.absolutno.sifry.VSTUP1";
    public static final String VSTUP2 = "cz.absolutno.sifry.VSTUP2";
    public static final String DATA = "cz.absolutno.sifry.RAW_DATA";
    public static final String VAR = "cz.absolutno.sifry.VAR";
    public static final String SPEC = "cz.absolutno.sifry.SPEC";
    public static final String RESENI = "cz.absolutno.sifry.RESENI";

    private static App instance;
    private static float scale = 1.0f;

    public App() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.MainTheme);
        scale = getResources().getDisplayMetrics().density;
        updateLocale();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    public static Context getContext() {
        return instance;
    }

    public static LayoutInflater getInflater() {
        return (LayoutInflater) instance.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    public static float getScale() {
        return scale;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Context localizedContext(Context base) {
        String loc = PreferenceManager.getDefaultSharedPreferences(base).getString("pref_locale", "");
        Resources res = base.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = loc.equals("") ? Locale.getDefault() : new Locale(loc);
        conf.setLocale(locale);
        return base.createConfigurationContext(conf);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(localizedContext(base));
    }

    private static void updateLocale(String loc) {
        Resources res = instance.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = loc.equals("") ? Locale.getDefault() : new Locale(loc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            conf.setLocale(locale);
        else
            conf.locale = locale;
        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    public static void updateLocale() {
        updateLocale(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_locale", ""));
    }

    public static void restart() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        System.exit(0);
    }

}
