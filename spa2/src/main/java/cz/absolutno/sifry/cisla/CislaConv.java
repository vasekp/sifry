package cz.absolutno.sifry.cisla;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.regex.Pattern;

import cz.absolutno.sifry.R;

final class CislaConv {

    public static int parseRoman(String s) {
        if (s.length() == 0) return 0;
        if (Pattern.matches("M*(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})", s)) {
            int i = 0, v = 0;
            for (; i < s.length() && s.charAt(i) == 'M'; i++)
                v += 1000;
            if (i == s.length()) return v;
            if (s.charAt(i) == 'C' && i <= s.length() - 2 && (s.charAt(i + 1) == 'M' || s.charAt(i + 1) == 'D')) {
                v += (s.charAt(i + 1) == 'M' ? 900 : 400);
                i += 2;
            } else {
                if (s.charAt(i) == 'D') {
                    v += 500;
                    i++;
                }
                for (; i < s.length() && s.charAt(i) == 'C'; i++)
                    v += 100;
            }
            if (i == s.length()) return v;
            if (s.charAt(i) == 'X' && i <= s.length() - 2 && (s.charAt(i + 1) == 'C' || s.charAt(i + 1) == 'L')) {
                v += (s.charAt(i + 1) == 'C' ? 90 : 40);
                i += 2;
            } else {
                if (s.charAt(i) == 'L') {
                    v += 50;
                    i++;
                }
                for (; i < s.length() && s.charAt(i) == 'X'; i++)
                    v += 10;
            }
            if (i == s.length()) return v;
            if (s.charAt(i) == 'I' && i == s.length() - 2 && s.charAt(i + 1) != 'I')
                v += (s.charAt(i + 1) == 'X' ? 9 : 4);
            else {
                if (s.charAt(i) == 'V') {
                    v += 5;
                    i++;
                }
                v += s.length() - i;
            }
            return v;
        } else return -1;
    }

    public static int parsePerm(String s, int mod) {
        if (s.length() == 0) return 0;
        if (s.length() == 4 && s.indexOf('A') >= 0 && s.indexOf('B') >= 0 && s.indexOf('C') >= 0 && s.indexOf('D') >= 0) {
            int a, b, c, d, v;
            if (mod == R.id.idCDPerm1) {
                a = s.charAt(0) - 'A';
                b = s.charAt(1) - 'A';
                c = s.charAt(2) - 'A';
                d = s.charAt(3) - 'A';
            } else {
                a = s.indexOf('A');
                b = s.indexOf('B');
                c = s.indexOf('C');
                d = s.indexOf('D');
            }
            v = a * 6;
            if (b > a) b--;
            v += b * 2;
            if (c > d) v++;
            v++;
            return v;
        } else return -1;
    }

    public static String toBinary(int x, int d) {
        StringBuilder sb = new StringBuilder();
        for (int i = d - 1; i >= 0; i--)
            sb.append((x >> i) & 1);
        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String toTernary(int x) {
        return String.format("%d%d%d", x / 9, (x / 3) % 3, x % 3);
    }

    public static String toRoman(int x) {
        /* Works for 0 <= x < 40 */
        if (x == 0) return "––––";
        StringBuilder sb = new StringBuilder();
        int des = (x / 10) % 10, jed = x % 10;
        for (int i = 0; i < des; i++)
            sb.append("X");
        switch (jed) {
            case 9:
                sb.append("IX");
                break;
            case 4:
                sb.append("IV");
                break;
            default:
                if (jed >= 5) {
                    sb.append("V");
                    jed -= 5;
                }
                for (int i = 0; i < jed; i++)
                    sb.append("I");
                break;
        }
        return sb.toString();
    }

    public static String toPerm(int x) {
        if (x < 0) return "––––";
        ArrayList<String> arr = new ArrayList<>();
        arr.add("A");
        arr.add("B");
        arr.add("C");
        arr.add("D");
        StringBuilder sb = new StringBuilder();
        sb.append(arr.get(x / 6));
        arr.remove(x / 6);
        x %= 6;
        sb.append(arr.get(x / 2));
        arr.remove(x / 2);
        x %= 2;
        sb.append(arr.get(x));
        arr.remove(x);
        sb.append(arr.get(0));
        return sb.toString();
    }

}
