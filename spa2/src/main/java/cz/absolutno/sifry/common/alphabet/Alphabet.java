package cz.absolutno.sifry.common.alphabet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

import cz.absolutno.sifry.App;

public abstract class Alphabet {

    public abstract int count();

    public abstract String chr(int i);

    public abstract int ord(String c);

    public abstract StringParser getStringParser(String s);

    public static Alphabet getPreferentialInstance() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        if (sp == null)
            return getVariantInstance(0, "");
        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("cs")) {
            return new CzechAlphabet(sp);
        } else {
            return new EnglishAlphabet(sp);
        }
    }

    public static Alphabet getPreferentialFullInstance() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return getVariantInstance(0, (sp != null ? sp.getString("pref_parse_abc", "") : ""));
    }

    public static Alphabet getVariantInstance(int maxCount, String variant) {
        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("cs")) {
            return CzechAlphabet.getVariantInstance(maxCount, variant);
        } else {
            return EnglishAlphabet.getVariantInstance(maxCount, variant);
        }
    }

    public final String filter(String in) {
        StringParser sp = getStringParser(in);
        StringBuilder sb = new StringBuilder();
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF)
            sb.append(chr(ord));
        return sb.toString();
    }

}