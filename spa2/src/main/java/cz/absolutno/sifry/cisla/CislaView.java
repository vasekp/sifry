package cz.absolutno.sifry.cisla;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.widget.ButtonView;

public final class CislaView extends ButtonView {

    private int mod;
    private int zaklad;

    public CislaView(Context ctx, AttributeSet as) {
        super(ctx, as);

        setLayout(R.id.idCDCisla, 10, R.id.idCDPrimo1);
    }

    public void setLayout(int mod, int zaklad, int itp) {
        this.mod = mod;
        this.zaklad = zaklad;
        if (mod == R.id.idCDCisla) {
            switch (zaklad) {
                case 2:
                    setSize(1, 2, 2);
                    break;
                case 3:
                    setSize(1, 3, 3);
                    break;
                case 8:
                    setSize(2, 4, 8);
                    break;
                case 10:
                    setSize(4, 3, 10);
                    break;
                case 16:
                    setSize(4, 4, 16);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (mod == R.id.idCDRim) {
            if (itp == R.id.idCDPrimo1 || itp == R.id.idCDPrimo0) {
                setSize(1, 3, 3);
            } else {
                setSize(2, 4, 7);
            }
        } else if (mod == R.id.idCDPerm1 || mod == R.id.idCDPerm2)
            setSize(1, 4, 4);
        else
            throw new IllegalArgumentException();
    }

    @Override
    protected RectF getRectF(int ix) {
        RectF rf = super.getRectF(ix);
        if (mod == R.id.idCDCisla && zaklad == 10 && ix == 9) {
            rf.left += 1;
            rf.right += 1;
        }
        if (mod == R.id.idCDRim && ix >= 4) {
            rf.left += 0.5f;
            rf.right += 0.5f;
        }
        return rf;
    }

    @Override
    protected int getIxTag(int ix) {
        if (mod != R.id.idCDCisla) return 0;
        if (zaklad == 10) {
            if (ix == 9) return 0;
            else return ix + 1;
        } else return ix;
    }

    @Override
    protected String getIxText(int ix) {
        if (mod == R.id.idCDCisla)
            return String.format("%X", getIxTag(ix));
        else if (mod == R.id.idCDRim)
            return rimske[ix];
        else if (mod == R.id.idCDPerm1 || mod == R.id.idCDPerm2)
            return String.valueOf((char) ('A' + ix));
        else
            return "";
    }

    private static final String rimske[] = {"I", "V", "X", "L", "C", "D", "M"};

}