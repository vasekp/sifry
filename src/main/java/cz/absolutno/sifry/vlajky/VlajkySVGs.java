package cz.absolutno.sifry.vlajky;

import android.content.res.AssetManager;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;
import com.larvalabs.svgandroid.SVGParseException;

import java.io.IOException;
import java.lang.ref.SoftReference;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public class VlajkySVGs {

    private static SoftReference<VlajkySVGs> soft = null;

    public static VlajkySVGs getInstance() {
        VlajkySVGs ref;
        if (soft == null || soft.get() == null) {
            ref = new VlajkySVGs();
            soft = new SoftReference<>(ref);
        } else {
            ref = soft.get();
        }
        return ref;
    }


    private static SVG[] pismena, cislaICS, cislaNATO;

    private VlajkySVGs() {
        AssetManager assets = App.getContext().getAssets();

        pismena = new SVG[26];
        for (char c = 'a'; c <= 'z'; c++) {
            try {
                pismena[c - 'a'] = new SVGBuilder().readFromAsset(assets, "vlajky/pismena/" + c + ".svg").build();
            } catch (SVGParseException | IOException e) {
                e.printStackTrace();
            }
        }

        cislaICS = new SVG[10];
        cislaNATO = new SVG[10];
        for (char c = '0'; c <= '9'; c++) {
            try {
                cislaICS[c - '0'] = new SVGBuilder().readFromAsset(assets, "vlajky/cisla1/" + c + ".svg").build();
                cislaNATO[c - '0'] = new SVGBuilder().readFromAsset(assets, "vlajky/cisla2/" + c + ".svg").build();
            } catch (SVGParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SVG getChar(char c) {
        if (c >= 'A' && c <= 'Z')
            return pismena[c - 'A'];
        else if (c >= '0' && c <= '9')
            return cislaICS[c - '0'];
        else
            return null;
    }

    public SVG get(int set, int ix) {
        switch (set) {
            case R.id.idVRPismena:
                return pismena[ix];
            case R.id.idVRCislaICS:
                return cislaICS[ix];
            case R.id.idVRCislaNATO:
                return cislaNATO[ix];
            default:
                throw new IllegalArgumentException();
        }
    }

}
