package cz.absolutno.sifry.common.decoder;

import android.annotation.SuppressLint;

import java.util.ArrayList;


public abstract class Decoder {

    protected Decoder() {
    }

    protected abstract String decode(int x);

    String getDesc(int x) {
        return decode(x);
    }

    public String decode(ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer x : list)
            sb.append(decode(x));
        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    public final boolean encode(String s, ArrayList<Integer> list) {
        list.clear();
        return encodeInternal(s.toUpperCase(), list);
    }

    protected abstract boolean encodeInternal(String s, ArrayList<Integer> list);

}