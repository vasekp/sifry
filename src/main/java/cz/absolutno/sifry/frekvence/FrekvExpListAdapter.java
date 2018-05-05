package cz.absolutno.sifry.frekvence;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public final class FrekvExpListAdapter extends BaseExpandableListAdapter {

    private Alphabet abc;
    private int abcCount;

    private int[] pism, prvni, posledni, cisl, ip;
    private int pismen, cislic, mezer, itp, znaku, slov, vet, souveti, nerozp, maxNum;
    private int ruzPismen, ruzCislic;
    private final SparseIntArray delky = new SparseIntArray();
    private final SparseIntArray nezn = new SparseIntArray();
    private final SparseIntArray vse = new SparseIntArray();

    private final String[] groups;
    private final String[] stat;
    private final int[] groupIDs;
    private final int[] statIDs;
    private final int[] statPluralIDs;

    private final ArrayList<Integer> groupsFiltered = new ArrayList<>();
    private final ArrayList<Integer> statFiltered = new ArrayList<>();

    private final String formatString;
    private final String mezera;
    private final String inter;

    private SortItem[] sortedPismena, sortedCislice, sortedAlnum, sortedVse,
            sortedDelky, sortedPrvni, sortedPosledni, sortedNezname;

    private final int priColor, secColor;
    private static final int maxDS = 30;

    public FrekvExpListAdapter() {
        Context ctx = App.getContext();
        Resources res = ctx.getResources();
        groups = res.getStringArray(R.array.saFDGroups);
        stat = res.getStringArray(R.array.saFDStat);
        groupIDs = Utils.getIdArray(R.array.iaFDGroups);
        statIDs = Utils.getIdArray(R.array.iaFDStat);
        statPluralIDs = Utils.getIdArray(R.array.iaFDStatRuzne);
        priColor = ContextCompat.getColor(ctx, R.color.priColor);
        secColor = ContextCompat.getColor(ctx, R.color.secColor);
        formatString = res.getString(R.string.patFDRes);
        mezera = res.getString(R.string.tFDMezera);
        inter = res.getString(R.string.tFDInter);

        loadAbc();
    }

    public void reloadPref() {
        loadAbc();
        notifyDataSetChanged();
    }

    private void loadAbc() {
        abc = Alphabet.getPreferentialFullInstance();
        abcCount = abc.count();

        pism = new int[abcCount];
        prvni = new int[abcCount];
        posledni = new int[abcCount];
        cisl = new int[10];
        ip = new int[inter.length()];
    }

    private void clear() {
        maxNum = 0;
        pismen = cislic = mezer = itp = znaku = slov = vet = souveti = nerozp = 0;
        ruzPismen = ruzCislic;
        groupsFiltered.clear();
        for (int i = 0; i < abcCount; i++)
            pism[i] = 0;
        for (int i = 0; i < abcCount; i++)
            prvni[i] = 0;
        for (int i = 0; i < abcCount; i++)
            posledni[i] = 0;
        for (int i = 0; i < 10; i++)
            cisl[i] = 0;
        for (int i = 0; i < inter.length(); i++)
            ip[i] = 0;
        delky.clear();
        nezn.clear();
        vse.clear();
        notifyDataSetChanged();
    }

    @SuppressWarnings("ConstantConditions") // "too complex to analyze by data flow algorithm" :-)
    public void go(String vstup) {
        int ad = 0;
        boolean av = false;
        boolean as = false;
        clear();
        StringParser sp = abc.getStringParser(vstup);
        int ord, lastOrd = -1;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            znaku++;
            if (ord >= 0) {
                ad++;
                if (ad == 1)
                    prvni[ord]++;
                av = true;
                as = true;
                pism[ord]++;
                pismen++;
                lastOrd = ord;
                ord -= abcCount;
                vse.put(ord, vse.get(ord) + 1);
            } else {
                char c = sp.getLastChar();
                boolean rozp = false;
                if (c >= '0' && c <= '9') {
                    cisl[c - '0']++;
                    cislic++;
                    rozp = true;
                }
                if (c == ' ' || c == '\n') {
                    mezer++;
                    if (c == '\n' && as) {
                        vet++;
                        souveti++;
                        av = false;
                        as = false;
                    }
                    rozp = true;
                }
                int j;
                if ((j = inter.indexOf(c)) >= 0) {
                    if (av) {
                        vet++;
                        av = false;
                    }
                    ip[j]++;
                    itp++;
                    rozp = true;
                }
                if (c == '.' || c == '!' || c == '?' || c == '(')
                    if (as) {
                        souveti++;
                        as = false;
                    }
                if (!rozp) {
                    nezn.put(c, nezn.get(c) + 1);
                    nerozp++;
                } else if (ad != 0) {
                    if(ad >= maxDS)
                        ad = maxDS;
                    delky.put(ad, delky.get(ad) + 1);
                    if (ad > maxNum)
                        maxNum = ad;
                    posledni[lastOrd]++;
                    slov++;
                    ad = 0;
                }
                vse.put(c, vse.get(c) + 1);
            }
        }
        if (ad != 0) {
            if(ad >= maxDS)
                ad = maxDS;
            delky.put(ad, delky.get(ad) + 1);
            if (ad > maxNum)
                maxNum = ad;
            posledni[lastOrd]++;
            slov++;
            //ad = 0;
        }
        if (av) {
            vet++;
            //av = false;
        }
        if (as) {
            souveti++;
            //as = false;
        }

        ArrayList<SortItem> sort;

        sort = new ArrayList<>(abcCount);
        for (int i = 0; i < abcCount; i++) {
            sort.add(new SortItem(abc.chr(i), i, pism[i]));
            if (pism[i] != 0)
                ruzPismen++;
        }
        sortedPismena = sortAndCollect(sort);

        sort = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            sort.add(new SortItem(String.valueOf(i), i, cisl[i]));
            if (cisl[i] != 0)
                ruzCislic++;
        }
        sortedCislice = sortAndCollect(sort);

        sort = new ArrayList<>(10 + abcCount);
        for (int i = 0; i < abcCount; i++)
            if (pism[i] > 0)
                sort.add(new SortItem(abc.chr(i), i, pism[i]));
        for (int i = 0; i < 10; i++)
            if (cisl[i] > 0)
                sort.add(new SortItem(String.valueOf(i), i, cisl[i]));
        sortedAlnum = sortAndCollect(sort);

        sort = new ArrayList<>(nezn.size());
        for (int i = 0; i < nezn.size(); i++)
            sort.add(new SortItem(String.valueOf((char) nezn.keyAt(i)), nezn
                    .keyAt(i), nezn.valueAt(i)));
        sortedNezname = sortAndCollect(sort);

        sort = new ArrayList<>(vse.size());
        for (int i = 0; i < vse.size(); i++) {
            int x = vse.keyAt(i);
            if (x < 0)
                sort.add(new SortItem(abc.chr(x + abc.count()), x, vse
                        .valueAt(i)));
            else
                sort.add(new SortItem(Utils.getCharDesc((char) x, mezera), x,
                        vse.valueAt(i)));
        }
        sortedVse = sortAndCollect(sort);

        sort = new ArrayList<>(abc.count());
        for (int i = 0; i < abc.count(); i++)
            if (prvni[i] > 0)
                sort.add(new SortItem(abc.chr(i), i, prvni[i]));
        sortedPrvni = sortAndCollect(sort);

        sort = new ArrayList<>(abc.count());
        for (int i = 0; i < abc.count(); i++)
            if (posledni[i] > 0)
                sort.add(new SortItem(abc.chr(i), i, posledni[i]));
        sortedPosledni = sortAndCollect(sort);

        sort = new ArrayList<>(maxNum);
        for (int i = 0; i < delky.size(); i++)
            sort.add(new SortItem(
                    delky.keyAt(i) < maxDS
                            ? String.valueOf(delky.keyAt(i))
                            : String.valueOf(maxDS) + "+",
                    i, delky.valueAt(i)));
        sortedDelky = sortAndCollect(sort);

        if (pismen > 0)
            groupsFiltered.add(R.id.idFDGPismenaC);
        if (cislic > 0)
            groupsFiltered.add(R.id.idFDGCisliceC);
        if (pismen > 0 && cislic > 0)
            groupsFiltered.add(R.id.idFDGAlnumC);
        if (znaku != pismen && znaku != cislic && znaku != pismen + cislic)
            groupsFiltered.add(R.id.idFDGVseC);
        if (pismen > 0)
            groupsFiltered.add(R.id.idFDGPismenaP);
        if (cislic > 0)
            groupsFiltered.add(R.id.idFDGCisliceP);
        if (itp > 0)
            groupsFiltered.add(R.id.idFDGInterp);
        if (nerozp > 0)
            groupsFiltered.add(R.id.idFDGNezname);

        statFiltered.clear();
        statFiltered.add(R.id.idFDSPismen);
        statFiltered.add(R.id.idFDSCislic);
        statFiltered.add(R.id.idFDSMezer);
        if (itp > 0)
            statFiltered.add(R.id.idFDSInterp);
        if (nerozp > 0)
            statFiltered.add(R.id.idFDSNeznamych);
        statFiltered.add(R.id.idFDSVsech);

        if (slov > 1) {
            groupsFiltered.add(R.id.idFDGDelkyC);
            groupsFiltered.add(R.id.idFDGDelkyP);
            groupsFiltered.add(R.id.idFDGPrvni);
            groupsFiltered.add(R.id.idFDGPosledni);
            statFiltered.add(R.id.idFDSSlov);
            /*
			 * statFiltered.add(R.id.idFDSVet);
			 * statFiltered.add(R.id.idFDSSouveti);
			 */
        }
        if (znaku > 0)
            groupsFiltered.add(R.id.idFDGText);

        notifyDataSetChanged();
    }

    private SortItem[] sortAndCollect(ArrayList<SortItem> sort) {
        Collections.sort(sort, new SortComparator());
        StringBuilder sb = new StringBuilder();
        ArrayList<SortItem> result = new ArrayList<>();
        int last = -1;
        for(SortItem si : sort) {
            if(si.pocet != last) {
                if(sb.length() > 0)
                    result.add(new SortItem(sb.toString(), 0, last));
                sb.setLength(0);
                sb.append(si.pismena);
                last = si.pocet;
            } else {
                sb.append(", ");
                sb.append(si.pismena);
            }
        }
        if(sb.length() > 0)
            result.add(new SortItem(sb.toString(), 0, last));
        return result.toArray(new SortItem[result.size()]);
    }

    private String format(int n, int t) {
        if (t != 0)
            return String.format(formatString, n, (float) n / t * 100);
        else
            return String.valueOf(n);
    }

    public int getGroupCount() {
        return groupsFiltered.size();
    }

    public long getGroupId(int groupPosition) {
        return groupsFiltered.get(groupPosition);
    }

    public String getGroup(int groupPosition) {
        final int id = (int) getGroupId(groupPosition);
        for (int i = 0; i < groups.length; i++) {
            if (groupIDs[i] == id)
                return groups[i];
        }
        return null;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
        ((TextView) convertView).setText(getGroup(groupPosition));
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        switch (groupsFiltered.get(groupPosition)) {
            case R.id.idFDGPismenaC:
                if (sortedPismena != null)
                    return sortedPismena.length;
                else
                    return 0;
            case R.id.idFDGCisliceC:
                if (sortedPismena != null)
                    return sortedCislice.length;
                else
                    return 0;
            case R.id.idFDGAlnumC:
                if (sortedAlnum != null)
                    return sortedAlnum.length;
                else
                    return 0;
            case R.id.idFDGVseC:
                if (sortedVse != null)
                    return sortedVse.length;
                else
                    return 0;
            case R.id.idFDGPismenaP:
                return abc.count();
            case R.id.idFDGCisliceP:
                return 10;
            case R.id.idFDGInterp:
                return inter.length();
            case R.id.idFDGNezname:
                if (sortedNezname != null)
                    return sortedNezname.length;
                else
                    return 0;
            case R.id.idFDGDelkyC:
                if (sortedDelky != null)
                    return sortedDelky.length;
                else
                    return 0;
            case R.id.idFDGDelkyP:
                return maxNum;
            case R.id.idFDGPrvni:
                if (sortedPrvni != null)
                    return sortedPrvni.length;
                else
                    return 0;
            case R.id.idFDGPosledni:
                if (sortedPosledni != null)
                    return sortedPosledni.length;
                else
                    return 0;
            case R.id.idFDGText:
                return statFiltered.size();
            default:
                return 0;
        }
    }

    public long getChildId(int groupPosition, int childPosition) {
        if (groupsFiltered.get(groupPosition) == R.id.idFDGText)
            return statFiltered.get(childPosition);
        else
            return childPosition;
    }

    private int statIndexOf(int id) {
        int i;
        for (i = 0; i < statIDs.length; i++)
            if (statIDs[i] == id)
                return i;
        return -1;
    }

    public Ret getChild(int groupPosition, int childPosition) {
        switch (groupsFiltered.get(groupPosition)) {
            case R.id.idFDGPismenaC:
                return new Ret(sortedPismena[childPosition].pismena,
                        sortedPismena[childPosition].pocet == 0);
            case R.id.idFDGCisliceC:
                return new Ret(sortedCislice[childPosition].pismena,
                        sortedCislice[childPosition].pocet == 0);
            case R.id.idFDGAlnumC:
                return new Ret(sortedAlnum[childPosition].pismena,
                        sortedAlnum[childPosition].pocet == 0);
            case R.id.idFDGVseC:
                return new Ret(sortedVse[childPosition].pismena,
                        sortedVse[childPosition].pocet == 0);
            case R.id.idFDGPismenaP:
                return new Ret(pism[childPosition], pismen);
            case R.id.idFDGCisliceP:
                return new Ret(cisl[childPosition], cislic);
            case R.id.idFDGInterp:
                return new Ret(ip[childPosition], itp);
            case R.id.idFDGDelkyC:
                return new Ret(sortedDelky[childPosition].pismena,
                        sortedDelky[childPosition].pocet == 0);
            case R.id.idFDGDelkyP:
                return new Ret(delky.get(childPosition + 1), slov);
            case R.id.idFDGNezname:
                return new Ret(sortedNezname[childPosition].pismena,
                        sortedNezname[childPosition].pocet == 0);
            case R.id.idFDGPrvni:
                return new Ret(sortedPrvni[childPosition].pismena,
                        sortedPrvni[childPosition].pocet == 0);
            case R.id.idFDGPosledni:
                return new Ret(sortedPosledni[childPosition].pismena,
                        sortedPosledni[childPosition].pocet == 0);
            case R.id.idFDGText:
                switch (statFiltered.get(childPosition)) {
                    case R.id.idFDSPismen:
                        return new RetStat(pismen, ruzPismen, R.id.idFDSPismen);
                    case R.id.idFDSCislic:
                        return new RetStat(cislic, ruzCislic, R.id.idFDSCislic);
                    case R.id.idFDSMezer:
                        return new RetStat(mezer);
                    case R.id.idFDSInterp:
                        return new RetStat(itp);
                    case R.id.idFDSNeznamych:
                        return new RetStat(nerozp, nezn.size(), R.id.idFDSNeznamych);
                    case R.id.idFDSVsech:
                        return new RetStat(znaku, vse.size(), R.id.idFDSVsech);
                    case R.id.idFDSSlov:
                        if (delky.size() == 1)
                            return new RetStat(slov, 1, delky.keyAt(0), R.id.idFDSSlov);
                        else
                            return new RetStat(slov, delky.size(), R.id.idFDSSlov);
                    case R.id.idFDSVet:
                        return new RetStat(vet);
                    case R.id.idFDSSouveti:
                        return new RetStat(souveti);
                }
        }
        return null;
    }

    private String getChildDesc(int groupPosition, int childPosition) {
        switch (groupsFiltered.get(groupPosition)) {
            case R.id.idFDGPismenaC:
                return format(sortedPismena[childPosition].pocet, pismen);
            case R.id.idFDGCisliceC:
                return format(sortedCislice[childPosition].pocet, cislic);
            case R.id.idFDGAlnumC:
                return format(sortedAlnum[childPosition].pocet, pismen + cislic);
            case R.id.idFDGVseC:
                return format(sortedVse[childPosition].pocet, znaku);
            case R.id.idFDGPismenaP:
                return abc.chr(childPosition);
            case R.id.idFDGCisliceP:
                return String.valueOf(childPosition);
            case R.id.idFDGInterp:
                return inter.substring(childPosition, childPosition + 1);
            case R.id.idFDGNezname:
                return String.valueOf(sortedNezname[childPosition].pocet);
            case R.id.idFDGDelkyC:
                return format(sortedDelky[childPosition].pocet, slov);
            case R.id.idFDGDelkyP:
                return childPosition + 1 < maxDS
                    ? String.valueOf(childPosition + 1)
                    : String.valueOf(maxDS) + "+";
            case R.id.idFDGPrvni:
                return format(sortedPrvni[childPosition].pocet, slov);
            case R.id.idFDGPosledni:
                return format(sortedPosledni[childPosition].pocet, slov);
            case R.id.idFDGText:
                final int id = (int) getChildId(groupPosition, childPosition);
                return stat[statIndexOf(id)];
            default:
                return null;
        }
    }

    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = App.getInflater().inflate(R.layout.gen_list_item, parent, false);
        TextView tvDesc = convertView.findViewById(R.id.desc);
        TextView tvCont = convertView.findViewById(R.id.cont);
        tvDesc.setText(getChildDesc(groupPosition, childPosition));
        Ret r = getChild(groupPosition, childPosition);
        assert r != null;
        tvCont.setText(r.s);
        if (r.zero)
            tvCont.setTextColor(secColor);
        else
            tvCont.setTextColor(priColor);
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public boolean hasStableIds() {
        return true;
    }


    private static final class SortItem {
        final int pocet;
        final int ord;
        final String pismena;

        SortItem(String pismena, int ord, int pocet) {
            this.pocet = pocet;
            this.ord = ord;
            this.pismena = pismena;
        }
    }

    private static final class SortComparator implements Comparator<SortItem> {
        public int compare(SortItem lhs, SortItem rhs) {
            if (lhs.pocet != rhs.pocet)
                return rhs.pocet - lhs.pocet; /* Od největšího k nejmenšímu */
            char l = lhs.pismena.charAt(0), r = rhs.pismena.charAt(0);
            if (Character.isDigit(l) != Character.isDigit(r))
                return Character.isDigit(r) ? -1 : 1; /* Písmenka před číslice */
            else
                return lhs.ord - rhs.ord;
        }
    }


    private class Ret {
        String s;
        final boolean zero;

        Ret(String s, boolean z) {
            this.s = s;
            this.zero = z;
        }

        Ret(int n) {
            s = String.valueOf(n);
            zero = (n == 0);
        }

        Ret(int n, int t) {
            s = FrekvExpListAdapter.this.format(n, t);
            zero = (n == 0);
        }
    }

    private class RetStat extends Ret {
        RetStat(int n) {
            super(n);
        }

        RetStat(int n, int diff, int id) {
            super(n);
            if (diff >= 2 && diff < n)
                s = App.getContext().getResources().getQuantityString(
                        statPluralIDs[statIndexOf(id)], diff, n, diff);
        }

        @SuppressWarnings("SameParameterValue")
        RetStat(int n, int diff, int spol, int id) {
            super(n);
            if (diff < n)
                s = App.getContext().getResources().getQuantityString(
                        statPluralIDs[statIndexOf(id)], diff, n, diff, spol);
        }
    }

}
