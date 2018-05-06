package cz.absolutno.sifry.zapisnik;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Stanoviste implements Parcelable, Serializable {

    @SuppressWarnings("unused")
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

    Stanoviste(String nazev) {
        this.nazev = nazev;
    }

    private Stanoviste(String nazev, Date prichod, Date odchod,
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
