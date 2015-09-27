package cz.absolutno.sifry.zapisnik;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class Stanoviste implements Parcelable, Serializable {

    private static final long serialVersionUID = -8774189962438235929L;
    private static final DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
    private static final DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    public String nazev;
    public String heslo;
    public Date prichod;
    public Date odchod;
    public String reseni;
    public String upres;
    public String pozn;

    public Stanoviste(String nazev) {
        this.nazev = nazev;
    }

    public Stanoviste(String nazev, Date prichod, Date odchod,
                      String reseni, String upres, String heslo, String pozn) {
        super();
        this.nazev = nazev;
        this.heslo = heslo;
        this.prichod = prichod;
        this.odchod = odchod;
        this.reseni = reseni;
        this.upres = upres;
        this.pozn = pozn;
    }

    public String fmtCas() {
        if ((prichod == null) && (odchod == null))
            return "";
        StringBuilder sb = new StringBuilder();
        Calendar c = Calendar.getInstance();
        int day1, day2;
        day1 = c.get(Calendar.DAY_OF_YEAR);
        if (prichod != null) {
            c.setTime(prichod);
            day2 = c.get(Calendar.DAY_OF_YEAR);
            sb.append((day2 != day1 ? dateTimeFormatter : timeFormatter).format(prichod));
            day1 = day2;
        } else
            sb.append("?");
        sb.append(" – ");
        if (odchod != null) {
            c.setTime(odchod);
            day2 = c.get(Calendar.DAY_OF_YEAR);
            sb.append((day2 != day1 ? dateTimeFormatter : timeFormatter).format(odchod));
        } else
            sb.append("?");
        return sb.toString();
    }

    public String[] toStringArray(int[] fields) {
        final int sz = fields.length;
        String[] out = new String[sz];
        for (int i = 0; i < sz; i++) {
            switch (fields[i]) {
                case R.id.cbZENazev:
                    out[i] = nazev;
                    break;
                case R.id.cbZEHeslo:
                    out[i] = heslo;
                    break;
                case R.id.cbZEPrichod:
                    out[i] = (prichod != null ? dateTimeFormatter.format(prichod) : "");
                    break;
                case R.id.cbZEOdchod:
                    out[i] = (odchod != null ? dateTimeFormatter.format(odchod) : "");
                    break;
                case R.id.cbZERes:
                    out[i] = reseni;
                    break;
                case R.id.cbZEPozn:
                    out[i] = pozn;
                    break;
                case R.id.cbZEUpres:
                    out[i] = upres;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        return out;
    }

    public static String[] getHeaders(int[] fields) {
        final int sz = fields.length;
        Resources res = App.getContext().getResources();
        String[] out = new String[sz];
        for (int i = 0; i < sz; i++) {
            switch (fields[i]) {
                case R.id.cbZENazev:
                    out[i] = res.getString(R.string.tZDNazev);
                    break;
                case R.id.cbZEHeslo:
                    out[i] = res.getString(R.string.tZDHeslo);
                    break;
                case R.id.cbZEPrichod:
                    out[i] = res.getString(R.string.tZDPrichod);
                    break;
                case R.id.cbZEOdchod:
                    out[i] = res.getString(R.string.tZDOdchod);
                    break;
                case R.id.cbZERes:
                    out[i] = res.getString(R.string.tZDRes);
                    break;
                case R.id.cbZEPozn:
                    out[i] = res.getString(R.string.tZDPozn);
                    break;
                case R.id.cbZEUpres:
                    out[i] = res.getString(R.string.tZDUpres);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        for (int i = 0; i < sz; i++)
            out[i] = out[i].substring(0, out[i].lastIndexOf(':'));
        return out;
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nazev);
        dest.writeString(heslo);
        dest.writeSerializable(prichod);
        dest.writeSerializable(odchod);
        dest.writeString(reseni);
        dest.writeString(upres);
        dest.writeString(pozn);
    }

    public static final Parcelable.Creator<Stanoviste> CREATOR = new Parcelable.Creator<Stanoviste>() {
        public Stanoviste createFromParcel(Parcel in) {
            String nazev = in.readString();
            String heslo = in.readString();
            Date prichod = (Date) in.readSerializable();
            Date odchod = (Date) in.readSerializable();
            String reseni = in.readString();
            String upres = in.readString();
            String pozn = in.readString();

            return new Stanoviste(nazev, prichod, odchod, reseni, upres,
                    heslo, pozn);
        }

        public Stanoviste[] newArray(int size) {
            return new Stanoviste[size];
        }
    };

}